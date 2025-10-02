-- Quick database verification query
-- Run this in Supabase SQL Editor to check if tables exist

-- Check if tables exist
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'public' 
    AND tablename IN ('users', 'listings', 'transactions', 'otp_verifications', 'local_ops')
ORDER BY tablename;

-- Check if we have any sample data
SELECT 'users' as table_name, count(*) as row_count FROM users
UNION ALL
SELECT 'listings' as table_name, count(*) as row_count FROM listings
UNION ALL
SELECT 'transactions' as table_name, count(*) as row_count FROM transactions;

-- Check if extensions are enabled
SELECT extname FROM pg_extension WHERE extname IN ('uuid-ossp', 'unaccent');