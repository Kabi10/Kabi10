# Missing Backend Endpoints - Implementation Guide

## Overview

This document provides complete implementation code for all missing backend endpoints required by the Android app.

---

## 1. Listings Endpoints

### 1.1 GET /api/v1/listings - List All Listings

**File:** `backend/api/listings/index.js`

**Replace existing stub with:**

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  // CORS headers
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const {
      cropType,
      quality,
      minPrice,
      maxPrice,
      location,
      farmerId,
      isActive = true,
      page = 1,
      limit = 20,
      sortBy = 'created_at',
      sortOrder = 'desc',
      q: searchQuery
    } = req.query;

    // Build query
    let query = supabaseAdmin
      .from('listings')
      .select('*', { count: 'exact' });

    // Apply filters
    if (cropType) query = query.eq('crop_type', cropType);
    if (quality) query = query.eq('quality', quality);
    if (minPrice) query = query.gte('price_per_unit', parseFloat(minPrice));
    if (maxPrice) query = query.lte('price_per_unit', parseFloat(maxPrice));
    if (location) query = query.ilike('location', `%${location}%`);
    if (farmerId) query = query.eq('farmer_id', farmerId);
    if (isActive !== undefined) query = query.eq('is_active', isActive === 'true');

    // Search query
    if (searchQuery) {
      query = query.or(`crop_type.ilike.%${searchQuery}%,location.ilike.%${searchQuery}%,description.ilike.%${searchQuery}%`);
    }

    // Pagination
    const offset = (parseInt(page) - 1) * parseInt(limit);
    query = query.range(offset, offset + parseInt(limit) - 1);

    // Sorting
    const validSortFields = ['created_at', 'price_per_unit', 'quantity', 'harvest_date'];
    const sortField = validSortFields.includes(sortBy) ? sortBy : 'created_at';
    query = query.order(sortField, { ascending: sortOrder === 'asc' });

    const { data: listings, error, count } = await query;

    if (error) {
      logger.error('Get listings error:', error);
      return res.status(500).json({ success: false, message: 'Failed to fetch listings' });
    }

    // Transform to match Android model
    const transformedListings = listings.map(listing => ({
      id: listing.id,
      farmerId: listing.farmer_id,
      cropType: listing.crop_type,
      cropNameTamil: listing.crop_name_tamil || '',
      cropNameEnglish: listing.crop_name_english || '',
      cropNameSinhala: listing.crop_name_sinhala || '',
      quantity: parseFloat(listing.quantity),
      unit: listing.unit,
      pricePerUnit: parseFloat(listing.price_per_unit),
      quality: listing.quality,
      harvestDate: listing.harvest_date,
      availableFrom: listing.available_from,
      availableUntil: listing.available_until,
      location: listing.location,
      locationTamil: listing.location_tamil || '',
      locationSinhala: listing.location_sinhala || '',
      description: listing.description || '',
      descriptionTamil: listing.description_tamil || '',
      descriptionSinhala: listing.description_sinhala || '',
      images: listing.images || [],
      pickupLocations: listing.pickup_locations || [],
      isActive: listing.is_active,
      syncStatus: 'SYNCED',
      createdAt: listing.created_at,
      updatedAt: listing.updated_at,
      viewCount: listing.view_count || 0,
      inquiryCount: listing.inquiry_count || 0
    }));

    res.status(200).json({
      success: true,
      data: transformedListings,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total: count,
        totalPages: Math.ceil(count / parseInt(limit))
      }
    });
  } catch (error) {
    logger.error('Get listings error:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch listings' });
  }
};
```

### 1.2 GET /api/v1/listings/[id].js - Get Single Listing

**File:** `backend/api/listings/[id].js` (create new file)

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,PUT,DELETE,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  const { id } = req.query;

  try {
    if (req.method === 'GET') {
      const { data: listing, error } = await supabaseAdmin
        .from('listings')
        .select('*')
        .eq('id', id)
        .single();

      if (error || !listing) {
        return res.status(404).json({ success: false, message: 'Listing not found' });
      }

      // Increment view count
      await supabaseAdmin.rpc('increment_listing_views', { listing_uuid: id });

      res.status(200).json({
        success: true,
        data: {
          id: listing.id,
          farmerId: listing.farmer_id,
          cropType: listing.crop_type,
          quantity: parseFloat(listing.quantity),
          unit: listing.unit,
          pricePerUnit: parseFloat(listing.price_per_unit),
          quality: listing.quality,
          harvestDate: listing.harvest_date,
          availableFrom: listing.available_from,
          availableUntil: listing.available_until,
          location: listing.location,
          description: listing.description || '',
          images: listing.images || [],
          pickupLocations: listing.pickup_locations || [],
          isActive: listing.is_active,
          createdAt: listing.created_at,
          updatedAt: listing.updated_at,
          viewCount: (listing.view_count || 0) + 1
        }
      });
    } else if (req.method === 'PUT') {
      // Update listing (requires authentication)
      const { authenticateToken } = require('../../src/middleware/auth');
      const authResult = await authenticateToken(req);
      
      if (!authResult.success) {
        return res.status(401).json(authResult);
      }

      const updates = req.body;
      const { data: listing, error } = await supabaseAdmin
        .from('listings')
        .update({
          crop_type: updates.cropType,
          quantity: updates.quantity,
          unit: updates.unit,
          price_per_unit: updates.pricePerUnit,
          quality: updates.quality,
          harvest_date: updates.harvestDate,
          available_from: updates.availableFrom,
          available_until: updates.availableUntil,
          location: updates.location,
          description: updates.description,
          images: updates.images,
          pickup_locations: updates.pickupLocations,
          updated_at: new Date().toISOString()
        })
        .eq('id', id)
        .eq('farmer_id', authResult.user.userId)
        .select()
        .single();

      if (error) {
        return res.status(500).json({ success: false, message: 'Failed to update listing' });
      }

      res.status(200).json({ success: true, data: listing });
    } else if (req.method === 'DELETE') {
      // Soft delete (set isActive = false)
      const { authenticateToken } = require('../../src/middleware/auth');
      const authResult = await authenticateToken(req);
      
      if (!authResult.success) {
        return res.status(401).json(authResult);
      }

      const { error } = await supabaseAdmin
        .from('listings')
        .update({ is_active: false, updated_at: new Date().toISOString() })
        .eq('id', id)
        .eq('farmer_id', authResult.user.userId);

      if (error) {
        return res.status(500).json({ success: false, message: 'Failed to delete listing' });
      }

      res.status(200).json({ success: true, message: 'Listing deleted successfully' });
    } else {
      res.status(405).json({ success: false, message: 'Method not allowed' });
    }
  } catch (error) {
    logger.error('Listing operation error:', error);
    res.status(500).json({ success: false, message: 'Operation failed' });
  }
};
```

