package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ProductDetailViewModel : ViewModel() {

    // Repositories for data operations
    private val productRepository = ProductRepository.getInstance()
    private val userRepository = UserRepository.getInstance()
    private val transactionRepository = TransactionRepository.getInstance()

    // LiveData for the product being viewed
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    // LiveData for the quantity selected by the user
    private val _quantity = MutableLiveData(1) // Default quantity is 1
    val quantity: LiveData<Int> = _quantity

    // LiveData for the calculated total price based on product price and quantity
    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    // LiveData to communicate the result of a purchase attempt to the UI
    private val _purchaseStatus = MutableLiveData<PurchaseResult>(PurchaseResult.Idle) // Start in Idle state
    val purchaseStatus: LiveData<PurchaseResult> = _purchaseStatus

    // LiveData for indicating loading state (fetching product or processing purchase)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for holding error messages (e.g., validation, purchase failure)
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Loads the product details from the repository based on the provided ID.
     * Updates LiveData for the product, total price, and loading state.
     */
    fun loadProduct(productId: String) {
        _isLoading.value = true
        _error.value = null // Clear previous errors
        viewModelScope.launch {
            val fetchedProduct = productRepository.findProductById(productId)
            _product.value = fetchedProduct
            if (fetchedProduct != null) {
                // Calculate initial total price based on the loaded product and current quantity
                calculateTotalPrice(fetchedProduct.price, _quantity.value ?: 1)
            } else {
                _error.value = "Product not found." // Set error if product doesn't exist
            }
            _isLoading.value = false // Loading finished
        }
    }

    /**
     * Increases the purchase quantity, ensuring it doesn't exceed available stock.
     * Updates the quantity and recalculates the total price.
     */
    fun incrementQuantity() {
        val currentQuantity = _quantity.value ?: 1
        val maxStock = _product.value?.stock ?: 0 // Get current stock, default to 0 if product is null
        if (currentQuantity < maxStock) {
            _quantity.value = currentQuantity + 1
            // Recalculate total price after changing quantity
            calculateTotalPrice(_product.value?.price ?: 0.0, _quantity.value ?: 1)
            _error.value = null // Clear previous stock errors if any
        } else {
            // Show error if trying to exceed stock
            _error.value = "Cannot exceed available stock ($maxStock)"
        }
    }

    /**
     * Decreases the purchase quantity, ensuring it doesn't go below 1.
     * Updates the quantity and recalculates the total price.
     */
    fun decrementQuantity() {
        val currentQuantity = _quantity.value ?: 1
        if (currentQuantity > 1) {
            _quantity.value = currentQuantity - 1
            // Recalculate total price after changing quantity
            calculateTotalPrice(_product.value?.price ?: 0.0, _quantity.value ?: 1)
            _error.value = null // Clear previous stock errors if any
        }
        // No action needed if quantity is already 1
    }

    /**
     * Calculates the total price based on unit price and quantity.
     * Updates the _totalPrice LiveData.
     */
    private fun calculateTotalPrice(price: Double, quantity: Int) {
        _totalPrice.value = price * quantity
    }

    /**
     * Attempts to purchase the currently selected product with the chosen quantity.
     * Performs validation checks (stock, balance).
     * Updates product stock, user balance, and creates a transaction record upon success.
     * Updates LiveData for purchase status, loading state, and errors.
     *
     * @param customerEmail The email of the customer making the purchase.
     * @param currentBalance The current balance of the customer.
     */
    fun buyProduct(customerEmail: String, currentBalance: Double) {
        _isLoading.value = true
        _error.value = null // Clear previous errors
        _purchaseStatus.value = PurchaseResult.Idle // Reset status before attempting purchase

        val productToBuy = _product.value
        val qty = _quantity.value ?: 1
        val calculatedTotalPrice = _totalPrice.value ?: 0.0

        // --- Validation Checks ---
        if (productToBuy == null) {
            _error.value = "Product details are missing."
            _purchaseStatus.value = PurchaseResult.Error("Product details missing.")
            _isLoading.value = false
            return // Stop the purchase process
        }

        if (productToBuy.stock < qty) {
            _error.value = "Not enough stock available (Available: ${productToBuy.stock})."
            _purchaseStatus.value = PurchaseResult.Error("Insufficient stock.")
            _isLoading.value = false
            return // Stop the purchase process
        }

        if (currentBalance < calculatedTotalPrice) {
            _error.value = "Insufficient balance (Required: ${formatCurrency(calculatedTotalPrice)}, Available: ${formatCurrency(currentBalance)})."
            _purchaseStatus.value = PurchaseResult.Error("Insufficient balance.")
            _isLoading.value = false
            return // Stop the purchase process
        }
        // --- End Validation Checks ---

        // Proceed with purchase if validations pass
        viewModelScope.launch {
            // 1. Decrease product stock in the repository
            val stockDecreased = productRepository.decreaseStock(productToBuy.id, qty)

            if (stockDecreased) {
                // 2. Deduct balance from user in the repository
                val newBalance = currentBalance - calculatedTotalPrice
                val balanceUpdated = userRepository.updateBalance(customerEmail, newBalance)

                if (balanceUpdated) {
                    // 3. Add transaction record to the repository
                    // *** Ensure retailerEmail is included ***
                    val transaction = Transaction(
                        customerEmail = customerEmail,
                        productId = productToBuy.id,
                        productName = productToBuy.name,
                        retailerName = productToBuy.retailerName,
                        retailerEmail = productToBuy.retailerEmail, // Included retailer email
                        quantity = qty,
                        totalPrice = calculatedTotalPrice
                        // Timestamp is added automatically in the Transaction data class
                    )
                    transactionRepository.addTransaction(transaction)

                    // 4. Update LiveData and Signal Success
                    loadProduct(productToBuy.id) // Reload product to show updated stock
                    // Reset quantity? Optional. Let's keep it for now.
                    _purchaseStatus.value = PurchaseResult.Success("Purchase successful! New balance: ${formatCurrency(newBalance)}", newBalance)

                } else {
                    // Rollback stock if balance update failed (Basic rollback)
                    productRepository.findProductById(productToBuy.id)?.let { it.stock += qty }
                    _error.value = "Failed to update balance. Purchase cancelled."
                    _purchaseStatus.value = PurchaseResult.Error("Balance update failed.")
                }
            } else {
                // This case might happen if stock changed between check and update (race condition)
                _error.value = "Failed to update stock. Purchase cancelled."
                _purchaseStatus.value = PurchaseResult.Error("Stock update failed.")
            }
            _isLoading.value = false // Purchase attempt finished
        }
    }

    /**
     * Resets the purchase status LiveData to Idle and clears any associated error message.
     * Should be called by the UI after handling a Success or Error status.
     */
    fun resetPurchaseStatus() {
        _purchaseStatus.value = PurchaseResult.Idle
        _error.value = null // Also clear error message when resetting status
    }

    /**
     * Helper function to format a Double value into Indonesian Rupiah currency string.
     * Example: 50000.0 -> "Rp 50.000"
     *
     * @param amount The numeric amount to format.
     * @return A formatted currency string.
     */
    fun formatCurrency(amount: Double): String {
        val localeID = Locale("in", "ID") // Indonesian locale
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0 // Typically no decimals for Rupiah
        // Add space after "Rp" for better readability
        return format.format(amount).replace("Rp", "Rp ")
    }
}

/**
 * Sealed class representing the possible outcomes of a purchase attempt.
 * Used to communicate the result from the ViewModel to the UI.
 */
sealed class PurchaseResult {
    /** Initial state before a purchase attempt or after handling a result. */
    object Idle : PurchaseResult()

    /** Indicates a successful purchase. Contains a success message and the user's new balance. */
    data class Success(val message: String, val newBalance: Double) : PurchaseResult()

    /** Indicates a failed purchase attempt. Contains an error message detailing the reason. */
    data class Error(val message: String) : PurchaseResult()
}