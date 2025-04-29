package com.example.week6

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    private val _loginErrorMessage = MutableLiveData<String?>()
    val loginErrorMessage: LiveData<String?> get() = _loginErrorMessage

    private val _registerErrorMessage = MutableLiveData<String?>()
    val registerErrorMessage: LiveData<String?> get() = _registerErrorMessage

    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> get() = _navigateToRegister

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> get() = _navigateToLogin

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    // --- Logika Login ---
    fun login(email: String, password: String) {
        _loginErrorMessage.value = null

        if (email.isBlank() || password.isBlank()) {
            _loginErrorMessage.value = "Email dan password tidak boleh kosong."
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginErrorMessage.value = "Format email tidak valid."
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.findUserByEmailAndPassword(email, password)
                if (user != null) {
                    _loginSuccess.value = true
                } else {
                    _loginErrorMessage.value = "Email atau password salah."
                }
            } catch (e: Exception) {
                // Tangani error tak terduga (misalnya masalah database)
                _loginErrorMessage.value = "Terjadi kesalahan saat login."
            }
        }
    }

    // --- Logika Registrasi ---
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        _registerErrorMessage.value = null

        // Validasi
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _registerErrorMessage.value = "Semua field wajib diisi."
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerErrorMessage.value = "Format email tidak valid."
            return
        }
        if (password.length < 3) {
            _registerErrorMessage.value = "Password minimal 3 karakter."
            return
        }
        if (password != confirmPassword) {
            _registerErrorMessage.value = "Konfirmasi password tidak cocok."
            return
        }

        viewModelScope.launch {
            try {
                if (repository.getUserByUsername(username) != null) {
                    _registerErrorMessage.value = "Username sudah digunakan."
                    return@launch
                }
                if (repository.getUserByEmail(email) != null) {
                    _registerErrorMessage.value = "Email sudah terdaftar."
                    return@launch
                }

                val newUser = User(username = username, email = email, password = password)
                repository.insertUser(newUser)

                _navigateToLogin.value = true

            } catch (e: Exception) {
                _registerErrorMessage.value = "Registrasi gagal: ${e.message}"
            }
        }
    }

    // --- Fungsi untuk Memicu Navigasi dari UI ---
    fun onGoToRegisterClicked() {
        _navigateToRegister.value = true
    }

    fun onGoToLoginClicked() {
        _navigateToLogin.value = true
    }

    // --- Fungsi untuk Mereset Event setelah Ditangani oleh UI ---
    fun onNavigationToRegisterDone() {
        _navigateToRegister.value = false
    }

    fun onNavigationToLoginDone() {
        _navigateToLogin.value = false
    }

    fun onLoginSuccessDone() {
        _loginSuccess.value = false
    }
}