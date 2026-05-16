# 🚀 Agrimarket Android Pre-Launch Checklist

**Version:** 1.0  
**Created:** December 21, 2025  
**Target Device:** Android 7.0+ (API 24+)

> This checklist provides **executable verification steps** using ADB, Gradle commands, and Logcat analysis. Focus on actual device testing rather than documentation review.

---

## 📋 Prerequisites

```powershell
# Set up environment variables
$env:ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

# Verify connected device
& $env:ADB devices

# Get device ID for subsequent commands
$DEVICE = (& $env:ADB devices | Select-String -Pattern "^\w+" | Select-Object -First 1).Matches.Value
```

---

## ⚡ Quick Reference (Copy-Paste Commands)

> **TL;DR:** Most common testing commands for rapid verification. See detailed sections below for full context.

### Automated Tests

```powershell
.\gradlew test                     # Unit tests (142)
.\gradlew lint                     # Lint checks
.\gradlew connectedAndroidTest     # UI tests (27)
.\gradlew assembleRelease          # Release build
```

### Logcat Monitoring

```powershell
& $env:ADB logcat -v time | Select-String "(OkHttp|HTTP|200|201|Auth|Sync)"
```

### App Control

```powershell
& $env:ADB shell pm clear com.senthapps.slagrimarket          # Fresh start
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity
& $env:ADB shell am force-stop com.senthapps.slagrimarket     # Kill app
```

### Network Testing

```powershell
& $env:ADB shell cmd connectivity airplane-mode enable   # Offline
& $env:ADB shell cmd connectivity airplane-mode disable  # Online
```

### Language Testing

```powershell
& $env:ADB shell "setprop persist.sys.locale ta-IN; stop; start"  # Tamil
& $env:ADB shell "setprop persist.sys.locale si-LK; stop; start"  # Sinhala
& $env:ADB shell "setprop persist.sys.locale en-US; stop; start"  # English
```

### Dark Mode & Screenshots

```powershell
& $env:ADB shell "cmd uimode night yes"    # Dark mode ON
& $env:ADB shell "cmd uimode night no"     # Dark mode OFF
& $env:ADB exec-out screencap -p > screenshot.png
```

### TalkBack Accessibility

```powershell
# Enable
& $env:ADB shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
& $env:ADB shell settings put secure accessibility_enabled 1

# Disable
& $env:ADB shell settings put secure enabled_accessibility_services ""
& $env:ADB shell settings put secure accessibility_enabled 0
```

### 12-Point Verification Checklist

| #   | Item                 | Command/Action                                                         | ✓   |
| --- | -------------------- | ---------------------------------------------------------------------- | --- |
| 1   | **Release Build**    | `.\gradlew assembleRelease` (must succeed)                             | [ ] |
| 2   | **Debug Bypass OFF** | Verify `BuildConfig.DEBUG=false` in release APK                        | [ ] |
| 3   | API Health           | `Invoke-RestMethod "https://backend-psi-tan-18.vercel.app/api/health"` | [ ] |
| 4   | Auth Flow            | Manual login with OTP (no debug bypass)                                | [ ] |
| 5   | Sync Test            | Toggle airplane mode                                                   | [ ] |
| 6   | Offline Data         | View cached listings offline                                           | [ ] |
| 7   | Create Listing       | Add new listing, check logcat                                          | [ ] |
| 8   | Unit Tests           | `.\gradlew test`                                                       | [ ] |
| 9   | UI Tests             | `.\gradlew connectedAndroidTest`                                       | [ ] |
| 10  | Tamil UI             | Language toggle                                                        | [ ] |
| 11  | Dark Mode            | Theme consistency                                                      | [ ] |
| 12  | TalkBack             | Accessibility                                                          | [ ] |

---

## 🚨 PRODUCTION BUILD VERIFICATION (Run First!)

> **CRITICAL:** These steps verify that debug code is disabled and the release build is production-ready. Run these BEFORE testing app functionality.

### PB-1. Build Release APK

```powershell
# Clean and build release
.\gradlew clean assembleRelease

# Verify APK was created
Get-ChildItem ".\app\build\outputs\apk\release\*.apk" | Select-Object Name, @{N='SizeMB';E={[math]::Round($_.Length/1MB,2)}}
# Expected: app-release-unsigned.apk, Size < 25MB
```

### PB-2. Verify BuildConfig Values in Release APK

```powershell
# Extract BuildConfig from release APK to verify DEBUG=false
$APK = Get-ChildItem ".\app\build\outputs\apk\release\*.apk" | Select-Object -First 1
$AAPT = "$env:LOCALAPPDATA\Android\Sdk\build-tools\34.0.0\aapt2.exe"

# Check if APK contains debug flag (should NOT find "DEBUG:true")
& $AAPT dump badging $APK.FullName | Select-String "debug"

# Verify via decompiled BuildConfig (requires apktool or jadx)
# The release APK should have: BuildConfig.DEBUG = false
```

