-- Insert Sample Data for Testing
-- Run this in Supabase SQL Editor
-- This script uses proper UUIDs and handles foreign key constraints

-- Clear existing data (optional - comment out if you want to keep existing data)
-- TRUNCATE TABLE transactions, listings, users, otp_verifications, local_ops, sync_metadata RESTART IDENTITY CASCADE;

-- Insert sample users with specific UUIDs
-- Using DO UPDATE to ensure the UUIDs match what we need for foreign keys
INSERT INTO users (id, phone_number, name, user_type, location, verified, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', '+94771234567', 'Ravi Kumar', 'FARMER', 'Jaffna North', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', '+94771234568', 'Priya Sharma', 'BUYER', 'Jaffna Central', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', '+94771234569', 'Kumar Patel', 'FARMER', 'Jaffna South', true, NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', '+94771234570', 'Sita Devi', 'BUYER', 'Jaffna East', true, NOW(), NOW()),
    ('55555555-5555-5555-5555-555555555555', '+94771234571', 'Arjun Singh', 'FARMER', 'Jaffna West', true, NOW(), NOW())
ON CONFLICT (phone_number) DO UPDATE SET
    id = EXCLUDED.id,
    name = EXCLUDED.name,
    user_type = EXCLUDED.user_type,
    location = EXCLUDED.location,
    verified = EXCLUDED.verified,
    updated_at = NOW();

-- Insert sample listings
INSERT INTO listings (
    id,
    farmer_id,
    crop_type,
    quantity,
    unit,
    price_per_unit,
    quality,
    available_from,
    available_until,
    location,
    description,
    images,
    pickup_locations,
    is_active,
    view_count,
    inquiry_count,
    created_at,
    updated_at
) VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '11111111-1111-1111-1111-111111111111',
        'onion',
        500.00,
        'kg',
        180.00,
        'A',
        '2025-09-20',
        '2025-10-15',
        'Jaffna North',
        'Fresh red onions, Grade A quality',
        ARRAY[]::TEXT[],
        ARRAY['Jaffna Market', 'Farm Gate']::TEXT[],
        true,
        15,
        3,
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '33333333-3333-3333-3333-333333333333',
        'tomato',
        300.00,
        'kg',
        120.00,
        'A',
        '2025-09-22',
        '2025-10-10',
        'Jaffna South',
        'Organic tomatoes, freshly harvested',
        ARRAY[]::TEXT[],
        ARRAY['Jaffna Market', 'Delivery Available']::TEXT[],
        true,
        28,
        7,
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        '55555555-5555-5555-5555-555555555555',
        'potato',
        800.00,
        'kg',
        95.00,
        'B',
        '2025-09-15',
        '2025-10-20',
        'Jaffna West',
        'Good quality potatoes, bulk available',
        ARRAY[]::TEXT[],
        ARRAY['Farm Gate', 'Wholesale Market']::TEXT[],
        true,
        42,
        12,
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        '11111111-1111-1111-1111-111111111111',
        'carrot',
        200.00,
        'kg',
        110.00,
        'A',
        '2025-09-25',
        '2025-10-12',
        'Jaffna North',
        'Premium carrots, perfect for salads',
        ARRAY[]::TEXT[],
        ARRAY['Jaffna Market']::TEXT[],
        true,
        19,
        5,
        NOW() - INTERVAL '12 hours',
        NOW() - INTERVAL '12 hours'
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        '33333333-3333-3333-3333-333333333333',
        'cabbage',
        400.00,
        'kg',
        75.00,
        'B',
        '2025-09-21',
        '2025-10-08',
        'Jaffna South',
        'Fresh cabbage, good for cooking',
        ARRAY[]::TEXT[],
        ARRAY['Farm Gate', 'Jaffna Market']::TEXT[],
        true,
        31,
        8,
        NOW() - INTERVAL '6 hours',
        NOW() - INTERVAL '6 hours'
    )
ON CONFLICT (id) DO UPDATE SET
    quantity = EXCLUDED.quantity,
    price_per_unit = EXCLUDED.price_per_unit,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Insert sample transactions
INSERT INTO transactions (
    id,
    listing_id,
    farmer_id,
    buyer_id,
    quantity,
    total_amount,
    pickup_location,
    pickup_date,
    status,
    payment_method,
    notes,
    created_at,
    updated_at
) VALUES
    (
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '11111111-1111-1111-1111-111111111111',
        '22222222-2222-2222-2222-222222222222',
        50.00,
        9000.00,
        'Jaffna Market',
        '2025-10-05',
        'PENDING',
        'CASH',
        'Please pack carefully',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        '77777777-7777-7777-7777-777777777777',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '33333333-3333-3333-3333-333333333333',
        '44444444-4444-4444-4444-444444444444',
        30.00,
        3600.00,
        'Farm Gate',
        '2025-10-03',
        'CONFIRMED',
        'MOBILE_PAYMENT',
        'Early morning pickup preferred',
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours'
    )
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    updated_at = NOW();

-- Verify data inserted
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Listings', COUNT(*) FROM listings
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'Market Prices', COUNT(*) FROM market_prices;

-- Show sample data
SELECT 'Sample Users:' as info;
SELECT id, name, user_type, phone_number, location FROM users LIMIT 3;

SELECT 'Sample Listings:' as info;
SELECT id, crop_type, quantity, unit, price_per_unit, location, is_active FROM listings LIMIT 3;

SELECT 'Sample Transactions:' as info;
SELECT id, quantity, total_amount, status, payment_method FROM transactions LIMIT 2;

