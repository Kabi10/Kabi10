package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Enhanced Transaction data model with trilingual support and comprehensive tracking
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Listing::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["farmerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["buyerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listingId"]),
        Index(value = ["farmerId"]),
        Index(value = ["buyerId"]),
        Index(value = ["status"]),
        Index(value = ["paymentMethod"]),
        Index(value = ["pickupDate"]),
        Index(value = ["createdAt"])
    ]
)
@TypeConverters(TransactionConverters::class)
data class Transaction(
    @PrimaryKey
    @Json(name = "id")
    val id: String = "txn_${UUID.randomUUID()}",

    @Json(name = "listingId")
    val listingId: String,

    @Json(name = "farmerId")
    val farmerId: String,

    @Json(name = "buyerId")
    val buyerId: String,

    // Name/phone fields populated by backend via JOIN with users table
    @Json(name = "sellerName")
    val sellerName: String = "",

    @Json(name = "buyerName")
    val buyerName: String = "",

    @Json(name = "sellerPhone")
    val sellerPhone: String = "",

    @Json(name = "buyerPhone")
    val buyerPhone: String = "",

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "unit")
    val unit: String = "kg",

    @Json(name = "pricePerUnit")
    val pricePerUnit: Double,

    @Json(name = "totalAmount")
    val totalAmount: Double,

    @Json(name = "pickupLocation")
    val pickupLocation: String,

    @Json(name = "pickupLocationTamil")
    val pickupLocationTamil: String = "",

    @Json(name = "pickupLocationSinhala")
    val pickupLocationSinhala: String = "",

    @Json(name = "pickupDate")
    val pickupDate: String,

    @Json(name = "pickupTime")
    val pickupTime: String = "",

    @Json(name = "status")
    val status: TransactionStatus,

    @Json(name = "paymentMethod")
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Json(name = "paymentStatus")
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Json(name = "notes")
    val notes: String = "",

    @Json(name = "notesTamil")
    val notesTamil: String = "",

    @Json(name = "notesSinhala")
    val notesSinhala: String = "",

    @Json(name = "farmerRating")
    val farmerRating: Int? = null,

    @Json(name = "buyerRating")
    val buyerRating: Int? = null,

    @Json(name = "createdAt")
    val createdAt: String = Instant.now().toString(),

    @Json(name = "updatedAt")
    val updatedAt: String = Instant.now().toString(),

    @Json(name = "completedAt")
    val completedAt: String? = null,

    @Json(name = "clientId")
    val clientId: String? = null
) {
    /**
     * Check if transaction is overdue for pickup
     */
    fun isOverdue(): Boolean {
        return try {
            val pickupLocalDate = LocalDate.parse(pickupDate)
            pickupLocalDate < LocalDate.now() && status != TransactionStatus.COMPLETED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if pickup is today
     */
    fun isPickupToday(): Boolean {
        return try {
            val pickupLocalDate = LocalDate.parse(pickupDate)
            pickupLocalDate == LocalDate.now()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get days until pickup
     */
    fun getDaysUntilPickup(): Int {
        return try {
            val pickupLocalDate = LocalDate.parse(pickupDate)
            val now = LocalDate.now()
            java.time.Period.between(now, pickupLocalDate).days
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate commission (if applicable)
     */
    fun getCommission(rate: Double = 0.05): Double {
        return totalAmount * rate
    }

    /**
     * Get net amount after commission
     */
    fun getNetAmount(commissionRate: Double = 0.05): Double {
        return totalAmount - getCommission(commissionRate)
    }
}

/**
 * Enhanced Transaction Status enum with trilingual support
 */
enum class TransactionStatus {
    @Json(name = "PENDING")
    PENDING,

    @Json(name = "CONFIRMED")
    CONFIRMED,

    @Json(name = "IN_PROGRESS")
    IN_PROGRESS,

    @Json(name = "COMPLETED")
    COMPLETED,

    @Json(name = "CANCELLED")
    CANCELLED;

    /**
     * Get status display string
     */
    fun getDisplayString(language: String): String = when (this) {
        PENDING -> when (language) {
            "ta" -> "நிலுவையில்"
            "si" -> "අපේක්ෂිත"
            else -> "Pending"
        }
        CONFIRMED -> when (language) {
            "ta" -> "உறுதிப்படுத்தப்பட்டது"
            "si" -> "තහවුරු කරන ලදී"
            else -> "Confirmed"
        }
        IN_PROGRESS -> when (language) {
            "ta" -> "செயல்பாட்டில்"
            "si" -> "ක්‍රියාත්මක වෙමින්"
            else -> "In Progress"
        }
        COMPLETED -> when (language) {
            "ta" -> "முடிக்கப்பட்டது"
            "si" -> "සම්පූර්ණ කරන ලදී"
            else -> "Completed"
        }
        CANCELLED -> when (language) {
            "ta" -> "ரத்து செய்யப்பட்டது"
            "si" -> "අවලංගු කරන ලදී"
            else -> "Cancelled"
        }
    }

    /**
     * Get status color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        PENDING -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        CONFIRMED -> androidx.compose.ui.graphics.Color(0xFF3b82f6) // Blue
        IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFF8b5cf6) // Purple
        COMPLETED -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        CANCELLED -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
    }

    /**
     * Get status icon
     */
    fun getIcon(): String = when (this) {
        PENDING -> "⏳"
        CONFIRMED -> "✅"
        IN_PROGRESS -> "🚚"
        COMPLETED -> "🎉"
        CANCELLED -> "❌"
    }
}

/**
 * Enhanced Payment Method enum with trilingual support
 */
enum class PaymentMethod {
    @Json(name = "CASH")
    CASH,

    @Json(name = "CARD")
    CARD,

    @Json(name = "ONLINE")
    ONLINE,

    @Json(name = "BANK_TRANSFER")
    BANK_TRANSFER;

    /**
     * Get payment method display string
     */
    fun getDisplayString(language: String): String = when (this) {
        CASH -> when (language) {
            "ta" -> "பணம்"
            "si" -> "මුදල්"
            else -> "Cash"
        }
        CARD -> when (language) {
            "ta" -> "அட்டை"
            "si" -> "කාඩ්පත"
            else -> "Card"
        }
        ONLINE -> when (language) {
            "ta" -> "ஆன்லைன்"
            "si" -> "අන්තර්ජාලය"
            else -> "Online"
        }
        BANK_TRANSFER -> when (language) {
            "ta" -> "வங்கி பரிமாற்றம்"
            "si" -> "බැංකු මාරුව"
            else -> "Bank Transfer"
        }
    }

    /**
     * Get payment method icon
     */
    fun getIcon(): String = when (this) {
        CASH -> "💵"
        CARD -> "💳"
        ONLINE -> "📱"
        BANK_TRANSFER -> "🏦"
    }
}

/**
 * Payment Status enum
 */
enum class PaymentStatus {
    @Json(name = "PENDING")
    PENDING,

    @Json(name = "PAID")
    PAID,

    @Json(name = "FAILED")
    FAILED,

    @Json(name = "REFUNDED")
    REFUNDED;

    /**
     * Get payment status display string
     */
    fun getDisplayString(language: String): String = when (this) {
        PENDING -> when (language) {
            "ta" -> "பணம் நிலுவையில்"
            "si" -> "ගෙවීම අපේක්ෂිත"
            else -> "Payment Pending"
        }
        PAID -> when (language) {
            "ta" -> "பணம் செலுத்தப்பட்டது"
            "si" -> "ගෙවීම සම්පූර්ණයි"
            else -> "Paid"
        }
        FAILED -> when (language) {
            "ta" -> "பணம் செலுத்துதல் தோல்வி"
            "si" -> "ගෙවීම අසාර්ථකයි"
            else -> "Payment Failed"
        }
        REFUNDED -> when (language) {
            "ta" -> "பணம் திரும்பப் பெறப்பட்டது"
            "si" -> "මුදල් ආපසු ලබා දෙන ලදී"
            else -> "Refunded"
        }
    }

    /**
     * Get payment status color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        PENDING -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        PAID -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        FAILED -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
        REFUNDED -> androidx.compose.ui.graphics.Color(0xFF6b7280) // Gray
    }
}

// ============================================================================
// EXTENSION FUNCTIONS FOR TRANSACTION
// ============================================================================

/**
 * Get pickup location name in specified language
 */
fun Transaction.getPickupLocationName(language: String): String = when (language) {
    "ta" -> if (pickupLocationTamil.isNotEmpty()) pickupLocationTamil else pickupLocation
    "si" -> if (pickupLocationSinhala.isNotEmpty()) pickupLocationSinhala else pickupLocation
    else -> pickupLocation
}

/**
 * Get notes in specified language
 */
fun Transaction.getNotes(language: String): String = when (language) {
    "ta" -> if (notesTamil.isNotEmpty()) notesTamil else notes
    "si" -> if (notesSinhala.isNotEmpty()) notesSinhala else notes
    else -> notes
}

/**
 * Get formatted total amount
 */
fun Transaction.getFormattedTotalAmount(language: String = "en"): String {
    val currency = when (language) {
        "ta" -> "ரூ"
        "si" -> "රු"
        else -> "LKR"
    }
    return "$currency ${String.format("%.2f", totalAmount)}"
}

/**
 * Get formatted quantity
 */
fun Transaction.getFormattedQuantity(language: String = "en"): String {
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
 * Get formatted pickup date
 */
fun Transaction.getFormattedPickupDate(language: String = "en"): String {
    return try {
        val date = LocalDate.parse(pickupDate)
        val formatter = when (language) {
            "ta" -> DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "si" -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
            else -> DateTimeFormatter.ofPattern("MMM dd, yyyy")
        }
        date.format(formatter)
    } catch (e: Exception) {
        pickupDate
    }
}

/**
 * Get urgency level based on pickup date and status
 */
fun Transaction.getUrgencyLevel(): UrgencyLevel {
    val daysUntilPickup = getDaysUntilPickup()
    return when {
        isOverdue() -> UrgencyLevel.HIGH
        isPickupToday() -> UrgencyLevel.HIGH
        daysUntilPickup <= 1 -> UrgencyLevel.MEDIUM
        else -> UrgencyLevel.LOW
    }
}

/**
 * Check if transaction can be cancelled
 */
fun Transaction.canBeCancelled(): Boolean {
    return status in listOf(TransactionStatus.PENDING, TransactionStatus.CONFIRMED)
}

/**
 * Check if transaction can be completed
 */
fun Transaction.canBeCompleted(): Boolean {
    return status == TransactionStatus.IN_PROGRESS
}

/**
 * Get transaction summary for display
 */
fun Transaction.getSummary(language: String = "en"): String {
    val quantityText = getFormattedQuantity(language)
    val amountText = getFormattedTotalAmount(language)
    val statusText = status.getDisplayString(language)

    return when (language) {
        "ta" -> "$quantityText - $amountText ($statusText)"
        "si" -> "$quantityText - $amountText ($statusText)"
        else -> "$quantityText - $amountText ($statusText)"
    }
}

// ============================================================================
// TRANSACTION UTILITIES AND CONVERTERS
// ============================================================================

class TransactionConverters {
    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus?): String? = value?.name

    @TypeConverter
    fun toTransactionStatus(value: String?): TransactionStatus? =
        value?.let { TransactionStatus.valueOf(it) }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? =
        value?.let { PaymentMethod.valueOf(it) }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? =
        value?.let { PaymentStatus.valueOf(it) }
}

// Common pickup locations in Jaffna with trilingual support
object PickupLocations {
    const val JAFFNA_MARKET = "Jaffna Central Market"
    const val CHAVAKACHCHERI = "Chavakachcheri Market"
    const val POINT_PEDRO = "Point Pedro Market"
    const val NALLUR = "Nallur Market"
    const val KOPAY = "Kopay Market"
    const val FARM_PICKUP = "Farm Pickup"
    const val KILINOCHCHI = "Kilinochchi Market"
    const val MANNAR = "Mannar Market"

    val ALL_LOCATIONS = listOf(
        JAFFNA_MARKET, CHAVAKACHCHERI, POINT_PEDRO,
        NALLUR, KOPAY, FARM_PICKUP, KILINOCHCHI, MANNAR
    )

    /**
     * Get location name in specified language
     */
    fun getLocationName(location: String, language: String): String = when (location) {
        JAFFNA_MARKET -> when (language) {
            "ta" -> "யாழ்ப்பாணம் மத்திய சந்தை"
            "si" -> "යාපනය මධ්‍යම වෙළඳපොළ"
            else -> "Jaffna Central Market"
        }
        CHAVAKACHCHERI -> when (language) {
            "ta" -> "சாவகச்சேரி சந்தை"
            "si" -> "චාවකච්චේරි වෙළඳපොළ"
            else -> "Chavakachcheri Market"
        }
        POINT_PEDRO -> when (language) {
            "ta" -> "பாயிண்ட் பெட்ரோ சந்தை"
            "si" -> "පොයින්ට් පේද්‍රෝ වෙළඳපොළ"
            else -> "Point Pedro Market"
        }
        NALLUR -> when (language) {
            "ta" -> "நல்லூர் சந்தை"
            "si" -> "නල්ලූර් වෙළඳපොළ"
            else -> "Nallur Market"
        }
        KOPAY -> when (language) {
            "ta" -> "கோப்பாய் சந்தை"
            "si" -> "කෝපායි වෙළඳපොළ"
            else -> "Kopay Market"
        }
        FARM_PICKUP -> when (language) {
            "ta" -> "பண்ணையில் எடுத்துக்கொள்ளுதல்"
            "si" -> "ගොවිපලෙන් ගැනීම"
            else -> "Farm Pickup"
        }
        KILINOCHCHI -> when (language) {
            "ta" -> "கிளிநொச்சி சந்தை"
            "si" -> "කිලිනොච්චි වෙළඳපොළ"
            else -> "Kilinochchi Market"
        }
        MANNAR -> when (language) {
            "ta" -> "மன்னார் சந்தை"
            "si" -> "මන්නාරම වෙළඳපොළ"
            else -> "Mannar Market"
        }
        else -> location
    }

    /**
     * Get all locations with trilingual names
     */
    fun getAllLocationsWithNames(language: String): List<Pair<String, String>> {
        return ALL_LOCATIONS.map { location ->
            location to getLocationName(location, language)
        }
    }
}
