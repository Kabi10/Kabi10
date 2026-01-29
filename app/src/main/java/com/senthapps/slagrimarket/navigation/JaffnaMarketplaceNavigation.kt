package com.senthapps.slagrimarket.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senthapps.slagrimarket.util.RelativeTimeUtil
import com.senthapps.slagrimarket.ui.analytics.AnalyticsScreen
import com.senthapps.slagrimarket.ui.auth.IndustrialOtpVerificationScreen
import com.senthapps.slagrimarket.ui.auth.IndustrialPhoneInputScreen
import com.senthapps.slagrimarket.ui.chat.ChatScreen
import com.senthapps.slagrimarket.ui.chat.ConversationsScreen
import com.senthapps.slagrimarket.ui.favorites.FavoritesScreen
import com.senthapps.slagrimarket.ui.help.ContactScreen
import com.senthapps.slagrimarket.ui.help.FAQScreen
import com.senthapps.slagrimarket.ui.help.HelpScreen
import com.senthapps.slagrimarket.ui.help.PrivacyScreen
import com.senthapps.slagrimarket.ui.help.TermsScreen
import com.senthapps.slagrimarket.ui.map.ListingsMapScreen
import com.senthapps.slagrimarket.ui.map.LocationMapScreen
import com.senthapps.slagrimarket.ui.notifications.NotificationsScreen
import com.senthapps.slagrimarket.ui.profile.EditProfileScreen
import com.senthapps.slagrimarket.ui.profile.ProfileScreen
import com.senthapps.slagrimarket.ui.reviews.WriteReviewScreen
import com.senthapps.slagrimarket.ui.search.AdvancedSearchScreen
import com.senthapps.slagrimarket.ui.search.SearchScreen
import com.senthapps.slagrimarket.ui.settings.IndustrialSettingsScreen
import com.senthapps.slagrimarket.ui.sync.SyncSettingsScreen
import com.senthapps.slagrimarket.ui.home.IndustrialHomeScreen
import com.senthapps.slagrimarket.ui.home.IndustrialMarketPricesScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingSuccessScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingViewModel
import com.senthapps.slagrimarket.ui.listings.IndustrialCategorySelectionScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialCreateListingScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialListingDetailScreen
import com.senthapps.slagrimarket.ui.listings.IndustrialListingsListScreen
import com.senthapps.slagrimarket.ui.listings.ListingDetail
import com.senthapps.slagrimarket.ui.listings.ListingPreview
import com.senthapps.slagrimarket.ui.listings.ListingsScreen
import com.senthapps.slagrimarket.ui.transactions.CreateTransactionScreen
import com.senthapps.slagrimarket.ui.transactions.IndustrialTransactionDetailScreen
import com.senthapps.slagrimarket.ui.transactions.IndustrialTransactionsScreen
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.theme.LocalAppLanguage

