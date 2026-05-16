package com.senthapps.slagrimarket.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CropCategoryDto(
    val id: Int,
    val categoryId: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class CropDto(
    val id: Int,
    val cropId: String,
    val description: String,
    val cropType: String?,
    val scientificName: String?,
    val presignedUrl: String?
)

/** CROPIX envelope for /crop-details/crop-categories */
@JsonClass(generateAdapter = true)
data class CropCategoriesResponse(
    @Json(name = "payloadDto") val payload: List<CropCategoryDto>? = null,
    val totalElements: Int? = null
)

/** CROPIX envelope for /crop-details/crops */
@JsonClass(generateAdapter = true)
data class CropsResponse(
    @Json(name = "payloadDto") val payload: List<CropDto>? = null,
    val totalElements: Int? = null
)
