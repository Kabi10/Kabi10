const axios = require('axios');
const logger = require('../utils/logger');

/**
 * SMS Service for Sri Lankan providers
 * Supports Dialog Ideamart, Mobitel mSpace, and Twilio fallback
 */
class SMSService {
  constructor() {
    this.provider = process.env.SMS_PROVIDER || 'dialog';
    this.mockMode = process.env.MOCK_SMS === 'true' || process.env.NODE_ENV === 'development';
  }

  /**
   * Send SMS using configured provider
   */
  async sendSMS(phoneNumber, message) {
    if (this.mockMode) {
      return this.mockSend(phoneNumber, message);
    }

    try {
      switch (this.provider) {
        case 'dialog':
          return await this.sendViaDialog(phoneNumber, message);
        case 'mobitel':
          return await this.sendViaMobitel(phoneNumber, message);
        case 'twilio':
          return await this.sendViaTwilio(phoneNumber, message);
        default:
          throw new Error(`Unknown SMS provider: ${this.provider}`);
      }
    } catch (error) {
      logger.error('SMS sending failed:', { provider: this.provider, error: error.message });

      // Try fallback provider if primary fails
      if (this.provider !== 'twilio') {
        logger.info('Attempting SMS fallback to Twilio');
        try {
          return await this.sendViaTwilio(phoneNumber, message);
        } catch (fallbackError) {
          logger.error('SMS fallback also failed:', fallbackError);
          throw fallbackError;
        }
      }

      throw error;
    }
  }

  /**
   * Send SMS via Dialog Ideamart (Primary Sri Lankan provider)
   */
  async sendViaDialog(phoneNumber, message) {
    const apiUrl = process.env.DIALOG_API_URL || 'https://api.dialog.lk/sms/send';
    const apiKey = process.env.DIALOG_API_KEY;
    const apiSecret = process.env.DIALOG_API_SECRET;
    const senderId = process.env.DIALOG_SENDER_ID || 'JaffnaFarm';

    if (!apiKey || !apiSecret) {
      throw new Error('Dialog API credentials not configured');
    }

    // Format phone number for Dialog (remove +94, add 0)
    const formattedNumber = phoneNumber.replace('+94', '0');

    const payload = {
      to: formattedNumber,
      message,
      sender: senderId,
    };

    const config = {
      method: 'POST',
      url: apiUrl,
      headers: {
        Authorization: `Bearer ${apiKey}`,
        'Content-Type': 'application/json',
      },
      data: payload,
      timeout: 10000,
    };

    const response = await axios(config);

    if (response.data.status === 'success' || response.status === 200) {
      logger.info('SMS sent via Dialog', {
        to: this.maskPhoneNumber(phoneNumber),
        messageId: response.data.messageId,
      });
      return {
        success: true,
        provider: 'dialog',
        messageId: response.data.messageId,
      };
    }
    throw new Error(`Dialog API error: ${response.data.message || 'Unknown error'}`);
  }

  /**
   * Send SMS via Mobitel mSpace (Alternative Sri Lankan provider)
   */
  async sendViaMobitel(phoneNumber, message) {
    const apiUrl = process.env.MOBITEL_API_URL || 'https://api.mobitel.lk/sms';
    const username = process.env.MOBITEL_USERNAME;
    const password = process.env.MOBITEL_PASSWORD;

    if (!username || !password) {
      throw new Error('Mobitel API credentials not configured');
    }

    // Format phone number for Mobitel
    const formattedNumber = phoneNumber.replace('+94', '94');

    const payload = {
      username,
      password,
      to: formattedNumber,
      message,
      from: 'JaffnaFarm',
    };

    const config = {
      method: 'POST',
      url: `${apiUrl}/send`,
      headers: {
        'Content-Type': 'application/json',
      },
      data: payload,
      timeout: 10000,
    };

    const response = await axios(config);

    if (response.data.status === 'OK' || response.status === 200) {
      logger.info('SMS sent via Mobitel', {
        to: this.maskPhoneNumber(phoneNumber),
        messageId: response.data.messageId,
      });
      return {
        success: true,
        provider: 'mobitel',
        messageId: response.data.messageId,
      };
    }
    throw new Error(`Mobitel API error: ${response.data.message || 'Unknown error'}`);
  }

