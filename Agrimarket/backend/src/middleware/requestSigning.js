/**
 * Request Signing Middleware for Sensitive Endpoints
 *
 * Implements HMAC-SHA256 signature validation to prevent:
 * - Request tampering
 * - Replay attacks
 * - Unauthorized API access
 *
 * Usage:
 * 1. Client generates signature: HMAC-SHA256(timestamp + method + path + body, secret)
 * 2. Client sends headers: X-Signature, X-Timestamp, X-Nonce (optional)
 * 3. Server validates signature and timestamp freshness
 */

const crypto = require("crypto");

// Configuration
const CONFIG = {
  // Maximum age of request in milliseconds (5 minutes)
  MAX_REQUEST_AGE_MS: 5 * 60 * 1000,

  // Signature algorithm
  ALGORITHM: "sha256",

  // Header names
  HEADERS: {
    SIGNATURE: "x-signature",
    TIMESTAMP: "x-timestamp",
    NONCE: "x-nonce",
  },
};

// Nonce cache to prevent replay attacks (in-memory, per instance)
const nonceCache = new Map();

// Cleanup old nonces every 10 minutes
const NONCE_CLEANUP_INTERVAL = 10 * 60 * 1000;
setInterval(() => {
  const cutoff = Date.now() - CONFIG.MAX_REQUEST_AGE_MS;
  for (const [nonce, timestamp] of nonceCache.entries()) {
    if (timestamp < cutoff) {
      nonceCache.delete(nonce);
    }
  }
}, NONCE_CLEANUP_INTERVAL);

/**
 * Generate HMAC signature for a request
 *
 * @param {string} secret - The shared secret key
 * @param {string} timestamp - Unix timestamp in milliseconds
 * @param {string} method - HTTP method (GET, POST, etc.)
 * @param {string} path - Request path
 * @param {string} body - Request body (empty string for GET)
 * @returns {string} Base64-encoded HMAC signature
 */
function generateSignature(secret, timestamp, method, path, body = "") {
  const payload = `${timestamp}${method.toUpperCase()}${path}${body}`;
  const hmac = crypto.createHmac(CONFIG.ALGORITHM, secret);
  hmac.update(payload);
  return hmac.digest("base64");
}

/**
 * Validate request signature
 *
 * @param {Object} req - Request object
 * @param {string} secret - The shared secret key
 * @returns {Object} { valid: boolean, error?: string }
 */
function validateSignature(req, secret) {
  const signature = req.headers[CONFIG.HEADERS.SIGNATURE];
  const timestamp = req.headers[CONFIG.HEADERS.TIMESTAMP];
  const nonce = req.headers[CONFIG.HEADERS.NONCE];

  // Check required headers
  if (!signature) {
    return { valid: false, error: "Missing X-Signature header" };
  }

  if (!timestamp) {
    return { valid: false, error: "Missing X-Timestamp header" };
  }

  // Validate timestamp format
  const requestTime = parseInt(timestamp, 10);
  if (isNaN(requestTime)) {
    return { valid: false, error: "Invalid X-Timestamp format" };
  }

  // Check timestamp freshness (prevent replay attacks)
  const now = Date.now();
  const age = Math.abs(now - requestTime);
  if (age > CONFIG.MAX_REQUEST_AGE_MS) {
    return {
      valid: false,
      error: `Request expired. Timestamp age: ${Math.round(age / 1000)}s, max allowed: ${CONFIG.MAX_REQUEST_AGE_MS / 1000}s`,
    };
  }

  // Check nonce if provided (additional replay protection)
  if (nonce) {
    if (nonceCache.has(nonce)) {
      return {
        valid: false,
        error: "Duplicate nonce - possible replay attack",
      };
    }
    nonceCache.set(nonce, requestTime);
  }

  // Generate expected signature
  const path = req.url || req.path || "/";
  const body = req.body
    ? typeof req.body === "string"
      ? req.body
      : JSON.stringify(req.body)
    : "";
  const expectedSignature = generateSignature(
    secret,
    timestamp,
    req.method,
    path,
    body,
  );

  // Constant-time comparison to prevent timing attacks
  const signatureBuffer = Buffer.from(signature, "base64");
  const expectedBuffer = Buffer.from(expectedSignature, "base64");

  if (signatureBuffer.length !== expectedBuffer.length) {
    return { valid: false, error: "Invalid signature" };
  }

  if (!crypto.timingSafeEqual(signatureBuffer, expectedBuffer)) {
    return { valid: false, error: "Invalid signature" };
  }

  return { valid: true };
}

/**
 * Request signing middleware for Vercel serverless functions
 *
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {boolean} True if signature is valid, false otherwise (response already sent)
 */
function requireSignedRequest(req, res) {
  const secret = process.env.REQUEST_SIGNING_SECRET;

  if (!secret) {
    // In development, skip signature validation if secret not configured
    if (process.env.NODE_ENV === "development") {
      console.warn(
        "⚠️  REQUEST_SIGNING_SECRET not configured - skipping signature validation",
      );
      return true;
    }

    res.status(500).json({
      success: false,
      message: "Server configuration error",
      code: "SIGNING_NOT_CONFIGURED",
    });
    return false;
  }

  const result = validateSignature(req, secret);

  if (!result.valid) {
    res.status(401).json({
      success: false,
      message: result.error,
      code: "INVALID_SIGNATURE",
    });
    return false;
  }

  return true;
}

module.exports = {
  generateSignature,
  validateSignature,
  requireSignedRequest,
  CONFIG,
};
