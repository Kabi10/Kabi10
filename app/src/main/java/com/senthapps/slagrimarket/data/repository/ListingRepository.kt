package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateListingRequest
import com.senthapps.slagrimarket.data.api.ListingApiService
import com.senthapps.slagrimarket.data.api.UpdateListingRequest
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OpType
import com.senthapps.slagrimarket.data.model.QualityGrade
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
 * Repository for listing operations with enhanced offline-first architecture
 * Uses Room database as single source of truth and syncs with network in background
 */
@Singleton
class ListingRepository @Inject constructor(
    private val listingApiService: ListingApiService,
    private val listingDao: ListingDao,
    private val localOpDao: LocalOpDao,
    private val moshi: Moshi
) {
    
    // ============================================================================
    // ENHANCED OFFLINE-FIRST DATA ACCESS
    // ============================================================================

    /**
     * Get all active listings - offline-first with background refresh
     */
    fun getAllActiveListings(forceRefresh: Boolean = false): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        try {
            // Always emit cached data first (offline-first)
            val cachedListings = listingDao.getAllActiveListings()
            if (cachedListings.isNotEmpty()) {
                emit(Resource.Success(cachedListings))
            }

            // Check if we need to refresh from network
            val shouldRefresh = forceRefresh || shouldRefreshListings()

            if (shouldRefresh) {
                try {
                    // Fetch from network
                    val response = listingApiService.getListings(limit = 100, isActive = true)
                    if (response.isSuccessful) {
                        val networkListings = response.body()?.listings ?: emptyList()

                        // Update local database
                        listingDao.insertListings(networkListings)

                        // Emit updated data
                        emit(Resource.Success(networkListings))
                    } else {
                        // Network failed, but we have cached data
                        if (cachedListings.isEmpty()) {
                            emit(Resource.Error("Failed to load listings", null))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh listings from network")
                    // Network failed, but we have cached data
                    if (cachedListings.isEmpty()) {
                        emit(Resource.Error("No internet connection", e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting listings")
            emit(Resource.Error("Failed to load listings", e))
        }
    }

    /**
     * Get all active listings with reactive Flow (original method preserved)
     */
    fun getAllActiveListingsFlow(): Flow<List<Listing>> {
        return listingDao.getAllActiveListingsFlow()
    }

    /**
     * Get listings by farmer - offline-first with background refresh
     */
    fun getListingsByFarmer(farmerId: String, forceRefresh: Boolean = false): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        try {
            // Emit cached data first
            val cachedListings = listingDao.getListingsByFarmer(farmerId)
            if (cachedListings.isNotEmpty()) {
                emit(Resource.Success(cachedListings))
            }

            if (forceRefresh || shouldRefreshListings()) {
                try {
                    val response = listingApiService.getListingsByFarmer(farmerId, limit = 50)
                    if (response.isSuccessful) {
                        val networkListings = response.body()?.listings ?: emptyList()
                        listingDao.insertListings(networkListings)
                        emit(Resource.Success(networkListings))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh farmer listings")
                    if (cachedListings.isEmpty()) {
                        emit(Resource.Error("Failed to load farmer listings", e))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading farmer listings", e))
        }
    }

    /**
     * Get listings by farmer with reactive Flow (original method preserved)
     */
    fun getListingsByFarmerFlow(farmerId: String): Flow<List<Listing>> {
        return listingDao.getListingsByFarmerFlow(farmerId)
    }

    /**
     * Search listings - offline-first with background refresh
     */
    fun searchListings(
        cropType: String? = null,
        location: String? = null,
        forceRefresh: Boolean = false
    ): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        try {
            // Emit cached search results first
            val cachedResults = listingDao.searchListings(cropType, location)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            if (forceRefresh || shouldRefreshListings()) {
                try {
                    val response = listingApiService.searchListings(
                        query = "$cropType $location".trim(),
                        limit = 50
                    )
                    if (response.isSuccessful) {
                        val networkResults = response.body()?.listings ?: emptyList()
                        listingDao.insertListings(networkResults)
                        emit(Resource.Success(networkResults))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh search results")
                    if (cachedResults.isEmpty()) {
                        emit(Resource.Error("Failed to search listings", e))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error searching listings", e))
        }
    }

    /**
     * Search listings with reactive Flow (original method preserved)
     */
    fun searchListingsFlow(cropType: String? = null, location: String? = null): Flow<List<Listing>> {
        return listingDao.searchListingsFlow(cropType, location)
    }

    /**
     * Get listing by ID - offline-first
     */
    suspend fun getListingById(listingId: String): Resource<Listing> {
        return try {
            // Try local first
            val localListing = listingDao.getListingById(listingId)
            if (localListing != null) {
                Resource.Success(localListing)
            } else {
                // Try network
                val response = listingApiService.getListingById(listingId)
                if (response.isSuccessful) {
                    val networkListing = response.body()
                    if (networkListing != null) {
                        listingDao.insertListing(networkListing)
                        Resource.Success(networkListing)
                    } else {
                        Resource.Error("Listing not found", null)
                    }
                } else {
                    Resource.Error("Failed to load listing", null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting listing by ID")
            Resource.Error("Failed to load listing", e)
        }
    }
    
    suspend fun createListing(
        cropType: String,
        quantity: Double,
        unit: String,
        pricePerUnit: Double,
        quality: String,
        harvestDate: String,
        location: String,
        farmerId: String
    ): Result<Listing> {
        return try {
            // Parse quality string to enum
            val qualityGrade = try {
                QualityGrade.valueOf(quality.uppercase())
            } catch (e: IllegalArgumentException) {
                QualityGrade.B // Default to B grade if invalid
            }

            // Create listing object
            val listing = Listing(
                id = UUID.randomUUID().toString(),
                farmerId = farmerId,
                cropType = cropType,
                quantity = quantity,
                unit = unit,
                pricePerUnit = pricePerUnit,
                quality = qualityGrade,
                harvestDate = harvestDate,
                location = location,
                images = emptyList(),
                isActive = true,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )
            
            // Save locally first (optimistic update)
            listingDao.insertListing(listing)
            
            // Create operation for sync
            val createRequest = CreateListingRequest(
                cropType = cropType,
                quantity = quantity,
                unit = unit,
                pricePerUnit = pricePerUnit,
                quality = quality,
                harvestDate = harvestDate,
                location = location
            )
            
            val op = LocalOp(
                opId = UUID.randomUUID().toString(),
                type = OpType.CREATE_LISTING,
                payload = moshi.adapter(CreateListingRequest::class.java).toJson(createRequest),
                clientTs = Instant.now().toString()
            )
            
            localOpDao.insertOp(op)
            
            Result.success(listing)
        } catch (e: Exception) {
            Timber.e(e, "Error creating listing")
            Result.failure(e)
        }
    }
    
    suspend fun updateListing(
        listingId: String,
        quantity: Double? = null,
        pricePerUnit: Double? = null,
        quality: String? = null,
        location: String? = null,
        isActive: Boolean? = null
    ): Result<Listing> {
        return try {
            val existingListing = listingDao.getListingById(listingId)
                ?: return Result.failure(Exception("Listing not found"))
            
            // Parse quality string to enum if provided
            val qualityGrade = quality?.let {
                try {
                    QualityGrade.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    existingListing.quality // Keep existing if invalid
                }
            } ?: existingListing.quality

            val updatedListing = existingListing.copy(
                quantity = quantity ?: existingListing.quantity,
                pricePerUnit = pricePerUnit ?: existingListing.pricePerUnit,
                quality = qualityGrade,
                location = location ?: existingListing.location,
                isActive = isActive ?: existingListing.isActive,
                updatedAt = Instant.now().toString()
            )
            
            // Update locally first (optimistic update)
            listingDao.updateListing(updatedListing)
            
            // Create operation for sync
            val updateRequest = UpdateListingRequest(
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                quality = quality,
                location = location,
                isActive = isActive
            )
            
            val op = LocalOp(
                opId = UUID.randomUUID().toString(),
                type = OpType.UPDATE_LISTING,
                payload = moshi.adapter(UpdateListingRequest::class.java).toJson(updateRequest),
                clientTs = Instant.now().toString()
            )
            
            localOpDao.insertOp(op)
            
            Result.success(updatedListing)
        } catch (e: Exception) {
            Timber.e(e, "Error updating listing")
            Result.failure(e)
        }
    }
    
    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            // Deactivate locally first (optimistic update)
            listingDao.deactivateListing(listingId)
            
            // Create operation for sync
            val op = LocalOp(
                opId = UUID.randomUUID().toString(),
                type = OpType.DELETE_LISTING,
                payload = """{"listingId": "$listingId"}""",
                clientTs = Instant.now().toString()
            )
            
            localOpDao.insertOp(op)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting listing")
            Result.failure(e)
        }
    }
    
    suspend fun syncListings(): Result<Unit> {
        return try {
            // Fetch latest listings from server
            val response = listingApiService.getListings(limit = 100)
            if (response.isSuccessful) {
                val listings = response.body()?.listings ?: emptyList()
                listingDao.insertListings(listings)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync listings"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing listings")
            Result.failure(e)
        }
    }
    
    suspend fun getAvailableCropTypes(): List<String> {
        return listingDao.getAvailableCropTypes()
    }

    suspend fun getAvailableLocations(): List<String> {
        return listingDao.getAvailableLocations()
    }

    // ============================================================================
    // CACHE MANAGEMENT
    // ============================================================================

    /**
     * Check if listings need refresh (older than 10 minutes)
     */
    private suspend fun shouldRefreshListings(): Boolean {
        // For now, always refresh if forced or if no cached data
        // TODO: Implement proper timestamp checking when DAO method is available
        return true
    }

    /**
     * Manual refresh of listings
     */
    suspend fun refreshListings(): Resource<Unit> {
        return try {
            val response = listingApiService.getListings(limit = 100, isActive = true)
            if (response.isSuccessful) {
                val listings = response.body()?.listings ?: emptyList()
                listingDao.insertListings(listings)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to refresh listings", null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing listings")
            Resource.Error("Network error", e)
        }
    }

    /**
     * Clear stale listings
     */
    suspend fun clearStaleData() {
        try {
            listingDao.deactivateExpiredListings()
            listingDao.cleanupOldInactiveListings(30) // Delete listings older than 30 days
        } catch (e: Exception) {
            Timber.e(e, "Error clearing stale listing data")
        }
    }

    /**
     * Get trending listings
     */
    fun getTrendingListings(): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        try {
            // Get cached trending listings (high view count, recent)
            val cachedTrending = listingDao.getTrendingListings()
            if (cachedTrending.isNotEmpty()) {
                emit(Resource.Success(cachedTrending))
            }

            // Try to fetch fresh trending data
            try {
                val response = listingApiService.getTrendingListings(limit = 20)
                if (response.isSuccessful) {
                    val trendingListings = response.body()?.listings ?: emptyList()

                    // Update cache
                    listingDao.insertListings(trendingListings)

                    emit(Resource.Success(trendingListings))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch trending listings")
                if (cachedTrending.isEmpty()) {
                    emit(Resource.Error("Failed to load trending listings", e))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading trending listings", e))
        }
    }

    /**
     * Increment view count for a listing
     */
    suspend fun incrementViewCount(listingId: String): Resource<Unit> {
        return try {
            // Update locally first
            listingDao.incrementViewCount(listingId)

            // Try to sync with server
            try {
                val response = listingApiService.incrementViewCount(listingId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync view count with server")
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing view count")
            Resource.Error("Failed to update view count", e)
        }
    }

    /**
     * Increment inquiry count for a listing
     */
    suspend fun incrementInquiryCount(listingId: String): Resource<Unit> {
        return try {
            // Update locally first
            listingDao.incrementInquiryCount(listingId)

            // Try to sync with server
            try {
                val response = listingApiService.incrementInquiryCount(listingId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync inquiry count with server")
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing inquiry count")
            Resource.Error("Failed to update inquiry count", e)
        }
    }
}
