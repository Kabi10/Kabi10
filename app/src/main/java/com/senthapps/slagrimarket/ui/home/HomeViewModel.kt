package com.senthapps.slagrimarket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.repository.ActivityRepository
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.MarketPriceRepository
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import com.senthapps.slagrimarket.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val marketPriceRepository: MarketPriceRepository,
    private val activityRepository: ActivityRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUser = authRepository.currentUser

    init {
        loadAllData()
    }

    /**
     * Load all home screen data
     */
    private fun loadAllData() {
        loadRecentListings()
        loadMarketPrices()
        loadRecentActivities()
        loadStatistics()
    }

    /**
     * Load recent listings
     */
    private fun loadRecentListings() {
        viewModelScope.launch {
            try {
                listingRepository.getAllActiveListingsFlow()
                    .catch { e ->
                        Timber.e(e, "Error loading recent listings")
                        _uiState.update { it.copy(error = "Failed to load listings") }
                    }
                    .collect { listings ->
                        _uiState.update {
                            it.copy(
                                recentListings = listings.take(5),
                                isLoadingListings = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading recent listings")
                _uiState.update { it.copy(isLoadingListings = false) }
            }
        }
    }

    /**
     * Load market prices
     */
    private fun loadMarketPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPrices = true) }

            try {
                marketPriceRepository.getAllMarketPrices(forceRefresh = false)
                    .catch { e ->
                        Timber.e(e, "Error loading market prices")
                        _uiState.update {
                            it.copy(
                                isLoadingPrices = false,
                                error = "Failed to load market prices"
                            )
                        }
                    }
                    .collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                _uiState.update { it.copy(isLoadingPrices = true) }
                            }
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        marketPrices = resource.data ?: emptyList(),
                                        isLoadingPrices = false,
                                        lastPricesUpdated = System.currentTimeMillis()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoadingPrices = false,
                                        error = resource.message
                                    )
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading market prices")
                _uiState.update { it.copy(isLoadingPrices = false) }
            }
        }
    }

    /**
     * Load recent activities
     */
    private fun loadRecentActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingActivities = true) }

            try {
                currentUser.firstOrNull()?.let { user ->
                    activityRepository.getActivitiesForUser(user.id, forceRefresh = false)
                        .catch { e ->
                            Timber.e(e, "Error loading activities")
                            // Don't show error for activities - they're not critical
                            _uiState.update {
                                it.copy(
                                    isLoadingActivities = false,
                                    recentActivities = emptyList()
                                )
                            }
                        }
                        .collect { resource ->
                            when (resource) {
                                is Resource.Loading -> {
                                    _uiState.update { it.copy(isLoadingActivities = true) }
                                }
                                is Resource.Success -> {
                                    _uiState.update {
                                        it.copy(
                                            recentActivities = resource.data?.take(5) ?: emptyList(),
                                            isLoadingActivities = false
                                        )
                                    }
                                }
                                is Resource.Error -> {
                                    Timber.w("Activities loading failed: ${resource.message}")
                                    // Don't show error to user for activities - they're supplementary
                                    _uiState.update {
                                        it.copy(
                                            isLoadingActivities = false,
                                            recentActivities = emptyList()
                                        )
                                    }
                                }
                            }
                        }
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoadingActivities = false,
                            recentActivities = emptyList()
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading activities")
                _uiState.update { 
                    it.copy(
                        isLoadingActivities = false,
                        recentActivities = emptyList()
                    ) 
                }
            }
        }
    }

    /**
     * Load user statistics
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }

            try {
                currentUser.firstOrNull()?.let { user ->
                    val statsResource = transactionRepository.getTransactionStatisticsResource(user.id)

                    when (statsResource) {
                        is Resource.Success -> {
                            val stats = statsResource.data ?: emptyMap()
                            _uiState.update {
                                it.copy(
                                    todayOrders = (stats["today_orders"] as? Int) ?: 0,
                                    todayRevenue = (stats["today_revenue"] as? Double) ?: 0.0,
                                    isLoadingStats = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Error loading statistics: ${statsResource.message}")
                            _uiState.update { it.copy(isLoadingStats = false) }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoadingStats = true) }
                        }
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoadingStats = false) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading statistics")
                _uiState.update { it.copy(isLoadingStats = false) }
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            try {
                // Refresh market prices
                marketPriceRepository.refreshMarketPrices()

                // Reload all data
                loadAllData()

                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing data")
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "Failed to refresh data"
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for home screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingListings: Boolean = false,
    val isLoadingPrices: Boolean = false,
    val isLoadingActivities: Boolean = false,
    val isLoadingStats: Boolean = false,
    val recentListings: List<Listing> = emptyList(),
    val marketPrices: List<MarketPrice> = emptyList(),
    val recentActivities: List<Activity> = emptyList(),
    val todayOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val error: String? = null,
    val lastPricesUpdated: Long = 0L
)
