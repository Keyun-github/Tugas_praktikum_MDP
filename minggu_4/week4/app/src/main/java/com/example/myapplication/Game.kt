package com.example.myapplication

data class Game(
    val name: String,
    val description: String,
    val genres: List<String>,
    val isFree: Boolean,
    val price: Long
)