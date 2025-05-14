package com.example.eco_trash_bank.ui.info_harga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.adapter.HargaAdapter
import com.example.eco_trash_bank.databinding.FragmentInfoHargaBinding
import com.example.eco_trash_bank.viewmodel.InfoHargaViewModel

class InfoHargaFragment : Fragment() {

    private var _binding: FragmentInfoHargaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HargaAdapter
    private lateinit var viewModel: InfoHargaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoHargaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoHargaViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        setupListeners()

        viewModel.fetchUserProfile(requireContext())
        viewModel.fetchHargaList(requireContext()) // tampilkan semua data default

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HargaAdapter()
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

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.hargaList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun setupListeners() {
        binding.cardOrganik.setOnClickListener {
            viewModel.fetchHargaList(requireContext(), "organik")
        }

        binding.cardAnorganik.setOnClickListener {
            viewModel.fetchHargaList(requireContext(), "anorganik")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
