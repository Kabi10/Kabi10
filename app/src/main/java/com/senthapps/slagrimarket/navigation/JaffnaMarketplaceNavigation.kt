package com.senthapps.slagrimarket.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senthapps.slagrimarket.ui.analytics.AnalyticsScreen
import com.senthapps.slagrimarket.ui.auth.OtpVerificationScreen
import com.senthapps.slagrimarket.ui.auth.PhoneInputScreen
import com.senthapps.slagrimarket.ui.chat.ChatScreen
import com.senthapps.slagrimarket.ui.chat.ConversationsScreen
import com.senthapps.slagrimarket.ui.favorites.FavoritesScreen
import com.senthapps.slagrimarket.ui.help.FAQScreen
import com.senthapps.slagrimarket.ui.help.HelpScreen
import com.senthapps.slagrimarket.ui.map.ListingsMapScreen
import com.senthapps.slagrimarket.ui.map.LocationMapScreen
import com.senthapps.slagrimarket.ui.notifications.NotificationsScreen
import com.senthapps.slagrimarket.ui.profile.EditProfileScreen
import com.senthapps.slagrimarket.ui.reviews.WriteReviewScreen
import com.senthapps.slagrimarket.ui.search.AdvancedSearchScreen
import com.senthapps.slagrimarket.ui.sync.SyncSettingsScreen
import com.senthapps.slagrimarket.ui.home.HomeScreen
import com.senthapps.slagrimarket.ui.home.IndustrialHomeScreen
import com.senthapps.slagrimarket.ui.home.IndustrialMarketPricesScreen
import com.senthapps.slagrimarket.ui.home.MarketPricesScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialCategorySelectionScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialCreateListingScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialListingDetailScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialListingsListScreen
import com.senthapps.slagrimarket.ui.listings.ListingDetailScreen
import com.senthapps.slagrimarket.ui.listings.ListingPreview
import com.senthapps.slagrimarket.ui.listings.ListingsScreen
import com.senthapps.slagrimarket.ui.profile.EditProfileScreen
import com.senthapps.slagrimarket.ui.profile.ProfileScreen
import com.senthapps.slagrimarket.ui.search.AdvancedSearchScreen
import com.senthapps.slagrimarket.ui.search.SearchScreen
import com.senthapps.slagrimarket.ui.transactions.CreateTransactionScreen
import com.senthapps.slagrimarket.ui.transactions.TransactionDetailScreen
import com.senthapps.slagrimarket.ui.transactions.TransactionsScreen

