---
name: android-architecture
description: Enforces MVVM + Compose + Hilt patterns for Agrimarket Android app. Includes templates and forbidden patterns.
---

# Android Architecture Skill

**Context:** Enforces architectural patterns for the Agrimarket Android app.  
**Activate when:** Creating/modifying screens, ViewModels, repositories, or navigation.

---

## Reference Docs First
- `docs/DOCUMENTATION.md` - Full architecture overview
- `README.md` - Tech stack & quick start

---

## Architecture Rules

### 1. Tech Stack (Required for New or Modified Code)
| Layer | Technology | Pattern |
|-------|------------|---------|
| UI | **Jetpack Compose + Material 3** | Stateless composables |
| State | **StateFlow** | MutableStateFlow → asStateFlow() |
| Logic | **ViewModel** | @HiltViewModel, viewModelScope |
| Data | **Repository** | @Singleton, offline-first |
| Local | **Room** | DAOs + Flow |
| Remote | **Retrofit/OkHttp** | Moshi serialization |
| DI | **Hilt** | @Inject constructor |

> Existing legacy code (Fragments, LiveData, etc.) may remain untouched unless the task explicitly requires migration.

### 2. File Structure
```
app/src/main/java/com/senthapps/slagrimarket/
├── data/
│   ├── api/          # Retrofit services (*ApiService.kt)
│   ├── dao/          # Room DAOs (*Dao.kt)
│   ├── model/        # Data classes (*.kt)
│   ├── repository/   # *Repository.kt
│   └── network/      # Network utilities
├── di/               # Hilt modules (*Module.kt)
├── navigation/       # Single NavHost
├── ui/
│   └── <feature>/    # Screen.kt + ViewModel.kt
└── util/             # Utilities
```

### 3. Naming Conventions
| Type | Pattern | Example |
|------|---------|---------|
| Screen | `<Feature>Screen` | `ListingsScreen.kt` |
| ViewModel | `<Feature>ViewModel` | `ListingsViewModel.kt` |
| UI State | `<Feature>UiState` | `ListingsUiState` |
| Repository | `<Feature>Repository` | `ListingRepository.kt` |
| DAO | `<Feature>Dao` | `ListingDao.kt` |
| API Service | `<Feature>ApiService` | `ListingApiService.kt` |
| Hilt Module | `<Category>Module` | `NetworkModule.kt` |

---

## Templates

### ViewModel Template
```kotlin
package com.senthapps.slagrimarket.ui.<feature>

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class <Feature>ViewModel @Inject constructor(
    private val repository: <Feature>Repository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(<Feature>UiState())
    val uiState: StateFlow<<Feature>UiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Collect from repository flow
                repository.getDataFlow()
                    .collect { items ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            items = items,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class <Feature>UiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)
```

### Screen Template
```kotlin
@Composable
fun <Feature>Screen(
    onNavigateBack: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: <Feature>ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { /* TopAppBar */ }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> ErrorMessage(uiState.error!!)
                else -> ContentList(uiState.items, onItemClick)
            }
        }
    }
}
```

### Navigation Route Template
```kotlin
// In navigation/Screen.kt
object <Feature> : Screen("<feature>")
object <Feature>Detail : Screen("<feature>_detail/{id}") {
    fun createRoute(id: String) = "<feature>_detail/$id"
}
```

---

## DO NOT DO

| ❌ Forbidden | ✅ Instead |
|-------------|-----------|
| XML layouts | Compose only |
| Fragment-based navigation | NavHost composables |
| LiveData for new code | StateFlow |
| Manual DI | Hilt @Inject |
| ViewModel in composables | Pass via hiltViewModel() at screen level |
| Hardcoded strings | String resources (values/, values-ta/, values-si/) |
| Business logic in UI | Move to ViewModel/Repository |
| Direct API calls from UI | Repository → ViewModel → UI |
| New architectural patterns | Follow existing patterns |
| Broad refactors | Single-feature scope |

---

## Offline-First Pattern

When offline-first is already used in a feature, implement data flow as:
1. **Emit cached data first** from Room
2. **Fetch from network** in background
3. **Update cache** on success
4. **Emit error only** if cache empty

Reference: `ListingRepository.kt` for implementation example.

> If a feature currently uses a simpler network-only flow, follow the existing pattern unless instructed otherwise.

---

## Resource Wrapper

Use `Resource<T>` sealed class from `data/repository/Resource.kt`:
- `Resource.Loading(data?)` - Show loading, optionally with stale data
- `Resource.Success(data)` - Show fresh data
- `Resource.Error(message, exception?, data?)` - Show error with optional fallback

> If `Resource<T>` is not used in the current feature area, follow existing result/error handling patterns instead of introducing it.

---

## Before Adding New Components

1. Check existing components in `ui/components/` directory
2. Follow Material 3 theming from `ui/theme/`
3. Support all 3 languages (en, ta, si)
4. Add accessibility content descriptions
