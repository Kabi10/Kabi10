const crypto = require('crypto');

/**
 * Generate random OTP code
 */
function generateOTP(length = 6) {
  const digits = '0123456789';
  let otp = '';
  
  for (let i = 0; i < length; i++) {
    otp += digits[Math.floor(Math.random() * digits.length)];
  }
  
  return otp;
}

/**
 * Validate Sri Lankan phone number format
 */
function isValidSriLankanPhone(phoneNumber) {
  // Sri Lankan mobile numbers: +94 followed by 9 digits
  // Valid prefixes: 70, 71, 72, 74, 75, 76, 77, 78
  const regex = /^\+94(70|71|72|74|75|76|77|78)[0-9]{7}$/;
  return regex.test(phoneNumber);
}

/**
 * Validate UUID format
 */
function validateUUID(uuid) {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(uuid);
}

/**
 * Format phone number for display
 */
function formatPhoneNumber(phoneNumber) {
  if (!phoneNumber) return '';
  
  // Convert +94XXXXXXXXX to +94 XX XXX XXXX
  if (phoneNumber.startsWith('+94') && phoneNumber.length === 12) {
    return `+94 ${phoneNumber.slice(3, 5)} ${phoneNumber.slice(5, 8)} ${phoneNumber.slice(8)}`;
  }
  
  return phoneNumber;
}

/**
 * Mask sensitive data for logging
 */
function maskSensitiveData(data, fields = ['password', 'token', 'otp', 'phoneNumber']) {
  if (typeof data !== 'object' || data === null) {
    return data;
  }

  const masked = { ...data };
  
  fields.forEach(field => {
    if (masked[field]) {
      if (field === 'phoneNumber') {
        masked[field] = maskPhoneNumber(masked[field]);
      } else {
        masked[field] = '***masked***';
      }
    }
  });

  return masked;
}

/**
 * Mask phone number for privacy
 */
function maskPhoneNumber(phoneNumber) {
  if (!phoneNumber || phoneNumber.length <= 4) return phoneNumber;
  
  const start = phoneNumber.substring(0, 3);
  const end = phoneNumber.substring(phoneNumber.length - 2);
  const middle = '*'.repeat(phoneNumber.length - 5);
  
  return `${start}${middle}${end}`;
}

/**
 * Generate secure random string
 */
function generateSecureToken(length = 32) {
  return crypto.randomBytes(length).toString('hex');
}

/**
 * Hash password using crypto
 */
function hashPassword(password, salt = null) {
  if (!salt) {
    salt = crypto.randomBytes(16).toString('hex');
  }
  
  const hash = crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
  return { hash, salt };
}

/**
 * Verify password against hash
 */
function verifyPassword(password, hash, salt) {
  const verifyHash = crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
  return hash === verifyHash;
}

/**
 * Calculate distance between two coordinates (Haversine formula)
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Earth's radius in kilometers
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);
  
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;
  
  return distance;
}

/**
 * Convert degrees to radians
 */
function toRadians(degrees) {
  return degrees * (Math.PI / 180);
}

/**
 * Validate email format
 */
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Sanitize string for database storage
 */
function sanitizeString(str) {
  if (typeof str !== 'string') return str;
  
  return str
    .trim()
    .replace(/[<>]/g, '') // Remove potential HTML tags
    .substring(0, 1000); // Limit length
}

/**
 * Format currency for Sri Lankan Rupees
 */
function formatCurrency(amount, currency = 'LKR') {
  if (typeof amount !== 'number') return amount;
  
  return new Intl.NumberFormat('en-LK', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2
  }).format(amount);
}

/**
 * Parse and validate date
 */
function parseDate(dateString) {
  if (!dateString) return null;
  
  const date = new Date(dateString);
  
  if (isNaN(date.getTime())) {
    throw new Error('Invalid date format');
  }
  
  return date;
}

/**
 * Check if date is in the future
 */
function isFutureDate(date) {
  const now = new Date();
  const checkDate = typeof date === 'string' ? new Date(date) : date;
  
  return checkDate > now;
}

/**
 * Get date range for queries
 */
function getDateRange(period = '7d') {
  const now = new Date();
  const start = new Date();
  
  switch (period) {
    case '1d':
      start.setDate(now.getDate() - 1);
      break;
    case '7d':
      start.setDate(now.getDate() - 7);
      break;
    case '30d':
      start.setDate(now.getDate() - 30);
      break;
    case '90d':
      start.setDate(now.getDate() - 90);
      break;
    default:
      start.setDate(now.getDate() - 7);
  }
  
  return { start, end: now };
}

/**
 * Paginate array
 */
function paginate(array, page = 1, limit = 10) {
  const offset = (page - 1) * limit;
  const paginatedItems = array.slice(offset, offset + limit);
  
  return {
    data: paginatedItems,
    pagination: {
      page: parseInt(page),
      limit: parseInt(limit),
      total: array.length,
      totalPages: Math.ceil(array.length / limit)
    }
  };
}

/**
 * Debounce function
 */
function debounce(func, wait) {
  let timeout;
  
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * Retry function with exponential backoff
 */
async function retry(fn, maxAttempts = 3, baseDelay = 1000) {
  let lastError;
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;
      
      if (attempt === maxAttempts) {
        throw lastError;
      }
      
      const delay = baseDelay * Math.pow(2, attempt - 1);
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
}

/**
 * Deep clone object
 */
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime());
  if (obj instanceof Array) return obj.map(item => deepClone(item));
  if (typeof obj === 'object') {
    const cloned = {};
    Object.keys(obj).forEach(key => {
      cloned[key] = deepClone(obj[key]);
    });
    return cloned;
  }
}

/**
 * Check if object is empty
 */
function isEmpty(obj) {
  if (obj === null || obj === undefined) return true;
  if (Array.isArray(obj)) return obj.length === 0;
  if (typeof obj === 'object') return Object.keys(obj).length === 0;
  if (typeof obj === 'string') return obj.trim().length === 0;
  return false;
}

module.exports = {
  generateOTP,
  isValidSriLankanPhone,
  validateUUID,
  formatPhoneNumber,
  maskSensitiveData,
  maskPhoneNumber,
  generateSecureToken,
  hashPassword,
  verifyPassword,
  calculateDistance,
  toRadians,
  isValidEmail,
  sanitizeString,
  formatCurrency,
  parseDate,
  isFutureDate,
  getDateRange,
  paginate,
  debounce,
  retry,
  deepClone,
  isEmpty
};
