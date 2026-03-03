const express = require("express");
const { body, validationResult } = require("express-validator");
const db = require("../database/connection");
const logger = require("../utils/logger");
const { validateUUID } = require("../utils/helpers");
const { createNotification } = require("../utils/notifications");

const router = express.Router();

/**
 * GET /api/v1/reviews/user/:userId
 * Get reviews for a user (paginated)
 */
router.get("/user/:userId", async (req, res) => {
  try {
    const { userId } = req.params;
    const { page = 1, limit = 20 } = req.query;

    if (!validateUUID(userId)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid user ID" });
    }

    const offset = (parseInt(page) - 1) * parseInt(limit);

    const result = await db.query(
      `
      SELECT r.*, u.name as reviewer_name
      FROM reviews r
      JOIN users u ON r.reviewer_id = u.id
      WHERE r.reviewee_id = $1
      ORDER BY r.created_at DESC
      LIMIT $2 OFFSET $3
    `,
      [userId, parseInt(limit), offset],
    );

    const countResult = await db.query(
      "SELECT COUNT(*) as total FROM reviews WHERE reviewee_id = $1",
      [userId],
    );
    const total = parseInt(countResult.rows[0].total);

    const reviews = result.rows.map((row) => ({
      id: row.id,
      transactionId: row.transaction_id,
      reviewerId: row.reviewer_id,
      reviewerName: row.reviewer_name,
      revieweeId: row.reviewee_id,
      rating: row.rating,
      comment: row.comment,
      reviewType: row.review_type,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    }));

    res.json({
      success: true,
      reviews,
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
    });
  } catch (error) {
    logger.error("Get reviews error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch reviews" });
  }
});

/**
 * GET /api/v1/reviews/user/:userId/summary
 * Get average rating + count + distribution
 */
router.get("/user/:userId/summary", async (req, res) => {
  try {
    const { userId } = req.params;

    if (!validateUUID(userId)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid user ID" });
    }

    const result = await db.query(
      `
      SELECT
        COUNT(*) as total_reviews,
        COALESCE(AVG(rating), 0) as average_rating,
        COUNT(*) FILTER (WHERE rating = 1) as rating_1,
        COUNT(*) FILTER (WHERE rating = 2) as rating_2,
        COUNT(*) FILTER (WHERE rating = 3) as rating_3,
        COUNT(*) FILTER (WHERE rating = 4) as rating_4,
        COUNT(*) FILTER (WHERE rating = 5) as rating_5
      FROM reviews WHERE reviewee_id = $1
    `,
      [userId],
    );

    const row = result.rows[0];
    res.json({
      success: true,
      data: {
        totalReviews: parseInt(row.total_reviews),
        averageRating: parseFloat(parseFloat(row.average_rating).toFixed(1)),
        distribution: {
          1: parseInt(row.rating_1),
          2: parseInt(row.rating_2),
          3: parseInt(row.rating_3),
          4: parseInt(row.rating_4),
          5: parseInt(row.rating_5),
        },
      },
    });
  } catch (error) {
    logger.error("Get review summary error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch review summary" });
  }
});

/**
 * POST /api/v1/reviews
 * Create review (validates transaction exists and is COMPLETED)
 */
