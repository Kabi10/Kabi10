package com.senthapps.slagrimarket.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageToggleViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(LanguagePreferences.DEFAULT_LANGUAGE)
    val currentLanguage: StateFlow<String> = _currentLanguage

    init {
        viewModelScope.launch {
            languagePreferences.getLanguage().collect { savedLanguage ->
                _currentLanguage.value = savedLanguage
            }
        }
    }

    fun setLanguage(language: String) {
        _currentLanguage.value = language
        viewModelScope.launch {
            languagePreferences.saveLanguage(language)
        }
    }
}
