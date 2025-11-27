const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const { rateLimit } = require('../../src/middleware/rateLimit');
const logger = require('../../src/utils/logger');

/**
 * Vercel Serverless Function: Create Listing
 * POST /api/listings/create
 * Rate limited: 20 write requests per minute per IP
 */
module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  // Apply rate limiting for write operations (20 per minute)
  const rateLimitCheck = rateLimit('write');
  if (!rateLimitCheck(req, res)) {
    return; // Rate limit exceeded, response already sent
  }

  try {
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const { cropType, quantity, unit, pricePerUnit, quality, location, pickupLocations, availableFrom, availableUntil, description, images = [] } = req.body;

    if (!cropType || !quantity || !unit || !pricePerUnit || !quality || !location || !pickupLocations || !availableFrom || !availableUntil) {
      return res.status(400).json({ success: false, message: 'Missing required fields' });
    }

    const { data: user, error: userError } = await supabaseAdmin
      .from('users')
      .select('user_type')
      .eq('id', authResult.user.userId)
      .single();

    if (userError || !user || user.user_type !== 'FARMER') {
      return res.status(403).json({ success: false, message: 'Only farmers can create listings' });
    }

    const { data: listing, error: listingError } = await supabaseAdmin
      .from('listings')
      .insert({
        farmer_id: authResult.user.userId,
        crop_type: cropType,
        quantity: parseFloat(quantity),
        unit,
        price_per_unit: parseFloat(pricePerUnit),
        quality,
        location,
        pickup_locations: pickupLocations,
        available_from: availableFrom,
        available_until: availableUntil,
        description,
        images,
        is_active: true
      })
      .select()
      .single();

    if (listingError) {
      logger.error('Create listing error:', listingError);
      return res.status(500).json({ success: false, message: 'Failed to create listing' });
    }

    logger.info('Listing created', { listingId: listing.id, farmerId: authResult.user.userId });

    res.status(201).json({
      success: true,
      data: listing,
      message: 'Listing created successfully'
    });
  } catch (error) {
    logger.error('Create listing error:', error);
    res.status(500).json({ success: false, message: 'Internal server error' });
  }
};