#!/usr/bin/env node
/**
 * Execute SQL via Supabase Management API
 * Uses HTTPS REST API instead of direct PostgreSQL connection
 */

const https = require('https');
const fs = require('fs');
const path = require('path');

const PROJECT_REF = 'lxsbdluguyaaxzaeovwx';
const ACCESS_TOKEN = process.env.SUPABASE_ACCESS_TOKEN;
const SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!ACCESS_TOKEN && !SERVICE_ROLE_KEY) {
  console.error('\n❌ Error: Authentication required\n');
  console.error('Option 1: Use Access Token (Recommended)');
  console.error('  1. Go to: https://supabase.com/dashboard/account/tokens');
  console.error('  2. Generate a new token');
  console.error('  3. Set: $env:SUPABASE_ACCESS_TOKEN="your-token"\n');
  console.error('Option 2: Use Service Role Key');
  console.error('  1. Go to: https://supabase.com/dashboard/project/lxsbdluguyaaxzaeovwx/settings/api');
  console.error('  2. Copy service_role key');
  console.error('  3. Set: $env:SUPABASE_SERVICE_ROLE_KEY="your-key"\n');
  process.exit(1);
}

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

function executeSQL(sql) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({ query: sql });
    
    const options = {
      hostname: 'api.supabase.com',
      port: 443,
      path: `/v1/projects/${PROJECT_REF}/database/query`,
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${ACCESS_TOKEN}`,
        'Content-Type': 'application/json',
        'Content-Length': data.length
      }
    };

    const req = https.request(options, (res) => {
      let body = '';
      
      res.on('data', (chunk) => {
        body += chunk;
      });
      
      res.on('end', () => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          try {
            const result = JSON.parse(body);
            resolve(result);
          } catch (e) {
            resolve({ success: true, body });
          }
        } else {
          reject(new Error(`HTTP ${res.statusCode}: ${body}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    req.write(data);
    req.end();
  });
}

async function executeMigration(migration) {
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
    console.log(`   🔄 Executing SQL via Management API...`);
    const result = await executeSQL(sql);
    console.log(`   ✅ Migration completed successfully`);
    return true;
  } catch (error) {
    console.error(`   ❌ Migration failed: ${error.message}`);
    return false;
  }
}

async function main() {
  console.log('🚀 Agrimarket Migration Executor (API Method)');
  console.log('==============================================\n');
  console.log(`📍 Project: ${PROJECT_REF}`);
  console.log(`🔑 Auth: ${ACCESS_TOKEN ? 'Access Token' : 'Service Role Key'}`);
  console.log('');
  
  let successCount = 0;
  for (const migration of migrations) {
    const success = await executeMigration(migration);
    if (success) successCount++;
  }
  
  console.log('\n' + '='.repeat(60));
  console.log(`✅ Completed: ${successCount}/${migrations.length} migrations successful`);
  console.log('='.repeat(60));
  
  if (successCount === migrations.length) {
    console.log('\n🎉 All migrations executed successfully!');
    console.log('\nNext steps:');
    console.log('  1. Test backend: ..\\test-backend-complete.ps1');
    console.log('  2. Update Android app API URL');
    console.log('  3. Rebuild and test Android app\n');
  } else {
    console.log('\n⚠️  Some migrations failed. Check errors above.');
    console.log('\nAlternative: Use web-based method');
    console.log('  Run: ..\\deploy-database.ps1\n');
  }
}

main().catch(error => {
  console.error('\n❌ Fatal error:', error.message);
  process.exit(1);
});

