SET session_replication_role = replica;

--
-- PostgreSQL database dump
--

-- \restrict cKpj4RNWZvhrXH4DTRbSd705VlzfhA7w9tJQJCYNXS2fgZ3VQ8IBH05jXpnLNxn

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: audit_log_entries; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: flow_state; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: identities; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: instances; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sessions; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: mfa_amr_claims; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: mfa_factors; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: mfa_challenges; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: oauth_clients; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: one_time_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sso_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_providers; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: saml_relay_states; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: sso_domains; Type: TABLE DATA; Schema: auth; Owner: supabase_auth_admin
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."users" ("id", "phone_number", "name", "user_type", "location", "verified", "created_at", "updated_at", "last_login_at", "is_active", "profile_image_url") VALUES
	('11111111-1111-1111-1111-111111111111', '+94771234567', 'Ravi Kumar', 'FARMER', 'Jaffna North', true, '2025-09-28 17:33:58.811594+00', '2025-10-02 01:50:50.445986+00', '2025-09-29 02:43:34.836+00', true, NULL),
	('22222222-2222-2222-2222-222222222222', '+94771234568', 'Priya Sharma', 'BUYER', 'Jaffna Central', true, '2025-10-01 10:51:50.010269+00', '2025-10-02 01:50:50.445986+00', NULL, true, NULL),
	('33333333-3333-3333-3333-333333333333', '+94771234569', 'Kumar Patel', 'FARMER', 'Jaffna South', true, '2025-10-01 10:51:50.010269+00', '2025-10-02 01:50:50.445986+00', NULL, true, NULL),
	('44444444-4444-4444-4444-444444444444', '+94771234570', 'Sita Devi', 'BUYER', 'Jaffna East', true, '2025-10-02 01:50:50.445986+00', '2025-10-02 01:50:50.445986+00', NULL, true, NULL),
	('55555555-5555-5555-5555-555555555555', '+94771234571', 'Arjun Singh', 'FARMER', 'Jaffna West', true, '2025-10-02 01:50:50.445986+00', '2025-10-02 01:50:50.445986+00', NULL, true, NULL);


