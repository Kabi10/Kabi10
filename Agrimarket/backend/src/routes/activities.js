/**
 * Activities API Routes
 * Implements Activity Feed endpoints for Android ActivityApiService.kt
 *
 * Endpoints:
 *   GET    /api/v1/activities                 - List activities (paginated, filterable)
 *   GET    /api/v1/activities/:id             - Get single activity
 *   GET    /api/v1/activities/unread-count    - Get unread count
 *   GET    /api/v1/activities/actionable-count - Get actionable count
 *   GET    /api/v1/activities/summary         - Get activity summary
 *   GET    /api/v1/activities/recent          - Get recent activities
 *   POST   /api/v1/activities                 - Create activity
 *   PATCH  /api/v1/activities/:id/read        - Mark as read
 *   PATCH  /api/v1/activities/mark-all-read   - Mark all as read
 *   PATCH  /api/v1/activities/:id/dismiss     - Dismiss activity
 *   PATCH  /api/v1/activities/:id/archive     - Archive activity
 *   DELETE /api/v1/activities/:id             - Delete activity
 *
 * Created: 2026-02-16
 */

const express = require("express");
const { body, query, validationResult } = require("express-validator");
const db = require("../database/connection");
const logger = require("../utils/logger");
const { validateUUID } = require("../utils/helpers");

const router = express.Router();

/**
 * Helper function to map activity row to response DTO
 */
function mapActivityToDto(row, language = "en") {
  return {
    id: row.id,
    userId: row.user_id,
    activityType: row.activity_type,
    title:
      language === "ta"
        ? row.title_tamil || row.title
        : language === "si"
          ? row.title_sinhala || row.title
          : row.title,
    description:
      language === "ta"
        ? row.description_tamil || row.description
        : language === "si"
          ? row.description_sinhala || row.description
          : row.description,
    relatedEntityType: row.related_entity_type,
    relatedEntityId: row.related_entity_id,
    priority: row.priority,
    status: row.status,
    isRead: row.is_read,
    isActionable: row.is_actionable,
    expiresAt: row.expires_at,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    readAt: row.read_at,
    dismissedAt: row.dismissed_at,
    archivedAt: row.archived_at,
    metadata: row.metadata || {},
  };
}

/**
 * GET /api/v1/activities
 * List activities for authenticated user (paginated, filterable)
 */
