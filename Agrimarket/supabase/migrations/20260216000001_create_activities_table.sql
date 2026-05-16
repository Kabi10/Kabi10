-- Migration: Create activities table for Activity Feed feature
-- Created: 2026-02-16
-- Purpose: Backend infrastructure for ActivityApiService.kt

-- Create activities table
CREATE TABLE IF NOT EXISTS public.activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,

    -- Activity content (trilingual support for Sri Lankan farmers)
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    title_tamil VARCHAR(255),
    title_sinhala VARCHAR(255),
    description TEXT,
    description_tamil TEXT,
    description_sinhala TEXT,

    -- Related entity (optional - link to listings, transactions, messages, etc.)
    related_entity_type VARCHAR(50),
    related_entity_id UUID,

    -- Activity metadata
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISMISSED', 'ARCHIVED')),
    is_read BOOLEAN DEFAULT FALSE,
    is_actionable BOOLEAN DEFAULT FALSE,

    -- Timestamps
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    read_at TIMESTAMPTZ,
    dismissed_at TIMESTAMPTZ,
    archived_at TIMESTAMPTZ,

    -- Metadata storage (flexible JSON for future extensibility)
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Constraints
    CONSTRAINT valid_activity_type CHECK (activity_type IN (
        'NEW_LISTING', 'LISTING_UPDATE', 'LISTING_EXPIRED', 'LISTING_SOLD',
        'NEW_TRANSACTION', 'TRANSACTION_UPDATE', 'TRANSACTION_COMPLETED', 'TRANSACTION_CANCELLED',
        'NEW_MESSAGE', 'MESSAGE_UNREAD',
        'NEW_REVIEW', 'REVIEW_RECEIVED',
        'PRICE_ALERT', 'PRICE_DROP', 'PRICE_SURGE',
        'SYSTEM_ANNOUNCEMENT', 'ACCOUNT_UPDATE', 'VERIFICATION_REQUIRED',
        'FAVORITE_AVAILABLE', 'FAVORITE_PRICE_DROP',
        'OTHER'
    ))
);

-- Indexes for performance (optimized for common query patterns)
CREATE INDEX idx_activities_user_id ON public.activities(user_id);
CREATE INDEX idx_activities_type ON public.activities(activity_type);
CREATE INDEX idx_activities_status ON public.activities(status);
CREATE INDEX idx_activities_priority ON public.activities(priority);
CREATE INDEX idx_activities_unread ON public.activities(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_activities_actionable ON public.activities(user_id, is_actionable) WHERE is_actionable = TRUE AND status = 'ACTIVE';
CREATE INDEX idx_activities_created_at ON public.activities(created_at DESC);
CREATE INDEX idx_activities_expires_at ON public.activities(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_activities_user_status ON public.activities(user_id, status, created_at DESC);
CREATE INDEX idx_activities_user_filters ON public.activities(user_id, status, is_read, created_at DESC);

-- Trigger for updated_at timestamp (reuse existing function)
CREATE TRIGGER update_activities_updated_at
    BEFORE UPDATE ON public.activities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to clean up expired activities (optional - can be called via pg_cron or manually)
CREATE OR REPLACE FUNCTION public.cleanup_expired_activities()
RETURNS INTEGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM public.activities
    WHERE expires_at < NOW()
      AND status = 'ACTIVE';

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$;

-- Row Level Security (RLS) policies
ALTER TABLE public.activities ENABLE ROW LEVEL SECURITY;

-- Policy: Users can view their own activities
CREATE POLICY "Users can view own activities"
    ON public.activities
    FOR SELECT
    USING (auth.uid() = user_id);

-- Policy: Users can insert their own activities
CREATE POLICY "Users can create own activities"
    ON public.activities
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Policy: Users can update their own activities
CREATE POLICY "Users can update own activities"
    ON public.activities
    FOR UPDATE
    USING (auth.uid() = user_id);

-- Policy: Users can delete their own activities
CREATE POLICY "Users can delete own activities"
    ON public.activities
    FOR DELETE
    USING (auth.uid() = user_id);

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.activities TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.activities TO service_role;

-- Comments for documentation
COMMENT ON TABLE public.activities IS 'Stores user activity feed entries for the Android app. Supports trilingual content (English, Tamil, Sinhala).';
COMMENT ON COLUMN public.activities.activity_type IS 'Type of activity - must match ActivityType enum in Android ActivityApiService.kt';
COMMENT ON COLUMN public.activities.priority IS 'Priority level: LOW, NORMAL, HIGH, URGENT - affects display order and notifications';
COMMENT ON COLUMN public.activities.status IS 'Activity lifecycle: ACTIVE (visible), DISMISSED (hidden but kept), ARCHIVED (historical)';
COMMENT ON COLUMN public.activities.is_actionable IS 'Whether the activity requires user action (e.g., "Review pending transaction")';
COMMENT ON COLUMN public.activities.metadata IS 'Flexible JSON storage for additional context (e.g., crop type, price change amount)';
