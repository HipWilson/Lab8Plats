package com.example.myapplication.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QueryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuery(query: QueryEntity)

    @Query("UPDATE recent_queries SET lastUsedAt = :timestamp, useCount = useCount + 1 WHERE query = :query")
    suspend fun updateQueryUsage(query: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recent_queries ORDER BY lastUsedAt DESC LIMIT :limit")
    suspend fun getRecentQueries(limit: Int = 10): List<QueryEntity>

    @Query("DELETE FROM recent_queries WHERE query = :query")
    suspend fun deleteQuery(query: String)
}