---

## 2. Market Prices Endpoints

### 2.1 GET /api/v1/market-prices/index.js

**File:** `backend/api/market-prices/index.js` (create new directory and file)

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const { cropType, location, isActive = true, limit = 50 } = req.query;

    let query = supabaseAdmin
      .from('market_prices')
      .select('*')
      .order('last_updated', { ascending: false });

    if (cropType) query = query.eq('crop_type', cropType);
    if (location) query = query.ilike('location', `%${location}%`);
    if (isActive !== undefined) query = query.eq('is_active', isActive === 'true');
    
    query = query.limit(parseInt(limit));

    const { data: prices, error } = await query;

    if (error) {
      logger.error('Get market prices error:', error);
      return res.status(500).json({ success: false, message: 'Failed to fetch market prices' });
    }

    const transformedPrices = prices.map(price => ({
      id: price.id,
      cropType: price.crop_type,
      cropNameTamil: price.crop_name_tamil || '',
      cropNameEnglish: price.crop_name_english || '',
      cropNameSinhala: price.crop_name_sinhala || '',
      currentPrice: parseFloat(price.current_price),
      previousPrice: parseFloat(price.previous_price || price.current_price),
      unit: price.unit,
      trend: price.trend || 'STABLE',
      changePercentage: parseFloat(price.change_percentage || 0),
      changeAmount: parseFloat(price.change_amount || 0),
      location: price.location,
      locationTamil: price.location_tamil || '',
      locationSinhala: price.location_sinhala || '',
      lastUpdated: price.last_updated,
      isActive: price.is_active,
      source: price.source || 'MARKET',
      reliability: parseFloat(price.reliability || 0.8)
    }));

    res.status(200).json({
      success: true,
      data: transformedPrices
    });
  } catch (error) {
    logger.error('Get market prices error:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch market prices' });
  }
};
```

---

## 3. Transactions Endpoints

### 3.1 GET /api/v1/transactions/index.js

**File:** `backend/api/transactions/index.js`

**Replace stub with:**

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const { status, page = 1, limit = 20 } = req.query;
    const userId = authResult.user.userId;

    let query = supabaseAdmin
      .from('transactions')
      .select('*', { count: 'exact' })
      .or(`farmer_id.eq.${userId},buyer_id.eq.${userId}`);

    if (status) query = query.eq('status', status);

    const offset = (parseInt(page) - 1) * parseInt(limit);
    query = query
      .range(offset, offset + parseInt(limit) - 1)
      .order('created_at', { ascending: false });

    const { data: transactions, error, count } = await query;

    if (error) {
      logger.error('Get transactions error:', error);
      return res.status(500).json({ success: false, message: 'Failed to fetch transactions' });
    }

    const transformedTransactions = transactions.map(txn => ({
      id: txn.id,
      listingId: txn.listing_id,
      farmerId: txn.farmer_id,
      buyerId: txn.buyer_id,
      quantity: parseFloat(txn.quantity),
      unit: txn.unit,
      pricePerUnit: parseFloat(txn.price_per_unit),
      totalAmount: parseFloat(txn.total_amount),
      pickupLocation: txn.pickup_location,
      pickupDate: txn.pickup_date,
      pickupTime: txn.pickup_time,
      status: txn.status,
      paymentMethod: txn.payment_method,
      paymentStatus: txn.payment_status,
      notes: txn.notes || '',
      createdAt: txn.created_at,
      updatedAt: txn.updated_at,
      completedAt: txn.completed_at
    }));

    res.status(200).json({
      success: true,
      data: transformedTransactions,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total: count,
        totalPages: Math.ceil(count / parseInt(limit))
      }
    });
  } catch (error) {
    logger.error('Get transactions error:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch transactions' });
  }
};
```

