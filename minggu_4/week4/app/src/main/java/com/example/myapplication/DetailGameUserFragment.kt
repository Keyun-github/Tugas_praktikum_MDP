package com.example.myapplication // <-- Sesuaikan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.example.myapplication.databinding.FragmentDetailGameUserBinding
import java.text.NumberFormat
import java.util.Locale

class DetailGameUserFragment : Fragment() {

    private var _binding: FragmentDetailGameUserBinding? = null
    private val binding get() = _binding!!

    private val args: DetailGameUserFragmentArgs by navArgs()
    private var currentGame: Game? = null // Store the fetched game

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailGameUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadGameData()
        updateWalletDisplay()
        setupBuyButton()
    }

    private fun loadGameData() {
        val gameNameFromArgs = args.gameName
        currentGame = GameRepository.findGameByName(gameNameFromArgs)

        currentGame?.let { game ->
            binding.textViewDetailGameName.text = game.name
            binding.textViewDetailGameGenre.text = game.genres.joinToString(", ")
            binding.textViewDetailGameDescription.text = game.description
            binding.textViewDetailGamePrice.text = formatCurrency(game.price, game.isFree)
        } ?: run {
            // Handle game not found
            Toast.makeText(requireContext(), "Error: Game not found!", Toast.LENGTH_SHORT).show()
            activity?.onBackPressedDispatcher?.onBackPressed() // Go back
        }
    }

    private fun updateWalletDisplay() {
        val currentBalance = UserCredentialsManager.getWalletBalance(requireContext())
        binding.textViewWalletBalance.text = formatCurrency(currentBalance, false) // Format balance
    }


    private fun setupBuyButton() {
        currentGame?.let { game ->
            val isOwned = UserCredentialsManager.isGameInLibrary(requireContext(), game.name)

            if (isOwned) {
                showOwnedStatus()
            } else {
                showBuyButton()
                binding.buttonBuy.setOnClickListener {
                    handlePurchase(game)
                }
            }
        }
    }

    private fun handlePurchase(game: Game) {
        val currentBalance = UserCredentialsManager.getWalletBalance(requireContext())

        if (game.isFree) { // Handle free games
            purchaseGame(game)
        } else if (currentBalance >= game.price) {
            purchaseGame(game)
        } else {
            Toast.makeText(requireContext(), "Insufficient wallet balance!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun purchaseGame(game: Game) {
        // Deduct price (only if not free)
        if (!game.isFree) {
            UserCredentialsManager.updateWalletBalance(requireContext(), -game.price)
        }
        // Add to library
        val added = UserCredentialsManager.addGameToLibrary(requireContext(), game.name)

        if(added) {
            Toast.makeText(requireContext(), "${game.name} added to library!", Toast.LENGTH_SHORT).show()
            updateWalletDisplay() // Update displayed balance
            showOwnedStatus() // Update button to show owned status
        } else {
            Toast.makeText(requireContext(), "Failed to add game to library.", Toast.LENGTH_SHORT).show()
            // Optional: Re-enable buy button or show error state
        }
    }

    private fun showOwnedStatus() {
        binding.buttonBuy.visibility = View.GONE
        binding.textViewOwnedStatus.visibility = View.VISIBLE
        binding.textViewOwnedStatus.text = "${currentGame?.name ?: "Game"} is already in your Kukus library"
    }

    private fun showBuyButton() {
        binding.buttonBuy.visibility = View.VISIBLE
        binding.textViewOwnedStatus.visibility = View.GONE
    }


    private fun formatCurrency(value: Long, isFree: Boolean): String {
        if (isFree && value == 0L) return "Free" // Check specifically for free games
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0 // Optional: Remove cents/decimal part
        return formatter.format(value)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}