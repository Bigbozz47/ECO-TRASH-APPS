package com.example.nasabahbanksampah.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentBerandaBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up navigation
        binding.menuTukarPoin.setOnClickListener {
            findNavController().navigate(R.id.tukarPoinFragment)
        }

        binding.menuInformasiHarga.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNav.selectedItemId = R.id.navigation_beranda
            if (findNavController().currentDestination?.id != R.id.informasiHargaFragment) {
                findNavController().navigate(R.id.informasiHargaFragment)
            }
        }

        binding.menuTransfer.setOnClickListener {
            findNavController().navigate(R.id.transferFragment)
        }

        binding.menuInputSampah.setOnClickListener {
            findNavController().navigate(R.id.inputSampahFragment)
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNav.selectedItemId = R.id.navigation_beranda

        requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
            .popBackStack(R.id.navigation_beranda, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}