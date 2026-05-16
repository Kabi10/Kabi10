package com.senthapps.slagrimarket.ui.transactions

import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
class TransactionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: TransactionsViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    private val mockTransactions = listOf(
        Transaction(
            id = "tx1",
            listingId = "listing1",
            farmerId = "user1",
            buyerId = "buyer1",
            quantity = 50.0,
            unit = "kg",
            pricePerUnit = 50.0,
            totalAmount = 2500.0,
            pickupLocation = "Jaffna Central Market",
            pickupDate = "2025-11-25",
            status = TransactionStatus.PENDING,
            createdAt = "2025-11-20T10:00:00Z",
            updatedAt = "2025-11-20T10:00:00Z"
        ),
        Transaction(
            id = "tx2",
            listingId = "listing2",
            farmerId = "user1",
            buyerId = "buyer2",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 80.0,
            totalAmount = 8000.0,
            pickupLocation = "Chavakachcheri Market",
            pickupDate = "2025-11-24",
            status = TransactionStatus.COMPLETED,
            createdAt = "2025-11-19T10:00:00Z",
            updatedAt = "2025-11-19T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        
        every { authRepository.currentUser } returns flowOf(mockUser)
        coEvery { authRepository.getCurrentUser() } returns mockUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTransactions should emit success state with data`() = runTest {
        // Given: Repository returns transactions
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)

        // When: ViewModel is created
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have transactions
        val state = viewModel.uiState.value
        assertEquals(2, state.transactions.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadTransactions should emit error when user not authenticated`() = runTest {
        // Given: No authenticated user
        coEvery { authRepository.getCurrentUser() } returns null

        // When: ViewModel is created
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have error
        val state = viewModel.uiState.value
        assertTrue(state.transactions.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("User not authenticated", state.error)
    }

    @Test
    fun `filterTransactionsByStatus should update selected status`() = runTest {
        // Given: ViewModel with transactions
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: Filter is applied
        viewModel.filterTransactionsByStatus(TransactionStatus.PENDING)

        // Then: Selected status should be updated
        assertEquals(TransactionStatus.PENDING, viewModel.uiState.value.selectedStatus)
    }

    @Test
    fun `getFilteredTransactions should return filtered list`() = runTest {
        // Given: ViewModel with transactions
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: Filter is applied
        viewModel.filterTransactionsByStatus(TransactionStatus.PENDING)

        // Then: Filtered list should only contain pending transactions
        val filtered = viewModel.getFilteredTransactions()
        assertEquals(1, filtered.size)
        assertEquals(TransactionStatus.PENDING, filtered[0].status)
    }

    @Test
    fun `getFilteredTransactions should return all when no filter`() = runTest {
        // Given: ViewModel with transactions and no filter
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: No filter is applied
        viewModel.filterTransactionsByStatus(null)

        // Then: All transactions should be returned
        val filtered = viewModel.getFilteredTransactions()
        assertEquals(2, filtered.size)
    }

    @Test
    fun `updateTransactionStatus should call repository and reload`() = runTest {
        // Given: ViewModel with transactions
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        coEvery { transactionRepository.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED) } returns Result.success(mockTransactions[0].copy(status = TransactionStatus.CONFIRMED))
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: Status is updated
        viewModel.confirmTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Repository should be called
        coVerify { transactionRepository.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED) }
    }

    @Test
    fun `canUpdateStatus should return true for farmer with pending transaction`() = runTest {
        // Given: ViewModel
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When/Then: Farmer can update pending transaction
        val pendingTx = mockTransactions[0]
        assertTrue(viewModel.canUpdateStatus(pendingTx, UserType.FARMER))
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `updateTransactionStatus sets error on repository failure`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        coEvery { transactionRepository.updateTransactionStatus(any(), any()) } returns Result.failure(RuntimeException("server error"))
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("server error"))
    }

    @Test
    fun `loadTransactions sets error when flow emits exception`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flow {
            throw RuntimeException("DB crashed")
        }
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `canUpdateStatus returns true for farmer with in_progress transaction`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val inProgressTx = mockTransactions[0].copy(status = TransactionStatus.IN_PROGRESS)
        assertTrue(viewModel.canUpdateStatus(inProgressTx, UserType.FARMER))
    }

    @Test
    fun `canUpdateStatus returns false for farmer with completed transaction`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.canUpdateStatus(mockTransactions[1], UserType.FARMER).not())
    }

    @Test
    fun `canUpdateStatus returns true for buyer with confirmed transaction`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val confirmedTx = mockTransactions[0].copy(status = TransactionStatus.CONFIRMED)
        assertTrue(viewModel.canUpdateStatus(confirmedTx, UserType.BUYER))
    }

    @Test
    fun `canUpdateStatus returns false for buyer with pending transaction`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.canUpdateStatus(mockTransactions[0], UserType.BUYER))
    }

    @Test
    fun `getNextStatusForUser returns correct next status for each role and state`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(emptyList())
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = mockTransactions[0]
        val inProgress = pending.copy(status = TransactionStatus.IN_PROGRESS)
        val confirmed = pending.copy(status = TransactionStatus.CONFIRMED)

        assertEquals(TransactionStatus.CONFIRMED, viewModel.getNextStatusForUser(pending, UserType.FARMER))
        assertEquals(TransactionStatus.COMPLETED, viewModel.getNextStatusForUser(inProgress, UserType.FARMER))
        assertNull(viewModel.getNextStatusForUser(mockTransactions[1], UserType.FARMER))
        assertEquals(TransactionStatus.IN_PROGRESS, viewModel.getNextStatusForUser(confirmed, UserType.BUYER))
        assertNull(viewModel.getNextStatusForUser(pending, UserType.BUYER))
    }

    @Test
    fun `getStatusActionText returns correct labels for farmer and buyer`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(emptyList())
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = mockTransactions[0]
        val inProgress = pending.copy(status = TransactionStatus.IN_PROGRESS)
        val confirmed = pending.copy(status = TransactionStatus.CONFIRMED)

        assertEquals("Confirm Order", viewModel.getStatusActionText(pending, UserType.FARMER))
        assertEquals("Mark Complete", viewModel.getStatusActionText(inProgress, UserType.FARMER))
        assertNull(viewModel.getStatusActionText(mockTransactions[1], UserType.FARMER))
        assertEquals("Start Pickup", viewModel.getStatusActionText(confirmed, UserType.BUYER))
        assertNull(viewModel.getStatusActionText(pending, UserType.BUYER))
    }

    @Test
    fun `startTransaction calls updateTransactionStatus with IN_PROGRESS`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        coEvery { transactionRepository.updateTransactionStatus("tx1", TransactionStatus.IN_PROGRESS) } returns Result.success(mockTransactions[0])
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { transactionRepository.updateTransactionStatus("tx1", TransactionStatus.IN_PROGRESS) }
    }

    @Test
    fun `completeTransaction and cancelTransaction call repository with correct status`() = runTest {
        coEvery { transactionRepository.getTransactionsForUserFlow("user1") } returns flowOf(mockTransactions)
        coEvery { transactionRepository.updateTransactionStatus(any(), any()) } returns Result.success(mockTransactions[0])
        viewModel = TransactionsViewModel(transactionRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.completeTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { transactionRepository.updateTransactionStatus("tx1", TransactionStatus.COMPLETED) }

        viewModel.cancelTransaction("tx2")
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { transactionRepository.updateTransactionStatus("tx2", TransactionStatus.CANCELLED) }
    }
}

