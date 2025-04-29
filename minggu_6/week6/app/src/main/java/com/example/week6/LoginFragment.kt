package com.example.week6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.week6.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            authViewModel.login(email, password)
        }

        binding.editTextEmail.addTextChangedListener {
            binding.textFieldEmail.error = null
        }
        binding.editTextPassword.addTextChangedListener {
            binding.textFieldPassword.error = null
        }
    }

    private fun setupObservers() {
        authViewModel.loginErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.textFieldEmail.error = errorMessage
            binding.textFieldPassword.error = if (errorMessage != null && errorMessage.contains("password", ignoreCase = true)) errorMessage else null
        }

        authViewModel.loginSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                // TODO: Navigasi ke halaman utama (Home) aplikasi Anda
                authViewModel.onLoginSuccessDone()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}