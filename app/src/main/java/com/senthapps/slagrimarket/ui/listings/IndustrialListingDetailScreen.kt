package com.senthapps.slagrimarket.ui.listings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL LISTING DETAIL SCREEN v2.0
// "The Market Stall Detail" - Full details of a listing
// Sections separated by 4dp Earth dividers
// ============================================================================

/**
 * Data class for listing detail
 */
data class ListingDetail(
    val id: String,
    val productName: String,
    val price: Double,
    val unit: String,
    val categoryName: String,
    val availableFrom: String? = null,
    val availableUntil: String? = null,
    val pickupLocation: String,
    val sellerName: String,
    val sellerDistrict: String,
    val sellerPhone: String,
    val postedTime: String
)

/**
 * Industrial listing detail screen - full details of a listing
 *
 * Header: 72dp Rice with back text and category
 * Sections: Product, Availability, Seller, Actions
 * Each section separated by 4dp Earth divider
 *
 * @param listing Listing detail data
 * @param language Current language for labels
 * @param onNavigateBack Callback to navigate back
 * @param onCallSeller Callback to call seller (or null to use default phone dialer)
 * @param onSendMessage Callback to send message
 * @param isLoading Loading state
 * @param isError Error state
 * @param onRetry Retry callback
 */
@Composable
fun IndustrialListingDetailScreen(
    listing: ListingDetail?,
    language: AppLanguage = AppLanguage.SINHALA,
    onNavigateBack: () -> Unit,
    onCallSeller: (() -> Unit)? = null,
    onSendMessage: () -> Unit = {},
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header
        DetailHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            categoryName = listing?.categoryName ?: "",
            onBackClick = onNavigateBack
        )

        // Earth divider
        EarthDivider()

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "රැඳී සිටින්න"
                            AppLanguage.TAMIL -> "காத்திருங்கள்"
                            AppLanguage.ENGLISH -> "WAIT"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            isError || listing == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.SINHALA -> "සම්බන්ධ වීමට නොහැකි විය"
                                AppLanguage.TAMIL -> "இணைக்க முடியவில்லை"
                                AppLanguage.ENGLISH -> "COULD NOT CONNECT"
                            },
                            style = HumanIndustrialType.body,
                            color = HumanIndustrial.Stone
                        )
                        SecondaryButton(
                            text = when (language) {
                                AppLanguage.SINHALA -> "නැවත උත්සාහ කරන්න"
                                AppLanguage.TAMIL -> "மீண்டும் முயற்சிக்கவும்"
                                AppLanguage.ENGLISH -> "TRY AGAIN"
                            },
                            onClick = onRetry
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Product section
                    ProductSection(listing = listing)

                    EarthDivider()

                    // Availability section
                    AvailabilitySection(
                        listing = listing,
                        language = language
                    )

                    EarthDivider()

                    // Seller section
                    SellerSection(
                        listing = listing,
                        language = language
                    )

                    EarthDivider()

                    // Action buttons section
                    ActionButtonsSection(
                        language = language,
                        phoneAvailable = listing.sellerPhone.isNotEmpty(),
                        onCallSeller = onCallSeller ?: {
                            // Default: open phone dialer
                            if (listing.sellerPhone.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${listing.sellerPhone}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onSendMessage = onSendMessage
                    )
                }
            }
        }
    }
}

/**
 * Header component - 72dp Rice background
 */
@Composable
private fun DetailHeader(
    backText: String,
    categoryName: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(HumanIndustrial.Rice)
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(48.dp)
                .industrialClickable(onClick = onBackClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = backText,
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Earth
            )
        }

        Text(
            text = categoryName,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * 4dp Earth divider
 */
@Composable
private fun EarthDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BorderWidth.Thick)
            .background(HumanIndustrial.Earth)
    )
}

/**
 * Product section - name, price, unit
 */
@Composable
private fun ProductSection(listing: ListingDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
    ) {
        Text(
            text = listing.productName,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink
        )
        Text(
            text = "රු ${listing.price.toInt()}",
            style = HumanIndustrialType.price,
            color = HumanIndustrial.Gold
        )
        Text(
            text = "1 ${listing.unit}",
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Ink
        )
    }
}

/**
 * Availability section - available dates and pickup location
 */
@Composable
private fun AvailabilitySection(
    listing: ListingDetail,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Available dates
        if (listing.availableFrom != null || listing.availableUntil != null) {
            LabelValuePair(
                label = when (language) {
                    AppLanguage.SINHALA -> "ලබා ගත හැකි කාලය"
                    AppLanguage.TAMIL -> "கிடைக்கும் நேரம்"
                    AppLanguage.ENGLISH -> "AVAILABLE"
                },
                value = buildString {
                    listing.availableFrom?.let { append(it) }
                    if (listing.availableFrom != null && listing.availableUntil != null) {
                        append(" - ")
                    }
                    listing.availableUntil?.let { append(it) }
                }
            )
        }

        // Pickup location
        LabelValuePair(
            label = when (language) {
                AppLanguage.SINHALA -> "රැගෙන යන ස්ථානය"
                AppLanguage.TAMIL -> "எடுக்கும் இடம்"
                AppLanguage.ENGLISH -> "PICKUP"
            },
            value = listing.pickupLocation
        )
    }
}

