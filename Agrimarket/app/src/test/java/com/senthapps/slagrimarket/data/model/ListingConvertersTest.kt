package com.senthapps.slagrimarket.data.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test suite for ListingConverters
 *
 * Feature: Search filter crash on null qualityGrade
 *
 * Bug Description:
 * - SearchViewModel$searchWithFilters crashes with NPE when calling ListingConverters.fromQualityGrade
 * - Error: "Parameter specified as non-null is null: method ListingConverters.fromQualityGrade, parameter value"
 * - Triggered when user selects "Crop Type" filter in search
 *
 * Root Cause:
 * - Database or API may return listings with null quality grades
 * - fromQualityGrade() expects non-null QualityGrade but receives null
 *
 * Fix:
 * - Make fromQualityGrade() accept nullable input
 * - Default to QualityGrade.C when null (standard quality)
 */
class ListingConvertersTest {

    private lateinit var converters: ListingConverters

    @Before
    fun setup() {
        converters = ListingConverters()
    }

    // ============================================================================
    // REGRESSION TEST FOR NULL QUALITY GRADE BUG
    // ============================================================================

    @Test
    fun `fromQualityGrade should handle null input without crashing`() {
        // Given: A null quality grade (simulating database or API returning null)
        val nullQualityGrade: QualityGrade? = null

        // When: Converting null to string
        val result = converters.fromQualityGrade(nullQualityGrade)

        // Then: Should return default grade "C" instead of crashing
        assertEquals("C", result)
    }

    @Test
    fun `fromQualityGrade should convert valid grades correctly`() {
        // Given: Valid quality grades
        val gradeA = QualityGrade.A
        val gradeB = QualityGrade.B
        val gradeC = QualityGrade.C

        // When: Converting to strings
        val resultA = converters.fromQualityGrade(gradeA)
        val resultB = converters.fromQualityGrade(gradeB)
        val resultC = converters.fromQualityGrade(gradeC)

        // Then: Should return correct enum names
        assertEquals("A", resultA)
        assertEquals("B", resultB)
        assertEquals("C", resultC)
    }

    // ============================================================================
    // EXISTING CONVERTER TESTS (COMPREHENSIVE COVERAGE)
    // ============================================================================

    @Test
    fun `toQualityGrade should handle valid grade strings`() {
        // Given: Valid grade strings
        val gradeAString = "A"
        val gradeBString = "B"
        val gradeCString = "C"

        // When: Converting to enums
        val resultA = converters.toQualityGrade(gradeAString)
        val resultB = converters.toQualityGrade(gradeBString)
        val resultC = converters.toQualityGrade(gradeCString)

        // Then: Should return correct enum values
        assertEquals(QualityGrade.A, resultA)
        assertEquals(QualityGrade.B, resultB)
        assertEquals(QualityGrade.C, resultC)
    }

    @Test
    fun `toQualityGrade should default to C for invalid strings`() {
        // Given: Invalid grade strings
        val invalidGrade = "INVALID"
        val emptyString = ""

        // When: Converting to enums
        val resultInvalid = converters.toQualityGrade(invalidGrade)
        val resultEmpty = converters.toQualityGrade(emptyString)

        // Then: Should default to QualityGrade.C
        assertEquals(QualityGrade.C, resultInvalid)
        assertEquals(QualityGrade.C, resultEmpty)
    }

    @Test
    fun `fromSyncStatus should handle null input without crashing`() {
        // Given: A null sync status
        val nullStatus: SyncStatus? = null

        // When: Converting null to string
        val result = converters.fromSyncStatus(nullStatus)

        // Then: Should return default status "PENDING"
        assertEquals("PENDING", result)
    }

    @Test
    fun `fromSyncStatus should convert valid statuses correctly`() {
        // Given: Valid sync statuses
        val synced = SyncStatus.SYNCED
        val pending = SyncStatus.PENDING
        val failed = SyncStatus.FAILED

        // When: Converting to strings
        val resultSynced = converters.fromSyncStatus(synced)
        val resultPending = converters.fromSyncStatus(pending)
        val resultFailed = converters.fromSyncStatus(failed)

        // Then: Should return correct enum names
        assertEquals("SYNCED", resultSynced)
        assertEquals("PENDING", resultPending)
        assertEquals("FAILED", resultFailed)
    }

    @Test
    fun `toSyncStatus should handle valid status strings`() {
        // Given: Valid status strings
        val syncedString = "SYNCED"
        val pendingString = "PENDING"
        val failedString = "FAILED"

        // When: Converting to enums
        val resultSynced = converters.toSyncStatus(syncedString)
        val resultPending = converters.toSyncStatus(pendingString)
        val resultFailed = converters.toSyncStatus(failedString)

        // Then: Should return correct enum values
        assertEquals(SyncStatus.SYNCED, resultSynced)
        assertEquals(SyncStatus.PENDING, resultPending)
        assertEquals(SyncStatus.FAILED, resultFailed)
    }

