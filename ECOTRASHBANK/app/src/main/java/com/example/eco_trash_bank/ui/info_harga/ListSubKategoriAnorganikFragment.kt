package com.example.eco_trash_bank.ui.harga

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eco_trash_bank.databinding.FragmentListSubkategoriAnorganikBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ListSubKategoriAnorganikFragment : Fragment() {

    private var _binding: FragmentListSubkategoriAnorganikBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListSubkategoriAnorganikBinding.inflate(inflater, container, false)

        setupSubKategoriButtons()
        fetchUserProfile()

        return binding.root
    }

    private fun setupSubKategoriButtons() {
        binding.cardPlastik.setOnClickListener {
            navigateToList("plastik")
        }
        binding.cardKertas.setOnClickListener {
            navigateToList("kertas")
        }
        binding.cardLogam.setOnClickListener {
            navigateToList("logam")
        }
        binding.cardElektronik.setOnClickListener {
            navigateToList("elektronik")
        }
        binding.cardKacaMinyak.setOnClickListener {
            navigateToList("kaca & minyak")
        }
    }

    private fun navigateToList(subKategori: String) {
        val action = ListSubKategoriAnorganikFragmentDirections
            .actionListSubKategoriAnorganikFragmentToListHargaAnorganikFragment(subKategori)
        findNavController().navigate(action)
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }

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
