/**
 * Rate Limiting Middleware for Vercel Serverless Functions
 * Uses in-memory store with IP-based limiting
 *
 * For production with multiple instances, consider:
 * - Vercel KV (Redis)
 * - Upstash Redis
 * - Supabase for persistent rate limit storage
 */

// In-memory store for rate limiting (per serverless instance)
// Note: In serverless, each instance has its own memory, so this provides
// per-instance limiting. For stricter limits, use a distributed store.
const rateLimitStore = new Map();

// Cleanup old entries every 5 minutes
const CLEANUP_INTERVAL = 5 * 60 * 1000;
setInterval(() => {
  const now = Date.now();
  for (const [key, value] of rateLimitStore.entries()) {
    if (now > value.resetTime) {
      rateLimitStore.delete(key);
    }
  }
}, CLEANUP_INTERVAL);

/**
 * Rate limit configurations for different endpoint types
 */
const RATE_LIMITS = {
  // Auth endpoints - stricter limits to prevent brute force
  auth: {
    windowMs: 15 * 60 * 1000, // 15 minutes
    maxRequests: 10, // 10 requests per window
    message:
      "Too many authentication attempts. Please try again in 15 minutes.",
  },

  // OTP endpoints - very strict to prevent abuse
  otp: {
    windowMs: 60 * 60 * 1000, // 1 hour
    maxRequests: 5, // 5 OTP requests per hour
    message: "Too many OTP requests. Please try again later.",
  },

  // General API endpoints
  api: {
    windowMs: 60 * 1000, // 1 minute
    maxRequests: 60, // 60 requests per minute
    message: "Too many requests. Please slow down.",
  },

  // Write operations (create, update, delete)
  write: {
    windowMs: 60 * 1000, // 1 minute
    maxRequests: 20, // 20 writes per minute
    message: "Too many write operations. Please slow down.",
  },

  // Search/filter operations
  search: {
    windowMs: 60 * 1000, // 1 minute
    maxRequests: 30, // 30 searches per minute
    message: "Too many search requests. Please slow down.",
  },
};

/**
 * Get client IP from request
 * Handles Vercel's x-forwarded-for header
 */
function getClientIP(req) {
  const forwardedFor = req.headers["x-forwarded-for"];
  if (forwardedFor) {
    return forwardedFor.split(",")[0].trim();
  }
  return req.headers["x-real-ip"] || req.connection?.remoteAddress || "unknown";
}

/**
 * Rate limiting function for serverless endpoints
 *
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @param {string} limitType - Type of rate limit to apply ('auth', 'otp', 'api', 'write', 'search')
 * @returns {Object} { allowed: boolean, remaining: number, resetTime: number }
 */
function checkRateLimit(req, limitType = "api") {
  const config = RATE_LIMITS[limitType] || RATE_LIMITS.api;
  const clientIP = getClientIP(req);
  const key = `${limitType}:${clientIP}`;
  const now = Date.now();

  // Get or create rate limit entry
  let entry = rateLimitStore.get(key);

  if (!entry || now > entry.resetTime) {
    // Create new window
    entry = {
      count: 1,
      resetTime: now + config.windowMs,
    };
    rateLimitStore.set(key, entry);

    return {
      allowed: true,
      remaining: config.maxRequests - 1,
      resetTime: entry.resetTime,
      limit: config.maxRequests,
    };
  }

  // Increment count
  entry.count++;
  rateLimitStore.set(key, entry);

  const remaining = Math.max(0, config.maxRequests - entry.count);
  const allowed = entry.count <= config.maxRequests;

  return {
    allowed,
    remaining,
    resetTime: entry.resetTime,
    limit: config.maxRequests,
    message: allowed ? null : config.message,
  };
}

/**
 * Apply rate limit headers to response
 */
function setRateLimitHeaders(res, rateLimitResult) {
  res.setHeader("X-RateLimit-Limit", rateLimitResult.limit);
  res.setHeader("X-RateLimit-Remaining", rateLimitResult.remaining);
  res.setHeader(
    "X-RateLimit-Reset",
    Math.ceil(rateLimitResult.resetTime / 1000),
  );
}

/**
 * Rate limit middleware wrapper for serverless functions
 * Returns a function that checks rate limits and returns 429 if exceeded
 *
 * @param {string} limitType - Type of rate limit ('auth', 'otp', 'api', 'write', 'search')
 * @returns {Function} Middleware function
 */
function rateLimit(limitType = "api") {
  return (req, res) => {
    const result = checkRateLimit(req, limitType);
    setRateLimitHeaders(res, result);

    if (!result.allowed) {
      res.status(429).json({
        success: false,
        message: result.message,
        retryAfter: Math.ceil((result.resetTime - Date.now()) / 1000),
      });
      return false;
    }

    return true;
  };
}

module.exports = {
  checkRateLimit,
  setRateLimitHeaders,
  rateLimit,
  RATE_LIMITS,
  getClientIP,
};
