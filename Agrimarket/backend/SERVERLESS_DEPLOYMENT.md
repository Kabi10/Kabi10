# Serverless Deployment Guide - Jaffna Farmers Marketplace

## 🚀 Quick Start (15 minutes)

This guide will get your serverless backend running in production in under 15 minutes.

## 📋 Prerequisites

- Node.js 18+ installed
- Git repository access
- Vercel account (free)
- Supabase account (free)
- Dialog Ideamart account (for SMS)

## 🔧 Step 1: Supabase Setup (5 minutes)

### 1.1 Create Supabase Project
```bash
# Visit https://supabase.com/dashboard
# Click "New Project"
# Choose organization and region (Singapore for Sri Lanka)
# Set database password (save this!)
# Wait for project creation (~2 minutes)
```

### 1.2 Get Supabase Credentials
```bash
# In Supabase Dashboard → Settings → API
# Copy these values:
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 1.3 Run Database Migration
```sql
-- Go to Supabase Dashboard → SQL Editor
-- Copy entire contents of src/database/supabase-migrations.sql
-- Paste and click "Run"
-- Verify tables are created in Database → Tables
```

### 1.4 Configure Storage
```bash
# In Supabase Dashboard → Storage
# Verify buckets are created:
# - listing-images (public)
# - profile-images (public)
```

## 🌐 Step 2: Vercel Setup (5 minutes)

### 2.1 Install Vercel CLI
```bash
npm install -g vercel
vercel login
```

### 2.2 Clone and Prepare Project
```bash
git clone <your-repository>
cd backend
npm install
```

### 2.3 Configure Environment Variables
```bash
# Set production environment variables
vercel env add SUPABASE_URL production
# Paste your Supabase URL

vercel env add SUPABASE_ANON_KEY production
# Paste your anon key

vercel env add SUPABASE_SERVICE_ROLE_KEY production
# Paste your service role key

vercel env add JWT_SECRET production
# Generate: openssl rand -base64 32

vercel env add JWT_REFRESH_SECRET production
# Generate: openssl rand -base64 32

vercel env add SMS_PROVIDER production
# Enter: dialog

vercel env add DIALOG_API_KEY production
# Enter your Dialog API key

vercel env add DIALOG_API_SECRET production
# Enter your Dialog API secret
```

### 2.4 Deploy to Production
```bash
vercel --prod
# Follow prompts:
# - Link to existing project? No
# - Project name: jaffna-marketplace-api
# - Directory: ./
# - Override settings? No
```

## 📱 Step 3: SMS Setup (3 minutes)

### 3.1 Dialog Ideamart Registration
```bash
# Visit https://www.ideamart.io/
# Register for developer account
# Create new application
# Get API credentials
```

### 3.2 Test SMS Integration
```bash
# Use health endpoint to verify deployment
curl https://your-app.vercel.app/health

# Test OTP sending (replace with your phone)
curl -X POST https://your-app.vercel.app/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+94771234567"}'
```

## ✅ Step 4: Verification (2 minutes)

### 4.1 Test All Endpoints
```bash
# Health check
curl https://your-app.vercel.app/health

# Send OTP
curl -X POST https://your-app.vercel.app/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+94771234567"}'

# Verify OTP (replace with actual OTP)
curl -X POST https://your-app.vercel.app/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+94771234567", "otp": "123456"}'
```

### 4.2 Check Vercel Dashboard
```bash
# Visit https://vercel.com/dashboard
# Check function logs and metrics
# Verify all functions deployed successfully
```

### 4.3 Check Supabase Dashboard
```bash
# Visit your Supabase dashboard
# Check Database → Tables for data
# Check Auth → Users for test user
# Check Logs for any errors
```

## 🔧 Development Setup

### Local Development
```bash
# Clone repository
git clone <repository>
cd backend

# Install dependencies
npm install

# Create .env.local
cp .env.example .env.local
# Fill in your credentials

