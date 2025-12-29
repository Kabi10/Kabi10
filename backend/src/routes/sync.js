const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../database/connection');
const logger = require('../utils/logger');
const { validateUUID } = require('../utils/helpers');

const router = express.Router();

/**
 * POST /api/v1/sync/operations
 * Process batch of local operations from client
 * Matches Android SyncApiService.syncOperations()
 */
router.post('/operations', async (req, res) => {
  try {
    const { operations, lastSyncAt } = req.body;

    if (!Array.isArray(operations)) {
      return res.status(400).json({
        success: false,
        message: 'Operations must be an array',
      });
    }

    const results = {
      appliedOps: [],
      conflicts: [],
      errors: [],
    };

    // Process each operation in a transaction
    for (const op of operations) {
      try {
        await db.transaction(async (client) => {
          const result = await processOperation(client, op, req.user.userId);

          if (result.success) {
            results.appliedOps.push(op.opId);

            // Store the operation in local_ops table for tracking
            await client.query(`
              INSERT INTO local_ops (
                op_id, user_id, type, payload, client_ts, 
                synced, client_id, entity_id
              ) VALUES ($1, $2, $3, $4, $5, true, $6, $7)
              ON CONFLICT (op_id) DO UPDATE SET
                synced = true,
                server_ts = NOW()
            `, [
              op.opId,
              req.user.userId,
              op.type,
              JSON.stringify(op.payload),
              op.clientTs,
              op.clientId,
              result.entityId,
            ]);
          } else if (result.conflict) {
            results.conflicts.push({
              opId: op.opId,
              reason: result.reason,
              serverObject: result.serverObject,
            });
          } else {
            results.errors.push({
              opId: op.opId,
              error: result.error,
            });
          }
        });
      } catch (error) {
        logger.error('Operation processing error:', { opId: op.opId, error });
        results.errors.push({
          opId: op.opId,
          error: error.message,
        });
      }
    }

    // Get updated data since last sync
    const updatedData = await getUpdatedDataSince(lastSyncAt, req.user.userId);

    logger.info('Sync operations processed', {
      userId: req.user.userId,
      totalOps: operations.length,
      applied: results.appliedOps.length,
      conflicts: results.conflicts.length,
      errors: results.errors.length,
    });

    res.json({
      success: true,
      appliedOps: results.appliedOps,
      conflicts: results.conflicts,
      errors: results.errors,
      serverData: updatedData,
      serverTimestamp: new Date().toISOString(),
    });
  } catch (error) {
    logger.error('Sync operations error:', error);
    res.status(500).json({
      success: false,
      message: 'Sync failed',
    });
  }
});

/**
 * GET /api/v1/sync/data
 * Get updated data since last sync timestamp
 */
router.get('/data', async (req, res) => {
  try {
    const { lastSyncAt } = req.query;

    const updatedData = await getUpdatedDataSince(lastSyncAt, req.user.userId);

    res.json({
      success: true,
      data: updatedData,
      serverTimestamp: new Date().toISOString(),
    });
  } catch (error) {
    logger.error('Get sync data error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to get sync data',
    });
  }
});

/**
 * Process individual operation based on type
 */
async function processOperation(client, operation, userId) {
  const {
    type, payload, opId, clientId,
  } = operation;

  try {
    switch (type) {
      case 'CREATE_LISTING':
        return await processCreateListing(client, payload, userId, clientId);

      case 'UPDATE_LISTING':
        return await processUpdateListing(client, payload, userId);

      case 'DELETE_LISTING':
        return await processDeleteListing(client, payload, userId);

      case 'CREATE_TRANSACTION':
        return await processCreateTransaction(client, payload, userId, clientId);

      case 'UPDATE_TRANSACTION':
        return await processUpdateTransaction(client, payload, userId);

      case 'UPDATE_USER':
        return await processUpdateUser(client, payload, userId);

      default:
        return {
          success: false,
          error: `Unknown operation type: ${type}`,
        };
    }
  } catch (error) {
    logger.error('Operation processing error:', { type, opId, error });
    return {
      success: false,
      error: error.message,
    };
  }
}

/**
 * Process CREATE_LISTING operation
 */
