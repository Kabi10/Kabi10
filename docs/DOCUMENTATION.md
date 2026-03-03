# Agrimarket Documentation

**Last Updated:** February 14, 2026
**Version:** 1.0

This is the comprehensive documentation for the Agrimarket (Sri Lanka Farmers Marketplace) Android application.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Architecture](#2-architecture)
3. [Development Guide](#3-development-guide)
4. [Integrations](#4-integrations)
5. [Operations](#5-operations)
6. [Known Issues & Roadmap](#6-known-issues--roadmap)

---

# 1. Getting Started

## 1.1 Quick Start (5 Steps)

Get the app running in under 30 minutes.

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 11+** (bundled with Android Studio)
- **Git** for version control
- **Android device or emulator** (API 24+ / Android 7.0+)

### Step 1: Clone Repository

```bash
git clone https://github.com/Kabi10/Srilanka-Farmers-Marketplace.git
cd Srilanka-Farmers-Marketplace
```

### Step 2: Configure local.properties

Open in Android Studio - it auto-creates `local.properties` with SDK path.

**Manual (if needed):**

```properties
# Windows
sdk.dir=C\\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# Mac
sdk.dir=/Users/YourUsername/Library/Android/sdk
```

### Step 3: Firebase Configuration

```bash
cp app/google-services.json.template app/google-services.json
```

### Step 4: Build

```bash
.\gradlew assembleDebug
```

### Step 5: Run

Connect device or start emulator, then click Run in Android Studio.

---

## 1.2 Full Setup Guide

### System Requirements

- **RAM:** 8 GB minimum (16 GB recommended)
- **Disk:** 10 GB free space
- **OS:** Windows 10/11, macOS 10.14+, Ubuntu 18.04+

### SDK Components Required

In Android Studio → Tools → SDK Manager:

- ✅ Android 14.0 (API 34)
- ✅ Android 7.0 (API 24) - minimum supported
- ✅ Android SDK Build-Tools
- ✅ Android Emulator
- ✅ Google Play services

### Backend Setup (Optional)

The app works fully offline. Backend is only needed for multi-user sync.

```bash
cd backend
npm install
cp .env.example .env
# Configure SUPABASE_URL, SUPABASE_ANON_KEY, JWT_SECRET
npm run dev
```

### Database Setup (Optional)

1. Create project at [Supabase](https://supabase.com/)
2. Copy credentials to `.env`
3. Run migrations: `npx supabase db push`

---

# 2. Architecture

## 2.1 Overview

The app follows **Clean Architecture** with **MVVM** pattern and **offline-first** design.

```
┌─────────────────────────────────────────┐
│         UI Layer (Jetpack Compose)       │
├─────────────────────────────────────────┤
│         ViewModel Layer (StateFlow)      │
├─────────────────────────────────────────┤
│         Repository Layer (Offline-First) │
├──────────────────┬──────────────────────┤
│  Local (Room DB) │  Remote (Retrofit)   │
└──────────────────┴──────────────────────┘
```

## 2.2 Key Components

| Layer      | Location            | Purpose                                              |
| ---------- | ------------------- | ---------------------------------------------------- |
| UI         | `ui/`               | Jetpack Compose screens                              |
| ViewModel  | `ui/*/ViewModel.kt` | State management                                     |
| Repository | `data/repository/`  | Data coordination                                    |
| Local DB   | `data/dao/`         | Room DAOs                                            |
| Remote API | `data/api/`         | Retrofit services (11 services)                      |
| Real-time  | `data/sync/`        | Supabase Realtime for chat                           |
| DI         | `di/`               | Hilt modules (Network, Database, Supabase, Firebase) |

## 2.3 Offline-First Pattern

```kotlin
fun getAllListings(): Flow<Resource<List<Listing>>> = flow {
    emit(Resource.Loading())

    // 1. Emit cached data first
    val cached = listingDao.getAll()
    if (cached.isNotEmpty()) emit(Resource.Success(cached))

    // 2. Fetch from network
    try {
        val remote = api.getListings()
        listingDao.insertAll(remote)
        emit(Resource.Success(remote))
    } catch (e: Exception) {
        if (cached.isEmpty()) emit(Resource.Error(e.message))
    }
}
```

## 2.4 Trilingual Support

- **Languages:** English, Tamil (தமிழ்), Sinhala (සිංහල)
- **Resources:** `values/`, `values-ta/`, `values-si/`
- **173 strings** fully translated

---

# 3. Development Guide

## 3.1 Code Standards

### Kotlin Style

- **Indentation:** 4 spaces
- **Line length:** 120 chars max
- **Naming:** `PascalCase` for classes/composables, `camelCase` for functions

### Compose Best Practices

```kotlin
// ✅ Stateless with modifier parameter
@Composable
fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// ❌ Avoid ViewModel in composables
@Composable
fun ListingCard(viewModel: ViewModel)
```

### String Resources

- **Never hardcode** user-facing text
- Support all 3 languages
- Naming: `screen_element_description`

## 3.2 Testing

### Run Tests

```bash
.\gradlew test              # Unit tests
.\gradlew connectedAndroidTest  # UI tests
.\gradlew lint              # Lint checks
```

### Test Coverage

- **Unit tests:** 143 tests across 11 test files (ViewModels, Repositories, Converters)
- **UI tests:** 27 tests across 5 test files (critical flows)
- Target: >70% coverage for new code

## 3.3 Contributing

### Branching Strategy

- `master` - Production
- `feature/name` - New features
- `bugfix/description` - Bug fixes

### PR Process

1. Create feature branch from master
2. Make changes with tests
3. Run lint and tests locally
4. Create PR with description
5. Address review feedback

### Commit Format

```
feat(listings): add filter by crop type
fix(auth): resolve OTP timeout
docs(readme): update setup instructions
```

---

# 4. Integrations

## 4.1 Firebase

### Services Enabled

- ✅ **Analytics** - User tracking
- ✅ **Crashlytics** - Crash reporting
- ✅ **Cloud Messaging** - Push notifications
- ✅ **Performance** - App monitoring

### Configuration

1. Create Firebase project
2. Add Android app with package `com.senthapps.slagrimarket`
3. Download `google-services.json` to `app/`

### Usage

```kotlin
// Analytics
Firebase.analytics.logEvent("listing_viewed") {
    param("listing_id", id)
}

// Crashlytics
FirebaseCrashlytics.getInstance().recordException(e)
```

## 4.2 Image Upload

### Flow

1. User selects images (max 5)
2. Images converted to base64
3. Uploaded to Supabase Storage
4. URLs stored in listing record

### Components

- `ImagePicker.kt` - Multi-image selection
- `ImageUploadUtil.kt` - Compression & encoding
- `ListingRepository.uploadImages()` - API call

## 4.3 Supabase Pro Configuration

### Features Enabled ($25/mo)

- ✅ 7-day automatic backups
- ✅ Image transformation API
- ✅ MFA (TOTP + Phone)
- ✅ Read replicas (available when needed)

### Dashboard Steps

1. **Upgrade to Pro** at supabase.com
2. **Enable Image Transformation** in Settings → Storage
3. **Configure MFA** in Authentication → Providers

See `docs/SUPABASE_PRO_CONFIGURATION.md` for detailed steps.

---

# 5. Operations

## 5.1 Deployment

### Backend (Vercel)

```bash
cd backend
vercel --prod
```

### Android (Google Play)

```bash
.\gradlew bundleRelease
# Upload app/build/outputs/bundle/release/*.aab to Play Console
```

### Environment Variables (Vercel)

| Variable                    | Purpose       |
| --------------------------- | ------------- |
| `SUPABASE_URL`              | Database URL  |
| `SUPABASE_SERVICE_ROLE_KEY` | Admin access  |
| `JWT_SECRET`                | Token signing |
| `REQUEST_SIGNING_SECRET`    | API security  |

## 5.2 Disaster Recovery

### Incident Severity

| Level | Description        | Response |
| ----- | ------------------ | -------- |
| SEV-1 | Complete outage    | 15 min   |
| SEV-2 | Major feature down | 1 hour   |
| SEV-3 | Minor impact       | 4 hours  |

### Recovery Commands

```bash
# Rollback Vercel deployment
vercel rollback [deployment-url]

# Check database connections
SELECT count(*), state FROM pg_stat_activity GROUP BY state;

# Restore from backup
pg_restore -h HOST -U postgres backup.dump
```

### Contacts

- **Supabase:** supabase.com/dashboard/support
- **Vercel:** vercel.com/support
- **Firebase:** firebase.google.com/support

## 5.3 Monitoring

### Health Endpoints

- `GET /health` - Basic health check
- `GET /health/detailed` - Full system status

### Key Metrics

- API response times (<500ms p95)
- Crash-free users (>99%)
- Database connections (<80% of pool)

---

# 6. Known Issues & Roadmap

## 6.1 Current Issues

### High Priority

| Issue                             | Status         | Workaround        |
| --------------------------------- | -------------- | ----------------- |
| Test coverage gap                 | 🟡 In Progress | Manual testing    |
| Language preference not persisted | 🟡 Planned     | Toggle on restart |

### Medium Priority

| Issue                  | Description                     |
| ---------------------- | ------------------------------- |
| Image upload >5MB      | May fail - resize before upload |
| Limited search         | Exact matches only              |
| Offline sync conflicts | Last-write-wins strategy        |

### Resolved Recently

- ✅ Trilingual support completion
- ✅ Deprecated Divider() components
- ✅ Critical UI bugs fixed

## 6.2 Development Roadmap

### Phase 1: Production (Complete)

- ✅ Real authentication
- ✅ Backend deployment
- ✅ ProGuard obfuscation
- ✅ CI/CD pipeline

### Phase 2: Scale (Current)

- ✅ Supabase Pro upgrade
- ✅ Read replica support
- 🔄 Push notifications
- 🔄 Advanced search filters

### Phase 3: Future

- [ ] In-app messaging improvements
- [ ] Video support for listings
- [ ] AI-powered crop detection
- [ ] Payment integration

---

## Additional Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Supabase Documentation](https://supabase.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)

---

**For production readiness status, see:** `PRODUCTION_READINESS_ASSESSMENT.md`

**Questions?** Create an issue at [GitHub](https://github.com/Kabi10/Srilanka-Farmers-Marketplace/issues)
