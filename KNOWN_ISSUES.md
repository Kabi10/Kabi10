# Known Issues and Limitations - Agrimarket

This document tracks current bugs, limitations, technical debt, and workarounds for the Agrimarket Android app. It helps developers understand what's being worked on and what to expect.

**Last Updated:** 2025-11-14

---

## 📊 Current Status

- **Version:** 1.0-debug
- **Build Status:** ✅ Passing
- **Test Coverage:** ~0% (needs significant improvement)
- **Open Issues:** See [GitHub Issues](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues)
- **Active PRs:** See [Pull Requests](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/pulls)

---

## 🔴 Critical Issues

### None Currently

All critical issues have been addressed in recent PRs. See [Resolved Issues](#resolved-issues) below.

---

## 🟠 High Priority Issues

### 1. Test Coverage Gap

**Status:** 🟠 **Open** - Tracked in [ROADMAP.md](ROADMAP.md)

**Description:**
- Current test coverage is approximately 0%
- No unit tests for ViewModels
- No integration tests for repositories
- No UI tests for screens

**Impact:**
- Difficult to catch regressions
- Refactoring is risky without test safety net
- Cannot verify business logic correctness

**Workaround:**
- Manual testing on physical devices
- Thorough code review process
- Incremental testing as features are added

**Planned Fix:**
- See ROADMAP.md "Testing Infrastructure" section
- Priority: High
- Timeline: Q1 2026

---

### 2. Language Preference Not Persisted

**Status:** 🟠 **Open** - Tracked in [Issue #14](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues/14)

**Description:**
- Language selection resets to English on app restart
- User must re-select Tamil or Sinhala each time
- No persistent storage of language preference

**Impact:**
- Poor user experience for non-English speakers
- Users must change language every time they open the app

**Workaround:**
- Users can quickly toggle language using the top bar button
- Language state persists during app session (only resets on restart)

**Planned Fix:**
- Store language preference in DataStore
- Load saved preference on app startup
- Assigned to: Aqeel
- Timeline: Next sprint

---

## 🟡 Medium Priority Issues

### 1. Offline Sync Conflict Resolution

**Status:** 🟡 **Open** - Design phase

**Description:**
- When multiple users edit the same listing offline, conflicts may occur
- Current implementation uses "last write wins" strategy
- No user notification of conflicts
- No manual conflict resolution UI

**Impact:**
- Data loss possible in multi-user scenarios
- Users unaware when their changes are overwritten

**Workaround:**
- Primarily single-user app in current deployment
- Conflicts are rare in practice
- Backend tracks modification timestamps

**Planned Fix:**
- Implement conflict detection UI
- Allow users to choose which version to keep
- Show diff of conflicting changes
- Timeline: Q2 2026

---

### 2. Image Upload Size Limitations

**Status:** 🟡 **Open** - Documented limitation

**Description:**
- Images larger than 5MB may fail to upload
- No client-side image compression
- Large images consume significant storage and bandwidth

**Impact:**
- Users with high-resolution cameras may experience upload failures
- Slow upload times on poor network connections

**Workaround:**
- Users can resize images before uploading using external apps
- Most modern phones produce images under 5MB by default

**Planned Fix:**
- Implement client-side image compression
- Resize images to max 1920x1080 before upload
- Show upload progress indicator
- Timeline: Q1 2026

---

### 3. Limited Search Functionality

**Status:** 🟡 **Open** - Feature enhancement

**Description:**
- Search only supports exact matches
- No fuzzy search or typo tolerance
- Cannot search by price range
- Cannot search by harvest date range

**Impact:**
- Users may miss relevant listings due to spelling variations
- Limited filtering capabilities

**Workaround:**
- Use filter chips for crop types and locations
- Browse all listings in category
- Use English search terms for better results

**Planned Fix:**
- Implement fuzzy search algorithm
- Add advanced filter options (price range, date range)
- Support search in all three languages simultaneously
- Timeline: Q2 2026

---

## 🟢 Low Priority Issues / Minor Bugs

### 1. Keyboard Doesn't Auto-Dismiss

**Status:** 🟢 **Open** - Minor UX issue

**Description:**
- Keyboard remains visible after submitting forms
- User must manually tap outside to dismiss keyboard

**Impact:**
- Minor UX annoyance
- Doesn't affect functionality

**Workaround:**
- Tap outside the text field to dismiss keyboard
- Use device back button to dismiss keyboard

**Planned Fix:**
- Add `LocalFocusManager.clearFocus()` after form submission
- Timeline: Next minor release

---

### 2. Loading Indicators Missing in Some Screens

**Status:** 🟢 **Open** - UX enhancement

**Description:**
- Some screens don't show loading indicators during data fetch
- Users may think the app is frozen

**Impact:**
- Confusing UX during slow network conditions
- Users may tap multiple times thinking the app is unresponsive

**Workaround:**
- Wait a few seconds for data to load
- Check network connection if loading takes too long

**Planned Fix:**
- Add loading indicators to all data-fetching screens
- Implement skeleton screens for better UX
- Timeline: Q1 2026

---

## 🚧 Technical Debt

### 1. Hardcoded String Resources

**Status:** ✅ **Mostly Resolved** - Recent PRs addressed this

**Description:**
- Some error messages and UI text were hardcoded in English
- Breaking trilingual support

**Resolution:**
- PR #17 fixed ViewModel error messages
- PR #18 fixed placeholder text and UI labels
- Remaining instances are minimal and tracked

**Remaining Work:**
- Audit entire codebase for any remaining hardcoded strings
- Add lint rule to prevent future hardcoded strings

---

### 2. Deprecated API Usage

**Status:** ✅ **Resolved** - PR #19 addressed this

**Description:**
- Material Design 3 `Divider()` component was deprecated
- Using old API caused deprecation warnings

**Resolution:**
- PR #19 replaced all `Divider()` with `HorizontalDivider()`
- All deprecation warnings resolved

---

### 3. Missing Documentation

**Status:** ✅ **Resolved** - This document and others created

**Description:**
- Missing QUICKSTART.md, SETUP.md, KNOWN_ISSUES.md
- Broken documentation links in README.md

**Resolution:**
- All critical documentation files created
- README.md links updated
- Comprehensive setup guides available

---

## 🌍 Platform-Specific Issues

### Android 7.0 (API 24) Limitations

**Description:**
- Some Material Design 3 animations may not work smoothly on Android 7.0
- Older devices may experience slower performance

**Impact:**
- Slightly degraded UX on older devices
- App still fully functional

**Workaround:**
- Recommend Android 8.0+ for best experience
- App is optimized for low-end devices where possible

---

### Emulator-Specific Issues

**Description:**
- Camera functionality doesn't work in emulator
- Location services may be unreliable in emulator
- Performance is slower than physical devices

**Impact:**
- Cannot test image upload in emulator
- Cannot test location-based features in emulator

**Workaround:**
- Use physical device for testing camera and location features
- Use mock data for emulator testing

---

## 🔧 Workarounds and Best Practices

### General Development

1. **Always test on physical devices** for camera, location, and performance testing
2. **Use demo user** for development (no OTP required)
3. **Enable airplane mode** to test offline functionality
4. **Clear app data** if you encounter strange behavior
5. **Check Logcat** for detailed error messages

### Building and Running

1. **Clean build** if you encounter build errors: `.\gradlew clean assembleDebug`
2. **Invalidate caches** if Gradle sync fails: **File** → **Invalidate Caches / Restart**
3. **Update dependencies** regularly to get bug fixes
4. **Use latest Android Studio** for best compatibility

### Testing Trilingual Support

1. **Test all three languages** (English, Tamil, Sinhala) for every UI change
2. **Verify string resources** exist in all three `values-*/strings.xml` files
3. **Check for text overflow** in Tamil and Sinhala (longer text)
4. **Test RTL layout** if adding Arabic support in future

---

## 📋 Reporting New Issues

If you discover a new bug or issue:

### 1. Check Existing Issues

Search [GitHub Issues](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues) to see if it's already reported.

### 2. Gather Information

Before reporting, collect:
- **Android version** (e.g., Android 12)
- **Device model** (e.g., Samsung Galaxy S21)
- **App version** (check About screen)
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Screenshots or screen recording** if applicable
- **Logcat output** if available

### 3. Create Detailed Bug Report

Use the bug report template:

```markdown
**Bug Description:**
Brief description of the issue

**Steps to Reproduce:**
1. Open the app
2. Navigate to...
3. Tap on...
4. Observe...

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happens

**Environment:**
- Device: Samsung Galaxy S21
- Android Version: 12
- App Version: 1.0-debug

**Screenshots:**
[Attach screenshots]

**Logcat:**
[Paste relevant logcat output]
```

### 4. Label Appropriately

Use labels to categorize:
- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention needed
- `critical` - Blocks core functionality
- `trilingual-support` - Related to language support

---

## 🔗 Related Resources

- **Roadmap:** [ROADMAP.md](ROADMAP.md) - Planned features and improvements
- **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md) - System design and patterns
- **Contributing:** [CONTRIBUTING.md](CONTRIBUTING.md) - Development workflow
- **Setup Guide:** [SETUP.md](SETUP.md) - Detailed setup instructions
- **Quick Start:** [QUICKSTART.md](QUICKSTART.md) - Get started in 5 steps

---

## ✅ Resolved Issues

### Recently Fixed (Last 30 Days)

- ✅ **Critical UI Bugs** - PR #15
  - Fixed duplicate sections in ProfileScreen
  - Fixed non-clickable buttons in HomeScreen and ProfileScreen
  - Fixed navigation issues

- ✅ **Trilingual Support Completion** - PR #16, #17, #18
  - Fixed Orders page language support
  - Fixed filter options language support
  - Fixed Sinhala title bug
  - Fixed ViewModel error messages
  - Fixed popular crops translation
  - Fixed placeholder text translation
  - Fixed review count translation

- ✅ **Deprecated Components** - PR #19
  - Replaced all `Divider()` with `HorizontalDivider()`
  - Resolved Material Design 3 deprecation warnings

---

**This document is actively maintained. Last review: 2025-11-14**

For questions or clarifications, please comment on the relevant GitHub issue or create a new one.

