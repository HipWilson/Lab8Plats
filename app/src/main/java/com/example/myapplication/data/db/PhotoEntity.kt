package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["queryKey", "pageIndex"]),
        Index(value = ["isFavorite"])
    ]
)
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographerUrl: String,
    val src: String, // URL de la imagen grande
    val avg_color: String?,
    val isFavorite: Boolean = false,
    val queryKey: String, // query normalizada
    val pageIndex: Int,
    val updatedAt: Long = System.currentTimeMillis()
)