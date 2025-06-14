package com.example.nasabahbanksampah.ui.inputsampah

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentInputDetailBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class InputDetailFragment : Fragment() {

    private var _binding: FragmentInputDetailBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data?.data
            Log.d("UPLOAD_DEBUG", "URI dari galeri: $uri")
            if (uri != null) {
                selectedImageUri = uri
                binding.imgPreview.setImageURI(uri)
                binding.imgPreview.visibility = View.VISIBLE

                Toast.makeText(requireContext(), "Foto berhasil dimuat", Toast.LENGTH_SHORT).show()
                // Tidak langsung navigate
            } else {
                Toast.makeText(requireContext(), "Gagal mendapatkan gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        // Fetch user profile on create
        fetchUserProfile()

        // Handle "Kirim" button click
        binding.btnKirim.setOnClickListener {
            val berat = binding.etBerat.text.toString()
            if (berat.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan berat sampah", Toast.LENGTH_SHORT).show()
            } else if (selectedImageUri == null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Upload Foto")
                    .setMessage("Anda belum mengunggah foto. Ingin lanjut tanpa foto?")
                    .setPositiveButton("Lanjut") { _, _ ->
                        findNavController().navigate(R.id.trackingPengangkutFragment)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                // Jika berat ada dan foto sudah dipilih, langsung navigate
                findNavController().navigate(R.id.trackingPengangkutFragment)
            }
        }

        binding.btnUploadFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
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