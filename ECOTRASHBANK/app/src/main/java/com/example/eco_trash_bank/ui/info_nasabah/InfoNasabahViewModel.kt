package com.example.eco_trash_bank.ui.laporan

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eco_trash_bank.models.Kontribusi
import com.example.eco_trash_bank.models.Penukaran
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class InfoNasabahViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _noHp = MutableLiveData<String>()
    val noHp: LiveData<String> get() = _noHp

    private val _alamat = MutableLiveData<String>()
    val alamat: LiveData<String> get() = _alamat

    private val _kontribusiList = MutableLiveData<List<Kontribusi>>()
    val kontribusiList: LiveData<List<Kontribusi>> get() = _kontribusiList

    private val _penukaranList = MutableLiveData<List<Penukaran>>()
    val penukaranList: LiveData<List<Penukaran>> get() = _penukaranList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchUserByEmail(context: Context, email: String) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://192.168.18.10:8000/api/nasabah/$email/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal koneksi: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                Log.d("NASABAH_DETAIL", bodyString ?: "null")

                if (!response.isSuccessful || bodyString == null) {
                    _error.postValue("Gagal memuat data nasabah")
                    return
                }

                val json = JSONObject(bodyString)

                _username.postValue(json.optString("username", "Pengguna"))
                _role.postValue(if (json.optBoolean("is_active", false)) "Active" else "Inactive")
                _noHp.postValue(json.optString("no_hp", "-"))
                _alamat.postValue(json.optString("alamat", "-"))

                // Kontribusi
                val kontribusiArr = json.optJSONArray("riwayat_kontribusi") ?: JSONArray()
                val kontribusiList = mutableListOf<Kontribusi>()
                for (i in 0 until kontribusiArr.length()) {
                    val obj = kontribusiArr.getJSONObject(i)
                    val jenis = obj.optString("jenis", "-")
                    val berat = obj.optInt("berat", 0)
                    kontribusiList.add(Kontribusi(jenis, berat))
                }
                _kontribusiList.postValue(kontribusiList)

                // Penukaran
                val penukaranArr = json.optJSONArray("riwayat_penukaran") ?: JSONArray()
                val penukaranList = mutableListOf<Penukaran>()
                for (i in 0 until penukaranArr.length()) {
                    val obj = penukaranArr.getJSONObject(i)
                    val poin = obj.optString("poin", "-")
                    val jenis = obj.optString("jenis", "-")
                    val tanggal = obj.optString("tanggal", "-")
                    penukaranList.add(Penukaran(poin, jenis, tanggal))
                }
                _penukaranList.postValue(penukaranList)
            }
        })
    }
}
