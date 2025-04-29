package com.example.week6

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.week6.databinding.ActivityMainBinding // Ganti nama package jika perlu

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(LoginFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_login
        }

        setupBottomNavigation()
        observeNavigationEvents()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragmentTag = supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.simpleName
            val targetFragment: Fragment? = when (item.itemId) {
                R.id.navigation_login -> if (currentFragmentTag != LoginFragment::class.java.simpleName) LoginFragment() else null
                R.id.navigation_register -> if (currentFragmentTag != RegisterFragment::class.java.simpleName) RegisterFragment() else null
                else -> null
            }

            if (targetFragment != null) {
                replaceFragment(targetFragment)
                true
            } else {
                false
            }
        }
    }

    private fun observeNavigationEvents() {
        authViewModel.navigateToLogin.observe(this) { navigate ->
            if (navigate) {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment is RegisterFragment) {
                    Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                }
                replaceFragment(LoginFragment())
                binding.bottomNavigation.selectedItemId = R.id.navigation_login
                authViewModel.onNavigationToLoginDone()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
            .commit()
    }
}