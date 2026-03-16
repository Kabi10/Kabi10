package com.senthapps.slagrimarket.data.repository

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.api.TransactionApiService
import com.senthapps.slagrimarket.data.api.TransactionsResponse
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.squareup.moshi.Moshi
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryTest {

    private lateinit var transactionApiService: TransactionApiService
    private lateinit var transactionDao: TransactionDao
    private lateinit var localOpDao: LocalOpDao
    private lateinit var moshi: Moshi
    private lateinit var repository: TransactionRepository

    private val mockTransactions = listOf(
        Transaction(
            id = "tx1",
            listingId = "listing1",
            farmerId = "farmer1",
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
            farmerId = "farmer1",
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
        transactionApiService = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        localOpDao = mockk(relaxed = true)
        moshi = Moshi.Builder().build()

        repository = TransactionRepository(transactionApiService, transactionDao, localOpDao, moshi)
    }

    @Test
    fun `createTransaction should save to DAO and create local op`() = runTest {
        coEvery { transactionDao.insertTransaction(any()) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        val result = repository.createTransaction(
            listingId = "listing1",
            farmerId = "farmer1",
            buyerId = "buyer1",
            quantity = 50.0,
            totalAmount = 2500.0,
            pickupLocation = "Jaffna Central Market",
            pickupDate = "2025-11-25"
        )

        assertTrue(result.isSuccess)
        val tx = result.getOrNull()
        assertNotNull(tx)
        assertEquals("listing1", tx?.listingId)
        assertEquals(TransactionStatus.PENDING, tx?.status)
        coVerify(exactly = 1) { transactionDao.insertTransaction(any()) }
        coVerify(exactly = 1) { localOpDao.insertOp(any()) }
    }

    @Test
    fun `createTransaction should fail when DAO throws`() = runTest {
        coEvery { transactionDao.insertTransaction(any()) } throws RuntimeException("DB write failed")

        val result = repository.createTransaction(
            listingId = "listing1",
            farmerId = "farmer1",
            buyerId = "buyer1",
            quantity = 50.0,
            totalAmount = 2500.0,
            pickupLocation = "Jaffna",
            pickupDate = "2025-11-25"
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { localOpDao.insertOp(any()) }
    }

    @Test
    fun `createTransaction should calculate pricePerUnit from quantity and totalAmount`() = runTest {
        val capturedTx = slot<Transaction>()
        coEvery { transactionDao.insertTransaction(capture(capturedTx)) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        repository.createTransaction(
            listingId = "listing1",
            farmerId = "farmer1",
            buyerId = "buyer1",
            quantity = 100.0,
            totalAmount = 5000.0,
            pickupLocation = "Jaffna",
            pickupDate = "2025-11-25"
        )

        assertEquals(50.0, capturedTx.captured.pricePerUnit, 0.001)
    }

    @Test
    fun `updateTransactionStatus should update existing transaction and create local op`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns mockTransactions[0]
        coEvery { transactionDao.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        val result = repository.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED)

        assertTrue(result.isSuccess)
        assertEquals(TransactionStatus.CONFIRMED, result.getOrNull()?.status)
        coVerify { transactionDao.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED) }
        coVerify { localOpDao.insertOp(any()) }
    }

    @Test
    fun `updateTransactionStatus should fail when transaction not found`() = runTest {
        coEvery { transactionDao.getTransactionById("nonexistent") } returns null

        val result = repository.updateTransactionStatus("nonexistent", TransactionStatus.CONFIRMED)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
        coVerify(exactly = 0) { localOpDao.insertOp(any()) }
    }

    @Test
    fun `getTransactionsForUser should emit cached data when available`() = runTest {
        // Prevent network refresh by returning a recent timestamp
        coEvery { transactionDao.getTransactionsForUser("user1") } returns mockTransactions
        coEvery { transactionDao.getLastUpdateTimeForUser("user1") } returns Instant.now().toString()

        repository.getTransactionsForUser("user1", forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem()
            assertTrue(success is Resource.Success)
            assertEquals(2, (success as Resource.Success).data?.size)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsForUser with forceRefresh should emit cached then network data`() = runTest {
        val networkResponse = TransactionsResponse(
            transactions = mockTransactions,
            totalCount = 2,
            page = 1,
            totalPages = 1
        )
        coEvery { transactionDao.getTransactionsForUser("user1") } returns mockTransactions
        coEvery { transactionApiService.getTransactions(limit = 50) } returns Response.success(networkResponse)

        repository.getTransactionsForUser("user1", forceRefresh = true).test {
            assertTrue(awaitItem() is Resource.Loading)
            val cached = awaitItem()
            assertTrue(cached is Resource.Success)
            assertEquals(2, (cached as Resource.Success).data?.size)
            val network = awaitItem()
            assertTrue(network is Resource.Success)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsForUser should emit error when cache empty and network fails`() = runTest {
        coEvery { transactionDao.getTransactionsForUser("user1") } returns emptyList()
        coEvery { transactionApiService.getTransactions(limit = 50) } throws IOException("No internet")

        repository.getTransactionsForUser("user1", forceRefresh = true).test {
            assertTrue(awaitItem() is Resource.Loading)
            val error = awaitItem()
            assertTrue(error is Resource.Error)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsForUser should complete silently when cache available and network fails`() = runTest {
        coEvery { transactionDao.getTransactionsForUser("user1") } returns mockTransactions
        coEvery { transactionApiService.getTransactions(limit = 50) } throws IOException("Offline")

        repository.getTransactionsForUser("user1", forceRefresh = true).test {
            assertTrue(awaitItem() is Resource.Loading)
            val cached = awaitItem()
            assertTrue(cached is Resource.Success)
            assertEquals(2, (cached as Resource.Success).data?.size)
            awaitComplete() // no error emitted when cache is available
        }
    }

    @Test
    fun `getTransactionById should return success when found locally`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns mockTransactions[0]

        val result = repository.getTransactionById("tx1")

        assertTrue(result is Resource.Success)
        assertEquals("tx1", (result as Resource.Success).data?.id)
        coVerify(exactly = 0) { transactionApiService.getTransactionById(any()) }
    }

    @Test
    fun `getTransactionById should fall back to network when not found locally`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns null
        coEvery { transactionApiService.getTransactionById("tx1") } returns Response.success(mockTransactions[0])
        coEvery { transactionDao.insertTransaction(any()) } just Runs

        val result = repository.getTransactionById("tx1")

        assertTrue(result is Resource.Success)
        assertEquals("tx1", (result as Resource.Success).data?.id)
        coVerify { transactionDao.insertTransaction(any()) }
    }

    @Test
    fun `confirmTransaction should call updateTransactionStatus with CONFIRMED`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns mockTransactions[0]
        coEvery { transactionDao.updateTransactionStatus("tx1", TransactionStatus.CONFIRMED) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        val result = repository.confirmTransaction("tx1")

        assertTrue(result.isSuccess)
        assertEquals(TransactionStatus.CONFIRMED, result.getOrNull()?.status)
    }

    @Test
    fun `completeTransaction should call updateTransactionStatus with COMPLETED`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns mockTransactions[0]
        coEvery { transactionDao.updateTransactionStatus("tx1", TransactionStatus.COMPLETED) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        val result = repository.completeTransaction("tx1")

        assertTrue(result.isSuccess)
        assertEquals(TransactionStatus.COMPLETED, result.getOrNull()?.status)
    }

    @Test
    fun `cancelTransaction should call updateTransactionStatus with CANCELLED`() = runTest {
        coEvery { transactionDao.getTransactionById("tx1") } returns mockTransactions[0]
        coEvery { transactionDao.updateTransactionStatus("tx1", TransactionStatus.CANCELLED) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        val result = repository.cancelTransaction("tx1", reason = "Changed mind")

        assertTrue(result.isSuccess)
        assertEquals(TransactionStatus.CANCELLED, result.getOrNull()?.status)
    }

    @Test
    fun `refreshTransactions should update DAO on network success`() = runTest {
        val response = TransactionsResponse(
            transactions = mockTransactions,
            totalCount = 2,
            page = 1,
            totalPages = 1
        )
        coEvery { transactionApiService.getTransactions(limit = 50) } returns Response.success(response)
        coEvery { transactionDao.insertTransactions(any()) } just Runs

        val result = repository.refreshTransactions("user1")

        assertTrue(result is Resource.Success)
        coVerify { transactionDao.insertTransactions(mockTransactions) }
    }
}
