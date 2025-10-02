#!/usr/bin/env node

/**
 * OTP Testing Script for Jaffna Farmers Marketplace
 * 
 * This script helps you get OTP codes for testing the Android app
 * without needing actual SMS delivery.
 */

const https = require('https');

const API_BASE = 'https://agrimarket-78kpst6w0-kabilantharmaratnam-kpucas-projects.vercel.app/api';

async function sendOtpRequest(phoneNumber) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({
      phoneNumber: phoneNumber
    });

    const options = {
      hostname: 'agrimarket-78kpst6w0-kabilantharmaratnam-kpucas-projects.vercel.app',
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

      res.on('data', (chunk) => {
        responseData += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(responseData);
          resolve(response);
        } catch (error) {
          reject(error);
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    req.write(data);
    req.end();
  });
}

async function testOtp() {
  console.log('🌾 Jaffna Farmers Marketplace - OTP Testing Tool\n');
  
  const testPhones = [
    '+94771234567',
    '+94701234567', 
    '+94111234567'
  ];

  for (const phone of testPhones) {
    try {
      console.log(`📱 Testing OTP for: ${phone}`);
      const response = await sendOtpRequest(phone);
      
      if (response.success && response.otp) {
        console.log(`🔑 OTP CODE: ${response.otp}`);
        console.log(`📝 Message: ${response.message}`);
        console.log(`✅ Status: Success\n`);
      } else {
        console.log(`❌ Failed: ${response.message}\n`);
      }
    } catch (error) {
      console.log(`❌ Error: ${error.message}\n`);
    }
  }

  console.log('📋 How to use these OTP codes:');
  console.log('1. Open the Jaffna Farmers Marketplace Android app');
  console.log('2. Enter one of the test phone numbers above');
  console.log('3. Tap "Send OTP"');
  console.log('4. Use the OTP code shown above');
  console.log('5. Complete the authentication flow\n');
  
  console.log('🔧 For production SMS setup:');
  console.log('Visit: https://www.ideamart.io/developer');
  console.log('Create account → Get API credentials → Update Vercel env vars');
}

// Run the test
testOtp().catch(console.error);
