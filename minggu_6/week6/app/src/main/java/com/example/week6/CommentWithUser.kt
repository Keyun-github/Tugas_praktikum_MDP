package com.example.week6

import androidx.room.Embedded
import androidx.room.Relation
import java.util.Date

data class CommentWithUser(
    @Embedded val comment: Comment,

    @Relation(
        parentColumn = "user_commenter_id",
        entityColumn = "user_id"
    )
    val user: User
) {
    val username: String
        get() = user.username
    val text: String
        get() = comment.text
    val createdAt: Date
        get() = comment.createdAt
}