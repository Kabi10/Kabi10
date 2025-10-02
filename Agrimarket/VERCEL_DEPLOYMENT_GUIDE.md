# Vercel Deployment Guide - Jaffna Farmers Marketplace API

## Quick Deployment (Automated)

Run the automated deployment script:
```powershell
.\deploy-to-vercel.ps1
```

## Manual Deployment Steps

### Step 1: Install Vercel CLI (5 minutes)

```bash
# Install Vercel CLI globally
npm install -g vercel

# Login to Vercel (opens browser)
vercel login
```

### Step 2: Navigate to Backend Directory (1 minute)

```bash
cd backend
npm install
```

### Step 3: Configure Environment Variables (5 minutes)

Set these environment variables in Vercel:

```bash
# Core Configuration
vercel env add SUPABASE_URL production
# Enter: https://your-project-id.supabase.co

vercel env add SUPABASE_ANON_KEY production
# Enter: your_anon_key_from_supabase

vercel env add SUPABASE_SERVICE_ROLE_KEY production
# Enter: your_service_role_key_from_supabase

# JWT Secrets (generate with: openssl rand -base64 32)
vercel env add JWT_SECRET production
# Enter: generated_jwt_secret

vercel env add JWT_REFRESH_SECRET production
# Enter: generated_refresh_secret

# SMS Configuration
vercel env add SMS_PROVIDER production
# Enter: dialog

vercel env add DIALOG_API_KEY production
# Enter: your_dialog_api_key

vercel env add DIALOG_API_SECRET production
# Enter: your_dialog_api_secret

# Environment
vercel env add NODE_ENV production
# Enter: production
```

### Step 4: Deploy to Production (2 minutes)

```bash
# Deploy to production
vercel --prod

# Follow prompts:
# - Project name: agrimarket-api
# - Directory: ./
# - Override settings? No
```

### Expected Output

```
✅ Production: https://agrimarket-api-xxx.vercel.app [copied to clipboard]
```

## Verification Steps

### 1. Test Health Endpoint

```bash
curl https://your-deployment-url.vercel.app/health
```

Expected response:
```json
{
  "success": true,
  "message": "Jaffna Farmers Marketplace API is healthy",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "version": "2.0.0-serverless",
  "database": "ready",
  "environment": "production"
}
```

### 2. Test API Endpoints

```bash
# Test authentication endpoint
curl -X POST https://your-deployment-url.vercel.app/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phone": "+94771234567"}'

# Test listings endpoint
curl https://your-deployment-url.vercel.app/api/listings
```

## Troubleshooting

### Common Issues

1. **Environment Variables Not Set**
   - Solution: Use `vercel env ls` to check variables
   - Add missing variables with `vercel env add VARIABLE_NAME production`

2. **Build Failures**
   - Check `vercel logs` for detailed error information
   - Ensure all dependencies are in `package.json`

3. **Function Timeout**
   - Vercel functions have a 30-second timeout limit
   - Optimize database queries and external API calls

4. **CORS Issues**
   - Update CORS configuration in your API endpoints
   - Add your frontend domain to allowed origins

### Useful Commands

```bash
# View deployment logs
vercel logs

# List environment variables
vercel env ls

# Remove environment variable
vercel env rm VARIABLE_NAME production

# Redeploy
vercel --prod

# View project info
vercel inspect
```

## Post-Deployment Checklist

- [ ] Health endpoint responds correctly
- [ ] All API endpoints are accessible
- [ ] Environment variables are properly set
- [ ] SMS functionality works (test OTP)
- [ ] Database connections are working
- [ ] Update Android app API base URL
- [ ] Test complete user flow

## Production URL Structure

Your API will be available at:
- Base URL: `https://agrimarket-api-xxx.vercel.app`
- Health: `https://agrimarket-api-xxx.vercel.app/health`
- Auth: `https://agrimarket-api-xxx.vercel.app/api/auth/*`
- Listings: `https://agrimarket-api-xxx.vercel.app/api/listings/*`
- Transactions: `https://agrimarket-api-xxx.vercel.app/api/transactions/*`
- Sync: `https://agrimarket-api-xxx.vercel.app/api/sync/*`

## Next Steps

1. Update your Android app's `ApiConfig.kt` with the new production URL
2. Test the complete application flow
3. Monitor performance and logs in Vercel dashboard
4. Set up custom domain (optional)