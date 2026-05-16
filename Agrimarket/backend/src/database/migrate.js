const fs = require("fs");
const path = require("path");
const db = require("./connection");
const logger = require("../utils/logger");

/**
 * Database migration script
 * Runs the schema.sql file to set up the database
 */
async function migrate() {
  try {
    logger.info("Starting database migration...");

    // Read the schema file
    const schemaPath = path.join(__dirname, "schema.sql");
    const schema = fs.readFileSync(schemaPath, "utf8");

    // Execute the schema
    await db.query(schema);

    logger.info("Database migration completed successfully");

    // Verify tables were created
    const tables = await db.query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      ORDER BY table_name
    `);

    logger.info(
      "Created tables:",
      tables.rows.map((row) => row.table_name),
    );
  } catch (error) {
    logger.error("Database migration failed:", error);
    throw error;
  } finally {
    await db.end();
  }
}

// Run migration if called directly
if (require.main === module) {
  migrate()
    .then(() => {
      console.log("Migration completed successfully");
      process.exit(0);
    })
    .catch((error) => {
      console.error("Migration failed:", error);
      process.exit(1);
    });
}

module.exports = migrate;
