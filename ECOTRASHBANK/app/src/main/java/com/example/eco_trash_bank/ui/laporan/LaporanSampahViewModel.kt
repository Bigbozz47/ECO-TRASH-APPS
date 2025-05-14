package com.example.eco_trash_bank.ui.laporan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanSampahViewModel : ViewModel() {

    private val client = OkHttpClient()

    // LiveData
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    val ringkasanJenis = MutableLiveData<List<RingkasanItem>>()
    val ringkasanBulanan = MutableLiveData<List<BulanItem>>()
    val riwayatNasabah = MutableLiveData<List<RiwayatItem>>()

    fun fetchUserProfile(context: Context) {
        val token = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal koneksi: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                if (!response.isSuccessful) {
                    _error.postValue("Gagal memuat profil")
                    return
                }

                val json = JSONObject(body)
                _username.postValue(json.optString("username", "Pengguna"))
                _role.postValue(json.optString("role", "nasabah"))
            }
        })
    }

    fun fetchRingkasanJenis(context: Context) {
        _loading.postValue(true)

        val token = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/ringkasan-jenis/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _loading.postValue(false)
                _error.postValue("Gagal ambil ringkasan jenis")
            }

            override fun onResponse(call: Call, response: Response) {
                _loading.postValue(false)
                val jsonArray = JSONArray(response.body?.string())
                val data = mutableListOf<RingkasanItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val jenis = obj.optString("jenis", obj.optString("jenis__jenis", ""))
                    val total = obj.getDouble("total")
                    data.add(RingkasanItem(jenis, total))
                }
                ringkasanJenis.postValue(data)
            }
        })
    }

    fun fetchRingkasanBulanan(context: Context) {
        _loading.postValue(true)

        val token = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/ringkasan-bulanan/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _loading.postValue(false)
                _error.postValue("Gagal ambil data bulanan")
            }

            override fun onResponse(call: Call, response: Response) {
                _loading.postValue(false)
                val jsonArray = JSONArray(response.body?.string())
                val data = mutableListOf<BulanItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val date = obj.getString("month")
                    val formatted = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
                    data.add(BulanItem(formatted, obj.getDouble("total")))
                }
                ringkasanBulanan.postValue(data)
            }
        })
    }

    fun fetchRiwayatNasabah(context: Context) {
        _loading.postValue(true)

        val token = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/ringkasan-nasabah/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _loading.postValue(false)
                _error.postValue("Gagal ambil riwayat nasabah: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                _loading.postValue(false)

                val bodyString = response.body?.string()
                if (!response.isSuccessful || bodyString.isNullOrEmpty() || !bodyString.trim().startsWith("[")) {
                    _error.postValue("Gagal memuat riwayat nasabah (Format salah atau token tidak valid)")
                    return
                }

                try {
                    val jsonArray = JSONArray(bodyString)
                    val data = mutableListOf<RiwayatItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        data.add(
                            RiwayatItem(
                                nama = obj.getString("nama"),
                                totalSetoran = obj.getDouble("total_setoran")
                            )
                        )
                    }
                    riwayatNasabah.postValue(data)
                } catch (e: Exception) {
                    _error.postValue("Data tidak valid: ${e.message}")
                }
            }
        })
    }

    fun exportLaporanPDF(context: Context) {
        val token = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/export-laporan/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal mengunduh laporan: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    _error.postValue("Gagal mengunduh laporan dari server")
                    return
                }

                val inputStream = response.body?.byteStream()
                val file = File(context.getExternalFilesDir(null), "laporan_sampah_backend.pdf")
                FileOutputStream(file).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }

                _error.postValue("Laporan berhasil diunduh: ${file.absolutePath}")
            }
        })
    }

    fun saveChartsAsPdf(barChart: View, lineChart: View, context: Context) {
        try {
            val file = File(context.getExternalFilesDir(null), "laporan_grafik.pdf")
            val document = Document()
            val output = FileOutputStream(file)
            PdfWriter.getInstance(document, output)
            document.open()

            document.add(Paragraph("Grafik Ringkasan Sampah\n\n"))

            val barBitmap = getBitmapFromView(barChart)
            val barStream = ByteArrayOutputStream()
            barBitmap.compress(Bitmap.CompressFormat.PNG, 100, barStream)
            val barImage = Image.getInstance(barStream.toByteArray())
            barImage.scaleToFit(500f, 300f)
            document.add(barImage)

            document.add(Paragraph("\n\n"))

            val lineBitmap = getBitmapFromView(lineChart)
            val lineStream = ByteArrayOutputStream()
            lineBitmap.compress(Bitmap.CompressFormat.PNG, 100, lineStream)
            val lineImage = Image.getInstance(lineStream.toByteArray())
            lineImage.scaleToFit(500f, 300f)
            document.add(lineImage)

            document.close()
            _error.postValue("Grafik disimpan di: ${file.absolutePath}")
        } catch (e: Exception) {
            _error.postValue("Gagal menyimpan grafik: ${e.message}")
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        if (view.width == 0 || view.height == 0) {
            throw IllegalStateException("View belum dirender (width atau height 0). Tunggu view selesai load sebelum ekspor PDF.")
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun loadDummyData() {
        val dummyRiwayatNasabah = listOf(
            RiwayatItem("Andi", 12.5), RiwayatItem("Budi", 10.0), RiwayatItem("Citra", 15.3),
            RiwayatItem("Dina", 8.2), RiwayatItem("Eka", 11.7), RiwayatItem("Fajar", 13.4),
            RiwayatItem("Gina", 9.9), RiwayatItem("Hadi", 14.6), RiwayatItem("Indah", 10.1),
            RiwayatItem("Joko", 16.2), RiwayatItem("Kiki", 7.8), RiwayatItem("Lina", 12.0),
            RiwayatItem("Mahmud", 11.3), RiwayatItem("Nina", 13.8), RiwayatItem("Omar", 6.7),
            RiwayatItem("Putri", 17.2), RiwayatItem("Qori", 9.1), RiwayatItem("Rudi", 10.5),
            RiwayatItem("Santi", 14.0), RiwayatItem("Toni", 12.6)
        )
        val dummyRingkasanJenis = listOf(
            RingkasanItem("Plastik", 45.0), RingkasanItem("Kertas", 32.5),
            RingkasanItem("Logam", 28.0), RingkasanItem("Kaca", 15.0), RingkasanItem("Organik", 55.3)
        )
        val dummyRingkasanBulanan = listOf(
            BulanItem("2025-01", 100.0), BulanItem("2025-02", 120.0),
            BulanItem("2025-03", 95.0), BulanItem("2025-04", 130.0),
            BulanItem("2025-05", 110.0), BulanItem("2025-06", 125.5)
        )

        ringkasanJenis.postValue(dummyRingkasanJenis)
        ringkasanBulanan.postValue(dummyRingkasanBulanan)
        riwayatNasabah.postValue(dummyRiwayatNasabah)
    }
}
