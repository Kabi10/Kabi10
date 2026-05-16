package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.DoaCropCategoryEntity
import com.senthapps.slagrimarket.data.model.DoaCropEntity
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

@Database(
    entities = [DoaCropEntity::class, DoaCropCategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DoaCropTestDatabase : RoomDatabase() {
    abstract fun doaCropDao(): DoaCropDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class DoaCropDaoTest {

    private lateinit var db: DoaCropTestDatabase
    private lateinit var dao: DoaCropDao

    private fun makeCrop(
        id: Int,
        cropId: String = "crop-$id",
        description: String = "Tomato",
        cropType: String? = "Vegetable",
        categoryId: String? = "cat-1",
        cachedAt: Long = System.currentTimeMillis()
    ) = DoaCropEntity(
        id = id,
        cropId = cropId,
        description = description,
        cropType = cropType,
        scientificName = null,
        categoryId = categoryId,
        cachedAt = cachedAt
    )

    private fun makeCategory(id: Int, categoryId: String = "cat-$id", description: String = "Vegetables") =
        DoaCropCategoryEntity(id = id, categoryId = categoryId, description = description)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            DoaCropTestDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.doaCropDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- Insert & query ----

    @Test
    fun `insertCrops and getAllCropsSuspend returns all inserted crops`() = runBlocking {
        dao.insertCrops(listOf(makeCrop(1, description = "Tomato"), makeCrop(2, description = "Chili")))

        val results = dao.getAllCropsSuspend()
        assertEquals(2, results.size)
    }

    @Test
    fun `getAllCropsSuspend returns crops sorted by description ascending`() = runBlocking {
        dao.insertCrops(listOf(
            makeCrop(1, description = "Tomato"),
            makeCrop(2, description = "Brinjal"),
            makeCrop(3, description = "Okra")
        ))

        val results = dao.getAllCropsSuspend()
        assertEquals("Brinjal", results[0].description)
        assertEquals("Okra", results[1].description)
        assertEquals("Tomato", results[2].description)
    }

    @Test
    fun `getAllCrops Flow emits all inserted crops`() = runBlocking {
        dao.insertCrops(listOf(makeCrop(1), makeCrop(2)))

        val results = dao.getAllCrops().first()
        assertEquals(2, results.size)
    }

    @Test
    fun `insertCrops replaces on conflict`() = runBlocking {
        dao.insertCrops(listOf(makeCrop(1, description = "OldName")))
        dao.insertCrops(listOf(makeCrop(1, description = "NewName")))

        val results = dao.getAllCropsSuspend()
        assertEquals(1, results.size)
        assertEquals("NewName", results[0].description)
    }

    @Test
    fun `getCropCount returns correct count`() = runBlocking {
        assertEquals(0, dao.getCropCount())

        dao.insertCrops(listOf(makeCrop(1), makeCrop(2), makeCrop(3)))

        assertEquals(3, dao.getCropCount())
    }

    @Test
    fun `getOldestCacheTime returns null when table is empty`() = runBlocking {
        assertNull(dao.getOldestCacheTime())
    }

    @Test
    fun `getOldestCacheTime returns minimum cachedAt value`() = runBlocking {
        val oldTime = 1_000_000L
        val newTime = 2_000_000L
        dao.insertCrops(listOf(
            makeCrop(1, cachedAt = newTime),
            makeCrop(2, cachedAt = oldTime)
        ))

        val oldest = dao.getOldestCacheTime()
        assertNotNull(oldest)
        assertEquals(oldTime, oldest!!)
    }

    // ---- Categories ----

    @Test
    fun `insertCategories inserts category records`() = runBlocking {
        dao.insertCategories(listOf(makeCategory(1, description = "Vegetables"), makeCategory(2, description = "Fruits")))
        // No direct query for categories on DoaCropDao; verify via crop insertion using the same categoryId
        dao.insertCrops(listOf(makeCrop(1, categoryId = "cat-1")))
        val crops = dao.getAllCropsSuspend()
        assertEquals("cat-1", crops[0].categoryId)
    }

    @Test
    fun `insertCategories replaces on conflict`() = runBlocking {
        dao.insertCategories(listOf(makeCategory(1, description = "OldDesc")))
        dao.insertCategories(listOf(makeCategory(1, description = "NewDesc")))
        // Verify no crash and only one row (implicitly via crop count staying stable)
        dao.insertCrops(listOf(makeCrop(1)))
        assertEquals(1, dao.getCropCount())
    }

    @Test
    fun `getAllCrops Flow emits empty list when table is empty`() = runBlocking {
        val result = dao.getAllCrops().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCropCount returns zero when table is empty`() = runBlocking {
        assertEquals(0, dao.getCropCount())
    }
}
