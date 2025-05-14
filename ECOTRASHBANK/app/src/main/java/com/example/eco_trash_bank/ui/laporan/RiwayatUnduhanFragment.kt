package com.example.eco_trash_bank.ui.laporan

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.databinding.FragmentRiwayatUnduhanBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RiwayatUnduhanFragment : Fragment() {

    private var _binding: FragmentRiwayatUnduhanBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()
    private val laporanList = mutableListOf<LaporanDownloadItem>()
    private lateinit var adapter: LaporanDownloadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatUnduhanBinding.inflate(inflater, container, false)
        adapter = LaporanDownloadAdapter(laporanList)
        binding.rvLaporan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLaporan.adapter = adapter

        fetchRiwayatUnduhan(requireContext())

        return binding.root
    }

    private fun fetchRiwayatUnduhan(context: Context) {
        val sharedPref = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        val role = sharedPref.getString("role", null)

        // Validasi role admin
        if (role != "admin") {
            Toast.makeText(context, "Akses hanya untuk Admin", Toast.LENGTH_SHORT).show()
            return
        }

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading
        binding.progressLoading.visibility = View.VISIBLE

        val request = Request.Builder()
            .url("http://192.168.18.10:8000/api/laporan-downloads/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    binding.progressLoading.visibility = View.GONE
                    Toast.makeText(context, "Gagal koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val jsonArray = JSONArray(body)

                laporanList.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val adminObj = obj.getJSONObject("admin")

                    laporanList.add(
                        LaporanDownloadItem(
                            id = obj.getInt("id"),
                            admin = AdminInfo(adminObj.getString("username")),
                            file_path = obj.getString("file_path"),
                            tanggal_unduh = obj.getString("tanggal_unduh")
                        )
                    )
                }

                activity?.runOnUiThread {
                    binding.progressLoading.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
