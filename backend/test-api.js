// Test script for Jaffna Farmers Marketplace API
const https = require('https');

const API_BASE = 'https://agrimarket-78kpst6w0-kabilantharmaratnam-kpucas-projects.vercel.app';

async function testEndpoint(path, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, API_BASE);
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'Jaffna-Marketplace-Test/1.0'
      }
    };

    const req = https.request(url, options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(body);
          resolve({ status: res.statusCode, data: parsed });
        } catch (e) {
          resolve({ status: res.statusCode, data: body });
        }
      });
    });

    req.on('error', reject);
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function runTests() {
  console.log('🧪 Testing Jaffna Farmers Marketplace API...\n');

  // Test 1: Health Check
  console.log('1. Testing Health Endpoint...');
  try {
    const health = await testEndpoint('/api/health.js');
    console.log(`   Status: ${health.status}`);
    console.log(`   Response: ${JSON.stringify(health.data, null, 2)}\n`);
  } catch (error) {
    console.log(`   Error: ${error.message}\n`);
  }

  // Test 2: Send OTP (with test phone number)
  console.log('2. Testing Send OTP Endpoint...');
  try {
    const otp = await testEndpoint('/api/auth/send-otp.js', 'POST', {
      phoneNumber: '+94771234567'
    });
    console.log(`   Status: ${otp.status}`);
    console.log(`   Response: ${JSON.stringify(otp.data, null, 2)}\n`);
  } catch (error) {
    console.log(`   Error: ${error.message}\n`);
  }

  // Test 3: Get Listings (without auth - should fail)
  console.log('3. Testing Get Listings Endpoint (no auth)...');
  try {
    const listings = await testEndpoint('/api/listings/index.js');
    console.log(`   Status: ${listings.status}`);
    console.log(`   Response: ${JSON.stringify(listings.data, null, 2)}\n`);
  } catch (error) {
    console.log(`   Error: ${error.message}\n`);
  }

  console.log('✅ API Tests Complete!');
  console.log('\n📱 Android App Configuration:');
  console.log(`   BASE_URL = "${API_BASE}/api/v1/"`);
}

runTests().catch(console.error);
