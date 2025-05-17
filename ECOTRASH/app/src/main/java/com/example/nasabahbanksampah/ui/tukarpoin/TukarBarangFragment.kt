package com.example.nasabahbanksampah.ui.tukarpoin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.databinding.FragmentTukarBarangBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TukarBarangFragment : Fragment() {

    private var _binding: FragmentTukarBarangBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTukarBarangBinding.inflate(inflater, container, false)

        binding.btnKebutuhanPokok.setOnClickListener {
            showConfirmationDialog("Kebutuhan Pokok")
        }

        binding.btnKebutuhanRT.setOnClickListener {
            showConfirmationDialog("Kebutuhan Rumah Tangga")
        }

        binding.btnKebutuhanATK.setOnClickListener {
            showConfirmationDialog("Kebutuhan ATK")
        }

        fetchUserProfile()

        return binding.root
    }

    private fun showConfirmationDialog(jenis: String) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi Penukaran")
        builder.setMessage("Apakah Anda yakin ingin menukar poin untuk $jenis?")
        builder.setPositiveButton("Ya") { _, _ ->
            val dialog = NotifikasiBerhasilFragment(jenis)
            dialog.show(parentFragmentManager, "NotifikasiBerhasil")
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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
                    // Pastikan ID ini tersedia di layout FragmentTukarBarangBinding
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