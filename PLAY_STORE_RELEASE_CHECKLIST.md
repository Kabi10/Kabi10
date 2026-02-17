# Play Store Release Checklist

**App:** Agrimarket - Sri Lanka Farmers Marketplace
**Package:** com.senthapps.slagrimarket
**Current Version:** 1.0 (versionCode: 1)

---

## Pre-Release Configuration ✅

### 1. Network Security (Phase 1) ✅ COMPLETE
- [x] Debug build allows localhost/10.0.2.2 (for emulator development)
- [x] Release build enforces HTTPS ONLY (no cleartext)
- [x] Variant-specific configs in `app/src/debug/` and `app/src/release/`

**Verification:**
```bash
# Check release APK network config
unzip -p app/build/outputs/apk/release/app-release.apk res/xml/network_security_config.xml
# Should NOT contain cleartextTrafficPermitted="true"
```

---

### 2. Release Signing (Phase 2) ⚠️ ACTION REQUIRED

**Status:** Not configured yet (APK will be unsigned)

**Action Required:**
1. Generate release keystore (one-time):
```bash
keytool -genkey -v -keystore agrimarket-release.keystore \
  -alias agrimarket -keyalg RSA -keysize 2048 -validity 10000
```

2. Add to `local.properties`:
```properties
KEYSTORE_PATH=/absolute/path/to/agrimarket-release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=agrimarket
KEY_PASSWORD=your_key_password
```

3. Rebuild and verify:
```bash
./gradlew assembleRelease
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

**Documentation:** See `docs/RELEASE_SIGNING_SETUP.md`

**⚠️ CRITICAL:** Back up keystore file in 3 secure locations!

---

### 3. ProGuard/R8 Obfuscation (Phase 3) ✅ COMPLETE
- [x] Enhanced rules for Moshi, Room, Retrofit, Hilt
- [x] Data model preservation
- [x] Enum serialization with `@Json` annotations
- [x] Ktor 3.x and Supabase 3.x support
- [x] Debug logging stripped in release
- [x] Unit tests validate serialization

**Verification:**
```bash
./gradlew testDebugUnitTest --tests ProguardTest
# All 11 tests should pass
```

---

### 4. Automated Verification (Phase 4) ✅ COMPLETE

**Run before every release:**
```bash
# Bash (Git Bash, WSL, Linux, macOS)
./scripts/verify_release_build.sh

# PowerShell (Windows)
.\scripts\verify_release_build.ps1
```

**Checks:**
- ✅ Network security (HTTPS only)
- ✅ APK signature
- ✅ ProGuard mapping file (>100KB)
- ✅ APK size (<50MB)
- ✅ Dangerous permissions

---

## Build Commands

### Debug Build (for development)
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (for Play Store)
```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk (signed)
#  OR: app/build/outputs/apk/release/app-release-unsigned.apk (if not configured)
```

### Clean Build (recommended before release)
```bash
./gradlew clean assembleRelease
```

---

## Manual Testing Checklist

Install release APK on **physical device** (not emulator) and test:

### Core Functionality
- [ ] **Login/Authentication:** OTP works, user can log in
- [ ] **Listings:** Fetch listings from production API
- [ ] **Create Listing:** Upload images, set quality grade, save listing
- [ ] **Transactions:** Create transaction, confirm, complete
- [ ] **Messages:** Send/receive messages in real-time
- [ ] **Notifications:** Receive and view notifications

### UI/Display
- [ ] **Quality Grades:** A, B, C display correctly with colors
- [ ] **Crop Types:** Emojis show correctly (🧅 🌾 🥕)
- [ ] **Tamil/Sinhala:** Language switching works
- [ ] **Maps:** Listings display on map (Jaffna region)
- [ ] **Dark Mode:** Toggle works (if implemented)

### Network & Security
- [ ] **HTTPS Enforcement:** Cannot connect to HTTP endpoints
- [ ] **API Calls:** All API calls succeed (production URL)
- [ ] **Error Handling:** Graceful handling of network errors
- [ ] **Offline Mode:** Local operations queue works
- [ ] **Sync:** Background sync completes successfully

### Edge Cases
- [ ] **No crashes** on app startup
- [ ] **No crashes** on network timeout
- [ ] **No crashes** on invalid data
- [ ] **Firebase Crashlytics:** Stack traces are readable (if crash occurs)
- [ ] **Memory:** No memory leaks during extended use

---

## Play Store Submission

### 1. Build Final APK
```bash
# Clean build
./gradlew clean assembleRelease

# Verify
./scripts/verify_release_build.sh
```

### 2. Verify Signature
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Extract certificate fingerprints (for Play Console)
keytool -list -v -keystore agrimarket-release.keystore -alias agrimarket
# Copy SHA-1 and SHA-256 fingerprints
```

### 3. Prepare Store Listing

**Content from `store-listing.md`:**
- App title: "Agrimarket - Sri Lanka Farmers Marketplace"
- Short description (80 chars max)
- Full description (4000 chars max)
- Screenshots (minimum 2)
- Feature graphic (1024x500)
- App icon (512x512)

