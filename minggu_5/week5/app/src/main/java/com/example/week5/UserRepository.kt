package com.example.week5

import com.example.week5.Customer
import com.example.week5.Retailer
import com.example.week5.User
import com.example.week5.UserRole

class UserRepository private constructor() {
    private val users = mutableListOf<User>()

    init {
        // Add some sample users for testing
        users.add(Customer("customer@example.com", "password", "Test Customer"))
        users.add(Retailer("retailer@example.com", "password", "Test Store"))
    }

    fun updateBalance(email: String, newBalance: Double): Boolean {
        val user = findUserByEmail(email)
        return if (user != null) {
            user.balance = newBalance
            true // Indicate success
        } else {
            false // Indicate user not found
        }
    }


    fun getUsers(): List<User> = users

    fun findUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    fun isEmailTaken(email: String): Boolean {
        return users.any { it.email == email }
    }

    fun isStoreNameTaken(storeName: String): Boolean {
        return users.any {
            it is Retailer && it.name.equals(storeName, ignoreCase = true)
        }
    }

    fun registerUser(user: User): Boolean {
        // Check if email is already taken
        if (isEmailTaken(user.email)) {
            return false
        }

        // Check if store name is taken (for retailers)
        if (user is Retailer && isStoreNameTaken(user.name)) {
            return false
        }

        users.add(user)
        return true
    }

    fun validateCredentials(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
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