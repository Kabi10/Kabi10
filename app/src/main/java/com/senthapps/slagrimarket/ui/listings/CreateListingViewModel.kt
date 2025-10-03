package com.senthapps.slagrimarket.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.QualityGrades
import com.senthapps.slagrimarket.data.model.Units
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateListingUiState())
    val uiState: StateFlow<CreateListingUiState> = _uiState.asStateFlow()
    
    fun updateCropType(cropType: String) {
        _uiState.value = _uiState.value.copy(
            cropType = cropType,
            cropTypeError = null
        )
    }

    fun updateQuantity(quantity: String) {
        _uiState.value = _uiState.value.copy(
            quantity = quantity,
            quantityError = null
        )
    }

    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(
            unit = unit,
            unitError = null
        )
    }

    fun updatePricePerUnit(price: String) {
        _uiState.value = _uiState.value.copy(
            pricePerUnit = price,
            priceError = null
        )
    }

    fun updateQuality(quality: String) {
        _uiState.value = _uiState.value.copy(
            quality = quality,
            qualityError = null
        )
    }

    fun updateHarvestDate(date: String) {
        _uiState.value = _uiState.value.copy(
            harvestDate = date,
            harvestDateError = null
        )
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(
            location = location,
            locationError = null
        )
    }

    fun setHarvestDateError(error: String) {
        _uiState.value = _uiState.value.copy(harvestDateError = error)
    }
    
    fun createListing() {
        // Clear previous errors
        _uiState.value = _uiState.value.copy(
            error = null,
            cropTypeError = null,
            quantityError = null,
            unitError = null,
            priceError = null,
            qualityError = null,
            harvestDateError = null,
            locationError = null
        )

        // Validate form
        if (!validateForm()) {
            return
        }

        val state = _uiState.value
        val quantityValue = state.quantity.toDoubleOrNull()!!
        val priceValue = state.pricePerUnit.toDoubleOrNull()!!

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }
                
                // Upload images first if any
                val imageUrls = if (state.images.isNotEmpty()) {
                    listingRepository.uploadImages("temp", state.images, context).getOrElse {
                        Timber.e(it, "Failed to upload images")
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                
                listingRepository.createListing(
                    cropType = state.cropType,
                    quantity = quantityValue!!,
                    unit = state.unit,
                    pricePerUnit = priceValue!!,
                    quality = state.quality,
                    harvestDate = state.harvestDate,
                    location = state.location,
                    farmerId = currentUser.id,
                    imageUrls = imageUrls
                ).fold(
                    onSuccess = { listing ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        Timber.d("Listing created successfully: ${listing.id} with ${imageUrls.size} images")
                    },
                    onFailure = { error ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create listing"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetForm() {
        _uiState.value = CreateListingUiState()
    }
    
    fun getAvailableCropTypes(): List<String> = CropTypes.ALL_CROPS
    fun getAvailableUnits(): List<String> = Units.ALL_UNITS
    fun getAvailableQualityGrades(): List<String> = QualityGrades.ALL_GRADES
    
    fun getTodayDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value

        // Check all required fields are filled
        if (state.cropType.isBlank()) return false
        if (state.quantity.isBlank()) return false
        if (state.unit.isBlank()) return false
        if (state.pricePerUnit.isBlank()) return false
        if (state.quality.isBlank()) return false
        if (state.harvestDate.isBlank()) return false
        if (state.location.isBlank()) return false

        // Check quantity is valid number > 0
        val quantityValue = state.quantity.toDoubleOrNull()
        if (quantityValue == null || quantityValue <= 0 || quantityValue > 100000) return false

        // Check price is valid number > 0
        val priceValue = state.pricePerUnit.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0 || priceValue > 1000000) return false

        // Check no errors are present
        if (state.cropTypeError != null) return false
        if (state.quantityError != null) return false
        if (state.unitError != null) return false
        if (state.priceError != null) return false
        if (state.qualityError != null) return false
        if (state.harvestDateError != null) return false
        if (state.locationError != null) return false

        return true
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        // Validate crop type
        if (state.cropType.isBlank()) {
            _uiState.value = _uiState.value.copy(cropTypeError = "Please select a crop type")
            isValid = false
        }

        // Validate quantity
        val quantityValue = state.quantity.toDoubleOrNull()
        when {
            state.quantity.isBlank() -> {
                _uiState.value = _uiState.value.copy(quantityError = "Quantity is required")
                isValid = false
            }
            quantityValue == null -> {
                _uiState.value = _uiState.value.copy(quantityError = "Please enter a valid number")
                isValid = false
            }
            quantityValue <= 0 -> {
                _uiState.value = _uiState.value.copy(quantityError = "Quantity must be greater than 0")
                isValid = false
            }
            quantityValue > 100000 -> {
                _uiState.value = _uiState.value.copy(quantityError = "Quantity cannot exceed 100,000")
                isValid = false
            }
        }

        // Validate unit
        if (state.unit.isBlank()) {
            _uiState.value = _uiState.value.copy(unitError = "Please select a unit")
            isValid = false
        }

        // Validate price
        val priceValue = state.pricePerUnit.toDoubleOrNull()
        when {
            state.pricePerUnit.isBlank() -> {
                _uiState.value = _uiState.value.copy(priceError = "Price is required")
                isValid = false
            }
            priceValue == null -> {
                _uiState.value = _uiState.value.copy(priceError = "Please enter a valid price")
                isValid = false
            }
            priceValue <= 0 -> {
                _uiState.value = _uiState.value.copy(priceError = "Price must be greater than 0")
                isValid = false
            }
            priceValue > 1000000 -> {
                _uiState.value = _uiState.value.copy(priceError = "Price cannot exceed 1,000,000")
                isValid = false
            }
        }

        // Validate quality
        if (state.quality.isBlank()) {
            _uiState.value = _uiState.value.copy(qualityError = "Please select a quality grade")
            isValid = false
        }

        // Validate harvest date
        if (state.harvestDate.isBlank()) {
            _uiState.value = _uiState.value.copy(harvestDateError = "Harvest date is required")
            isValid = false
        }

        // Validate location
        if (state.location.isBlank()) {
            _uiState.value = _uiState.value.copy(locationError = "Location is required")
            isValid = false
        }

        return isValid
    }

    fun updateImages(images: List<android.net.Uri>) {
        _uiState.value = _uiState.value.copy(images = images)
    }

    fun removeImage(imageUri: android.net.Uri) {
        val currentImages = _uiState.value.images.toMutableList()
        currentImages.remove(imageUri)
        _uiState.value = _uiState.value.copy(images = currentImages)
    }
}

data class CreateListingUiState(
    val cropType: String = "",
    val quantity: String = "",
    val unit: String = "",
    val pricePerUnit: String = "",
    val quality: String = "",
    val harvestDate: String = "",
    val location: String = "",
    val images: List<android.net.Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val cropTypeError: String? = null,
    val quantityError: String? = null,
    val unitError: String? = null,
    val priceError: String? = null,
    val qualityError: String? = null,
    val harvestDateError: String? = null,
    val locationError: String? = null
)
