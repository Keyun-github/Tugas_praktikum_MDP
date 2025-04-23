package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemGameAdminBinding
import java.text.NumberFormat
import java.util.Locale

class GameAdapter(
    private var games: List<Game>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    inner class GameViewHolder(val binding: ItemGameAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(games[position].name)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun getItemCount(): Int = games.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val currentGame = games[position]
        holder.binding.textViewGameName.text = currentGame.name
        holder.binding.textViewGameGenre.text = currentGame.genres.joinToString(", ")

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedPrice = if (currentGame.isFree) "Free" else formatter.format(currentGame.price)
        holder.binding.textViewGamePrice.text = formattedPrice
    }

    fun updateData(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }
}