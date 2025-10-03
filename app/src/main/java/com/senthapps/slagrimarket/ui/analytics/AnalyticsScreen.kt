package com.senthapps.slagrimarket.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Analytics"
                            "ta" -> "பகுப்பாய்வு"
                            "si" -> "විශ්ලේෂණය"
                            else -> "Analytics"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadAnalytics() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                AnalyticsContent(
                    uiState = uiState,
                    currentLanguage = currentLanguage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview cards
        Text(
            text = when (currentLanguage) {
                "en" -> "Overview"
                "ta" -> "மேலோட்டம்"
                "si" -> "දළ විශ්ලේෂණය"
                else -> "Overview"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = when (currentLanguage) {
                    "en" -> "Total Sales"
                    "ta" -> "மொத்த விற்பனை"
                    "si" -> "මුළු විකුණුම්"
                    else -> "Total Sales"
                },
                value = "LKR ${String.format("%,.0f", uiState.totalRevenue)}",
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = when (currentLanguage) {
                    "en" -> "Orders"
                    "ta" -> "ஆர்டர்கள்"
                    "si" -> "ඇණවුම්"
                    else -> "Orders"
                },
                value = uiState.totalOrders.toString(),
                icon = Icons.Default.List,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = when (currentLanguage) {
                    "en" -> "Active Listings"
                    "ta" -> "செயலில் உள்ள பட்டியல்கள்"
                    "si" -> "ක්‍රියාත්මක ලැයිස්තු"
                    else -> "Active Listings"
                },
                value = uiState.activeListings.toString(),
                icon = Icons.Default.Star,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = when (currentLanguage) {
                    "en" -> "Total Views"
                    "ta" -> "மொத்த பார்வைகள்"
                    "si" -> "මුළු නැරඹුම්"
                    else -> "Total Views"
                },
                value = uiState.totalViews.toString(),
                icon = Icons.Default.Info,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Popular crops
        Text(
            text = when (currentLanguage) {
                "en" -> "Popular Crops"
                "ta" -> "பிரபலமான பயிர்கள்"
                "si" -> "ජනප්‍රිය බෝග"
                else -> "Popular Crops"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.popularCrops.isEmpty()) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "No data available"
                            "ta" -> "தரவு இல்லை"
                            "si" -> "දත්ත නොමැත"
                            else -> "No data available"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.popularCrops.forEach { crop ->
                        CropRow(
                            cropName = crop.name,
                            count = crop.count,
                            revenue = crop.revenue
                        )
                    }
                }
            }
        }

        // Recent activity
        Text(
            text = when (currentLanguage) {
                "en" -> "Recent Activity"
                "ta" -> "சமீபத்திய செயல்பாடு"
                "si" -> "මෑත ක්‍රියාකාරකම්"
                else -> "Recent Activity"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.recentActivity.isEmpty()) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "No recent activity"
                            "ta" -> "சமீபத்திய செயல்பாடு இல்லை"
                            "si" -> "මෑත ක්‍රියාකාරකම් නැත"
                            else -> "No recent activity"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.recentActivity.forEach { activity ->
                        ActivityRow(
                            activity = activity,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CropRow(
    cropName: String,
    count: Int,
    revenue: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cropName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count orders",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "LKR ${String.format("%,.0f", revenue)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ActivityRow(
    activity: ActivityItem,
    currentLanguage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = activity.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
