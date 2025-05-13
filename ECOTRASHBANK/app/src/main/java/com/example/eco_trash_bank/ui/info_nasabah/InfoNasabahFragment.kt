package com.example.eco_trash_bank.ui.info_nasabah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eco_trash_bank.databinding.FragmentInfoNasabahBinding
import com.example.eco_trash_bank.models.Kontribusi
import com.example.eco_trash_bank.models.Penukaran
import com.example.eco_trash_bank.ui.laporan.InfoNasabahViewModel
import com.example.eco_trash_bank.ui.list_nasabah.KontribusiAdapter
import com.example.eco_trash_bank.ui.list_nasabah.PenukaranAdapter
import okhttp3.OkHttpClient

class InfoNasabahFragment : Fragment() {

    private var _binding: FragmentInfoNasabahBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()
    private lateinit var viewModel: InfoNasabahViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoNasabahBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoNasabahViewModel::class.java]

        val email = arguments?.getString("email")
        if (!email.isNullOrEmpty()) {
            viewModel.fetchUserByEmail(requireContext(), email)
        }

        setupObservers()
        setupRecyclerViews()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.userName.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        viewModel.noHp.observe(viewLifecycleOwner) {
            binding.tvNoHp.text = it
        }

        viewModel.alamat.observe(viewLifecycleOwner) {
            binding.tvAlamat.text = it
        }

        viewModel.kontribusiList.observe(viewLifecycleOwner) {
            binding.rvKontribusi.adapter = KontribusiAdapter(it)
        }

        viewModel.penukaranList.observe(viewLifecycleOwner) {
            binding.rvPenukaran.adapter = PenukaranAdapter(it)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rvKontribusi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPenukaran.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
