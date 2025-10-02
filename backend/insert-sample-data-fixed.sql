-- Insert Sample Data for Testing
-- Run this in Supabase SQL Editor
-- This script uses proper UUIDs and handles foreign key constraints

-- Clear existing data (optional - comment out if you want to keep existing data)
-- TRUNCATE TABLE transactions, listings, users, otp_verifications, local_ops, sync_metadata RESTART IDENTITY CASCADE;

-- Insert sample users with specific UUIDs
INSERT INTO users (id, phone_number, name, user_type, location, verified, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', '+94771234567', 'Ravi Kumar', 'FARMER', 'Jaffna North', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', '+94771234568', 'Priya Sharma', 'BUYER', 'Jaffna Central', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', '+94771234569', 'Kumar Patel', 'FARMER', 'Jaffna South', true, NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', '+94771234570', 'Sita Devi', 'BUYER', 'Jaffna East', true, NOW(), NOW()),
    ('55555555-5555-5555-5555-555555555555', '+94771234571', 'Arjun Singh', 'FARMER', 'Jaffna West', true, NOW(), NOW())
ON CONFLICT (phone_number) DO UPDATE SET
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
    crop_name_tamil,
    crop_name_english,
    crop_name_sinhala,
    quantity,
    unit,
    price_per_unit,
    quality,
    harvest_date,
    available_from,
    available_until,
    location,
    location_tamil,
    location_sinhala,
    description,
    description_tamil,
    description_sinhala,
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
        'RED_ONION',
        'சிவப்பு வெங்காயம்',
        'Red Onion',
        'රතු ළූණු',
        500.00,
        'Kg',
        180.00,
        'A',
        '2025-09-15',
        '2025-09-20',
        '2025-10-15',
        'Jaffna North',
        'யாழ்ப்பாணம் வடக்கு',
        'යාපනය උතුර',
        'Fresh red onions, Grade A quality',
        'புதிய சிவப்பு வெங்காயம், A தர தரம்',
        'නැවුම් රතු ළූණු, A ශ්‍රේණිය',
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
        'TOMATO',
        'தக்காளி',
        'Tomato',
        'තක්කාලි',
        300.00,
        'Kg',
        120.00,
        'A',
        '2025-09-18',
        '2025-09-22',
        '2025-10-10',
        'Jaffna South',
        'யாழ்ப்பாணம் தெற்கு',
        'යාපනය දකුණ',
        'Organic tomatoes, freshly harvested',
        'இயற்கை தக்காளி, புதிதாக அறுவடை செய்யப்பட்டது',
        'කාබනික තක්කාලි, නැවුම් අස්වැන්න',
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
        'POTATO',
        'உருளைக்கிழங்கு',
        'Potato',
        'අල',
        800.00,
        'Kg',
        95.00,
        'B',
        '2025-09-10',
        '2025-09-15',
        '2025-10-20',
        'Jaffna West',
        'யாழ்ப்பாணம் மேற்கு',
        'යාපනය බටහිර',
        'Good quality potatoes, bulk available',
        'நல்ல தர உருளைக்கிழங்கு, மொத்தமாக கிடைக்கும்',
        'හොඳ තත්ත්වයේ අල, තොග ලබා ගත හැක',
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
        'CARROT',
        'கேரட்',
        'Carrot',
        'කැරට්',
        200.00,
        'Kg',
        110.00,
        'A',
        '2025-09-20',
        '2025-09-25',
        '2025-10-12',
        'Jaffna North',
        'யாழ்ப்பாணம் வடக்கு',
        'යාපනය උතුර',
        'Premium carrots, perfect for salads',
        'பிரீமியம் கேரட், சாலட்களுக்கு சரியானது',
        'ප්‍රිමියම් කැරට්, සලාද සඳහා සුදුසුයි',
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
        'CABBAGE',
        'முட்டைக்கோஸ்',
        'Cabbage',
        'ගෝවා',
        400.00,
        'Kg',
        75.00,
        'B',
        '2025-09-17',
        '2025-09-21',
        '2025-10-08',
        'Jaffna South',
        'யாழ்ப்பாணம் தெற்கு',
        'යාපනය දකුණ',
        'Fresh cabbage, good for cooking',
        'புதிய முட்டைக்கோஸ், சமைக்க நல்லது',
        'නැවුම් ගෝවා, ඉවුම් පිසීම සඳහා හොඳයි',
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
    unit,
    price_per_unit,
    total_amount,
    pickup_location,
    pickup_location_tamil,
    pickup_location_sinhala,
    pickup_date,
    pickup_time,
    status,
    payment_method,
    payment_status,
    notes,
    notes_tamil,
    notes_sinhala,
    created_at,
    updated_at
) VALUES
    (
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '11111111-1111-1111-1111-111111111111',
        '22222222-2222-2222-2222-222222222222',
        50.00,
        'Kg',
        180.00,
        9000.00,
        'Jaffna Market',
        'யாழ்ப்பாண சந்தை',
        'යාපනය වෙළඳපොළ',
        '2025-10-05',
        '10:00',
        'PENDING',
        'CASH',
        'PENDING',
        'Please pack carefully',
        'தயவுசெய்து கவனமாக பேக் செய்யவும்',
        'කරුණාකර ප්‍රවේශමෙන් ඇසුරුම් කරන්න',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        'gggggggg-gggg-gggg-gggg-gggggggggggg',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '33333333-3333-3333-3333-333333333333',
        '44444444-4444-4444-4444-444444444444',
        30.00,
        'Kg',
        120.00,
        3600.00,
        'Farm Gate',
        'பண்ணை வாயில்',
        'ගොවිපල ගේට්ටුව',
        '2025-10-03',
        '14:00',
        'CONFIRMED',
        'MOBILE_MONEY',
        'PAID',
        'Early morning pickup preferred',
        'காலை நேரத்தில் எடுப்பது விரும்பத்தக்கது',
        'උදෑසන පාන්දර ලබා ගැනීම වඩාත් සුදුසුයි',
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours'
    )
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    payment_status = EXCLUDED.payment_status,
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
SELECT id, quantity, total_amount, status, payment_status FROM transactions LIMIT 2;

