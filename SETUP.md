# Agrimarket - Development Setup Guide

This guide will walk you through setting up the Agrimarket Android app development environment from scratch.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Android App Setup](#android-app-setup)
- [Firebase Configuration](#firebase-configuration)
- [Backend Setup (Optional)](#backend-setup-optional)
- [Database Setup (Optional)](#database-setup-optional)
- [Running the App](#running-the-app)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

1. **Android Studio** - Hedgehog (2023.1.1) or later
   - Download: https://developer.android.com/studio
   - Recommended: Latest stable version

2. **Java Development Kit (JDK) 11**
   - Android Studio includes JDK, but verify version
   - Check: `java -version` should show version 11 or later

3. **Git**
   - Download: https://git-scm.com/downloads
   - Verify: `git --version`

4. **Android SDK**
   - Installed automatically with Android Studio
   - Required API levels: 24 (minimum) to 36 (target)

### Optional Software (for backend development)

5. **Node.js 18+** (for backend)
   - Download: https://nodejs.org/
   - Verify: `node --version`

6. **Supabase CLI** (for database)
   - Install: `npm install -g supabase`
   - Verify: `supabase --version`

### Hardware Requirements

- **Minimum**: 8GB RAM, 10GB free disk space
- **Recommended**: 16GB RAM, 20GB free disk space, SSD
- **Android Device** or Emulator with API level 24+

## Android App Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/Agrimarket.git
cd Agrimarket
```

### Step 2: Configure Local Properties

1. Copy the template file:
   ```bash
   cp local.properties.template local.properties
   ```

2. Edit `local.properties` and set your Android SDK path:

   **Windows**:
   ```properties
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```

   **macOS**:
   ```properties
   sdk.dir=/Users/YourName/Library/Android/sdk
   ```

   **Linux**:
   ```properties
   sdk.dir=/home/YourName/Android/Sdk
   ```

   > **Note**: Use double backslashes (`\\`) on Windows!

### Step 3: Open Project in Android Studio

1. Launch Android Studio
2. Select **"Open an Existing Project"**
3. Navigate to the `Agrimarket` folder
4. Click **"OK"**
5. Wait for Gradle sync to complete (may take 5-10 minutes first time)

### Step 4: Verify Gradle Build

```bash
# On Windows
.\gradlew build

# On macOS/Linux
./gradlew build
```

If successful, you should see `BUILD SUCCESSFUL`.

## Firebase Configuration

Firebase is required for Analytics, Crashlytics, Storage, and Cloud Messaging.

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **"Add project"**
3. Enter project name (e.g., "Agrimarket Dev")
4. Disable Google Analytics (optional for development)
5. Click **"Create project"**

### Step 2: Add Android App to Firebase

1. In Firebase Console, click **"Add app"** → **Android**
2. Enter package name: `com.senthapps.slagrimarket`
3. Enter app nickname: "Agrimarket Android"
4. Leave SHA-1 blank for now (needed later for Auth)
5. Click **"Register app"**

### Step 3: Download google-services.json

1. Download the `google-services.json` file
2. Place it in the `app/` directory:
   ```
   Agrimarket/
   └── app/
       └── google-services.json  ← Place here
   ```

3. **IMPORTANT**: Never commit this file to Git! It's already in `.gitignore`.

### Step 4: Enable Firebase Services

In Firebase Console, enable these services:

#### 4.1 Firebase Analytics
- Already enabled by default
- No additional configuration needed

#### 4.2 Firebase Crashlytics
1. Go to **Crashlytics** in left menu
2. Click **"Enable Crashlytics"**
3. Follow setup wizard

#### 4.3 Firebase Storage
1. Go to **Storage** in left menu
2. Click **"Get started"**
3. Use default security rules for now:
   ```
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /{allPaths=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
4. Choose storage location (e.g., `asia-south1`)

#### 4.4 Firebase Cloud Messaging (FCM)
1. Go to **Cloud Messaging** in left menu
2. No additional setup needed for basic functionality

### Step 5: Verify Firebase Integration

1. Build and run the app
2. Check Firebase Console → Analytics → Dashboard
3. You should see an active user within a few minutes

## Backend Setup (Optional)

The app works in offline mode, but for full functionality, you can run the backend locally.

### Step 1: Install Dependencies

```bash
cd backend
npm install
```

### Step 2: Configure Environment Variables

1. Copy the example file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and configure:

   ```env
   # Server Configuration
   NODE_ENV=development
   PORT=3000
   
   # Supabase Configuration (see Database Setup section)
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key
   
   # JWT Configuration
   JWT_SECRET=your-development-secret-key
   JWT_REFRESH_SECRET=your-development-refresh-key
   ```

### Step 3: Run Backend Locally

```bash
npm run dev
```

Backend will start on `http://localhost:3000`

### Step 4: Test Backend

```bash
curl http://localhost:3000/api/health
```

Should return: `{"status":"ok"}`

## Database Setup (Optional)

### Option 1: Use Supabase Cloud (Recommended)

1. Go to [Supabase](https://supabase.com)
2. Create a new project
3. Copy the project URL and API keys
4. Update `backend/.env` with Supabase credentials
5. Run migrations:
   ```bash
   cd supabase
   npx supabase db push
   ```

### Option 2: Run Supabase Locally

1. Install Docker Desktop
2. Start Supabase:
   ```bash
   cd supabase
   npx supabase start
   ```
3. Supabase will provide local credentials
4. Update `backend/.env` with local credentials

## Running the App

### Option 1: Using Android Emulator

1. In Android Studio, click **"Device Manager"** (phone icon)
2. Click **"Create Device"**
3. Select a device (e.g., Pixel 5)
4. Select system image: **API 34** (Android 14)
5. Click **"Finish"**
6. Click **"Run"** (green play button) in Android Studio

### Option 2: Using Physical Device

1. Enable **Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   
2. Enable **USB Debugging**:
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

3. Connect device via USB

4. In Android Studio, select your device from dropdown

5. Click **"Run"** (green play button)

### Verify App is Running

- App should launch and show the home screen
- Default demo user is automatically logged in
- You should see sample listings (if backend is not running)

## Troubleshooting

### Issue: Gradle Sync Failed

**Error**: `Could not resolve all dependencies`

**Solution**:
1. Check internet connection
2. In Android Studio: File → Invalidate Caches → Invalidate and Restart
3. Delete `.gradle` folder and sync again

---

### Issue: SDK Location Not Found

**Error**: `SDK location not found`

**Solution**:
1. Verify `local.properties` exists in project root
2. Check SDK path is correct
3. Use double backslashes on Windows: `C\:\\Users\\...`

---

### Issue: google-services.json Missing

**Error**: `File google-services.json is missing`

**Solution**:
1. Follow [Firebase Configuration](#firebase-configuration) steps
2. Ensure file is in `app/` directory (not `app/src/`)
3. Sync Gradle again

---

### Issue: App Crashes on Launch

**Error**: Firebase initialization error

**Solution**:
1. Verify `google-services.json` is properly configured
2. Check package name matches: `com.senthapps.slagrimarket`
3. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

---

### Issue: Cannot Connect to Backend

**Error**: Network errors, empty data

**Solution**:
1. **Using Emulator**: Backend URL should be `http://10.0.2.2:3000/api/`
   - `10.0.2.2` is the emulator's alias for `localhost`
   
2. **Using Physical Device**: 
   - Backend must be accessible on your network
   - Use your computer's IP address: `http://192.168.x.x:3000/api/`
   - Update `BuildConfig.BASE_URL` in `app/build.gradle.kts`

3. **Verify backend is running**:
   ```bash
   curl http://localhost:3000/api/health
   ```

---

### Issue: Build Takes Too Long

**Solution**:
1. Increase Gradle memory in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx4096m
   ```

2. Enable Gradle daemon:
   ```properties
   org.gradle.daemon=true
   ```

3. Use parallel builds:
   ```properties
   org.gradle.parallel=true
   ```

---

### Issue: Emulator is Slow

**Solution**:
1. Enable hardware acceleration (HAXM on Intel, WHPX on Windows)
2. Allocate more RAM to emulator (4GB recommended)
3. Use x86_64 system images (faster than ARM)
4. Consider using a physical device for testing

---

### Issue: Language Not Changing

**Known Issue**: Language preference is not persisted (see `KNOWN_ISSUES.md`)

**Workaround**: Re-select language each time app is opened

**Fix**: Implement DataStore persistence (good first issue for interns!)

---

### Issue: Images Not Uploading

**Error**: Firebase Storage errors

**Solution**:
1. Verify Firebase Storage is enabled in Firebase Console
2. Check storage rules allow writes
3. Ensure `google-services.json` includes storage bucket
4. Check internet connection

---

## Next Steps

After successful setup:

1. **Read the documentation**:
   - [ARCHITECTURE.md](ARCHITECTURE.md) - Understand the app structure
   - [CONTRIBUTING.md](CONTRIBUTING.md) - Learn the development workflow
   - [KNOWN_ISSUES.md](KNOWN_ISSUES.md) - See current limitations

2. **Explore the codebase**:
   - Start with `JaffnaMarketplaceApplication.kt` (app entry point)
   - Look at `HomeScreen.kt` (main UI)
   - Examine `ListingRepository.kt` (data layer)

3. **Make your first change**:
   - Try changing a string resource
   - Modify a UI color
   - Add a log statement

4. **Run tests**:
   ```bash
   ./gradlew test
   ```

5. **Pick a starter task**:
   - See "Suggested Starter Tasks" in the pre-handoff review
   - Check GitHub Issues for "good first issue" label

## Getting Help

- **Documentation**: Check `ARCHITECTURE.md`, `CONTRIBUTING.md`, `KNOWN_ISSUES.md`
- **GitHub Issues**: Search existing issues or create a new one
- **Code Comments**: Look for inline comments in complex code sections

---

**Happy Coding!** 🚀🌾

