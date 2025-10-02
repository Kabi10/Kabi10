#!/usr/bin/env node
/**
 * Database Migration Executor
 * Connects directly to Supabase PostgreSQL and executes SQL migrations
 */

const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

// Supabase connection details
const PROJECT_REF = 'lxsbdluguyaaxzaeovwx';
const DB_PASSWORD = process.env.SUPABASE_DB_PASSWORD || process.env.DB_PASSWORD;

if (!DB_PASSWORD) {
  console.error('\n❌ Error: Database password required\n');
  console.error('Get your database password from:');
  console.error(`https://supabase.com/dashboard/project/${PROJECT_REF}/settings/database\n`);
  console.error('Then set it with:');
  console.error('  PowerShell: $env:SUPABASE_DB_PASSWORD="your-password"');
  console.error('  Bash: export SUPABASE_DB_PASSWORD="your-password"\n');
  process.exit(1);
}

// PostgreSQL connection config
const config = {
  host: `${PROJECT_REF}.supabase.co`,
  port: 5432,
  database: 'postgres',
  user: 'postgres',
  password: DB_PASSWORD,
  ssl: {
    rejectUnauthorized: false
  }
};

// Migration files in order
const migrations = [
  {
    name: '01_fix_rls_policies',
    file: 'fix-rls-policies.sql',
    description: 'Fix RLS policies to allow public read access'
  },
  {
    name: '02_create_market_prices',
    file: 'create-market-prices-table.sql',
    description: 'Create market_prices table with sample data'
  },
  {
    name: '03_insert_sample_data',
    file: 'insert-sample-data-fixed.sql',
    description: 'Insert sample users, listings, and transactions'
  }
];

async function executeMigration(client, migration) {
  console.log(`\n📄 ${migration.name}: ${migration.description}`);
  
  const filePath = path.join(__dirname, migration.file);
  
  if (!fs.existsSync(filePath)) {
    console.error(`   ❌ File not found: ${migration.file}`);
    return false;
  }
  
  const sql = fs.readFileSync(filePath, 'utf8');
  const lines = sql.split('\n').length;
  console.log(`   📝 Loaded ${lines} lines from ${migration.file}`);
  
  try {
    console.log(`   🔄 Executing SQL...`);
    await client.query(sql);
    console.log(`   ✅ Migration completed successfully`);
    return true;
  } catch (error) {
    console.error(`   ❌ Migration failed: ${error.message}`);
    if (error.detail) {
      console.error(`   Details: ${error.detail}`);
    }
    return false;
  }
}

async function verifyData(client) {
  console.log('\n🔍 Verifying data...');
  
  try {
    const queries = [
      { name: 'Users', query: 'SELECT COUNT(*) as count FROM users' },
      { name: 'Listings', query: 'SELECT COUNT(*) as count FROM listings' },
      { name: 'Transactions', query: 'SELECT COUNT(*) as count FROM transactions' },
      { name: 'Market Prices', query: 'SELECT COUNT(*) as count FROM market_prices' }
    ];
    
    for (const q of queries) {
      try {
        const result = await client.query(q.query);
        const count = result.rows[0].count;
        console.log(`   ✅ ${q.name}: ${count} rows`);
      } catch (error) {
        console.log(`   ⚠️  ${q.name}: Table may not exist yet`);
      }
    }
  } catch (error) {
    console.error(`   ❌ Verification failed: ${error.message}`);
  }
}

async function main() {
  console.log('🚀 Agrimarket Database Migration Executor');
  console.log('==========================================\n');
  console.log(`📍 Host: ${config.host}`);
  console.log(`🔌 Port: ${config.port}`);
  console.log(`💾 Database: ${config.database}`);
  console.log(`👤 User: ${config.user}`);
  console.log(`🔑 Password: ${config.password.substring(0, 4)}${'*'.repeat(config.password.length - 4)}`);
  
  const client = new Client(config);
  
  try {
    console.log('\n🔌 Connecting to database...');
    await client.connect();
    console.log('✅ Connected successfully\n');
    
    // Execute migrations
    let successCount = 0;
    for (const migration of migrations) {
      const success = await executeMigration(client, migration);
      if (success) successCount++;
    }
    
    console.log('\n' + '='.repeat(60));
    console.log(`✅ Completed: ${successCount}/${migrations.length} migrations successful`);
    console.log('='.repeat(60));
    
    // Verify data
    await verifyData(client);
    
    console.log('\n🎉 Migration execution complete!');
    console.log('\nNext steps:');
    console.log('  1. Test backend: .\\test-backend-complete.ps1');
    console.log('  2. Update Android app API URL');
    console.log('  3. Rebuild and test Android app\n');
    
  } catch (error) {
    console.error('\n❌ Fatal error:', error.message);
    console.error('Error code:', error.code);
    console.error('Full error:', JSON.stringify(error, null, 2));

    if (error.code === 'ENOTFOUND') {
      console.error('\n⚠️  Could not resolve hostname. Check your internet connection.');
    } else if (error.code === '28P01') {
      console.error('\n⚠️  Authentication failed. Check your database password.');
      console.error(`Get it from: https://supabase.com/dashboard/project/${PROJECT_REF}/settings/database`);
    } else if (error.code === 'ECONNREFUSED') {
      console.error('\n⚠️  Connection refused. Check host and port.');
    } else if (error.code === 'ETIMEDOUT') {
      console.error('\n⚠️  Connection timed out. Check firewall or network settings.');
    }
    process.exit(1);
  } finally {
    try {
      await client.end();
      console.log('🔌 Database connection closed\n');
    } catch (e) {
      // Ignore errors when closing
    }
  }
}

main().catch(console.error);

