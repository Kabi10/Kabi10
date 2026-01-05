---
name: android-build-test
description: Build commands, test structure, error interpretation, and verification checklist for Agrimarket Android app.
---

# Android Build & Test Skill

**Context:** Build, test, and verify the Agrimarket Android app.  
**Activate when:** Building, running tests, fixing build errors, or preparing PRs.

---

## Reference Docs First
- [README.md](file:///c:/Dev/Agrimarket/README.md#287) - Test commands section
- [DOCUMENTATION.md](file:///c:/Dev/Agrimarket/docs/DOCUMENTATION.md#185) - Testing section

---

## Build Commands

### Quick Reference
> **Note:** On Windows, use `gradlew.bat` or `.\gradlew` instead of `./gradlew`.

```bash
# Debug build (fastest)
./gradlew assembleDebug

# Unit tests (no device needed)
./gradlew test

# UI/Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint

# Coverage report
./gradlew jacocoTestReport

# Clean build
./gradlew clean assembleDebug

# Release bundle (for Play Store)
./gradlew bundleRelease
```

### Install & Run
```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat -s "SLAgrimarket:*"
```

---

## Test Structure

| Test Type | Location | Command | Device Needed |
|-----------|----------|---------|---------------|
| Unit | `app/src/test/` | `./gradlew test` | No |
| UI/Instrumented | `app/src/androidTest/` | `./gradlew connectedAndroidTest` | Yes |

### Test Snapshot (as of Jan 2026)
- **Unit tests:** 48 tests in 7 files
- **UI tests:** 27 tests in 5 files
- **Target:** >=70% coverage for touched code if coverage tooling is configured

> Do not assume these counts are current; verify via Gradle output.

### Test File Naming
| Type | Pattern | Example |
|------|---------|---------|
| Unit (ViewModel) | `<Feature>ViewModelTest.kt` | `ListingsViewModelTest.kt` |
| Unit (Repository) | `<Feature>RepositoryTest.kt` | `ListingRepositoryTest.kt` |
| UI | `<Feature>ScreenTest.kt` | `ListingsScreenTest.kt` |

---

## Interpreting Build Failures

### Common Gradle Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `SDK location not found` | Missing local.properties | Copy template: `cp local.properties.template local.properties` |
| `google-services.json missing` | Firebase config missing | Copy from template in `app/` |
| `Unresolved reference` | Missing import/dependency | Check imports, run Gradle sync |
| `Execution failed for task :kapt` | Hilt/Room annotation issue | Check @Inject, @HiltViewModel annotations |
| `Type mismatch` | Kotlin type error | Check nullability, use `?.` or `!!` |

### Common Test Failures

| Error | Cause | Fix |
|-------|-------|-----|
| `No tests found` | Wrong test runner | Check `HiltTestRunner` in `build.gradle.kts` |
| `Hilt component not generated` | Missing @HiltAndroidTest | Add annotation to test class |
| `FocusRequester not initialized` | Compose test setup | Use proper ComposeTestRule |
| `Timeout` | Async operation | Use `advanceUntilIdle()` or `runTest` |

---

## Safe Practices

### ✅ DO
- Run `./gradlew test` before committing
- Run `./gradlew lint` to catch issues early
- Test on physical device for real-world verification
- Check logcat for runtime errors
- Use existing test files as templates

### ❌ DO NOT
- Upgrade dependencies speculatively
- Skip tests to save time
- Ignore lint warnings without reason
- Commit with failing tests
- Modify Gradle version without explicit approval

---

## Before Calling Done Checklist

Before marking any work complete, verify:

```
[ ] 1. Build succeeds: `./gradlew assembleDebug`
[ ] 2. Unit tests pass: `./gradlew test`
[ ] 3. Lint passes: `./gradlew lint`
[ ] 4. If UI changes: Manual verification on device
[ ] 5. If new feature: Added unit tests (>70% coverage)
[ ] 6. If UI feature: Added UI tests for critical paths
[ ] 7. Strings added to all 3 language files (values/, values-ta/, values-si/)
```

### For PRs
```
[ ] All above checks pass
[ ] Commit messages follow format: type(scope): description
[ ] No TODOs left in code unless tracked
[ ] Documentation updated if needed
```

---

## Debugging Tips

### View Logs
```bash
# Filter by app
adb logcat -s "SLAgrimarket:*" "*:E"

# Save to file
adb logcat -d > logcat.txt
```

### Common Runtime Issues

| Issue | Debug Approach |
|-------|---------------|
| Crash on launch | Check logcat for stack trace |
| Navigation crash | Verify route params match Screen definition |
| API error | Check network connectivity, BASE_URL in BuildConfig |
| Data not loading | Check Room DAO queries, repository Flow |
| UI not updating | Verify StateFlow collection with lifecycle |

---

## CI/CD Pipeline

GitHub Actions runs on push:
1. **Build** - assembleDebug
2. **Unit Tests** - test
3. **Lint** - lint
4. **CodeQL** - Security scanning

Check `.github/workflows/` for configurations.
