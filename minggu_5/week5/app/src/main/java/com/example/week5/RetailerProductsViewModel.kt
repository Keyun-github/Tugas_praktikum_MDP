package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RetailerProductsViewModel : ViewModel() {
    private val productRepository = ProductRepository.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var searchJob: Job? = null
    private val debouncePeriod = 300L // milliseconds

    fun loadProducts(retailerEmail: String?) {
        if (retailerEmail == null) {
            _products.value = emptyList()
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            _products.value = productRepository.getProductsByRetailer(retailerEmail)
            _isLoading.value = false
        }
    }

    fun searchProducts(retailerEmail: String?, query: String) {
        if (retailerEmail == null) {
            _products.value = emptyList()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(debouncePeriod)
            _isLoading.value = true
            _products.value = productRepository.searchOwnProducts(retailerEmail, query)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}