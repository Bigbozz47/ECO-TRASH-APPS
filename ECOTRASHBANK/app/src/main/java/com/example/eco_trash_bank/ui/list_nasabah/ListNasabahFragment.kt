package com.example.eco_trash_bank.ui.list_nasabah

import ListNasabahAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.databinding.FragmentListNasabahBinding

class ListNasabahFragment : Fragment() {

    private var _binding: FragmentListNasabahBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ListNasabahViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListNasabahBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ListNasabahViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        // Panggil data user & list nasabah
        viewModel.fetchUserProfile(requireContext())
        viewModel.fetchNasabahList(requireContext())

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvNasabah.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        // Observasi username dan role untuk header
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        // Observasi list nasabah
        viewModel.nasabahList.observe(viewLifecycleOwner) { list ->
            binding.rvNasabah.adapter = ListNasabahAdapter(list) { nasabah ->
                val action = ListNasabahFragmentDirections
                    .actionListToInfo(nasabah.email)
                findNavController().navigate(action)
            }
        }

        // Observasi error
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