router.get(
  "/",
  [
    query("activityType").optional().isString(),
    query("status").optional().isIn(["ACTIVE", "DISMISSED", "ARCHIVED"]),
    query("priority").optional().isIn(["LOW", "NORMAL", "HIGH", "URGENT"]),
    query("isRead").optional().isBoolean(),
    query("isActionable").optional().isBoolean(),
    query("fromDate").optional().isISO8601(),
    query("toDate").optional().isISO8601(),
    query("page").optional().isInt({ min: 1 }),
    query("limit").optional().isInt({ min: 1, max: 100 }),
    query("sortBy").optional().isIn(["timestamp", "priority", "type"]),
    query("sortOrder").optional().isIn(["asc", "desc"]),
    query("language").optional().isIn(["en", "ta", "si"]),
  ],
  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          message: "Validation failed",
          errors: errors.array(),
        });
      }

      const {
        activityType,
        status,
        priority,
        isRead,
        isActionable,
        fromDate,
        toDate,
        page = 1,
        limit = 20,
        sortBy = "timestamp",
        sortOrder = "desc",
        language = "en",
      } = req.query;

      const userId = req.user.userId;
      const offset = (parseInt(page) - 1) * parseInt(limit);

      // Build dynamic WHERE clause
      const conditions = ["user_id = $1"];
      const params = [userId];
      let paramIndex = 2;

      if (activityType) {
        conditions.push(`activity_type = $${paramIndex}`);
        params.push(activityType);
        paramIndex++;
      }

      if (status) {
        conditions.push(`status = $${paramIndex}`);
        params.push(status);
        paramIndex++;
      }

      if (priority) {
        conditions.push(`priority = $${paramIndex}`);
        params.push(priority);
        paramIndex++;
      }

      if (isRead !== undefined) {
        conditions.push(`is_read = $${paramIndex}`);
        params.push(isRead === "true" || isRead === true);
        paramIndex++;
      }

      if (isActionable !== undefined) {
        conditions.push(`is_actionable = $${paramIndex}`);
        params.push(isActionable === "true" || isActionable === true);
        paramIndex++;
      }

      if (fromDate) {
        conditions.push(`created_at >= $${paramIndex}`);
        params.push(fromDate);
        paramIndex++;
      }

      if (toDate) {
        conditions.push(`created_at <= $${paramIndex}`);
        params.push(toDate);
        paramIndex++;
      }

      const whereClause = conditions.join(" AND ");

      // Build ORDER BY clause
      let orderByClause = "created_at DESC";
      if (sortBy === "priority") {
        orderByClause = `CASE priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 WHEN 'LOW' THEN 4 END ${sortOrder.toUpperCase()}`;
      } else if (sortBy === "type") {
        orderByClause = `activity_type ${sortOrder.toUpperCase()}`;
      } else {
        orderByClause = `created_at ${sortOrder.toUpperCase()}`;
      }

      // Fetch activities
      const result = await db.query(
        `
        SELECT * FROM activities
        WHERE ${whereClause}
        ORDER BY ${orderByClause}
        LIMIT $${paramIndex} OFFSET $${paramIndex + 1}
      `,
        [...params, parseInt(limit), offset],
      );

      // Get total count
      const countResult = await db.query(
        `
        SELECT COUNT(*) as total FROM activities
        WHERE ${whereClause}
      `,
        params,
      );

      const total = parseInt(countResult.rows[0].total);
      const totalPages = Math.ceil(total / parseInt(limit));

      const activities = result.rows.map((row) =>
        mapActivityToDto(row, language),
      );

      res.json({
        activities,
        totalCount: total,
        page: parseInt(page),
        totalPages,
        hasNext: parseInt(page) < totalPages,
        hasPrevious: parseInt(page) > 1,
        lastUpdated: new Date().toISOString(),
      });
    } catch (error) {
      logger.error("Get activities error:", error);
      res.status(500).json({
        success: false,
        message: "Failed to fetch activities",
      });
    }
  },
);

/**
 * GET /api/v1/activities/unread-count
 * Get unread activities count for authenticated user
 */
router.get("/unread-count", async (req, res) => {
  try {
    const result = await db.query(
      `
      SELECT COUNT(*) as count FROM activities
      WHERE user_id = $1 AND is_read = FALSE AND status = 'ACTIVE'
    `,
      [req.user.userId],
    );

    res.json({
      count: parseInt(result.rows[0].count),
    });
  } catch (error) {
    logger.error("Get unread count error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to get unread count",
    });
  }
});

/**
 * GET /api/v1/activities/actionable-count
 * Get actionable activities count for authenticated user
 */
router.get("/actionable-count", async (req, res) => {
  try {
    const result = await db.query(
      `
      SELECT COUNT(*) as count FROM activities
      WHERE user_id = $1 AND is_actionable = TRUE AND status = 'ACTIVE'
    `,
      [req.user.userId],
    );

    res.json({
      count: parseInt(result.rows[0].count),
    });
  } catch (error) {
    logger.error("Get actionable count error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to get actionable count",
    });
  }
});

/**
 * GET /api/v1/activities/summary
 * Get activity summary (total, unread, actionable, distributions)
 */
