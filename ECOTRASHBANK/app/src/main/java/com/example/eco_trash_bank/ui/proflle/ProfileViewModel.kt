package com.example.eco_trash_bank.ui.proflle

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ProfileViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> get() = _phone

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

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
                try {
                    val bodyString = response.body?.string()
                    if (!response.isSuccessful || bodyString == null) {
                        _error.postValue("Gagal memuat profil")
                        return
                    }

                    val json = JSONObject(bodyString)
                    _username.postValue(json.optString("username", "Pengguna"))
                    _email.postValue(json.optString("email", "-"))
                    _phone.postValue(json.optString("no_hp", "-"))
                    _address.postValue(json.optString("alamat", "-"))
                    _role.postValue(json.optString("role", "nasabah"))
                } catch (e: Exception) {
                    Log.e("PROFILE_PARSING_ERROR", e.message ?: "unknown error")
                    _error.postValue("Data tidak valid dari server")
                }
            }
        })
    }

    fun updateProfile(context: Context, phone: String, address: String) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            _error.postValue("Token tidak ditemukan")
            return
        }

        val json = JSONObject()
        json.put("no_hp", phone)
        json.put("alamat", address)

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/profile/") // Ganti sesuai backend
            .put(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _error.postValue("Gagal mengupdate profil: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    fetchUserProfile(context)
                    _error.postValue("Profil berhasil diperbarui")
                } else {
                    val body = response.body?.string()
                    Log.e("UPDATE_PROFILE_ERROR", body ?: "null")
                    _error.postValue("Gagal memperbarui profil")
                }
            }
        })
    }
}
