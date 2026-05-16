package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.BatchSyncResponse
import com.senthapps.slagrimarket.data.model.SyncRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {

    @POST("v1/sync/operations")
    suspend fun syncOperations(@Body request: SyncRequest): Response<BatchSyncResponse>

    @GET("v1/sync/data")
    suspend fun getSyncData(
        @Query("lastSyncAt") lastSyncAt: String?,
        @Query("listingPage") listingPage: Int = 1,
        @Query("listingLimit") listingLimit: Int = 50,
        @Query("transactionPage") transactionPage: Int = 1,
        @Query("transactionLimit") transactionLimit: Int = 50
    ): Response<BatchSyncResponse>
}
