# Development Environment Setup - Agrimarket

This comprehensive guide will help you set up a complete development environment for the Agrimarket Android app, including Android Studio, Firebase, backend services, and testing tools.

---

## 📋 Table of Contents

- [System Requirements](#system-requirements)
- [Android Studio Setup](#android-studio-setup)
- [Project Configuration](#project-configuration)
- [Firebase Setup](#firebase-setup)
- [Backend Setup (Optional)](#backend-setup-optional)
- [Database Setup (Optional)](#database-setup-optional)
- [IDE Configuration](#ide-configuration)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)

---

## 💻 System Requirements

### Minimum Requirements
- **OS:** Windows 10/11, macOS 10.14+, or Ubuntu 18.04+
- **RAM:** 8 GB (16 GB recommended)
- **Disk Space:** 10 GB free space
- **Internet:** Stable connection for downloading dependencies

### Recommended Specifications
- **RAM:** 16 GB or more
- **CPU:** Intel i5/i7 or AMD Ryzen 5/7 (8th gen or newer)
- **SSD:** For faster build times
- **Display:** 1920x1080 or higher resolution

---

## 🛠️ Android Studio Setup

### Step 1: Download and Install Android Studio

1. **Download Android Studio Hedgehog (2023.1.1) or later:**
   - Visit: https://developer.android.com/studio
   - Download the installer for your operating system

2. **Install Android Studio:**
   - **Windows:** Run the `.exe` installer and follow the wizard
   - **Mac:** Open the `.dmg` file and drag Android Studio to Applications
   - **Linux:** Extract the archive and run `studio.sh` from the `bin/` directory

3. **Complete the Setup Wizard:**
   - Choose "Standard" installation type
   - Select your preferred UI theme
   - Allow the wizard to download required SDK components

### Step 2: Install Required SDK Components

1. Open Android Studio
2. Go to **Tools** → **SDK Manager**
3. In the **SDK Platforms** tab, install:
   - ✅ Android 14.0 (API 34) - Latest
   - ✅ Android 7.0 (API 24) - Minimum supported version
   - ✅ Android 10.0 (API 29) - Recommended for testing

4. In the **SDK Tools** tab, ensure these are installed:
   - ✅ Android SDK Build-Tools (latest version)
   - ✅ Android SDK Platform-Tools
   - ✅ Android SDK Tools
   - ✅ Android Emulator
   - ✅ Intel x86 Emulator Accelerator (HAXM) - Windows/Mac only
   - ✅ Google Play services
   - ✅ Google USB Driver (Windows only)

5. Click **Apply** to download and install selected components

### Step 3: Install Required Plugins

1. Go to **File** → **Settings** (Windows/Linux) or **Android Studio** → **Preferences** (Mac)
2. Navigate to **Plugins**
3. Ensure these plugins are installed and enabled:
   - ✅ **Kotlin** (should be pre-installed)
   - ✅ **Android APK Support**
   - ✅ **Gradle**
   - ✅ **Git**

4. **Optional but Recommended:**
   - **Rainbow Brackets** - Better code readability
   - **Key Promoter X** - Learn keyboard shortcuts
   - **GitToolBox** - Enhanced Git integration

---

## ⚙️ Project Configuration

### Step 1: Clone the Repository

```bash
# Using HTTPS
git clone https://github.com/Kabi10/Srilanka-Farmers-Marketplace.git

# OR using SSH (if you have SSH keys configured)
git clone git@github.com:Kabi10/Srilanka-Farmers-Marketplace.git

# Navigate to project directory
cd Srilanka-Farmers-Marketplace
```

### Step 2: Configure local.properties

The `local.properties` file contains local machine-specific configuration.

**Automatic Configuration (Recommended):**
1. Open the project in Android Studio
2. Android Studio will automatically create `local.properties`
3. Verify the file was created in the project root

**Manual Configuration:**

Create `local.properties` in the project root with the following content:

**Windows:**
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

**macOS:**
```properties
sdk.dir=/Users/YourUsername/Library/Android/sdk
```

**Linux:**
```properties
sdk.dir=/home/YourUsername/Android/Sdk
```

**Important Notes:**
- Replace `YourUsername` with your actual username
- Windows paths require double backslashes (`\\`)
- The file should NOT be committed to Git (it's in `.gitignore`)

**Finding Your SDK Path:**
1. Open Android Studio
2. Go to **Tools** → **SDK Manager**
3. The SDK path is shown at the top: **Android SDK Location**

### Step 3: Configure Gradle Properties (Optional)

For better build performance, create or edit `gradle.properties` in the project root:

```properties
# Enable Gradle daemon
org.gradle.daemon=true

# Increase heap size for better performance
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# Enable parallel builds
org.gradle.parallel=true

# Enable configuration cache
org.gradle.configuration-cache=true

# Enable build cache
org.gradle.caching=true

# Use AndroidX
android.useAndroidX=true
android.enableJetifier=true
```

---

## 🔥 Firebase Setup

Firebase is used for analytics, crash reporting, and cloud storage.

### Option 1: Quick Start (Demo Configuration)

For development and testing without Firebase features:

```bash
# Copy the template
cp app/google-services.json.template app/google-services.json
```

**Limitations:**
- Analytics disabled
- Crash reporting disabled
- Cloud storage unavailable

### Option 2: Full Firebase Setup (Recommended)

#### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project**
3. Enter project name: `Agrimarket` (or your preferred name)
4. Enable Google Analytics (recommended)
5. Select or create a Google Analytics account
6. Click **Create project**

#### Step 2: Add Android App to Firebase

1. In Firebase Console, click **Add app** → **Android**
2. Enter the following details:
   - **Package name:** `com.senthapps.slagrimarket` (MUST match exactly)
   - **App nickname:** `Agrimarket Android` (optional)
   - **Debug signing certificate SHA-1:** (optional, for Google Sign-In)
3. Click **Register app**

#### Step 3: Download Configuration File

1. Download the `google-services.json` file
2. Place it in the `app/` directory (NOT `app/src/`)
3. Verify the file location:
   ```
   Srilanka-Farmers-Marketplace/
   ├── app/
   │   ├── google-services.json  ← Should be here
   │   ├── build.gradle.kts
   │   └── src/
   ```

#### Step 4: Enable Firebase Services

In Firebase Console, enable the following services:

1. **Analytics:**
   - Already enabled by default
   - No additional configuration needed

2. **Crashlytics:**
   - Go to **Crashlytics** in left sidebar
   - Click **Enable Crashlytics**

3. **Cloud Storage:**
   - Go to **Storage** in left sidebar
   - Click **Get started**
   - Choose production mode
   - Select a location (asia-south1 recommended for Sri Lanka)

4. **Authentication** (if using OTP):
   - Go to **Authentication** → **Sign-in method**
   - Enable **Phone** authentication
   - Configure phone number verification settings

#### Step 5: Verify Firebase Integration

1. Build and run the app
2. Check Firebase Console → **Analytics** → **Dashboard**
3. You should see app connection within a few minutes

---

## 🖥️ Backend Setup (Optional)

The app works fully offline! Backend is only needed for multi-user sync and OTP authentication.

### Prerequisites

- **Node.js 18+** and **npm 9+**
  - Download: https://nodejs.org/
  - Verify: `node --version` and `npm --version`

### Step 1: Navigate to Backend Directory

```bash
cd backend
```

### Step 2: Install Dependencies

```bash
npm install
```

### Step 3: Configure Environment Variables

Create a `.env` file in the `backend/` directory:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# Supabase Configuration
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key

# Server Configuration
PORT=3000
NODE_ENV=development

# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRES_IN=7d

# OTP Configuration (for SMS)
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_PHONE_NUMBER=+1234567890

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,http://10.0.2.2:3000
```

### Step 4: Start Development Server

```bash
npm run dev
```

The server will start on `http://localhost:3000`

### Step 5: Test Backend Connection

```bash
# Test health endpoint
curl http://localhost:3000/health

# Expected response:
# {"status":"ok"}
```

### Step 6: Configure App to Use Local Backend

Edit `app/build.gradle.kts` and update the `BASE_URL`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")  // For emulator
// OR
buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:3000/api/\"")  // For physical device
```

**Finding Your IP Address:**
- **Windows:** `ipconfig` (look for IPv4 Address)
- **Mac/Linux:** `ifconfig` or `ip addr`

---

## 🗄️ Database Setup (Optional)

### Prerequisites

- **Supabase account** (free tier available)
  - Sign up: https://supabase.com/
- **Supabase CLI**
  - Install: `npm install -g supabase`

### Step 1: Create Supabase Project

1. Go to [Supabase Dashboard](https://app.supabase.com/)
2. Click **New project**
3. Enter project details:
   - **Name:** `agrimarket`
   - **Database Password:** (choose a strong password)
   - **Region:** Southeast Asia (Singapore) - closest to Sri Lanka
4. Click **Create new project**

### Step 2: Get Supabase Credentials

1. In Supabase Dashboard, go to **Settings** → **API**
2. Copy the following:
   - **Project URL:** `https://your-project.supabase.co`
   - **anon/public key:** `eyJhbGc...`
   - **service_role key:** `eyJhbGc...` (keep this secret!)

3. Update your backend `.env` file with these credentials

### Step 3: Link Local Project to Supabase

```bash
# Navigate to supabase directory
cd supabase

# Login to Supabase CLI
npx supabase login

# Link to your project
npx supabase link --project-ref your-project-ref
```

**Finding your project ref:**
- In Supabase Dashboard → **Settings** → **General**
- Look for **Reference ID**

### Step 4: Apply Database Migrations

```bash
# Push migrations to Supabase
npx supabase db push

# Verify migrations were applied
npx supabase db diff
```

### Step 5: Verify Database Setup

1. Go to Supabase Dashboard → **Table Editor**
2. You should see the following tables:
   - `users`
   - `listings`
   - `transactions`
   - `activities`
   - `market_prices`

---

## 🎨 IDE Configuration

### Code Style Settings

1. Go to **File** → **Settings** → **Editor** → **Code Style**
2. **Kotlin:**
   - **Scheme:** Project (should be configured automatically)
   - **Tabs and Indents:** 4 spaces
   - **Continuation indent:** 4 spaces

3. **XML:**
   - **Tabs and Indents:** 4 spaces

### Recommended Android Studio Settings

1. **File** → **Settings** → **Build, Execution, Deployment** → **Compiler**
   - ✅ Enable **Build project automatically**
   - Set **Command-line Options:** `--parallel --configuration-cache`

2. **File** → **Settings** → **Editor** → **General** → **Auto Import**
   - ✅ Enable **Add unambiguous imports on the fly**
   - ✅ Enable **Optimize imports on the fly**

3. **File** → **Settings** → **Editor** → **Inspections**
   - ✅ Enable **Kotlin** → **Probable bugs**
   - ✅ Enable **Android** → **Lint**

### Git Configuration

```bash
# Set your name and email
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Set default branch name
git config --global init.defaultBranch main

# Enable credential caching (optional)
git config --global credential.helper cache
```

---

## ✅ Verification

### Run Tests

```bash
# Run unit tests
.\gradlew test

# Run instrumented tests (requires device/emulator)
.\gradlew connectedAndroidTest

# Check for lint issues
.\gradlew lint

# Generate test coverage report
.\gradlew jacocoTestReport
```

### Build Release APK

```bash
# Build release APK (unsigned)
.\gradlew assembleRelease

# APK location:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### Verify Firebase Connection

1. Build and run the app
2. Navigate through different screens
3. Check Firebase Console → **Analytics** → **Events**
4. You should see events like `screen_view`, `app_open`, etc.

---

## 🐛 Troubleshooting

See [QUICKSTART.md](QUICKSTART.md) for common issues and solutions.

For additional help:
- Check [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for known bugs
- Search existing [GitHub issues](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues)
- Create a new issue with detailed reproduction steps

---

## 📚 Next Steps

- Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the codebase structure
- Read [CONTRIBUTING.md](CONTRIBUTING.md) to learn the development workflow
- Check [ROADMAP.md](ROADMAP.md) to see planned features
- Start contributing! Look for `good first issue` labels

---

**Happy coding! 🚀**

