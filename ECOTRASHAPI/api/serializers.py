from rest_framework import serializers
from django.db.models.functions import TruncMonth
from .models import User, TrashPrice, Transaction, PoinExchange, TransferSaldo, LaporanDownload


class RegisterSerializer(serializers.ModelSerializer):
    """
    Serializer untuk registrasi user baru.
    Menyembunyikan password dan membuat user dengan create_user().
    """
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ['id', 'username', 'password', 'role']
        read_only_fields = ['id']

    def create(self, validated_data):
        return User.objects.create_user(**validated_data)


class UserSerializer(serializers.ModelSerializer):
    """
    Serializer untuk menampilkan info dasar user (nasabah/admin).
    """
    class Meta:
        model = User
        fields = ['id', 'username', 'role', 'poin', 'saldo']
        read_only_fields = ['id', 'poin', 'saldo']


class NasabahListSerializer(serializers.ModelSerializer):
    """
    Serializer khusus endpoint daftar nasabah (role='nasabah').
    """
    class Meta:
        model = User
        fields = ['id', 'username', 'poin', 'saldo']
        read_only_fields = ['id', 'username', 'poin', 'saldo']


class TrashPriceSerializer(serializers.ModelSerializer):
    """
    Serializer untuk CRUD harga sampah.
    """
    class Meta:
        model = TrashPrice
        fields = ['id', 'jenis', 'harga_per_kg', 'is_active', 'tanggal_diperbarui']
        read_only_fields = ['id', 'tanggal_diperbarui']


class ActiveTrashPriceSerializer(serializers.ModelSerializer):
    """
    Serializer untuk menampilkan hanya harga sampah yang aktif.
    """
    class Meta:
        model = TrashPrice
        fields = ['id', 'jenis', 'harga_per_kg']


class TransactionCreateSerializer(serializers.ModelSerializer):
    """
    Serializer untuk pembuatan setoran sampah oleh nasabah.
    Menghitung `nilai_transaksi` dan `poin`.
    """
    class Meta:
        model = Transaction
        fields = ['jenis', 'berat']

    def validate_berat(self, value):
        if value <= 0:
            raise serializers.ValidationError("Berat harus lebih dari 0 kg.")
        return value

    def create(self, validated_data):
        user = self.context['request'].user
        harga = validated_data['jenis'].harga_per_kg
        nilai = validated_data['berat'] * harga
        poin = int(nilai)
        return Transaction.objects.create(
            user=user,
            nilai_transaksi=nilai,
            poin=poin,
            **validated_data
        )


class TransactionDetailSerializer(serializers.ModelSerializer):
    """
    Serializer untuk detail transaksi, termasuk relasi user dan jenis sampah.
    """
    user = UserSerializer(read_only=True)
    jenis = TrashPriceSerializer(read_only=True)
    divalidasi_oleh = UserSerializer(read_only=True)

    class Meta:
        model = Transaction
        fields = ['id', 'user', 'jenis', 'berat', 'nilai_transaksi',
                  'poin', 'status', 'tanggal', 'divalidasi_oleh']
        read_only_fields = fields


class TransactionSummaryJenisSerializer(serializers.Serializer):
    """
    Serializer untuk ringkasan total berat per jenis sampah.
    """
    jenis = serializers.CharField()
    total = serializers.DecimalField(max_digits=12, decimal_places=2)


class TransactionSummaryBulananSerializer(serializers.Serializer):
    """
    Serializer untuk ringkasan total berat transaksi per bulan.
    """
    month = serializers.DateField(format="%Y-%m")
    total = serializers.DecimalField(max_digits=12, decimal_places=2)


class PoinExchangeSerializer(serializers.ModelSerializer):
    """
    Serializer untuk penukaran poin oleh nasabah.
    Mengurangi poin user secara otomatis.
    """
    user = serializers.HiddenField(default=serializers.CurrentUserDefault())

    class Meta:
        model = PoinExchange
        fields = ['id', 'user', 'item', 'poin_dipakai', 'tanggal']
        read_only_fields = ['id', 'tanggal']

    def validate_poin_dipakai(self, value):
        user = self.context['request'].user
        if value <= 0:
            raise serializers.ValidationError("Jumlah poin yang dipakai harus positif.")
        if user.poin < value:
            raise serializers.ValidationError("Poin tidak mencukupi.")
        return value

    def create(self, validated_data):
        user = validated_data['user']
        user.poin -= validated_data['poin_dipakai']
        user.save()
        return super().create(validated_data)


class TransferSaldoSerializer(serializers.ModelSerializer):
    """
    Serializer untuk transfer saldo antar nasabah.
    Mengurangi saldo pengirim dan menambah penerima.
    """
    pengirim = serializers.HiddenField(default=serializers.CurrentUserDefault())

    class Meta:
        model = TransferSaldo
        fields = ['id', 'pengirim', 'penerima', 'jumlah', 'tanggal']
        read_only_fields = ['id', 'tanggal']

    def validate_jumlah(self, value):
        pengirim = self.context['request'].user
        if value <= 0:
            raise serializers.ValidationError("Jumlah yang ditransfer harus positif.")
        if pengirim.saldo < value:
            raise serializers.ValidationError("Saldo tidak mencukupi.")
        return value

    def create(self, validated_data):
        pengirim = validated_data['pengirim']
        penerima = validated_data['penerima']
        jumlah = validated_data['jumlah']
        pengirim.saldo -= jumlah
        penerima.saldo += jumlah
        pengirim.save()
        penerima.save()
        return super().create(validated_data)


class LaporanDownloadSerializer(serializers.ModelSerializer):
    """
    Serializer untuk riwayat unduh laporan PDF oleh admin.
    """
    admin = UserSerializer(read_only=True)

    class Meta:
        model = LaporanDownload
        fields = ['id', 'admin', 'file_path', 'tanggal_unduh']
        read_only_fields = ['id', 'admin', 'file_path', 'tanggal_unduh']