package com.example.week5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class AddEditProductViewModel : ViewModel() {

    private val productRepository = ProductRepository.getInstance()

    // Product being edited (null if adding)
    private val _productToEdit = MutableLiveData<Product?>()
    val productToEdit: LiveData<Product?> = _productToEdit

    // Loading state for fetching product to edit
    private val _isLoadingData = MutableLiveData<Boolean>()
    val isLoadingData: LiveData<Boolean> = _isLoadingData

    // Operation status (Add/Edit)
    private val _operationStatus = MutableLiveData<OperationResult>()
    val operationStatus: LiveData<OperationResult> = _operationStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Mode: Add or Edit
    val isEditMode: LiveData<Boolean> = MutableLiveData(false)

    fun loadProductForEdit(productId: String?) {
        if (productId == null) {
            (isEditMode as MutableLiveData).value = false
            _productToEdit.value = null // Clear any previous edit data
            return // Add mode
        }

        (isEditMode as MutableLiveData).value = true
        _isLoadingData.value = true
        viewModelScope.launch {
            val product = productRepository.findProductById(productId)
            _productToEdit.value = product
            if (product == null) {
                _error.value = "Product not found for editing."
            }
            _isLoadingData.value = false
        }
    }

    fun saveProduct(
        retailerEmail: String?,
        retailerName: String?,
        productId: String?, // Null for add, non-null for edit
        name: String,
        priceStr: String,
        description: String,
        stockStr: String
    ) {
        _error.value = null // Clear previous errors
        _operationStatus.value = OperationResult.Idle

        if (retailerEmail == null || retailerName == null) {
            _error.value = "Retailer information is missing."
            _operationStatus.value = OperationResult.Error("Retailer info missing")
            return
        }

        // --- Validations ---
        if (name.isBlank()) {
            _error.value = "Product name cannot be empty."
            _operationStatus.value = OperationResult.Error("Name empty")
            return
        }
        if (description.isBlank()) {
            _error.value = "Product description cannot be empty."
            _operationStatus.value = OperationResult.Error("Description empty")
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price < 1000) {
            _error.value = "Price must be a number and at least Rp 1,000."
            _operationStatus.value = OperationResult.Error("Invalid price")
            return
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 1) {
            _error.value = "Stock must be a number and at least 1."
            _operationStatus.value = OperationResult.Error("Invalid stock")
            return
        }
        // --- End Validations ---


        viewModelScope.launch {
            _isLoadingData.value = true // Use same loading indicator for save operation
            val success: Boolean
            if (productId == null) { // Add Mode
                val newProduct = Product(
                    name = name,
                    price = price,
                    description = description,
                    stock = stock,
                    retailerName = retailerName,
                    retailerEmail = retailerEmail
                )
                success = productRepository.addProduct(newProduct)
            } else { // Edit Mode
                val editedProduct = Product(
                    id = productId, // Keep original ID
                    name = name,
                    price = price,
                    description = description,
                    stock = stock,
                    retailerName = retailerName, // Retailer info shouldn't change on edit
                    retailerEmail = retailerEmail
                )
                success = productRepository.updateProduct(editedProduct)
            }
            _isLoadingData.value = false

            if (success) {
                _operationStatus.value = OperationResult.Success(if (productId == null) "Product added successfully!" else "Product updated successfully!")
            } else {
                _error.value = "Failed to ${if (productId == null) "add" else "update"} product."
                _operationStatus.value = OperationResult.Error("Operation failed")
            }
        }

    }

    // Function to reset the status after UI handles it
    fun resetOperationStatus() {
        _operationStatus.value = OperationResult.Idle
        _error.value = null
    }

    // Helper to format currency
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

}

// Sealed class to represent Add/Edit outcome
sealed class OperationResult {
    object Idle : OperationResult()
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}