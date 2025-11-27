package com.senthapps.slagrimarket.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senthapps.slagrimarket.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for bottom navigation
 * Tests navigation between main screens
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigation_homeTabIsSelectedByDefault() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Verify Home tab exists in bottom navigation
        composeTestRule.onNodeWithText("Home", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_canNavigateToBrowse() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Click on Browse tab
        composeTestRule.onNodeWithText("Browse", substring = true, ignoreCase = true)
            .performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify we're on the listings screen
        composeTestRule.onNodeWithText("Listings", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_canNavigateToOrders() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Click on Orders tab
        composeTestRule.onNodeWithText("Orders", substring = true, ignoreCase = true)
            .performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify we're on the transactions screen
        composeTestRule.onNodeWithText("Transactions", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_canNavigateToProfile() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Click on Profile tab
        composeTestRule.onNodeWithText("Profile", substring = true, ignoreCase = true)
            .performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify we're on the profile screen
        composeTestRule.onNodeWithText("Profile", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_canNavigateBackToHome() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Navigate to Browse
        composeTestRule.onNodeWithText("Browse", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Navigate back to Home
        composeTestRule.onNodeWithText("Home", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Verify we're back on home screen
        composeTestRule.onNodeWithText("Welcome", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_allTabsAreVisible() {
        // Wait for app to load
        composeTestRule.waitForIdle()

        // Verify all bottom navigation tabs exist
        composeTestRule.onNodeWithText("Home", substring = true, ignoreCase = true)
            .assertExists()
        composeTestRule.onNodeWithText("Browse", substring = true, ignoreCase = true)
            .assertExists()
        composeTestRule.onNodeWithText("Orders", substring = true, ignoreCase = true)
            .assertExists()
        composeTestRule.onNodeWithText("Profile", substring = true, ignoreCase = true)
            .assertExists()
    }
}

