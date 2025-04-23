package com.example.week5

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.* // Import widgets
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView // <<<---- IMPORT YANG BENAR
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CustomerTopupFragment : Fragment() {

    private lateinit var viewModel: CustomerTopupViewModel
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels() // Use shared VM for balance consistency

    // Views
    private lateinit var tvCurrentBalance: TextView
    private lateinit var etTopupAmount: TextInputEditText
    private lateinit var btnTopup: Button
    private lateinit var progressBar: ProgressBar
    private var bottomNavView: BottomNavigationView? = null

    // Listener defined as a property using the correct type
    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item -> // <<<---- TIPE LISTENER YANG BENAR
        // Prevent re-selection
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false
        }
        when (item.itemId) {
            R.id.customerProductsFragment -> {
                findNavController().navigate(R.id.action_customerTopupFragment_to_customerProductsFragment)
                true
            }
            R.id.customerHistoryFragment -> {
                findNavController().navigate(R.id.action_customerTopupFragment_to_customerHistoryFragment)
                true
            }
            R.id.customerTopupFragment -> {
                // Already here
                true
            }
            R.id.logout -> {
                SessionManager.logout()
                findNavController().navigate(R.id.action_global_logout_to_loginFragment)
                true
            }
            else -> false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[CustomerTopupViewModel::class.java]
        return inflater.inflate(R.layout.fragment_customer_topup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        // setupBottomNavigation() // Moved to onResume
        setupClickListeners()
        setupObservers()

        // Load initial balance from ViewModel, but also observe SharedViewModel for external updates
        viewModel.loadBalance(SessionManager.loggedInUserEmail)
        observeSharedBalance() // Start observing the shared balance
    }

    private fun setupViews(view: View) {
        tvCurrentBalance = view.findViewById(R.id.tvCurrentBalance)
        etTopupAmount = view.findViewById(R.id.etTopupAmount)
        btnTopup = view.findViewById(R.id.btnTopup)
        progressBar = view.findViewById(R.id.progressBarTopup)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_customer)
    }

    // --- Lifecycle aware listener handling ---
    override fun onResume() {
        super.onResume()
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener) // <<<---- Listener diterapkan di sini
        // Ensure the correct item is selected
        bottomNavView?.selectedItemId = R.id.customerTopupFragment
    }

    override fun onPause() {
        super.onPause()
        // Remove listener when fragment is not active
        bottomNavView?.setOnItemSelectedListener(null)
    }
    // --- End Lifecycle aware listener handling ---


    private fun setupClickListeners() {
        btnTopup.setOnClickListener {
            val amount = etTopupAmount.text.toString()
            // Hide keyboard after button press
            hideKeyboard()
            viewModel.topUp(SessionManager.loggedInUserEmail, amount)
        }
    }

    private fun setupObservers() {
        // Observe balance managed by this specific ViewModel (might be slightly delayed compared to shared)
        viewModel.currentBalance.observe(viewLifecycleOwner) { balance ->
            // Update UI if needed, but sharedViewModel observer is likely preferred for immediate updates
            // tvCurrentBalance.text = viewModel.formatCurrency(balance)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnTopup.isEnabled = !isLoading
            etTopupAmount.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showSnackbar(it, true)
                viewModel.resetTopupStatus() // Reset error after showing
            }
        }

        viewModel.topupStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TopupResult.Success -> {
                    showSnackbar(result.message, false)
                    etTopupAmount.text?.clear() // Clear input field
                    // Update shared view model as well for instant reflection across fragments
                    sharedViewModel.updateUserBalance(result.newBalance)
                    viewModel.resetTopupStatus() // Reset status after handling
                }
                is TopupResult.Error -> {
                    // Error message is now handled by the _error LiveData observer
                    // No need to call reset here as error observer does it
                }
                TopupResult.Idle -> { /* Do nothing */ }
            }
        }
    }

    // Observe the balance from the Shared ViewModel for immediate UI updates
    private fun observeSharedBalance() {
        sharedViewModel.balance.observe(viewLifecycleOwner) { balance ->
            // Update the TextView directly from the shared source of truth
            tvCurrentBalance.text = viewModel.formatCurrency(balance)
        }
    }


    private fun showSnackbar(message: String, isError: Boolean) {
        view?.let { currentView -> // Use a different variable name to avoid conflict
            val snackbar = Snackbar.make(currentView, message, Snackbar.LENGTH_LONG)
            val color = if (isError) android.R.color.holo_red_dark else android.R.color.holo_green_dark
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), color))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            snackbar.show()
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}