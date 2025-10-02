const { supabaseAdmin } = require('../../src/config/supabase');
const { authenticateToken } = require('../../src/middleware/auth');
const logger = require('../../src/utils/logger');

/**
 * Vercel Serverless Function: Sync Operations
 * POST /api/sync/operations
 */
module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
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

    const { operations = [], lastSyncAt } = req.body;

    if (!Array.isArray(operations)) {
      return res.status(400).json({ success: false, message: 'Operations must be an array' });
    }

    const results = { appliedOps: [], conflicts: [], errors: [] };

    for (const op of operations) {
      try {
        const { opId, type, payload, clientId } = op;
        let result = { opId, success: false };

        switch (type) {
          case 'CREATE_LISTING':
            const { data: listing, error: createError } = await supabaseAdmin
              .from('listings')
              .insert({
                farmer_id: authResult.user.userId,
                crop_type: payload.cropType,
                quantity: payload.quantity,
                unit: payload.unit,
                price_per_unit: payload.pricePerUnit,
                quality: payload.quality,
                location: payload.location,
                pickup_locations: payload.pickupLocations || [],
                available_from: payload.availableFrom,
                available_until: payload.availableUntil,
                description: payload.description,
                images: payload.images || [],
                client_id: clientId
              })
              .select()
              .single();

            if (!createError) {
              result = { opId, success: true, entityId: listing.id };
            }
            break;

          case 'UPDATE_LISTING':
            const { data: updated, error: updateError } = await supabaseAdmin
              .from('listings')
              .update({
                crop_type: payload.cropType,
                quantity: payload.quantity,
                unit: payload.unit,
                price_per_unit: payload.pricePerUnit,
                quality: payload.quality,
                location: payload.location,
                pickup_locations: payload.pickupLocations,
                available_from: payload.availableFrom,
                available_until: payload.availableUntil,
                description: payload.description,
                images: payload.images
              })
              .eq('id', payload.listingId)
              .eq('farmer_id', authResult.user.userId)
              .select()
              .single();

            if (!updateError) {
              result = { opId, success: true };
            }
            break;

          case 'DELETE_LISTING':
            const { error: deleteError } = await supabaseAdmin
              .from('listings')
              .update({ is_active: false })
              .eq('id', payload.listingId)
              .eq('farmer_id', authResult.user.userId);

            if (!deleteError) {
              result = { opId, success: true };
            }
            break;

          case 'CREATE_TRANSACTION':
            const { data: transaction, error: transError } = await supabaseAdmin
              .from('transactions')
              .insert({
                listing_id: payload.listingId,
                buyer_id: authResult.user.userId,
                quantity: payload.quantity,
                total_amount: payload.totalAmount,
                pickup_location: payload.pickupLocation,
                pickup_date: payload.pickupDate,
                payment_method: payload.paymentMethod,
                notes: payload.notes,
                status: 'PENDING',
                client_id: clientId
              })
              .select()
              .single();

            if (!transError) {
              result = { opId, success: true, entityId: transaction.id };
            }
            break;

          default:
            result = { opId, success: false, error: 'Unknown operation type' };
        }

        if (result.success) {
          results.appliedOps.push(opId);
        } else {
          results.errors.push(result);
        }
      } catch (opError) {
        logger.error('Sync operation error:', opError);
        results.errors.push({ opId: op.opId, success: false, error: opError.message });
      }
    }

    const syncTimestamp = lastSyncAt ? new Date(lastSyncAt) : new Date(0);
    const { data: updatedListings } = await supabaseAdmin
      .from('listings')
      .select('*')
      .eq('is_active', true)
      .gt('updated_at', syncTimestamp.toISOString());

    logger.info('Sync operations processed', {
      userId: authResult.user.userId,
      totalOps: operations.length,
      applied: results.appliedOps.length,
      errors: results.errors.length
    });

    res.json({
      success: true,
      appliedOps: results.appliedOps,
      conflicts: results.conflicts,
      errors: results.errors,
      serverData: { listings: updatedListings || [] },
      serverTimestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Sync operations error:', error);
    res.status(500).json({ success: false, message: 'Sync failed', error: error.message });
  }
};