**Additional Requirements:**
- Privacy Policy URL: Add to Play Console
- Content rating questionnaire
- Target audience: Adults
- App category: Shopping / Food & Drink

### 4. Upload to Play Console

1. Go to [Play Console](https://play.google.com/console)
2. Create new app or select existing
3. Upload APK to **Internal Testing** track first
4. Fill in store listing
5. Complete content rating
6. Set pricing & distribution
7. Submit for review

### 5. Internal Testing (Recommended)

Before public release:
1. Upload to Internal Testing track
2. Add test users (email addresses)
3. Share testing link with team
4. Test on various devices
5. Collect feedback
6. Fix critical issues
7. Promote to Production

---

## Post-Release Monitoring

### 1. Firebase Crashlytics
- Monitor crash-free rate (target: >99%)
- Review top crashes
- Fix critical crashes in next update

### 2. Play Console Metrics
- Install/uninstall rate
- User ratings/reviews
- ANR (App Not Responding) rate
- Crash rate

### 3. API Monitoring
- Monitor Vercel backend logs
- Check Supabase database performance
- Review API error rates

---

## Version Increments

For next release, update `app/build.gradle.kts`:
```kotlin
versionCode = 2  // Increment by 1 for each release
versionName = "1.1"  // Follow semantic versioning
```

**Never decrement versionCode** - Play Store rejects it.

---

## Troubleshooting

### "Release build will be UNSIGNED"
**Cause:** Keystore not configured
**Solution:** Configure signing in `local.properties` (see Phase 2)

### "Cleartext traffic not permitted"
**Cause:** App trying to connect to HTTP in release build
**Solution:** Ensure production BASE_URL uses HTTPS

### ProGuard/R8 crashes at runtime
**Cause:** Missing ProGuard rules
**Solution:** Run `./gradlew testDebugUnitTest --tests ProguardTest` to identify issues

### APK size too large (>150MB)
**Cause:** Too many resources or dependencies
**Solution:**
- Enable resource shrinking (already enabled)
- Remove unused dependencies
- Use WebP images instead of PNG
- Enable App Bundle (`.aab` instead of `.apk`)

### Location permissions warning in Play Console
**Cause:** Maps SDK included
**Solution:** Already addressed - location is disabled in code (`isMyLocationEnabled = false`)

---

## Important Files

**Build Configuration:**
- `app/build.gradle.kts` - Build config, signing, ProGuard
- `app/proguard-rules.pro` - ProGuard rules
- `local.properties` - Signing credentials (NOT in git)

**Network Security:**
- `app/src/debug/res/xml/network_security_config.xml` - Debug config
- `app/src/release/res/xml/network_security_config.xml` - Release config

**Documentation:**
- `docs/RELEASE_SIGNING_SETUP.md` - Signing guide
- `PRODUCTION_READINESS_ASSESSMENT.md` - MVP checklist
- `docs/PRE_LAUNCH_CHECKLIST.md` - Testing checklist

**Scripts:**
- `scripts/verify_release_build.sh` - Bash verification
- `scripts/verify_release_build.ps1` - PowerShell verification

**Tests:**
- `app/src/test/java/com/senthapps/slagrimarket/ProguardTest.kt` - ProGuard tests

---

## Security Best Practices

### DO ✅
- ✅ Always use HTTPS for production API
- ✅ Store keystore in secure location (NOT in git)
- ✅ Use Play App Signing (Google manages final key)
- ✅ Enable ProGuard/R8 obfuscation
- ✅ Review permissions before each release
- ✅ Test release builds on physical devices
- ✅ Monitor crash reports

### DON'T ❌
- ❌ Never commit keystore to git (even private repos)
- ❌ Never hardcode API keys in source code
- ❌ Never use `android:debuggable="true"` in release
- ❌ Never use `android:usesCleartextTraffic="true"` in release
- ❌ Never skip ProGuard testing
- ❌ Never force push to main/master with `--force`
- ❌ Never lose your keystore file

---

## Emergency Contacts

**If keystore is lost:**
1. **Cannot update existing app** - Play Store rejects different signatures
2. Must publish as new app with new package name
3. Lose all ratings, reviews, install base
4. **Prevention:** Back up keystore in 3 secure locations NOW

**If critical crash post-release:**
1. Monitor Firebase Crashlytics for stack traces
2. Fix in development branch
3. Test thoroughly
4. Increment versionCode
5. Build and upload hotfix APK
6. Submit for expedited review (if available)

---

## Support Resources

- [Android Developer Docs - App Signing](https://developer.android.com/studio/publish/app-signing)
- [Play Console - Release Guide](https://support.google.com/googleplay/android-developer/answer/9859152)
- [ProGuard Manual](https://www.guardsquare.com/manual/configuration/usage)
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)

---

**Last Updated:** 2026-02-16
**Prepared By:** Claude Sonnet 4.5
**Status:** ✅ Ready for Play Store Release (configure signing first)
