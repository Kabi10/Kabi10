package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.AddFavoriteRequest
import com.senthapps.slagrimarket.data.api.FavoriteApiService
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
    private val favoriteDao: FavoriteDao,
    private val favoriteApiService: FavoriteApiService
) {

    fun getFavoriteListingsForUser(userId: String): Flow<List<Listing>> {
        return favoriteDao.getFavoriteListingsForUser(userId)
    }

    suspend fun refreshFavorites(userId: String) {
        try {
            val response = favoriteApiService.getFavorites()
            if (response.isSuccessful && response.body()?.success == true) {
                val favorites = response.body()!!.favorites
                favorites.forEach { dto ->
                    val favorite = Favorite(
                        id = dto.favoriteId,
                        userId = userId,
                        listingId = dto.listing.id,
                        createdAt = dto.favoritedAt
                    )
                    favoriteDao.insertFavorite(favorite)
                }
                Timber.d("Refreshed ${favorites.size} favorites from API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing favorites from API")
        }
    }

    suspend fun isFavorite(userId: String, listingId: String): Boolean {
        return try {
            // Try API first, fall back to local
            val response = favoriteApiService.checkFavorite(listingId)
            if (response.isSuccessful) {
                response.body()?.isFavorited ?: favoriteDao.isFavorite(userId, listingId)
            } else {
                favoriteDao.isFavorite(userId, listingId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if listing is favorite, using local")
            favoriteDao.isFavorite(userId, listingId)
        }
    }

    suspend fun toggleFavorite(userId: String, listingId: String): Result<Boolean> {
        return try {
            val isFav = favoriteDao.isFavorite(userId, listingId)

            if (isFav) {
                // Remove from API
                try {
                    favoriteApiService.removeFavorite(listingId)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to remove favorite from API, removing locally")
                }
                favoriteDao.deleteFavorite(userId, listingId)
                Timber.d("Removed listing $listingId from favorites")
                Result.success(false)
            } else {
                // Add to API
                try {
                    favoriteApiService.addFavorite(AddFavoriteRequest(listingId))
                } catch (e: Exception) {
                    Timber.w(e, "Failed to add favorite to API, adding locally")
                }
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
