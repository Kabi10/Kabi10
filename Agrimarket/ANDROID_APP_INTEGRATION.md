# 📱 Android App Integration Guide

## ✅ Backend Deployment Complete

Your Agrimarket backend is now live and fully operational on Vercel!

**Production URL:** `https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app`

---

## 🔧 Quick Integration Steps

### Step 1: Update API Configuration

Edit the file: `app/src/main/java/com/senthapps/slagrimarket/data/api/ApiConfig.kt`

```kotlin
package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.BuildConfig

object ApiConfig {
    // Production Vercel URL
    const val PRODUCTION_BASE_URL = "https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app"
    
    // Use production URL for both dev and prod
    val BASE_URL = PRODUCTION_BASE_URL
    
    // API version
    const val API_VERSION = "v1"
    
    // Timeout settings
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}
```

### Step 2: Verify Retrofit Configuration

Ensure your Retrofit instance is configured correctly in `ApiModule.kt` or similar:

```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
}
```

### Step 3: Test the Integration

Build and run your app:

```bash
cd Agrimarket
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Testing Checklist

### Authentication Flow
- [ ] Send OTP to phone number
- [ ] Receive OTP in development mode
- [ ] Verify OTP and receive JWT token
- [ ] Token stored securely in app
- [ ] Refresh token functionality works

### Listings
- [ ] Fetch all active listings
- [ ] Filter listings by crop type
- [ ] Filter listings by location
- [ ] Create new listing (farmers only)
- [ ] View listing details
- [ ] Pagination works correctly

### Transactions
- [ ] View transaction history
- [ ] Create new purchase transaction
- [ ] Transaction status updates
- [ ] Buyer and farmer details displayed

### Offline Sync
- [ ] Operations queued when offline
- [ ] Sync executes when back online
- [ ] Conflicts handled properly
- [ ] Local data updated after sync

### UI/UX
- [ ] Loading states display correctly
- [ ] Error messages are user-friendly
- [ ] Trilingual support (English/Tamil/Sinhala) works
- [ ] Material Design 3 dark theme applied
- [ ] All screens responsive

---

## 📋 API Endpoint Reference

### Base URL
```
https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app
```

### Authentication Endpoints

#### Send OTP
```http
POST /api/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "+94771234567"
}

Response:
{
  "success": true,
  "message": "OTP sent successfully (development mode)",
  "otp": "123456",
  "phoneNumber": "+94771234567",
  "otpId": "uuid"
}
```

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+94771234567",
  "otp": "123456"
}

Response:
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "phoneNumber": "+94771234567",
    "userType": "FARMER",
    "isActive": true,
    "verified": true
  }
}
```

#### Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your_refresh_token"
}

Response:
{
  "success": true,
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token"
}
```

### Listings Endpoints

#### Get All Listings
```http
GET /api/listings
GET /api/listings?cropType=tomato&location=Jaffna&page=1&limit=20

Response:
{
  "success": true,
  "data": [...],
  "count": 5,
  "page": 1,
  "limit": 20,
  "totalPages": 1,
  "message": "Listings retrieved successfully"
}
```

#### Create Listing
```http
POST /api/listings/create
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "cropType": "tomato",
  "quantity": 100,
  "unit": "kg",
  "pricePerUnit": 250,
  "quality": "A",
  "location": "Jaffna",
  "pickupLocations": ["Jaffna Market", "Farm Gate"],
  "availableFrom": "2025-10-01",
  "availableUntil": "2025-10-15",
  "description": "Fresh organic tomatoes from Jaffna",
  "images": []
}

Response:
{
  "success": true,
  "data": {
    "id": "uuid",
    "farmerId": "uuid",
    "cropType": "tomato",
    ...
  },
  "message": "Listing created successfully"
}
```

### Transaction Endpoints

#### Get Transactions
```http
GET /api/transactions
Authorization: Bearer <your_jwt_token>

Response:
{
  "success": true,
  "data": [...],
  "count": 10,
  "page": 1,
  "limit": 20,
  "totalPages": 1
}
```

#### Create Transaction
```http
POST /api/transactions/create
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "listingId": "uuid",
  "quantity": 50,
  "totalAmount": 12500,
  "pickupLocation": "Jaffna Market",
  "pickupDate": "2025-10-05",
  "paymentMethod": "CASH",
  "notes": "Please call before delivery"
}

