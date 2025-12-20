-- Migration: 20251220000004_trilingual_listings.sql
-- Goal: Improve localization support for listings.

-- 1. Add description columns for Tamil and Sinhala
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='description_ta') THEN
        ALTER TABLE listings ADD COLUMN description_ta TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='description_si') THEN
        ALTER TABLE listings ADD COLUMN description_si TEXT;
    END IF;
END $$;

-- 2. Update search index to include all descriptions
DROP INDEX IF EXISTS idx_listings_search;
CREATE INDEX idx_listings_search ON listings USING gin(
    to_tsvector('english', 
        crop_type || ' ' || 
        location || ' ' || 
        COALESCE(description, '') || ' ' || 
        COALESCE(description_ta, '') || ' ' || 
        COALESCE(description_si, '')
    )
);
