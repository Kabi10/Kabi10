package com.senthapps.slagrimarket.ui.listings

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.preferences.LastUsedPreferences
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateListingSuccessViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var lastUsedPreferences: LastUsedPreferences

    private val cropTypeFlow = MutableStateFlow("")
    private val priceFlow = MutableStateFlow("")
    private val locationFlow = MutableStateFlow("")

    private lateinit var viewModel: CreateListingSuccessViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        lastUsedPreferences = mockk(relaxed = true)
        every { lastUsedPreferences.getLastCropType() } returns cropTypeFlow
        every { lastUsedPreferences.getLastPrice() } returns priceFlow
        every { lastUsedPreferences.getLastLocation() } returns locationFlow
        viewModel = CreateListingSuccessViewModel(lastUsedPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cropType initial value is empty string`() = runTest {
        advanceUntilIdle()
        assertEquals("", viewModel.cropType.value)
    }

    @Test
    fun `price initial value is empty string`() = runTest {
        advanceUntilIdle()
        assertEquals("", viewModel.price.value)
    }

    @Test
    fun `location initial value is empty string`() = runTest {
        advanceUntilIdle()
        assertEquals("", viewModel.location.value)
    }

    @Test
    fun `cropType reflects value from preferences`() = runTest {
        cropTypeFlow.value = "Tomato"
        advanceUntilIdle()

        viewModel.cropType.test {
            assertEquals("Tomato", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `price reflects value from preferences`() = runTest {
        priceFlow.value = "150"
        advanceUntilIdle()

        viewModel.price.test {
            assertEquals("150", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `location reflects value from preferences`() = runTest {
        locationFlow.value = "Colombo"
        advanceUntilIdle()

        viewModel.location.test {
            assertEquals("Colombo", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cropType updates when preferences flow emits new value`() = runTest {
        advanceUntilIdle()

        viewModel.cropType.test {
            awaitItem() // current value
            cropTypeFlow.value = "Red Onion"
            assertEquals("Red Onion", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `price updates when preferences flow emits new value`() = runTest {
        advanceUntilIdle()

        viewModel.price.test {
            awaitItem()
            priceFlow.value = "200"
            assertEquals("200", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `location updates when preferences flow emits new value`() = runTest {
        advanceUntilIdle()

        viewModel.location.test {
            awaitItem()
            locationFlow.value = "Jaffna"
            assertEquals("Jaffna", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all three fields populated simultaneously`() = runTest {
        cropTypeFlow.value = "Chili"
        priceFlow.value = "300"
        locationFlow.value = "Kandy"
        advanceUntilIdle()

        assertEquals("Chili", viewModel.cropType.value)
        assertEquals("300", viewModel.price.value)
        assertEquals("Kandy", viewModel.location.value)
    }
}
