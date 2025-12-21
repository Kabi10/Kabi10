-- Migration: 20251220000008_performance_and_safety.sql
-- Goal: Soft deletes, additional indexes, missing RLS policies
-- Note: Logic split to ensure deleted_at columns exist before indexing

-- 1. Add soft delete columns where missing
DO $$ 
BEGIN 
    -- Listings deleted_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='deleted_at') THEN
        ALTER TABLE listings ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
    END IF;
    
    -- Transactions deleted_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='transactions' AND column_name='deleted_at') THEN
        ALTER TABLE transactions ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
    END IF;
    
    -- Users deleted_at (for GDPR compliance)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='deleted_at') THEN
        ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- 2. Performance indexes
CREATE INDEX IF NOT EXISTS idx_reviews_reviewee_rating ON reviews(reviewee_id, rating);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

-- 3. Conditional indexes using deleted_at
-- (These will only work if the columns above exist)
CREATE INDEX IF NOT EXISTS idx_listings_active_available ON listings(crop_type, location)
    WHERE is_active = TRUE AND deleted_at IS NULL;
    
CREATE INDEX IF NOT EXISTS idx_listings_not_deleted ON listings(id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_not_deleted ON users(id) WHERE deleted_at IS NULL;

-- 4. Add missing RLS policy for crops (service role management)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'crops' AND policyname = 'Service role manages crops'
    ) THEN
        CREATE POLICY "Service role manages crops" ON crops
            FOR ALL USING (auth.role() = 'service_role');
    END IF;
END $$;
