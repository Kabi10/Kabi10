const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const db = require('../database/connection');
const smsService = require('../services/smsService');
const logger = require('../utils/logger');
const { generateOTP, isValidSriLankanPhone } = require('../utils/helpers');

const router = express.Router();

// Validation middleware
const validatePhoneNumber = [
  body('phoneNumber')
    .matches(/^\+94[0-9]{9}$/)
    .withMessage('Invalid Sri Lankan phone number format. Use +94XXXXXXXXX'),
];

const validateOTP = [
  body('phoneNumber')
    .matches(/^\+94[0-9]{9}$/)
    .withMessage('Invalid phone number format'),
  body('otp')
    .isLength({ min: 6, max: 6 })
    .isNumeric()
    .withMessage('OTP must be 6 digits'),
];

/**
 * POST /api/v1/auth/send-otp
 * Send OTP to phone number
 * Matches Android SendOtpRequest/Response
 */
router.post('/send-otp', validatePhoneNumber, async (req, res) => {
  try {
    // Check validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Invalid phone number format',
        errors: errors.array(),
      });
    }

    const { phoneNumber } = req.body;

    // Additional phone number validation
    if (!isValidSriLankanPhone(phoneNumber)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid Sri Lankan phone number',
      });
    }

    // Check for recent OTP requests (rate limiting)
    const recentOTP = await db.query(
      `SELECT * FROM otp_verifications 
       WHERE phone_number = $1 AND created_at > NOW() - INTERVAL '1 minute'
       ORDER BY created_at DESC LIMIT 1`,
      [phoneNumber],
    );

    if (recentOTP.rows.length > 0) {
      return res.status(429).json({
        success: false,
        message: 'Please wait before requesting another OTP',
      });
    }

    // Generate OTP
    const otpCode = generateOTP();
    const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // 5 minutes

    // Store OTP in database
    await db.query(
      `INSERT INTO otp_verifications (phone_number, otp_code, expires_at)
       VALUES ($1, $2, $3)`,
      [phoneNumber, otpCode, expiresAt],
    );

    // Send SMS
    try {
      const message = `Your Jaffna Marketplace verification code is: ${otpCode}. Valid for 5 minutes. Do not share this code.`;
      await smsService.sendSMS(phoneNumber, message);

      logger.info('OTP sent successfully', { phoneNumber, masked: true });

      res.json({
        success: true,
        message: 'OTP sent successfully',
      });
    } catch (smsError) {
      logger.error('Failed to send SMS:', smsError);

      // In development, return the OTP for testing
      if (process.env.NODE_ENV === 'development' || process.env.MOCK_SMS === 'true') {
        res.json({
          success: true,
          message: 'OTP sent successfully',
          otp: otpCode, // Only in development
        });
      } else {
        res.status(500).json({
          success: false,
          message: 'Failed to send OTP. Please try again.',
        });
      }
    }
  } catch (error) {
    logger.error('Send OTP error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error',
    });
  }
});

/**
 * POST /api/v1/auth/verify-otp
 * Verify OTP and login/register user
 * Matches Android LoginRequest/Response
 */
router.post('/verify-otp', validateOTP, async (req, res) => {
  try {
    // Check validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Invalid input',
        errors: errors.array(),
      });
    }

    const { phoneNumber, otp } = req.body;

    // Verify OTP
    const otpRecord = await db.query(
      `SELECT * FROM otp_verifications 
       WHERE phone_number = $1 AND otp_code = $2 AND expires_at > NOW() AND verified = false
       ORDER BY created_at DESC LIMIT 1`,
      [phoneNumber, otp],
    );

    if (otpRecord.rows.length === 0) {
      // Increment attempts for existing OTP
      await db.query(
        `UPDATE otp_verifications 
         SET attempts = attempts + 1 
         WHERE phone_number = $1 AND expires_at > NOW()`,
        [phoneNumber],
      );

      return res.status(400).json({
        success: false,
        message: 'Invalid or expired OTP',
      });
    }

    // Mark OTP as verified
    await db.query(
      'UPDATE otp_verifications SET verified = true WHERE id = $1',
      [otpRecord.rows[0].id],
    );

    // Check if user exists
    let user = await db.query(
      'SELECT * FROM users WHERE phone_number = $1',
      [phoneNumber],
    );

    if (user.rows.length === 0) {
      // Create new user
      const newUser = await db.query(
        `INSERT INTO users (phone_number, verified, user_type)
         VALUES ($1, true, 'BUYER')
         RETURNING *`,
        [phoneNumber],
      );
      user = newUser;
    } else {
      // Update existing user
      await db.query(
        'UPDATE users SET verified = true, last_login_at = NOW() WHERE id = $1',
        [user.rows[0].id],
      );
    }

    const userData = user.rows[0];

    // Generate JWT tokens
    const accessToken = jwt.sign(
      {
        userId: userData.id,
        phoneNumber: userData.phone_number,
        userType: userData.user_type,
      },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '24h' },
    );

    const refreshToken = jwt.sign(
      { userId: userData.id },
      process.env.JWT_REFRESH_SECRET,
      { expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d' },
    );

    // Clean up old OTPs
    await db.query(
      'DELETE FROM otp_verifications WHERE phone_number = $1',
      [phoneNumber],
    );

    logger.info('User authenticated successfully', {
      userId: userData.id,
      phoneNumber: userData.phone_number,
    });

    // Response matches Android LoginResponse
    res.json({
      success: true,
      token: accessToken,
      refreshToken,
      user: {
        id: userData.id,
        phoneNumber: userData.phone_number,
        name: userData.name,
        userType: userData.user_type,
        location: userData.location,
        verified: userData.verified,
        createdAt: userData.created_at,
      },
    });
  } catch (error) {
    logger.error('Verify OTP error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error',
    });
  }
});

/**
 * POST /api/v1/auth/refresh-token
 * Refresh access token using refresh token
 */
router.post('/refresh-token', async (req, res) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        message: 'Refresh token is required',
      });
    }

    // Verify refresh token
    const decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET);

    // Get user data
    const user = await db.query(
      'SELECT * FROM users WHERE id = $1 AND is_active = true',
      [decoded.userId],
    );

    if (user.rows.length === 0) {
      return res.status(401).json({
        success: false,
        message: 'Invalid refresh token',
      });
    }

    const userData = user.rows[0];

    // Generate new access token
    const accessToken = jwt.sign(
      {
        userId: userData.id,
        phoneNumber: userData.phone_number,
        userType: userData.user_type,
      },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '24h' },
    );

    res.json({
      success: true,
      token: accessToken,
    });
  } catch (error) {
    logger.error('Refresh token error:', error);
    res.status(401).json({
      success: false,
      message: 'Invalid refresh token',
    });
  }
});

/**
 * POST /api/v1/auth/logout
 * Logout user (invalidate tokens on client side)
 */
router.post('/logout', async (req, res) => {
  // In a more sophisticated setup, you might maintain a blacklist of tokens
  // For now, we rely on client-side token removal
  res.json({
    success: true,
    message: 'Logged out successfully',
  });
});

module.exports = router;
