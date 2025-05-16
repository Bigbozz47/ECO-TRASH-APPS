package com.example.eco_trash_bank.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eco_trash_bank.model.HargaSampah
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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

    private var allHargaList: List<HargaSampah> = emptyList()

    fun fetchUserProfile(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

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
                if (!response.isSuccessful) {
                    _error.postValue("Error: ${response.code}")
                    return
                }

                val responseData = response.body?.string() ?: return

                try {
                    val jsonObject = JSONObject(responseData)
                    _username.postValue(jsonObject.optString("username", "Pengguna"))
                    _role.postValue(jsonObject.optString("role", "nasabah"))
                } catch (e: Exception) {
                    _error.postValue("Gagal parsing profil: ${e.message}")
                }
            }
        })
    }

    fun fetchHargaList(context: Context, kategori: String? = null, subKategori: String? = null) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val urlBuilder = StringBuilder("http://10.0.2.2:8000/api/harga/?")
        if (!kategori.isNullOrEmpty()) urlBuilder.append("kategori=$kategori&")
        if (!subKategori.isNullOrEmpty()) urlBuilder.append("sub_kategori=$subKategori")

        val request = Request.Builder()
            .url(urlBuilder.toString())
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

                try {
                    val jsonObject = JSONObject(responseData)
                    val resultsArray = jsonObject.getJSONArray("results")
                    val list = mutableListOf<HargaSampah>()

                    for (i in 0 until resultsArray.length()) {
                        val obj = resultsArray.getJSONObject(i)
                        val data = HargaSampah(
                            id = obj.getInt("id"),
                            jenis = obj.getString("jenis"),
                            harga_per_kg = obj.getDouble("harga_per_kg"),
                            poin_per_kg = obj.optInt("poin_per_kg", obj.getDouble("harga_per_kg").toInt()),
                            kategori = obj.optString("kategori", "-"),
                            is_active = obj.optBoolean("is_active", true)
                        )
                        list.add(data)
                    }

                    allHargaList = list
                    _hargaList.postValue(list)

                } catch (e: Exception) {
                    _error.postValue("Gagal parsing data: ${e.message}")
                }
            }
        })
    }

    fun filterHargaList(keyword: String, kategori: String? = null) {
        val filtered = allHargaList.filter {
            it.jenis.contains(keyword, ignoreCase = true) &&
                    (kategori == null || it.kategori.equals(kategori, ignoreCase = true))
        }
        _hargaList.postValue(filtered)
    }
}
