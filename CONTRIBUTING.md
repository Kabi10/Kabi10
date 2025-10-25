# Contributing to Agrimarket

Thank you for your interest in contributing to the Agrimarket Android app! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Standards](#code-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Getting Help](#getting-help)

## Getting Started

### Prerequisites

Before you begin, ensure you have:

1. **Android Studio** - Hedgehog (2023.1.1) or later
2. **JDK 11** or later
3. **Git** installed and configured
4. **Android SDK** with API level 24-36
5. A **physical Android device** or emulator for testing

### Initial Setup

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Agrimarket.git
   cd Agrimarket
   ```

3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/Agrimarket.git
   ```

4. **Set up Firebase** (see `SETUP.md` for detailed instructions):
   - Copy `app/google-services.json.template` to `app/google-services.json`
   - Fill in your Firebase project credentials

5. **Configure local properties**:
   - Copy `local.properties.template` to `local.properties`
   - Set your Android SDK path

6. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Development Workflow

### Branching Strategy

We use a simplified Git Flow:

- `master` - Production-ready code
- `develop` - Integration branch for features (if applicable)
- `feature/feature-name` - New features
- `bugfix/bug-description` - Bug fixes
- `hotfix/critical-fix` - Critical production fixes

### Creating a Feature Branch

```bash
# Update your local master
git checkout master
git pull upstream master

# Create a new feature branch
git checkout -b feature/your-feature-name
```

### Making Changes

1. **Make your changes** in small, logical commits
2. **Test your changes** thoroughly
3. **Run the linter** (if configured):
   ```bash
   ./gradlew ktlintCheck
   ```
4. **Build the project** to ensure no compilation errors:
   ```bash
   ./gradlew assembleDebug
   ```

### Keeping Your Branch Updated

```bash
# Fetch latest changes from upstream
git fetch upstream

# Rebase your branch on upstream/master
git rebase upstream/master

# If conflicts occur, resolve them and continue
git rebase --continue
```

## Code Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Naming**:
  - Classes: `PascalCase`
  - Functions/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Composables: `PascalCase` (like classes)

### Architecture Guidelines

- **Follow MVVM pattern** (see `ARCHITECTURE.md`)
- **Use dependency injection** (Hilt) for all dependencies
- **Keep ViewModels UI-agnostic** - no Android framework imports except lifecycle
- **Repository pattern** - all data access through repositories
- **Offline-first** - always cache data locally

### Compose Best Practices

```kotlin
// ✅ Good: Stateless composable with clear parameters
@Composable
fun ListingCard(
    listing: Listing,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}

// ❌ Bad: Stateful composable with ViewModel dependency
@Composable
fun ListingCard(viewModel: ListingsViewModel) {
    // Don't do this
}
```

### String Resources

- **Never hardcode strings** in UI code
- **Use string resources** for all user-facing text
- **Support all three languages**: English, Tamil, Sinhala
- **Naming convention**: `screen_element_description`
  ```xml
  <string name="home_welcome_message">Welcome to Agrimarket</string>
  ```

### Error Handling

```kotlin
// ✅ Good: Proper error handling with logging
try {
    val result = repository.createListing(listing)
    result.onSuccess { 
        Timber.d("Listing created successfully")
    }.onFailure { error ->
        Timber.e(error, "Failed to create listing")
    }
} catch (e: Exception) {
    Timber.e(e, "Unexpected error creating listing")
}

// ❌ Bad: Silent failures
try {
    repository.createListing(listing)
} catch (e: Exception) {
    // Empty catch block
}
```

## Testing Guidelines

### Writing Tests

1. **Unit Tests** - Test ViewModels and Repositories
   - Location: `app/src/test/`
   - Use MockK for mocking
   - Test both success and failure cases

2. **UI Tests** - Test Compose screens
   - Location: `app/src/androidTest/`
   - Use Compose Test APIs
   - Test user interactions

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumentation tests
./gradlew connectedAndroidTest

# Run tests for a specific variant
./gradlew testDebugUnitTest
```

### Test Coverage

- Aim for **>70% code coverage** for new features
- **All ViewModels** should have unit tests
- **Critical user flows** should have UI tests

### Example Unit Test

```kotlin
@Test
fun `loadListings should emit success state with data`() = runTest {
    // Given
    val mockListings = listOf(
        Listing(id = "1", cropName = "Tomatoes", quantity = 100.0)
    )
    coEvery { repository.getAllActiveListings() } returns flowOf(
        Resource.Success(mockListings)
    )
    
    // When
    viewModel.loadListings()
    
    // Then
    val state = viewModel.uiState.value
    assertEquals(mockListings, state.listings)
    assertFalse(state.isLoading)
    assertNull(state.error)
}
```

## Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] No new lint warnings
- [ ] Code follows style guidelines
- [ ] Strings are externalized and translated
- [ ] Documentation updated (if needed)
- [ ] Self-review completed

### Creating a Pull Request

1. **Push your branch** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a Pull Request** on GitHub:
   - Use a clear, descriptive title
   - Reference any related issues
   - Provide a detailed description of changes
   - Add screenshots for UI changes

3. **PR Template**:
   ```markdown
   ## Description
   Brief description of what this PR does
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] UI tests added/updated
   - [ ] Manual testing completed
   
   ## Screenshots (if applicable)
   [Add screenshots here]
   
   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review completed
   - [ ] Comments added for complex logic
   - [ ] Documentation updated
   - [ ] No new warnings
   - [ ] Tests pass locally
   ```

### Code Review

- Be **respectful and constructive** in reviews
- Address **all review comments** before merging
- Request re-review after making changes
- Squash commits if requested

## Commit Message Guidelines

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, no logic change)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks

### Examples

```bash
# Feature
feat(listings): add filter by crop type

# Bug fix
fix(auth): resolve OTP verification timeout issue

# Documentation
docs(readme): update setup instructions for Firebase

# Refactoring
refactor(repository): simplify offline-first logic in ListingRepository
```

### Scope

Use the module or feature name:
- `listings`
- `transactions`
- `auth`
- `profile`
- `search`
- `analytics`

## Getting Help

### Communication Channels

- **GitHub Issues** - Bug reports and feature requests
- **GitHub Discussions** - Questions and general discussion
- **Pull Request Comments** - Code-specific questions

### Asking Questions

When asking for help:

1. **Search existing issues** first
2. **Provide context** - what are you trying to do?
3. **Include error messages** - full stack traces
4. **Share code snippets** - minimal reproducible example
5. **Describe what you've tried** - show your debugging efforts

### Reporting Bugs

Use the bug report template and include:

- **Android version** and device model
- **App version** (debug or release)
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Logcat output** (if applicable)
- **Screenshots** (if applicable)

## Additional Resources

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture overview
- [SETUP.md](SETUP.md) - Detailed setup guide
- [KNOWN_ISSUES.md](KNOWN_ISSUES.md) - Current limitations
- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

---

Thank you for contributing to Agrimarket! 🌾

