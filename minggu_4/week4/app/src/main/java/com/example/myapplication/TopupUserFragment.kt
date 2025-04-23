package com.example.myapplication // <-- Sesuaikan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentTopupUserBinding

class TopupUserFragment : Fragment() {

    private var _binding: FragmentTopupUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopupUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTopupConfirm.setOnClickListener {
            handleTopup()
        }
    }

    private fun handleTopup() {
        val amountString = binding.editTextTopupAmount.text.toString()
        binding.textFieldTopupAmount.error = null // Clear previous error

        if (amountString.isBlank()) {
            binding.textFieldTopupAmount.error = "Amount cannot be empty"
            return
        }

        try {
            val amount = amountString.toLong()
            if (amount < 1000) {
                binding.textFieldTopupAmount.error = "Minimum top up is Rp 1000"
                return
            }

            // Process top up
            val success = UserCredentialsManager.updateWalletBalance(requireContext(), amount)
            if (success) {
                Toast.makeText(requireContext(), "Top up successful!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Go back to Account screen
            } else {
                // This might happen if loggedInUsername is somehow null
                Toast.makeText(requireContext(), "Top up failed. Please try again.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: NumberFormatException) {
            binding.textFieldTopupAmount.error = "Invalid amount format"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}