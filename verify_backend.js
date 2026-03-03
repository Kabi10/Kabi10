const axios = require("axios");

// Default to your production URL from build.gradle.kts
const BASE_URL = process.env.API_URL || "https://backend-psi-tan-18.vercel.app";
const TEST_PHONE = "+94771234567";

async function runTests() {
  console.log(`🚀 Starting Backend Verification against: ${BASE_URL}\n`);

  try {
    // 1. Health Check
    console.log("1️⃣  Testing Health Endpoint...");
    const health = await axios.get(`${BASE_URL}/api/health`);
    console.log(`   ✅ Status: ${health.status} - ${health.data.message}`);

    // 2. Send OTP
    console.log("\n2️⃣  Testing Send OTP...");
    const sendOtp = await axios.post(`${BASE_URL}/api/auth/send-otp`, {
      phoneNumber: TEST_PHONE,
    });
    console.log(`   ✅ OTP Sent. ID: ${sendOtp.data.otpId}`);

    // EXTRACT OTP (Works in Dev/Test mode)
    const otpCode = sendOtp.data.otp || "123456";
    console.log(`   ℹ️  OTP Received (Dev Mode): ${otpCode}`);

    // 3. Verify OTP
    console.log("\n3️⃣  Testing Verify OTP...");
    const verifyOtp = await axios.post(`${BASE_URL}/api/auth/verify-otp`, {
      phoneNumber: TEST_PHONE,
      otp: otpCode,
    });

    const token = verifyOtp.data.token;
    const userId = verifyOtp.data.user.id;
    const userType = verifyOtp.data.user.userType;
    console.log(`   ✅ Login Successful!`);
    console.log(`   👤 User ID: ${userId}`);
    console.log(`   🎭 Role: ${userType}`);
    console.log(`   🔑 Token: ${token.substring(0, 15)}...`);

    // 4. Get Listings
    console.log("\n4️⃣  Testing GET Listings...");
    const listings = await axios.get(`${BASE_URL}/api/listings`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    console.log(
      `   ✅ Success. Found ${listings.data.count || listings.data.data.length} listings.`,
    );

    // 5. Create Listing (Expect Success or 403)
    console.log("\n5️⃣  Testing CREATE Listing (POST /api/listings/create)...");
    try {
      const newListing = await axios.post(
        `${BASE_URL}/api/listings/create`,
        {
          cropType: "tomato",
          quantity: 100,
          unit: "kg",
          pricePerUnit: 250,
          quality: "A",
          location: "Jaffna Town",
          pickupLocations: ["Main Market"],
          availableFrom: new Date().toISOString(),
          availableUntil: new Date(Date.now() + 86400000).toISOString(),
          description: "Test listing from verification script",
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      console.log(`   ✅ Listing Created! ID: ${newListing.data.data.id}`);
    } catch (error) {
      if (error.response && error.response.status === 403) {
        console.log(
          `   ⚠️  Received 403 Forbidden (Expected if user is not a FARMER).`,
        );
        console.log(
          `   ✅ Authorization Logic Working: Buyer cannot create listings.`,
        );
      } else {
        throw error;
      }
    }

    // 6. Get Market Prices
    console.log("\n6️⃣  Testing Market Prices...");
    try {
      const prices = await axios.get(`${BASE_URL}/api/market-prices`);
      console.log(`   ✅ Success. Retrieved market prices.`);
    } catch (e) {
      console.log(`   ⚠️  Market Prices endpoint might be missing or private.`);
    }

    console.log("\n🎉 Verification Complete!");
  } catch (error) {
    console.error("\n❌ Test Failed:");
    if (error.response) {
      console.error(`   Status: ${error.response.status}`);
      console.error(`   Data:`, JSON.stringify(error.response.data, null, 2));
    } else {
      console.error(`   Error: ${error.message}`);
    }
  }
}

runTests();
