package com.example.myapplication.data.api

import com.google.gson.annotations.SerializedName

data class PexelsResponse(
    val photos: List<PexelsPhoto>,
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val next_page: String?
)

data class PexelsPhoto(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographer_url: String,
    val photographer_id: Long,
    val avg_color: String?,
    val src: PhotoSource
)

data class PhotoSource(
    val original: String,
    val large: String,
    val large2x: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)