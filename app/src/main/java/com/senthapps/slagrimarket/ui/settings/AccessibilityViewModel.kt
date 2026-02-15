package com.senthapps.slagrimarket.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.preferences.AccessibilityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    private val accessibilityPreferences: AccessibilityPreferences
) : ViewModel() {

    val isLargeTextEnabled = accessibilityPreferences.isLargeTextEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val textScale = accessibilityPreferences.getTextScale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val isHighContrastEnabled = accessibilityPreferences.isHighContrastEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleLargeText(enabled: Boolean) {
        viewModelScope.launch {
            accessibilityPreferences.setLargeTextEnabled(enabled)
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            accessibilityPreferences.setHighContrastEnabled(enabled)
        }
    }
}
