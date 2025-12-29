const jwt = require('jsonwebtoken');
const { supabaseAdmin } = require('../config/supabase');
const db = require('../database/connection');
const logger = require('../utils/logger');

/**
 * JWT Authentication Middleware for Serverless Functions
 * Validates JWT tokens and returns user info
 */
const authenticateToken = async (req) => {
  try {
    const authHeader = req.headers.authorization;
    const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

    if (!token) {
      return {
        success: false,
        message: 'Access token required',
        code: 'TOKEN_MISSING',
      };
    }

    // Verify JWT token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // Get user from Supabase to ensure they still exist and are active
    const { data: userData, error } = await supabaseAdmin
      .from('users')
      .select('id, phone_number, user_type, is_active, verified')
      .eq('id', decoded.userId)
      .single();

    if (error || !userData) {
      return {
        success: false,
        message: 'User not found',
        code: 'USER_NOT_FOUND',
      };
    }

    if (!userData.is_active) {
      return {
        success: false,
        message: 'Account is deactivated',
        code: 'ACCOUNT_DEACTIVATED',
      };
    }

    if (!userData.verified) {
      return {
        success: false,
        message: 'Account not verified',
        code: 'ACCOUNT_NOT_VERIFIED',
      };
    }

    // Return user info
    return {
      success: true,
      user: {
        userId: userData.id,
        phoneNumber: userData.phone_number,
        userType: userData.user_type,
        isActive: userData.is_active,
        verified: userData.verified,
      },
    };
  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return {
        success: false,
        message: 'Invalid token',
        code: 'TOKEN_INVALID',
      };
    }

    if (error.name === 'TokenExpiredError') {
      return {
        success: false,
        message: 'Token expired',
        code: 'TOKEN_EXPIRED',
      };
    }

    logger.error('Authentication error:', error);
    return {
      success: false,
      message: 'Authentication failed',
      code: 'AUTH_ERROR',
    };
  }
};

/**
 * Role-based authorization middleware
 * Ensures user has required role(s)
 */
const requireRole = (roles) => (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({
      success: false,
      message: 'Authentication required',
      code: 'AUTH_REQUIRED',
    });
  }

  const userRole = req.user.userType;
  const allowedRoles = Array.isArray(roles) ? roles : [roles];

  if (!allowedRoles.includes(userRole)) {
    return res.status(403).json({
      success: false,
      message: `Access denied. Required role: ${allowedRoles.join(' or ')}`,
      code: 'INSUFFICIENT_PERMISSIONS',
    });
  }

  next();
};

/**
 * Farmer-only middleware
 */
const requireFarmer = requireRole('FARMER');

/**
 * Buyer-only middleware
 */
const requireBuyer = requireRole('BUYER');

/**
 * Optional authentication middleware
 * Attaches user info if token is present, but doesn't require it
 */
const optionalAuth = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
      return next(); // No token, continue without user info
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    const user = await db.query(
      'SELECT id, phone_number, user_type, is_active, verified FROM users WHERE id = $1',
      [decoded.userId],
    );

    if (user.rows.length > 0 && user.rows[0].is_active && user.rows[0].verified) {
      const userData = user.rows[0];
      req.user = {
        userId: userData.id,
        phoneNumber: userData.phone_number,
        userType: userData.user_type,
        isActive: userData.is_active,
        verified: userData.verified,
      };
    }

    next();
  } catch (error) {
    // Ignore token errors for optional auth
    next();
  }
};

/**
 * Resource ownership middleware
 * Ensures user can only access their own resources
 */
const requireOwnership = (resourceType, idParam = 'id') => async (req, res, next) => {
  try {
    const resourceId = req.params[idParam];
    const userId = req.user.userId;

    let query;
    const params = [resourceId];

    switch (resourceType) {
      case 'listing':
        query = 'SELECT farmer_id FROM listings WHERE id = $1';
        break;
      case 'transaction':
        query = 'SELECT farmer_id, buyer_id FROM transactions WHERE id = $1';
        break;
      case 'user':
        // For user resources, just check if the ID matches
        if (resourceId !== userId) {
          return res.status(403).json({
            success: false,
            message: 'Access denied',
            code: 'RESOURCE_ACCESS_DENIED',
          });
        }
        return next();
      default:
        return res.status(500).json({
          success: false,
          message: 'Unknown resource type',
          code: 'UNKNOWN_RESOURCE_TYPE',
        });
    }

    const result = await db.query(query, params);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Resource not found',
        code: 'RESOURCE_NOT_FOUND',
      });
    }

    const resource = result.rows[0];
    let hasAccess = false;

    switch (resourceType) {
      case 'listing':
        hasAccess = resource.farmer_id === userId;
        break;
      case 'transaction':
        hasAccess = resource.farmer_id === userId || resource.buyer_id === userId;
        break;
    }

    if (!hasAccess) {
      return res.status(403).json({
        success: false,
        message: 'Access denied',
        code: 'RESOURCE_ACCESS_DENIED',
      });
    }

    next();
  } catch (error) {
    logger.error('Ownership check error:', error);
    return res.status(500).json({
      success: false,
      message: 'Access check failed',
      code: 'ACCESS_CHECK_ERROR',
    });
  }
};

/**
 * API key authentication for internal services
 */
const authenticateApiKey = (req, res, next) => {
  const apiKey = req.headers['x-api-key'];
  const validApiKey = process.env.INTERNAL_API_KEY;

  if (!validApiKey) {
    return res.status(500).json({
      success: false,
      message: 'API key authentication not configured',
      code: 'API_KEY_NOT_CONFIGURED',
    });
  }

  if (!apiKey || apiKey !== validApiKey) {
    return res.status(401).json({
      success: false,
      message: 'Invalid API key',
      code: 'INVALID_API_KEY',
    });
  }

  next();
};

module.exports = {
  authenticateToken,
  requireRole,
  requireFarmer,
  requireBuyer,
  optionalAuth,
  requireOwnership,
  authenticateApiKey,
};
