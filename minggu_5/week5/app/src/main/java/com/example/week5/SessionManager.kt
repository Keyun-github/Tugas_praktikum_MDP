package com.example.week5

// Simple Singleton to hold logged-in user state
object SessionManager {
    var loggedInUserEmail: String? = null

    fun login(email: String) {
        loggedInUserEmail = email
    }

    fun logout() {
        loggedInUserEmail = null
    }

    fun isLoggedIn(): Boolean {
        return loggedInUserEmail != null
    }
}