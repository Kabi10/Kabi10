const jwt = require('jsonwebtoken');
const { supabaseAdmin } = require('../../src/config/supabase');
const { isValidSriLankanPhone } = require('../../src/utils/helpers');
const logger = require('../../src/utils/logger');

/**
 * Vercel Serverless Function: Verify OTP
 * POST /api/auth/verify-otp
 * Matches original Express route exactly
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

  if (req.method !== 'POST') {
    return res.status(405).json({
      success: false,
      message: 'Method not allowed'
    });
  }

  try {
    const { phoneNumber, phone, otp } = req.body;
    const userPhone = phoneNumber || phone; // Handle both formats

    // Validate input
    if (!userPhone || !otp) {
      return res.status(400).json({
        success: false,
        message: 'Phone number and OTP are required'
      });
    }

    if (!isValidSriLankanPhone(userPhone)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid phone number format'
      });
    }

    if (otp.length !== 6 || !/^\d+$/.test(otp)) {
      return res.status(400).json({
        success: false,
        message: 'OTP must be 6 digits'
      });
    }

    // Verify OTP
    const { data: otpRecord, error: otpError } = await supabaseAdmin
      .from('otp_verifications')
      .select('*')
      .eq('phone_number', userPhone)
      .eq('otp_code', otp)
      .gt('expires_at', new Date().toISOString())
      .eq('verified', false)
      .order('created_at', { ascending: false })
      .limit(1);

    if (otpError || !otpRecord || otpRecord.length === 0) {
      // Increment attempts for existing OTP
      await supabaseAdmin
        .from('otp_verifications')
        .update({ attempts: supabaseAdmin.raw('attempts + 1') })
        .eq('phone_number', userPhone)
        .gt('expires_at', new Date().toISOString());

      return res.status(400).json({
        success: false,
        message: 'Invalid or expired OTP'
      });
    }

    // Mark OTP as verified
    await supabaseAdmin
      .from('otp_verifications')
      .update({ verified: true })
      .eq('id', otpRecord[0].id);

    // Check if user exists
    const { data: existingUser } = await supabaseAdmin
      .from('users')
      .select('*')
      .eq('phone_number', userPhone)
      .single();

    let userData;

    if (!existingUser) {
      // Create new user
      const { data: newUser, error: createError } = await supabaseAdmin
        .from('users')
        .insert({
          phone_number: userPhone,
          verified: true,
          user_type: 'BUYER' // Default to buyer
        })
        .select()
        .single();

      if (createError) {
        logger.error('Failed to create user:', createError);
        return res.status(500).json({
          success: false,
          message: 'Failed to create user account'
        });
      }

      userData = newUser;
    } else {
      // Update existing user
      const { data: updatedUser, error: updateError } = await supabaseAdmin
        .from('users')
        .update({ 
          verified: true, 
          last_login_at: new Date().toISOString() 
        })
        .eq('id', existingUser.id)
        .select()
        .single();

      if (updateError) {
        logger.error('Failed to update user:', updateError);
        return res.status(500).json({
          success: false,
          message: 'Failed to update user account'
        });
      }

      userData = updatedUser;
    }

    // Generate JWT tokens
    let accessToken, refreshToken;

    try {
      if (!process.env.JWT_SECRET) {
        throw new Error('JWT_SECRET not configured');
      }

      accessToken = jwt.sign(
        {
          userId: userData.id,
          phoneNumber: userData.phone_number,
          userType: userData.user_type
        },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '24h' }
      );

      refreshToken = jwt.sign(
        { userId: userData.id },
        process.env.JWT_REFRESH_SECRET || process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d' }
      );
    } catch (jwtError) {
      console.error('JWT generation error:', jwtError);
      return res.status(500).json({
        success: false,
        message: 'Authentication token generation failed'
      });
    }

    // Clean up old OTPs
    await supabaseAdmin
      .from('otp_verifications')
      .delete()
      .eq('phone_number', userPhone);

    logger.info('User authenticated successfully', { 
      userId: userData.id, 
      phoneNumber: userData.phone_number 
    });

    // Response matches Android LoginResponse exactly
    res.json({
      success: true,
      token: accessToken,
      refreshToken: refreshToken,
      user: {
        id: userData.id,
        phoneNumber: userData.phone_number,
        name: userData.name,
        userType: userData.user_type,
        location: userData.location,
        verified: userData.verified,
        createdAt: userData.created_at
      }
    });
  } catch (error) {
    console.error('Verify OTP error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      debug: error.message
    });
  }
};