  /**
   * Send SMS via Twilio (International fallback)
   */
  async sendViaTwilio(phoneNumber, message) {
    const accountSid = process.env.TWILIO_ACCOUNT_SID;
    const authToken = process.env.TWILIO_AUTH_TOKEN;
    const fromNumber = process.env.TWILIO_PHONE_NUMBER;

    if (!accountSid || !authToken || !fromNumber) {
      throw new Error('Twilio credentials not configured');
    }

    const apiUrl = `https://api.twilio.com/2010-04-01/Accounts/${accountSid}/Messages.json`;

    const payload = new URLSearchParams({
      To: phoneNumber,
      From: fromNumber,
      Body: message,
    });

    const config = {
      method: 'POST',
      url: apiUrl,
      headers: {
        Authorization: `Basic ${Buffer.from(`${accountSid}:${authToken}`).toString('base64')}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      data: payload,
      timeout: 10000,
    };

    const response = await axios(config);

    if (response.status === 201) {
      logger.info('SMS sent via Twilio', {
        to: this.maskPhoneNumber(phoneNumber),
        sid: response.data.sid,
      });
      return {
        success: true,
        provider: 'twilio',
        messageId: response.data.sid,
      };
    }
    throw new Error(`Twilio API error: ${response.data.message || 'Unknown error'}`);
  }

  /**
   * Mock SMS sending for development/testing
   */
  async mockSend(phoneNumber, message) {
    logger.info('Mock SMS sent', {
      to: this.maskPhoneNumber(phoneNumber),
      message: `${message.substring(0, 50)}...`,
    });

    // Simulate API delay
    await new Promise((resolve) => setTimeout(resolve, 500));

    return {
      success: true,
      provider: 'mock',
      messageId: `mock_${Date.now()}`,
    };
  }

  /**
   * Mask phone number for logging (privacy)
   */
  maskPhoneNumber(phoneNumber) {
    if (phoneNumber.length <= 4) return phoneNumber;
    const start = phoneNumber.substring(0, 3);
    const end = phoneNumber.substring(phoneNumber.length - 2);
    const middle = '*'.repeat(phoneNumber.length - 5);
    return `${start}${middle}${end}`;
  }

  /**
   * Validate phone number format
   */
  isValidPhoneNumber(phoneNumber) {
    // Sri Lankan phone number format: +94XXXXXXXXX
    const sriLankanRegex = /^\+94[0-9]{9}$/;
    return sriLankanRegex.test(phoneNumber);
  }

  /**
   * Get SMS cost estimate (in LKR)
   */
  getCostEstimate(provider = this.provider) {
    const costs = {
      dialog: 0.50, // ~0.50 LKR per SMS
      mobitel: 0.45, // ~0.45 LKR per SMS
      twilio: 2.50, // ~2.50 LKR per SMS (international rates)
    };

    return costs[provider] || 1.00;
  }

  /**
   * Get provider status and capabilities
   */
  getProviderInfo() {
    return {
      current: this.provider,
      mockMode: this.mockMode,
      available: {
        dialog: {
          name: 'Dialog Ideamart',
          configured: !!(process.env.DIALOG_API_KEY && process.env.DIALOG_API_SECRET),
          costPerSMS: 0.50,
          coverage: 'Sri Lanka',
          recommended: true,
        },
        mobitel: {
          name: 'Mobitel mSpace',
          configured: !!(process.env.MOBITEL_USERNAME && process.env.MOBITEL_PASSWORD),
          costPerSMS: 0.45,
          coverage: 'Sri Lanka',
          recommended: true,
        },
        twilio: {
          name: 'Twilio',
          configured: !!(process.env.TWILIO_ACCOUNT_SID && process.env.TWILIO_AUTH_TOKEN),
          costPerSMS: 2.50,
          coverage: 'International',
          recommended: false,
        },
      },
    };
  }
}

// Export singleton instance
module.exports = new SMSService();
