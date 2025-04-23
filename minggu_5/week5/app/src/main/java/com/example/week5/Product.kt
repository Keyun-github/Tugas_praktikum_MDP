package com.example.week5

import java.util.UUID // For unique IDs

data class Product(
    val id: String = UUID.randomUUID().toString(), // Unique ID for each product
    var name: String,
    var price: Double,
    var description: String,
    var stock: Int,
    val retailerName: String, // Store the name of the retailer selling it
    val retailerEmail: String // Store the retailer's email for reference
)