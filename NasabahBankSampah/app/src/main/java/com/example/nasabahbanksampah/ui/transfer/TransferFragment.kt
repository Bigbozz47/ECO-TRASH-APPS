package com.example.nasabahbanksampah.ui.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentTransferBinding
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController

class TransferFragment : Fragment() {
    private lateinit var binding: FragmentTransferBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransferBinding.inflate(inflater, container, false)

        binding.btnLanjutTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_transferFragment_to_transferFormFragment)
        }

        return binding.root
    }
}