async function processCreateListing(client, payload, userId, clientId) {
  // Check if listing already exists (conflict resolution)
  if (clientId) {
    const existing = await client.query(
      'SELECT id FROM listings WHERE client_id = $1',
      [clientId],
    );

    if (existing.rows.length > 0) {
      return {
        success: true,
        entityId: existing.rows[0].id,
      };
    }
  }

  // Validate user is a farmer
  const user = await client.query(
    'SELECT user_type FROM users WHERE id = $1',
    [userId],
  );

  if (user.rows.length === 0 || user.rows[0].user_type !== 'FARMER') {
    return {
      success: false,
      error: 'Only farmers can create listings',
    };
  }

  // Create listing
  const result = await client.query(`
    INSERT INTO listings (
      farmer_id, crop_type, quantity, unit, price_per_unit,
      quality, location, pickup_locations, available_from,
      available_until, description, images, client_id
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
    RETURNING id
  `, [
    userId,
    payload.cropType,
    payload.quantity,
    payload.unit,
    payload.pricePerUnit,
    payload.quality,
    payload.location,
    payload.pickupLocations || [],
    payload.availableFrom,
    payload.availableUntil,
    payload.description,
    payload.images || [],
    clientId,
  ]);

  return {
    success: true,
    entityId: result.rows[0].id,
  };
}

/**
 * Process UPDATE_LISTING operation
 */
async function processUpdateListing(client, payload, userId) {
  const { listingId, ...updateData } = payload;

  // Check if listing exists and belongs to user
  const existing = await client.query(
    'SELECT farmer_id, updated_at FROM listings WHERE id = $1',
    [listingId],
  );

  if (existing.rows.length === 0) {
    return {
      success: false,
      error: 'Listing not found',
    };
  }

  if (existing.rows[0].farmer_id !== userId) {
    return {
      success: false,
      error: 'You can only update your own listings',
    };
  }

  // Check for conflicts (server version newer than client)
  if (payload.lastUpdated && existing.rows[0].updated_at > new Date(payload.lastUpdated)) {
    const serverListing = await client.query(
      'SELECT * FROM listings WHERE id = $1',
      [listingId],
    );

    return {
      success: false,
      conflict: true,
      reason: 'Server version is newer',
      serverObject: JSON.stringify(serverListing.rows[0]),
    };
  }

  // Update listing
  await client.query(`
    UPDATE listings SET
      crop_type = COALESCE($2, crop_type),
      quantity = COALESCE($3, quantity),
      unit = COALESCE($4, unit),
      price_per_unit = COALESCE($5, price_per_unit),
      quality = COALESCE($6, quality),
      location = COALESCE($7, location),
      pickup_locations = COALESCE($8, pickup_locations),
      available_from = COALESCE($9, available_from),
      available_until = COALESCE($10, available_until),
      description = COALESCE($11, description),
      images = COALESCE($12, images),
      updated_at = NOW()
    WHERE id = $1
  `, [
    listingId,
    updateData.cropType,
    updateData.quantity,
    updateData.unit,
    updateData.pricePerUnit,
    updateData.quality,
    updateData.location,
    updateData.pickupLocations,
    updateData.availableFrom,
    updateData.availableUntil,
    updateData.description,
    updateData.images,
  ]);

  return {
    success: true,
    entityId: listingId,
  };
}

/**
 * Process DELETE_LISTING operation
 */
async function processDeleteListing(client, payload, userId) {
  const { listingId } = payload;

  // Check if listing exists and belongs to user
  const existing = await client.query(
    'SELECT farmer_id FROM listings WHERE id = $1',
    [listingId],
  );

  if (existing.rows.length === 0) {
    return {
      success: false,
      error: 'Listing not found',
    };
  }

  if (existing.rows[0].farmer_id !== userId) {
    return {
      success: false,
      error: 'You can only delete your own listings',
    };
  }

  // Soft delete
  await client.query(
    'UPDATE listings SET is_active = false, updated_at = NOW() WHERE id = $1',
    [listingId],
  );

  return {
    success: true,
    entityId: listingId,
  };
}

/**
 * Process CREATE_TRANSACTION operation
 */
async function processCreateTransaction(client, payload, userId, clientId) {
  // Check if transaction already exists
  if (clientId) {
    const existing = await client.query(
      'SELECT id FROM transactions WHERE client_id = $1',
      [clientId],
    );

    if (existing.rows.length > 0) {
      return {
        success: true,
        entityId: existing.rows[0].id,
      };
    }
  }

  // Validate listing exists and is available
  const listing = await client.query(
    'SELECT farmer_id, quantity, is_active FROM listings WHERE id = $1',
    [payload.listingId],
  );

  if (listing.rows.length === 0 || !listing.rows[0].is_active) {
    return {
      success: false,
      error: 'Listing not found or not available',
    };
  }

  // Check quantity availability
  if (payload.quantity > listing.rows[0].quantity) {
    return {
      success: false,
      error: 'Requested quantity exceeds available quantity',
    };
  }

  // Create transaction
  const result = await client.query(`
    INSERT INTO transactions (
      listing_id, farmer_id, buyer_id, quantity, total_amount,
      pickup_location, pickup_date, buyer_contact, notes, client_id
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
    RETURNING id
  `, [
    payload.listingId,
    listing.rows[0].farmer_id,
    userId,
    payload.quantity,
    payload.totalAmount,
    payload.pickupLocation,
    payload.pickupDate,
    payload.buyerContact,
    payload.notes,
    clientId,
  ]);

  return {
    success: true,
    entityId: result.rows[0].id,
  };
}

