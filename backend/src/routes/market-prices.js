const express = require('express');
const { query, validationResult } = require('express-validator');
const { supabase } = require('../config/supabase');
const logger = require('../utils/logger');

const router = express.Router();

/**
 * GET /api/market-prices
 * Get all active market prices with optional filtering
 * Matches Android MarketPriceApiService.getMarketPrices()
 *
 * Query Parameters:
 * - cropType: Filter by specific crop type (e.g., "RED_ONION", "TOMATO")
 * - location: Filter by location (partial match, case-insensitive)
 * - trend: Filter by price trend ("UP", "DOWN", "STABLE")
 * - limit: Number of results per page (default: 20, max: 100)
 * - page: Page number for pagination (default: 1)
 * - sortBy: Sort field (default: "last_updated")
 * - sortOrder: Sort direction ("asc" or "desc", default: "desc")
 */
router.get('/', async (req, res) => {
  try {
    const {
      cropType,
      location,
      trend,
      minPrice,
      maxPrice,
      page = 1,
      limit = 20,
      sortBy = 'last_updated',
      sortOrder = 'desc',
    } = req.query;

    // Validate and sanitize inputs
    const pageNum = Math.max(1, parseInt(page) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 20));
    const offset = (pageNum - 1) * limitNum;

    // Validate sortOrder
    const order = sortOrder.toLowerCase() === 'asc' ? 'asc' : 'desc';

    // Validate sortBy to prevent SQL injection
    const allowedSortFields = ['last_updated', 'current_price', 'crop_type', 'location', 'created_at'];
    const sortField = allowedSortFields.includes(sortBy) ? sortBy : 'last_updated';

    // Build query with filters
    let query = supabase
      .from('market_prices')
      .select('*', { count: 'exact' })
      .eq('is_active', true);

    // Apply filters
    if (cropType) {
      query = query.eq('crop_type', cropType.toUpperCase());
    }

    if (location) {
      query = query.ilike('location', `%${location}%`);
    }

    if (trend) {
      const trendUpper = trend.toUpperCase();
      if (['UP', 'DOWN', 'STABLE'].includes(trendUpper)) {
        query = query.eq('trend', trendUpper);
      }
    }

    if (minPrice) {
      const minPriceNum = parseFloat(minPrice);
      if (!isNaN(minPriceNum)) {
        query = query.gte('current_price', minPriceNum);
      }
    }

    if (maxPrice) {
      const maxPriceNum = parseFloat(maxPrice);
      if (!isNaN(maxPriceNum)) {
        query = query.lte('current_price', maxPriceNum);
      }
    }

    // Apply sorting and pagination
    query = query
      .order(sortField, { ascending: order === 'asc' })
      .range(offset, offset + limitNum - 1);

    const { data, error, count } = await query;

    if (error) {
      logger.error('Supabase query error:', error);
      return res.status(500).json({
        success: false,
        message: 'Failed to fetch market prices',
        error: process.env.NODE_ENV === 'development' ? error.message : undefined,
      });
    }

    // Transform snake_case to camelCase for Android compatibility
    const prices = (data || []).map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || '',
      cropNameEnglish: row.crop_name_english || '',
      cropNameSinhala: row.crop_name_sinhala || '',
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || '',
      locationSinhala: row.location_sinhala || '',
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || 'MARKET',
      reliability: parseFloat(row.reliability || 0.8),
    }));

    const totalCount = count || 0;
    const totalPages = Math.ceil(totalCount / limitNum);

    // Android MarketPricesResponse format
    res.json({
      prices,
      totalCount,
      page: pageNum,
      totalPages,
      hasNext: pageNum < totalPages,
      hasPrevious: pageNum > 1,
      lastUpdated: new Date().toISOString(),
    });

    logger.info('Market prices fetched', {
      count: prices.length,
      page: pageNum,
      filters: { cropType, location, trend },
    });
  } catch (error) {
    logger.error('Get market prices error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch market prices',
    });
  }
});

/**
 * GET /api/market-prices/:id
 * Get specific market price by ID
 */
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    // Simple UUID validation
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    if (!uuidRegex.test(id)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid market price ID format',
      });
    }

    const { data, error } = await supabase
      .from('market_prices')
      .select('*')
      .eq('id', id)
      .eq('is_active', true)
      .single();

    if (error || !data) {
      logger.warn('Market price not found:', { id, error });
      return res.status(404).json({
        success: false,
        message: 'Market price not found',
      });
    }

    // Transform to camelCase
    const price = {
      id: data.id,
      cropType: data.crop_type,
      cropNameTamil: data.crop_name_tamil || '',
      cropNameEnglish: data.crop_name_english || '',
      cropNameSinhala: data.crop_name_sinhala || '',
      currentPrice: parseFloat(data.current_price),
      previousPrice: parseFloat(data.previous_price || data.current_price),
      unit: data.unit,
      trend: data.trend,
      changePercentage: parseFloat(data.change_percentage || 0),
      changeAmount: parseFloat(data.change_amount || 0),
      location: data.location,
      locationTamil: data.location_tamil || '',
      locationSinhala: data.location_sinhala || '',
      lastUpdated: data.last_updated,
      isActive: data.is_active,
      source: data.source || 'MARKET',
      reliability: parseFloat(data.reliability || 0.8),
    };

    res.json(price);
  } catch (error) {
    logger.error('Get market price by ID error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch market price',
    });
  }
});

/**
 * POST /api/market-prices/refresh-live
 * Trigger a live price refresh from HDX HAPI (WFP data)
 * This populates the market_prices table with real Sri Lankan food prices.
 * Can be called by a cron job or manually.
 */
router.post('/refresh-live', async (req, res) => {
  try {
    const { refreshLivePrices } = require('../services/livePriceService');
    const result = await refreshLivePrices();

    res.json({
      success: true,
      ...result,
    });
  } catch (error) {
    logger.error('Live price refresh error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to refresh live prices',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
});

/**
 * GET /api/market-prices/refresh-live
 * Same as POST but accessible via GET for Vercel Cron
 */
router.get('/refresh-live', async (req, res) => {
  try {
    // Verify cron secret if provided
    const cronSecret = req.headers['authorization'];
    if (process.env.CRON_SECRET && cronSecret !== `Bearer ${process.env.CRON_SECRET}`) {
      // Allow without auth for now, but log it
      logger.warn('Cron refresh called without proper auth');
    }

    const { refreshLivePrices } = require('../services/livePriceService');
    const result = await refreshLivePrices();

    res.json({
      success: true,
      ...result,
    });
  } catch (error) {
    logger.error('Live price refresh (cron) error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to refresh live prices',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined,
    });
  }
});

module.exports = router;
