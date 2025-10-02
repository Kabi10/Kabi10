#!/usr/bin/env node
/**
 * Force Execute Migrations via Direct Connection with Aggressive Timeout Settings
 */

const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

const PROJECT_REF = 'lxsbdluguyaaxzaeovwx';
const DB_PASSWORD = process.env.SUPABASE_DB_PASSWORD || 'Kabithara_090624!';

// PostgreSQL connection - standard Supabase format
const config = {
  host: `${PROJECT_REF}.supabase.co`,
  port: 5432,
  database: 'postgres',
  user: 'postgres',
  password: DB_PASSWORD,
  ssl: {
    rejectUnauthorized: false
  },
  connectionTimeoutMillis: 15000
};

const migrations = [
  { name: '01_fix_rls', file: 'fix-rls-policies.sql', desc: 'Fix RLS policies' },
  { name: '02_market_prices', file: 'create-market-prices-table.sql', desc: 'Create market prices' },
  { name: '03_sample_data', file: 'insert-sample-data-fixed.sql', desc: 'Insert sample data' }
];

async function executeWithRetry(client, sql, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      await client.query(sql);
      return true;
    } catch (error) {
      if (i === retries - 1) throw error;
      console.log(`   Retry ${i + 1}/${retries}...`);
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }
}

async function main() {
  console.log('🚀 Force Execute Migrations');
  console.log('===========================\n');
  console.log(`Host: ${PROJECT_REF}.supabase.co:5432`);
  console.log(`User: postgres`);
  console.log('');

  const client = new Client(config);
  
  try {
    console.log('🔌 Connecting (timeout: 10s)...');
    await client.connect();
    console.log('✅ Connected\n');
    
    for (const mig of migrations) {
      console.log(`📄 ${mig.desc}`);
      const sql = fs.readFileSync(path.join(__dirname, mig.file), 'utf8');
      console.log(`   Executing ${sql.split('\n').length} lines...`);
      
      await executeWithRetry(client, sql);
      console.log(`   ✅ Success\n`);
    }
    
    console.log('🎉 All migrations completed!\n');
    
    // Verify
    const counts = await client.query(`
      SELECT 
        (SELECT COUNT(*) FROM users) as users,
        (SELECT COUNT(*) FROM listings) as listings,
        (SELECT COUNT(*) FROM transactions) as transactions,
        (SELECT COUNT(*) FROM market_prices) as market_prices
    `);
    
    console.log('📊 Data verification:');
    console.log(`   Users: ${counts.rows[0].users}`);
    console.log(`   Listings: ${counts.rows[0].listings}`);
    console.log(`   Transactions: ${counts.rows[0].transactions}`);
    console.log(`   Market Prices: ${counts.rows[0].market_prices}\n`);
    
  } catch (error) {
    console.error('\n❌ Error:', error.message);
    console.error('Code:', error.code);
    
    if (error.code === 'ETIMEDOUT' || error.code === 'ECONNREFUSED') {
      console.error('\n⚠️  Connection blocked. Trying alternative method...\n');
      
      // Try using psql if available
      const { execSync } = require('child_process');
      try {
        console.log('Attempting psql connection...');
        process.env.PGPASSWORD = DB_PASSWORD;
        
        for (const mig of migrations) {
          console.log(`\n📄 ${mig.desc}`);
          const cmd = `psql -h ${PROJECT_REF}.supabase.co -p 5432 -U postgres -d postgres -f ${mig.file}`;
          execSync(cmd, { stdio: 'inherit', cwd: __dirname });
          console.log(`   ✅ Success`);
        }
        
        console.log('\n🎉 Migrations completed via psql!');
      } catch (psqlError) {
        console.error('\n❌ psql also failed');
        console.error('\nFINAL OPTION: Manual execution required');
        console.error('URL: https://supabase.com/dashboard/project/lxsbdluguyaaxzaeovwx/sql/new');
        process.exit(1);
      }
    }
    process.exit(1);
  } finally {
    try { await client.end(); } catch (e) {}
  }
}

main();

