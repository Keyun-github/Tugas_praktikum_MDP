package com.example.week6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddPostViewModel(application: Application, private val currentUserId: Int) : AndroidViewModel(application) {

    private val communityRepository: CommunityRepository
    private val postRepository: PostRepository

    val allCommunities: LiveData<List<Community>>

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> get() = _validationError

    private val _postCreatedEvent = MutableLiveData<Boolean>()
    val postCreatedEvent: LiveData<Boolean> get() = _postCreatedEvent

    init {
        val db = AppDatabase.getDatabase(application)
        communityRepository = CommunityRepository(db.communityDao())
        postRepository = PostRepository(db.postDao(), db.commentDao(), db.voteDao())
        allCommunities = communityRepository.allCommunities
    }

    fun createPost(title: String, description: String, selectedCommunity: Community?) {
        _validationError.value = null
        if (selectedCommunity == null) {
            _validationError.value = "Please select a community"
            return
        }
        if (title.isBlank() || title.length < 3) {
            _validationError.value = "Title cannot be empty and must be at least 3 characters"
            return
        }
        if (description.isBlank()) {
            _validationError.value = "Description cannot be empty"
            return
        }

        viewModelScope.launch {
            postRepository.insertPost(title, description, selectedCommunity.id, currentUserId)
            _postCreatedEvent.value = true
        }
    }

    fun onPostCreatedEventDone() {
        _postCreatedEvent.value = false
    }

    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddPostViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddPostViewModel(application, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}