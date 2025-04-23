package com.example.week5

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
import com.google.android.material.bottomnavigation.BottomNavigationView // Import BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.example.week5.SessionManager // Import SessionManager

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterPrompt: TextView // TextView or Button used for register navigation
    private lateinit var tvErrorMessage: TextView
    private var bottomNavView: BottomNavigationView? = null // Reference for hiding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout awal
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Inisialisasi Views
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin) // Pastikan ID ini benar di layout Anda
        tvRegisterPrompt = view.findViewById(R.id.tvRegisterPrompt) // ID untuk navigasi ke Register
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_customer) // Dapatkan referensi BottomNav dari Activity

        // Sembunyikan Bottom Nav saat di halaman login
        bottomNavView?.visibility = View.GONE

        // Setup Listeners
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            // Clear session sebelum mencoba login baru
            SessionManager.logout()
            // Panggil fungsi login di ViewModel
            viewModel.login(email, password)
        }

        tvRegisterPrompt.setOnClickListener {
            // Navigasi ke Register Fragment
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Setup Observers
        viewModel.loginResult.observe(viewLifecycleOwner) { user ->
            // Cek jika user tidak null (login berhasil)
            if (user != null) {
                // Simpan email user di SessionManager
                SessionManager.login(user.email)

                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                tvErrorMessage.visibility = View.GONE // Sembunyikan pesan error jika ada

                // Kosongkan field setelah berhasil login
                etEmail.text?.clear()
                etPassword.text?.clear()

                // Navigasi berdasarkan role user
                if (user.role == UserRole.CUSTOMER) {
                    findNavController().navigate(R.id.action_loginFragment_to_customerProductsFragment)
                } else {
                    // Gantilah dengan action navigasi ke Retailer jika sudah dibuat
                    findNavController().navigate(R.id.action_loginFragment_to_retailerProductsFragment)
                }

                // Jangan reset status ViewModel di sini agar navigasi berjalan
                // viewModel.resetLoginStatus()
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { errorMessage ->
            // Tampilkan pesan error jika ada
            if (errorMessage.isNotEmpty()) {
                tvErrorMessage.text = errorMessage
                tvErrorMessage.visibility = View.VISIBLE
                // Pastikan session kosong jika login gagal
                SessionManager.logout()
            } else {
                tvErrorMessage.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset status ViewModel saat fragmen ini kembali aktif
        // (misalnya setelah logout atau menekan tombol back dari register)
        viewModel.resetLoginStatus()

        // Pastikan session kosong saat berada di layar login
        SessionManager.logout()

        // Sembunyikan lagi BottomNavigationView untuk memastikan
        bottomNavView?.visibility = View.GONE
    }
}