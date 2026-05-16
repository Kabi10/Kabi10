package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doa_crop_categories")
data class DoaCropCategoryEntity(
    @PrimaryKey val id: Int,
    val categoryId: String,
    val description: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "doa_crops")
data class DoaCropEntity(
    @PrimaryKey val id: Int,
    val cropId: String,
    val description: String,
    val cropType: String?,
    val scientificName: String?,
    val categoryId: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
