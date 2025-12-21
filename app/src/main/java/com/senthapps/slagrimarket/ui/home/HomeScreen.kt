package com.senthapps.slagrimarket.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.*
import com.senthapps.slagrimarket.ui.common.LanguageToggleButton
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.components.WeatherWidget
import com.senthapps.slagrimarket.ui.components.WeatherCondition
import com.senthapps.slagrimarket.ui.theme.DarkGradientBackground
import com.senthapps.slagrimarket.ui.theme.HeroGradientBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToListings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToMarketPrices: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher_round),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    "en" -> stringResource(R.string.app_title_english)
                                    "ta" -> stringResource(R.string.app_title_tamil)
                                    "si" -> stringResource(R.string.app_title_sinhala)
                                    else -> "${stringResource(R.string.app_title_tamil)} / ${stringResource(R.string.app_title_english)}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentLanguage !in listOf("en", "ta", "si")) {
                                Text(
                                    text = stringResource(R.string.app_title_english),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = languageViewModel::setLanguage,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.nav_profile)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser?.userType == UserType.FARMER) {
                FloatingActionButton(
                    onClick = onNavigateToCreateListing,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Listing"
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Welcome section with animation
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        WelcomeSection(
                            userName = currentUser?.name ?: "Demo User",
                            userType = currentUser?.userType ?: UserType.BUYER,
                            currentLanguage = currentLanguage,
                            todayOrders = uiState.todayOrders,
                            todayRevenue = uiState.todayRevenue,
                            isLoading = uiState.isLoadingStats
                        )
                    }
                }

                // Quick actions with animation
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        QuickActionsSection(
                            userType = currentUser?.userType ?: UserType.BUYER,
                            onNavigateToListings = onNavigateToListings,
                            onNavigateToCreateListing = onNavigateToCreateListing,
                            onNavigateToTransactions = onNavigateToTransactions,
                            onNavigateToAnalytics = onNavigateToAnalytics,
                            currentLanguage = currentLanguage
                        )
                    }
                }

                // Weather widget
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        WeatherWidget(
                            temperature = 28,
                            condition = WeatherCondition.SUNNY,
                            humidity = 65,
                            windSpeed = 12,
                            location = "Jaffna",
                            currentLanguage = currentLanguage
                        )
                    }
                }

                // Market prices section with loading state
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        MarketPricesSectionEnhanced(
                            marketPrices = uiState.marketPrices,
                            isLoading = uiState.isLoadingPrices,
                            onViewAllPrices = onNavigateToMarketPrices,
                            currentLanguage = currentLanguage
                        )
                    }
                }

                // Recent activities section
                item {
                    AnimatedVisibility(
                        visible = uiState.recentActivities.isNotEmpty() || uiState.isLoadingActivities,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        RecentActivitiesSection(
                            activities = uiState.recentActivities,
                            isLoading = uiState.isLoadingActivities,
                            currentLanguage = currentLanguage
                        )
                    }
                }

                // Popular crops
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        PopularCropsSection(
                            currentLanguage = currentLanguage,
                            onNavigateToListings = onNavigateToListings
                        )
                    }
                }

            // Recent listings
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    RecentListingsSection(
                        listings = uiState.recentListings,
                        onViewAllListings = onNavigateToListings,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    userName: String,
    userType: UserType,
    currentLanguage: String,
    todayOrders: Int,
    todayRevenue: Double,
    isLoading: Boolean
) {
    // Hero section with gradient background
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroGradientBackground)
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome message with emoji - animated
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Welcome, $userName!"
                            "ta" -> "வரவேற்கிறோம், $userName!"
                            "si" -> "ආයුබෝවන්, $userName!"
                            else -> "Welcome, $userName! / வரவேற்கிறோம், $userName!"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Subtitle
                Text(
                    text = when (userType) {
                        UserType.FARMER -> when (currentLanguage) {
                            "en" -> "Ready to connect with buyers and grow your business"
                            "ta" -> "வாங்குபவர்களுடன் இணைந்து உங்கள் வணிகத்தை வளர்க்க தயாராகுங்கள்"
                            "si" -> "ගැනුම්කරුවන් සමඟ සම්බන්ධ වී ඔබේ ව්‍යාපාරය වර්ධනය කිරීමට සූදානම්"
                            else -> "Ready to connect with buyers and grow your business"
                        }
                        else -> when (currentLanguage) {
                            "en" -> "Discover fresh produce from local farmers"
                            "ta" -> "உள்ளூர் விவசாயிகளிடமிருந்து புதிய பொருட்களைக் கண்டறியுங்கள்"
                            "si" -> "ප්‍රාදේශීය ගොවීන්ගෙන් නැවුම් නිෂ්පාදන සොයා ගන්න"
                            else -> "Discover fresh produce from local farmers"
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // Stats cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Today's Orders card
                    StatsCard(
                        title = when (currentLanguage) {
                            "en" -> "Today's Orders"
                            "ta" -> "இன்றைய ஆர்டர்கள்"
                            "si" -> "අද ඇණවුම්"
                            else -> "Today's Orders"
                        },
                        value = if (isLoading) "..." else todayOrders.toString(),
                        modifier = Modifier.weight(1f),
                        isLoading = isLoading
                    )

                    // Revenue card
                    StatsCard(
                        title = when (currentLanguage) {
                            "en" -> "Revenue"
                            "ta" -> "வருமானம்"
                            "si" -> "ආදායම"
                            else -> "Revenue"
                        },
                        value = if (isLoading) "..." else "LKR ${String.format("%.0f", todayRevenue / 1000)}K",
                        modifier = Modifier.weight(1f),
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    userType: UserType,
    onNavigateToListings: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    currentLanguage: String
) {
    Column {
        Text(
            text = when (currentLanguage) {
                "en" -> stringResource(R.string.home_quick_actions)
                "ta" -> stringResource(R.string.home_quick_actions)
                "si" -> stringResource(R.string.home_quick_actions)
                else -> "${stringResource(R.string.home_quick_actions)} / Quick Actions"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    title = when (currentLanguage) {
                        "en" -> stringResource(R.string.action_browse)
                        "ta" -> stringResource(R.string.action_browse)
                        "si" -> stringResource(R.string.action_browse)
                        else -> "${stringResource(R.string.action_browse)} / Browse"
                    },
                    subtitle = if (currentLanguage in listOf("en", "ta", "si")) "" else "Browse",
                    icon = Icons.Default.Search,
                    onClick = onNavigateToListings,
                    currentLanguage = currentLanguage
                )
            }

            item {
                QuickActionCard(
                    title = when (currentLanguage) {
                        "en" -> "Analytics"
                        "ta" -> "பகுப்பாய்வு"
                        "si" -> "විශ්ලේෂණ"
                        else -> "Analytics"
                    },
                    subtitle = when (currentLanguage) {
                        "en" -> "View insights"
                        "ta" -> "நுண்ணறிவுகளைக் காண்க"
                        "si" -> "තීක්ෂ්ණ බව බලන්න"
                        else -> "View insights"
                    },
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAnalytics,
                    currentLanguage = currentLanguage
                )
            }

            item {
                QuickActionCard(
                    title = when (currentLanguage) {
                        "en" -> stringResource(R.string.nav_transactions)
                        "ta" -> stringResource(R.string.nav_transactions)
                        "si" -> stringResource(R.string.nav_transactions)
                        else -> "${stringResource(R.string.nav_transactions)} / Transactions"
                    },
                    subtitle = when (currentLanguage) {
                        "en" -> "View orders"
                        "ta" -> "ஆர்டர்களைக் காண்க"
                        "si" -> "ඇණවුම් බලන්න"
                        else -> "View orders"
                    },
                    icon = Icons.Default.List,
                    onClick = onNavigateToTransactions,
                    currentLanguage = currentLanguage
                )
            }

            if (userType == UserType.FARMER) {
                item {
                    QuickActionCard(
                        title = when (currentLanguage) {
                            "en" -> stringResource(R.string.action_sell)
                            "ta" -> stringResource(R.string.action_sell)
                            "si" -> stringResource(R.string.action_sell)
                            else -> "${stringResource(R.string.action_sell)} / Sell"
                        },
                        subtitle = when (currentLanguage) {
                            "en" -> "Create listing"
                            "ta" -> "பட்டியல் உருவாக்கவும்"
                            "si" -> "ලැයිස්තුව සාදන්න"
                            else -> "Create listing"
                        },
                        icon = Icons.Default.Add,
                        onClick = onNavigateToCreateListing,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    currentLanguage: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp, 120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with background circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularCropsSection(
    currentLanguage: String,
    onNavigateToListings: () -> Unit
) {
    Column {
        Text(
            text = when (currentLanguage) {
                "en" -> stringResource(R.string.home_popular_crops)
                "ta" -> stringResource(R.string.home_popular_crops)
                "si" -> stringResource(R.string.home_popular_crops)
                else -> "${stringResource(R.string.home_popular_crops)} / Popular Crops"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CropTypes.ALL_CROPS.take(6)) { cropType ->
                FilterChip(
                    onClick = {
                        // Navigate to listings screen where users can filter by crop type
                        onNavigateToListings()
                    },
                    label = { Text(CropTypes.getCropName(cropType, currentLanguage)) },
                    selected = false
                )
            }
        }
    }
}

@Composable
private fun RecentListingsSection(
    listings: List<Any>, // Replace with actual Listing type
    onViewAllListings: () -> Unit,
    currentLanguage: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> stringResource(R.string.home_recent_listings)
                    "ta" -> stringResource(R.string.home_recent_listings)
                    "si" -> stringResource(R.string.home_recent_listings)
                    else -> "${stringResource(R.string.home_recent_listings)} / Recent Listings"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAllListings) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> stringResource(R.string.action_view_all)
                        "ta" -> stringResource(R.string.action_view_all)
                        "si" -> stringResource(R.string.action_view_all)
                        else -> stringResource(R.string.action_view_all)
                    }
                )
            }
        }

        if (listings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (currentLanguage) {
                            "en" -> stringResource(R.string.home_no_listings)
                            "ta" -> stringResource(R.string.home_no_listings)
                            "si" -> stringResource(R.string.home_no_listings)
                            else -> "${stringResource(R.string.home_no_listings)} / No listings available"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (currentLanguage !in listOf("en", "ta", "si")) {
                        Text(
                            text = "No listings available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ENHANCED MARKET PRICES SECTION WITH LOADING STATE
// ============================================================================

@Composable
private fun MarketPricesSectionEnhanced(
    marketPrices: List<MarketPrice>,
    isLoading: Boolean,
    onViewAllPrices: () -> Unit,
    currentLanguage: String
) {
    Column {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> stringResource(R.string.home_market_prices)
                    "ta" -> stringResource(R.string.home_market_prices)
                    "si" -> stringResource(R.string.home_market_prices)
                    else -> "${stringResource(R.string.home_market_prices)} / Market Prices"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAllPrices) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> stringResource(R.string.action_view_all)
                        "ta" -> stringResource(R.string.action_view_all)
                        "si" -> stringResource(R.string.action_view_all)
                        else -> "${stringResource(R.string.action_view_all)} / View All"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Market prices horizontal scroll with shimmer loading
        if (isLoading && marketPrices.isEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(3) {
                    ShimmerMarketPriceCard()
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(marketPrices.take(10)) { index, marketPrice ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 50
                            )
                        ) + slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 50
                            )
                        )
                    ) {
                        EnhancedMarketPriceCard(
                            marketPrice = marketPrice,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerMarketPriceCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Card(
        modifier = Modifier.size(width = 180.dp, height = 160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    ) {
        // Shimmer placeholder
    }
}

@Composable
private fun EnhancedMarketPriceCard(
    marketPrice: MarketPrice,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.size(width = 180.dp, height = 160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Crop name with emoji
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = SampleMarketPrices.getCropEmoji(marketPrice.cropType),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when (currentLanguage) {
                        "ta" -> marketPrice.cropNameTamil
                        "si" -> marketPrice.cropNameSinhala
                        else -> marketPrice.cropNameEnglish
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Price and trend
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LKR ${String.format("%.0f", marketPrice.currentPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Trend indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (marketPrice.trend) {
                            PriceTrend.UP -> Color(0xFF16a34a).copy(alpha = 0.1f)
                            PriceTrend.DOWN -> Color(0xFFdc2626).copy(alpha = 0.1f)
                            PriceTrend.STABLE -> Color(0xFF6b7280).copy(alpha = 0.1f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = when (marketPrice.trend) {
                                    PriceTrend.UP -> "↗"
                                    PriceTrend.DOWN -> "↘"
                                    PriceTrend.STABLE -> "→"
                                },
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${String.format("%.1f", marketPrice.changePercentage)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = when (marketPrice.trend) {
                                    PriceTrend.UP -> Color(0xFF16a34a)
                                    PriceTrend.DOWN -> Color(0xFFdc2626)
                                    PriceTrend.STABLE -> Color(0xFF6b7280)
                                }
                            )
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        "en" -> "per kg"
                        "ta" -> "ஒரு கிலோ"
                        "si" -> "කිලෝ ග්‍රාමයකට"
                        else -> "per kg"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (currentLanguage) {
                        "ta" -> marketPrice.locationTamil.ifEmpty { marketPrice.location }
                        "si" -> marketPrice.locationSinhala.ifEmpty { marketPrice.location }
                        else -> marketPrice.location
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// RECENT ACTIVITIES SECTION
// ============================================================================

@Composable
private fun RecentActivitiesSection(
    activities: List<Activity>,
    isLoading: Boolean,
    currentLanguage: String
) {
    Column {
        // Section header
        Text(
            text = when (currentLanguage) {
                "en" -> "Recent Activity"
                "ta" -> "சமீபத்திய செயல்பாடு"
                "si" -> "මෑත ක්‍රියාකාරකම්"
                else -> "Recent Activity"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading && activities.isEmpty()) {
            // Loading state
            repeat(3) {
                ShimmerActivityCard()
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (activities.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "No recent activity"
                            "ta" -> "சமீபத்திய செயல்பாடு இல்லை"
                            "si" -> "මෑත ක්‍රියාකාරකම් නැත"
                            else -> "No recent activity"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Activity list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    ActivityCard(
                        activity = activity,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerActivityCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(activity.getColor().copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.getIcon(),
                    fontSize = 20.sp
                )
            }

            // Activity details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activity.getTitle(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activity.getDescription(currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activity.getRelativeTime(currentLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Priority indicator (if high or urgent)
            if (activity.priority == ActivityPriority.HIGH || activity.priority == ActivityPriority.URGENT) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activity.getPriorityColor())
                )
            }
        }
    }
}
