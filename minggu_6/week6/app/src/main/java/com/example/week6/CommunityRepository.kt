package com.example.week6

import androidx.lifecycle.LiveData

class CommunityRepository(private val communityDao: CommunityDao) {

    val allCommunities: LiveData<List<Community>> = communityDao.getAllCommunities()

    suspend fun insert(community: Community) {
        communityDao.insertCommunity(community)
    }

    suspend fun getCommunityByName(name: String): Community? {
        return communityDao.getCommunityByName(name)
    }
}