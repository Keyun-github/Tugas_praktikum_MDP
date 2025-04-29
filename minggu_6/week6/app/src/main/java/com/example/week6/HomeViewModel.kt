package com.example.week6

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application, private val currentUserId: Int) : AndroidViewModel(application) {

    private val postRepository: PostRepository

    // Sumber LiveData dasar dari DAO
    private val sourcePosts: LiveData<List<PostWithDetails>>

    // LiveData hasil gabungan yang akan diobservasi UI
    private val _enrichedPosts = MediatorLiveData<List<PostWithDetails>>()
    val enrichedPosts: LiveData<List<PostWithDetails>> get() = _enrichedPosts

    // LiveData untuk hasil search (sudah diperkaya)
    private val _searchResults = MutableLiveData<List<PostWithDetails>>()
    val searchResults: LiveData<List<PostWithDetails>> get() = _searchResults

    private var searchJob: Job? = null
    private var lastQuery: String? = null // Simpan query terakhir

    init {
        val db = AppDatabase.getDatabase(application)
        postRepository = PostRepository(db.postDao(), db.commentDao(), db.voteDao())

        sourcePosts = postRepository.getAllPostsWithDetails()

        _enrichedPosts.addSource(sourcePosts) { posts ->
            posts?.let { enrichPostList(it) }
        }
        _enrichedPosts.value = emptyList() // Inisialisasi awal
    }

    private fun enrichPostList(posts: List<PostWithDetails>) {
        if (posts.isEmpty()) {
            _enrichedPosts.postValue(emptyList()) // Gunakan postValue jika di background thread
            return
        }
        viewModelScope.launch { // Jalankan di coroutine
            try {
                val postIds = posts.map { it.post.id }
                val userVotesMap = postRepository.getVotesForPostList(currentUserId, postIds) // suspend call

                val enrichedList = posts.map { postDetail ->
                    postDetail.copy().apply {
                        commentCount = postRepository.getCommentCount(postDetail.post.id) // suspend call
                        userVoteType = userVotesMap[postDetail.post.id]?.voteType ?: 0
                    }
                }
                _enrichedPosts.postValue(enrichedList) // Update LiveData
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error enriching post list", e)
                _enrichedPosts.postValue(posts) // Tampilkan data dasar jika error
            }
        }
    }


    fun searchPosts(query: String) {
        lastQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            try {
                val basicResults = postRepository.searchPosts(query) // suspend call
                if (basicResults.isNotEmpty()) {
                    val postIds = basicResults.map { it.post.id }
                    val userVotesMap = postRepository.getVotesForPostList(currentUserId, postIds) // suspend call
                    val enrichedResults = basicResults.map { postDetail ->
                        postDetail.copy().apply {
                            commentCount = postRepository.getCommentCount(postDetail.post.id) // suspend call
                            userVoteType = userVotesMap[postDetail.post.id]?.voteType ?: 0
                        }
                    }
                    _searchResults.postValue(enrichedResults) // Gunakan postValue
                } else {
                    _searchResults.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error searching posts", e)
                _searchResults.postValue(emptyList())
            }
        }
    }

    fun clearSearch() {
        lastQuery = null
        searchJob?.cancel()
        _searchResults.value = emptyList()
    }


    fun handleVote(postId: Int, voteAction: Int) {
        viewModelScope.launch {
            try {
                postRepository.handleVote(postId, currentUserId, voteAction) // suspend call
                // Trigger search refresh HANYA jika ada query aktif
                triggerSearchRefresh()
                // enrichedPosts akan otomatis refresh karena sourcePosts dari Room
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error handling vote", e)
            }
        }
    }

    private fun triggerSearchRefresh() {
        val currentQuery = lastQuery
        if (!currentQuery.isNullOrBlank()) {
            searchPosts(currentQuery)
        }
    }

    // Factory
    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}