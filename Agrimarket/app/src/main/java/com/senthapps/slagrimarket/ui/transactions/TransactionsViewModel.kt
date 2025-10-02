package com.senthapps.slagrimarket.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    
    val currentUser = authRepository.currentUser
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            transactionRepository.getTransactionsForUserFlow(user.id)
                .catch { error ->
                    Timber.e(error, "Error loading transactions")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load transactions"
                    )
                }
                .collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactions = transactions,
                        error = null
                    )
                }
        }
    }
    
    fun updateTransactionStatus(transactionId: String, newStatus: TransactionStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            transactionRepository.updateTransactionStatus(transactionId, newStatus).fold(
                onSuccess = { updatedTransaction ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Reload transactions to get updated data
                    loadTransactions()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update transaction"
                    )
                }
            )
        }
    }
    
    fun confirmTransaction(transactionId: String) {
        updateTransactionStatus(transactionId, TransactionStatus.CONFIRMED)
    }
    
    fun startTransaction(transactionId: String) {
        updateTransactionStatus(transactionId, TransactionStatus.IN_PROGRESS)
    }
    
    fun completeTransaction(transactionId: String) {
        updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)
    }
    
    fun cancelTransaction(transactionId: String) {
        updateTransactionStatus(transactionId, TransactionStatus.CANCELLED)
    }
    
    fun filterTransactionsByStatus(status: TransactionStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }
    
    fun getFilteredTransactions(): List<Transaction> {
        val allTransactions = _uiState.value.transactions
        val selectedStatus = _uiState.value.selectedStatus
        
        return if (selectedStatus != null) {
            allTransactions.filter { it.status == selectedStatus }
        } else {
            allTransactions
        }
    }
    
    fun refreshTransactions() {
        loadTransactions()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun canUpdateStatus(transaction: Transaction, userType: UserType): Boolean {
        return when (userType) {
            UserType.FARMER -> {
                // Farmers can confirm pending transactions and mark as completed
                when (transaction.status) {
                    TransactionStatus.PENDING -> true
                    TransactionStatus.IN_PROGRESS -> true
                    else -> false
                }
            }
            UserType.BUYER -> {
                // Buyers can start confirmed transactions
                when (transaction.status) {
                    TransactionStatus.CONFIRMED -> true
                    else -> false
                }
            }
        }
    }
    
    fun getNextStatusForUser(transaction: Transaction, userType: UserType): TransactionStatus? {
        return when (userType) {
            UserType.FARMER -> {
                when (transaction.status) {
                    TransactionStatus.PENDING -> TransactionStatus.CONFIRMED
                    TransactionStatus.IN_PROGRESS -> TransactionStatus.COMPLETED
                    else -> null
                }
            }
            UserType.BUYER -> {
                when (transaction.status) {
                    TransactionStatus.CONFIRMED -> TransactionStatus.IN_PROGRESS
                    else -> null
                }
            }
        }
    }
    
    fun getStatusActionText(transaction: Transaction, userType: UserType): String? {
        return when (userType) {
            UserType.FARMER -> {
                when (transaction.status) {
                    TransactionStatus.PENDING -> "Confirm Order"
                    TransactionStatus.IN_PROGRESS -> "Mark Complete"
                    else -> null
                }
            }
            UserType.BUYER -> {
                when (transaction.status) {
                    TransactionStatus.CONFIRMED -> "Start Pickup"
                    else -> null
                }
            }
        }
    }
}

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val selectedStatus: TransactionStatus? = null,
    val error: String? = null
)
