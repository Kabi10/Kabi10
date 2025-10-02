package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.PaymentMethod
import com.senthapps.slagrimarket.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced TransactionDao with reactive Flow-based queries and comprehensive transaction management
 */
@Dao
interface TransactionDao {

    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransactionByIdFlow(transactionId: String): Flow<Transaction?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // ============================================================================
    // USER-SPECIFIC QUERIES
    // ============================================================================

    @Query("SELECT * FROM transactions WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    suspend fun getTransactionsByFarmer(farmerId: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getTransactionsByFarmerFlow(farmerId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    suspend fun getTransactionsByBuyer(buyerId: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getTransactionsByBuyerFlow(buyerId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE listingId = :listingId ORDER BY createdAt DESC")
    suspend fun getTransactionsByListing(listingId: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE listingId = :listingId ORDER BY createdAt DESC")
    fun getTransactionsByListingFlow(listingId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE (:userId IS NULL OR farmerId = :userId OR buyerId = :userId)
        AND (:status IS NULL OR status = :status)
        AND (:paymentStatus IS NULL OR paymentStatus = :paymentStatus)
        AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTransactionsForUser(
        userId: String? = null,
        status: TransactionStatus? = null,
        paymentStatus: PaymentStatus? = null,
        paymentMethod: PaymentMethod? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE (:userId IS NULL OR farmerId = :userId OR buyerId = :userId)
        AND (:status IS NULL OR status = :status)
        AND (:paymentStatus IS NULL OR paymentStatus = :paymentStatus)
        ORDER BY createdAt DESC
    """)
    fun getTransactionsForUserFlow(
        userId: String? = null,
        status: TransactionStatus? = null,
        paymentStatus: PaymentStatus? = null
    ): Flow<List<Transaction>>

    // ============================================================================
    // STATUS AND PRIORITY QUERIES
    // ============================================================================

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getTransactionsByStatus(status: TransactionStatus): List<Transaction>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY createdAt DESC")
    fun getTransactionsByStatusFlow(status: TransactionStatus): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE paymentStatus = :paymentStatus ORDER BY createdAt DESC")
    suspend fun getTransactionsByPaymentStatus(paymentStatus: PaymentStatus): List<Transaction>

    @Query("SELECT * FROM transactions WHERE paymentStatus = :paymentStatus ORDER BY createdAt DESC")
    fun getTransactionsByPaymentStatusFlow(paymentStatus: PaymentStatus): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) = date('now')
        ORDER BY pickupDate ASC
    """)
    suspend fun getTodayPickupsByUser(userId: String): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) = date('now')
        ORDER BY pickupDate ASC
    """)
    fun getTodayPickupsByUserFlow(userId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) < date('now')
        AND status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY pickupDate ASC
    """)
    suspend fun getOverdueTransactionsByUser(userId: String): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) < date('now')
        AND status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY pickupDate ASC
    """)
    fun getOverdueTransactionsByUserFlow(userId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) BETWEEN date('now', '+1 day') AND date('now', '+3 days')
        AND status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY pickupDate ASC
    """)
    suspend fun getUpcomingTransactionsByUser(userId: String): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE (farmerId = :userId OR buyerId = :userId)
        AND date(pickupDate) BETWEEN date('now', '+1 day') AND date('now', '+3 days')
        AND status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY pickupDate ASC
    """)
    fun getUpcomingTransactionsByUserFlow(userId: String): Flow<List<Transaction>>

    // ============================================================================
    // UPDATE OPERATIONS
    // ============================================================================

    @Query("UPDATE transactions SET status = :status WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)

    @Query("UPDATE transactions SET paymentStatus = :paymentStatus WHERE id = :transactionId")
    suspend fun updatePaymentStatus(transactionId: String, paymentStatus: PaymentStatus)

    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE id = :transactionId")
    suspend fun updateTransactionStatusWithTimestamp(transactionId: String, status: TransactionStatus, updatedAt: String)

    @Query("UPDATE transactions SET status = 'COMPLETED', completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :transactionId")
    suspend fun completeTransaction(transactionId: String, completedAt: String, updatedAt: String)

    @Query("UPDATE transactions SET farmerRating = :rating WHERE id = :transactionId")
    suspend fun updateFarmerRating(transactionId: String, rating: Int)

    @Query("UPDATE transactions SET buyerRating = :rating WHERE id = :transactionId")
    suspend fun updateBuyerRating(transactionId: String, rating: Int)

    // ============================================================================
    // ANALYTICS AND STATISTICS QUERIES
    // ============================================================================

    @Query("SELECT COUNT(*) FROM transactions WHERE farmerId = :farmerId AND status = 'COMPLETED'")
    suspend fun getCompletedTransactionCountByFarmer(farmerId: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE farmerId = :farmerId AND status = 'COMPLETED'")
    fun getCompletedTransactionCountByFarmerFlow(farmerId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE buyerId = :buyerId AND status = 'COMPLETED'")
    suspend fun getCompletedTransactionCountByBuyer(buyerId: String): Int

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE farmerId = :farmerId AND status = 'COMPLETED'")
    suspend fun getTotalEarningsByFarmer(farmerId: String): Double?

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE farmerId = :farmerId AND status = 'COMPLETED'")
    fun getTotalEarningsByFarmerFlow(farmerId: String): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE buyerId = :buyerId AND status = 'COMPLETED'")
    suspend fun getTotalSpendingByBuyer(buyerId: String): Double?

    @Query("SELECT AVG(farmerRating) FROM transactions WHERE farmerId = :farmerId AND farmerRating IS NOT NULL")
    suspend fun getAverageFarmerRating(farmerId: String): Double?

    @Query("SELECT AVG(buyerRating) FROM transactions WHERE buyerId = :buyerId AND buyerRating IS NOT NULL")
    suspend fun getAverageBuyerRating(buyerId: String): Double?

    @Query("""
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled,
            SUM(CASE WHEN status = 'COMPLETED' THEN totalAmount ELSE 0 END) as totalValue,
            AVG(CASE WHEN status = 'COMPLETED' THEN totalAmount END) as avgValue
        FROM transactions
        WHERE farmerId = :farmerId
    """)
    suspend fun getFarmerTransactionStats(farmerId: String): TransactionStats

    @Query("""
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled,
            SUM(CASE WHEN status = 'COMPLETED' THEN totalAmount ELSE 0 END) as totalValue,
            AVG(CASE WHEN status = 'COMPLETED' THEN totalAmount END) as avgValue
        FROM transactions
        WHERE farmerId = :farmerId
    """)
    fun getFarmerTransactionStatsFlow(farmerId: String): Flow<TransactionStats>

    @Query("""
        SELECT DISTINCT pickupLocation
        FROM transactions
        WHERE status IN ('COMPLETED', 'IN_PROGRESS', 'CONFIRMED')
        ORDER BY pickupLocation
    """)
    suspend fun getActivePickupLocations(): List<String>

    // ============================================================================
    // CLEANUP QUERIES
    // ============================================================================

    @Query("""
        DELETE FROM transactions
        WHERE status = 'CANCELLED'
        AND datetime(updatedAt) < datetime('now', '-' || :retentionDays || ' days')
    """)
    suspend fun cleanupCancelledTransactions(retentionDays: Int = 90)

    @Query("""
        DELETE FROM transactions
        WHERE status = 'COMPLETED'
        AND datetime(completedAt) < datetime('now', '-' || :retentionDays || ' days')
    """)
    suspend fun cleanupOldCompletedTransactions(retentionDays: Int = 365)

    @Query("SELECT COUNT(*) FROM transactions WHERE datetime(createdAt) > datetime('now', '-1 hour')")
    suspend fun getRecentTransactionCount(): Int

    // ============================================================================
    // LEGACY SYNC METHODS (MAINTAINED FOR COMPATIBILITY)
    // ============================================================================

    @Query("SELECT * FROM transactions WHERE clientId = :clientId")
    suspend fun getTransactionByClientId(clientId: String): Transaction?

    @Query("UPDATE transactions SET id = :serverId WHERE id = :localId")
    suspend fun updateTransactionServerId(localId: String, serverId: String)
}

/**
 * Data class for transaction statistics
 */
data class TransactionStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val cancelled: Int,
    val totalValue: Double,
    val avgValue: Double?
)
