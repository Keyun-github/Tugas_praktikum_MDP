package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CustomerHistoryViewModel : ViewModel() {

    private val transactionRepository = TransactionRepository.getInstance()

    private val _history = MutableLiveData<List<Transaction>>()
    val history: LiveData<List<Transaction>> = _history

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    fun loadHistory(customerEmail: String?) {
        if (customerEmail == null) {
            _history.value = emptyList()
            _isEmpty.value = true
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            // Simulate delay
            // kotlinx.coroutines.delay(500)
            val customerHistory = transactionRepository.getCustomerHistory(customerEmail)
            _history.value = customerHistory
            _isEmpty.value = customerHistory.isEmpty()
            _isLoading.value = false
        }
    }
}