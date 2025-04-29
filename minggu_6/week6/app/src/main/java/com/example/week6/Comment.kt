package com.example.week6

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_commenter_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = ["post_id"],
            childColumns = ["post_owner_id"],
            onDelete = ForeignKey.CASCADE // Hapus comment jika post dihapus
        )
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "comment_id")
    val id: Int = 0,

    @ColumnInfo(name = "post_owner_id", index = true)
    val postId: Int,

    @ColumnInfo(name = "user_commenter_id", index = true)
    val userId: Int,

    @ColumnInfo(name = "comment_text")
    val text: String,

    @ColumnInfo(name = "comment_created_at")
    val createdAt: Date
)