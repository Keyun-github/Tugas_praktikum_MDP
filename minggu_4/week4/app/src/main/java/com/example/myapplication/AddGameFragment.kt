package com.example.myapplication // <-- Sesuaikan package name

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAddGameBinding

class AddGameFragment : Fragment() {

    private var _binding: FragmentAddGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // Listener untuk CheckBox "Is Free"
        binding.checkBoxIsFree.setOnCheckedChangeListener { _, isChecked ->
            binding.textFieldGamePrice.isEnabled = !isChecked // Disable price jika free
            if (isChecked) {
                binding.editTextGamePrice.setText("0") // Set harga 0 jika free
            }
        }

        // Listener untuk tombol Add Game
        binding.buttonAddGame.setOnClickListener {
            if (validateInput()) {
                addGameToRepository()
                Toast.makeText(requireContext(), "Game added successfully!", Toast.LENGTH_SHORT).show()
                // Kembali ke Admin Home
                // findNavController().popBackStack() // Cara 1: Kembali ke fragment sebelumnya
                // Cara 2: Navigasi spesifik (lebih aman jika backstack kompleks)
                findNavController().navigate(R.id.action_addGameFragment_to_adminHomeFragment)
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Reset errors
        binding.textFieldGameName.error = null
        binding.textFieldGameDescription.error = null
        binding.textFieldGamePrice.error = null
        binding.textViewGenreError.visibility = View.GONE

        // Validasi Nama
        if (binding.editTextGameName.text.isNullOrBlank()) {
            binding.textFieldGameName.error = "Name cannot be empty"
            isValid = false
        }

        // Validasi Deskripsi
        if (binding.editTextGameDescription.text.isNullOrBlank()) {
            binding.textFieldGameDescription.error = "Description cannot be empty"
            isValid = false
        }

        // Validasi Genre (minimal 1 dipilih)
        val selectedGenres = getSelectedGenres()
        if (selectedGenres.isEmpty()) {
            binding.textViewGenreError.visibility = View.VISIBLE
            isValid = false
        }

        // Validasi Harga (minimal 1000 jika tidak free)
        val isFree = binding.checkBoxIsFree.isChecked
        val priceString = binding.editTextGamePrice.text.toString()
        if (!isFree) {
            if (priceString.isBlank()) {
                binding.textFieldGamePrice.error = "Price cannot be empty if not free"
                isValid = false
            } else {
                try {
                    val price = priceString.toLong()
                    if (price < 1000) {
                        binding.textFieldGamePrice.error = "Minimum price is Rp 1000"
                        isValid = false
                    }
                } catch (e: NumberFormatException) {
                    binding.textFieldGamePrice.error = "Invalid price format"
                    isValid = false
                }
            }
        }

        return isValid
    }

    // Helper untuk mendapatkan list genre yang dipilih
    private fun getSelectedGenres(): List<String> {
        val genres = mutableListOf<String>()
        // Loop melalui semua CheckBox di dalam GridLayout
        binding.gridLayoutGenres.children.forEach { view ->
            if (view is CheckBox && view.isChecked) {
                genres.add(view.text.toString())
            }
        }
        return genres
    }

    private fun addGameToRepository() {
        val name = binding.editTextGameName.text.toString().trim()
        val description = binding.editTextGameDescription.text.toString().trim()
        val genres = getSelectedGenres()
        val isFree = binding.checkBoxIsFree.isChecked
        val price = if (isFree) 0L else binding.editTextGamePrice.text.toString().toLongOrNull() ?: 0L

        val newGame = Game(name, description, genres, isFree, price)
        GameRepository.addGame(newGame)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}