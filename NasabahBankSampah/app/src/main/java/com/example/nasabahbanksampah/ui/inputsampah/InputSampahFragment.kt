package com.example.nasabahbanksampah.ui.inputsampah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentInputSampahBinding

class InputSampahFragment : Fragment() {

    private var _binding: FragmentInputSampahBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JenisSampahAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputSampahBinding.inflate(inflater, container, false)
        val view = binding.root

        val jenisSampahList = listOf("Organik", "Anorganik")

        adapter = JenisSampahAdapter(jenisSampahList)
        binding.rvJenisSampah.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJenisSampah.adapter = adapter

        binding.btnLanjut.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal satu jenis sampah", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_inputSampahFragment_to_inputDetailFragment)
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
