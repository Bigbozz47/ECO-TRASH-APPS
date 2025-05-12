package com.example.eco_trash.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eco_trash.R
import com.example.eco_trash.databinding.FragmentHomeBinding

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
        val role = sharedPref.getString("role", "-")

        // Set text to header TextViews
        binding.userName.text = username
        binding.userStatus.text = "● ${role?.replace("_", " ")?.replaceFirstChar { it.uppercase() }}"

        // Navigasi tombol Tukar Poin
        binding.menuTukarPoin.setOnClickListener {
            findNavController().navigate(R.id.tukarPoinFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
