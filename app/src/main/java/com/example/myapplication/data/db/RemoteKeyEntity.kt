package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey
    val photoId: String,
    val prevKey: Int?,
    val nextKey: Int?,
    val queryKey: String
)