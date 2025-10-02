# Backend Integration Checklist

Quick reference checklist for integrating the Agrimarket Android app with production backend.

---

## Phase 1: Backend Deployment (30 minutes)

### Supabase Setup
- [ ] Create Supabase project at https://supabase.com/dashboard
- [ ] Choose Singapore region (closest to Sri Lanka)
- [ ] Save database password securely
- [ ] Copy SUPABASE_URL from Settings → API
- [ ] Copy SUPABASE_ANON_KEY from Settings → API
- [ ] Copy SUPABASE_SERVICE_ROLE_KEY from Settings → API
- [ ] Go to SQL Editor
- [ ] Open `backend/src/database/supabase-migrations.sql`
- [ ] Copy entire file contents
- [ ] Paste into SQL Editor and click "Run"
- [ ] Verify tables created: users, listings, transactions, local_ops, otp_verifications, sync_metadata
- [ ] Go to Storage
- [ ] Verify buckets exist: listing-images, profile-images
- [ ] If not, create them manually as public buckets

### Vercel Setup
- [ ] Install Vercel CLI: `npm install -g vercel`
- [ ] Login: `vercel login`
- [ ] Navigate to backend: `cd backend`
- [ ] Install dependencies: `npm install`
- [ ] Add environment variables:
  ```bash
  vercel env add SUPABASE_URL production
  vercel env add SUPABASE_ANON_KEY production
  vercel env add SUPABASE_SERVICE_ROLE_KEY production
  vercel env add JWT_SECRET production  # Generate: openssl rand -base64 32
  vercel env add JWT_REFRESH_SECRET production  # Generate: openssl rand -base64 32
  vercel env add SMS_PROVIDER production  # Enter: dialog
  vercel env add DIALOG_API_KEY production
  vercel env add DIALOG_API_SECRET production
  vercel env add NODE_ENV production  # Enter: production
  ```
