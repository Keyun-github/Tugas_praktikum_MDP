package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CustomerTopupViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    // Use SharedViewModel balance or fetch independently? Fetch independently for now.
    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _topupStatus = MutableLiveData<TopupResult>()
    val topupStatus: LiveData<TopupResult> = _topupStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun loadBalance(customerEmail: String?) {
        if (customerEmail == null) {
            _currentBalance.value = 0.0
            return
        }
        _isLoading.value = true
        _error.value = null // Clear previous errors
        viewModelScope.launch {
            val user = userRepository.findUserByEmail(customerEmail)
            _currentBalance.value = user?.balance ?: 0.0
            _isLoading.value = false
        }
    }

    fun topUp(customerEmail: String?, amountString: String) {
        if (customerEmail == null) {
            _error.value = "User not logged in."
            _topupStatus.value = TopupResult.Error("User not logged in.")
            return
        }

        _isLoading.value = true
        _error.value = null
        _topupStatus.value = TopupResult.Idle // Reset status

        val amount = amountString.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _error.value = "Please enter a valid positive amount."
            _topupStatus.value = TopupResult.Error("Invalid amount.")
            _isLoading.value = false
            return
        }

        if (amount < 1000) {
            _error.value = "Minimum top-up amount is Rp 1,000."
            _topupStatus.value = TopupResult.Error("Amount too low.")
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            val user = userRepository.findUserByEmail(customerEmail)
            if (user == null) {
                _error.value = "User not found."
                _topupStatus.value = TopupResult.Error("User not found.")
                _isLoading.value = false
                return@launch
            }

            val currentBal = user.balance
            val newBalance = currentBal + amount
            val success = userRepository.updateBalance(customerEmail, newBalance)

            if (success) {
                _currentBalance.value = newBalance // Update displayed balance
                _topupStatus.value = TopupResult.Success("Top-up successful! New balance: ${formatCurrency(newBalance)}", newBalance)
            } else {
                _error.value = "Top-up failed. Please try again."
                _topupStatus.value = TopupResult.Error("Top-up failed.")
            }
            _isLoading.value = false
        }
    }

    // Function to reset the status after UI handles it
    fun resetTopupStatus() {
        _topupStatus.value = TopupResult.Idle
        _error.value = null // Clear error
    }

    // Helper to format currency
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")) // Indonesian Rupiah
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

}

// Sealed class to represent top-up outcome
sealed class TopupResult {
    object Idle : TopupResult()
    data class Success(val message: String, val newBalance: Double) : TopupResult()
    data class Error(val message: String) : TopupResult()
}