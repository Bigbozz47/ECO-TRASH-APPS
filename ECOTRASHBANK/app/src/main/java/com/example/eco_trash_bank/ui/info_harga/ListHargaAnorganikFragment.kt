package com.example.eco_trash_bank.ui.harga

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.adapter.HargaAdapter
import com.example.eco_trash_bank.databinding.FragmentListHargaOrganikBinding
import com.example.eco_trash_bank.model.HargaSampah
import com.example.eco_trash_bank.viewmodel.InfoHargaViewModel
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ListHargaAnorganikFragment : Fragment() {

    private var _binding: FragmentListHargaOrganikBinding? = null
    private val binding get() = _binding!!
    private val args: ListHargaAnorganikFragmentArgs by navArgs()
    private lateinit var viewModel: InfoHargaViewModel
    private lateinit var adapter: HargaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListHargaOrganikBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoHargaViewModel::class.java]

        setupRecyclerView()
        setupUI()
        observeViewModel()
        fetchUserProfile()

        // Hanya fetch harga sekali di sini
        viewModel.fetchHargaList(requireContext(), "anorganik", args.subKategori)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kembali ke fragment
        viewModel.fetchHargaList(requireContext(), "anorganik", args.subKategori)
    }

    private fun setupRecyclerView() {
        adapter = HargaAdapter(
            onEditClick = { harga ->
                val action = ListHargaAnorganikFragmentDirections
                    .actionListHargaAnorganikFragmentToFormHargaFragment(
                        harga, "anorganik", args.subKategori
                    )
                findNavController().navigate(action)
            },
            onDeleteClick = { harga -> showDeleteDialog(harga) }
        )
        binding.recyclerViewHarga.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHarga.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }
        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }
        // Tampilkan data hasil filter/search (bukan submitList dobel)
        viewModel.hargaList.observe(viewLifecycleOwner) { hargaList ->
            adapter.submitList(hargaList)
        }
        viewModel.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        // Tombol tambah harga: navigasi ke form dengan argumen yang benar
        binding.btnTambahHarga.setOnClickListener {
            val action = ListHargaAnorganikFragmentDirections
                .actionListHargaAnorganikFragmentToFormHargaFragment(
                    null, "anorganik", args.subKategori
                )
            findNavController().navigate(action)
        }

        // Search field
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterHargaList(newText.orEmpty(), "anorganik")
                return true
            }
        })
    }

    private fun showDeleteDialog(harga: HargaSampah) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Harga")
            .setMessage("Apakah Anda yakin ingin menghapus harga untuk '${harga.jenis}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteHarga(harga) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteHarga(harga: HargaSampah) {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/harga/${harga.id}/")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Harga berhasil dihapus", Toast.LENGTH_SHORT).show()
                        viewModel.fetchHargaList(requireContext(), "anorganik", args.subKategori)
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null) ?: return

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/me/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
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
