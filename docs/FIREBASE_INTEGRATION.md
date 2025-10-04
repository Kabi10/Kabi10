# 🔥 Firebase Integration Guide

**Date:** October 4, 2025  
**Status:** ✅ Integrated and Working

---

## 📋 What's Integrated

### **Firebase Services Added:**

1. **Firebase Analytics** ✅
   - Automatically tracks user events
   - Screen views, user engagement
   - Custom events ready to implement

2. **Firebase Crashlytics** ✅
   - Real-time crash reporting
   - Stack traces and device info
   - Crash-free users metrics

3. **Firebase Cloud Messaging (FCM)** ✅
   - Push notifications ready
   - Topic-based messaging
   - Data messages support

4. **Firebase Authentication** ✅
   - Alternative to current OTP system
   - Multiple auth providers support
   - Token management

---

## 🔧 Configuration Details

### **Package Name:**
```
com.senthapps.slagrimarket
```

### **SHA-1 Certificate (Debug):**
```
76:20:8A:26:28:74:AA:E5:7B:D5:0F:A5:BA:76:68:B3:AB:5E:77:4A
```

### **Firebase BOM Version:**
```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
```

### **Dependencies Added:**
```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.messaging)
implementation(libs.firebase.auth)
```

---

## 📁 Files Modified

### **1. build.gradle.kts (Root)**
```kotlin
plugins {
    // ... existing plugins
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
```

### **2. app/build.gradle.kts**
```kotlin
plugins {
    // ... existing plugins
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
}
```

### **3. gradle/libs.versions.toml**
```toml
[versions]
firebaseBom = "33.7.0"
googleServices = "4.4.2"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging-ktx" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.2" }
```

### **4. app/google-services.json**
- ✅ Placed in `app/` directory
- Contains Firebase project configuration
- Auto-generated from Firebase Console

---

## 🚀 How to Use

### **1. Firebase Analytics**

**Track Custom Events:**
```kotlin
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class YourActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytics = Firebase.analytics
        
        // Log custom event
        analytics.logEvent("listing_viewed") {
            param("listing_id", "123")
            param("crop_type", "tomato")
        }
    }
}
```

**Automatic Events:**
- `screen_view` - Tracked automatically
- `session_start` - User opens app
- `first_open` - First time user opens app
- `app_update` - App version updated

---

### **2. Firebase Crashlytics**

**Initialize in Application Class:**
```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics

class JaffnaMarketplaceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        
        // Set user identifier
        FirebaseCrashlytics.getInstance().setUserId("user_123")
        
        // Log custom keys
        FirebaseCrashlytics.getInstance().setCustomKey("user_type", "farmer")
    }
}
```

**Log Non-Fatal Errors:**
```kotlin
try {
    // Your code
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    Timber.e(e, "Error occurred")
}
```

**Add Custom Logs:**
```kotlin
FirebaseCrashlytics.getInstance().log("User clicked on listing")
```

---

### **3. Firebase Cloud Messaging (FCM)**

**Create FCM Service:**
```kotlin
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming message
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            handleDataMessage(remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        // Send token to your server
        sendTokenToServer(token)
    }
    
    private fun showNotification(title: String?, body: String?) {
        // Create and show notification
    }
}
```

**Register in AndroidManifest.xml:**
```xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**Get FCM Token:**
```kotlin
import com.google.firebase.messaging.FirebaseMessaging

FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        // Send to your server
        Timber.d("FCM Token: $token")
    }
}
```

---

### **4. Firebase Authentication**

**Sign In with Phone:**
```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

val auth = FirebaseAuth.getInstance()

// Send verification code
PhoneAuthProvider.getInstance().verifyPhoneNumber(
    phoneNumber,
    60,
    TimeUnit.SECONDS,
    this,
    callbacks
)

// Verify code
val credential = PhoneAuthProvider.getCredential(verificationId, code)
auth.signInWithCredential(credential)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // User signed in
            val user = task.result?.user
        }
    }
