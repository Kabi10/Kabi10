const express = require('express');
const { body, query, validationResult } = require('express-validator');
const db = require('../database/connection');
const logger = require('../utils/logger');
const { validateUUID } = require('../utils/helpers');

const router = express.Router();

/**
 * Middleware to apply safe defaults for listing creation
 * Makes the API backward-compatible with simpler Android clients
 */
const applyListingDefaults = (req, res, next) => {
  const body = req.body;

  // Default availableFrom to today if not provided
  if (!body.availableFrom) {
    body.availableFrom = new Date().toISOString().split('T')[0];
  }

  // Default availableUntil to 30 days from today if not provided
  if (!body.availableUntil) {
    const thirtyDaysFromNow = new Date();
    thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30);
    body.availableUntil = thirtyDaysFromNow.toISOString().split('T')[0];
  }

  // Default pickupLocations to [location] if location is provided but pickupLocations is not
  if (!body.pickupLocations || !Array.isArray(body.pickupLocations) || body.pickupLocations.length === 0) {
    if (body.location) {
      body.pickupLocations = [body.location];
    }
  }

  // Default quality to 'B' (standard) if not provided
  if (!body.quality) {
    body.quality = 'B';
  }

  // Default unit to 'kg' if not provided
  if (!body.unit) {
    body.unit = 'kg';
  }

  next();
};

// Validation schemas
const createListingValidation = [
  body('cropType').isIn([
    'rice', 'coconut', 'banana', 'mango', 'papaya', 'pineapple',
    'tomato', 'onion', 'potato', 'carrot', 'cabbage', 'beans',
    'okra', 'eggplant', 'chili', 'curry_leaves', 'coriander',
    'mint', 'lemongrass', 'ginger', 'turmeric', 'other',
  ]).withMessage('Invalid crop type'),
  body('quantity').isFloat({ min: 0.1 }).withMessage('Quantity must be greater than 0'),
  body('unit').isIn(['kg', 'g', 'lb', 'piece', 'bunch', 'bag']).withMessage('Invalid unit'),
  body('pricePerUnit').isFloat({ min: 0.01 }).withMessage('Price must be greater than 0'),
  body('quality').isIn(['A', 'B', 'C']).withMessage('Quality must be A, B, or C'),
  body('location').isLength({ min: 1, max: 255 }).withMessage('Location is required'),
  body('availableFrom').isISO8601().withMessage('Invalid date format'),
  body('availableUntil').isISO8601().withMessage('Invalid date format'),
  body('pickupLocations').isArray({ min: 1 }).withMessage('At least one pickup location required'),
  body('description').optional().isLength({ max: 1000 }).withMessage('Description too long'),
  // Storytelling fields
  body('story').optional().isString(),
  body('farmingMethods').optional().isArray(),
  body('certifications').optional().isArray(),
  body('harvestedAt').optional().isISO8601().withMessage('Invalid harvest date format'),
  body('sustainabilityPractices').optional().isArray(),
];

/**
 * GET /api/v1/listings
 * Get all active listings with optional filtering
 * Matches Android ListingApiService.getListings()
 */
