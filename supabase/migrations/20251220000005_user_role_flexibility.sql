-- Migration: 20251220000005_user_role_flexibility.sql
-- Goal: Allow flexible user roles while maintaining primary identity

-- 1. Add role capability columns to users
DO $$ 
BEGIN 
    -- can_sell: Farmers default TRUE, Buyers default FALSE
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='can_sell') THEN
        ALTER TABLE users ADD COLUMN can_sell BOOLEAN DEFAULT FALSE;
    END IF;
    
    -- can_buy: Everyone can buy by default
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='can_buy') THEN
        ALTER TABLE users ADD COLUMN can_buy BOOLEAN DEFAULT TRUE;
    END IF;
END $$;

-- 2. Set existing farmers to can_sell = TRUE
UPDATE users SET can_sell = TRUE WHERE user_type = 'FARMER' AND can_sell IS NULL;

-- 3. Ensure all users can buy
UPDATE users SET can_buy = TRUE WHERE can_buy IS NULL;
