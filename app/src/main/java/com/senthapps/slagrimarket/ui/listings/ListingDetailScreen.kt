package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.util.CropImageProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    onNavigateBack: () -> Unit,
    onPlaceOrder: (String) -> Unit,
    onContactFarmer: (String) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)

    LaunchedEffect(listingId) {
        viewModel.loadListing(listingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Listing Details"
                            "ta" -> "பட்டியல் விவரங்கள்"
                            "si" -> "ලැයිස්තු විස්තර"
                            else -> "Listing Details"
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
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* TODO: Share listing */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.listing != null && currentUser?.userType == UserType.BUYER) {
                BottomActionBar(
                    listing = uiState.listing!!,
                    onPlaceOrder = { onPlaceOrder(listingId) },
                    onContactFarmer = { onContactFarmer(uiState.listing!!.farmerId) },
                    currentLanguage = currentLanguage
                )
            }
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
                    onRetry = { viewModel.loadListing(listingId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.listing != null -> {
                ListingDetailContent(
                    listing = uiState.listing!!,
                    currentLanguage = currentLanguage,
                    currentUserType = currentUser?.userType,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ListingDetailContent(
    listing: Listing,
    currentLanguage: String,
    currentUserType: UserType?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image gallery with farmer photos or generic crop image
        ListingImageGallery(
            cropType = listing.cropType,
            farmerImages = listing.images,
            currentLanguage = currentLanguage
        )

        // Main info card
        MainInfoCard(
            listing = listing,
            currentLanguage = currentLanguage
        )

        // Farmer info card
        FarmerInfoCard(
            farmerId = listing.farmerId,
            farmerName = listing.farmerName ?: "Unknown Farmer",
            currentLanguage = currentLanguage
        )

        // Details card
        DetailsCard(
            listing = listing,
            currentLanguage = currentLanguage
        )

        // Location card
        LocationCard(
            location = listing.location,
            pickupLocations = listing.pickupLocations,
            currentLanguage = currentLanguage
        )

        // Description if available
        if (!listing.description.isNullOrBlank()) {
            DescriptionCard(
                description = listing.description,
                currentLanguage = currentLanguage
            )
        }

        // Spacer for bottom bar
        if (currentUserType == UserType.BUYER) {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Image gallery component that displays farmer-uploaded photos or falls back to generic crop images
 */
@Composable
private fun ListingImageGallery(
    cropType: String,
    farmerImages: List<String>,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val hasFarmerPhotos = CropImageProvider.hasFarmerPhotos(farmerImages)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Image display
        if (hasFarmerPhotos) {
            // Show farmer-uploaded photos in a horizontal scrollable gallery
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(farmerImages) { imageUrl ->
                    FarmerPhotoCard(imageUrl = imageUrl)
                }
            }
        } else {
            // Show generic crop image as fallback
            GenericCropImageCard(cropType = cropType)
        }

        // Image type indicator badge
        ImageTypeBadge(
            hasFarmerPhotos = hasFarmerPhotos,
            currentLanguage = currentLanguage
        )
    }
}

/**
 * Card displaying a farmer-uploaded photo
 */
@Composable
private fun FarmerPhotoCard(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .height(240.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Farmer's produce photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.crop_generic)
        )
    }
}

/**
 * Card displaying generic crop image
 */
@Composable
private fun GenericCropImageCard(
    cropType: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Display generic crop image from drawable resources
            AsyncImage(
                model = CropImageProvider.getGenericCropImage(cropType),
                contentDescription = "Generic ${cropType.replace("_", " ")} image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay with crop emoji for visual appeal
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = CropTypes.getCropEmoji(cropType),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }
    }
}

/**
 * Badge indicating whether showing generic stock image or actual farmer photos
 */
@Composable
private fun ImageTypeBadge(
    hasFarmerPhotos: Boolean,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val (badgeText, badgeColor) = if (hasFarmerPhotos) {
        when (currentLanguage) {
            "ta" -> "விவசாயியின் புகைப்படங்கள்" to MaterialTheme.colorScheme.primary
            "si" -> "ගොවියාගේ ඡායාරූප" to MaterialTheme.colorScheme.primary
            else -> "Farmer's Photos" to MaterialTheme.colorScheme.primary
        }
    } else {
        when (currentLanguage) {
            "ta" -> "பொதுவான படம்" to MaterialTheme.colorScheme.secondary
            "si" -> "සාමාන්‍ය රූපය" to MaterialTheme.colorScheme.secondary
            else -> "Stock Image" to MaterialTheme.colorScheme.secondary
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = badgeColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasFarmerPhotos) Icons.Default.Check else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = badgeColor
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = badgeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MainInfoCard(
    listing: Listing,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Crop name
            Text(
                text = CropTypes.getCropName(listing.cropType, currentLanguage),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LKR ${String.format("%.2f", listing.pricePerUnit)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "per ${listing.unit}"
                            "ta" -> "ஒரு ${listing.unit}"
                            "si" -> "එක් ${listing.unit}"
                            else -> "per ${listing.unit}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quality badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = listing.quality.getColor()
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Grade ${listing.quality.name}"
                            "ta" -> "தரம் ${listing.quality.name}"
                            "si" -> "ශ්‍රේණිය ${listing.quality.name}"
                            else -> "Grade ${listing.quality.name}"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Divider()

            // Quantity available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Available Quantity"
                        "ta" -> "கிடைக்கும் அளவு"
                        "si" -> "ලබා ගත හැකි ප්‍රමාණය"
                        else -> "Available Quantity"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${listing.quantity} ${listing.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Status
            if (listing.isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Available Now"
                            "ta" -> "இப்போது கிடைக்கும்"
                            "si" -> "දැන් ලබා ගත හැකිය"
                            else -> "Available Now"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmerInfoCard(
    farmerId: String,
    farmerName: String,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Farmer avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Sold by"
                        "ta" -> "விற்பனையாளர்"
                        "si" -> "විකුණන්නේ"
                        else -> "Sold by"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = farmerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "4.5",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "(89 reviews)",
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

@Composable
private fun DetailsCard(
    listing: Listing,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Details"
                    "ta" -> "விவரங்கள்"
                    "si" -> "විස්තර"
                    else -> "Details"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DetailRow(
                icon = Icons.Default.DateRange,
                label = when (currentLanguage) {
                    "en" -> "Harvest Date"
                    "ta" -> "அறுவடை தேதி"
                    "si" -> "අස්වනු නෙලීමේ දිනය"
                    else -> "Harvest Date"
                },
                value = formatDate(listing.harvestDate)
            )

            DetailRow(
                icon = Icons.Default.Info,
                label = when (currentLanguage) {
                    "en" -> "Crop Type"
                    "ta" -> "பயிர் வகை"
                    "si" -> "බෝග වර්ගය"
                    else -> "Crop Type"
                },
                value = CropTypes.getCropName(listing.cropType, currentLanguage)
            )

            DetailRow(
                icon = Icons.Default.DateRange,
                label = when (currentLanguage) {
                    "en" -> "Listed On"
                    "ta" -> "பட்டியலிடப்பட்ட தேதி"
                    "si" -> "ලැයිස්තුගත කළ දිනය"
                    else -> "Listed On"
                },
                value = formatDate(listing.createdAt)
            )
        }
    }
}

@Composable
private fun LocationCard(
    location: String,
    pickupLocations: List<String>,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Location"
                    "ta" -> "இடம்"
                    "si" -> "ස්ථානය"
                    else -> "Location"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (pickupLocations.isNotEmpty()) {
                Divider()
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Pickup Locations"
                        "ta" -> "எடுத்துச் செல்லும் இடங்கள்"
                        "si" -> "ලබා ගන්නා ස්ථාන"
                        else -> "Pickup Locations"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pickupLocations.forEach { pickupLocation ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = pickupLocation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Description"
                    "ta" -> "விளக்கம்"
                    "si" -> "විස්තරය"
                    else -> "Description"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    listing: Listing,
    onPlaceOrder: () -> Unit,
    onContactFarmer: () -> Unit,
    currentLanguage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onContactFarmer,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Contact"
                        "ta" -> "தொடர்பு"
                        "si" -> "සම්බන්ධ"
                        else -> "Contact"
                    }
                )
            }

            Button(
                onClick = onPlaceOrder,
                modifier = Modifier.weight(1f),
                enabled = listing.isActive && listing.quantity > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Place Order"
                        "ta" -> "ஆர்டர் செய்"
                        "si" -> "ඇණවුම් කරන්න"
                        else -> "Place Order"
                    }
                )
            }
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
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        dateString
    }
}