### PB-3. Verify Debug Bypass is Disabled

The `AuthRepository.kt` uses `BuildConfig.DEBUG` to enable/disable debug bypass:

```kotlin
private val isDebugMode: Boolean = BuildConfig.DEBUG
```

**Verification Steps:**

```powershell
# 1. Install RELEASE APK (not debug)
& $env:ADB install -r ".\app\build\outputs\apk\release\app-release.apk"

# 2. Clear app data for fresh start
& $env:ADB shell pm clear com.senthapps.slagrimarket

# 3. Launch app and monitor logs
& $env:ADB logcat -c
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity
& $env:ADB logcat -v time | Select-String "(DEBUG|debug_user|bypass|fake)"

# Expected: NO logs containing "DEBUG:", "debug_user_123", "bypass auth", or "fake"
# The app MUST show the login screen, NOT auto-login with debug user
```

**Pass Criteria:**

- [ ] Release APK builds successfully
- [ ] App shows login screen on fresh install (NOT home screen)
- [ ] No "debug_user_123" or "Debug Farmer" visible
- [ ] Logcat shows NO debug bypass messages
- [ ] Real OTP authentication is required

### PB-4. Verify Production API URL

```powershell
# Check release build config has production URL
Select-String -Path ".\app\build.gradle.kts" -Pattern 'release\s*\{' -Context 0,10 |
    Select-String "BASE_URL"
# Expected: "https://backend-psi-tan-18.vercel.app/api/"

# Verify API calls go to production (not localhost)
& $env:ADB logcat -v time | Select-String "OkHttp" | Select-String "backend-psi-tan-18"
# Should see requests to production URL, NOT 10.0.2.2:3000
```

### PB-5. Verify ProGuard/R8 Obfuscation

```powershell
# Check ProGuard is enabled
Select-String -Path ".\app\build.gradle.kts" -Pattern "isMinifyEnabled|isShrinkResources"
# Expected: isMinifyEnabled = true, isShrinkResources = true

# Verify mapping file was generated (indicates obfuscation ran)
Test-Path ".\app\build\outputs\mapping\release\mapping.txt"
# Expected: True

# Check mapping file size (should be substantial if obfuscation worked)
Get-ChildItem ".\app\build\outputs\mapping\release\mapping.txt" | Select-Object Length
# Expected: > 100KB
```

### PB-6. Verify APK Signing (for Play Store)

```powershell
# Check if APK is signed
$APKSIGNER = "$env:LOCALAPPDATA\Android\Sdk\build-tools\34.0.0\apksigner.bat"
$APK = Get-ChildItem ".\app\build\outputs\apk\release\*.apk" | Select-Object -First 1

# Verify signature
& $APKSIGNER verify --verbose $APK.FullName

# For unsigned APK, you'll need to sign it:
# & $APKSIGNER sign --ks release-keystore.jks --out app-release-signed.apk $APK.FullName
```

### PB-7. Release Build Size Check

```powershell
# APK size analysis
$APK = Get-ChildItem ".\app\build\outputs\apk\release\*.apk" | Select-Object -First 1
$SizeMB = [math]::Round($APK.Length/1MB, 2)

Write-Host "Release APK Size: $SizeMB MB"
# Target: < 25MB for base APK
# Warning: > 30MB may indicate missing shrinkResources or large assets
```

**Production Build Summary:**
| Check | Command | Expected | ✓ |
|-------|---------|----------|---|
| Build succeeds | `.\gradlew assembleRelease` | Exit code 0 | [ ] |
| Debug bypass OFF | Install + launch release APK | Shows login screen | [ ] |
| Production URL | Check logcat for API calls | `backend-psi-tan-18.vercel.app` | [ ] |
| ProGuard enabled | Check mapping.txt exists | File > 100KB | [ ] |
| APK size | Check file size | < 25MB | [ ] |
| No debug logs | Monitor logcat | No DEBUG/bypass messages | [ ] |

---

## 🔴 CRITICAL - Core Functionality (Highest Priority)

### 1. Backend API Connectivity & Supabase Integration

#### 1.1 Verify API Base URL Configuration

```powershell
# Check release build config
Select-String -Path ".\app\build.gradle.kts" -Pattern "BASE_URL"
# Expected: https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/
```

#### 1.2 Test API Health Endpoint

```powershell
# Direct API health check
Invoke-RestMethod -Uri "https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/health" -Method GET
# Expected: { "status": "ok", "timestamp": "..." }

# Detailed health check
Invoke-RestMethod -Uri "https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/health/detailed" -Method GET
```

#### 1.3 Monitor API Calls via Logcat

