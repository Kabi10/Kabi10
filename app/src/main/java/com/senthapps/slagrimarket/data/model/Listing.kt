package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.ForeignKey
import androidx.room.Index
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

/**
 * Enhanced Listing data model for farmer produce listings
 * Supports trilingual display, sync status, and comprehensive validation
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "listings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["farmerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["cropType"]),
        Index(value = ["quality"]),
        Index(value = ["syncStatus"]),
        Index(value = ["isActive"]),
        Index(value = ["harvestDate"]),
        Index(value = ["createdAt"])
    ]
)
@TypeConverters(ListingConverters::class)
data class Listing(
    @PrimaryKey
    @Json(name = "id")
    val id: String = "listing_${UUID.randomUUID()}",

    @Json(name = "farmerId")
    val farmerId: String,

    @Json(name = "farmerName")
    val farmerName: String? = null,

    @Json(name = "cropType")
    val cropType: String,

    @Json(name = "cropNameTamil")
    val cropNameTamil: String = "",

    @Json(name = "cropNameEnglish")
    val cropNameEnglish: String = "",

    @Json(name = "cropNameSinhala")
    val cropNameSinhala: String = "",

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "unit")
    val unit: String,

    @Json(name = "pricePerUnit")
    val pricePerUnit: Double,

    @Json(name = "quality")
    val quality: QualityGrade,

    @Json(name = "harvestDate")
    val harvestDate: String,

    @Json(name = "availableFrom")
    val availableFrom: String = LocalDate.now().toString(),

    @Json(name = "availableUntil")
    val availableUntil: String = LocalDate.now().plusDays(7).toString(),

    @Json(name = "location")
    val location: String,

    @Json(name = "locationTamil")
    val locationTamil: String = "",

    @Json(name = "locationSinhala")
    val locationSinhala: String = "",

    @Json(name = "description")
    val description: String = "",

    @Json(name = "descriptionTamil")
    val descriptionTamil: String = "",

    @Json(name = "descriptionSinhala")
    val descriptionSinhala: String = "",

    @Json(name = "images")
    val images: List<String> = emptyList(),

    @Json(name = "pickupLocations")
    val pickupLocations: List<String> = emptyList(),

    @Json(name = "isActive")
    val isActive: Boolean = true,

    @Json(name = "syncStatus")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @Json(name = "createdAt")
    val createdAt: String = Instant.now().toString(),

    @Json(name = "updatedAt")
    val updatedAt: String = Instant.now().toString(),

    @Json(name = "clientId")
    val clientId: String? = null,

    @Json(name = "viewCount")
    val viewCount: Int = 0,

    @Json(name = "inquiryCount")
    val inquiryCount: Int = 0
) {
    /**
     * Validation rules for listing data
     */
    fun isValid(): Boolean {
        return quantity > 0.0 &&
               pricePerUnit > 0.0 &&
               pricePerUnit <= MAX_PRICE_PER_UNIT &&
               farmerId.isNotBlank() &&
               cropType.isNotBlank() &&
               location.isNotBlank() &&
               isValidDateRange()
    }

    /**
     * Check if date range is valid
     */
    private fun isValidDateRange(): Boolean {
        return try {
            val harvestLocalDate = LocalDate.parse(harvestDate)
            val availableFromDate = LocalDate.parse(availableFrom)
            val availableUntilDate = LocalDate.parse(availableUntil)

            harvestLocalDate <= availableFromDate &&
            availableFromDate <= availableUntilDate &&
            availableUntilDate >= LocalDate.now()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if listing is stale (past availability date)
     */
    fun isStale(): Boolean {
        return try {
            val availableUntilDate = LocalDate.parse(availableUntil)
            availableUntilDate < LocalDate.now()
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Check if listing is available now
     */
    fun isAvailableNow(): Boolean {
        return try {
            val now = LocalDate.now()
            val availableFromDate = LocalDate.parse(availableFrom)
            val availableUntilDate = LocalDate.parse(availableUntil)

            now >= availableFromDate && now <= availableUntilDate && isActive
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get total value of the listing
     */
    fun getTotalValue(): Double = quantity * pricePerUnit

    companion object {
        const val MAX_PRICE_PER_UNIT = 10000.0 // Maximum reasonable price per unit in LKR
        const val MAX_QUANTITY = 100000.0 // Maximum reasonable quantity
        const val MIN_AVAILABILITY_DAYS = 1 // Minimum availability period
        const val MAX_AVAILABILITY_DAYS = 30 // Maximum availability period
    }
}

/**
 * Enhanced Quality Grade enum with trilingual support
 */
enum class QualityGrade {
    @Json(name = "A")
    A,

    @Json(name = "B")
    B,

    @Json(name = "C")
    C;

    /**
     * Get quality grade display string in specified language
     */
    fun getDisplayString(language: String): String = when (this) {
        A -> when (language) {
            "ta" -> "உயர் தரம் (A)"
            "si" -> "ඉහළ ගුණත්වය (A)"
            else -> "Premium Quality (A)"
        }
        B -> when (language) {
            "ta" -> "நல்ல தரம் (B)"
            "si" -> "හොඳ ගුණත්වය (B)"
            else -> "Good Quality (B)"
        }
        C -> when (language) {
            "ta" -> "சாதாரண தரம் (C)"
            "si" -> "සාමාන්‍ය ගුණත්වය (C)"
            else -> "Standard Quality (C)"
        }
    }

    /**
     * Get quality grade description
     */
    fun getDescription(language: String): String = when (this) {
        A -> when (language) {
            "ta" -> "சிறந்த தரம், குறைபாடுகள் இல்லை"
            "si" -> "ඉහළම ගුණත්වය, දෝෂ නැත"
            else -> "Highest quality, no defects"
        }
        B -> when (language) {
            "ta" -> "நல்ல தரம், சிறிய குறைபாடுகள்"
            "si" -> "හොඳ ගුණත්වය, සුළු දෝෂ"
            else -> "Good quality, minor defects"
        }
        C -> when (language) {
            "ta" -> "சாதாரண தரம், சில குறைபாடுகள்"
            "si" -> "සාමාන්‍ය ගුණත්වය, සමහර දෝෂ"
            else -> "Standard quality, some defects"
        }
    }

    /**
     * Get quality grade color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        A -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        B -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        C -> androidx.compose.ui.graphics.Color(0xFF6b7280) // Gray
    }
}

/**
 * Sync Status enum for offline-first architecture
 */
enum class SyncStatus {
    @Json(name = "SYNCED")
    SYNCED,

    @Json(name = "PENDING")
    PENDING,

    @Json(name = "FAILED")
    FAILED;

    /**
     * Get sync status display string
     */
    fun getDisplayString(language: String): String = when (this) {
        SYNCED -> when (language) {
            "ta" -> "ஒத்திசைக்கப்பட்டது"
            "si" -> "සමමුහුර්ත කර ඇත"
            else -> "Synced"
        }
        PENDING -> when (language) {
            "ta" -> "ஒத்திசைவு நிலுவையில்"
            "si" -> "සමමුහුර්ත කිරීම අපේක්ෂිත"
            else -> "Pending Sync"
        }
        FAILED -> when (language) {
            "ta" -> "ஒத்திசைவு தோல்வி"
            "si" -> "සමමුහුර්ත කිරීම අසාර්ථක"
            else -> "Sync Failed"
        }
    }

    /**
     * Get sync status color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        SYNCED -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        PENDING -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        FAILED -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
    }

    /**
     * Get sync status icon
     */
    fun getIcon(): String = when (this) {
        SYNCED -> "✅"
        PENDING -> "⏳"
        FAILED -> "❌"
    }
}

class ListingConverters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromQualityGrade(value: QualityGrade): String {
        return value.name
    }

    @TypeConverter
    fun toQualityGrade(value: String): QualityGrade {
        return try {
            QualityGrade.valueOf(value)
        } catch (e: Exception) {
            QualityGrade.C // Default to standard quality
        }
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (e: Exception) {
            SyncStatus.PENDING // Default to pending
        }
    }
}

// ============================================================================
// EXTENSION FUNCTIONS FOR LISTING
// ============================================================================

/**
 * Get crop name in specified language
 */
fun Listing.getCropName(language: String): String = when (language) {
    "ta" -> if (cropNameTamil.isNotEmpty()) cropNameTamil else cropType.replace("_", " ")
    "si" -> if (cropNameSinhala.isNotEmpty()) cropNameSinhala else cropType.replace("_", " ")
    "en" -> if (cropNameEnglish.isNotEmpty()) cropNameEnglish else cropType.replace("_", " ").replaceFirstChar { it.uppercase() }
    else -> "${cropNameTamil.ifEmpty { cropType }} / ${cropNameEnglish.ifEmpty { cropType }}"
}

/**
 * Get location name in specified language
 */
fun Listing.getLocationName(language: String): String = when (language) {
    "ta" -> if (locationTamil.isNotEmpty()) locationTamil else location
    "si" -> if (locationSinhala.isNotEmpty()) locationSinhala else location
    else -> location
}

/**
 * Get description in specified language
 */
fun Listing.getDescription(language: String): String = when (language) {
    "ta" -> if (descriptionTamil.isNotEmpty()) descriptionTamil else description
    "si" -> if (descriptionSinhala.isNotEmpty()) descriptionSinhala else description
    else -> description
}

/**
 * Get formatted price string
 */
fun Listing.getFormattedPrice(language: String = "en"): String {
    val currency = when (language) {
        "ta" -> "ரூ"
        "si" -> "රු"
        else -> "LKR"
    }
    return "$currency ${String.format("%.2f", pricePerUnit)}/$unit"
}

/**
 * Get formatted total value
 */
fun Listing.getFormattedTotalValue(language: String = "en"): String {
    val currency = when (language) {
        "ta" -> "ரூ"
        "si" -> "රු"
        else -> "LKR"
    }
    return "$currency ${String.format("%.2f", getTotalValue())}"
}

/**
 * Get formatted quantity string
 */
fun Listing.getFormattedQuantity(language: String = "en"): String {
    val unitText = when (language) {
        "ta" -> when (unit) {
            "kg" -> "கிலோ"
            "tons" -> "டன்"
            "pieces" -> "துண்டுகள்"
            "bunches" -> "கொத்துகள்"
            else -> unit
        }
        "si" -> when (unit) {
            "kg" -> "කිලෝ"
            "tons" -> "ටොන්"
            "pieces" -> "කෑලි"
            "bunches" -> "පොකුරු"
            else -> unit
        }
        else -> unit
    }
    return "${String.format("%.1f", quantity)} $unitText"
}

/**
 * Get formatted harvest date
 */
fun Listing.getFormattedHarvestDate(language: String = "en"): String {
    return try {
        val date = LocalDate.parse(harvestDate)
        val formatter = when (language) {
            "ta" -> DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "si" -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
            else -> DateTimeFormatter.ofPattern("MMM dd, yyyy")
        }
        date.format(formatter)
    } catch (e: Exception) {
        harvestDate
    }
}

/**
 * Get days until expiry
 */
fun Listing.getDaysUntilExpiry(): Int {
    return try {
        val availableUntilDate = LocalDate.parse(availableUntil)
        val now = LocalDate.now()
        java.time.Period.between(now, availableUntilDate).days
    } catch (e: Exception) {
        0
    }
}

/**
 * Get crop emoji
 */
fun Listing.getCropEmoji(): String {
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
        else -> "🌱"
    }
}

/**
 * Check if listing needs urgent attention (expiring soon or sync failed)
 */
fun Listing.needsAttention(): Boolean {
    return getDaysUntilExpiry() <= 2 || syncStatus == SyncStatus.FAILED
}

/**
 * Get urgency level
 */
fun Listing.getUrgencyLevel(): UrgencyLevel {
    val daysLeft = getDaysUntilExpiry()
    return when {
        daysLeft <= 1 -> UrgencyLevel.HIGH
        daysLeft <= 3 -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }
}

enum class UrgencyLevel {
    LOW, MEDIUM, HIGH;

    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        LOW -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        MEDIUM -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        HIGH -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
    }
}

// ============================================================================
// CONSTANTS AND UTILITIES
// ============================================================================

// Common crop types for Jaffna region
object CropTypes {
    const val RED_ONION = "red_onion"
    const val CHILI = "chili"
    const val TOMATO = "tomato"
    const val BRINJAL = "brinjal"
    const val OKRA = "okra"
    const val COCONUT = "coconut"
    const val PALMYRA = "palmyra"
    const val MANGO = "mango"
    const val BANANA = "banana"
    const val RICE = "rice"

    val ALL_CROPS = listOf(
        RED_ONION, CHILI, TOMATO, BRINJAL, OKRA,
        COCONUT, PALMYRA, MANGO, BANANA, RICE
    )

    /**
     * Get crop emoji
     */
    fun getCropEmoji(cropType: String): String = when (cropType) {
        RED_ONION -> "🧅"
        CHILI -> "🌶️"
        TOMATO -> "🍅"
        BRINJAL -> "🍆"
        OKRA -> "🥒"
        COCONUT -> "🥥"
        PALMYRA -> "🌴"
        MANGO -> "🥭"
        BANANA -> "🍌"
        RICE -> "🌾"
        else -> "🌱"
    }

    /**
     * Get crop name in specified language
     */
    fun getCropName(cropType: String, language: String): String = when (cropType) {
        RED_ONION -> when (language) {
            "ta" -> "வெங்காயம்"
            "si" -> "රතු ළූණු"
            else -> "Red Onion"
        }
        CHILI -> when (language) {
            "ta" -> "மிளகாய்"
            "si" -> "මිරිස්"
            else -> "Chili"
        }
        TOMATO -> when (language) {
            "ta" -> "தக்காளி"
            "si" -> "තක්කාලි"
            else -> "Tomato"
        }
        BRINJAL -> when (language) {
            "ta" -> "கத்தரிக்காய்"
            "si" -> "වම්බටු"
            else -> "Brinjal"
        }
        OKRA -> when (language) {
            "ta" -> "வெண்டைக்காய்"
            "si" -> "බණ්ඩක්කා"
            else -> "Okra"
        }
        COCONUT -> when (language) {
            "ta" -> "தேங்காய்"
            "si" -> "පොල්"
            else -> "Coconut"
        }
        PALMYRA -> when (language) {
            "ta" -> "பனை"
            "si" -> "තල්"
            else -> "Palmyra"
        }
        MANGO -> when (language) {
            "ta" -> "மாம்பழம்"
            "si" -> "අඹ"
            else -> "Mango"
        }
        BANANA -> when (language) {
            "ta" -> "வாழைப்பழம்"
            "si" -> "කෙසෙල්"
            else -> "Banana"
        }
        RICE -> when (language) {
            "ta" -> "அரிசி"
            "si" -> "සහල්"
            else -> "Rice"
        }
        else -> cropType.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

// Quality grades - Updated to use enum
object QualityGrades {
    const val GRADE_A = "A"
    const val GRADE_B = "B"
    const val GRADE_C = "C"

    val ALL_GRADES = listOf(GRADE_A, GRADE_B, GRADE_C)
    val ALL_GRADE_ENUMS = listOf(QualityGrade.A, QualityGrade.B, QualityGrade.C)
}

// Common units
object Units {
    const val KG = "kg"
    const val TONS = "tons"
    const val PIECES = "pieces"
    const val BUNCHES = "bunches"
    const val GRAMS = "g"
    const val POUNDS = "lb"
    const val BAGS = "bags"

    val ALL_UNITS = listOf(KG, GRAMS, TONS, PIECES, BUNCHES, POUNDS, BAGS)

    /**
     * Get unit name in specified language
     */
    fun getUnitName(unit: String, language: String): String = when (unit) {
        KG -> when (language) {
            "ta" -> "கிலோ"
            "si" -> "කිලෝ"
            else -> "kg"
        }
        GRAMS -> when (language) {
            "ta" -> "கிராம்"
            "si" -> "ග්‍රෑම්"
            else -> "grams"
        }
        TONS -> when (language) {
            "ta" -> "டன்"
            "si" -> "ටොන්"
            else -> "tons"
        }
        PIECES -> when (language) {
            "ta" -> "துண்டுகள்"
            "si" -> "කෑලි"
            else -> "pieces"
        }
        BUNCHES -> when (language) {
            "ta" -> "கொத்துகள்"
            "si" -> "පොකුරු"
            else -> "bunches"
        }
        BAGS -> when (language) {
            "ta" -> "பைகள்"
            "si" -> "බෑග්"
            else -> "bags"
        }
        else -> unit
    }
}
