package com.example.myapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myapplication.data.api.PexelsApi
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.db.PhotoEntity
import com.example.myapplication.data.db.QueryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PhotoRepository(
    private val api: PexelsApi,
    private val database: AppDatabase
) {
    private val photoDao = database.photoDao()
    private val queryDao = database.queryDao()

    fun searchPhotos(query: String): Flow<PagingData<PhotoEntity>> {
        val queryKey = normalizeQuery(query)

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                PhotoPagingSource(api, photoDao, query, queryKey)
            }
        ).flow
    }

    fun getFavoritePhotos(): Flow<PagingData<PhotoEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { photoDao.getFavoritePhotos() }
        ).flow
    }

    suspend fun getPhotoById(photoId: String): PhotoEntity? {
        return photoDao.getPhotoById(photoId)
    }

    suspend fun toggleFavorite(photo: PhotoEntity) {
        photoDao.updatePhoto(photo.copy(isFavorite = !photo.isFavorite))
    }

    suspend fun recordSearch(query: String) {
        val queryKey = normalizeQuery(query)
        val existing = queryDao.getRecentQueries(1).firstOrNull()

        if (existing?.query == queryKey) {
            queryDao.updateQueryUsage(queryKey)
        } else {
            queryDao.insertQuery(QueryEntity(query = queryKey))
        }
    }

    suspend fun getRecentQueries(): List<QueryEntity> {
        return queryDao.getRecentQueries(limit = 10)
    }

    suspend fun getLastQuery(): String? {
        return queryDao.getRecentQueries(limit = 1).firstOrNull()?.query
    }

    suspend fun getOfflinePhotos(queryKey: String): Flow<List<PhotoEntity>> {
        return flow {
            val photos = photoDao.getPhotosByQueryAndPage(queryKey, 1)
            emit(photos)
        }
    }

    private fun normalizeQuery(query: String): String {
        return query.trim().lowercase()
    }
}