router.get(
  "/summary",
  [query("timeframe").optional().isIn(["24h", "7d", "30d"])],
  async (req, res) => {
    try {
      const { timeframe = "24h" } = req.query;
      const userId = req.user.userId;

      // Calculate time filter
      let timeFilter = "created_at >= NOW() - INTERVAL '24 hours'";
      if (timeframe === "7d") {
        timeFilter = "created_at >= NOW() - INTERVAL '7 days'";
      } else if (timeframe === "30d") {
        timeFilter = "created_at >= NOW() - INTERVAL '30 days'";
      }

      // Get summary statistics
      const summaryResult = await db.query(
        `
        SELECT
          COUNT(*) as total_activities,
          COUNT(*) FILTER (WHERE is_read = FALSE) as unread_count,
          COUNT(*) FILTER (WHERE is_actionable = TRUE AND status = 'ACTIVE') as actionable_count
        FROM activities
        WHERE user_id = $1 AND ${timeFilter}
      `,
        [userId],
      );

      // Get type distribution
      const typeResult = await db.query(
        `
        SELECT activity_type, COUNT(*) as count
        FROM activities
        WHERE user_id = $1 AND ${timeFilter}
        GROUP BY activity_type
      `,
        [userId],
      );

      const typeDistribution = {};
      typeResult.rows.forEach((row) => {
        typeDistribution[row.activity_type] = parseInt(row.count);
      });

      // Get priority distribution
      const priorityResult = await db.query(
        `
        SELECT priority, COUNT(*) as count
        FROM activities
        WHERE user_id = $1 AND ${timeFilter}
        GROUP BY priority
      `,
        [userId],
      );

      const priorityDistribution = {};
      priorityResult.rows.forEach((row) => {
        priorityDistribution[row.priority] = parseInt(row.count);
      });

      // Get recent activities (last 5)
      const recentResult = await db.query(
        `
        SELECT * FROM activities
        WHERE user_id = $1 AND ${timeFilter}
        ORDER BY created_at DESC
        LIMIT 5
      `,
        [userId],
      );

      const language = req.query.language || "en";
      const recentActivities = recentResult.rows.map((row) =>
        mapActivityToDto(row, language),
      );

      const summary = summaryResult.rows[0];

      res.json({
        totalActivities: parseInt(summary.total_activities),
        unreadCount: parseInt(summary.unread_count),
        actionableCount: parseInt(summary.actionable_count),
        typeDistribution,
        priorityDistribution,
        recentActivities,
      });
    } catch (error) {
      logger.error("Get activity summary error:", error);
      res.status(500).json({
        success: false,
        message: "Failed to fetch activity summary",
      });
    }
  },
);

/**
 * GET /api/v1/activities/recent
 * Get recent activities (default limit: 10)
 */
router.get(
  "/recent",
  [query("limit").optional().isInt({ min: 1, max: 50 })],
  async (req, res) => {
    try {
      const { limit = 10 } = req.query;
      const userId = req.user.userId;

      const result = await db.query(
        `
        SELECT * FROM activities
        WHERE user_id = $1 AND status = 'ACTIVE'
        ORDER BY created_at DESC
        LIMIT $2
      `,
        [userId, parseInt(limit)],
      );

      const language = req.query.language || "en";
      const activities = result.rows.map((row) =>
        mapActivityToDto(row, language),
      );

      res.json({
        activities,
        totalCount: activities.length,
        page: 1,
        totalPages: 1,
        hasNext: false,
        hasPrevious: false,
        lastUpdated: new Date().toISOString(),
      });
    } catch (error) {
      logger.error("Get recent activities error:", error);
      res.status(500).json({
        success: false,
        message: "Failed to fetch recent activities",
      });
    }
  },
);

/**
 * GET /api/v1/activities/:id
 * Get single activity by ID
 */
router.get("/:id", async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid activity ID",
      });
    }

    const result = await db.query(
      "SELECT * FROM activities WHERE id = $1 AND user_id = $2",
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Activity not found",
      });
    }

    const language = req.query.language || "en";
    const activity = mapActivityToDto(result.rows[0], language);

    res.json(activity);
  } catch (error) {
    logger.error("Get activity by ID error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch activity",
    });
  }
});

/**
 * POST /api/v1/activities
 * Create new activity
 */
