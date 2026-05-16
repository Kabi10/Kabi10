const express = require("express");
const { body, validationResult } = require("express-validator");
const db = require("../database/connection");
const logger = require("../utils/logger");
const { validateUUID } = require("../utils/helpers");
const { createNotification } = require("../utils/notifications");

const router = express.Router();

/**
 * GET /api/v1/messages/conversations
 * List user's conversations (paginated, ordered by last_message_at)
 */
router.get("/conversations", async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const offset = (parseInt(page) - 1) * parseInt(limit);
    const userId = req.user.userId;

    const result = await db.query(
      `
      SELECT
        c.*,
        u1.name as participant_1_name,
        u1.phone_number as participant_1_phone,
        u2.name as participant_2_name,
        u2.phone_number as participant_2_phone,
        l.crop_type as listing_crop_type,
        l.id as listing_id_ref,
        (SELECT COUNT(*) FROM messages m WHERE m.conversation_id = c.id AND m.is_read = FALSE AND m.sender_id != $1) as unread_count
      FROM conversations c
      JOIN users u1 ON c.participant_1_id = u1.id
      JOIN users u2 ON c.participant_2_id = u2.id
      LEFT JOIN listings l ON c.listing_id = l.id
      WHERE c.participant_1_id = $1 OR c.participant_2_id = $1
      ORDER BY c.last_message_at DESC NULLS LAST
      LIMIT $2 OFFSET $3
    `,
      [userId, parseInt(limit), offset],
    );

    const countResult = await db.query(
      `SELECT COUNT(*) as total FROM conversations
       WHERE participant_1_id = $1 OR participant_2_id = $1`,
      [userId],
    );
    const total = parseInt(countResult.rows[0].total);

    const conversations = result.rows.map((row) => {
      const isParticipant1 = row.participant_1_id === userId;
      return {
        id: row.id,
        otherParticipant: {
          id: isParticipant1 ? row.participant_2_id : row.participant_1_id,
          name: isParticipant1
            ? row.participant_2_name
            : row.participant_1_name,
          phone: isParticipant1
            ? row.participant_2_phone
            : row.participant_1_phone,
        },
        listingId: row.listing_id,
        listingCropType: row.listing_crop_type,
        lastMessageText: row.last_message_text,
        lastMessageAt: row.last_message_at,
        unreadCount: parseInt(row.unread_count),
        createdAt: row.created_at,
        updatedAt: row.updated_at,
      };
    });

    res.json({
      success: true,
      conversations,
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
    });
  } catch (error) {
    logger.error("Get conversations error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch conversations" });
  }
});

/**
 * GET /api/v1/messages/conversations/:id
 * Get conversation detail
 */
router.get("/conversations/:id", async (req, res) => {
  try {
    const { id } = req.params;
    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid conversation ID" });
    }

    const result = await db.query(
      `
      SELECT
        c.*,
        u1.name as participant_1_name,
        u1.phone_number as participant_1_phone,
        u2.name as participant_2_name,
        u2.phone_number as participant_2_phone,
        l.crop_type as listing_crop_type
      FROM conversations c
      JOIN users u1 ON c.participant_1_id = u1.id
      JOIN users u2 ON c.participant_2_id = u2.id
      LEFT JOIN listings l ON c.listing_id = l.id
      WHERE c.id = $1 AND (c.participant_1_id = $2 OR c.participant_2_id = $2)
    `,
      [id, req.user.userId],
    );

    if (result.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: "Conversation not found" });
    }

    const row = result.rows[0];
    const userId = req.user.userId;
    const isParticipant1 = row.participant_1_id === userId;

    res.json({
      success: true,
      data: {
        id: row.id,
        otherParticipant: {
          id: isParticipant1 ? row.participant_2_id : row.participant_1_id,
          name: isParticipant1
            ? row.participant_2_name
            : row.participant_1_name,
          phone: isParticipant1
            ? row.participant_2_phone
            : row.participant_1_phone,
        },
        listingId: row.listing_id,
        listingCropType: row.listing_crop_type,
        lastMessageText: row.last_message_text,
        lastMessageAt: row.last_message_at,
        createdAt: row.created_at,
      },
    });
  } catch (error) {
    logger.error("Get conversation error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch conversation" });
  }
});

/**
 * POST /api/v1/messages/conversations
 * Create or get existing conversation
 */
router.post(
  "/conversations",
  [body("participantId").isUUID().withMessage("Invalid participant ID")],
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

      const { participantId, listingId } = req.body;
      const userId = req.user.userId;

      if (userId === participantId) {
        return res.status(400).json({
          success: false,
          message: "Cannot start conversation with yourself",
        });
      }

      // Ensure ordered participants for unique constraint
      const p1 = userId < participantId ? userId : participantId;
      const p2 = userId < participantId ? participantId : userId;

      // Try to find existing conversation
      let result = await db.query(
        `SELECT id FROM conversations WHERE participant_1_id = $1 AND participant_2_id = $2`,
        [p1, p2],
      );

      if (result.rows.length > 0) {
        return res.json({
          success: true,
          data: { id: result.rows[0].id, created: false },
        });
      }

      // Create new conversation
      result = await db.query(
        `INSERT INTO conversations (participant_1_id, participant_2_id, listing_id)
         VALUES ($1, $2, $3) RETURNING id, created_at`,
        [p1, p2, listingId || null],
      );

      res.status(201).json({
        success: true,
        data: {
          id: result.rows[0].id,
          created: true,
          createdAt: result.rows[0].created_at,
        },
      });
    } catch (error) {
      logger.error("Create conversation error:", error);
      res
        .status(500)
        .json({ success: false, message: "Failed to create conversation" });
    }
  },
);

