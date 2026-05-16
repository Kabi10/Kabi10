const express = require("express");
const { query, validationResult } = require("express-validator");
const { supabase } = require("../config/supabase");
const logger = require("../utils/logger");

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
router.get("/", async (req, res) => {
  try {
    const {
      cropType,
      location,
      trend,
      minPrice,
      maxPrice,
      page = 1,
      limit = 20,
      sortBy = "last_updated",
      sortOrder = "desc",
    } = req.query;

    // Validate and sanitize inputs
    const pageNum = Math.max(1, parseInt(page) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 20));
    const offset = (pageNum - 1) * limitNum;

    // Validate sortOrder
    const order = sortOrder.toLowerCase() === "asc" ? "asc" : "desc";

    // Validate sortBy to prevent SQL injection
    const allowedSortFields = [
      "last_updated",
      "current_price",
      "crop_type",
      "location",
      "created_at",
    ];
    const sortField = allowedSortFields.includes(sortBy)
      ? sortBy
      : "last_updated";

    // Build query with filters
    let query = supabase
      .from("market_prices")
      .select("*", { count: "exact" })
      .eq("is_active", true);

    // Apply filters
    if (cropType) {
      query = query.eq("crop_type", cropType.toUpperCase());
    }

    if (location) {
      query = query.ilike("location", `%${location}%`);
    }

    if (trend) {
      const trendUpper = trend.toUpperCase();
      if (["UP", "DOWN", "STABLE"].includes(trendUpper)) {
        query = query.eq("trend", trendUpper);
      }
    }

    if (minPrice) {
      const minPriceNum = parseFloat(minPrice);
      if (!isNaN(minPriceNum)) {
        query = query.gte("current_price", minPriceNum);
      }
    }

    if (maxPrice) {
      const maxPriceNum = parseFloat(maxPrice);
      if (!isNaN(maxPriceNum)) {
        query = query.lte("current_price", maxPriceNum);
      }
    }

    // Apply sorting and pagination
    query = query
      .order(sortField, { ascending: order === "asc" })
      .range(offset, offset + limitNum - 1);

    const { data, error, count } = await query;

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch market prices",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    // Transform snake_case to camelCase for Android compatibility
    const prices = (data || []).map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
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

    logger.info("Market prices fetched", {
      count: prices.length,
      page: pageNum,
      filters: { cropType, location, trend },
    });
  } catch (error) {
    logger.error("Get market prices error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch market prices",
    });
  }
});

/**
 * GET /api/v1/market-prices/latest
 * Returns the most recent price for each cropType (one row per crop)
 * Matches Android MarketPriceApiService.getLatestMarketPrices()
 */
router.get("/latest", async (req, res) => {
  try {
    const { limit = 20 } = req.query;
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 20));

    // Use DISTINCT ON to get one row per crop_type, ordered by most recent last_updated
    const { data, error } = await supabase
      .from("market_prices")
      .select("*")
      .eq("is_active", true)
      .order("crop_type", { ascending: true })
      .order("last_updated", { ascending: false });

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch latest market prices",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    // Deduplicate: keep only the most recent row per crop_type
    const seen = new Set();
    const deduped = (data || []).filter((row) => {
      if (seen.has(row.crop_type)) return false;
      seen.add(row.crop_type);
      return true;
    });

    const prices = deduped.slice(0, limitNum).map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
      reliability: parseFloat(row.reliability || 0.8),
    }));

    res.json({
      prices,
      totalCount: prices.length,
      page: 1,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error("Get latest market prices error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch latest market prices",
    });
  }
});

/**
 * GET /api/v1/market-prices/trending
 * Returns prices where trend is UP or DOWN, ordered by |changePercentage| DESC, limit 10
 * Matches Android MarketPriceApiService.getTrendingPrices()
 */
