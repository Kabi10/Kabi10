package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateTransactionRequest
import com.senthapps.slagrimarket.data.api.TransactionApiService
import com.senthapps.slagrimarket.data.api.UpdateTransactionStatusRequest
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OpType
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for transaction operations with enhanced offline-first architecture
 * Uses Room database as single source of truth and syncs with network in background
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionApiService: TransactionApiService,
    private val transactionDao: TransactionDao,
    private val localOpDao: LocalOpDao,
    private val moshi: Moshi
) {
    
    // ============================================================================
    // ENHANCED OFFLINE-FIRST DATA ACCESS
    // ============================================================================

    /**
     * Get transactions for user - offline-first with background refresh
     */
    fun getTransactionsForUser(userId: String, forceRefresh: Boolean = false): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())

        try {
            // Always emit cached data first
            val cachedTransactions = transactionDao.getTransactionsForUser(userId)
            if (cachedTransactions.isNotEmpty()) {
                emit(Resource.Success(cachedTransactions))
            }

            // Check if we need to refresh
            val shouldRefresh = forceRefresh || shouldRefreshTransactions(userId)

            if (shouldRefresh) {
                try {
                    val response = transactionApiService.getTransactions(
                        limit = 50
                    )

                    if (response.isSuccessful) {
                        val networkTransactions = response.body()?.transactions ?: emptyList()

                        // Update local database
                        transactionDao.insertTransactions(networkTransactions)

                        // Emit updated data
                        emit(Resource.Success(networkTransactions))
                    } else {
                        if (cachedTransactions.isEmpty()) {
                            emit(Resource.Error("Failed to load transactions", null))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh transactions from network")
                    if (cachedTransactions.isEmpty()) {
                        emit(Resource.Error("No internet connection", e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions")
            emit(Resource.Error("Failed to load transactions", e))
        }
    }

    /**
     * Get transactions for user with reactive Flow (original method preserved)
     */
    fun getTransactionsForUserFlow(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForUserFlow(userId)
    }

    /**
     * Get transactions by farmer - offline-first with background refresh
     */
    fun getTransactionsByFarmer(farmerId: String, forceRefresh: Boolean = false): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())

        try {
            val cachedTransactions = transactionDao.getTransactionsByFarmer(farmerId)
            if (cachedTransactions.isNotEmpty()) {
                emit(Resource.Success(cachedTransactions))
            }

            if (forceRefresh || shouldRefreshTransactions(farmerId)) {
                try {
                    val response = transactionApiService.getTransactions(
                        farmerId = farmerId,
                        limit = 50
                    )
                    if (response.isSuccessful) {
                        val networkTransactions = response.body()?.transactions ?: emptyList()
                        transactionDao.insertTransactions(networkTransactions)
                        emit(Resource.Success(networkTransactions))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh farmer transactions")
                    if (cachedTransactions.isEmpty()) {
                        emit(Resource.Error("Failed to load farmer transactions", e))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading farmer transactions", e))
        }
    }

    /**
     * Get transactions by farmer with reactive Flow (original method preserved)
     */
    fun getTransactionsByFarmerFlow(farmerId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByFarmerFlow(farmerId)
    }

    /**
     * Get transactions by buyer - offline-first with background refresh
     */
    fun getTransactionsByBuyer(buyerId: String, forceRefresh: Boolean = false): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())

        try {
            val cachedTransactions = transactionDao.getTransactionsByBuyer(buyerId)
            if (cachedTransactions.isNotEmpty()) {
                emit(Resource.Success(cachedTransactions))
            }

            if (forceRefresh || shouldRefreshTransactions(buyerId)) {
                try {
                    val response = transactionApiService.getTransactions(
                        buyerId = buyerId,
                        limit = 50
                    )
                    if (response.isSuccessful) {
                        val networkTransactions = response.body()?.transactions ?: emptyList()
                        transactionDao.insertTransactions(networkTransactions)
                        emit(Resource.Success(networkTransactions))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh buyer transactions")
                    if (cachedTransactions.isEmpty()) {
                        emit(Resource.Error("Failed to load buyer transactions", e))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading buyer transactions", e))
        }
    }

    /**
     * Get transactions by buyer with reactive Flow (original method preserved)
     */
    fun getTransactionsByBuyerFlow(buyerId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByBuyerFlow(buyerId)
    }

    /**
     * Get transaction by ID - offline-first
     */
    suspend fun getTransactionById(transactionId: String): Resource<Transaction> {
        return try {
            // Try local first
            val localTransaction = transactionDao.getTransactionById(transactionId)
            if (localTransaction != null) {
                Resource.Success(localTransaction)
            } else {
                // Try network
                val response = transactionApiService.getTransactionById(transactionId)
                if (response.isSuccessful) {
                    val networkTransaction = response.body()
                    if (networkTransaction != null) {
                        transactionDao.insertTransaction(networkTransaction)
                        Resource.Success(networkTransaction)
                    } else {
                        Resource.Error("Transaction not found", null)
                    }
                } else {
                    Resource.Error("Failed to load transaction", null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting transaction by ID")
            Resource.Error("Failed to load transaction", e)
        }
    }
    
    suspend fun createTransaction(
        listingId: String,
        farmerId: String,
        buyerId: String,
        quantity: Double,
        totalAmount: Double,
        pickupLocation: String,
        pickupDate: String,
        buyerContact: String? = null,
        notes: String? = null
    ): Result<Transaction> {
        return try {
            // Calculate price per unit
            val pricePerUnit = if (quantity > 0) totalAmount / quantity else 0.0

            // Create transaction object
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                listingId = listingId,
                farmerId = farmerId,
                buyerId = buyerId,
                quantity = quantity,
                totalAmount = totalAmount,
                pricePerUnit = pricePerUnit,
                pickupLocation = pickupLocation,
                pickupDate = pickupDate,
                status = TransactionStatus.PENDING,
                createdAt = Instant.now().toString()
            )
            
            // Save locally first (optimistic update)
            transactionDao.insertTransaction(transaction)
            
            // Create operation for sync
            val createRequest = CreateTransactionRequest(
                listingId = listingId,
                quantity = quantity,
                totalAmount = totalAmount,
                pricePerUnit = pricePerUnit,
                pickupDate = pickupDate,
                pickupLocation = pickupLocation,
                buyerContact = buyerContact,
                notes = notes
            )
            
            val op = LocalOp(
                opId = UUID.randomUUID().toString(),
                type = OpType.CREATE_TRANSACTION,
                payload = moshi.adapter(CreateTransactionRequest::class.java).toJson(createRequest),
                clientTs = Instant.now().toString()
            )
            
            localOpDao.insertOp(op)
            
            Result.success(transaction)
        } catch (e: Exception) {
            Timber.e(e, "Error creating transaction")
            Result.failure(e)
        }
    }
    
    suspend fun updateTransactionStatus(
        transactionId: String,
        newStatus: TransactionStatus,
        notes: String? = null
    ): Result<Transaction> {
        return try {
            val existingTransaction = transactionDao.getTransactionById(transactionId)
                ?: return Result.failure(Exception("Transaction not found"))
            
            // Update locally first (optimistic update)
            transactionDao.updateTransactionStatus(transactionId, newStatus)
            
            // Create operation for sync
            val updateRequest = UpdateTransactionStatusRequest(
                status = newStatus,
                notes = notes
            )
            
            val op = LocalOp(
                opId = UUID.randomUUID().toString(),
                type = OpType.UPDATE_TRANSACTION,
                payload = moshi.adapter(UpdateTransactionStatusRequest::class.java).toJson(updateRequest),
                clientTs = Instant.now().toString()
            )
            
            localOpDao.insertOp(op)
            
            val updatedTransaction = existingTransaction.copy(status = newStatus)
            Result.success(updatedTransaction)
        } catch (e: Exception) {
            Timber.e(e, "Error updating transaction status")
            Result.failure(e)
        }
    }
    
    suspend fun confirmTransaction(transactionId: String): Result<Transaction> {
        return updateTransactionStatus(transactionId, TransactionStatus.CONFIRMED)
    }
    
    suspend fun startTransaction(transactionId: String): Result<Transaction> {
        return updateTransactionStatus(transactionId, TransactionStatus.IN_PROGRESS)
    }
    
    suspend fun completeTransaction(transactionId: String): Result<Transaction> {
        return updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)
    }
    
    suspend fun cancelTransaction(transactionId: String, reason: String? = null): Result<Transaction> {
        return updateTransactionStatus(transactionId, TransactionStatus.CANCELLED, reason)
    }
    
    suspend fun syncTransactions(): Result<Unit> {
        return try {
            // In a real implementation, this would fetch transactions from the server
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing transactions")
            Result.failure(e)
        }
    }
    
    suspend fun getTransactionsByStatus(
        userId: String,
        status: TransactionStatus
    ): List<Transaction> {
        return transactionDao.getTransactionsForUser(userId, status)
    }
    
    suspend fun getPendingTransactionsForFarmer(farmerId: String): List<Transaction> {
        return transactionDao.getTransactionsForUser(farmerId, TransactionStatus.PENDING)
    }
    
    suspend fun getActiveTransactionsForBuyer(buyerId: String): List<Transaction> {
        return transactionDao.getTransactionsForUser(buyerId).filter { transaction ->
            transaction.status in listOf(
                TransactionStatus.PENDING,
                TransactionStatus.CONFIRMED,
                TransactionStatus.IN_PROGRESS
            )
        }
    }

    // ============================================================================
    // CACHE MANAGEMENT
    // ============================================================================

    /**
     * Check if transactions need refresh (older than 15 minutes)
     */
    private suspend fun shouldRefreshTransactions(userId: String): Boolean {
        // For now, always refresh if forced or if no cached data
        // TODO: Implement proper timestamp checking when DAO method is available
        return true
    }

    /**
     * Manual refresh of transactions
     */
    suspend fun refreshTransactions(userId: String): Resource<Unit> {
        return try {
            val response = transactionApiService.getTransactions(
                limit = 50
            )

            if (response.isSuccessful) {
                val transactions = response.body()?.transactions ?: emptyList()
                transactionDao.insertTransactions(transactions)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to refresh transactions", null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing transactions")
            Resource.Error("Network error", e)
        }
    }

    /**
     * Clear old transactions
     */
    suspend fun clearOldTransactions(userId: String, daysToKeep: Int = 90) {
        try {
            // TODO: Implement when DAO method is available
            Timber.d("Clear old transactions requested for user: $userId, days: $daysToKeep")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing old transaction data")
        }
    }

    /**
     * Get transactions by status with Resource wrapper
     */
    suspend fun getTransactionsByStatusResource(
        userId: String,
        status: TransactionStatus
    ): Resource<List<Transaction>> {
        return try {
            val transactions = transactionDao.getTransactionsForUser(userId, status)
            Resource.Success(transactions)
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions by status")
            Resource.Error("Failed to load transactions", e)
        }
    }

    /**
     * Get transaction statistics with Resource wrapper
     */
    suspend fun getTransactionStatisticsResource(userId: String): Resource<Map<String, Any>> {
        return try {
            // TODO: Implement when DAO method is available
            val stats = emptyMap<String, Any>()
            Resource.Success(stats)
        } catch (e: Exception) {
            Timber.e(e, "Error getting transaction statistics")
            Resource.Error("Failed to load statistics", e)
        }
    }

    /**
     * Get recent transactions with Resource wrapper
     */
    suspend fun getRecentTransactionsResource(userId: String, limit: Int = 10): Resource<List<Transaction>> {
        return try {
            // TODO: Implement when DAO method is available
            val transactions = emptyList<Transaction>()
            Resource.Success(transactions)
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent transactions")
            Resource.Error("Failed to load recent transactions", e)
        }
    }
}
