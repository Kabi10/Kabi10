package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced MarketPriceDao with reactive Flow-based queries for real-time market data
 */
@Dao
interface MarketPriceDao {
    
    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================
    
    @Query("SELECT * FROM market_prices WHERE id = :priceId")
    suspend fun getMarketPriceById(priceId: String): MarketPrice?
    
    @Query("SELECT * FROM market_prices WHERE id = :priceId")
    fun getMarketPriceByIdFlow(priceId: String): Flow<MarketPrice?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrice(marketPrice: MarketPrice)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(marketPrices: List<MarketPrice>)
    
    @Update
    suspend fun updateMarketPrice(marketPrice: MarketPrice)
    
    @Delete
    suspend fun deleteMarketPrice(marketPrice: MarketPrice)
    
    @Query("DELETE FROM market_prices WHERE id = :priceId")
    suspend fun deleteMarketPriceById(priceId: String)
    
    @Query("DELETE FROM market_prices")
    suspend fun deleteAllMarketPrices()
    
    // ============================================================================
    // REAL-TIME MARKET DATA QUERIES
    // ============================================================================
    
    @Query("SELECT * FROM market_prices WHERE isActive = 1 ORDER BY lastUpdated DESC")
    suspend fun getAllActiveMarketPrices(): List<MarketPrice>
    
    @Query("SELECT * FROM market_prices WHERE isActive = 1 ORDER BY lastUpdated DESC")
    fun getAllActiveMarketPricesFlow(): Flow<List<MarketPrice>>
    
    @Query("SELECT * FROM market_prices WHERE cropType = :cropType AND isActive = 1 ORDER BY lastUpdated DESC")
    suspend fun getMarketPricesForCrop(cropType: String): List<MarketPrice>
    
    @Query("SELECT * FROM market_prices WHERE cropType = :cropType AND isActive = 1 ORDER BY lastUpdated DESC")
    fun getMarketPricesForCropFlow(cropType: String): Flow<List<MarketPrice>>
    
    @Query("SELECT * FROM market_prices WHERE location = :location AND isActive = 1 ORDER BY lastUpdated DESC")
    suspend fun getMarketPricesForLocation(location: String): List<MarketPrice>
    
