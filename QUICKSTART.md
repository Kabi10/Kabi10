# Agrimarket - Quick Start Guide

**Get the entire Agrimarket project running in under 30 minutes!**

This guide will help you clone, build, and run all components of the Agrimarket project (Android app, backend API, and web landing page).

---

## 📋 Prerequisites Checklist

Before you begin, ensure you have the following installed:

### Required for Android App
- [ ] **Android Studio** Hedgehog (2023.1.1) or later - [Download](https://developer.android.com/studio)
- [ ] **JDK 11+** (usually bundled with Android Studio)
- [ ] **Git** - [Download](https://git-scm.com/downloads)
- [ ] **Android device** or emulator (API 24+)

### Required for Backend (Optional - app works offline)
- [ ] **Node.js 18+** and npm 9+ - [Download](https://nodejs.org/)
- [ ] **Supabase account** (free tier) - [Sign up](https://supabase.com)

### System Requirements
- [ ] **8GB RAM minimum** (16GB recommended)
- [ ] **10GB free disk space** (20GB recommended)
- [ ] **Internet connection** for initial setup

---

## 🚀 Quick Start (5 Steps)

### Step 1: Clone the Repository

```bash
git clone https://github.com/Kabi10/Srilanka-Farmers-Marketplace.git
cd Srilanka-Farmers-Marketplace
```

### Step 2: Configure Android App

1. **Set up local.properties:**
   ```bash
   # Windows (PowerShell)
   Copy-Item local.properties.template local.properties
   
   # macOS/Linux
   cp local.properties.template local.properties
   ```

2. **Edit `local.properties`** and set your Android SDK path:
   
   **Windows:**
   ```properties
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```
   
   **macOS:**
   ```properties
   sdk.dir=/Users/YourName/Library/Android/sdk
   ```
   
   **Linux:**
   ```properties
   sdk.dir=/home/YourName/Android/Sdk
   ```

3. **Set up Firebase:**
   ```bash
   # Copy the template
   # Windows
   Copy-Item app\google-services.json.template app\google-services.json
   
   # macOS/Linux
   cp app/google-services.json.template app/google-services.json
   ```
   
   Then follow the [Firebase Setup Guide](#firebase-setup-detailed) below.

### Step 3: Build the Android App

```bash
# Windows
.\gradlew assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

**Expected output:** `BUILD SUCCESSFUL` in 2-5 minutes

### Step 4: Run the Android App

**Option A: Using Android Studio (Recommended)**
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete
5. Click the green "Run" button (▶️)

**Option B: Using Command Line**
```bash
# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.senthapps.slagrimarket/.MainActivity
```

### Step 5: Verify the App Works

✅ **Success Checklist:**
- [ ] App launches without crashing
- [ ] You see the home screen with demo listings
- [ ] You can switch languages (English/Tamil/Sinhala)
- [ ] You can navigate between tabs (Home, Market, Transactions, Profile)
- [ ] Demo user is automatically logged in

**🎉 Congratulations! The Android app is running!**

The app works fully offline with demo data. For full functionality with backend sync, continue to the optional backend setup below.

---

## 🔧 Optional: Backend Setup

The backend enables real-time data sync, OTP authentication, and multi-user functionality.

### Step 1: Set Up Supabase

1. **Create a Supabase project:**
   - Go to [supabase.com](https://supabase.com)
   - Click "New Project"
   - Name: "agrimarket-dev"
   - Database password: (save this securely)
   - Region: Choose closest to Sri Lanka (e.g., Singapore)

2. **Get your credentials:**
   - Go to Project Settings → API
   - Copy: `Project URL`, `anon public key`, `service_role key`

3. **Run database migrations:**
   ```bash
   # Install Supabase CLI
   npm install -g supabase
   
   # Login to Supabase
   npx supabase login
   
   # Link to your project
   cd supabase
   npx supabase link --project-ref YOUR_PROJECT_REF
   
   # Push migrations
   npx supabase db push
   ```

### Step 2: Configure Backend

1. **Install dependencies:**
   ```bash
   cd backend
   npm install
   ```

2. **Set up environment variables:**
   ```bash
   # Windows
   Copy-Item .env.example .env
   
   # macOS/Linux
   cp .env.example .env
   ```

3. **Edit `backend/.env`** with your Supabase credentials:
   ```env
   NODE_ENV=development
   PORT=3000
   
   # Supabase Configuration
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your_anon_key_here
   SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here
   
   # JWT Configuration
   JWT_SECRET=your-development-secret-key
   JWT_REFRESH_SECRET=your-development-refresh-key
   
   # For development, enable mock SMS
   MOCK_SMS=true
   ```

### Step 3: Run the Backend

```bash
cd backend
npm run dev
```

**Expected output:**
```
Server running on http://localhost:3000
Database connected successfully
```

### Step 4: Test the Backend

```bash
# Test health endpoint
curl http://localhost:3000/health

# Expected response:
# {"status":"ok","timestamp":"..."}
```

---

## 🌐 Optional: Web Landing Page

The web landing page is a static site that requires no build process.

### Local Development

```bash
cd web

# Option 1: Python
python -m http.server 8000

# Option 2: Node.js
npx http-server -p 8000

# Option 3: PHP
php -S localhost:8000
```

Open `http://localhost:8000` in your browser.

### Deploy to Vercel (Free)

```bash
cd web
npx vercel
```

Follow the prompts to deploy. Your site will be live at `https://your-project.vercel.app`

---

## 🔥 Firebase Setup (Detailed)

Firebase is required for Analytics, Crashlytics, Storage, and Cloud Messaging.

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Project name: "Agrimarket Dev"
4. Disable Google Analytics (optional for dev)
5. Click "Create project"

### 2. Add Android App

1. Click "Add app" → Android (robot icon)
2. **Android package name:** `com.senthapps.slagrimarket`
3. App nickname: "Agrimarket Android"
4. Leave SHA-1 blank for now
5. Click "Register app"

### 3. Download Configuration

1. Download `google-services.json`
2. Place it in `app/google-services.json` (replace the template)
3. **IMPORTANT:** Never commit this file to Git!

### 4. Enable Firebase Services

**Firebase Storage:**
1. Go to Storage in left menu
2. Click "Get started"
3. Use default security rules
4. Choose location: `asia-south1` (Mumbai)

**Firebase Crashlytics:**
1. Go to Crashlytics in left menu
2. Click "Enable Crashlytics"
3. Follow setup wizard

**Cloud Messaging (FCM):**
- Already enabled by default
- No additional setup needed

### 5. Verify Integration

1. Build and run the app
2. Check Firebase Console → Analytics → Dashboard
3. You should see an active user within a few minutes

---

## ✅ Verification & Testing

### Android App Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### Backend Tests

```bash
cd backend
npm test
```

### Manual Testing Checklist

- [ ] **Language switching** works (EN/TA/SI)
- [ ] **Create listing** flow works
- [ ] **View listings** shows data
- [ ] **Offline mode** works (airplane mode)
- [ ] **Firebase** integration works (check console)
- [ ] **Backend sync** works (if backend is running)

---

## 🐛 Common Issues & Solutions

### Issue: "SDK location not found"

**Solution:**
1. Verify `local.properties` exists in project root
2. Check SDK path is correct
3. Use double backslashes on Windows: `C\:\\Users\\...`

### Issue: "google-services.json is missing"

**Solution:**
1. Download from Firebase Console
2. Place in `app/` directory (not `app/src/`)
3. Rebuild project

### Issue: "Gradle sync failed"

**Solution:**
```bash
# Invalidate caches in Android Studio
File → Invalidate Caches → Invalidate and Restart

# Or delete .gradle folder
rm -rf .gradle
./gradlew clean build
```

### Issue: "Cannot connect to backend"

**Solution:**
- **Emulator:** Use `http://10.0.2.2:3000/api/` (not localhost)
- **Physical device:** Use your computer's IP: `http://192.168.x.x:3000/api/`
- Verify backend is running: `curl http://localhost:3000/health`

### Issue: "Build takes too long"

**Solution:** Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.daemon=true
org.gradle.parallel=true
```

---

## 📚 Next Steps

After successful setup:

1. **Read the documentation:**
   - [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture
   - [CONTRIBUTING.md](CONTRIBUTING.md) - Development workflow
   - [KNOWN_ISSUES.md](KNOWN_ISSUES.md) - Current limitations

2. **Explore the codebase:**
   - Start with `app/src/main/java/com/senthapps/slagrimarket/`
   - Look at `HomeScreen.kt` for UI examples
   - Examine `ListingRepository.kt` for data layer

3. **Pick a starter task:**
   - Check [GitHub Issues](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues)
   - Look for "good first issue" label
   - See ROADMAP.md for planned features

4. **Join the community:**
   - Create issues for bugs or questions
   - Submit pull requests for improvements
   - Help other developers in discussions

---

## 🆘 Getting Help

- **Documentation:** Check SETUP.md, ARCHITECTURE.md, CONTRIBUTING.md
- **GitHub Issues:** [Create an issue](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues/new)
- **Code Comments:** Look for inline comments in complex sections

---

**Happy Coding! 🚀🌾**

Built with ❤️ for the farming community of Jaffna, Sri Lanka

