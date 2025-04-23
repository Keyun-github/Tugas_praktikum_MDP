package com.example.myapplication

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
import com.example.myapplication.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameAdapter: GameAdapter
    private var currentFilter = "No Filter"
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
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
        gameAdapter = GameAdapter(emptyList()) { gameName ->
            val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToDetailGameAdminFragment(gameName)
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
            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }
    }

    private fun setupSearch() {
        binding.buttonSearch.setOnClickListener {
            currentSearchQuery = binding.editTextSearch.text.toString().trim()
            filterAndSortGames()
        }
        binding.editTextSearch.addTextChangedListener { text ->
            currentSearchQuery = text.toString().trim()

        }
    }

    private fun filterAndSortGames() {
        var filteredGames = GameRepository.games.toList()

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

        if (filteredGames.isEmpty() && GameRepository.games.isNotEmpty()) {
            binding.textViewEmptyGames.text = "No games found matching search/filter"
            binding.textViewEmptyGames.visibility = View.VISIBLE
            binding.recyclerViewGames.visibility = View.GONE
        } else if (GameRepository.games.isEmpty()){
            binding.textViewEmptyGames.text = "No games added yet"
            binding.textViewEmptyGames.visibility = View.VISIBLE
            binding.recyclerViewGames.visibility = View.GONE
        }
        else {
            binding.textViewEmptyGames.visibility = View.GONE
            binding.recyclerViewGames.visibility = View.VISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}