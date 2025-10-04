package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "listingId")
    val listingId: String,
    
    @Json(name = "createdAt")
    val createdAt: String
)