@Composable
fun JaffnaMarketplaceNavigation(
    navController: NavHostController = rememberNavController(),
    startWithAuth: Boolean = false // Set to true to enable auth flow
) {
    // Choose start destination based on auth requirement
    val startDestination = if (startWithAuth) Screen.PhoneInput.route else "home_direct"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // Authentication screens
        composable(Screen.PhoneInput.route) {
            PhoneInputScreen(
                onNavigateToOtpVerification = { phoneNumber, otpId ->
                    navController.navigate(Screen.OtpVerification.createRoute(phoneNumber, otpId))
                }
            )
        }

        composable(Screen.OtpVerification.route) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val otpId = backStackEntry.arguments?.getString("otpId") ?: ""
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                otpId = otpId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVerificationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PhoneInput.route) { inclusive = true }
                    }
                }
            )
        }
        // MVP: Direct home screen access with unique route - INDUSTRIAL UI
        composable("home_direct") {
            IndustrialHomeScreen(
                onNavigateToSell = {
                    navController.navigate(Screen.CreateListing.route)
                },
                onNavigateToBuy = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToPrices = {
                    navController.navigate(Screen.MarketPrices.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Transactions.route)
                }
            )
        }

        // Alternative home route (in case something tries to navigate to original home) - INDUSTRIAL UI
        composable(Screen.Home.route) {
            IndustrialHomeScreen(
                onNavigateToSell = {
                    navController.navigate(Screen.CreateListing.route)
                },
                onNavigateToBuy = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToPrices = {
                    navController.navigate(Screen.MarketPrices.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Transactions.route)
                }
            )
        }
        
        composable(Screen.Listings.route) {
            ListingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        // Categories screen - 2x2 grid for buyer browsing
        composable(Screen.Categories.route) {
            IndustrialCategorySelectionScreen(
                onCategorySelected = { category ->
                    navController.navigate(Screen.CategoryListings.createRoute(category))
                }
            )
        }

        // Category listings - filtered list of products by category
        composable(Screen.CategoryListings.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val viewModel: com.senthapps.slagrimarket.ui.listings.ListingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Filter listings by category (convert industrial categories to existing format)
            val filteredListings = uiState.listings
                .filter { listing ->
                    // Map industrial categories to existing listing crop types
                    when (category) {
                        "VEGETABLES" -> listing.cropType.contains("Vegetable", ignoreCase = true) ||
                                       listing.cropType.contains("Tomato", ignoreCase = true) ||
                                       listing.cropType.contains("Carrot", ignoreCase = true) ||
                                       listing.cropType.contains("Onion", ignoreCase = true)
                        "FRUITS" -> listing.cropType.contains("Fruit", ignoreCase = true) ||
                                   listing.cropType.contains("Mango", ignoreCase = true) ||
                                   listing.cropType.contains("Banana", ignoreCase = true)
                        "GRAINS" -> listing.cropType.contains("Rice", ignoreCase = true) ||
                                  listing.cropType.contains("Wheat", ignoreCase = true) ||
                                  listing.cropType.contains("Grain", ignoreCase = true)
                        "LIVESTOCK" -> listing.cropType.contains("Livestock", ignoreCase = true) ||
                                     listing.cropType.contains("Poultry", ignoreCase = true) ||
                                     listing.cropType.contains("Chicken", ignoreCase = true)
                        else -> true
                    }
                }
                .map { listing ->
                    ListingPreview(
                        id = listing.id,
                        productName = listing.cropNameEnglish.ifEmpty { listing.cropType },
                        price = listing.pricePerUnit,
                        unit = listing.unit,
                        location = listing.location
                    )
                }

            IndustrialListingsListScreen(
                categoryName = category,
                listings = filteredListings,
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateListing.route) {
            IndustrialCreateListingScreen(
                onSubmit = { productName, category, quantity, unit, price, location ->
                    // TODO: Wire up to ViewModel to actually create listing
                    // For now, just navigate back
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        composable(Screen.ListingDetail.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val viewModel: com.senthapps.slagrimarket.ui.listings.ListingDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Load listing detail
            androidx.compose.runtime.LaunchedEffect(listingId) {
                viewModel.loadListing(listingId)
            }

            // Display industrial listing detail screen
            uiState.listing?.let { listing ->
                IndustrialListingDetailScreen(
                    productName = listing.cropNameEnglish.ifEmpty { listing.cropType },
                    price = listing.pricePerUnit,
                    unit = listing.unit,
                    quantity = listing.quantity,
                    location = listing.location,
                    sellerName = listing.farmerName ?: listing.farmerId,
                    imageUrl = null, // TODO: listing.imageUrls when available
                    isOwnListing = false, // TODO: Check if current user owns this listing
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCallSeller = {
                        // TODO: Open phone dialer or messaging app
                    },
                    onEdit = {
                        // TODO: Navigate to edit listing screen
                    },
                    onDelete = {
                        // TODO: Delete listing via ViewModel
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                }
            )
        }

        composable(Screen.CreateTransaction.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            CreateTransactionScreen(
                listingId = listingId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionCreated = {
                    navController.navigate(Screen.Transactions.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.TransactionDetail.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactUser = { userId ->
                    // TODO: Implement contact user functionality
                }
            )
        }
        
        composable(Screen.MarketPrices.route) {
            val viewModel: com.senthapps.slagrimarket.ui.home.HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            IndustrialMarketPricesScreen(
                marketPrices = uiState.marketPrices,
                onNavigateBack = {
                    navController.popBackStack()
                },
                lastUpdatedText = "UPDATED: JUST NOW" // TODO: Calculate actual time since last update
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    // MVP: No logout functionality needed
                    navController.popBackStack()
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSyncSettings = {
                    navController.navigate(Screen.SyncSettings.route)
                },
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdvancedSearch.route) {
            AdvancedSearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNotificationClick = { type, relatedId ->
                    // Navigate based on notification type
                    relatedId?.let {
                        when {
                            type.contains("ORDER") -> navController.navigate(Screen.TransactionDetail.createRoute(it))
                            type.contains("LISTING") -> navController.navigate(Screen.ListingDetail.createRoute(it))
                        }
                    }
                }
            )
        }

        composable(Screen.WriteReview.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val revieweeId = backStackEntry.arguments?.getString("revieweeId") ?: ""
            val revieweeName = backStackEntry.arguments?.getString("revieweeName") ?: ""
            WriteReviewScreen(
                transactionId = transactionId,
                revieweeId = revieweeId,
                revieweeName = revieweeName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SyncSettings.route) {
            SyncSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onNavigateToChat = { conversationId, otherUserName ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, otherUserName))
                }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            ChatScreen(
                conversationId = conversationId,
                otherUserName = otherUserName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LocationMap.route) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 9.6615
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 80.0255
            val locationName = backStackEntry.arguments?.getString("locationName") ?: "Jaffna"
            LocationMapScreen(
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ListingsMap.route) {
            val viewModel: com.senthapps.slagrimarket.ui.listings.ListingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            ListingsMapScreen(
                listings = uiState.listings,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }

        composable(Screen.Help.route) {
            HelpScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFAQ = {
                    navController.navigate(Screen.FAQ.route)
                },
                onNavigateToContact = {
                    // TODO: Implement contact screen
                },
                onNavigateToTerms = {
                    // TODO: Implement terms screen
                },
                onNavigateToPrivacy = {
                    // TODO: Implement privacy screen
                }
            )
        }

        composable(Screen.FAQ.route) {
            FAQScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    // Authentication screens
    object PhoneInput : Screen("phone_input")
    object OtpVerification : Screen("otp_verification/{phoneNumber}/{otpId}") {
        fun createRoute(phoneNumber: String, otpId: String) = "otp_verification/$phoneNumber/$otpId"
    }
    
    // Main app screens
    object Home : Screen("home")
    object Listings : Screen("listings")
    object Categories : Screen("categories") // Industrial UI - Category selection
    object CategoryListings : Screen("category_listings/{category}") { // Industrial UI - Filtered listings
        fun createRoute(category: String) = "category_listings/$category"
    }
    object Profile : Screen("profile")
    object CreateListing : Screen("create_listing")
    object Search : Screen("search")
    object MarketPrices : Screen("market_prices")
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    object Transactions : Screen("transactions")
    object CreateTransaction : Screen("create_transaction/{listingId}") {
        fun createRoute(listingId: String) = "create_transaction/$listingId"
    }
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
    object Analytics : Screen("analytics")
    object EditProfile : Screen("edit_profile")
    object AdvancedSearch : Screen("advanced_search")
    object Notifications : Screen("notifications")
    object WriteReview : Screen("write_review/{transactionId}/{revieweeId}/{revieweeName}") {
        fun createRoute(transactionId: String, revieweeId: String, revieweeName: String) = 
            "write_review/$transactionId/$revieweeId/$revieweeName"
    }
    object SyncSettings : Screen("sync_settings")
    object Favorites : Screen("favorites")
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{conversationId}/{otherUserName}") {
        fun createRoute(conversationId: String, otherUserName: String) = 
            "chat/$conversationId/$otherUserName"
    }
    object LocationMap : Screen("location_map/{latitude}/{longitude}/{locationName}") {
        fun createRoute(latitude: Double, longitude: Double, locationName: String) = 
            "location_map/$latitude/$longitude/$locationName"
    }
    object ListingsMap : Screen("listings_map")
    object Help : Screen("help")
    object FAQ : Screen("faq")
}
