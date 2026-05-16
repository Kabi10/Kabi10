package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.LocalAppLanguage
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialFormat

// ============================================================================
// INDUSTRIAL BOTTOM SHEET COMPONENT
// Hard-edged confirmation dialog with vertical button stack
// ============================================================================

/**
 * Confirmation bottom sheet for destructive actions
 * Vertical button stack to prevent mis-taps with thick fingers
 *
 * @param title The confirmation question (e.g., "DELETE LISTING?")
 * @param confirmText Text for confirm button (e.g., "DELETE")
 * @param cancelText Text for cancel button (e.g., "CANCEL")
 * @param onConfirm Callback when user confirms
 * @param onCancel Callback when user cancels or dismisses
 * @param isDangerous If true, uses DangerButton for confirm; otherwise PrimaryButton
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialConfirmationBottomSheet(
    title: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isDangerous: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = AgrimarketWhite,
        contentColor = AgrimarketBlack,
        tonalElevation = 0.dp,
        scrimColor = AgrimarketBlack.copy(alpha = 0.6f),
        dragHandle = null, // No rounded drag handle
        modifier = modifier.border(
            width = BorderWidth.Standard,
            color = AgrimarketBlack,
            shape = RectangleShape
        )
    ) {
        val language = LocalAppLanguage.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AgrimarketWhite)
                .padding(Spacing.Double),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Base)
        ) {
            // Title text
            Text(
                text = title.industrialFormat(language),
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(bottom = Spacing.Base)
            )

            // Confirm button (stacked on top)
            if (isDangerous) {
                DangerButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Cancel button (below confirm)
            SecondaryButton(
                text = cancelText,
                onClick = {
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
