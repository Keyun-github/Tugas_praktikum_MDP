package com.example.week5

// Simple in-memory storage for transactions
class TransactionRepository private constructor() {

    private val transactions = mutableListOf<Transaction>()

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun getCustomerHistory(customerEmail: String): List<Transaction> {
        // Return transactions for the specific customer, newest first
        return transactions.filter { it.customerEmail == customerEmail }
            .sortedByDescending { it.timestamp }
    }

    // Function for Retailer history (needed later)
    fun getRetailerHistory(retailerEmail: String): List<Transaction> {
        return transactions.filter { product ->
            // Need ProductRepository to check retailer email for the product ID
            // This dependency suggests maybe transaction should store retailerEmail directly
            // Let's modify Transaction.kt to store retailerName for simplicity now
            // If we stored retailerEmail in Transaction, the lookup would be easier.
            // For now, we'll rely on retailerName stored in Transaction.
            val productRepo = ProductRepository.getInstance()
            val product = productRepo.findProductById(product.productId)
            product?.retailerEmail == retailerEmail
        }.sortedByDescending { it.timestamp }

        // ---- OR a simpler approach if retailerName is reliable ----
        // return transactions.filter { it.retailerName == retailerNameFromEmail } // Need a way to get name from email
        //                      .sortedByDescending { it.timestamp }
    }


    companion object {
        @Volatile
        private var instance: TransactionRepository? = null

        fun getInstance(): TransactionRepository {
            return instance ?: synchronized(this) {
                instance ?: TransactionRepository().also { instance = it }
            }
        }
    }
}