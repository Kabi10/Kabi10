---
name: "🌟 Good First Issue: Implement Language Preference Persistence"
about: Add DataStore persistence for language selection (Easy - Good for beginners!)
title: "[Easy] Implement language preference persistence"
labels: good first issue, enhancement, beginner-friendly
assignees: ''
---

## 🎯 Issue Description

Currently, the app supports three languages (English, Tamil, Sinhala) with a language toggle feature, but the selected language doesn't persist across app restarts. Users have to re-select their preferred language every time they open the app.

**Your task**: Implement DataStore persistence so the selected language is saved and restored when the app launches.

## 📚 What You'll Learn

- ✅ Android DataStore (modern replacement for SharedPreferences)
- ✅ Kotlin Coroutines and Flow
- ✅ ViewModel initialization
- ✅ State management in Jetpack Compose
- ✅ Reading existing code and making targeted improvements

## 🔍 Current Behavior

1. User opens app → Default language is Tamil
2. User switches to English → UI updates correctly
3. User closes app
4. User reopens app → Language resets to Tamil ❌

## ✨ Expected Behavior

1. User opens app → Default language is Tamil
2. User switches to English → UI updates correctly
3. User closes app
4. User reopens app → Language is still English ✅

## 📂 Relevant Files

You'll need to modify these files:

1. **`app/src/main/java/com/senthapps/slagrimarket/data/preferences/LanguagePreferences.kt`**
   - Add DataStore implementation
   - Add `saveLanguage(language: String)` function
   - Add `getLanguage(): Flow<String>` function

2. **`app/src/main/java/com/senthapps/slagrimarket/ui/common/LanguageToggleViewModel.kt`**
   - Load saved language in `init` block
   - Save language when user changes it

3. **`app/build.gradle.kts`** (if needed)
   - DataStore dependency is already added: `androidx.datastore:datastore-preferences:1.0.0`

## 💡 Implementation Hints

### Step 1: Add DataStore to LanguagePreferences

```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanguagePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        const val DEFAULT_LANGUAGE = "ta" // Tamil
    }
    
    // TODO: Implement saveLanguage() function
    suspend fun saveLanguage(language: String) {
        // Use dataStore.edit { preferences -> ... }
    }
    
    // TODO: Implement getLanguage() function
    fun getLanguage(): Flow<String> {
        // Use dataStore.data.map { preferences -> ... }
        // Return DEFAULT_LANGUAGE if not set
    }
}
```

### Step 2: Provide DataStore in Hilt Module

You may need to create a `PreferencesModule.kt` or add to existing module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

private val Context.dataStore by preferencesDataStore(name = "settings")
```

### Step 3: Update LanguageToggleViewModel

```kotlin
class LanguageToggleViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {
    
    init {
        // TODO: Load saved language on initialization
        viewModelScope.launch {
            languagePreferences.getLanguage().collect { savedLanguage ->
                _selectedLanguage.value = savedLanguage
            }
        }
    }
    
    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        // TODO: Save language to DataStore
        viewModelScope.launch {
            languagePreferences.saveLanguage(language)
        }
    }
}
```

## ✅ Acceptance Criteria

- [ ] Language selection persists across app restarts
- [ ] Default language is Tamil if no preference is saved
- [ ] Language changes are saved immediately when user toggles
- [ ] No crashes or errors in Logcat
- [ ] Code follows existing Kotlin style conventions
- [ ] (Bonus) Add unit tests for `LanguagePreferences`

## 🧪 Testing Instructions

1. Build and run the app
2. Change language from Tamil to English
3. Close the app completely (swipe away from recent apps)
4. Reopen the app
5. Verify language is still English
6. Repeat with Sinhala

## 📖 Helpful Resources

- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Flow Documentation](https://kotlinlang.org/docs/flow.html)
- See `ARCHITECTURE.md` for app architecture overview
- See `CONTRIBUTING.md` for code style guidelines

## 🎓 Difficulty Level

**Easy** - Estimated time: 2-4 hours

This is a great first issue because:
- ✅ Small, focused scope
- ✅ Clear acceptance criteria
- ✅ Existing code to reference
- ✅ Well-documented APIs
- ✅ Immediate visual feedback

## 💬 Need Help?

- Check `KNOWN_ISSUES.md` - this issue is documented there (#7)
- Look at how `AuthPreferences.kt` uses DataStore (similar pattern)
- Ask questions in the comments below
- Reference the helpful resources above

Good luck! 🚀

