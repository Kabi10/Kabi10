const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const { rateLimit } = require('../../src/middleware/rateLimit');
const { requireSignedRequest } = require('../../src/middleware/requestSigning');
const { validateUUID } = require('../../src/utils/helpers');
const logger = require('../../src/utils/logger');

/**
 * Vercel Serverless Function: Create Transaction
 * POST /api/transactions/create
 *
 * Security layers:
 * - Rate limiting: 20 write requests per minute per IP
 * - Request signing: HMAC-SHA256 signature validation (when configured)
 * - Authentication: JWT token validation
 */
module.exports = async (req, res) => {
  // Set CORS headers (include signature headers)
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization, X-Signature, X-Timestamp, X-Nonce');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({
      success: false,
      message: 'Method not allowed'
    });
  }

  // Apply rate limiting for write operations (20 per minute)
  const rateLimitCheck = rateLimit('write');
  if (!rateLimitCheck(req, res)) {
    return; // Rate limit exceeded, response already sent
  }

  // Validate request signature for transaction creation (financial operation)
  if (!requireSignedRequest(req, res)) {
    return; // Invalid signature, response already sent
  }

  try {
    // Authenticate user
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const {
      listingId,
      quantity,
      pickupLocation,
      pickupDate,
      paymentMethod = 'CASH',
      notes
    } = req.body;

    // Validation
    if (!listingId || !validateUUID(listingId)) {
      return res.status(400).json({
        success: false,
        message: 'Valid listing ID is required'
      });
    }

    if (!quantity || quantity <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Quantity must be greater than 0'
      });
    }

    if (!pickupLocation || !pickupDate) {
      return res.status(400).json({
        success: false,
        message: 'Pickup location and date are required'
      });
    }

    // Validate pickup date is in the future
    if (new Date(pickupDate) <= new Date()) {
      return res.status(400).json({
        success: false,
        message: 'Pickup date must be in the future'
      });
    }

    // Check if listing exists and is available
    const { data: listing, error: listingError } = await supabaseAdmin
      .from('listings')
      .select(`
        *,
        users!farmer_id (
          id,
          name,
          phone_number
        )
      `)
      .eq('id', listingId)
      .eq('is_active', true)
      .single();

    if (listingError || !listing) {
      return res.status(404).json({
        success: false,
        message: 'Listing not found or not available'
      });
    }

    // Check if user is trying to buy their own listing
    if (listing.farmer_id === authResult.user.userId) {
      return res.status(400).json({
        success: false,
        message: 'You cannot buy your own listing'
      });
    }

    // Check if listing is still available (date range)
    const today = new Date().toISOString().split('T')[0];
    if (listing.available_until < today) {
      return res.status(400).json({
        success: false,
        message: 'This listing is no longer available'
      });
    }

    // Check if requested quantity is available
    if (quantity > listing.quantity) {
      return res.status(400).json({
        success: false,
        message: `Only ${listing.quantity} ${listing.unit} available`
      });
    }

    // Calculate total amount
    const totalAmount = quantity * listing.price_per_unit;

    // Get buyer information
    const { data: buyer } = await supabaseAdmin
      .from('users')
      .select('name, phone_number')
      .eq('id', authResult.user.userId)
      .single();

    // Create transaction
    const { data: transaction, error: transactionError } = await supabaseAdmin
      .from('transactions')
      .insert({
        listing_id: listingId,
        farmer_id: listing.farmer_id,
        buyer_id: authResult.user.userId,
        quantity,
        total_amount: totalAmount,
        pickup_location: pickupLocation,
        pickup_date: pickupDate,
        payment_method: paymentMethod,
        notes,
        buyer_contact: buyer?.phone_number,
        farmer_contact: listing.users?.phone_number,
        status: 'PENDING'
      })
      .select()
      .single();

    if (transactionError) {
      logger.error('Create transaction error:', transactionError);
      return res.status(500).json({
        success: false,
        message: 'Failed to create transaction'
      });
    }

    // Update listing quantity
    const newQuantity = listing.quantity - quantity;
    const updateData = { quantity: newQuantity };
    
    // If quantity becomes 0, mark as inactive
    if (newQuantity <= 0) {
      updateData.is_active = false;
    }

    await supabaseAdmin
      .from('listings')
      .update(updateData)
      .eq('id', listingId);

    logger.info('Transaction created', {
      transactionId: transaction.id,
      buyerId: authResult.user.userId,
      farmerId: listing.farmer_id,
      listingId,
      quantity,
      totalAmount
    });

    // Transform response to match Android model
    res.status(201).json({
      success: true,
      data: {
        id: transaction.id,
        listingId: transaction.listing_id,
        farmerId: transaction.farmer_id,
        buyerId: transaction.buyer_id,
        quantity: parseFloat(transaction.quantity),
        totalAmount: parseFloat(transaction.total_amount),
        pickupLocation: transaction.pickup_location,
        pickupDate: transaction.pickup_date,
        status: transaction.status,
        paymentMethod: transaction.payment_method,
        createdAt: transaction.created_at,
        updatedAt: transaction.updated_at,
        notes: transaction.notes,
        
        // Additional info for display
        cropType: listing.crop_type,
        unit: listing.unit,
        pricePerUnit: parseFloat(listing.price_per_unit),
        farmerName: listing.users?.name,
        farmerContact: listing.users?.phone_number,
        buyerName: buyer?.name,
        buyerContact: buyer?.phone_number
      }
    });
  } catch (error) {
    logger.error('Create transaction error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create transaction'
    });
  }
};
