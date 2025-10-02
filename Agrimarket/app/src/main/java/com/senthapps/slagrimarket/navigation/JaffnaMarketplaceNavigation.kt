package com.senthapps.slagrimarket.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senthapps.slagrimarket.ui.home.HomeScreen
import com.senthapps.slagrimarket.ui.home.MarketPricesScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingScreen
import com.senthapps.slagrimarket.ui.listings.ListingsScreen
import com.senthapps.slagrimarket.ui.profile.ProfileScreen
import com.senthapps.slagrimarket.ui.search.SearchScreen
import com.senthapps.slagrimarket.ui.transactions.CreateTransactionScreen
import com.senthapps.slagrimarket.ui.transactions.TransactionsScreen

@Composable
fun JaffnaMarketplaceNavigation(
    navController: NavHostController = rememberNavController()
) {
    // MVP: FORCE DIRECT HOME SCREEN ACCESS - NO AUTHENTICATION
    // This completely bypasses any authentication logic that might exist elsewhere

    NavHost(
        navController = navController,
        startDestination = "home_direct" // Use unique route to avoid conflicts
    ) {
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

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
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
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    // MVP: ALL AUTHENTICATION SCREENS COMPLETELY REMOVED
    // NO PHONE INPUT, NO OTP VERIFICATION, NO AUTH FLOWS
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
}
