package com.example.week5.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.week5.models.Customer
import com.example.week5.models.Retailer
import com.example.week5.models.User
import com.example.week5.models.UserRole
import com.example.week5.repository.UserRepository

class LoginViewModel : ViewModel() {
    private val repository = UserRepository.getInstance()

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    fun login(email: String, password: String) {
        _loginError.value = "" // Clear previous error
        if (email.isEmpty() || password.isEmpty()) {
            _loginError.value = "Please fill in all fields"
            return
        }

        val user = repository.validateCredentials(email, password)
        if (user == null) {
            _loginError.value = "Invalid email or password"
            _loginResult.value = null // Ensure result is null on failure
        } else {
            _loginResult.value = user
        }
    }

    fun resetLoginStatus() {
        _loginResult.value = null
        _loginError.value = ""
    }
}


class RegisterViewModel : ViewModel() {
    private val repository = UserRepository.getInstance()

    private val _registrationResult = MutableLiveData<User?>()
    val registrationResult: LiveData<User?> = _registrationResult

    private val _registrationError = MutableLiveData<String>()
    val registrationError: LiveData<String> = _registrationError

    fun register(role: UserRole, name: String, email: String, password: String, confirmPassword: String) {
        _registrationError.value = ""

        when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                _registrationError.value = "Please fill in all fields"
                _registrationResult.value = null
                return
            }
            password.length < 6 -> {
                _registrationError.value = "Password must be at least 6 characters long"
                _registrationResult.value = null
                return
            }
            password != confirmPassword -> {
                _registrationError.value = "Passwords do not match"
                _registrationResult.value = null
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { // Email format validation
                _registrationError.value = "Invalid email format"
                _registrationResult.value = null
                return
            }
            repository.isEmailTaken(email) -> {
                _registrationError.value = "Email already registered"
                _registrationResult.value = null
                return
            }
            role == UserRole.RETAILER && name.isBlank() -> {
                _registrationError.value = "Store name cannot be empty"
                _registrationResult.value = null
                return
            }
            role == UserRole.RETAILER && repository.isStoreNameTaken(name) -> {
                _registrationError.value = "Store name already taken"
                _registrationResult.value = null
                return
            }
        }

        val user = if (role == UserRole.CUSTOMER) {
            Customer(email, password, name)
        } else {
            Retailer(email, password, name)
        }

        val success = repository.registerUser(user)

        if (success) {
            _registrationResult.value = user
        } else {
            _registrationError.value = "Registration failed. Please try again."
            _registrationResult.value = null
        }
    }

    fun resetRegistrationStatus() {
        _registrationResult.value = null
        _registrationError.value = ""
    }
}