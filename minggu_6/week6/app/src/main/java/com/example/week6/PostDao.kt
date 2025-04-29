package com.example.week6

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface PostDao {
    // Query utama tetap LiveData
    @Transaction
    @Query("SELECT * FROM posts ORDER BY post_created_at DESC")
    fun getAllPostsWithDetails(): LiveData<List<PostWithDetails>>

    @Transaction
    @Query("""
        SELECT p.* FROM posts p
        INNER JOIN communities c ON p.community_owner_id = c.community_id
        WHERE p.post_title LIKE '%' || :query || '%' OR c.community_name LIKE '%' || :query || '%'
        ORDER BY p.post_created_at DESC
    """)
    suspend fun searchPostsWithDetails(query: String): List<PostWithDetails> // Suspend OK untuk search

    @Transaction
    @Query("SELECT * FROM posts WHERE post_id = :postId LIMIT 1")
    fun getPostWithDetailsById(postId: Int): LiveData<PostWithDetails?> // Tetap LiveData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Query("UPDATE posts SET post_upvotes = post_upvotes + :change WHERE post_id = :postId")
    suspend fun changeUpvoteCount(postId: Int, change: Int)

    @Query("UPDATE posts SET post_downvotes = post_downvotes + :change WHERE post_id = :postId")
    suspend fun changeDownvoteCount(postId: Int, change: Int)

    // Kembalikan ke suspend fun
    @Query("SELECT COUNT(*) FROM comments WHERE post_owner_id = :postId")
    suspend fun getCommentCountForPost(postId: Int): Int
}