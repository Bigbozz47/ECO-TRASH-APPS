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
from rest_framework.decorators import api_view, permission_classes
from ecotrash.firebase import db

from .serializers import (
    RegisterSerializer, UserSerializer, NasabahListSerializer,
    TransactionCreateSerializer, TransactionDetailSerializer,
    TrashPriceSerializer, ActiveTrashPriceSerializer,
    TransactionSummaryJenisSerializer, TransactionSummaryBulananSerializer,
    PoinExchangeSerializer, TransferSaldoSerializer, LaporanDownloadSerializer,
    SetoranSerializer
)
from .models import User, TrashPrice, Transaction, LaporanDownload
from .permissions import IsAdmin, IsNasabah

# === AUTHENTICATION ===
class RegisterView(generics.CreateAPIView):
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(operation_summary="Registrasi User", request_body=RegisterSerializer)
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
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(operation_summary="Login User")
    def post(self, request, *args, **kwargs):
        return super().post(request, *args, **kwargs)

class MeView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        email = request.user.email
        user_doc = db.collection('users').document(email).get()
        if user_doc.exists:
            return Response(user_doc.to_dict())
        return Response({'error': 'User not found in Firestore'}, status=404)

class ProfileUpdateView(APIView):
    permission_classes = [permissions.IsAuthenticated]

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

# === NASABAH ===
class DetailNasabahByEmailView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, email):
        doc = db.collection('users').document(email).get()
        if doc.exists:
            return Response(doc.to_dict())
        return Response({'error': 'Nasabah tidak ditemukan'}, status=404)

@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated, IsAdmin])
def list_nasabah(request):
    user_docs = db.collection('users').stream()
    data = [doc.to_dict() for doc in user_docs if doc.to_dict().get('role') == 'nasabah']
    return Response(data)

# === TRANSAKSI ===
class SetorSampahView(generics.CreateAPIView):
    serializer_class = TransactionCreateSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

class DaftarSetoranBelumValidView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request):
        setoran = Transaction.objects.filter(status='pending')
        serializer = SetoranSerializer(setoran, many=True)
        return Response(serializer.data)

class ValidasiSetoranView(generics.RetrieveUpdateAPIView):
    queryset = Transaction.objects.filter(status='pending')
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def post(self, request, *args, **kwargs):
        trans = self.get_object()
        nilai = trans.berat * trans.jenis.harga_per_kg
        poin = int(trans.berat * trans.jenis.poin_per_kg)
        trans.status = 'selesai'
        trans.nilai_transaksi = nilai
        trans.poin = poin
        trans.divalidasi_oleh = request.user
        trans.save()

        user = trans.user
        user.poin += poin
        user.saldo += nilai
        user.save()

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

class RiwayatTransaksiView(generics.ListAPIView):
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        return Transaction.objects.filter(user=user) if user.role == 'nasabah' else Transaction.objects.all()

# === RINGKASAN ===
class RingkasanJenisView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request):
        data = Transaction.objects.filter(status='selesai') \
            .values('jenis__jenis') \
            .annotate(total=Sum('berat'))
        return Response(data)

class RingkasanBulananView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request):
        data = Transaction.objects.annotate(month=TruncMonth('tanggal')) \
            .values('month') \
            .annotate(total=Sum('berat'))
        return Response(data)

class RingkasanNasabahView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request):
        users = User.objects.filter(role='nasabah').annotate(total_setoran=Sum('transaksi__berat'))
        hasil = [{'nama': u.username, 'total_setoran': u.total_setoran or 0} for u in users]
        return Response(hasil)

# === CRUD HARGA ===
class HargaViewSet(viewsets.ModelViewSet):
    queryset = TrashPrice.objects.all()
    serializer_class = TrashPriceSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def perform_create(self, serializer):
        instance = serializer.save()
        db.collection('trash_prices').document(str(instance.id)).set({
            'jenis': instance.jenis,
            'harga_per_kg': float(instance.harga_per_kg),
            'poin_per_kg': instance.poin_per_kg,
            'kategori': instance.kategori,
            'is_active': instance.is_active,
            'tanggal_diperbarui': instance.tanggal_diperbarui.isoformat(),
        })

    def perform_update(self, serializer):
        instance = serializer.save()
        db.collection('trash_prices').document(str(instance.id)).update({
            'jenis': instance.jenis,
            'harga_per_kg': float(instance.harga_per_kg),
            'poin_per_kg': instance.poin_per_kg,
            'kategori': instance.kategori,
            'is_active': instance.is_active,
            'tanggal_diperbarui': instance.tanggal_diperbarui.isoformat(),
        })

    def perform_destroy(self, instance):
        db.collection('trash_prices').document(str(instance.id)).delete()
        instance.delete()

class HargaAktifView(generics.ListAPIView):
    queryset = TrashPrice.objects.filter(is_active=True)
    serializer_class = ActiveTrashPriceSerializer
    permission_classes = [permissions.AllowAny]

# === POIN, SALDO, LAPORAN ===
class NasabahListView(generics.ListAPIView):
    serializer_class = NasabahListSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get_queryset(self):
        return User.objects.filter(role='nasabah')

class TukarPoinView(generics.CreateAPIView):
    serializer_class = PoinExchangeSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

class TransferSaldoView(generics.CreateAPIView):
    serializer_class = TransferSaldoSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

class ExportLaporanView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request):
        response = HttpResponse(content_type='application/pdf')
        response['Content-Disposition'] = 'attachment; filename="laporan.pdf"'

        p = canvas.Canvas(response)
        p.drawString(100, 800, "Laporan Transaksi Eco Trash")
        p.showPage()
        p.save()

        log = LaporanDownload.objects.create(admin=request.user, file_path='laporan.pdf')
        db.collection('laporan_downloads').document(str(log.id)).set({
            'admin': request.user.username,
            'file_path': log.file_path,
            'tanggal_unduh': log.tanggal_unduh.isoformat()
        })

        return response

class LaporanDownloadListView(generics.ListAPIView):
    queryset = LaporanDownload.objects.all().order_by('-tanggal_unduh')
    serializer_class = LaporanDownloadSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]
