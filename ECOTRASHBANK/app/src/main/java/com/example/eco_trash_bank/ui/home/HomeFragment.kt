package com.example.eco_trash_bank.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.eco_trash_bank.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val sharedPref = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Pengguna")
        val role = sharedPref.getString("role", "nasabah")

        binding.userName.text = username
        binding.userStatus.text = "• ${role?.replace('_', ' ')?.replaceFirstChar { it.uppercase() }}"

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
