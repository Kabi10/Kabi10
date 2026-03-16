package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OpType
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalOpDao {
    
    @Query("SELECT * FROM local_ops WHERE opId = :opId")
    suspend fun getOpById(opId: String): LocalOp?
    
    @Query("SELECT * FROM local_ops WHERE synced = 0 ORDER BY clientTs ASC")
    suspend fun getUnsyncedOps(): List<LocalOp>
    
    @Query("SELECT * FROM local_ops WHERE synced = 0 ORDER BY clientTs ASC")
    fun getUnsyncedOpsFlow(): Flow<List<LocalOp>>
    
    @Query("SELECT * FROM local_ops WHERE type = :type ORDER BY clientTs DESC")
    suspend fun getOpsByType(type: OpType): List<LocalOp>
    
    @Query("SELECT * FROM local_ops WHERE attempts < :maxAttempts AND synced = 0 ORDER BY clientTs ASC")
    suspend fun getRetryableOps(maxAttempts: Int = 3): List<LocalOp>
    
    @Query("SELECT COUNT(*) FROM local_ops WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Query("SELECT COUNT(*) FROM local_ops WHERE synced = 0")
    fun getUnsyncedCountFlow(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOp(op: LocalOp)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOps(ops: List<LocalOp>)
    
    @Update
    suspend fun updateOp(op: LocalOp)
    
    @Query("UPDATE local_ops SET synced = 1 WHERE opId = :opId")
    suspend fun markOpAsSynced(opId: String)
    
    @Query("UPDATE local_ops SET synced = 1 WHERE opId IN (:opIds)")
    suspend fun markOpsAsSynced(opIds: List<String>)
    
    @Query("""
        UPDATE local_ops 
        SET attempts = attempts + 1, 
            lastAttemptAt = :timestamp,
            errorMessage = :errorMessage
        WHERE opId = :opId
    """)
    suspend fun incrementAttempts(opId: String, timestamp: String, errorMessage: String?)
    
    @Delete
    suspend fun deleteOp(op: LocalOp)
    
    @Query("DELETE FROM local_ops WHERE opId = :opId")
    suspend fun deleteOpById(opId: String)
    
    @Query("DELETE FROM local_ops WHERE synced = 1")
    suspend fun deleteSyncedOps()
    
    @Query("DELETE FROM local_ops WHERE attempts >= :maxAttempts")
    suspend fun deleteFailedOps(maxAttempts: Int = 5)
    
    @Query("DELETE FROM local_ops")
    suspend fun deleteAllOps()

    // Additional methods for SyncManager
    @Query("SELECT * FROM local_ops WHERE synced = 0 AND attempts < 3 ORDER BY clientTs ASC")
    suspend fun getPendingOps(): List<LocalOp>

    @Query("SELECT * FROM local_ops WHERE attempts >= 3 ORDER BY clientTs ASC")
    suspend fun getFailedOps(): List<LocalOp>

    @Query("SELECT COUNT(*) FROM local_ops WHERE synced = 0 AND attempts < 3")
    suspend fun getPendingOpsCount(): Int

    @Query("UPDATE local_ops SET attempts = 3, errorMessage = :errorMessage WHERE opId = :opId")
    suspend fun markOpAsFailed(opId: String, errorMessage: String)

    @Query("UPDATE local_ops SET attempts = 0, errorMessage = NULL WHERE opId = :opId")
    suspend fun resetOpStatus(opId: String)

    @Query("DELETE FROM local_ops WHERE synced = 1 AND clientTs < :cutoffTs")
    suspend fun deleteOldSyncedOps(cutoffTs: String)
}
