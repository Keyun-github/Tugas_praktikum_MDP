package com.example.week5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels // Import correct viewModels delegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val sharedViewModel: CustomerSharedViewModel by viewModels() // Initialize Shared ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Hide BottomNav initially or based on destination changes
        val bottomNavViewCustomer = findViewById<BottomNavigationView>(R.id.bottom_navigation_customer)
        // val bottomNavViewRetailer = findViewById<BottomNavigationView>(R.id.bottom_navigation_retailer) // If you add retailer nav

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    bottomNavViewCustomer.visibility = View.GONE
                    // bottomNavViewRetailer.visibility = View.GONE
                }
                // Add Retailer fragments here if needed
                // R.id.retailerProductsFragment, ... -> {
                //    bottomNavViewCustomer.visibility = View.GONE
                //    bottomNavViewRetailer.visibility = View.VISIBLE
                // }
                else -> { // Assume customer fragments otherwise for now
                    bottomNavViewCustomer.visibility = View.VISIBLE
                    // bottomNavViewRetailer.visibility = View.GONE // Hide retailer nav
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}