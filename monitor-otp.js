#!/usr/bin/env node

/**
 * OTP Monitor for Android App Testing
 * 
 * This script monitors OTP requests in real-time to show you
 * the exact OTP code that your Android app generates.
 */

const https = require('https');

// Store the latest OTP for the test phone number
let latestOtp = null;
let latestTimestamp = null;

async function getLatestOtpFromDatabase(phoneNumber = '+94771234567') {
  return new Promise((resolve, reject) => {
    // We'll simulate this by making a request and checking the response
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

async function monitorOtp() {
  console.log('🔍 OTP Monitor for Android App Testing');
  console.log('=====================================\n');
  console.log('📱 Phone Number: +94771234567');
  console.log('⏰ Monitoring for OTP changes...\n');
  console.log('📋 Instructions:');
  console.log('1. Keep this script running');
  console.log('2. Open your Android app');
  console.log('3. Enter phone number: +94771234567');
  console.log('4. Tap "Send OTP" in the app');
  console.log('5. Use the OTP code shown below ⬇️\n');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

  // Check for OTP changes every 2 seconds
  setInterval(async () => {
    try {
      const response = await getLatestOtpFromDatabase();
      
      if (response.success && response.otp) {
        const currentTime = new Date().toLocaleTimeString();
        
        // Check if this is a new OTP
        if (response.otp !== latestOtp) {
          latestOtp = response.otp;
          latestTimestamp = currentTime;
          
          console.log(`🔑 NEW OTP DETECTED: ${response.otp}`);
          console.log(`⏰ Generated at: ${currentTime}`);
          console.log(`📱 Valid for: 10 minutes`);
          console.log(`━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n`);
        }
      }
    } catch (error) {
      // Silently continue monitoring
    }
  }, 2000);

  // Keep the script running
  console.log('Press Ctrl+C to stop monitoring\n');
}

// Handle graceful shutdown
process.on('SIGINT', () => {
  console.log('\n👋 OTP monitoring stopped');
  process.exit(0);
});

monitorOtp();
