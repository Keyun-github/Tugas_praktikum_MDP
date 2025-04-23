package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.databinding.FragmentDetailGameAdminBinding
import java.text.NumberFormat
import java.util.Locale

class DetailGameAdminFragment : Fragment() {

    private var _binding: FragmentDetailGameAdminBinding? = null
    private val binding get() = _binding!!

    private val args: DetailGameAdminFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailGameAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameNameFromArgs = args.gameName

        val game = GameRepository.findGameByName(gameNameFromArgs)

        if (game != null) {
            binding.textViewDetailGameName.text = game.name
            binding.textViewDetailGameGenre.text = game.genres.joinToString(", ")
            binding.textViewDetailGameDescription.text = game.description

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val formattedPrice = if (game.isFree) "Free" else formatter.format(game.price)
            binding.textViewDetailGamePrice.text = formattedPrice
        } else {
            Toast.makeText(requireContext(), "Error: Game '$gameNameFromArgs' not found!", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}