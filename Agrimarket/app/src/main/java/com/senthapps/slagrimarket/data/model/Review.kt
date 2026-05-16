package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "transactionId")
    val transactionId: String,
    
    @Json(name = "reviewerId")
    val reviewerId: String,
    
    @Json(name = "reviewerName")
    val reviewerName: String,
    
    @Json(name = "revieweeId")
    val revieweeId: String, // Person being reviewed
    
    @Json(name = "rating")
    val rating: Int, // 1-5 stars
    
    @Json(name = "comment")
    val comment: String,
    
    @Json(name = "reviewType")
    val reviewType: ReviewType,
    
    @Json(name = "createdAt")
    val createdAt: String
)

enum class ReviewType {
    @Json(name = "FARMER")
    FARMER, // Review of farmer by buyer

    @Json(name = "BUYER")
    BUYER, // Review of buyer by farmer

    @Json(name = "BUYER_TO_FARMER")
    BUYER_TO_FARMER,

    @Json(name = "FARMER_TO_BUYER")
    FARMER_TO_BUYER
}

data class UserRating(
    val userId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val fiveStars: Int,
    val fourStars: Int,
    val threeStars: Int,
    val twoStars: Int,
    val oneStar: Int
)
