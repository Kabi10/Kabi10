package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "type")
    val type: NotificationType,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "relatedId")
    val relatedId: String? = null, // Transaction ID, Listing ID, etc.
    
    @Json(name = "isRead")
    val isRead: Boolean = false,
    
    @Json(name = "createdAt")
    val createdAt: String
)

enum class NotificationType {
    @Json(name = "ORDER_RECEIVED")
    ORDER_RECEIVED,
    
    @Json(name = "ORDER_CONFIRMED")
    ORDER_CONFIRMED,
    
    @Json(name = "ORDER_READY")
    ORDER_READY,
    
    @Json(name = "ORDER_COMPLETED")
    ORDER_COMPLETED,
    
    @Json(name = "ORDER_CANCELLED")
    ORDER_CANCELLED,
    
    @Json(name = "LISTING_VIEWED")
    LISTING_VIEWED,
    
    @Json(name = "PRICE_ALERT")
    PRICE_ALERT,
    
    @Json(name = "SYSTEM")
    SYSTEM
}
