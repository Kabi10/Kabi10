const { createClient } = require('@supabase/supabase-js');
require('dotenv').config({ path: './backend/.env' }); // Try to load from backend env

// ⚠️ FILL THESE IN or set in .env if not loaded
const SUPABASE_URL = process.env.SUPABASE_URL || "YOUR_SUPABASE_URL";
const SERVICE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || "YOUR_SERVICE_ROLE_KEY";

if (SUPABASE_URL === "YOUR_SUPABASE_URL") {
    console.error("❌ Error: Please set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY in this script or .env file.");
    process.exit(1);
}

const supabase = createClient(SUPABASE_URL, SERVICE_KEY);

async function checkDatabase() {
  console.log(`🔍 Connecting to Supabase at: ${SUPABASE_URL}`);

  // 1. Check Users Table
  const { data: users, error: userError } = await supabase
    .from('users')
    .select('count')
    .limit(1);

  if (userError) {
    console.error("❌ Failed to query 'users' table:", userError.message);
  } else {
    console.log("✅ 'users' table accessible.");
  }

  // 2. Check Listings
  const { data: listings, error: listError } = await supabase
    .from('listings')
    .select('*')
    .limit(1);

  if (listError) {
    console.error("❌ Failed to query 'listings' table:", listError.message);
  } else {
    console.log(`✅ 'listings' table accessible. Found ${listings.length} rows.`);
  }
}

checkDatabase();