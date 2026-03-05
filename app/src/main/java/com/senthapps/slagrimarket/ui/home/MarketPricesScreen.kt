package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.SampleMarketPrices
import com.senthapps.slagrimarket.ui.common.LanguageToggleButton
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.settings.AccessibilityViewModel
import com.senthapps.slagrimarket.ui.theme.FieldMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPricesScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel(),
    accessibilityViewModel: AccessibilityViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isFieldMode by accessibilityViewModel.isFieldModeEnabled.collectAsState()
    val marketPrices = SampleMarketPrices.SAMPLE_PRICES

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> stringResource(R.string.market_prices_title)
                            "ta" -> stringResource(R.string.market_prices_title)
                            "si" -> stringResource(R.string.market_prices_title)
                            else -> "${stringResource(R.string.market_prices_title)} / Market Prices"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = languageViewModel::setLanguage,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // DoA source badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🌿 DoA Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isFieldMode) 1 else 2),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(marketPrices) { marketPrice ->
                MarketPriceDetailCard(
                    marketPrice = marketPrice,
                    currentLanguage = currentLanguage,
                    isFieldMode = isFieldMode
                )
            }
        }
        } // end Column
    }
}

@Composable
private fun MarketPriceDetailCard(
    marketPrice: MarketPrice,
    currentLanguage: String,
    isFieldMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFieldMode) FieldMode.Surface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Crop name
            Text(
                text = SampleMarketPrices.getCropName(marketPrice, currentLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                color = if (isFieldMode) FieldMode.Text else MaterialTheme.colorScheme.onSurface
            )

            // Price section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // 40sp price — large enough for bright sun + aging eyes
                    Text(
                        text = "LKR ${String.format("%.0f", marketPrice.pricePerKg)}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFieldMode) FieldMode.Accent else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "per kg"
                            "ta" -> "ஒரு கிலோ"
                            "si" -> "කිලෝ ග්‍රාමයකට"
                            else -> "per kg"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFieldMode) FieldMode.Text.copy(alpha = 0.7f)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trend — simple bold arrow in field mode, full chip otherwise
                if (isFieldMode) {
                    Text(
                        text = when (marketPrice.trend.name) {
                            "UP" -> "↑"; "DOWN" -> "↓"; else -> "→"
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = when (marketPrice.trend.name) {
                            "UP" -> Color(0xFF2D5016)
                            "DOWN" -> Color(0xFFA63D2F)
                            else -> Color(0xFF6B6B6B)
                        }
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SampleMarketPrices.getTrendColor(marketPrice.trend).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = SampleMarketPrices.getTrendIcon(marketPrice.trend),
                                fontSize = 16.sp
                            )
                            Text(
                                text = when (marketPrice.trend.name) {
                                    "UP" -> when (currentLanguage) {
                                        "en" -> "Rising"
                                        "ta" -> "உயர்வு"
                                        "si" -> "ඉහළ යමින්"
                                        else -> "Rising"
                                    }
                                    "DOWN" -> when (currentLanguage) {
                                        "en" -> "Falling"
                                        "ta" -> "வீழ்ச்சி"
                                        "si" -> "පහත වැටෙමින්"
                                        else -> "Falling"
                                    }
                                    else -> when (currentLanguage) {
                                        "en" -> "Stable"
                                        "ta" -> "நிலையான"
                                        "si" -> "ස්ථාවර"
                                        else -> "Stable"
                                    }
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = SampleMarketPrices.getTrendColor(marketPrice.trend),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Location
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isFieldMode) FieldMode.Background
                       else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = SampleMarketPrices.getLocationName(marketPrice.location, currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFieldMode) FieldMode.Text.copy(alpha = 0.8f)
                           else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
