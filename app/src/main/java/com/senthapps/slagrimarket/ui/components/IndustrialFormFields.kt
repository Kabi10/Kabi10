package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketRed
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// INDUSTRIAL FORM FIELD COMPONENTS
// Hard-bordered input fields with no Material styling
// ============================================================================

/**
 * Industrial text input field with hard borders
 * No Material TextField styling - pure BasicTextField with custom border
 */
@Composable
fun IndustrialFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportingText: String? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        // Label (if provided)
        if (label.isNotEmpty()) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Input field with hard border
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = BorderWidth.Standard,
                    color = if (errorMessage != null) AgrimarketRed else AgrimarketBlack,
                    shape = RectangleShape
                )
                .background(AgrimarketWhite)
                .padding(horizontal = Spacing.Base, vertical = Spacing.Base),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = AgrimarketBlack,
                letterSpacing = 0.sp
            ),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder.uppercase(),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = AgrimarketGray,
                            letterSpacing = 0.sp
                        )
                    )
                }
                innerTextField()
            }
        )

        // Error message (if present)
        errorMessage?.let {
            Text(
                text = it.uppercase(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AgrimarketRed,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Supporting text (if present and no error)
        if (errorMessage == null) {
            supportingText?.let {
                Text(
                    text = it.uppercase(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgrimarketGray,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Industrial dropdown field with hard borders
 * No Material styling - pure Box with custom border and dropdown
 */
@Composable
fun IndustrialFormDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label (if provided)
        if (label.isNotEmpty()) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Dropdown trigger
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = BorderWidth.Standard,
                        color = AgrimarketBlack,
                        shape = RectangleShape
                    )
                    .background(AgrimarketWhite)
                    .clickable(
                        onClick = { expanded = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(horizontal = Spacing.Base),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (selectedOption.isEmpty()) placeholder.uppercase() else selectedOption.uppercase(),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (selectedOption.isEmpty()) AgrimarketGray else AgrimarketBlack,
                        letterSpacing = 0.sp
                    )
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AgrimarketWhite)
                    .border(
                        width = BorderWidth.Standard,
                        color = AgrimarketBlack,
                        shape = RectangleShape
                    )
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.uppercase(),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AgrimarketBlack,
                                    letterSpacing = 0.sp
                                )
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier.background(AgrimarketWhite)
                    )
                }
            }
        }
    }
}
