package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class WithdrawViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _withdrawStatus = MutableLiveData<WithdrawResult>()
    val withdrawStatus: LiveData<WithdrawResult> = _withdrawStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun loadBalance(retailerEmail: String?) {
        if (retailerEmail == null) {
            _currentBalance.value = 0.0
            return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val user = userRepository.findUserByEmail(retailerEmail)
            _currentBalance.value = user?.balance ?: 0.0
            _isLoading.value = false
        }
    }

    fun withdraw(retailerEmail: String?, amountString: String) {
        if (retailerEmail == null) {
            _error.value = "User not logged in."
            _withdrawStatus.value = WithdrawResult.Error("User not logged in.")
            return
        }

        _isLoading.value = true
        _error.value = null
        _withdrawStatus.value = WithdrawResult.Idle // Reset status

        val amount = amountString.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _error.value = "Please enter a valid positive amount to withdraw."
            _withdrawStatus.value = WithdrawResult.Error("Invalid amount.")
            _isLoading.value = false
            return
        }

        if (amount < 1000) {
            _error.value = "Minimum withdrawal amount is Rp 1,000."
            _withdrawStatus.value = WithdrawResult.Error("Amount too low.")
            _isLoading.value = false
            return
        }

        val currentBal = _currentBalance.value ?: 0.0
        if (amount > currentBal) {
            _error.value = "Insufficient balance (Available: ${formatCurrency(currentBal)})."
            _withdrawStatus.value = WithdrawResult.Error("Insufficient balance.")
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            val newBalance = currentBal - amount
            val success = userRepository.updateBalance(retailerEmail, newBalance)

            if (success) {
                _currentBalance.value = newBalance // Update displayed balance
                _withdrawStatus.value = WithdrawResult.Success("Withdrawal successful! New balance: ${formatCurrency(newBalance)}", newBalance)
            } else {
                _error.value = "Withdrawal failed. Please try again."
                _withdrawStatus.value = WithdrawResult.Error("Withdrawal failed.")
            }
            _isLoading.value = false
        }
    }

    // Function to reset the status after UI handles it
    fun resetWithdrawStatus() {
        _withdrawStatus.value = WithdrawResult.Idle
        _error.value = null // Clear error
    }

    // Helper to format currency
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

}

// Sealed class to represent withdraw outcome
sealed class WithdrawResult {
    object Idle : WithdrawResult()
    data class Success(val message: String, val newBalance: Double) : WithdrawResult()
    data class Error(val message: String) : WithdrawResult()
}