-- Create Market Prices Table (if not exists)
-- Run this in Supabase SQL Editor

CREATE TABLE IF NOT EXISTS market_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crop_type VARCHAR(100) NOT NULL,
    crop_name_tamil VARCHAR(255),
    crop_name_english VARCHAR(255),
    crop_name_sinhala VARCHAR(255),
    current_price DECIMAL(10,2) NOT NULL CHECK (current_price >= 0),
    previous_price DECIMAL(10,2),
    unit VARCHAR(20) NOT NULL,
    trend VARCHAR(20) DEFAULT 'STABLE' CHECK (trend IN ('UP', 'DOWN', 'STABLE')),
    change_percentage DECIMAL(5,2) DEFAULT 0,
    change_amount DECIMAL(10,2) DEFAULT 0,
    location VARCHAR(255) NOT NULL,
    location_tamil VARCHAR(255),
    location_sinhala VARCHAR(255),
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    source VARCHAR(50) DEFAULT 'MARKET',
    reliability DECIMAL(3,2) DEFAULT 0.8 CHECK (reliability >= 0 AND reliability <= 1),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_market_prices_crop_type ON market_prices(crop_type);
CREATE INDEX IF NOT EXISTS idx_market_prices_location ON market_prices(location);
CREATE INDEX IF NOT EXISTS idx_market_prices_active ON market_prices(is_active);
CREATE INDEX IF NOT EXISTS idx_market_prices_updated ON market_prices(last_updated DESC);

-- Enable RLS
ALTER TABLE market_prices ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
DROP POLICY IF EXISTS "Anyone can view market prices" ON market_prices;
DROP POLICY IF EXISTS "Service role can manage market prices" ON market_prices;

CREATE POLICY "Anyone can view market prices" ON market_prices
    FOR SELECT
    USING (is_active = true);

CREATE POLICY "Service role can manage market prices" ON market_prices
    FOR ALL
    USING (auth.role() = 'service_role');

-- Create trigger for updated_at
DROP TRIGGER IF EXISTS update_market_prices_updated_at ON market_prices;

CREATE TRIGGER update_market_prices_updated_at 
    BEFORE UPDATE ON market_prices 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample market prices data
INSERT INTO market_prices (
    crop_type, 
    crop_name_tamil, 
    crop_name_english, 
    crop_name_sinhala,
    current_price, 
    previous_price, 
    unit, 
    trend, 
    change_percentage,
    change_amount,
    location,
    location_tamil,
    location_sinhala,
    source,
    reliability
) VALUES
    ('RED_ONION', 'சிவப்பு வெங்காயம்', 'Red Onion', 'රතු ළූණු', 180.00, 150.00, 'Kg', 'UP', 20.00, 30.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.9),
    ('TOMATO', 'தக்காளி', 'Tomato', 'තක්කාලි', 120.00, 130.00, 'Kg', 'DOWN', -7.69, -10.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.85),
    ('POTATO', 'உருளைக்கிழங்கு', 'Potato', 'අල', 95.00, 95.00, 'Kg', 'STABLE', 0.00, 0.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.88),
    ('CARROT', 'கேரட்', 'Carrot', 'කැරට්', 110.00, 100.00, 'Kg', 'UP', 10.00, 10.00, 'Jaffna', 'யாழ்ப்பாணம්', 'යාපනය', 'MARKET', 0.82),
    ('CABBAGE', 'முட்டைக்கோஸ்', 'Cabbage', 'ගෝවා', 75.00, 80.00, 'Kg', 'DOWN', -6.25, -5.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.87),
    ('BEANS', 'பீன்ஸ்', 'Beans', 'බෝංචි', 140.00, 135.00, 'Kg', 'UP', 3.70, 5.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.84),
    ('EGGPLANT', 'கத்தரிக்காய்', 'Eggplant', 'වම්බටු', 85.00, 85.00, 'Kg', 'STABLE', 0.00, 0.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.86),
    ('CHILI', 'மிளகாய்', 'Chili', 'මිරිස්', 450.00, 420.00, 'Kg', 'UP', 7.14, 30.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', 'MARKET', 0.91)
ON CONFLICT DO NOTHING;

-- Verify data inserted
SELECT COUNT(*) as market_price_count FROM market_prices;
SELECT * FROM market_prices ORDER BY crop_type LIMIT 5;

