package com.senthapps.slagrimarket.data.api

import retrofit2.http.GET

/**
 * Retrofit interface for the Sri Lanka DoA CROPIX public API.
 * Base URL: https://digital.doa.gov.lk/
 * No auth required for crop-details endpoints (live-tested 2026-03-03).
 */
interface CropixApiService {

    @GET("cropix/api/v1/crop-details/crop-categories")
    suspend fun getCropCategories(): CropCategoriesResponse

    @GET("cropix/api/v1/crop-details/crops")
    suspend fun getCrops(): CropsResponse
}
