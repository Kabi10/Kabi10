const { Pool } = require('pg');
const logger = require('../utils/logger');
const crypto = require('crypto');

// Check if mock mode is enabled
const MOCK_DB = process.env.MOCK_DB === 'true';

// In-memory storage for mock mode
const mockStorage = {
  otpVerifications: [],
  users: [],
  listings: [],
  transactions: [],
};

// Helper to generate UUID
const generateUUID = () => crypto.randomUUID();

// Mock database implementation
const mockDb = {
  async query(text, params = []) {
    logger.debug('Mock DB query:', { text: text.substring(0, 80), params });

    // Handle OTP queries
    if (text.includes('FROM otp_verifications') && text.includes('created_at > NOW()')) {
      // Check recent OTP (rate limiting)
      const phoneNumber = params[0];
      const recent = mockStorage.otpVerifications.find(
        o => o.phone_number === phoneNumber &&
        Date.now() - new Date(o.created_at).getTime() < 60000
      );
      return { rows: recent ? [recent] : [], rowCount: recent ? 1 : 0 };
    }

    if (text.includes('INSERT INTO otp_verifications')) {
      const id = generateUUID();
      const otp = {
        id,
        phone_number: params[0],
        otp_code: params[1],
        expires_at: params[2],
        attempts: 0,
        verified: false,
        created_at: new Date().toISOString(),
      };
      mockStorage.otpVerifications.push(otp);
      return { rows: [{ id }], rowCount: 1 };
    }

    if (text.includes('FROM otp_verifications') && text.includes('expires_at > NOW()')) {
      const phoneNumber = params.find(p => typeof p === 'string' && p.startsWith('+94'));
      const otp = mockStorage.otpVerifications.find(
        o => o.phone_number === phoneNumber && !o.verified && new Date(o.expires_at) > new Date()
      );
      return { rows: otp ? [otp] : [], rowCount: otp ? 1 : 0 };
    }

    if (text.includes('UPDATE otp_verifications SET verified')) {
      const id = params[params.length - 1];
      const otp = mockStorage.otpVerifications.find(o => o.id === id);
      if (otp) otp.verified = true;
      return { rows: [], rowCount: 1 };
    }

    if (text.includes('UPDATE otp_verifications SET attempts')) {
      const id = params[1];
      const otp = mockStorage.otpVerifications.find(o => o.id === id);
      if (otp) otp.attempts = params[0];
      return { rows: [], rowCount: 1 };
    }

    if (text.includes('DELETE FROM otp_verifications')) {
      const phoneNumber = params[0];
      mockStorage.otpVerifications = mockStorage.otpVerifications.filter(
        o => o.phone_number !== phoneNumber
      );
      return { rows: [], rowCount: 1 };
    }

    // Handle user queries
    if (text.includes('FROM users WHERE phone_number')) {
      const phoneNumber = params[0];
      const user = mockStorage.users.find(u => u.phone_number === phoneNumber);
      return { rows: user ? [user] : [], rowCount: user ? 1 : 0 };
    }

    if (text.includes('FROM users WHERE id')) {
      const userId = params[0];
      const user = mockStorage.users.find(u => u.id === userId);
      // Add can_sell field for listing creation check
      if (user) {
        user.can_sell = true;
      }
      return { rows: user ? [user] : [], rowCount: user ? 1 : 0 };
    }

    if (text.includes('INSERT INTO users')) {
      const id = generateUUID();
      const user = {
        id,
        phone_number: params[0],
        verified: true,
        user_type: 'FARMER',
        name: null,
        location: 'Jaffna',
        is_active: true,
        created_at: new Date().toISOString(),
        last_login_at: new Date().toISOString(),
      };
      mockStorage.users.push(user);
      return { rows: [user], rowCount: 1 };
    }

    if (text.includes('UPDATE users SET verified')) {
      return { rows: [], rowCount: 1 };
    }

    // Handle listings queries
    if (text.includes('FROM listings') && !text.includes('INSERT') && !text.includes('COUNT(*)')) {
      // Return listings with mock farmer data joined
      const listings = mockStorage.listings.map(l => ({
        ...l,
        farmer_name: 'Mock Farmer',
        farmer_contact: '+94771234567',
      }));
      return {
        rows: listings,
        rowCount: listings.length
      };
    }

    if (text.includes('COUNT(*)') && text.includes('listings')) {
      return { rows: [{ total: String(mockStorage.listings.length) }], rowCount: 1 };
    }

    if (text.includes('INSERT INTO listings')) {
      const id = generateUUID();
      // Match the actual INSERT query params order from listings.js:
      // farmer_id, crop_type, quantity, unit, price_per_unit,
      // quality, location, pickup_locations, available_from,
      // available_until, description, images,
      // story, farming_methods, certifications, harvested_at, sustainability_practices
      const listing = {
        id,
        farmer_id: params[0],
        crop_type: params[1],
        quantity: params[2],
        unit: params[3],
        price_per_unit: params[4],
        quality: params[5] || 'B',
        location: params[6] || 'Unknown',
        pickup_locations: params[7] || [],
        available_from: params[8] || new Date().toISOString().split('T')[0],
        available_until: params[9] || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        description: params[10] || '',
        images: params[11] || [],
        story: params[12] || '',
        farming_methods: params[13] || [],
        certifications: params[14] || [],
        harvested_at: params[15] || null,
        sustainability_practices: params[16] || [],
        is_active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
        view_count: 0,
        inquiry_count: 0,
      };
      mockStorage.listings.push(listing);
      return { rows: [listing], rowCount: 1 };
    }

    // Handle transactions queries
    if (text.includes('FROM transactions') && !text.includes('INSERT') && !text.includes('COUNT(*)')) {
      return {
        rows: mockStorage.transactions,
        rowCount: mockStorage.transactions.length
      };
    }

    if (text.includes('COUNT(*)') && text.includes('transactions')) {
      return { rows: [{ total: String(mockStorage.transactions.length) }], rowCount: 1 };
    }

    // Default: return empty result
    logger.warn('Mock DB: Unhandled query', { text: text.substring(0, 100) });
    return { rows: [], rowCount: 0 };
  },

  async getClient() {
    return {
      query: this.query.bind(this),
      release: () => {},
    };
  },

  async transaction(callback) {
    const client = await this.getClient();
    return await callback(client);
  },

  async healthCheck() {
    return {
      status: 'healthy',
      timestamp: new Date().toISOString(),
      version: 'mock-db',
      pool: { totalCount: 1, idleCount: 1, waitingCount: 0 },
    };
  },

  async end() {
    logger.info('Mock database connection closed');
  },
};

