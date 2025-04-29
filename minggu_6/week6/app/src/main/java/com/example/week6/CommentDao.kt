package com.example.week6

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CommentDao {
    @Transaction
    @Query("SELECT * FROM comments WHERE post_owner_id = :postId ORDER BY comment_created_at DESC")
    fun getCommentsWithUserForPost(postId: Int): LiveData<List<CommentWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}