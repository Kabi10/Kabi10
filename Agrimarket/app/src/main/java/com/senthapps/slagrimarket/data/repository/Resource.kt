package com.senthapps.slagrimarket.data.repository

/**
 * A generic wrapper class that represents the state of a network operation
 * Provides Success, Error, and Loading states for UI consumption
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val exception: Throwable? = null
) {
    /**
     * Represents a successful operation with data
     */
    class Success<T>(data: T) : Resource<T>(data)
    
    /**
     * Represents an error state with optional data and error information
     */
    class Error<T>(message: String, exception: Throwable? = null, data: T? = null) : Resource<T>(data, message, exception)
    
    /**
     * Represents a loading state with optional cached data
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
    
    /**
     * Check if the resource is in success state
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if the resource is in error state
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Check if the resource is in loading state
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Get data if available, regardless of state
     */
    fun getDataOrNull(): T? = data
    
    /**
     * Get data or throw exception if in error state
     */
    fun getDataOrThrow(): T {
        return when (this) {
            is Success -> data!!
            is Error -> throw (exception ?: RuntimeException(message ?: "Unknown error"))
            is Loading -> data ?: throw IllegalStateException("Data not available in loading state")
        }
    }
    
    /**
     * Transform the data using the provided function
     */
    fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data!!))
            is Error -> Error(message ?: "Unknown error", exception, data?.let(transform))
            is Loading -> Loading(data?.let(transform))
        }
    }

    /**
     * Transform the data using a suspend function
     */
    suspend fun <R> mapSuspend(transform: suspend (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data!!))
            is Error -> Error(message ?: "Unknown error", exception, data?.let { transform(it) })
            is Loading -> Loading(data?.let { transform(it) })
        }
    }
    
    /**
     * Execute action if resource is in success state
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) {
            action(data!!)
        }
        return this
    }
    
    /**
     * Execute action if resource is in error state
     */
    inline fun onError(action: (String?, Throwable?) -> Unit): Resource<T> {
        if (this is Error) {
            action(message, exception)
        }
        return this
    }
    
    /**
     * Execute action if resource is in loading state
     */
    inline fun onLoading(action: (T?) -> Unit): Resource<T> {
        if (this is Loading) {
            action(data)
        }
        return this
    }
    
    /**
     * Get error message or default
     */
    fun getErrorMessage(default: String = "Unknown error"): String {
        return if (this is Error) message ?: default else default
    }
    
    /**
     * Get exception or null
     */
    fun getThrowable(): Throwable? {
        return if (this is Error) exception else null
    }
    
    companion object {
        /**
         * Create a success resource
         */
        fun <T> success(data: T): Resource<T> = Success(data)
        
        /**
         * Create an error resource
         */
        fun <T> error(message: String, exception: Throwable? = null, data: T? = null): Resource<T> = 
            Error(message, exception, data)
        
        /**
         * Create a loading resource
         */
        fun <T> loading(data: T? = null): Resource<T> = Loading(data)
    }
}

/**
 * Extension function to convert Result to Resource
 */
fun <T> Result<T>.toResource(): Resource<T> {
    return fold(
        onSuccess = { Resource.success(it) },
        onFailure = { Resource.error(it.message ?: "Unknown error", it) }
    )
}

/**
 * Extension function to convert Resource to Result
 */
fun <T> Resource<T>.toResult(): Result<T> {
    return when (this) {
        is Resource.Success -> Result.success(data!!)
        is Resource.Error -> Result.failure(exception ?: RuntimeException(message ?: "Unknown error"))
        is Resource.Loading -> if (data != null) Result.success(data!!) else Result.failure(IllegalStateException("Data not available"))
    }
}

/**
 * Combine two resources into one
 */
fun <T, R, S> combineResources(
    resource1: Resource<T>,
    resource2: Resource<R>,
    combiner: (T, R) -> S
): Resource<S> {
    return when {
        resource1 is Resource.Loading || resource2 is Resource.Loading -> {
            val combinedData = if (resource1.data != null && resource2.data != null) {
                combiner(resource1.data!!, resource2.data!!)
            } else null
            Resource.loading(combinedData)
        }
        resource1 is Resource.Error -> Resource.error(resource1.message ?: "Error in first resource", resource1.exception)
        resource2 is Resource.Error -> Resource.error(resource2.message ?: "Error in second resource", resource2.exception)
        resource1 is Resource.Success && resource2 is Resource.Success -> {
            Resource.success(combiner(resource1.data!!, resource2.data!!))
        }
        else -> Resource.error("Unknown state in resource combination")
    }
}

/**
 * Network state for monitoring connectivity
 */
sealed class NetworkState {
    object Available : NetworkState()
    object Unavailable : NetworkState()
    object Losing : NetworkState()
    object Lost : NetworkState()
}

/**
 * Sync state for tracking synchronization operations
 */
data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val pendingOperations: Int = 0,
    val successfulOperations: Int = 0,
    val failedOperations: Int = 0,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val hasPendingOperations: Boolean get() = pendingOperations > 0
    val isIdle: Boolean get() = !isSyncing && error == null
}

/**
 * Cache state for tracking data freshness
 */
data class CacheState(
    val isStale: Boolean = false,
    val lastUpdated: Long? = null,
    val expiresAt: Long? = null
) {
    val isExpired: Boolean get() = expiresAt?.let { System.currentTimeMillis() > it } ?: false
    val isValid: Boolean get() = !isStale && !isExpired
}
