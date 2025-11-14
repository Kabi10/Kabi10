# Quick Start Guide - Agrimarket Android App

Get the Agrimarket app running on your development machine in **5 simple steps**. This guide will have you building and testing the app in under 30 minutes.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Tools
- ✅ **Android Studio** Hedgehog (2023.1.1) or later
  - Download: https://developer.android.com/studio
- ✅ **JDK 11+** (bundled with Android Studio)
- ✅ **Git** for version control
  - Download: https://git-scm.com/downloads
- ✅ **Android device or emulator** (API 24+ / Android 7.0+)

### Optional (for backend development)
- 🔧 **Node.js 18+** and npm 9+
- 🔧 **Supabase account** (free tier)
- 🔧 **Vercel CLI**

---

## 🚀 5-Step Setup Process

### **Step 1: Clone the Repository**

Open your terminal and run:

```bash
# Clone the repository
git clone https://github.com/Kabi10/Srilanka-Farmers-Marketplace.git

# Navigate to the project directory
cd Srilanka-Farmers-Marketplace
```

**✅ Success Check:** You should see the project files in your directory.

---

### **Step 2: Configure local.properties**

The `local.properties` file tells Android Studio where your Android SDK is located.

**Option A: Automatic (Recommended)**

1. Open the project in Android Studio
2. Android Studio will automatically create `local.properties` with the correct SDK path
3. Skip to Step 3

**Option B: Manual**

Create a file named `local.properties` in the project root directory:

```bash
# For Windows
echo sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk > local.properties

# For Mac/Linux
echo "sdk.dir=/Users/YourUsername/Library/Android/sdk" > local.properties
```

**Important Notes:**
- **Windows users:** Use double backslashes (`\\`) in the path
- Replace `YourUsername` with your actual username
- The SDK path should point to your Android SDK installation directory

**Common SDK Locations:**
- **Windows:** `C:\Users\YourUsername\AppData\Local\Android\Sdk`
- **Mac:** `/Users/YourUsername/Library/Android/sdk`
- **Linux:** `/home/YourUsername/Android/Sdk`

**✅ Success Check:** The file `local.properties` exists in your project root.

---

### **Step 3: Set Up Firebase Configuration**

The app uses Firebase for analytics and crash reporting. You have two options:

**Option A: Use Demo Configuration (Quick Start)**

For development and testing, you can use a placeholder Firebase configuration:

1. Copy the template file:
   ```bash
   cp app/google-services.json.template app/google-services.json
   ```

2. The app will work with limited Firebase functionality (analytics disabled)

**Option B: Use Your Own Firebase Project (Recommended for Production)**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app to your Firebase project
   - **Package name:** `com.senthapps.slagrimarket`
4. Download the `google-services.json` file
5. Place it in the `app/` directory (NOT `app/src/`)

**✅ Success Check:** The file `app/google-services.json` exists.

---

### **Step 4: Build the Debug APK**

Now build the app using Gradle:

**Using Terminal:**
```bash
# For Windows
.\gradlew assembleDebug

# For Mac/Linux
./gradlew assembleDebug
```

**Using Android Studio:**
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**

**Build Time:** First build may take 5-10 minutes as Gradle downloads dependencies.

