package com.example.eco_trash_bank.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eco_trash_bank.R
import com.example.eco_trash_bank.databinding.FragmentHomeBinding
import okhttp3.*



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Observasi LiveData
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Panggil API
        viewModel.fetchUserProfile(requireContext())

        binding.btnInfoNasabah.setOnClickListener {
            findNavController().navigate(R.id.navigation_list_nasabah)
        }

        binding.btnLaporanSampah.setOnClickListener {
            findNavController().navigate(R.id.navigation_laporan_sampah)
        }

        binding.btnEditHarga.setOnClickListener {
            findNavController().navigate(R.id.navigation_info_harga)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
