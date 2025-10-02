-- Insert sample users first
INSERT INTO users (id, phone_number, name, user_type, location, verified) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '+94771234567', 'Ravi Farmer', 'FARMER', 'Jaffna', true),
    ('550e8400-e29b-41d4-a716-446655440002', '+94771234568', 'Priya Buyer', 'BUYER', 'Colombo', true),
    ('550e8400-e29b-41d4-a716-446655440003', '+94771234569', 'Kumar Farmer', 'FARMER', 'Vavuniya', true)
ON CONFLICT (phone_number) DO NOTHING;

SELECT 'Users inserted successfully!' as message;
SELECT count(*) as user_count FROM users;