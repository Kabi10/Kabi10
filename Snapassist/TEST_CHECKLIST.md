# Snapassist Test Verification Checklist

## Device & OS Test Matrix (Must Pass)

### ✅ Android 12 (API 31)
- [ ] **Foreground Service Launch Limits**: 
  - [ ] Service starts only when app is in foreground
  - [ ] ForegroundServiceStartNotAllowedException properly handled
  - [ ] Fallback notification shown when background start attempted
  - [ ] User can tap notification to re-arm from foreground

### ✅ Android 13 (API 33)
- [ ] **POST_NOTIFICATIONS Permission**:
  - [ ] Runtime permission request shown before starting service
  - [ ] Persistent \"Armed\" notification appears after permission granted
  - [ ] Graceful fallback if permission denied
  - [ ] Re-permission request available in UI

### ✅ Android 14 (API 34)
- [ ] **Foreground Service Type**:
  - [ ] `android:foregroundServiceType=\"camera\"` declared in manifest
  - [ ] Service starts in foreground immediately when armed
  - [ ] No background service start attempts
  - [ ] Proper notification channel configuration

## Core Manual Test Scripts

### A) Happy Path Test (SNAP → Upload → URL)

**Prerequisites:**
- [ ] Firebase project configured with Authentication, Storage, and FCM
- [ ] google-services.json in app/ directory
- [ ] Device with camera permission granted
- [ ] Network connectivity available

**Test Steps:**
1. [ ] **Arm Service**: Tap \"Arm Camera\" in app
   - [ ] Persistent notification appears with \"Snapassist Armed\"
   - [ ] Service status shows \"Armed\" with green indicator
   - [ ] Notification has \"Disarm\" action button

2. [ ] **Send FCM Message**: From Firebase Console → Cloud Messaging
   ```json
   {
     \"data\": {
       \"cmd\": \"SNAP\"
     },
     \"android\": {
       \"priority\": \"high\"
     }
   }
   ```
   - [ ] Message received within 5 seconds
   - [ ] Processing completes within 18 seconds
   - [ ] No unhandled exceptions in logs

3. [ ] **Photo Capture**: CameraX ImageCapture
   - [ ] Back camera used automatically
   - [ ] Photo saved to temporary file
   - [ ] File URI returned successfully
   - [ ] No camera permission errors

4. [ ] **Firebase Upload**: Storage putFile()
   - [ ] Upload to `shots/{uid}/{timestamp}_deviceinfo.jpg`
   - [ ] Custom metadata included (device model, Android version)
   - [ ] Download URL obtained
   - [ ] Network retry on failure

5. [ ] **Verification**: Firebase Console → Storage → Files
   - [ ] Image appears in correct user folder
   - [ ] Download URL opens image for authenticated user
   - [ ] Unauthorized users cannot access

**Success Criteria:**
- [ ] Photo appears within 30 seconds of SNAP command
- [ ] Download URL works for authenticated user
- [ ] Logs show no unhandled exceptions
- [ ] Process completes under 20-second FCM limit

### B) Background Behavior Test (Compliance)

**Test Steps:**
1. [ ] **Disarm Service**: Stop camera service in app
2. [ ] **Background App**: Put app in background (home screen)
3. [ ] **Send SNAP Command**: Same FCM message as above
4. [ ] **Verify Behavior**:
   - [ ] No ForegroundServiceStartNotAllowedException thrown
   - [ ] High-priority notification shown instead
   - [ ] Notification text: \"Remote capture failed. Tap to re-arm.\"
   - [ ] Tapping notification brings app to foreground
   - [ ] Can successfully arm service after returning to foreground

**Success Criteria:**
- [ ] No background FGS start attempted
- [ ] User guided to re-arm through notification
- [ ] App behavior compliant with API 31+ restrictions

### C) Notifications Permission Test (Android 13+)

**Prerequisites:**
- [ ] Fresh app install on API 33+ device
- [ ] No previous permissions granted

**Test Steps:**
1. [ ] **Open App**: Launch for first time
2. [ ] **Camera Permission**: Grant when requested
3. [ ] **Notification Permission**: Should be requested before arming
4. [ ] **Grant Permission**: Tap \"Allow\" on system dialog
5. [ ] **Arm Service**: Should work normally after permission granted

**Denial Test:**
1. [ ] **Deny Permission**: Tap \"Don't allow\" on system dialog
2. [ ] **Check Fallback**: App should show explanation
3. [ ] **Re-request Available**: Button to request permission again
4. [ ] **Manual Grant**: User can grant in system settings

**Success Criteria:**
- [ ] Permission requested at appropriate time
- [ ] Clear explanation of why permission needed
- [ ] Graceful fallback when denied
- [ ] Re-request mechanism available

### D) Offline/Network Test

**Test Steps:**
1. [ ] **Arm Service**: Start camera service with network on
2. [ ] **Disable Network**: Turn off WiFi and cellular data
3. [ ] **Send SNAP**: FCM message (will be queued)
4. [ ] **Verify Capture**: Photo should still be captured locally
5. [ ] **Enable Network**: Turn connectivity back on
6. [ ] **Verify Upload**: Photo should upload with retry mechanism

**Retry Behavior:**
- [ ] Initial upload failure logged
- [ ] Exponential backoff retry (1s, 2s, 4s intervals)
- [ ] Maximum 3 retry attempts
- [ ] Success after network restoration

**Success Criteria:**
- [ ] Local capture works offline
- [ ] Upload retry mechanism functions
- [ ] No message processing timeout exceeded
- [ ] Final upload success within 2 minutes of network restoration

### E) Emulator vs Real Device

