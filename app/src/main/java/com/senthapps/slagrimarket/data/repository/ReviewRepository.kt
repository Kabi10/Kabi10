package com.senthapps.slagrimarket.data.repository

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
    private val reviewDao: ReviewDao
) {

    fun getReviewsForUser(userId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForUser(userId)
    }

    fun getReviewsByUser(userId: String): Flow<List<Review>> {
        return reviewDao.getReviewsByUser(userId)
    }

    suspend fun getUserRating(userId: String): UserRating {
        return try {
            val averageRating = reviewDao.getAverageRating(userId) ?: 0.0
            val totalReviews = reviewDao.getTotalReviews(userId)
            val fiveStars = reviewDao.getReviewCountByStars(userId, 5)
            val fourStars = reviewDao.getReviewCountByStars(userId, 4)
            val threeStars = reviewDao.getReviewCountByStars(userId, 3)
            val twoStars = reviewDao.getReviewCountByStars(userId, 2)
            val oneStar = reviewDao.getReviewCountByStars(userId, 1)

            UserRating(
                userId = userId,
                averageRating = averageRating,
                totalReviews = totalReviews,
                fiveStars = fiveStars,
                fourStars = fourStars,
                threeStars = threeStars,
                twoStars = twoStars,
                oneStar = oneStar
            )
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
            // Check if review already exists for this transaction
            val existingReviews = reviewDao.getReviewsForTransaction(transactionId)
            val alreadyReviewed = existingReviews.any { it.reviewerId == reviewerId }

            if (alreadyReviewed) {
                return Result.failure(Exception("You have already reviewed this transaction"))
            }

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
            Timber.d("Review created: $rating stars for user $revieweeId")
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

            val updatedReview = existingReview.copy(
                rating = rating,
                comment = comment
            )

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
            reviewDao.deleteReview(reviewId)
            Timber.d("Review deleted: $reviewId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting review")
            Result.failure(e)
        }
    }
}
