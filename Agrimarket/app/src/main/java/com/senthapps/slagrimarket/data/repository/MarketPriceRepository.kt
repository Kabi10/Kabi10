package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.MarketPriceApiService
import com.senthapps.slagrimarket.data.dao.MarketPriceDao
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for market price operations with offline-first architecture
 * Uses Room database as single source of truth and syncs with network in background
 */
@Singleton
class MarketPriceRepository @Inject constructor(
    private val marketPriceApiService: MarketPriceApiService,
    private val marketPriceDao: MarketPriceDao
) {
    
    // ============================================================================
    // OFFLINE-FIRST DATA ACCESS
    // ============================================================================
    
    /**
     * Get all market prices - offline-first with background refresh
     */
    fun getAllMarketPrices(forceRefresh: Boolean = false): Flow<Resource<List<MarketPrice>>> = flow {
        // Emit loading state
        emit(Resource.Loading())
        
        try {
            // Always emit cached data first (offline-first)
            val cachedPrices = marketPriceDao.getAllActiveMarketPrices()
            if (cachedPrices.isNotEmpty()) {
                emit(Resource.Success(cachedPrices))
            }
            
            // Check if we need to refresh from network
            val shouldRefresh = forceRefresh || shouldRefreshMarketPrices()
            
            if (shouldRefresh) {
                try {
                    // Fetch from network
                    val response = marketPriceApiService.getMarketPrices(limit = 100)
                    if (response.isSuccessful) {
                        val networkPrices = response.body()?.prices ?: emptyList()
                        
                        // Update local database
                        marketPriceDao.insertMarketPrices(networkPrices)
                        
                        // Emit updated data
                        emit(Resource.Success(networkPrices))
                    } else {
                        // Network failed, but we have cached data
                        if (cachedPrices.isEmpty()) {
                            emit(Resource.Error("Failed to load market prices", null))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh market prices from network")
                    // Network failed, but we have cached data
                    if (cachedPrices.isEmpty()) {
                        emit(Resource.Error("No internet connection", e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting market prices")
            emit(Resource.Error("Failed to load market prices", e))
        }
    }
    
    /**
     * Get market prices with reactive Flow
     */
    fun getMarketPricesFlow(): Flow<List<MarketPrice>> {
        return marketPriceDao.getAllActiveMarketPricesFlow()
    }

    /**
     * Search market prices by crop type
     */
    fun searchMarketPrices(cropType: String): Flow<List<MarketPrice>> {
        return marketPriceDao.getMarketPricesForCropFlow(cropType)
    }

    /**
     * Get market prices by location
     */
    fun getMarketPricesByLocation(location: String): Flow<List<MarketPrice>> {
        return marketPriceDao.getMarketPricesForLocationFlow(location)
    }
    
    /**
     * Get trending market prices (gainers/losers)
     */
    fun getTrendingPrices(): Flow<Resource<List<MarketPrice>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Emit cached trending data first
            val cachedTrending = marketPriceDao.getTrendingPrices()
            if (cachedTrending.isNotEmpty()) {
                emit(Resource.Success(cachedTrending))
            }
            
            // Try to fetch fresh trending data
            try {
                val response = marketPriceApiService.getTrendingPrices()
                if (response.isSuccessful) {
                    val trendingData = response.body()
                    val allTrending = mutableListOf<MarketPrice>()
                    
                    trendingData?.gainers?.let { allTrending.addAll(it) }
                    trendingData?.losers?.let { allTrending.addAll(it) }
                    trendingData?.mostActive?.let { allTrending.addAll(it) }
                    
                    // Update cache
                    marketPriceDao.insertMarketPrices(allTrending)
                    
                    emit(Resource.Success(allTrending))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch trending prices")
                if (cachedTrending.isEmpty()) {
                    emit(Resource.Error("Failed to load trending prices", e))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading trending prices", e))
        }
    }
    
    /**
     * Get market price by ID
     */
    suspend fun getMarketPriceById(priceId: String): Resource<MarketPrice> {
        return try {
            // Try local first
            val localPrice = marketPriceDao.getMarketPriceById(priceId)
            if (localPrice != null) {
                Resource.Success(localPrice)
            } else {
                // Try network
                val response = marketPriceApiService.getMarketPriceById(priceId)
                if (response.isSuccessful) {
                    val networkPrice = response.body()
                    if (networkPrice != null) {
                        marketPriceDao.insertMarketPrice(networkPrice)
                        Resource.Success(networkPrice)
                    } else {
                        Resource.Error("Market price not found", null)
                    }
                } else {
                    Resource.Error("Failed to load market price", null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting market price by ID")
            Resource.Error("Failed to load market price", e)
        }
    }
    
    /**
     * Get market statistics
     */
    suspend fun getMarketStatistics(): Resource<Map<String, Any>> {
        return try {
            // Try network first for fresh statistics
            val response = marketPriceApiService.getMarketStatistics()
            if (response.isSuccessful) {
                val stats = response.body()
                if (stats != null) {
                    val statsMap: Map<String, Any> = mapOf(
                        "totalCrops" to stats.totalCrops,
                        "totalLocations" to stats.totalLocations,
                        "averagePrice" to stats.averagePrice,
                        "priceRange" to stats.priceRange,
                        "trendDistribution" to stats.trendDistribution
                    )
                    Resource.Success(statsMap)
                } else {
                    Resource.Error("No statistics available", null)
                }
            } else {
                // Fallback to local statistics
                val localStats: Map<String, Any> = emptyMap() // TODO: Implement local stats
                Resource.Success(localStats)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting market statistics")
            // Fallback to local statistics
            try {
                val localStats: Map<String, Any> = emptyMap() // TODO: Implement local stats
                Resource.Success(localStats)
            } catch (localE: Exception) {
                Resource.Error("Failed to load statistics", e)
            }
        }
    }
    
    /**
     * Manual refresh of market prices
     */
    suspend fun refreshMarketPrices(): Resource<Unit> {
        return try {
            val response = marketPriceApiService.getMarketPrices(limit = 100)
            if (response.isSuccessful) {
                val prices = response.body()?.prices ?: emptyList()
                marketPriceDao.insertMarketPrices(prices)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to refresh market prices", null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing market prices")
            Resource.Error("Network error", e)
        }
    }
    
    /**
     * Get filtered market prices
     */
    fun getFilteredMarketPrices(
        cropType: String? = null,
        location: String? = null,
        trend: PriceTrend? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): Flow<List<MarketPrice>> {
        return marketPriceDao.searchMarketPricesFlow(
            cropType, location, trend
        )
    }
    
    /**
     * Get available crop types
     */
    suspend fun getAvailableCropTypes(): List<String> {
        return marketPriceDao.getAvailableCropTypes()
    }
    
    /**
     * Get available locations
     */
    suspend fun getAvailableLocations(): List<String> {
        return marketPriceDao.getAvailableLocations()
    }
    
    // ============================================================================
    // CACHE MANAGEMENT
    // ============================================================================
    
    /**
     * Check if market prices need refresh (older than 5 minutes)
     */
    private suspend fun shouldRefreshMarketPrices(): Boolean {
        val lastUpdateTime = marketPriceDao.getLastUpdateTime()
        if (lastUpdateTime == null) return true

        val lastUpdated = Instant.parse(lastUpdateTime)
        val now = Instant.now()
        val minutesSinceUpdate = ChronoUnit.MINUTES.between(lastUpdated, now)

        return minutesSinceUpdate >= 5 // Refresh if older than 5 minutes
    }
    
    /**
     * Clear stale market prices
     */
    suspend fun clearStaleData() {
        try {
            marketPriceDao.deactivateStaleMarketPrices(24) // Deactivate prices older than 24 hours
            marketPriceDao.cleanupOldMarketPrices(7) // Delete prices older than 7 days
        } catch (e: Exception) {
            Timber.e(e, "Error clearing stale market price data")
        }
    }
}
