package com.senthapps.slagrimarket.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateTransactionUiState())
    val uiState: StateFlow<CreateTransactionUiState> = _uiState.asStateFlow()
    
    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                listingRepository.getListingById(listingId).collect { resource ->
                    when (resource) {
                        is com.senthapps.slagrimarket.data.repository.Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                listing = resource.data,
                                isLoading = false
                            )
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message ?: "Listing not found"
                            )
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading listing")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load listing"
                )
            }
        }
    }
    
    fun updateQuantity(quantity: String) {
        _uiState.value = _uiState.value.copy(quantity = quantity)
        calculateTotalAmount()
        validateForm()
    }
    
    fun updatePickupLocation(location: String) {
        _uiState.value = _uiState.value.copy(pickupLocation = location)
        validateForm()
    }
    
    fun updatePickupDate(date: String) {
        _uiState.value = _uiState.value.copy(pickupDate = date)
        validateForm()
    }
    
    fun updateBuyerContact(contact: String) {
        _uiState.value = _uiState.value.copy(buyerContact = contact)
        validateForm()
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    private fun calculateTotalAmount() {
        val state = _uiState.value
        val listing = state.listing
        val quantityValue = state.quantity.toDoubleOrNull()
        
        if (listing != null && quantityValue != null && quantityValue > 0) {
            val total = quantityValue * listing.pricePerUnit
            _uiState.value = _uiState.value.copy(totalAmount = total)
        } else {
            _uiState.value = _uiState.value.copy(totalAmount = 0.0)
        }
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val listing = state.listing
        val quantityValue = state.quantity.toDoubleOrNull()
        
        val isValid = listing != null &&
                quantityValue != null &&
                quantityValue > 0 &&
                quantityValue <= listing.quantity &&
                state.pickupLocation.isNotBlank() &&
                state.pickupDate.isNotBlank() &&
                state.buyerContact.isNotBlank() &&
                isValidDate(state.pickupDate) &&
                isValidPhoneNumber(state.buyerContact)
        
        _uiState.value = _uiState.value.copy(isFormValid = isValid)
    }
    
    private fun isValidDate(date: String): Boolean {
        // Simple date validation for YYYY-MM-DD format
        val regex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        return regex.matches(date)
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Simple phone validation - at least 10 digits
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return digitsOnly.length >= 10
    }
    
    fun createTransaction() {
        val state = _uiState.value
        val listing = state.listing

        if (!state.isFormValid || listing == null) {
            _uiState.value = _uiState.value.copy(
                error = context.getString(com.senthapps.slagrimarket.R.string.error_fill_required_fields)
            )
            return
        }

        val quantityValue = state.quantity.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = context.getString(com.senthapps.slagrimarket.R.string.error_user_not_authenticated)
                )
                return@launch
            }

            transactionRepository.createTransaction(
                listingId = listing.id,
                farmerId = listing.farmerId,
                buyerId = currentUser.id,
                quantity = quantityValue,
                totalAmount = state.totalAmount,
                pickupLocation = state.pickupLocation,
                pickupDate = state.pickupDate,
                buyerContact = state.buyerContact,
                notes = state.notes.takeIf { it.isNotBlank() }
            ).fold(
                onSuccess = { transaction ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        isTransactionCreated = true
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Error creating transaction")
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = error.message ?: "Failed to create transaction"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CreateTransactionUiState(
    val isLoading: Boolean = false,
    val listing: Listing? = null,
    val quantity: String = "",
    val pickupLocation: String = "",
    val pickupDate: String = "",
    val buyerContact: String = "",
    val notes: String = "",
    val totalAmount: Double = 0.0,
    val isFormValid: Boolean = false,
    val isCreating: Boolean = false,
    val isTransactionCreated: Boolean = false,
    val error: String? = null
)
