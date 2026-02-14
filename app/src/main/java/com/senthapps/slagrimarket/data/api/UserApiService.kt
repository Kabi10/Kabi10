package com.senthapps.slagrimarket.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("v1/users/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @PUT("v1/users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>
}

// Request/Response models

@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: UserProfileDto
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "phoneNumber") val phoneNumber: String,
    @Json(name = "name") val name: String?,
    @Json(name = "userType") val userType: String,
    @Json(name = "location") val location: String?,
    @Json(name = "verified") val verified: Boolean,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "location") val location: String? = null
)
