package com.example.week5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView // <<<---- IMPORT YANG BENAR

class CustomerHistoryFragment : Fragment() {

    private lateinit var viewModel: CustomerHistoryViewModel
    private lateinit var historyAdapter: HistoryListAdapter
    private lateinit var rvHistory: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoHistory: TextView
    private var bottomNavView: BottomNavigationView? = null

    // Listener defined as a property using the correct type
    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item -> // <<<---- TIPE LISTENER YANG BENAR
        // Prevent re-selection
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false
        }
        when (item.itemId) {
            R.id.customerProductsFragment -> {
                findNavController().navigate(R.id.action_customerHistoryFragment_to_customerProductsFragment)
                true
            }
            R.id.customerHistoryFragment -> {
                // Already here
                true
            }
            R.id.customerTopupFragment -> {
                findNavController().navigate(R.id.action_customerHistoryFragment_to_customerTopupFragment)
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
        viewModel = ViewModelProvider(this)[CustomerHistoryViewModel::class.java]
        return inflater.inflate(R.layout.fragment_customer_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        // setupBottomNavigation() // Moved to onResume
        setupObservers()

        // Load history for the logged-in user when the view is created
        viewModel.loadHistory(SessionManager.loggedInUserEmail)
    }

    private fun setupViews(view: View) {
        rvHistory = view.findViewById(R.id.rvHistory)
        progressBar = view.findViewById(R.id.progressBarHistory)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_customer)
    }

    // --- Lifecycle aware listener handling ---
    override fun onResume() {
        super.onResume()
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener) // <<<---- Listener diterapkan di sini
        // Ensure the correct item is selected
        bottomNavView?.selectedItemId = R.id.customerHistoryFragment
    }

    override fun onPause() {
        super.onPause()
        // Remove listener when fragment is not active
        bottomNavView?.setOnItemSelectedListener(null)
    }
    // --- End Lifecycle aware listener handling ---

    private fun setupRecyclerView() {
        historyAdapter = HistoryListAdapter(emptyList())
        rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.history.observe(viewLifecycleOwner) { historyList ->
            historyAdapter.updateHistory(historyList)
            // Visibility based on emptiness is handled in the isEmpty observer combined with loading state
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Control visibility of list/empty message based on loading AND emptiness
            val isEmpty = viewModel.isEmpty.value ?: true // Assume empty if not yet loaded
            if (isLoading) {
                rvHistory.visibility = View.GONE // Hide list while loading
                tvNoHistory.visibility = View.GONE // Hide empty message while loading
            } else {
                // Not loading, rely on isEmpty state
                tvNoHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            val isLoading = viewModel.isLoading.value ?: false // Check loading state again
            if (!isLoading) { // Only update based on emptiness if NOT loading
                tvNoHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }
}