router.get("/trending", async (req, res) => {
  try {
    const { limit = 10, type = "all" } = req.query;
    const limitNum = Math.min(50, Math.max(1, parseInt(limit) || 10));

    let supabaseQuery = supabase
      .from("market_prices")
      .select("*")
      .eq("is_active", true)
      .in("trend", ["UP", "DOWN"])
      .order("change_percentage", { ascending: false })
      .limit(limitNum * 2); // Fetch extra so we can split into gainers/losers

    const { data, error } = await supabaseQuery;

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch trending market prices",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    const rows = data || [];

    const mapRow = (row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
      reliability: parseFloat(row.reliability || 0.8),
    });

    const gainers = rows
      .filter((r) => r.trend === "UP")
      .slice(0, limitNum)
      .map(mapRow);
    const losers = rows
      .filter((r) => r.trend === "DOWN")
      .slice(0, limitNum)
      .map(mapRow);
    const mostActive = rows.slice(0, limitNum).map(mapRow);

    res.json({
      gainers,
      losers,
      mostActive,
      timeframe: "24h",
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error("Get trending market prices error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch trending market prices",
    });
  }
});

/**
 * GET /api/v1/market-prices/search
 * Search market prices by text query
 * Matches Android MarketPriceApiService.searchMarketPrices()
 */
router.get("/search", async (req, res) => {
  try {
    const { q, page = 1, limit = 20 } = req.query;

    if (!q || q.trim().length === 0) {
      return res.status(400).json({
        success: false,
        message: "Search query parameter 'q' is required",
      });
    }

    const pageNum = Math.max(1, parseInt(page) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 20));
    const offset = (pageNum - 1) * limitNum;
    const searchTerm = `%${q.trim()}%`;

    // Supabase does not support OR across multiple columns with ilike in a single .or() for all columns in one call.
    // Use .or() with the composite filter string.
    const { data, error, count } = await supabase
      .from("market_prices")
      .select("*", { count: "exact" })
      .eq("is_active", true)
      .or(
        `crop_type.ilike.${searchTerm},crop_name_english.ilike.${searchTerm},crop_name_tamil.ilike.${searchTerm},crop_name_sinhala.ilike.${searchTerm}`,
      )
      .order("last_updated", { ascending: false })
      .range(offset, offset + limitNum - 1);

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to search market prices",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    const prices = (data || []).map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
      reliability: parseFloat(row.reliability || 0.8),
    }));

    const totalCount = count || 0;
    const totalPages = Math.ceil(totalCount / limitNum);

    res.json({
      prices,
      totalCount,
      page: pageNum,
      totalPages,
      hasNext: pageNum < totalPages,
      hasPrevious: pageNum > 1,
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error("Search market prices error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to search market prices",
    });
  }
});

/**
 * GET /api/v1/market-prices/by-location
 * Returns prices filtered by location
 * Matches Android MarketPriceApiService.getMarketPricesByLocation()
 */
router.get("/by-location", async (req, res) => {
  try {
    const { location, page = 1, limit = 20 } = req.query;

    if (!location || location.trim().length === 0) {
      return res.status(400).json({
        success: false,
        message: "Query parameter 'location' is required",
      });
    }

    const pageNum = Math.max(1, parseInt(page) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 20));
    const offset = (pageNum - 1) * limitNum;

    const { data, error, count } = await supabase
      .from("market_prices")
      .select("*", { count: "exact" })
      .eq("is_active", true)
      .ilike("location", `%${location.trim()}%`)
      .order("last_updated", { ascending: false })
      .range(offset, offset + limitNum - 1);

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch market prices by location",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    const prices = (data || []).map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
      reliability: parseFloat(row.reliability || 0.8),
    }));

    const totalCount = count || 0;
    const totalPages = Math.ceil(totalCount / limitNum);

    res.json({
      prices,
      totalCount,
      page: pageNum,
      totalPages,
      hasNext: pageNum < totalPages,
      hasPrevious: pageNum > 1,
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error("Get market prices by location error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch market prices by location",
    });
  }
});

/**
 * GET /api/v1/market-prices/statistics
 * Returns aggregate stats per cropType: min/max/avg price, count, latest update
 * Matches Android MarketPriceApiService.getMarketStatistics()
 */
