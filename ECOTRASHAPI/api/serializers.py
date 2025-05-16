from rest_framework import serializers
from django.db.models.functions import TruncMonth
from .models import (
    User, TrashPrice, Transaction,
    PoinExchange, TransferSaldo, LaporanDownload, TempBerat
)

# ============================
# USER & REGISTER SERIALIZER
# ============================
class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    no_hp = serializers.CharField(required=True)
    alamat = serializers.CharField(required=True)

    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password', 'role', 'no_hp', 'alamat']
        read_only_fields = ['id']

    def create(self, validated_data):
        password = validated_data.pop('password')
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'role', 'poin', 'saldo', 'no_hp', 'alamat']
        read_only_fields = fields


class NasabahListSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'poin', 'saldo']
        read_only_fields = fields


# ============================
# TRASH PRICE SERIALIZERS
# ============================
class TrashPriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = TrashPrice
        fields = ['id', 'jenis', 'harga_per_kg', 'poin_per_kg', 'kategori', 'sub_kategori', 'is_active', 'tanggal_diperbarui']
        read_only_fields = ['id', 'tanggal_diperbarui']

class ActiveTrashPriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = TrashPrice
        fields = ['id', 'jenis', 'harga_per_kg', 'poin_per_kg', 'kategori', 'sub_kategori']

# ============================
# TEMP BERAT (BERAT SEMENTARA)
# ============================
class TempBeratSerializer(serializers.ModelSerializer):
    nasabah_id = serializers.IntegerField(source='nasabah.id', read_only=True)
    username = serializers.CharField(source='nasabah.username', read_only=True)

    class Meta:
        model = TempBerat
        fields = ['id', 'nasabah_id', 'username', 'berat', 'waktu']
        read_only_fields = ['id', 'waktu', 'nasabah_id', 'username']


class TempBeratCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = TempBerat
        fields = ['nasabah', 'berat']


# ============================
# TRANSACTION SERIALIZERS
# ============================
class TransactionCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Transaction
        fields = ['jenis', 'berat']

    def validate_berat(self, value):
        if value <= 0:
            raise serializers.ValidationError("Berat harus lebih dari 0 kg.")
        return value

    def create(self, validated_data):
        user = self.context['request'].user
        jenis = validated_data['jenis']
        berat = validated_data['berat']
        nilai = berat * jenis.harga_per_kg
        poin = int(berat * jenis.poin_per_kg)

        return Transaction.objects.create(
            user=user,
            nilai_transaksi=nilai,
            poin=poin,
            **validated_data
        )


class TransactionDetailSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    jenis = TrashPriceSerializer(read_only=True)
    divalidasi_oleh = UserSerializer(read_only=True)

    class Meta:
        model = Transaction
        fields = [
            'id', 'user', 'jenis', 'berat', 'nilai_transaksi',
            'poin', 'status', 'tanggal', 'divalidasi_oleh'
        ]
        read_only_fields = fields


class TransactionSummaryJenisSerializer(serializers.Serializer):
    jenis = serializers.CharField()
    total = serializers.DecimalField(max_digits=12, decimal_places=2)


class TransactionSummaryBulananSerializer(serializers.Serializer):
    month = serializers.DateField(format="%Y-%m")
    total = serializers.DecimalField(max_digits=12, decimal_places=2)


# ============================
# SETORAN VALIDASI LIST
# ============================
class SetoranSerializer(serializers.ModelSerializer):
    nama_nasabah = serializers.CharField(source='user.username', read_only=True)
    kategori = serializers.CharField(source='jenis.kategori', read_only=True)
    poin = serializers.SerializerMethodField()
    jumlah_sampah = serializers.DecimalField(source='berat', max_digits=10, decimal_places=2)

    class Meta:
        model = Transaction
        fields = ['id', 'nama_nasabah', 'jumlah_sampah', 'kategori', 'poin']

    def get_poin(self, obj):
        return int(obj.berat * obj.jenis.poin_per_kg)


# ============================
# POIN EXCHANGE
# ============================
class PoinExchangeSerializer(serializers.ModelSerializer):
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


# ============================
# TRANSFER SALDO
# ============================
class TransferSaldoSerializer(serializers.ModelSerializer):
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


# ============================
# LAPORAN DOWNLOAD
# ============================
class LaporanDownloadSerializer(serializers.ModelSerializer):
    admin = UserSerializer(read_only=True)

    class Meta:
        model = LaporanDownload
        fields = ['id', 'admin', 'file_path', 'tanggal_unduh']
        read_only_fields = fields
