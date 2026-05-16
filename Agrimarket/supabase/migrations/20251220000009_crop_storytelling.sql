-- Migration: 20251220000009_crop_storytelling.sql
-- Goal: Enhance crop upload with rich storytelling and authenticity markers

-- 1. Add storytelling columns to listings
DO $$ 
BEGIN 
    -- Narrative field for the story behind the crop
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='story') THEN
        ALTER TABLE listings ADD COLUMN story TEXT;
    END IF;

    -- Farming methods tags (e.g., 'Organic', 'Traditional', 'Hydroponic')
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='farming_methods') THEN
        ALTER TABLE listings ADD COLUMN farming_methods TEXT[];
    END IF;

    -- Certifications details (Stored as JSONB array: [{name, issuer, numeric_id, image_url}])
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='certifications') THEN
        ALTER TABLE listings ADD COLUMN certifications JSONB;
    END IF;

    -- Specific harvest date (distinct from available_from)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='harvested_at') THEN
        ALTER TABLE listings ADD COLUMN harvested_at DATE;
    END IF;
    
    -- Sustainability practices (e.g., 'Water conservation', 'No pesticides')
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='listings' AND column_name='sustainability_practices') THEN
        ALTER TABLE listings ADD COLUMN sustainability_practices TEXT[];
    END IF;
END $$;

-- 2. Update search index to include the story
-- Note: farming_methods (array) excluded from index to avoid IMMUTABLE errors with array_to_string
DROP INDEX IF EXISTS idx_listings_search;

CREATE INDEX idx_listings_search ON listings USING gin(
    to_tsvector('english', 
        crop_type || ' ' || 
        location || ' ' || 
        COALESCE(description, '') || ' ' || 
        COALESCE(story, '') || ' ' || 
        COALESCE(description_ta, '') || ' ' || 
        COALESCE(description_si, '')
    )
);
