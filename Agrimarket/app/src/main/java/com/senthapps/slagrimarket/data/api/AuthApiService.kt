package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("v1/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    @POST("v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("v1/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
}

@JsonClass(generateAdapter = true)
data class SendOtpRequest(
    @Json(name = "phoneNumber")
    val phone: String
)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String,

    @Json(name = "otpId")
    val otpId: String? = null,

    @Json(name = "otp")
    val otp: String? = null // For development mode
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "phone")
    val phone: String,
    
    @Json(name = "otp")
    val otp: String,
    
    @Json(name = "otpId")
    val otpId: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyOtpResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "message")
    val message: String? = null,
    
    @Json(name = "token")
    val token: String? = null,
    
    @Json(name = "refreshToken")
    val refreshToken: String? = null,
    
    @Json(name = "user")
    val user: User? = null
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "phoneNumber") val phoneNumber: String,
    @Json(name = "password") val password: String,
    @Json(name = "userType") val userType: String = "BUYER"
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "phoneNumber") val phoneNumber: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "user") val user: com.senthapps.slagrimarket.data.model.User? = null
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refreshToken")
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "token")
    val token: String? = null,
    
    @Json(name = "refreshToken")
    val refreshToken: String? = null
)
