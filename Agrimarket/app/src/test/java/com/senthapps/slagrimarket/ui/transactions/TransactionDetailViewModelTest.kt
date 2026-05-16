package com.senthapps.slagrimarket.ui.transactions

import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.Resource
import com.senthapps.slagrimarket.data.repository.TransactionRepository
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
class TransactionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: TransactionDetailViewModel

    private val farmerUser = User(
        id = "farmer1",
        name = "Farm Bob",
        phone = "+94771111111",
        userType = UserType.FARMER,
        verified = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private val buyerUser = User(
        id = "buyer1",
        name = "Buyer Alice",
        phone = "+94772222222",
        userType = UserType.BUYER,
        verified = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private fun makeTransaction(status: TransactionStatus) = Transaction(
        id = "tx1",
        listingId = "l1",
        farmerId = "farmer1",
        buyerId = "buyer1",
        quantity = 50.0,
        unit = "kg",
        pricePerUnit = 50.0,
        totalAmount = 2500.0,
        pickupLocation = "Colombo",
        pickupDate = "2026-02-01",
        status = status,
        createdAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        coEvery { authRepository.currentUser } returns flowOf(null)
        viewModel = TransactionDetailViewModel(transactionRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // initial state
    // -------------------------------------------------------------------------

    @Test
    fun `initial state has no transaction and is not loading`() {
        val state = viewModel.uiState.value
        assertNull(state.transaction)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.canUpdateStatus)
        assertNull(state.nextStatus)
        assertNull(state.actionText)
    }

    // -------------------------------------------------------------------------
    // loadTransaction
    // -------------------------------------------------------------------------

    @Test
    fun `loadTransaction populates state on success`() = runTest {
        val tx = makeTransaction(TransactionStatus.PENDING)
        coEvery { transactionRepository.getTransactionById("tx1") } returns Resource.Success(tx)
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.transaction)
        assertEquals("tx1", state.transaction!!.id)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadTransaction sets error on Resource Error`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Error("Not found")
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Not found", state.error)
        assertNull(state.transaction)
    }

    @Test
    fun `loadTransaction sets error on unexpected exception`() = runTest {
        coEvery { transactionRepository.getTransactionById(any()) } throws RuntimeException("DB crash")
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load transaction"))
    }

    // -------------------------------------------------------------------------
    // canUpdateStatus — role-based state machine
    // -------------------------------------------------------------------------

    @Test
    fun `farmer can update PENDING transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.PENDING))
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canUpdateStatus)
        assertEquals(TransactionStatus.CONFIRMED, viewModel.uiState.value.nextStatus)
        assertEquals("Confirm Order", viewModel.uiState.value.actionText)
    }

    @Test
    fun `buyer cannot update PENDING transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.PENDING))
        coEvery { authRepository.getCurrentUser() } returns buyerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canUpdateStatus)
        assertNull(viewModel.uiState.value.nextStatus)
    }

    @Test
    fun `farmer can advance CONFIRMED to IN_PROGRESS`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.CONFIRMED))
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canUpdateStatus)
        assertEquals(TransactionStatus.IN_PROGRESS, viewModel.uiState.value.nextStatus)
        assertEquals("Mark Ready", viewModel.uiState.value.actionText)
    }

    @Test
    fun `buyer can complete IN_PROGRESS transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.IN_PROGRESS))
        coEvery { authRepository.getCurrentUser() } returns buyerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canUpdateStatus)
        assertEquals(TransactionStatus.COMPLETED, viewModel.uiState.value.nextStatus)
        assertEquals("Complete Order", viewModel.uiState.value.actionText)
    }

    @Test
    fun `farmer cannot update IN_PROGRESS transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.IN_PROGRESS))
        coEvery { authRepository.getCurrentUser() } returns farmerUser

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canUpdateStatus)
    }

    @Test
    fun `nobody can update COMPLETED transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.COMPLETED))

        for (user in listOf(farmerUser, buyerUser)) {
            coEvery { authRepository.getCurrentUser() } returns user
            viewModel.loadTransaction("tx1")
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("${user.userType} should not update COMPLETED", viewModel.uiState.value.canUpdateStatus)
        }
    }

    @Test
    fun `nobody can update CANCELLED transaction`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.CANCELLED))

        for (user in listOf(farmerUser, buyerUser)) {
            coEvery { authRepository.getCurrentUser() } returns user
            viewModel.loadTransaction("tx1")
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("${user.userType} should not update CANCELLED", viewModel.uiState.value.canUpdateStatus)
        }
    }

    @Test
    fun `canUpdateStatus is false when user is null`() = runTest {
        coEvery { transactionRepository.getTransactionById("tx1") } returns
            Resource.Success(makeTransaction(TransactionStatus.PENDING))
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.loadTransaction("tx1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canUpdateStatus)
    }

    // -------------------------------------------------------------------------
    // updateTransactionStatus
    // -------------------------------------------------------------------------

    @Test
    fun `updateTransactionStatus reloads transaction on success`() = runTest {
        val tx = makeTransaction(TransactionStatus.PENDING)
        coEvery { transactionRepository.getTransactionById("tx1") } returns Resource.Success(tx)
        coEvery { authRepository.getCurrentUser() } returns farmerUser
        coEvery {
            transactionRepository.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)
        } returns Result.success(tx.copy(status = TransactionStatus.CONFIRMED))

        viewModel.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { transactionRepository.getTransactionById("tx1") }
    }

    @Test
    fun `updateTransactionStatus sets error on failure`() = runTest {
        coEvery {
            transactionRepository.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)
        } returns Result.failure(Exception("Network error"))

        viewModel.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to update status"))
    }
}