Response:
{
  "success": true,
  "data": {
    "id": "uuid",
    "listingId": "uuid",
    "buyerId": "uuid",
    "farmerId": "uuid",
    "quantity": 50,
    "totalAmount": 12500,
    "status": "PENDING",
    ...
  },
  "message": "Transaction created successfully"
}
```

### Sync Endpoint

#### Sync Operations
```http
POST /api/sync/operations
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "operations": [
    {
      "opId": "uuid",
      "type": "CREATE_LISTING",
      "payload": {
        "cropType": "onion",
        "quantity": 200,
        ...
      },
      "clientTs": "2025-10-01T10:00:00Z",
      "clientId": "uuid"
    }
  ],
  "lastSyncAt": "2025-10-01T00:00:00Z"
}

Response:
{
  "success": true,
  "appliedOps": ["uuid1", "uuid2"],
  "conflicts": [],
  "errors": [],
  "serverData": {
    "listings": [...]
  },
  "serverTimestamp": "2025-10-02T10:00:00Z"
}
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Network Timeout Errors
**Symptom:** Requests timeout after 10 seconds  
**Solution:** Increase timeout in OkHttp configuration:
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

#### 2. CORS Errors
**Symptom:** "CORS policy" errors in logs  
**Solution:** Backend already configured for CORS. Ensure you're using the correct URL.

#### 3. 401 Unauthorized
**Symptom:** Protected endpoints return 401  
**Solution:** 
- Verify JWT token is being sent in Authorization header
- Check token hasn't expired (refresh if needed)
- Ensure token format is `Bearer <token>`

#### 4. 404 Not Found
**Symptom:** Endpoints return 404  
**Solution:**
- Verify base URL is correct
- Check endpoint paths match API documentation
- Ensure no trailing slashes in URLs

#### 5. Empty Response Data
**Symptom:** API returns success but empty data array  
**Solution:**
- Database might be empty
- Check filters aren't too restrictive
- Verify RLS policies in Supabase

---

## 📊 Monitoring & Logs

### View Vercel Logs
```bash
npx vercel logs --follow
```

### View Supabase Logs
1. Visit: https://supabase.com/dashboard
2. Select your project
3. Go to Logs → API

### Android App Logs
```bash
adb logcat | grep -i "agrimarket\|retrofit\|okhttp"
```

---

## 🚀 Performance Tips

### 1. Implement Caching
Cache API responses to reduce network calls:
```kotlin
@GET("api/listings")
suspend fun getListings(
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 20
): Response<ListingsResponse>

// Cache in Room database
listingDao.insertListings(response.data)
```

### 2. Use Pagination
Always use pagination for large datasets:
```kotlin
val listings = listingApiService.getListings(
    page = currentPage,
    limit = 20
)
```

### 3. Optimize Images
If implementing image upload:
- Compress images before upload
- Use WebP format
- Resize to max 1024x1024

### 4. Background Sync
Use WorkManager for offline sync:
```kotlin
val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
    15, TimeUnit.MINUTES
).build()

WorkManager.getInstance(context).enqueue(syncWork)
```

---

## ✅ Final Checklist

Before releasing to users:

- [ ] API URL updated in `ApiConfig.kt`
- [ ] All endpoints tested and working
- [ ] Authentication flow complete
- [ ] Offline sync tested
- [ ] Error handling implemented
- [ ] Loading states added
- [ ] Trilingual support verified
- [ ] Dark theme applied
- [ ] Performance optimized
- [ ] Logs reviewed for errors
- [ ] User acceptance testing completed

---

## 📞 Support

### Backend Issues
- Check Vercel logs: `npx vercel logs`
- Review deployment: https://vercel.com/kabilantharmaratnam-kpucas-projects/agrimarket
- Test endpoints: `.\test-vercel-deployment.ps1`

### Database Issues
- Supabase Dashboard: https://supabase.com/dashboard
- Check RLS policies
- Verify data integrity

### App Issues
- Check Android logs: `adb logcat`
- Review Retrofit/OkHttp logs
- Test with Postman/curl first

---

**🎉 Your backend is ready! Start integrating and testing your Android app now!**

