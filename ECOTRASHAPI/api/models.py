# models.py
from django.db import models
from django.contrib.auth.models import AbstractUser, Group, Permission

class User(AbstractUser):
    groups = models.ManyToManyField(
        Group,
        related_name='api_user_groups',
        blank=True,
        help_text='The groups this user belongs to.'
    )
    user_permissions = models.ManyToManyField(
        Permission,
        related_name='api_user_permissions',
        blank=True,
        help_text='Specific permissions for this user.'
    )

    ROLE_CHOICES = (
        ('nasabah', 'Nasabah'),
        ('admin', 'Bank Sampah'),
    )
    role = models.CharField(max_length=10, choices=ROLE_CHOICES, default='nasabah')
    poin = models.IntegerField(default=0)
    saldo = models.DecimalField(default=0, max_digits=12, decimal_places=2)
    no_hp = models.CharField(max_length=20, blank=True, null=True)
    alamat = models.TextField(blank=True, null=True)

    email = models.EmailField(unique=True)
    USERNAME_FIELD = 'username'
    REQUIRED_FIELDS = ['email']

    def __str__(self):
        return f"{self.username} ({self.role})"

class TrashPrice(models.Model):
    jenis = models.CharField(max_length=100)
    harga_per_kg = models.DecimalField(max_digits=10, decimal_places=2)
    is_active = models.BooleanField(default=True)
    tanggal_diperbarui = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.jenis} - Rp{self.harga_per_kg}/kg"

class Transaction(models.Model):
    STATUS_CHOICES = (
        ('pending', 'Pending'),
        ('selesai', 'Selesai'),
    )
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='transaksi')
    jenis = models.ForeignKey(TrashPrice, on_delete=models.PROTECT)
    berat = models.DecimalField(max_digits=6, decimal_places=2)
    poin = models.IntegerField(default=0)
    nilai_transaksi = models.DecimalField(max_digits=12, decimal_places=2, default=0)
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default='pending')
    tanggal = models.DateTimeField(auto_now_add=True)
    divalidasi_oleh = models.ForeignKey(User, null=True, blank=True, on_delete=models.SET_NULL, related_name='validasi_transaksi')

    def __str__(self):
        return f"{self.user.username} - {self.jenis.jenis} - {self.status}"

class PoinExchange(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='penukaran_poin')
    item = models.CharField(max_length=100)
    poin_dipakai = models.IntegerField()
    tanggal = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.username} - {self.item} ({self.poin_dipakai} poin)"

class TransferSaldo(models.Model):
    pengirim = models.ForeignKey(User, on_delete=models.CASCADE, related_name='transfer_keluar')
    penerima = models.ForeignKey(User, on_delete=models.CASCADE, related_name='transfer_masuk')
    jumlah = models.DecimalField(max_digits=12, decimal_places=2)
    tanggal = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.pengirim.username} -> {self.penerima.username} : Rp{self.jumlah}"

class LaporanDownload(models.Model):
    admin = models.ForeignKey(User, on_delete=models.CASCADE, related_name='laporan_unduhan')
    file_path = models.CharField(max_length=255)
    tanggal_unduh = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Laporan oleh {self.admin.username} - {self.tanggal_unduh.strftime('%Y-%m-%d')}"
