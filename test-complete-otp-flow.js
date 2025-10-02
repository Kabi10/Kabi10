#!/usr/bin/env node

/**
 * Complete OTP Flow Testing Script for Jaffna Farmers Marketplace
 * 
 * This script tests the entire OTP authentication flow:
 * 1. Send OTP
 * 2. Verify OTP
 * 3. Get authentication tokens
 */

const https = require('https');

const API_BASE = 'https://agrimarket-okge7x2nq-kabilantharmaratnam-kpucas-projects.vercel.app/api';

async function makeRequest(path, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'agrimarket-okge7x2nq-kabilantharmaratnam-kpucas-projects.vercel.app',
      port: 443,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    if (data) {
      const jsonData = JSON.stringify(data);
      options.headers['Content-Length'] = jsonData.length;
    }

    const req = https.request(options, (res) => {
      let responseData = '';

      res.on('data', (chunk) => {
        responseData += chunk;
      });

      res.on('end', () => {
        try {
          const response = JSON.parse(responseData);
          resolve({
            statusCode: res.statusCode,
            headers: res.headers,
            data: response
          });
        } catch (error) {
          resolve({
            statusCode: res.statusCode,
            headers: res.headers,
            data: responseData,
            parseError: error.message
          });
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function testCompleteOtpFlow() {
  console.log('🌾 Jaffna Farmers Marketplace - Complete OTP Flow Test\n');
  
  const testPhone = '+94771234567';
  
  try {
    // Step 1: Send OTP
    console.log('📱 Step 1: Sending OTP...');
    const sendResponse = await makeRequest('/api/auth/send-otp.js', 'POST', {
      phoneNumber: testPhone
    });
    
    console.log(`Status: ${sendResponse.statusCode}`);
    console.log('Response:', JSON.stringify(sendResponse.data, null, 2));
    
    if (sendResponse.statusCode !== 200 || !sendResponse.data.success) {
      console.log('❌ Send OTP failed');
      return;
    }
    
    const otpCode = sendResponse.data.otp;
    console.log(`✅ OTP sent successfully: ${otpCode}\n`);
    
    // Step 2: Verify OTP
    console.log('🔐 Step 2: Verifying OTP...');
    const verifyResponse = await makeRequest('/api/auth/verify-otp.js', 'POST', {
      phoneNumber: testPhone,
      otp: otpCode
    });
    
    console.log(`Status: ${verifyResponse.statusCode}`);
    console.log('Response:', JSON.stringify(verifyResponse.data, null, 2));
    
    if (verifyResponse.statusCode === 200 && verifyResponse.data.success) {
      console.log('✅ OTP verification successful!');
      console.log('🎉 Authentication flow completed successfully!\n');
      
      // Display tokens
      if (verifyResponse.data.token) {
        console.log('🔑 Access Token:', verifyResponse.data.token.substring(0, 50) + '...');
      }
      if (verifyResponse.data.refreshToken) {
        console.log('🔄 Refresh Token:', verifyResponse.data.refreshToken.substring(0, 50) + '...');
      }
      if (verifyResponse.data.user) {
        console.log('👤 User Info:', {
          id: verifyResponse.data.user.id,
          phoneNumber: verifyResponse.data.user.phoneNumber,
          userType: verifyResponse.data.user.userType,
          verified: verifyResponse.data.user.verified
        });
      }
    } else {
      console.log('❌ OTP verification failed');
      if (verifyResponse.data.debug) {
        console.log('Debug info:', verifyResponse.data.debug);
      }
    }
    
  } catch (error) {
    console.error('❌ Test failed with error:', error.message);
  }
  
  console.log('\n📋 How to use in Android app:');
  console.log('1. Enter phone number: +94771234567');
  console.log('2. Tap "Send OTP"');
  console.log('3. Check logcat for OTP code or use the test script');
  console.log('4. Enter the OTP code in the app');
  console.log('5. Complete authentication');
}

// Test with different phone formats
async function testPhoneFormats() {
  console.log('\n🧪 Testing different phone number formats...\n');
  
  const phoneFormats = [
    { phone: '+94771234567', format: 'phoneNumber' },
    { phone: '+94771234567', format: 'phone' }
  ];
  
  for (const test of phoneFormats) {
    try {
      console.log(`Testing with ${test.format} field...`);
      
      // Send OTP
      const sendResponse = await makeRequest('/api/auth/send-otp.js', 'POST', {
        phoneNumber: test.phone
      });
      
      if (sendResponse.statusCode === 200 && sendResponse.data.success) {
        const otpCode = sendResponse.data.otp;
        
        // Verify with different field name
        const verifyData = {};
        verifyData[test.format] = test.phone;
        verifyData.otp = otpCode;
        
        const verifyResponse = await makeRequest('/api/auth/verify-otp.js', 'POST', verifyData);
        
        console.log(`${test.format} format: ${verifyResponse.statusCode === 200 ? '✅ Success' : '❌ Failed'}`);
      }
    } catch (error) {
      console.log(`${test.format} format: ❌ Error - ${error.message}`);
    }
  }
}

// Run the tests
async function runAllTests() {
  await testCompleteOtpFlow();
  await testPhoneFormats();
}

runAllTests().catch(console.error);