**Real Device Testing:**
- [ ] FCM messages received and processed
- [ ] Camera capture works with actual hardware
- [ ] Upload to Firebase Storage successful
- [ ] All permissions work correctly

**Emulator Testing (Play-enabled):**
- [ ] Firebase Storage rules testing
- [ ] Authentication flows
- [ ] UI functionality
- [ ] Permission request dialogs

**Note**: FCM testing requires real device or Play-enabled emulator

## Security & Rules Validation

### Positive Tests (Authenticated User)
- [ ] **Login**: Authenticate with Firebase Auth
- [ ] **Upload Permission**: Can upload to `shots/{own-uid}/`
- [ ] **Read Permission**: Can access own uploaded files
- [ ] **Download URLs**: Work for authenticated user

### Negative Tests (Unauthorized Access)
- [ ] **Different User**: Cannot access other users' shots
- [ ] **Unauthenticated**: Cannot read/write without auth
- [ ] **Path Traversal**: Cannot access outside own folder
- [ ] **Direct Access**: Storage URLs require authentication

### Storage Rules Testing
```javascript
// Test with Firebase Local Emulator Suite
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /shots/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

**Test Commands:**
```bash
# Install emulator
npm install -g firebase-tools

# Start storage emulator
firebase emulators:start --only storage

# Test rules
firebase emulators:exec --only storage \"npm test\"
```

## QA Sign-off Checklist

### ✅ Core Requirements
- [ ] **Foreground Service Type**: `camera` declared and enforced
- [ ] **Service Lifecycle**: Starts only when armed, stops cleanly
- [ ] **FCM Data Messages**: Handled within 18-second limit
- [ ] **CameraX Integration**: ImageCapture used per documentation
- [ ] **Firebase Upload**: putFile() with download URL retrieval
- [ ] **Authentication**: Required for all storage operations

### ✅ API Compliance
- [ ] **Android 13+**: POST_NOTIFICATIONS flow implemented
- [ ] **Android 12+**: Background FGS start prevention
- [ ] **Error Handling**: Proper exception catching and user feedback
- [ ] **Permission Flow**: Runtime requests with clear rationale

### ✅ Network & Reliability
- [ ] **Retry Mechanism**: Exponential backoff for uploads
- [ ] **Idempotency**: Duplicate upload prevention
- [ ] **Offline Handling**: Local capture with delayed upload
- [ ] **Timeout Compliance**: FCM processing under 20 seconds

### ✅ Security
- [ ] **Storage Rules**: User-specific folder access only
- [ ] **Service Export**: Both services marked as not exported
- [ ] **Authentication**: Required for Firebase operations
- [ ] **Permission Minimal**: Only necessary permissions requested

## Nice-to-Have Hardening (Fast Wins)

### ✅ Enhanced Features
- [ ] **Structured Metadata**: Device info, timestamp in uploads
- [ ] **Debug Utilities**: FCM token display, simulate SNAP button
- [ ] **Network Monitoring**: Real-time connectivity status
- [ ] **Upload Progress**: Progress tracking for large files
- [ ] **Error Reporting**: Detailed logging for troubleshooting

### ✅ Testing Tools
- [ ] **Debug Screen**: Copy FCM token, trigger local SNAP
- [ ] **Test Report**: Automated validation results
- [ ] **Log Analysis**: Structured logging for debugging
- [ ] **Performance Metrics**: Upload time, processing duration

## Test Environment Setup

### Firebase Configuration
1. [ ] **Project Setup**: Firebase project with required services
2. [ ] **Authentication**: Email/password provider enabled
3. [ ] **Storage**: Rules deployed from `storage.rules`
4. [ ] **FCM**: Cloud messaging enabled
5. [ ] **google-services.json**: Placed in `app/` directory

### Device Requirements
- [ ] **Minimum API**: Android 7.0 (API 24)
- [ ] **Camera**: Rear-facing camera available
- [ ] **Network**: WiFi or cellular data
- [ ] **Storage**: Minimum 100MB free space
- [ ] **RAM**: Minimum 2GB for stable operation

### Build Requirements
- [ ] **Android Studio**: Arctic Fox or newer
- [ ] **Gradle**: 8.0+
- [ ] **Target SDK**: API 34
- [ ] **Dependencies**: All Firebase SDKs up to date

## Automated Test Integration

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Firebase Emulator Tests
```bash
firebase emulators:exec --only storage \"./gradlew test\"
```

### Performance Tests
```bash
# Memory usage
adb shell dumpsys meminfo com.senthapps.snapassist

# Battery usage
adb shell dumpsys batterystats | grep com.senthapps.snapassist

# Network usage
adb shell dumpsys netstats | grep com.senthapps.snapassist
```

## Sign-off Criteria

### Must Pass (Blocking Issues)
- [ ] All API level compatibility tests pass
- [ ] Happy path test completes successfully
- [ ] Background behavior complies with platform restrictions
- [ ] Security rules prevent unauthorized access
- [ ] No unhandled exceptions in core flows

### Should Pass (Non-blocking Issues)
- [ ] Network retry mechanism functions correctly
- [ ] Debug utilities work as expected
- [ ] Performance metrics within acceptable range
- [ ] Error messages clear and actionable

### Documentation Complete
- [ ] README updated with test instructions
- [ ] Code comments explain complex logic
- [ ] Security considerations documented
- [ ] Deployment guide accurate and complete

---

**Test Sign-off**: 
- Tester: _________________ Date: _________________
- Platform: Android _______ API Level: _____________
- Result: PASS / FAIL / CONDITIONAL PASS
- Notes: ________________________________________________