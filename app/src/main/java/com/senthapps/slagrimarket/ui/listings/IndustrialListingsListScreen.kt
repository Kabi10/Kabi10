package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable
import java.time.Duration
import java.time.Instant

// ============================================================================
// HUMAN INDUSTRIAL LISTINGS LIST SCREEN v2.0
// "The Market Stalls" - Browse available listings in a category
// 72dp Rice header, 4dp Earth divider, 112dp rows with Earth accent bar
// ============================================================================

/**
 * Data class representing a listing in the list view
 */
data class ListingPreview(
    val id: String,
    val productName: String,
    val price: Double,
    val unit: String,
    val location: String,
    val createdAt: Instant = Instant.now(),
    val quantity: Int = 0,
    val farmerName: String = ""
)

/**
 * Industrial listings list screen - browse products in a category
 *
 * Header: 72dp Rice with back text left, category right
 * Divider: 4dp Earth
 * Rows: 112dp with Earth accent bar, alternating Rice/Dust backgrounds
 *
 * @param categoryName Display name for the category
 * @param categoryId Category ID for display
 * @param listings List of listings to display
 * @param language Current language for labels
 * @param onListingClick Callback when listing is clicked
 * @param onNavigateBack Callback to navigate back
 * @param isLoading Loading state
 * @param isError Error state
 * @param onRetry Retry callback for error state
 */
@Composable
fun IndustrialListingsListScreen(
    categoryName: String,
    categoryId: String = categoryName,
    listings: List<ListingPreview>,
    language: AppLanguage = AppLanguage.SINHALA,
    onListingClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp Rice background
        ListingsHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            categoryName = categoryName,
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BorderWidth.Thick)
                .background(HumanIndustrial.Earth)
        )

        // Content area
        when {
            isLoading -> {
                LoadingState(language = language)
            }
            isError -> {
                ErrorState(language = language, onRetry = onRetry)
            }
            listings.isEmpty() -> {
                EmptyState(language = language)
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(listings) { index, listing ->
                        ListingRow(
                            listing = listing,
                            language = language,
                            useAlternateBackground = index % 2 == 1,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header component - 72dp Rice background
 * Back text left-aligned (14sp Bold Earth), category right-aligned (24sp Bold Ink)
 */
@Composable
private fun ListingsHeader(
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
        // Back text - left aligned
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

        // Category name - right aligned
        Text(
            text = categoryName,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Listing row - 112dp height per UI plan
 *
 * Layout:
 * ▌ Product Name (20sp Bold Ink)
 * ▌ Price (28sp Bold Gold)    Unit (16sp Regular Ink)
 * ▌ Location (14sp Bold Stone)  Time (14sp Bold Stone)
 */
@Composable
private fun ListingRow(
    listing: ListingPreview,
    language: AppLanguage,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice
    val timeText = getRelativeTimeText(listing.createdAt, language)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(backgroundColor)
            .industrialClickable(onClick = onClick)
    ) {
        // 4dp Earth accent bar
        Box(
            modifier = Modifier
                .width(BorderWidth.Accent)
                .height(112.dp)
                .background(HumanIndustrial.Earth)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Spacing.lg.dp,
                    end = Spacing.lg.dp,
                    top = Spacing.md.dp,
                    bottom = Spacing.md.dp
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
        ) {
            // Line 1: Product name
            Text(
                text = listing.productName,
                style = HumanIndustrialType.productName,
                color = HumanIndustrial.Ink
            )

            // Line 2: Price + Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
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

            // Line 3: Location + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.location,
                    style = HumanIndustrialType.sectionLabel,
                    color = HumanIndustrial.Stone
                )
                Text(
                    text = timeText,
                    style = HumanIndustrialType.sectionLabel,
                    color = HumanIndustrial.Stone
                )
            }
        }
    }
}

/**
 * Loading state - centered text
 */
@Composable
private fun LoadingState(language: AppLanguage) {
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

/**
 * Error state - message + retry button
 */
@Composable
private fun ErrorState(language: AppLanguage, onRetry: () -> Unit) {
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
                color = HumanIndustrial.Stone,
                textAlign = TextAlign.Center
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

/**
 * Empty state - centered text
 */
@Composable
private fun EmptyState(language: AppLanguage) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "මෙහි තවම කිසිවක් නැත"
                AppLanguage.TAMIL -> "இங்கே இன்னும் எதுவும் இல்லை"
                AppLanguage.ENGLISH -> "NOTHING HERE YET"
            },
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Stone,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Get relative time text per UI plan spec
 */
private fun getRelativeTimeText(createdAt: Instant, language: AppLanguage): String {
    val now = Instant.now()
    val duration = Duration.between(createdAt, now)
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        hours < 1 -> when (language) {
            AppLanguage.SINHALA -> "දැන්"
            AppLanguage.TAMIL -> "இப்போது"
            AppLanguage.ENGLISH -> "JUST NOW"
        }
        hours in 1..6 -> when (language) {
            AppLanguage.SINHALA -> "පැය ${hours}කට පෙර"
            AppLanguage.TAMIL -> "$hours மணி நேரம் முன்"
            AppLanguage.ENGLISH -> "$hours HOURS AGO"
        }
        days < 1 -> when (language) {
            AppLanguage.SINHALA -> "අද"
            AppLanguage.TAMIL -> "இன்று"
            AppLanguage.ENGLISH -> "TODAY"
        }
        days == 1L -> when (language) {
            AppLanguage.SINHALA -> "ඊයේ"
            AppLanguage.TAMIL -> "நேற்று"
            AppLanguage.ENGLISH -> "YESTERDAY"
        }
        days in 2..6 -> when (language) {
            AppLanguage.SINHALA -> "දින ${days}කට පෙර"
            AppLanguage.TAMIL -> "$days நாட்கள் முன்"
            AppLanguage.ENGLISH -> "$days DAYS AGO"
        }
        else -> {
            val weeks = days / 7
            when (language) {
                AppLanguage.SINHALA -> "සතිය ${weeks}කට පෙර"
                AppLanguage.TAMIL -> "$weeks வாரங்கள் முன்"
                AppLanguage.ENGLISH -> "$weeks WEEKS AGO"
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingsListPreview() {
    val sampleListings = listOf(
        ListingPreview(
            id = "1",
            productName = "තක්කාලි",
            price = 280.0,
            unit = "kg",
            location = "අනුරාධපුර",
            createdAt = Instant.now()
        ),
        ListingPreview(
            id = "2",
            productName = "බෝංචි",
            price = 320.0,
            unit = "kg",
            location = "පොළොන්නරුව",
            createdAt = Instant.now().minus(Duration.ofDays(1))
        )
    )

    IndustrialListingsListScreen(
        categoryName = "එළවළු",
        listings = sampleListings,
        language = AppLanguage.SINHALA,
        onListingClick = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingsListEmptyPreview() {
    IndustrialListingsListScreen(
        categoryName = "එළවළු",
        listings = emptyList(),
        language = AppLanguage.SINHALA,
        onListingClick = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingsListLoadingPreview() {
    IndustrialListingsListScreen(
        categoryName = "VEGETABLES",
        listings = emptyList(),
        language = AppLanguage.ENGLISH,
        onListingClick = {},
        onNavigateBack = {},
        isLoading = true
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ListingsListErrorPreview() {
    IndustrialListingsListScreen(
        categoryName = "VEGETABLES",
        listings = emptyList(),
        language = AppLanguage.ENGLISH,
        onListingClick = {},
        onNavigateBack = {},
        isError = true
    )
}
