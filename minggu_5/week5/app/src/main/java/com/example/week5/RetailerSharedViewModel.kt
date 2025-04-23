package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RetailerSharedViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    fun loadCurrentUser() {
        viewModelScope.launch {
            SessionManager.loggedInUserEmail?.let { email ->
                val user = userRepository.findUserByEmail(email)
                if (user is Retailer) { // Ensure it's a retailer
                    _loggedInUser.postValue(user)
                    _balance.postValue(user.balance)
                } else {
                    // Handle error: logged in user is not a retailer
                    _loggedInUser.postValue(null)
                    _balance.postValue(0.0)
                }
            }
        }
    }

    fun updateUserBalance(newBalance: Double) {
        viewModelScope.launch {
            SessionManager.loggedInUserEmail?.let { email ->
                if (userRepository.updateBalance(email, newBalance)) {
                    _balance.postValue(newBalance)
                }
            }
        }
    }
}