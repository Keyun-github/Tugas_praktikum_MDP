package com.example.week6

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.week6.databinding.ItemCommentBinding // Pastikan import binding benar
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter : ListAdapter<CommentWithUser, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentWithUser = getItem(position)
        holder.bind(commentWithUser)
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(commentWithUser: CommentWithUser) {
            binding.textViewCommentUsername.text = commentWithUser.username
            binding.textViewCommentText.text = commentWithUser.text
            binding.textViewCommentTimestamp.text = dateFormat.format(commentWithUser.createdAt)
        }
    }
}

// DiffUtil untuk Comments
class CommentDiffCallback : DiffUtil.ItemCallback<CommentWithUser>() {
    override fun areItemsTheSame(oldItem: CommentWithUser, newItem: CommentWithUser): Boolean {
        return oldItem.comment.id == newItem.comment.id
    }

    override fun areContentsTheSame(oldItem: CommentWithUser, newItem: CommentWithUser): Boolean {
        return oldItem == newItem // Data class bisa dibandingkan langsung jika isinya sama
    }
}