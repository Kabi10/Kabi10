package com.senthapps.slagrimarket.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Comprehensive error response models for API operations
 * Provides structured error handling with proper error codes and messages
 */

/**
 * Standard API error response model
 */
@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "success")
    val success: Boolean = false,
    
    @Json(name = "error")
    val error: ApiError,
    
    @Json(name = "timestamp")
    val timestamp: String,
    
    @Json(name = "path")
    val path: String? = null,
    
    @Json(name = "requestId")
    val requestId: String? = null
)

/**
 * Detailed error information
 */
@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code")
    val code: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "details")
    val details: String? = null,
    
    @Json(name = "field")
    val field: String? = null,
    
    @Json(name = "validationErrors")
    val validationErrors: List<ValidationError>? = null
)

/**
 * Validation error for form fields
 */
@JsonClass(generateAdapter = true)
data class ValidationError(
    @Json(name = "field")
    val field: String,
    
    @Json(name = "code")
    val code: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "rejectedValue")
    val rejectedValue: String? = null
)

/**
 * Network error wrapper for client-side errors
 */
data class NetworkError(
    val type: NetworkErrorType,
    val message: String,
    val cause: Throwable? = null
)

/**
 * Types of network errors
 */
enum class NetworkErrorType {
    NO_INTERNET,
    TIMEOUT,
    SERVER_ERROR,
    AUTHENTICATION_ERROR,
    AUTHORIZATION_ERROR,
    VALIDATION_ERROR,
    NOT_FOUND,
    CONFLICT,
    RATE_LIMITED,
    UNKNOWN
}

/**
 * Common API error codes
 */
object ApiErrorCodes {
    // Authentication errors
    const val TOKEN_MISSING = "TOKEN_MISSING"
    const val TOKEN_INVALID = "TOKEN_INVALID"
    const val TOKEN_EXPIRED = "TOKEN_EXPIRED"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    
    // Validation errors
    const val VALIDATION_FAILED = "VALIDATION_FAILED"
    const val REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING"
    const val INVALID_FORMAT = "INVALID_FORMAT"
    const val VALUE_OUT_OF_RANGE = "VALUE_OUT_OF_RANGE"
    const val DUPLICATE_VALUE = "DUPLICATE_VALUE"
    
    // Resource errors
    const val RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND"
    const val RESOURCE_CONFLICT = "RESOURCE_CONFLICT"
    const val RESOURCE_LOCKED = "RESOURCE_LOCKED"
    const val RESOURCE_EXPIRED = "RESOURCE_EXPIRED"
    
    // Business logic errors
    const val INSUFFICIENT_QUANTITY = "INSUFFICIENT_QUANTITY"
    const val LISTING_EXPIRED = "LISTING_EXPIRED"
    const val LISTING_INACTIVE = "LISTING_INACTIVE"
    const val TRANSACTION_ALREADY_EXISTS = "TRANSACTION_ALREADY_EXISTS"
    const val INVALID_TRANSACTION_STATUS = "INVALID_TRANSACTION_STATUS"
    
    // System errors
    const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
    const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
    const val DATABASE_ERROR = "DATABASE_ERROR"
    const val EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR"
    
    // Rate limiting
    const val RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED"
    const val QUOTA_EXCEEDED = "QUOTA_EXCEEDED"
    
    // Sync errors
    const val SYNC_CONFLICT = "SYNC_CONFLICT"
    const val SYNC_VERSION_MISMATCH = "SYNC_VERSION_MISMATCH"
    const val SYNC_DATA_CORRUPTED = "SYNC_DATA_CORRUPTED"
}

/**
 * HTTP status code mappings
 */
object HttpStatusCodes {
    const val OK = 200
    const val CREATED = 201
    const val NO_CONTENT = 204
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val CONFLICT = 409
    const val UNPROCESSABLE_ENTITY = 422
    const val TOO_MANY_REQUESTS = 429
    const val INTERNAL_SERVER_ERROR = 500
    const val SERVICE_UNAVAILABLE = 503
}

/**
 * Extension functions for error handling
 */
fun ApiErrorResponse.isAuthenticationError(): Boolean {
    return error.code in listOf(
        ApiErrorCodes.TOKEN_MISSING,
        ApiErrorCodes.TOKEN_INVALID,
        ApiErrorCodes.TOKEN_EXPIRED,
        ApiErrorCodes.UNAUTHORIZED
    )
}

