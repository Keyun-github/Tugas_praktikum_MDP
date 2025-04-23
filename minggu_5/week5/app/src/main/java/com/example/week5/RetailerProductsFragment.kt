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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

class RetailerProductsFragment : Fragment() {

    private lateinit var viewModel: RetailerProductsViewModel
    private lateinit var productAdapter: RetailerProductListAdapter
    private lateinit var etSearch: EditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoProducts: TextView
    private lateinit var fabAddProduct: FloatingActionButton
    private var bottomNavView: BottomNavigationView? = null

    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item ->
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false
        }
        when (item.itemId) {
            R.id.retailerProductsFragment -> true // Already here
            R.id.retailerHistoryFragment -> {
                findNavController().navigate(R.id.action_retailerProductsFragment_to_retailerHistoryFragment)
                true
            }
            R.id.withdrawFragment -> {
                findNavController().navigate(R.id.action_retailerProductsFragment_to_withdrawFragment)
                true
            }
            R.id.logout_retailer -> {
                SessionManager.logout()
                // Use the global logout action defined in nav_graph
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
        viewModel = ViewModelProvider(this)[RetailerProductsViewModel::class.java]
        return inflater.inflate(R.layout.fragment_retailer_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupClickListeners()

        // Load products for the current retailer
        viewModel.loadProducts(SessionManager.loggedInUserEmail)
    }

    private fun setupViews(view: View) {
        etSearch = view.findViewById(R.id.etSearchRetailer)
        rvProducts = view.findViewById(R.id.rvRetailerProducts)
        progressBar = view.findViewById(R.id.progressBarRetailer)
        tvNoProducts = view.findViewById(R.id.tvNoProductsRetailer)
        fabAddProduct = view.findViewById(R.id.fabAddProduct)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_retailer)
    }

    // --- Lifecycle aware listener handling ---
    override fun onResume() {
        super.onResume()
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener)
        bottomNavView?.selectedItemId = R.id.retailerProductsFragment // Ensure correct item is selected
        // Reload products in case navigated back from add/edit
        viewModel.loadProducts(SessionManager.loggedInUserEmail)
    }

    override fun onPause() {
        super.onPause()
        bottomNavView?.setOnItemSelectedListener(null) // Important: Remove listener
        // Optional visibility handling if needed when paused, though MainActivity often controls this
    }
    // --- End Lifecycle aware listener handling ---


    private fun setupRecyclerView() {
        productAdapter = RetailerProductListAdapter(emptyList()) { product ->
            // Navigate to AddEdit Fragment in Edit mode, passing product ID
            val action = RetailerProductsFragmentDirections.actionRetailerProductsFragmentToAddEditProductFragment(product.id)
            findNavController().navigate(action)
        }
        rvProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        fabAddProduct.setOnClickListener {
            // Navigate to AddEdit Fragment in Add mode (pass null productId)
            val action = RetailerProductsFragmentDirections.actionRetailerProductsFragmentToAddEditProductFragment(null)
            findNavController().navigate(action)
        }
    }


    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.updateProducts(products)
            val isEmpty = products.isNullOrEmpty()
            val isLoading = viewModel.isLoading.value ?: false
            if(!isLoading){ // Only update visibility if not loading
                tvNoProducts.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if(isLoading){ // Hide list and message when loading starts
                rvProducts.visibility = View.GONE
                tvNoProducts.visibility = View.GONE
            } else {
                // When loading finishes, let the products observer handle final visibility
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
                // Trigger search in ViewModel for the logged-in retailer's products
                viewModel.searchProducts(SessionManager.loggedInUserEmail, s.toString().trim())
            }
        })
    }
} // <<<--- Penutup Class Fragment