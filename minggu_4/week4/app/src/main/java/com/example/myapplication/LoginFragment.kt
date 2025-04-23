package com.example.myapplication

import android.os.Bundle // Import Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            binding.textFieldUsername.error = null
            binding.textFieldPassword.error = null

            if (username.isEmpty()) {
                binding.textFieldUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.textFieldPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            val loginResult = UserCredentialsManager.checkLogin(requireContext(), username, password)

            when (loginResult) {
                LoginResult.SUCCESS_ADMIN -> {
                    Toast.makeText(requireContext(), "Login Admin Berhasil!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_adminHomeFragment)
                }
                LoginResult.SUCCESS_USER -> {
                    val profileName = UserCredentialsManager.getProfileName(requireContext(), username) ?: username
                    Toast.makeText(requireContext(), "Login Berhasil, Selamat Datang $profileName!", Toast.LENGTH_SHORT).show()

                    // ================= PERUBAHAN DI SINI =================
                    // Buat Bundle untuk menampung argumen
                    val bundle = Bundle().apply {
                        // Masukkan profileName dengan key yang sama persis
                        // seperti android:name di <argument> nav_auth.xml
                        putString("profileName", profileName)
                    }
                    // Navigasi menggunakan ID action dan Bundle
                    findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment, bundle)
                    // ====================================================

                }
                LoginResult.INVALID_CREDENTIALS -> {
                    Toast.makeText(requireContext(), "Username atau Password Salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.buttonCurrentLogin.isClickable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}