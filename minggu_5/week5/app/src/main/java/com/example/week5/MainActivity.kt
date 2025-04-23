package com.example.week5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout // For LayoutParams
import androidx.constraintlayout.widget.Guideline // For Guideline
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val customerSharedViewModel: CustomerSharedViewModel by viewModels()
    private val retailerSharedViewModel: RetailerSharedViewModel by viewModels() // Add Retailer Shared VM

    private lateinit var bottomNavViewCustomer: BottomNavigationView
    private lateinit var bottomNavViewRetailer: BottomNavigationView
    private lateinit var navHostFragmentContainer: View // The FragmentContainerView
    private lateinit var guideline: Guideline


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find Views
        bottomNavViewCustomer = findViewById(R.id.bottom_navigation_customer)
        bottomNavViewRetailer = findViewById(R.id.bottom_navigation_retailer)
        navHostFragmentContainer = findViewById(R.id.nav_host_fragment) // Get FragmentContainerView
        guideline = findViewById(R.id.bottom_nav_top_guideline) // Get Guideline

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Listener to control BottomNav visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Determine which nav bar should be visible
            val isCustomerDestination = when (destination.id) {
                R.id.customerProductsFragment,
                R.id.customerHistoryFragment,
                R.id.customerTopupFragment,
                R.id.productDetailFragment // Detail is part of customer flow
                    -> true
                else -> false
            }

            val isRetailerDestination = when (destination.id) {
                R.id.retailerProductsFragment,
                R.id.addEditProductFragment, // Add/Edit is part of retailer flow
                R.id.retailerHistoryFragment,
                R.id.withdrawFragment
                    -> true
                else -> false
            }

            // Set Guideline constraint based on which nav is visible
            val params = guideline.layoutParams as ConstraintLayout.LayoutParams
            if (isCustomerDestination || isRetailerDestination) {
                params.guidePercent = 1.0f // Set to bottom (will be pushed up by nav bar height)
                // Adjust constraint to be above the *specific* visible nav bar dynamically
                // This ensures the fragment container resizes correctly.
                val navHostParams = navHostFragmentContainer.layoutParams as ConstraintLayout.LayoutParams
                navHostParams.bottomToTop = if (isCustomerDestination) R.id.bottom_navigation_customer else R.id.bottom_navigation_retailer
                navHostFragmentContainer.layoutParams = navHostParams

            } else {
                // If neither (e.g., Login/Register), nav bar hidden, constrain to parent bottom
                params.guidePercent = 1.0f // Guideline at bottom
                val navHostParams = navHostFragmentContainer.layoutParams as ConstraintLayout.LayoutParams
                navHostParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET // Remove constraint to top of nav
                navHostParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID // Constrain to parent bottom
                navHostFragmentContainer.layoutParams = navHostParams

            }
            guideline.layoutParams = params // Apply guideline changes


            // Set visibility
            bottomNavViewCustomer.visibility = if (isCustomerDestination) View.VISIBLE else View.GONE
            bottomNavViewRetailer.visibility = if (isRetailerDestination) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}