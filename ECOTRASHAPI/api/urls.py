from django.urls import path, include, re_path
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenRefreshView
from drf_yasg.views import get_schema_view
from drf_yasg import openapi
from rest_framework import permissions

from .views import (
    # Auth & Profile
    RegisterView, LoginView, MeView, ProfileUpdateView,

    # Nasabah
    list_nasabah, DetailNasabahByEmailView, NasabahListView,

    # Transaksi & Validasi
    SetorSampahView, ValidasiSetoranView, DaftarSetoranBelumValidView, RiwayatTransaksiView,update_berat_terbaru, get_berat_terbaru,

    # Harga Sampah
    HargaViewSet, HargaAktifView,

    # Ringkasan & Laporan
    RingkasanJenisView, RingkasanBulananView, RingkasanNasabahView,
    ExportLaporanView, LaporanDownloadListView,

    # Poin & Transfer
    TukarPoinView, TransferSaldoView,
)

# Swagger/OpenAPI documentation config
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

# Router untuk ViewSet
router = DefaultRouter()
router.register(r'harga', HargaViewSet, basename='harga')

urlpatterns = [
    # Swagger docs
    re_path(r'^swagger(?P<format>\\.json|\\.yaml)$', schema_view.without_ui(cache_timeout=0), name='schema-json'),
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),

    # Include router URLs
    path('', include(router.urls)),

    # Auth & Profile
    path('register/', RegisterView.as_view(), name='register'),
    path('login/', LoginView.as_view(), name='login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('me/', MeView.as_view(), name='me'),
    path('profile/', ProfileUpdateView.as_view(), name='profile-update'),

    # Nasabah
    path('nasabah/', list_nasabah, name='list_nasabah_firestore'),
    path('nasabah/<str:email>/', DetailNasabahByEmailView.as_view(), name='detail_nasabah'),
    path('nasabah-list/', NasabahListView.as_view(), name='nasabah_list'),

    # Transaksi & Validasi
    path('setor/', SetorSampahView.as_view(), name='setor_sampah'),
    path('validasi-setor/<int:pk>/', ValidasiSetoranView.as_view(), name='validasi_setoran'),
    path('validasi-setor/', DaftarSetoranBelumValidView.as_view(), name='daftar_validasi'),
    path('transaksi/', RiwayatTransaksiView.as_view(), name='riwayat_transaksi'),
    path('berat-terbaru/', update_berat_terbaru),
    path('get-berat-terbaru/', get_berat_terbaru),

    # Harga Sampah
    path('harga-aktif/', HargaAktifView.as_view(), name='harga_aktif'),

    # Ringkasan & Laporan
    path('ringkasan-jenis/', RingkasanJenisView.as_view(), name='ringkasan_jenis'),
    path('ringkasan-bulanan/', RingkasanBulananView.as_view(), name='ringkasan_bulanan'),
    path('ringkasan-nasabah/', RingkasanNasabahView.as_view(), name='ringkasan_nasabah'),
    path('export-laporan/', ExportLaporanView.as_view(), name='export_laporan'),
    path('laporan-downloads/', LaporanDownloadListView.as_view(), name='laporan_downloads'),

    # Poin & Transfer
    path('tukar-poin/', TukarPoinView.as_view(), name='tukar_poin'),
    path('transfer/', TransferSaldoView.as_view(), name='transfer_saldo'),
]