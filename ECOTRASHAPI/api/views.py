from rest_framework import generics, viewsets, permissions, status
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from django.db.models import Sum
from django.db.models.functions import TruncMonth
from django.http import HttpResponse
from reportlab.pdfgen import canvas

from .models import User, TrashPrice, Transaction, PoinExchange, TransferSaldo, LaporanDownload
from .serializers import (
    RegisterSerializer, UserSerializer, NasabahListSerializer,
    TransactionCreateSerializer, TransactionDetailSerializer,
    TrashPriceSerializer, ActiveTrashPriceSerializer,
    TransactionSummaryJenisSerializer, TransactionSummaryBulananSerializer,
    PoinExchangeSerializer, TransferSaldoSerializer, LaporanDownloadSerializer
)
from .permissions import IsAdmin, IsNasabah

# Auth Views
class RegisterView(generics.CreateAPIView):
    """Endpoint: POST /api/register/ — Registrasi user baru."""
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(
        operation_summary="Registrasi User",
        operation_description="Buat user baru dengan username/password/role.",
        request_body=RegisterSerializer,
        responses={201: UserSerializer, 400: 'Bad Request'}
    )
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

class LoginView(TokenObtainPairView):
    """Endpoint: POST /api/login/ — JWT login."""
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(
        operation_summary="Login User",
        operation_description="Dapatkan access & refresh JWT token.",
        responses={200: openapi.Response('Tokens', schema=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'access': openapi.Schema(type=openapi.TYPE_STRING),
                'refresh': openapi.Schema(type=openapi.TYPE_STRING)
            }
        )), 401: 'Unauthorized'}
    )
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

# Transaction Views
class SetorSampahView(generics.CreateAPIView):
    """Endpoint: POST /api/setor/ — Nasabah setor sampah."""
    serializer_class = TransactionCreateSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

    @swagger_auto_schema(
        operation_summary="Setor Sampah",
        operation_description="Nasabah kirim jenis & berat, server hitung poin & nilai, status pending.",
        request_body=TransactionCreateSerializer,
        responses={201: TransactionDetailSerializer, 400: 'Bad Request', 401: 'Unauthorized'}
    )
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

class ValidasiSetoranView(generics.GenericAPIView):
    """Endpoint: POST /api/validasi-setor/{pk}/ — Admin validasi setoran."""
    queryset = Transaction.objects.filter(status='pending')
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="Validasi Setoran",
        operation_description="Admin setujui setoran, update poin & saldo nasabah.",
        responses={200: TransactionDetailSerializer, 403: 'Forbidden', 404: 'Not Found'}
    )
    def post(self, request, *args, **kwargs):
        trans = self.get_object()
        nilai = trans.berat * trans.jenis.harga_per_kg
        poin = int(nilai)
        trans.status = 'selesai'
        trans.nilai_transaksi = nilai
        trans.poin = poin
        trans.divalidasi_oleh = request.user
        trans.save()
        user = trans.user
        user.poin += poin
        user.saldo += nilai
        user.save()
        return Response(self.get_serializer(trans).data)

# Price Views
class HargaViewSet(viewsets.ModelViewSet):
    """Endpoint: /api/harga/ — CRUD harga sampah (Admin)."""
    queryset = TrashPrice.objects.all()
    serializer_class = TrashPriceSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="List Harga",
        operation_description="List semua data harga sampah (aktif & non-aktif).",
        responses={200: TrashPriceSerializer(many=True)}
    )
    def list(self, request, *args, **kwargs):
        return super().list(request, *args, **kwargs)

    @swagger_auto_schema(
        operation_summary="Buat Harga",
        operation_description="Admin tambah jenis baru dengan harga per kg.",
        request_body=TrashPriceSerializer,
        responses={201: TrashPriceSerializer, 400: 'Bad Request'}
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)

    @swagger_auto_schema(
        operation_summary="Update Harga",
        operation_description="Admin ubah detail harga sampah tertentu.",
        request_body=TrashPriceSerializer,
        responses={200: TrashPriceSerializer, 404: 'Not Found'}
    )
    def update(self, request, *args, **kwargs):
        return super().update(request, *args, **kwargs)

    @swagger_auto_schema(
        operation_summary="Hapus Harga",
        operation_description="Admin hapus data harga sampah.",
        responses={204: 'No Content', 404: 'Not Found'}
    )
    def destroy(self, request, *args, **kwargs):
        return super().destroy(request, *args, **kwargs)

