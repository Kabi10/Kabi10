# Jaffna Farmers Marketplace - Serverless Implementation

## 🚀 Overview

This document describes the serverless architecture implementation of the Jaffna Farmers Marketplace backend using **Vercel** and **Supabase**. This is an alternative deployment option to the traditional Node.js backend, designed for cost optimization and scalability.

## 🏗️ Architecture

### Technology Stack
- **Vercel**: Serverless functions platform
- **Supabase**: Backend-as-a-Service (PostgreSQL + Auth + Storage)
- **Node.js 18**: Runtime environment
- **JWT**: Authentication tokens
- **Dialog Ideamart**: SMS service for Sri Lankan OTP

### Key Benefits
- **Cost Effective**: Pay-per-execution model
- **Auto Scaling**: Handles traffic spikes automatically
- **Global CDN**: Fast response times worldwide
- **Zero Maintenance**: No server management required
- **Tamil-First**: Maintains all original features

## 📁 Project Structure

```
backend/
├── api/                          # Vercel serverless functions
│   ├── auth/
│   │   ├── send-otp.js          # POST /api/v1/auth/send-otp
│   │   ├── verify-otp.js        # POST /api/v1/auth/verify-otp
│   │   └── refresh-token.js     # POST /api/v1/auth/refresh-token
│   ├── listings/
│   │   ├── index.js             # GET /api/v1/listings
│   │   └── create.js            # POST /api/v1/listings/create
│   ├── sync/
│   │   └── operations.js        # POST /api/v1/sync/operations
│   └── health.js                # GET /health
├── src/
│   ├── config/
│   │   └── supabase.js          # Supabase client configuration
│   ├── middleware/
│   │   └── auth.js              # JWT authentication for serverless
│   ├── services/
│   │   ├── smsService.js        # SMS integration
│   │   └── database.js          # Database operations
│   ├── utils/
│   │   ├── helpers.js           # Utility functions
│   │   └── logger.js            # Logging service
│   └── database/
│       └── supabase-migrations.sql  # Database schema
├── vercel.json                  # Vercel deployment configuration
└── package.json                 # Dependencies
```

## 🔧 Setup Instructions

### 1. Supabase Setup

1. **Create Supabase Project**
   ```bash
   # Visit https://supabase.com/dashboard
   # Create new project
   # Note down: Project URL, Anon Key, Service Role Key
   ```

2. **Run Database Migration**
   ```sql
   -- Copy and paste contents of src/database/supabase-migrations.sql
   -- into Supabase SQL Editor and execute
   ```

3. **Configure Row Level Security**
   ```sql
   -- RLS policies are included in the migration
   -- Verify they're applied correctly in Supabase dashboard
   ```

### 2. Vercel Setup

1. **Install Vercel CLI**
   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**
   ```bash
   vercel login
   ```

3. **Set Environment Variables**
   ```bash
   vercel env add SUPABASE_URL
   vercel env add SUPABASE_ANON_KEY
   vercel env add SUPABASE_SERVICE_ROLE_KEY
   vercel env add JWT_SECRET
   vercel env add JWT_REFRESH_SECRET
   vercel env add SMS_PROVIDER
   vercel env add DIALOG_API_KEY
   vercel env add DIALOG_API_SECRET
   ```

4. **Deploy**
   ```bash
   vercel --prod
   ```

### 3. SMS Provider Setup

Configure Dialog Ideamart for Sri Lankan SMS:

```bash
# Environment variables
SMS_PROVIDER=dialog
DIALOG_API_KEY=your_api_key
DIALOG_API_SECRET=your_api_secret
```

## 🔌 API Endpoints

All endpoints maintain exact compatibility with the original Node.js backend:

### Authentication
- `POST /api/v1/auth/send-otp` - Send OTP to phone number
- `POST /api/v1/auth/verify-otp` - Verify OTP and login
- `POST /api/v1/auth/refresh-token` - Refresh access token

### Listings
- `GET /api/v1/listings` - Get listings with filters
- `POST /api/v1/listings/create` - Create new listing

