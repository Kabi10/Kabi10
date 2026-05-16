package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateReviewRequest
import com.senthapps.slagrimarket.data.api.ReviewApiService
import com.senthapps.slagrimarket.data.api.UpdateReviewRequest
import com.senthapps.slagrimarket.data.dao.ReviewDao
import com.senthapps.slagrimarket.data.model.Review
import com.senthapps.slagrimarket.data.model.ReviewType
import com.senthapps.slagrimarket.data.model.UserRating
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao,
    private val reviewApiService: ReviewApiService
) {

    fun getReviewsForUser(userId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForUser(userId)
    }

    fun getReviewsByUser(userId: String): Flow<List<Review>> {
        return reviewDao.getReviewsByUser(userId)
    }

    suspend fun refreshReviews(userId: String) {
        try {
            val response = reviewApiService.getReviewsForUser(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val reviews = response.body()!!.reviews
                reviews.forEach { dto ->
                    val review = Review(
                        id = dto.id,
                        transactionId = dto.transactionId,
                        reviewerId = dto.reviewerId,
                        reviewerName = dto.reviewerName ?: "",
                        revieweeId = dto.revieweeId,
                        rating = dto.rating,
                        comment = dto.comment ?: "",
                        reviewType = try { ReviewType.valueOf(dto.reviewType) } catch (_: Exception) { ReviewType.BUYER_TO_FARMER },
                        createdAt = dto.createdAt
                    )
                    reviewDao.insertReview(review)
                }
                Timber.d("Refreshed ${reviews.size} reviews for user $userId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing reviews from API")
        }
    }

    suspend fun getUserRating(userId: String): UserRating {
        return try {
            // Try API first — wrap separately so network exceptions fall through to local fallback
            try {
                val response = reviewApiService.getReviewSummary(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data
                    return UserRating(
                        userId = userId,
                        averageRating = data.averageRating,
                        totalReviews = data.totalReviews,
                        fiveStars = data.distribution["5"] ?: 0,
                        fourStars = data.distribution["4"] ?: 0,
                        threeStars = data.distribution["3"] ?: 0,
                        twoStars = data.distribution["2"] ?: 0,
                        oneStar = data.distribution["1"] ?: 0
                    )
                }
            } catch (e: Exception) {
                Timber.w(e, "Network error fetching rating summary, falling back to local")
            }

            // Fallback to local
            val averageRating = reviewDao.getAverageRating(userId) ?: 0.0
            val totalReviews = reviewDao.getTotalReviews(userId)
            val fiveStars = reviewDao.getReviewCountByStars(userId, 5)
            val fourStars = reviewDao.getReviewCountByStars(userId, 4)
            val threeStars = reviewDao.getReviewCountByStars(userId, 3)
            val twoStars = reviewDao.getReviewCountByStars(userId, 2)
            val oneStar = reviewDao.getReviewCountByStars(userId, 1)

            UserRating(userId, averageRating, totalReviews, fiveStars, fourStars, threeStars, twoStars, oneStar)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user rating")
            UserRating(userId, 0.0, 0, 0, 0, 0, 0, 0)
        }
    }

    suspend fun createReview(
        transactionId: String,
        reviewerId: String,
        reviewerName: String,
        revieweeId: String,
        rating: Int,
        comment: String,
        reviewType: ReviewType
    ): Result<Review> {
        return try {
            // Check if review already exists for this transaction locally
            val existingReviews = reviewDao.getReviewsForTransaction(transactionId)
            val alreadyReviewed = existingReviews.any { it.reviewerId == reviewerId }

            if (alreadyReviewed) {
                return Result.failure(Exception("You have already reviewed this transaction"))
            }

            // Create via API
            try {
                val response = reviewApiService.createReview(
                    CreateReviewRequest(transactionId = transactionId, rating = rating, comment = comment)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val dto = response.body()!!.data
                    val review = Review(
                        id = dto.id,
                        transactionId = dto.transactionId,
                        reviewerId = dto.reviewerId,
                        reviewerName = reviewerName,
                        revieweeId = dto.revieweeId,
                        rating = dto.rating,
                        comment = dto.comment ?: "",
                        reviewType = reviewType,
                        createdAt = dto.createdAt
                    )
                    reviewDao.insertReview(review)
                    Timber.d("Review created via API: $rating stars for user $revieweeId")
                    return Result.success(review)
                } else {
                    // Server explicitly rejected the review (4xx/5xx) — do not create locally,
                    // as the server may be enforcing a duplicate or validation constraint.
                    val code = response.code()
                    val errorMsg = response.errorBody()?.string() ?: "Server rejected review (HTTP $code)"
                    Timber.w("Review rejected by server: HTTP $code")
                    return Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                // Network/IO error — server unreachable, fall back to local so the review
                // can be synced later when connectivity is restored.
                Timber.w(e, "Network error creating review, saving locally for later sync")
            }

            // Offline fallback: create locally only when the server was unreachable
            val review = Review(
                id = UUID.randomUUID().toString(),
                transactionId = transactionId,
                reviewerId = reviewerId,
                reviewerName = reviewerName,
                revieweeId = revieweeId,
                rating = rating,
                comment = comment,
                reviewType = reviewType,
                createdAt = Instant.now().toString()
            )

            reviewDao.insertReview(review)
            Timber.d("Review saved locally (offline): $rating stars for user $revieweeId")
            Result.success(review)
        } catch (e: Exception) {
            Timber.e(e, "Error creating review")
            Result.failure(e)
        }
    }

    suspend fun updateReview(
        reviewId: String,
        rating: Int,
        comment: String
    ): Result<Review> {
        return try {
            val existingReview = reviewDao.getReviewById(reviewId)
                ?: return Result.failure(Exception("Review not found"))

            // Update via API
            try {
                reviewApiService.updateReview(reviewId, UpdateReviewRequest(rating = rating, comment = comment))
            } catch (e: Exception) {
                Timber.w(e, "Failed to update review on API")
            }

            val updatedReview = existingReview.copy(rating = rating, comment = comment)
            reviewDao.updateReview(updatedReview)
            Timber.d("Review updated: $reviewId")
            Result.success(updatedReview)
        } catch (e: Exception) {
            Timber.e(e, "Error updating review")
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            // Delete from API
            try {
                reviewApiService.deleteReview(reviewId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to delete review from API")
            }

            reviewDao.deleteReview(reviewId)
            Timber.d("Review deleted: $reviewId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting review")
            Result.failure(e)
        }
    }
}
