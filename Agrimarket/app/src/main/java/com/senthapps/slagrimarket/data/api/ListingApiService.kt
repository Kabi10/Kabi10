package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Enhanced API service interface for listing operations
 * Supports trilingual content, quality grades, and comprehensive filtering
 */
interface ListingApiService {

    /**
     * Get listings with comprehensive filtering options
     */
    @GET("v1/listings")
    suspend fun getListings(
        @Query("market") market: String? = null,
        @Query("cropType") cropType: String? = null,
        @Query("quality") quality: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("location") location: String? = null,
        @Query("farmerId") farmerId: String? = null,
        @Query("isActive") isActive: Boolean? = null,
        @Query("availableFrom") availableFrom: String? = null,
        @Query("availableUntil") availableUntil: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortOrder") sortOrder: String = "desc",
        @Query("q") searchQuery: String? = null,
        @Query("language") language: String = "en"
    ): Response<ListingsResponse>
    
    /**
     * Get listing by ID
     */
    @GET("v1/listings/{id}")
    suspend fun getListingById(@Path("id") listingId: String): Response<Listing>

    /**
     * Get listings by farmer ID
     */
    @GET("v1/listings/farmer/{farmerId}")
    suspend fun getListingsByFarmer(
        @Path("farmerId") farmerId: String,
        @Query("isActive") isActive: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ListingsResponse>

    /**
     * Get trending/popular listings
     */
    @GET("v1/listings/trending")
    suspend fun getTrendingListings(
        @Query("timeframe") timeframe: String = "24h", // "1h", "24h", "7d"
        @Query("limit") limit: Int = 10
    ): Response<ListingsResponse>

    /**
     * Search listings with text query
     */
    @GET("v1/listings/search")
    suspend fun searchListings(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ListingsResponse>

    /**
     * Create new listing
     */
    @POST("v1/listings")
    suspend fun createListing(@Body listing: CreateListingRequest): Response<Listing>

    /**
     * Update existing listing
     */
    @PUT("v1/listings/{id}")
    suspend fun updateListing(
        @Path("id") listingId: String,
        @Body listing: UpdateListingRequest
    ): Response<Listing>

    /**
     * Delete listing (soft delete - sets isActive to false)
     */
    @DELETE("v1/listings/{id}")
    suspend fun deleteListing(@Path("id") listingId: String): Response<DeleteResponse>

    /**
     * Increment view count for listing
     */
    @POST("v1/listings/{id}/view")
    suspend fun incrementViewCount(@Path("id") listingId: String): Response<ViewCountResponse>

    /**
     * Increment inquiry count for listing
     */
    @POST("v1/listings/{id}/inquiry")
    suspend fun incrementInquiryCount(@Path("id") listingId: String): Response<InquiryCountResponse>

    /**
     * Get image upload URL
     */
    @POST("v1/listings/{id}/images")
    suspend fun getImageUploadUrl(@Path("id") listingId: String): Response<ImageUploadResponse>

    /**
     * Confirm image upload
     */
    @POST("v1/listings/{id}/images/confirm")
    suspend fun confirmImageUpload(
        @Path("id") listingId: String,
        @Body request: ConfirmImageUploadRequest
    ): Response<Listing>
}

@JsonClass(generateAdapter = true)
data class ListingsResponse(
    @Json(name = "listings")
    val listings: List<Listing>,

    @Json(name = "totalCount")
    val totalCount: Int,

    @Json(name = "page")
    val page: Int,

    @Json(name = "totalPages")
    val totalPages: Int,

    @Json(name = "hasNext")
    val hasNext: Boolean,

    @Json(name = "hasPrevious")
    val hasPrevious: Boolean,

    @Json(name = "lastUpdated")
    val lastUpdated: String
)

@JsonClass(generateAdapter = true)
data class CreateListingRequest(
    @Json(name = "cropType")
    val cropType: String,

    @Json(name = "cropNameTamil")
    val cropNameTamil: String = "",

    @Json(name = "cropNameEnglish")
    val cropNameEnglish: String = "",

    @Json(name = "cropNameSinhala")
    val cropNameSinhala: String = "",

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "unit")
    val unit: String,

    @Json(name = "pricePerUnit")
    val pricePerUnit: Double,

    @Json(name = "quality")
    val quality: String,

    @Json(name = "harvestDate")
    val harvestDate: String,

    @Json(name = "location")
    val location: String,

    @Json(name = "locationTamil")
    val locationTamil: String = "",

    @Json(name = "locationSinhala")
    val locationSinhala: String = "",

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "descriptionTamil")
    val descriptionTamil: String = "",

    @Json(name = "descriptionSinhala")
    val descriptionSinhala: String = "",

    @Json(name = "availableFrom")
    val availableFrom: String? = null,

    @Json(name = "availableUntil")
    val availableUntil: String? = null,

    @Json(name = "pickupLocations")
    val pickupLocations: List<String> = emptyList(),

    @Json(name = "story")
    val story: String? = null,

    @Json(name = "farmingMethods")
    val farmingMethods: List<String> = emptyList(),

    @Json(name = "certifications")
    val certifications: List<com.senthapps.slagrimarket.data.model.Certification> = emptyList(),

    @Json(name = "harvestedAt")
    val harvestedAt: String? = null,

    @Json(name = "sustainabilityPractices")
    val sustainabilityPractices: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UpdateListingRequest(
    @Json(name = "quantity")
    val quantity: Double? = null,
    
    @Json(name = "pricePerUnit")
    val pricePerUnit: Double? = null,
    
    @Json(name = "quality")
    val quality: String? = null,
    
    @Json(name = "location")
    val location: String? = null,
    
    @Json(name = "isActive")
    val isActive: Boolean? = null,
    
    @Json(name = "description")
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ImageUploadResponse(
    @Json(name = "uploadUrl")
    val uploadUrl: String,
    
    @Json(name = "imageId")
    val imageId: String
)

@JsonClass(generateAdapter = true)
data class ConfirmImageUploadRequest(
    @Json(name = "imageId")
    val imageId: String,
    
    @Json(name = "imageUrl")
    val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class DeleteResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String
)

/**
 * Response model for view count increment
 */
@JsonClass(generateAdapter = true)
data class ViewCountResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "viewCount")
    val viewCount: Int
)

/**
 * Response model for inquiry count increment
 */
@JsonClass(generateAdapter = true)
data class InquiryCountResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "inquiryCount")
    val inquiryCount: Int
)
