package com.senthapps.slagrimarket

import com.senthapps.slagrimarket.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test
import org.junit.Assert.*

/**
 * Test that ProGuard rules preserve critical classes for JSON serialization.
 *
 * These tests verify that our ProGuard configuration doesn't strip out:
 * - Data model classes and their fields
 * - Enum serialization (especially with @Json annotations)
 * - Type converters for complex types
 * - Moshi adapters for custom serialization
 *
 * If these tests fail after enabling ProGuard, it indicates missing keep rules.
 */
class ProguardTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `verify Listing can be serialized to JSON`() {
        // Create a sample listing with minimal required fields
        val listing = Listing(
            id = "listing_123",
            farmerId = "farmer_123",
            cropType = "red_onion",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 50.0,
            quality = QualityGrade.A,
            harvestDate = "2026-02-01",
            location = "Jaffna"
        )

        val adapter = moshi.adapter(Listing::class.java)
        val json = adapter.toJson(listing)

        assertNotNull("JSON should not be null", json)
        assertTrue("JSON should contain farmerId", json.contains("farmer_123"))
        assertTrue("JSON should contain quality", json.contains("\"quality\":\"A\""))
        assertTrue("JSON should contain cropType", json.contains("red_onion"))

        // Verify deserialization works
        val deserializedListing = adapter.fromJson(json)
        assertNotNull("Deserialized listing should not be null", deserializedListing)
        assertEquals("Farmer ID should match", "farmer_123", deserializedListing?.farmerId)
        assertEquals("Quality should match", QualityGrade.A, deserializedListing?.quality)
    }

    @Test
    fun `verify enum QualityGrade serialization`() {
        val gradeAdapter = moshi.adapter(QualityGrade::class.java)

        // Test serialization
        assertEquals("\"A\"", gradeAdapter.toJson(QualityGrade.A))
        assertEquals("\"B\"", gradeAdapter.toJson(QualityGrade.B))
        assertEquals("\"C\"", gradeAdapter.toJson(QualityGrade.C))

        // Test deserialization
        assertEquals(QualityGrade.A, gradeAdapter.fromJson("\"A\""))
        assertEquals(QualityGrade.B, gradeAdapter.fromJson("\"B\""))
        assertEquals(QualityGrade.C, gradeAdapter.fromJson("\"C\""))
    }

    @Test
    fun `verify enum SyncStatus serialization`() {
        val statusAdapter = moshi.adapter(SyncStatus::class.java)

        // Test serialization
        assertEquals("\"PENDING\"", statusAdapter.toJson(SyncStatus.PENDING))
        assertEquals("\"SYNCED\"", statusAdapter.toJson(SyncStatus.SYNCED))
        assertEquals("\"FAILED\"", statusAdapter.toJson(SyncStatus.FAILED))

        // Test deserialization
        assertEquals(SyncStatus.PENDING, statusAdapter.fromJson("\"PENDING\""))
        assertEquals(SyncStatus.SYNCED, statusAdapter.fromJson("\"SYNCED\""))
        assertEquals(SyncStatus.FAILED, statusAdapter.fromJson("\"FAILED\""))
    }

    @Test
    fun `verify LocalOp can be serialized`() {
        val localOp = LocalOp(
            opId = "op_123",
            type = OpType.CREATE_LISTING,
            payload = "{\"farmerId\": \"farmer_123\"}"
        )

        val adapter = moshi.adapter(LocalOp::class.java)
        val json = adapter.toJson(localOp)

        assertNotNull("JSON should not be null", json)
        assertTrue("JSON should contain CREATE_LISTING", json.contains("CREATE_LISTING"))
        assertTrue("JSON should contain opId", json.contains("op_123"))

        // Verify deserialization
        val deserializedOp = adapter.fromJson(json)
        assertNotNull("Deserialized op should not be null", deserializedOp)
        assertEquals("Op ID should match", "op_123", deserializedOp?.opId)
        assertEquals("Type should match", OpType.CREATE_LISTING, deserializedOp?.type)
    }

    @Test
    fun `verify BatchSyncResponse can be deserialized`() {
        val jsonResponse = """
            {
                "success": true,
                "appliedOps": ["op1", "op2"],
                "conflicts": [],
                "errors": [],
                "serverData": {
                    "users": [],
                    "listings": {"data": [], "total": 0, "page": 1, "limit": 20},
                    "transactions": {"data": [], "total": 0, "page": 1, "limit": 20},
                    "messages": {"data": [], "total": 0, "page": 1, "limit": 20},
                    "reviews": {"data": [], "total": 0, "page": 1, "limit": 20},
                    "favorites": [],
                    "notifications": {"data": [], "total": 0, "page": 1, "limit": 20}
                },
                "serverTimestamp": "2026-02-16T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(BatchSyncResponse::class.java)
        val response = adapter.fromJson(jsonResponse)

        assertNotNull("Response should not be null", response)
        assertTrue("Success should be true", response!!.success)
        assertEquals("Should have 2 applied ops", 2, response.appliedOps.size)
        assertEquals("First op should be op1", "op1", response.appliedOps[0])
        assertTrue("Conflicts should be empty", response.conflicts.isEmpty())
        assertTrue("Errors should be empty", response.errors.isEmpty())
        assertNotNull("Server data should not be null", response.serverData)
        assertEquals("Server timestamp should match", "2026-02-16T00:00:00Z", response.serverTimestamp)
    }

    @Test
    fun `verify Transaction serialization`() {
        val transaction = Transaction(
            id = "txn_123",
            listingId = "listing_123",
            farmerId = "farmer_123",
            buyerId = "buyer_123",
            quantity = 50.0,
            pricePerUnit = 50.0,
            totalAmount = 2500.0,
            pickupLocation = "Jaffna Market",
            pickupDate = "2026-02-10",
            status = TransactionStatus.PENDING,
            paymentMethod = PaymentMethod.CASH
        )

        val adapter = moshi.adapter(Transaction::class.java)
        val json = adapter.toJson(transaction)

        assertNotNull("JSON should not be null", json)
        assertTrue("JSON should contain transaction ID", json.contains("txn_123"))
        assertTrue("JSON should contain PENDING status", json.contains("PENDING"))

        // Verify deserialization
        val deserializedTxn = adapter.fromJson(json)
        assertNotNull("Deserialized transaction should not be null", deserializedTxn)
        assertEquals("Transaction ID should match", "txn_123", deserializedTxn?.id)
        assertEquals("Quantity should match", 50.0, deserializedTxn!!.quantity, 0.001)
        assertEquals("Status should match", TransactionStatus.PENDING, deserializedTxn.status)
    }

    @Test
    fun `verify User serialization with complex types`() {
        val user = User(
            id = "user_123",
            name = "Test Farmer",
            phone = "+94771234567",
            userType = UserType.FARMER,
            location = "Jaffna"
        )

        val adapter = moshi.adapter(User::class.java)
        val json = adapter.toJson(user)

        assertNotNull("JSON should not be null", json)
        assertTrue("JSON should contain phone number", json.contains("+94771234567"))
        assertTrue("JSON should contain FARMER type", json.contains("FARMER"))

        // Verify deserialization
        val deserializedUser = adapter.fromJson(json)
        assertNotNull("Deserialized user should not be null", deserializedUser)
        assertEquals("User ID should match", "user_123", deserializedUser?.id)
        assertEquals("User type should match", UserType.FARMER, deserializedUser?.userType)
    }

    @Test
    fun `verify OpType enum all values can be serialized`() {
        val opTypeAdapter = moshi.adapter(OpType::class.java)

        // Test all OpType values (only the ones that actually exist)
        val opTypes = listOf(
            OpType.CREATE_LISTING,
            OpType.UPDATE_LISTING,
            OpType.DELETE_LISTING,
            OpType.CREATE_TRANSACTION,
            OpType.UPDATE_TRANSACTION,
            OpType.UPDATE_USER
        )

        opTypes.forEach { opType ->
            val json = opTypeAdapter.toJson(opType)
            assertNotNull("JSON for $opType should not be null", json)

            val deserialized = opTypeAdapter.fromJson(json)
            assertEquals("OpType $opType should roundtrip correctly", opType, deserialized)
        }
    }

    @Test
    fun `verify TransactionStatus enum serialization`() {
        val statusAdapter = moshi.adapter(TransactionStatus::class.java)

        // Test common transaction statuses
        val statuses = listOf(
            TransactionStatus.PENDING,
            TransactionStatus.CONFIRMED,
            TransactionStatus.COMPLETED,
            TransactionStatus.CANCELLED
        )

        statuses.forEach { status ->
            val json = statusAdapter.toJson(status)
            assertNotNull("JSON for $status should not be null", json)

            val deserialized = statusAdapter.fromJson(json)
            assertEquals("Status $status should roundtrip correctly", status, deserialized)
        }
    }

    @Test
    fun `verify PaymentMethod enum serialization`() {
        val methodAdapter = moshi.adapter(PaymentMethod::class.java)

        // Test payment methods
        val methods = listOf(
            PaymentMethod.CASH,
            PaymentMethod.CARD,
            PaymentMethod.ONLINE,
            PaymentMethod.BANK_TRANSFER
        )

        methods.forEach { method ->
            val json = methodAdapter.toJson(method)
            assertNotNull("JSON for $method should not be null", json)

            val deserialized = methodAdapter.fromJson(json)
            assertEquals("Method $method should roundtrip correctly", method, deserialized)
        }
    }

    @Test
    fun `verify UserType enum serialization`() {
        val typeAdapter = moshi.adapter(UserType::class.java)

        // Test user types
        assertEquals("\"FARMER\"", typeAdapter.toJson(UserType.FARMER))
        assertEquals("\"BUYER\"", typeAdapter.toJson(UserType.BUYER))

        assertEquals(UserType.FARMER, typeAdapter.fromJson("\"FARMER\""))
        assertEquals(UserType.BUYER, typeAdapter.fromJson("\"BUYER\""))
    }
}
