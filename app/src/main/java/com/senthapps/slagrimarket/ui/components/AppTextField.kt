package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.CornerRadius
import com.senthapps.slagrimarket.ui.theme.Green600
import com.senthapps.slagrimarket.ui.theme.ErrorRed

/**
 * Standard text field with Material Design 3 styling
 * Uses 16dp corner radius and proper label/hint/error states
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        supportingText = {
            when {
                isError && errorMessage != null -> Text(errorMessage, color = ErrorRed)
                supportingText != null -> Text(supportingText)
            }
        },
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onSearch = { onImeAction() },
            onGo = { onImeAction() }
        ),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green600,
            focusedLabelColor = Green600,
            cursorColor = Green600,
            errorBorderColor = ErrorRed,
            errorLabelColor = ErrorRed,
            errorCursorColor = ErrorRed
        )
    )
}

/**
 * Filled text field variant with Material Design 3 styling
 */
@Composable
fun FilledAppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        supportingText = {
            when {
                isError && errorMessage != null -> Text(errorMessage, color = ErrorRed)
                supportingText != null -> Text(supportingText)
            }
        },
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onSearch = { onImeAction() },
            onGo = { onImeAction() }
        ),
        shape = RoundedCornerShape(topStart = CornerRadius.Large, topEnd = CornerRadius.Large),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Green600,
            focusedLabelColor = Green600,
            cursorColor = Green600,
            errorIndicatorColor = ErrorRed,
            errorLabelColor = ErrorRed,
            errorCursorColor = ErrorRed
        )
    )
}

/**
 * Search text field with search icon and clear button
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green600,
            focusedLabelColor = Green600,
            cursorColor = Green600
        )
    )
}

