package com.senthapps.slagrimarket.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun LanguageToggleButton(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Language toggle button
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
                text = LanguagePreferences.LANGUAGE_CODES[currentLanguage] ?: "EN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.wrapContentSize()
        ) {
            LanguagePreferences.SUPPORTED_LANGUAGES.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = LanguagePreferences.LANGUAGE_CODES[language] ?: language.uppercase(),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = LanguagePreferences.LANGUAGE_NAMES[language] ?: language,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
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
fun LanguageToggleViewModel(
    viewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    
    LanguageToggleButton(
        currentLanguage = currentLanguage,
        onLanguageChange = viewModel::setLanguage
    )
}

// ViewModel for Language Toggle

@HiltViewModel
class LanguageToggleViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {
    
    val currentLanguage: StateFlow<String> = languagePreferences.selectedLanguage
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = LanguagePreferences.TAMIL
        )
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            languagePreferences.setLanguage(language)
        }
    }
}