    @Test
    fun `toSyncStatus should default to PENDING for invalid strings`() {
        // Given: Invalid status strings
        val invalidStatus = "INVALID"
        val emptyString = ""

        // When: Converting to enums
        val resultInvalid = converters.toSyncStatus(invalidStatus)
        val resultEmpty = converters.toSyncStatus(emptyString)

        // Then: Should default to SyncStatus.PENDING
        assertEquals(SyncStatus.PENDING, resultInvalid)
        assertEquals(SyncStatus.PENDING, resultEmpty)
    }

    @Test
    fun `fromStringList should convert empty list correctly`() {
        // Given: Empty list
        val emptyList = emptyList<String>()

        // When: Converting to JSON
        val result = converters.fromStringList(emptyList)

        // Then: Should return valid JSON array
        assertEquals("[]", result)
    }

    @Test
    fun `fromStringList should convert list with items correctly`() {
        // Given: List with items
        val list = listOf("item1", "item2", "item3")

        // When: Converting to JSON
        val result = converters.fromStringList(list)

        // Then: Should return valid JSON array
        assertTrue(result.contains("item1"))
        assertTrue(result.contains("item2"))
        assertTrue(result.contains("item3"))
    }

    @Test
    fun `toStringList should handle empty JSON array`() {
        // Given: Empty JSON array
        val emptyJson = "[]"

        // When: Converting to list
        val result = converters.toStringList(emptyJson)

        // Then: Should return empty list
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toStringList should handle JSON array with items`() {
        // Given: JSON array with items
        val json = """["item1","item2","item3"]"""

        // When: Converting to list
        val result = converters.toStringList(json)

        // Then: Should return list with items
        assertEquals(3, result.size)
        assertTrue(result.contains("item1"))
        assertTrue(result.contains("item2"))
        assertTrue(result.contains("item3"))
    }

    @Test
    fun `toStringList should return empty list for invalid JSON`() {
        // Given: Invalid JSON
        val invalidJson = "not valid json"

        // When: Converting to list
        val result = converters.toStringList(invalidJson)

        // Then: Should return empty list (graceful degradation)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fromCertificationList should convert empty list correctly`() {
        // Given: Empty certification list
        val emptyList = emptyList<Certification>()

        // When: Converting to JSON
        val result = converters.fromCertificationList(emptyList)

        // Then: Should return valid JSON array
        assertEquals("[]", result)
    }

    @Test
    fun `fromCertificationList should convert list with certifications`() {
        // Given: List with certifications
        val certifications = listOf(
            Certification(name = "Organic", issuer = "SL Organic Board"),
            Certification(name = "GAP", issuer = "Agriculture Dept")
        )

        // When: Converting to JSON
        val result = converters.fromCertificationList(certifications)

        // Then: Should return valid JSON array with certifications
        assertTrue(result.contains("Organic"))
        assertTrue(result.contains("SL Organic Board"))
        assertTrue(result.contains("GAP"))
        assertTrue(result.contains("Agriculture Dept"))
    }

    @Test
    fun `toCertificationList should handle empty JSON array`() {
        // Given: Empty JSON array
        val emptyJson = "[]"

        // When: Converting to list
        val result = converters.toCertificationList(emptyJson)

        // Then: Should return empty list
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toCertificationList should return empty list for invalid JSON`() {
        // Given: Invalid JSON
        val invalidJson = "not valid json"

        // When: Converting to list
        val result = converters.toCertificationList(invalidJson)

        // Then: Should return empty list (graceful degradation)
        assertTrue(result.isEmpty())
    }

    // ============================================================================
    // ROUND-TRIP CONVERSION TESTS
    // ============================================================================

    @Test
    fun `quality grade round-trip conversion should preserve value`() {
        // Given: A quality grade
        val original = QualityGrade.B

        // When: Converting to string and back
        val stringValue = converters.fromQualityGrade(original)
        val backToEnum = converters.toQualityGrade(stringValue)

        // Then: Should preserve the original value
        assertEquals(original, backToEnum)
    }

    @Test
    fun `sync status round-trip conversion should preserve value`() {
        // Given: A sync status
        val original = SyncStatus.SYNCED

        // When: Converting to string and back
        val stringValue = converters.fromSyncStatus(original)
        val backToEnum = converters.toSyncStatus(stringValue)

        // Then: Should preserve the original value
        assertEquals(original, backToEnum)
    }

    @Test
    fun `string list round-trip conversion should preserve values`() {
        // Given: A string list
        val original = listOf("organic", "pesticide-free", "locally-grown")

        // When: Converting to JSON and back
        val jsonValue = converters.fromStringList(original)
        val backToList = converters.toStringList(jsonValue)

        // Then: Should preserve the original list
        assertEquals(original.size, backToList.size)
        assertEquals(original, backToList)
    }
}