**✅ Success Check:** You should see:
```
BUILD SUCCESSFUL in Xs
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

---

### **Step 5: Install and Run the App**

**Option A: Using Android Studio (Easiest)**

1. Connect your Android device via USB (with USB debugging enabled) OR start an emulator
2. Click the **Run** button (green play icon) in Android Studio
3. Select your device/emulator from the list
4. Wait for the app to install and launch

**Option B: Using ADB (Command Line)**

```bash
# Connect your device and verify it's detected
adb devices

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.senthapps.slagrimarket/.MainActivity
```

**✅ Success Check:** The app launches and you see the home screen with sample listings.

---

## ✅ Verification Steps

After the app launches, verify everything is working correctly:

### 1. **Demo User Auto-Login**
- ✅ You should be automatically logged in as a demo user
- ✅ No OTP verification required for development

### 2. **Language Switching**
- ✅ Tap the language toggle button in the top bar
- ✅ Switch between English (E), Tamil (த), and Sinhala (සි)
- ✅ All UI text should change to the selected language

### 3. **Navigation**
- ✅ Test all bottom navigation tabs:
  - 🏠 **Home** - View sample crop listings
  - 🔍 **Search** - Search and filter crops
  - ➕ **Create** - Create new listing (demo mode)
  - 📦 **Orders** - View transactions
  - 👤 **Profile** - View user profile

### 4. **Sample Data**
- ✅ Home screen shows sample crop listings (tomatoes, rice, onions, etc.)
- ✅ Listings display crop images, prices, and farmer information
- ✅ You can view listing details by tapping on a listing card

### 5. **Offline Mode**
- ✅ Enable airplane mode on your device
- ✅ The app should continue to work with cached data
- ✅ You can browse listings, create new ones (saved locally)

### 6. **Firebase Integration** (if using real Firebase config)
- ✅ Check Firebase Console for app connection
- ✅ Analytics events should appear (may take a few minutes)

---

## 🐛 Troubleshooting

### **Issue: "SDK location not found"**

**Cause:** `local.properties` file is missing or has incorrect SDK path.

**Solution:**
1. Verify `local.properties` exists in project root
2. Check the SDK path is correct for your system
3. Use double backslashes on Windows: `C\:\\Users\\...`
4. Restart Android Studio after creating the file

---

### **Issue: "google-services.json is missing"**

**Cause:** Firebase configuration file not found.

**Solution:**
1. Copy the template: `cp app/google-services.json.template app/google-services.json`
2. OR download from Firebase Console and place in `app/` directory
3. Ensure the file is named exactly `google-services.json` (not `.json.template`)
4. Sync Gradle: **File** → **Sync Project with Gradle Files**

---

### **Issue: Build fails with "Execution failed for task ':app:processDebugGoogleServices'"**

**Cause:** Invalid or missing `google-services.json` file.

**Solution:**
1. Verify `google-services.json` is in the `app/` directory (not `app/src/`)
2. Ensure the package name in the file matches: `com.senthapps.slagrimarket`
3. If using template, verify it's valid JSON
4. Clean and rebuild: `.\gradlew clean assembleDebug`

---

### **Issue: "Cannot connect to backend" or API errors**

**Cause:** The app is trying to connect to a backend server that's not running.

**Solution:**
- **For development:** The app works fully offline! Backend is optional.
- **If you need backend:** See [SETUP.md](SETUP.md) for backend setup instructions
- **Quick fix:** The app will automatically use cached data and work offline

---

### **Issue: Gradle sync fails or dependencies won't download**

**Cause:** Network issues or Gradle cache corruption.

**Solution:**
1. Check your internet connection
2. Invalidate caches: **File** → **Invalidate Caches / Restart**
3. Clear Gradle cache:
   ```bash
   # Windows
   rmdir /s /q %USERPROFILE%\.gradle\caches
   
   # Mac/Linux
   rm -rf ~/.gradle/caches
   ```
4. Sync again: **File** → **Sync Project with Gradle Files**

---

### **Issue: App crashes on launch**

**Cause:** Various possible causes.

**Solution:**
1. Check Android Studio Logcat for error messages
2. Verify minimum Android version (API 24 / Android 7.0)
3. Clean and rebuild:
   ```bash
   .\gradlew clean assembleDebug
   ```
4. Uninstall the app from device and reinstall
5. Check [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for known bugs

---

### **Issue: Emulator is slow or unresponsive**

**Cause:** Insufficient system resources or emulator configuration.

**Solution:**
1. Use a physical device instead (recommended for testing)
2. Enable hardware acceleration (Intel HAXM or AMD Hypervisor)
3. Reduce emulator RAM allocation
4. Use a lower API level emulator (API 24-28 is faster)
5. Close other applications to free up resources

---

## 🎯 Next Steps

Congratulations! You now have a working Agrimarket development environment. Here's what to do next:

### **1. Understand the Architecture**
Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand:
- MVVM architecture pattern
- Offline-first design
- Repository pattern
- Dependency injection with Hilt

### **2. Learn the Development Workflow**
Read [CONTRIBUTING.md](CONTRIBUTING.md) to learn:
- Git workflow and branching strategy
- Code review process
- Testing requirements
- Pull request guidelines

### **3. Check Current Limitations**
Read [KNOWN_ISSUES.md](KNOWN_ISSUES.md) to understand:
- Known bugs and workarounds
- Technical debt
- Platform-specific issues

### **4. Review the Roadmap**
Read [ROADMAP.md](ROADMAP.md) to see:
- Planned features
- Priority tasks
- Areas where you can contribute

### **5. Start Contributing**
- Check the [issue tracker](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues) for tasks
- Look for `good first issue` labels to get started
- Ask questions early - clarity is critical!

---

## 📚 Additional Resources

- **Android Basics:** [Android Developer Guides](https://developer.android.com/guide)
- **Jetpack Compose:** [Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- **Kotlin:** [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- **Material Design 3:** [Material Design Guidelines](https://m3.material.io/)
- **Git Workflow:** [GitHub Flow Guide](https://guides.github.com/introduction/flow/)

---

## 💬 Need Help?

- 📚 **Check Documentation First:** Most answers are in ARCHITECTURE.md or SETUP.md
- 🐛 **Report Issues:** Create detailed bug reports on GitHub
- 💬 **Ask Questions:** Comment on issues, don't wait until you're blocked
- ⏰ **Communicate Blockers:** If stuck for >2 hours, raise it immediately

---

**Welcome to the Agrimarket team! Let's build something that matters for the Jaffna farming community.** 🌾🚀

