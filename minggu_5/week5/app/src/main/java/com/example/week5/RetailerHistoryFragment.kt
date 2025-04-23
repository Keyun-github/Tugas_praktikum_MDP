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
import com.google.android.material.navigation.NavigationBarView

class RetailerHistoryFragment : Fragment() {

    private lateinit var viewModel: RetailerHistoryViewModel
    private lateinit var historyAdapter: RetailerHistoryAdapter
    private lateinit var rvHistory: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoHistory: TextView
    private var bottomNavView: BottomNavigationView? = null

    private val bottomNavListener = NavigationBarView.OnItemSelectedListener { item ->
        if (item.itemId == findNavController().currentDestination?.id) {
            return@OnItemSelectedListener false
        }
        when (item.itemId) {
            R.id.retailerProductsFragment -> {
                findNavController().navigate(R.id.action_retailerHistoryFragment_to_retailerProductsFragment)
                true
            }
            R.id.retailerHistoryFragment -> true // Already here
            R.id.withdrawFragment -> {
                findNavController().navigate(R.id.action_retailerHistoryFragment_to_withdrawFragment)
                true
            }
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
        viewModel = ViewModelProvider(this)[RetailerHistoryViewModel::class.java]
        return inflater.inflate(R.layout.fragment_retailer_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        setupObservers()
        viewModel.loadHistory(SessionManager.loggedInUserEmail)
    }

    private fun setupViews(view: View) {
        rvHistory = view.findViewById(R.id.rvRetailerHistory)
        progressBar = view.findViewById(R.id.progressBarRetailerHistory)
        tvNoHistory = view.findViewById(R.id.tvNoRetailerHistory)
        bottomNavView = activity?.findViewById(R.id.bottom_navigation_retailer)
    }

    // --- Lifecycle aware listener handling ---
    override fun onResume() {
        super.onResume()
        bottomNavView?.visibility = View.VISIBLE
        bottomNavView?.setOnItemSelectedListener(bottomNavListener)
        bottomNavView?.selectedItemId = R.id.retailerHistoryFragment // Ensure correct item is selected
    }

    override fun onPause() {
        super.onPause()
        bottomNavView?.setOnItemSelectedListener(null) // Remove listener
    }
    // --- End Lifecycle aware listener handling ---


    private fun setupRecyclerView() {
        historyAdapter = RetailerHistoryAdapter(emptyList())
        rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.historyItems.observe(viewLifecycleOwner) { items ->
            historyAdapter.updateHistory(items)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            val isEmpty = viewModel.isEmpty.value ?: true
            if (isLoading) {
                rvHistory.visibility = View.GONE
                tvNoHistory.visibility = View.GONE
            } else {
                tvNoHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            val isLoading = viewModel.isLoading.value ?: false
            if (!isLoading) {
                tvNoHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }
}
