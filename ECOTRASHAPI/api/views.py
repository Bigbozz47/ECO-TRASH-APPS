from rest_framework import generics, viewsets, permissions, status
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from django.db.models import Sum
from django.db.models.functions import TruncMonth
from django.http import HttpResponse
from reportlab.pdfgen import canvas
from rest_framework.views import APIView
from ecotrash.firebase import db

from .serializers import (
    RegisterSerializer, UserSerializer, NasabahListSerializer,
    TransactionCreateSerializer, TransactionDetailSerializer,
    TrashPriceSerializer, ActiveTrashPriceSerializer,
    TransactionSummaryJenisSerializer, TransactionSummaryBulananSerializer,
    PoinExchangeSerializer, TransferSaldoSerializer, LaporanDownloadSerializer
)
from .models import User, TrashPrice, Transaction, LaporanDownload
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
        serializer = self.get_serializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            db.collection('users').document(user.email).set({
                'username': user.username,
                'email': user.email,
                'role': user.role,
                'no_hp': user.no_hp,
                'alamat': user.alamat,
                'poin': user.poin,
                'saldo': float(user.saldo),
            })
            return Response(UserSerializer(user).data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

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

class MeView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        email = request.user.email
        user_ref = db.collection('users').document(email)
        user_doc = user_ref.get()
        if user_doc.exists:
            return Response(user_doc.to_dict())
        return Response({'error': 'User not found in Firestore'}, status=404)

class ProfileUpdateView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @swagger_auto_schema(
        operation_summary="Update Profil Pengguna",
        operation_description="Mengupdate informasi user yang sedang login dan sinkronisasi ke Firestore.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=['no_hp', 'alamat'],
            properties={
                'no_hp': openapi.Schema(type=openapi.TYPE_STRING),
                'alamat': openapi.Schema(type=openapi.TYPE_STRING),
            }
        ),
        responses={200: UserSerializer, 400: 'Bad Request'}
    )
    def put(self, request):
        user = request.user
        no_hp = request.data.get('no_hp')
        alamat = request.data.get('alamat')
        if not no_hp or not alamat:
            return Response({'error': 'no_hp dan alamat wajib diisi'}, status=400)
        user.no_hp = no_hp
        user.alamat = alamat
        user.save()
        db.collection('users').document(user.email).update({
            'no_hp': no_hp,
            'alamat': alamat
        })
        return Response(UserSerializer(user).data, status=200)

# Transaction Views
class SetorSampahView(generics.CreateAPIView):
    """Endpoint: POST /api/setor/ — Nasabah setor sampah."""
    serializer_class = TransactionCreateSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

    @swagger_auto_schema(...)
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

class ValidasiSetoranView(generics.GenericAPIView):
    """Endpoint: POST /api/validasi-setor/{pk}/ — Admin validasi setoran."""
    queryset = Transaction.objects.filter(status='pending')
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    @swagger_auto_schema(...)
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

        # Backup ke Firestore
        db.collection('transactions').document(str(trans.id)).set({
            'user': user.username,
            'email': user.email,
            'jenis': trans.jenis.jenis,
            'berat': float(trans.berat),
            'nilai_transaksi': float(trans.nilai_transaksi),
            'poin': trans.poin,
            'status': trans.status,
            'tanggal': trans.tanggal.isoformat(),
            'divalidasi_oleh': request.user.username
        })

        return Response(self.get_serializer(trans).data)

# Harga Sampah Views
class HargaViewSet(viewsets.ModelViewSet):
    """Endpoint: /api/harga/ — CRUD harga sampah (Admin)."""
    queryset = TrashPrice.objects.all()
    serializer_class = TrashPriceSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def perform_create(self, serializer):
        instance = serializer.save()
        db.collection('trash_prices').document(str(instance.id)).set({
            'jenis': instance.jenis,
            'harga_per_kg': float(instance.harga_per_kg),
            'is_active': instance.is_active,
            'tanggal_diperbarui': instance.tanggal_diperbarui.isoformat(),
        })

    def perform_update(self, serializer):
        instance = serializer.save()
        db.collection('trash_prices').document(str(instance.id)).update({
            'jenis': instance.jenis,
            'harga_per_kg': float(instance.harga_per_kg),
            'is_active': instance.is_active,
            'tanggal_diperbarui': instance.tanggal_diperbarui.isoformat(),
        })

    def perform_destroy(self, instance):
        db.collection('trash_prices').document(str(instance.id)).delete()
        instance.delete()


class HargaAktifView(generics.ListAPIView):
    """Endpoint: GET /api/harga-aktif/ — List harga aktif."""
    queryset = TrashPrice.objects.filter(is_active=True)
    serializer_class = ActiveTrashPriceSerializer
    permission_classes = [permissions.AllowAny]

# Riwayat Transaksi dan Statistik
class RiwayatTransaksiView(generics.ListAPIView):
    """Endpoint: GET /api/transaksi/ — Riwayat transaksi."""
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        return Transaction.objects.filter(user=user) if user.role == 'nasabah' else Transaction.objects.all()

class RingkasanJenisView(generics.GenericAPIView):
    """Endpoint: GET /api/ringkasan-jenis/ — Statistik per jenis."""
    serializer_class = TransactionSummaryJenisSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        data = Transaction.objects.filter(status='selesai')\
            .values('jenis__jenis')\
            .annotate(total=Sum('berat'))
        return Response(data)

class RingkasanBulananView(generics.GenericAPIView):
    """Endpoint: GET /api/ringkasan-bulanan/ — Statistik bulanan."""
    serializer_class = TransactionSummaryBulananSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        qs = Transaction.objects.annotate(month=TruncMonth('tanggal'))
        data = qs.values('month').annotate(total=Sum('berat'))
        return Response(data)

# Nasabah
class NasabahListView(generics.ListAPIView):
    """Endpoint: GET /api/nasabah/ — List semua nasabah."""
    serializer_class = NasabahListSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get_queryset(self):
        return User.objects.filter(role='nasabah')

# Poin dan Saldo
class TukarPoinView(generics.CreateAPIView):
    """Endpoint: POST /api/tukar-poin/ — Nasabah tukar poin."""
    serializer_class = PoinExchangeSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

class TransferSaldoView(generics.CreateAPIView):
    """Endpoint: POST /api/transfer/ — Nasabah transfer saldo."""
    serializer_class = TransferSaldoSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

# Laporan
class ExportLaporanView(generics.GenericAPIView):
    """Endpoint: GET /api/export-laporan/ — Admin download laporan PDF."""
    serializer_class = LaporanDownloadSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        response = HttpResponse(content_type='application/pdf')
        response['Content-Disposition'] = 'attachment; filename="laporan.pdf"'
        p = canvas.Canvas(response)
        p.drawString(100, 800, "Laporan Transaksi Eco Trash")
        p.showPage()
        p.save()
        log = LaporanDownload.objects.create(admin=request.user, file_path='laporan.pdf')

        # Simpan log laporan ke Firestore
        db.collection('laporan_downloads').document(str(log.id)).set({
            'admin': request.user.username,
            'file_path': log.file_path,
            'tanggal_unduh': log.tanggal_unduh.isoformat()
        })

        return response

