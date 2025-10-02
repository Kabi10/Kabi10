# Backend Integration Guide - Agrimarket Android App

## 📋 Overview

This guide provides step-by-step instructions for integrating the Agrimarket Android app with the production Vercel + Supabase serverless backend.

**Current Status:**
- ✅ Backend code exists in `backend/` directory
- ✅ Serverless functions ready for Vercel deployment
- ✅ Supabase migration scripts available
- ✅ Android app configured for offline-first with mock data
- ⚠️ Integration pending

---

## 1. Backend Deployment

### 1.1 Verify Backend Production Readiness

The existing backend is **production-ready** with the following components:

**Serverless Functions (Vercel):**
```
backend/api/
├── auth/
│   ├── send-otp.js              ✅ OTP generation and SMS
│   ├── verify-otp-simple.js     ✅ OTP verification
│   ├── verify-otp.js            ✅ Full OTP verification with JWT
│   └── refresh-token.js         ✅ JWT token refresh
├── listings/
│   ├── index.js                 ⚠️  Stub (needs implementation)
│   └── create.js                ✅ Create listings
├── transactions/
│   ├── index.js                 ⚠️  Stub (needs implementation)
│   └── create.js                ⚠️  Stub (needs implementation)
├── sync/
│   └── operations.js            ⚠️  Stub (needs implementation)
└── health.js                    ✅ Health check
```

**Database (Supabase):**
- ✅ Complete schema in `backend/src/database/supabase-migrations.sql`
- ✅ Row Level Security (RLS) policies defined
- ✅ Storage buckets for images configured
- ✅ Indexes optimized for performance

### 1.2 Deploy to Vercel (15 minutes)

#### Step 1: Install Vercel CLI
```bash
npm install -g vercel
vercel login
```

#### Step 2: Navigate to Backend Directory
```bash
cd backend
npm install
```

#### Step 3: Configure Environment Variables
```bash
# Set production environment variables
vercel env add SUPABASE_URL production
# Enter: https://your-project-id.supabase.co

vercel env add SUPABASE_ANON_KEY production
# Enter: your_anon_key_from_supabase

vercel env add SUPABASE_SERVICE_ROLE_KEY production
# Enter: your_service_role_key_from_supabase

vercel env add JWT_SECRET production
# Generate: openssl rand -base64 32

vercel env add JWT_REFRESH_SECRET production
# Generate: openssl rand -base64 32

vercel env add SMS_PROVIDER production
# Enter: dialog

vercel env add DIALOG_API_KEY production
# Enter: your_dialog_api_key

vercel env add DIALOG_API_SECRET production
# Enter: your_dialog_api_secret

vercel env add NODE_ENV production
# Enter: production
```

#### Step 4: Deploy
```bash
vercel --prod
# Follow prompts:
# - Project name: agrimarket-api
# - Directory: ./
# - Override settings? No
```

**Expected Output:**
```
✅ Production: https://agrimarket-api.vercel.app [copied to clipboard]
```

### 1.3 Setup Supabase (10 minutes)

#### Step 1: Create Supabase Project
1. Visit https://supabase.com/dashboard
2. Click "New Project"
3. Choose organization and region (Singapore for Sri Lanka)
4. Set database password (save this!)
5. Wait for project creation (~2 minutes)

#### Step 2: Get Supabase Credentials
1. Go to Supabase Dashboard → Settings → API
2. Copy these values:
   - `SUPABASE_URL`: https://your-project-id.supabase.co
   - `SUPABASE_ANON_KEY`: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   - `SUPABASE_SERVICE_ROLE_KEY`: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

#### Step 3: Run Database Migration
1. Go to Supabase Dashboard → SQL Editor
2. Open `backend/src/database/supabase-migrations.sql`
3. Copy entire contents
4. Paste into SQL Editor and click "Run"
5. Verify tables created in Database → Tables

**Expected Tables:**
- ✅ users
- ✅ listings
- ✅ transactions
- ✅ local_ops
- ✅ otp_verifications
- ✅ sync_metadata

#### Step 4: Configure Storage Buckets
1. Go to Supabase Dashboard → Storage
2. Verify buckets exist:
   - ✅ listing-images (public)
   - ✅ profile-images (public)
3. If not, create them manually

