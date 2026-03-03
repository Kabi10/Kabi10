/* eslint-disable max-classes-per-file */
const logger = require("../utils/logger");

/**
 * Global error handling middleware
 * Catches all unhandled errors and returns appropriate responses
 */
const errorHandler = (error, req, res, next) => {
  let statusCode = 500;
  let message = "Internal server error";
  let code = "INTERNAL_ERROR";
  let details = null;

  // Log the error
  logger.logError(error, {
    method: req.method,
    url: req.originalUrl,
    userAgent: req.get("User-Agent"),
    ip: req.ip,
    userId: req.user?.userId,
    body: req.body,
    params: req.params,
    query: req.query,
  });

  // Handle specific error types
  if (error.name === "ValidationError") {
    statusCode = 400;
    message = "Validation failed";
    code = "VALIDATION_ERROR";
    details = error.details || error.message;
  } else if (error.name === "JsonWebTokenError") {
    statusCode = 401;
    message = "Invalid token";
    code = "INVALID_TOKEN";
  } else if (error.name === "TokenExpiredError") {
    statusCode = 401;
    message = "Token expired";
    code = "TOKEN_EXPIRED";
  } else if (error.name === "UnauthorizedError") {
    statusCode = 401;
    message = "Unauthorized";
    code = "UNAUTHORIZED";
  } else if (error.name === "ForbiddenError") {
    statusCode = 403;
    message = "Forbidden";
    code = "FORBIDDEN";
  } else if (error.name === "NotFoundError") {
    statusCode = 404;
    message = "Resource not found";
    code = "NOT_FOUND";
  } else if (error.name === "ConflictError") {
    statusCode = 409;
    message = "Resource conflict";
    code = "CONFLICT";
  } else if (error.name === "TooManyRequestsError") {
    statusCode = 429;
    message = "Too many requests";
    code = "RATE_LIMIT_EXCEEDED";
  } else if (error.code === "23505") {
    // PostgreSQL unique violation
    statusCode = 409;
    message = "Resource already exists";
    code = "DUPLICATE_RESOURCE";

    // Extract field name from error detail if available
    if (error.detail) {
      const match = error.detail.match(/Key \(([^)]+)\)/);
      if (match) {
        details = `Duplicate value for field: ${match[1]}`;
      }
    }
  } else if (error.code === "23503") {
    // PostgreSQL foreign key violation
    statusCode = 400;
    message = "Invalid reference";
    code = "INVALID_REFERENCE";
  } else if (error.code === "23514") {
    // PostgreSQL check constraint violation
    statusCode = 400;
    message = "Invalid data";
    code = "CONSTRAINT_VIOLATION";
  } else if (error.code === "ECONNREFUSED") {
    statusCode = 503;
    message = "Service unavailable";
    code = "SERVICE_UNAVAILABLE";
  } else if (error.code === "ETIMEDOUT") {
    statusCode = 504;
    message = "Request timeout";
    code = "TIMEOUT";
  } else if (error.type === "entity.parse.failed") {
    statusCode = 400;
    message = "Invalid JSON";
    code = "INVALID_JSON";
  } else if (error.type === "entity.too.large") {
    statusCode = 413;
    message = "Request entity too large";
    code = "PAYLOAD_TOO_LARGE";
  }

  // Handle custom application errors
  if (error.statusCode) {
    statusCode = error.statusCode;
  }
  if (error.message) {
    message = error.message;
  }
  if (error.code) {
    code = error.code;
  }

  // Prepare error response
  const errorResponse = {
    success: false,
    error: message,
    code,
    timestamp: new Date().toISOString(),
    path: req.originalUrl,
    method: req.method,
  };

  // Add details in development mode or for validation errors
  if (process.env.NODE_ENV === "development" || statusCode === 400) {
    if (details) {
      errorResponse.details = details;
    }

    // Add stack trace in development
    if (process.env.NODE_ENV === "development") {
      errorResponse.stack = error.stack;
    }
  }

  // Add request ID if available
  if (req.id) {
    errorResponse.requestId = req.id;
  }

  // Security headers for error responses
  res.set({
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "X-XSS-Protection": "1; mode=block",
  });

  res.status(statusCode).json(errorResponse);
};

/**
 * 404 handler for unmatched routes
 */
const notFoundHandler = (req, res) => {
  const error = {
    success: false,
    error: "Endpoint not found",
    code: "NOT_FOUND",
    timestamp: new Date().toISOString(),
    path: req.originalUrl,
    method: req.method,
    availableEndpoints: {
      auth: "/api/v1/auth",
      users: "/api/v1/users",
      listings: "/api/v1/listings",
      transactions: "/api/v1/transactions",
      sync: "/api/v1/sync",
      health: "/health",
    },
  };

  logger.warn("404 - Endpoint not found", {
    method: req.method,
    url: req.originalUrl,
    userAgent: req.get("User-Agent"),
    ip: req.ip,
    userId: req.user?.userId,
  });

  res.status(404).json(error);
};

/**
 * Async error wrapper
 * Wraps async route handlers to catch errors
 */
const asyncHandler = (fn) => (req, res, next) => {
  Promise.resolve(fn(req, res, next)).catch(next);
};

/**
 * Custom error classes
 */
class AppError extends Error {
  constructor(message, statusCode = 500, code = "APP_ERROR") {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.name = "AppError";
    Error.captureStackTrace(this, this.constructor);
  }
}

class ValidationError extends AppError {
  constructor(message, details = null) {
    super(message, 400, "VALIDATION_ERROR");
    this.name = "ValidationError";
    this.details = details;
  }
}

class UnauthorizedError extends AppError {
  constructor(message = "Unauthorized") {
    super(message, 401, "UNAUTHORIZED");
    this.name = "UnauthorizedError";
  }
}

class ForbiddenError extends AppError {
  constructor(message = "Forbidden") {
    super(message, 403, "FORBIDDEN");
    this.name = "ForbiddenError";
  }
}

class NotFoundError extends AppError {
  constructor(message = "Resource not found") {
    super(message, 404, "NOT_FOUND");
    this.name = "NotFoundError";
  }
}

class ConflictError extends AppError {
  constructor(message = "Resource conflict") {
    super(message, 409, "CONFLICT");
    this.name = "ConflictError";
  }
}

class TooManyRequestsError extends AppError {
  constructor(message = "Too many requests") {
    super(message, 429, "RATE_LIMIT_EXCEEDED");
    this.name = "TooManyRequestsError";
  }
}

module.exports = {
  errorHandler,
  notFoundHandler,
  asyncHandler,
  AppError,
  ValidationError,
  UnauthorizedError,
  ForbiddenError,
  NotFoundError,
  ConflictError,
  TooManyRequestsError,
};
