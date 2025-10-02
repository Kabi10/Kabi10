# 🗄️ Database Migration Instructions

## Your API is deployed and ready! 

**API URL**: `https://agrimarket-qwf850445-kabilantharmaratnam-kpucas-projects.vercel.app`

✅ **Health endpoint working**: `/health`  
❌ **Listings endpoint failing**: `/api/listings` (needs database tables)

## 🚀 Quick Migration Steps (5 minutes)

### Step 1: Open Supabase Dashboard
1. Go to: https://supabase.com/dashboard
2. Select your project: **jaffna-farmers-marketplace**
3. Click on **SQL Editor** in the left sidebar

### Step 2: Run Migration
1. Click **"New Query"** button
2. Copy ALL content from `supabase-migration.sql` file
3. Paste into the SQL editor
4. Click **"Run"** button (or press Ctrl+Enter)

### Step 3: Verify Migration
You should see success messages like:
```
NOTICE: Jaffna Farmers Marketplace database schema created successfully!
NOTICE: Sample data inserted for testing.
NOTICE: You can now test your API endpoints.
```

### Step 4: Test API
After migration, test your API:

```powershell
# Test listings endpoint
Invoke-RestMethod -Uri "https://agrimarket-qwf850445-kabilantharmaratnam-kpucas-projects.vercel.app/api/listings" -Method GET
```

Expected response:
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "crop_type": "tomato",
      "quantity": 100,
      "unit": "kg",
      "price_per_unit": 250,
      "quality": "A",
      "location": "Jaffna",
      "farmer": {
        "name": "Ravi Farmer",
        "phone_number": "+94771234567"
      }
    }
  ],
  "count": 2,
  "message": "Listings retrieved successfully"
}
```

## 🎯 After Migration Success

### Update Android App
In your `ApiConfig.kt`:
```kotlin
const val BASE_URL = "https://agrimarket-qwf850445-kabilantharmaratnam-kpucas-projects.vercel.app/"
```

### Available Endpoints
- `GET /health` - API health check ✅
- `GET /api/listings` - Get all listings ✅ (after migration)
- `POST /api/listings/create` - Create listing ✅
- `POST /api/auth/send-otp` - Send OTP ✅
- `POST /api/auth/verify-otp` - Verify OTP ✅
- `POST /api/transactions/create` - Create transaction ✅

## 🔧 Troubleshooting

### If migration fails:
1. Check for syntax errors in the SQL
2. Make sure you copied the complete migration file
3. Try running sections of the migration separately

### If API still fails after migration:
1. Check Supabase logs in dashboard
2. Verify environment variables in Vercel
3. Redeploy: `npx vercel --prod`

## 📞 Test Complete Flow

1. **Register user**: `POST /api/auth/send-otp`
2. **Verify OTP**: `POST /api/auth/verify-otp`  
3. **Create listing**: `POST /api/listings/create`
4. **View listings**: `GET /api/listings`
5. **Create transaction**: `POST /api/transactions/create`

---

**Ready to run the migration? It takes just 2 minutes!** 🚀