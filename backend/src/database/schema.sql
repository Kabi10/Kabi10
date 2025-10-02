-- Jaffna Farmers Marketplace Database Schema
-- Mirrors the Android Room database structure exactly

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable case-insensitive text search
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Users table (mirrors Android User entity)
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
    
    -- Indexes
    CONSTRAINT valid_phone_format CHECK (phone_number ~ '^\+94[0-9]{9}$')
);

-- Listings table (mirrors Android Listing entity)
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
    view_count INTEGER DEFAULT 0,
    inquiry_count INTEGER DEFAULT 0,
    client_id UUID, -- For sync purposes
    
    -- Constraints
    CONSTRAINT valid_date_range CHECK (available_until >= available_from),
    CONSTRAINT valid_crop_type CHECK (crop_type IN (
        'rice', 'coconut', 'banana', 'mango', 'papaya', 'pineapple',
        'tomato', 'onion', 'potato', 'carrot', 'cabbage', 'beans',
        'okra', 'eggplant', 'chili', 'curry_leaves', 'coriander',
        'mint', 'lemongrass', 'ginger', 'turmeric', 'other'
    )),
    CONSTRAINT valid_quality CHECK (quality IN ('A', 'B', 'C')),
    CONSTRAINT valid_unit CHECK (unit IN ('kg', 'g', 'lb', 'piece', 'bunch', 'bag'))
);

-- Transactions table (mirrors Android Transaction entity)
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    pickup_location VARCHAR(255) NOT NULL,
    pickup_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) DEFAULT 'CASH',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Additional fields for backend
    buyer_contact VARCHAR(20),
    farmer_contact VARCHAR(20),
    notes TEXT,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    client_id UUID, -- For sync purposes
    
    -- Constraints
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT valid_payment_method CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'MOBILE_PAYMENT')),
    CONSTRAINT valid_pickup_date CHECK (pickup_date >= CURRENT_DATE)
);

-- Local operations table (mirrors Android LocalOp entity)
CREATE TABLE local_ops (
    op_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    client_ts TIMESTAMP WITH TIME ZONE NOT NULL,
    server_ts TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    synced BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    client_id UUID,
    entity_id UUID,
    
    -- Constraints
    CONSTRAINT valid_op_type CHECK (type IN (
        'CREATE_LISTING', 'UPDATE_LISTING', 'DELETE_LISTING',
        'CREATE_TRANSACTION', 'UPDATE_TRANSACTION', 'UPDATE_USER'
    )),
    CONSTRAINT max_attempts CHECK (attempts <= 5)
);

-- OTP verification table
CREATE TABLE otp_verifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT valid_phone_format CHECK (phone_number ~ '^\+94[0-9]{9}$'),
    CONSTRAINT max_otp_attempts CHECK (attempts <= 3)
);

-- Sync metadata table
CREATE TABLE sync_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    last_sync_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    version INTEGER DEFAULT 1,
    checksum VARCHAR(64),
    
    UNIQUE(user_id, entity_type, entity_id)
);

-- Indexes for performance
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_location ON users(location);

CREATE INDEX idx_listings_farmer_id ON listings(farmer_id);
CREATE INDEX idx_listings_crop_type ON listings(crop_type);
CREATE INDEX idx_listings_location ON listings(location);
CREATE INDEX idx_listings_active ON listings(is_active);
CREATE INDEX idx_listings_available_dates ON listings(available_from, available_until);
CREATE INDEX idx_listings_created_at ON listings(created_at DESC);

CREATE INDEX idx_transactions_listing_id ON transactions(listing_id);
CREATE INDEX idx_transactions_farmer_id ON transactions(farmer_id);
CREATE INDEX idx_transactions_buyer_id ON transactions(buyer_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_pickup_date ON transactions(pickup_date);

CREATE INDEX idx_local_ops_user_id ON local_ops(user_id);
CREATE INDEX idx_local_ops_synced ON local_ops(synced);
CREATE INDEX idx_local_ops_type ON local_ops(type);
CREATE INDEX idx_local_ops_client_ts ON local_ops(client_ts);

CREATE INDEX idx_otp_phone_number ON otp_verifications(phone_number);
CREATE INDEX idx_otp_expires_at ON otp_verifications(expires_at);

CREATE INDEX idx_sync_metadata_user_entity ON sync_metadata(user_id, entity_type, entity_id);

-- Full-text search indexes
CREATE INDEX idx_listings_search ON listings USING gin(to_tsvector('english', crop_type || ' ' || location || ' ' || COALESCE(description, '')));

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_listings_updated_at BEFORE UPDATE ON listings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to clean up expired OTPs
CREATE OR REPLACE FUNCTION cleanup_expired_otps()
RETURNS void AS $$
BEGIN
    DELETE FROM otp_verifications WHERE expires_at < NOW();
END;
$$ LANGUAGE plpgsql;

-- Function to update listing view count
CREATE OR REPLACE FUNCTION increment_listing_views(listing_uuid UUID)
RETURNS void AS $$
BEGIN
    UPDATE listings SET view_count = view_count + 1 WHERE id = listing_uuid;
END;
$$ LANGUAGE plpgsql;
