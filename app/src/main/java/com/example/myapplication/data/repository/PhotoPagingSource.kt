package com.example.myapplication.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.data.api.PexelsApi
import com.example.myapplication.data.db.PhotoDao
import com.example.myapplication.data.db.PhotoEntity
import retrofit2.HttpException
import java.io.IOException

class PhotoPagingSource(
    private val api: PexelsApi,
    private val photoDao: PhotoDao,
    private val query: String,
    private val queryKey: String
) : PagingSource<Int, PhotoEntity>() {

    override fun getRefreshKey(state: PagingState<Int, PhotoEntity>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoEntity> {
        return try {
            val page = params.key ?: 1
            val response = api.searchPhotos(query, page, params.loadSize)

            val photos = response.photos.map { photo ->
                PhotoEntity(
                    id = photo.id.toString(),
                    width = photo.width,
                    height = photo.height,
                    url = photo.url,
                    photographer = photo.photographer,
                    photographerUrl = photo.photographer_url,
                    src = photo.src.large,
                    avg_color = photo.avg_color,
                    queryKey = queryKey,
                    pageIndex = page
                )
            }

            // Guardar en caché
            photoDao.insertPhotos(photos)

            LoadResult.Page(
                data = photos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.photos.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            // Si hay error de red, intenta cargar del caché
            try {
                val cachedPhotos = photoDao.getPhotosByQueryAndPage(queryKey, params.key ?: 1)
                if (cachedPhotos.isNotEmpty()) {
                    LoadResult.Page(
                        data = cachedPhotos,
                        prevKey = if ((params.key ?: 1) == 1) null else (params.key ?: 1) - 1,
                        nextKey = (params.key ?: 1) + 1
                    )
                } else {
                    LoadResult.Error(e)
                }
            } catch (cacheException: Exception) {
                LoadResult.Error(e)
            }
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}