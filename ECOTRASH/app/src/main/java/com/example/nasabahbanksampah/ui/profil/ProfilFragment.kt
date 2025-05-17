package com.example.nasabahbanksampah.ui.profil

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nasabahbanksampah.databinding.DialogEditProfileBinding
import com.example.nasabahbanksampah.databinding.FragmentProfilBinding

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfilViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfilViewModel::class.java]

        setupObservers()
        setupEditButton()

        viewModel.fetchUserProfile(requireContext())

        return binding.root
    }

    private fun setupObservers() {
        viewModel.username.observe(viewLifecycleOwner) {
            binding.tvName.text = it
            binding.userName.text = it
        }

        viewModel.email.observe(viewLifecycleOwner) {
            binding.tvEmail.text = it
        }

        viewModel.phone.observe(viewLifecycleOwner) {
            binding.tvPhone.text = it
        }

        viewModel.address.observe(viewLifecycleOwner) {
            binding.tvAddress.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            binding.userStatus.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { message ->
                if (isAdded) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupEditButton() {
        binding.btnEdit.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

        dialogBinding.etName.setText(binding.tvName.text)
        dialogBinding.etPhone.setText(binding.tvPhone.text)
        dialogBinding.etAddress.setText(binding.tvAddress.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Profil")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val newPhone = dialogBinding.etPhone.text.toString()
                val newAddress = dialogBinding.etAddress.text.toString()

                viewModel.updateProfile(
                    requireContext(),
                    phone = newPhone,
                    address = newAddress
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}