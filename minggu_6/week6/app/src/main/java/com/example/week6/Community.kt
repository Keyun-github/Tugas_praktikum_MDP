package com.example.week6

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "communities", indices = [Index(value = ["community_name"], unique = true)])
data class Community(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "community_id")
    val id: Int = 0,

    @ColumnInfo(name = "community_name")
    val name: String
)