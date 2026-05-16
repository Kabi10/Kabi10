package com.senthapps.slagrimarket.ui.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                // Get farmer's transactions
                transactionRepository.getTransactionsByFarmer(currentUser.id).collect { resource ->
                    when (resource) {
                        is com.senthapps.slagrimarket.data.repository.Resource.Success -> {
                            val transactions = resource.data ?: emptyList()
                            
                            // Calculate analytics
                            val totalRevenue = transactions
                                .filter { it.status == com.senthapps.slagrimarket.data.model.TransactionStatus.COMPLETED }
                                .sumOf { it.totalAmount }
                            
                            val totalOrders = transactions.size
                            
                            // Get active listings count
                            listingRepository.getListingsByFarmer(currentUser.id).collect { listingsResource ->
                                when (listingsResource) {
                                    is com.senthapps.slagrimarket.data.repository.Resource.Success -> {
                                        val listings = listingsResource.data ?: emptyList()
                                        val activeListings = listings.count { it.isActive }
                                        val totalViews = listings.sumOf { it.viewCount }
                                        
                                        // Calculate popular crops
                                        val cropStats = transactions
                                            .groupBy { it.listingId }
                                            .mapNotNull { (listingId, txns) ->
                                                val listing = listings.find { it.id == listingId }
                                                listing?.let {
                                                    CropStat(
                                                        name = it.cropType,
                                                        count = txns.size,
                                                        revenue = txns
                                                            .filter { tx -> tx.status == com.senthapps.slagrimarket.data.model.TransactionStatus.COMPLETED }
                                                            .sumOf { tx -> tx.totalAmount }
                                                    )
                                                }
                                            }
                                            .groupBy { it.name }
                                            .map { (name, stats) ->
                                                CropStat(
                                                    name = name,
                                                    count = stats.sumOf { it.count },
                                                    revenue = stats.sumOf { it.revenue }
                                                )
                                            }
                                            .sortedByDescending { it.revenue }
                                            .take(5)
                                        
                                        // Generate recent activity
                                        val recentActivity = generateRecentActivity(transactions, listings)
                                        
                                        _uiState.value = _uiState.value.copy(
                                            totalRevenue = totalRevenue,
                                            totalOrders = totalOrders,
                                            activeListings = activeListings,
                                            totalViews = totalViews,
                                            popularCrops = cropStats,
                                            recentActivity = recentActivity,
                                            isLoading = false
                                        )
                                    }
                                    is com.senthapps.slagrimarket.data.repository.Resource.Error -> {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            error = listingsResource.message
                                        )
                                    }
                                    is com.senthapps.slagrimarket.data.repository.Resource.Loading -> {
                                        // Keep loading
                                    }
                                }
                            }
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Loading -> {
                            // Keep loading
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading analytics")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }

    private fun generateRecentActivity(
        transactions: List<com.senthapps.slagrimarket.data.model.Transaction>,
        listings: List<com.senthapps.slagrimarket.data.model.Listing>
    ): List<ActivityItem> {
        val activities = mutableListOf<ActivityItem>()
        
        // Add recent transactions
        transactions.sortedByDescending { it.createdAt }.take(3).forEach { transaction ->
            activities.add(
                ActivityItem(
                    title = "New order received",
                    time = formatTimeAgo(transaction.createdAt),
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFF4CAF50)
                )
            )
        }
        
        // Add recent listings
        listings.sortedByDescending { it.createdAt }.take(2).forEach { listing ->
            activities.add(
                ActivityItem(
                    title = "Listed ${listing.cropType}",
                    time = formatTimeAgo(listing.createdAt),
                    icon = Icons.Default.Add,
                    color = Color(0xFF2196F3)
                )
            )
        }
        
        return activities.sortedByDescending { it.time }.take(5)
    }

    private fun formatTimeAgo(timestamp: String): String {
        return try {
            val instant = java.time.Instant.parse(timestamp)
            val now = java.time.Instant.now()
            val duration = java.time.Duration.between(instant, now)
            
            when {
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
                duration.toHours() < 24 -> "${duration.toHours()}h ago"
                duration.toDays() < 7 -> "${duration.toDays()}d ago"
                else -> "${duration.toDays() / 7}w ago"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}

data class AnalyticsUiState(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val activeListings: Int = 0,
    val totalViews: Int = 0,
    val popularCrops: List<CropStat> = emptyList(),
    val recentActivity: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CropStat(
    val name: String,
    val count: Int,
    val revenue: Double
)

data class ActivityItem(
    val title: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)
