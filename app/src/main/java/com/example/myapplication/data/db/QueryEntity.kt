package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_queries")
data class QueryEntity(
    @PrimaryKey
    val query: String, // Normalizada
    val lastUsedAt: Long = System.currentTimeMillis(),
    val useCount: Int = 1
)