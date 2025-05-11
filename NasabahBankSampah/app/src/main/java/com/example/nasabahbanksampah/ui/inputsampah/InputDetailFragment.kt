package com.example.nasabahbanksampah.ui.inputsampah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.databinding.FragmentInputDetailBinding

class InputDetailFragment : Fragment() {

    private var _binding: FragmentInputDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnKirim.setOnClickListener {
            val berat = binding.etBerat.text.toString()
            if (berat.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan berat sampah", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Sampah berhasil dikirim", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}