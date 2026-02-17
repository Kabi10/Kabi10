package com.senthapps.slagrimarket.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for image upload operations via backend Storage API
 */
interface StorageApiService {

    /**
     * Upload a single image to backend storage
     */
    @Multipart
    @POST("v1/storage/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<StorageUploadResponse>
}

data class StorageUploadResponse(
    val success: Boolean,
    val data: StorageUploadData? = null,
    val message: String? = null
)

data class StorageUploadData(
    val url: String,
    val path: String
)
