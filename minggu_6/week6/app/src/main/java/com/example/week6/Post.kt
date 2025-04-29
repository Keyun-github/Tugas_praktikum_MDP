package com.example.week6

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date // Import Date

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_creator_id"],
            onDelete = ForeignKey.CASCADE // Hapus post jika user dihapus
        ),
        ForeignKey(
            entity = Community::class,
            parentColumns = ["community_id"],
            childColumns = ["community_owner_id"],
            onDelete = ForeignKey.CASCADE // Hapus post jika community dihapus
        )
    ]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "post_id")
    val id: Int = 0,

    @ColumnInfo(name = "user_creator_id", index = true)
    val userId: Int,

    @ColumnInfo(name = "community_owner_id", index = true)
    val communityId: Int,

    @ColumnInfo(name = "post_title")
    val title: String,

    @ColumnInfo(name = "post_description")
    val description: String,

    @ColumnInfo(name = "post_upvotes")
    var upvotes: Int = 0, // Simpan jumlah vote untuk tampilan cepat

    @ColumnInfo(name = "post_downvotes")
    var downvotes: Int = 0, // Simpan jumlah vote untuk tampilan cepat

    @ColumnInfo(name = "post_created_at")
    val createdAt: Date // Simpan timestamp
)