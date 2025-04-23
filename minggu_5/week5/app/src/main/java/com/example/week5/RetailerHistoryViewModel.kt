package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Data class to hold combined Transaction and Customer Name
data class RetailerHistoryItem(
    val transaction: Transaction,
    val customerName: String?
)

class RetailerHistoryViewModel : ViewModel() {

    private val transactionRepository = TransactionRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    private val _historyItems = MutableLiveData<List<RetailerHistoryItem>>()
    val historyItems: LiveData<List<RetailerHistoryItem>> = _historyItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    fun loadHistory(retailerEmail: String?) {
        if (retailerEmail == null) {
            _historyItems.value = emptyList()
            _isEmpty.value = true
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val transactions = transactionRepository.getRetailerHistory(retailerEmail)
            val historyItemList = mutableListOf<RetailerHistoryItem>()

            // Fetch customer name for each transaction
            for (transaction in transactions) {
                // Consider fetching names concurrently if performance is an issue
                val customerName = userRepository.getUserNameByEmail(transaction.customerEmail)
                historyItemList.add(RetailerHistoryItem(transaction, customerName ?: "Unknown Customer"))
            }

            _historyItems.value = historyItemList
            _isEmpty.value = historyItemList.isEmpty()
            _isLoading.value = false
        }
    }
}