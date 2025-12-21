-- Migration: 20251220000006_abuse_prevention.sql
-- Goal: Prevent marketplace abuse (reselling, price manipulation)

-- 1. Create user verifications table
CREATE TABLE IF NOT EXISTS user_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verification_type VARCHAR(50) NOT NULL DEFAULT 'FARMER', -- 'FARMER', 'BUSINESS', 'COOPERATIVE'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    documents JSONB, -- Store document URLs/metadata
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID REFERENCES users(id),
    rejection_reason TEXT,
    notes TEXT,
    
    UNIQUE(user_id, verification_type)
);

-- 2. Add verification status to users
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='is_verified_seller') THEN
        ALTER TABLE users ADD COLUMN is_verified_seller BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='trust_level') THEN
        ALTER TABLE users ADD COLUMN trust_level VARCHAR(20) DEFAULT 'NEW';
        -- Values: 'NEW', 'VERIFIED', 'TRUSTED', 'FLAGGED', 'SUSPENDED'
    END IF;
END $$;

-- 3. Add listing source tracking
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='source_type') THEN
        ALTER TABLE listings ADD COLUMN source_type VARCHAR(30) DEFAULT 'OWN_PRODUCTION';
        -- Values: 'OWN_PRODUCTION', 'COOPERATIVE', 'RESALE', 'OTHER'
    END IF;
END $$;

-- 4. Indexes for verification queries
CREATE INDEX IF NOT EXISTS idx_user_verifications_user_id ON user_verifications(user_id);
CREATE INDEX IF NOT EXISTS idx_user_verifications_status ON user_verifications(status);
CREATE INDEX IF NOT EXISTS idx_users_trust_level ON users(trust_level);

-- 5. Enable RLS on verifications
ALTER TABLE user_verifications ENABLE ROW LEVEL SECURITY;

-- Users can view their own verification status
CREATE POLICY "Users can view own verifications" ON user_verifications
    FOR SELECT USING (auth.uid()::text = user_id::text);

-- Users can submit verification requests
CREATE POLICY "Users can submit verifications" ON user_verifications
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

-- Service role can manage all verifications (for admin review)
CREATE POLICY "Service role manages verifications" ON user_verifications
    FOR ALL USING (auth.role() = 'service_role');

-- 6. Function to check if listing price is within acceptable range
CREATE OR REPLACE FUNCTION check_listing_price_ceiling()
RETURNS TRIGGER AS $$
DECLARE
    market_price DECIMAL(10,2);
    max_allowed_price DECIMAL(10,2);
BEGIN
    -- Get current market price for this crop type
    SELECT current_price INTO market_price
    FROM market_prices
    WHERE crop_type = NEW.crop_type
    AND is_active = TRUE
    ORDER BY last_updated DESC
    LIMIT 1;
    
    -- If we have a market price, check ceiling (30% above market)
    IF market_price IS NOT NULL THEN
        max_allowed_price := market_price * 1.30;
        
        IF NEW.price_per_unit > max_allowed_price THEN
            RAISE EXCEPTION 'Price exceeds maximum allowed (30%% above market price of %). Max: %', 
                market_price, max_allowed_price;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 7. Create trigger for price ceiling (disabled by default, enable when ready)
-- DROP TRIGGER IF EXISTS check_listing_price ON listings;
-- CREATE TRIGGER check_listing_price
--     BEFORE INSERT OR UPDATE ON listings
--     FOR EACH ROW
--     EXECUTE FUNCTION check_listing_price_ceiling();
-- NOTE: Uncomment above when ready to enforce price ceilings
