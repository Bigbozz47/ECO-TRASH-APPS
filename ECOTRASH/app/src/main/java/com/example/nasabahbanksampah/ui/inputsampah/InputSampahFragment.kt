package com.example.nasabahbanksampah.ui.inputsampah

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentInputSampahBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class InputSampahFragment : Fragment() {

    private var _binding: FragmentInputSampahBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JenisSampahAdapter
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputSampahBinding.inflate(inflater, container, false)
        val view = binding.root

        val jenisSampahList = listOf("Organik", "Anorganik")

        adapter = JenisSampahAdapter(jenisSampahList)
        binding.rvJenisSampah.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJenisSampah.adapter = adapter

        binding.btnLanjut.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal satu jenis sampah", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_inputSampahFragment_to_inputDetailFragment)
            }
        }

        // Fetch user profile data
        fetchUserProfile()

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
                    binding.userName.text = username
                    binding.userStatus.text = "• ${role.replaceFirstChar { it.uppercase() }}"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}