package com.example.eco_trash_bank.ui.info_harga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.eco_trash_bank.databinding.FragmentInfoHargaBinding
import com.example.eco_trash_bank.ui.laporan.InfoHargaViewModel
import okhttp3.OkHttpClient

class InfoHargaFragment : Fragment() {

    private var _binding: FragmentInfoHargaBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoHargaBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this)[InfoHargaViewModel::class.java]

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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}