const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../database/connection');
const logger = require('../utils/logger');
const { validateUUID, parseDate, isFutureDate } = require('../utils/helpers');

const router = express.Router();

// Validation schemas
const createTransactionValidation = [
  body('listingId').isUUID().withMessage('Invalid listing ID'),
  body('quantity').isFloat({ min: 0.1 }).withMessage('Quantity must be greater than 0'),
  body('totalAmount').isFloat({ min: 0.01 }).withMessage('Total amount must be greater than 0'),
  body('pickupLocation').isLength({ min: 1, max: 255 }).withMessage('Pickup location is required'),
  body('pickupDate').isISO8601().withMessage('Invalid pickup date format'),
  body('buyerContact').optional().isLength({ max: 20 }).withMessage('Buyer contact too long'),
  body('notes').optional().isLength({ max: 500 }).withMessage('Notes too long'),
];

/**
 * GET /api/v1/transactions
 * Get user's transactions with filtering
 */
router.get('/', async (req, res) => {
  try {
    const {
      status,
      role, // 'farmer' or 'buyer'
      page = 1,
      limit = 20,
      sortBy = 'created_at',
      sortOrder = 'DESC',
    } = req.query;

    // Build dynamic query based on user role
    const whereConditions = [];
    const queryParams = [req.user.userId];
    let paramIndex = 2;

    // Role-based filtering
    if (role === 'farmer') {
      whereConditions.push('t.farmer_id = $1');
    } else if (role === 'buyer') {
      whereConditions.push('t.buyer_id = $1');
    } else {
      // Default: show all user's transactions
      whereConditions.push('(t.farmer_id = $1 OR t.buyer_id = $1)');
    }

    if (status) {
      whereConditions.push(`t.status = $${paramIndex}`);
      queryParams.push(status);
      paramIndex++;
    }

    const offset = (parseInt(page) - 1) * parseInt(limit);

    const query = `
      SELECT 
        t.*,
        l.crop_type,
        l.unit,
        l.price_per_unit,
        farmer.name as farmer_name,
        farmer.phone_number as farmer_contact,
        buyer.name as buyer_name,
        buyer.phone_number as buyer_contact
      FROM transactions t
      JOIN listings l ON t.listing_id = l.id
      JOIN users farmer ON t.farmer_id = farmer.id
      JOIN users buyer ON t.buyer_id = buyer.id
      WHERE ${whereConditions.join(' AND ')}
      ORDER BY t.${sortBy} ${sortOrder}
      LIMIT $${paramIndex} OFFSET $${paramIndex + 1}
    `;

    queryParams.push(parseInt(limit), offset);

    const result = await db.query(query, queryParams);

    // Get total count
    const countQuery = `
      SELECT COUNT(*) as total
      FROM transactions t
      WHERE ${whereConditions.join(' AND ')}
    `;

    const countResult = await db.query(countQuery, queryParams.slice(0, -2));
    const total = parseInt(countResult.rows[0].total);

    // Transform data
    const transactions = result.rows.map((row) => ({
      id: row.id,
      listingId: row.listing_id,
      farmerId: row.farmer_id,
      buyerId: row.buyer_id,
      quantity: parseFloat(row.quantity),
      totalAmount: parseFloat(row.total_amount),
      pickupLocation: row.pickup_location,
      pickupDate: row.pickup_date,
      status: row.status,
      paymentMethod: row.payment_method,
      notes: row.notes,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
      completedAt: row.completed_at,
      cancelledAt: row.cancelled_at,
      cancellationReason: row.cancellation_reason,
      listing: {
        cropType: row.crop_type,
        unit: row.unit,
        pricePerUnit: parseFloat(row.price_per_unit),
      },
      farmer: {
        name: row.farmer_name,
        contact: row.farmer_contact,
      },
      buyer: {
        name: row.buyer_name,
        contact: row.buyer_contact,
      },
    }));

    // Response matches Android TransactionsResponse DTO
    res.json({
      success: true,
      transactions: transactions,  // Android expects 'transactions' not 'data'
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
    });
  } catch (error) {
    logger.error('Get transactions error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch transactions',
    });
  }
});

/**
 * GET /api/v1/transactions/:id
 * Get specific transaction details
 */
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid transaction ID',
      });
    }

    const result = await db.query(`
      SELECT 
        t.*,
        l.crop_type,
        l.unit,
        l.price_per_unit,
        l.quality,
        l.description as listing_description,
        farmer.name as farmer_name,
        farmer.phone_number as farmer_contact,
        farmer.location as farmer_location,
        buyer.name as buyer_name,
        buyer.phone_number as buyer_contact
      FROM transactions t
      JOIN listings l ON t.listing_id = l.id
      JOIN users farmer ON t.farmer_id = farmer.id
      JOIN users buyer ON t.buyer_id = buyer.id
      WHERE t.id = $1 AND (t.farmer_id = $2 OR t.buyer_id = $2)
    `, [id, req.user.userId]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Transaction not found',
      });
    }

    const transaction = result.rows[0];

    res.json({
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
        notes: transaction.notes,
        createdAt: transaction.created_at,
        updatedAt: transaction.updated_at,
        completedAt: transaction.completed_at,
        cancelledAt: transaction.cancelled_at,
        cancellationReason: transaction.cancellation_reason,
        listing: {
          cropType: transaction.crop_type,
          unit: transaction.unit,
          pricePerUnit: parseFloat(transaction.price_per_unit),
          quality: transaction.quality,
          description: transaction.listing_description,
        },
        farmer: {
          name: transaction.farmer_name,
          contact: transaction.farmer_contact,
          location: transaction.farmer_location,
        },
        buyer: {
          name: transaction.buyer_name,
          contact: transaction.buyer_contact,
        },
      },
    });
  } catch (error) {
    logger.error('Get transaction error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch transaction',
    });
  }
});

