package com.example.week5

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val customerEmail: String,
    val productId: String,
    val productName: String,
    val retailerName: String,
    val retailerEmail: String,
    val quantity: Int,
    val totalPrice: Double,
    val timestamp: Date = Date()
)