# Start development server
vercel dev
```

### Environment Variables (.env.local)
```bash
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
JWT_SECRET=your_jwt_secret
JWT_REFRESH_SECRET=your_refresh_secret
SMS_PROVIDER=dialog
DIALOG_API_KEY=your_dialog_key
DIALOG_API_SECRET=your_dialog_secret
NODE_ENV=development
MOCK_SMS=true
```

## 📱 Android App Configuration

### Update Base URL
```kotlin
// In Android app: app/src/main/java/com/jaffna/marketplace/network/ApiConfig.kt
object ApiConfig {
    const val BASE_URL = "https://your-app.vercel.app/api/v1/"
    // Replace your-app with your actual Vercel app name
}
```

### No Other Changes Required
- All API contracts remain the same
- Authentication flow unchanged
- Offline sync works identically

## 🔍 Monitoring & Debugging

### Vercel Monitoring
```bash
# View function logs
vercel logs --follow

# Check function metrics
# Visit Vercel Dashboard → Functions tab
```

### Supabase Monitoring
```bash
# Check database performance
# Visit Supabase Dashboard → Reports

# View real-time logs
# Visit Supabase Dashboard → Logs
```

### Common Issues & Solutions

#### Function Timeout
```bash
# Symptom: 504 Gateway Timeout
# Solution: Optimize database queries
# Check: Supabase connection pooling settings
```

#### Cold Start Latency
```bash
# Symptom: First request slow (800ms+)
# Solution: Implement warming strategy
# Add: Health check pings every 5 minutes
```

#### Database Connection Errors
```bash
# Symptom: "Connection refused" errors
# Solution: Check Supabase service role key
# Verify: RLS policies are correctly configured
```

## 🔄 CI/CD Pipeline

### GitHub Actions (Optional)
```yaml
# .github/workflows/deploy.yml
name: Deploy to Vercel
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
          vercel-args: '--prod'
```

## 📊 Performance Optimization

### Database Optimization
```sql
-- Add indexes for common queries
CREATE INDEX CONCURRENTLY idx_listings_search 
ON listings USING gin(to_tsvector('english', crop_type || ' ' || location));

-- Optimize RLS policies
-- Check query plans in Supabase Dashboard
```

### Function Optimization
```javascript
// Minimize cold start time
// Keep imports at top level
// Use connection pooling
// Implement caching where appropriate
```

## 🔒 Security Checklist

- ✅ RLS policies enabled on all tables
- ✅ Service role key secured in Vercel env vars
- ✅ JWT secrets are strong (32+ characters)
- ✅ CORS configured for your domain only
- ✅ Rate limiting enabled
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention
- ✅ XSS protection headers

## 🆘 Troubleshooting

### Quick Diagnostics
```bash
# Test health endpoint
curl https://your-app.vercel.app/health

# Check Vercel function logs
vercel logs --follow

# Check Supabase logs
# Visit Dashboard → Logs → API

# Test database connection
# Visit Dashboard → SQL Editor
# Run: SELECT NOW();
```

### Support Resources
- [Vercel Support](https://vercel.com/support)
- [Supabase Support](https://supabase.com/support)
- [Project Issues](https://github.com/jaffna-marketplace/backend/issues)

## 🎉 Success!

Your Jaffna Farmers Marketplace serverless backend is now live! 

**Next Steps:**
1. Update Android app with new API URL
2. Test with real farmers and buyers
3. Monitor performance and costs
4. Scale as needed

**Your API is available at:**
- Health: `https://your-app.vercel.app/health`
- Auth: `https://your-app.vercel.app/api/v1/auth/*`
- Listings: `https://your-app.vercel.app/api/v1/listings/*`
- Sync: `https://your-app.vercel.app/api/v1/sync/*`

---

**The Tamil farming community now has a cost-effective, scalable backend ready to serve their marketplace needs! 🌾**
