package com.senthapps.slagrimarket.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
// INDUSTRIAL SETTINGS SCREEN (Screen J)
// Language Selection - One-time or occasional use
// 72dp header, 96dp rows, current selection in Green
// ============================================================================

/**
 * Industrial settings screen - language selection
 *
 * Header: 72dp Rice with back text and title
 * Rows: 96dp each, alternating Rice/Dust backgrounds
 * Current selection shown in Green
 *
 * @param currentLanguage Currently selected language
 * @param onLanguageSelected Callback when language is selected (should restart app)
 * @param onNavigateBack Callback to navigate back
 */
@Composable
fun IndustrialSettingsScreen(
    currentLanguage: AppLanguage = AppLanguage.SINHALA,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf<AppLanguage?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp
        SettingsHeader(
            backText = when (currentLanguage) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (currentLanguage) {
                AppLanguage.SINHALA -> "භාෂාව"
                AppLanguage.TAMIL -> "மொழி"
                AppLanguage.ENGLISH -> "LANGUAGE"
            },
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BorderWidth.Thick)
                .background(HumanIndustrial.Earth)
        )

        // Language options - always shown in native script
        LanguageRow(
            text = "සිංහල",
            language = AppLanguage.SINHALA,
            isSelected = currentLanguage == AppLanguage.SINHALA,
            useAlternateBackground = false,
            onClick = {
                if (currentLanguage != AppLanguage.SINHALA) {
                    pendingLanguage = AppLanguage.SINHALA
                    showConfirmDialog = true
                }
            }
        )

        LanguageRow(
            text = "தமிழ்",
            language = AppLanguage.TAMIL,
            isSelected = currentLanguage == AppLanguage.TAMIL,
            useAlternateBackground = true,
            onClick = {
                if (currentLanguage != AppLanguage.TAMIL) {
                    pendingLanguage = AppLanguage.TAMIL
                    showConfirmDialog = true
                }
            }
        )

        LanguageRow(
            text = "ENGLISH",
            language = AppLanguage.ENGLISH,
            isSelected = currentLanguage == AppLanguage.ENGLISH,
            useAlternateBackground = false,
            onClick = {
                if (currentLanguage != AppLanguage.ENGLISH) {
                    pendingLanguage = AppLanguage.ENGLISH
                    showConfirmDialog = true
                }
            }
        )
    }

    // Confirmation dialog
    if (showConfirmDialog && pendingLanguage != null) {
        LanguageChangeDialog(
            currentLanguage = currentLanguage,
            onConfirm = {
                pendingLanguage?.let { onLanguageSelected(it) }
                showConfirmDialog = false
                pendingLanguage = null
            },
            onDismiss = {
                showConfirmDialog = false
                pendingLanguage = null
            }
        )
    }
}

/**
 * Header component - 72dp Rice background
 */
@Composable
private fun SettingsHeader(
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
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Language row - 96dp height
 * Text centered, selected language shown in Green
 */
@Composable
private fun LanguageRow(
    text: String,
    language: AppLanguage,
    isSelected: Boolean,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice
    val textColor = if (isSelected) HumanIndustrial.Green else HumanIndustrial.Ink

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(backgroundColor)
            .industrialClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HumanIndustrialType.productName,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Language change confirmation dialog
 */
@Composable
private fun LanguageChangeDialog(
    currentLanguage: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HumanIndustrial.Rice,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.SINHALA -> "භාෂාව වෙනස් කරන්නද?"
                    AppLanguage.TAMIL -> "மொழியை மாற்றவா?"
                    AppLanguage.ENGLISH -> "CHANGE LANGUAGE?"
                },
                style = HumanIndustrialType.productName,
                color = HumanIndustrial.Ink
            )
        },
        confirmButton = {
            PrimaryButton(
                text = when (currentLanguage) {
                    AppLanguage.SINHALA -> "ඔව්"
                    AppLanguage.TAMIL -> "ஆம்"
                    AppLanguage.ENGLISH -> "YES"
                },
                onClick = onConfirm
            )
        },
        dismissButton = {
            SecondaryButton(
                text = when (currentLanguage) {
                    AppLanguage.SINHALA -> "නැත"
                    AppLanguage.TAMIL -> "இல்லை"
                    AppLanguage.ENGLISH -> "NO"
                },
                onClick = onDismiss
            )
        }
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenSinhalaPreview() {
    IndustrialSettingsScreen(
        currentLanguage = AppLanguage.SINHALA,
        onLanguageSelected = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenEnglishPreview() {
    IndustrialSettingsScreen(
        currentLanguage = AppLanguage.ENGLISH,
        onLanguageSelected = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenTamilPreview() {
    IndustrialSettingsScreen(
        currentLanguage = AppLanguage.TAMIL,
        onLanguageSelected = {},
        onNavigateBack = {}
    )
}
