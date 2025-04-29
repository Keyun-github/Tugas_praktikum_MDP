package com.example.week6

import android.util.Log
import androidx.lifecycle.LiveData
import java.util.Date

class PostRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val voteDao: VoteDao
) {

    // --- Post Operations ---

    // Mengembalikan LiveData<List<PostWithDetails>> dasar dari DAO
    fun getAllPostsWithDetails(): LiveData<List<PostWithDetails>> {
        return postDao.getAllPostsWithDetails()
    }

    // Fungsi suspend untuk search, mengembalikan List<PostWithDetails> dasar
    suspend fun searchPosts(query: String): List<PostWithDetails> {
        return postDao.searchPostsWithDetails(query)
    }

    // Mengembalikan LiveData<PostWithDetails?> dasar dari DAO
    fun getPostDetails(postId: Int): LiveData<PostWithDetails?> {
        return postDao.getPostWithDetailsById(postId)
    }

    // Fungsi suspend untuk mengambil data tambahan (dipanggil dari ViewModel)
    suspend fun getCommentCount(postId: Int): Int {
        // Memastikan tidak error jika post tidak ada
        return try { postDao.getCommentCountForPost(postId) } catch (e: Exception) { 0 }
    }

    suspend fun getVoteState(userId: Int, postId: Int): Vote? {
        return try { voteDao.getVoteByUserAndPost(userId, postId) } catch (e: Exception) { null }
    }

    suspend fun getVotesForPostList(userId: Int, postIds: List<Int>): Map<Int, Vote> {
        return try { voteDao.getVotesByUserForPosts(userId, postIds).associateBy { it.postId } } catch (e: Exception) { emptyMap() }
    }


    suspend fun insertPost(title: String, description: String, communityId: Int, userId: Int) {
        val post = Post(
            userId = userId,
            communityId = communityId,
            title = title,
            description = description,
            createdAt = Date()
        )
        postDao.insertPost(post)
    }

    // --- Comment Operations ---

    fun getCommentsForPost(postId: Int): LiveData<List<CommentWithUser>> {
        return commentDao.getCommentsWithUserForPost(postId)
    }

    suspend fun addComment(postId: Int, userId: Int, text: String) {
        if (text.isNotBlank()) {
            val comment = Comment(
                postId = postId,
                userId = userId,
                text = text,
                createdAt = Date()
            )
            commentDao.insertComment(comment)
        }
    }

    // --- Vote Operations ---
    suspend fun handleVote(postId: Int, userId: Int, voteAction: Int) {
        val currentVote = try { voteDao.getVoteByUserAndPost(userId, postId) } catch (e: Exception) { null } // Tangani error potensial
        val currentVoteType = currentVote?.voteType ?: 0

        when {
            currentVoteType == voteAction -> {
                voteDao.deleteVote(userId, postId)
                if (voteAction == 1) postDao.changeUpvoteCount(postId, -1)
                else postDao.changeDownvoteCount(postId, -1)
            }
            currentVoteType == -voteAction -> {
                val newVote = Vote(userId, postId, voteAction)
                voteDao.upsertVote(newVote)
                if (voteAction == 1) {
                    postDao.changeUpvoteCount(postId, 1)
                    postDao.changeDownvoteCount(postId, -1)
                } else {
                    postDao.changeUpvoteCount(postId, -1)
                    postDao.changeDownvoteCount(postId, 1)
                }
            }
            currentVoteType == 0 -> {
                val newVote = Vote(userId, postId, voteAction)
                voteDao.upsertVote(newVote)
                if (voteAction == 1) postDao.changeUpvoteCount(postId, 1)
                else postDao.changeDownvoteCount(postId, 1)
            }
        }
    }
}