@Composable
fun JaffnaMarketplaceNavigation(
    navController: NavHostController = rememberNavController(),
    startWithAuth: Boolean = false // Set to true to enable auth flow
) {
    val context = LocalContext.current
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
        // Authentication screens - INDUSTRIAL STYLE
        composable(Screen.PhoneInput.route) {
            IndustrialPhoneInputScreen(
                onNavigateToOtpVerification = { phoneNumber, otpId ->
                    navController.navigate(Screen.OtpVerification.createRoute(phoneNumber, otpId))
                }
            )
        }

        composable(Screen.OtpVerification.route) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val otpId = backStackEntry.arguments?.getString("otpId") ?: ""
            IndustrialOtpVerificationScreen(
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
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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
            val viewModel: CreateListingViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Navigate to success screen when listing is successfully created
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(Screen.CreateListingSuccess.route) {
                        popUpTo(Screen.CreateListing.route) { inclusive = true }
                    }
                }
            }

            IndustrialCreateListingScreen(
                onSubmit = { productName, category, quantity, unit, price, location ->
                    // Set all form values in ViewModel
                    viewModel.updateCropType(productName)
                    viewModel.updateQuantity(quantity)
                    viewModel.updateUnit(unit)
                    viewModel.updatePricePerUnit(price)
                    viewModel.updateLocation(location)
                    // Set defaults for required fields not in industrial form
                    viewModel.updateQuality("A") // Default quality grade
                    viewModel.updateHarvestDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    // Create the listing
                    viewModel.createListing()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Create Listing Success Screen (Industrial UI)
        composable(Screen.CreateListingSuccess.route) {
            CreateListingSuccessScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
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

            // Convert Listing to ListingDetail data class
            val listingDetail = uiState.listing?.let { listing ->
                ListingDetail(
                    id = listing.id,
                    productName = listing.cropNameEnglish.ifEmpty { listing.cropType },
                    price = listing.pricePerUnit,
                    unit = listing.unit,
                    categoryName = listing.cropType,
                    availableFrom = listing.availableFrom,
                    availableUntil = listing.availableUntil,
                    pickupLocation = listing.location,
                    sellerName = listing.farmerName ?: "",
                    sellerDistrict = listing.location,
                    sellerPhone = listing.farmerPhone,
                    postedTime = listing.createdAt
                )
            }

            // Display industrial listing detail screen with proper data class
            IndustrialListingDetailScreen(
                listing = listingDetail,
                language = com.senthapps.slagrimarket.ui.home.AppLanguage.ENGLISH,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSendMessage = {
                    // Chat functionality is not yet implemented - show placeholder message
                    Toast.makeText(context, "Chat coming soon", Toast.LENGTH_SHORT).show()
                },
                isLoading = uiState.isLoading,
                isError = uiState.error != null,
                onRetry = { viewModel.loadListing(listingId) }
            )
        }

        composable(Screen.Transactions.route) {
            IndustrialTransactionsScreen(
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
            IndustrialTransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactUser = { phoneNumber ->
                    // Open phone dialer with the user's phone number
                    if (phoneNumber.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        
        composable(Screen.MarketPrices.route) {
            val viewModel: com.senthapps.slagrimarket.ui.home.HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Calculate relative time for the market prices update
            val lastUpdatedText = if (uiState.lastPricesUpdated > 0) {
                RelativeTimeUtil.getUpdatedTimeString(uiState.lastPricesUpdated, "en")
            } else {
                "UPDATED: JUST NOW"
            }

            IndustrialMarketPricesScreen(
                marketPrices = uiState.marketPrices,
                onNavigateBack = {
                    navController.popBackStack()
                },
                lastUpdatedText = lastUpdatedText,
                isLoading = uiState.isLoadingPrices,
                isError = uiState.error != null && uiState.marketPrices.isEmpty(),
                onRetry = { viewModel.refreshData() }
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
                },
                onNavigateToHelp = {
                    navController.navigate(Screen.Help.route)
                },
                onNavigateToAbout = {
                    // Show app version info via a toast for MVP
                    // AboutScreen route exists but navigates to Help for now
                    navController.navigate(Screen.Help.route)
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

        // Industrial Settings (Language Selection)
        composable(Screen.Settings.route) {
            val languageViewModel: LanguageToggleViewModel = hiltViewModel()
            val currentLanguage = LocalAppLanguage.current

            IndustrialSettingsScreen(
                currentLanguage = currentLanguage,
                onLanguageSelected = { language ->
                    // Convert AppLanguage to string code and persist
                    val languageCode = when (language) {
                        com.senthapps.slagrimarket.ui.home.AppLanguage.ENGLISH -> "en"
                        com.senthapps.slagrimarket.ui.home.AppLanguage.SINHALA -> "si"
                        com.senthapps.slagrimarket.ui.home.AppLanguage.TAMIL -> "ta"
                    }
                    languageViewModel.setLanguage(languageCode)
                    // Navigate back - UI will update reactively via CompositionLocal
                    navController.popBackStack()
                },
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
                    navController.navigate(Screen.Contact.route)
                },
                onNavigateToTerms = {
                    navController.navigate(Screen.Terms.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.Privacy.route)
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

        composable(Screen.Contact.route) {
            ContactScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Terms.route) {
            TermsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(
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
    object CreateListingSuccess : Screen("create_listing_success") // Industrial UI - Success confirmation
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
    object Contact : Screen("contact")
    object Terms : Screen("terms")
    object Privacy : Screen("privacy")
    object Settings : Screen("settings") // Industrial UI - Language settings
}
