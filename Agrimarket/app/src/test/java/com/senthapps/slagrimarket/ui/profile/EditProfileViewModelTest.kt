package com.senthapps.slagrimarket.ui.profile

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
class EditProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AuthRepository
    private lateinit var context: Context
    private lateinit var viewModel: EditProfileViewModel

    private val mockUser = User(
        id = "user1",
        name = "Ravi Kumar",
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
        every { context.getString(any()) } returns "Error message"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = EditProfileViewModel(repository, context)

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun `initial state is not loading and fields are empty`() = runTest {
        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("", state.name)
        assertEquals("", state.location)
        assertNull(state.error)
        assertNull(state.nameError)
        assertNull(state.locationError)
    }

    // ── loadUserProfile ────────────────────────────────────────────────────────

    @Test
    fun `loadUserProfile populates name and phoneNumber on success`() = runTest {
        coEvery { repository.getCurrentUser() } returns mockUser
        viewModel = createViewModel()

        viewModel.loadUserProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Ravi Kumar", state.name)
        assertEquals("+94771234567", state.phoneNumber)
        assertEquals(UserType.FARMER, state.userType)
        assertNull(state.error)
    }

    @Test
    fun `loadUserProfile sets error when user is null`() = runTest {
        coEvery { repository.getCurrentUser() } returns null
        viewModel = createViewModel()

        viewModel.loadUserProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadUserProfile sets error on exception`() = runTest {
        coEvery { repository.getCurrentUser() } throws RuntimeException("DB error")
        viewModel = createViewModel()

        viewModel.loadUserProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("DB error"))
    }

    // ── Field updates ──────────────────────────────────────────────────────────

    @Test
    fun `updateName clears nameError`() = runTest {
        viewModel = createViewModel()
        viewModel.saveProfile() // triggers validation → sets nameError
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.nameError)

        viewModel.updateName("New Name")

        assertNull(viewModel.uiState.value.nameError)
        assertEquals("New Name", viewModel.uiState.value.name)
    }

    @Test
    fun `updateLocation clears locationError`() = runTest {
        viewModel = createViewModel()
        viewModel.updateName("Valid Name")
        viewModel.saveProfile() // triggers validation → sets locationError
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.locationError)

        viewModel.updateLocation("Colombo")

        assertNull(viewModel.uiState.value.locationError)
        assertEquals("Colombo", viewModel.uiState.value.location)
    }

    // ── isFormValid ────────────────────────────────────────────────────────────

    @Test
    fun `isFormValid returns false when name is blank`() = runTest {
        viewModel = createViewModel()
        viewModel.updateLocation("Colombo")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid returns false when location is blank`() = runTest {
        viewModel = createViewModel()
        viewModel.updateName("Ravi")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid returns true when both fields are filled`() = runTest {
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")
        viewModel.updateLocation("Colombo")

        assertTrue(viewModel.isFormValid())
    }

    // ── saveProfile validation ─────────────────────────────────────────────────

    @Test
    fun `saveProfile sets nameError when name is blank`() = runTest {
        viewModel = createViewModel()

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `saveProfile sets nameError when name is too short`() = runTest {
        viewModel = createViewModel()
        viewModel.updateName("A")
        viewModel.updateLocation("Colombo")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `saveProfile sets locationError when location is blank`() = runTest {
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.locationError)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    // ── saveProfile success / failure ──────────────────────────────────────────

    @Test
    fun `saveProfile sets isSuccess on repository success`() = runTest {
        coEvery { repository.getCurrentUser() } returns mockUser
        coEvery { repository.updateUserProfile(any(), any(), any()) } returns Result.success(mockUser)
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")
        viewModel.updateLocation("Colombo")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `saveProfile sets error on repository failure`() = runTest {
        coEvery { repository.getCurrentUser() } returns mockUser
        coEvery { repository.updateUserProfile(any(), any(), any()) } returns Result.failure(Exception("Server error"))
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")
        viewModel.updateLocation("Colombo")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Server error"))
    }

    @Test
    fun `saveProfile sets error when getCurrentUser returns null`() = runTest {
        coEvery { repository.getCurrentUser() } returns null
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")
        viewModel.updateLocation("Colombo")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
    }

    @Test
    fun `saveProfile sets error on unexpected exception`() = runTest {
        coEvery { repository.getCurrentUser() } throws RuntimeException("Unexpected")
        viewModel = createViewModel()
        viewModel.updateName("Ravi Kumar")
        viewModel.updateLocation("Colombo")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Unexpected"))
    }

    @Test
    fun `saveProfile calls repository with correct userId name and location`() = runTest {
        coEvery { repository.getCurrentUser() } returns mockUser
        coEvery { repository.updateUserProfile(any(), any(), any()) } returns Result.success(mockUser)
        viewModel = createViewModel()
        viewModel.updateName("New Name")
        viewModel.updateLocation("Kandy")

        viewModel.saveProfile()
        advanceUntilIdle()

        coVerify { repository.updateUserProfile("user1", "New Name", "Kandy") }
    }
}
