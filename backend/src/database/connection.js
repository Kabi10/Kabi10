const { Pool } = require('pg');
const logger = require('../utils/logger');

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
const pool = new Pool(dbConfig);

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

// Database helper functions
const db = {
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

module.exports = db;
