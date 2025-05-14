package com.example.eco_trash_bank.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eco_trash_bank.model.HargaSampah
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class InfoHargaViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _hargaList = MutableLiveData<List<HargaSampah>>()
    val hargaList: LiveData<List<HargaSampah>> get() = _hargaList

    fun fetchUserProfile(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://192.168.18.10:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal koneksi: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()

                if (!response.isSuccessful || bodyString == null) {
                    _error.postValue("Gagal memuat profil")
                    return
                }

                val json = JSONObject(bodyString)
                _username.postValue(json.optString("username", "Pengguna"))
                _role.postValue(json.optString("role", "nasabah"))
            }
        })
    }

    fun fetchHargaList(context: Context, kategori: String? = null) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://192.168.18.10:8000/api/harga/")
            .addHeader("Authorization", "Bearer ${token.trim()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal memuat data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    _error.postValue("Error: ${response.code}")
                    return
                }

                val responseData = response.body?.string() ?: return
                val jsonArray = JSONArray(responseData)
                val list = mutableListOf<HargaSampah>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val data = HargaSampah(
                        id = obj.getInt("id"),
                        jenis = obj.getString("jenis"),
                        harga_per_kg = obj.getDouble("harga_per_kg"),
                        poin_per_kg = obj.optInt("poin_per_kg", obj.getDouble("harga_per_kg").toInt()),
                        kategori = obj.optString("kategori", "-"),
                        is_active = obj.optBoolean("is_active", true)
                    )
                    if (kategori == null || data.kategori.equals(kategori, ignoreCase = true)) {
                        list.add(data)
                    }
                }

                _hargaList.postValue(list)
            }
        })
    }
}
