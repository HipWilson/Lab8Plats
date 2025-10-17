package com.example.myapplication.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.PhotoEntity
import com.example.myapplication.data.repository.PhotoRepository
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: PhotoRepository) : ViewModel() {

    suspend fun loadPhoto(photoId: String): PhotoEntity? {
        return repository.getPhotoById(photoId)
    }

    fun toggleFavorite(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(photo)
        }
    }
}