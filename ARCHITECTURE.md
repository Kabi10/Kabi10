# Architecture Documentation

## Overview

The Agrimarket Android app follows **Clean Architecture** principles with an **MVVM (Model-View-ViewModel)** pattern and an **offline-first** design philosophy. This ensures the app works seamlessly even with poor or no internet connectivity, which is critical for farmers in rural areas.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  - Screens (Composables)                                 │
│  - Navigation                                            │
│  - UI State Management                                   │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                  ViewModel Layer                         │
│  - Business Logic                                        │
│  - State Management (StateFlow)                          │
│  - Event Handling                                        │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                 Repository Layer                         │
│  - Data Source Coordination                              │
│  - Offline-First Logic                                   │
│  - Sync Management                                       │
└─────────────┬───────────────────┬───────────────────────┘
              │                   │
              ▼                   ▼
┌─────────────────────┐  ┌──────────────────────┐
│   Local Data Source  │  │ Remote Data Source   │
│   (Room Database)    │  │ (Retrofit + API)     │
└─────────────────────┘  └──────────────────────┘
```

## Core Components

### 1. UI Layer (Jetpack Compose)

**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/`

The UI is built entirely with **Jetpack Compose** using **Material Design 3** components.

**Key Screens**:
- `HomeScreen.kt` - Dashboard with recent activities and quick actions
- `ListingsScreen.kt` - Browse all marketplace listings
- `ListingDetailScreen.kt` - Detailed view of a single listing
- `CreateListingScreen.kt` - Form to create new listings
- `TransactionsScreen.kt` - View all transactions
- `ProfileScreen.kt` - User profile and settings
- `SearchScreen.kt` - Search and filter listings
- `AnalyticsScreen.kt` - Farmer analytics dashboard

**Navigation**: 
- Uses Jetpack Navigation Compose
- Bottom navigation bar for main sections
- Deep linking support for notifications

### 2. ViewModel Layer

