package com.example.minggu3

data class Barang(
    val id: Int,
    var nama: String,
    var stok: Int,
    var isPenting: Boolean = false
)