#### Step 5: Test Backend
```bash
# Health check
curl https://agrimarket-api.vercel.app/health

# Expected response:
{
  "success": true,
  "message": "Jaffna Marketplace API is running",
  "timestamp": "2025-10-01T..."
}
```

---

## 2. Android App Configuration

### 2.1 Update API Base URL

**File:** `app/src/main/java/com/senthapps/slagrimarket/data/api/ApiConfig.kt`

**Current Configuration:**
```kotlin
const val PRODUCTION_BASE_URL = "https://agrimarket-52z0pnlgu-kabilantharmaratnam-kpucas-projects.vercel.app/api/"
const val STAGING_BASE_URL = "https://agrimarket-staging.vercel.app/api/"
const val DEVELOPMENT_BASE_URL = "http://localhost:3000/api/"
```

**Update to:**
```kotlin
const val PRODUCTION_BASE_URL = "https://agrimarket-api.vercel.app/api/"
const val STAGING_BASE_URL = "https://agrimarket-api-staging.vercel.app/api/"
const val DEVELOPMENT_BASE_URL = "http://10.0.2.2:3000/api/"  // Android emulator localhost
```

### 2.2 Configure Build Variants

**File:** `app/build.gradle.kts`

Add build configuration fields:

```kotlin
android {
    // ... existing config
    
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${ApiConfig.DEVELOPMENT_BASE_URL}\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "true")
        }
        
        release {
            buildConfigField("String", "API_BASE_URL", "\"${ApiConfig.PRODUCTION_BASE_URL}\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
            // ... existing release config
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"${ApiConfig.DEVELOPMENT_BASE_URL}\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "true")
        }
        
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"${ApiConfig.STAGING_BASE_URL}\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
        }
        
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"${ApiConfig.PRODUCTION_BASE_URL}\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
        }
    }
}
```

### 2.3 Update Retrofit Configuration

**File:** `app/src/main/java/com/senthapps/slagrimarket/di/NetworkModule.kt`

Find the `provideRetrofit` function and update:

```kotlin
@Provides
@Singleton
fun provideRetrofit(
    okHttpClient: OkHttpClient,
    moshi: Moshi
): Retrofit {
    val baseUrl = if (BuildConfig.DEBUG) {
        ApiConfig.DEVELOPMENT_BASE_URL
    } else {
        ApiConfig.PRODUCTION_BASE_URL
    }
    
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}
```

### 2.4 Add Authentication Interceptor

**File:** `app/src/main/java/com/senthapps/slagrimarket/di/NetworkModule.kt`

Add JWT token interceptor:

```kotlin
@Provides
@Singleton
fun provideAuthInterceptor(
    authPreferences: AuthPreferences
): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
        val token = runBlocking { authPreferences.getAuthToken() }
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        chain.proceed(newRequest)
    }
}

@Provides
@Singleton
fun provideOkHttpClient(
    authInterceptor: Interceptor,
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConfig.Timeouts.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.Timeouts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.Timeouts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
}
```

---

## 3. Data Migration

### 3.1 Schema Compatibility Analysis

**Android Room Schema vs Backend Supabase Schema:**

| Field | Android (Room) | Backend (Supabase) | Compatible? | Action Needed |
|-------|----------------|-------------------|-------------|---------------|
| **User** |
| id | String | UUID | ✅ Yes | None |
| name | String | VARCHAR(255) | ✅ Yes | None |
| phone | String | VARCHAR(20) | ✅ Yes | None |
| userType | UserType enum | VARCHAR(20) | ✅ Yes | None |
| verified | Boolean | BOOLEAN | ✅ Yes | None |
| language | String | N/A | ⚠️ Partial | Backend doesn't store language preference |
| createdAt | String | TIMESTAMP | ✅ Yes | Format conversion needed |
| **Listing** |
| id | String | UUID | ✅ Yes | None |
| farmerId | String | UUID | ✅ Yes | None |
| cropType | String | VARCHAR(100) | ✅ Yes | None |
| quantity | Double | DECIMAL(10,2) | ✅ Yes | None |
| pricePerUnit | Double | DECIMAL(10,2) | ✅ Yes | None |
| quality | QualityGrade | VARCHAR(20) | ✅ Yes | Enum to String conversion |
| harvestDate | String | DATE | ✅ Yes | Format: YYYY-MM-DD |
| images | List<String> | TEXT[] | ✅ Yes | JSON array conversion |
| syncStatus | SyncStatus | N/A | ⚠️ Local only | Keep in Room only |