router.post(
  "/",
  [
    body("transactionId").isUUID().withMessage("Invalid transaction ID"),
    body("rating").isInt({ min: 1, max: 5 }).withMessage("Rating must be 1-5"),
    body("comment")
      .optional()
      .isLength({ max: 1000 })
      .withMessage("Comment too long"),
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

      const { transactionId, rating, comment } = req.body;
      const reviewerId = req.user.userId;

      // Verify transaction exists, is COMPLETED, and reviewer is participant
      const transaction = await db.query(
        `SELECT farmer_id, buyer_id, status FROM transactions WHERE id = $1`,
        [transactionId],
      );

      if (transaction.rows.length === 0) {
        return res
          .status(404)
          .json({ success: false, message: "Transaction not found" });
      }

      const tx = transaction.rows[0];
      if (tx.status !== "COMPLETED") {
        return res.status(400).json({
          success: false,
          message: "Can only review completed transactions",
        });
      }

      if (tx.farmer_id !== reviewerId && tx.buyer_id !== reviewerId) {
        return res.status(403).json({
          success: false,
          message: "You are not a participant in this transaction",
        });
      }

      // Determine review type and reviewee
      const isBuyer = tx.buyer_id === reviewerId;
      const reviewType = isBuyer ? "BUYER_TO_FARMER" : "FARMER_TO_BUYER";
      const revieweeId = isBuyer ? tx.farmer_id : tx.buyer_id;

      const result = await db.query(
        `INSERT INTO reviews (transaction_id, reviewer_id, reviewee_id, rating, comment, review_type)
         VALUES ($1, $2, $3, $4, $5, $6) RETURNING *`,
        [transactionId, reviewerId, revieweeId, rating, comment, reviewType],
      );

      // Notify reviewee
      const reviewerName = await db.query(
        "SELECT name FROM users WHERE id = $1",
        [reviewerId],
      );
      const name = reviewerName.rows[0]?.name || "Someone";
      await createNotification(
        revieweeId,
        "NEW_REVIEW",
        `${name} left you a ${rating}-star review`,
        comment ? comment.substring(0, 100) : "",
        result.rows[0].id,
      );

      const review = result.rows[0];
      res.status(201).json({
        success: true,
        data: {
          id: review.id,
          transactionId: review.transaction_id,
          reviewerId: review.reviewer_id,
          revieweeId: review.reviewee_id,
          rating: review.rating,
          comment: review.comment,
          reviewType: review.review_type,
          createdAt: review.created_at,
        },
      });
    } catch (error) {
      if (error.code === "23505") {
        // unique_violation
        return res.status(409).json({
          success: false,
          message: "You have already reviewed this transaction",
        });
      }
      logger.error("Create review error:", error);
      res
        .status(500)
        .json({ success: false, message: "Failed to create review" });
    }
  },
);

/**
 * PUT /api/v1/reviews/:id
 * Update own review
 */
router.put(
  "/:id",
  [
    body("rating")
      .optional()
      .isInt({ min: 1, max: 5 })
      .withMessage("Rating must be 1-5"),
    body("comment")
      .optional()
      .isLength({ max: 1000 })
      .withMessage("Comment too long"),
  ],
  async (req, res) => {
    try {
      const { id } = req.params;
      if (!validateUUID(id)) {
        return res
          .status(400)
          .json({ success: false, message: "Invalid review ID" });
      }

      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          message: "Validation failed",
          errors: errors.array(),
        });
      }

      const existing = await db.query(
        "SELECT reviewer_id FROM reviews WHERE id = $1",
        [id],
      );
      if (existing.rows.length === 0) {
        return res
          .status(404)
          .json({ success: false, message: "Review not found" });
      }
      if (existing.rows[0].reviewer_id !== req.user.userId) {
        return res.status(403).json({
          success: false,
          message: "You can only update your own reviews",
        });
      }

      const { rating, comment } = req.body;
      const updates = [];
      const params = [id];
      let idx = 2;

      if (rating !== undefined) {
        updates.push(`rating = $${idx}`);
        params.push(rating);
        idx++;
      }
      if (comment !== undefined) {
        updates.push(`comment = $${idx}`);
        params.push(comment);
        idx++;
      }

      if (updates.length === 0) {
        return res
          .status(400)
          .json({ success: false, message: "Nothing to update" });
      }

      const result = await db.query(
        `UPDATE reviews SET ${updates.join(", ")}, updated_at = NOW() WHERE id = $1 RETURNING *`,
        params,
      );

      const review = result.rows[0];
      res.json({
        success: true,
        data: {
          id: review.id,
          transactionId: review.transaction_id,
          reviewerId: review.reviewer_id,
          revieweeId: review.reviewee_id,
          rating: review.rating,
          comment: review.comment,
          reviewType: review.review_type,
          createdAt: review.created_at,
          updatedAt: review.updated_at,
        },
      });
    } catch (error) {
      logger.error("Update review error:", error);
      res
        .status(500)
        .json({ success: false, message: "Failed to update review" });
    }
  },
);

/**
 * DELETE /api/v1/reviews/:id
 * Delete own review
 */
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    if (!validateUUID(id)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid review ID" });
    }

    const existing = await db.query(
      "SELECT reviewer_id FROM reviews WHERE id = $1",
      [id],
    );
    if (existing.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: "Review not found" });
    }
    if (existing.rows[0].reviewer_id !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: "You can only delete your own reviews",
      });
    }

    await db.query("DELETE FROM reviews WHERE id = $1", [id]);
    res.json({ success: true, message: "Review deleted" });
  } catch (error) {
    logger.error("Delete review error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to delete review" });
  }
});

module.exports = router;
