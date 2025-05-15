package com.example.nasabahbanksampah.ui.riwayat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nasabahbanksampah.databinding.FragmentRiwayatBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    private lateinit var adapter: RiwayatAdapter
    private lateinit var allItems: List<RiwayatItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        val view = binding.root

        fetchUserProfile()

        allItems = listOf(
            RiwayatItem("poin", "Penukaran Poin", "Hari Ini", "900"),
            RiwayatItem("poin", "Penukaran Poin", "Senin, 6 Maret 2025", "850"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Februari 2025", "8 kg"),
            RiwayatItem("poin", "Penukaran Poin", "Rabu, 4 Januari 2024", "1000"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Rabu, 4 Januari 2024", "20 kg"),
            RiwayatItem("poin", "Penukaran Poin", "Senin, 5 Desember 2023", "800"),
            RiwayatItem("sampah", "Pengumpulan Sampah", "Senin, 5 Desember 2023", "10 kg")
        )

        adapter = RiwayatAdapter(allItems.toMutableList())
        binding.recyclerRiwayat.layoutManager = LinearLayoutManager(context)
        binding.recyclerRiwayat.adapter = adapter

        binding.btnSemua.setOnClickListener {
            adapter.updateData(allItems.toMutableList())
        }

        binding.btnPoin.setOnClickListener {
            val filtered = allItems.filter { it.jenis == "poin" }
            adapter.updateData(filtered.toMutableList())
        }

        binding.btnSampah.setOnClickListener {
            val filtered = allItems.filter { it.jenis == "sampah" }
            adapter.updateData(filtered.toMutableList())
        }

        return view
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        Log.d("DEBUG_TOKEN", "Token: $token")

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                Log.d("DEBUG_FIRESTORE_BODY", bodyString ?: "null")

                if (!response.isSuccessful || bodyString == null) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val json = JSONObject(bodyString)
                val username = json.optString("username", "Pengguna")
                val role = json.optString("role", "nasabah")

                activity?.runOnUiThread {
                    // Pastikan TextView userName dan userStatus tersedia di layout fragment_riwayat.xml
                    binding.userName?.text = username
                    binding.userStatus?.text = "• ${role.replaceFirstChar { it.uppercase() }}"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}