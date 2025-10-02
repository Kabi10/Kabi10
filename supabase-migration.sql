-- Jaffna Farmers Marketplace Database Schema for Supabase
-- Run this in your Supabase SQL Editor

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable case-insensitive text search
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Users table (mirrors Android User entity exactly)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255),
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('FARMER', 'BUYER')),
    location VARCHAR(255),
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Additional fields for backend
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    profile_image_url TEXT,

    -- Constraints
    CONSTRAINT valid_phone_format CHECK (phone_number ~ '^\+94[0-9]{9}$')
);

-- Listings table (mirrors Android Listing entity exactly)
CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    crop_type VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(20) NOT NULL,
    price_per_unit DECIMAL(10,2) NOT NULL CHECK (price_per_unit > 0),
    quality VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    pickup_locations TEXT[], -- Array of pickup locations
    available_from DATE NOT NULL,
    available_until DATE NOT NULL,
    description TEXT,
    images TEXT[], -- Array of image URLs
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Additional fields for backend
    views_count INTEGER DEFAULT 0,
    contact_count INTEGER DEFAULT 0,

    -- Constraints
    CONSTRAINT valid_available_dates CHECK (available_until >= available_from),
    CONSTRAINT valid_crop_type CHECK (crop_type IN (
        'rice', 'coconut', 'banana', 'mango', 'papaya', 'pineapple',
        'tomato', 'onion', 'potato', 'carrot', 'cabbage', 'beans',
        'okra', 'eggplant', 'chili', 'curry_leaves', 'coriander',
        'mint', 'lemongrass', 'ginger', 'turmeric', 'other'
    )),
    CONSTRAINT valid_unit CHECK (unit IN ('kg', 'g', 'lb', 'piece', 'bunch', 'bag')),
    CONSTRAINT valid_quality CHECK (quality IN ('A', 'B', 'C'))
);

-- Transactions table (mirrors Android Transaction entity exactly)
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    pickup_location VARCHAR(255) NOT NULL,
    pickup_date DATE NOT NULL,
    pickup_time TIME NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Constraints
    CONSTRAINT valid_pickup_date CHECK (pickup_date >= CURRENT_DATE),
    CONSTRAINT different_buyer_farmer CHECK (buyer_id != farmer_id)
);

-- OTP table for authentication
CREATE TABLE otp_codes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Constraints
    CONSTRAINT valid_otp_phone CHECK (phone_number ~ '^\+94[0-9]{9}$'),
    CONSTRAINT valid_otp_code CHECK (code ~ '^[0-9]{6}$')
);

-- Sync operations table for offline sync
CREATE TABLE sync_operations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_type VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id UUID NOT NULL,
    data JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,

    -- Constraints
    CONSTRAINT valid_operation_type CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT valid_table_name CHECK (table_name IN ('users', 'listings', 'transactions'))
);

-- Create indexes for better performance
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_type ON users(user_type);
CREATE INDEX idx_users_active ON users(is_active);

CREATE INDEX idx_listings_farmer ON listings(farmer_id);
CREATE INDEX idx_listings_active ON listings(is_active, created_at DESC);
CREATE INDEX idx_listings_crop_type ON listings(crop_type);
CREATE INDEX idx_listings_location ON listings(location);
CREATE INDEX idx_listings_available ON listings(available_from, available_until);
CREATE INDEX idx_listings_price ON listings(price_per_unit);

CREATE INDEX idx_transactions_listing ON transactions(listing_id);
CREATE INDEX idx_transactions_buyer ON transactions(buyer_id);
CREATE INDEX idx_transactions_farmer ON transactions(farmer_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_pickup_date ON transactions(pickup_date);

CREATE INDEX idx_otp_phone ON otp_codes(phone_number, expires_at);
CREATE INDEX idx_otp_expires ON otp_codes(expires_at) WHERE used = FALSE;

CREATE INDEX idx_sync_user ON sync_operations(user_id, created_at DESC);
CREATE INDEX idx_sync_status ON sync_operations(status, created_at);

-- Enable Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE listings ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE otp_codes ENABLE ROW LEVEL SECURITY;
ALTER TABLE sync_operations ENABLE ROW LEVEL SECURITY;

-- RLS Policies

-- Users policies
CREATE POLICY "Users can view their own data" ON users 
    FOR ALL USING (auth.uid()::text = id::text);

CREATE POLICY "Public can view verified farmers" ON users 
    FOR SELECT USING (user_type = 'FARMER' AND verified = true);

-- Listings policies
CREATE POLICY "Public can view active listings" ON listings 
    FOR SELECT USING (is_active = true);

CREATE POLICY "Farmers can manage their listings" ON listings 
    FOR ALL USING (auth.uid()::text = farmer_id::text);

-- Transactions policies
CREATE POLICY "Users can view their transactions" ON transactions 
    FOR SELECT USING (auth.uid()::text = buyer_id::text OR auth.uid()::text = farmer_id::text);

CREATE POLICY "Buyers can create transactions" ON transactions 
    FOR INSERT WITH CHECK (auth.uid()::text = buyer_id::text);

CREATE POLICY "Farmers and buyers can update their transactions" ON transactions 
    FOR UPDATE USING (auth.uid()::text = buyer_id::text OR auth.uid()::text = farmer_id::text);

-- OTP policies (service role only)
CREATE POLICY "Service role can manage OTP" ON otp_codes 
    FOR ALL USING (auth.role() = 'service_role');

-- Sync operations policies
CREATE POLICY "Users can manage their sync operations" ON sync_operations 
    FOR ALL USING (auth.uid()::text = user_id::text);

-- Insert some sample data for testing
INSERT INTO users (id, phone_number, name, user_type, location, verified) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '+94771234567', 'Ravi Farmer', 'FARMER', 'Jaffna', true),
    ('550e8400-e29b-41d4-a716-446655440002', '+94771234568', 'Priya Buyer', 'BUYER', 'Colombo', true);

INSERT INTO listings (farmer_id, crop_type, quantity, unit, price_per_unit, quality, location, pickup_locations, available_from, available_until, description) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'tomato', 100.00, 'kg', 250.00, 'A', 'Jaffna', ARRAY['Jaffna Market', 'Farm Gate'], '2025-10-01', '2025-10-15', 'Fresh organic tomatoes from Jaffna'),
    ('550e8400-e29b-41d4-a716-446655440001', 'coconut', 500.00, 'piece', 45.00, 'A', 'Jaffna', ARRAY['Jaffna Market'], '2025-10-01', '2025-10-30', 'Fresh coconuts, perfect for cooking');

-- Create a function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_listings_updated_at BEFORE UPDATE ON listings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create a function to clean up expired OTP codes
CREATE OR REPLACE FUNCTION cleanup_expired_otp()
RETURNS void AS $$
BEGIN
    DELETE FROM otp_codes WHERE expires_at < NOW();
END;
$$ language 'plpgsql';

-- Success message
DO $$
BEGIN
    RAISE NOTICE 'Jaffna Farmers Marketplace database schema created successfully!';
    RAISE NOTICE 'Sample data inserted for testing.';
    RAISE NOTICE 'You can now test your API endpoints.';
END $$;