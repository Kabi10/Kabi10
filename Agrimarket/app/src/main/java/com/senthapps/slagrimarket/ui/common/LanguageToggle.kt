package com.senthapps.slagrimarket.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences

@Composable
fun LanguageToggleButton(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { showDropdown = true },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "E"
                    "ta" -> "த"
                    "si" -> "සි"
                    else -> "E"
                },
                fontSize = 16.sp
            )
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.wrapContentSize()
        ) {
            LanguagePreferences.SUPPORTED_LANGUAGES.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = language.uppercase(), fontSize = 14.sp) },
                    onClick = {
                        onLanguageChange(language)
                        showDropdown = false
                    },
                    leadingIcon = if (currentLanguage == language) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun LanguageToggleView(
    viewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    LanguageToggleButton(
        currentLanguage = currentLanguage,
        onLanguageChange = viewModel::setLanguage
    )
}
