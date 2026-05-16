package com.senthapps.slagrimarket.ui.common

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageToggleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var languagePreferences: LanguagePreferences
    private val languageFlow = MutableStateFlow(LanguagePreferences.DEFAULT_LANGUAGE)

    private lateinit var viewModel: LanguageToggleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        languagePreferences = mockk(relaxed = true)
        every { languagePreferences.getLanguage() } returns languageFlow
        viewModel = LanguageToggleViewModel(languagePreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial language is DEFAULT_LANGUAGE before preferences emit`() = runTest {
        assertEquals(LanguagePreferences.DEFAULT_LANGUAGE, viewModel.currentLanguage.value)
    }

    @Test
    fun `currentLanguage updates when preferences flow emits new value`() = runTest {
        advanceUntilIdle() // let init coroutine run

        viewModel.currentLanguage.test {
            assertEquals(LanguagePreferences.DEFAULT_LANGUAGE, awaitItem())
            languageFlow.value = "en"
            assertEquals("en", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLanguage updates currentLanguage immediately`() = runTest {
        advanceUntilIdle()

        viewModel.setLanguage("si")

        assertEquals("si", viewModel.currentLanguage.value)
    }

    @Test
    fun `setLanguage persists via preferences`() = runTest {
        advanceUntilIdle()

        viewModel.setLanguage("en")
        advanceUntilIdle()

        coVerify { languagePreferences.saveLanguage("en") }
    }

    @Test
    fun `setLanguage to Tamil persists Tamil`() = runTest {
        advanceUntilIdle()

        viewModel.setLanguage("ta")
        advanceUntilIdle()

        coVerify { languagePreferences.saveLanguage("ta") }
    }

    @Test
    fun `setLanguage to Sinhala persists Sinhala`() = runTest {
        advanceUntilIdle()

        viewModel.setLanguage("si")
        advanceUntilIdle()

        coVerify { languagePreferences.saveLanguage("si") }
    }

    @Test
    fun `setLanguage multiple times retains last value`() = runTest {
        advanceUntilIdle()

        viewModel.setLanguage("en")
        viewModel.setLanguage("si")
        viewModel.setLanguage("ta")

        assertEquals("ta", viewModel.currentLanguage.value)
    }

    @Test
    fun `currentLanguage reflects saved preference on init`() = runTest {
        languageFlow.value = "si"
        val freshViewModel = LanguageToggleViewModel(languagePreferences)
        advanceUntilIdle()

        assertEquals("si", freshViewModel.currentLanguage.value)
    }

    @Test
    fun `setLanguage emits updated value via StateFlow`() = runTest {
        advanceUntilIdle()

        viewModel.currentLanguage.test {
            awaitItem() // current value
            viewModel.setLanguage("en")
            assertEquals("en", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preferences emit overrides initial DEFAULT_LANGUAGE`() = runTest {
        viewModel.currentLanguage.test {
            awaitItem() // DEFAULT_LANGUAGE ("ta")
            languageFlow.value = "en"
            advanceUntilIdle()
            assertEquals("en", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