- [ ] Deploy: `vercel --prod`
- [ ] Save deployment URL (e.g., https://agrimarket-api.vercel.app)
- [ ] Test health endpoint: `curl https://agrimarket-api.vercel.app/health`

### Implement Missing Endpoints
- [ ] Create `backend/api/listings/index.js` (see MISSING_ENDPOINTS_IMPLEMENTATION.md)
- [ ] Create `backend/api/listings/[id].js`
- [ ] Create `backend/api/market-prices/index.js`
- [ ] Update `backend/api/transactions/index.js`
- [ ] Update `backend/api/transactions/create.js`
- [ ] Update `backend/api/sync/operations.js`
- [ ] Redeploy: `vercel --prod`
- [ ] Test all endpoints

---

## Phase 2: Android App Configuration (15 minutes)

### Update API Configuration
- [ ] Open `app/src/main/java/com/senthapps/slagrimarket/data/api/ApiConfig.kt`
- [ ] Update `PRODUCTION_BASE_URL` to your Vercel URL + `/api/`
- [ ] Example: `"https://agrimarket-api.vercel.app/api/"`
- [ ] Update `DEVELOPMENT_BASE_URL` to `"http://10.0.2.2:3000/api/"` for emulator

### Configure Build Variants
- [ ] Open `app/build.gradle.kts`
- [ ] Add buildConfigField for API_BASE_URL in debug and release
- [ ] Add productFlavors: dev, staging, prod
- [ ] Sync Gradle

### Add Authentication Interceptor
- [ ] Open `app/src/main/java/com/senthapps/slagrimarket/di/NetworkModule.kt`
- [ ] Add `provideAuthInterceptor` function
- [ ] Update `provideOkHttpClient` to include auth interceptor
- [ ] Verify token is added to Authorization header

### Update Retrofit Base URL
- [ ] In `NetworkModule.kt`, update `provideRetrofit`
- [ ] Use BuildConfig.API_BASE_URL or conditional based on BuildConfig.DEBUG
- [ ] Rebuild project

---

## Phase 3: Testing (30 minutes)

### Authentication Testing
- [ ] Build and install app: `./gradlew installDebug`
- [ ] Launch app
- [ ] Enter phone number: +94771234567
- [ ] Click "Send OTP"
- [ ] Check Supabase Dashboard → Database → otp_verifications for OTP code
- [ ] Enter OTP in app
- [ ] Verify successful login
- [ ] Check auth token saved in app preferences

### Listings CRUD Testing
- [ ] Navigate to Create Listing screen
- [ ] Fill in all fields:
  - Crop Type: RED_ONION
  - Quantity: 100
  - Unit: Kg
  - Price: 150
  - Quality: A
  - Harvest Date: Future date
  - Location: Jaffna
- [ ] Click "Create Listing"
- [ ] Verify success message
- [ ] Check Supabase Dashboard → Database → listings for new entry
- [ ] Verify listing appears in app's listings screen
- [ ] Verify syncStatus = SYNCED in Room database

### Offline Sync Testing
- [ ] Turn off device network (Airplane mode)
- [ ] Create new listing
- [ ] Verify listing saved locally
- [ ] Check Room database: syncStatus = PENDING
- [ ] Turn on network
- [ ] Wait for automatic sync or trigger manual refresh
- [ ] Verify listing synced to Supabase
- [ ] Verify syncStatus = SYNCED

### Market Prices Testing
- [ ] Navigate to Market Prices screen
- [ ] Verify prices load from backend
- [ ] Pull to refresh
- [ ] Verify updated prices
- [ ] Turn off network
- [ ] Verify cached prices still display

### Transactions Testing
- [ ] Navigate to Browse Listings
- [ ] Select a listing
- [ ] Click "Buy" or "Place Order"
- [ ] Fill in transaction details
- [ ] Submit transaction
- [ ] Verify transaction created in Supabase
- [ ] Navigate to Transactions screen
- [ ] Verify transaction appears

### Error Handling Testing
- [ ] Test with invalid phone number
- [ ] Test with wrong OTP
- [ ] Test with expired token
- [ ] Test with network errors
- [ ] Verify user-friendly error messages
- [ ] Verify retry mechanisms work

---

## Phase 4: Production Preparation (15 minutes)

### Security Checklist
- [ ] Verify JWT secrets are strong (32+ characters)
- [ ] Verify service role key not exposed in Android app
- [ ] Verify HTTPS enforced
- [ ] Verify RLS policies enabled in Supabase
- [ ] Verify rate limiting configured
- [ ] Verify input validation on all endpoints

### Performance Checklist
- [ ] Test with 100+ listings
- [ ] Verify pagination works
- [ ] Verify image loading optimized
- [ ] Verify offline mode responsive
- [ ] Verify sync doesn't block UI
- [ ] Check APK size (should be < 20MB)

### Release Build
- [ ] Update version code and version name
- [ ] Configure ProGuard rules
- [ ] Configure release signing
- [ ] Build release APK: `./gradlew assembleRelease`
- [ ] Test release APK on device
- [ ] Verify production API URL used
- [ ] Verify no debug logs in release

---

## Phase 5: Monitoring & Maintenance

### Setup Monitoring
- [ ] Configure Vercel function logs
- [ ] Setup error tracking (Sentry/LogRocket)
- [ ] Configure Supabase alerts
- [ ] Setup uptime monitoring
- [ ] Configure performance monitoring

### Documentation
- [ ] Update API documentation
- [ ] Document environment variables
- [ ] Document deployment process
- [ ] Create runbook for common issues
- [ ] Document rollback procedure

---

## Common Issues & Solutions

### Issue: "Network error" in app
**Solution:**
- Verify Vercel deployment successful
- Check Vercel function logs: `vercel logs --follow`
- Verify API base URL correct in app
- Test endpoint with curl

### Issue: "Unauthorized" errors
**Solution:**
- Verify JWT token saved in app
- Check token expiration
- Verify Authorization header sent
- Check Supabase RLS policies

### Issue: OTP not received
**Solution:**
- Verify Dialog API credentials
- Check Supabase otp_verifications table
- Verify phone number format (+94XXXXXXXXX)
- Check SMS provider logs

### Issue: Sync not working
**Solution:**
- Verify sync endpoint implemented
- Check local_ops table in Room
- Verify network connectivity
- Check WorkManager logs

### Issue: Images not uploading
**Solution:**
- Verify Supabase storage buckets exist
- Check storage policies
- Verify image size < 5MB
- Check multipart upload implementation

---

## Success Criteria

### Backend
- ✅ All endpoints responding
- ✅ Database tables populated
- ✅ Authentication working
- ✅ No errors in Vercel logs
- ✅ Response times < 500ms

### Android App
- ✅ Login successful
- ✅ Listings CRUD working
- ✅ Offline mode functional
- ✅ Sync working
- ✅ No crashes
- ✅ User-friendly errors

### Integration
- ✅ End-to-end flow working
- ✅ Data consistency maintained
- ✅ Performance acceptable
- ✅ Security verified
- ✅ Ready for production

---

## Next Steps After Integration

1. **User Acceptance Testing**
   - Beta test with 10-20 real farmers
   - Collect feedback
   - Fix critical issues

2. **Performance Optimization**
   - Optimize database queries
   - Add caching where appropriate
   - Optimize image loading

3. **Feature Enhancements**
   - Push notifications
   - Real-time updates
   - Advanced search
   - Chat/messaging

4. **Play Store Submission**
   - Prepare store listing
   - Create screenshots
   - Write description
   - Submit for review

---

**Estimated Total Time:** 90 minutes  
**Difficulty:** Medium  
**Prerequisites:** Vercel account, Supabase account, Dialog API credentials

**Status:** Ready to begin! 🚀

