package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.CornerRadius
import com.senthapps.slagrimarket.ui.theme.Spacing

/**
 * Empty state component with icon, title, description, and optional action button
 * Follows Material Design 3 guidelines
 * Includes accessibility support for TalkBack
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    // Build accessibility description for TalkBack
    val stateDescription = "$title. $description"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.ExtraLarge)
            .semantics { contentDescription = stateDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with background - decorative, so contentDescription = null is correct
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(CornerRadius.ExtraLarge),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Decorative icon, meaning conveyed by title
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.Small))

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.Large)
        )

        // Action button
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.Large))
            PrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}

/**
 * Empty listings state with trilingual support
 */
@Composable
fun EmptyListingsState(
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onCreateListing: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.ShoppingCart,
        title = when (currentLanguage) {
            "en" -> "No Listings Available"
            "ta" -> "பட்டியல்கள் இல்லை"
            "si" -> "ලැයිස්තු නොමැත"
            else -> "No Listings Available"
        },
        description = when (currentLanguage) {
            "en" -> "New listings will appear here. Check back soon or create your own listing."
            "ta" -> "புதிய பட்டியல்கள் இங்கே தோன்றும். விரைவில் மீண்டும் சரிபார்க்கவும் அல்லது உங்கள் சொந்த பட்டியலை உருவாக்கவும்."
            "si" -> "නව ලැයිස්තු මෙහි දිස්වනු ඇත. ඉක්මනින් නැවත පරීක්ෂා කරන්න හෝ ඔබේම ලැයිස්තුවක් සාදන්න."
            else -> "New listings will appear here. Check back soon or create your own listing."
        },
        modifier = modifier,
        actionText = if (onCreateListing != null) {
            when (currentLanguage) {
                "en" -> "Create Listing"
                "ta" -> "பட்டியல் உருவாக்கு"
                "si" -> "ලැයිස්තුව සාදන්න"
                else -> "Create Listing"
            }
        } else null,
        onAction = onCreateListing
    )
}

/**
 * Empty transactions state with trilingual support
 */
@Composable
fun EmptyTransactionsState(
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onBrowseListings: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.List,
        title = when (currentLanguage) {
            "en" -> "No Transactions Yet"
            "ta" -> "இன்னும் பரிவர்த்தனைகள் இல்லை"
            "si" -> "තවම ගනුදෙනු නැත"
            else -> "No Transactions Yet"
        },
        description = when (currentLanguage) {
            "en" -> "Your transactions will appear here once you start buying or selling."
            "ta" -> "நீங்கள் வாங்க அல்லது விற்க தொடங்கியவுடன் உங்கள் பரிவர்த்தனைகள் இங்கே தோன்றும்."
            "si" -> "ඔබ මිලදී ගැනීම හෝ විකිණීම ආරම්භ කළ පසු ඔබේ ගනුදෙනු මෙහි දිස්වනු ඇත."
            else -> "Your transactions will appear here once you start buying or selling."
        },
        modifier = modifier,
        actionText = if (onBrowseListings != null) {
            when (currentLanguage) {
                "en" -> "Browse Listings"
                "ta" -> "பட்டியல்களை உலாவு"
                "si" -> "ලැයිස්තු බ්‍රවුස් කරන්න"
                else -> "Browse Listings"
            }
        } else null,
        onAction = onBrowseListings
    )
}

/**
 * Empty search results state with trilingual support
 */
@Composable
fun EmptySearchState(
    currentLanguage: String,
    searchQuery: String,
    modifier: Modifier = Modifier,
    onClearSearch: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Search,
        title = when (currentLanguage) {
            "en" -> "No Results Found"
            "ta" -> "முடிவுகள் இல்லை"
            "si" -> "ප්‍රතිඵල හමු නොවීය"
            else -> "No Results Found"
        },
        description = when (currentLanguage) {
            "en" -> "No results found for \"$searchQuery\". Try a different search term."
            "ta" -> "\"$searchQuery\" க்கு முடிவுகள் இல்லை. வேறு தேடல் சொல்லை முயற்சிக்கவும்."
            "si" -> "\"$searchQuery\" සඳහා ප්‍රතිඵල හමු නොවීය. වෙනත් සෙවුම් පදයක් උත්සාහ කරන්න."
            else -> "No results found for \"$searchQuery\". Try a different search term."
        },
        modifier = modifier,
        actionText = if (onClearSearch != null) {
            when (currentLanguage) {
                "en" -> "Clear Search"
                "ta" -> "தேடலை அழி"
                "si" -> "සෙවුම හිස් කරන්න"
                else -> "Clear Search"
            }
        } else null,
        onAction = onClearSearch
    )
}

