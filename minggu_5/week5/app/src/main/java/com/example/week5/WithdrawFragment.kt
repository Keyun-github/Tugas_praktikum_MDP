package com.example.week5

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class WithdrawFragment : Fragment() {

    private lateinit var viewModel: WithdrawViewModel
    private val retailerSharedViewModel: RetailerSharedViewModel by activityViewModels() // Use shared VM for balance

    // Views
    private lateinit var tvCurrentBalance: TextView
    private lateinit var etWithdrawAmount: TextInputEditText
    private lateinit var btnWithdraw: Button
    private lateinit var progressBar: ProgressBar
    private var bottomNavView: BottomNavigationView? = null


    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item ->
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false
        }
        when (item.itemId) {
            R.id.retailerProductsFragment -> {
                findNavController().navigate(R.id.action_withdrawFragment_to_retailerProductsFragment)
                true
            }
            R.id.retailerHistoryFragment -> {
                findNavController().navigate(R.id.action_withdrawFragment_to_retailerHistoryFragment)
                true
            }
            R.id.withdrawFragment -> true // Already here
            R.id.logout_retailer -> {
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
        viewModel = ViewModelProvider(this)[WithdrawViewModel::class.java]
        return inflater.inflate(R.layout.fragment_withdraw, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners()
        setupObservers()

        // Load initial balance & observe shared balance
        viewModel.loadBalance(SessionManager.loggedInUserEmail)
        observeSharedBalance()
    }

    private fun setupViews(view: View) {
        tvCurrentBalance = view.findViewById(R.id.tvCurrentBalanceWithdraw)
        etWithdrawAmount = view.findViewById(R.id.etWithdrawAmount)
        btnWithdraw = view.findViewById(R.id.btnWithdraw)
        progressBar = view.findViewById(R.id.progressBarWithdraw)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_retailer)
    }

    // --- Lifecycle aware listener handling ---
    override fun onResume() {
        super.onResume()
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener)
        bottomNavView?.selectedItemId = R.id.withdrawFragment // Ensure correct item selected
    }

    override fun onPause() {
        super.onPause()
        bottomNavView?.setOnItemSelectedListener(null) // Remove listener
    }
    // --- End Lifecycle aware listener handling ---

    private fun setupClickListeners() {
        btnWithdraw.setOnClickListener {
            hideKeyboard()
            viewModel.withdraw(
                SessionManager.loggedInUserEmail,
                etWithdrawAmount.text.toString()
            )
        }
    }

    private fun setupObservers() {
        // Observe balance from shared view model for real-time UI update
        observeSharedBalance()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnWithdraw.isEnabled = !isLoading
            etWithdrawAmount.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showSnackbar(it, true)
                viewModel.resetWithdrawStatus() // Clear error
            }
        }

        viewModel.withdrawStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is WithdrawResult.Success -> {
                    showSnackbar(result.message, false)
                    etWithdrawAmount.text?.clear() // Clear input
                    // Update shared view model instantly
                    retailerSharedViewModel.updateUserBalance(result.newBalance)
                    viewModel.resetWithdrawStatus()
                }
                is WithdrawResult.Error -> {
                    // Error message handled by the _error observer
                }
                WithdrawResult.Idle -> { /* Do nothing */ }
            }
        }
    }

    private fun observeSharedBalance() {
        retailerSharedViewModel.balance.observe(viewLifecycleOwner) { balance ->
            tvCurrentBalance.text = viewModel.formatCurrency(balance)
        }
    }


    private fun showSnackbar(message: String, isError: Boolean) {
        view?.let { currentView ->
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