package com.example.nasabahbanksampah.ui.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentTransferBerhasilBinding

class TransferBerhasilFragment : Fragment() {
    private lateinit var binding: FragmentTransferBerhasilBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransferBerhasilBinding.inflate(inflater, container, false)

        binding.btnKembali.setOnClickListener {
            findNavController().navigate(R.id.action_transferBerhasilFragment_to_berandaFragment)
        }

        return binding.root
    }
}