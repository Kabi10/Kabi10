-- Migration: 20251220000007_favorites_and_engagement.sql
-- Goal: User favorites/wishlist and engagement features

-- 1. Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Each user can only favorite a listing once
    UNIQUE(user_id, listing_id)
);

-- 2. Indexes
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_listing_id ON favorites(listing_id);

-- 3. Enable RLS
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;

-- Users can manage their own favorites
CREATE POLICY "Users manage own favorites" ON favorites
    FOR ALL USING (auth.uid()::text = user_id::text);

-- Service role full access
CREATE POLICY "Service role manages favorites" ON favorites
    FOR ALL USING (auth.role() = 'service_role');

-- 4. Helper function: Get farmer average rating
CREATE OR REPLACE FUNCTION get_farmer_rating(farmer_uuid UUID)
RETURNS TABLE(average_rating DECIMAL(3,2), total_reviews INTEGER) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(ROUND(AVG(rating)::DECIMAL, 2), 0.00) as average_rating,
        COUNT(*)::INTEGER as total_reviews
    FROM reviews
    WHERE reviewee_id = farmer_uuid;
END;
$$ LANGUAGE plpgsql;

-- 5. Helper function: Get unread notification count
CREATE OR REPLACE FUNCTION get_unread_notification_count(user_uuid UUID)
RETURNS INTEGER AS $$
BEGIN
    RETURN (
        SELECT COUNT(*)::INTEGER
        FROM notifications
        WHERE user_id = user_uuid AND is_read = FALSE
    );
END;
$$ LANGUAGE plpgsql;