```powershell
# Clear logcat and filter for HTTP calls
& $env:ADB logcat -c
& $env:ADB logcat -v time | Select-String -Pattern "(OkHttp|Retrofit|HTTP|200|201|400|401|500)"

# Look for:
# - "200 OK" responses for GET requests
# - "201 Created" for POST requests
# - No "401 Unauthorized" errors after login
```

**Pass Criteria:**

- [ ] Health endpoint returns HTTP 200 with `status: "ok"`
- [ ] Logcat shows actual HTTP requests (not just cached data)
- [ ] No persistent 4xx/5xx errors during normal operation

---

### 2. User Authentication Flow

#### 2.1 Verify Auth is Enabled (Not Demo Mode)

```powershell
# Check MainActivity doesn't bypass auth
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\MainActivity.kt" -Pattern "startWithAuth"
# Expected: startWithAuth = true

# Verify no mock user exists
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\data\repository\AuthRepository.kt" -Pattern "mockUser|bypassOtp"
# Expected: No matches (mock user removed)
```

#### 2.2 Test Full OTP Flow via ADB

```powershell
# Launch app fresh (clear data first)
& $env:ADB shell pm clear com.senthapps.slagrimarket
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity

# Monitor auth-related logs
& $env:ADB logcat -v time | Select-String -Pattern "(Auth|OTP|Login|JWT|Token)"
```

#### 2.3 Manual Auth Flow Test Steps

1. Open app → Should show Login/Register screen
2. Enter phone number → OTP should be sent
3. Enter OTP → Should navigate to Home screen
4. Kill app and reopen → Should auto-login (token persisted)
5. Logout → Should return to Login screen

**Pass Criteria:**

- [ ] App starts with authentication screen (not Home directly)
- [ ] OTP verification works with real backend
- [ ] Token persists across app restarts
- [ ] Logout clears session completely

---

### 3. Real-Time Data Synchronization

#### 3.1 Verify Sync Manager Configuration

```powershell
# Check SyncWorker exists
Get-ChildItem -Path ".\app\src\main\java\com\senthapps\slagrimarket" -Recurse -Filter "*Sync*.kt"
# Expected: SyncManager.kt, SyncWorker.kt
```

#### 3.2 Monitor Sync Operations via Logcat

```powershell
& $env:ADB logcat -v time | Select-String -Pattern "(SyncWorker|SyncManager|SYNC|LocalOp|enqueue|pending)"
```

#### 3.3 Test Sync with Network Toggle

```powershell
# Disable network
& $env:ADB shell cmd connectivity airplane-mode enable

# Create a listing while offline (via UI)
# Re-enable network
& $env:ADB shell cmd connectivity airplane-mode disable

# Monitor sync completion
& $env:ADB logcat -v time | Select-String -Pattern "(sync|upload|success)" -Quiet
```

**Pass Criteria:**

- [ ] Offline changes queue in LocalOp table
- [ ] Sync triggers automatically when online
- [ ] Logcat shows successful upload after reconnection

---

### 4. Offline Functionality & Caching

#### 4.1 Verify Room Database Schema

```powershell
# Check Room entities exist
Get-ChildItem -Path ".\app\src\main\java\com\senthapps\slagrimarket\data" -Recurse -Filter "*.kt" |
    Select-String -Pattern "@Entity"
```

#### 4.2 Test Offline Mode

```powershell
# Load app with data (while online)
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity
Start-Sleep -Seconds 5

# Go offline
& $env:ADB shell cmd connectivity airplane-mode enable

# Navigate through all main screens - data should still display
# Monitor for cache hits
& $env:ADB logcat -v time | Select-String -Pattern "(cache|Room|DAO|local)"
```

#### 4.3 Verify Offline Operation Types

```powershell
# Check LocalOp implementation
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\data" -Recurse -Pattern "LocalOp|OperationType"
# Expected: CREATE, UPDATE, DELETE operation types
```

**Pass Criteria:**

- [ ] App displays cached data when offline
- [ ] CRUD operations create LocalOp entries offline
- [ ] No crash when offline (graceful degradation)
- [ ] Pending operations persist across app restart

---

### 5. CRUD Operations for Marketplace Features

#### 5.1 Test Listing CRUD via Logcat

```powershell
& $env:ADB logcat -c
& $env:ADB logcat -v time | Select-String -Pattern "(listing|Listing|POST|PUT|DELETE|GET /api/listings)"
```

**Manual Test Steps:**

1. **CREATE:** Add new listing → Verify HTTP 201 in logcat
2. **READ:** View listings → Verify HTTP 200 in logcat
3. **UPDATE:** Edit listing details → Verify HTTP 200 in logcat
4. **DELETE:** Remove listing → Verify HTTP 200 in logcat

#### 5.2 Verify Transaction CRUD

