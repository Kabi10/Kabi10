/**
 * Live Market Price Service
 * Fetches real food prices from HDX HAPI (Humanitarian Data Exchange API)
 * for Sri Lanka and populates the market_prices table in Supabase.
 *
 * Data source: WFP (World Food Programme) via HDX HAPI
 * Endpoint: https://hapi.humdata.org/api/v2/food-security-nutrition-poverty/food-prices-market-monitor
 * Requires an encoded app_identifier (base64 of "app:email").
 *
 * Tested: Returns 38 commodity types from 2 Sri Lankan markets (Polonnaruwa, Monaragala)
 * with latest data from Sep 2025. All prices in LKR.
 */

const logger = require('../utils/logger');
const { supabaseAdmin } = require('../config/supabase');

const HDX_HAPI_BASE = 'https://hapi.humdata.org/api/v2';
// Encoded app identifier: base64("agrimarket:admin@agrimarket.lk")
const HDX_APP_ID = 'YWdyaW1hcmtldDphZG1pbkBhZ3JpbWFya2V0Lmxr';
const SRI_LANKA_CODE = 'LKA';

// Commodity name → trilingual mapping for Sri Lankan crops
// Based on actual HDX HAPI response for LKA
const COMMODITY_MAP = {
  // Grains & staples
  'rice (white)': { type: 'RICE_WHITE', en: 'Rice (White)', ta: 'அரிசி (வெள்ளை)', si: 'සහල් (සුදු)' },
  'rice (medium grain)': { type: 'RICE_MEDIUM', en: 'Rice (Medium)', ta: 'அரிசி (நடுத்தர)', si: 'සහල් (මධ්‍යම)' },
  'rice (long grain)': { type: 'RICE_LONG', en: 'Rice (Long Grain)', ta: 'அரிசி (நீள்)', si: 'සහල් (දිග ඇට)' },
  'wheat flour': { type: 'WHEAT_FLOUR', en: 'Wheat Flour', ta: 'கோதுமை மாவு', si: 'ගෝධුම්බ පිටි' },

  // Vegetables
  'tomatoes': { type: 'TOMATO', en: 'Tomatoes', ta: 'தக்காளி', si: 'තක්කාලි' },
  'potatoes (local)': { type: 'POTATO_LOCAL', en: 'Potatoes (Local)', ta: 'உருளைக்கிழங்கு (உள்ளூர்)', si: 'අල (දේශීය)' },
  'potatoes (imported)': { type: 'POTATO_IMPORTED', en: 'Potatoes (Imported)', ta: 'உருளைக்கிழங்கு (இறக்குமதி)', si: 'අල (ආනයන)' },
  'onions (red, local)': { type: 'ONION_LOCAL', en: 'Red Onion (Local)', ta: 'சிவப்பு வெங்காயம் (உள்ளூர்)', si: 'රතු ළූණු (දේශීය)' },
  'onions (red, imported)': { type: 'ONION_IMPORTED', en: 'Red Onion (Imported)', ta: 'சிவப்பு வெங்காயம் (இறக்குமதி)', si: 'රතු ළූණු (ආනයන)' },
  'onions (imported)': { type: 'ONION_IMPORTED', en: 'Onion (Imported)', ta: 'வெங்காயம் (இறக்குமதி)', si: 'ළූණු (ආනයන)' },
  'onions (red)': { type: 'RED_ONION', en: 'Red Onion', ta: 'சிவப்பு வெங்காயம்', si: 'රතු ළූණු' },
  'beans': { type: 'BEANS', en: 'Beans', ta: 'பீன்ஸ்', si: 'බෝංචි' },
  'beans (mung)': { type: 'MUNG_BEANS', en: 'Mung Beans', ta: 'பச்சைப் பயிறு', si: 'මුං ඇට' },
  'lentils': { type: 'LENTILS', en: 'Lentils (Dhal)', ta: 'பருப்பு', si: 'පරිප්පු' },
  'cabbage': { type: 'CABBAGE', en: 'Cabbage', ta: 'முட்டைக்கோஸ்', si: 'ගෝවා' },
  'carrots': { type: 'CARROT', en: 'Carrots', ta: 'கேரட்', si: 'කැරට්' },
  'eggplants': { type: 'BRINJAL', en: 'Eggplant (Brinjal)', ta: 'கத்தரிக்காய்', si: 'වම්බටු' },
  'pumpkin': { type: 'PUMPKIN', en: 'Pumpkin', ta: 'பூசணி', si: 'වට්ටක්කා' },
  'snake gourd': { type: 'SNAKE_GOURD', en: 'Snake Gourd', ta: 'புடலங்காய்', si: 'පතෝල' },
  'chili (red, dry raw)': { type: 'CHILI', en: 'Chili (Dried)', ta: 'மிளகாய் (உலர்)', si: 'මිරිස් (වියළි)' },

  // Fruits
  'bananas': { type: 'BANANA', en: 'Bananas', ta: 'வாழைப்பழம்', si: 'කෙසෙල්' },
  'papaya': { type: 'PAPAYA', en: 'Papaya', ta: 'பப்பாளி', si: 'පැපොල්' },
  'pineapples': { type: 'PINEAPPLE', en: 'Pineapple', ta: 'அன்னாசி', si: 'අන්නාසි' },
  'coconut': { type: 'COCONUT', en: 'Coconut', ta: 'தேங்காய்', si: 'පොල්' },

  // Protein
  'meat (chicken, broiler)': { type: 'CHICKEN', en: 'Chicken', ta: 'கோழி', si: 'කුකුළු මස්' },
  'eggs': { type: 'EGGS', en: 'Eggs', ta: 'முட்டை', si: 'බිත්තර' },
  'fish (jack)': { type: 'FISH_JACK', en: 'Jack Fish', ta: 'பாரை மீன்', si: 'පරව් මාළු' },
  'fish (skipjack tuna)': { type: 'FISH_TUNA', en: 'Skipjack Tuna', ta: 'சூரை மீன்', si: 'බලයා මාළු' },
  'fish (goldstripe sardinella)': { type: 'FISH_SARDINE', en: 'Sardine', ta: 'நெத்தலி மீன்', si: 'සාලයා මාළු' },
  'fish (sail fish)': { type: 'FISH_SAIL', en: 'Sail Fish', ta: 'கொம்பன் மீன்', si: 'තලපත් මාළු' },
  'fish (yellowfin tuna)': { type: 'FISH_YELLOWFIN', en: 'Yellowfin Tuna', ta: 'மஞ்சள் சூரை', si: 'කෙලවල්ලා මාළු' },
  'fish (trenched sardinella)': { type: 'FISH_SARDINELLA', en: 'Sardinella', ta: 'சாளை மீன்', si: 'හුරුල්ලා මාළු' },
  'fish (dry, katta)': { type: 'FISH_DRY_KATTA', en: 'Dried Fish (Katta)', ta: 'கருவாடு (கட்டா)', si: 'කරවල (කට්ටා)' },
  'fish (dry, sprats)': { type: 'FISH_DRY_SPRATS', en: 'Dried Sprats', ta: 'நெத்திலி கருவாடு', si: 'හාල් මැස්සෝ' },

  // Other
  'sugar': { type: 'SUGAR', en: 'Sugar', ta: 'சர்க்கரை', si: 'සීනි' },
  'cowpeas (whole, average)': { type: 'COWPEAS', en: 'Cowpeas', ta: 'தட்டைப் பயிறு', si: 'කව්පි' },
};

