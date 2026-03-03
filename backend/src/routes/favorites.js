const express = require("express");
const { body, validationResult } = require("express-validator");
const db = require("../database/connection");
const logger = require("../utils/logger");
const { validateUUID } = require("../utils/helpers");

const router = express.Router();

/**
 * GET /api/v1/favorites
 * List user's favorited listings (paginated)
 */
router.get("/", async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const offset = (parseInt(page) - 1) * parseInt(limit);
    const userId = req.user.userId;

    const result = await db.query(
      `
      SELECT
        f.id as favorite_id,
        f.created_at as favorited_at,
        l.*,
        u.name as farmer_name,
        u.phone_number as farmer_contact
      FROM favorites f
      JOIN listings l ON f.listing_id = l.id
      JOIN users u ON l.farmer_id = u.id
      WHERE f.user_id = $1 AND l.deleted_at IS NULL
      ORDER BY f.created_at DESC
      LIMIT $2 OFFSET $3
    `,
      [userId, parseInt(limit), offset],
    );

    const countResult = await db.query(
      `SELECT COUNT(*) as total FROM favorites f
       JOIN listings l ON f.listing_id = l.id
       WHERE f.user_id = $1 AND l.deleted_at IS NULL`,
      [userId],
    );
    const total = parseInt(countResult.rows[0].total);

    const favorites = result.rows.map((row) => ({
      favoriteId: row.favorite_id,
      favoritedAt: row.favorited_at,
      listing: {
        id: row.id,
        farmerId: row.farmer_id,
        cropType: row.crop_type,
        quantity: parseFloat(row.quantity),
        unit: row.unit,
        pricePerUnit: parseFloat(row.price_per_unit),
        quality: row.quality,
        location: row.location,
        description: row.description,
        images: row.images || [],
        isActive: row.is_active,
        createdAt: row.created_at,
        farmerName: row.farmer_name,
        farmerPhone: row.farmer_contact || "",
      },
    }));

    res.json({
      success: true,
      favorites,
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
    });
  } catch (error) {
    logger.error("Get favorites error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to fetch favorites" });
  }
});

/**
 * POST /api/v1/favorites
 * Add listing to favorites
 */
router.post(
  "/",
  [body("listingId").isUUID().withMessage("Invalid listing ID")],
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

      const { listingId } = req.body;
      const userId = req.user.userId;

      // Check listing exists
      const listing = await db.query(
        "SELECT id FROM listings WHERE id = $1 AND deleted_at IS NULL",
        [listingId],
      );
      if (listing.rows.length === 0) {
        return res
          .status(404)
          .json({ success: false, message: "Listing not found" });
      }

      const result = await db.query(
        `INSERT INTO favorites (user_id, listing_id)
         VALUES ($1, $2)
         ON CONFLICT (user_id, listing_id) DO NOTHING
         RETURNING *`,
        [userId, listingId],
      );

      res.status(201).json({
        success: true,
        data: result.rows[0]
          ? {
              id: result.rows[0].id,
              userId: result.rows[0].user_id,
              listingId: result.rows[0].listing_id,
              createdAt: result.rows[0].created_at,
            }
          : { message: "Already favorited" },
      });
    } catch (error) {
      logger.error("Add favorite error:", error);
      res
        .status(500)
        .json({ success: false, message: "Failed to add favorite" });
    }
  },
);

/**
 * DELETE /api/v1/favorites/:listingId
 * Remove listing from favorites
 */
router.delete("/:listingId", async (req, res) => {
  try {
    const { listingId } = req.params;
    if (!validateUUID(listingId)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid listing ID" });
    }

    await db.query(
      "DELETE FROM favorites WHERE user_id = $1 AND listing_id = $2",
      [req.user.userId, listingId],
    );

    res.json({ success: true, message: "Favorite removed" });
  } catch (error) {
    logger.error("Remove favorite error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to remove favorite" });
  }
});

/**
 * GET /api/v1/favorites/check/:listingId
 * Check if listing is favorited
 */
router.get("/check/:listingId", async (req, res) => {
  try {
    const { listingId } = req.params;
    if (!validateUUID(listingId)) {
      return res
        .status(400)
        .json({ success: false, message: "Invalid listing ID" });
    }

    const result = await db.query(
      "SELECT id FROM favorites WHERE user_id = $1 AND listing_id = $2",
      [req.user.userId, listingId],
    );

    res.json({
      success: true,
      isFavorited: result.rows.length > 0,
    });
  } catch (error) {
    logger.error("Check favorite error:", error);
    res
      .status(500)
      .json({ success: false, message: "Failed to check favorite" });
  }
});

module.exports = router;
