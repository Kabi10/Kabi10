const db = require('./connection');
const logger = require('../utils/logger');

/**
 * Database seeding script
 * Populates the database with sample data for development/testing
 */
async function seed() {
  try {
    logger.info('Starting database seeding...');

    // Clear existing data (in development only)
    if (process.env.NODE_ENV === 'development') {
      await db.query('TRUNCATE TABLE local_ops, transactions, listings, users, otp_verifications, sync_metadata RESTART IDENTITY CASCADE');
      logger.info('Cleared existing data');
    }

    // Create sample users
    const users = [
      {
        phoneNumber: '+94771234567',
        name: 'Ravi Farmer',
        userType: 'FARMER',
        location: 'Jaffna North',
        verified: true,
      },
      {
        phoneNumber: '+94771234568',
        name: 'Priya Buyer',
        userType: 'BUYER',
        location: 'Jaffna Central',
        verified: true,
      },
      {
        phoneNumber: '+94771234569',
        name: 'Kumar Farmer',
        userType: 'FARMER',
        location: 'Jaffna South',
        verified: true,
      },
      {
        phoneNumber: '+94771234570',
        name: 'Sita Buyer',
        userType: 'BUYER',
        location: 'Jaffna East',
        verified: true,
      },
    ];

    const createdUsers = [];
    for (const user of users) {
      const result = await db.query(`
        INSERT INTO users (phone_number, name, user_type, location, verified)
        VALUES ($1, $2, $3, $4, $5)
        RETURNING *
      `, [user.phoneNumber, user.name, user.userType, user.location, user.verified]);

      createdUsers.push(result.rows[0]);
      logger.info(`Created user: ${user.name} (${user.userType})`);
    }

    // Get farmers for creating listings
    const farmers = createdUsers.filter((user) => user.user_type === 'FARMER');
    const buyers = createdUsers.filter((user) => user.user_type === 'BUYER');

    // Create sample listings
    const listings = [
      {
        farmerId: farmers[0].id,
        cropType: 'rice',
        quantity: 100,
        unit: 'kg',
        pricePerUnit: 150.00,
        quality: 'A',
        location: 'Jaffna North',
        pickupLocations: ['Farm Gate', 'Jaffna Market'],
        availableFrom: new Date(),
        availableUntil: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
        description: 'High quality red rice, organically grown',
      },
      {
        farmerId: farmers[0].id,
        cropType: 'coconut',
        quantity: 50,
        unit: 'piece',
        pricePerUnit: 45.00,
        quality: 'A',
        location: 'Jaffna North',
        pickupLocations: ['Farm Gate'],
        availableFrom: new Date(),
        availableUntil: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000), // 15 days
        description: 'Fresh coconuts, perfect for cooking',
      },
      {
        farmerId: farmers[1].id,
        cropType: 'tomato',
        quantity: 25,
        unit: 'kg',
        pricePerUnit: 200.00,
        quality: 'B',
        location: 'Jaffna South',
        pickupLocations: ['Farm Gate', 'Jaffna Market', 'Nallur Market'],
        availableFrom: new Date(),
        availableUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
        description: 'Fresh tomatoes, good for curry',
      },
      {
        farmerId: farmers[1].id,
        cropType: 'onion',
        quantity: 40,
        unit: 'kg',
        pricePerUnit: 180.00,
        quality: 'A',
        location: 'Jaffna South',
        pickupLocations: ['Farm Gate', 'Jaffna Market'],
        availableFrom: new Date(),
        availableUntil: new Date(Date.now() + 20 * 24 * 60 * 60 * 1000), // 20 days
        description: 'Red onions, excellent quality',
      },
      {
        farmerId: farmers[0].id,
        cropType: 'curry_leaves',
        quantity: 5,
        unit: 'bunch',
        pricePerUnit: 25.00,
        quality: 'A',
        location: 'Jaffna North',
        pickupLocations: ['Farm Gate'],
        availableFrom: new Date(),
        availableUntil: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000), // 3 days
        description: 'Fresh curry leaves, aromatic',
      },
    ];

    const createdListings = [];
    for (const listing of listings) {
      const result = await db.query(`
        INSERT INTO listings (
          farmer_id, crop_type, quantity, unit, price_per_unit,
          quality, location, pickup_locations, available_from,
          available_until, description
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
        RETURNING *
      `, [
        listing.farmerId, listing.cropType, listing.quantity, listing.unit,
        listing.pricePerUnit, listing.quality, listing.location,
        listing.pickupLocations, listing.availableFrom, listing.availableUntil,
        listing.description,
      ]);

      createdListings.push(result.rows[0]);
      logger.info(`Created listing: ${listing.cropType} by farmer ${listing.farmerId}`);
    }

    // Create sample transactions
    const transactions = [
      {
        listingId: createdListings[0].id,
        farmerId: createdListings[0].farmer_id,
        buyerId: buyers[0].id,
        quantity: 10,
        totalAmount: 1500.00,
        pickupLocation: 'Jaffna Market',
        pickupDate: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000), // 2 days from now
        status: 'PENDING',
        buyerContact: buyers[0].phone_number,
        notes: 'Please call before pickup',
      },
      {
        listingId: createdListings[2].id,
        farmerId: createdListings[2].farmer_id,
        buyerId: buyers[1].id,
        quantity: 5,
        totalAmount: 1000.00,
        pickupLocation: 'Farm Gate',
        pickupDate: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000), // 1 day from now
        status: 'CONFIRMED',
        buyerContact: buyers[1].phone_number,
        notes: 'Early morning pickup preferred',
      },
    ];

    for (const transaction of transactions) {
      const result = await db.query(`
        INSERT INTO transactions (
          listing_id, farmer_id, buyer_id, quantity, total_amount,
          pickup_location, pickup_date, status, buyer_contact, notes
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
        RETURNING *
      `, [
        transaction.listingId, transaction.farmerId, transaction.buyerId,
        transaction.quantity, transaction.totalAmount, transaction.pickupLocation,
        transaction.pickupDate, transaction.status, transaction.buyerContact,
        transaction.notes,
      ]);

      logger.info(`Created transaction: ${result.rows[0].id} (${transaction.status})`);
    }

    // Create some sample OTP records (for testing)
    /* if (process.env.NODE_ENV === 'development') {
      const testOTP = await db.query(`
        INSERT INTO otp_verifications (phone_number, otp_code, expires_at)
        VALUES ($1, $2, $3)
        RETURNING *
      `, ['+94771111111', '123456', new Date(Date.now() + 5 * 60 * 1000)]);

      logger.info('Created test OTP: +94771111111 -> 123456');
    } */

    logger.info('Database seeding completed successfully');

    // Print summary
    const summary = await db.query(`
      SELECT 
        (SELECT COUNT(*) FROM users) as users,
        (SELECT COUNT(*) FROM listings) as listings,
        (SELECT COUNT(*) FROM transactions) as transactions
    `);

    logger.info('Seeding summary:', summary.rows[0]);
  } catch (error) {
    logger.error('Database seeding failed:', error);
    throw error;
  } finally {
    await db.end();
  }
}

// Run seeding if called directly
if (require.main === module) {
  seed()
    .then(() => {
      console.log('Seeding completed successfully');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Seeding failed:', error);
      process.exit(1);
    });
}

module.exports = seed;
