package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Instant

@Database(entities = [MarketPrice::class], version = 1, exportSchema = false)
abstract class MarketPriceTestDatabase : RoomDatabase() {
    abstract fun marketPriceDao(): MarketPriceDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class MarketPriceDaoTest {

    private lateinit var db: MarketPriceTestDatabase
    private lateinit var dao: MarketPriceDao

    private val recentTs = Instant.now().toString()
    private val oldTs = Instant.now().minusSeconds(3600 * 50).toString()

    private fun makePrice(
        id: String,
        cropType: String = "Tomato",
        location: String = "Colombo",
        currentPrice: Double = 120.0,
        previousPrice: Double = 100.0,
        trend: PriceTrend = PriceTrend.UP,
        changePercentage: Double = 20.0,
        isActive: Boolean = true,
        lastUpdated: String = recentTs
    ) = MarketPrice(
        id = id,
        cropType = cropType,
        cropNameTamil = "தக்காளி",
        cropNameEnglish = cropType,
        cropNameSinhala = "තක්කාලි",
        currentPrice = currentPrice,
        previousPrice = previousPrice,
        trend = trend,
        changePercentage = changePercentage,
        location = location,
        isActive = isActive,
        lastUpdated = lastUpdated
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            MarketPriceTestDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.marketPriceDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- CRUD ----

    @Test
    fun `insertMarketPrice and getMarketPriceById returns inserted price`() = runBlocking {
        val price = makePrice("mp-1")
        dao.insertMarketPrice(price)

        val result = dao.getMarketPriceById("mp-1")
        assertNotNull(result)
        assertEquals("mp-1", result!!.id)
        assertEquals("Tomato", result.cropType)
    }

    @Test
    fun `getMarketPriceById returns null for unknown id`() = runBlocking {
        val result = dao.getMarketPriceById("nonexistent")
        assertNull(result)
    }

    @Test
    fun `insertMarketPrices bulk inserts all records`() = runBlocking {
        val prices = listOf(makePrice("mp-1"), makePrice("mp-2"), makePrice("mp-3"))
        dao.insertMarketPrices(prices)

        val all = dao.getAllActiveMarketPrices()
        assertEquals(3, all.size)
    }

    @Test
    fun `insertMarketPrice replaces on conflict`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", currentPrice = 100.0))
        dao.insertMarketPrice(makePrice("mp-1", currentPrice = 150.0))

        val result = dao.getMarketPriceById("mp-1")
        assertEquals(150.0, result!!.currentPrice, 0.001)
    }

    @Test
    fun `deleteMarketPriceById removes the record`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1"))
        dao.deleteMarketPriceById("mp-1")

        assertNull(dao.getMarketPriceById("mp-1"))
    }

    @Test
    fun `deleteAllMarketPrices clears the table`() = runBlocking {
        dao.insertMarketPrices(listOf(makePrice("mp-1"), makePrice("mp-2")))
        dao.deleteAllMarketPrices()

        assertTrue(dao.getAllActiveMarketPrices().isEmpty())
    }

    // ---- Active filter ----

    @Test
    fun `getAllActiveMarketPrices excludes inactive records`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-active", isActive = true))
        dao.insertMarketPrice(makePrice("mp-inactive", isActive = false))