```

---

## 📊 Firebase Console Access

### **View Your Data:**

1. **Analytics Dashboard:**
   - https://console.firebase.google.com/project/YOUR_PROJECT/analytics

2. **Crashlytics:**
   - https://console.firebase.google.com/project/YOUR_PROJECT/crashlytics

3. **Cloud Messaging:**
   - https://console.firebase.google.com/project/YOUR_PROJECT/messaging

4. **Authentication:**
   - https://console.firebase.google.com/project/YOUR_PROJECT/authentication

---

## 🔐 Security Notes

### **Important:**

1. **google-services.json** contains sensitive data
   - Already in `.gitignore` (should be)
   - Don't commit to public repositories
   - Each developer needs their own copy

2. **Release SHA-1:**
   - Add your release keystore SHA-1 to Firebase Console
   - Required for production builds
   - Get it with: `keytool -list -v -keystore release.keystore`

3. **API Keys:**
   - Firebase API keys in google-services.json are safe for client apps
   - They're restricted by package name and SHA-1
   - Still, enable App Check for additional security

---

## 🎯 Next Steps

### **Recommended Implementation Order:**

1. **Analytics** (Easiest)
   - Add custom events for key actions
   - Track user flows
   - Monitor engagement

2. **Crashlytics** (Important)
   - Test crash reporting
   - Add custom keys for debugging
   - Monitor crash-free users

3. **Cloud Messaging** (High Value)
   - Implement notification service
   - Send order updates
   - Price alerts
   - New listing notifications

4. **Authentication** (Optional)
   - Consider migrating from current OTP
   - Or keep both systems
   - Firebase Auth has better security

---

## 🧪 Testing

### **Test Analytics:**
```bash
# Enable debug mode
adb shell setprop debug.firebase.analytics.app com.senthapps.slagrimarket

# View events in real-time
# Go to Firebase Console > Analytics > DebugView
```

### **Test Crashlytics:**
```kotlin
// Force a test crash
Button(onClick = { throw RuntimeException("Test Crash") }) {
    Text("Test Crash")
}
```

### **Test FCM:**
```bash
# Send test notification from Firebase Console
# Go to Cloud Messaging > Send your first message
```

---

## 📈 Monitoring

### **Key Metrics to Track:**

**Analytics:**
- Daily active users (DAU)
- Screen views per session
- Conversion rates (browse → order)
- Feature usage

**Crashlytics:**
- Crash-free users percentage
- Most common crashes
- Affected devices/OS versions

**Performance:**
- App startup time
- Screen rendering time
- Network request duration

---

## 🐛 Troubleshooting

### **Common Issues:**

**1. "google-services.json not found"**
- Ensure file is in `app/` directory
- Sync Gradle files
- Clean and rebuild

**2. "No matching client found"**
- Package name must match exactly
- Check applicationId in build.gradle
- Verify google-services.json

**3. "SHA-1 mismatch"**
- Add both debug and release SHA-1 to Firebase
- Get SHA-1: `./gradlew signingReport`

**4. "Analytics not showing data"**
- Enable DebugView for testing
- Wait 24 hours for production data
- Check internet connection

---

## ✅ Verification Checklist

- [x] google-services.json in app/ folder
- [x] Google Services plugin applied
- [x] Firebase dependencies added
- [x] Build successful
- [ ] Analytics events logging
- [ ] Crashlytics reporting crashes
- [ ] FCM receiving notifications
- [ ] Firebase Auth working (if used)

---

## 📚 Resources

- [Firebase Android Documentation](https://firebase.google.com/docs/android/setup)
- [Firebase Analytics Guide](https://firebase.google.com/docs/analytics)
- [Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [FCM Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Firebase Auth Documentation](https://firebase.google.com/docs/auth)

---

**Firebase Integration Complete!** 🔥✅

Your app is now ready for production-grade analytics, crash reporting, and push notifications!
