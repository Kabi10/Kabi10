const { supabaseAdmin } = require('../../src/config/supabase');
const { rateLimit } = require('../../src/middleware/rateLimit');

/**
 * Vercel Serverless Function: Get Listings
 * GET /api/listings - Get all active listings
 * Rate limited: 60 requests per minute per IP
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

  // Apply rate limiting for API requests (60 per minute)
  const rateLimitCheck = rateLimit('api');
  if (!rateLimitCheck(req, res)) {
    return; // Rate limit exceeded, response already sent
  }

  try {
    const { cropType, location, minPrice, maxPrice, quality, page = 1, limit = 20, sortBy = 'created_at', sortOrder = 'desc' } = req.query;

    let query = supabaseAdmin
      .from('listings')
      .select('*, users!farmer_id(id,phone_number,user_type,profile_data)', { count: 'exact' })
      .eq('is_active', true);

    if (cropType) query = query.eq('crop_type', cropType);
    if (location) query = query.ilike('location', `%${location}%`);
    if (quality) query = query.eq('quality', quality);
    if (minPrice) query = query.gte('price_per_unit', parseFloat(minPrice));
    if (maxPrice) query = query.lte('price_per_unit', parseFloat(maxPrice));

    const validSortFields = ['created_at', 'price_per_unit', 'quantity', 'available_from'];
    const sortField = validSortFields.includes(sortBy) ? sortBy : 'created_at';
    query = query.order(sortField, { ascending: sortOrder === 'asc' });

    const pageNum = parseInt(page);
    const limitNum = parseInt(limit);
    const offset = (pageNum - 1) * limitNum;
    query = query.range(offset, offset + limitNum - 1);

    const { data: listings, error, count } = await query;

    if (error) {
      console.error('Database error:', error);
      return res.status(500).json({ success: false, message: 'Failed to fetch listings', error: error.message });
    }

    res.json({
      success: true,
      data: listings || [],
      count: count || 0,
      page: pageNum,
      limit: limitNum,
      totalPages: Math.ceil((count || 0) / limitNum),
      message: 'Listings retrieved successfully'
    });
  } catch (error) {
    console.error('Get listings error:', error);
    res.status(500).json({ success: false, message: 'Internal server error', error: error.message });
  }
};