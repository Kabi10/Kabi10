-- Migration: Align reviews and notifications tables with backend schema
-- Date: 2026-02-14

-- Add review_type column to reviews table (distinguishes buyer vs farmer reviews)
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS review_type VARCHAR(20)
    CHECK (review_type IN ('BUYER_TO_FARMER', 'FARMER_TO_BUYER'));

-- Align notifications table columns with backend expectations
-- Backend routes use 'message' column, existing migration uses 'body'
-- Backend routes use 'related_id' column, existing migration uses 'payload'
-- Add the missing columns (keep existing ones for backwards compatibility)
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS message TEXT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS related_id UUID;

-- Backfill: copy body -> message for existing rows
UPDATE notifications SET message = body WHERE message IS NULL AND body IS NOT NULL;