// Market name → trilingual mapping
const MARKET_MAP = {
  'colombo': { en: 'Colombo', ta: 'கொழும்பு', si: 'කොළඹ' },
  'kandy': { en: 'Kandy', ta: 'கண்டி', si: 'මහනුවර' },
  'jaffna': { en: 'Jaffna', ta: 'யாழ்ப்பாணம்', si: 'යාපනය' },
  'galle': { en: 'Galle', ta: 'காலி', si: 'ගාල්ල' },
  'kurunegala': { en: 'Kurunegala', ta: 'குருநாகல்', si: 'කුරුණෑගල' },
  'anuradhapura': { en: 'Anuradhapura', ta: 'அனுராதபுரம்', si: 'අනුරාධපුරය' },
  'polonnaruwa': { en: 'Polonnaruwa', ta: 'பொலன்னறுவை', si: 'පොළොන්නරුව' },
  'monaragala': { en: 'Monaragala', ta: 'மொனராகலை', si: 'මොනරාගල' },
  'batticaloa': { en: 'Batticaloa', ta: 'மட்டக்களப்பு', si: 'මඩකලපුව' },
  'trincomalee': { en: 'Trincomalee', ta: 'திருகோணமலை', si: 'ත්‍රිකුණාමලය' },
  'matara': { en: 'Matara', ta: 'மாத்தறை', si: 'මාතර' },
  'ratnapura': { en: 'Ratnapura', ta: 'இரத்தினபுரி', si: 'රත්නපුර' },
  'badulla': { en: 'Badulla', ta: 'பதுளை', si: 'බදුල්ල' },
};

