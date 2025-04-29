package com.example.week6

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.week6.databinding.FragmentDetailPostBinding
import java.text.SimpleDateFormat
import java.util.*

class DetailPostFragment : Fragment() {

    private var _binding: FragmentDetailPostBinding? = null
    private val binding get() = _binding!!

    private val args: DetailPostFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var commentAdapter: CommentAdapter

    private var currentUserId: Int? = null
    private var currentPostId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPostBinding.inflate(inflater, container, false)
        currentPostId = args.postId
        Log.d("DetailPostFragment", "Received postId: $currentPostId")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.loggedInUserId.observe(viewLifecycleOwner) { userId ->
            if (userId != null && currentPostId != -1) {
                if (currentUserId == null || currentUserId != userId) {
                    currentUserId = userId
                    Log.d("DetailPostFragment", "User ID: $userId, Post ID: $currentPostId")
                    setupViewModel(currentPostId, userId)
                    setupRecyclerView()
                    setupUIListeners()
                    observeViewModel()
                }
            } else {
                Log.e("DetailPostFragment", "User ID ($userId) or Post ID ($currentPostId) is invalid")
                Toast.makeText(context, "Error loading post details.", Toast.LENGTH_SHORT).show()
                if (findNavController().currentDestination?.id == R.id.detailPostFragment) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupViewModel(postId: Int, userId: Int) {
        val factory = DetailViewModel.Factory(requireActivity().application, postId, userId)
        detailViewModel = ViewModelProvider(this, factory)[DetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupUIListeners() {
        binding.buttonAddComment.setOnClickListener {
            val commentText = binding.editTextComment.text.toString()
            detailViewModel.addComment(commentText)
            val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }

        binding.imageViewUpvote.setOnClickListener { detailViewModel.handleVote(1) }
        binding.imageViewDownvote.setOnClickListener { detailViewModel.handleVote(-1) }
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
    }


    private fun observeViewModel() {
        detailViewModel.combinedPostDetails.observe(viewLifecycleOwner) { postWithDetails ->
            if (postWithDetails != null) {
                binding.textViewCommunity.text = "r/${postWithDetails.community.name}"
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = sdf.format(postWithDetails.createdAt)
                binding.textViewPostInfo.text = "Posted by ${postWithDetails.username} • $formattedDate"
                binding.textViewPostTitle.text = postWithDetails.post.title
                binding.textViewPostDescription.text = postWithDetails.post.description
                binding.textViewUpvotes.text = postWithDetails.post.upvotes.toString()
                binding.textViewDownvotes.text = postWithDetails.post.downvotes.toString()

                updateVoteButtonStates(postWithDetails.userVoteType ?: 0)
            } else {
                Log.w("DetailPostFragment", "Observed combinedPostDetails is null for postId: $currentPostId")
                if (isAdded && findNavController().currentDestination?.id == R.id.detailPostFragment) {
                    Toast.makeText(context, "Post not found or deleted.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }

        detailViewModel.comments.observe(viewLifecycleOwner) { commentsList ->
            Log.d("DetailPostFragment", "Comments observed: ${commentsList?.size ?: 0}")
            commentAdapter.submitList(commentsList)
            // Update comment count dengan lebih aman dan menghilangkan warning
            binding.textViewCommentsCountDetail.text = (commentsList?.size ?: 0).toString()
        }

        detailViewModel.commentError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textFieldComment.error = error
            } else {
                binding.textFieldComment.error = null
                if (binding.textFieldComment.error == null) { // Hanya clear jika tidak ada error
                    binding.editTextComment.text?.clear()
                }
            }
        }
    }

    private fun updateVoteButtonStates(userVoteType: Int) {
        val context = context ?: return
        binding.imageViewUpvote.setColorFilter(
            ContextCompat.getColor(context,
                if (userVoteType == 1) R.color.reddid_orange
                else R.color.icon_tint_normal
            )
        )
        binding.imageViewDownvote.setColorFilter(
            ContextCompat.getColor(context,
                if (userVoteType == -1) R.color.purple_500
                else R.color.icon_tint_normal
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}