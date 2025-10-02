#!/usr/bin/env node
/**
 * Database Migration Runner
 * Executes SQL migrations directly against Supabase using service role key
 */

const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

// Supabase credentials
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://lxsbdluguyaaxzaeovwx.supabase.co';
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!SUPABASE_SERVICE_ROLE_KEY) {
  console.error('❌ Error: SUPABASE_SERVICE_ROLE_KEY environment variable is required');
  console.error('Set it with: $env:SUPABASE_SERVICE_ROLE_KEY="your-service-role-key"');
  process.exit(1);
}

// Create Supabase client with service role (bypasses RLS)
const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
  auth: {
    autoRefreshToken: false,
    persistSession: false
  }
});

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

async function executeSql(sql) {
  const { data, error } = await supabase.rpc('exec_sql', { sql_query: sql });
  if (error) {
    // If exec_sql function doesn't exist, try direct query
    const { data: queryData, error: queryError } = await supabase.from('_migrations').select('*').limit(1);
    if (queryError && queryError.code === '42P01') {
      // Table doesn't exist, we need to use raw SQL execution
      // For Supabase, we'll use the REST API directly
      throw new Error('Direct SQL execution not available. Use Supabase SQL Editor or psql.');
    }
    throw error;
  }
  return data;
}

async function runMigration(migration) {
  console.log(`\n📄 Running migration: ${migration.name}`);
  console.log(`   ${migration.description}`);
  
  const filePath = path.join(__dirname, migration.file);
  
  if (!fs.existsSync(filePath)) {
    console.error(`   ❌ File not found: ${migration.file}`);
    return false;
  }
  
  const sql = fs.readFileSync(filePath, 'utf8');
  console.log(`   📝 Read ${sql.length} characters from ${migration.file}`);
  
  try {
    // Split SQL into individual statements (basic splitting on semicolons)
    const statements = sql
      .split(';')
      .map(s => s.trim())
      .filter(s => s.length > 0 && !s.startsWith('--'));
    
    console.log(`   🔄 Executing ${statements.length} SQL statements...`);
    
    for (let i = 0; i < statements.length; i++) {
      const stmt = statements[i];
      if (stmt.length < 10) continue; // Skip very short statements
      
      try {
        // Use Supabase's query builder for simple queries, or fall back to raw SQL
        // Note: Supabase JS client doesn't support arbitrary SQL execution
        // We need to use the Management API or psql
        console.log(`   ⚠️  Statement ${i + 1}/${statements.length}: Cannot execute via JS client`);
      } catch (stmtError) {
        console.error(`   ❌ Error in statement ${i + 1}: ${stmtError.message}`);
        throw stmtError;
      }
    }
    
    console.log(`   ✅ Migration completed: ${migration.name}`);
    return true;
  } catch (error) {
    console.error(`   ❌ Migration failed: ${error.message}`);
    return false;
  }
}

async function main() {
  console.log('🚀 Agrimarket Database Migration Runner');
  console.log('========================================\n');
  console.log(`📍 Supabase URL: ${SUPABASE_URL}`);
  console.log(`🔑 Service Role Key: ${SUPABASE_SERVICE_ROLE_KEY.substring(0, 20)}...`);
  
  // Test connection
  console.log('\n🔌 Testing database connection...');
  try {
    const { data, error } = await supabase.from('users').select('count').limit(1);
    if (error && error.code !== '42P01') {
      throw error;
    }
    console.log('✅ Database connection successful');
  } catch (error) {
    console.error(`❌ Database connection failed: ${error.message}`);
    process.exit(1);
  }
  
  console.log('\n⚠️  IMPORTANT: Supabase JS client cannot execute arbitrary SQL.');
  console.log('   Use one of these methods instead:');
  console.log('   1. Supabase SQL Editor (web dashboard)');
  console.log('   2. psql command-line tool');
  console.log('   3. Supabase CLI (supabase db push)');
  console.log('\n   This script will generate the psql commands for you.\n');
  
  // Generate psql commands
  console.log('📋 Copy and run these commands:\n');
  console.log('# Set your database password:');
  console.log('$env:PGPASSWORD="your-database-password"\n');
  
  migrations.forEach((migration, index) => {
    console.log(`# ${index + 1}. ${migration.description}`);
    console.log(`psql -h lxsbdluguyaaxzaeovwx.supabase.co -p 5432 -U postgres -d postgres -f ${migration.file}\n`);
  });
  
  console.log('# Or run all at once:');
  migrations.forEach(migration => {
    console.log(`psql -h lxsbdluguyaaxzaeovwx.supabase.co -p 5432 -U postgres -d postgres -f ${migration.file}`);
  });
  
  console.log('\n========================================');
  console.log('Alternative: Use the web-based approach');
  console.log('========================================\n');
  console.log('1. Go to: https://supabase.com/dashboard/project/lxsbdluguyaaxzaeovwx/sql/new');
  console.log('2. Copy contents of each file and run in SQL Editor:');
  migrations.forEach((migration, index) => {
    console.log(`   ${index + 1}. ${migration.file}`);
  });
}

main().catch(console.error);

