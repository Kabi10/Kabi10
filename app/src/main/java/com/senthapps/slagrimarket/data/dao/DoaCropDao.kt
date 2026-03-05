package com.senthapps.slagrimarket.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.senthapps.slagrimarket.data.model.DoaCropCategoryEntity
import com.senthapps.slagrimarket.data.model.DoaCropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoaCropDao {

    @Query("SELECT * FROM doa_crops ORDER BY description ASC")
    fun getAllCrops(): Flow<List<DoaCropEntity>>

    @Query("SELECT * FROM doa_crops ORDER BY description ASC")
    suspend fun getAllCropsSuspend(): List<DoaCropEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<DoaCropEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<DoaCropCategoryEntity>)

    @Query("SELECT COUNT(*) FROM doa_crops")
    suspend fun getCropCount(): Int

    @Query("SELECT MIN(cachedAt) FROM doa_crops")
    suspend fun getOldestCacheTime(): Long?
}