/**
 * GET /api/v1/messages/conversations/:id/messages
 * Get messages in conversation (paginated, newest first)
 */
router.get("/conversations/:id/messages", async (req, res) => {
  try {
    const { id } = req.params;
    const { page = 1, limit = 50 } = req.query;

    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid conversation ID" });
    }

    // Verify user is participant
    const conv = await db.query(
      `SELECT id FROM conversations WHERE id = $1 AND (participant_1_id = $2 OR participant_2_id = $2)`,
      [id, req.user.userId],
    );
    if (conv.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: "Conversation not found" });
    }

    const offset = (parseInt(page) - 1) * parseInt(limit);

    const result = await db.query(
      `
      SELECT m.*, u.name as sender_name
      FROM messages m
      JOIN users u ON m.sender_id = u.id
      WHERE m.conversation_id = $1
      ORDER BY m.created_at DESC
      LIMIT $2 OFFSET $3
    `,
      [id, parseInt(limit), offset],
    );

    const countResult = await db.query(
      "SELECT COUNT(*) as total FROM messages WHERE conversation_id = $1",
      [id],
    );
    const total = parseInt(countResult.rows[0].total);

    const messages = result.rows.map((row) => ({
      id: row.id,
      conversationId: row.conversation_id,
      senderId: row.sender_id,
      senderName: row.sender_name,
      content: row.content,
      messageType: row.message_type,
      isRead: row.is_read,
      createdAt: row.created_at,
    }));

    res.json({
      success: true,
      messages,
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
    });
  } catch (error) {
    logger.error("Get messages error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch messages" });
  }
});

/**
 * POST /api/v1/messages/conversations/:id/messages
 * Send a message
 */
router.post(
  "/conversations/:id/messages",
  [
    body("content")
      .isLength({ min: 1, max: 2000 })
      .withMessage("Message content required (max 2000 chars)"),
  ],
  async (req, res) => {
    try {
      const { id } = req.params;
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          message: "Validation failed",
          errors: errors.array(),
        });
      }

      if (!validateUUID(id)) {
        return res
          .status(400)
          .json({ success: false, message: "Invalid conversation ID" });
      }

      const userId = req.user.userId;
      const { content, messageType = "TEXT" } = req.body;

      // Verify user is participant and get other participant
      const conv = await db.query(
        `SELECT participant_1_id, participant_2_id FROM conversations
         WHERE id = $1 AND (participant_1_id = $2 OR participant_2_id = $2)`,
        [id, userId],
      );
      if (conv.rows.length === 0) {
        return res
          .status(404)
          .json({ success: false, message: "Conversation not found" });
      }

      const otherUserId =
        conv.rows[0].participant_1_id === userId
          ? conv.rows[0].participant_2_id
          : conv.rows[0].participant_1_id;

      // Insert message
      const result = await db.query(
        `INSERT INTO messages (conversation_id, sender_id, content, message_type)
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [id, userId, content, messageType],
      );

      // Update conversation's last message
      await db.query(
        `UPDATE conversations SET last_message_text = $1, last_message_at = NOW() WHERE id = $2`,
        [content.substring(0, 100), id],
      );

      // Create notification for recipient
      const senderName = await db.query(
        "SELECT name FROM users WHERE id = $1",
        [userId],
      );
      const name = senderName.rows[0]?.name || "Someone";
      await createNotification(
        otherUserId,
        "NEW_MESSAGE",
        `New message from ${name}`,
        content.substring(0, 100),
        id,
      );

      const msg = result.rows[0];
      res.status(201).json({
        success: true,
        data: {
          id: msg.id,
          conversationId: msg.conversation_id,
          senderId: msg.sender_id,
          content: msg.content,
          messageType: msg.message_type,
          isRead: msg.is_read,
          createdAt: msg.created_at,
        },
      });
    } catch (error) {
      logger.error("Send message error:", error);
      res
        .status(500)
        .json({ success: false, message: "Failed to send message" });
    }
  },
);

/**
 * PATCH /api/v1/messages/conversations/:id/read
 * Mark all messages in conversation as read
 */
router.patch("/conversations/:id/read", async (req, res) => {
  try {
    const { id } = req.params;
    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid conversation ID" });
    }

    const result = await db.query(
      `UPDATE messages SET is_read = TRUE
       WHERE conversation_id = $1 AND sender_id != $2 AND is_read = FALSE`,
      [id, req.user.userId],
    );

    res.json({ success: true, markedCount: result.rowCount });
  } catch (error) {
    logger.error("Mark messages read error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to mark messages as read" });
  }
});

/**
 * GET /api/v1/messages/unread-count
 * Get total unread message count for user
 */
router.get("/unread-count", async (req, res) => {
  try {
    const userId = req.user.userId;

    const result = await db.query(
      `
      SELECT COUNT(*) as count
      FROM messages m
      JOIN conversations c ON m.conversation_id = c.id
      WHERE (c.participant_1_id = $1 OR c.participant_2_id = $1)
        AND m.sender_id != $1
        AND m.is_read = FALSE
    `,
      [userId],
    );

    res.json({ success: true, count: parseInt(result.rows[0].count) });
  } catch (error) {
    logger.error("Get unread count error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to get unread count" });
  }
});

module.exports = router;
