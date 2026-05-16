package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Weather Widget Component
 * Displays current weather information for farmers
 */
@Composable
fun WeatherWidget(
    temperature: Int = 28,
    condition: WeatherCondition = WeatherCondition.SUNNY,
    humidity: Int = 65,
    windSpeed: Int = 12,
    location: String = "Jaffna",
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (condition) {
                WeatherCondition.SUNNY -> Color(0xFF60a5fa).copy(alpha = 0.1f)
                WeatherCondition.CLOUDY -> Color(0xFF6b7280).copy(alpha = 0.1f)
                WeatherCondition.RAINY -> Color(0xFF3b82f6).copy(alpha = 0.15f)
                WeatherCondition.PARTLY_CLOUDY -> Color(0xFF60a5fa).copy(alpha = 0.12f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Weather"
                        "ta" -> "வானிலை"
                        "si" -> "කාලගුණය"
                        else -> "Weather"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Main weather display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature and condition
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = condition.getEmoji(),
                        fontSize = 48.sp
                    )
                    Column {
                        Text(
                            text = "${temperature}°C",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = condition.getName(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider()

            // Additional weather details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(
                    icon = Icons.Default.Star,
                    label = when (currentLanguage) {
                        "en" -> "Humidity"
                        "ta" -> "ஈரப்பதம்"
                        "si" -> "ආර්ද්‍රතාව"
                        else -> "Humidity"
                    },
                    value = "$humidity%"
                )

                WeatherDetail(
                    icon = Icons.Default.Send,
                    label = when (currentLanguage) {
                        "en" -> "Wind"
                        "ta" -> "காற்று"
                        "si" -> "සුළං"
                        else -> "Wind"
                    },
                    value = "$windSpeed km/h"
                )
            }

            // Farming tip based on weather
            val farmingTip = getFarmingTip(condition, currentLanguage)
            if (farmingTip.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = farmingTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Weather detail item (humidity, wind, etc.)
 */
@Composable
private fun WeatherDetail(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Weather conditions enum
 */
enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    PARTLY_CLOUDY;

    fun getEmoji(): String = when (this) {
        SUNNY -> "☀️"
        CLOUDY -> "☁️"
        RAINY -> "🌧️"
        PARTLY_CLOUDY -> "⛅"
    }

    fun getName(language: String): String = when (this) {
        SUNNY -> when (language) {
            "en" -> "Sunny"
            "ta" -> "வெயில்"
            "si" -> "අව්ව"
            else -> "Sunny"
        }
        CLOUDY -> when (language) {
            "en" -> "Cloudy"
            "ta" -> "மேகமூட்டம்"
            "si" -> "වලාකුළු සහිත"
            else -> "Cloudy"
        }
        RAINY -> when (language) {
            "en" -> "Rainy"
            "ta" -> "மழை"
            "si" -> "වැසි"
            else -> "Rainy"
        }
        PARTLY_CLOUDY -> when (language) {
            "en" -> "Partly Cloudy"
            "ta" -> "சிறிது மேகமூட்டம்"
            "si" -> "අර්ධ වලාකුළු සහිත"
            else -> "Partly Cloudy"
        }
    }
}

/**
 * Get farming tip based on weather condition
 */
private fun getFarmingTip(condition: WeatherCondition, language: String): String {
    return when (condition) {
        WeatherCondition.SUNNY -> when (language) {
            "en" -> "Good day for harvesting and drying crops"
            "ta" -> "அறுவடை மற்றும் பயிர்களை உலர்த்துவதற்கு நல்ல நாள்"
            "si" -> "අස්වනු නෙලීම සහ බෝග වියළීම සඳහා හොඳ දිනයකි"
            else -> "Good day for harvesting and drying crops"
        }
        WeatherCondition.RAINY -> when (language) {
            "en" -> "Avoid harvesting. Good for irrigation"
            "ta" -> "அறுவடையைத் தவிர்க்கவும். நீர்ப்பாசனத்திற்கு நல்லது"
            "si" -> "අස්වනු නෙලීම වළක්වන්න. වාරිමාර්ග සඳහා හොඳයි"
            else -> "Avoid harvesting. Good for irrigation"
        }
        WeatherCondition.CLOUDY -> when (language) {
            "en" -> "Moderate conditions for farming activities"
            "ta" -> "விவசாய நடவடிக்கைகளுக்கு மிதமான சூழ்நிலை"
            "si" -> "ගොවිතැන් ක්‍රියාකාරකම් සඳහා මධ්‍යස්ථ තත්ත්වයන්"
            else -> "Moderate conditions for farming activities"
        }
        WeatherCondition.PARTLY_CLOUDY -> when (language) {
            "en" -> "Good conditions for most farming work"
            "ta" -> "பெரும்பாலான விவசாய வேலைகளுக்கு நல்ல சூழ்நிலை"
            "si" -> "බොහෝ ගොවිතැන් වැඩ සඳහා හොඳ තත්ත්වයන්"
            else -> "Good conditions for most farming work"
        }
    }
}

/**
 * Compact weather widget for smaller spaces
 */
@Composable
fun CompactWeatherWidget(
    temperature: Int = 28,
    condition: WeatherCondition = WeatherCondition.SUNNY,
    location: String = "Jaffna",
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = condition.getEmoji(),
                fontSize = 24.sp
            )
            Column {
                Text(
                    text = "${temperature}°C",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

