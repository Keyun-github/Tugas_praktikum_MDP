package com.example.week5

// Base User class
open class User(
    val email: String,
    val password: String,
    val name: String,
    var balance: Double = 0.0,
    val role: UserRole
)

// Specific user types
class Customer(
    email: String,
    password: String,
    name: String,
    balance: Double = 0.0
) : User(email, password, name, balance, UserRole.CUSTOMER)

class Retailer(
    email: String,
    password: String,
    storeName: String,
    balance: Double = 0.0
) : User(email, password, storeName, balance, UserRole.RETAILER)

// Enum to distinguish between user roles
enum class UserRole {
    CUSTOMER,
    RETAILER
}