package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomerProductsViewModel : ViewModel() {
    private val productRepository = ProductRepository.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var searchJob: Job? = null
    private val debouncePeriod = 300L // milliseconds

    init {
        loadProducts() // Load initial products
    }

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            // Simulate network delay if needed
            // delay(500)
            _products.value = productRepository.getAllProducts()
            _isLoading.value = false
        }
    }

    fun searchProducts(query: String) {
        searchJob?.cancel() // Cancel previous search if user types quickly
        searchJob = viewModelScope.launch {
            delay(debouncePeriod) // Wait for user to stop typing
            _isLoading.value = true
            // Simulate search delay if needed
            // delay(300)
            _products.value = productRepository.searchProducts(query)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel() // Ensure job is cancelled when ViewModel is destroyed
    }
}