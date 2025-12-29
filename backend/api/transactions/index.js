const { supabaseAdmin, getReadClient } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const logger = require('../../src/utils/logger');

/**
 * Vercel Serverless Function: Get Transactions
 * GET /api/transactions
 * 
 * Performance: Uses read replica (if available) for GET requests
 * to reduce load on primary database (INFRA-02)
 */
module.exports = async (req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

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
    // Authenticate user
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const {
      status,
      role, // 'buyer' or 'farmer'
      page = 1,
      limit = 20,
      sortBy = 'created_at',
      sortOrder = 'desc'
    } = req.query;

    // Use read replica for GET requests (reduces primary DB load - INFRA-02)
    const client = getReadClient();

    // Build query based on user role
    let query = client
      .from('transactions')
      .select(`
        *,
        listings!listing_id (
          crop_type,
          unit,
          price_per_unit,
          location
        ),
        farmer:users!farmer_id (
          name,
          phone_number
        ),
        buyer:users!buyer_id (
          name,
          phone_number
        )
      `);

    // Filter by user role
    if (role === 'buyer') {
      query = query.eq('buyer_id', authResult.user.userId);
    } else if (role === 'farmer') {
      query = query.eq('farmer_id', authResult.user.userId);
    } else {
      // Show all transactions where user is either buyer or farmer
      query = query.or(`buyer_id.eq.${authResult.user.userId},farmer_id.eq.${authResult.user.userId}`);
    }

    // Apply status filter
    if (status) {
      query = query.eq('status', status);
    }

    // Apply sorting
    query = query.order(sortBy, { ascending: sortOrder.toLowerCase() === 'asc' });

    // Apply pagination
    const offset = (parseInt(page) - 1) * parseInt(limit);
    query = query.range(offset, offset + parseInt(limit) - 1);

    const { data: transactions, error } = await query;

    if (error) {
      logger.error('Get transactions error:', error);
      return res.status(500).json({
        success: false,
        message: 'Failed to fetch transactions'
      });
    }

    // Transform data to match Android model
    const transformedTransactions = transactions.map(transaction => ({
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
      completedAt: transaction.completed_at,
      cancelledAt: transaction.cancelled_at,
      cancellationReason: transaction.cancellation_reason,

      // Listing information
      cropType: transaction.listings?.crop_type,
      unit: transaction.listings?.unit,
      pricePerUnit: parseFloat(transaction.listings?.price_per_unit || 0),
      listingLocation: transaction.listings?.location,

      // User information
      farmerName: transaction.farmer?.name,
      farmerContact: transaction.farmer?.phone_number,
      buyerName: transaction.buyer?.name,
      buyerContact: transaction.buyer?.phone_number,

      // User's role in this transaction
      userRole: transaction.buyer_id === authResult.user.userId ? 'BUYER' : 'FARMER'
    }));

    // Get total count for pagination (also use read replica)
    let countQuery = client
      .from('transactions')
      .select('*', { count: 'exact', head: true });

    if (role === 'buyer') {
      countQuery = countQuery.eq('buyer_id', authResult.user.userId);
    } else if (role === 'farmer') {
      countQuery = countQuery.eq('farmer_id', authResult.user.userId);
    } else {
      countQuery = countQuery.or(`buyer_id.eq.${authResult.user.userId},farmer_id.eq.${authResult.user.userId}`);
    }

    if (status) {
      countQuery = countQuery.eq('status', status);
    }

    const { count: totalCount } = await countQuery;

    res.json({
      success: true,
      data: transformedTransactions,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total: totalCount || 0,
        totalPages: Math.ceil((totalCount || 0) / parseInt(limit))
      }
    });
  } catch (error) {
    logger.error('Get transactions error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch transactions'
    });
  }
};
