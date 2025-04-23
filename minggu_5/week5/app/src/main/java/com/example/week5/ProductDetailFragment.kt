package com.example.week5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.* // Import Button, TextView etc.
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class ProductDetailFragment : Fragment() {

    private lateinit var viewModel: ProductDetailViewModel
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvRetailerName: TextView
    private lateinit var tvStock: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnMinus: Button
    private lateinit var btnPlus: Button
    private lateinit var tvQuantity: TextView
    private lateinit var tvYourBalance: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvResultBalance: TextView
    private lateinit var btnBuy: Button
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[ProductDetailViewModel::class.java]
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupToolbar()
        setupClickListeners()
        setupObservers()

        // Load product data based on argument
        viewModel.loadProduct(args.productId)
        // Load current user balance (needed for calculation and purchase check)
        sharedViewModel.loadCurrentUser() // Ensure sharedVM has the user loaded
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tvProductName = view.findViewById(R.id.tvDetailProductName)
        tvProductPrice = view.findViewById(R.id.tvDetailProductPrice)
        tvRetailerName = view.findViewById(R.id.tvDetailRetailerName)
        tvStock = view.findViewById(R.id.tvDetailStock)
        tvDescription = view.findViewById(R.id.tvDetailDescription)
        btnMinus = view.findViewById(R.id.btnMinus)
        btnPlus = view.findViewById(R.id.btnPlus)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        tvYourBalance = view.findViewById(R.id.tvYourBalance)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        tvResultBalance = view.findViewById(R.id.tvResultBalance)
        btnBuy = view.findViewById(R.id.btnBuy)
        progressBar = view.findViewById(R.id.progressBarDetail)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Go back
        }
    }

    private fun setupClickListeners() {
        btnMinus.setOnClickListener { viewModel.decrementQuantity() }
        btnPlus.setOnClickListener { viewModel.incrementQuantity() }
        btnBuy.setOnClickListener {
            val currentUserEmail = SessionManager.loggedInUserEmail
            val currentBalance = sharedViewModel.balance.value ?: 0.0 // Get balance from sharedVM
            if (currentUserEmail != null) {
                viewModel.buyProduct(currentUserEmail, currentBalance)
            } else {
                showSnackbar("Error: User not logged in.", true)
            }
        }
    }

    private fun setupObservers() {
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                tvProductName.text = it.name
                tvProductPrice.text = viewModel.formatCurrency(it.price)
                tvRetailerName.text = it.retailerName
                tvStock.text = it.stock.toString()
                tvDescription.text = it.description
                // Recalculate total price if product changes (e.g., after purchase/stock update)
                viewModel.quantity.value?.let { qty ->
                    viewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice -> // Observe total price
                        tvTotalPrice.text = viewModel.formatCurrency(totalPrice)
                        updateResultBalance() // Update result whenever total price changes
                    }
                }
            }
        }

        viewModel.quantity.observe(viewLifecycleOwner) { quantity ->
            tvQuantity.text = quantity.toString()
            // No need to observe total price here again if observed above
        }

        // Observe balance from Shared ViewModel
        sharedViewModel.balance.observe(viewLifecycleOwner) { balance ->
            tvYourBalance.text = viewModel.formatCurrency(balance)
            updateResultBalance() // Update result balance when user balance changes
        }

        viewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice -> // Moved observation here
            tvTotalPrice.text = viewModel.formatCurrency(totalPrice)
            updateResultBalance()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnBuy.isEnabled = !isLoading
            btnMinus.isEnabled = !isLoading
            btnPlus.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showSnackbar(it, true)
                viewModel.resetPurchaseStatus() // Reset error after showing
            }
        }

        viewModel.purchaseStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PurchaseResult.Success -> {
                    showSnackbar(result.message, false)
                    // Update shared view model balance immediately
                    sharedViewModel.updateUserBalance(result.newBalance)
                    viewModel.resetPurchaseStatus() // Reset status after handling
                }
                is PurchaseResult.Error -> {
                    // Error message is now handled by the _error LiveData observer
                    // showSnackbar(result.message, true)
                    // viewModel.resetPurchaseStatus() // Reset status after handling
                }
                PurchaseResult.Idle -> { /* Do nothing */ }
            }
        }
    }

    private fun updateResultBalance() {
        val balance = sharedViewModel.balance.value ?: 0.0
        val total = viewModel.totalPrice.value ?: 0.0
        val result = balance - total
        tvResultBalance.text = viewModel.formatCurrency(result)

        // Set text color based on result
        val colorRes = if (result < 0) android.R.color.holo_red_dark else android.R.color.holo_green_dark
        context?.let {
            tvResultBalance.setTextColor(ContextCompat.getColor(it, colorRes))
        }
        // Disable buy button if balance would go negative or stock is zero
        btnBuy.isEnabled = result >= 0 && (viewModel.product.value?.stock ?: 0) >= (viewModel.quantity.value ?: 1)

    }

    private fun showSnackbar(message: String, isError: Boolean) {
        view?.let {
            val snackbar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            if (isError) {
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            } else {
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            }
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            snackbar.show()
        }
    }
}