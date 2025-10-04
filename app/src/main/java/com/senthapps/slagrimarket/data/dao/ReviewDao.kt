package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    
    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY createdAt DESC")
    fun getReviewsForUser(userId: String): Flow<List<Review>>
    
    @Query("SELECT * FROM reviews WHERE reviewerId = :userId ORDER BY createdAt DESC")
    fun getReviewsByUser(userId: String): Flow<List<Review>>
    
    @Query("SELECT * FROM reviews WHERE transactionId = :transactionId")
    suspend fun getReviewsForTransaction(transactionId: String): List<Review>
    
    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: String): Review?
    
    @Query("SELECT AVG(rating) FROM reviews WHERE revieweeId = :userId")
    suspend fun getAverageRating(userId: String): Double?
    
    @Query("SELECT COUNT(*) FROM reviews WHERE revieweeId = :userId")
    suspend fun getTotalReviews(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM reviews WHERE revieweeId = :userId AND rating = :stars")
    suspend fun getReviewCountByStars(userId: String, stars: Int): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)
    
    @Update
    suspend fun updateReview(review: Review)
    
    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: String)
    
    @Query("DELETE FROM reviews WHERE transactionId = :transactionId")
    suspend fun deleteReviewsForTransaction(transactionId: String)
}
