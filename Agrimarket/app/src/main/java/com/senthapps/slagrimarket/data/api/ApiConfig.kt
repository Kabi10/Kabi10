package com.senthapps.slagrimarket.data.api

/**
 * API configuration constants and endpoints
 * Centralized configuration for all API-related constants
 */
object ApiConfig {
    
    // ============================================================================
    // BASE URLS
    // ============================================================================

    const val PRODUCTION_BASE_URL = "https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/"
    const val STAGING_BASE_URL = "https://agrimarket-staging.vercel.app/api/"
    const val DEVELOPMENT_BASE_URL = "http://localhost:3000/api/"
    
    // ============================================================================
    // API VERSIONS
    // ============================================================================
    
    const val API_VERSION_V1 = "v1"
    
    // ============================================================================
    // ENDPOINT PATHS
    // ============================================================================
    
    object Endpoints {
        // Authentication endpoints (v1 prefix, no .js suffix)
        const val AUTH_SEND_OTP = "v1/auth/send-otp"
        const val AUTH_VERIFY_OTP = "v1/auth/verify-otp"
        const val AUTH_REFRESH_TOKEN = "v1/auth/refresh-token"

        // User endpoints
        const val USERS_PROFILE = "users/profile"
        const val USERS_UPDATE_PROFILE = "users/profile"

        // Listing endpoints
        const val LISTINGS = "listings"
        const val LISTINGS_BY_ID = "listings/{id}"
        const val LISTINGS_BY_FARMER = "listings/farmer/{farmerId}"
        const val LISTINGS_TRENDING = "listings/trending"
        const val LISTINGS_SEARCH = "listings/search"
        const val LISTINGS_VIEW = "listings/{id}/view"
        const val LISTINGS_INQUIRY = "listings/{id}/inquiry"
        const val LISTINGS_IMAGES = "listings/{id}/images"
        const val LISTINGS_IMAGES_CONFIRM = "listings/{id}/images/confirm"

        // Market price endpoints
        const val MARKET_PRICES = "market-prices"
        const val MARKET_PRICES_BY_ID = "market-prices/{id}"
        const val MARKET_PRICES_TRENDING = "market-prices/trending"
        const val MARKET_PRICES_STATISTICS = "market-prices/statistics"
        const val MARKET_PRICES_HISTORY = "market-prices/history"
        const val MARKET_PRICES_SEARCH = "market-prices/search"
        const val MARKET_PRICES_BY_LOCATION = "market-prices/by-location"
        const val MARKET_PRICES_LATEST = "market-prices/latest"

        // Transaction endpoints
        const val TRANSACTIONS = "transactions"
        const val TRANSACTIONS_BY_ID = "transactions/{id}"
        const val TRANSACTIONS_UPDATE_STATUS = "transactions/{id}"

        // Activity endpoints
        const val ACTIVITIES = "activities"
        const val ACTIVITIES_BY_ID = "activities/{id}"
        const val ACTIVITIES_UNREAD_COUNT = "activities/unread-count"
        const val ACTIVITIES_ACTIONABLE_COUNT = "activities/actionable-count"
        const val ACTIVITIES_SUMMARY = "activities/summary"
        const val ACTIVITIES_RECENT = "activities/recent"
        const val ACTIVITIES_MARK_READ = "activities/{id}/read"
        const val ACTIVITIES_MARK_ALL_READ = "activities/mark-all-read"
        const val ACTIVITIES_DISMISS = "activities/{id}/dismiss"
        const val ACTIVITIES_ARCHIVE = "activities/{id}/archive"

        // Sync endpoints
        const val SYNC_OPERATIONS = "sync/operations"
        const val SYNC_STATUS = "sync/status"

        // Health check endpoints
        const val HEALTH = "health"
        const val HEALTH_DETAILED = "health/detailed"
    }
    
    // ============================================================================
    // REQUEST/RESPONSE LIMITS
    // ============================================================================
    
    object Limits {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        const val MIN_PAGE_SIZE = 1
        
        const val MAX_SEARCH_QUERY_LENGTH = 100
        const val MAX_DESCRIPTION_LENGTH = 1000
        const val MAX_TITLE_LENGTH = 200
        
        const val MAX_IMAGE_SIZE_MB = 5
        const val MAX_IMAGES_PER_LISTING = 5
        
        const val MAX_SYNC_OPERATIONS_PER_REQUEST = 50
    }
    
    // ============================================================================
    // TIMEOUT CONFIGURATIONS
    // ============================================================================
    
    object Timeouts {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 60L
        const val WRITE_TIMEOUT_SECONDS = 30L
        const val CALL_TIMEOUT_SECONDS = 90L
        
        // Specific timeouts for different operations
        const val AUTH_TIMEOUT_SECONDS = 15L
        const val SYNC_TIMEOUT_SECONDS = 120L
        const val IMAGE_UPLOAD_TIMEOUT_SECONDS = 180L
    }
    
