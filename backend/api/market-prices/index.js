const { supabaseAdmin } = require('../../src/config/supabase');

/**
 * Vercel Serverless Function: Get Market Prices
 * GET /api/market-prices - Get all market prices
 */
module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const { cropType, location, trend, minPrice, maxPrice, page = 1, limit = 20, sortBy = 'last_updated', sortOrder = 'desc' } = req.query;

    let query = supabaseAdmin
      .from('market_prices')
      .select('*', { count: 'exact' });

    if (cropType) query = query.eq('crop_type', cropType);
    if (location) query = query.eq('location', location);
    if (trend) query = query.eq('trend', trend);
    if (minPrice) query = query.gte('current_price', parseFloat(minPrice));
    if (maxPrice) query = query.lte('current_price', parseFloat(maxPrice));

    const validSortFields = ['last_updated', 'current_price', 'change_percentage', 'crop_type'];
    const sortField = validSortFields.includes(sortBy) ? sortBy : 'last_updated';
    query = query.order(sortField, { ascending: sortOrder === 'asc' });

    const pageNum = parseInt(page);
    const limitNum = parseInt(limit);
    const offset = (pageNum - 1) * limitNum;
    query = query.range(offset, offset + limitNum - 1);

    const { data: marketPrices, error, count } = await query;

    if (error) {
      console.error('Database error:', error);
      return res.status(500).json({ success: false, message: 'Failed to fetch market prices', error: error.message });
    }

    return res.status(200).json({
      success: true,
      data: marketPrices || [],
      pagination: {
        page: pageNum,
        limit: limitNum,
        total: count || 0,
        totalPages: Math.ceil((count || 0) / limitNum)
      }
    });

  } catch (error) {
    console.error('Server error:', error);
    return res.status(500).json({ success: false, message: 'Internal server error', error: error.message });
  }
};

