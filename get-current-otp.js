#!/usr/bin/env node

/**
 * Quick OTP Retrieval Script for Android Testing
 */

const https = require('https');

async function getCurrentOtp(phoneNumber = '+94771234567') {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({ phoneNumber });

    const options = {
      hostname: 'agrimarket-1g2z1tr0p-kabilantharmaratnam-kpucas-projects.vercel.app',
      port: 443,
      path: '/api/auth/send-otp.js',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length
      }
    };

    const req = https.request(options, (res) => {
      let responseData = '';
      res.on('data', (chunk) => { responseData += chunk; });
      res.on('end', () => {
        try {
          const response = JSON.parse(responseData);
          resolve(response);
        } catch (error) {
          reject(error);
        }
      });
    });

    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

async function main() {
  try {
    console.log('🔑 Getting fresh OTP for testing...\n');
    
    const response = await getCurrentOtp();
    
    if (response.success) {
      console.log('📱 Phone Number:', response.phoneNumber);
      console.log('🔑 OTP Code:', response.otp);
      console.log('⏰ Valid for: 10 minutes');
      console.log('\n📋 Android App Testing Steps:');
      console.log('1. Open Jaffna Farmers Marketplace app');
      console.log('2. Enter phone number:', response.phoneNumber);
      console.log('3. Tap "Send OTP"');
      console.log('4. Enter OTP code:', response.otp);
      console.log('5. Complete authentication ✅');
    } else {
      console.log('❌ Failed to get OTP:', response.message);
    }
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

main();
