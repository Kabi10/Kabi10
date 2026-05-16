package com.senthapps.slagrimarket.util

import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Utility object for calculating and formatting relative time strings.
 * Supports multiple languages (English, Sinhala, Tamil).
 */
object RelativeTimeUtil {

    /**
     * Calculates the relative time string from an ISO 8601 timestamp.
     *
     * @param timestamp ISO 8601 formatted timestamp (e.g., "2024-01-15T10:30:00Z")
     * @param language Language code ("en", "si", "ta") - defaults to "en"
     * @return Human-readable relative time string (e.g., "5 min ago", "2 hours ago")
     */
    fun getRelativeTimeString(timestamp: String?, language: String = "en"): String {
        if (timestamp.isNullOrBlank()) {
            return getJustNowText(language)
        }

        return try {
            val instant = Instant.parse(timestamp)
            val now = Instant.now()
            val duration = Duration.between(instant, now)

            formatDuration(duration, language)
        } catch (e: DateTimeParseException) {
            // If parsing fails, return "just now" as fallback
            getJustNowText(language)
        } catch (e: Exception) {
            getJustNowText(language)
        }
    }

    /**
     * Calculates the relative time string from a timestamp in milliseconds.
     *
     * @param timestampMillis Unix timestamp in milliseconds
     * @param language Language code ("en", "si", "ta") - defaults to "en"
     * @return Human-readable relative time string
     */
    fun getRelativeTimeString(timestampMillis: Long, language: String = "en"): String {
        if (timestampMillis <= 0) {
            return getJustNowText(language)
        }

        return try {
            val instant = Instant.ofEpochMilli(timestampMillis)
            val now = Instant.now()
            val duration = Duration.between(instant, now)

            formatDuration(duration, language)
        } catch (e: Exception) {
            getJustNowText(language)
        }
    }

    /**
     * Formats "UPDATED: X time ago" for market prices screen.
     *
     * @param timestamp ISO 8601 formatted timestamp
     * @param language Language code ("en", "si", "ta")
     * @return Formatted update time string (e.g., "UPDATED: 5 MIN AGO")
     */
    fun getUpdatedTimeString(timestamp: String?, language: String = "en"): String {
        val relativeTime = getRelativeTimeString(timestamp, language)
        return when (language) {
            "si" -> "යාවත්කාලීන: $relativeTime"
            "ta" -> "புதுப்பிக்கப்பட்டது: $relativeTime"
            else -> "UPDATED: ${relativeTime.uppercase()}"
        }
    }

    /**
     * Formats "UPDATED: X time ago" for market prices using milliseconds timestamp.
     *
     * @param timestampMillis Unix timestamp in milliseconds
     * @param language Language code ("en", "si", "ta")
     * @return Formatted update time string
     */
    fun getUpdatedTimeString(timestampMillis: Long, language: String = "en"): String {
        val relativeTime = getRelativeTimeString(timestampMillis, language)
        return when (language) {
            "si" -> "යාවත්කාලීන: $relativeTime"
            "ta" -> "புதுப்பிக்கப்பட்டது: $relativeTime"
            else -> "UPDATED: ${relativeTime.uppercase()}"
        }
    }

    private fun formatDuration(duration: Duration, language: String): String {
        val seconds = duration.seconds

        return when {
            seconds < 0 -> getJustNowText(language) // Future time
            seconds < 60 -> getJustNowText(language)
            seconds < 3600 -> {
                val minutes = seconds / 60
                formatMinutes(minutes.toInt(), language)
            }
            seconds < 86400 -> {
                val hours = seconds / 3600
                formatHours(hours.toInt(), language)
            }
            seconds < 604800 -> {
                val days = seconds / 86400
                formatDays(days.toInt(), language)
            }
            seconds < 2592000 -> {
                val weeks = seconds / 604800
                formatWeeks(weeks.toInt(), language)
            }
            else -> {
                val months = seconds / 2592000
                formatMonths(months.toInt(), language)
            }
        }
    }

    private fun getJustNowText(language: String): String {
        return when (language) {
            "si" -> "දැන්"
            "ta" -> "இப்போது"
            else -> "Just now"
        }
    }

    private fun formatMinutes(minutes: Int, language: String): String {
        return when (language) {
            "si" -> if (minutes == 1) "විනාඩි 1කට පෙර" else "විනාඩි ${minutes}කට පෙර"
            "ta" -> if (minutes == 1) "1 நிமிடம் முன்பு" else "$minutes நிமிடங்களுக்கு முன்பு"
            else -> if (minutes == 1) "1 min ago" else "$minutes min ago"
        }
    }

    private fun formatHours(hours: Int, language: String): String {
        return when (language) {
            "si" -> if (hours == 1) "පැය 1කට පෙර" else "පැය ${hours}කට පෙර"
            "ta" -> if (hours == 1) "1 மணி நேரத்திற்கு முன்பு" else "$hours மணி நேரத்திற்கு முன்பு"
            else -> if (hours == 1) "1 hour ago" else "$hours hours ago"
        }
    }

    private fun formatDays(days: Int, language: String): String {
        return when (language) {
            "si" -> when (days) {
                1 -> "ඊයේ"
                else -> "දින ${days}කට පෙර"
            }
            "ta" -> when (days) {
                1 -> "நேற்று"
                else -> "$days நாட்களுக்கு முன்பு"
            }
            else -> when (days) {
                1 -> "Yesterday"
                else -> "$days days ago"
            }
        }
    }

    private fun formatWeeks(weeks: Int, language: String): String {
        return when (language) {
            "si" -> if (weeks == 1) "සති 1කට පෙර" else "සති ${weeks}කට පෙර"
            "ta" -> if (weeks == 1) "1 வாரத்திற்கு முன்பு" else "$weeks வாரங்களுக்கு முன்பு"
            else -> if (weeks == 1) "1 week ago" else "$weeks weeks ago"
        }
    }

    private fun formatMonths(months: Int, language: String): String {
        return when (language) {
            "si" -> if (months == 1) "මාස 1කට පෙර" else "මාස ${months}කට පෙර"
            "ta" -> if (months == 1) "1 மாதத்திற்கு முன்பு" else "$months மாதங்களுக்கு முன்பு"
            else -> if (months == 1) "1 month ago" else "$months months ago"
        }
    }
}
