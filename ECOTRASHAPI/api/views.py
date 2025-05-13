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
from rest_framework.decorators import api_view, permission_classes

from .serializers import (
    RegisterSerializer, UserSerializer, NasabahListSerializer,
    TransactionCreateSerializer, TransactionDetailSerializer,
    TrashPriceSerializer, ActiveTrashPriceSerializer,
    TransactionSummaryJenisSerializer, TransactionSummaryBulananSerializer,
    PoinExchangeSerializer, TransferSaldoSerializer, LaporanDownloadSerializer
)
from .models import User, TrashPrice, Transaction, LaporanDownload
from .permissions import IsAdmin, IsNasabah

# === AUTHENTICATION ===
class RegisterView(generics.CreateAPIView):
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(
        operation_summary="Registrasi User",
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
    permission_classes = [permissions.AllowAny]

    @swagger_auto_schema(
        operation_summary="Login User",
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

class DetailNasabahByEmailView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, email):
        user_ref = db.collection('users').document(email)
        user_doc = user_ref.get()
        if user_doc.exists:
            return Response(user_doc.to_dict())
        return Response({'error': 'Nasabah tidak ditemukan di Firestore'}, status=404)

@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated, IsAdmin])
def list_nasabah(request):
    user_docs = db.collection('users').stream()
    nasabah_list = []
    for doc in user_docs:
        data = doc.to_dict()
        if data.get('role') == 'nasabah':
            nasabah_list.append({
                'username': data.get('username'),
                'email': data.get('email'),
                'profile_image_url': data.get('profile_image_url', '')
            })
    return Response(nasabah_list)

# === TRANSAKSI DAN VALIDASI ===
class SetorSampahView(generics.CreateAPIView):
    serializer_class = TransactionCreateSerializer
    permission_classes = [permissions.IsAuthenticated, IsNasabah]

class ValidasiSetoranView(generics.GenericAPIView):
    queryset = Transaction.objects.filter(status='pending')
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

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

# === HARGA SAMPAH ===
class HargaViewSet(viewsets.ModelViewSet):
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
    queryset = TrashPrice.objects.filter(is_active=True)
    serializer_class = ActiveTrashPriceSerializer
    permission_classes = [permissions.AllowAny]

# === RINGKASAN & RIWAYAT ===
class RiwayatTransaksiView(generics.ListAPIView):
    serializer_class = TransactionDetailSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        return Transaction.objects.filter(user=user) if user.role == 'nasabah' else Transaction.objects.all()

class RingkasanJenisView(generics.GenericAPIView):
    serializer_class = TransactionSummaryJenisSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        data = Transaction.objects.filter(status='selesai')            .values('jenis__jenis')            .annotate(total=Sum('berat'))
        return Response(data)

class RingkasanBulananView(generics.GenericAPIView):
    serializer_class = TransactionSummaryBulananSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        qs = Transaction.objects.annotate(month=TruncMonth('tanggal'))
        data = qs.values('month').annotate(total=Sum('berat'))
        return Response(data)

# === LAINNYA ===
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

class ExportLaporanView(generics.GenericAPIView):
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

        db.collection('laporan_downloads').document(str(log.id)).set({
            'admin': request.user.username,
            'file_path': log.file_path,
            'tanggal_unduh': log.tanggal_unduh.isoformat()
        })

        return response
