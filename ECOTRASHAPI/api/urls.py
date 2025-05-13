from django.urls import path, include, re_path
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenRefreshView
from drf_yasg.views import get_schema_view
from drf_yasg import openapi
from rest_framework import permissions

from .views import (
    RegisterView, LoginView, MeView, ProfileUpdateView,
    SetorSampahView, ValidasiSetoranView,
    HargaViewSet, HargaAktifView, RiwayatTransaksiView,
    RingkasanJenisView, RingkasanBulananView, NasabahListView,
    TukarPoinView, TransferSaldoView, ExportLaporanView,
    DetailNasabahByEmailView, list_nasabah
)

schema_view = get_schema_view(
    openapi.Info(
        title="Eco Trash API",
        default_version='v1',
        description="API dokumentasi interaktif untuk Eco Trash",
        contact=openapi.Contact(email="support@ecotrash.local"),
    ),
    public=True,
    permission_classes=[permissions.AllowAny],
)

router = DefaultRouter()
router.register(r'harga', HargaViewSet, basename='harga')

urlpatterns = [
    # Swagger docs
    re_path(r'^swagger(?P<format>\.json|\.yaml)$', schema_view.without_ui(cache_timeout=0), name='schema-json'),
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),

    # ViewSet router
    path('', include(router.urls)),

    # Auth & Profile
    path('register/', RegisterView.as_view(), name='register'),
    path('login/', LoginView.as_view(), name='login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('me/', MeView.as_view(), name='me'),
    path('profile/', ProfileUpdateView.as_view(), name='profile-update'),

    # Nasabah Actions
    path('setor/', SetorSampahView.as_view(), name='setor_sampah'),
    path('tukar-poin/', TukarPoinView.as_view(), name='tukar_poin'),
    path('transfer/', TransferSaldoView.as_view(), name='transfer_saldo'),

    # Admin Validasi & Laporan
    path('validasi-setor/<int:pk>/', ValidasiSetoranView.as_view(), name='validasi_setor'),
    path('export-laporan/', ExportLaporanView.as_view(), name='export_laporan'),

    # Data Lookup
    path('harga-aktif/', HargaAktifView.as_view(), name='harga_aktif'),
    path('transaksi/', RiwayatTransaksiView.as_view(), name='riwayat_transaksi'),
    path('ringkasan-jenis/', RingkasanJenisView.as_view(), name='ringkasan_jenis'),
    path('ringkasan-bulanan/', RingkasanBulananView.as_view(), name='ringkasan_bulanan'),

    # Nasabah Management
    path('nasabah/', list_nasabah, name='list_nasabah'),
    path('nasabah/<str:email>/', DetailNasabahByEmailView.as_view(), name='detail_nasabah'),
]