router.post(
  "/",
  [
    body("userId").isUUID().withMessage("Invalid user ID"),
    body("activityType")
      .isString()
      .notEmpty()
      .withMessage("Activity type is required"),
    body("title").isString().notEmpty().withMessage("Title is required"),
    body("description")
      .isString()
      .notEmpty()
      .withMessage("Description is required"),
    body("titleTamil").optional().isString(),
    body("titleSinhala").optional().isString(),
    body("descriptionTamil").optional().isString(),
    body("descriptionSinhala").optional().isString(),
    body("relatedEntityType").optional().isString(),
    body("relatedEntityId").optional().isUUID(),
    body("priority").optional().isIn(["LOW", "NORMAL", "HIGH", "URGENT"]),
    body("isActionable").optional().isBoolean(),
    body("expiresAt").optional().isISO8601(),
    body("metadata").optional().isObject(),
  ],
  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          message: "Validation failed",
          errors: errors.array(),
        });
      }

      const {
        userId,
        activityType,
        title,
        titleTamil = "",
        titleSinhala = "",
        description,
        descriptionTamil = "",
        descriptionSinhala = "",
        relatedEntityType = null,
        relatedEntityId = null,
        priority = "NORMAL",
        isActionable = false,
        expiresAt = null,
        metadata = {},
      } = req.body;

      const result = await db.query(
        `
        INSERT INTO activities (
          user_id, activity_type, title, title_tamil, title_sinhala,
          description, description_tamil, description_sinhala,
          related_entity_type, related_entity_id,
          priority, is_actionable, expires_at, metadata
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
        RETURNING *
      `,
        [
          userId,
          activityType,
          title,
          titleTamil,
          titleSinhala,
          description,
          descriptionTamil,
          descriptionSinhala,
          relatedEntityType,
          relatedEntityId,
          priority,
          isActionable,
          expiresAt,
          JSON.stringify(metadata),
        ],
      );

      const activity = mapActivityToDto(result.rows[0]);

      logger.info(`Activity created: ${activity.id} for user ${userId}`);

      res.status(201).json(activity);
    } catch (error) {
      logger.error("Create activity error:", error);
      res.status(500).json({
        success: false,
        message: "Failed to create activity",
      });
    }
  },
);

/**
 * PATCH /api/v1/activities/:id/read
 * Mark activity as read
 */
router.patch("/:id/read", async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid activity ID",
      });
    }

    const result = await db.query(
      `
      UPDATE activities
      SET is_read = TRUE, read_at = NOW()
      WHERE id = $1 AND user_id = $2
      RETURNING id
    `,
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Activity not found",
      });
    }

    res.json({ success: true });
  } catch (error) {
    logger.error("Mark activity read error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to mark activity as read",
    });
  }
});

/**
 * PATCH /api/v1/activities/mark-all-read
 * Mark all activities as read for authenticated user
 */
router.patch("/mark-all-read", async (req, res) => {
  try {
    const result = await db.query(
      `
      UPDATE activities
      SET is_read = TRUE, read_at = NOW()
      WHERE user_id = $1 AND is_read = FALSE
      RETURNING id
    `,
      [req.user.userId],
    );

    res.json({
      success: true,
      markedCount: result.rows.length,
    });
  } catch (error) {
    logger.error("Mark all activities read error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to mark all activities as read",
    });
  }
});

/**
 * PATCH /api/v1/activities/:id/dismiss
 * Dismiss activity (soft hide, keep in database)
 */
router.patch("/:id/dismiss", async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid activity ID",
      });
    }

    const result = await db.query(
      `
      UPDATE activities
      SET status = 'DISMISSED', dismissed_at = NOW()
      WHERE id = $1 AND user_id = $2
      RETURNING id
    `,
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Activity not found",
      });
    }

    res.json({ success: true });
  } catch (error) {
    logger.error("Dismiss activity error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to dismiss activity",
    });
  }
});

/**
 * PATCH /api/v1/activities/:id/archive
 * Archive activity (move to historical records)
 */
router.patch("/:id/archive", async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid activity ID",
      });
    }

    const result = await db.query(
      `
      UPDATE activities
      SET status = 'ARCHIVED', archived_at = NOW()
      WHERE id = $1 AND user_id = $2
      RETURNING id
    `,
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Activity not found",
      });
    }

    res.json({ success: true });
  } catch (error) {
    logger.error("Archive activity error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to archive activity",
    });
  }
});

/**
 * DELETE /api/v1/activities/:id
 * Delete activity permanently
 */
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid activity ID",
      });
    }

    const result = await db.query(
      "DELETE FROM activities WHERE id = $1 AND user_id = $2 RETURNING id",
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Activity not found",
      });
    }

    logger.info(`Activity deleted: ${id} by user ${req.user.userId}`);

    res.json({
      success: true,
      message: "Activity deleted successfully",
    });
  } catch (error) {
    logger.error("Delete activity error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to delete activity",
    });
  }
});

module.exports = router;
