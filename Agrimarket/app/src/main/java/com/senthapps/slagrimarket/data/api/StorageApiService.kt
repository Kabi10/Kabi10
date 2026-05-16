package com.senthapps.slagrimarket.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

    @DELETE("v1/storage/delete")
    suspend fun deleteImage(
        @Body request: StorageDeleteRequest
    ): Response<StorageDeleteResponse>
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

@JsonClass(generateAdapter = true)
data class StorageDeleteRequest(
    @Json(name = "path") val path: String
)

@JsonClass(generateAdapter = true)
data class StorageDeleteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)
