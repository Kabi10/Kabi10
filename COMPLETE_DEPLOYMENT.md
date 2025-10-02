# Complete Backend Deployment - Final Steps

## 🎯 Current Status

Based on your DEPLOYMENT_SUMMARY.md, you have:
- ✅ Vercel deployed and configured
- ✅ Supabase database created with schema
- ✅ Environment variables set
- ⚠️ RLS policies blocking public access to listings
- ⚠️ Missing market_prices table
- ⚠️ Sample data needs to be inserted

**Completion: 85% → Target: 100%**

---

## 🚀 Step-by-Step Completion Guide

### Step 1: Fix RLS Policies (5 minutes)

**Problem:** Current RLS policies are too restrictive and block public access to listings.

**Solution:**

1. Go to Supabase Dashboard: https://supabase.com/dashboard/project/lxsbdluguyaaxzaeovwx
2. Click on "SQL Editor" in the left sidebar
3. Click "New Query"
4. Copy the entire contents of `backend/fix-rls-policies.sql`
5. Paste into the SQL Editor
6. Click "Run" (or press Ctrl+Enter)
7. Verify success message appears

**Expected Output:**
```
Success. No rows returned
```

**What this does:**
- Removes restrictive RLS policies
- Allows public read access to active listings
- Allows service role (backend) to perform all operations
- Enables public viewing of user profiles and market prices

---

### Step 2: Create Market Prices Table (3 minutes)

**Problem:** Market prices table doesn't exist yet.

**Solution:**

1. In Supabase SQL Editor, click "New Query"
2. Copy the entire contents of `backend/create-market-prices-table.sql`
3. Paste into the SQL Editor
4. Click "Run"
5. Verify table created and sample data inserted

**Expected Output:**
```
Success. 8 rows affected
market_price_count: 8
```

**What this does:**
- Creates market_prices table with all required fields
- Adds indexes for performance
- Enables RLS with public read access
- Inserts 8 sample market prices (Red Onion, Tomato, Potato, etc.)

---

### Step 3: Insert Sample Data (3 minutes)

**Problem:** Database needs sample users, listings, and transactions for testing.

**Solution:**

1. In Supabase SQL Editor, click "New Query"
2. Copy the entire contents of `backend/insert-sample-data-fixed.sql`
3. Paste into the SQL Editor
4. Click "Run"
5. Verify data inserted successfully

**Expected Output:**
```
Users: 5
Listings: 5
Transactions: 2
Market Prices: 8
```

**What this does:**
- Creates 5 sample users (3 farmers, 2 buyers)
- Creates 5 sample listings (Red Onion, Tomato, Potato, Carrot, Cabbage)
- Creates 2 sample transactions
- Uses proper UUIDs to avoid foreign key constraint errors

---

### Step 4: Verify Database Setup (2 minutes)

**Check that everything is working:**

1. In Supabase Dashboard, go to "Database" → "Tables"
2. Verify these tables exist and have data:
   - ✅ users (5 rows)
   - ✅ listings (5 rows)
   - ✅ transactions (2 rows)
   - ✅ market_prices (8 rows)
   - ✅ otp_verifications (0 rows - will populate on OTP requests)
   - ✅ local_ops (0 rows - will populate on sync)
   - ✅ sync_metadata (0 rows - will populate on sync)

3. Click on each table to verify data is present

---

### Step 5: Test API Endpoints (5 minutes)

**Test that the backend is working:**

#### Test 1: Health Check
```bash
curl https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/health
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Jaffna Marketplace API is running",
  "timestamp": "2025-10-01T..."
}
```

#### Test 2: Get Listings (This should now work!)
```bash
curl https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/api/listings
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "cropType": "RED_ONION",
      "quantity": 500,
      "pricePerUnit": 180,
      "location": "Jaffna North",
      ...
    },
    ...
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 5,
    "totalPages": 1
  }
}
```

#### Test 3: Get Market Prices
```bash
curl https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/api/v1/market-prices
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "cropType": "RED_ONION",
      "currentPrice": 180,
      "trend": "UP",
      "changePercentage": 20,
      ...
    },
    ...
  ]
}
```