### 3.2 POST /api/v1/transactions/create.js

**File:** `backend/api/transactions/create.js`

**Replace stub with:**

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const {
      listingId,
      quantity,
      pickupLocation,
      pickupDate,
      pickupTime,
      paymentMethod,
      notes
    } = req.body;

    // Get listing details
    const { data: listing, error: listingError } = await supabaseAdmin
      .from('listings')
      .select('*')
      .eq('id', listingId)
      .single();

    if (listingError || !listing) {
      return res.status(404).json({ success: false, message: 'Listing not found' });
    }

    if (!listing.is_active) {
      return res.status(400).json({ success: false, message: 'Listing is not active' });
    }

    if (quantity > listing.quantity) {
      return res.status(400).json({ success: false, message: 'Insufficient quantity available' });
    }

    const totalAmount = quantity * listing.price_per_unit;

    // Create transaction
    const { data: transaction, error } = await supabaseAdmin
      .from('transactions')
      .insert({
        listing_id: listingId,
        farmer_id: listing.farmer_id,
        buyer_id: authResult.user.userId,
        quantity,
        unit: listing.unit,
        price_per_unit: listing.price_per_unit,
        total_amount: totalAmount,
        pickup_location: pickupLocation,
        pickup_date: pickupDate,
        pickup_time: pickupTime,
        status: 'PENDING',
        payment_method: paymentMethod,
        payment_status: 'PENDING',
        notes
      })
      .select()
      .single();

    if (error) {
      logger.error('Create transaction error:', error);
      return res.status(500).json({ success: false, message: 'Failed to create transaction' });
    }

    // Update listing quantity
    await supabaseAdmin
      .from('listings')
      .update({ 
        quantity: listing.quantity - quantity,
        updated_at: new Date().toISOString()
      })
      .eq('id', listingId);

    res.status(201).json({
      success: true,
      data: {
        id: transaction.id,
        listingId: transaction.listing_id,
        farmerId: transaction.farmer_id,
        buyerId: transaction.buyer_id,
        quantity: parseFloat(transaction.quantity),
        unit: transaction.unit,
        pricePerUnit: parseFloat(transaction.price_per_unit),
        totalAmount: parseFloat(transaction.total_amount),
        pickupLocation: transaction.pickup_location,
        pickupDate: transaction.pickup_date,
        pickupTime: transaction.pickup_time,
        status: transaction.status,
        paymentMethod: transaction.payment_method,
        paymentStatus: transaction.payment_status,
        notes: transaction.notes,
        createdAt: transaction.created_at
      }
    });
  } catch (error) {
    logger.error('Create transaction error:', error);
    res.status(500).json({ success: false, message: 'Failed to create transaction' });
  }
};
```

---

## 4. Sync Endpoint

### 4.1 POST /api/v1/sync/operations.js

**File:** `backend/api/sync/operations.js`

**Replace stub with:**

```javascript
const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const logger = require('../../src/utils/logger');

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const authResult = await authenticateToken(req);
    if (!authResult.success) {
      return res.status(401).json(authResult);
    }

    const { operations } = req.body;

    if (!Array.isArray(operations) || operations.length === 0) {
      return res.status(400).json({ success: false, message: 'Operations array required' });
    }

    const results = [];

    for (const op of operations) {
      try {
        const { opId, type, payload, entityId } = op;
        let result = { opId, success: false };

        switch (type) {
          case 'CREATE_LISTING':
            const createData = JSON.parse(payload);
            const { data: createdListing, error: createError } = await supabaseAdmin
              .from('listings')
              .insert({
                ...createData,
                farmer_id: authResult.user.userId
              })
              .select()
              .single();
            
            if (!createError) {
              result = { opId, success: true, data: createdListing };
            }
            break;

          case 'UPDATE_LISTING':
            const updateData = JSON.parse(payload);
            const { data: updatedListing, error: updateError } = await supabaseAdmin
              .from('listings')
              .update(updateData)
              .eq('id', entityId)
              .eq('farmer_id', authResult.user.userId)
              .select()
              .single();
            
            if (!updateError) {
              result = { opId, success: true, data: updatedListing };
            }
            break;

          case 'DELETE_LISTING':
            const { error: deleteError } = await supabaseAdmin
              .from('listings')
              .update({ is_active: false })
              .eq('id', entityId)
              .eq('farmer_id', authResult.user.userId);
            
            if (!deleteError) {
              result = { opId, success: true };
            }
            break;

          default:
            result = { opId, success: false, error: 'Unknown operation type' };
        }

        results.push(result);
      } catch (opError) {
        logger.error('Sync operation error:', opError);
        results.push({ opId: op.opId, success: false, error: opError.message });
      }
    }

    res.status(200).json({
      success: true,
      results,
      syncedAt: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Sync operations error:', error);
    res.status(500).json({ success: false, message: 'Sync failed' });
  }
};
```

---

## 5. Deployment Instructions

### Deploy All New Endpoints

```bash
cd backend

# Verify all files created
ls -la api/listings/
ls -la api/market-prices/
ls -la api/transactions/
ls -la api/sync/

# Deploy to Vercel
vercel --prod

# Test endpoints
curl https://agrimarket-api.vercel.app/api/v1/listings
curl https://agrimarket-api.vercel.app/api/v1/market-prices
```

### Verify Deployment

```bash
# Check Vercel dashboard
vercel logs --follow

# Test each endpoint
./test-api.js
```

---

## 6. Testing Checklist

- [ ] GET /api/v1/listings returns data
- [ ] GET /api/v1/listings/{id} returns single listing
- [ ] POST /api/v1/listings creates listing
- [ ] PUT /api/v1/listings/{id} updates listing
- [ ] DELETE /api/v1/listings/{id} soft deletes
- [ ] GET /api/v1/market-prices returns prices
- [ ] GET /api/v1/transactions returns user transactions
- [ ] POST /api/v1/transactions creates transaction
- [ ] POST /api/v1/sync/operations syncs offline ops

---

**Status:** Implementation complete. Ready for deployment! 🚀

