package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface TransactionApiService {
    
    @POST("v1/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<Transaction>
    
    @GET("v1/transactions/{id}")
    suspend fun getTransactionById(@Path("id") transactionId: String): Response<Transaction>
    
    @PATCH("v1/transactions/{id}")
    suspend fun updateTransactionStatus(
        @Path("id") transactionId: String,
        @Body request: UpdateTransactionStatusRequest
    ): Response<Transaction>
    
    @GET("v1/transactions")
    suspend fun getTransactions(
        @Query("farmerId") farmerId: String? = null,
        @Query("buyerId") buyerId: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TransactionsResponse>
}

@JsonClass(generateAdapter = true)
data class CreateTransactionRequest(
    @Json(name = "listingId")
    val listingId: String,

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "totalAmount")
    val totalAmount: Double,

    @Json(name = "pricePerUnit")
    val pricePerUnit: Double,

    @Json(name = "unit")
    val unit: String = "kg",

    @Json(name = "pickupDate")
    val pickupDate: String,

    @Json(name = "pickupLocation")
    val pickupLocation: String,

    @Json(name = "pickupLocationTamil")
    val pickupLocationTamil: String = "",

    @Json(name = "pickupLocationSinhala")
    val pickupLocationSinhala: String = "",

    @Json(name = "pickupTime")
    val pickupTime: String = "",

    @Json(name = "paymentMethod")
    val paymentMethod: String = "CASH",

    @Json(name = "buyerContact")
    val buyerContact: String? = null,

    @Json(name = "notes")
    val notes: String? = null,

    @Json(name = "notesTamil")
    val notesTamil: String = "",

    @Json(name = "notesSinhala")
    val notesSinhala: String = ""
)

@JsonClass(generateAdapter = true)
data class UpdateTransactionStatusRequest(
    @Json(name = "status")
    val status: TransactionStatus,
    
    @Json(name = "notes")
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class TransactionsResponse(
    @Json(name = "transactions")
    val transactions: List<Transaction>,
    
    @Json(name = "totalCount")
    val totalCount: Int,
    
    @Json(name = "page")
    val page: Int,
    
    @Json(name = "totalPages")
    val totalPages: Int
)
