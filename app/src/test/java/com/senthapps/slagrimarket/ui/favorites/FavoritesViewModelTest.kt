package com.senthapps.slagrimarket.ui.favorites

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: FavoritesViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    private val mockListings = listOf(
        Listing(
            id = "listing1",
            farmerId = "farmer1",
            cropType = "Tomatoes",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 50.0,
            quality = QualityGrade.A,
            harvestDate = "2025-11-20",
            location = "Jaffna",
            isActive = true,
            createdAt = "2025-11-20T10:00:00Z",
            updatedAt = "2025-11-20T10:00:00Z"
        ),
        Listing(
            id = "listing2",
            farmerId = "farmer2",
            cropType = "Rice",
            quantity = 500.0,
            unit = "kg",
            pricePerUnit = 80.0,
            quality = QualityGrade.B,
            harvestDate = "2025-11-19",
            location = "Colombo",
            isActive = true,
            createdAt = "2025-11-19T10:00:00Z",
            updatedAt = "2025-11-19T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        favoriteRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFavorites should set error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = FavoritesViewModel(favoriteRepository, authRepository)
        viewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
        assertTrue(state.favorites.isEmpty())
    }

    @Test
    fun `loadFavorites should populate state with favorites`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.getFavoriteListingsForUser("user1") } returns flowOf(mockListings)

        viewModel = FavoritesViewModel(favoriteRepository, authRepository)
        viewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.favorites.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadFavorites should show empty list when no favorites`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.getFavoriteListingsForUser("user1") } returns flowOf(emptyList())

        viewModel = FavoritesViewModel(favoriteRepository, authRepository)
        viewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.favorites.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `removeFavorite should set error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = FavoritesViewModel(favoriteRepository, authRepository)

        viewModel.removeFavorite("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("User not found", viewModel.uiState.value.error)
        coVerify(exactly = 0) { favoriteRepository.toggleFavorite(any(), any()) }
    }

    @Test
    fun `removeFavorite should call toggleFavorite with correct userId and listingId`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.toggleFavorite("user1", "listing1") } returns Result.success(false)
        viewModel = FavoritesViewModel(favoriteRepository, authRepository)

        viewModel.removeFavorite("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.toggleFavorite("user1", "listing1") }
    }

    @Test
    fun `loadFavorites should set error on repository exception`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.getFavoriteListingsForUser("user1") } throws RuntimeException("DB error")

        viewModel = FavoritesViewModel(favoriteRepository, authRepository)
        viewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load favorites"))
    }

    @Test
    fun `clearAllFavorites should trigger reload by calling getCurrentUser`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.getFavoriteListingsForUser("user1") } returns flowOf(mockListings)
        viewModel = FavoritesViewModel(favoriteRepository, authRepository)

        viewModel.clearAllFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { authRepository.getCurrentUser() }
    }
}
