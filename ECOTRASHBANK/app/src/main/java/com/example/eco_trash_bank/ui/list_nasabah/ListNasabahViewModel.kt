package com.example.eco_trash_bank.ui.list_nasabah

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ListNasabahViewModel : ViewModel() {

    private val client = OkHttpClient()

    // LiveData untuk daftar nasabah
    private val _nasabahList = MutableLiveData<List<Nasabah>>()
    val nasabahList: LiveData<List<Nasabah>> get() = _nasabahList

    // LiveData untuk error
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData untuk profil pengguna login
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    fun fetchNasabahList(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/nasabah/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal koneksi: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                if (!response.isSuccessful || bodyString == null) {
                    _error.postValue("Gagal memuat daftar nasabah")
                    return
                }

                try {
                    val jsonArray = JSONArray(bodyString)
                    val result = mutableListOf<Nasabah>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val username = obj.optString("username", "-")
                        val email = obj.optString("email", "-")
                        val profileImage = obj.optString("profile_image_url", "")
                        result.add(Nasabah(username, email, profileImage))
                    }

                    _nasabahList.postValue(result)

                } catch (e: Exception) {
                    _error.postValue("Kesalahan parsing data")
                }
            }
        })
    }

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
}
