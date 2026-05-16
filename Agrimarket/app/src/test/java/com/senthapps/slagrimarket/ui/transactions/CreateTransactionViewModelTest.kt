package com.senthapps.slagrimarket.ui.transactions

import android.content.Context
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.PaymentStatus
import com.senthapps.slagrimarket.data.model.PaymentMethod
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.Resource
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import io.mockk.*
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
class CreateTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var listingRepository: ListingRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context
    private lateinit var viewModel: CreateTransactionViewModel

    private val mockUser = User(
        id = "buyer1",
        name = "Test Buyer",
        phone = "+94771234567",
        userType = UserType.BUYER,
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

    private val mockTransaction = Transaction(
        id = "txn1",
        listingId = "listing1",
        farmerId = "farmer1",
        buyerId = "buyer1",
        quantity = 10.0,
        pricePerUnit = 50.0,
        totalAmount = 500.0,
        status = TransactionStatus.PENDING,
        paymentStatus = PaymentStatus.PENDING,
        paymentMethod = PaymentMethod.CASH,
        pickupLocation = "Jaffna Market",
        pickupDate = "2025-11-25",
        createdAt = "2025-11-20T10:00:00Z",
        updatedAt = "2025-11-20T10:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        listingRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock context string resources
        every { context.getString(any()) } returns "Error message"

        viewModel = CreateTransactionViewModel(
            transactionRepository,
            listingRepository,
            authRepository,
            context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==========================================================================
    // INITIAL STATE TESTS
    // ==========================================================================

    @Test
    fun `initial state should have empty fields and no listing`() = runTest {
        val state = viewModel.uiState.value

        assertNull(state.listing)
        assertEquals("", state.quantity)
        assertEquals("", state.pickupLocation)
        assertEquals("", state.pickupDate)
        assertEquals("", state.buyerContact)
        assertEquals("", state.notes)
        assertEquals(0.0, state.totalAmount, 0.01)
        assertFalse(state.isFormValid)
        assertFalse(state.isLoading)
        assertFalse(state.isCreating)
        assertFalse(state.isTransactionCreated)
        assertNull(state.error)
    }

    // ==========================================================================
    // LOAD LISTING TESTS
    // ==========================================================================

    @Test
    fun `loadListing should update state with listing on success`() = runTest {
        coEvery { listingRepository.getListingById("listing1") } returns flowOf(
            Resource.Success(mockListing)
        )

        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.listing)
        assertEquals("Tomatoes", state.listing?.cropType)
        assertEquals(100.0, state.listing?.quantity ?: 0.0, 0.01)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadListing should set error on failure`() = runTest {
        coEvery { listingRepository.getListingById("invalid") } returns flowOf(
            Resource.Error("Listing not found")
        )

        viewModel.loadListing("invalid")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.listing)
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("not found"))
    }

    // ==========================================================================
    // FIELD UPDATE TESTS
    // ==========================================================================

    @Test
    fun `updateQuantity should update state and calculate total`() = runTest {
        // First load a listing
        coEvery { listingRepository.getListingById("listing1") } returns flowOf(
            Resource.Success(mockListing)
        )
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Update quantity
        viewModel.updateQuantity("10")

        val state = viewModel.uiState.value
        assertEquals("10", state.quantity)
        // 10 * 50 (pricePerUnit) = 500
        assertEquals(500.0, state.totalAmount, 0.01)
    }

    @Test
    fun `updateQuantity with invalid value should set total to zero`() = runTest {
        viewModel.updateQuantity("abc")

        val state = viewModel.uiState.value
        assertEquals("abc", state.quantity)
        assertEquals(0.0, state.totalAmount, 0.01)
    }

    @Test
    fun `updatePickupLocation should update state`() = runTest {
        viewModel.updatePickupLocation("Jaffna Market")

        val state = viewModel.uiState.value
        assertEquals("Jaffna Market", state.pickupLocation)
    }

    @Test
    fun `updatePickupDate should update state`() = runTest {
        viewModel.updatePickupDate("2025-11-25")

        val state = viewModel.uiState.value
        assertEquals("2025-11-25", state.pickupDate)
    }

    @Test
    fun `updateBuyerContact should update state`() = runTest {
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertEquals("+94771234567", state.buyerContact)
    }

    @Test
    fun `updateNotes should update state`() = runTest {
        viewModel.updateNotes("Please deliver fresh")

        val state = viewModel.uiState.value
        assertEquals("Please deliver fresh", state.notes)
    }

    // ==========================================================================
    // FORM VALIDATION TESTS
    // ==========================================================================

    @Test
    fun `form should be invalid when no listing is loaded`() = runTest {
        fillValidForm()

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when quantity is empty`() = runTest {
        loadMockListing()
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when quantity exceeds available`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("200") // listing has 100
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when pickup location is empty`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when pickup date is empty`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when pickup date format is wrong`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("25-11-2025") // Wrong format
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when buyer contact is empty`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when phone number is too short`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("12345") // Too short

        val state = viewModel.uiState.value
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be valid when all fields are correctly filled`() = runTest {
        loadMockListing()
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")

        val state = viewModel.uiState.value
        assertTrue(state.isFormValid)
    }

    // ==========================================================================
    // CREATE TRANSACTION TESTS
    // ==========================================================================

    @Test
    fun `createTransaction should fail when form is invalid`() = runTest {
        // Don't fill form
        viewModel.createTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isTransactionCreated)
        assertNotNull(state.error)
    }

    @Test
    fun `createTransaction should fail when user is not authenticated`() = runTest {
        loadMockListing()
        fillValidFormWithListing()
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.createTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreating)
        assertFalse(state.isTransactionCreated)
        assertNotNull(state.error)
    }

    @Test
    fun `createTransaction should succeed when form is valid and user is authenticated`() = runTest {
        loadMockListing()
        fillValidFormWithListing()
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery {
            transactionRepository.createTransaction(
                listingId = any(),
                farmerId = any(),
                buyerId = any(),
                quantity = any(),
                totalAmount = any(),
                pickupLocation = any(),
                pickupDate = any(),
                buyerContact = any(),
                notes = any()
            )
        } returns Result.success(mockTransaction)

        viewModel.createTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreating)
        assertTrue(state.isTransactionCreated)
        assertNull(state.error)
    }

    @Test
    fun `createTransaction should handle repository failure`() = runTest {
        loadMockListing()
        fillValidFormWithListing()
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery {
            transactionRepository.createTransaction(
                listingId = any(),
                farmerId = any(),
                buyerId = any(),
                quantity = any(),
                totalAmount = any(),
                pickupLocation = any(),
                pickupDate = any(),
                buyerContact = any(),
                notes = any()
            )
        } returns Result.failure(RuntimeException("Network error"))

        viewModel.createTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreating)
        assertFalse(state.isTransactionCreated)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }

    // ==========================================================================
    // UTILITY TESTS
    // ==========================================================================

    @Test
    fun `clearError should reset error state`() = runTest {
        // Trigger an error first
        viewModel.createTransaction()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // Clear error
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `total amount should be calculated correctly`() = runTest {
        loadMockListing()

        // 25 * 50 (pricePerUnit) = 1250
        viewModel.updateQuantity("25")

        val state = viewModel.uiState.value
        assertEquals(1250.0, state.totalAmount, 0.01)
    }

    @Test
    fun `total amount should update when quantity changes`() = runTest {
        loadMockListing()

        viewModel.updateQuantity("10")
        assertEquals(500.0, viewModel.uiState.value.totalAmount, 0.01)

        viewModel.updateQuantity("20")
        assertEquals(1000.0, viewModel.uiState.value.totalAmount, 0.01)
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    private suspend fun loadMockListing() {
        coEvery { listingRepository.getListingById("listing1") } returns flowOf(
            Resource.Success(mockListing)
        )
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun fillValidForm() {
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")
    }

    private fun fillValidFormWithListing() {
        viewModel.updateQuantity("10")
        viewModel.updatePickupLocation("Jaffna Market")
        viewModel.updatePickupDate("2025-11-25")
        viewModel.updateBuyerContact("+94771234567")
    }
}
