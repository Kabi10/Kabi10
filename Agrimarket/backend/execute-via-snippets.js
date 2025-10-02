#!/usr/bin/env node
/**
 * Execute SQL via Supabase Management API (Snippets)
 * Uses the CLI access token to execute SQL
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const PROJECT_REF = 'lxsbdluguyaaxzaeovwx';

// Get access token from Supabase CLI
function getAccessToken() {
  try {
    // The token is stored in ~/.supabase/access-token
    const homeDir = process.env.HOME || process.env.USERPROFILE;
    const tokenPath = path.join(homeDir, '.supabase', 'access-token');
    
    if (fs.existsSync(tokenPath)) {
      const token = fs.readFileSync(tokenPath, 'utf8').trim();
      return token;
    }
    
    throw new Error('No access token found. Run: npx supabase login');
  } catch (error) {
    console.error('Failed to get access token:', error.message);
    process.exit(1);
  }
}

async function executeSQLViaAPI(sql, description) {
  const token = getAccessToken();
  
  console.log(`\n📄 ${description}`);
  console.log(`   SQL length: ${sql.length} characters`);
  
  // Create a temporary file with the SQL
  const tempFile = path.join(__dirname, 'temp-migration.sql');
  fs.writeFileSync(tempFile, sql);
  
  try {
    // Use curl to execute via Management API
    const curlCommand = `curl -X POST "https://api.supabase.com/v1/projects/${PROJECT_REF}/database/query" -H "Authorization: Bearer ${token}" -H "Content-Type: application/json" -d "{\\"query\\":\\"${sql.replace(/"/g, '\\\\"').replace(/\n/g, '\\n')}\\"}"`;
    
    const result = execSync(curlCommand, { encoding: 'utf8', stdio: 'pipe' });
    console.log(`   ✅ Success`);
    console.log(`   Response:`, result.substring(0, 200));
    
    return true;
  } catch (error) {
    console.error(`   ❌ Failed:`, error.message);
    return false;
  } finally {
    // Clean up temp file
    if (fs.existsSync(tempFile)) {
      fs.unlinkSync(tempFile);
    }
  }
}

async function main() {
  console.log('🚀 Execute Migrations via Supabase Management API');
  console.log('================================================\n');
  
  const migrations = [
    { file: 'insert-sample-data-fixed.sql', desc: 'Insert Sample Data' }
  ];
  
  for (const mig of migrations) {
    const sqlPath = path.join(__dirname, '..', 'supabase', 'migrations', '20250101000003_insert_sample_data.sql');
    
    if (!fs.existsSync(sqlPath)) {
      console.error(`❌ File not found: ${sqlPath}`);
      continue;
    }
    
    const sql = fs.readFileSync(sqlPath, 'utf8');
    await executeSQLViaAPI(sql, mig.desc);
  }
  
  console.log('\n✅ Migration execution complete!');
}

main().catch(console.error);

