package com.example.week6

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index // Import Index

// Gunakan composite primary key untuk memastikan 1 vote per user per post
@Entity(
    tableName = "votes",
    primaryKeys = ["user_voter_id", "post_voted_id"],
    // Tambahkan index pada kolom foreign key untuk performa
    indices = [Index(value = ["post_voted_id"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_voter_id"],
            onDelete = ForeignKey.CASCADE // Hapus vote jika user dihapus
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = ["post_id"],
            childColumns = ["post_voted_id"],
            onDelete = ForeignKey.CASCADE // Hapus vote jika post dihapus
        )
    ]
)
data class Vote(
    @ColumnInfo(name = "user_voter_id")
    val userId: Int,

    @ColumnInfo(name = "post_voted_id") // Index sudah didefinisikan di level @Entity
    val postId: Int,

    @ColumnInfo(name = "vote_type") // 1 for upvote, -1 for downvote
    val voteType: Int
)