package com.senthapps.slagrimarket.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.ui.auth.AuthViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleButton
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSyncSettings: () -> Unit = {},
    onNavigateToListingDetail: (String) -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> stringResource(R.string.profile_title)
                            "ta" -> stringResource(R.string.profile_title)
                            "si" -> stringResource(R.string.profile_title)
                            else -> "${stringResource(R.string.profile_title)} / Profile"
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
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, "Edit Profile")
                    }
                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = languageViewModel::setLanguage,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.action_logout)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Section
            ProfileHeaderSection(
                userName = "Demo User",
                userType = currentUser?.userType?.name ?: "BUYER",
                verified = true,
                currentLanguage = currentLanguage
            )

            // Statistics Cards Section
            StatisticsCardsSection(currentLanguage = currentLanguage)

            // Rating System Section
            RatingSystemSection(currentLanguage = currentLanguage)

            // Location and Social Metrics Section
            LocationAndSocialMetricsSection(currentLanguage = currentLanguage)

            // Active Listings Gallery Section
            ActiveListingsGallerySection(
                currentLanguage = currentLanguage,
                onNavigateToListingDetail = onNavigateToListingDetail
            )

            // Settings/Preferences Section
            SettingsPreferencesSection(
                currentLanguage = currentLanguage,
                onNavigateToSyncSettings = onNavigateToSyncSettings
            )

            // Logout Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> stringResource(R.string.action_logout)
                            "ta" -> stringResource(R.string.action_logout)
                            "si" -> stringResource(R.string.action_logout)
                            else -> "${stringResource(R.string.action_logout)} / Logout"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Confirm Logout"
                        "ta" -> "வெளியேறுவதை உறுதிப்படுத்தவும்"
                        "si" -> "ඉවත් වීම තහවුරු කරන්න"
                        else -> "Confirm Logout"
                    }
                )
            },
            text = {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Are you sure you want to logout?"
                        "ta" -> "நீங்கள் நிச்சயமாக வெளியேற விரும்புகிறீர்களா?"
                        "si" -> "ඔබට නිශ්චිතව ඉවත් වීමට අවශ්‍යද?"
                        else -> "Are you sure you want to logout?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Logout"
                            "ta" -> "வெளியேறு"
                            "si" -> "ඉවත් වන්න"
                            else -> "Logout"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Cancel"
                            "ta" -> "ரத்துசெய்"
                            "si" -> "අවලංගු කරන්න"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileHeaderSection(
    userName: String,
    userType: String,
    verified: Boolean,
    currentLanguage: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Photo with enhanced styling
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 4.dp
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // User Name with enhanced styling
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Verification Badge with icon
            if (verified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Verified Farmer"
                            "ta" -> "சரிபார்க்கப்பட்ட விவசாயி"
                            "si" -> "සත්‍යාපිත ගොවියා"
                            else -> "Verified Farmer"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Location with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Jaffna District"
                        "ta" -> "யாழ்ப்பாணம் மாவட்டம்"
                        "si" -> "යාපනය දිස්ත්‍රික්කය"
                        else -> "Jaffna District"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun StatisticsCardsSection(currentLanguage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Followers card
        StatCard(
            title = when (currentLanguage) {
                "en" -> "Followers"
                "ta" -> "பின்தொடர்பவர்கள்"
                "si" -> "අනුගාමිකයින්"
                else -> "Followers"
            },
            value = "127",
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )

        // Completed Orders card
        StatCard(
            title = when (currentLanguage) {
                "en" -> "Completed Orders"
                "ta" -> "முடிக்கப்பட்ட ஆர்டர்கள்"
                "si" -> "සම්පූර්ණ කළ ඇණවුම්"
                else -> "Completed Orders"
            },
            value = "89",
            icon = Icons.Default.ShoppingCart,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with background
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RatingSystemSection(currentLanguage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Ratings"
                    "ta" -> "மதிப்பீடுகள்"
                    "si" -> "ශ්‍රේණිගත කිරීම්"
                    else -> "மதிப்பீடுகள் / Ratings"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Quality Rating
            EnhancedRatingRow(
                title = when (currentLanguage) {
                    "en" -> "Quality Rating"
                    "ta" -> "தரமான மதிப்பீடு"
                    "si" -> "ගුණාත්මක ශ්‍රේණිගත කිරීම"
                    else -> "Quality Rating"
                },
                rating = 4.2f,
                maxRating = 5
            )

            // Communication Rating
            EnhancedRatingRow(
                title = when (currentLanguage) {
                    "en" -> "Communication"
                    "ta" -> "தொடர்பு"
                    "si" -> "සන්නිවේදනය"
                    else -> "Communication"
                },
                rating = 4.7f,
                maxRating = 5
            )
        }
    }
}

@Composable
private fun EnhancedRatingRow(
    title: String,
    rating: Float,
    maxRating: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Enhanced star rating display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(maxRating) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < rating.toInt()) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RatingRow(
    title: String,
    rating: Float,
    maxRating: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Star rating display
            repeat(maxRating) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating.toInt()) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LocationAndSocialMetricsSection(currentLanguage: String) {
    // Location Display
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> stringResource(R.string.profile_location)
                    "ta" -> stringResource(R.string.profile_location)
                    "si" -> stringResource(R.string.profile_location)
                    else -> "${stringResource(R.string.profile_location)} / Jaffna District"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActiveListingsGallerySection(
    currentLanguage: String,
    onNavigateToListingDetail: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> stringResource(R.string.profile_active_listings)
                    "ta" -> stringResource(R.string.profile_active_listings)
                    "si" -> stringResource(R.string.profile_active_listings)
                    else -> "${stringResource(R.string.profile_active_listings)} / Active Listings"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 2-column grid of sample listings
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(getSampleListings()) { listing ->
                    ListingCard(
                        listing = listing,
                        currentLanguage = currentLanguage,
                        onNavigateToListingDetail = onNavigateToListingDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: SampleListing,
    currentLanguage: String,
    onNavigateToListingDetail: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Thumbnail placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Product name
            Text(
                text = when (currentLanguage) {
                    "en" -> listing.nameEnglish
                    "ta" -> listing.nameTamil
                    "si" -> listing.nameSinhala
                    else -> "${listing.nameTamil} / ${listing.nameEnglish}"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            // Price
            Text(
                text = "LKR ${listing.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // View Details button
            TextButton(
                onClick = { onNavigateToListingDetail(listing.id) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> stringResource(R.string.profile_view_details)
                        "ta" -> stringResource(R.string.profile_view_details)
                        "si" -> stringResource(R.string.profile_view_details)
                        else -> stringResource(R.string.profile_view_details)
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// Sample data class for listings
data class SampleListing(
    val id: String,
    val nameTamil: String,
    val nameEnglish: String,
    val nameSinhala: String,
    val price: String
)

// Sample listings data
private fun getSampleListings(): List<SampleListing> {
    return listOf(
        SampleListing("1", "வெங்காயம்", "Red Onion", "රතු ළූණු", "120.00"),
        SampleListing("2", "தக்காளி", "Tomato", "තක්කාලි", "95.00"),
        SampleListing("3", "மிளகாய்", "Chili", "මිරිස්", "280.00"),
        SampleListing("4", "கத்தரிக்காய்", "Brinjal", "වම්බටු", "85.00"),
        SampleListing("5", "வெண்டைக்காய்", "Okra", "බණ්ඩක්කා", "110.00"),
        SampleListing("6", "தேங்காய்", "Coconut", "පොල්", "45.00")
    )
}

@Composable
private fun SettingsPreferencesSection(
    currentLanguage: String,
    onNavigateToSyncSettings: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Settings & Preferences"
                    "ta" -> "அமைப்புகள் & விருப்பத்தேர்வுகள்"
                    "si" -> "සැකසුම් සහ මනාපයන්"
                    else -> "Settings & Preferences"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Language Setting
            SettingsItem(
                icon = Icons.Default.Settings,
                title = when (currentLanguage) {
                    "en" -> "Language"
                    "ta" -> "மொழி"
                    "si" -> "භාෂාව"
                    else -> "Language"
                },
                subtitle = when (currentLanguage) {
                    "en" -> "English"
                    "ta" -> "தமிழ்"
                    "si" -> "සිංහල"
                    else -> "English"
                },
                onClick = { /* Language is handled by top bar toggle */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Sync Settings
            SettingsItem(
                icon = Icons.Default.Settings,
                title = when (currentLanguage) {
                    "en" -> "Sync Settings"
                    "ta" -> "ஒத்திசைவு அமைப்புகள்"
                    "si" -> "සමමුහුර්ත සැකසුම්"
                    else -> "Sync Settings"
                },
                subtitle = when (currentLanguage) {
                    "en" -> "Manage offline sync preferences"
                    "ta" -> "ஆஃப்லைன் ஒத்திசைவு விருப்பங்களை நிர்வகிக்கவும்"
                    "si" -> "නොබැඳි සමමුහුර්ත මනාපයන් කළමනාකරණය කරන්න"
                    else -> "Manage offline sync preferences"
                },
                onClick = onNavigateToSyncSettings
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Help Setting
            SettingsItem(
                icon = Icons.Default.Info,
                title = when (currentLanguage) {
                    "en" -> "Help & Support"
                    "ta" -> "உதவி & ஆதரவு"
                    "si" -> "උදව් සහ සහාය"
                    else -> "Help & Support"
                },
                subtitle = when (currentLanguage) {
                    "en" -> "Get help and contact support"
                    "ta" -> "உதவி பெறவும் மற்றும் ஆதரவைத் தொடர்பு கொள்ளவும்"
                    "si" -> "උදව් ලබා ගන්න සහ සහාය අමතන්න"
                    else -> "Get help and contact support"
                },
                onClick = { /* TODO: Navigate to help screen */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // About Setting
            SettingsItem(
                icon = Icons.Default.Info,
                title = when (currentLanguage) {
                    "en" -> "About"
                    "ta" -> "பற்றி"
                    "si" -> "පිළිබඳව"
                    else -> "About"
                },
                subtitle = when (currentLanguage) {
                    "en" -> "App version and information"
                    "ta" -> "பயன்பாட்டு பதிப்பு மற்றும் தகவல்"
                    "si" -> "යෙදුම් අනුවාදය සහ තොරතුරු"
                    else -> "App version and information"
                },
                onClick = { /* TODO: Navigate to about screen */ }
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