/**
 * POST /api/v1/transactions
 * Create new transaction (place order)
 */
router.post('/', createTransactionValidation, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array(),
      });
    }

    const {
      listingId,
      quantity,
      totalAmount,
      pickupLocation,
      pickupDate,
      buyerContact,
      notes,
    } = req.body;

    // Validate pickup date is in the future
    if (!isFutureDate(pickupDate)) {
      return res.status(400).json({
        success: false,
        message: 'Pickup date must be in the future',
      });
    }

    // Validate listing exists and is available with locking inside a transaction
    const result = await db.transaction(async (client) => {
      const listing = await client.query(
        'SELECT farmer_id, quantity, price_per_unit, is_active, available_until FROM listings WHERE id = $1 FOR UPDATE',
        [listingId],
      );

      if (listing.rows.length === 0) {
        throw new Error('Listing not found');
      }

      const listingData = listing.rows[0];

      if (!listingData.is_active) {
        throw new Error('Listing is not active');
      }

      if (new Date(listingData.available_until) < new Date()) {
        throw new Error('Listing is no longer available');
      }

      if (listingData.farmer_id === req.user.userId) {
        throw new Error('Cannot create transaction for your own listing');
      }

      if (quantity > parseFloat(listingData.quantity)) {
        throw new Error('Requested quantity exceeds available quantity');
      }

      // Calculate total amount
      const expectedAmount = quantity * parseFloat(listingData.price_per_unit);
      
      // Allow 1 cent difference for floating point errors
      if (Math.abs(totalAmount - expectedAmount) > 0.01) {
        throw new Error('Total amount does not match expected calculation');
      }

      // Create transaction
      const insertResult = await client.query(`
        INSERT INTO transactions (
          listing_id, farmer_id, buyer_id, quantity, total_amount,
          pickup_location, pickup_date, buyer_contact, notes
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
        RETURNING *
      `, [
        listingId,
        listingData.farmer_id,
        req.user.userId,
        quantity,
        totalAmount,
        pickupLocation,
        pickupDate,
        buyerContact || req.user.phoneNumber,
        notes,
      ]);

      return insertResult.rows[0];
    });

    const transaction = result;

    logger.info('Transaction created', {
      transactionId: transaction.id,
      buyerId: req.user.userId,
      farmerId: listingData.farmer_id,
      listingId,
      quantity,
      totalAmount,
    });

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
        notes: transaction.notes,
        createdAt: transaction.created_at,
        updatedAt: transaction.updated_at,
      },
    });
  } catch (error) {
    logger.error('Create transaction error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create transaction',
    });
  }
});

/**
 * PATCH /api/v1/transactions/:id/status
 * Update transaction status
 */
router.patch('/:id/status', [
  body('status').isIn(['PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'])
    .withMessage('Invalid status'),
  body('notes').optional().isLength({ max: 500 }).withMessage('Notes too long'),
], async (req, res) => {
  try {
    const { id } = req.params;
    const { status, notes } = req.body;

    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array(),
      });
    }

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid transaction ID',
      });
    }

    // Get current transaction
    const current = await db.query(
      'SELECT farmer_id, buyer_id, status FROM transactions WHERE id = $1',
      [id],
    );

    if (current.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Transaction not found',
      });
    }

    const transaction = current.rows[0];
    const isFarmer = transaction.farmer_id === req.user.userId;
    const isBuyer = transaction.buyer_id === req.user.userId;

    if (!isFarmer && !isBuyer) {
      return res.status(403).json({
        success: false,
        message: 'Access denied',
      });
    }

    // Validate status transitions
    const validTransitions = {
      PENDING: ['CONFIRMED', 'CANCELLED'],
      CONFIRMED: ['IN_PROGRESS', 'CANCELLED'],
      IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
      COMPLETED: [],
      CANCELLED: [],
    };

    if (!validTransitions[transaction.status].includes(status)) {
      return res.status(400).json({
        success: false,
        message: `Cannot change status from ${transaction.status} to ${status}`,
      });
    }

    // Update transaction
    const updateFields = ['status = $2', 'updated_at = NOW()'];
    const updateValues = [id, status];
    let paramIndex = 3;

    if (status === 'COMPLETED') {
      updateFields.push('completed_at = NOW()');
    } else if (status === 'CANCELLED') {
      updateFields.push('cancelled_at = NOW()');
      if (notes) {
        updateFields.push(`cancellation_reason = $${paramIndex}`);
        updateValues.push(notes);
        paramIndex++;
      }
    }

    if (notes && status !== 'CANCELLED') {
      updateFields.push(`notes = $${paramIndex}`);
      updateValues.push(notes);
    }

    const result = await db.query(`
      UPDATE transactions SET ${updateFields.join(', ')}
      WHERE id = $1
      RETURNING *
    `, updateValues);

    const updatedTransaction = result.rows[0];

    logger.info('Transaction status updated', {
      transactionId: id,
      userId: req.user.userId,
      oldStatus: transaction.status,
      newStatus: status,
    });

    res.json({
      success: true,
      data: {
        id: updatedTransaction.id,
        status: updatedTransaction.status,
        updatedAt: updatedTransaction.updated_at,
        completedAt: updatedTransaction.completed_at,
        cancelledAt: updatedTransaction.cancelled_at,
        cancellationReason: updatedTransaction.cancellation_reason,
        notes: updatedTransaction.notes,
      },
    });
  } catch (error) {
    logger.error('Update transaction status error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update transaction status',
    });
  }
});

module.exports = router;
