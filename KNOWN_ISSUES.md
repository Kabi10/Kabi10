# Known Issues and Limitations

This document tracks current limitations, known bugs, and technical debt in the Agrimarket Android app. These items are documented for transparency and to help contributors understand the current state of the project.

## Current Limitations

### 1. Testing Coverage

**Status**: ⚠️ Critical Gap

**Description**: The project has comprehensive testing infrastructure configured (JUnit, MockK, Espresso, Compose Test) but currently only contains placeholder tests.

**Current State**:
- `ExampleUnitTest.kt` - Only tests `2 + 2 = 4`
- `ExampleInstrumentedTest.kt` - Only tests package name

**Impact**: 
- No automated verification of business logic
- Regression bugs may go undetected
- Refactoring is risky without test coverage

**Recommended Action**:
- Start with ViewModel unit tests (see `CONTRIBUTING.md` for examples)
- Add UI tests for critical user flows (listing creation, transactions)
- Target >70% code coverage for new features

**Related Files**:
- `app/src/test/java/com/senthapps/slagrimarket/ExampleUnitTest.kt`
- `app/src/androidTest/java/com/senthapps/slagrimarket/ExampleInstrumentedTest.kt`

---

### 2. Demo Authentication Mode

**Status**: ⚠️ By Design (MVP)

**Description**: The app currently uses a demo/bypass authentication system instead of real OTP verification.

**Current Behavior**:
- Mock user automatically logged in on app start
- OTP API endpoints exist but use simplified verification
- No real SMS integration

**Impact**:
- Cannot be used in production without implementing real authentication
- Security risk if deployed as-is

**Implementation Details**:
- `AuthRepository.kt` - Contains `mockUser` and `initializeMockAuthState()`
- `AuthViewModel.kt` - Bypasses real OTP flow

**Future Work**:
- Integrate real SMS provider (Dialog Ideamart, Mobitel, or Twilio)
- Implement proper OTP generation and verification
- Add rate limiting and security measures

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/AuthRepository.kt`
- `backend/.env.example` - Contains SMS provider configuration templates

---

### 3. Backend API Deployment Status

**Status**: ⚠️ Configuration Required

**Description**: The backend API may not be deployed or accessible at the configured production URL.

**Current Configuration**:
- Production URL: `https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/`
- Debug URL: `http://10.0.2.2:3000/api/` (Android emulator localhost)

**Impact**:
- App may fail to fetch data in release builds
- Offline-first design mitigates this partially (cached data still works)

**Recommended Action**:
- Verify backend is deployed to Vercel
- Test API endpoints are accessible
- Configure environment variables in Vercel dashboard
- See `docs/PRODUCTION_DEPLOYMENT_GUIDE.md` for deployment instructions

**Related Files**:
- `app/build.gradle.kts` - BuildConfig.BASE_URL configuration
- `backend/vercel.json` - Vercel deployment configuration
- `backend/.env.example` - Required environment variables

---

### 4. Firebase Configuration Required

**Status**: ⚠️ Setup Required

**Description**: Each developer/intern needs to configure their own Firebase project.

**Current State**:
- `google-services.json` is gitignored (security best practice)
- Template file provided: `app/google-services.json.template`

**Impact**:
- App will crash on first run without Firebase configuration
- Firebase features won't work (Analytics, Crashlytics, Storage, Messaging)

**Setup Instructions**:
1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app with package name: `com.senthapps.slagrimarket`
3. Download `google-services.json`
4. Place it in `app/` directory
5. Enable required services: Analytics, Crashlytics, Storage, Cloud Messaging

**Related Files**:
- `app/google-services.json.template` - Template with placeholder values
- `docs/FIREBASE_INTEGRATION.md` - Detailed Firebase setup guide

---

### 5. Google Maps API Key Required

**Status**: ⚠️ Configuration Required

**Description**: Map functionality requires a Google Maps API key.

**Current State**:
- Maps library is included in dependencies
- API key not configured in manifest or BuildConfig

**Impact**:
- Map features may not work or show "For development purposes only" watermark
- Location-based features limited

**Setup Instructions**:
1. Get API key from Google Cloud Console
2. Enable Maps SDK for Android
3. Add to `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE"/>
   ```

**Related Files**:
- `docs/MAP_IMPLEMENTATION.md` - Map integration guide
- `app/src/main/AndroidManifest.xml` - Where API key should be added

---

### 6. Image Upload Requires Firebase Storage

**Status**: ⚠️ Configuration Required

**Description**: Image upload functionality depends on Firebase Storage being properly configured.

**Current State**:
- Image upload code implemented
- Firebase Storage rules may need configuration
- Storage bucket must be created in Firebase Console

**Impact**:
- Image uploads will fail without proper Firebase Storage setup
- Listing creation with images won't work

**Setup Instructions**:
1. Enable Firebase Storage in Firebase Console
2. Configure storage rules (see `docs/IMAGE_UPLOAD_IMPLEMENTATION.md`)
3. Ensure storage bucket matches `google-services.json` configuration

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/util/ImageUploadUtil.kt`
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/StorageRepository.kt`
- `docs/IMAGE_UPLOAD_IMPLEMENTATION.md`

