package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CropixApiService
import com.senthapps.slagrimarket.data.dao.DoaCropDao
import com.senthapps.slagrimarket.data.model.DoaCropEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropixRepository @Inject constructor(
    private val cropixApiService: CropixApiService,
    private val doaCropDao: DoaCropDao
) {
    companion object {
        // 7-day cache — DoA crop list changes rarely
        private const val CACHE_TTL_MS = 7L * 24 * 3600 * 1000
    }

    /** Reactive stream of cached DoA crops, sorted by description. */
    fun getCrops(): Flow<List<DoaCropEntity>> = doaCropDao.getAllCrops()

    /**
     * Sync crops from DoA API if cache is empty or stale.
     * Silently uses cache on network failure.
     */
    suspend fun syncCrops() {
        try {
            val count = doaCropDao.getCropCount()
            val oldestCache = doaCropDao.getOldestCacheTime()
            val isCacheStale = oldestCache == null ||
                (System.currentTimeMillis() - oldestCache) > CACHE_TTL_MS

            if (count > 0 && !isCacheStale) return

            val response = cropixApiService.getCrops()
            val crops = response.payload ?: return

            val entities = crops.map { dto ->
                DoaCropEntity(
                    id = dto.id,
                    cropId = dto.cropId,
                    description = dto.description,
                    cropType = dto.cropType,
                    scientificName = dto.scientificName,
                    categoryId = dto.cropId.substringBeforeLast("/").ifEmpty { null }
                )
            }
            doaCropDao.insertCrops(entities)
            Timber.d("CROPIX: synced ${entities.size} crops from DoA")
        } catch (e: Exception) {
            Timber.w(e, "CROPIX: sync failed, using cached/fallback data")
        }
    }
}
