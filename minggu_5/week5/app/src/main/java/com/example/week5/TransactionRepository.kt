package com.example.week5

class TransactionRepository private constructor() {

    private val transactions = mutableListOf<Transaction>()

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun getCustomerHistory(customerEmail: String): List<Transaction> {
        return transactions.filter { it.customerEmail == customerEmail }
            .sortedByDescending { it.timestamp }
    }

    // --- RETAILER METHOD ---
    fun getRetailerHistory(retailerEmail: String): List<Transaction> {
        return transactions.filter { it.retailerEmail == retailerEmail } // <<<--- Filter by retailerEmail
            .sortedByDescending { it.timestamp }
    }
    // --- END RETAILER METHOD ---


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