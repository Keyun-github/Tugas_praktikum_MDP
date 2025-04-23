package com.example.myapplication // <-- Sesuaikan package name

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson // Add Gson dependency for Set<String> storage
import com.google.gson.reflect.TypeToken

enum class LoginResult {
    SUCCESS_ADMIN,
    SUCCESS_USER,
    INVALID_CREDENTIALS
}

object UserCredentialsManager {

    private const val PREFS_NAME = "user_prefs_kukus"
    private const val KEY_PASSWORD_SUFFIX = "_password"
    private const val KEY_PROFILE_NAME_SUFFIX = "_profileName"
    // === New Keys ===
    private const val KEY_WALLET_BALANCE_SUFFIX = "_walletBalance"
    private const val KEY_GAME_LIBRARY_SUFFIX = "_gameLibrary" // Stores Set<String> of game names
    private const val KEY_LOGGED_IN_USER = "loggedInUsername" // Stores current user

    private val gson = Gson() // For storing Set<String> easily

    // --- Login State ---
    var loggedInUsername: String? = null
        private set // Only allow setting internally

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Existing Functions (Register, isUsernameTaken) ---
    fun isUsernameTaken(context: Context, username: String): Boolean {
        if (username.equals("Admin", ignoreCase = true)) return true
        val prefs = getPrefs(context)
        return prefs.all.keys.any { it.equals("${username}$KEY_PASSWORD_SUFFIX", ignoreCase = true) }
    }

    fun registerUser(context: Context, profileName: String, username: String, password: String): Boolean {
        if (isUsernameTaken(context, username)) return false
        val prefs = getPrefs(context)
        with(prefs.edit()) {
            putString("${username}$KEY_PASSWORD_SUFFIX", password)
            putString("${username}$KEY_PROFILE_NAME_SUFFIX", profileName)
            // Initialize wallet for new user
            putLong("${username}$KEY_WALLET_BALANCE_SUFFIX", 0L) // Start with 0 balance
            // Initialize empty library
            putString("${username}$KEY_GAME_LIBRARY_SUFFIX", gson.toJson(emptySet<String>()))
            apply()
        }
        return true
    }

    // --- Modified Login/Logout ---
    fun checkLogin(context: Context, usernameEntered: String, passwordEntered: String): LoginResult {
        if (usernameEntered == "Admin" && passwordEntered == "Admin") {
            loggedInUsername = "Admin" // Store admin state (optional, depends if needed)
            return LoginResult.SUCCESS_ADMIN
        }

        val prefs = getPrefs(context)
        val passwordKey = prefs.all.keys.firstOrNull {
            it.endsWith(KEY_PASSWORD_SUFFIX) &&
                    it.dropLast(KEY_PASSWORD_SUFFIX.length).equals(usernameEntered, ignoreCase = true)
        }

        if (passwordKey != null) {
            val storedPassword = prefs.getString(passwordKey, null)
            if (storedPassword == passwordEntered) {
                // Store the *actual* username (case preserved) as logged in
                loggedInUsername = passwordKey.dropLast(KEY_PASSWORD_SUFFIX.length)
                return LoginResult.SUCCESS_USER
            }
        }
        loggedInUsername = null // Clear on failed login
        return LoginResult.INVALID_CREDENTIALS
    }

    fun logoutUser() {
        loggedInUsername = null
        // Optionally clear other session data if needed
    }

    // --- Profile Name Retrieval (Existing) ---
    fun getProfileName(context: Context, username: String): String? {
        val prefs = getPrefs(context)
        val profileNameKey = prefs.all.keys.firstOrNull {
            it.endsWith(KEY_PROFILE_NAME_SUFFIX) &&
                    it.dropLast(KEY_PROFILE_NAME_SUFFIX.length).equals(username, ignoreCase = true)
        }
        return if (profileNameKey != null) {
            prefs.getString(profileNameKey, null)
        } else {
            null
        }
    }

    // Convenience function to get profile name of logged-in user
    fun getCurrentUserProfileName(context: Context): String? {
        return loggedInUsername?.let { getProfileName(context, it) } ?: "Guest"
    }

    // === Wallet Management ===
    fun getWalletBalance(context: Context, username: String? = loggedInUsername): Long {
        if (username == null) return 0L // Return 0 if no user logged in
        val prefs = getPrefs(context)
        return prefs.getLong("${username}$KEY_WALLET_BALANCE_SUFFIX", 0L) // Default 0
    }

    fun updateWalletBalance(context: Context, amountChange: Long, username: String? = loggedInUsername): Boolean {
        if (username == null) return false // Cannot update if no user logged in
        val prefs = getPrefs(context)
        val currentBalance = getWalletBalance(context, username)
        val newBalance = currentBalance + amountChange
        // Optional: Prevent negative balance if deducting
        // if (newBalance < 0) return false

        with(prefs.edit()) {
            putLong("${username}$KEY_WALLET_BALANCE_SUFFIX", newBalance)
            apply()
        }
        return true
    }

    // === Library Management ===
    fun isGameInLibrary(context: Context, gameName: String, username: String? = loggedInUsername): Boolean {
        if (username == null) return false
        val library = getUserLibraryGameNames(context, username)
        return library.contains(gameName)
    }

    fun addGameToLibrary(context: Context, gameName: String, username: String? = loggedInUsername): Boolean {
        if (username == null || isGameInLibrary(context, gameName, username)) return false
        val prefs = getPrefs(context)
        val currentLibraryJson = prefs.getString("${username}$KEY_GAME_LIBRARY_SUFFIX", "[]")
        val type = object : TypeToken<MutableSet<String>>() {}.type
        val currentLibrary: MutableSet<String> = gson.fromJson(currentLibraryJson, type) ?: mutableSetOf()

        currentLibrary.add(gameName)

        with(prefs.edit()) {
            putString("${username}$KEY_GAME_LIBRARY_SUFFIX", gson.toJson(currentLibrary))
            apply()
        }
        return true
    }

    fun getUserLibraryGameNames(context: Context, username: String? = loggedInUsername): Set<String> {
        if (username == null) return emptySet()
        val prefs = getPrefs(context)
        val libraryJson = prefs.getString("${username}$KEY_GAME_LIBRARY_SUFFIX", "[]")
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(libraryJson, type) ?: emptySet()
    }
}