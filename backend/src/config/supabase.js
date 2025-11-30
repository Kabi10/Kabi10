const { createClient } = require('@supabase/supabase-js');

// Supabase configuration
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseAnonKey = process.env.SUPABASE_ANON_KEY;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error('Missing Supabase environment variables');
}

/**
 * Supabase Client Configuration for Vercel Serverless
 *
 * Connection Pooling (PERF-01 optimization):
 * - Supabase automatically uses Supavisor (PgBouncer) for connection pooling
 * - For serverless environments, the JS client manages connections efficiently
 * - Direct PostgreSQL connections should use the pooler URL (port 6543) instead of direct (port 5432)
 *
 * Performance optimizations:
 * - persistSession: false (serverless functions don't maintain state)
 * - autoRefreshToken: false for admin client (uses service key, no refresh needed)
 * - Supabase JS client uses HTTP connection reuse by default
 */

// Client for public operations (with RLS)
const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    autoRefreshToken: true,
    persistSession: false, // Serverless optimization
    detectSessionInUrl: false
  },
  global: {
    headers: {
      'x-application-name': 'agrimarket-mobile' // Helps identify traffic in Supabase dashboard
    }
  },
  db: {
    schema: 'public'
  }
});

// Admin client for server-side operations (bypasses RLS)
// Used for backend operations that need full database access
const supabaseAdmin = supabaseServiceKey
  ? createClient(supabaseUrl, supabaseServiceKey, {
      auth: {
        autoRefreshToken: false, // Service key doesn't need refresh
        persistSession: false,   // Serverless optimization
        detectSessionInUrl: false
      },
      global: {
        headers: {
          'x-application-name': 'agrimarket-backend' // Helps identify traffic in Supabase dashboard
        }
      },
      db: {
        schema: 'public'
      }
    })
  : null;

module.exports = {
  supabase,
  supabaseAdmin
};