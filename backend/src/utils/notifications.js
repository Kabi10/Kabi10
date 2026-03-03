const db = require("../database/connection");
const logger = require("./logger");

/**
 * Create a notification for a user
 * @param {string} userId - Target user ID
 * @param {string} type - Notification type (e.g., 'NEW_MESSAGE', 'TRANSACTION_UPDATE', 'NEW_REVIEW')
 * @param {string} title - Notification title
 * @param {string} message - Notification body
 * @param {string|null} relatedId - Optional related entity ID
 */
async function createNotification(
  userId,
  type,
  title,
  message,
  relatedId = null,
) {
  try {
    await db.query(
      `INSERT INTO notifications (user_id, type, title, message, related_id)
       VALUES ($1, $2, $3, $4, $5)`,
      [userId, type, title, message, relatedId],
    );
  } catch (error) {
    logger.error("Failed to create notification:", error);
    // Don't throw — notification failure shouldn't break the calling operation
  }
}

module.exports = { createNotification };
