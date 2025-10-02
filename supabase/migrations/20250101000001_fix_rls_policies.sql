-- Fix RLS Policies for Public Listing Access
-- Run this in Supabase SQL Editor to allow public read access to listings

-- Drop existing restrictive policies
DROP POLICY IF EXISTS "Users can view own listings" ON listings;
DROP POLICY IF EXISTS "Users can create own listings" ON listings;
DROP POLICY IF EXISTS "Users can update own listings" ON listings;
DROP POLICY IF EXISTS "Users can delete own listings" ON listings;

-- Create new policies that allow public read access

-- 1. Allow anyone to view active listings (public read)
DO $$ BEGIN
    CREATE POLICY "Anyone can view active listings" ON listings
        FOR SELECT
        USING (is_active = true);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 2. Allow service role to view all listings (for admin/backend)
DO $$ BEGIN
    CREATE POLICY "Service role can view all listings" ON listings
        FOR SELECT
        USING (auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 3. Allow authenticated users to create listings
DO $$ BEGIN
    CREATE POLICY "Authenticated users can create listings" ON listings
        FOR INSERT
        WITH CHECK (auth.role() = 'authenticated' OR auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 4. Allow users to update their own listings
DO $$ BEGIN
    CREATE POLICY "Users can update own listings" ON listings
        FOR UPDATE
        USING (auth.role() = 'service_role')
        WITH CHECK (auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 5. Allow users to delete their own listings
DO $$ BEGIN
    CREATE POLICY "Users can delete own listings" ON listings
        FOR DELETE
        USING (auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Fix users table policies for public profile viewing
DROP POLICY IF EXISTS "Users can view own profile" ON users;

DO $$ BEGIN
    CREATE POLICY "Anyone can view user profiles" ON users
        FOR SELECT
        USING (true);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE POLICY "Service role can manage users" ON users
        FOR ALL
        USING (auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Fix transactions policies
DROP POLICY IF EXISTS "Users can view own transactions" ON transactions;

DO $$ BEGIN
    CREATE POLICY "Service role can manage transactions" ON transactions
        FOR ALL
        USING (auth.role() = 'service_role');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Fix market_prices table if it exists
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE tablename = 'market_prices') THEN
        DROP POLICY IF EXISTS "Anyone can view market prices" ON market_prices;
        DROP POLICY IF EXISTS "Service role can manage market prices" ON market_prices;

        BEGIN
            CREATE POLICY "Anyone can view market prices" ON market_prices
                FOR SELECT
                USING (is_active = true);
        EXCEPTION WHEN duplicate_object THEN NULL;
        END;

        BEGIN
            CREATE POLICY "Service role can manage market prices" ON market_prices
                FOR ALL
                USING (auth.role() = 'service_role');
        EXCEPTION WHEN duplicate_object THEN NULL;
        END;
    END IF;
END $$;

-- Verify policies are applied
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual 
FROM pg_policies 
WHERE tablename IN ('listings', 'users', 'transactions', 'market_prices')
ORDER BY tablename, policyname;

