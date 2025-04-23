package com.example.myapplication // <-- Sesuaikan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentUserHomeBinding // Use correct binding

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameAdapter: GameAdapter // Reuse GameAdapter
    private var currentFilter = "No Filter"
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSpinner()
        setupSearch()
        filterAndSortGames()
    }

    private fun setupRecyclerView() {
        // Navigate to DetailGameUserFragment
        gameAdapter = GameAdapter(emptyList()) { gameName ->
            val action = UserHomeFragmentDirections.actionUserHomeFragmentToDetailGameUserFragment(gameName)
            findNavController().navigate(action)
        }
        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gameAdapter
        }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("No Filter", "A-Z", "Z-A", "Price Lowest-Highest", "Price Highest-Lowest")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = filterOptions[position]
                filterAndSortGames()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.buttonSearch.setOnClickListener {
            currentSearchQuery = binding.editTextSearch.text.toString().trim()
            filterAndSortGames()
        }
        binding.editTextSearch.addTextChangedListener { text ->
            currentSearchQuery = text.toString().trim()
            // Optional: Real-time search (consider debouncing)
            // filterAndSortGames()
        }
    }

    private fun filterAndSortGames() {
        var filteredGames = GameRepository.games.toList() // Get all available games

        if (currentSearchQuery.isNotEmpty()) {
            filteredGames = filteredGames.filter {
                it.name.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        when (currentFilter) {
            "A-Z" -> filteredGames = filteredGames.sortedBy { it.name }
            "Z-A" -> filteredGames = filteredGames.sortedByDescending { it.name }
            "Price Lowest-Highest" -> filteredGames = filteredGames.sortedWith(compareBy<Game>{ it.isFree }.thenBy { it.price })
            "Price Highest-Lowest" -> filteredGames = filteredGames.sortedWith(compareByDescending<Game>{ it.isFree }.thenByDescending { it.price })
        }

        gameAdapter.updateData(filteredGames)

        binding.textViewEmptyGames.visibility = if (filteredGames.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewGames.visibility = if (filteredGames.isEmpty()) View.GONE else View.VISIBLE
        if(filteredGames.isEmpty()) binding.textViewEmptyGames.text = "No games found matching search/filter"

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}