package com.senthapps.slagrimarket.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {

    @GET("v1/reviews/user/{userId}")
    suspend fun getReviewsForUser(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ReviewsResponse>

    @GET("v1/reviews/user/{userId}/summary")
    suspend fun getReviewSummary(
        @Path("userId") userId: String
    ): Response<ReviewSummaryResponse>

    @POST("v1/reviews")
    suspend fun createReview(
        @Body request: CreateReviewRequest
    ): Response<CreateReviewResponse>

    @PUT("v1/reviews/{id}")
    suspend fun updateReview(
        @Path("id") reviewId: String,
        @Body request: UpdateReviewRequest
    ): Response<UpdateReviewResponse>

    @DELETE("v1/reviews/{id}")
    suspend fun deleteReview(
        @Path("id") reviewId: String
    ): Response<DeleteReviewResponse>
}

// Request/Response models

@JsonClass(generateAdapter = true)
data class ReviewsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "reviews") val reviews: List<ReviewDto>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrevious") val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    @Json(name = "id") val id: String,
    @Json(name = "transactionId") val transactionId: String,
    @Json(name = "reviewerId") val reviewerId: String,
    @Json(name = "reviewerName") val reviewerName: String?,
    @Json(name = "revieweeId") val revieweeId: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String?,
    @Json(name = "reviewType") val reviewType: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class ReviewSummaryResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: ReviewSummaryData
)

@JsonClass(generateAdapter = true)
data class ReviewSummaryData(
    @Json(name = "totalReviews") val totalReviews: Int,
    @Json(name = "averageRating") val averageRating: Double,
    @Json(name = "distribution") val distribution: Map<String, Int>
)

@JsonClass(generateAdapter = true)
data class CreateReviewRequest(
    @Json(name = "transactionId") val transactionId: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateReviewResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: ReviewDto
)

@JsonClass(generateAdapter = true)
data class UpdateReviewRequest(
    @Json(name = "rating") val rating: Int? = null,
    @Json(name = "comment") val comment: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateReviewResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: ReviewDto
)

@JsonClass(generateAdapter = true)
data class DeleteReviewResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?
)