    // ============================================================================
    // RETRY CONFIGURATIONS
    // ============================================================================
    
    object Retry {
        const val MAX_RETRY_ATTEMPTS = 3
        const val INITIAL_RETRY_DELAY_MS = 1000L
        const val MAX_RETRY_DELAY_MS = 10000L
        const val RETRY_MULTIPLIER = 2.0
        
        // Retry-able HTTP status codes
        val RETRYABLE_STATUS_CODES = setOf(408, 429, 500, 502, 503, 504)
        
        // Retry-able error codes
        val RETRYABLE_ERROR_CODES = setOf(
            ApiErrorCodes.SERVICE_UNAVAILABLE,
            ApiErrorCodes.EXTERNAL_SERVICE_ERROR,
            ApiErrorCodes.RATE_LIMIT_EXCEEDED
        )
    }
    
    // ============================================================================
    // CACHE CONFIGURATIONS
    // ============================================================================
    
    object Cache {
        const val MARKET_PRICES_CACHE_DURATION_MINUTES = 5L
        const val LISTINGS_CACHE_DURATION_MINUTES = 10L
        const val USER_PROFILE_CACHE_DURATION_MINUTES = 30L
        const val ACTIVITIES_CACHE_DURATION_MINUTES = 2L
        
        const val MAX_CACHE_SIZE_MB = 50L
        const val CACHE_DIRECTORY_NAME = "api_cache"
    }
    
    // ============================================================================
    // HEADER NAMES
    // ============================================================================
    
    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
        const val USER_AGENT = "User-Agent"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val CACHE_CONTROL = "Cache-Control"
        const val IF_NONE_MATCH = "If-None-Match"
        const val ETAG = "ETag"
        
        // Custom headers
        const val API_VERSION = "X-API-Version"
        const val CLIENT_VERSION = "X-Client-Version"
        const val DEVICE_ID = "X-Device-ID"
        const val REQUEST_ID = "X-Request-ID"
        const val LANGUAGE = "X-Language"
    }
    
    // ============================================================================
    // CONTENT TYPES
    // ============================================================================
    
    object ContentTypes {
        const val APPLICATION_JSON = "application/json"
        const val MULTIPART_FORM_DATA = "multipart/form-data"
        const val APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded"
        const val TEXT_PLAIN = "text/plain"
    }
    
    // ============================================================================
    // QUERY PARAMETER NAMES
    // ============================================================================
    
    object QueryParams {
        // Pagination
        const val PAGE = "page"
        const val LIMIT = "limit"
        const val OFFSET = "offset"
        
        // Sorting
        const val SORT_BY = "sortBy"
        const val SORT_ORDER = "sortOrder"
        
        // Filtering
        const val SEARCH_QUERY = "q"
        const val LANGUAGE = "language"
        const val CROP_TYPE = "cropType"
        const val LOCATION = "location"
        const val QUALITY = "quality"
        const val MIN_PRICE = "minPrice"
        const val MAX_PRICE = "maxPrice"
        const val IS_ACTIVE = "isActive"
        const val FARMER_ID = "farmerId"
        const val BUYER_ID = "buyerId"
        const val STATUS = "status"
        const val PRIORITY = "priority"
        const val ACTIVITY_TYPE = "activityType"
        const val IS_READ = "isRead"
        const val IS_ACTIONABLE = "isActionable"
        const val FROM_DATE = "fromDate"
        const val TO_DATE = "toDate"
        const val TIMEFRAME = "timeframe"
        const val TREND = "trend"
        const val AVAILABLE_FROM = "availableFrom"
        const val AVAILABLE_UNTIL = "availableUntil"
        
        // Market prices specific
        const val DAYS = "days"
        const val INTERVAL = "interval"
        const val TYPE = "type"
    }
    
    // ============================================================================
    // SORT ORDERS
    // ============================================================================
    
    object SortOrders {
        const val ASC = "asc"
        const val DESC = "desc"
    }
    
    // ============================================================================
    // TIMEFRAMES
    // ============================================================================
    
    object Timeframes {
        const val ONE_HOUR = "1h"
        const val TWENTY_FOUR_HOURS = "24h"
        const val SEVEN_DAYS = "7d"
        const val THIRTY_DAYS = "30d"
    }
    
    // ============================================================================
    // INTERVALS
    // ============================================================================
    
    object Intervals {
        const val HOURLY = "hourly"
        const val DAILY = "daily"
        const val WEEKLY = "weekly"
        const val MONTHLY = "monthly"
    }
    
    // ============================================================================
    // LANGUAGES
    // ============================================================================
    
    object Languages {
        const val ENGLISH = "en"
        const val TAMIL = "ta"
        const val SINHALA = "si"
    }
}
