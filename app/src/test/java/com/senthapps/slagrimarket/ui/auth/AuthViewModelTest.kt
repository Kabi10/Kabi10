package com.senthapps.slagrimarket.ui.auth

import android.content.Context
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AuthRepository
    private lateinit var context: Context
    private lateinit var viewModel: AuthViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        context = mockk(relaxed = true)
        
        // Setup default mock behaviors
        every { repository.isLoggedIn } returns flowOf(false)
        every { repository.currentUser } returns flowOf(null)
        every { context.getString(any()) } returns "Error message"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(repository, context)
    }

    @Test
    fun `initial state should not be loading and not authenticated`() = runTest {
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertFalse(state.otpSent)
        assertNull(state.error)
        assertNull(state.user)
    }

    @Test
    fun `sendOtp should set loading state and then success state`() = runTest {
        // Given: Repository returns success
        coEvery { repository.sendOtp("+94771234567") } returns Result.success(AuthRepository.OtpResult("otp123", null))
        viewModel = createViewModel()

        // When: sendOtp is called
        viewModel.sendOtp("+94771234567")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should show OTP sent
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.otpSent)
        assertEquals("otp123", state.otpId)
        assertEquals("+94771234567", state.phoneNumber)
        assertNull(state.error)
    }

    @Test
    fun `sendOtp should set error state on failure`() = runTest {
        // Given: Repository returns failure
        coEvery { repository.sendOtp("+94771234567") } returns Result.failure(Exception("Network error"))
        viewModel = createViewModel()

        // When: sendOtp is called
        viewModel.sendOtp("+94771234567")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have error
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.otpSent)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }

    @Test
    fun `verifyOtp should set authenticated state on success`() = runTest {
        // Given: Repository returns success with user
        coEvery { repository.verifyOtp("+94771234567", "123456", "otp123") } returns Result.success(mockUser)
        viewModel = createViewModel()

        // When: verifyOtp is called
        viewModel.verifyOtp("+94771234567", "123456", "otp123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should be authenticated
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals(mockUser, state.user)
        assertNull(state.error)
    }

    @Test
    fun `verifyOtp should set error state on failure`() = runTest {
        // Given: Repository returns failure
        coEvery { repository.verifyOtp(any(), any(), any()) } returns Result.failure(Exception("Invalid OTP"))
        viewModel = createViewModel()

        // When: verifyOtp is called
        viewModel.verifyOtp("+94771234567", "000000", "otp123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have error
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertNotNull(state.error)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given: ViewModel with error
        coEvery { repository.sendOtp(any()) } returns Result.failure(Exception("Error"))
        viewModel = createViewModel()
        viewModel.sendOtp("+94771234567")
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When: clearError is called
        viewModel.clearError()

        // Then: Error should be null
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetOtpState should reset OTP-related state`() = runTest {
        // Given: ViewModel with OTP sent
        coEvery { repository.sendOtp(any()) } returns Result.success(AuthRepository.OtpResult("otp123", null))
        viewModel = createViewModel()
        viewModel.sendOtp("+94771234567")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.otpSent)

        // When: resetOtpState is called
        viewModel.resetOtpState()

        // Then: OTP state should be reset
        val state = viewModel.uiState.value
        assertFalse(state.otpSent)
        assertNull(state.otpId)
        assertNull(state.error)
    }

    @Test
    fun `logout should reset state and call repository logout`() = runTest {
        // Given: Authenticated user
        coEvery { repository.verifyOtp(any(), any(), any()) } returns Result.success(mockUser)
        viewModel = createViewModel()
        viewModel.verifyOtp("+94771234567", "123456", "otp123")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAuthenticated)

        // When: logout is called
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should be reset
        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertNull(state.user)
        coVerify { repository.logout() }
    }
}

