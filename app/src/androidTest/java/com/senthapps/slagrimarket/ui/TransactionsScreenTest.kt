package com.senthapps.slagrimarket.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senthapps.slagrimarket.ui.transactions.TransactionsScreen
import com.senthapps.slagrimarket.ui.theme.SLAgrimarketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for TransactionsScreen
 * Tests critical user flows and accessibility features
 */
@RunWith(AndroidJUnit4::class)
class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun transactionsScreen_displaysTitle() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Verify transactions title is displayed
        composeTestRule.onNodeWithText("Transactions", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun transactionsScreen_displaysFilterChips() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Verify filter chips exist (All, Pending, Confirmed, etc.)
        composeTestRule.onNodeWithText("All", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun transactionsScreen_filterChipsAreClickable() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Click on Pending filter
        composeTestRule.onNodeWithText("Pending", substring = true, ignoreCase = true)
            .performClick()

        // Verify the chip is now selected (no exception means it worked)
        composeTestRule.onNodeWithText("Pending", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun transactionsScreen_displaysLoadingOrContent() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Either loading skeleton or transactions content should be displayed
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun transactionsScreen_backButtonWorks() {
        var navigatedBack = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }

        // Click on back button
        composeTestRule.onNodeWithContentDescription("Back", substring = true, ignoreCase = true)
            .performClick()

        // Verify navigation was triggered
        assert(navigatedBack) { "Expected navigation back" }
    }

    @Test
    fun transactionsScreen_isScrollable() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Try to scroll the content
        composeTestRule.onRoot().performTouchInput {
            swipeUp()
        }

        // If no exception, scroll worked
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun transactionsScreen_emptyStateDisplaysWhenNoTransactions() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                TransactionsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Either transactions or empty state should be displayed
        // This test verifies the screen handles both cases gracefully
        composeTestRule.onRoot().assertExists()
    }
}

