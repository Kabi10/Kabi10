package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Favorite
import com.senthapps.slagrimarket.data.model.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFavoritesForUser(userId: String): Flow<List<Favorite>>
    
    @Query("""
        SELECT l.* FROM listings l 
        INNER JOIN favorites f ON l.id = f.listingId 
        WHERE f.userId = :userId 
        ORDER BY f.createdAt DESC
    """)
    fun getFavoriteListingsForUser(userId: String): Flow<List<Listing>>
    
    @Query("SELECT * FROM favorites WHERE userId = :userId AND listingId = :listingId LIMIT 1")
    suspend fun getFavorite(userId: String, listingId: String): Favorite?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND listingId = :listingId)")
    suspend fun isFavorite(userId: String, listingId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    suspend fun getFavoriteCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)
    
    @Query("DELETE FROM favorites WHERE userId = :userId AND listingId = :listingId")
    suspend fun deleteFavorite(userId: String, listingId: String)
    
    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)
    
    @Query("DELETE FROM favorites WHERE listingId = :listingId")
    suspend fun deleteFavoritesForListing(listingId: String)
}
