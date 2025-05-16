package com.example.eco_trash_bank.ui.info_harga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eco_trash_bank.databinding.FragmentInfoHargaBinding
import com.example.eco_trash_bank.viewmodel.InfoHargaViewModel

class InfoHargaFragment : Fragment() {

    private var _binding: FragmentInfoHargaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: InfoHargaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoHargaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoHargaViewModel::class.java]

        observeViewModel()
        setupClickListeners()

        viewModel.fetchUserProfile(requireContext())

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }
    }

    private fun setupClickListeners() {
        binding.cardOrganik.setOnClickListener {
            val action = InfoHargaFragmentDirections.actionInfoHargaFragmentToListHargaOrganikFragment()
            findNavController().navigate(action)
        }

        binding.cardAnorganik.setOnClickListener {
            val action = InfoHargaFragmentDirections
                .actionInfoHargaFragmentToListSubKategoriAnorganikFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
