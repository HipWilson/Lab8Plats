package com.example.myapplication.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.myapplication.data.db.PhotoEntity
import com.example.myapplication.data.db.QueryEntity
import com.example.myapplication.data.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val searchResults: Flow<PagingData<PhotoEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            repository.searchPhotos("nature") // Default query
        } else {
            repository.searchPhotos(query)
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            repository.recordSearch(query)
            _searchQuery.value = query
        }
    }

    fun toggleFavorite(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(photo)
        }
    }

    suspend fun getRecentQueries(): List<QueryEntity> {
        return repository.getRecentQueries()
    }
}