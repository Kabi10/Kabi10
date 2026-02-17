-- Activities table for Activity Feed feature
-- Mirrors Android ActivityApiService.kt requirements
-- Created: 2026-02-16

-- Activities table
CREATE TABLE IF NOT EXISTS activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

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
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    read_at TIMESTAMP WITH TIME ZONE,
    dismissed_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE,

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

-- Indexes for performance (optimized for common queries)
CREATE INDEX IF NOT EXISTS idx_activities_user_id ON activities(user_id);
CREATE INDEX IF NOT EXISTS idx_activities_type ON activities(activity_type);
CREATE INDEX IF NOT EXISTS idx_activities_status ON activities(status);
CREATE INDEX IF NOT EXISTS idx_activities_priority ON activities(priority);
CREATE INDEX IF NOT EXISTS idx_activities_unread ON activities(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX IF NOT EXISTS idx_activities_actionable ON activities(user_id, is_actionable) WHERE is_actionable = TRUE AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_activities_created_at ON activities(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activities_expires_at ON activities(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_activities_user_status ON activities(user_id, status, created_at DESC);

-- Composite index for common filtering patterns
CREATE INDEX IF NOT EXISTS idx_activities_user_filters ON activities(user_id, status, is_read, created_at DESC);

-- Trigger for updated_at timestamp
CREATE TRIGGER update_activities_updated_at
    BEFORE UPDATE ON activities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to clean up expired activities (optional - can be called via cron)
CREATE OR REPLACE FUNCTION cleanup_expired_activities()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM activities
    WHERE expires_at < NOW()
      AND status = 'ACTIVE';

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Comment for documentation
COMMENT ON TABLE activities IS 'Stores user activity feed entries for the Android app. Supports trilingual content (English, Tamil, Sinhala).';
COMMENT ON COLUMN activities.activity_type IS 'Type of activity - must match ActivityType enum in Android ActivityApiService.kt';
COMMENT ON COLUMN activities.priority IS 'Priority level: LOW, NORMAL, HIGH, URGENT - affects display order and notifications';
COMMENT ON COLUMN activities.status IS 'Activity lifecycle: ACTIVE (visible), DISMISSED (hidden but kept), ARCHIVED (historical)';
COMMENT ON COLUMN activities.is_actionable IS 'Whether the activity requires user action (e.g., "Review pending transaction")';
COMMENT ON COLUMN activities.metadata IS 'Flexible JSON storage for additional context (e.g., crop type, price change amount)';
