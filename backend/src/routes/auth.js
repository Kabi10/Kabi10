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

    // Check for recent OTP requests (rate limiting - 60 second cooldown)
    const recentOTP = await db.query(
      `SELECT id, created_at FROM otp_verifications
       WHERE phone_number = $1 AND created_at > NOW() - INTERVAL '1 minute'
       ORDER BY created_at DESC LIMIT 1`,
      [phoneNumber],
    );

    if (recentOTP.rows.length > 0) {
      const createdAt = new Date(recentOTP.rows[0].created_at);
      const waitSeconds = 60 - Math.floor((Date.now() - createdAt.getTime()) / 1000);
      return res.status(429).json({
        success: false,
        message: `Please wait ${waitSeconds} seconds before requesting another OTP`,
        retryAfterSeconds: waitSeconds,
      });
    }

    // Generate OTP
    const otpCode = generateOTP();
    const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // 5 minutes

    // Store OTP in database and get the ID
    const otpResult = await db.query(
      `INSERT INTO otp_verifications (phone_number, otp_code, expires_at, attempts)
       VALUES ($1, $2, $3, 0)
       RETURNING id`,
      [phoneNumber, otpCode, expiresAt],
    );

    const otpId = otpResult.rows[0].id;

    // Send SMS (or mock)
    const message = `Your Jaffna Marketplace verification code is: ${otpCode}. Valid for 5 minutes. Do not share this code.`;

    try {
      const smsResult = await smsService.sendSMS(phoneNumber, message, otpCode);

      logger.info('OTP generated', {
        phoneNumber: phoneNumber.replace(/(.{4}).*(.{2})$/, '$1****$2'),
        otpId,
        mockMode: smsService.isMockMode(),
      });

      // Response for Android
      const response = {
        success: true,
        message: 'OTP sent successfully',
        otpId: otpId, // Android needs this for verify-otp
      };

      // In mock mode, also return OTP for testing convenience
      if (smsService.isMockMode()) {
        response.otp = otpCode;
      }

      res.json(response);
    } catch (smsError) {
      logger.error('Failed to send SMS:', smsError);

      // Delete the OTP record since SMS failed
      await db.query('DELETE FROM otp_verifications WHERE id = $1', [otpId]);

      res.status(500).json({
        success: false,
        message: 'Failed to send OTP. Please try again.',
      });
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
 *
 * Accepts both `phone` and `phoneNumber` for compatibility
 * Max 3 attempts per OTP, then must request new OTP
 */
router.post('/verify-otp', async (req, res) => {
  try {
    // Accept both phone and phoneNumber for Android compatibility
    const phoneNumber = req.body.phoneNumber || req.body.phone;
    const { otp, otpId } = req.body;

    // Validate inputs
    if (!phoneNumber || !/^\+94[0-9]{9}$/.test(phoneNumber)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid phone number format. Use +94XXXXXXXXX',
      });
    }

    if (!otp || !/^[0-9]{6}$/.test(otp)) {
      return res.status(400).json({
        success: false,
        message: 'OTP must be 6 digits',
      });
    }

    // Find the OTP record (optionally by ID if provided)
    let otpQuery = `
      SELECT id, otp_code, attempts, verified
      FROM otp_verifications
      WHERE phone_number = $1 AND expires_at > NOW() AND verified = false
      ORDER BY created_at DESC LIMIT 1`;
    let otpParams = [phoneNumber];

    if (otpId) {
      otpQuery = `
        SELECT id, otp_code, attempts, verified
        FROM otp_verifications
        WHERE id = $1 AND phone_number = $2 AND expires_at > NOW() AND verified = false`;
      otpParams = [otpId, phoneNumber];
    }

    const existingOtp = await db.query(otpQuery, otpParams);

    if (existingOtp.rows.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'OTP expired or not found. Please request a new OTP.',
      });
    }

    const otpRecord = existingOtp.rows[0];
    const MAX_ATTEMPTS = 3;

    // Check attempt limit FIRST
    if (otpRecord.attempts >= MAX_ATTEMPTS) {
      // Mark OTP as used/invalid
      await db.query(
        'UPDATE otp_verifications SET verified = true WHERE id = $1',
        [otpRecord.id],
      );

      return res.status(400).json({
        success: false,
        message: 'Too many attempts. Please request a new OTP.',
        attemptsExceeded: true,
      });
    }

    // Verify OTP code
    if (otpRecord.otp_code !== otp) {
      // Increment attempts
      const newAttempts = otpRecord.attempts + 1;
      await db.query(
        'UPDATE otp_verifications SET attempts = $1 WHERE id = $2',
        [newAttempts, otpRecord.id],
      );

      const remainingAttempts = MAX_ATTEMPTS - newAttempts;

      return res.status(400).json({
        success: false,
        message: remainingAttempts > 0
          ? `Invalid OTP. ${remainingAttempts} attempt${remainingAttempts !== 1 ? 's' : ''} remaining.`
          : 'Invalid OTP. No attempts remaining. Please request a new OTP.',
        remainingAttempts,
      });
    }

    // OTP is valid - mark as verified
    await db.query(
      'UPDATE otp_verifications SET verified = true WHERE id = $1',
      [otpRecord.id],
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

    // Clean up all OTPs for this phone number
    await db.query(
      'DELETE FROM otp_verifications WHERE phone_number = $1',
      [phoneNumber],
    );

    logger.info('User authenticated successfully', {
      userId: userData.id,
      phoneNumber: phoneNumber.replace(/(.{4}).*(.{2})$/, '$1****$2'),
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
