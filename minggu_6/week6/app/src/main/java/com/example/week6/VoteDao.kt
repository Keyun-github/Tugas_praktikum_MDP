package com.example.week6

import androidx.lifecycle.LiveData // Boleh dihapus jika tidak dipakai lagi
import androidx.room.*

@Dao
interface VoteDao {
    @Upsert
    suspend fun upsertVote(vote: Vote)

    // Kembalikan ke suspend fun
    @Query("SELECT * FROM votes WHERE user_voter_id = :userId AND post_voted_id = :postId LIMIT 1")
    suspend fun getVoteByUserAndPost(userId: Int, postId: Int): Vote? // Hapus LiveData

    @Query("DELETE FROM votes WHERE user_voter_id = :userId AND post_voted_id = :postId")
    suspend fun deleteVote(userId: Int, postId: Int)

    // Fungsi ini bisa tetap suspend karena akan dipanggil dari coroutine di ViewModel
    @Query("SELECT * FROM votes WHERE user_voter_id = :userId AND post_voted_id IN (:postIds)")
    suspend fun getVotesByUserForPosts(userId: Int, postIds: List<Int>): List<Vote>
}