package com.example.week5

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView // <<<---- IMPORT INI

class CustomerProductsFragment : Fragment() {

    private lateinit var viewModel: CustomerProductsViewModel
    private val sharedViewModel: CustomerSharedViewModel by activityViewModels()
    private lateinit var productAdapter: ProductListAdapter
    private lateinit var etSearch: EditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoProducts: TextView
    private var bottomNavView: BottomNavigationView? = null

    // Listener defined as a property using the correct type
    // Handles navigation initiated from the BottomNavigationView
    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item -> // <<<---- GUNAKAN NavigationBarView
        // Prevent re-selecting the same item causing navigation loop or unnecessary actions
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false // Indicate item selection was not handled
        }
        when (item.itemId) {
            R.id.customerProductsFragment -> {
                // Already here, handled by the check above
                true // Indicate handled (although no action needed)
            }
            R.id.customerHistoryFragment -> {
                findNavController().navigate(R.id.action_customerProductsFragment_to_customerHistoryFragment)
                true // Indicate handled
            }
            R.id.customerTopupFragment -> {
                findNavController().navigate(R.id.action_customerProductsFragment_to_customerTopupFragment)
                true // Indicate handled
            }
            R.id.logout -> {
                SessionManager.logout() // Clear session data
                findNavController().navigate(R.id.action_global_logout_to_loginFragment) // Navigate to login
                true // Indicate handled
            }
            else -> false // Indicate not handled for other potential items
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[CustomerProductsViewModel::class.java]
        return inflater.inflate(R.layout.fragment_customer_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        // setupBottomNavigation() // Moved to onResume for lifecycle awareness
        setupObservers()
        setupSearch()

        // Load initial user data if needed by other parts or shared components
        sharedViewModel.loadCurrentUser()

        // Initial product load happens in ViewModel's init block
    }

    private fun setupViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)
        rvProducts = view.findViewById(R.id.rvProducts)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoProducts = view.findViewById(R.id.tvNoProducts)
        // Get reference to BottomNav in MainActivity
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_customer)
    }

    // --- Lifecycle aware listener handling for BottomNavigationView ---
    override fun onResume() {
        super.onResume()
        // Set visibility, set the listener, and select the correct item when fragment is active
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener) // <<<---- Tipe listener sudah benar
        // Ensure the correct item is visually selected when returning to this fragment
        bottomNavView?.selectedItemId = R.id.customerProductsFragment
    }

    override fun onPause() {
        super.onPause()
        // Remove the listener when the fragment is paused (no longer in the foreground)
        // This prevents the listener from triggering navigation actions when it shouldn't,
        // especially when another fragment (like ProductDetailFragment) is displayed.
        bottomNavView?.setOnItemSelectedListener(null)
        // Optional: Hide if needed, but MainActivity listener usually handles this based on destination
        // bottomNavView?.visibility = View.GONE
    }
    // --- End Lifecycle aware listener handling ---


    private fun setupRecyclerView() {
        productAdapter = ProductListAdapter(emptyList()) { product ->
            // Navigate to Detail Fragment, passing product ID
            val action = CustomerProductsFragmentDirections.actionCustomerProductsFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }
        rvProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.updateProducts(products)
            // Handle visibility based on whether the list is empty AND not currently loading
            val isLoading = viewModel.isLoading.value ?: false
            val isEmpty = products.isNullOrEmpty()
            if (!isLoading) { // Only update visibility if not loading
                tvNoProducts.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // When loading starts, hide the list and the "no products" message
            if(isLoading) {
                rvProducts.visibility = View.GONE
                tvNoProducts.visibility = View.GONE
            } else {
                // When loading finishes, the products observer will handle correct visibility
                // based on whether the list is empty or not. Trigger re-check just in case.
                val isEmpty = viewModel.products.value.isNullOrEmpty()
                tvNoProducts.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Trigger search in ViewModel after text changes (with debouncing in ViewModel)
                viewModel.searchProducts(s.toString().trim())
            }
        })
    }

    // onDestroyView is not needed for listener cleanup now as it's handled in onPause
}