package com.example.eco_trash_bank.ui.proflle

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.eco_trash_bank.databinding.DialogEditProfileBinding
import com.example.eco_trash_bank.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupObservers()
        setupEditButton()

        viewModel.fetchUserProfile(requireContext())
        return binding.root
    }

    private fun setupObservers() {
        viewModel.username.observe(viewLifecycleOwner) {
            _binding?.let { binding ->
                binding.tvName.text = it
                binding.userName.text = it // untuk header
            }
        }

        viewModel.email.observe(viewLifecycleOwner) {
            _binding?.tvEmail?.text = it
        }

        viewModel.phone.observe(viewLifecycleOwner) {
            _binding?.tvPhone?.text = it
        }

        viewModel.address.observe(viewLifecycleOwner) {
            _binding?.tvAddress?.text = it
        }

        viewModel.role.observe(viewLifecycleOwner) {
            _binding?.userStatus?.text = "• ${it.replaceFirstChar { c -> c.uppercase() }}"
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { message ->
                if (isAdded && _binding != null) {
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
                val newName = dialogBinding.etName.text.toString()
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
