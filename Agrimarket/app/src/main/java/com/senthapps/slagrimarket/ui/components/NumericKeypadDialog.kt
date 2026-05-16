package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// NUMERIC KEYPAD DIALOG
// Big-digit phone-style keypad — voice fallback for number entry
// Used for quantity and price fields when farmer can't speak clearly
// ============================================================================

/**
 * Full-screen numeric keypad dialog — large keys for thick fingers
 *
 * Layout:
 *   [Display: current value large]
 *   [7] [8] [9]
 *   [4] [5] [6]
 *   [1] [2] [3]
 *   [⌫] [0] [✓]
 *
 * @param title Label shown above the display (e.g. "Quantity (kg)" / "Price (Rs)")
 * @param initialValue Starting value string
 * @param maxDigits Maximum number of digits allowed (default 6)
 * @param onConfirm Called with the final numeric string when ✓ is pressed
 * @param onDismiss Called when dialog is dismissed
 * @param language Language code for ✓/⌫ labels
 */
@Composable
fun NumericKeypadDialog(
    title: String,
    initialValue: String = "",
    maxDigits: Int = 6,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    language: String = "ta"
) {
    var current by remember { mutableStateOf(initialValue) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HumanIndustrial.Rice)
        ) {
            // ─── Title bar ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF5C3317))
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = HumanIndustrialType.screenTitle,
                    color = HumanIndustrial.Rice
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BorderWidth.Thick)
                    .background(HumanIndustrial.Earth)
            )

            // ─── Display ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(HumanIndustrial.Dust)
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = current.ifEmpty { "0" },
                    style = HumanIndustrialType.price.copy(fontSize = 48.sp),
                    color = HumanIndustrial.Ink,
                    textAlign = TextAlign.End
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BorderWidth.Thick)
                    .background(HumanIndustrial.Earth)
            )

            // ─── Keypad rows ────────────────────────────────────────────
            val keys = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf("⌫", "0", "✓")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    row.forEachIndexed { index, key ->
                        val isConfirm = key == "✓"
                        val isBackspace = key == "⌫"
                        val bgColor = when {
                            isConfirm -> HumanIndustrial.Green
                            isBackspace -> HumanIndustrial.Dust
                            else -> HumanIndustrial.Rice
                        }
                        val textColor = when {
                            isConfirm -> HumanIndustrial.Rice
                            else -> HumanIndustrial.Ink
                        }

                        // Add vertical divider between columns (not before the first)
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .height(80.dp)
                                    .background(HumanIndustrial.Earth)
                                    .padding(horizontal = BorderWidth.Thick / 2)
                            )
                            Box(
                                modifier = Modifier
                                    .background(HumanIndustrial.Earth)
                                    .padding(start = BorderWidth.Thick)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .background(bgColor)
                                .industrialClickable(onClick = {
                                    when {
                                        isConfirm -> {
                                            onConfirm(current.ifEmpty { "0" })
                                        }
                                        isBackspace -> {
                                            if (current.isNotEmpty()) {
                                                current = current.dropLast(1)
                                            }
                                        }
                                        else -> {
                                            // Prevent leading zeros (unless the value is just "0")
                                            if (current == "0" && key != ".") {
                                                current = key
                                            } else if (current.length < maxDigits) {
                                                current += key
                                            }
                                        }
                                    }
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = HumanIndustrialType.screenTitle.copy(fontSize = 32.sp),
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Horizontal divider between rows
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BorderWidth.Thick)
                        .background(HumanIndustrial.Earth)
                )
            }

            // ─── Cancel link ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .industrialClickable(onClick = onDismiss)
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (language) {
                        "si" -> "අවලංගු කරන්න"
                        "ta" -> "ரத்து செய்"
                        else -> "CANCEL"
                    },
                    style = HumanIndustrialType.sectionLabel,
                    color = HumanIndustrial.Urgent
                )
            }
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun NumericKeypadDialogPreview() {
    NumericKeypadDialog(
        title = "Price (Rs per kg)",
        initialValue = "280",
        onConfirm = {},
        onDismiss = {},
        language = "en"
    )
}
