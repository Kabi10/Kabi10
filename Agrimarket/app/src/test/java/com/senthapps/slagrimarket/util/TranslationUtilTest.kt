package com.senthapps.slagrimarket.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationUtilTest {

    // ============================================================================
    // getCropName
    // ============================================================================

    @Test
    fun `getCropName returns English name for known crop`() {
        assertEquals("Tomato", TranslationUtil.getCropName("tomato", "en"))
    }

    @Test
    fun `getCropName returns Tamil name for known crop`() {
        assertEquals("தக்காளி", TranslationUtil.getCropName("tomato", "ta"))
    }

    @Test
    fun `getCropName returns Sinhala name for known crop`() {
        assertEquals("තක්කාලි", TranslationUtil.getCropName("tomato", "si"))
    }

    @Test
    fun `getCropName returns key for unknown crop`() {
        assertEquals("unknown_crop", TranslationUtil.getCropName("unknown_crop", "en"))
    }

    @Test
    fun `getCropName defaults to English for unrecognised language code`() {
        assertEquals("Tomato", TranslationUtil.getCropName("tomato", "fr"))
    }

    @Test
    fun `getCropName handles red onion correctly in Tamil`() {
        assertEquals("வெங்காயம்", TranslationUtil.getCropName("red_onion", "ta"))
    }

    @Test
    fun `getCropName handles chili correctly in Sinhala`() {
        assertEquals("මිරිස්", TranslationUtil.getCropName("chili", "si"))
    }

    // ============================================================================
    // getLocationName
    // ============================================================================

    @Test
    fun `getLocationName returns English name for Jaffna`() {
        assertEquals("Jaffna", TranslationUtil.getLocationName("Jaffna", "en"))
    }

    @Test
    fun `getLocationName returns Tamil name for Jaffna`() {
        assertEquals("யாழ்ப்பாணம்", TranslationUtil.getLocationName("Jaffna", "ta"))
    }

    @Test
    fun `getLocationName returns Sinhala name for Jaffna`() {
        assertEquals("යාපනය", TranslationUtil.getLocationName("Jaffna", "si"))
    }

    @Test
    fun `getLocationName returns key for unknown location`() {
        assertEquals("Unknown Town", TranslationUtil.getLocationName("Unknown Town", "en"))
    }

    @Test
    fun `getLocationName handles Chavakachcheri Market in Tamil`() {
        assertEquals("சாவகச்சேரி சந்தை", TranslationUtil.getLocationName("Chavakachcheri Market", "ta"))
    }

    // ============================================================================
    // getQualityGrade
    // ============================================================================

    @Test
    fun `getQualityGrade returns English for PREMIUM`() {
        assertEquals("Premium", TranslationUtil.getQualityGrade("PREMIUM", "en"))
    }

    @Test
    fun `getQualityGrade returns Tamil for GRADE_A`() {
        assertEquals("தரம் A", TranslationUtil.getQualityGrade("GRADE_A", "ta"))
    }

    @Test
    fun `getQualityGrade returns Sinhala for GRADE_B`() {
        assertEquals("ශ්‍රේණිය B", TranslationUtil.getQualityGrade("GRADE_B", "si"))
    }

    @Test
    fun `getQualityGrade returns key for unknown grade`() {
        assertEquals("UNKNOWN", TranslationUtil.getQualityGrade("UNKNOWN", "en"))
    }

    // ============================================================================
    // getUnit
    // ============================================================================

    @Test
    fun `getUnit returns kg in English`() {
        assertEquals("kg", TranslationUtil.getUnit("kg", "en"))
    }

    @Test
    fun `getUnit returns Tamil for kg`() {
        assertEquals("கிலோ", TranslationUtil.getUnit("kg", "ta"))
    }

    @Test
    fun `getUnit returns Sinhala for bunch`() {
        assertEquals("මිටියක්", TranslationUtil.getUnit("bunch", "si"))
    }

    @Test
    fun `getUnit returns key for unknown unit`() {
        assertEquals("ton", TranslationUtil.getUnit("ton", "en"))
    }

    // ============================================================================
    // getAllCrops / getAllLocations
    // ============================================================================

    @Test
    fun `getAllCrops returns non-empty list`() {
        val crops = TranslationUtil.getAllCrops()
        assertTrue(crops.isNotEmpty())
    }

    @Test
    fun `getAllCrops contains tomato`() {
        assertTrue(TranslationUtil.getAllCrops().contains("tomato"))
    }

    @Test
    fun `getAllLocations returns non-empty list`() {
        val locations = TranslationUtil.getAllLocations()
        assertTrue(locations.isNotEmpty())
    }

    @Test
    fun `getAllLocations contains Jaffna`() {
        assertTrue(TranslationUtil.getAllLocations().contains("Jaffna"))
    }

    // ============================================================================
    // searchCrops
    // ============================================================================

    @Test
    fun `searchCrops finds tomato by partial English name`() {
        val results = TranslationUtil.searchCrops("tom", "en")
        assertTrue(results.contains("tomato"))
    }

    @Test
    fun `searchCrops is case-insensitive in English`() {
        val results = TranslationUtil.searchCrops("MANGO", "en")
        assertTrue(results.contains("mango"))
    }

    @Test
    fun `searchCrops returns empty list for no match`() {
        val results = TranslationUtil.searchCrops("zzznomatch", "en")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchCrops finds crop by Tamil script`() {
        val results = TranslationUtil.searchCrops("தக்காளி", "ta")
        assertTrue(results.contains("tomato"))
    }

    @Test
    fun `searchCrops finds multiple crops with shared substring`() {
        val results = TranslationUtil.searchCrops("gourd", "en")
        assertTrue(results.size >= 2)
        assertTrue(results.contains("bitter_gourd"))
        assertTrue(results.contains("snake_gourd"))
    }
}
