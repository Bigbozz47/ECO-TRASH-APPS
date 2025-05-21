package com.example.eco_trash_bank.ui.harga

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.eco_trash_bank.databinding.FragmentFormHargaBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FormHargaFragment : Fragment() {

    private var _binding: FragmentFormHargaBinding? = null
    private val binding get() = _binding!!
    private val args: FormHargaFragmentArgs by navArgs()

    private val client = OkHttpClient()
    private var isEditMode = false
    private var hargaId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormHargaBinding.inflate(inflater, container, false)

        setupTitle()
        setupFormIfEdit()
        setupSaveButton()
        fetchUserProfile()

        return binding.root
    }

    private fun setupTitle() {
        val kategori = args.kategori
        val subKategori = args.subKategori

        binding.title.text = when {
            kategori.equals("organik", ignoreCase = true) -> "Form Harga Sampah Organik"
            kategori.equals("anorganik", ignoreCase = true) && !subKategori.isNullOrBlank() -> "Form Harga Sampah Anorganik (${subKategori})"
            kategori.equals("anorganik", ignoreCase = true) -> "Form Harga Sampah Anorganik"
            else -> "Form Harga Sampah"
        }
    }

    private fun setupFormIfEdit() {
        val data = args.hargaData
        if (data != null) {
            isEditMode = true
            hargaId = data.id
            binding.etJenis.setText(data.jenis)
            binding.etHarga.setText(data.harga_per_kg.toString())
            binding.etPoin.setText(data.poin_per_kg.toString())
            binding.btnSimpan.text = "Perbarui"
        } else {
            binding.etJenis.setText("")
            binding.etHarga.setText("")
            binding.etPoin.setText("")
            binding.btnSimpan.text = "Simpan"
        }
    }

    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            val jenis = binding.etJenis.text.toString().trim()
            val harga = binding.etHarga.text.toString().toDoubleOrNull()
            val poin = binding.etPoin.text.toString().toIntOrNull()
            val kategori = args.kategori
            val subKategori = args.subKategori ?: ""

            if (jenis.isEmpty() || harga == null || poin == null) {
                Toast.makeText(requireContext(), "Mohon isi semua field dengan benar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("jenis", jenis)
                put("harga_per_kg", harga)
                put("poin_per_kg", poin)
                put("kategori", kategori)
                put("sub_kategori", subKategori)
                put("is_active", true)
            }

            val token = requireContext()
                .getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
                .getString("access_token", null)

            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mediaType = "application/json".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = if (isEditMode) {
                Request.Builder()
                    .url("http://10.0.2.2:8000/api/harga/$hargaId/")
                    .put(body)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                Request.Builder()
                    .url("http://10.0.2.2:8000/api/harga/")
                    .post(body)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal mengirim data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    requireActivity().runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                if (isEditMode) "Berhasil memperbarui harga" else "Berhasil menambahkan harga",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menyimpan: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val json = JSONObject(responseBody)
                    val username = json.optString("username", "Pengguna")
                    val role = json.optString("role", "admin")

                    requireActivity().runOnUiThread {
                        binding.userName.text = username
                        binding.userStatus.text = "• ${role.replaceFirstChar { it.uppercase() }}"
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