fun ApiErrorResponse.isValidationError(): Boolean {
    return error.code == ApiErrorCodes.VALIDATION_FAILED || 
           error.validationErrors?.isNotEmpty() == true
}

fun ApiErrorResponse.isResourceError(): Boolean {
    return error.code in listOf(
        ApiErrorCodes.RESOURCE_NOT_FOUND,
        ApiErrorCodes.RESOURCE_CONFLICT,
        ApiErrorCodes.RESOURCE_LOCKED,
        ApiErrorCodes.RESOURCE_EXPIRED
    )
}

fun ApiErrorResponse.isServerError(): Boolean {
    return error.code in listOf(
        ApiErrorCodes.INTERNAL_SERVER_ERROR,
        ApiErrorCodes.SERVICE_UNAVAILABLE,
        ApiErrorCodes.DATABASE_ERROR,
        ApiErrorCodes.EXTERNAL_SERVICE_ERROR
    )
}

fun ApiErrorResponse.isRateLimitError(): Boolean {
    return error.code in listOf(
        ApiErrorCodes.RATE_LIMIT_EXCEEDED,
        ApiErrorCodes.QUOTA_EXCEEDED
    )
}

fun ApiErrorResponse.isSyncError(): Boolean {
    return error.code in listOf(
        ApiErrorCodes.SYNC_CONFLICT,
        ApiErrorCodes.SYNC_VERSION_MISMATCH,
        ApiErrorCodes.SYNC_DATA_CORRUPTED
    )
}

/**
 * Helper function to create network error from HTTP status code
 */
fun createNetworkErrorFromHttpCode(statusCode: Int, message: String): NetworkError {
    val errorType = when (statusCode) {
        HttpStatusCodes.UNAUTHORIZED -> NetworkErrorType.AUTHENTICATION_ERROR
        HttpStatusCodes.FORBIDDEN -> NetworkErrorType.AUTHORIZATION_ERROR
        HttpStatusCodes.NOT_FOUND -> NetworkErrorType.NOT_FOUND
        HttpStatusCodes.CONFLICT -> NetworkErrorType.CONFLICT
        HttpStatusCodes.UNPROCESSABLE_ENTITY -> NetworkErrorType.VALIDATION_ERROR
        HttpStatusCodes.TOO_MANY_REQUESTS -> NetworkErrorType.RATE_LIMITED
        in 500..599 -> NetworkErrorType.SERVER_ERROR
        else -> NetworkErrorType.UNKNOWN
    }
    
    return NetworkError(
        type = errorType,
        message = message
    )
}

/**
 * Helper function to create timeout error
 */
fun createTimeoutError(): NetworkError {
    return NetworkError(
        type = NetworkErrorType.TIMEOUT,
        message = "Request timed out. Please check your internet connection and try again."
    )
}

/**
 * Helper function to create no internet error
 */
fun createNoInternetError(): NetworkError {
    return NetworkError(
        type = NetworkErrorType.NO_INTERNET,
        message = "No internet connection. Please check your network settings and try again."
    )
}

/**
 * Helper function to get user-friendly error message
 */
fun ApiErrorResponse.getUserFriendlyMessage(): String {
    return when (error.code) {
        ApiErrorCodes.TOKEN_EXPIRED -> "Your session has expired. Please log in again."
        ApiErrorCodes.UNAUTHORIZED -> "You are not authorized to perform this action."
        ApiErrorCodes.FORBIDDEN -> "Access denied. You don't have permission for this action."
        ApiErrorCodes.RESOURCE_NOT_FOUND -> "The requested item was not found."
        ApiErrorCodes.VALIDATION_FAILED -> "Please check your input and try again."
        ApiErrorCodes.INSUFFICIENT_QUANTITY -> "Not enough quantity available for this request."
        ApiErrorCodes.LISTING_EXPIRED -> "This listing has expired and is no longer available."
        ApiErrorCodes.RATE_LIMIT_EXCEEDED -> "Too many requests. Please wait a moment and try again."
        ApiErrorCodes.INTERNAL_SERVER_ERROR -> "Something went wrong on our end. Please try again later."
        ApiErrorCodes.SERVICE_UNAVAILABLE -> "Service is temporarily unavailable. Please try again later."
        else -> error.message
    }
}
