package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

/**
 * Enhanced MarketPrice data model for real-time market prices
 * Supports trilingual display, price trends, and validation
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "market_prices",
    indices = [
        Index(value = ["cropType"]),
        Index(value = ["trend"]),
        Index(value = ["location"]),
        Index(value = ["lastUpdated"])
    ]
)
data class MarketPrice(
    @PrimaryKey
    @Json(name = "id")
    val id: String = "mp_${UUID.randomUUID()}",

    @Json(name = "cropType")
    val cropType: String,

    @Json(name = "cropNameTamil")
    val cropNameTamil: String,

    @Json(name = "cropNameEnglish")
    val cropNameEnglish: String,

    @Json(name = "cropNameSinhala")
    val cropNameSinhala: String,

    @Json(name = "currentPrice")
    val currentPrice: Double,

    @Json(name = "previousPrice")
    val previousPrice: Double = currentPrice,

    @Json(name = "unit")
    val unit: String = "kg",

    @Json(name = "trend")
    val trend: PriceTrend,

    @Json(name = "changePercentage")
    val changePercentage: Double = 0.0,

    @Json(name = "changeAmount")
    val changeAmount: Double = 0.0,

    @Json(name = "location")
    val location: String,

    @Json(name = "locationTamil")
    val locationTamil: String = "",

    @Json(name = "locationSinhala")
    val locationSinhala: String = "",

    @Json(name = "lastUpdated")
    val lastUpdated: String = Instant.now().toString(),

    @Json(name = "isActive")
    val isActive: Boolean = true,

    @Json(name = "source")
    val source: String = "market_data",

    @Json(name = "reliability")
    val reliability: Double = 1.0 // 0.0 to 1.0 reliability score
) {
    // Legacy compatibility
    @Json(name = "pricePerKg")
    val pricePerKg: Double get() = if (unit == "kg") currentPrice else currentPrice

    /**
     * Validation logic for price ranges
     */
    fun isValidPrice(): Boolean {
        return currentPrice > 0.0 &&
               currentPrice <= MAX_PRICE_LKR &&
               previousPrice >= 0.0 &&
               abs(changePercentage) <= MAX_CHANGE_PERCENTAGE
    }

    /**
     * Check if price data is stale (older than threshold)
     */
    fun isStale(thresholdHours: Int = 24): Boolean {
        return try {
            val lastUpdate = Instant.parse(lastUpdated)
            val now = Instant.now()
            val hoursDiff = java.time.Duration.between(lastUpdate, now).toHours()
            hoursDiff > thresholdHours
        } catch (e: Exception) {
            true // Consider invalid timestamps as stale
        }
    }

    companion object {
        const val MAX_PRICE_LKR = 10000.0 // Maximum reasonable price per unit in LKR
        const val MAX_CHANGE_PERCENTAGE = 100.0 // Maximum reasonable change percentage

        /**
         * Create a new MarketPrice with calculated trend and change
         */
        fun create(
            cropType: String,
            cropNameTamil: String,
            cropNameEnglish: String,
            cropNameSinhala: String,
            currentPrice: Double,
            previousPrice: Double,
            unit: String = "kg",
            location: String,
            locationTamil: String = "",
            locationSinhala: String = ""
        ): MarketPrice {
            val changeAmount = currentPrice - previousPrice
            val changePercentage = if (previousPrice > 0) {
                (changeAmount / previousPrice) * 100
            } else 0.0

            val trend = when {
                changePercentage > 1.0 -> PriceTrend.UP
                changePercentage < -1.0 -> PriceTrend.DOWN
                else -> PriceTrend.STABLE
            }

            return MarketPrice(
                cropType = cropType,
                cropNameTamil = cropNameTamil,
                cropNameEnglish = cropNameEnglish,
                cropNameSinhala = cropNameSinhala,
                currentPrice = currentPrice,
                previousPrice = previousPrice,
                unit = unit,
                trend = trend,
                changePercentage = changePercentage,
                changeAmount = changeAmount,
                location = location,
                locationTamil = locationTamil,
                locationSinhala = locationSinhala
            )
        }
    }
}