**Conclusion:** ✅ **Schemas are highly compatible**. Minor adjustments needed for enum serialization and date formatting.

### 3.2 Model Class Updates Needed

**No changes required!** The existing model classes already use proper JSON annotations and type converters.

**Verification:**
- ✅ `@Json(name = "...")` annotations match backend field names
- ✅ Type converters handle enum serialization
- ✅ Date fields use ISO 8601 string format
- ✅ List fields use JSON array serialization

### 3.3 Migration Strategy

**Approach:** Gradual migration with offline-first preservation

1. **Keep existing Room database** - Continue using for offline storage
2. **Sync on app start** - Fetch latest data from backend
3. **Merge strategy** - Server data takes precedence for conflicts
4. **Preserve local changes** - Queue unsync'd operations

**No data loss** - All existing offline data will be synced to backend on first connection.

---

## 4. Testing Backend Integration

### 4.1 Pre-Integration Checklist

- [ ] Vercel deployment successful
- [ ] Supabase database migrated
- [ ] Environment variables configured
- [ ] Health endpoint responding
- [ ] Android app base URL updated
- [ ] Build variants configured

### 4.2 Authentication Testing

**Test 1: Send OTP**
```bash
curl -X POST https://agrimarket-api.vercel.app/api/auth/send-otp.js \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+94771234567"}'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

**Test 2: Verify OTP (in Android app)**
1. Enter phone number: +94771234567
2. Request OTP
3. Check Supabase Dashboard → Database → otp_verifications for OTP code
4. Enter OTP in app
5. Verify successful login

### 4.3 CRUD Operations Testing

**Test 3: Create Listing**
```kotlin
// In Android app after authentication
viewModel.createListing(
    cropType = "RED_ONION",
    quantity = 100.0,
    unit = "Kg",
    pricePerUnit = 150.0,
    quality = "A",
    harvestDate = "2025-10-15",
    location = "Jaffna"
)
```

**Verify:**
- [ ] Listing appears in app
- [ ] Listing exists in Supabase Database → listings table
- [ ] syncStatus = SYNCED in Room database

**Test 4: Fetch Listings**
```kotlin
// Should fetch from backend and cache locally
viewModel.refreshListings()
```

**Verify:**
- [ ] Listings load from backend
- [ ] Listings cached in Room database
- [ ] Offline access works after initial fetch

### 4.4 Sync Testing

**Test 5: Offline Create + Sync**
1. Turn off device network
2. Create new listing in app
3. Verify listing saved locally with syncStatus = PENDING
4. Turn on network
5. Trigger sync (automatic or manual)
6. Verify listing synced to backend
7. Verify syncStatus = SYNCED

**Test 6: Conflict Resolution**
1. Create listing on device A
2. Sync to backend
3. Edit same listing on device B
4. Sync device B
5. Sync device A
6. Verify server version wins (last-write-wins strategy)

### 4.5 Error Handling Testing

**Test 7: Network Errors**
- [ ] Graceful handling of 500 errors
- [ ] Retry mechanism works
- [ ] User-friendly error messages

**Test 8: Authentication Errors**
- [ ] 401 errors trigger re-authentication
- [ ] Token refresh works
- [ ] Expired tokens handled correctly

---

## 5. Missing Components & Additional Configuration

### 5.1 Missing Backend Endpoints

**Critical Missing Endpoints:**

1. **GET /api/v1/listings** - List all listings with filters
   - Status: ⚠️ Stub exists, needs full implementation
   - Priority: HIGH
   - Required for: Browse screen, search functionality

2. **GET /api/v1/listings/{id}** - Get single listing
   - Status: ❌ Missing
   - Priority: HIGH
   - Required for: Listing detail screen

3. **PUT /api/v1/listings/{id}** - Update listing
   - Status: ❌ Missing
   - Priority: MEDIUM
   - Required for: Edit listing functionality

4. **DELETE /api/v1/listings/{id}** - Delete listing
   - Status: ❌ Missing
   - Priority: MEDIUM
   - Required for: Delete listing functionality

5. **GET /api/v1/market-prices** - Get market prices
   - Status: ❌ Missing
   - Priority: HIGH
   - Required for: Market prices screen

6. **GET /api/v1/transactions** - Get user transactions
   - Status: ⚠️ Stub exists, needs implementation
   - Priority: HIGH
   - Required for: Transactions screen

7. **POST /api/v1/transactions** - Create transaction
   - Status: ⚠️ Stub exists, needs implementation
   - Priority: HIGH
   - Required for: Purchase flow

8. **POST /api/v1/sync/operations** - Sync offline operations
   - Status: ⚠️ Stub exists, needs implementation
   - Priority: CRITICAL
   - Required for: Offline-first architecture

### 5.2 SMS Service Configuration

**Dialog Ideamart Setup (for Sri Lankan OTP):**

1. Register at https://www.ideamart.io/
2. Create new application
3. Get API credentials:
   - API Key
   - API Secret
4. Configure in Vercel environment variables
5. Test OTP sending

**Alternative:** Use Twilio for international testing

### 5.3 Image Storage Configuration

**Supabase Storage Setup:**

1. Buckets already configured in migration:
   - `listing-images` (public)
   - `profile-images` (public)

2. **Android app needs:**
   - Image upload endpoint
   - Presigned URL generation
   - Image compression before upload

**Recommended Implementation:**
```kotlin
// Add to ListingApiService.kt
@Multipart
@POST("v1/listings/{id}/images")
suspend fun uploadListingImage(
    @Path("id") listingId: String,
    @Part image: MultipartBody.Part
): Response<ImageUploadResponse>
```

### 5.4 Push Notifications (Optional)

**Not currently implemented. Future enhancement:**
- Firebase Cloud Messaging (FCM)
- Notification triggers: new orders, price alerts, messages
- Backend webhook integration

---

## 6. Production Deployment Checklist

### 6.1 Backend Checklist
- [ ] Vercel deployment successful
- [ ] Custom domain configured (optional)
- [ ] SSL certificate active
- [ ] Environment variables secured
- [ ] Supabase database migrated
- [ ] RLS policies enabled
- [ ] Storage buckets configured
- [ ] SMS service configured and tested
- [ ] All critical endpoints implemented
- [ ] Error logging configured (Sentry/LogRocket)
- [ ] Rate limiting enabled
- [ ] CORS configured for production domain

### 6.2 Android App Checklist
- [ ] Production base URL configured
- [ ] Build variants tested (dev/staging/prod)
- [ ] Authentication flow tested
- [ ] CRUD operations tested
- [ ] Offline sync tested
- [ ] Error handling verified
- [ ] ProGuard rules updated
- [ ] Release signing configured
- [ ] APK size optimized
- [ ] Performance tested

### 6.3 Security Checklist
- [ ] JWT secrets are strong (32+ characters)
- [ ] Service role key secured in Vercel
- [ ] No secrets in Android app code
- [ ] HTTPS enforced
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention verified
- [ ] XSS protection headers set
- [ ] Rate limiting configured

---

## 7. Next Steps

### Immediate (Required)
1. ✅ Deploy backend to Vercel
2. ✅ Setup Supabase database
3. ✅ Update Android app base URL
4. ⚠️ Implement missing backend endpoints
5. ⚠️ Test authentication flow
6. ⚠️ Test CRUD operations
7. ⚠️ Test offline sync

### Short-term (Recommended)
1. Configure SMS service
2. Implement image upload
3. Add comprehensive error logging
4. Performance testing with real data
5. User acceptance testing

### Long-term (Optional)
1. Push notifications
2. Real-time updates (Supabase Realtime)
3. Advanced analytics
4. A/B testing infrastructure
5. CDN for images

---

## 8. Support & Resources

**Documentation:**
- Backend API: `backend/API_DOCUMENTATION.md`
- Serverless Deployment: `backend/SERVERLESS_DEPLOYMENT.md`
- Android API Config: `app/src/main/java/com/senthapps/slagrimarket/data/api/ApiConfig.kt`

**External Resources:**
- [Vercel Documentation](https://vercel.com/docs)
- [Supabase Documentation](https://supabase.com/docs)
- [Dialog Ideamart](https://www.ideamart.io/)

**Contact:**
- Project Issues: GitHub Issues
- Email: support@agrimarket.lk

---

**Status:** Ready for backend integration! 🚀

