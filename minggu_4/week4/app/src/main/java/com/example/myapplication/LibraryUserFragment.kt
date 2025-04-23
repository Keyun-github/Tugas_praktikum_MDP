package com.example.myapplication // <-- Sesuaikan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentLibraryUserBinding

class LibraryUserFragment : Fragment() {

    private var _binding: FragmentLibraryUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: GameAdapter // Reuse adapter
    private var ownedGames = listOf<Game>() // Store the full owned list

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadOwnedGames()
        setupRecyclerView()
        setupSearch()
        displayGames(ownedGames) // Display initial list
    }

    private fun loadOwnedGames() {
        val ownedGameNames = UserCredentialsManager.getUserLibraryGameNames(requireContext())
        ownedGames = GameRepository.games.filter { ownedGameNames.contains(it.name) }
    }

    private fun setupRecyclerView() {
        // Navigate to DetailGameUserFragment
        libraryAdapter = GameAdapter(emptyList()) { gameName ->
            val action = LibraryUserFragmentDirections.actionLibraryUserFragmentToDetailGameUserFragment(gameName)
            findNavController().navigate(action)
        }
        binding.recyclerViewLibrary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = libraryAdapter
        }
    }

    private fun setupSearch() {
        binding.buttonSearchLibrary.setOnClickListener {
            filterLibrary(binding.editTextSearchLibrary.text.toString().trim())
        }
        binding.editTextSearchLibrary.addTextChangedListener { text ->
            // Optional: real-time search (consider debounce)
            // filterLibrary(text.toString().trim())
        }
    }

    private fun filterLibrary(query: String) {
        val filteredList = if (query.isBlank()) {
            ownedGames // Show all owned if query is blank
        } else {
            ownedGames.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        displayGames(filteredList)
    }

    private fun displayGames(gamesToShow: List<Game>) {
        libraryAdapter.updateData(gamesToShow)
        binding.textViewEmptyLibrary.visibility = if (gamesToShow.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewLibrary.visibility = if (gamesToShow.isEmpty()) View.GONE else View.VISIBLE

        if(gamesToShow.isEmpty() && ownedGames.isNotEmpty()){
            binding.textViewEmptyLibrary.text = "No games found in library matching search"
        } else if (ownedGames.isEmpty()){
            binding.textViewEmptyLibrary.text = "Your library is empty"
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}