package com.senthapps.slagrimarket.ui.listings

import androidx.lifecycle.ViewModel
import com.senthapps.slagrimarket.data.preferences.LastUsedPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CreateListingSuccessViewModel @Inject constructor(
    lastUsedPreferences: LastUsedPreferences
) : ViewModel() {
    val cropType: StateFlow<String> = lastUsedPreferences.getLastCropType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val price: StateFlow<String> = lastUsedPreferences.getLastPrice()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val location: StateFlow<String> = lastUsedPreferences.getLastLocation()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
}
