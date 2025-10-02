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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Enhanced Activity data model for tracking user activities and transactions
 * Supports trilingual descriptions, filtering, and comprehensive activity tracking
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["activityType"]),
        Index(value = ["status"]),
        Index(value = ["timestamp"]),
        Index(value = ["relatedEntityId"]),
        Index(value = ["priority"])
    ]
)
@TypeConverters(ActivityConverters::class)
data class Activity(
    @PrimaryKey
    @Json(name = "id")
    val id: String = "activity_${UUID.randomUUID()}",
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "activityType")
    val activityType: ActivityType,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "titleTamil")
    val titleTamil: String = "",
    
    @Json(name = "titleSinhala")
    val titleSinhala: String = "",
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "descriptionTamil")
    val descriptionTamil: String = "",
    
    @Json(name = "descriptionSinhala")
    val descriptionSinhala: String = "",
    
    @Json(name = "relatedEntityType")
    val relatedEntityType: EntityType? = null,
    
    @Json(name = "relatedEntityId")
    val relatedEntityId: String? = null,
    
    @Json(name = "status")
    val status: ActivityStatus = ActivityStatus.ACTIVE,
    
    @Json(name = "priority")
    val priority: ActivityPriority = ActivityPriority.NORMAL,
    
    @Json(name = "timestamp")
    val timestamp: String = Instant.now().toString(),
    
    @Json(name = "metadata")
    val metadata: Map<String, String> = emptyMap(),
    
    @Json(name = "isRead")
    val isRead: Boolean = false,
    
    @Json(name = "isActionable")
    val isActionable: Boolean = false,
    
    @Json(name = "expiresAt")
    val expiresAt: String? = null,
    
    @Json(name = "createdAt")
    val createdAt: String = Instant.now().toString()
) {
    /**
     * Check if activity is expired
     */
    fun isExpired(): Boolean {
        return expiresAt?.let { expiry ->
            try {
                val expiryInstant = Instant.parse(expiry)
                Instant.now().isAfter(expiryInstant)
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
    
    /**
     * Check if activity is recent (within last 24 hours)
     */
    fun isRecent(): Boolean {
        return try {
            val activityTime = Instant.parse(timestamp)
            val dayAgo = Instant.now().minusSeconds(24 * 60 * 60)
            activityTime.isAfter(dayAgo)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get relative time string
     */
    fun getRelativeTime(language: String = "en"): String {
        return try {
            val activityTime = Instant.parse(timestamp)
            val now = Instant.now()
            val diffSeconds = java.time.Duration.between(activityTime, now).seconds
            
            when {
                diffSeconds < 60 -> when (language) {
                    "ta" -> "இப்போது"
                    "si" -> "දැන්"
                    else -> "Just now"
                }
                diffSeconds < 3600 -> {
                    val minutes = diffSeconds / 60
                    when (language) {
                        "ta" -> "${minutes} நிமிடங்களுக்கு முன்"
                        "si" -> "මිනිත්තු ${minutes}කට පෙර"
                        else -> "${minutes}m ago"
                    }
                }
                diffSeconds < 86400 -> {
                    val hours = diffSeconds / 3600
                    when (language) {
                        "ta" -> "${hours} மணி நேரத்திற்கு முன்"
                        "si" -> "පැය ${hours}කට පෙර"
                        else -> "${hours}h ago"
                    }
                }
                else -> {
                    val days = diffSeconds / 86400
                    when (language) {
                        "ta" -> "${days} நாட்களுக்கு முன்"
                        "si" -> "දින ${days}කට පෙර"
                        else -> "${days}d ago"
                    }
                }
            }
        } catch (e: Exception) {
            timestamp
        }
    }
}

/**
 * Activity Type enum with display properties
 */
enum class ActivityType {
    @Json(name = "LISTING_CREATED")
    LISTING_CREATED,
    
    @Json(name = "LISTING_UPDATED")
    LISTING_UPDATED,
    
    @Json(name = "LISTING_EXPIRED")
    LISTING_EXPIRED,
    
    @Json(name = "ORDER_PLACED")
    ORDER_PLACED,
    
    @Json(name = "ORDER_CONFIRMED")
    ORDER_CONFIRMED,
    
    @Json(name = "ORDER_COMPLETED")
    ORDER_COMPLETED,
    
    @Json(name = "ORDER_CANCELLED")
    ORDER_CANCELLED,
    
    @Json(name = "PRICE_ALERT")
    PRICE_ALERT,
    
    @Json(name = "PAYMENT_RECEIVED")
    PAYMENT_RECEIVED,
    
    @Json(name = "PAYMENT_PENDING")
    PAYMENT_PENDING,
    
    @Json(name = "INQUIRY_RECEIVED")
    INQUIRY_RECEIVED,
    
    @Json(name = "REVIEW_RECEIVED")
    REVIEW_RECEIVED,
    
    @Json(name = "SYSTEM_NOTIFICATION")
    SYSTEM_NOTIFICATION,
    
    @Json(name = "MARKET_UPDATE")
    MARKET_UPDATE,
    
    @Json(name = "PROFILE_UPDATED")
    PROFILE_UPDATED;
    
    /**
     * Get activity type icon
     */
    fun getIcon(): String = when (this) {
        LISTING_CREATED -> "📝"
        LISTING_UPDATED -> "✏️"
        LISTING_EXPIRED -> "⏰"
        ORDER_PLACED -> "🛒"
        ORDER_CONFIRMED -> "✅"
        ORDER_COMPLETED -> "🎉"
        ORDER_CANCELLED -> "❌"
        PRICE_ALERT -> "💰"
        PAYMENT_RECEIVED -> "💳"
        PAYMENT_PENDING -> "⏳"
        INQUIRY_RECEIVED -> "💬"
        REVIEW_RECEIVED -> "⭐"
        SYSTEM_NOTIFICATION -> "🔔"
        MARKET_UPDATE -> "📈"
        PROFILE_UPDATED -> "👤"
    }
    
    /**
     * Get activity type color
     */
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        LISTING_CREATED, LISTING_UPDATED -> androidx.compose.ui.graphics.Color(0xFF22c55e) // Green
        LISTING_EXPIRED, ORDER_CANCELLED -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
        ORDER_PLACED, ORDER_CONFIRMED, ORDER_COMPLETED -> androidx.compose.ui.graphics.Color(0xFF3b82f6) // Blue
        PRICE_ALERT, PAYMENT_RECEIVED -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        PAYMENT_PENDING -> androidx.compose.ui.graphics.Color(0xFF6b7280) // Gray
        INQUIRY_RECEIVED, REVIEW_RECEIVED -> androidx.compose.ui.graphics.Color(0xFF8b5cf6) // Purple
        SYSTEM_NOTIFICATION, MARKET_UPDATE -> androidx.compose.ui.graphics.Color(0xFF06b6d4) // Cyan
        PROFILE_UPDATED -> androidx.compose.ui.graphics.Color(0xFF10b981) // Emerald
    }
    
    /**
     * Get activity type display name
     */
    fun getDisplayName(language: String): String = when (this) {
        LISTING_CREATED -> when (language) {
            "ta" -> "பட்டியல் உருவாக்கப்பட்டது"
            "si" -> "ලැයිස්තුව නිර්මාණය කරන ලදී"
            else -> "Listing Created"
        }
        LISTING_UPDATED -> when (language) {
            "ta" -> "பட்டியல் புதுப்பிக்கப்பட்டது"
            "si" -> "ලැයිස්තුව යාවත්කාලීන කරන ලදී"
            else -> "Listing Updated"
        }
        LISTING_EXPIRED -> when (language) {
            "ta" -> "பட்டியல் காலாவதியானது"
            "si" -> "ලැයිස්තුව කල් ඉකුත් විය"
            else -> "Listing Expired"
        }
        ORDER_PLACED -> when (language) {
            "ta" -> "ஆர்டர் வைக்கப்பட்டது"
            "si" -> "ඇණවුම ලබා දෙන ලදී"
            else -> "Order Placed"
        }
        ORDER_CONFIRMED -> when (language) {
            "ta" -> "ஆர்டர் உறுதிப்படுத்தப்பட்டது"
            "si" -> "ඇණවුම තහවුරු කරන ලදී"
            else -> "Order Confirmed"
        }
        ORDER_COMPLETED -> when (language) {
            "ta" -> "ஆர்டர் முடிக்கப்பட்டது"
            "si" -> "ඇණවුම සම්පූර්ණ කරන ලදී"
            else -> "Order Completed"
        }
        ORDER_CANCELLED -> when (language) {
            "ta" -> "ஆர்டர் ரத்து செய்யப்பட்டது"
            "si" -> "ඇණවුම අවලංගු කරන ලදී"
            else -> "Order Cancelled"
        }
        PRICE_ALERT -> when (language) {
            "ta" -> "விலை எச்சரிக்கை"
            "si" -> "මිල අනතුරු ඇඟවීම"
            else -> "Price Alert"
        }
        PAYMENT_RECEIVED -> when (language) {
            "ta" -> "பணம் பெறப்பட்டது"
            "si" -> "ගෙවීම ලැබුණි"
            else -> "Payment Received"
        }
        PAYMENT_PENDING -> when (language) {
            "ta" -> "பணம் நிலுவையில்"
            "si" -> "ගෙවීම අපේක්ෂිත"
            else -> "Payment Pending"
        }
        INQUIRY_RECEIVED -> when (language) {
            "ta" -> "விசாரணை பெறப்பட்டது"
            "si" -> "විමසීම ලැබුණි"
            else -> "Inquiry Received"
        }
        REVIEW_RECEIVED -> when (language) {
            "ta" -> "மதிப்பாய்வு பெறப்பட்டது"
            "si" -> "සමාලෝචනය ලැබුණි"
            else -> "Review Received"
        }
        SYSTEM_NOTIFICATION -> when (language) {
            "ta" -> "கணினி அறிவிப்பு"
            "si" -> "පද්ධති දැනුම්දීම"
            else -> "System Notification"
        }
        MARKET_UPDATE -> when (language) {
            "ta" -> "சந்தை புதுப்பிப்பு"
            "si" -> "වෙළඳපල යාවත්කාලීනය"
            else -> "Market Update"
        }
        PROFILE_UPDATED -> when (language) {
            "ta" -> "சுயவிவரம் புதுப்பிக்கப்பட்டது"
            "si" -> "පැතිකඩ යාවත්කාලීන කරන ලදී"
            else -> "Profile Updated"
        }
    }
}

/**
 * Entity Type enum for related entities
 */
enum class EntityType {
    @Json(name = "LISTING")
    LISTING,
    
    @Json(name = "TRANSACTION")
    TRANSACTION,
    
    @Json(name = "USER")
    USER,
    
    @Json(name = "MARKET_PRICE")
    MARKET_PRICE,
    
    @Json(name = "REVIEW")
    REVIEW,
    
    @Json(name = "PAYMENT")
    PAYMENT
}

/**
 * Activity Status enum
 */
enum class ActivityStatus {
    @Json(name = "ACTIVE")
    ACTIVE,
    
    @Json(name = "ARCHIVED")
    ARCHIVED,
    
    @Json(name = "DISMISSED")
    DISMISSED
}

/**
 * Activity Priority enum
 */
enum class ActivityPriority {
    @Json(name = "LOW")
    LOW,
    
    @Json(name = "NORMAL")
    NORMAL,
    
    @Json(name = "HIGH")
    HIGH,
    
    @Json(name = "URGENT")
    URGENT;
    
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        LOW -> androidx.compose.ui.graphics.Color(0xFF6b7280) // Gray
        NORMAL -> androidx.compose.ui.graphics.Color(0xFF3b82f6) // Blue
        HIGH -> androidx.compose.ui.graphics.Color(0xFFf59e0b) // Amber
        URGENT -> androidx.compose.ui.graphics.Color(0xFFef4444) // Red
    }
}

// ============================================================================
// EXTENSION FUNCTIONS FOR ACTIVITY
// ============================================================================

/**
 * Get title in specified language
 */
fun Activity.getTitle(language: String): String = when (language) {
    "ta" -> if (titleTamil.isNotEmpty()) titleTamil else title
    "si" -> if (titleSinhala.isNotEmpty()) titleSinhala else title
    else -> title
}

/**
 * Get description in specified language
 */
fun Activity.getDescription(language: String): String = when (language) {
    "ta" -> if (descriptionTamil.isNotEmpty()) descriptionTamil else description
    "si" -> if (descriptionSinhala.isNotEmpty()) descriptionSinhala else description
    else -> description
}

/**
 * Get formatted timestamp
 */
fun Activity.getFormattedTimestamp(language: String = "en"): String {
    return try {
        val instant = Instant.parse(timestamp)
        val formatter = when (language) {
            "ta" -> DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            "si" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            else -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        }
        instant.atZone(java.time.ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        timestamp
    }
}

/**
 * Check if activity requires user action
 */
fun Activity.requiresAction(): Boolean {
    return isActionable && !isRead && status == ActivityStatus.ACTIVE && !isExpired()
}

/**
 * Get activity icon
 */
fun Activity.getIcon(): String = activityType.getIcon()

/**
 * Get activity color
 */
fun Activity.getColor(): androidx.compose.ui.graphics.Color = activityType.getColor()

/**
 * Get priority color
 */
fun Activity.getPriorityColor(): androidx.compose.ui.graphics.Color = priority.getColor()

/**
 * Create activity summary for display
 */
fun Activity.getSummary(language: String = "en"): String {
    val title = getTitle(language)
    val time = getRelativeTime(language)
    return "$title • $time"
}

// ============================================================================
// ACTIVITY UTILITIES
// ============================================================================

object ActivityUtils {
    /**
     * Create a listing created activity
     */
    fun createListingActivity(
        userId: String,
        listingId: String,
        cropName: String,
        language: String = "en"
    ): Activity {
        return Activity(
            userId = userId,
            activityType = ActivityType.LISTING_CREATED,
            title = when (language) {
                "ta" -> "புதிய பட்டியல் உருவாக்கப்பட்டது"
                "si" -> "නව ලැයිස්තුව නිර්මාණය කරන ලදී"
                else -> "New listing created"
            },
            description = when (language) {
                "ta" -> "$cropName பட்டியல் வெற்றிகரமாக உருவாக்கப்பட்டது"
                "si" -> "$cropName ලැයිස්තුව සාර්ථකව නිර්මාණය කරන ලදී"
                else -> "Your $cropName listing has been created successfully"
            },
            relatedEntityType = EntityType.LISTING,
            relatedEntityId = listingId,
            isActionable = false
        )
    }

    /**
     * Create an order placed activity
     */
    fun createOrderActivity(
        userId: String,
        transactionId: String,
        cropName: String,
        quantity: Double,
        language: String = "en"
    ): Activity {
        return Activity(
            userId = userId,
            activityType = ActivityType.ORDER_PLACED,
            title = when (language) {
                "ta" -> "புதிய ஆர்டர் பெறப்பட்டது"
                "si" -> "නව ඇණවුමක් ලැබුණි"
                else -> "New order received"
            },
            description = when (language) {
                "ta" -> "$quantity கிலோ $cropName க்கான ஆர்டர் பெறப்பட்டது"
                "si" -> "කිලෝ $quantity $cropName සඳහා ඇණවුමක් ලැබුණි"
                else -> "Order received for $quantity kg of $cropName"
            },
            relatedEntityType = EntityType.TRANSACTION,
            relatedEntityId = transactionId,
            priority = ActivityPriority.HIGH,
            isActionable = true
        )
    }

    /**
     * Create a price alert activity
     */
    fun createPriceAlertActivity(
        userId: String,
        cropName: String,
        currentPrice: Double,
        targetPrice: Double,
        language: String = "en"
    ): Activity {
        return Activity(
            userId = userId,
            activityType = ActivityType.PRICE_ALERT,
            title = when (language) {
                "ta" -> "விலை எச்சரிக்கை"
                "si" -> "මිල අනතුරු ඇඟවීම"
                else -> "Price Alert"
            },
            description = when (language) {
                "ta" -> "$cropName விலை ரூ $currentPrice ஆக உயர்ந்துள்ளது (இலக்கு: ரூ $targetPrice)"
                "si" -> "$cropName මිල රු $currentPrice දක්වා ඉහළ ගොස් ඇත (ඉලක්කය: රු $targetPrice)"
                else -> "$cropName price has reached LKR $currentPrice (target: LKR $targetPrice)"
            },
            priority = ActivityPriority.HIGH,
            isActionable = true
        )
    }

    /**
     * Filter activities by type
     */
    fun filterByType(activities: List<Activity>, types: List<ActivityType>): List<Activity> {
        return activities.filter { it.activityType in types }
    }

    /**
     * Filter activities by status
     */
    fun filterByStatus(activities: List<Activity>, status: ActivityStatus): List<Activity> {
        return activities.filter { it.status == status }
    }

    /**
     * Filter unread activities
     */
    fun getUnreadActivities(activities: List<Activity>): List<Activity> {
        return activities.filter { !it.isRead && it.status == ActivityStatus.ACTIVE }
    }

    /**
     * Filter actionable activities
     */
    fun getActionableActivities(activities: List<Activity>): List<Activity> {
        return activities.filter { it.requiresAction() }
    }

    /**
     * Sort activities by priority and timestamp
     */
    fun sortByPriorityAndTime(activities: List<Activity>): List<Activity> {
        return activities.sortedWith(
            compareByDescending<Activity> { it.priority.ordinal }
                .thenByDescending { it.timestamp }
        )
    }
}

class ActivityConverters {
    @TypeConverter
    fun fromActivityType(value: ActivityType): String = value.name

    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)

    @TypeConverter
    fun fromEntityType(value: EntityType?): String? = value?.name

    @TypeConverter
    fun toEntityType(value: String?): EntityType? = value?.let { EntityType.valueOf(it) }

    @TypeConverter
    fun fromActivityStatus(value: ActivityStatus): String = value.name

    @TypeConverter
    fun toActivityStatus(value: String): ActivityStatus = ActivityStatus.valueOf(value)

    @TypeConverter
    fun fromActivityPriority(value: ActivityPriority): String = value.name

    @TypeConverter
    fun toActivityPriority(value: String): ActivityPriority = ActivityPriority.valueOf(value)

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate {
            val parts = it.split(":", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }
}
