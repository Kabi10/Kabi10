package com.senthapps.slagrimarket.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    val currentUser: Flow<User?> = authRepository.currentUser

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = transactionRepository.getTransactionById(transactionId)) {
                    is com.senthapps.slagrimarket.data.repository.Resource.Success -> {
                        val transaction = result.data!!
                        val user = authRepository.getCurrentUser()
                        val canUpdate = canUpdateStatus(transaction, user?.userType)
                        val nextStatus = if (canUpdate) getNextStatusForUser(transaction, user?.userType) else null
                        val actionText = if (canUpdate) getStatusActionText(transaction, user?.userType) else null

                        _uiState.update {
                            it.copy(
                                transaction = transaction,
                                isLoading = false,
                                canUpdateStatus = canUpdate,
                                nextStatus = nextStatus,
                                actionText = actionText
                            )
                        }
                    }
                    is com.senthapps.slagrimarket.data.repository.Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load transaction"
                            )
                        }
                    }
                    is com.senthapps.slagrimarket.data.repository.Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transaction")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load transaction: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateTransactionStatus(transactionId: String, newStatus: TransactionStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            transactionRepository.updateTransactionStatus(transactionId, newStatus).fold(
                onSuccess = { updatedTransaction ->
                    Timber.d("Transaction status updated successfully")
                    // Reload to get fresh data
                    loadTransaction(transactionId)
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to update transaction status")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to update status: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun canUpdateStatus(transaction: Transaction, userType: UserType?): Boolean {
        if (userType == null) return false
        
        return when (transaction.status) {
            TransactionStatus.PENDING -> userType == UserType.FARMER
            TransactionStatus.CONFIRMED -> userType == UserType.FARMER
            TransactionStatus.IN_PROGRESS -> userType == UserType.BUYER
            TransactionStatus.COMPLETED, TransactionStatus.CANCELLED -> false
        }
    }

    private fun getNextStatusForUser(transaction: Transaction, userType: UserType?): TransactionStatus? {
        if (userType == null) return null

        return when (transaction.status) {
            TransactionStatus.PENDING -> if (userType == UserType.FARMER) TransactionStatus.CONFIRMED else null
            TransactionStatus.CONFIRMED -> if (userType == UserType.FARMER) TransactionStatus.IN_PROGRESS else null
            TransactionStatus.IN_PROGRESS -> if (userType == UserType.BUYER) TransactionStatus.COMPLETED else null
            else -> null
        }
    }

    private fun getStatusActionText(transaction: Transaction, userType: UserType?): String? {
        if (userType == null) return null

        return when (transaction.status) {
            TransactionStatus.PENDING -> if (userType == UserType.FARMER) "Confirm Order" else null
            TransactionStatus.CONFIRMED -> if (userType == UserType.FARMER) "Mark Ready" else null
            TransactionStatus.IN_PROGRESS -> if (userType == UserType.BUYER) "Complete Order" else null
            else -> null
        }
    }
}

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val canUpdateStatus: Boolean = false,
    val nextStatus: TransactionStatus? = null,
    val actionText: String? = null
)