router.get('/', async (req, res) => {
  try {
    const {
      cropType,
      location,
      minPrice,
      maxPrice,
      quality,
      farmerId,
      page = 1,
      limit = 20,
      sortBy = 'created_at',
      sortOrder = 'DESC',
    } = req.query;

    // Build dynamic query
    const whereConditions = ['l.is_active = true', 'l.available_until >= CURRENT_DATE', 'l.deleted_at IS NULL'];
    const queryParams = [];
    let paramIndex = 1;

    if (cropType) {
      whereConditions.push(`l.crop_type = $${paramIndex}`);
      queryParams.push(cropType);
      paramIndex++;
    }

    if (location) {
      whereConditions.push(`l.location ILIKE $${paramIndex}`);
      queryParams.push(`%${location}%`);
      paramIndex++;
    }

    if (minPrice) {
      whereConditions.push(`l.price_per_unit >= $${paramIndex}`);
      queryParams.push(parseFloat(minPrice));
      paramIndex++;
    }

    if (maxPrice) {
      whereConditions.push(`l.price_per_unit <= $${paramIndex}`);
      queryParams.push(parseFloat(maxPrice));
      paramIndex++;
    }

    if (quality) {
      whereConditions.push(`l.quality = $${paramIndex}`);
      queryParams.push(quality);
      paramIndex++;
    }

    if (farmerId) {
      whereConditions.push(`l.farmer_id = $${paramIndex}`);
      queryParams.push(farmerId);
      paramIndex++;
    }

    // Calculate offset
    const offset = (parseInt(page) - 1) * parseInt(limit);

    // Main query
    const query = `
      SELECT 
        l.*,
        u.name as farmer_name,
        u.phone_number as farmer_contact
      FROM listings l
      JOIN users u ON l.farmer_id = u.id
      WHERE ${whereConditions.join(' AND ')}
      ORDER BY l.${sortBy} ${sortOrder}
      LIMIT $${paramIndex} OFFSET $${paramIndex + 1}
    `;

    queryParams.push(parseInt(limit), offset);

    const result = await db.query(query, queryParams);

    // Get total count for pagination
    const countQuery = `
      SELECT COUNT(*) as total
      FROM listings l
      WHERE ${whereConditions.join(' AND ')}
    `;

    const countResult = await db.query(countQuery, queryParams.slice(0, -2));
    const total = parseInt(countResult.rows[0].total);

    // Transform data to match Android model
    const listings = result.rows.map((row) => ({
      id: row.id,
      farmerId: row.farmer_id,
      cropType: row.crop_type,
      quantity: parseFloat(row.quantity),
      unit: row.unit,
      pricePerUnit: parseFloat(row.price_per_unit),
      quality: row.quality,
      location: row.location,
      pickupLocations: row.pickup_locations || [],
      availableFrom: row.available_from,
      availableUntil: row.available_until,
      description: row.description,
      images: row.images || [],
      isActive: row.is_active,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
      farmerName: row.farmer_name,
      farmerContact: row.farmer_contact,
      // Storytelling fields
      story: row.story || '',
      farmingMethods: row.farming_methods || [],
      certifications: row.certifications || [],
      harvestedAt: row.harvested_at ? row.harvested_at.toISOString().split('T')[0] : '',
      sustainabilityPractices: row.sustainability_practices || [],
    }));

    // Response matches Android ListingsResponse DTO
    res.json({
      success: true,
      listings: listings,  // Android expects 'listings' not 'data'
      totalCount: total,
      page: parseInt(page),
      totalPages: Math.ceil(total / parseInt(limit)),
      hasNext: parseInt(page) < Math.ceil(total / parseInt(limit)),
      hasPrevious: parseInt(page) > 1,
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error('Get listings error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch listings',
    });
  }
});

/**
 * GET /api/v1/listings/:id
 * Get specific listing by ID
 */
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid listing ID',
      });
    }

    // Increment view count
    await db.query('SELECT increment_listing_views($1)', [id]);

    const result = await db.query(`
      SELECT 
        l.*,
        u.name as farmer_name,
        u.phone_number as farmer_contact,
        u.location as farmer_location
      FROM listings l
      JOIN users u ON l.farmer_id = u.id
      WHERE l.id = $1 AND l.is_active = true AND l.deleted_at IS NULL
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Listing not found',
      });
    }

    const listing = result.rows[0];

    res.json({
      success: true,
      data: {
        id: listing.id,
        farmerId: listing.farmer_id,
        cropType: listing.crop_type,
        quantity: parseFloat(listing.quantity),
        unit: listing.unit,
        pricePerUnit: parseFloat(listing.price_per_unit),
        quality: listing.quality,
        location: listing.location,
        pickupLocations: listing.pickup_locations || [],
        availableFrom: listing.available_from,
        availableUntil: listing.available_until,
        description: listing.description,
        images: listing.images || [],
        isActive: listing.is_active,
        createdAt: listing.created_at,
        updatedAt: listing.updated_at,
        viewCount: listing.view_count,
        farmer: {
          name: listing.farmer_name,
          contact: listing.farmer_contact,
          location: listing.farmer_location,
        },
        // Storytelling fields
        story: listing.story || '',
        farmingMethods: listing.farming_methods || [],
        certifications: listing.certifications || [],
        harvestedAt: listing.harvested_at ? listing.harvested_at.toISOString().split('T')[0] : '',
        sustainabilityPractices: listing.sustainability_practices || [],
      },
    });
  } catch (error) {
    logger.error('Get listing error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch listing',
    });
  }
});

/**
 * POST /api/v1/listings
 * Create new listing
 * Matches Android CreateListingRequest
 */
router.post('/', applyListingDefaults, createListingValidation, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array(),
      });
    }

    const {
      cropType,
      quantity,
      unit,
      pricePerUnit,
      quality,
      location,
      pickupLocations,
      availableFrom,
      availableUntil,
      description,
      images = [],
      // Storytelling fields
      story,
      farmingMethods = [],
      certifications = [],
      harvestedAt,
      sustainabilityPractices = [],
    } = req.body;

    // Validate date range
    if (new Date(availableUntil) <= new Date(availableFrom)) {
      return res.status(400).json({
        success: false,
        message: 'Available until date must be after available from date',
      });
    }

    // Check if user is a farmer
    const user = await db.query(
      'SELECT user_type, can_sell FROM users WHERE id = $1',
      [req.user.userId],
    );

    // Allow if user is FARMER OR can_sell is TRUE
    if (user.rows.length === 0 || (user.rows[0].user_type !== 'FARMER' && !user.rows[0].can_sell)) {
      return res.status(403).json({
        success: false,
        message: 'Only farmers or approved sellers can create listings',
      });
    }

    const result = await db.query(`
      INSERT INTO listings (
        farmer_id, crop_type, quantity, unit, price_per_unit,
        quality, location, pickup_locations, available_from,
        available_until, description, images,
        story, farming_methods, certifications, harvested_at, sustainability_practices
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17)
      RETURNING *
    `, [
      req.user.userId,
      cropType,
      quantity,
      unit,
      pricePerUnit,
      quality,
      location,
      pickupLocations,
      availableFrom,
      availableUntil,
      description,
      images,
      story,
      farmingMethods,
      JSON.stringify(certifications), // Store JSONB as string if needed, or pass object if pg driver handles it. pg usually handles objects for JSONB.
      harvestedAt,
      sustainabilityPractices,
    ]);

    const listing = result.rows[0];

    logger.info('Listing created', {
      listingId: listing.id,
      farmerId: req.user.userId,
      cropType,
    });

    res.status(201).json({
      success: true,
      data: {
        id: listing.id,
        farmerId: listing.farmer_id,
        cropType: listing.crop_type,
        quantity: parseFloat(listing.quantity),
        unit: listing.unit,
        pricePerUnit: parseFloat(listing.price_per_unit),
        quality: listing.quality,
        location: listing.location,
        pickupLocations: listing.pickup_locations || [],
        availableFrom: listing.available_from,
        availableUntil: listing.available_until,
        description: listing.description,
        images: listing.images || [],
        isActive: listing.is_active,
        createdAt: listing.created_at,
        updatedAt: listing.updated_at,
        // Storytelling fields
        story: listing.story || '',
        farmingMethods: listing.farming_methods || [],
        certifications: listing.certifications || [],
        harvestedAt: listing.harvested_at ? listing.harvested_at.toISOString().split('T')[0] : '',
        sustainabilityPractices: listing.sustainability_practices || [],
      },
    });
  } catch (error) {
    logger.error('Create listing error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create listing',
    });
  }
});

/**
 * PUT /api/v1/listings/:id
 * Update existing listing
 */
router.put('/:id', createListingValidation, async (req, res) => {
  try {
    const { id } = req.params;
    const errors = validationResult(req);

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid listing ID',
      });
    }

    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: errors.array(),
      });
    }

    // Check if listing exists and belongs to user
    const existingListing = await db.query(
      'SELECT farmer_id FROM listings WHERE id = $1 AND deleted_at IS NULL',
      [id],
    );

    if (existingListing.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Listing not found',
      });
    }

    if (existingListing.rows[0].farmer_id !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'You can only update your own listings',
      });
    }

    const {
      cropType,
      quantity,
      unit,
      pricePerUnit,
      quality,
      location,
      pickupLocations,
      availableFrom,
      availableUntil,
      description,
      images = [],
      // Storytelling fields
      story,
      farmingMethods = [],
      certifications = [],
      harvestedAt,
      sustainabilityPractices = [],
    } = req.body;

    const result = await db.query(`
      UPDATE listings SET
        crop_type = $2,
        quantity = $3,
        unit = $4,
        price_per_unit = $5,
        quality = $6,
        location = $7,
        pickup_locations = $8,
        available_from = $9,
        available_until = $10,
        description = $11,
        images = $12,
        story = $13,
        farming_methods = $14,
        certifications = $15,
        harvested_at = $16,
        sustainability_practices = $17,
        updated_at = NOW()
      WHERE id = $1
      RETURNING *
    `, [
      id, cropType, quantity, unit, pricePerUnit,
      quality, location, pickupLocations, availableFrom,
      availableUntil, description, images,
      story, farmingMethods, JSON.stringify(certifications), harvestedAt, sustainabilityPractices,
    ]);

    const listing = result.rows[0];

    logger.info('Listing updated', {
      listingId: id,
      farmerId: req.user.userId,
    });

    res.json({
      success: true,
      data: {
        id: listing.id,
        farmerId: listing.farmer_id,
        cropType: listing.crop_type,
        quantity: parseFloat(listing.quantity),
        unit: listing.unit,
        pricePerUnit: parseFloat(listing.price_per_unit),
        quality: listing.quality,
        location: listing.location,
        pickupLocations: listing.pickup_locations || [],
        availableFrom: listing.available_from,
        availableUntil: listing.available_until,
        description: listing.description,
        images: listing.images || [],
        isActive: listing.is_active,
        createdAt: listing.created_at,
        updatedAt: listing.updated_at,
      },
    });
  } catch (error) {
    logger.error('Update listing error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update listing',
    });
  }
});

/**
 * POST /api/v1/listings/:id/images
 * Upload images for a listing
 */
router.post('/:id/images', async (req, res) => {
  try {
    const { id } = req.params;
    const { images } = req.body; // Array of base64 or URLs

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid listing ID',
      });
    }

    if (!images || !Array.isArray(images) || images.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'No images provided',
      });
    }

    if (images.length > 5) {
      return res.status(400).json({
        success: false,
        message: 'Maximum 5 images allowed',
      });
    }

    // Check if listing exists and belongs to user
    const existingListing = await db.query(
      'SELECT farmer_id, images FROM listings WHERE id = $1',
      [id],
    );

    if (existingListing.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Listing not found',
      });
    }

    if (existingListing.rows[0].farmer_id !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'You can only upload images to your own listings',
      });
    }

    // For now, just store the URLs directly
    // In production, you would upload to Supabase Storage here
    const currentImages = existingListing.rows[0].images || [];
    const updatedImages = [...currentImages, ...images].slice(0, 5);

    await db.query(
      'UPDATE listings SET images = $1, updated_at = NOW() WHERE id = $2',
      [updatedImages, id],
    );

    logger.info('Images uploaded to listing', {
      listingId: id,
      imageCount: images.length,
    });

    res.json({
      success: true,
      data: {
        images: updatedImages,
      },
    });
  } catch (error) {
    logger.error('Upload images error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to upload images',
    });
  }
});

/**
 * DELETE /api/v1/listings/:id
 * Delete (deactivate) listing
 */
router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!validateUUID(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid listing ID',
      });
    }

    // Check if listing exists and belongs to user
    const existingListing = await db.query(
      'SELECT farmer_id FROM listings WHERE id = $1',
      [id],
    );

    if (existingListing.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Listing not found',
      });
    }

    if (existingListing.rows[0].farmer_id !== req.user.userId) {
      return res.status(403).json({
        success: false,
        message: 'You can only delete your own listings',
      });
    }

    // Soft delete by setting is_active to false
    await db.query(
      'UPDATE listings SET is_active = false, updated_at = NOW() WHERE id = $1',
      [id],
    );

    logger.info('Listing deleted', {
      listingId: id,
      farmerId: req.user.userId,
    });

    res.json({
      success: true,
      message: 'Listing deleted successfully',
    });
  } catch (error) {
    logger.error('Delete listing error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete listing',
    });
  }
});

module.exports = router;
