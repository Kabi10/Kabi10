# Snapassist Extended System - Test & Deployment Guide

## 🧪 Testing Checklist

### Phase 1: Firebase Setup
1. **Update Configuration**
   - Set `AUTHORIZED_EMAIL` in `functions/src/index.ts`
   - Add Firebase config to `public/index.html`

2. **Deploy Services**
   ```bash
   npx firebase deploy --only functions,hosting
   ```

3. **Test Dashboard**
   - Open hosted URL
   - Sign in with Google (authorized email)
   - Verify device list loads

### Phase 2: Android Testing

#### Core Permissions
- ✅ Camera permission
- ✅ POST_NOTIFICATIONS (Android 13+)  
- ✅ Usage Access (Settings > Apps > Special app access)

#### Service Testing
1. Arm camera service
2. Verify FCM token appears in Firestore
3. Check device shows as "Active" in dashboard

### Phase 3: Command Testing

#### SNAP Command
- Dashboard: Click "📸 SNAP"
- Android: Photo captured and uploaded to Storage

#### LAUNCH Command  
- Dashboard: Enter package (e.g., `com.whatsapp`), click "🚀 LAUNCH"
- Foreground app: Launches immediately
- Background app: Shows notification → tap to launch

#### LIST_APPS Command
- Dashboard: Click "📋 LIST APPS"
- Report appears with installed apps and current foreground app

### Phase 4: Edge Cases
- Test missing Usage Access permission
- Test unknown package names
- Test background launch restrictions
- Test service persistence after force-stop
- Test auto re-arm after device reboot

## 🚀 Deployment Steps

1. **Firebase Project Configuration**
   - Enable Auth, Firestore, Functions, Storage, Hosting
   - Configure Google Sign-in provider

2. **Security Rules Update**
   ```bash
   npx firebase deploy --only firestore:rules,storage:rules
   ```

3. **Production Deployment**
   ```bash
   npx firebase deploy
   ```

4. **Android App**
   - Build signed APK/AAB
   - Upload to Play Store or distribute via other channels

## 📋 Production Checklist

- [ ] Replace `snapassist-demo` with actual project ID
- [ ] Update authorized email in functions
- [ ] Remove `QUERY_ALL_PACKAGES` for Play Store builds
- [ ] Add specific package queries for production
- [ ] Test on multiple Android versions (API 24-34)
- [ ] Verify all permissions work correctly
- [ ] Test FCM delivery reliability
- [ ] Monitor Firebase usage and costs

## 🔧 Troubleshooting

**Dashboard not loading devices:**
- Check Firestore security rules
- Verify authenticated user email matches functions auth

**FCM commands not working:**
- Check device token in Firestore
- Verify app has notification permissions
- Check battery optimization settings

**App launch failing:**
- Verify package visibility in manifest
- Check Usage Access permission granted
- Test with foreground/background app states

**Service not persisting:**
- Check battery optimization whitelist
- Verify AUTO_START_MANAGEMENT on some devices
- Test START_STICKY behavior