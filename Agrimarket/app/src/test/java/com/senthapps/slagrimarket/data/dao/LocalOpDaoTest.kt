package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OpType
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

// Minimal in-memory database scoped to LocalOp only — keeps tests independent
// of full database migrations and unrelated TypeConverters.
@Database(entities = [LocalOp::class], version = 1, exportSchema = false)
abstract class LocalOpTestDatabase : RoomDatabase() {
    abstract fun localOpDao(): LocalOpDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class LocalOpDaoTest {

    private lateinit var db: LocalOpTestDatabase
    private lateinit var dao: LocalOpDao

    private val now = Instant.now().toString()

    private fun makeOp(
        id: String,
        attempts: Int = 0,
        synced: Boolean = false,
        clientTs: String = now,
        errorMessage: String? = null
    ) = LocalOp(
        opId = id,
        type = OpType.CREATE_LISTING,
        payload = "{}",
        clientTs = clientTs,
        attempts = attempts,
        synced = synced,
        errorMessage = errorMessage
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, LocalOpTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.localOpDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insert / getOpById
    // -------------------------------------------------------------------------

    @Test
    fun `insertOp and getOpById round-trips correctly`() = runBlocking {
        val op = makeOp("op1")
        dao.insertOp(op)

        val retrieved = dao.getOpById("op1")

        assertNotNull(retrieved)
        assertEquals("op1", retrieved!!.opId)
        assertEquals(OpType.CREATE_LISTING, retrieved.type)
        assertFalse(retrieved.synced)
        assertEquals(0, retrieved.attempts)
    }

    @Test
    fun `insertOp with REPLACE strategy overwrites existing`() = runBlocking {
        dao.insertOp(makeOp("op1", attempts = 0))
        dao.insertOp(makeOp("op1", attempts = 2))

        assertEquals(2, dao.getOpById("op1")!!.attempts)
    }

    // -------------------------------------------------------------------------
    // getPendingOps — WHERE synced = 0 AND attempts < 3
    // -------------------------------------------------------------------------

    @Test
    fun `getPendingOps returns ops with attempts less than 3 and not synced`() = runBlocking {
        dao.insertOp(makeOp("pending-0", attempts = 0, synced = false))
        dao.insertOp(makeOp("pending-2", attempts = 2, synced = false))
        dao.insertOp(makeOp("failed-3", attempts = 3, synced = false))   // excluded
        dao.insertOp(makeOp("synced-0", attempts = 0, synced = true))    // excluded

        val pending = dao.getPendingOps()

        assertEquals(2, pending.size)
        assertTrue(pending.all { !it.synced && it.attempts < 3 })
    }

    @Test
    fun `getPendingOps returns empty when all ops are failed or synced`() = runBlocking {
        dao.insertOp(makeOp("f1", attempts = 3))
        dao.insertOp(makeOp("s1", synced = true))

        assertTrue(dao.getPendingOps().isEmpty())
    }

    // -------------------------------------------------------------------------
    // getFailedOps — WHERE attempts >= 3
    // -------------------------------------------------------------------------

    @Test
    fun `getFailedOps returns ops with 3 or more attempts`() = runBlocking {
        dao.insertOp(makeOp("ok", attempts = 2))
        dao.insertOp(makeOp("failed-3", attempts = 3))
        dao.insertOp(makeOp("failed-5", attempts = 5))

        val failed = dao.getFailedOps()

        assertEquals(2, failed.size)
        assertTrue(failed.all { it.attempts >= 3 })
    }

    // -------------------------------------------------------------------------
    // getPendingOpsCount
    // -------------------------------------------------------------------------

    @Test
    fun `getPendingOpsCount matches getPendingOps list size`() = runBlocking {
        dao.insertOp(makeOp("p1", attempts = 0))
        dao.insertOp(makeOp("p2", attempts = 1))
        dao.insertOp(makeOp("f1", attempts = 3))  // not counted

        assertEquals(2, dao.getPendingOpsCount())
    }

    // -------------------------------------------------------------------------
    // markOpAsSynced
    // -------------------------------------------------------------------------

    @Test
    fun `markOpAsSynced flips synced to true`() = runBlocking {
        dao.insertOp(makeOp("op1"))
        dao.markOpAsSynced("op1")

        assertTrue(dao.getOpById("op1")!!.synced)
    }

    @Test
    fun `markOpAsSynced removes op from pending list`() = runBlocking {
        dao.insertOp(makeOp("op1"))
        dao.markOpAsSynced("op1")

        assertTrue(dao.getPendingOps().isEmpty())
    }

    // -------------------------------------------------------------------------
    // markOpAsFailed
    // -------------------------------------------------------------------------

    @Test
    fun `markOpAsFailed sets attempts to 3 and stores error message`() = runBlocking {
        dao.insertOp(makeOp("op1", attempts = 1))
        dao.markOpAsFailed("op1", "conflict detected")

        val op = dao.getOpById("op1")!!
        assertEquals(3, op.attempts)
        assertEquals("conflict detected", op.errorMessage)
    }

    @Test
    fun `markOpAsFailed removes op from pending list`() = runBlocking {
        dao.insertOp(makeOp("op1"))
        dao.markOpAsFailed("op1", "rejected")

        assertTrue(dao.getPendingOps().isEmpty())
        assertEquals(1, dao.getFailedOps().size)
    }

    // -------------------------------------------------------------------------
    // resetOpStatus
    // -------------------------------------------------------------------------

    @Test
    fun `resetOpStatus zeroes attempts and clears error message`() = runBlocking {
        dao.insertOp(makeOp("op1", attempts = 3, errorMessage = "old error"))
        dao.resetOpStatus("op1")

        val op = dao.getOpById("op1")!!
        assertEquals(0, op.attempts)
        assertNull(op.errorMessage)
    }

    @Test
    fun `resetOpStatus makes op appear in pending again`() = runBlocking {
        dao.insertOp(makeOp("op1", attempts = 3))
        assertTrue(dao.getPendingOps().isEmpty())

        dao.resetOpStatus("op1")

        assertEquals(1, dao.getPendingOps().size)
    }

    // -------------------------------------------------------------------------
    // incrementAttempts
    // -------------------------------------------------------------------------

    @Test
    fun `incrementAttempts increments by 1 and records timestamp and error`() = runBlocking {
        dao.insertOp(makeOp("op1", attempts = 1))
        dao.incrementAttempts("op1", "2026-01-01T00:00:00Z", "timeout")

        val op = dao.getOpById("op1")!!
        assertEquals(2, op.attempts)
        assertEquals("2026-01-01T00:00:00Z", op.lastAttemptAt)
        assertEquals("timeout", op.errorMessage)
    }

    // -------------------------------------------------------------------------
    // deleteFailedOps
    // -------------------------------------------------------------------------

    @Test
    fun `deleteFailedOps removes ops with attempts at or above maxAttempts`() = runBlocking {
        dao.insertOp(makeOp("ok", attempts = 2))
        dao.insertOp(makeOp("failed", attempts = 3))
        dao.insertOp(makeOp("very-failed", attempts = 5))

        dao.deleteFailedOps(maxAttempts = 3)

        assertNull(dao.getOpById("failed"))
        assertNull(dao.getOpById("very-failed"))
        assertNotNull(dao.getOpById("ok"))
    }

    @Test
    fun `deleteFailedOps does not touch pending or synced ops`() = runBlocking {
        dao.insertOp(makeOp("pending", attempts = 0))
        dao.insertOp(makeOp("synced", attempts = 0, synced = true))

        dao.deleteFailedOps(maxAttempts = 3)

        assertNotNull(dao.getOpById("pending"))
        assertNotNull(dao.getOpById("synced"))
    }

    // -------------------------------------------------------------------------
    // deleteOldSyncedOps
    // -------------------------------------------------------------------------

    @Test
    fun `deleteOldSyncedOps removes synced ops older than cutoff`() = runBlocking {
        val old = "2026-01-01T00:00:00Z"
        val recent = "2026-06-01T00:00:00Z"
        val cutoff = "2026-03-01T00:00:00Z"

        dao.insertOp(makeOp("old-synced", synced = true, clientTs = old))
        dao.insertOp(makeOp("recent-synced", synced = true, clientTs = recent))
        dao.insertOp(makeOp("old-unsynced", synced = false, clientTs = old))  // not deleted

        dao.deleteOldSyncedOps(cutoff)

        assertNull(dao.getOpById("old-synced"))
        assertNotNull(dao.getOpById("recent-synced"))
        assertNotNull(dao.getOpById("old-unsynced"))
    }

    // -------------------------------------------------------------------------
    // deleteSyncedOps
    // -------------------------------------------------------------------------

    @Test
    fun `deleteSyncedOps removes all synced ops`() = runBlocking {
        dao.insertOp(makeOp("synced1", synced = true))
        dao.insertOp(makeOp("synced2", synced = true))
        dao.insertOp(makeOp("pending", synced = false))

        dao.deleteSyncedOps()

        assertNull(dao.getOpById("synced1"))
        assertNull(dao.getOpById("synced2"))
        assertNotNull(dao.getOpById("pending"))
    }

    // -------------------------------------------------------------------------
    // markOpsAsSynced (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `markOpsAsSynced marks multiple ops at once`() = runBlocking {
        dao.insertOp(makeOp("op1"))
        dao.insertOp(makeOp("op2"))
        dao.insertOp(makeOp("op3"))

        dao.markOpsAsSynced(listOf("op1", "op2"))

        assertTrue(dao.getOpById("op1")!!.synced)
        assertTrue(dao.getOpById("op2")!!.synced)
        assertFalse(dao.getOpById("op3")!!.synced)
    }

    // -------------------------------------------------------------------------
    // getUnsyncedCount
    // -------------------------------------------------------------------------

    @Test
    fun `getUnsyncedCount counts all unsynced regardless of attempts`() = runBlocking {
        dao.insertOp(makeOp("p1", attempts = 0, synced = false))
        dao.insertOp(makeOp("p2", attempts = 3, synced = false))  // failed but still unsynced
        dao.insertOp(makeOp("s1", synced = true))

        assertEquals(2, dao.getUnsyncedCount())
    }
}
