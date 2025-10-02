# Supabase Setup Guide - Jaffna Farmers Marketplace

## Current Status Check

Your API is deployed to Vercel, but we need to ensure Supabase is properly set up and connected.

## 🔍 What We Need to Verify

1. **Supabase Project Creation** - Is your Supabase project created?
2. **Database Schema** - Are the tables created?
3. **Environment Variables** - Are they correctly set in Vercel?
4. **API Integration** - Are the endpoints connecting to Supabase?

## 📋 Step-by-Step Supabase Setup

### Step 1: Create Supabase Project (5 minutes)

1. Go to [https://supabase.com](https://supabase.com)
2. Sign up/Login with your account
3. Click "New Project"
4. Fill in project details:
   - **Name**: `jaffna-farmers-marketplace`
   - **Database Password**: Create a strong password
   - **Region**: Choose closest to Sri Lanka (Singapore/Mumbai)
5. Wait for project creation (2-3 minutes)

### Step 2: Get Your Credentials (2 minutes)

Once your project is ready:

1. Go to **Settings** → **API**
2. Copy these values:
   - **Project URL** (looks like: `https://your-project-id.supabase.co`)
   - **anon public key** (starts with `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`)
   - **service_role secret key** (starts with `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`)

### Step 3: Verify Environment Variables in Vercel

Your environment variables are already set in Vercel. Let's verify they're correct:

```bash
# Check current environment variables
npx vercel env ls

# If you need to update any:
npx vercel env rm SUPABASE_URL production
npx vercel env add SUPABASE_URL production
# Enter your new Supabase URL
```

### Step 4: Create Database Schema (5 minutes)

1. In your Supabase dashboard, go to **SQL Editor**
2. Click **New Query**
3. Copy and paste the migration SQL (see below)
4. Click **Run** to execute

### Step 5: Configure Authentication (3 minutes)

1. Go to **Authentication** → **Settings**
2. Enable **Phone** authentication
3. Configure SMS provider (optional for development)
4. Set **Site URL** to your Vercel deployment URL

## 🗄️ Database Migration SQL

Run this in your Supabase SQL Editor:

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255),
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('FARMER', 'BUYER')),
    location VARCHAR(255),
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    profile_image_url TEXT,
    CONSTRAINT valid_phone_format CHECK (phone_number ~ '^\\+94[0-9]{9}$')
);

-- Listings table
CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    crop_type VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(20) NOT NULL,
    price_per_unit DECIMAL(10,2) NOT NULL CHECK (price_per_unit > 0),
    quality VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    pickup_locations TEXT[],
    available_from DATE NOT NULL,
    available_until DATE NOT NULL,
    description TEXT,
    images TEXT[],
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    views_count INTEGER DEFAULT 0,
    contact_count INTEGER DEFAULT 0
);

-- Transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    pickup_location VARCHAR(255) NOT NULL,
    pickup_date DATE NOT NULL,
    pickup_time TIME NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- OTP table for authentication
CREATE TABLE otp_codes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Sync operations table
CREATE TABLE sync_operations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_type VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id UUID NOT NULL,
    data JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for better performance
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_listings_farmer ON listings(farmer_id);
CREATE INDEX idx_listings_active ON listings(is_active, created_at DESC);
CREATE INDEX idx_transactions_listing ON transactions(listing_id);
CREATE INDEX idx_transactions_buyer ON transactions(buyer_id);
CREATE INDEX idx_otp_phone ON otp_codes(phone_number, expires_at);
CREATE INDEX idx_sync_user ON sync_operations(user_id, created_at DESC);

-- Enable Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE listings ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE otp_codes ENABLE ROW LEVEL SECURITY;
ALTER TABLE sync_operations ENABLE ROW LEVEL SECURITY;

-- RLS Policies (basic - can be refined later)
CREATE POLICY "Users can view their own data" ON users FOR ALL USING (auth.uid()::text = id::text);
CREATE POLICY "Public can view active listings" ON listings FOR SELECT USING (is_active = true);
CREATE POLICY "Farmers can manage their listings" ON listings FOR ALL USING (auth.uid()::text = farmer_id::text);
CREATE POLICY "Users can view their transactions" ON transactions FOR SELECT USING (auth.uid()::text = buyer_id::text OR auth.uid()::text = farmer_id::text);
```

## 🧪 Testing Your Setup

After completing the setup, test these endpoints:

### 1. Health Check (Should work)
```bash
curl https://agrimarket-4iylhtjpy-kabilantharmaratnam-kpucas-projects.vercel.app/health
```

### 2. Listings (Should return empty array initially)
```bash
curl https://agrimarket-4iylhtjpy-kabilantharmaratnam-kpucas-projects.vercel.app/api/listings
```

### 3. Request OTP (Test SMS integration)
```bash
curl -X POST https://agrimarket-4iylhtjpy-kabilantharmaratnam-kpucas-projects.vercel.app/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phone": "+94771234567"}'
```

## 🔧 Troubleshooting

### Common Issues:

1. **Environment Variables Not Set**
   ```bash
   npx vercel env ls
   # Verify all required variables are present
   ```

2. **Database Connection Failed**
   - Check Supabase project is active
   - Verify URL and keys are correct
   - Check database schema is created

3. **API Endpoints Returning Errors**
   - Check Vercel function logs
   - Verify Supabase RLS policies
   - Test database queries in Supabase SQL editor

### Quick Fixes:

```bash
# Redeploy after changes
npx vercel --prod

# Check logs
npx vercel logs https://your-deployment-url.vercel.app

# Test specific endpoint
curl -v https://your-deployment-url.vercel.app/api/listings
```

## 📱 Next Steps After Supabase Setup

1. **Update API Endpoints** - Replace test endpoints with real Supabase queries
2. **Test Authentication Flow** - Verify OTP system works
3. **Test CRUD Operations** - Create, read, update listings
4. **Configure File Storage** - Set up Supabase Storage for images
5. **Update Android App** - Point to production API

## 🎯 Priority Actions

1. ✅ **Create Supabase project** (if not done)
2. ✅ **Run database migration**
3. ✅ **Verify environment variables**
4. ✅ **Test API endpoints**
5. ✅ **Update Android app configuration**

Would you like me to help you with any specific step?