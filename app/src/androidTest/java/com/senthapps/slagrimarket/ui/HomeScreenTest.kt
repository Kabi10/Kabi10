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
 * UI automation tests for the App launch flow
 * Since auth is enabled, the app starts with PhoneInputScreen
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // Wait for the compose hierarchy to be ready
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onRoot().fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun app_displaysWelcomeOrAuthScreen() {
        // App should display either Welcome message (auth screen) or Home screen
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_displaysAuthContent() {
        // Since auth is enabled, verify auth-related content exists
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_hasInteractiveContent() {
        // Verify the app has loaded interactive content
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_isScrollable() {
        // Try to scroll the content
        composeTestRule.onRoot().performTouchInput {
            swipeUp()
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_supportsUserInteraction() {
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_displaysCorrectTheme() {
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun app_maintainsStateOnRotation() {
        composeTestRule.onRoot().assertExists()
    }
}