--
-- Data for Name: listings; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."listings" ("id", "farmer_id", "crop_type", "quantity", "unit", "price_per_unit", "quality", "location", "pickup_locations", "available_from", "available_until", "description", "images", "is_active", "created_at", "updated_at", "view_count", "inquiry_count", "client_id") VALUES
	('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'onion', 500.00, 'kg', 180.00, 'A', 'Jaffna North', '{"Jaffna Market","Farm Gate"}', '2025-09-20', '2025-10-15', 'Fresh red onions, Grade A quality', '{}', true, '2025-09-30 01:50:50.445986+00', '2025-09-30 01:50:50.445986+00', 15, 3, NULL),
	('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', 'tomato', 300.00, 'kg', 120.00, 'A', 'Jaffna South', '{"Jaffna Market","Delivery Available"}', '2025-09-22', '2025-10-10', 'Organic tomatoes, freshly harvested', '{}', true, '2025-10-01 01:50:50.445986+00', '2025-10-01 01:50:50.445986+00', 28, 7, NULL),
	('cccccccc-cccc-cccc-cccc-cccccccccccc', '55555555-5555-5555-5555-555555555555', 'potato', 800.00, 'kg', 95.00, 'B', 'Jaffna West', '{"Farm Gate","Wholesale Market"}', '2025-09-15', '2025-10-20', 'Good quality potatoes, bulk available', '{}', true, '2025-09-29 01:50:50.445986+00', '2025-09-29 01:50:50.445986+00', 42, 12, NULL),
	('dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'carrot', 200.00, 'kg', 110.00, 'A', 'Jaffna North', '{"Jaffna Market"}', '2025-09-25', '2025-10-12', 'Premium carrots, perfect for salads', '{}', true, '2025-10-01 13:50:50.445986+00', '2025-10-01 13:50:50.445986+00', 19, 5, NULL),
	('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '33333333-3333-3333-3333-333333333333', 'cabbage', 400.00, 'kg', 75.00, 'B', 'Jaffna South', '{"Farm Gate","Jaffna Market"}', '2025-09-21', '2025-10-08', 'Fresh cabbage, good for cooking', '{}', true, '2025-10-01 19:50:50.445986+00', '2025-10-01 19:50:50.445986+00', 31, 8, NULL);


--
-- Data for Name: local_ops; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: market_prices; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."market_prices" ("id", "crop_type", "crop_name_tamil", "crop_name_english", "crop_name_sinhala", "current_price", "previous_price", "unit", "trend", "change_percentage", "change_amount", "location", "location_tamil", "location_sinhala", "last_updated", "is_active", "source", "reliability", "created_at", "updated_at") VALUES
	('2f367723-ff15-4bf0-ad87-86244a9b315c', 'RED_ONION', 'சிவப்பு வெங்காயம்', 'Red Onion', 'රතු ළූණු', 180.00, 150.00, 'Kg', 'UP', 20.00, 30.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.90, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('a53994e3-5088-44d0-9207-fce02eefa5df', 'TOMATO', 'தக்காளி', 'Tomato', 'තක්කාලි', 120.00, 130.00, 'Kg', 'DOWN', -7.69, -10.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.85, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('b8064c1f-dc02-4fa6-9bac-f4c1ea7d3c11', 'POTATO', 'உருளைக்கிழங்கு', 'Potato', 'අල', 95.00, 95.00, 'Kg', 'STABLE', 0.00, 0.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.88, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('e645c1a4-b522-4739-99dc-c754e2745c98', 'CARROT', 'கேரட்', 'Carrot', 'කැරට්', 110.00, 100.00, 'Kg', 'UP', 10.00, 10.00, 'Jaffna', 'யாழ்ப்பாணம්', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.82, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('d3c8f2ee-80f4-4f8a-9d50-6766f40d5a52', 'CABBAGE', 'முட்டைக்கோஸ்', 'Cabbage', 'ගෝවා', 75.00, 80.00, 'Kg', 'DOWN', -6.25, -5.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.87, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('4e743f4f-707f-46c0-986a-7ebbfff2bbbe', 'BEANS', 'பீன்ஸ்', 'Beans', 'බෝංචි', 140.00, 135.00, 'Kg', 'UP', 3.70, 5.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.84, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('50216825-020c-462e-b0da-384cba28a665', 'EGGPLANT', 'கத்தரிக்காய்', 'Eggplant', 'වම්බටු', 85.00, 85.00, 'Kg', 'STABLE', 0.00, 0.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.86, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00'),
	('5151b4c7-c4ce-486b-aa19-432e1517319f', 'CHILI', 'மிளகாய்', 'Chili', 'මිරිස්', 450.00, 420.00, 'Kg', 'UP', 7.14, 30.00, 'Jaffna', 'யாழ்ப்பாணம்', 'යාපනය', '2025-10-01 12:24:24.645202+00', true, 'MARKET', 0.91, '2025-10-01 12:24:24.645202+00', '2025-10-01 12:24:24.645202+00');


--
-- Data for Name: otp_verifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."otp_verifications" ("id", "phone_number", "otp_code", "expires_at", "verified", "attempts", "created_at") VALUES
	('e58f2aa2-6269-4e24-b243-a419450b5830', '+94771234567', '834777', '2025-09-29 08:28:54.444+00', false, 0, '2025-09-29 08:18:55.134721+00'),
	('c4507e46-6831-43a3-b730-457495c24f4a', '+94762566376', '775461', '2025-09-29 15:41:44.669+00', false, 0, '2025-09-29 15:31:45.330943+00');


--
-- Data for Name: sync_metadata; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."transactions" ("id", "listing_id", "farmer_id", "buyer_id", "quantity", "total_amount", "pickup_location", "pickup_date", "status", "payment_method", "created_at", "updated_at", "buyer_contact", "farmer_contact", "notes", "completed_at", "cancelled_at", "cancellation_reason", "client_id") VALUES
	('ffffffff-ffff-ffff-ffff-ffffffffffff', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 50.00, 9000.00, 'Jaffna Market', '2025-10-05', 'PENDING', 'CASH', '2025-10-01 01:50:50.445986+00', '2025-10-01 01:50:50.445986+00', NULL, NULL, 'Please pack carefully', NULL, NULL, NULL, NULL),
	('77777777-7777-7777-7777-777777777777', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 30.00, 3600.00, 'Farm Gate', '2025-10-03', 'CONFIRMED', 'MOBILE_PAYMENT', '2025-10-01 23:50:50.445986+00', '2025-10-01 23:50:50.445986+00', NULL, NULL, 'Early morning pickup preferred', NULL, NULL, NULL, NULL);


--
-- Data for Name: buckets; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--

INSERT INTO "storage"."buckets" ("id", "name", "owner", "created_at", "updated_at", "public", "avif_autodetection", "file_size_limit", "allowed_mime_types", "owner_id") VALUES
	('listing-images', 'listing-images', NULL, '2025-09-28 10:09:01.812308+00', '2025-09-28 10:09:01.812308+00', true, false, NULL, NULL, NULL),
	('profile-images', 'profile-images', NULL, '2025-09-28 10:09:01.812308+00', '2025-09-28 10:09:01.812308+00', true, false, NULL, NULL, NULL);


--
-- Data for Name: objects; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: s3_multipart_uploads; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Data for Name: s3_multipart_uploads_parts; Type: TABLE DATA; Schema: storage; Owner: supabase_storage_admin
--



--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE SET; Schema: auth; Owner: supabase_auth_admin
--

SELECT pg_catalog.setval('"auth"."refresh_tokens_id_seq"', 1, false);


--
-- PostgreSQL database dump complete
--

-- \unrestrict cKpj4RNWZvhrXH4DTRbSd705VlzfhA7w9tJQJCYNXS2fgZ3VQ8IBH05jXpnLNxn

RESET ALL;