/**
 * Find the best matching commodity mapping
 */
function findCommodityMapping(commodityName) {
  if (!commodityName) return null;
  const lower = commodityName.toLowerCase().trim();

  // Exact match first
  if (COMMODITY_MAP[lower]) return COMMODITY_MAP[lower];

  // Partial match - check if commodity name contains a key or vice versa
  for (const [key, value] of Object.entries(COMMODITY_MAP)) {
    if (lower.includes(key) || key.includes(lower)) return value;
  }

  return null;
}

/**
 * Find the best matching market mapping
 */
function findMarketMapping(marketName) {
  if (!marketName) return null;
  const lower = marketName.toLowerCase().trim();

  for (const [key, value] of Object.entries(MARKET_MAP)) {
    if (lower.includes(key) || key.includes(lower)) return value;
  }

  // Return as-is with all languages set to the original name
  return { en: marketName, ta: marketName, si: marketName };
}

/**
 * Fetch live prices from HDX HAPI for Sri Lanka
 */
async function fetchHDXPrices() {
  const url = new URL(`${HDX_HAPI_BASE}/food-security-nutrition-poverty/food-prices-market-monitor`);
  url.searchParams.set('location_code', SRI_LANKA_CODE);
  url.searchParams.set('output_format', 'json');
  url.searchParams.set('app_identifier', HDX_APP_ID);
  url.searchParams.set('limit', '10000');

  logger.info('Fetching live prices from HDX HAPI', { url: url.toString() });

  const response = await fetch(url.toString(), {
    headers: { 'Accept': 'application/json' },
    signal: AbortSignal.timeout(30000),
  });

  if (!response.ok) {
    throw new Error(`HDX HAPI responded with ${response.status}: ${response.statusText}`);
  }

  const data = await response.json();
  return data.data || [];
}

/**
 * Transform HDX HAPI price records into our market_prices table format.
 * Groups by commodity+market, keeping the latest price and the second-latest for trend.
 */
