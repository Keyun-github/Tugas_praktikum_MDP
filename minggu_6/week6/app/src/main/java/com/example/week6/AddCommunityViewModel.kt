package com.example.week6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddCommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CommunityRepository

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> get() = _validationError

    private val _communityCreatedEvent = MutableLiveData<Boolean>()
    val communityCreatedEvent: LiveData<Boolean> get() = _communityCreatedEvent

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CommunityRepository(db.communityDao())
    }

    fun createCommunity(name: String) {
        _validationError.value = null
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _validationError.value = "Community name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                // Cek duplikasi nama
                if (repository.getCommunityByName(trimmedName) != null) {
                    _validationError.value = "Community name already exists"
                } else {
                    repository.insert(Community(name = trimmedName))
                    _communityCreatedEvent.value = true
                }
            } catch (e: Exception) {
                _validationError.value = "Failed to create community: ${e.message}"
            }
        }
    }

    fun onCommunityCreatedEventDone() {
        _communityCreatedEvent.value = false
    }
}