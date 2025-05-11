package com.example.nasabahbanksampah.ui.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentTransferFormBinding

class TransferFormFragment : Fragment() {
    private lateinit var binding: FragmentTransferFormBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransferFormBinding.inflate(inflater, container, false)

        binding.btnTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_transferFormFragment_to_transferBerhasilFragment)
        }

        return binding.root
    }
}