        val active = dao.getAllActiveMarketPrices()
        assertEquals(1, active.size)
        assertEquals("mp-active", active[0].id)
    }

    @Test
    fun `getAllActiveMarketPricesFlow emits only active prices`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", isActive = true))
        dao.insertMarketPrice(makePrice("mp-2", isActive = false))

        val result = dao.getAllActiveMarketPricesFlow().first()
        assertEquals(1, result.size)
    }

    // ---- Crop type filter ----

    @Test
    fun `getMarketPricesForCrop returns prices matching crop type`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-tomato", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-onion", cropType = "Onion"))

        val tomatoes = dao.getMarketPricesForCrop("Tomato")
        assertEquals(1, tomatoes.size)
        assertEquals("Tomato", tomatoes[0].cropType)
    }

    @Test
    fun `getMarketPricesForCrop returns empty list for unknown crop`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato"))

        val result = dao.getMarketPricesForCrop("Chili")
        assertTrue(result.isEmpty())
    }

    // ---- Location filter ----

    @Test
    fun `getMarketPricesForLocation returns prices for specified location`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-col", location = "Colombo"))
        dao.insertMarketPrice(makePrice("mp-jaf", location = "Jaffna"))

        val colombo = dao.getMarketPricesForLocation("Colombo")
        assertEquals(1, colombo.size)
        assertEquals("Colombo", colombo[0].location)
    }

    // ---- Latest price ----

    @Test
    fun `getLatestMarketPrice returns most recently updated price`() = runBlocking {
        val earlier = makePrice("mp-old", cropType = "Tomato", location = "Colombo",
            lastUpdated = Instant.now().minusSeconds(3600).toString())
        val latest = makePrice("mp-new", cropType = "Tomato", location = "Colombo",
            lastUpdated = recentTs)
        dao.insertMarketPrices(listOf(earlier, latest))

        val result = dao.getLatestMarketPrice("Tomato", "Colombo")
        assertNotNull(result)
        assertEquals("mp-new", result!!.id)
    }

    @Test
    fun `getLatestMarketPrice returns null when no match`() = runBlocking {
        val result = dao.getLatestMarketPrice("Tomato", "Colombo")
        assertNull(result)
    }

    // ---- Trend filter ----

    @Test
    fun `getMarketPricesByTrend returns only prices matching trend`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-up1", trend = PriceTrend.UP, changePercentage = 10.0))
        dao.insertMarketPrice(makePrice("mp-up2", trend = PriceTrend.UP, changePercentage = 5.0))
        dao.insertMarketPrice(makePrice("mp-down", trend = PriceTrend.DOWN, changePercentage = -8.0))

        val rising = dao.getMarketPricesByTrend(PriceTrend.UP)
        assertEquals(2, rising.size)
        assertTrue(rising.all { it.trend == PriceTrend.UP })
    }

    @Test
    fun `getTrendingPrices returns prices with change above threshold`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-high", changePercentage = 15.0))
        dao.insertMarketPrice(makePrice("mp-low", changePercentage = 1.0))

        val trending = dao.getTrendingPrices(threshold = 5.0)
        assertEquals(1, trending.size)
        assertEquals("mp-high", trending[0].id)
    }

    @Test
    fun `getTopGainers returns UP-trending prices ordered by change desc`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-g1", trend = PriceTrend.UP, changePercentage = 20.0))
        dao.insertMarketPrice(makePrice("mp-g2", trend = PriceTrend.UP, changePercentage = 10.0))
        dao.insertMarketPrice(makePrice("mp-d1", trend = PriceTrend.DOWN, changePercentage = -5.0))

        val gainers = dao.getTopGainers(limit = 5)
        assertEquals(2, gainers.size)
        assertEquals("mp-g1", gainers[0].id)
        assertEquals(20.0, gainers[0].changePercentage, 0.001)
    }

    @Test
    fun `getTopLosers returns DOWN-trending prices ordered by change asc`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-l1", trend = PriceTrend.DOWN, changePercentage = -15.0))
        dao.insertMarketPrice(makePrice("mp-l2", trend = PriceTrend.DOWN, changePercentage = -5.0))
        dao.insertMarketPrice(makePrice("mp-u1", trend = PriceTrend.UP, changePercentage = 10.0))

        val losers = dao.getTopLosers(limit = 5)
        assertEquals(2, losers.size)
        assertEquals("mp-l1", losers[0].id)
    }

    // ---- Search ----

    @Test
    fun `searchMarketPrices with no filters returns all active prices`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Onion"))

        val results = dao.searchMarketPrices()
        assertEquals(2, results.size)
    }

    @Test
    fun `searchMarketPrices with cropType filter returns matching prices`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Chili"))

        val results = dao.searchMarketPrices(cropType = "Tomato")
        assertEquals(1, results.size)
        assertEquals("Tomato", results[0].cropType)
    }

    @Test
    fun `searchMarketPrices with price range filters correctly`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-cheap", currentPrice = 50.0))
        dao.insertMarketPrice(makePrice("mp-mid", currentPrice = 150.0))
        dao.insertMarketPrice(makePrice("mp-exp", currentPrice = 300.0))

        val results = dao.searchMarketPrices(minPrice = 100.0, maxPrice = 200.0)
        assertEquals(1, results.size)
        assertEquals("mp-mid", results[0].id)
    }

    @Test
    fun `searchMarketPrices with searchQuery matches cropNameEnglish`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-tomato", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-onion", cropType = "Onion"))

        val results = dao.searchMarketPrices(searchQuery = "Tomato")
        assertEquals(1, results.size)
    }

    // ---- Statistics ----

    @Test
    fun `getAvailableCropTypes returns distinct crop types`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-3", cropType = "Onion"))

        val types = dao.getAvailableCropTypes()
        assertEquals(2, types.size)
        assertTrue(types.contains("Tomato"))
        assertTrue(types.contains("Onion"))
    }

    @Test
    fun `getAvailableLocations returns distinct locations`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", location = "Colombo"))
        dao.insertMarketPrice(makePrice("mp-2", location = "Colombo"))
        dao.insertMarketPrice(makePrice("mp-3", location = "Jaffna"))

        val locations = dao.getAvailableLocations()
        assertEquals(2, locations.size)
    }

    @Test
    fun `getAveragePriceForCrop computes correct average`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato", currentPrice = 100.0))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Tomato", currentPrice = 200.0))

        val avg = dao.getAveragePriceForCrop("Tomato")
        assertNotNull(avg)
        assertEquals(150.0, avg!!, 0.001)
    }

    @Test
    fun `getMinPriceForCrop and getMaxPriceForCrop return correct extremes`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato", currentPrice = 80.0))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Tomato", currentPrice = 200.0))

        assertEquals(80.0, dao.getMinPriceForCrop("Tomato")!!, 0.001)
        assertEquals(200.0, dao.getMaxPriceForCrop("Tomato")!!, 0.001)
    }

    @Test
    fun `getMarketStatistics returns correct counts for mixed trends`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-up1", trend = PriceTrend.UP, currentPrice = 100.0, changePercentage = 10.0))
        dao.insertMarketPrice(makePrice("mp-up2", trend = PriceTrend.UP, currentPrice = 200.0, changePercentage = 20.0))
        dao.insertMarketPrice(makePrice("mp-down", trend = PriceTrend.DOWN, currentPrice = 50.0, changePercentage = -5.0))
        dao.insertMarketPrice(makePrice("mp-stable", trend = PriceTrend.STABLE, currentPrice = 150.0, changePercentage = 0.0))

        val stats = dao.getMarketStatistics()
        assertEquals(4, stats.total)
        assertEquals(2, stats.rising)
        assertEquals(1, stats.falling)
        assertEquals(1, stats.stable)
    }

    // ---- Flow ----

    @Test
    fun `getMarketPriceByIdFlow emits price after insert`() = runBlocking {
        val price = makePrice("mp-flow")
        dao.insertMarketPrice(price)

        val result = dao.getMarketPriceByIdFlow("mp-flow").first()
        assertNotNull(result)
        assertEquals("mp-flow", result!!.id)
    }

    @Test
    fun `getMarketPricesForCropFlow emits matching prices`() = runBlocking {
        dao.insertMarketPrice(makePrice("mp-1", cropType = "Tomato"))
        dao.insertMarketPrice(makePrice("mp-2", cropType = "Onion"))

        val result = dao.getMarketPricesForCropFlow("Tomato").first()
        assertEquals(1, result.size)
    }

    // ---- Update ----

    @Test
    fun `updateMarketPrice persists changes`() = runBlocking {
        val original = makePrice("mp-update", currentPrice = 100.0)
        dao.insertMarketPrice(original)

        dao.updateMarketPrice(original.copy(currentPrice = 200.0))

        val updated = dao.getMarketPriceById("mp-update")
        assertEquals(200.0, updated!!.currentPrice, 0.001)
    }

    @Test
    fun `deleteMarketPrice removes the entity`() = runBlocking {
        val price = makePrice("mp-del")
        dao.insertMarketPrice(price)
        dao.deleteMarketPrice(price)

        assertNull(dao.getMarketPriceById("mp-del"))
    }

    // ---- Deactivation & cleanup ----

    @Test
    fun `deactivateStaleMarketPrices sets isActive false for old records`() = runBlocking {
        // Insert a price with a timestamp far in the past (72 hours ago)
        val staleTs = Instant.now().minusSeconds(3600L * 72).toString()
        dao.insertMarketPrice(makePrice("mp-stale", isActive = true, lastUpdated = staleTs))
        dao.insertMarketPrice(makePrice("mp-fresh", isActive = true, lastUpdated = recentTs))

        dao.deactivateStaleMarketPrices(hoursThreshold = 48)

        val active = dao.getAllActiveMarketPrices()
        assertEquals(1, active.size)
        assertEquals("mp-fresh", active[0].id)
    }
}
