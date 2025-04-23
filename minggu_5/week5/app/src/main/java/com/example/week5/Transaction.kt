package com.example.week5

import java.util.Date // For timestamping transactions
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val customerEmail: String,
    val productId: String, // Reference the product
    val productName: String,
    val retailerName: String,
    val quantity: Int,
    val totalPrice: Double,
    val timestamp: Date = Date() // Record when the transaction happened
)