```powershell
& $env:ADB logcat -v time | Select-String -Pattern "(transaction|Transaction|order)"
```

**Pass Criteria:**

- [ ] Create listing → HTTP 201 response
- [ ] Fetch listings → HTTP 200 with data
- [ ] Update listing → HTTP 200 response
- [ ] Delete listing → HTTP 200/204 response
- [ ] Transaction creation works end-to-end

---

### 6. API Error Handling & Retry Logic

#### 6.1 Verify Error Handler Implementation

```powershell
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\util\ErrorHandling.kt" -Pattern "getErrorMessage|UnknownHostException|SocketTimeoutException"
```

#### 6.2 Verify Retry Configuration

```powershell
# Check SyncWorker retry policy
Select-String -Path ".\app\src\main\java\com\senthapps" -Recurse -Pattern "backoffPolicy|BackoffPolicy|Result.retry|maxAttempts"
```

#### 6.3 Simulate Network Error

```powershell
# Temporarily break network during operation
& $env:ADB shell cmd connectivity airplane-mode enable
# Trigger API call via UI
& $env:ADB shell cmd connectivity airplane-mode disable

# Monitor retry behavior
& $env:ADB logcat -v time | Select-String -Pattern "(retry|Retry|attempt|backoff)"
```

**Pass Criteria:**

- [ ] Network errors show user-friendly messages
- [ ] Failed requests automatically retry with backoff
- [ ] Max retry limit prevents infinite loops

---

### 7. Network Connectivity Detection

#### 7.1 Verify Network Monitoring

```powershell
# Check for connectivity checks
Select-String -Path ".\app\src\main\java\com\senthapps" -Recurse -Pattern "ConnectivityManager|NetworkCallback|isNetworkAvailable"
```

#### 7.2 Test Connectivity UI Feedback

```powershell
# Toggle airplane mode and observe UI
& $env:ADB shell cmd connectivity airplane-mode enable
Start-Sleep -Seconds 3
# App should show offline indicator or appropriate message

& $env:ADB shell cmd connectivity airplane-mode disable
Start-Sleep -Seconds 3
# App should resume normal operation
```

**Pass Criteria:**

- [ ] App detects network state changes
- [ ] UI provides feedback when offline
- [ ] Operations gracefully degrade offline

---

## 🟡 HIGH PRIORITY - User Experience

### 8. Trilingual Support (English/Tamil/Sinhala)

#### 8.1 Verify String Resources Exist

```powershell
# Count strings in each language
(Get-Content ".\app\src\main\res\values\strings.xml" | Select-String "<string").Count
(Get-Content ".\app\src\main\res\values-ta\strings.xml" | Select-String "<string").Count
(Get-Content ".\app\src\main\res\values-si\strings.xml" | Select-String "<string").Count
# Expected: 173+ strings in each file
```

#### 8.2 Compare String Keys Across Languages

```powershell
# Extract string names and compare
$en = (Get-Content ".\app\src\main\res\values\strings.xml" | Select-String 'name="(\w+)"' -AllMatches).Matches.Groups[1].Value | Sort-Object
$ta = (Get-Content ".\app\src\main\res\values-ta\strings.xml" | Select-String 'name="(\w+)"' -AllMatches).Matches.Groups[1].Value | Sort-Object
$si = (Get-Content ".\app\src\main\res\values-si\strings.xml" | Select-String 'name="(\w+)"' -AllMatches).Matches.Groups[1].Value | Sort-Object

Compare-Object $en $ta
Compare-Object $en $si
# Expected: No differences (all keys translated)
```

#### 8.3 Test Language Toggle via ADB

```powershell
# Change device language to Tamil
& $env:ADB shell "setprop persist.sys.locale ta-IN; stop; start"

# Capture screenshot
& $env:ADB exec-out screencap -p > tamil_screen.png

# Change to Sinhala
& $env:ADB shell "setprop persist.sys.locale si-LK; stop; start"
& $env:ADB exec-out screencap -p > sinhala_screen.png

# Reset to English
& $env:ADB shell "setprop persist.sys.locale en-US; stop; start"
```

**Pass Criteria:**

- [ ] All 173+ strings present in all 3 languages
- [ ] No missing translations (string names match)
- [ ] Language toggle changes all visible text
- [ ] RTL layout adjustments work (if applicable)

---

### 9. Material Design 3 Dark Theme Consistency

#### 9.1 Verify Theme Colors

```powershell
# Check for Green-600 CTA color
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\ui\theme" -Recurse -Pattern "16a34a|Green.?600"

# Check for Blue-400 secondary action color
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\ui\theme" -Recurse -Pattern "60a5fa|Blue.?400"
```

#### 9.2 Test Dark Theme via ADB

