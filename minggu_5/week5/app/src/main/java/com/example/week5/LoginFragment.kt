package com.example.week5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.week5.R
import com.example.week5.models.UserRole
import com.example.week5.viewmodels.LoginViewModel
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegisterPrompt = view.findViewById<TextView>(R.id.tvRegisterPrompt)
        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            viewModel.login(email, password)
        }

        tvRegisterPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                tvErrorMessage.visibility = View.GONE // Hide error on success

                if (user.role == UserRole.CUSTOMER) {
                    findNavController().navigate(R.id.action_loginFragment_to_customerProductsFragment)
                } else {
                    findNavController().navigate(R.id.action_loginFragment_to_retailerProductsFragment)
                }

                viewModel.resetLoginStatus()
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                tvErrorMessage.text = errorMessage
                tvErrorMessage.visibility = View.VISIBLE
            } else {
                tvErrorMessage.visibility = View.GONE
            }
        }
    }
}