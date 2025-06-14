package com.example.eco_trash_bank.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class HomeViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _totalSampah = MutableLiveData<Double>()
    val totalSampah: LiveData<Double> get() = _totalSampah

    fun fetchUserProfile(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        Log.d("DEBUG_TOKEN", "Token: $token")

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
                Log.d("DEBUG_FIRESTORE_BODY", bodyString ?: "null")

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

    fun fetchTotalSampah(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/total-sampah/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal memuat total sampah: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    _error.postValue("Gagal memuat total sampah")
                    return
                }

                val json = JSONObject(body)
                val total = json.optDouble("total_berat", 0.0)
                _totalSampah.postValue(total)
            }
        })
    }
}
