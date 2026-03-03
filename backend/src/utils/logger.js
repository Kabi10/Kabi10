const winston = require("winston");
const path = require("path");

// Define log levels
const levels = {
  error: 0,
  warn: 1,
  info: 2,
  http: 3,
  debug: 4,
};

// Define colors for each level
const colors = {
  error: "red",
  warn: "yellow",
  info: "green",
  http: "magenta",
  debug: "white",
};

// Tell winston that you want to link the colors
winston.addColors(colors);

// Define which level to log based on environment
const level = () => {
  const env = process.env.NODE_ENV || "development";
  const isDevelopment = env === "development";
  return isDevelopment ? "debug" : "warn";
};

// Detect if running on Vercel
const isVercel = process.env.VERCEL === "1" || !!process.env.VERCEL;

// Define format for logs
const format = winston.format.combine(
  winston.format.timestamp({ format: "YYYY-MM-DD HH:mm:ss:ms" }),
  winston.format.colorize({ all: true }),
  winston.format.printf(
    (info) => `${info.timestamp} ${info.level}: ${info.message}`,
  ),
);

// Define format for file logs (without colors)
const fileFormat = winston.format.combine(
  winston.format.timestamp({ format: "YYYY-MM-DD HH:mm:ss:ms" }),
  winston.format.errors({ stack: true }),
  winston.format.json(),
);

// Define transports
const transports = [
  // Console transport - Always active and works on Vercel
  new winston.transports.Console({
    level: level(),
    format,
  }),
];

// Add file transports in production ONLY if not running on Vercel
// Vercel has a read-only filesystem which causes fs.mkdirSync to fail
if (process.env.NODE_ENV === "production" && !isVercel) {
  // Ensure logs directory exists
  const fs = require("fs");
  const logsDir = path.join(process.cwd(), "logs");
  if (!fs.existsSync(logsDir)) {
    try {
      fs.mkdirSync(logsDir, { recursive: true });

      // Error log file
      transports.push(
        new winston.transports.File({
          filename: path.join(logsDir, "error.log"),
          level: "error",
          format: fileFormat,
          maxsize: 5242880, // 5MB
          maxFiles: 5,
        }),
      );

      // Combined log file
      transports.push(
        new winston.transports.File({
          filename: path.join(logsDir, "combined.log"),
          format: fileFormat,
          maxsize: 5242880, // 5MB
          maxFiles: 5,
        }),
      );
    } catch (err) {
      console.error("Failed to initialize file logging:", err);
    }
  }
}

// Create the logger
const logger = winston.createLogger({
  level: level(),
  levels,
  format: fileFormat,
  transports,
  // Handle uncaught exceptions
  exceptionHandlers: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple(),
      ),
    }),
  ],
  // Handle unhandled promise rejections
  rejectionHandlers: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple(),
      ),
    }),
  ],
  exitOnError: false,
});

// Add custom methods for structured logging
logger.logRequest = (req, res, responseTime) => {
  const logData = {
    method: req.method,
    url: req.originalUrl,
    statusCode: res.statusCode,
    responseTime: `${responseTime}ms`,
    userAgent: req.get("User-Agent"),
    ip: req.ip || req.connection.remoteAddress,
  };

  if (req.user) {
    logData.userId = req.user.userId;
    logData.userType = req.user.userType;
  }

  logger.http("HTTP Request", logData);
};

logger.logError = (error, context = {}) => {
  const errorData = {
    message: error.message,
    stack: error.stack,
    ...context,
  };

  logger.error("Application Error", errorData);
};

logger.logAuth = (
  action,
  userId,
  phoneNumber,
  success = true,
  details = {},
) => {
  const logData = {
    action,
    userId,
    phoneNumber: phoneNumber
      ? phoneNumber.replace(/(\+94)(\d{2})(\d{3})(\d{4})/, "$1 $2 *** $4")
      : null,
    success,
    timestamp: new Date().toISOString(),
    ...details,
  };

  if (success) {
    logger.info("Auth Success", logData);
  } else {
    logger.warn("Auth Failure", logData);
  }
};

logger.logBusiness = (event, data = {}) => {
  const logData = {
    event,
    timestamp: new Date().toISOString(),
    ...data,
  };

  logger.info("Business Event", logData);
};

logger.logPerformance = (operation, duration, details = {}) => {
  const logData = {
    operation,
    duration: `${duration}ms`,
    timestamp: new Date().toISOString(),
    ...details,
  };

  if (duration > 1000) {
    logger.warn("Slow Operation", logData);
  } else {
    logger.debug("Performance", logData);
  }
};

logger.logSecurity = (event, severity = "medium", details = {}) => {
  const logData = {
    securityEvent: event,
    severity,
    timestamp: new Date().toISOString(),
    ...details,
  };

  if (severity === "high") {
    logger.error("Security Alert", logData);
  } else if (severity === "medium") {
    logger.warn("Security Warning", logData);
  } else {
    logger.info("Security Info", logData);
  }
};

// Log startup information
logger.info("Logger initialized", {
  level: level(),
  environment: process.env.NODE_ENV || "development",
  transports: transports.map((t) => t.constructor.name),
});

module.exports = logger;
