package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for market price operations
 * Handles real-time market data, price trends, and market analytics
 */
interface MarketPriceApiService {
    
    /**
     * Get all market prices with optional filtering
     */
    @GET("v1/market-prices")
    suspend fun getMarketPrices(
        @Query("cropType") cropType: String? = null,
        @Query("location") location: String? = null,
        @Query("trend") trend: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sortBy") sortBy: String = "lastUpdated",
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<MarketPricesResponse>
    
    /**
     * Get market price by ID
     */
    @GET("v1/market-prices/{id}")
    suspend fun getMarketPriceById(@Path("id") priceId: String): Response<MarketPrice>
    
    /**
     * Get trending market prices (top gainers/losers)
     */
    @GET("v1/market-prices/trending")
    suspend fun getTrendingPrices(
        @Query("type") type: String = "all", // "gainers", "losers", "all"
        @Query("limit") limit: Int = 10,
        @Query("timeframe") timeframe: String = "24h" // "1h", "24h", "7d"
    ): Response<TrendingPricesResponse>
    
    /**
     * Get market price statistics
     */
    @GET("v1/market-prices/statistics")
    suspend fun getMarketStatistics(
        @Query("cropType") cropType: String? = null,
        @Query("location") location: String? = null,
        @Query("timeframe") timeframe: String = "24h"
    ): Response<MarketStatisticsResponse>
    
    /**
     * Get price history for a specific crop
     */
    @GET("v1/market-prices/history")
    suspend fun getPriceHistory(
        @Query("cropType") cropType: String,
        @Query("location") location: String? = null,
        @Query("days") days: Int = 7,
        @Query("interval") interval: String = "daily" // "hourly", "daily", "weekly"
    ): Response<PriceHistoryResponse>
    
    /**
     * Search market prices with text query
     */
    @GET("v1/market-prices/search")
    suspend fun searchMarketPrices(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MarketPricesResponse>
    
    /**
     * Get market prices by location
     */
    @GET("v1/market-prices/by-location")
    suspend fun getMarketPricesByLocation(
        @Query("location") location: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MarketPricesResponse>
    
    /**
     * Get latest market prices (most recently updated)
     */
    @GET("v1/market-prices/latest")
    suspend fun getLatestMarketPrices(
        @Query("limit") limit: Int = 20
    ): Response<MarketPricesResponse>
}

// ============================================================================
// REQUEST/RESPONSE MODELS
// ============================================================================

/**
 * Response model for market prices list
 */
@JsonClass(generateAdapter = true)
data class MarketPricesResponse(
    @Json(name = "prices")
    val prices: List<MarketPrice>,
    
    @Json(name = "totalCount")
    val totalCount: Int,
    
    @Json(name = "page")
    val page: Int,
    
    @Json(name = "totalPages")
    val totalPages: Int,
    
    @Json(name = "hasNext")
    val hasNext: Boolean,
    
    @Json(name = "hasPrevious")
    val hasPrevious: Boolean,
    
    @Json(name = "lastUpdated")
    val lastUpdated: String
)

/**
 * Response model for trending prices
 */
@JsonClass(generateAdapter = true)
data class TrendingPricesResponse(
    @Json(name = "gainers")
    val gainers: List<MarketPrice>,
    
    @Json(name = "losers")
    val losers: List<MarketPrice>,
    
    @Json(name = "mostActive")
    val mostActive: List<MarketPrice>,
    
    @Json(name = "timeframe")
    val timeframe: String,
    
    @Json(name = "lastUpdated")
    val lastUpdated: String
)

/**
 * Response model for market statistics
 */
@JsonClass(generateAdapter = true)
data class MarketStatisticsResponse(
    @Json(name = "totalCrops")
    val totalCrops: Int,
    
    @Json(name = "totalLocations")
    val totalLocations: Int,
    
    @Json(name = "averagePrice")
    val averagePrice: Double,
    
    @Json(name = "priceRange")
    val priceRange: PriceRange,
    
    @Json(name = "trendDistribution")
    val trendDistribution: TrendDistribution,
    
    @Json(name = "topCrops")
    val topCrops: List<CropStatistic>,
    
    @Json(name = "topLocations")
    val topLocations: List<LocationStatistic>,
    
    @Json(name = "lastUpdated")
    val lastUpdated: String
)

/**
 * Response model for price history
 */
@JsonClass(generateAdapter = true)
data class PriceHistoryResponse(
    @Json(name = "cropType")
    val cropType: String,
    
    @Json(name = "location")
    val location: String?,
    
    @Json(name = "history")
    val history: List<PriceHistoryPoint>,
    
    @Json(name = "interval")
    val interval: String,
    
    @Json(name = "summary")
    val summary: PriceHistorySummary
)

/**
 * Price range model
 */
@JsonClass(generateAdapter = true)
data class PriceRange(
    @Json(name = "min")
    val min: Double,
    
    @Json(name = "max")
    val max: Double,
    
    @Json(name = "average")
    val average: Double
)

/**
 * Trend distribution model
 */
@JsonClass(generateAdapter = true)
data class TrendDistribution(
    @Json(name = "up")
    val up: Int,
    
    @Json(name = "down")
    val down: Int,
    
    @Json(name = "stable")
    val stable: Int
)

/**
 * Crop statistic model
 */
@JsonClass(generateAdapter = true)
data class CropStatistic(
    @Json(name = "cropType")
    val cropType: String,
    
    @Json(name = "averagePrice")
    val averagePrice: Double,
    
    @Json(name = "priceChange")
    val priceChange: Double,
    
    @Json(name = "trend")
    val trend: PriceTrend,
    
    @Json(name = "listingCount")
    val listingCount: Int
)

/**
 * Location statistic model
 */
@JsonClass(generateAdapter = true)
data class LocationStatistic(
    @Json(name = "location")
    val location: String,
    
    @Json(name = "averagePrice")
    val averagePrice: Double,
    
    @Json(name = "cropCount")
    val cropCount: Int,
    
    @Json(name = "listingCount")
    val listingCount: Int
)

/**
 * Price history point model
 */
@JsonClass(generateAdapter = true)
data class PriceHistoryPoint(
    @Json(name = "timestamp")
    val timestamp: String,
    
    @Json(name = "price")
    val price: Double,
    
    @Json(name = "volume")
    val volume: Double?,
    
    @Json(name = "trend")
    val trend: PriceTrend
)

/**
 * Price history summary model
 */
@JsonClass(generateAdapter = true)
data class PriceHistorySummary(
    @Json(name = "startPrice")
    val startPrice: Double,
    
    @Json(name = "endPrice")
    val endPrice: Double,
    
    @Json(name = "highPrice")
    val highPrice: Double,
    
    @Json(name = "lowPrice")
    val lowPrice: Double,
    
    @Json(name = "averagePrice")
    val averagePrice: Double,
    
    @Json(name = "totalChange")
    val totalChange: Double,
    
    @Json(name = "percentageChange")
    val percentageChange: Double
)
