# 🌿 ECO TRASH APPS

![Platform](https://img.shields.io/badge/platform-Android%20%26%20Django-blue)
![Status](https://img.shields.io/badge/status-Aktif%20%26%20Berkembang-success)
![License](https://img.shields.io/badge/license-MIT-green)
![PRs Welcome](https://img.shields.io/badge/contributions-welcome-brightgreen)

**Eco Trash APPS** adalah aplikasi pengelolaan **bank sampah digital** berbasis Android dengan backend Django REST API. Aplikasi ini mendukung pengelolaan transaksi sampah, pengumpulan poin, validasi setoran, dan pembuatan laporan secara otomatis dan transparan.

---

## 🎯 TUJUAN PROYEK

- Mempermudah proses **pengumpulan dan pengelolaan data bank sampah**.
- Memberikan **insentif berupa poin** kepada masyarakat untuk berkontribusi dalam pengelolaan lingkungan.
- Memastikan **transparansi dan efisiensi** dalam proses validasi dan pelaporan sampah.
- Mendukung pengambilan keputusan berbasis data (melalui grafik dan PDF).

---

## 🧩 FITUR LENGKAP

### 🔐 1. Autentikasi & Role
- Login menggunakan **JWT Token**.
- Role pengguna:
  - `admin`: Mengelola data, memvalidasi, membuat laporan.
  - `nasabah`: Melakukan setoran, melihat riwayat, tukar poin.

### 👤 2. Manajemen Nasabah (Admin)
- Lihat seluruh daftar akun nasabah.
- Fitur detail menampilkan:
  - Nama
  - Alamat
  - Nomor HP
  - Status login aktif
- CRUD nasabah tersedia (opsional via backend).

### ♻️ 3. Kategori Sampah dan Harga
- Dibagi menjadi:
  - **Organik**
  - **Anorganik**, dengan subkategori:
    - Kertas
    - Logam
    - Elektronik
    - Kaca & Minyak
- Fitur:
  - Tambah/Edit/Hapus harga per kategori/subkategori.
  - Menentukan `harga/kg` dan `poin/kg`.
  - Tampilan navigasi terpisah per kategori dan subkategori.

### ⚖️ 4. Setoran Sampah (Nasabah)
- Nasabah melakukan:
  - Pemilihan jenis sampah
  - Input berat (atau otomatis dari load cell)
- Setoran akan memiliki status **pending** menunggu validasi.

### ✅ 5. Validasi Setoran (Admin)
- Admin menerima daftar setoran belum divalidasi.
- Melihat detail, lalu:
  - Menyetujui setoran
  - Menyimpan **berat final**
  - Sistem menghitung otomatis:
    - `nilai_transaksi = berat x harga_per_kg`
    - `poin = berat x poin_per_kg`
- Data transaksi disimpan dan status menjadi `selesai`.

### 🪙 6. Manajemen Poin & Penukaran
- Poin diperoleh dari hasil setoran.
- Nasabah dapat:
  - Melihat total poin
  - Menukar poin menjadi saldo
  - Riwayat penukaran dicatat

### 📈 7. Laporan & Statistik (Admin)
- Ringkasan transaksi per bulan dan jenis sampah.
- Visualisasi:
  - **Grafik batang**: total berat & poin per bulan
  - **Pie chart**: kontribusi tiap jenis sampah
- Dapat **mengunduh laporan otomatis dalam PDF**
  - Menggunakan **ReportLab**
  - File dikirim ke admin

### ⚙️ 8. Integrasi Load Cell (ESP32)
- Timbangan load cell mengirim data ke Django.
- Berat sementara ditampilkan di form setor.
- Berat final bisa dikoreksi saat validasi.

### 🔍 9. Pencarian dan Navigasi UI
- Pencarian harga sampah via SearchView.
- Navigasi fragment per kategori dan subkategori.
- UI terstruktur dengan Navigation Component dan Material Design.

### 🔒 10. Keamanan & Hak Akses
- Setiap endpoint API memiliki permission:
  - `IsAdmin`: hanya untuk admin
  - `IsNasabah`: hanya untuk nasabah
- Endpoint yang sensitif dilindungi oleh JWT.

---

## 🧠 ARSITEKTUR APLIKASI

### 🔧 Backend (Django)
- Django REST Framework
- JWT (SimpleJWT)
- PostgreSQL / SQLite
- ReportLab (PDF)
- Firebase SDK (opsional)

### 📱 Android (Kotlin)
- MVVM Architecture
- Retrofit2 + Gson
- Navigation Component
- ViewModel + LiveData + Repository
- RecyclerView + Material UI

---

## 🔗 ENDPOINT API UTAMA

| Endpoint                  | Method | Akses     | Deskripsi                           |
|--------------------------|--------|-----------|-------------------------------------|
| `/api/register/`         | POST   | Umum      | Registrasi nasabah                  |
| `/api/login/`            | POST   | Umum      | Login dan dapatkan token            |
| `/api/me/`               | GET    | Login     | Dapatkan info akun aktif            |
| `/api/harga/`            | CRUD   | Admin     | Kelola daftar harga                 |
| `/api/setor/`            | POST   | Nasabah   | Buat setoran                        |
| `/api/validasi-setor/`   | POST   | Admin     | Validasi setoran dan hitung poin   |
| `/api/riwayat/`          | GET    | Nasabah   | Lihat riwayat transaksi             |
| `/api/tukar-poin/`       | POST   | Nasabah   | Tukar poin menjadi saldo            |
| `/api/ringkasan-bulanan/`| GET    | Admin     | Ringkasan statistik bulanan         |
| `/api/laporan-pdf/`      | GET    | Admin     | Generate dan download PDF laporan  |

---

## 🧪 DATA DUMMY (Seeder Opsional)
Tersedia file seeder untuk mengisi:
- Data nasabah acak.
- Transaksi sampah random.
- Harga sampah default.

---

## 🛠️ PANDUAN INSTALASI & CARA MENJALANKAN APLIKASI

### 🔧 Backend
```bash
git clone https://github.com/username/eco-trash-bank.git
cd backend/

# Install requirements
pip install -r requirements.txt

# Setup database
python manage.py makemigrations
python manage.py migrate
python manage.py loaddata seeder.json

# Jalankan server
python manage.py runserver
