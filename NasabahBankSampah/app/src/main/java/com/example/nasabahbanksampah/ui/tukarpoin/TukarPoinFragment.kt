package com.example.nasabahbanksampah.ui.tukarpoin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentTukarPoinBinding

class TukarPoinFragment : Fragment() {

    private var _binding: FragmentTukarPoinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTukarPoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuTabungan.setOnClickListener {
            findNavController().navigate(R.id.tukarTabunganFragment)
        }

        binding.menuBarang.setOnClickListener {
            findNavController().navigate(R.id.tukarBarangFragment)
        }

        binding.btnKembali.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}