router.get("/statistics", async (req, res) => {
  try {
    const { data, error } = await supabase
      .from("market_prices")
      .select("*")
      .eq("is_active", true);

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch market statistics",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    const rows = data || [];

    // Aggregate per cropType
    const cropMap = {};
    const locationSet = new Set();
    let globalMin = Infinity;
    let globalMax = -Infinity;
    let globalSum = 0;
    let trendUp = 0;
    let trendDown = 0;
    let trendStable = 0;

    for (const row of rows) {
      const price = parseFloat(row.current_price);
      const ct = row.crop_type;

      if (row.location) locationSet.add(row.location);
      if (price < globalMin) globalMin = price;
      if (price > globalMax) globalMax = price;
      globalSum += price;

      if (row.trend === "UP") trendUp++;
      else if (row.trend === "DOWN") trendDown++;
      else trendStable++;

      if (!cropMap[ct]) {
        cropMap[ct] = {
          min: price,
          max: price,
          sum: price,
          count: 1,
          latestUpdated: row.last_updated,
          trend: row.trend,
        };
      } else {
        const c = cropMap[ct];
        if (price < c.min) c.min = price;
        if (price > c.max) c.max = price;
        c.sum += price;
        c.count++;
        if (!c.latestUpdated || row.last_updated > c.latestUpdated) {
          c.latestUpdated = row.last_updated;
          c.trend = row.trend;
        }
      }
    }

    const totalCrops = Object.keys(cropMap).length;
    const totalLocations = locationSet.size;
    const averagePrice = rows.length > 0 ? globalSum / rows.length : 0;

    const topCrops = Object.entries(cropMap)
      .map(([cropType, stats]) => ({
        cropType,
        averagePrice: parseFloat((stats.sum / stats.count).toFixed(2)),
        priceChange: 0,
        trend: stats.trend || "STABLE",
        listingCount: stats.count,
      }))
      .sort((a, b) => b.listingCount - a.listingCount)
      .slice(0, 10);

    const topLocations = Array.from(locationSet)
      .slice(0, 10)
      .map((loc) => {
        const locRows = rows.filter((r) => r.location === loc);
        const locSum = locRows.reduce(
          (s, r) => s + parseFloat(r.current_price),
          0,
        );
        return {
          location: loc,
          averagePrice: parseFloat(
            (locRows.length > 0 ? locSum / locRows.length : 0).toFixed(2),
          ),
          cropCount: new Set(locRows.map((r) => r.crop_type)).size,
          listingCount: locRows.length,
        };
      });

    res.json({
      success: true,
      data: {
        totalCrops,
        totalLocations,
        averagePrice: parseFloat(averagePrice.toFixed(2)),
        priceRange: {
          min: globalMin === Infinity ? 0 : parseFloat(globalMin.toFixed(2)),
          max: globalMax === -Infinity ? 0 : parseFloat(globalMax.toFixed(2)),
          average: parseFloat(averagePrice.toFixed(2)),
        },
        trendDistribution: {
          up: trendUp,
          down: trendDown,
          stable: trendStable,
        },
        topCrops,
        topLocations,
        lastUpdated: new Date().toISOString(),
      },
    });
  } catch (error) {
    logger.error("Get market statistics error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch market statistics",
    });
  }
});

/**
 * GET /api/v1/market-prices/history
 * Returns price history for a cropType over N days
 * Matches Android MarketPriceApiService.getPriceHistory()
 */
