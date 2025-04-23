package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ProductDetailViewModel : ViewModel() {

    private val productRepository = ProductRepository.getInstance()
    private val userRepository = UserRepository.getInstance()
    private val transactionRepository = TransactionRepository.getInstance()

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _quantity = MutableLiveData(1) // Default quantity
    val quantity: LiveData<Int> = _quantity

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    // Use CustomerSharedViewModel for balance? Or manage separately? Let's manage here for now
    // Or better, get it from shared viewmodel or session
    // For simplicity here, assume we get the balance when needed

    private val _purchaseStatus = MutableLiveData<PurchaseResult>()
    val purchaseStatus: LiveData<PurchaseResult> = _purchaseStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun loadProduct(productId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val fetchedProduct = productRepository.findProductById(productId)
            _product.value = fetchedProduct
            if (fetchedProduct != null) {
                calculateTotalPrice(fetchedProduct.price, _quantity.value ?: 1)
            } else {
                _error.value = "Product not found."
            }
            _isLoading.value = false
        }
    }

    fun incrementQuantity() {
        val currentQuantity = _quantity.value ?: 1
        val maxStock = _product.value?.stock ?: 0
        if (currentQuantity < maxStock) {
            _quantity.value = currentQuantity + 1
            calculateTotalPrice(_product.value?.price ?: 0.0, _quantity.value ?: 1)
        } else {
            _error.value = "Cannot exceed available stock ($maxStock)"
        }
    }

    fun decrementQuantity() {
        val currentQuantity = _quantity.value ?: 1
        if (currentQuantity > 1) {
            _quantity.value = currentQuantity - 1
            calculateTotalPrice(_product.value?.price ?: 0.0, _quantity.value ?: 1)
        }
    }

    private fun calculateTotalPrice(price: Double, quantity: Int) {
        _totalPrice.value = price * quantity
    }

    fun buyProduct(customerEmail: String, currentBalance: Double) {
        _isLoading.value = true
        _error.value = null
        _purchaseStatus.value = PurchaseResult.Idle // Reset status

        val productToBuy = _product.value
        val qty = _quantity.value ?: 1
        val calculatedTotalPrice = _totalPrice.value ?: 0.0

        if (productToBuy == null) {
            _error.value = "Product details are missing."
            _purchaseStatus.value = PurchaseResult.Error("Product details missing.")
            _isLoading.value = false
            return
        }

        if (productToBuy.stock < qty) {
            _error.value = "Not enough stock available (Available: ${productToBuy.stock})."
            _purchaseStatus.value = PurchaseResult.Error("Insufficient stock.")
            _isLoading.value = false
            return
        }

        if (currentBalance < calculatedTotalPrice) {
            _error.value = "Insufficient balance (Required: ${formatCurrency(calculatedTotalPrice)}, Available: ${formatCurrency(currentBalance)})."
            _purchaseStatus.value = PurchaseResult.Error("Insufficient balance.")
            _isLoading.value = false
            return
        }

        // Proceed with purchase
        viewModelScope.launch {
            // 1. Decrease product stock
            val stockDecreased = productRepository.decreaseStock(productToBuy.id, qty)

            if (stockDecreased) {
                // 2. Deduct balance from user
                val newBalance = currentBalance - calculatedTotalPrice
                val balanceUpdated = userRepository.updateBalance(customerEmail, newBalance)

                if (balanceUpdated) {
                    // 3. Add transaction record
                    val transaction = Transaction(
                        customerEmail = customerEmail,
                        productId = productToBuy.id,
                        productName = productToBuy.name,
                        retailerName = productToBuy.retailerName,
                        quantity = qty,
                        totalPrice = calculatedTotalPrice
                    )
                    transactionRepository.addTransaction(transaction)

                    // 4. Update LiveData and Signal Success
                    loadProduct(productToBuy.id) // Reload product to show updated stock
                    // Reset quantity? Optional. Let's keep it for now.
                    _purchaseStatus.value = PurchaseResult.Success("Purchase successful! New balance: ${formatCurrency(newBalance)}", newBalance)

                } else {
                    // Rollback stock? (More complex, for simplicity ignore rollback for now)
                    _error.value = "Failed to update balance. Purchase cancelled."
                    _purchaseStatus.value = PurchaseResult.Error("Balance update failed.")
                    // Attempt to increase stock back - basic rollback
                    productRepository.findProductById(productToBuy.id)?.let { it.stock += qty }

                }
            } else {
                _error.value = "Failed to update stock. Purchase cancelled."
                _purchaseStatus.value = PurchaseResult.Error("Stock update failed.")
            }
            _isLoading.value = false
        }
    }

    // Function to reset the status after UI handles it
    fun resetPurchaseStatus() {
        _purchaseStatus.value = PurchaseResult.Idle
        _error.value = null // Also clear error message
    }

    // Helper to format currency
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")) // Indonesian Rupiah
        format.maximumFractionDigits = 0 // No decimal places for Rupiah usually
        return format.format(amount).replace("Rp", "Rp ") // Add space
    }
}

// Sealed class to represent purchase outcome
sealed class PurchaseResult {
    object Idle : PurchaseResult() // Initial state
    data class Success(val message: String, val newBalance: Double) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}