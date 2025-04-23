package com.example.week5

class UserRepository private constructor() {
    private val users = mutableListOf<User>()

    init {
        // Add some sample users for testing
        // Ensure retailer has initial balance if needed for withdraw testing
        users.add(Customer("customer@example.com", "password", "Test Customer", 150000.0))
        users.add(Customer("febrian@example.com", "password", "Febrian", 50000.0))
        users.add(Customer("timothy@example.com", "password", "Timothy", 200000.0))
        users.add(Retailer("retailer@example.com", "password", "ISTTS Store", 50000.0)) // Add initial balance
        users.add(Retailer("another@example.com", "password", "Toko Lain", 10000.0)) // Add initial balance
    }

    fun getUsers(): List<User> = users

    fun findUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    // --- ADDED METHOD ---
    fun getUserNameByEmail(email: String): String? {
        return findUserByEmail(email)?.name
    }
    // --- END ADDED METHOD ---

    fun isEmailTaken(email: String): Boolean {
        return users.any { it.email == email }
    }

    fun isStoreNameTaken(storeName: String): Boolean {
        return users.any {
            it is Retailer && it.name.equals(storeName, ignoreCase = true)
        }
    }

    fun registerUser(user: User): Boolean {
        if (isEmailTaken(user.email)) {
            return false
        }
        if (user is Retailer && isStoreNameTaken(user.name)) {
            return false
        }
        users.add(user)
        return true
    }

    fun validateCredentials(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }

    // Method to update user balance (used by both Customer Topup and Retailer Withdraw)
    fun updateBalance(email: String, newBalance: Double): Boolean {
        val user = findUserByEmail(email)
        return if (user != null) {
            user.balance = newBalance
            true
        } else {
            false
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository().also { instance = it }
            }
        }
    }
}