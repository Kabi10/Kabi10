package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced ListingDao with reactive Flow-based queries and comprehensive filtering
 */
@Dao
interface ListingDao {

    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================

    @Query("SELECT * FROM listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): Listing?

    @Query("SELECT * FROM listings WHERE id = :listingId")
    fun getListingByIdFlow(listingId: String): Flow<Listing?>

    @Query("SELECT * FROM listings WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    suspend fun getListingsByFarmer(farmerId: String): List<Listing>

    @Query("SELECT * FROM listings WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getListingsByFarmerFlow(farmerId: String): Flow<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<Listing>)

    @Update
    suspend fun updateListing(listing: Listing)

    @Delete
    suspend fun deleteListing(listing: Listing)

    @Query("DELETE FROM listings WHERE id = :listingId")
    suspend fun deleteListingById(listingId: String)

    @Query("UPDATE listings SET isActive = 0 WHERE id = :listingId")
    suspend fun deactivateListing(listingId: String)

    @Query("DELETE FROM listings")
    suspend fun deleteAllListings()

    // ============================================================================
    // ENHANCED FILTERING AND SEARCH QUERIES
    // ============================================================================
    
    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND (:cropType IS NULL OR cropType = :cropType)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        AND (:quality IS NULL OR quality = :quality)
        AND (:minPrice IS NULL OR pricePerUnit >= :minPrice)
        AND (:maxPrice IS NULL OR pricePerUnit <= :maxPrice)
        AND (:searchQuery IS NULL OR
             cropType LIKE '%' || :searchQuery || '%' OR
             location LIKE '%' || :searchQuery || '%' OR
             cropNameTamil LIKE '%' || :searchQuery || '%' OR
             cropNameEnglish LIKE '%' || :searchQuery || '%' OR
             cropNameSinhala LIKE '%' || :searchQuery || '%')
        AND (availableUntil >= date('now'))
        ORDER BY
            CASE WHEN :sortBy = 'price_asc' THEN pricePerUnit END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN pricePerUnit END DESC,
            CASE WHEN :sortBy = 'quantity_asc' THEN quantity END ASC,
            CASE WHEN :sortBy = 'quantity_desc' THEN quantity END DESC,
            CASE WHEN :sortBy = 'harvest_date' THEN harvestDate END DESC,
            createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchListings(
        cropType: String? = null,
        location: String? = null,
        quality: QualityGrade? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        searchQuery: String? = null,
        sortBy: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND (:cropType IS NULL OR cropType = :cropType)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        AND (:quality IS NULL OR quality = :quality)
        AND (availableUntil >= date('now'))
        ORDER BY createdAt DESC
    """)
    fun searchListingsFlow(
        cropType: String? = null,
        location: String? = null,
        quality: QualityGrade? = null
    ): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE isActive = 1 AND availableUntil >= date('now') ORDER BY createdAt DESC")
    suspend fun getAllActiveListings(): List<Listing>

    @Query("SELECT * FROM listings WHERE isActive = 1 AND availableUntil >= date('now') ORDER BY createdAt DESC")
    fun getAllActiveListingsFlow(): Flow<List<Listing>>

    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND availableUntil >= date('now')
        AND viewCount > 0
        ORDER BY viewCount DESC, createdAt DESC
        LIMIT :limit
    """)
    suspend fun getTrendingListings(limit: Int = 10): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND availableUntil >= date('now')
        AND viewCount > 0
        ORDER BY viewCount DESC, createdAt DESC
        LIMIT :limit
    """)
    fun getTrendingListingsFlow(limit: Int = 10): Flow<List<Listing>>

    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND availableUntil >= date('now')
        AND quality = 'A'
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getPremiumListings(limit: Int = 10): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE isActive = 1
        AND availableUntil >= date('now')
        AND quality = 'A'
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getPremiumListingsFlow(limit: Int = 10): Flow<List<Listing>>

    @Query("""
        SELECT * FROM listings
        WHERE farmerId = :farmerId
        AND isActive = 1
        AND availableUntil < date('now')
        ORDER BY availableUntil DESC
    """)
    suspend fun getExpiredListingsByFarmer(farmerId: String): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE farmerId = :farmerId
        AND isActive = 1
        AND date(availableUntil) BETWEEN date('now') AND date('now', '+3 days')
        ORDER BY availableUntil ASC
    """)
    suspend fun getExpiringSoonListingsByFarmer(farmerId: String): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE farmerId = :farmerId
        AND isActive = 1
        AND date(availableUntil) BETWEEN date('now') AND date('now', '+3 days')
        ORDER BY availableUntil ASC
    """)
    fun getExpiringSoonListingsByFarmerFlow(farmerId: String): Flow<List<Listing>>
    
    // ============================================================================
    // SYNC STATUS QUERIES
    // ============================================================================

    @Query("SELECT * FROM listings WHERE syncStatus = :syncStatus ORDER BY updatedAt DESC")
    suspend fun getListingsBySyncStatus(syncStatus: SyncStatus): List<Listing>

    @Query("SELECT * FROM listings WHERE syncStatus = :syncStatus ORDER BY updatedAt DESC")
    fun getListingsBySyncStatusFlow(syncStatus: SyncStatus): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE syncStatus = 'PENDING' ORDER BY updatedAt ASC")
    suspend fun getPendingSyncListings(): List<Listing>

    @Query("SELECT * FROM listings WHERE syncStatus = 'PENDING' ORDER BY updatedAt ASC")
    fun getPendingSyncListingsFlow(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE syncStatus = 'FAILED' ORDER BY updatedAt DESC")
    suspend fun getFailedSyncListings(): List<Listing>

    @Query("SELECT * FROM listings WHERE syncStatus = 'FAILED' ORDER BY updatedAt DESC")
    fun getFailedSyncListingsFlow(): Flow<List<Listing>>

    @Query("UPDATE listings SET syncStatus = :syncStatus WHERE id = :listingId")
    suspend fun updateSyncStatus(listingId: String, syncStatus: SyncStatus)

    @Query("UPDATE listings SET syncStatus = 'SYNCED' WHERE syncStatus = 'PENDING'")
    suspend fun markAllPendingAsSynced()

    // ============================================================================
    // ANALYTICS AND STATISTICS QUERIES
    // ============================================================================

    @Query("SELECT DISTINCT cropType FROM listings WHERE isActive = 1 AND availableUntil >= date('now') ORDER BY cropType")
    suspend fun getAvailableCropTypes(): List<String>

    @Query("SELECT DISTINCT location FROM listings WHERE isActive = 1 AND availableUntil >= date('now') ORDER BY location")
    suspend fun getAvailableLocations(): List<String>

    @Query("SELECT DISTINCT quality FROM listings WHERE isActive = 1 AND availableUntil >= date('now') ORDER BY quality")
    suspend fun getAvailableQualityGrades(): List<QualityGrade>

    @Query("SELECT COUNT(*) FROM listings WHERE farmerId = :farmerId AND isActive = 1")
    suspend fun getActiveListingCountByFarmer(farmerId: String): Int

    @Query("SELECT COUNT(*) FROM listings WHERE farmerId = :farmerId AND isActive = 1")
    fun getActiveListingCountByFarmerFlow(farmerId: String): Flow<Int>

    @Query("SELECT AVG(pricePerUnit) FROM listings WHERE cropType = :cropType AND isActive = 1 AND availableUntil >= date('now')")
    suspend fun getAveragePriceForCrop(cropType: String): Double?

    @Query("""
        SELECT cropType, AVG(pricePerUnit) as avgPrice, COUNT(*) as count
        FROM listings
        WHERE isActive = 1 AND availableUntil >= date('now')
        GROUP BY cropType
        ORDER BY count DESC
    """)
    suspend fun getCropPriceStatistics(): List<CropPriceStats>

    @Query("UPDATE listings SET viewCount = viewCount + 1 WHERE id = :listingId")
    suspend fun incrementViewCount(listingId: String)

    @Query("UPDATE listings SET inquiryCount = inquiryCount + 1 WHERE id = :listingId")
    suspend fun incrementInquiryCount(listingId: String)

    // ============================================================================
    // CLEANUP QUERIES
    // ============================================================================

    @Query("""
        DELETE FROM listings
        WHERE isActive = 0
        AND datetime(updatedAt) < datetime('now', '-' || :retentionDays || ' days')
    """)
    suspend fun cleanupOldInactiveListings(retentionDays: Int = 30)

    @Query("""
        DELETE FROM listings
        WHERE date(availableUntil) < date('now', '-' || :retentionDays || ' days')
        AND isActive = 1
    """)
    suspend fun cleanupExpiredListings(retentionDays: Int = 7)

    @Query("""
        UPDATE listings
        SET isActive = 0
        WHERE availableUntil < date('now')
        AND isActive = 1
    """)
    suspend fun deactivateExpiredListings()

    // ============================================================================
    // LEGACY SYNC METHODS (MAINTAINED FOR COMPATIBILITY)
    // ============================================================================

    @Query("SELECT * FROM listings WHERE clientId = :clientId")
    suspend fun getListingByClientId(clientId: String): Listing?

    @Query("UPDATE listings SET id = :serverId WHERE id = :localId")
    suspend fun updateListingServerId(localId: String, serverId: String)
}

/**
 * Data class for crop price statistics
 */
data class CropPriceStats(
    val cropType: String,
    val avgPrice: Double,
    val count: Int
)
