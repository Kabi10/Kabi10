package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.AuthApiService
import com.senthapps.slagrimarket.data.api.SendOtpResponse
import com.senthapps.slagrimarket.data.api.VerifyOtpResponse
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.preferences.AuthPreferences
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var authApiService: AuthApiService
    private lateinit var authPreferences: AuthPreferences
    private lateinit var userDao: UserDao
    private lateinit var repository: AuthRepository

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
        authApiService = mockk(relaxed = true)
        userDao = mockk(relaxed = true)

        // Use relaxed mock for AuthPreferences with relaxUnitFun
        authPreferences = mockk(relaxed = true, relaxUnitFun = true)

        repository = AuthRepository(authApiService, authPreferences, userDao)
    }

    @Test
    fun `sendOtp should return success with otpId on successful API call`() = runTest {
        // Given: API returns success (otp is null to avoid android.util.Log call in unit tests)
        val response = SendOtpResponse(success = true, otpId = "otp123", otp = null, message = "OTP sent")
        coEvery { authApiService.sendOtp(any()) } returns Response.success(response)

        // When: sendOtp is called
        val result = repository.sendOtp("+94771234567")

        // Then: Should return success with otpId
        assertTrue(result.isSuccess)
        assertEquals("otp123", result.getOrNull())
    }

    @Test
    fun `sendOtp should return failure on API error`() = runTest {
        // Given: API returns failure
        val response = SendOtpResponse(success = false, message = "Invalid phone number")
        coEvery { authApiService.sendOtp(any()) } returns Response.success(response)

        // When: sendOtp is called
        val result = repository.sendOtp("+94771234567")

        // Then: Should return failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid phone number") == true)
    }

    @Test
    fun `sendOtp should return failure on network exception`() = runTest {
        // Given: API throws exception
        coEvery { authApiService.sendOtp(any()) } throws RuntimeException("Network error")

        // When: sendOtp is called
        val result = repository.sendOtp("+94771234567")

        // Then: Should return failure
        assertTrue(result.isFailure)
    }

    @Test
    fun `verifyOtp should return success and save tokens on successful verification`() = runTest {
        // Given: API returns success with user
        val response = VerifyOtpResponse(
            success = true,
            token = "access_token",
            refreshToken = "refresh_token",
            user = mockUser,
            message = "Verified"
        )
        coEvery { authApiService.verifyOtp(any()) } returns Response.success(response)
        coEvery { authPreferences.saveTokens(any(), any()) } just Runs
        coEvery { authPreferences.saveUser(any()) } just Runs
        coEvery { userDao.insertUser(any()) } just Runs

        // When: verifyOtp is called
        val result = repository.verifyOtp("+94771234567", "123456", "otp123")

        // Then: Should return success and save tokens
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        coVerify { authPreferences.saveTokens("access_token", "refresh_token") }
        coVerify { authPreferences.saveUser(mockUser) }
        coVerify { userDao.insertUser(mockUser) }
    }

    @Test
    fun `verifyOtp should return failure on invalid OTP`() = runTest {
        // Given: API returns failure
        val response = VerifyOtpResponse(success = false, message = "Invalid OTP")
        coEvery { authApiService.verifyOtp(any()) } returns Response.success(response)

        // When: verifyOtp is called
        val result = repository.verifyOtp("+94771234567", "000000", "otp123")

        // Then: Should return failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid OTP") == true)
    }

    @Test
    fun `logout should clear auth preferences`() = runTest {
        // Given: Auth preferences can be cleared
        coEvery { authPreferences.clearAuth() } just Runs

        // When: logout is called
        repository.logout()

        // Then: Should clear auth
        coVerify { authPreferences.clearAuth() }
    }

    @Test
    fun `getCurrentUser should return user from preferences`() = runTest {
        // Given: Preferences has user
        every { authPreferences.getCurrentUser() } returns mockUser

        // When: getCurrentUser is called
        val result = repository.getCurrentUser()

        // Then: Should return user
        assertEquals(mockUser, result)
    }

    @Test
    fun `getCurrentUser should return null when not logged in`() = runTest {
        // Given: No user in preferences
        every { authPreferences.getCurrentUser() } returns null

        // When: getCurrentUser is called
        val result = repository.getCurrentUser()

        // Then: Should return null
        assertNull(result)
    }

    @Test
    fun `isUserLoggedIn should return true when token exists`() = runTest {
        // Given: Token exists
        coEvery { authPreferences.getAccessToken() } returns "valid_token"

        // When: isUserLoggedIn is called
        val result = repository.isUserLoggedIn()

        // Then: Should return true
        assertTrue(result)
    }

    @Test
    fun `isUserLoggedIn should return false when no token`() = runTest {
        // Given: No token
        coEvery { authPreferences.getAccessToken() } returns null

        // When: isUserLoggedIn is called
        val result = repository.isUserLoggedIn()

        // Then: Should return false
        assertFalse(result)
    }

    // Note: Tests for currentUser and isLoggedIn flows are omitted because they simply
    // delegate to authPreferences properties. Testing them requires mocking val properties
    // which is complex in MockK. The delegation is straightforward pass-through without logic.
}

