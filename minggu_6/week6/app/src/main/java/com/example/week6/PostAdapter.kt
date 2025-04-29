package com.example.week6

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.week6.databinding.ItemPostBinding // Pastikan import binding benar
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val onPostClick: (PostWithDetails) -> Unit,
    private val onVoteClick: (postId: Int, voteAction: Int) -> Unit, // 1 for up, -1 for down
    private val currentUserId: Int // User ID untuk cek vote state
) : ListAdapter<PostWithDetails, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    // Formatter tanggal bisa dibuat static atau di luar agar lebih efisien
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val postWithDetails = getItem(position)
        holder.bind(postWithDetails)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Klik pada seluruh item card
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPostClick(getItem(position))
                }
            }
            // Klik pada tombol upvote
            binding.imageViewItemUpvote.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVoteClick(getItem(position).post.id, 1) // 1 for upvote
                }
            }
            // Klik pada tombol downvote
            binding.imageViewItemDownvote.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVoteClick(getItem(position).post.id, -1) // -1 for downvote
                }
            }
        }

        fun bind(postWithDetails: PostWithDetails) {
            binding.textViewItemCommunity.text = "r/${postWithDetails.community.name}"
            // Format info: User • Timestamp
            val formattedDate = dateFormat.format(postWithDetails.createdAt)
            binding.textViewItemPostInfo.text = "• Posted by ${postWithDetails.username} • $formattedDate"
            binding.textViewItemTitle.text = postWithDetails.post.title
            binding.textViewItemDescription.text = postWithDetails.post.description
            binding.textViewItemUpvotes.text = postWithDetails.post.upvotes.toString()
            binding.textViewItemDownvotes.text = postWithDetails.post.downvotes.toString()
            // Gunakan elvis operator untuk commentCount karena nullable
            binding.textViewItemCommentsCount.text = (postWithDetails.commentCount ?: 0).toString()

            // Update tampilan vote button berdasarkan userVoteType (sudah nullable)
            val voteType = postWithDetails.userVoteType ?: 0 // Ambil nilai atau 0 jika null
            val context = binding.root.context
            binding.imageViewItemUpvote.setColorFilter(
                ContextCompat.getColor(
                    context,
                    if (voteType == 1) R.color.reddid_orange else R.color.icon_tint_normal
                )
            )
            binding.imageViewItemDownvote.setColorFilter(
                ContextCompat.getColor(
                    context,
                    // Ganti warna ungu jika perlu
                    if (voteType == -1) R.color.purple_500 else R.color.icon_tint_normal
                )
            )
        }
    }
}

// DiffUtil untuk performa RecyclerView
class PostDiffCallback : DiffUtil.ItemCallback<PostWithDetails>() {
    override fun areItemsTheSame(oldItem: PostWithDetails, newItem: PostWithDetails): Boolean {
        return oldItem.post.id == newItem.post.id
    }

    override fun areContentsTheSame(oldItem: PostWithDetails, newItem: PostWithDetails): Boolean {
        // Bandingkan konten yang relevan untuk update UI
        // Perhatikan bahwa userVoteType dan commentCount sekarang nullable
        return oldItem.post == newItem.post &&
                oldItem.community == newItem.community &&
                oldItem.user.username == newItem.user.username &&
                oldItem.commentCount == newItem.commentCount && // Perbandingan nullable
                oldItem.userVoteType == newItem.userVoteType   // Perbandingan nullable
    }
}