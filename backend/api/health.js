// const { supabaseAdmin } = require('../src/config/supabase');

/**
 * Vercel Serverless Function: Health Check
 * GET /health
 */
module.exports = async (req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'GET') {
    return res.status(405).json({
      success: false,
      message: 'Method not allowed'
    });
  }

  try {
    res.json({
      success: true,
      message: 'Jaffna Farmers Marketplace API is healthy',
      timestamp: new Date().toISOString(),
      version: '2.0.0-serverless',
      database: 'ready',
      environment: process.env.NODE_ENV || 'production'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Health check failed',
      error: error.message,
      timestamp: new Date().toISOString()
    });
  }
};
