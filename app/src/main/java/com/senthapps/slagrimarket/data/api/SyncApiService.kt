package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.SyncRequest
import com.senthapps.slagrimarket.data.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApiService {
    
    @POST("v1/sync")
    suspend fun syncData(@Body request: SyncRequest): Response<SyncResponse>
}
