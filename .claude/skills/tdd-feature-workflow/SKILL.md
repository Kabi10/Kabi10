---
name: tdd-feature-workflow
description: Default workflow for any new feature or bugfix: plan-first, tests-first, verify via Gradle.
---

# TDD Feature Workflow Skill

**Context:** Default workflow for implementing new features.  
**Activate when:** Starting any new feature, bugfix, or enhancement.

---

## Core Principle: Plan-First, Tests-First

1. **Plan** → Understand scope, design approach
2. **Test** → Write failing tests that define behavior
3. **Implement** → Write minimum code to pass tests
4. **Refactor** → _(Optional)_ Clean up only if it improves clarity and stays within scope
5. **Verify** → Full build + test suite

---

## Workflow Phases

### Phase 1: Spec & Plan

Before writing code:

```markdown
## Feature Spec: <Feature Name>

### Goal

What does this feature do?

### Scope (Single Feature Only)

- [ ] Specific change 1
- [ ] Specific change 2

### Files to Modify (use actual paths from repo)

- Example: `ui/listings/ListingsScreen.kt`
- Example: `ui/listings/ListingsViewModel.kt`
- Example: `data/repository/ListingRepository.kt`

### Files to Create

- `test/.../...Test.kt` - Unit tests
- `androidTest/.../...Test.kt` - UI tests (if applicable)

### Out of Scope

What will NOT be done in this change.
```

### Phase 2: Write Tests First

#### Unit Test Template

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class <Feature>ViewModelTest {

    @get:Rule
    val testDispatcher = MainDispatcherRule()

    private lateinit var viewModel: <Feature>ViewModel
    private lateinit var repository: <Feature>Repository

    @Before
    fun setup() {
        repository = mockk()
        viewModel = <Feature>ViewModel(repository)
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given fresh ViewModel

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load data success updates state`() = runTest {
        // Given
        val items = listOf(/* test data */)
        coEvery { repository.getDataFlow() } returns flowOf(items)

        // When
        viewModel.loadData()
        advanceUntilIdle()

        // Then
        assertEquals(items, viewModel.uiState.value.items)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `load data error shows error state`() = runTest {
        // Given - Use flow { throw } for Flow-based errors, or Resource.Error pattern
        coEvery { repository.getDataFlow() } returns flow { throw Exception("Network error") }

        // When
        viewModel.loadData()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)
    }
}
```

#### UI Test Template

```kotlin
@HiltAndroidTest
class <Feature>ScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun screen_displays_loading_state() {
        // Prefer faking repositories via Hilt @TestInstallIn modules
        // over mocking ViewModels directly
        composeTestRule.setContent {
            <Feature>Screen(
                onNavigateBack = {}
            )
        }

        // Ensure the composable uses Modifier.testTag("loading_indicator")
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }
}
```

### Phase 3: Implement

Write minimum code to pass tests:

1. Run tests → **RED** (failing)
2. Implement feature code
3. Run tests → **GREEN** (passing)
4. Refactor if needed, keep tests green

### Phase 4: Verify

Run full verification suite:

```bash
./gradlew clean test lint
./gradlew connectedAndroidTest  # if emulator/device available
```

---

## Test Targets

| Change Type       | Required Tests                     |
| ----------------- | ---------------------------------- |
| ViewModel logic   | Unit test covering all states      |
| Repository method | Unit test with mocked dependencies |
| New Screen        | UI test for critical paths         |
| Bug fix           | Regression test proving fix        |
| API integration   | Unit test with mocked API response |

### Coverage Guidance

- **New features:** Include tests covering main success + error paths. If coverage tooling is configured (`./gradlew jacocoTestReport`), target ≥70% for touched code.
- **Bug fixes:** Write regression test that reproduces the bug first
- **Refactors:** Existing tests must continue to pass

---

## Iteration Loop

```
┌─────────────────────────────────────┐
│  1. Write/update failing test       │
├─────────────────────────────────────┤
│  2. Run test → Verify it fails      │
├─────────────────────────────────────┤
│  3. Write minimum implementation    │
├─────────────────────────────────────┤
│  4. Run test → Verify it passes     │
├─────────────────────────────────────┤
│  5. Refactor (keep tests green)     │
└─────────────────────────────────────┘
        ↓ Repeat for next behavior
```

---

## Checkpoints

### Before Starting Implementation

- [ ] Feature spec written
- [ ] Scope is single-feature (not multi-feature)
- [ ] Files to modify identified
- [ ] Test cases designed

### After Writing Tests

- [ ] Tests compile
- [ ] Tests fail for the right reason
- [ ] Tests cover success, error, and edge cases

### After Implementation

- [ ] All new tests pass
- [ ] All existing tests still pass
- [ ] Lint passes (`./gradlew lint`)
- [ ] Manual verification done (if UI)

---

## Commit Rules

### Commit Message Format

```
<type>(<scope>): <description>

[optional body]
[optional footer]
```

| Type       | When                  |
| ---------- | --------------------- |
| `feat`     | New feature           |
| `fix`      | Bug fix               |
| `test`     | Adding/updating tests |
| `refactor` | Code restructuring    |
| `docs`     | Documentation         |
| `style`    | Formatting            |
| `chore`    | Build, config changes |

### Examples

```
feat(listings): add filter by crop type
fix(auth): resolve OTP timeout issue
test(home): add unit tests for HomeViewModel
refactor(repository): extract common network handling
```

### Commit Frequency

- Commit after each passing test cycle
- Each commit should be atomic and revertible
- Don't commit failing tests

---

## PR Summary Format

```markdown
## Summary

Brief description of what this PR does.

## Changes

- Added X
- Modified Y
- Fixed Z

## Testing

- [ ] Unit tests added/updated
- [ ] UI tests added/updated (if applicable)
- [ ] Manual testing performed

## Screenshots (if UI)

Before: [image]
After: [image]
```

---

## Anti-Patterns to Avoid

| ❌ Avoid            | ✅ Instead                  |
| ------------------- | --------------------------- |
| Multi-feature PRs   | One feature per PR          |
| Tests after code    | Tests before code           |
| Skipping edge cases | Test error states too       |
| Large refactors     | Small, incremental changes  |
| Untested bug fixes  | Write regression test first |
| Assuming it works   | Run full test suite         |