    @Query("SELECT * FROM market_prices WHERE location = :location AND isActive = 1 ORDER BY lastUpdated DESC")
    fun getMarketPricesForLocationFlow(location: String): Flow<List<MarketPrice>>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE cropType = :cropType 
        AND location = :location 
        AND isActive = 1 
        ORDER BY lastUpdated DESC 
        LIMIT 1
    """)
    suspend fun getLatestMarketPrice(cropType: String, location: String): MarketPrice?
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE cropType = :cropType 
        AND location = :location 
        AND isActive = 1 
        ORDER BY lastUpdated DESC 
        LIMIT 1
    """)
    fun getLatestMarketPriceFlow(cropType: String, location: String): Flow<MarketPrice?>
    
    // ============================================================================
    // TRENDING AND ANALYSIS QUERIES
    // ============================================================================
    
    @Query("SELECT * FROM market_prices WHERE trend = :trend AND isActive = 1 ORDER BY ABS(changePercentage) DESC")
    suspend fun getMarketPricesByTrend(trend: PriceTrend): List<MarketPrice>
    
    @Query("SELECT * FROM market_prices WHERE trend = :trend AND isActive = 1 ORDER BY ABS(changePercentage) DESC")
    fun getMarketPricesByTrendFlow(trend: PriceTrend): Flow<List<MarketPrice>>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND ABS(changePercentage) >= :threshold 
        ORDER BY ABS(changePercentage) DESC
        LIMIT :limit
    """)
    suspend fun getTrendingPrices(threshold: Double = 2.0, limit: Int = 10): List<MarketPrice>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND ABS(changePercentage) >= :threshold 
        ORDER BY ABS(changePercentage) DESC
        LIMIT :limit
    """)
    fun getTrendingPricesFlow(threshold: Double = 2.0, limit: Int = 10): Flow<List<MarketPrice>>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND trend = 'UP'
        ORDER BY changePercentage DESC
        LIMIT :limit
    """)
    suspend fun getTopGainers(limit: Int = 5): List<MarketPrice>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND trend = 'UP'
        ORDER BY changePercentage DESC
        LIMIT :limit
    """)
    fun getTopGainersFlow(limit: Int = 5): Flow<List<MarketPrice>>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND trend = 'DOWN'
        ORDER BY changePercentage ASC
        LIMIT :limit
    """)
    suspend fun getTopLosers(limit: Int = 5): List<MarketPrice>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1 
        AND trend = 'DOWN'
        ORDER BY changePercentage ASC
        LIMIT :limit
    """)
    fun getTopLosersFlow(limit: Int = 5): Flow<List<MarketPrice>>
    
    // ============================================================================
    // SEARCH AND FILTERING QUERIES
    // ============================================================================
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1
        AND (:cropType IS NULL OR cropType = :cropType)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        AND (:trend IS NULL OR trend = :trend)
        AND (:minPrice IS NULL OR currentPrice >= :minPrice)
        AND (:maxPrice IS NULL OR currentPrice <= :maxPrice)
        AND (:searchQuery IS NULL OR 
             cropType LIKE '%' || :searchQuery || '%' OR 
             location LIKE '%' || :searchQuery || '%' OR
             cropNameTamil LIKE '%' || :searchQuery || '%' OR
             cropNameEnglish LIKE '%' || :searchQuery || '%' OR
             cropNameSinhala LIKE '%' || :searchQuery || '%')
        ORDER BY 
            CASE WHEN :sortBy = 'price_asc' THEN currentPrice END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN currentPrice END DESC,
            CASE WHEN :sortBy = 'change_asc' THEN changePercentage END ASC,
            CASE WHEN :sortBy = 'change_desc' THEN changePercentage END DESC,
            lastUpdated DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMarketPrices(
        cropType: String? = null,
        location: String? = null,
        trend: PriceTrend? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        searchQuery: String? = null,
        sortBy: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<MarketPrice>
    
    @Query("""
        SELECT * FROM market_prices 
        WHERE isActive = 1
        AND (:cropType IS NULL OR cropType = :cropType)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        AND (:trend IS NULL OR trend = :trend)
        ORDER BY lastUpdated DESC
    """)
    fun searchMarketPricesFlow(
        cropType: String? = null,
        location: String? = null,
        trend: PriceTrend? = null
    ): Flow<List<MarketPrice>>
    
    // ============================================================================
    // STATISTICS AND ANALYTICS QUERIES
    // ============================================================================
    
    @Query("SELECT DISTINCT cropType FROM market_prices WHERE isActive = 1 ORDER BY cropType")
    suspend fun getAvailableCropTypes(): List<String>
    
    @Query("SELECT DISTINCT location FROM market_prices WHERE isActive = 1 ORDER BY location")
    suspend fun getAvailableLocations(): List<String>
    
    @Query("SELECT AVG(currentPrice) FROM market_prices WHERE cropType = :cropType AND isActive = 1")
    suspend fun getAveragePriceForCrop(cropType: String): Double?
    
    @Query("SELECT MIN(currentPrice) FROM market_prices WHERE cropType = :cropType AND isActive = 1")
    suspend fun getMinPriceForCrop(cropType: String): Double?
    
    @Query("SELECT MAX(currentPrice) FROM market_prices WHERE cropType = :cropType AND isActive = 1")
    suspend fun getMaxPriceForCrop(cropType: String): Double?
    
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN trend = 'UP' THEN 1 END) as rising,
            COUNT(CASE WHEN trend = 'DOWN' THEN 1 END) as falling,
            COUNT(CASE WHEN trend = 'STABLE' THEN 1 END) as stable,
            AVG(currentPrice) as avgPrice,
            AVG(changePercentage) as avgChange
        FROM market_prices 
        WHERE isActive = 1
    """)
    suspend fun getMarketStatistics(): MarketStatistics
    
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN trend = 'UP' THEN 1 END) as rising,
            COUNT(CASE WHEN trend = 'DOWN' THEN 1 END) as falling,
            COUNT(CASE WHEN trend = 'STABLE' THEN 1 END) as stable,
            AVG(currentPrice) as avgPrice,
            AVG(changePercentage) as avgChange
        FROM market_prices 
        WHERE isActive = 1
    """)
    fun getMarketStatisticsFlow(): Flow<MarketStatistics>
    
    // ============================================================================
    // DATA FRESHNESS AND CLEANUP QUERIES
    // ============================================================================
    
    @Query("""
        SELECT * FROM market_prices
        WHERE isActive = 1
        AND datetime(lastUpdated) < datetime('now', '-' || :hoursThreshold || ' hours')
        ORDER BY lastUpdated ASC
    """)
    suspend fun getStaleMarketPrices(hoursThreshold: Int = 24): List<MarketPrice>

    @Query("UPDATE market_prices SET isActive = 0 WHERE datetime(lastUpdated) < datetime('now', '-' || :hoursThreshold || ' hours')")
    suspend fun deactivateStaleMarketPrices(hoursThreshold: Int = 48)

    @Query("DELETE FROM market_prices WHERE isActive = 0 AND datetime(lastUpdated) < datetime('now', '-' || :retentionDays || ' days')")
    suspend fun cleanupOldMarketPrices(retentionDays: Int = 30)
    
    @Query("SELECT COUNT(*) FROM market_prices WHERE datetime(lastUpdated) > datetime('now', '-1 hour')")
    suspend fun getRecentUpdateCount(): Int
    
    @Query("SELECT MAX(datetime(lastUpdated)) FROM market_prices WHERE isActive = 1")
    suspend fun getLastUpdateTime(): String?
}

/**
 * Data class for market statistics
 */
data class MarketStatistics(
    val total: Int,
    val rising: Int,
    val falling: Int,
    val stable: Int,
    val avgPrice: Double,
    val avgChange: Double
)
