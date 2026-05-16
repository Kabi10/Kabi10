# Snapassist E2E Testing Guide & Quick Triage Checklist

## 🚀 Quick E2E Test (2-3 minutes)

### Prerequisites
- Android device with API 24+ 
- Google account whitelisted in Firebase Console
- `google-services.json` in `app/` directory
- FCM project deployed to Firebase

### 1. Device Preparation

**Access DiagnosticsActivity:**
```bash
# Via ADB (if using emulator/physical device)
adb shell am start -n com.senthapps.snapassist/.DiagnosticsActivity

# Or manually: Open app settings > Activities > DiagnosticsActivity
```

**Grant Essential Permissions:**
1. Tap **"Request notifications (API 33+)"** → Allow notifications
2. Tap **"Open Usage Access"** → Find Snapassist → Toggle ON
3. Tap **"Request ignore battery optimizations"** → Allow if prompted
4. Camera permission will be requested automatically when needed

**Arm the Service:**
1. Tap **"Arm (start service)"** 
2. ✅ Verify: You should see a persistent "Processing" notification
3. ✅ Verify: "Armed: true" appears in the DiagnosticsActivity

### 2. Dashboard Path (Network Required)

**Web Dashboard Test:**
1. Open the Firebase Hosting URL (from `firebase serve` or deployed URL)
2. Click **"Sign in with Google"** using whitelisted email
3. Select device **"primary"** from dropdown
4. Click **"SNAP"** button
5. ✅ Verify: Success message appears
6. ✅ Verify: Check Firebase Storage console for uploaded image
7. Click **"LIST_APPS"** button  
8. ✅ Verify: App report appears in Firestore and dashboard display
9. Enter package name (e.g., `com.whatsapp`) and click **"LAUNCH"**
10. ✅ Verify: If app is backgrounded, you get notification; tap to open

### 3. Local Path (No Network Required)

**Offline Simulation Test:**
1. In DiagnosticsActivity, tap **"Simulate SNAP (local)"**
2. ✅ Verify: "Requested local SNAP → snap-[timestamp].jpg" appears
3. ✅ Verify: Check logcat for "Local SNAP captured" message
4. ✅ Verify: Image file created in `externalCacheDir`

---

## 🔧 Quick Triage Checklist

### No Photo After SNAP

**Check Command:**
```bash
adb logcat | grep -i "Snapassist\|CameraX\|FirebaseMessaging"
```

**Common Issues:**
- [ ] **Service not armed**: Android 12+ blocks FGS from background → You should see fallback notification. Arm first, then SNAP.
- [ ] **Camera permission denied**: Grant camera permission in device settings
- [ ] **Background restriction**: FCM message arrived but service couldn't start → Check battery optimization settings
- [ ] **CameraX binding failed**: Check logcat for camera initialization errors

### LAUNCH Doesn't Open App

**Diagnosis Steps:**
- [ ] **Package visibility**: Ensure `<queries>` includes target package in AndroidManifest.xml
- [ ] **Invalid package**: `getLaunchIntentForPackage()` returns null → Check package name
- [ ] **Background activity start blocked**: Android 10+ requires notification → Tap the high-priority notification
- [ ] **App not installed**: Package doesn't exist on device

### LIST_APPS Empty or Missing Data

**Common Causes:**
- [ ] **Usage Access not granted**: Open **Usage Access** settings and grant permission
- [ ] **No foreground app**: `UsageStatsManager` needs recent app activity
- [ ] **Permission restrictions**: Check PACKAGE_USAGE_STATS permission
- [ ] **Query scope**: Verify QUERY_ALL_PACKAGES or specific `<queries>` entries

### Dashboard Sign-in "Does Nothing"

**Verification Steps:**
- [ ] **Auth domain**: Check **Firebase Console → Auth → Sign-in method → Authorized domains**
- [ ] **Required domains**: Ensure `*.web.app`, `*.firebaseapp.com`, and `localhost` are listed
- [ ] **Pop-up blocked**: Browser blocked popup → Page falls back to redirect method
- [ ] **Google provider**: Verify Google sign-in is enabled in Firebase Auth
- [ ] **API keys**: Check Firebase config object in HTML has correct keys

### FCM Token Issues

**Debug Token Registration:**
```bash
# Check if token is being registered
adb logcat | grep -i "FCM.*token"

# Verify Firestore document
# Check Firebase Console → Firestore → devices → primary → token field
```

**Token Problems:**
- [ ] **Token not saved**: Check `onNewToken` and DiagnosticsActivity startup
- [ ] **Token rotation**: Old token invalidated → App should auto-update via `onNewToken`
- [ ] **Network issues**: Token update failed → Check connectivity
- [ ] **Firestore rules**: Verify write permissions for devices collection

### Service Lifecycle Issues

**Service State Check:**
```bash
# Check if foreground service is running
adb shell dumpsys activity services | grep -i camera

# Monitor service lifecycle
adb logcat | grep -i "CameraService\|LifecycleService"
```

**Common Service Problems:**
- [ ] **Service not sticky**: Process killed and not restarted → Check START_STICKY return
- [ ] **Notification removed**: User swiped away foreground notification → Service gets killed
- [ ] **Battery optimization**: Device aggressively manages background apps
- [ ] **Memory pressure**: System killed service due to low memory

---

## 🛠️ Advanced Debugging

### Logcat Filters

**Essential Debugging:**
```bash
# Comprehensive logging
adb logcat | grep -E "(Snapassist|CameraX|FirebaseMessaging|FCM)"

# Service-specific
adb logcat | grep -E "(CameraService|AppFcmService)"

# Permission-related
adb logcat | grep -E "(Usage.*Access|Permission|Battery.*optimization)"
```

### Firebase Console Verification

**Check These Locations:**
1. **Storage → Files**: Uploaded images in `shots/{uid}/` folders
2. **Firestore → devices → primary**: FCM token and metadata
3. **Firestore → devices → primary → reports**: App inventory data
4. **Functions → Logs**: Cloud Function execution logs
5. **Authentication → Users**: Signed-in users list

### Network Connectivity

**Test FCM Delivery:**
```bash
# Send test message via Firebase Console
# Cloud Messaging → Send test message → Enter device token
# Data payload: {"cmd": "SNAP"}
```

**Verify Upload Path:**
```bash
# Check network connectivity
adb logcat | grep -i "network\|connectivity"

# Monitor Firebase Storage uploads
adb logcat | grep -i "firebase.*storage\|upload"
```

---

## ✅ Success Criteria

**Minimal Working System:**
- [ ] DiagnosticsActivity shows FCM token
- [ ] Service arms/disarms via buttons
- [ ] Local SNAP creates image file
- [ ] Remote SNAP (via dashboard) uploads to Storage
- [ ] LIST_APPS populates Firestore report
- [ ] LAUNCH opens app or shows notification

**Production Ready:**
- [ ] All permissions granted smoothly
- [ ] Service survives app backgrounding
- [ ] FCM messages processed within 20s timeout
- [ ] Network failures retry automatically
- [ ] Battery optimization configured
- [ ] Usage Access granted for app reporting

---

## 🎯 Next Steps

Once SNAP works end-to-end from dashboard → device → Firebase Storage, the core Snapassist system is complete and ready for extended features or integration with other services like **Moodlens**.

**Performance Monitoring:**
- Monitor FCM delivery rates
- Track upload success/failure rates  
- Measure service uptime and restart frequency
- Analyze battery usage impact