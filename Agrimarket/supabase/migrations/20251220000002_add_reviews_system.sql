-- Migration: 20251220000002_add_reviews_system.sql
-- Goal: Implement ratings and reviews for marketplace trust.

-- 1. Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Ensure a user can only review a transaction once
    UNIQUE(transaction_id, reviewer_id)
);

-- 2. Indexes for performance
CREATE INDEX IF NOT EXISTS idx_reviews_reviewee_id ON reviews(reviewee_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id);

-- 3. Enable RLS
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;

-- 4. RLS Policies
-- Anyone can view reviews (public trust signals)
DROP POLICY IF EXISTS "Anyone can view reviews" ON reviews;
CREATE POLICY "Anyone can view reviews" ON reviews
    FOR SELECT USING (true);

-- Authenticated users can insert reviews for transactions they are part of
-- Note: In a production environment, we'd also check if the transaction is COMPLETED.
DROP POLICY IF EXISTS "Users can review their own transactions" ON reviews;
CREATE POLICY "Users can review their own transactions" ON reviews
    FOR INSERT 
    WITH CHECK (
        auth.uid()::text = reviewer_id::text AND (
            EXISTS (
                SELECT 1 FROM transactions 
                WHERE id = transaction_id 
                AND (buyer_id::text = auth.uid()::text OR farmer_id::text = auth.uid()::text)
            )
        )
    );

-- Users can update/delete their own reviews
DROP POLICY IF EXISTS "Users can manage own reviews" ON reviews;
CREATE POLICY "Users can manage own reviews" ON reviews
    FOR ALL
    USING (auth.uid()::text = reviewer_id::text);

-- 5. Trigger for updated_at
DROP TRIGGER IF EXISTS update_reviews_updated_at ON reviews;
CREATE TRIGGER update_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
