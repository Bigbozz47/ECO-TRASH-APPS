package com.example.nasabahbanksampah.ui.tukarpoin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.nasabahbanksampah.R
import com.example.nasabahbanksampah.databinding.FragmentNotifikasiBerhasilBinding

class NotifikasiBerhasilFragment(
    private val jenisVoucher: String
) : DialogFragment() {

    private var _binding: FragmentNotifikasiBerhasilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotifikasiBerhasilBinding.inflate(inflater, container, false)

        binding.judulVoucher.text = "+ Voucher $jenisVoucher"
        binding.btnKembali.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}