/**
 * Enhanced PriceTrend enum with display properties
 */
enum class PriceTrend {
    @Json(name = "UP")
    UP,

    @Json(name = "DOWN")
    DOWN,

    @Json(name = "STABLE")
    STABLE;

    /**
     * Get trend icon/emoji
     */
    fun getIcon(): String = when (this) {
        UP -> "↗️"
        DOWN -> "↘️"
        STABLE -> "➡️"
    }

    /**
     * Get trend color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        UP -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Success green
        DOWN -> androidx.compose.ui.graphics.Color(0xFFef4444) // Error red
        STABLE -> androidx.compose.ui.graphics.Color(0xFF6b7280) // Neutral gray
    }

    /**
     * Get trend description in specified language
     */
    fun getDescription(language: String): String = when (this) {
        UP -> when (language) {
            "ta" -> "உயர்வு"
            "si" -> "ඉහළ යාම"
            else -> "Rising"
        }
        DOWN -> when (language) {
            "ta" -> "வீழ்ச்சி"
            "si" -> "පහත වැටීම"
            else -> "Falling"
        }
        STABLE -> when (language) {
            "ta" -> "நிலையான"
            "si" -> "ස්ථාවර"
            else -> "Stable"
        }
    }

    /**
     * Check if trend indicates price increase
     */
    fun isIncreasing(): Boolean = this == UP

    /**
     * Check if trend indicates price decrease
     */
    fun isDecreasing(): Boolean = this == DOWN

    /**
     * Check if trend indicates stable price
     */
    fun isStable(): Boolean = this == STABLE
}

// ============================================================================
// EXTENSION FUNCTIONS FOR MARKETPRICE
// ============================================================================

/**
 * Get formatted price string with currency
 */
fun MarketPrice.getFormattedPrice(language: String = "en"): String {
    val currency = when (language) {
        "ta" -> "ரூ"
        "si" -> "රු"
        else -> "LKR"
    }
    return "$currency ${String.format("%.2f", currentPrice)}/$unit"
}

/**
 * Get formatted change string with percentage and amount
 */
fun MarketPrice.getFormattedChange(language: String = "en"): String {
    val sign = if (changeAmount >= 0) "+" else ""
    val currency = when (language) {
        "ta" -> "ரூ"
        "si" -> "රු"
        else -> "LKR"
    }
    return "$sign${String.format("%.1f", changePercentage)}% ($sign$currency${String.format("%.2f", changeAmount)})"
}

/**
 * Get crop name in specified language
 */
fun MarketPrice.getCropName(language: String): String = when (language) {
    "ta" -> cropNameTamil
    "si" -> cropNameSinhala
    "en" -> cropNameEnglish
    else -> "$cropNameTamil / $cropNameEnglish"
}

/**
 * Get location name in specified language
 */
fun MarketPrice.getLocationName(language: String): String = when (language) {
    "ta" -> if (locationTamil.isNotEmpty()) locationTamil else location
    "si" -> if (locationSinhala.isNotEmpty()) locationSinhala else location
    else -> location
}

/**
 * Get trend color based on current trend
 */
fun MarketPrice.getTrendColor(): androidx.compose.ui.graphics.Color = trend.getColor()

/**
 * Get trend icon based on current trend
 */
fun MarketPrice.getTrendIcon(): String = trend.getIcon()

/**
 * Calculate price difference from another MarketPrice
 */
fun MarketPrice.getPriceDifference(other: MarketPrice): Double {
    return currentPrice - other.currentPrice
}

/**
 * Check if this price is higher than another
 */
fun MarketPrice.isHigherThan(other: MarketPrice): Boolean {
    return currentPrice > other.currentPrice
}

