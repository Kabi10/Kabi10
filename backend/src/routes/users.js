const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../database/connection');
const logger = require('../utils/logger');
const { validateUUID, sanitizeString } = require('../utils/helpers');

const router = express.Router();

/**
 * GET /api/v1/users/profile
 * Get current user profile
 */
router.get('/profile', async (req, res) => {
  try {
    const result = await db.query(
      'SELECT id, phone_number, name, user_type, location, verified, created_at, updated_at FROM users WHERE id = $1',
      [req.user.userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    const user = result.rows[0];

    res.json({
      success: true,
      data: {
        id: user.id,
        phoneNumber: user.phone_number,
        name: user.name,
        userType: user.user_type,
        location: user.location,
        verified: user.verified,
        createdAt: user.created_at,
        updatedAt: user.updated_at
      }
    });
  } catch (error) {
    logger.error('Get user profile error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get user profile'
    });
  }
});

/**
 * PUT /api/v1/users/profile
 * Update current user profile
 */
router.put('/profile', [
  body('name').optional().isLength({ min: 1, max: 255 }).withMessage('Name must be 1-255 characters'),
  body('location').optional().isLength({ min: 1, max: 255 }).withMessage('Location must be 1-255 characters'),
  body('userType').optional().isIn(['FARMER', 'BUYER']).withMessage('User type must be FARMER or BUYER')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array()
      });
    }

    const { name, location, userType } = req.body;

    // Build dynamic update query
    const updateFields = [];
    const updateValues = [req.user.userId];
    let paramIndex = 2;

    if (name !== undefined) {
      updateFields.push(`name = $${paramIndex}`);
      updateValues.push(sanitizeString(name));
      paramIndex++;
    }

    if (location !== undefined) {
      updateFields.push(`location = $${paramIndex}`);
      updateValues.push(sanitizeString(location));
      paramIndex++;
    }

    if (userType !== undefined) {
      updateFields.push(`user_type = $${paramIndex}`);
      updateValues.push(userType);
      paramIndex++;
    }

    if (updateFields.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'No fields to update'
      });
    }

    updateFields.push('updated_at = NOW()');

    const query = `
      UPDATE users SET ${updateFields.join(', ')}
      WHERE id = $1
      RETURNING id, phone_number, name, user_type, location, verified, created_at, updated_at
    `;

    const result = await db.query(query, updateValues);
    const user = result.rows[0];

    logger.info('User profile updated', { userId: req.user.userId });

    res.json({
      success: true,
      data: {
        id: user.id,
        phoneNumber: user.phone_number,
        name: user.name,
        userType: user.user_type,
        location: user.location,
        verified: user.verified,
        createdAt: user.created_at,
        updatedAt: user.updated_at
      }
    });
  } catch (error) {
    logger.error('Update user profile error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update user profile'
    });
  }
});

/**
 * GET /api/v1/users/:id
 * Get public user profile (limited info)
 */
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid user ID'
      });
    }

    const result = await db.query(
      'SELECT id, name, user_type, location, created_at FROM users WHERE id = $1 AND is_active = true',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    const user = result.rows[0];

    res.json({
      success: true,
      data: {
        id: user.id,
        name: user.name,
        userType: user.user_type,
        location: user.location,
        memberSince: user.created_at
      }
    });
  } catch (error) {
    logger.error('Get user error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get user'
    });
  }
});

/**
 * GET /api/v1/users/stats/dashboard
 * Get user dashboard statistics
 */
router.get('/stats/dashboard', async (req, res) => {
  try {
    const userId = req.user.userId;
    const userType = req.user.userType;

    let stats = {};

    if (userType === 'FARMER') {
      // Farmer statistics
      const listingsResult = await db.query(`
        SELECT 
          COUNT(*) as total_listings,
          COUNT(*) FILTER (WHERE is_active = true) as active_listings,
          COUNT(*) FILTER (WHERE available_until >= CURRENT_DATE) as available_listings
        FROM listings 
        WHERE farmer_id = $1
      `, [userId]);

      const transactionsResult = await db.query(`
        SELECT 
          COUNT(*) as total_transactions,
          COUNT(*) FILTER (WHERE status = 'PENDING') as pending_transactions,
          COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed_transactions,
          COALESCE(SUM(total_amount) FILTER (WHERE status = 'COMPLETED'), 0) as total_earnings
        FROM transactions 
        WHERE farmer_id = $1
      `, [userId]);

      stats = {
        listings: listingsResult.rows[0],
        transactions: transactionsResult.rows[0]
      };
    } else {
      // Buyer statistics
      const transactionsResult = await db.query(`
        SELECT 
          COUNT(*) as total_purchases,
          COUNT(*) FILTER (WHERE status = 'PENDING') as pending_purchases,
          COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed_purchases,
          COALESCE(SUM(total_amount) FILTER (WHERE status = 'COMPLETED'), 0) as total_spent
        FROM transactions 
        WHERE buyer_id = $1
      `, [userId]);

      stats = {
        purchases: transactionsResult.rows[0]
      };
    }

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    logger.error('Get user stats error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get user statistics'
    });
  }
});

/**
 * DELETE /api/v1/users/account
 * Deactivate user account
 */
router.delete('/account', async (req, res) => {
  try {
    // Soft delete by setting is_active to false
    await db.query(
      'UPDATE users SET is_active = false, updated_at = NOW() WHERE id = $1',
      [req.user.userId]
    );

    // Also deactivate user's listings
    await db.query(
      'UPDATE listings SET is_active = false, updated_at = NOW() WHERE farmer_id = $1',
      [req.user.userId]
    );

    logger.info('User account deactivated', { userId: req.user.userId });

    res.json({
      success: true,
      message: 'Account deactivated successfully'
    });
  } catch (error) {
    logger.error('Deactivate account error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to deactivate account'
    });
  }
});

module.exports = router;
