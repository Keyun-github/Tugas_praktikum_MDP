package com.example.week6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.week6.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            authViewModel.register(username, email, password, confirmPassword)
        }

        binding.editTextUsername.addTextChangedListener { binding.textFieldUsername.error = null }
        binding.editTextEmail.addTextChangedListener { binding.textFieldEmail.error = null }
        binding.editTextPassword.addTextChangedListener { binding.textFieldPassword.error = null }
        binding.editTextConfirmPassword.addTextChangedListener { binding.textFieldConfirmPassword.error = null }
    }

    private fun setupObservers() {
        authViewModel.registerErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                if (errorMessage.contains("Username", ignoreCase = true)) {
                    binding.textFieldUsername.error = errorMessage
                } else if (errorMessage.contains("Email", ignoreCase = true)) {
                    binding.textFieldEmail.error = errorMessage
                }
            } else {
                binding.textFieldUsername.error = null
                binding.textFieldEmail.error = null
                binding.textFieldPassword.error = null
                binding.textFieldConfirmPassword.error = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}