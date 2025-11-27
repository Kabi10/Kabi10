package com.senthapps.slagrimarket.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senthapps.slagrimarket.data.model.*
import com.senthapps.slagrimarket.ui.home.HomeScreen
import com.senthapps.slagrimarket.ui.theme.SLAgrimarketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for HomeScreen
 * Tests critical user flows and accessibility features
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Verify welcome message is displayed
        composeTestRule.onNodeWithText("Welcome", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysQuickActions() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Verify quick actions section exists
        composeTestRule.onNodeWithText("Quick Actions", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysMarketPricesSection() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Verify market prices section exists
        composeTestRule.onNodeWithText("Market Prices", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_browseButtonNavigatesToListings() {
        var navigatedToListings = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = { navigatedToListings = true },
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Click on Browse button
        composeTestRule.onNodeWithText("Browse", substring = true, ignoreCase = true)
            .performClick()

        // Verify navigation was triggered
        assert(navigatedToListings) { "Expected navigation to listings" }
    }

    @Test
    fun homeScreen_sellButtonNavigatesToCreateListing() {
        var navigatedToCreateListing = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = { navigatedToCreateListing = true },
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Click on Sell Now button
        composeTestRule.onNodeWithText("Sell Now", substring = true, ignoreCase = true)
            .performClick()

        // Verify navigation was triggered
        assert(navigatedToCreateListing) { "Expected navigation to create listing" }
    }

    @Test
    fun homeScreen_transactionsButtonNavigatesToTransactions() {
        var navigatedToTransactions = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = { navigatedToTransactions = true },
                    onNavigateToMarketPrices = {}
                )
            }
        }

        // Click on Transactions button
        composeTestRule.onNodeWithText("Transactions", substring = true, ignoreCase = true)
            .performClick()

        // Verify navigation was triggered
        assert(navigatedToTransactions) { "Expected navigation to transactions" }
    }

    @Test
    fun homeScreen_viewAllMarketPricesNavigates() {
        var navigatedToMarketPrices = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                HomeScreen(
                    onNavigateToListings = {},
                    onNavigateToProfile = {},
                    onNavigateToCreateListing = {},
                    onNavigateToTransactions = {},
                    onNavigateToMarketPrices = { navigatedToMarketPrices = true }
                )
            }
        }

        // Click on View All for market prices
        composeTestRule.onAllNodesWithText("View All", substring = true, ignoreCase = true)
            .onFirst()
            .performClick()

        // Verify navigation was triggered
        assert(navigatedToMarketPrices) { "Expected navigation to market prices" }
    }
}

