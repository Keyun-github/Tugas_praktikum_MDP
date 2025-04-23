package com.example.week5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
// Hapus import Toast jika tidak digunakan lagi untuk success message
// import android.widget.Toast
import androidx.core.content.ContextCompat // Import untuk warna
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.week5.R
import com.example.week5.User
import com.example.week5.UserRole
import com.example.week5.RegisterViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    // Deklarasikan view sebagai properti agar bisa diakses di postDelayed
    private var rootView: View? = null
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var rbCustomer: RadioButton
    private lateinit var tvErrorMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_register, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // Inisialisasi view menggunakan rootView
        val rgRole = view.findViewById<RadioGroup>(R.id.rgRole)
        rbCustomer = view.findViewById(R.id.rbCustomer)
        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLoginPrompt = view.findViewById<TextView>(R.id.tvLoginPrompt)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage) // Inisialisasi tvErrorMessage

        rgRole.setOnCheckedChangeListener { _, checkedId ->
            tilName.hint = if (checkedId == R.id.rbCustomer) "Name" else "Store Name"
        }
        tilName.hint = if (rbCustomer.isChecked) "Name" else "Store Name"

        btnRegister.setOnClickListener {
            val role = if (rbCustomer.isChecked) UserRole.CUSTOMER else UserRole.RETAILER
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            viewModel.register(role, name, email, password, confirmPassword)
        }

        tvLoginPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // Observe registration result
        viewModel.registrationResult.observe(viewLifecycleOwner) { user ->
            // Cek jika user tidak null (artinya registrasi sukses)
            if (user != null) {
                // Tampilkan pesan sukses di TextView
                tvErrorMessage.text = "Registration successful! Please login."
                // Ubah warna teks menjadi hijau (atau warna sukses lainnya)
                context?.let { ctx -> // Pastikan context tidak null
                    tvErrorMessage.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_green_dark))
                }
                tvErrorMessage.visibility = View.VISIBLE

                // Kosongkan field setelah berhasil
                etName.text?.clear()
                etEmail.text?.clear()
                etPassword.text?.clear()
                etConfirmPassword.text?.clear()
                rbCustomer.isChecked = true // Reset radio button ke default

                // Navigasi kembali ke LoginFragment setelah delay 2 detik
                // Gunakan rootView yang sudah disimpan
                rootView?.postDelayed({
                    // Pastikan fragment masih ter-attach sebelum navigasi
                    if (isAdded) {
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        // Reset status ViewModel setelah navigasi
                        viewModel.resetRegistrationStatus()
                    }
                }, 2000) // 2000 milidetik = 2 detik
            }
        }

        // Observe registration errors
        viewModel.registrationError.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                tvErrorMessage.text = errorMessage
                // Set warna teks menjadi merah untuk error
                context?.let { ctx ->
                    tvErrorMessage.setTextColor(ContextCompat.getColor(ctx, android.R.color.holo_red_dark))
                }
                tvErrorMessage.visibility = View.VISIBLE
            } else {
                // Sembunyikan jika tidak ada error (atau jika pesan sukses sudah hilang)
                if (tvErrorMessage.currentTextColor != ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)) {
                    tvErrorMessage.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bersihkan referensi view untuk menghindari memory leak
        rootView = null
    }
}