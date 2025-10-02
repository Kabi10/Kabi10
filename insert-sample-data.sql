-- Insert sample data for testing
-- Run this in your Supabase SQL Editor

-- Insert sample users
INSERT INTO users (id, phone_number, name, user_type, location, verified) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '+94771234567', 'Ravi Farmer', 'FARMER', 'Jaffna', true),
    ('550e8400-e29b-41d4-a716-446655440002', '+94771234568', 'Priya Buyer', 'BUYER', 'Colombo', true),
    ('550e8400-e29b-41d4-a716-446655440003', '+94771234569', 'Kumar Farmer', 'FARMER', 'Vavuniya', true)
ON CONFLICT (phone_number) DO NOTHING;

-- Insert sample listings
INSERT INTO listings (
    farmer_id, 
    crop_type, 
    quantity, 
    unit, 
    price_per_unit, 
    quality, 
    location, 
    pickup_locations, 
    available_from, 
    available_until, 
    description
) VALUES
    (
        '550e8400-e29b-41d4-a716-446655440001', 
        'tomato', 
        100.00, 
        'kg', 
        250.00, 
        'A', 
        'Jaffna', 
        ARRAY['Jaffna Market', 'Farm Gate'], 
        CURRENT_DATE, 
        CURRENT_DATE + INTERVAL '15 days', 
        'Fresh organic tomatoes from Jaffna. High quality, pesticide-free.'
    ),
    (
        '550e8400-e29b-41d4-a716-446655440001', 
        'coconut', 
        500.00, 
        'piece', 
        45.00, 
        'A', 
        'Jaffna', 
        ARRAY['Jaffna Market'], 
        CURRENT_DATE, 
        CURRENT_DATE + INTERVAL '30 days', 
        'Fresh coconuts, perfect for cooking and drinking.'
    ),
    (
        '550e8400-e29b-41d4-a716-446655440003', 
        'rice', 
        1000.00, 
        'kg', 
        180.00, 
        'A', 
        'Vavuniya', 
        ARRAY['Vavuniya Market', 'Warehouse'], 
        CURRENT_DATE, 
        CURRENT_DATE + INTERVAL '60 days', 
        'Premium quality rice, freshly harvested.'
    ),
    (
        '550e8400-e29b-41d4-a716-446655440003', 
        'onion', 
        200.00, 
        'kg', 
        120.00, 
        'B', 
        'Vavuniya', 
        ARRAY['Vavuniya Market'], 
        CURRENT_DATE, 
        CURRENT_DATE + INTERVAL '20 days', 
        'Good quality onions for cooking.'
    );

-- Verify the data was inserted
SELECT 'Sample data inserted successfully!' as message;
SELECT 'Users:' as table_name, count(*) as count FROM users
UNION ALL
SELECT 'Listings:' as table_name, count(*) as count FROM listings;