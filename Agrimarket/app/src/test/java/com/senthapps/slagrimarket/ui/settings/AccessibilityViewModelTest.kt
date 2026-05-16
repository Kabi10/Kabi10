package com.senthapps.slagrimarket.ui.settings

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.preferences.AccessibilityPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccessibilityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var prefs: AccessibilityPreferences

    private val largeTextFlow = MutableStateFlow(false)
    private val textScaleFlow = MutableStateFlow(1.0f)
    private val highContrastFlow = MutableStateFlow(false)
    private val fieldModeFlow = MutableStateFlow(false)

    private lateinit var viewModel: AccessibilityViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        prefs = mockk(relaxed = true)
        every { prefs.isLargeTextEnabled() } returns largeTextFlow
        every { prefs.getTextScale() } returns textScaleFlow
        every { prefs.isHighContrastEnabled() } returns highContrastFlow
        every { prefs.isFieldModeEnabled() } returns fieldModeFlow
        viewModel = AccessibilityViewModel(prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isLargeTextEnabled initial value is false`() = runTest {
        viewModel.isLargeTextEnabled.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `textScale initial value is 1_0f`() = runTest {
        viewModel.textScale.test {
            assertEquals(1.0f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isHighContrastEnabled initial value is false`() = runTest {
        viewModel.isHighContrastEnabled.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFieldModeEnabled initial value is false`() = runTest {
        viewModel.isFieldModeEnabled.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isLargeTextEnabled updates when flow emits true`() = runTest {
        viewModel.isLargeTextEnabled.test {
            assertFalse(awaitItem()) // initial
            largeTextFlow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `textScale updates when flow emits larger value`() = runTest {
        viewModel.textScale.test {
            assertEquals(1.0f, awaitItem()) // initial
            textScaleFlow.value = 1.35f
            assertEquals(1.35f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isHighContrastEnabled updates when flow emits true`() = runTest {
        viewModel.isHighContrastEnabled.test {
            assertFalse(awaitItem()) // initial
            highContrastFlow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFieldModeEnabled updates when flow emits true`() = runTest {
        viewModel.isFieldModeEnabled.test {
            assertFalse(awaitItem()) // initial
            fieldModeFlow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleLargeText true calls setLargeTextEnabled with true`() = runTest {
        viewModel.toggleLargeText(true)
        advanceUntilIdle()
        coVerify { prefs.setLargeTextEnabled(true) }
    }

    @Test
    fun `toggleLargeText false calls setLargeTextEnabled with false`() = runTest {
        viewModel.toggleLargeText(false)
        advanceUntilIdle()
        coVerify { prefs.setLargeTextEnabled(false) }
    }

    @Test
    fun `toggleHighContrast true calls setHighContrastEnabled with true`() = runTest {
        viewModel.toggleHighContrast(true)
        advanceUntilIdle()
        coVerify { prefs.setHighContrastEnabled(true) }
    }

    @Test
    fun `toggleHighContrast false calls setHighContrastEnabled with false`() = runTest {
        viewModel.toggleHighContrast(false)
        advanceUntilIdle()
        coVerify { prefs.setHighContrastEnabled(false) }
    }

    @Test
    fun `toggleFieldMode true calls setFieldModeEnabled with true`() = runTest {
        viewModel.toggleFieldMode(true)
        advanceUntilIdle()
        coVerify { prefs.setFieldModeEnabled(true) }
    }

    @Test
    fun `toggleFieldMode false calls setFieldModeEnabled with false`() = runTest {
        viewModel.toggleFieldMode(false)
        advanceUntilIdle()
        coVerify { prefs.setFieldModeEnabled(false) }
    }

    @Test
    fun `field mode toggle does not affect large text state`() = runTest {
        viewModel.isLargeTextEnabled.test {
            assertFalse(awaitItem()) // initial
            largeTextFlow.value = true
            viewModel.toggleFieldMode(true)
            advanceUntilIdle()
            assertTrue(awaitItem()) // large text still true
            cancelAndIgnoreRemainingEvents()
        }
    }
}
