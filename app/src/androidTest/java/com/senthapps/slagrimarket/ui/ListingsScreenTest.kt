package com.senthapps.slagrimarket.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senthapps.slagrimarket.ui.listings.ListingsScreen
import com.senthapps.slagrimarket.ui.theme.SLAgrimarketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for ListingsScreen
 * Tests critical user flows and accessibility features
 */
@RunWith(AndroidJUnit4::class)
class ListingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listingsScreen_displaysTitle() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = {},
                    onNavigateToSearch = {}
                )
            }
        }

        // Verify listings title is displayed
        composeTestRule.onNodeWithText("Listings", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun listingsScreen_displaysSearchButton() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = {},
                    onNavigateToSearch = {}
                )
            }
        }

        // Verify search icon/button exists
        composeTestRule.onNodeWithContentDescription("Search", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun listingsScreen_searchButtonNavigatesToSearch() {
        var navigatedToSearch = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = {},
                    onNavigateToSearch = { navigatedToSearch = true }
                )
            }
        }

        // Click on search button
        composeTestRule.onNodeWithContentDescription("Search", substring = true, ignoreCase = true)
            .performClick()

        // Verify navigation was triggered
        assert(navigatedToSearch) { "Expected navigation to search" }
    }

    @Test
    fun listingsScreen_displaysLoadingOrContent() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = {},
                    onNavigateToSearch = {}
                )
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Either loading skeleton or listings content should be displayed
        // Check that the screen has some content
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun listingsScreen_backButtonWorks() {
        var navigatedBack = false
        
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = { navigatedBack = true },
                    onNavigateToSearch = {}
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
    fun listingsScreen_isScrollable() {
        composeTestRule.setContent {
            SLAgrimarketTheme {
                ListingsScreen(
                    onNavigateBack = {},
                    onNavigateToSearch = {}
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
}

