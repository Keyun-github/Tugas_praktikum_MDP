package com.example.week6

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class DetailViewModel(
    application: Application,
    private val postId: Int,
    private val currentUserId: Int
) : AndroidViewModel(application) {

    private val postRepository: PostRepository

    // LiveData sumber
    private val basicPostDetails: LiveData<PostWithDetails?>
    val comments: LiveData<List<CommentWithUser>> // Comment bisa langsung diobservasi
    private val userVoteState: MediatorLiveData<Vote?> // Pakai mediator agar bisa refresh manual jika perlu

    // MediatorLiveData untuk menggabungkan post detail dengan vote state terbaru
    private val _combinedPostDetails = MediatorLiveData<PostWithDetails?>()
    val combinedPostDetails: LiveData<PostWithDetails?> get() = _combinedPostDetails

    private val _commentError = MutableLiveData<String?>()
    val commentError: LiveData<String?> get() = _commentError

    init {
        val db = AppDatabase.getDatabase(application)
        postRepository = PostRepository(db.postDao(), db.commentDao(), db.voteDao())

        // Inisialisasi LiveData sumber
        basicPostDetails = postRepository.getPostDetails(postId)
        comments = postRepository.getCommentsForPost(postId) // Langsung dari repo
        userVoteState = MediatorLiveData() // Inisialisasi mediator vote

        // Tambahkan observer ke MediatorLiveData
        _combinedPostDetails.addSource(basicPostDetails) { basicDetails ->
            combinePostData(basicDetails, userVoteState.value) // Kombinasi saat post dasar berubah
        }
        _combinedPostDetails.addSource(userVoteState) { vote ->
            combinePostData(basicPostDetails.value, vote) // Kombinasi saat vote berubah
        }

        // Ambil vote state awal
        fetchInitialVoteState()
    }

    private fun fetchInitialVoteState() {
        viewModelScope.launch {
            try {
                userVoteState.value = postRepository.getVoteState(currentUserId, postId)
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error fetching initial vote state", e)
            }
        }
    }


    // Fungsi untuk menggabungkan data post dasar dengan vote state
    private fun combinePostData(post: PostWithDetails?, vote: Vote?) {
        post?.let {
            it.userVoteType = vote?.voteType ?: 0
            // Comment count akan diambil dari observer 'comments' di Fragment
        }
        // Update hanya jika post tidak null, jika post null biarkan null
        if (post != null) {
            _combinedPostDetails.value = post
        } else {
            // Jika post dasar null (misal dihapus), set mediator jadi null juga
            _combinedPostDetails.value = null
        }
    }


    fun addComment(text: String) {
        _commentError.value = null
        if (text.isBlank()) {
            _commentError.value = "Comment cannot be empty"
            return
        }
        viewModelScope.launch {
            try {
                postRepository.addComment(postId, currentUserId, text)
                // LiveData `comments` akan refresh otomatis
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error adding comment", e)
                _commentError.value = "Failed to add comment"
            }
        }
    }

    fun handleVote(voteAction: Int) {
        viewModelScope.launch {
            try {
                postRepository.handleVote(postId, currentUserId, voteAction)
                // Setelah vote dihandle, fetch ulang vote state untuk update UI
                // Ini akan trigger observer userVoteState -> combinePostData -> _combinedPostDetails
                userVoteState.value = postRepository.getVoteState(currentUserId, postId)
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error handling vote", e)
                // Bisa tambahkan LiveData untuk error vote
            }
        }
    }

    // Factory
    class Factory(
        private val application: Application,
        private val postId: Int,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(application, postId, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}