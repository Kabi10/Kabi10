package com.senthapps.slagrimarket.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RelativeTimeUtilTest {

    // ============================================================================
    // NULL / BLANK / INVALID INPUTS
    // ============================================================================

    @Test
    fun `getRelativeTimeString null timestamp returns just now in English`() {
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(null))
    }

    @Test
    fun `getRelativeTimeString blank timestamp returns just now in English`() {
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString("   "))
    }

    @Test
    fun `getRelativeTimeString invalid timestamp returns just now`() {
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString("not-a-timestamp"))
    }

    @Test
    fun `getRelativeTimeString millis zero returns just now`() {
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(0L))
    }

    @Test
    fun `getRelativeTimeString millis negative returns just now`() {
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(-1L))
    }

    // ============================================================================
    // ENGLISH — TIME RANGE BOUNDARIES
    // ============================================================================

    @Test
    fun `getRelativeTimeString 30 seconds ago returns just now`() {
        val thirtySecondsAgo = System.currentTimeMillis() - 30_000L
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(thirtySecondsAgo))
    }

    @Test
    fun `getRelativeTimeString 1 minute ago returns 1 min ago`() {
        val oneMinuteAgo = System.currentTimeMillis() - 65_000L
        assertEquals("1 min ago", RelativeTimeUtil.getRelativeTimeString(oneMinuteAgo))
    }

    @Test
    fun `getRelativeTimeString 5 minutes ago returns 5 min ago`() {
        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60_000L - 1_000L
        assertEquals("5 min ago", RelativeTimeUtil.getRelativeTimeString(fiveMinutesAgo))
    }

    @Test
    fun `getRelativeTimeString 1 hour ago returns 1 hour ago`() {
        val oneHourAgo = System.currentTimeMillis() - 3_600_000L - 1_000L
        assertEquals("1 hour ago", RelativeTimeUtil.getRelativeTimeString(oneHourAgo))
    }

    @Test
    fun `getRelativeTimeString 3 hours ago returns 3 hours ago`() {
        val threeHoursAgo = System.currentTimeMillis() - 3 * 3_600_000L - 1_000L
        assertEquals("3 hours ago", RelativeTimeUtil.getRelativeTimeString(threeHoursAgo))
    }

    @Test
    fun `getRelativeTimeString 1 day ago returns Yesterday`() {
        val oneDayAgo = System.currentTimeMillis() - 86_400_000L - 1_000L
        assertEquals("Yesterday", RelativeTimeUtil.getRelativeTimeString(oneDayAgo))
    }

    @Test
    fun `getRelativeTimeString 3 days ago returns 3 days ago`() {
        val threeDaysAgo = System.currentTimeMillis() - 3 * 86_400_000L - 1_000L
        assertEquals("3 days ago", RelativeTimeUtil.getRelativeTimeString(threeDaysAgo))
    }

    @Test
    fun `getRelativeTimeString 1 week ago returns 1 week ago`() {
        val oneWeekAgo = System.currentTimeMillis() - 7 * 86_400_000L - 1_000L
        assertEquals("1 week ago", RelativeTimeUtil.getRelativeTimeString(oneWeekAgo))
    }

    @Test
    fun `getRelativeTimeString 2 weeks ago returns 2 weeks ago`() {
        val twoWeeksAgo = System.currentTimeMillis() - 14 * 86_400_000L - 1_000L
        assertEquals("2 weeks ago", RelativeTimeUtil.getRelativeTimeString(twoWeeksAgo))
    }

    @Test
    fun `getRelativeTimeString 1 month ago returns 1 month ago`() {
        val oneMonthAgo = System.currentTimeMillis() - 31 * 86_400_000L - 1_000L
        assertEquals("1 month ago", RelativeTimeUtil.getRelativeTimeString(oneMonthAgo))
    }

    @Test
    fun `getRelativeTimeString 3 months ago returns 3 months ago`() {
        val threeMonthsAgo = System.currentTimeMillis() - 91 * 86_400_000L - 1_000L
        assertEquals("3 months ago", RelativeTimeUtil.getRelativeTimeString(threeMonthsAgo))
    }

    // ============================================================================
    // FUTURE TIMESTAMP
    // ============================================================================

    @Test
    fun `getRelativeTimeString future timestamp returns just now`() {
        val futureMillis = System.currentTimeMillis() + 60_000L
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(futureMillis))
    }

    @Test
    fun `getRelativeTimeString future ISO timestamp returns just now`() {
        val futureTimestamp = Instant.now().plusSeconds(300).toString()
        assertEquals("Just now", RelativeTimeUtil.getRelativeTimeString(futureTimestamp))
    }

    // ============================================================================
    // TAMIL LANGUAGE
    // ============================================================================

    @Test
    fun `getRelativeTimeString null in Tamil returns Tamil just now`() {
        assertEquals("இப்போது", RelativeTimeUtil.getRelativeTimeString(null, "ta"))
    }

    @Test
    fun `getRelativeTimeString 1 minute in Tamil returns singular Tamil form`() {
        val oneMinuteAgo = System.currentTimeMillis() - 65_000L
        assertEquals("1 நிமிடம் முன்பு", RelativeTimeUtil.getRelativeTimeString(oneMinuteAgo, "ta"))
    }

    @Test
    fun `getRelativeTimeString 5 minutes in Tamil returns plural Tamil form`() {
        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60_000L - 1_000L
        assertEquals("5 நிமிடங்களுக்கு முன்பு", RelativeTimeUtil.getRelativeTimeString(fiveMinutesAgo, "ta"))
    }

    @Test
    fun `getRelativeTimeString 1 day in Tamil returns yesterday Tamil`() {
        val oneDayAgo = System.currentTimeMillis() - 86_400_000L - 1_000L
        assertEquals("நேற்று", RelativeTimeUtil.getRelativeTimeString(oneDayAgo, "ta"))
    }

    // ============================================================================
    // SINHALA LANGUAGE
    // ============================================================================

    @Test
    fun `getRelativeTimeString null in Sinhala returns Sinhala just now`() {
        assertEquals("දැන්", RelativeTimeUtil.getRelativeTimeString(null, "si"))
    }

    @Test
    fun `getRelativeTimeString 1 minute in Sinhala returns singular Sinhala form`() {
        val oneMinuteAgo = System.currentTimeMillis() - 65_000L
        assertEquals("විනාඩි 1කට පෙර", RelativeTimeUtil.getRelativeTimeString(oneMinuteAgo, "si"))
    }

    @Test
    fun `getRelativeTimeString 1 day in Sinhala returns yesterday Sinhala`() {
        val oneDayAgo = System.currentTimeMillis() - 86_400_000L - 1_000L
        assertEquals("ඊයේ", RelativeTimeUtil.getRelativeTimeString(oneDayAgo, "si"))
    }

    // ============================================================================
    // getUpdatedTimeString
    // ============================================================================

    @Test
    fun `getUpdatedTimeString null timestamp returns UPDATED JUST NOW in English`() {
        val result = RelativeTimeUtil.getUpdatedTimeString(null)
        assertEquals("UPDATED: JUST NOW", result)
    }

    @Test
    fun `getUpdatedTimeString in Sinhala contains Sinhala prefix`() {
        val result = RelativeTimeUtil.getUpdatedTimeString(null, "si")
        assertTrue(result.startsWith("යාවත්කාලීන:"))
    }

    @Test
    fun `getUpdatedTimeString in Tamil contains Tamil prefix`() {
        val result = RelativeTimeUtil.getUpdatedTimeString(null, "ta")
        assertTrue(result.startsWith("புதுப்பிக்கப்பட்டது:"))
    }

    @Test
    fun `getUpdatedTimeString millis in English is uppercased`() {
        val oneHourAgo = System.currentTimeMillis() - 3_600_000L - 1_000L
        val result = RelativeTimeUtil.getUpdatedTimeString(oneHourAgo)
        assertTrue(result.startsWith("UPDATED:"))
        assertEquals(result, result.uppercase().replace("UPDATED:", "UPDATED:"))
    }

    // ============================================================================
    // ISO STRING OVERLOAD
    // ============================================================================

    @Test
    fun `getRelativeTimeString ISO string 2 minutes ago returns 2 min ago`() {
        val twoMinutesAgo = Instant.now().minusSeconds(125).toString()
        assertEquals("2 min ago", RelativeTimeUtil.getRelativeTimeString(twoMinutesAgo))
    }

    @Test
    fun `getRelativeTimeString ISO string 2 hours ago returns 2 hours ago`() {
        val twoHoursAgo = Instant.now().minusSeconds(7_201).toString()
        assertEquals("2 hours ago", RelativeTimeUtil.getRelativeTimeString(twoHoursAgo))
    }
}
