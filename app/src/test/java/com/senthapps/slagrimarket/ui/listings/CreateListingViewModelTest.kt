package com.senthapps.slagrimarket.ui.listings

import android.content.Context
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateListingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var listingRepository: ListingRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context
    private lateinit var viewModel: CreateListingViewModel

    private val mockUser = User(
        id = "farmer1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    private val mockListing = Listing(
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
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        listingRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock context string resources
        every { context.getString(any()) } returns "Error message"

        viewModel = CreateListingViewModel(listingRepository, authRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==========================================================================
    // INITIAL STATE TESTS
    // ==========================================================================

    @Test
    fun `initial state should have empty fields and no errors`() = runTest {
        val state = viewModel.uiState.value

        assertEquals("", state.cropType)
        assertEquals("", state.quantity)
        assertEquals("", state.unit)
        assertEquals("", state.pricePerUnit)
        assertEquals("", state.quality)
        assertEquals("", state.harvestDate)
        assertEquals("", state.location)
        assertTrue(state.images.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.cropTypeError)
        assertNull(state.quantityError)
    }

    // ==========================================================================
    // FIELD UPDATE TESTS
    // ==========================================================================

    @Test
    fun `updateCropType should update state and clear error`() = runTest {
        // Given: Initial state with error
        viewModel.updateCropType("")  // Set to empty first

        // When: Update crop type
        viewModel.updateCropType("Tomatoes")

        // Then: State should be updated
        val state = viewModel.uiState.value
        assertEquals("Tomatoes", state.cropType)
        assertNull(state.cropTypeError)
    }

    @Test
    fun `updateQuantity should update state and clear error`() = runTest {
        viewModel.updateQuantity("100")

        val state = viewModel.uiState.value
        assertEquals("100", state.quantity)
        assertNull(state.quantityError)
    }

    @Test
    fun `updateUnit should update state and clear error`() = runTest {
        viewModel.updateUnit("kg")

        val state = viewModel.uiState.value
        assertEquals("kg", state.unit)
        assertNull(state.unitError)
    }

    @Test
    fun `updatePricePerUnit should update state and clear error`() = runTest {
        viewModel.updatePricePerUnit("50.00")

        val state = viewModel.uiState.value
        assertEquals("50.00", state.pricePerUnit)
        assertNull(state.priceError)
    }

    @Test
    fun `updateQuality should update state and clear error`() = runTest {
        viewModel.updateQuality("A")

        val state = viewModel.uiState.value
        assertEquals("A", state.quality)
        assertNull(state.qualityError)
    }

    @Test
    fun `updateHarvestDate should update state and clear error`() = runTest {
        viewModel.updateHarvestDate("2025-11-20")

        val state = viewModel.uiState.value
        assertEquals("2025-11-20", state.harvestDate)
        assertNull(state.harvestDateError)
    }

    @Test
    fun `updateLocation should update state and clear error`() = runTest {
        viewModel.updateLocation("Jaffna")

        val state = viewModel.uiState.value
        assertEquals("Jaffna", state.location)
        assertNull(state.locationError)
    }

    @Test
    fun `updateStory should update state`() = runTest {
        viewModel.updateStory("This is my farm story")

        val state = viewModel.uiState.value
        assertEquals("This is my farm story", state.story)
    }

    // ==========================================================================
    // FARMING METHODS AND SUSTAINABILITY TESTS
    // ==========================================================================

    @Test
    fun `toggleFarmingMethod should add method when not present`() = runTest {
        viewModel.toggleFarmingMethod("Organic")

        val state = viewModel.uiState.value
        assertTrue(state.farmingMethods.contains("Organic"))
    }

    @Test
    fun `toggleFarmingMethod should remove method when present`() = runTest {
        // Add first
        viewModel.toggleFarmingMethod("Organic")
        assertTrue(viewModel.uiState.value.farmingMethods.contains("Organic"))

        // Remove
        viewModel.toggleFarmingMethod("Organic")
        assertFalse(viewModel.uiState.value.farmingMethods.contains("Organic"))
    }

    @Test
    fun `toggleSustainabilityPractice should add practice when not present`() = runTest {
        viewModel.toggleSustainabilityPractice("No Pesticides")

        val state = viewModel.uiState.value
        assertTrue(state.sustainabilityPractices.contains("No Pesticides"))
    }

    @Test
    fun `toggleSustainabilityPractice should remove practice when present`() = runTest {
        // Add first
        viewModel.toggleSustainabilityPractice("No Pesticides")
        assertTrue(viewModel.uiState.value.sustainabilityPractices.contains("No Pesticides"))

        // Remove
        viewModel.toggleSustainabilityPractice("No Pesticides")
        assertFalse(viewModel.uiState.value.sustainabilityPractices.contains("No Pesticides"))
    }

    // ==========================================================================
    // FORM VALIDATION TESTS
    // ==========================================================================

    @Test
    fun `isFormValid should return false when all fields are empty`() = runTest {
        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when crop type is empty`() = runTest {
        fillValidForm()
        viewModel.updateCropType("")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when quantity is empty`() = runTest {
        fillValidForm()
        viewModel.updateQuantity("")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when quantity is invalid number`() = runTest {
        fillValidForm()
        viewModel.updateQuantity("abc")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when quantity is zero`() = runTest {
        fillValidForm()
        viewModel.updateQuantity("0")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when quantity is negative`() = runTest {
        fillValidForm()
        viewModel.updateQuantity("-10")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when quantity exceeds max`() = runTest {
        fillValidForm()
        viewModel.updateQuantity("100001")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when price is invalid`() = runTest {
        fillValidForm()
        viewModel.updatePricePerUnit("abc")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when price is zero`() = runTest {
        fillValidForm()
        viewModel.updatePricePerUnit("0")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when price exceeds max`() = runTest {
        fillValidForm()
        viewModel.updatePricePerUnit("1000001")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return true when all fields are valid`() = runTest {
        fillValidForm()

        assertTrue(viewModel.isFormValid())
    }

    // ==========================================================================
    // CREATE LISTING TESTS
    // ==========================================================================

    @Test
    fun `createListing should fail when user is not authenticated`() = runTest {
        fillValidForm()
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.createListing()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("User not authenticated", state.error)
    }

    @Test
    fun `createListing should succeed when form is valid and user is authenticated`() = runTest {
        fillValidForm()
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery {
            listingRepository.createListing(
                cropType = any(),
                quantity = any(),
                unit = any(),
                pricePerUnit = any(),
                quality = any(),
                harvestDate = any(),
                location = any(),
                farmerId = any(),
                imageUrls = any(),
                story = any(),
                farmingMethods = any(),
                harvestedAt = any(),
                sustainabilityPractices = any()
            )
        } returns Result.success(mockListing)

        viewModel.createListing()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `createListing should handle repository failure`() = runTest {
        fillValidForm()
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery {
            listingRepository.createListing(
                cropType = any(),
                quantity = any(),
                unit = any(),
                pricePerUnit = any(),
                quality = any(),
                harvestDate = any(),
                location = any(),
                farmerId = any(),
                imageUrls = any(),
                story = any(),
                farmingMethods = any(),
                harvestedAt = any(),
                sustainabilityPractices = any()
            )
        } returns Result.failure(RuntimeException("Network error"))

        viewModel.createListing()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }

    // ==========================================================================
    // UTILITY TESTS
    // ==========================================================================

    @Test
    fun `clearError should reset error state`() = runTest {
        // Set an error first
        fillValidForm()
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel.createListing()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // Clear error
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetForm should reset all fields to initial state`() = runTest {
        fillValidForm()
        viewModel.toggleFarmingMethod("Organic")

        viewModel.resetForm()

        val state = viewModel.uiState.value
        assertEquals("", state.cropType)
        assertEquals("", state.quantity)
        assertEquals("", state.unit)
        assertEquals("", state.pricePerUnit)
        assertEquals("", state.quality)
        assertEquals("", state.harvestDate)
        assertEquals("", state.location)
        assertTrue(state.farmingMethods.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `getAvailableCropTypes should return non-empty list`() {
        val cropTypes = viewModel.getAvailableCropTypes()
        assertTrue(cropTypes.isNotEmpty())
    }

    @Test
    fun `getAvailableUnits should return non-empty list`() {
        val units = viewModel.getAvailableUnits()
        assertTrue(units.isNotEmpty())
    }

    @Test
    fun `getAvailableQualityGrades should return non-empty list`() {
        val grades = viewModel.getAvailableQualityGrades()
        assertTrue(grades.isNotEmpty())
    }

    @Test
    fun `getTodayDate should return valid date format`() {
        val date = viewModel.getTodayDate()
        // ISO format: YYYY-MM-DD
        assertTrue(date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    private fun fillValidForm() {
        viewModel.updateCropType("Tomatoes")
        viewModel.updateQuantity("100")
        viewModel.updateUnit("kg")
        viewModel.updatePricePerUnit("50")
        viewModel.updateQuality("A")
        viewModel.updateHarvestDate("2025-11-20")
        viewModel.updateLocation("Jaffna")
    }
}