### Sync
- `POST /api/v1/sync/operations` - Sync offline operations

### Health
- `GET /health` - Health check endpoint

## 🔄 Migration from Node.js Backend

### Database Migration
1. Export data from PostgreSQL
2. Import into Supabase using the migration script
3. Verify data integrity

### API Compatibility
- All endpoints maintain the same request/response format
- JWT tokens remain compatible
- Android app requires no changes

### Environment Variables Mapping
```bash
# Node.js Backend → Serverless
DATABASE_URL → SUPABASE_URL + SUPABASE_SERVICE_ROLE_KEY
JWT_SECRET → JWT_SECRET (same)
SMS_* → SMS_* (same)
```

## 💰 Cost Analysis

### Vercel Costs (Free Tier)
- **Bandwidth**: 100GB/month (free)
- **Function Executions**: 100GB-hours/month (free)
- **Function Duration**: 10 seconds max
- **Estimated Cost**: $0/month for MVP

### Supabase Costs (Free Tier)
- **Database**: 500MB storage (free)
- **Bandwidth**: 2GB/month (free)
- **Auth Users**: 50,000 MAU (free)
- **Estimated Cost**: $0/month for MVP

### SMS Costs
- **Dialog Ideamart**: ~LKR 1.50 per SMS
- **Estimated**: $10/month for 1000 farmers

### Total Monthly Cost: ~$10/month (vs $25-50 for Node.js hosting)

## 🚀 Deployment Guide

### Production Deployment

1. **Prepare Environment**
   ```bash
   # Clone repository
   git clone <repository>
   cd backend
   npm install
   ```

2. **Configure Supabase**
   ```bash
   # Run migration in Supabase SQL Editor
   # Set up RLS policies
   # Configure storage buckets
   ```

3. **Deploy to Vercel**
   ```bash
   vercel --prod
   ```

4. **Test Deployment**
   ```bash
   curl https://your-app.vercel.app/health
   ```

### Environment Configuration

Create `.env.local` for development:
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
```

## 🔍 Monitoring & Debugging

### Vercel Dashboard
- Function logs and metrics
- Performance monitoring
- Error tracking

### Supabase Dashboard
- Database queries and performance
- Real-time subscriptions
- Storage usage

### Logging
```javascript
// All functions include comprehensive logging
logger.info('Operation completed', { userId, operation });
logger.error('Operation failed', { error, context });
```

## 🔒 Security Features

### Row Level Security (RLS)
- User data isolation
- Farmer-specific listing access
- Transaction privacy

### JWT Authentication
- Secure token validation
- User session management
- Role-based access control

### Input Validation
- Request payload validation
- SQL injection prevention
- XSS protection

## 📱 Android App Integration

### No Changes Required
- Same API endpoints
- Same request/response format
- Same authentication flow

### Configuration Update
```kotlin
// Update base URL in Android app
const val BASE_URL = "https://your-app.vercel.app/api/v1/"
```

## 🎯 Next Steps

1. **Complete Migration**: Move remaining endpoints to serverless
2. **Performance Testing**: Load test with realistic traffic
3. **Monitoring Setup**: Configure alerts and dashboards
4. **Backup Strategy**: Implement database backup procedures
5. **Scaling Plan**: Prepare for growth beyond free tiers

## 🆘 Troubleshooting

### Common Issues

1. **Function Timeout**
   - Optimize database queries
   - Use connection pooling
   - Break down large operations

2. **Cold Start Latency**
   - Keep functions warm with health checks
   - Optimize import statements
   - Use Vercel Edge Functions for critical paths

3. **Database Connection Limits**
   - Use Supabase connection pooling
   - Implement retry logic
   - Monitor connection usage

### Support Resources
- [Vercel Documentation](https://vercel.com/docs)
- [Supabase Documentation](https://supabase.com/docs)
- [Project Repository Issues](https://github.com/jaffna-marketplace/backend/issues)

---

**The Jaffna Farmers Marketplace serverless backend is now ready to serve the Tamil farming community with cost-effective, scalable infrastructure! 🌾**
