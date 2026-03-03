const express = require("express");
const db = require("../database/connection");
const smsService = require("../services/smsService");
const logger = require("../utils/logger");

const router = express.Router();

/**
 * GET /health
 * Basic health check endpoint
 */
router.get("/", async (req, res) => {
  const startTime = Date.now();

  try {
    // Basic health check
    const health = {
      status: "healthy",
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: process.env.NODE_ENV || "development",
      version: process.env.npm_package_version || "1.0.0",
      responseTime: 0,
    };

    health.responseTime = Date.now() - startTime;

    res.json(health);
  } catch (error) {
    logger.error("Health check error:", error);
    res.status(500).json({
      status: "unhealthy",
      timestamp: new Date().toISOString(),
      error: error.message,
      responseTime: Date.now() - startTime,
    });
  }
});

/**
 * GET /health/detailed
 * Detailed health check with dependencies
 */
router.get("/detailed", async (req, res) => {
  const startTime = Date.now();

  try {
    const health = {
      status: "healthy",
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: process.env.NODE_ENV || "development",
      version: process.env.npm_package_version || "1.0.0",
      services: {},
      system: {
        memory: process.memoryUsage(),
        cpu: process.cpuUsage(),
        platform: process.platform,
        nodeVersion: process.version,
      },
    };

    // Check database health
    try {
      const dbHealth = await db.healthCheck();
      health.services.database = dbHealth;
    } catch (error) {
      health.services.database = {
        status: "unhealthy",
        error: error.message,
      };
      health.status = "degraded";
    }

    // Check SMS service health
    try {
      const smsHealth = smsService.getProviderInfo();
      health.services.sms = {
        status: "healthy",
        provider: smsHealth.current,
        mockMode: smsHealth.mockMode,
        configured: smsHealth.available[smsHealth.current]?.configured || false,
      };
    } catch (error) {
      health.services.sms = {
        status: "unhealthy",
        error: error.message,
      };
      health.status = "degraded";
    }

    // Check if any critical services are down
    const criticalServices = ["database"];
    const unhealthyServices = criticalServices.filter(
      (service) => health.services[service]?.status === "unhealthy",
    );

    if (unhealthyServices.length > 0) {
      health.status = "unhealthy";
    }

    health.responseTime = Date.now() - startTime;

    const statusCode = health.status === "unhealthy" ? 503 : 200;
    res.status(statusCode).json(health);
  } catch (error) {
    logger.error("Detailed health check error:", error);
    res.status(500).json({
      status: "unhealthy",
      timestamp: new Date().toISOString(),
      error: error.message,
      responseTime: Date.now() - startTime,
    });
  }
});

/**
 * GET /health/database
 * Database-specific health check
 */
router.get("/database", async (req, res) => {
  const startTime = Date.now();

  try {
    const dbHealth = await db.healthCheck();
    dbHealth.responseTime = Date.now() - startTime;

    const statusCode = dbHealth.status === "healthy" ? 200 : 503;
    res.status(statusCode).json(dbHealth);
  } catch (error) {
    logger.error("Database health check error:", error);
    res.status(503).json({
      status: "unhealthy",
      error: error.message,
      responseTime: Date.now() - startTime,
    });
  }
});

/**
 * GET /health/sms
 * SMS service health check
 */
router.get("/sms", async (req, res) => {
  const startTime = Date.now();

  try {
    const smsHealth = smsService.getProviderInfo();

    const health = {
      status: "healthy",
      provider: smsHealth.current,
      mockMode: smsHealth.mockMode,
      configured: smsHealth.available[smsHealth.current]?.configured || false,
      availableProviders: smsHealth.available,
      responseTime: Date.now() - startTime,
    };

    if (!health.configured && !health.mockMode) {
      health.status = "unhealthy";
      health.error = "SMS provider not configured";
    }

    const statusCode = health.status === "healthy" ? 200 : 503;
    res.status(statusCode).json(health);
  } catch (error) {
    logger.error("SMS health check error:", error);
    res.status(503).json({
      status: "unhealthy",
      error: error.message,
      responseTime: Date.now() - startTime,
    });
  }
});

/**
 * GET /health/readiness
 * Kubernetes readiness probe
 */
router.get("/readiness", async (req, res) => {
  try {
    // Check if the application is ready to serve traffic
    const dbHealth = await db.healthCheck();

    if (dbHealth.status === "healthy") {
      res.status(200).json({
        status: "ready",
        timestamp: new Date().toISOString(),
      });
    } else {
      res.status(503).json({
        status: "not ready",
        reason: "Database not available",
        timestamp: new Date().toISOString(),
      });
    }
  } catch (error) {
    logger.error("Readiness check error:", error);
    res.status(503).json({
      status: "not ready",
      reason: error.message,
      timestamp: new Date().toISOString(),
    });
  }
});

/**
 * GET /health/liveness
 * Kubernetes liveness probe
 */
router.get("/liveness", (req, res) => {
  // Simple liveness check - if the process is running, it's alive
  res.status(200).json({
    status: "alive",
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  });
});

/**
 * GET /health/metrics
 * Basic application metrics
 */
router.get("/metrics", async (req, res) => {
  try {
    const metrics = {
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      cpu: process.cpuUsage(),
      platform: process.platform,
      nodeVersion: process.version,
      environment: process.env.NODE_ENV || "development",
    };

    // Add database metrics if available
    try {
      const dbHealth = await db.healthCheck();
      if (dbHealth.pool) {
        metrics.database = {
          totalConnections: dbHealth.pool.totalCount,
          idleConnections: dbHealth.pool.idleCount,
          waitingConnections: dbHealth.pool.waitingCount,
        };
      }
    } catch (error) {
      metrics.database = { error: "Unable to fetch database metrics" };
    }

    res.json(metrics);
  } catch (error) {
    logger.error("Metrics error:", error);
    res.status(500).json({
      error: "Failed to fetch metrics",
      timestamp: new Date().toISOString(),
    });
  }
});

module.exports = router;
