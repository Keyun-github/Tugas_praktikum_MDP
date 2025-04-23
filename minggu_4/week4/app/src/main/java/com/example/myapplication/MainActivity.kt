package com.example.myapplication // <-- Sesuaikan package name

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupAdminBottomNav()
        setupUserBottomNav() // Setup user nav

        setupBottomNavVisibility() // Control visibility
    }

    private fun setupAdminBottomNav() {
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.adminHomeFragment, R.id.addGameFragment -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                R.id.action_global_logout -> {
                    UserCredentialsManager.logoutUser() // Clear logged in state
                    navController.navigate(R.id.action_global_logout)
                    true
                }
                else -> false
            }
        }
        // Listener to sync highlight (optional but good)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavAdmin.menu.findItem(destination.id)?.isChecked = true
        }
    }

    // --- Setup User Bottom Nav ---
    private fun setupUserBottomNav() {
        // Use standard setup as both items directly map to destinations
        NavigationUI.setupWithNavController(binding.bottomNavUser, navController)
    }
    // ------------------------------

    // --- Control Visibility ---
    private fun setupBottomNavVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Define Admin and User top-level destinations
            val adminDestinations = setOf(
                R.id.adminHomeFragment,
                R.id.addGameFragment,
                R.id.detailGameAdminFragment
            )
            val userDestinations = setOf(
                R.id.userHomeFragment,
                R.id.detailGameUserFragment,
                R.id.accountUserFragment,
                R.id.libraryUserFragment,
                R.id.topupUserFragment
            )

            when {
                adminDestinations.contains(destination.id) -> {
                    binding.bottomNavAdmin.visibility = View.VISIBLE
                    binding.bottomNavUser.visibility = View.GONE
                }
                userDestinations.contains(destination.id) -> {
                    binding.bottomNavAdmin.visibility = View.GONE
                    binding.bottomNavUser.visibility = View.VISIBLE
                }
                else -> { // Login, Register, etc.
                    binding.bottomNavAdmin.visibility = View.GONE
                    binding.bottomNavUser.visibility = View.GONE
                }
            }
        }
    }
    // ---------------------------
}