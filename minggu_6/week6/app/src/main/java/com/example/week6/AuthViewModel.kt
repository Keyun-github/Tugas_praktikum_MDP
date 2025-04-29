package com.example.week6 // Pastikan package ini benar

import android.app.Application
import android.util.Patterns // Untuk validasi email
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Gunakan AndroidViewModel untuk mendapatkan Application context
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    // Gunakan backing property untuk enkapsulasi MutableLiveData
    private val _loginErrorMessage = MutableLiveData<String?>()
    val loginErrorMessage: LiveData<String?> get() = _loginErrorMessage // Hanya ekspos LiveData yg immutable

    private val _registerErrorMessage = MutableLiveData<String?>()
    val registerErrorMessage: LiveData<String?> get() = _registerErrorMessage

    // LiveData untuk trigger navigasi (sebagai event) - Anda mungkin tidak perlu _navigateToRegister jika hanya via BottomNav
    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> get() = _navigateToRegister

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> get() = _navigateToLogin

    // LiveData untuk menandakan login sukses (sebagai event)
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    // --- LiveData BARU untuk user ID dan event logout ---
    private val _loggedInUserId = MutableLiveData<Int?>()
    val loggedInUserId: LiveData<Int?> get() = _loggedInUserId // ID user yang sedang login

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> get() = _logoutEvent // Event untuk trigger navigasi ke login setelah logout


    // --- Inisialisasi ---
    init {
        // Dapatkan instance DAO dari AppDatabase
        val userDao = AppDatabase.getDatabase(application).userDao()
        // Inisialisasi repository
        repository = UserRepository(userDao)
        // Opsional: Anda bisa mencoba memuat ID user terakhir dari SharedPreferences di sini
        // jika Anda ingin mempertahankan sesi login antar penutupan aplikasi.
        // Contoh: _loggedInUserId.value = loadUserIdFromPreferences(application)
    }

    // --- Logika Login ---
    fun login(email: String, password: String) {
        // Reset pesan error sebelumnya
        _loginErrorMessage.value = null

        // Validasi dasar
        if (email.isBlank() || password.isBlank()) {
            _loginErrorMessage.value = "Email dan password tidak boleh kosong."
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginErrorMessage.value = "Format email tidak valid."
            return
        }

        // Jalankan operasi database di coroutine scope milik ViewModel
        viewModelScope.launch {
            try {
                val user = repository.findUserByEmailAndPassword(email, password)
                if (user != null) {
                    _loggedInUserId.value = user.id // Simpan ID user yang berhasil login
                    _loginSuccess.value = true // Kirim event sukses
                    // Opsional: Simpan ID ke SharedPreferences di sini untuk persistensi sesi
                    // saveUserIdToPreferences(application, user.id)
                } else {
                    _loginErrorMessage.value = "Email atau password salah."
                    _loggedInUserId.value = null // Pastikan ID user null jika login gagal
                }
            } catch (e: Exception) {
                // Tangani error tak terduga (misalnya masalah database)
                _loginErrorMessage.value = "Terjadi kesalahan saat login."
                _loggedInUserId.value = null // Pastikan ID user null jika error
                // Sebaiknya log exception e di aplikasi nyata
            }
        }
    }

    // --- Logika Registrasi ---
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Reset pesan error sebelumnya
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
                // Cek keunikan username dan email sebelum insert
                if (repository.getUserByUsername(username) != null) {
                    _registerErrorMessage.value = "Username sudah digunakan."
                    return@launch // Keluar dari coroutine jika username sudah ada
                }
                if (repository.getUserByEmail(email) != null) {
                    _registerErrorMessage.value = "Email sudah terdaftar."
                    return@launch // Keluar dari coroutine jika email sudah ada
                }

                // Buat user baru (password plain text!)
                val newUser = User(username = username, email = email, password = password)
                repository.insertUser(newUser)

                // Jika sukses, trigger navigasi kembali ke Login
                _navigateToLogin.value = true

            } catch (e: Exception) {
                // Tangani error (misalnya, ada race condition atau error DB lain)
                _registerErrorMessage.value = "Registrasi gagal: ${e.message}"
                // Sebaiknya log exception e
            }
        }
    }

    // --- Fungsi untuk Logout ---
    fun logout() {
        _loggedInUserId.value = null // Hapus ID user dari LiveData
        _logoutEvent.value = true // Trigger event logout
        // Opsional: Hapus ID user dari SharedPreferences di sini
        // clearUserIdFromPreferences(application)
    }

    // --- Fungsi untuk Mereset Event setelah Ditangani oleh UI ---
    // Ini penting agar event tidak terpicu lagi saat rotasi layar/perubahan konfigurasi

    fun onLogoutEventDone() {
        _logoutEvent.value = false
    }

    fun onLoginSuccessDone() {
        _loginSuccess.value = false
    }

    // Fungsi navigasi ini mungkin tidak dipakai jika via BottomNav, tapi bisa dibiarkan
    fun onGoToRegisterClicked() {
        _navigateToRegister.value = true
    }

    fun onGoToLoginClicked() {
        _navigateToLogin.value = true
    }

    fun onNavigationToRegisterDone() {
        _navigateToRegister.value = false
    }

    fun onNavigationToLoginDone() {
        _navigateToLogin.value = false
    }

}