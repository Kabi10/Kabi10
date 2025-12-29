package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "name")
    val name: String? = null,
    
    @Json(name = "phone")
    val phone: String? = null,
    
    @Json(name = "userType")
    val userType: UserType? = UserType.BUYER,
    
    @Json(name = "verified")
    val verified: Boolean = false,
    
    @Json(name = "language")
    val language: String = "ta", // Tamil by default
    
    @Json(name = "createdAt")
    val createdAt: String = Instant.now().toString()
)

enum class UserType {
    @Json(name = "FARMER")
    FARMER,
    
    @Json(name = "BUYER")
    BUYER
}
