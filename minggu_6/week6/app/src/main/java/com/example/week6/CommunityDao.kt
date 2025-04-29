package com.example.week6

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommunityDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCommunity(community: Community)

    @Query("SELECT * FROM communities ORDER BY community_name ASC")
    fun getAllCommunities(): LiveData<List<Community>> // LiveData agar otomatis update

    @Query("SELECT * FROM communities WHERE community_name = :name LIMIT 1")
    suspend fun getCommunityByName(name: String): Community?
}