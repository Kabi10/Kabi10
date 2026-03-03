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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL CATEGORY SELECTION SCREEN v2.0
// "The Market Sections" - Vertical list with 6 categories
// 72dp header with back text, 96dp rows, alternating Rice/Dust backgrounds
// ============================================================================

/**
 * Category data class with multilingual support and emoji
 */
data class Category(
    val id: String,
    val emoji: String,
    val sinhala: String,
    val tamil: String,
    val english: String
)

/**
 * All 6 categories per UI plan spec with emojis
 */
val CATEGORIES = listOf(
    Category("VEGETABLES", "🥬", "එළවළු", "காய்கறிகள்", "VEGETABLES"),
    Category("COCONUT", "🥥", "පොල්", "தேங்காය்", "COCONUT"),
    Category("PADDY_AND_GRAIN", "🌾", "වී සහ ධාන්‍ය", "நெல் மற்றும் தானியங்கள்", "PADDY AND GRAIN"),
    Category("FRUIT", "🍎", "පලතුරු", "பழங்கள்", "FRUIT"),
    Category("FISH", "🐟", "මාළු", "மீන்", "FISH"),
    Category("LIVESTOCK", "🐄", "සතුන்", "கால்நடை", "LIVESTOCK")
)

/**
 * Flow type - determines header title
 */
enum class CategoryFlow {
    BUY,
    SELL
}

/**
 * Industrial category selection screen - vertical list of 6 categories
 *
 * Header: 72dp with back text and flow-specific title
 * Rows: 96dp each with alternating Rice/Dust backgrounds
 *
 * @param flow BUY or SELL - determines header title
 * @param language Current language for labels
 * @param onCategorySelected Callback when category is selected
 * @param onNavigateBack Callback to navigate back
 */
@Composable
fun IndustrialCategorySelectionScreen(
    flow: CategoryFlow = CategoryFlow.BUY,
    language: AppLanguage = AppLanguage.SINHALA,
    onCategorySelected: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp
        IndustrialHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (flow) {
                CategoryFlow.SELL -> when (language) {
                    AppLanguage.SINHALA -> "ඔබ විකුණන්නේ මොනවාද?"
                    AppLanguage.TAMIL -> "நீங்கள் என்ன விற்கிறீர்கள்?"
                    AppLanguage.ENGLISH -> "What are you selling?"
                }
                CategoryFlow.BUY -> when (language) {
                    AppLanguage.SINHALA -> "ඔබ හොයන්නේ මොනවාද?"
                    AppLanguage.TAMIL -> "நீங்கள் என்ன தேடுகிறீர்கள்?"
                    AppLanguage.ENGLISH -> "What are you looking for?"
                }
            },
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider below header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BorderWidth.Thick)
                .background(HumanIndustrial.Earth)
        )

        // Category rows
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(CATEGORIES) { index, category ->
                CategoryRow(
                    category = category,
                    language = language,
                    useAlternateBackground = index % 2 == 1,
                    onClick = { onCategorySelected(category.id) }
                )
            }
        }
    }
}

/**
 * Industrial header component
 * 72dp height, Rice background
 * Back text left-aligned, title centered
 */
@Composable
private fun IndustrialHeader(
    backText: String,
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(HumanIndustrial.Rice)
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        // Back text - left aligned with 48x48dp hit area
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

        // Title - centered
        Text(
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Category row - 96dp height
 * Text centered, alternating Rice/Dust backgrounds
 * Uses IndustrialIndication for pressed state
 */
@Composable
private fun CategoryRow(
    category: Category,
    language: AppLanguage,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice
    val categoryText = when (language) {
        AppLanguage.SINHALA -> category.sinhala
        AppLanguage.TAMIL -> category.tamil
        AppLanguage.ENGLISH -> category.english
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(backgroundColor)
            .industrialClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.emoji,
                style = HumanIndustrialType.productName, // 20sp Bold
                textAlign = TextAlign.Center
            )
            Text(
                text = categoryText,
                style = HumanIndustrialType.productName, // 20sp Bold
                color = HumanIndustrial.Ink,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategorySelectionBuyPreview() {
    IndustrialCategorySelectionScreen(
        flow = CategoryFlow.BUY,
        language = AppLanguage.SINHALA,
        onCategorySelected = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategorySelectionSellPreview() {
    IndustrialCategorySelectionScreen(
        flow = CategoryFlow.SELL,
        language = AppLanguage.SINHALA,
        onCategorySelected = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategorySelectionEnglishPreview() {
    IndustrialCategorySelectionScreen(
        flow = CategoryFlow.BUY,
        language = AppLanguage.ENGLISH,
        onCategorySelected = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategorySelectionTamilPreview() {
    IndustrialCategorySelectionScreen(
        flow = CategoryFlow.BUY,
        language = AppLanguage.TAMIL,
        onCategorySelected = {},
        onNavigateBack = {}
    )
}
