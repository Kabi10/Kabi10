package com.senthapps.slagrimarket.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.auth.OtpVerificationScreen
import com.senthapps.slagrimarket.ui.auth.PhoneInputScreen
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.home.HomeScreen
import com.senthapps.slagrimarket.ui.home.MarketPricesScreen
import com.senthapps.slagrimarket.ui.listings.CreateListingScreen
import com.senthapps.slagrimarket.ui.listings.ListingDetailScreen
import com.senthapps.slagrimarket.ui.listings.ListingsScreen
import com.senthapps.slagrimarket.ui.profile.ProfileScreen
import com.senthapps.slagrimarket.ui.profile.EditProfileScreen
import com.senthapps.slagrimarket.ui.search.SearchScreen
import com.senthapps.slagrimarket.ui.search.AdvancedSearchScreen
import com.senthapps.slagrimarket.ui.transactions.CreateTransactionScreen
import com.senthapps.slagrimarket.ui.transactions.TransactionDetailScreen
import com.senthapps.slagrimarket.ui.transactions.TransactionsScreen
import com.senthapps.slagrimarket.ui.analytics.AnalyticsScreen
import com.senthapps.slagrimarket.ui.chat.ChatScreen
import com.senthapps.slagrimarket.ui.chat.ConversationsScreen
import com.senthapps.slagrimarket.ui.favorites.FavoritesScreen
import com.senthapps.slagrimarket.ui.help.FAQScreen
import com.senthapps.slagrimarket.ui.help.HelpScreen
import com.senthapps.slagrimarket.ui.map.ListingsMapScreen
import com.senthapps.slagrimarket.ui.map.LocationMapScreen
import com.senthapps.slagrimarket.ui.notifications.NotificationsScreen
import com.senthapps.slagrimarket.ui.reviews.WriteReviewScreen
import com.senthapps.slagrimarket.ui.sync.SyncSettingsScreen

/**
 * Main navigation composable with bottom navigation bar
 */
@Composable
fun AppNavigationWithBottomBar(
    startWithAuth: Boolean = false,
    navController: NavHostController = rememberNavController(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Define which routes should show the bottom bar
    val bottomBarRoutes = setOf(
        Screen.Home.route,
        "home_direct",
        Screen.Listings.route,
        Screen.Transactions.route,
        Screen.Profile.route
    )
    
    // Check if current route should show bottom bar
    val shouldShowBottomBar = currentDestination?.route in bottomBarRoutes
    
    // Choose start destination based on auth requirement
    val startDestination = if (startWithAuth) Screen.PhoneInput.route else "home_direct"
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems(currentLanguage).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
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

            // Alternative home route
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

            composable(Screen.MarketPrices.route) {
                MarketPricesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
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

            composable(Screen.ListingDetail.route) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                ListingDetailScreen(
                    listingId = listingId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPlaceOrder = { lid ->
                        navController.navigate(Screen.CreateTransaction.createRoute(lid))
                    },
                    onContactFarmer = { farmerId ->
                        navController.navigate(Screen.Conversations.route)
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
                        // TODO
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

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNotificationClick = { type, relatedId ->
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
                    onNavigateToContact = {},
                    onNavigateToTerms = {},
                    onNavigateToPrivacy = {}
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
}

/**
 * Bottom navigation items with trilingual support
 */
private fun bottomNavItems(currentLanguage: String): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = "home_direct",
            icon = Icons.Default.Home,
            label = when (currentLanguage) {
                "en" -> "Home"
                "ta" -> "முகப்பு"
                "si" -> "මුල් පිටුව"
                else -> "Home"
            }
        ),
        BottomNavItem(
            route = Screen.Listings.route,
            icon = Icons.Default.ShoppingCart,
            label = when (currentLanguage) {
                "en" -> "Browse"
                "ta" -> "உலாவவும்"
                "si" -> "බ්‍රවුස් කරන්න"
                else -> "Browse"
            }
        ),
        BottomNavItem(
            route = Screen.Transactions.route,
            icon = Icons.Default.List,
            label = when (currentLanguage) {
                "en" -> "Orders"
                "ta" -> "ஆர்டர்கள்"
                "si" -> "ඇණවුම්"
                else -> "Orders"
            }
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            icon = Icons.Default.Person,
            label = when (currentLanguage) {
                "en" -> "Profile"
                "ta" -> "சுயவிவரம்"
                "si" -> "පැතිකඩ"
                else -> "Profile"
            }
        )
    )
}

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

