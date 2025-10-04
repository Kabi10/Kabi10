package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.dao.FavoriteDao
import com.senthapps.slagrimarket.data.model.Favorite
import com.senthapps.slagrimarket.data.model.Listing
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {

    fun getFavoriteListingsForUser(userId: String): Flow<List<Listing>> {
        return favoriteDao.getFavoriteListingsForUser(userId)
    }

    suspend fun isFavorite(userId: String, listingId: String): Boolean {
        return try {
            favoriteDao.isFavorite(userId, listingId)
        } catch (e: Exception) {
            Timber.e(e, "Error checking if listing is favorite")
            false
        }
    }

    suspend fun toggleFavorite(userId: String, listingId: String): Result<Boolean> {
        return try {
            val isFav = favoriteDao.isFavorite(userId, listingId)
            
            if (isFav) {
                favoriteDao.deleteFavorite(userId, listingId)
                Timber.d("Removed listing $listingId from favorites")
                Result.success(false)
            } else {
                val favorite = Favorite(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    listingId = listingId,
                    createdAt = Instant.now().toString()
                )
                favoriteDao.insertFavorite(favorite)
                Timber.d("Added listing $listingId to favorites")
                Result.success(true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error toggling favorite")
            Result.failure(e)
        }
    }

    suspend fun getFavoriteCount(userId: String): Int {
        return try {
            favoriteDao.getFavoriteCount(userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting favorite count")
            0
        }
    }
}