---

## Known Bugs

### 7. Language Preference Not Persisted

**Status**: 🐛 Bug

**Description**: Selected language doesn't persist across app restarts.

**Expected Behavior**: Language selection should be saved and restored on app launch.

**Actual Behavior**: App always starts in default language (Tamil).

**Workaround**: User must re-select language each time app is opened.

**Suggested Fix**: 
- Implement DataStore persistence in `LanguagePreferences.kt`
- Load saved language in `LanguageToggleViewModel` initialization

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/data/preferences/LanguagePreferences.kt`
- `app/src/main/java/com/senthapps/slagrimarket/ui/common/LanguageToggleViewModel.kt`

**Good First Issue**: ⭐ Recommended for interns

---

### 8. Sync Worker Not Fully Tested

**Status**: ⚠️ Untested

**Description**: Background sync worker exists but hasn't been thoroughly tested in production scenarios.

**Potential Issues**:
- Sync conflicts not fully handled
- Battery optimization may prevent background sync
- Network state changes not optimally handled

**Recommended Action**:
- Add comprehensive tests for `SyncWorker`
- Test with various network conditions
- Implement better conflict resolution strategy

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/workers/SyncWorker.kt`
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/SyncRepository.kt`

---

## Technical Debt

### 9. Hardcoded Sample Data

**Status**: 📝 Technical Debt

**Description**: Some repositories create sample/mock data as fallback when API fails.

**Examples**:
- `ActivityRepository.createSampleActivities()` - Creates fake activities
- Market prices may use sample data

**Impact**:
- Can be confusing for users (fake data looks real)
- Should be clearly marked as "demo data" in UI

**Recommended Action**:
- Add UI indicators for demo/sample data
- Make sample data generation configurable (debug builds only)
- Remove sample data generation in production builds

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/ActivityRepository.kt`

---

### 10. No Proguard Rules for Release

**Status**: 📝 Technical Debt

**Description**: Release builds have `isMinifyEnabled = false`, meaning no code obfuscation or optimization.

**Impact**:
- Larger APK size
- Easier to reverse engineer
- Slower app performance

**Recommended Action**:
- Enable ProGuard/R8 for release builds
- Add keep rules for Retrofit, Moshi, Room
- Test release builds thoroughly after enabling

**Related Files**:
- `app/build.gradle.kts` - Build configuration
- `app/proguard-rules.pro` - ProGuard rules file

---

### 11. Missing Offline Indicators

**Status**: 📝 Enhancement Needed

**Description**: App doesn't clearly indicate when operating in offline mode.

**Current Behavior**: 
- Data loads from cache silently
- No visual feedback about network status

**Recommended Enhancement**:
- Add network status indicator in UI
- Show "Offline Mode" banner when no connectivity
- Indicate when data is stale/cached

**Related Files**:
- All screen composables
- Could add a `NetworkStatusViewModel` for global state

---

## Performance Considerations

### 12. Image Loading Optimization

**Status**: 📝 Could Be Improved

**Description**: Image loading uses Coil but could benefit from additional optimization.

**Potential Improvements**:
- Implement image caching strategy
- Add placeholder/error images
- Lazy loading for image galleries
- Image compression before upload

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/util/ImageUploadUtil.kt`
- All screens displaying images

---

### 13. Database Query Optimization

**Status**: 📝 Monitor Performance

**Description**: Some Room queries could be optimized with proper indexing.

**Recommended Action**:
- Add indexes to frequently queried columns
- Use `@Query` with LIMIT for large datasets
- Monitor query performance with Room's query logging

**Related Files**:
- All DAO files in `app/src/main/java/com/senthapps/slagrimarket/data/dao/`

---

## Documentation Gaps

### 14. API Documentation

**Status**: 📝 Missing

**Description**: Backend API endpoints are not fully documented.

**Impact**: 
- Difficult for new developers to understand API contracts
- No clear specification for request/response formats

**Recommended Action**:
- Add OpenAPI/Swagger documentation
- Document all endpoints in `backend/README.md`
- Include example requests/responses

---

### 15. Database Schema Documentation

**Status**: 📝 Partial

**Description**: Room database schema is exported but not documented.

**Current State**:
- Schema JSON files in `app/schemas/`
- No human-readable documentation

**Recommended Action**:
- Create ER diagram
- Document table relationships
- Explain migration strategy

**Related Files**:
- `app/schemas/` - Auto-generated schema files

---

## How to Report New Issues

If you discover a new issue:

1. **Check this document** to see if it's already known
2. **Search GitHub Issues** to avoid duplicates
3. **Create a new issue** with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Android version and device
   - Logcat output (if applicable)
   - Screenshots (if applicable)

See `CONTRIBUTING.md` for detailed bug reporting guidelines.

---

**Last Updated**: 2025-10-25  
**Document Version**: 1.0