function transformPrices(hdxRecords) {
  // Group all records by commodity+market
  const groupedByKey = new Map();

  for (const record of hdxRecords) {
    const commodity = findCommodityMapping(record.commodity_name);
    if (!commodity) continue; // Skip unmapped commodities

    const market = findMarketMapping(record.market_name || record.admin1_name);
    const key = `${commodity.type}_${market.en}`;
    const date = record.reference_period_start || '';

    if (!groupedByKey.has(key)) {
      groupedByKey.set(key, { records: [], commodity, market });
    }
    groupedByKey.get(key).records.push({ ...record, _date: date });
  }

  const results = [];

  for (const [key, { records, commodity, market }] of groupedByKey) {
    // Sort by date descending to get latest and previous
    records.sort((a, b) => (b._date || '').localeCompare(a._date || ''));

    const latest = records[0];
    const previous = records.length > 1 ? records[1] : null;

    const currentPrice = parseFloat(latest.price) || 0;
    if (currentPrice <= 0) continue;

    const previousPrice = previous ? (parseFloat(previous.price) || currentPrice) : currentPrice;
    const changeAmount = currentPrice - previousPrice;
    const changePercentage = previousPrice > 0 ? (changeAmount / previousPrice) * 100 : 0;

    let trend = 'STABLE';
    if (changePercentage > 1.0) trend = 'UP';
    else if (changePercentage < -1.0) trend = 'DOWN';

    const rawUnit = (latest.unit || 'KG').toUpperCase();
    const unit = rawUnit === 'KG' ? 'kg' : rawUnit === 'UNIT' ? 'piece' : rawUnit.toLowerCase();

    results.push({
      crop_type: commodity.type,
      crop_name_english: commodity.en,
      crop_name_tamil: commodity.ta,
      crop_name_sinhala: commodity.si,
      current_price: Math.round(currentPrice * 100) / 100,
      previous_price: Math.round(previousPrice * 100) / 100,
      unit,
      trend,
      change_percentage: Math.round(changePercentage * 100) / 100,
      change_amount: Math.round(changeAmount * 100) / 100,
      location: market.en,
      location_tamil: market.ta,
      location_sinhala: market.si,
      last_updated: latest._date || new Date().toISOString(),
      is_active: true,
      source: 'HDX_HAPI_WFP',
      reliability: 0.85,
    });
  }

  return results;
}

/**
 * Upsert transformed prices into the market_prices Supabase table.
 * Uses a single delete+insert approach for speed (avoids N+1 queries).
 */
async function upsertPrices(prices) {
  if (!prices.length) {
    logger.warn('No prices to upsert');
    return { inserted: 0, updated: 0 };
  }

  // Delete all existing HDX prices and re-insert fresh data (fast batch approach)
  const { error: deleteError } = await supabaseAdmin
    .from('market_prices')
    .delete()
    .eq('source', 'HDX_HAPI_WFP');

  if (deleteError) {
    logger.error('Failed to delete old HDX prices', { error: deleteError.message });
  }

  // Batch insert all prices at once
  const { error: insertError, data } = await supabaseAdmin
    .from('market_prices')
    .insert(prices);

  if (insertError) {
    logger.error('Failed to batch insert market prices', { error: insertError.message });
    return { inserted: 0, updated: 0, error: insertError.message };
  }

  logger.info('Market prices replaced', { count: prices.length });
  return { inserted: prices.length, updated: 0 };
}

/**
 * Main function: fetch, transform, and store live prices
 */
async function refreshLivePrices() {
  const startTime = Date.now();

  try {
    // 1. Fetch from HDX HAPI
    const hdxRecords = await fetchHDXPrices();
    logger.info(`Fetched ${hdxRecords.length} records from HDX HAPI`);

    if (hdxRecords.length === 0) {
      return {
        success: true,
        message: 'No price data available from HDX HAPI for Sri Lanka',
        duration: Date.now() - startTime,
      };
    }

    // 2. Transform to our format
    const prices = transformPrices(hdxRecords);
    logger.info(`Transformed ${prices.length} unique commodity-market prices`);

    // 3. Upsert into Supabase
    const result = await upsertPrices(prices);

    return {
      success: true,
      message: `Refreshed ${prices.length} live prices (${result.inserted} new, ${result.updated} updated)`,
      fetchedRecords: hdxRecords.length,
      transformedPrices: prices.length,
      ...result,
      duration: Date.now() - startTime,
    };
  } catch (error) {
    logger.error('Failed to refresh live prices', { error: error.message });
    throw error;
  }
}

module.exports = {
  refreshLivePrices,
  fetchHDXPrices,
  transformPrices,
};
