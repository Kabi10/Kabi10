const express = require("express");
const db = require("../database/connection");
const logger = require("../utils/logger");
const { validateUUID } = require("../utils/helpers");

const router = express.Router();

/**
 * GET /api/v1/notifications
 * List user's notifications (paginated, newest first)
 */
router.get("/", async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const offset = (parseInt(page) - 1) * parseInt(limit);
    const userId = req.user.userId;

    const result = await db.query(
      `
      SELECT * FROM notifications
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `,
      [userId, parseInt(limit), offset],
    );

    const countResult = await db.query(
      "SELECT COUNT(*) as total FROM notifications WHERE user_id = $1",
      [userId],
    );
    const total = parseInt(countResult.rows[0].total);

    const notifications = result.rows.map((row) => ({
      id: row.id,
      userId: row.user_id,
      type: row.type,
      title: row.title,
      message: row.message,
      relatedId: row.related_id,
      isRead: row.is_read,
      createdAt: row.created_at,
    }));

    res.json({
      success: true,
      notifications,
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
    });
  } catch (error) {
    logger.error("Get notifications error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch notifications" });
  }
});

/**
 * GET /api/v1/notifications/unread-count
 * Get unread notification count
 */
router.get("/unread-count", async (req, res) => {
  try {
    const result = await db.query(
      "SELECT COUNT(*) as count FROM notifications WHERE user_id = $1 AND is_read = FALSE",
      [req.user.userId],
    );

    res.json({ success: true, count: parseInt(result.rows[0].count) });
  } catch (error) {
    logger.error("Get unread count error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to get unread count" });
  }
});

/**
 * PATCH /api/v1/notifications/read-all
 * Mark all notifications as read
 */
router.patch("/read-all", async (req, res) => {
  try {
    const result = await db.query(
      "UPDATE notifications SET is_read = TRUE WHERE user_id = $1 AND is_read = FALSE",
      [req.user.userId],
    );

    res.json({ success: true, markedCount: result.rowCount });
  } catch (error) {
    logger.error("Mark all read error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to mark all as read" });
  }
});

/**
 * PATCH /api/v1/notifications/:id/read
 * Mark single notification as read
 */
router.patch("/:id/read", async (req, res) => {
  try {
    const { id } = req.params;
    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid notification ID" });
    }

    const result = await db.query(
      "UPDATE notifications SET is_read = TRUE WHERE id = $1 AND user_id = $2 RETURNING id",
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: "Notification not found" });
    }

    res.json({ success: true });
  } catch (error) {
    logger.error("Mark notification read error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to mark notification as read" });
  }
});

/**
 * DELETE /api/v1/notifications/:id
 * Delete notification
 */
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid notification ID" });
    }

    const result = await db.query(
      "DELETE FROM notifications WHERE id = $1 AND user_id = $2 RETURNING id",
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: "Notification not found" });
    }

    res.json({ success: true, message: "Notification deleted" });
  } catch (error) {
    logger.error("Delete notification error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to delete notification" });
  }
});

module.exports = router;
