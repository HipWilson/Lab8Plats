package com.example.myapplication.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE queryKey = :queryKey ORDER BY pageIndex ASC, id ASC")
    fun getPhotosByQuery(queryKey: String): PagingSource<Int, PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoritePhotos(): PagingSource<Int, PhotoEntity>

    @Query("DELETE FROM photos WHERE queryKey = :queryKey")
    suspend fun deletePhotosByQuery(queryKey: String)

    @Query("DELETE FROM photos WHERE updatedAt < :timestamp")
    suspend fun deleteOldPhotos(timestamp: Long)

    @Query("SELECT COUNT(*) FROM photos WHERE queryKey = :queryKey")
    suspend fun getPhotoCountByQuery(queryKey: String): Int

    @Query("SELECT * FROM photos WHERE queryKey = :queryKey AND pageIndex = :pageIndex ORDER BY id ASC")
    suspend fun getPhotosByQueryAndPage(queryKey: String, pageIndex: Int): List<PhotoEntity>
}