**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/*/`

ViewModels manage UI state and business logic using **StateFlow** for reactive updates.

**Key ViewModels**:
- `HomeViewModel` - Manages home screen state and recent activities
- `ListingsViewModel` - Handles listing data and filtering
- `AuthViewModel` - Manages authentication state
- `TransactionsViewModel` - Transaction management
- `SearchViewModel` - Search and filter logic
- `LanguageToggleViewModel` - Trilingual language switching

**State Management Pattern**:
```kotlin
data class ListingsUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ListingsViewModel @Inject constructor(
    private val repository: ListingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()
    
    fun loadListings() {
        viewModelScope.launch {
            repository.getAllActiveListings()
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Resource.Success -> _uiState.update { 
                            it.copy(listings = resource.data, isLoading = false) 
                        }
                        is Resource.Error -> _uiState.update { 
                            it.copy(error = resource.message, isLoading = false) 
                        }
                    }
                }
        }
    }
}
```

### 3. Repository Layer

**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/repository/`

Repositories coordinate between local and remote data sources, implementing the **offline-first** pattern.

**Key Repositories**:
- `ListingRepository` - Listing CRUD operations
- `TransactionRepository` - Transaction management
- `AuthRepository` - Authentication (currently demo mode)
- `MarketPriceRepository` - Market price data
- `ActivityRepository` - User activity tracking
- `StorageRepository` - Image upload to Firebase Storage

**Offline-First Pattern**:
```kotlin
fun getAllActiveListings(forceRefresh: Boolean = false): Flow<Resource<List<Listing>>> = flow {
    emit(Resource.Loading())
    
    // 1. Always emit cached data first (offline-first)
    val cachedListings = listingDao.getAllActiveListings()
    if (cachedListings.isNotEmpty()) {
        emit(Resource.Success(cachedListings))
    }
    
    // 2. Check if we need to refresh from network
    val shouldRefresh = forceRefresh || shouldRefreshListings()
    
    if (shouldRefresh) {
        try {
            // 3. Fetch from network
            val response = listingApiService.getListings(limit = 100)
            if (response.isSuccessful) {
                val networkListings = response.body()?.listings ?: emptyList()
                
                // 4. Update local database (single source of truth)
                listingDao.insertListings(networkListings)
                
                // 5. Emit updated data
                emit(Resource.Success(networkListings))
            }
        } catch (e: Exception) {
            // Network failed, but we already emitted cached data
            if (cachedListings.isEmpty()) {
                emit(Resource.Error("No internet connection", e))
            }
        }
    }
}
```

### 4. Local Data Source (Room Database)

**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/`

**Database**: `JaffnaMarketplaceDatabase.kt`

**DAOs (Data Access Objects)**:
- `ListingDao` - Listing queries
- `TransactionDao` - Transaction queries
- `UserDao` - User data
- `MarketPriceDao` - Market price queries
- `ActivityDao` - Activity queries
- `LocalOpDao` - Pending sync operations

**Key Features**:
- Type converters for complex types (enums, lists, dates)
- Foreign key relationships
- Indexes for performance
- Migration support
- Schema export for version control

### 5. Remote Data Source (Retrofit)

**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/api/`

**API Services**:
- `AuthApiService` - Authentication endpoints
- `ListingApiService` - Listing CRUD
- `TransactionApiService` - Transaction operations
- `MarketPriceApiService` - Market data
- `ActivityApiService` - Activity tracking
- `SyncApiService` - Data synchronization
- `StorageApiService` - Image upload

**Configuration**:
- Base URL configured via `BuildConfig.BASE_URL`
- OkHttp with logging interceptor (debug builds only)
- Moshi for JSON serialization
- Auth interceptor for JWT tokens
- Retry logic for failed requests

## Dependency Injection (Hilt)

**Location**: `app/src/main/java/com/senthapps/slagrimarket/di/`

All dependencies are provided via **Dagger Hilt**.

**Modules**:
- `NetworkModule` - Retrofit, OkHttp, API services
- `DatabaseModule` - Room database, DAOs
- `RepositoryModule` - Repository instances (if needed)

**Application Class**: `JaffnaMarketplaceApplication.kt`
- Annotated with `@HiltAndroidApp`
- Initializes Timber logging
- Creates notification channels
- Configures WorkManager

## Data Flow

### Example: Loading Listings

```
User Action (Pull to Refresh)
        │
        ▼
ListingsScreen (Composable)
        │
        ▼
ListingsViewModel.loadListings()
        │
        ▼
ListingRepository.getAllActiveListings()
        │
        ├─────────────────────┬─────────────────────┐
        ▼                     ▼                     ▼
    1. Emit Loading    2. Query Room DB      3. Fetch from API
                            │                       │
                            ▼                       ▼
                    Emit cached data        Update Room DB
                                                    │
                                                    ▼
                                            Emit fresh data
                                                    │
                                                    ▼
                                            Update UI State
                                                    │
                                                    ▼
                                            Recompose Screen
```

## Offline-First Design

### Principles

1. **Local Database as Single Source of Truth**: All UI reads from Room database
2. **Optimistic Updates**: Write to local DB immediately, sync in background
3. **Background Sync**: WorkManager periodically syncs pending operations
4. **Conflict Resolution**: Last-write-wins strategy with server timestamps
5. **Graceful Degradation**: App fully functional offline

### Sync Strategy

**LocalOp Table**: Tracks pending operations (create, update, delete)

```kotlin
@Entity(tableName = "local_ops")
data class LocalOp(
    @PrimaryKey val opId: String,
    val type: OpType, // CREATE_LISTING, UPDATE_LISTING, etc.
    val payload: String, // JSON of the operation
    val clientTs: String, // Client timestamp
    val synced: Boolean = false
)
```

**SyncWorker**: Periodically processes pending operations
- Runs every 15 minutes (configurable)
- Retries failed operations with exponential backoff
- Marks operations as synced on success

## Trilingual Support

**Languages**: English, Tamil (தமிழ்), Sinhala (සිංහල)

**Implementation**:
- String resources in `values/`, `values-ta/`, `values-si/`
- `LanguagePreferences` stores selected language
- `LanguageToggleViewModel` manages language switching
- Locale changes applied at runtime

## Testing Strategy

**Unit Tests**: `app/src/test/`
- ViewModel logic testing
- Repository logic testing
- Use MockK for mocking

**Instrumentation Tests**: `app/src/androidTest/`
- UI testing with Compose Test
- Database testing with Room
- Integration testing

**Libraries**:
- JUnit 4
- MockK
- Turbine (Flow testing)
- Robolectric
- Espresso
- Compose UI Test

## Build Configuration

**Build Types**:
- **Debug**: Local API (`http://10.0.2.2:3000/api/`), logging enabled
- **Release**: Production API, logging disabled, ProGuard ready

**BuildConfig Fields**:
- `BASE_URL` - API endpoint
- `ENABLE_LOGGING` - HTTP logging toggle

## Key Design Decisions

1. **Why Offline-First?** - Target users (farmers) often have poor connectivity
2. **Why Compose?** - Modern, declarative UI with less boilerplate
3. **Why Hilt?** - Standard DI solution for Android, good IDE support
4. **Why Room?** - Type-safe, compile-time verified SQL queries
5. **Why StateFlow over LiveData?** - Better Kotlin coroutines integration
6. **Why Moshi over Gson?** - Better Kotlin support, faster, smaller

## Further Reading

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [MVVM Architecture Guide](https://developer.android.com/topic/architecture)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)

