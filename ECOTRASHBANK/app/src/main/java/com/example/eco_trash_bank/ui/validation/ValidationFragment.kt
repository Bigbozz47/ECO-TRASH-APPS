package com.example.eco_trash_bank.ui.validation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.databinding.FragmentValidationBinding
import com.example.eco_trash_bank.ui.validation.adapter.ValidasiAdapter
import okhttp3.OkHttpClient

class ValidationFragment : Fragment() {

    private var _binding: FragmentValidationBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private lateinit var viewModel: ValidationViewModel
    private lateinit var adapter: ValidasiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentValidationBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ValidationViewModel::class.java]

        setupObservers()
        setupRecyclerView()

        // Ambil data user dan daftar setoran
        viewModel.fetchUserProfile(requireContext())
        viewModel.fetchSetoran()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        viewModel.setoranList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ValidasiAdapter(
            list = listOf(),
            onValidasi = { setoran ->
                viewModel.validasiSetoran(setoran.id, requireContext())
            },
            onTransfer = { setoran ->
                viewModel.transferPoin(setoran.id, requireContext())
            }
        )
        binding.recyclerValidationList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerValidationList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
