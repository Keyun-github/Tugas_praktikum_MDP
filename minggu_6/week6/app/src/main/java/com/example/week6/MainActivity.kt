package com.example.week6

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI // Import ini penting
import androidx.navigation.fragment.NavHostFragment
import com.example.week6.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply { }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Listener untuk show/hide UI
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Tampilkan Toolbar & BottomNav hanya di Login & Register
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.toolbar.visibility = View.VISIBLE // Tampilkan Toolbar Reddid
                    binding.bottomNavigation.visibility = View.VISIBLE // Tampilkan BottomNav Login/Register
                    // Ganti menu untuk Login/Register
                    binding.bottomNavigation.menu.clear() // Hapus menu sebelumnya
                    binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu) // Inflate menu Login/Register
                    // Update item terpilih
                    if (destination.id == R.id.loginFragment) {
                        binding.bottomNavigation.menu.findItem(R.id.navigation_login)?.isChecked = true
                    } else {
                        binding.bottomNavigation.menu.findItem(R.id.navigation_register)?.isChecked = true
                    }
                }
                // Tampilkan Toolbar & BottomNav juga di Home & AddPost
                R.id.homeFragment, R.id.addPostFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.VISIBLE
                    // Ganti menu untuk Halaman Utama
                    binding.bottomNavigation.menu.clear()
                    binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_main) // Inflate menu Home/Add/Logout
                    // Update item terpilih
                    if (destination.id == R.id.homeFragment) {
                        binding.bottomNavigation.menu.findItem(R.id.homeFragment)?.isChecked = true
                    } else {
                        binding.bottomNavigation.menu.findItem(R.id.addPostFragment)?.isChecked = true
                    }
                }
                // Hanya Toolbar di Detail & Add Community
                R.id.detailPostFragment, R.id.addCommunityFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.GONE // Sembunyikan BottomNav
                }
                // Default (misal splash screen atau halaman lain jika ada)
                else -> {
                    binding.toolbar.visibility = View.GONE
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }

        // Setup listener Bottom Navigation
        setupCombinedBottomNavigationListener()

        observeAuthEvents()
    }

    // Listener GABUNGAN untuk SEMUA item Bottom Navigation
    private fun setupCombinedBottomNavigationListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentDestinationId = navController.currentDestination?.id

            when (item.itemId) {
                // --- Item Menu Login/Register ---
                R.id.navigation_login -> {
                    if (currentDestinationId != R.id.loginFragment) {
                        // Pindah ke Login (biasanya dari Register)
                        if (currentDestinationId == R.id.registerFragment) {
                            navController.navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                    }
                    true
                }
                R.id.navigation_register -> {
                    if (currentDestinationId != R.id.registerFragment) {
                        if (currentDestinationId == R.id.loginFragment) {
                            navController.navigate(R.id.action_loginFragment_to_registerFragment)
                        }
                    }
                    true
                }

                // --- Item Menu Halaman Utama ---
                R.id.homeFragment -> {
                    if (currentDestinationId != R.id.homeFragment) {
                        // Gunakan NavigationUI helper atau navigate langsung
                        NavigationUI.onNavDestinationSelected(item, navController)
                        // atau navController.navigate(R.id.homeFragment) // Sesuaikan popUp behavior jika perlu
                    }
                    true
                }
                R.id.addPostFragment -> {
                    if (currentDestinationId != R.id.addPostFragment) {
                        NavigationUI.onNavDestinationSelected(item, navController)
                        // atau navController.navigate(R.id.addPostFragment)
                    }
                    true
                }
                R.id.action_logout -> {
                    authViewModel.logout()
                    true
                }
                else -> false
            }
        }
        // Hindari reselection trigger navigasi ulang
        binding.bottomNavigation.setOnItemReselectedListener {
            // Do nothing
        }
    }


    private fun observeAuthEvents() {
        authViewModel.loginSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Log.d("MainActivity", "Login successful event observed, navigating to home.")
                if (navController.currentDestination?.id == R.id.loginFragment || navController.currentDestination?.id == R.id.registerFragment ) {
                    navController.navigate(R.id.action_loginFragment_to_homeFragment)
                }
                authViewModel.onLoginSuccessDone()
            }
        }

        authViewModel.logoutEvent.observe(this) { isLogout ->
            if (isLogout) {
                Log.d("MainActivity", "Logout event observed, navigating to login.")
                if (navController.currentDestination?.id != R.id.loginFragment) {
                    navController.navigate(R.id.action_global_loginFragment)
                }
                authViewModel.onLogoutEventDone()
            }
        }

        authViewModel.navigateToLogin.observe(this) { navigate ->
            if (navigate) {
                if (navController.currentDestination?.id == R.id.registerFragment) {
                    navController.navigate(R.id.action_registerFragment_to_loginFragment)
                    Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()
                }
                authViewModel.onNavigationToLoginDone()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}