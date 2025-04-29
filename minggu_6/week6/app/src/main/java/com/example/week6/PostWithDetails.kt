package com.example.week6

import androidx.room.Embedded
import androidx.room.Relation
import java.util.Date

data class PostWithDetails(
    @Embedded val post: Post,

    @Relation(
        parentColumn = "community_owner_id",
        entityColumn = "community_id"
    )
    val community: Community, // Nama community

    @Relation(
        parentColumn = "user_creator_id",
        entityColumn = "user_id"
    )
    val user: User, // Username pembuat post

    // Jadikan nullable (?) dan beri nilai default agar Room tidak error
    // Field ini akan diisi secara manual di Repository/ViewModel
    var commentCount: Int? = 0,
    var userVoteType: Int? = 0  // 0 = none, 1 = upvoted, -1 = downvoted by current user
) {
    // Helper untuk mendapatkan username saja jika user object lengkap
    val username: String
        get() = user.username

    // Helper untuk timestamp
    val createdAt: Date
        get() = post.createdAt
}