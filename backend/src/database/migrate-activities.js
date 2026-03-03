/**
 * Database migration script for Activities table
 * Applies schema-activities.sql to create the activities table and indexes
 *
 * Usage:
 *   node backend/src/database/migrate-activities.js
 *
 * Created: 2026-02-16
 */

const db = require("./connection");
const fs = require("fs");
const path = require("path");
const logger = require("../utils/logger");

async function migrateActivities() {
  const client = await db.connect();

  try {
    logger.info("Starting activities table migration...");

    // Begin transaction
    await client.query("BEGIN");

    // Read schema file
    const schemaPath = path.join(__dirname, "schema-activities.sql");
    const schema = fs.readFileSync(schemaPath, "utf8");

    logger.info("Executing activities schema SQL...");
    await client.query(schema);

    // Verify table was created
    const tableCheck = await client.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'activities'
      ) as table_exists
    `);

    if (!tableCheck.rows[0].table_exists) {
      throw new Error("Activities table was not created successfully");
    }

    // Verify indexes were created
    const indexCheck = await client.query(`
      SELECT COUNT(*) as index_count
      FROM pg_indexes
      WHERE tablename = 'activities'
    `);

    const indexCount = parseInt(indexCheck.rows[0].index_count);
    logger.info(`Created ${indexCount} indexes on activities table`);

    // Commit transaction
    await client.query("COMMIT");

    logger.info("✅ Activities table migration completed successfully");
    logger.info(`   - Table: activities`);
    logger.info(`   - Indexes: ${indexCount}`);
    logger.info(`   - Triggers: update_activities_updated_at`);
    logger.info(`   - Functions: cleanup_expired_activities()`);

    return {
      success: true,
      indexCount,
      message: "Activities table created successfully",
    };
  } catch (error) {
    // Rollback on error
    await client.query("ROLLBACK");
    logger.error("❌ Activities migration failed:", error.message);
    logger.error(error.stack);

    throw error;
  } finally {
    // Release client back to pool
    client.release();
  }
}

// Rollback function (optional - for development)
async function rollbackActivities() {
  const client = await db.connect();

  try {
    logger.info("Rolling back activities table...");

    await client.query("BEGIN");

    // Drop table (CASCADE will drop indexes and triggers automatically)
    await client.query("DROP TABLE IF EXISTS activities CASCADE");

    // Drop function
    await client.query("DROP FUNCTION IF EXISTS cleanup_expired_activities()");

    await client.query("COMMIT");

    logger.info("✅ Activities table rollback completed");
  } catch (error) {
    await client.query("ROLLBACK");
    logger.error("❌ Activities rollback failed:", error.message);
    throw error;
  } finally {
    client.release();
  }
}

// CLI execution
if (require.main === module) {
  const args = process.argv.slice(2);
  const shouldRollback = args.includes("--rollback");

  const operation = shouldRollback ? rollbackActivities() : migrateActivities();

  operation
    .then(() => {
      logger.info("Migration script completed");
      process.exit(0);
    })
    .catch((error) => {
      logger.error("Migration script failed:", error.message);
      process.exit(1);
    });
}

module.exports = { migrateActivities, rollbackActivities };