#### Test 4: Send OTP (Optional - requires Dialog API credentials)
```bash
curl -X POST https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/api/auth/send-otp \
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

---

### Step 6: Update Android App Configuration (2 minutes)

**Update the Android app to use your production backend:**

1. Open `app/src/main/java/com/senthapps/slagrimarket/data/api/ApiConfig.kt`

2. Update the PRODUCTION_BASE_URL:
```kotlin
const val PRODUCTION_BASE_URL = "https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/api/"
```

3. Rebuild the app:
```bash
cd Agrimarket
./gradlew clean assembleDebug
```

4. Install on device/emulator:
```bash
./gradlew installDebug
```

---

## 🧪 Complete Integration Test

### Test the Full Flow:

1. **Launch Android App**
   - App should start successfully
   - No authentication required (demo mode)

2. **View Market Prices**
   - Navigate to Market Prices screen
   - Should see 8 market prices loaded from backend
   - Verify prices match what you inserted

3. **View Listings**
   - Navigate to Browse/Listings screen
   - Should see 5 listings loaded from backend
   - Verify listings display correctly

4. **Create New Listing** (if authenticated)
   - Navigate to Create Listing screen
   - Fill in all fields
   - Submit
   - Verify listing appears in Supabase database

5. **Offline Mode Test**
   - Turn off device network
   - Create a new listing
   - Verify it's saved locally (Room database)
   - Turn on network
   - Trigger sync
   - Verify listing synced to Supabase

---

## 🔧 Troubleshooting

### Issue: Listings endpoint still returns 500 error

**Solution:**
1. Check Vercel function logs:
   ```bash
   vercel logs --follow
   ```
2. Verify RLS policies were applied:
   ```sql
   SELECT * FROM pg_policies WHERE tablename = 'listings';
   ```
3. Ensure SUPABASE_SERVICE_ROLE_KEY is set in Vercel environment variables

### Issue: "relation 'market_prices' does not exist"

**Solution:**
1. Run `create-market-prices-table.sql` again
2. Verify table exists:
   ```sql
   SELECT * FROM pg_tables WHERE tablename = 'market_prices';
   ```

### Issue: Foreign key constraint errors when inserting data

**Solution:**
1. Ensure users are inserted before listings
2. Use the fixed sample data script: `insert-sample-data-fixed.sql`
3. Verify user IDs match in listings:
   ```sql
   SELECT id, name FROM users;
   SELECT id, farmer_id FROM listings;
   ```

### Issue: Android app shows "Network error"

**Solution:**
1. Verify API base URL is correct in ApiConfig.kt
2. Check device has internet connection
3. Test endpoint with curl to verify it's working
4. Check Android app logs for detailed error:
   ```bash
   adb logcat | grep -i "error\|exception"
   ```

---

## ✅ Success Criteria

After completing all steps, you should have:

- [x] RLS policies allow public read access
- [x] Market prices table created with sample data
- [x] Sample users, listings, and transactions inserted
- [x] GET /api/listings returns data successfully
- [x] GET /api/v1/market-prices returns data successfully
- [x] Health endpoint responding
- [x] Android app configured with production URL
- [x] Android app can fetch and display backend data

---

## 📊 Final Status

**Before:** 85% Complete
- ✅ Infrastructure deployed
- ⚠️ RLS policies blocking access
- ⚠️ Missing market prices table
- ⚠️ No sample data

**After:** 100% Complete
- ✅ Infrastructure deployed
- ✅ RLS policies configured correctly
- ✅ All tables created with sample data
- ✅ All endpoints working
- ✅ Android app integrated

---

## 🎉 Next Steps After Completion

### Immediate (Testing)
1. Test all CRUD operations from Android app
2. Test offline sync functionality
3. Verify error handling works correctly
4. Test with multiple users

### Short-term (Enhancements)
1. Implement image upload functionality
2. Configure SMS service for real OTP delivery
3. Add more sample data for realistic testing
4. Implement remaining endpoints (if any)

### Medium-term (Production Prep)
1. Setup custom domain for API
2. Configure monitoring and alerts
3. Implement rate limiting
4. Add comprehensive error logging
5. Performance optimization

### Long-term (Scaling)
1. CDN configuration for Sri Lanka
2. Database backup automation
3. Load testing and optimization
4. Advanced features (push notifications, real-time updates)

---

## 📞 Support

If you encounter any issues:

1. **Check Vercel Logs:**
   ```bash
   vercel logs --follow
   ```

2. **Check Supabase Logs:**
   - Go to Supabase Dashboard → Logs → API

3. **Verify Environment Variables:**
   ```bash
   vercel env ls
   ```

4. **Test Endpoints Manually:**
   - Use curl or Postman to test each endpoint
   - Check response status codes and error messages

---

**Estimated Time to Complete:** 20 minutes  
**Difficulty:** Easy (just running SQL scripts)  
**Status:** Ready to execute! 🚀

