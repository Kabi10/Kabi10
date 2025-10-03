package com.senthapps.slagrimarket.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for image upload operations
 */
interface StorageApiService {
    
    /**
     * Upload image to server
     */
    @Multipart
    @POST("v1/listings/{listingId}/images/upload")
    suspend fun uploadImage(
        @Path("listingId") listingId: String,
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<ImageUploadResponse>
    
    /**
     * Upload multiple images
     */
    @POST("v1/listings/{listingId}/images")
    suspend fun uploadImages(
        @Path("listingId") listingId: String,
        @Body request: UploadImagesRequest
    ): Response<UploadImagesResponse>
}

data class UploadImagesRequest(
    val images: List<String> // Base64 encoded images or URLs
)

data class UploadImagesResponse(
    val success: Boolean,
    val data: ImagesData
)

data class ImagesData(
    val images: List<String>
)
