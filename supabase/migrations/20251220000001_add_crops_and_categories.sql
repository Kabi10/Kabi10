-- Migration: 20251220000001_add_crops_and_categories.sql
-- Goal: Create a dynamic crops lookup table and migrate listings to reference it.

-- 1. Create crops table
CREATE TABLE IF NOT EXISTS crops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(50) UNIQUE NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_ta VARCHAR(100),
    name_si VARCHAR(100),
    category VARCHAR(50) DEFAULT 'VEGETABLE',
    image_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Enable RLS
ALTER TABLE crops ENABLE ROW LEVEL SECURITY;

-- 3. Public read access policy
CREATE POLICY "Anyone can view active crops" ON crops
    FOR SELECT USING (is_active = true);

-- 4. Initial crop data
INSERT INTO crops (slug, name_en, name_ta, name_si, category) VALUES
    ('rice', 'Rice', 'அரிசி', 'සහල්', 'GRAIN'),
    ('coconut', 'Coconut', 'தேங்காய்', 'පොල්', 'FRUIT'),
    ('banana', 'Banana', 'வாழைப்பழம்', 'කෙසෙල්', 'FRUIT'),
    ('mango', 'Mango', 'மாம்பழம்', 'අඹ', 'FRUIT'),
    ('papaya', 'Papaya', 'பப்பாளி', 'පැපොල්', 'FRUIT'),
    ('pineapple', 'Pineapple', 'அன்னாசி', 'අන්නාසි', 'FRUIT'),
    ('tomato', 'Tomato', 'தக்காளி', 'තක්කාලි', 'VEGETABLE'),
    ('onion', 'Onion', 'வெங்காயம்', 'ළූණු', 'VEGETABLE'),
    ('potato', 'Potato', 'உருளைக்கிழங்கு', 'අල', 'VEGETABLE'),
    ('carrot', 'Carrot', 'கேரட்', 'කැරට්', 'VEGETABLE'),
    ('cabbage', 'Cabbage', 'முட்டைக்கோஸ்', 'ගෝවා', 'VEGETABLE'),
    ('beans', 'Beans', 'பீன்ஸ்', 'බෝංචි', 'VEGETABLE'),
    ('okra', 'Okra', 'வெண்டைக்காய்', 'බණ්ඩක්කා', 'VEGETABLE'),
    ('eggplant', 'Eggplant', 'கத்தரிக்காய்', 'වම්බටු', 'VEGETABLE'),
    ('chili', 'Chili', 'மிளகாய்', 'මිරිස්', 'VEGETABLE'),
    ('curry_leaves', 'Curry Leaves', 'கறிவேப்பிலை', 'කරපිංචා', 'HERB'),
    ('coriander', 'Coriander', 'கொத்தமல்லி', 'කොත්තමල්ලි', 'HERB'),
    ('mint', 'Mint', 'புதினா', 'මින්ට්', 'HERB'),
    ('lemongrass', 'Lemongrass', 'எலுமிச்சை புல்', 'ලෙමන්ග්‍රාස්', 'HERB'),
    ('ginger', 'Ginger', 'இஞ்சி', 'ඉඟුරු', 'SPICE'),
    ('turmeric', 'Turmeric', 'மஞ்சள்', 'කහ', 'SPICE')
ON CONFLICT (slug) DO NOTHING;

-- 5. Note: Migration of listings.crop_type to reference crops.slug
-- Currently, we'll keep the column as VARCHAR to maintain backward compatibility with its content,
-- but we should eventually add a Foreign Key constraint.
-- For now, let's remove the hardcoded enum constraint from listings.
ALTER TABLE listings DROP CONSTRAINT IF EXISTS valid_crop_type;