// Only create real pool if not in mock mode
let pool = null;
if (!MOCK_DB) {
  // Database configuration
  const dbConfig = {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT) || 5432,
    database: process.env.DB_NAME || 'jaffna_marketplace',
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD,
    ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
    max: parseInt(process.env.DB_MAX_CONNECTIONS) || 20,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 2000,
  };

  // Create connection pool
  pool = new Pool(dbConfig);

  // Pool event handlers
  pool.on('connect', () => {
    logger.debug('New database client connected');
  });

  pool.on('error', (err) => {
    logger.error('Unexpected error on idle database client:', err);
  });

  pool.on('acquire', () => {
    logger.debug('Database client acquired from pool');
  });

  pool.on('remove', () => {
    logger.debug('Database client removed from pool');
  });
}

// Database helper functions for real PostgreSQL
const realDb = {
  // Execute a query
  async query(text, params) {
    const start = Date.now();
    try {
      const result = await pool.query(text, params);
      const duration = Date.now() - start;

      if (process.env.DEBUG_SQL === 'true') {
        logger.debug('Executed query', {
          text: text.substring(0, 100) + (text.length > 100 ? '...' : ''),
          duration: `${duration}ms`,
          rows: result.rowCount,
        });
      }

      return result;
    } catch (error) {
      logger.error('Database query error:', {
        error: error.message,
        query: text.substring(0, 100) + (text.length > 100 ? '...' : ''),
        params,
      });
      throw error;
    }
  },

  // Get a client from the pool for transactions
  async getClient() {
    try {
      const client = await pool.connect();

      // Add query method to client
      const originalQuery = client.query;
      client.query = async function (text, params) {
        const start = Date.now();
        try {
          const result = await originalQuery.call(this, text, params);
          const duration = Date.now() - start;

          if (process.env.DEBUG_SQL === 'true') {
            logger.debug('Executed transaction query', {
              text: text.substring(0, 100) + (text.length > 100 ? '...' : ''),
              duration: `${duration}ms`,
              rows: result.rowCount,
            });
          }

          return result;
        } catch (error) {
          logger.error('Transaction query error:', {
            error: error.message,
            query: text.substring(0, 100) + (text.length > 100 ? '...' : ''),
            params,
          });
          throw error;
        }
      };

      return client;
    } catch (error) {
      logger.error('Failed to get database client:', error);
      throw error;
    }
  },

  // Execute multiple queries in a transaction
  async transaction(callback) {
    const client = await this.getClient();

    try {
      await client.query('BEGIN');
      const result = await callback(client);
      await client.query('COMMIT');
      return result;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  },

  // Check if database is healthy
  async healthCheck() {
    try {
      const result = await this.query('SELECT NOW() as current_time, version() as version');
      return {
        status: 'healthy',
        timestamp: result.rows[0].current_time,
        version: result.rows[0].version,
        pool: {
          totalCount: pool.totalCount,
          idleCount: pool.idleCount,
          waitingCount: pool.waitingCount,
        },
      };
    } catch (error) {
      return {
        status: 'unhealthy',
        error: error.message,
      };
    }
  },

  // Close all connections
  async end() {
    try {
      await pool.end();
      logger.info('Database connection pool closed');
    } catch (error) {
      logger.error('Error closing database connection pool:', error);
      throw error;
    }
  },
};

// Export mock or real database based on MOCK_DB env
const db = MOCK_DB ? mockDb : realDb;

if (MOCK_DB) {
  logger.info('Database running in MOCK mode - no real database connection');
}

module.exports = db;
