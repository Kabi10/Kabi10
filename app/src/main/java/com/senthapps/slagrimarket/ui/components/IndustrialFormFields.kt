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
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.LocalAppLanguage
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.TouchTargets
import com.senthapps.slagrimarket.ui.theme.industrialFormat

// ============================================================================
// HUMAN INDUSTRIAL FORM FIELD COMPONENTS v1.0
// "The Sturdy Inputs" - Earth borders, Rice background, clear labels
// ============================================================================

/**
 * Industrial text input field with Earth borders
 * Label: 14sp Bold UPPERCASE Stone
 * Input: 56dp height, 2dp Earth border, Rice background
 * Text: 18sp Regular Ink
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
    val language = LocalAppLanguage.current
    Column(modifier = modifier) {
        // Label (if provided) - 14sp Bold Stone (uppercase only for English)
        if (label.isNotEmpty()) {
            Text(
                text = label.industrialFormat(language),
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Stone,
                modifier = Modifier.padding(bottom = Spacing.sm.dp)
            )
        }

        // Input field with Earth border
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(TouchTargets.button.dp)
                .border(
                    width = BorderWidth.Standard,
                    color = if (errorMessage != null) HumanIndustrial.Urgent else HumanIndustrial.Earth,
                    shape = RectangleShape
                )
                .background(HumanIndustrial.Rice)
                .padding(horizontal = Spacing.md.dp, vertical = Spacing.md.dp),
            textStyle = HumanIndustrialType.input.copy(color = HumanIndustrial.Ink),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = HumanIndustrialType.input,
                        color = HumanIndustrial.Stone
                    )
                }
                innerTextField()
            }
        )

        // Error message (if present) - Urgent color
        errorMessage?.let {
            Text(
                text = it.industrialFormat(language),
                style = HumanIndustrialType.unit,
                color = HumanIndustrial.Urgent,
                modifier = Modifier.padding(top = Spacing.xs.dp)
            )
        }

        // Supporting text (if present and no error) - Stone color
        if (errorMessage == null) {
            supportingText?.let {
                Text(
                    text = it.industrialFormat(language),
                    style = HumanIndustrialType.unit,
                    color = HumanIndustrial.Stone,
                    modifier = Modifier.padding(top = Spacing.xs.dp)
                )
            }
        }
    }
}

/**
 * Industrial dropdown field with Earth borders
 * Label: 14sp Bold UPPERCASE Stone
 * Dropdown: 56dp height, 2dp Earth border, Rice background
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
    val language = LocalAppLanguage.current

    Column(modifier = modifier) {
        // Label (if provided) - 14sp Bold Stone (uppercase only for English)
        if (label.isNotEmpty()) {
            Text(
                text = label.industrialFormat(language),
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Stone,
                modifier = Modifier.padding(bottom = Spacing.sm.dp)
            )
        }

        // Dropdown trigger
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TouchTargets.button.dp)
                    .border(
                        width = BorderWidth.Standard,
                        color = HumanIndustrial.Earth,
                        shape = RectangleShape
                    )
                    .background(HumanIndustrial.Rice)
                    .clickable(
                        onClick = { expanded = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(horizontal = Spacing.md.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (selectedOption.isEmpty()) placeholder else selectedOption.industrialFormat(language),
                    style = HumanIndustrialType.input,
                    color = if (selectedOption.isEmpty()) HumanIndustrial.Stone else HumanIndustrial.Ink
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HumanIndustrial.Rice)
                    .border(
                        width = BorderWidth.Standard,
                        color = HumanIndustrial.Earth,
                        shape = RectangleShape
                    )
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.industrialFormat(language),
                                style = HumanIndustrialType.input,
                                color = HumanIndustrial.Ink
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier.background(HumanIndustrial.Rice)
                    )
                }
            }
        }
    }
}
