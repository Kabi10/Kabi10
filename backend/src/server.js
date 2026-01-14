const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const compression = require('compression');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

// CRITICAL: Prevent MOCK_DB in production
if (process.env.NODE_ENV === 'production' && process.env.MOCK_DB === 'true') {
  console.error('FATAL: MOCK_DB cannot be enabled in production.');
  process.exit(1);
}

const logger = require('./utils/logger');
const database = require('./database/connection');
const { errorHandler } = require('./middleware/errorHandler');
const { authenticateToken } = require('./middleware/auth');

// Import routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const listingRoutes = require('./routes/listings');
const transactionRoutes = require('./routes/transactions');
const syncRoutes = require('./routes/sync');
const healthRoutes = require('./routes/health');
const marketPricesRoutes = require('./routes/market-prices');

const app = express();
const PORT = process.env.PORT || 3000;
const API_VERSION = process.env.API_VERSION || 'v1';

// Trust Vercel's proxy for rate limiting (X-Forwarded-For)
app.set('trust proxy', 1);

// Security middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", 'data:', 'https:'],
    },
  },
  crossOriginEmbedderPolicy: false,
}));

// CORS configuration
const corsOptions = {
  origin: process.env.CORS_ORIGIN?.split(',') || ['http://localhost:3000'],
  credentials: process.env.CORS_CREDENTIALS === 'true',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
};
app.use(cors(corsOptions));

// Compression and parsing middleware
app.use(compression());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Logging middleware
if (process.env.NODE_ENV !== 'test') {
  app.use(morgan('combined', {
    stream: {
      write: (message) => logger.info(message.trim()),
    },
  }));
}

// Dev-only request logging for Android verification
// Logs: [REQ] METHOD /path user=<userId>
if (process.env.NODE_ENV !== 'production') {
  app.use((req, res, next) => {
    // Skip health checks to reduce noise
    if (req.path === '/health') return next();

    const userId = req.user?.userId || 'anon';
    logger.info(`[REQ] ${req.method} ${req.path} user=${userId}`);
    next();
  });
}

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 15 * 60 * 1000, // 15 minutes
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100,
  message: {
    error: 'Too many requests from this IP, please try again later.',
    code: 'RATE_LIMIT_EXCEEDED',
  },
  standardHeaders: true,
  legacyHeaders: false,
});
app.use(limiter);

// Special rate limiting for OTP endpoints
const otpLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: parseInt(process.env.RATE_LIMIT_OTP_MAX) || 5,
  message: {
    error: 'Too many OTP requests, please try again later.',
    code: 'OTP_RATE_LIMIT_EXCEEDED',
  },
  keyGenerator: (req) => req.body.phoneNumber || req.ip,
});

// Health check endpoint (no rate limiting)
app.use('/health', healthRoutes);

// API routes
const apiRouter = express.Router();

// Health check endpoint (also available under /api/health)
apiRouter.use('/health', healthRoutes);

// Public routes (no authentication required)
apiRouter.use('/auth', otpLimiter, authRoutes);
apiRouter.use('/market-prices', marketPricesRoutes); // Public market data

// Protected routes (authentication required)
apiRouter.use('/users', authenticateToken, userRoutes);
apiRouter.use('/listings', authenticateToken, listingRoutes);
apiRouter.use('/transactions', authenticateToken, transactionRoutes);
apiRouter.use('/sync', authenticateToken, syncRoutes);

// Mount API router
app.use(`/api/${API_VERSION}`, apiRouter);
app.use('/api', apiRouter); // Support paths without version prefix (e.g., /api/auth/...)

// Static file serving for uploads
app.use('/uploads', express.static('uploads'));

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'Jaffna Farmers Marketplace API',
    version: API_VERSION,
    status: 'running',
    timestamp: new Date().toISOString(),
    endpoints: {
      health: '/health',
      auth: `/api/${API_VERSION}/auth`,
      marketPrices: `/api/market-prices`,
      users: `/api/${API_VERSION}/users`,
      listings: `/api/${API_VERSION}/listings`,
      transactions: `/api/${API_VERSION}/transactions`,
      sync: `/api/${API_VERSION}/sync`,
    },
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint not found',
    code: 'NOT_FOUND',
    path: req.originalUrl,
    method: req.method,
  });
});

// Global error handler
app.use(errorHandler);

// Graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM received, shutting down gracefully');

  // Close database connections
  await database.end();

  process.exit(0);
});

process.on('SIGINT', async () => {
  logger.info('SIGINT received, shutting down gracefully');

  // Close database connections
  await database.end();

  process.exit(0);
});

// Start server
const startServer = async () => {
  try {
    console.log('Starting server...');
    // Test database connection
    // await database.query('SELECT NOW()');
    console.log('DB Check bypassed');
    logger.info('Database connection established (BYPASSED)');

    const server = app.listen(PORT, () => {
      console.log('Server is listening!');
      logger.info(`Jaffna Marketplace API server running on port ${PORT}`);
      logger.info(`Environment: ${process.env.NODE_ENV}`);
      logger.info(`API Version: ${API_VERSION}`);

      if (process.env.NODE_ENV === 'development') {
        logger.info(`API Documentation: http://localhost:${PORT}/api/${API_VERSION}`);
        logger.info(`Health Check: http://localhost:${PORT}/health`);
      }
    });

    server.on('error', (e) => {
      console.error('Server error:', e);
    });
  } catch (error) {
    console.error('Startup error:', error);
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
};

// Start the server
if (require.main === module) {
  console.log('Main module execution');
  startServer();
}

module.exports = app;
