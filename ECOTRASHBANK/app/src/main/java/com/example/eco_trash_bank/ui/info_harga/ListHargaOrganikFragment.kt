package com.example.eco_trash_bank.ui.harga

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.R
import com.example.eco_trash_bank.adapter.HargaAdapter
import com.example.eco_trash_bank.databinding.FragmentListHargaOrganikBinding
import com.example.eco_trash_bank.model.HargaSampah
import com.example.eco_trash_bank.viewmodel.InfoHargaViewModel
import okhttp3.*
import java.io.IOException

class ListHargaOrganikFragment : Fragment() {

    private var _binding: FragmentListHargaOrganikBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: InfoHargaViewModel
    private lateinit var adapter: HargaAdapter
    private val client = OkHttpClient()

    override fun onResume() {
        super.onResume()
        // Selalu fetch data terbaru saat fragment kembali aktif
        viewModel.fetchHargaList(requireContext(), "organik")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListHargaOrganikBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoHargaViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        setupUI()

        viewModel.fetchUserProfile(requireContext())
        viewModel.fetchHargaList(requireContext(), "organik")

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HargaAdapter(
            onEditClick = { harga ->
                val action = ListHargaOrganikFragmentDirections
                    .actionListHargaOrganikFragmentToFormHargaFragment(
                        harga,      // hargaData
                        "organik",  // kategori
                        null        // subKategori
                    )
                findNavController().navigate(action)

            }
            ,
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
        viewModel.hargaList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        binding.btnTambahHarga.setOnClickListener {
            val action = ListHargaOrganikFragmentDirections
                .actionListHargaOrganikFragmentToFormHargaFragment(
                    null,       // hargaData (tambah baru)
                    "organik",  // kategori
                    null        // subKategori
                )
            findNavController().navigate(action)


        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterHargaList(newText.orEmpty(), "anorganik")
                return true
            }
        })
    }

    private fun filterSearch() {
        val query = binding.searchView.query?.toString() ?: ""
        adapter.filterAndSort(query, sortBy = "", aktifOnly = false)
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Harga berhasil dihapus", Toast.LENGTH_SHORT).show()
                        viewModel.fetchHargaList(requireContext(), "organik")
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus: ${response.code}", Toast.LENGTH_SHORT).show()
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
