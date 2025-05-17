package com.example.eco_trash_bank.ui.validation

import SetoranModel
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ValidationViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _setoranList = MutableLiveData<List<SetoranModel>>()
    val setoranList: LiveData<List<SetoranModel>> get() = _setoranList

    private var token: String? = null

    fun fetchUserProfile(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        token = sharedPref.getString("access_token", null)

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

    fun fetchSetoran() {
        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/validasi-setor/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal mengambil data setoran: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    _error.postValue("Gagal memuat setoran")
                    return
                }

                val jsonArray = JSONArray(body)
                val list = mutableListOf<SetoranModel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        SetoranModel(
                            id = obj.getInt("id"),
                            nama_nasabah = obj.getString("nama_nasabah"),
                            jumlah_sampah = obj.optDouble("jumlah_sampah", 0.0),
                            kategori = obj.getString("kategori"),
                            poin = obj.optInt("poin", 0)
                        )
                    )
                }
                _setoranList.postValue(list)
            }
        })
    }

    fun validasiSetoran(id: Int, context: Context) {
        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/validasi-setor/$id/")
            .post(RequestBody.create(null, ByteArray(0)))
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Validasi gagal: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    fetchSetoran() // Refresh list
                } else {
                    _error.postValue("Gagal validasi setoran")
                }
            }
        })
    }

    fun transferPoin(id: Int, context: Context) {
        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/transfer-saldo/$id/")
            .post(RequestBody.create(null, ByteArray(0)))
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Transfer gagal: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    fetchSetoran()
                } else {
                    _error.postValue("Gagal transfer poin/saldo")
                }
            }
        })
    }
}