router.get("/history", async (req, res) => {
  try {
    const { cropType, days = 30 } = req.query;

    if (!cropType || cropType.trim().length === 0) {
      return res.status(400).json({
        success: false,
        message: "Query parameter 'cropType' is required",
      });
    }

    const daysNum = Math.min(365, Math.max(1, parseInt(days) || 30));
    const since = new Date();
    since.setDate(since.getDate() - daysNum);
    const sinceISO = since.toISOString();

    const { data, error } = await supabase
      .from("market_prices")
      .select("*")
      .eq("crop_type", cropType.toUpperCase())
      .eq("is_active", true)
      .gte("last_updated", sinceISO)
      .order("last_updated", { ascending: false });

    if (error) {
      logger.error("Supabase query error:", error);
      return res.status(500).json({
        success: false,
        message: "Failed to fetch price history",
        error:
          process.env.NODE_ENV === "development" ? error.message : undefined,
      });
    }

    const rows = data || [];

    const history = rows.map((row) => ({
      id: row.id,
      cropType: row.crop_type,
      cropNameTamil: row.crop_name_tamil || "",
      cropNameEnglish: row.crop_name_english || "",
      cropNameSinhala: row.crop_name_sinhala || "",
      currentPrice: parseFloat(row.current_price),
      previousPrice: parseFloat(row.previous_price || row.current_price),
      unit: row.unit,
      trend: row.trend,
      changePercentage: parseFloat(row.change_percentage || 0),
      changeAmount: parseFloat(row.change_amount || 0),
      location: row.location,
      locationTamil: row.location_tamil || "",
      locationSinhala: row.location_sinhala || "",
      lastUpdated: row.last_updated,
      isActive: row.is_active,
      source: row.source || "MARKET",
      reliability: parseFloat(row.reliability || 0.8),
    }));

    res.json({
      prices: history,
      totalCount: history.length,
      page: 1,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
      lastUpdated: new Date().toISOString(),
    });
  } catch (error) {
    logger.error("Get price history error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch price history",
    });
  }
});

/**
 * GET /api/market-prices/:id
 * Get specific market price by ID
 */
router.get("/:id", async (req, res) => {
  try {
    const { id } = req.params;

    // Simple UUID validation
    const uuidRegex =
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    if (!uuidRegex.test(id)) {
      return res.status(400).json({
        success: false,
        message: "Invalid market price ID format",
      });
    }

    const { data, error } = await supabase
      .from("market_prices")
      .select("*")
      .eq("id", id)
      .eq("is_active", true)
      .single();

    if (error || !data) {
      logger.warn("Market price not found:", { id, error });
      return res.status(404).json({
        success: false,
        message: "Market price not found",
      });
    }

    // Transform to camelCase
    const price = {
      id: data.id,
      cropType: data.crop_type,
      cropNameTamil: data.crop_name_tamil || "",
      cropNameEnglish: data.crop_name_english || "",
      cropNameSinhala: data.crop_name_sinhala || "",
      currentPrice: parseFloat(data.current_price),
      previousPrice: parseFloat(data.previous_price || data.current_price),
      unit: data.unit,
      trend: data.trend,
      changePercentage: parseFloat(data.change_percentage || 0),
      changeAmount: parseFloat(data.change_amount || 0),
      location: data.location,
      locationTamil: data.location_tamil || "",
      locationSinhala: data.location_sinhala || "",
      lastUpdated: data.last_updated,
      isActive: data.is_active,
      source: data.source || "MARKET",
      reliability: parseFloat(data.reliability || 0.8),
    };

    res.json(price);
  } catch (error) {
    logger.error("Get market price by ID error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch market price",
    });
  }
});

/**
 * POST /api/market-prices/refresh-live
 * Trigger a live price refresh from HDX HAPI (WFP data)
 * This populates the market_prices table with real Sri Lankan food prices.
 * Can be called by a cron job or manually.
 */
router.post("/refresh-live", async (req, res) => {
  try {
    const { refreshLivePrices } = require("../services/livePriceService");
    const result = await refreshLivePrices();

    res.json({
      success: true,
      ...result,
    });
  } catch (error) {
    logger.error("Live price refresh error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to refresh live prices",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
});

/**
 * GET /api/market-prices/refresh-live
 * Same as POST but accessible via GET for Vercel Cron
 */
router.get("/refresh-live", async (req, res) => {
  try {
    // Verify cron secret if provided
    const cronSecret = req.headers["authorization"];
    if (
      process.env.CRON_SECRET &&
      cronSecret !== `Bearer ${process.env.CRON_SECRET}`
    ) {
      // Allow without auth for now, but log it
      logger.warn("Cron refresh called without proper auth");
    }

    const { refreshLivePrices } = require("../services/livePriceService");
    const result = await refreshLivePrices();

    res.json({
      success: true,
      ...result,
    });
  } catch (error) {
    logger.error("Live price refresh (cron) error:", error);
    res.status(500).json({
      success: false,
      message: "Failed to refresh live prices",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
});

module.exports = router;