class HargaAktifView(generics.ListAPIView):
    """Endpoint: GET /api/harga-aktif/ — List harga aktif."""
    queryset = TrashPrice.objects.filter(is_active=True)
    serializer_class = ActiveTrashPriceSerializer
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(
        operation_summary="Harga Aktif",
        operation_description="Tampilkan hanya harga sampah yang aktif.",
        responses={200: ActiveTrashPriceSerializer(many=True)}
    )
    def get(self, request, *args, **kwargs):
        return super().get(request, *args, **kwargs)

# Transaction History & Summary
class RiwayatTransaksiView(generics.ListAPIView):
    """Endpoint: GET /api/transaksi/ — Riwayat transaksi."""
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated]

    @swagger_auto_schema(
        operation_summary="Riwayat Transaksi",
        operation_description="List riwayat transaksi untuk nasabah atau semua untuk admin.",
        responses={200: TransactionDetailSerializer(many=True)}
    )
    def get_queryset(self):
        user = self.request.user
        qs = Transaction.objects.filter(user=user) if user.role=='nasabah' else Transaction.objects.all()
        return qs

class RingkasanJenisView(generics.GenericAPIView):
    """Endpoint: GET /api/ringkasan-jenis/ — Statistik per jenis."""
    serializer_class = TransactionSummaryJenisSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="Ringkasan per Jenis",
        operation_description="Total berat transaksi seluru jenis (status selesai).",
        responses={200: TransactionSummaryJenisSerializer(many=True)}
    )
    def get(self, request, *args, **kwargs):
        data = Transaction.objects.filter(status='selesai')\
            .values(jenis=openapi.Schema(type=openapi.TYPE_STRING))\
            .annotate(total=Sum('berat'))
        serializer = self.get_serializer(data, many=True)
        return Response(serializer.data)

class RingkasanBulananView(generics.GenericAPIView):
    """Endpoint: GET /api/ringkasan-bulanan/ — Statistik bulanan."""
    serializer_class = TransactionSummaryBulananSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="Ringkasan Bulanan",
        operation_description="Total berat transaksi per bulan.",
        responses={200: TransactionSummaryBulananSerializer(many=True)}
    )
    def get(self, request, *args, **kwargs):
        qs = Transaction.objects.annotate(month=TruncMonth('tanggal'))
        data = qs.values(month=openapi.Schema(type=openapi.FORMAT_DATE))\
            .annotate(total=Sum('berat'))
        serializer = self.get_serializer(data, many=True)
        return Response(serializer.data)

# Nasabah List
class NasabahListView(generics.ListAPIView):
    """Endpoint: GET /api/nasabah/ — List semua nasabah."""
    serializer_class = NasabahListSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="Daftar Nasabah",
        operation_description="List semua user dengan role nasabah.",
        responses={200: NasabahListSerializer(many=True)}
    )
    def get(self, request, *args, **kwargs):
        return super().get(request, *args, **kwargs)

# Poin & Saldo Transactions
class TukarPoinView(generics.CreateAPIView):
    """Endpoint: POST /api/tukar-poin/ — Nasabah tukar poin."""
    serializer_class = PoinExchangeSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

    @swagger_auto_schema(
        operation_summary="Tukar Poin",
        operation_description="Nasabah tukar poin ke item tertentu.",
        request_body=PoinExchangeSerializer,
        responses={201: PoinExchangeSerializer, 400: 'Bad Request'}
    )
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

class TransferSaldoView(generics.CreateAPIView):
    """Endpoint: POST /api/transfer/ — Nasabah transfer saldo."""
    serializer_class = TransferSaldoSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

    @swagger_auto_schema(
        operation_summary="Transfer Saldo",
        operation_description="Nasabah transfer saldo ke nasabah lain.",
        request_body=TransferSaldoSerializer,
        responses={201: TransferSaldoSerializer, 400: 'Bad Request'}
    )
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

# Laporan PDF
class ExportLaporanView(generics.GenericAPIView):
    """Endpoint: GET /api/export-laporan/ — Admin download laporan PDF."""
    serializer_class = LaporanDownloadSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(
        operation_summary="Export Laporan",
        operation_description="Admin mengunduh laporan transaksi dalam format PDF.",
        responses={200: 'PDF file', 403: 'Forbidden'}
    )
    def get(self, request, *args, **kwargs):
        response = HttpResponse(content_type='application/pdf')
        response['Content-Disposition'] = 'attachment; filename="laporan.pdf"'
        p = canvas.Canvas(response)
        p.drawString(100, 800, "Laporan Transaksi Eco Trash")
        p.showPage()
        p.save()
        LaporanDownload.objects.create(admin=request.user, file_path='laporan.pdf')
        return response