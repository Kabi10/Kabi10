---
name: "🌟 Good First Issue: Add Unit Tests for ViewModels"
about: Write unit tests for ViewModel business logic (Medium - Learn testing!)
title: "[Medium] Add unit tests for ListingsViewModel"
labels: good first issue, testing, enhancement
assignees: ''
---

## 🎯 Issue Description

The app currently has comprehensive testing infrastructure (JUnit, MockK, Turbine) but only placeholder tests. This is a critical gap that needs to be filled to ensure code quality and prevent regressions.

**Your task**: Write comprehensive unit tests for `ListingsViewModel` to verify its business logic works correctly.

## 📚 What You'll Learn

- ✅ Unit testing in Android with JUnit 4
- ✅ Mocking dependencies with MockK
- ✅ Testing Kotlin Coroutines and Flow
- ✅ Testing ViewModels and state management
- ✅ Test-Driven Development (TDD) principles
- ✅ Turbine library for Flow testing

## 🔍 Current State

The project has only placeholder tests:
- `ExampleUnitTest.kt` - Only tests `2 + 2 = 4`
- `ExampleInstrumentedTest.kt` - Only tests package name

**Test coverage**: ~0% ❌  
**Target coverage**: >70% ✅

## 📂 Relevant Files

### Files to Create/Modify:

1. **`app/src/test/java/com/senthapps/slagrimarket/ui/listings/ListingsViewModelTest.kt`** (CREATE THIS)
   - New file for ViewModel tests

### Files to Reference:

2. **`app/src/main/java/com/senthapps/slagrimarket/ui/listings/ListingsViewModel.kt`**
   - The ViewModel you're testing

3. **`app/src/main/java/com/senthapps/slagrimarket/data/repository/ListingRepository.kt`**
   - Repository that needs to be mocked

4. **`app/build.gradle.kts`**
   - Testing dependencies are already configured

## 💡 Implementation Hints

### Step 1: Create Test File Structure

Create `ListingsViewModelTest.kt` in the correct test directory:

```kotlin
package com.senthapps.slagrimarket.ui.listings

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ListingsViewModelTest {

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()
    
    // Mock repository
    private lateinit var repository: ListingRepository
    
    // ViewModel under test
    private lateinit var viewModel: ListingsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = ListingsViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // TODO: Add test methods here
}
```

### Step 2: Write Test Cases

Here are the key scenarios to test:

#### Test 1: Initial State
```kotlin
@Test
fun `initial state should be empty with no loading`() {
    // Given: ViewModel is created
    
    // When: We check initial state
    val state = viewModel.uiState.value
    
    // Then: State should be empty
    assertTrue(state.listings.isEmpty())
    assertFalse(state.isLoading)
    assertNull(state.error)
}
```

#### Test 2: Loading Listings Success
```kotlin
@Test
fun `loadListings should emit loading then success state`() = runTest {
    // Given: Repository returns successful data
    val mockListings = listOf(
        Listing(
            id = "1",
            cropName = "Tomatoes",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 50.0,
            // ... other fields
        )
    )
    
    coEvery { repository.getAllActiveListings(any()) } returns flowOf(
        Resource.Loading(),
        Resource.Success(mockListings)
    )
    
    // When: We load listings
    viewModel.uiState.test {
        viewModel.loadListings()
        
        // Then: Should emit loading state
        val loadingState = awaitItem()
        assertTrue(loadingState.isLoading)
        
        // Then: Should emit success state with data
        val successState = awaitItem()
        assertFalse(successState.isLoading)
        assertEquals(mockListings, successState.listings)
        assertNull(successState.error)
    }
}
```

#### Test 3: Loading Listings Error
```kotlin
@Test
fun `loadListings should emit error state on failure`() = runTest {
    // Given: Repository returns error
    val errorMessage = "Network error"
    
    coEvery { repository.getAllActiveListings(any()) } returns flowOf(
        Resource.Loading(),
        Resource.Error(errorMessage)
    )
    
    // When: We load listings
    viewModel.uiState.test {
        viewModel.loadListings()
        
        // Then: Should emit loading state
        awaitItem() // loading state
        
        // Then: Should emit error state
        val errorState = awaitItem()
        assertFalse(errorState.isLoading)
        assertEquals(errorMessage, errorState.error)
        assertTrue(errorState.listings.isEmpty())
    }
}
```

#### Test 4: Refresh Listings
```kotlin
@Test
fun `refreshListings should force refresh from repository`() = runTest {
    // Given: Repository is mocked
    val mockListings = listOf(/* ... */)
    
    coEvery { repository.getAllActiveListings(forceRefresh = true) } returns flowOf(
        Resource.Success(mockListings)
    )
    
    // When: We refresh listings
    viewModel.refreshListings()
    advanceUntilIdle()
    
    // Then: Repository should be called with forceRefresh = true
    coVerify { repository.getAllActiveListings(forceRefresh = true) }
}
```

### Step 3: Run Tests

```bash
# Run all unit tests
./gradlew test

# Run only ListingsViewModelTest
./gradlew test --tests ListingsViewModelTest

# Run with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

## ✅ Acceptance Criteria

- [ ] Test file created in correct location
- [ ] At least 5 test cases covering:
  - [ ] Initial state
  - [ ] Loading listings (success)
  - [ ] Loading listings (error)
  - [ ] Refresh listings
  - [ ] Filter/search functionality (if applicable)
- [ ] All tests pass (`./gradlew test`)
- [ ] Code coverage >70% for `ListingsViewModel`
- [ ] Tests follow naming convention: `` `methodName should expectedBehavior when condition` ``
- [ ] MockK used correctly for mocking repository
- [ ] Turbine used for testing Flow emissions
- [ ] No flaky tests (run multiple times to verify)

## 🧪 Testing Instructions

1. Create the test file
2. Write test cases
3. Run tests: `./gradlew test`
4. Check test report: `app/build/reports/tests/testDebugUnitTest/index.html`
5. Verify all tests pass
6. Check coverage report (if generated)

## 📖 Helpful Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [MockK Documentation](https://mockk.io/)
- [Turbine - Flow Testing Library](https://github.com/cashapp/turbine)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- See `CONTRIBUTING.md` for testing guidelines
- See `ARCHITECTURE.md` for ViewModel architecture

## 🎓 Difficulty Level

**Medium** - Estimated time: 4-6 hours

This is a great learning issue because:
- ✅ Learn industry-standard testing practices
- ✅ Understand ViewModel behavior deeply
- ✅ Practice mocking and dependency injection
- ✅ Build confidence in code quality
- ⚠️ Requires understanding of coroutines and Flow

## 💡 Tips for Success

1. **Start small**: Write one test at a time
2. **Run frequently**: Run tests after each addition
3. **Read existing code**: Understand what the ViewModel does before testing
4. **Use descriptive names**: Test names should explain what they verify
5. **Test behavior, not implementation**: Focus on inputs and outputs
6. **Mock external dependencies**: Repository should be mocked

## 🔄 Bonus Challenges

Once you complete `ListingsViewModel`, consider testing:
- `HomeViewModel`
- `TransactionsViewModel`
- `SearchViewModel`
- `AuthViewModel`

## 💬 Need Help?

- Check `KNOWN_ISSUES.md` - testing gaps are documented (#1)
- Look at Android testing samples online
- Ask questions in the comments below
- Reference the helpful resources above

Good luck! 🧪✨

