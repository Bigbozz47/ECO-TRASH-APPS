package com.example.eco_trash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.eco_trash.RegisterActivity
import com.example.eco_trash.main.MainActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterNow = findViewById(R.id.tvRegisterNow)

        btnLogin.setOnClickListener {
            performLogin()
        }

        tvRegisterNow.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan Password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/login/") // atau IP lokal jika pakai HP fisik maksudnya ip laptop tapi satu wifie,cek ipconfig di laptop dan lihat ipv4 nya copy dan masukkan ke file network_security_config.xml dan ganti juga disini
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Login gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    try {
                        if (response.isSuccessful && responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)

                            val accessToken = jsonResponse.optString("access", "")
                            val refreshToken = jsonResponse.optString("refresh", "")

                            val userObject = jsonResponse.optJSONObject("user")
                            val usernameFromServer = userObject?.optString("username", "") ?: ""
                            val email = userObject?.optString("email", "") ?: ""
                            val role = userObject?.optString("role", "") ?: ""

                            if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                                // Simpan ke SharedPreferences
                                val sharedPref = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("access_token", accessToken)
                                    putString("refresh_token", refreshToken)
                                    putString("username", usernameFromServer)
                                    putString("email", email)
                                    putString("role", role)
                                    apply()
                                }

                                Toast.makeText(this@LoginActivity, "Login sukses", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Token tidak ditemukan dalam respons", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorMsg = responseBody ?: "Login gagal: Tidak ada respons dari server"
                            Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Kesalahan parsing respons: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}
