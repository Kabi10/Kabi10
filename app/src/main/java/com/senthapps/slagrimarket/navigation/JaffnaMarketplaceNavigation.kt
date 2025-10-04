package com.senthapps.slagrimarket.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senthapps.slagrimarket.ui.analytics.AnalyticsScreen
import com.senthapps.slagrimarket.ui.auth.OtpVerificationScreen
import com.senthapps.slagrimarket.ui.auth.PhoneInputScreen
import com.senthapps.slagrimarket.ui.notifications.NotificationsScreen
import com.senthapps.slagrimarket.ui.profile.EditProfileScreen
import com.senthapps.slagrimarket.ui.reviews.WriteReviewScreen
import com.senthapps.slagrimarket.ui.search.AdvancedSearchScreen
import com.senthapps.slagrimarket.ui.home.HomeScreen
import com.senthapps.slagrimarket.ui.home.MarketPricesScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingScreen
import com.senthapps.slagrimarket.ui.listings.ListingDetailScreen
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
        startDestination = startDestination
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
        // MVP: Direct home screen access with unique route
        composable("home_direct") {
            HomeScreen(
                onNavigateToListings = {
                    navController.navigate(Screen.Listings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToCreateListing = {
                    navController.navigate(Screen.CreateListing.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToMarketPrices = {
                    navController.navigate(Screen.MarketPrices.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                }
            )
        }

        // Alternative home route (in case something tries to navigate to original home)
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToListings = {
                    navController.navigate(Screen.Listings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToCreateListing = {
                    navController.navigate(Screen.CreateListing.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToMarketPrices = {
                    navController.navigate(Screen.MarketPrices.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
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

        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onListingCreated = {
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
            ListingDetailScreen(
                listingId = listingId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlaceOrder = { listingId ->
                    navController.navigate(Screen.CreateTransaction.createRoute(listingId))
                },
                onContactFarmer = { farmerId ->
                    // TODO: Implement contact farmer functionality
                }
            )
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
            MarketPricesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
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
}
