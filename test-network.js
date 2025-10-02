#!/usr/bin/env node

/**
 * Network Connectivity Test for Android App
 */

const https = require('https');

async function testEndpoint(path, method = 'GET', data = null) {
  return new Promise((resolve) => {
    const options = {
      hostname: 'agrimarket-729z6lvto-kabilantharmaratnam-kpucas-projects.vercel.app',
      port: 443,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 10000
    };

    if (data) {
      const jsonData = JSON.stringify(data);
      options.headers['Content-Length'] = jsonData.length;
    }

    const req = https.request(options, (res) => {
      let responseData = '';
      res.on('data', (chunk) => { responseData += chunk; });
      res.on('end', () => {
        resolve({
          success: true,
          statusCode: res.statusCode,
          response: responseData.substring(0, 100) + '...'
        });
      });
    });

    req.on('error', (error) => {
      resolve({
        success: false,
        error: error.message
      });
    });

    req.on('timeout', () => {
      resolve({
        success: false,
        error: 'Request timeout'
      });
    });

    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function runNetworkTests() {
  console.log('🌐 Network Connectivity Test for Android App');
  console.log('===========================================\n');
  
  const tests = [
    {
      name: 'Health Check',
      path: '/api/health.js',
      method: 'GET'
    },
    {
      name: 'Send OTP Endpoint',
      path: '/api/auth/send-otp.js',
      method: 'POST',
      data: { phoneNumber: '+94771234567' }
    },
    {
      name: 'Verify OTP Endpoint',
      path: '/api/auth/verify-otp-simple.js',
      method: 'POST',
      data: { phone: '+94771234567', otp: '123456' }
    },
    {
      name: 'Get Current OTP Endpoint',
      path: '/api/auth/get-current-otp.js?phoneNumber=%2B94771234567',
      method: 'GET'
    }
  ];

  for (const test of tests) {
    process.stdout.write(`Testing ${test.name}... `);
    
    const result = await testEndpoint(test.path, test.method, test.data);
    
    if (result.success) {
      console.log(`✅ OK (${result.statusCode})`);
    } else {
      console.log(`❌ FAILED (${result.error})`);
    }
  }

  console.log('\n📱 Android App Configuration:');
  console.log('Base URL: https://agrimarket-729z6lvto-kabilantharmaratnam-kpucas-projects.vercel.app/api/');
  console.log('Send OTP: auth/send-otp.js');
  console.log('Verify OTP: auth/verify-otp-simple.js');
  
  console.log('\n🔧 If any tests failed:');
  console.log('1. Check your internet connection');
  console.log('2. Try again in a few minutes');
  console.log('3. Contact support if issues persist');
}

runNetworkTests();
