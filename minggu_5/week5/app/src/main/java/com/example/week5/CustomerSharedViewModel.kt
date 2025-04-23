package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel to share data like balance across customer fragments
class CustomerSharedViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    fun loadCurrentUser() {
        viewModelScope.launch {
            SessionManager.loggedInUserEmail?.let { email ->
                val user = userRepository.findUserByEmail(email)
                _loggedInUser.postValue(user)
                _balance.postValue(user?.balance ?: 0.0)
            }
        }
    }

    fun updateUserBalance(newBalance: Double) {
        viewModelScope.launch {
            SessionManager.loggedInUserEmail?.let { email ->
                if (userRepository.updateBalance(email, newBalance)) {
                    _balance.postValue(newBalance) // Update LiveData
                    // Optionally reload the full user object if needed elsewhere
                    // val updatedUser = userRepository.findUserByEmail(email)
                    // _loggedInUser.postValue(updatedUser)
                }
            }
        }
    }
}