package com.example.myapplication // <-- Sesuaikan package name

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val profileName = binding.editTextProfileName.text.toString().trim()
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            // Hapus error sebelumnya
            binding.textFieldProfileName.error = null
            binding.textFieldUsername.error = null
            binding.textFieldPassword.error = null
            binding.textFieldConfirmPassword.error = null

            // --- VALIDASI ---
            var isValid = true

            if (profileName.isEmpty()) {
                binding.textFieldProfileName.error = "Nama Profil tidak boleh kosong"
                isValid = false
            }

            if (username.isEmpty()) {
                binding.textFieldUsername.error = "Username tidak boleh kosong"
                isValid = false
            } else if (username.contains("admin", ignoreCase = true)) { // Tidak boleh mengandung kata admin
                binding.textFieldUsername.error = "Username tidak boleh mengandung kata 'admin'"
                isValid = false
            }
            // Cek keunikan username menggunakan helper
            else if (UserCredentialsManager.isUsernameTaken(requireContext(), username)) {
                binding.textFieldUsername.error = "Username sudah digunakan"
                isValid = false
            }


            if (password.isEmpty()) {
                binding.textFieldPassword.error = "Password tidak boleh kosong"
                isValid = false
            }

            if (confirmPassword.isEmpty()) {
                binding.textFieldConfirmPassword.error = "Konfirmasi Password tidak boleh kosong"
                isValid = false
            } else if (password != confirmPassword) {
                binding.textFieldConfirmPassword.error = "Password tidak cocok"
                isValid = false
            }

            // Jika ada validasi yang gagal, hentikan proses
            if (!isValid) {
                return@setOnClickListener
            }

            // --- LOGIKA REGISTER ---
            val registrationSuccess = UserCredentialsManager.registerUser(
                requireContext(),
                profileName,
                username, // Gunakan username asli saat menyimpan
                password
            )

            if (registrationSuccess) {
                Toast.makeText(requireContext(), "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman Login setelah registrasi berhasil
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                // Ini seharusnya tidak terjadi jika validasi isUsernameTaken di atas benar,
                // tapi sebagai fallback jika ada race condition atau logika terlewat.
                binding.textFieldUsername.error = "Username sudah digunakan atau tidak valid"
                Toast.makeText(requireContext(), "Registrasi Gagal, username mungkin sudah ada.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.buttonCurrentRegister.isClickable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}