/**
 * Seller section - name, district, posted time
 */
@Composable
private fun SellerSection(
    listing: ListingDetail,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Section label
        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "විකුණන්නා"
                AppLanguage.TAMIL -> "விற்பவர்"
                AppLanguage.ENGLISH -> "SELLER"
            },
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )

        // Seller name - with fallback for empty name
        Text(
            text = listing.sellerName.ifEmpty {
                when (language) {
                    AppLanguage.SINHALA -> "නම නොදනී"
                    AppLanguage.TAMIL -> "பெயர் தெரியாது"
                    AppLanguage.ENGLISH -> "NAME UNAVAILABLE"
                }
            },
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )

        // District
        Text(
            text = listing.sellerDistrict,
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Ink
        )

        // Posted time
        Text(
            text = listing.postedTime,
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Stone
        )
    }
}

/**
 * Action buttons section - Call and Send Message
 */
@Composable
private fun ActionButtonsSection(
    language: AppLanguage,
    phoneAvailable: Boolean,
    onCallSeller: () -> Unit,
    onSendMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Primary: Call - disabled if phone not available
        if (phoneAvailable) {
            PrimaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "ඇමතුමක් ගන්න"
                    AppLanguage.TAMIL -> "அழைக்கவும்"
                    AppLanguage.ENGLISH -> "CALL"
                },
                onClick = onCallSeller,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Show unavailable message when phone is missing
            SecondaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "දුරකතන අංකය නොමැත"
                    AppLanguage.TAMIL -> "தொலைபேசி இல்லை"
                    AppLanguage.ENGLISH -> "PHONE UNAVAILABLE"
                },
                onClick = { /* No action - informational only */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Secondary: Send Message
        SecondaryButton(
            text = when (language) {
                AppLanguage.SINHALA -> "පණිවිඩයක් යවන්න"
                AppLanguage.TAMIL -> "செய்தி அனுப்பவும்"
                AppLanguage.ENGLISH -> "SEND MESSAGE"
            },
            onClick = onSendMessage,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Label-value pair component
 */
@Composable
private fun LabelValuePair(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
    ) {
        Text(
            text = label,
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )
        Text(
            text = value,
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )
    }
}

// ============================================================================
// BACKWARD COMPATIBILITY - Keep old function signature
// ============================================================================

/**
 * Legacy function signature for backward compatibility
 */
@Composable
fun IndustrialListingDetailScreen(
    productName: String,
    price: Double,
    unit: String,
    quantity: Double,
    location: String,
    sellerName: String,
    imageUrl: String? = null,
    isOwnListing: Boolean = false,
    onNavigateBack: () -> Unit,
    onCallSeller: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val listing = ListingDetail(
        id = "",
        productName = productName,
        price = price,
        unit = unit,
        categoryName = "",
        pickupLocation = location,
        sellerName = sellerName,
        sellerDistrict = location,
        sellerPhone = "",
        postedTime = ""
    )

    IndustrialListingDetailScreen(
        listing = listing,
        language = AppLanguage.ENGLISH,
        onNavigateBack = onNavigateBack,
        onCallSeller = onCallSeller,
        onSendMessage = {}
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingDetailPreview() {
    val sampleListing = ListingDetail(
        id = "1",
        productName = "තක්කාලි",
        price = 280.0,
        unit = "kg",
        categoryName = "එළවළු",
        availableFrom = "ජනවාරි 15",
        availableUntil = "ජනවාරි 20",
        pickupLocation = "නුවර පොළ අසල",
        sellerName = "සුනිල් මහතා",
        sellerDistrict = "නුවර",
        sellerPhone = "+94771234567",
        postedTime = "පැය 3කට පෙර"
    )

    IndustrialListingDetailScreen(
        listing = sampleListing,
        language = AppLanguage.SINHALA,
        onNavigateBack = {},
        onSendMessage = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingDetailEnglishPreview() {
    val sampleListing = ListingDetail(
        id = "1",
        productName = "Tomatoes",
        price = 280.0,
        unit = "kg",
        categoryName = "VEGETABLES",
        availableFrom = "January 15",
        availableUntil = "January 20",
        pickupLocation = "Near Kandy Market",
        sellerName = "Mr. Sunil",
        sellerDistrict = "Kandy",
        sellerPhone = "+94771234567",
        postedTime = "3 hours ago"
    )

    IndustrialListingDetailScreen(
        listing = sampleListing,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        onSendMessage = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingDetailLoadingPreview() {
    IndustrialListingDetailScreen(
        listing = null,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        isLoading = true
    )
}