/**
 * Check if this price is lower than another
 */
fun MarketPrice.isLowerThan(other: MarketPrice): Boolean {
    return currentPrice < other.currentPrice
}

/**
 * Get formatted last updated time
 */
fun MarketPrice.getFormattedLastUpdated(language: String = "en"): String {
    return try {
        val instant = Instant.parse(lastUpdated)
        val formatter = when (language) {
            "ta" -> DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            "si" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            else -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        }
        instant.atZone(java.time.ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        lastUpdated
    }
}

/**
 * Get crop emoji/icon
 */
fun MarketPrice.getCropEmoji(): String {
    return when (cropType) {
        CropTypes.RED_ONION -> "🧅"
        CropTypes.CHILI -> "🌶️"
        CropTypes.TOMATO -> "🍅"
        CropTypes.BRINJAL -> "🍆"
        CropTypes.OKRA -> "🥒"
        CropTypes.COCONUT -> "🥥"
        CropTypes.PALMYRA -> "🌴"
        CropTypes.MANGO -> "🥭"
        CropTypes.BANANA -> "🍌"
        CropTypes.RICE -> "🌾"
        "potato" -> "🥔"
        "carrot" -> "🥕"
        "cabbage" -> "🥬"
        "beans" -> "🫘"
        "papaya" -> "🥭"
        "pineapple" -> "🍍"
        else -> "🌱"
    }
}

/**
 * Check if price is within reasonable range for the crop type
 */
fun MarketPrice.isReasonablePrice(): Boolean {
    val reasonableRanges = mapOf(
        CropTypes.RICE to 150.0..400.0,
        CropTypes.COCONUT to 30.0..80.0,
        CropTypes.BANANA to 100.0..200.0,
        CropTypes.MANGO to 120.0..300.0,
        CropTypes.TOMATO to 60.0..150.0,
        CropTypes.CHILI to 200.0..500.0,
        CropTypes.RED_ONION to 80.0..200.0,
        CropTypes.BRINJAL to 60.0..120.0,
        CropTypes.OKRA to 80.0..150.0,
        "potato" to 100.0..180.0
    )

    val range = reasonableRanges[cropType] ?: 10.0..1000.0
    return currentPrice in range
}

// ============================================================================
// SAMPLE DATA AND UTILITIES
// ============================================================================

// Sample market price data for Sri Lankan crops
object SampleMarketPrices {
    val SAMPLE_PRICES = listOf(
        MarketPrice.create(
            cropType = CropTypes.RED_ONION,
            cropNameTamil = "வெங்காயம்",
            cropNameEnglish = "Red Onion",
            cropNameSinhala = "රතු ළූණු",
            currentPrice = 120.00,
            previousPrice = 114.00,
            location = "Jaffna Central Market",
            locationTamil = "யாழ்ப்பாணம் மத்திய சந்தை",
            locationSinhala = "යාපනය මධ්‍යම වෙළඳපොළ"
        ).copy(id = "mp_red_onion"),

        MarketPrice.create(
            cropType = CropTypes.CHILI,
            cropNameTamil = "மிளகாய்",
            cropNameEnglish = "Chili",
            cropNameSinhala = "මිරිස්",
            currentPrice = 280.00,
            previousPrice = 313.00,
            location = "Chavakachcheri Market",
            locationTamil = "சாவகச்சேரி சந்தை",
            locationSinhala = "චාවකච්චේරි වෙළඳපොළ"
        ).copy(id = "mp_chili"),

        MarketPrice.create(
            cropType = CropTypes.TOMATO,
            cropNameTamil = "தக்காளி",
            cropNameEnglish = "Tomato",
            cropNameSinhala = "තක්කාලි",
            currentPrice = 95.00,
            previousPrice = 95.00,
            location = "Jaffna Central Market",
            locationTamil = "யாழ்ப்பாணம் மத்திய சந்தை",
            locationSinhala = "යාපනය මධ්‍යම වෙළඳපොළ"
        ).copy(id = "mp_tomato"),

        MarketPrice.create(
            cropType = CropTypes.BRINJAL,
            cropNameTamil = "கத்தரிக்காய்",
            cropNameEnglish = "Brinjal",
            cropNameSinhala = "වම්බටු",
            currentPrice = 85.00,
            previousPrice = 82.50,
            location = "Chavakachcheri Market",
            locationTamil = "சாவகச்சேரி சந்தை",
            locationSinhala = "චාවකච්චේරි වෙළඳපොළ"
        ).copy(id = "mp_brinjal"),

        MarketPrice.create(
            cropType = CropTypes.OKRA,
            cropNameTamil = "வெண்டைக்காய்",
            cropNameEnglish = "Okra",
            cropNameSinhala = "බණ්ඩක්කා",
            currentPrice = 110.00,
            previousPrice = 110.00,
            location = "Jaffna Central Market",
            locationTamil = "யாழ்ப்பாணம் மத்திய சந்தை",
            locationSinhala = "යාපනය මධ්‍යම වෙළඳපොළ"
        ).copy(id = "mp_okra"),

        MarketPrice.create(
            cropType = CropTypes.COCONUT,
            cropNameTamil = "தேங்காய்",
            cropNameEnglish = "Coconut",
            cropNameSinhala = "පොල්",
            currentPrice = 45.00,
            previousPrice = 43.80,
            unit = "piece",
            location = "Chavakachcheri Market",
            locationTamil = "சாவகச்சேரி சந்தை",
            locationSinhala = "චාවකච්චේරි වෙළඳපොළ"
        ).copy(id = "mp_coconut"),

        MarketPrice.create(
            cropType = CropTypes.MANGO,
            cropNameTamil = "மாம்பழம்",
            cropNameEnglish = "Mango",
            cropNameSinhala = "අඹ",
            currentPrice = 180.00,
            previousPrice = 194.00,
            location = "Jaffna Central Market",
            locationTamil = "யாழ்ப்பாணம் மத்திய சந்தை",
            locationSinhala = "යාපනය මධ්‍යම වෙළඳපොළ"
        ).copy(id = "mp_mango"),

        MarketPrice.create(
            cropType = CropTypes.BANANA,
            cropNameTamil = "வாழைப்பழம்",
            cropNameEnglish = "Banana",
            cropNameSinhala = "කෙසෙල්",
            currentPrice = 150.00,
            previousPrice = 150.00,
            unit = "bunch",
            location = "Chavakachcheri Market",
            locationTamil = "சாவகச்சேரி சந்தை",
            locationSinhala = "චාවකච්චේරි වෙළඳපොළ"
        ).copy(id = "mp_banana"),

        MarketPrice.create(
            cropType = CropTypes.RICE,
            cropNameTamil = "அரிசி",
            cropNameEnglish = "Rice",
            cropNameSinhala = "සහල්",
            currentPrice = 220.00,
            previousPrice = 210.00,
            location = "Jaffna Central Market",
            locationTamil = "யாழ்ப்பாணம் மத்திய சந்தை",
            locationSinhala = "යාපනය මධ්‍යම වෙළඳපොළ"
        ).copy(id = "mp_rice"),

        MarketPrice.create(
            cropType = "potato",
            cropNameTamil = "உருளைக்கிழங்கு",
            cropNameEnglish = "Potato",
            cropNameSinhala = "අල",
            currentPrice = 130.00,
            previousPrice = 135.00,
            location = "Chavakachcheri Market",
            locationTamil = "சாவகச்சேரி சந்தை",
            locationSinhala = "චාවකච්චේරි වෙළඳපොළ"
        ).copy(id = "mp_potato")
    )
    
    // Legacy utility functions - now delegate to extension functions
    @Deprecated("Use PriceTrend.getIcon() extension function instead")
    fun getTrendIcon(trend: PriceTrend): String = trend.getIcon()

    @Deprecated("Use PriceTrend.getColor() extension function instead")
    fun getTrendColor(trend: PriceTrend): androidx.compose.ui.graphics.Color = trend.getColor()

    @Deprecated("Use MarketPrice.getCropName() extension function instead")
    fun getCropName(marketPrice: MarketPrice, language: String): String = marketPrice.getCropName(language)

    @Deprecated("Use MarketPrice.getLocationName() extension function instead")
    fun getLocationName(location: String, language: String): String {
        // This function is kept for backward compatibility but should be replaced
        // with MarketPrice.getLocationName() which has better trilingual support
        return when (language) {
            "en" -> location
            "ta" -> when (location) {
                "Jaffna Central Market" -> "யாழ்ப்பாணம் மத்திய சந்தை"
                "Chavakachcheri Market" -> "சாவகச்சேரி சந்தை"
                else -> location
            }
            "si" -> when (location) {
                "Jaffna Central Market" -> "යාපනය මධ්‍යම වෙළඳපොළ"
                "Chavakachcheri Market" -> "චාවකච්චේරි වෙළඳපොළ"
                else -> location
            }
            else -> location
        }
    }

    @Deprecated("Use MarketPrice.getCropEmoji() extension function instead")
    fun getCropEmoji(cropType: String): String {
        return when (cropType) {
            CropTypes.RED_ONION -> "🧅"
            CropTypes.CHILI -> "🌶️"
            CropTypes.TOMATO -> "🍅"
            CropTypes.BRINJAL -> "🍆"
            CropTypes.OKRA -> "🥒"
            CropTypes.COCONUT -> "🥥"
            CropTypes.PALMYRA -> "🌴"
            CropTypes.MANGO -> "🥭"
            CropTypes.BANANA -> "🍌"
            CropTypes.RICE -> "🌾"
            "potato" -> "🥔"
            "carrot" -> "🥕"
            "cabbage" -> "🥬"
            "beans" -> "🫘"
            "papaya" -> "🥭"
            "pineapple" -> "🍍"
            else -> "🌱"
        }
    }

    /**
     * Get trending prices (prices with significant changes)
     */
    fun getTrendingPrices(threshold: Double = 2.0): List<MarketPrice> {
        return SAMPLE_PRICES.filter {
            kotlin.math.abs(it.changePercentage) >= threshold
        }.sortedByDescending { kotlin.math.abs(it.changePercentage) }
    }

    /**
     * Get prices by trend type
     */
    fun getPricesByTrend(trend: PriceTrend): List<MarketPrice> {
        return SAMPLE_PRICES.filter { it.trend == trend }
    }

    /**
     * Get prices by location
     */
    fun getPricesByLocation(location: String): List<MarketPrice> {
        return SAMPLE_PRICES.filter { it.location == location }
    }

    /**
     * Get prices for specific crop types
     */
    fun getPricesForCrops(cropTypes: List<String>): List<MarketPrice> {
        return SAMPLE_PRICES.filter { it.cropType in cropTypes }
    }

    /**
     * Get price statistics
     */
    fun getPriceStatistics(): Map<String, Any> {
        val prices = SAMPLE_PRICES.map { it.currentPrice }
        return mapOf(
            "total_items" to SAMPLE_PRICES.size,
            "average_price" to prices.average(),
            "min_price" to (prices.minOrNull() ?: 0.0),
            "max_price" to (prices.maxOrNull() ?: 0.0),
            "rising_count" to SAMPLE_PRICES.count { it.trend == PriceTrend.UP },
            "falling_count" to SAMPLE_PRICES.count { it.trend == PriceTrend.DOWN },
            "stable_count" to SAMPLE_PRICES.count { it.trend == PriceTrend.STABLE }
        )
    }
}