```powershell
# Enable dark mode
& $env:ADB shell "cmd uimode night yes"
& $env:ADB exec-out screencap -p > dark_mode_home.png

# Navigate to key screens and capture
& $env:ADB shell input tap 540 2000  # Tab to navigate
& $env:ADB exec-out screencap -p > dark_mode_listings.png

# Disable dark mode
& $env:ADB shell "cmd uimode night no"
```

**Pass Criteria:**

- [ ] Green-600 (#16a34a) used for primary CTAs
- [ ] Blue-400 (#60a5fa) used for secondary actions
- [ ] Dark theme applies consistently across all screens
- [ ] Text remains readable in dark mode

---

### 10. UI Responsiveness

#### 10.1 Test on Multiple Screen Sizes

```powershell
# Get current display info
& $env:ADB shell wm size
& $env:ADB shell wm density

# Simulate different screen sizes (reset after testing)
& $env:ADB shell wm size 1080x1920  # Standard phone
& $env:ADB shell wm size 1440x2560  # Large phone
& $env:ADB shell wm size 800x1280   # Tablet
& $env:ADB shell wm size reset      # Reset to default
```

#### 10.2 Test Different Android Versions

```powershell
# Check minSdk and targetSdk
Select-String -Path ".\app\build.gradle.kts" -Pattern "minSdk|targetSdk"
# Expected: minSdk = 24, targetSdk = 36
```

**Pass Criteria:**

- [ ] UI scales properly across screen sizes
- [ ] No cut-off text or overlapping elements
- [ ] Works on Android 7.0 (API 24) through Android 14 (API 34)

---

### 11. Form Validation & Error Messages

#### 11.1 Test Form Validation (Manual Steps)

1. **Phone Number Field:** Enter invalid format → Should show error
2. **Listing Price:** Enter non-numeric value → Should reject
3. **Required Fields:** Leave empty → Should prevent submission
4. **Image Upload:** Select >5MB image → Should show size error

#### 11.2 Verify Error Messages in All Languages

```powershell
# Check for validation error strings
Select-String -Path ".\app\src\main\res\values\strings.xml" -Pattern "error|invalid|required"
Select-String -Path ".\app\src\main\res\values-ta\strings.xml" -Pattern "error|invalid|required"
Select-String -Path ".\app\src\main\res\values-si\strings.xml" -Pattern "error|invalid|required"
```

**Pass Criteria:**

- [ ] All form fields have validation
- [ ] Error messages display in current language
- [ ] Validation prevents invalid data submission

---

### 12. Loading States & Progress Indicators

#### 12.1 Verify Loading Skeleton Implementation

```powershell
# Check for loading components
Get-ChildItem -Path ".\app\src\main\java\com\senthapps\slagrimarket\ui\components" -Recurse -Filter "*Loading*.kt"
Select-String -Path ".\app\src\main\java\com\senthapps" -Recurse -Pattern "LoadingSkeleton|shimmer|CircularProgressIndicator"
```

#### 12.2 Test Slow Network Loading States

```powershell
# Throttle network to see loading states
& $env:ADB shell "tc qdisc add dev wlan0 root netem delay 3000ms"
# Navigate through app - loading states should be visible
& $env:ADB shell "tc qdisc del dev wlan0 root"  # Remove throttle
```

**Pass Criteria:**

- [ ] Loading skeletons appear during data fetch
- [ ] Progress indicators for long operations
- [ ] Smooth transitions from loading to content

---

### 12b. TalkBack Accessibility

#### 12b.1 Verify Semantic Descriptions

```powershell
# Check for contentDescription in components
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\ui\components" -Recurse -Pattern "semantics|contentDescription|Modifier.semantics"
```

#### 12b.2 Test with TalkBack Enabled

```powershell
# Enable TalkBack
& $env:ADB shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
& $env:ADB shell settings put secure accessibility_enabled 1

# Navigate through app - verify all elements are announced

# Disable TalkBack when done
& $env:ADB shell settings put secure enabled_accessibility_services ""
& $env:ADB shell settings put secure accessibility_enabled 0
```

**Pass Criteria:**

- [ ] All interactive elements have content descriptions
- [ ] TalkBack announces elements in logical order
- [ ] Buttons and actions clearly described in all languages
- [ ] No "unlabeled" elements reported

---

## 🟠 MEDIUM PRIORITY - Quality Assurance

### 13. Complete Test Suite Verification

#### 13.1 Test Coverage Overview

| Test Type         | Tests  | Files  | Description              |
| ----------------- | ------ | ------ | ------------------------ |
| **Unit Tests**    | 142    | 12     | ViewModels, Repositories |
| **UI Automation** | 27     | 5      | Screen tests, Navigation |
| **Total**         | **75** | **12** | Full test suite          |

#### 13.2 Run All Unit Tests

```powershell
# Run unit tests
.\gradlew test

# Run with coverage report
.\gradlew jacocoTestReport
# View: app/build/reports/jacoco/html/index.html

# Run specific unit test file
.\gradlew test --tests "com.senthapps.slagrimarket.data.repository.AuthRepositoryTest"
```

**Unit Test Files:**
| Test File | Purpose |
|-----------|---------|
| `AuthRepositoryTest.kt` | Authentication repository tests |
| `ListingRepositoryTest.kt` | Listing repository tests |
| `AuthViewModelTest.kt` | Auth ViewModel tests |
| `HomeViewModelTest.kt` | Home ViewModel tests |
| `ListingsViewModelTest.kt` | Listings ViewModel tests |
| `TransactionsViewModelTest.kt` | Transactions ViewModel tests |
| `ExampleUnitTest.kt` | Basic sanity tests |

#### 13.3 Run UI Automation Tests

```powershell
# Run all UI tests (requires device/emulator)
.\gradlew connectedAndroidTest

# Run specific UI test class
.\gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.senthapps.slagrimarket.ui.HomeScreenTest

# Run with test orchestrator for isolation
.\gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.clearPackageData=true
```

**UI Test Files:**
| Test File | Purpose | Test Count |
|-----------|---------|------------|
| `HomeScreenTest.kt` | Home screen verification | 7 tests |
| `ListingsScreenTest.kt` | Listings CRUD UI | 6 tests |
| `TransactionsScreenTest.kt` | Transaction flow | 7 tests |
| `NavigationTest.kt` | Navigation flow | 6 tests |
| `ExampleInstrumentedTest.kt` | Basic sanity | 1 test |

#### 13.4 Run Lint Checks

```powershell
# Run Android lint
.\gradlew lint

# View report: app/build/reports/lint-results-debug.html
```

#### 13.5 Custom ADB UI Automation Script

```powershell
# Login flow automation
& $env:ADB shell input text "0771234567"  # Enter phone
& $env:ADB shell input tap 540 1800        # Submit
Start-Sleep -Seconds 5
# Note: OTP must be entered manually or mocked

# Navigate to Listings tab
& $env:ADB shell input tap 200 2300        # First tab
& $env:ADB shell input tap 400 2300        # Second tab (Listings)

# Create listing flow
& $env:ADB shell input tap 900 2200        # FAB button
```

**Pass Criteria:**

- [ ] All 142 unit tests pass (`.\gradlew test`)
- [ ] All 27 UI tests pass (`.\gradlew connectedAndroidTest`)
- [ ] Lint check passes with no errors (`.\gradlew lint`)
- [ ] Test coverage ≥50% for critical code
- [ ] No flaky tests (run 3x with same results)

---

### 14. Logcat Verification of API Calls

#### 14.1 Enable Verbose Logging

```powershell
# Verify logging is enabled in debug build
Select-String -Path ".\app\build.gradle.kts" -Pattern "ENABLE_LOGGING"
# Expected: debug { ENABLE_LOGGING = true }
```

#### 14.2 Monitor All API Traffic

```powershell
# Real-time API monitoring with timestamps
& $env:ADB logcat -v time "*:S OkHttp:D Retrofit:D"

# Log to file for analysis
& $env:ADB logcat -v time -d > api_traffic.log
Get-Content api_traffic.log | Select-String -Pattern "200|201|OK|POST|GET|PUT|DELETE"
```

#### 14.3 Verify Not Using Cached Data Only

```powershell
# Look for actual network requests vs cache hits
& $env:ADB logcat -v time | Select-String -Pattern "(-->|<--)"
# --> = outgoing request, <-- = response
# If only cache hits, you'll see Room/DAO logs without HTTP traffic
```

**Pass Criteria:**

- [ ] Logcat shows actual HTTP requests
- [ ] HTTP 200/201 responses visible
- [ ] Network traffic occurs on data refresh

---

### 15. Memory Leak Detection

#### 15.1 Verify LeakCanary Integration

```powershell
Select-String -Path ".\app\build.gradle.kts" -Pattern "leakcanary"
# Expected: debugImplementation(libs.leakcanary)
```

#### 15.2 Run Memory Leak Detection

```powershell
# Install debug build
.\gradlew installDebug

# Navigate through app multiple times
# LeakCanary will show notification if leaks detected

# Check logcat for leak reports
& $env:ADB logcat -v time | Select-String -Pattern "LeakCanary|HEAP ANALYSIS|leaking"
```

**Pass Criteria:**

- [ ] LeakCanary installed in debug builds
- [ ] No memory leaks reported during navigation
- [ ] Activities/Fragments properly destroyed

---

### 16. APK Size Optimization

#### 16.1 Build Release APK and Check Size

```powershell
.\gradlew assembleRelease

# Check APK size
Get-ChildItem ".\app\build\outputs\apk\release\*.apk" | Select-Object Name, @{N='SizeMB';E={[math]::Round($_.Length/1MB,2)}}
# Target: < 20MB for base APK

# Build AAB for Play Store
.\gradlew bundleRelease
Get-ChildItem ".\app\build\outputs\bundle\release\*.aab" | Select-Object Name, @{N='SizeMB';E={[math]::Round($_.Length/1MB,2)}}
```

#### 16.2 Analyze APK Contents

```powershell
# Use Android Studio APK Analyzer or:
$APK_ANALYZER = "$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin\apkanalyzer.bat"
& $APK_ANALYZER -h files list .\app\build\outputs\apk\release\*.apk
```

**Pass Criteria:**

- [ ] Release APK < 20MB
- [ ] R8/ProGuard shrinking enabled
- [ ] No debug resources in release build

---

### 17. Crash Reporting & Analytics

#### 17.1 Verify Firebase Crashlytics Setup

```powershell
# Check Crashlytics initialization
Select-String -Path ".\app\src\main\java\com\senthapps\slagrimarket\JaffnaMarketplaceApplication.kt" -Pattern "Crashlytics|setCrashCollectionEnabled"

# Check google-services.json exists
Test-Path ".\app\google-services.json"
```

#### 17.2 Test Crash Reporting

```powershell
# Force a test crash (if crash test button exists)
# Or check logcat for Crashlytics initialization
& $env:ADB logcat -v time | Select-String -Pattern "Crashlytics|FirebaseCrashlytics"
```

**Pass Criteria:**

- [ ] Firebase Crashlytics initialized
- [ ] Crashes uploaded to Firebase Console
- [ ] User identifiers properly set

---

### 18. Edge Case Handling

#### 18.1 Test Empty States

```powershell
# Clear app data and login fresh
& $env:ADB shell pm clear com.senthapps.slagrimarket
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity

# Navigate to listings - should show empty state
# Check for EmptyState component
Select-String -Path ".\app\src\main\java\com\senthapps" -Recurse -Pattern "EmptyState"
```

#### 18.2 Test Poor Network Conditions

```powershell
# Simulate poor network (if supported)
& $env:ADB shell svc wifi disable
Start-Sleep -Seconds 2
& $env:ADB shell svc data enable  # Mobile data only

# Perform actions and check behavior
& $env:ADB shell svc wifi enable
```

**Pass Criteria:**

- [ ] Empty states display appropriate messages
- [ ] App handles slow/intermittent network
- [ ] No crashes on edge cases

---

## 🟢 STANDARD - Production Requirements

### 19. App Signing Configuration

#### 19.1 Verify Release Signing

```powershell
# Check for signing config (shouldn't expose keys, just verify config exists)
Select-String -Path ".\app\build.gradle.kts" -Pattern "signingConfigs|release"

# Verify keystore exists (if configured)
Test-Path ".\app\release-keystore.jks"
```

#### 19.2 Build Signed Release

```powershell
.\gradlew bundleRelease
# Check if AAB is signed
$AAB = Get-ChildItem ".\app\build\outputs\bundle\release\*.aab" -Name
& jarsigner -verify ".\app\build\outputs\bundle\release\$AAB" -verbose
```

**Pass Criteria:**

- [ ] Release keystore configured
- [ ] AAB properly signed
- [ ] Keystore password not hardcoded

---

### 20. ProGuard/R8 Obfuscation

#### 20.1 Verify ProGuard Configuration

```powershell
# Check ProGuard is enabled
Select-String -Path ".\app\build.gradle.kts" -Pattern "isMinifyEnabled|isShrinkResources"
# Expected: isMinifyEnabled = true, isShrinkResources = true

# Count ProGuard rules
(Get-Content ".\app\proguard-rules.pro").Count
# Expected: 100+ lines of rules
```

#### 20.2 Verify Critical Classes Preserved

```powershell
Select-String -Path ".\app\proguard-rules.pro" -Pattern "keep class.*model|keep class.*dto|Retrofit|Moshi|Room|Firebase"
```

**Pass Criteria:**

- [ ] `isMinifyEnabled = true` in release
- [ ] `isShrinkResources = true` in release
- [ ] API models preserved in ProGuard rules

---

### 21. Version Code & Version Name

#### 21.1 Check Version Configuration

```powershell
Select-String -Path ".\app\build.gradle.kts" -Pattern "versionCode|versionName"
# Expected: Incremented from previous release
```

#### 21.2 Verify Version in Built APK

```powershell
& $env:ADB shell "dumpsys package com.senthapps.slagrimarket | grep -E 'versionCode|versionName'"
```

**Pass Criteria:**

- [ ] versionCode incremented for new release
- [ ] versionName follows semantic versioning (e.g., 1.0.0)
- [ ] Matches what's shown in app settings

---

### 22. Permissions Audit

#### 22.1 List Requested Permissions

```powershell
Select-String -Path ".\app\src\main\AndroidManifest.xml" -Pattern "uses-permission"
```

#### 22.2 Verify Only Necessary Permissions

```
Required permissions:
- INTERNET (API calls)
- ACCESS_NETWORK_STATE (connectivity check)
- READ_EXTERNAL_STORAGE (image picker - API 28 and below)
- READ_MEDIA_IMAGES (image picker - API 29+)
- CAMERA (optional, for taking photos)
- ACCESS_FINE_LOCATION (optional, for location features)

Avoid:
- CALL_PHONE
- READ_CONTACTS
- RECORD_AUDIO (unless voice features)
```

**Pass Criteria:**

- [ ] Only required permissions declared
- [ ] Dangerous permissions have runtime requests
- [ ] No unused permissions

---

### 23. Privacy Policy & Terms Integration

#### 23.1 Check for Legal Links

```powershell
Select-String -Path ".\app\src\main" -Recurse -Pattern "privacy.policy|terms.of.service|legal"
```

#### 23.2 Manual Verification

1. Go to Settings/About screen
2. Tap Privacy Policy → Should open browser with policy
3. Tap Terms of Service → Should open browser with terms

**Pass Criteria:**

- [ ] Privacy Policy link present and working
- [ ] Terms of Service link present and working
- [ ] Links accessible from Settings/About screen

---

### 24. SDK Version Verification

#### 24.1 Verify SDK Versions

```powershell
Select-String -Path ".\app\build.gradle.kts" -Pattern "minSdk|targetSdk|compileSdk"
```

#### 24.2 Expected Values

| Property     | Value | Notes              |
| ------------ | ----- | ------------------ |
| `minSdk`     | 24    | Android 7.0 Nougat |
| `targetSdk`  | 36    | Latest stable      |
| `compileSdk` | 36    | Latest stable      |

**Pass Criteria:**

- [ ] minSdk = 24 (Android 7.0+)
- [ ] targetSdk = 34 or higher (Play Store requirement)
- [ ] compileSdk matches targetSdk

---

### 25. Google Play Store Requirements

#### 25.1 Build App Bundle

```powershell
.\gradlew bundleRelease
```

#### 25.2 Screenshot Requirements

| Screen     | Sizes Needed    | Languages  |
| ---------- | --------------- | ---------- |
| Phone      | 2-8 screenshots | EN, TA, SI |
| Tablet 7"  | 1-8 screenshots | EN, TA, SI |
| Tablet 10" | 1-8 screenshots | EN, TA, SI |

#### 25.3 Store Listing Checklist

- [ ] Short description (80 chars) in all 3 languages
- [ ] Full description (4000 chars) in all 3 languages
- [ ] Feature graphic (1024x500)
- [ ] App icon (512x512)
- [ ] Privacy policy URL
- [ ] Content rating completed
- [ ] Target audience selected

**Pass Criteria:**

- [ ] AAB builds successfully
- [ ] All store assets prepared
- [ ] Descriptions in EN/TA/SI

---

## 🧪 Final Verification Commands

### Run All Unit Tests

```powershell
.\gradlew test
```

### Run All UI Tests

```powershell
.\gradlew connectedAndroidTest
```

### Run Lint Checks

```powershell
.\gradlew lint
```

### Build Release Bundle

```powershell
.\gradlew bundleRelease
```

### Test Coverage Report

```powershell
.\gradlew jacocoTestReport
# View: app/build/reports/jacoco/html/index.html
```

---

## 📊 Summary Checklist

| Category                           | Total Items | Status |
| ---------------------------------- | ----------- | ------ |
| 🚨 PRODUCTION BUILD (PB-1 to PB-7) | 7           | [ ]    |
| 🔴 CRITICAL (1-7)                  | 7           | [ ]    |
| 🟡 HIGH (8-12)                     | 5           | [ ]    |
| 🟠 MEDIUM (13-18)                  | 6           | [ ]    |
| 🟢 STANDARD (19-25)                | 7           | [ ]    |
| **TOTAL**                          | **32**      | [ ]    |

### ⚡ Priority Order for Launch

1. **Production Build Verification (PB-1 to PB-7)** - Must pass before any other testing
2. **Critical (1-7)** - Core functionality verification
3. **High (8-12)** - User experience essentials
4. **Medium (13-18)** - Quality assurance
5. **Standard (19-25)** - Play Store requirements

---

**Document Created:** December 21, 2025  
**For:** Agrimarket Android App v1.0 Pre-Launch  
**Based On:** PRODUCTION_READINESS_ASSESSMENT.md (100% MVP Complete)