/**
 * Process UPDATE_TRANSACTION operation
 */
async function processUpdateTransaction(client, payload, userId) {
  const { transactionId, status, notes } = payload;

  // Check if transaction exists and user has permission
  const existing = await client.query(
    'SELECT farmer_id, buyer_id, status FROM transactions WHERE id = $1',
    [transactionId],
  );

  if (existing.rows.length === 0) {
    return {
      success: false,
      error: 'Transaction not found',
    };
  }

  const transaction = existing.rows[0];
  const isFarmer = transaction.farmer_id === userId;
  const isBuyer = transaction.buyer_id === userId;

  if (!isFarmer && !isBuyer) {
    return {
      success: false,
      error: 'You can only update your own transactions',
    };
  }

  // Validate status transition
  const validTransitions = {
    PENDING: ['CONFIRMED', 'CANCELLED'],
    CONFIRMED: ['IN_PROGRESS', 'CANCELLED'],
    IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
    COMPLETED: [],
    CANCELLED: [],
  };

  if (status && !validTransitions[transaction.status].includes(status)) {
    return {
      success: false,
      error: `Invalid status transition from ${transaction.status} to ${status}`,
    };
  }

  // Update transaction
  const updateFields = [];
  const updateValues = [transactionId];
  let paramIndex = 2;

  if (status) {
    updateFields.push(`status = $${paramIndex}`);
    updateValues.push(status);
    paramIndex++;

    if (status === 'COMPLETED') {
      updateFields.push('completed_at = NOW()');
    } else if (status === 'CANCELLED') {
      updateFields.push('cancelled_at = NOW()');
      if (notes) {
        updateFields.push(`cancellation_reason = $${paramIndex}`);
        updateValues.push(notes);
        paramIndex++;
      }
    }
  }

  if (notes && status !== 'CANCELLED') {
    updateFields.push(`notes = $${paramIndex}`);
    updateValues.push(notes);
    paramIndex++;
  }

  updateFields.push('updated_at = NOW()');

  await client.query(`
    UPDATE transactions SET ${updateFields.join(', ')}
    WHERE id = $1
  `, updateValues);

  return {
    success: true,
    entityId: transactionId,
  };
}

/**
 * Process UPDATE_USER operation
 */
async function processUpdateUser(client, payload, userId) {
  const { name, location, userType } = payload;

  const updateFields = [];
  const updateValues = [userId];
  let paramIndex = 2;

  if (name) {
    updateFields.push(`name = $${paramIndex}`);
    updateValues.push(name);
    paramIndex++;
  }

  if (location) {
    updateFields.push(`location = $${paramIndex}`);
    updateValues.push(location);
    paramIndex++;
  }

  if (userType) {
    updateFields.push(`user_type = $${paramIndex}`);
    updateValues.push(userType);
    paramIndex++;
  }

  updateFields.push('updated_at = NOW()');

  await client.query(`
    UPDATE users SET ${updateFields.join(', ')}
    WHERE id = $1
  `, updateValues);

  return {
    success: true,
    entityId: userId,
  };
}

/**
 * Get updated data since last sync timestamp
 */
async function getUpdatedDataSince(lastSyncAt, userId) {
  const data = {
    users: [],
    listings: [],
    transactions: [],
  };

  const syncTimestamp = lastSyncAt ? new Date(lastSyncAt) : new Date(0);

  // Get updated users (only current user)
  const users = await db.query(
    'SELECT * FROM users WHERE id = $1 AND updated_at > $2',
    [userId, syncTimestamp],
  );
  data.users = users.rows;

  // Get updated listings (user's own + all active listings)
  const listings = await db.query(`
    SELECT l.*, u.name as farmer_name 
    FROM listings l
    JOIN users u ON l.farmer_id = u.id
    WHERE (l.farmer_id = $1 OR l.is_active = true) 
    AND l.updated_at > $2
    ORDER BY l.updated_at DESC
  `, [userId, syncTimestamp]);
  data.listings = listings.rows;

  // Get updated transactions (user's transactions only)
  const transactions = await db.query(
    'SELECT * FROM transactions WHERE (farmer_id = $1 OR buyer_id = $1) AND updated_at > $2',
    [userId, syncTimestamp],
  );
  data.transactions = transactions.rows;

  return data;
}

module.exports = router;
