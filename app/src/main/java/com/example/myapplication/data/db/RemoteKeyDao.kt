package com.example.myapplication.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKeys: List<RemoteKeyEntity>)

    @Query("SELECT * FROM remote_keys WHERE photoId = :photoId")
    suspend fun getRemoteKey(photoId: String): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys WHERE queryKey = :queryKey")
    suspend fun deleteByQuery(queryKey: String)

    @Query("SELECT * FROM remote_keys WHERE queryKey = :queryKey ORDER BY nextKey DESC LIMIT 1")
    suspend fun getLastRemoteKey(queryKey: String): RemoteKeyEntity?
}