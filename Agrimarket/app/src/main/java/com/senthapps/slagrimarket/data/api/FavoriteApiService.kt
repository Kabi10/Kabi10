package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Listing
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface FavoriteApiService {

    @GET("v1/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FavoritesResponse>

    @POST("v1/favorites")
    suspend fun addFavorite(
        @Body request: AddFavoriteRequest
    ): Response<AddFavoriteResponse>

    @DELETE("v1/favorites/{listingId}")
    suspend fun removeFavorite(
        @Path("listingId") listingId: String
    ): Response<RemoveFavoriteResponse>

    @GET("v1/favorites/check/{listingId}")
    suspend fun checkFavorite(
        @Path("listingId") listingId: String
    ): Response<CheckFavoriteResponse>
}

// Request/Response models

@JsonClass(generateAdapter = true)
data class FavoritesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "favorites") val favorites: List<FavoriteListingDto>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrevious") val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class FavoriteListingDto(
    @Json(name = "favoriteId") val favoriteId: String,
    @Json(name = "favoritedAt") val favoritedAt: String,
    @Json(name = "listing") val listing: FavoriteListingData
)

@JsonClass(generateAdapter = true)
data class FavoriteListingData(
    @Json(name = "id") val id: String,
    @Json(name = "farmerId") val farmerId: String,
    @Json(name = "cropType") val cropType: String,
    @Json(name = "quantity") val quantity: Double,
    @Json(name = "unit") val unit: String,
    @Json(name = "pricePerUnit") val pricePerUnit: Double,
    @Json(name = "quality") val quality: String,
    @Json(name = "location") val location: String,
    @Json(name = "description") val description: String?,
    @Json(name = "images") val images: List<String>,
    @Json(name = "isActive") val isActive: Boolean,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "farmerName") val farmerName: String?,
    @Json(name = "farmerPhone") val farmerPhone: String?
)

@JsonClass(generateAdapter = true)
data class AddFavoriteRequest(
    @Json(name = "listingId") val listingId: String
)

@JsonClass(generateAdapter = true)
data class AddFavoriteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: AddFavoriteData?
)

@JsonClass(generateAdapter = true)
data class AddFavoriteData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "listingId") val listingId: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoveFavoriteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class CheckFavoriteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "isFavorited") val isFavorited: Boolean
)
