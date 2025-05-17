from django.core.management.base import BaseCommand
from api.models import User, TrashPrice, Transaction
from django.utils import timezone
import random

class Command(BaseCommand):
    help = 'Seed dummy data for nasabah and setoran'

    def handle(self, *args, **kwargs):
        # Buat akun nasabah
        for i in range(1, 21):
            username = f'nasabah{i}'
            email = f'nasabah{i}@example.com'

            user, created = User.objects.get_or_create(
                username=username,
                defaults={
                    'email': email,
                    'no_hp': f'0812345678{i:02d}',
                    'alamat': f'Jl. Dummy {i}, Padang',
                    'role': 'nasabah',
                    'poin': 0,
                    'saldo': 0
                }
            )
            if created:
                user.set_password('nasabah123')
                user.save()

        # Buat jenis harga default kalau belum ada
        harga_organik, _ = TrashPrice.objects.get_or_create(
            jenis='Sampah Daun',
            kategori='organik',
            defaults={
                'harga_per_kg': 1000,
                'poin_per_kg': 10,
                'is_active': True
            }
        )

        harga_anorganik, _ = TrashPrice.objects.get_or_create(
            jenis='Botol Plastik',
            kategori='anorganik',
            defaults={
                'harga_per_kg': 2000,
                'poin_per_kg': 15,
                'is_active': True
            }
        )

        # Buat 20 transaksi dummy dengan status pending
        for i in range(1, 21):
            nasabah = User.objects.get(username=f'nasabah{i}')
            berat = round(random.uniform(1.0, 10.0), 2)
            kategori = harga_organik if i % 2 == 0 else harga_anorganik

            Transaction.objects.create(
                user=nasabah,
                jenis=kategori,
                berat=berat,
                nilai_transaksi=0,
                poin=0,
                status='pending',
            )

        self.stdout.write(self.style.SUCCESS('✅ 20 data dummy berhasil dibuat'))
