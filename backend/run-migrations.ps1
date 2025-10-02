# Agrimarket Database Migration Runner (PowerShell)
# Executes SQL migrations against Supabase using the Management API

param(
    [string]$ServiceRoleKey = $env:SUPABASE_SERVICE_ROLE_KEY,
    [string]$ProjectRef = "lxsbdluguyaaxzaeovwx",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

# Colors
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }
function Write-Error { Write-Host $args -ForegroundColor Red }

Write-Info "🚀 Agrimarket Database Migration Runner"
Write-Info "========================================"
Write-Host ""

# Check for service role key
if (-not $ServiceRoleKey) {
    Write-Error "❌ Error: SUPABASE_SERVICE_ROLE_KEY is required"
    Write-Host ""
    Write-Host "Set it with:" -ForegroundColor Yellow
    Write-Host '  $env:SUPABASE_SERVICE_ROLE_KEY="your-service-role-key"' -ForegroundColor White
    Write-Host ""
    Write-Host "Get it from: https://supabase.com/dashboard/project/$ProjectRef/settings/api" -ForegroundColor Gray
    exit 1
}

Write-Info "📍 Project: $ProjectRef"
Write-Info "🔑 Service Key: $($ServiceRoleKey.Substring(0, 20))..."
Write-Host ""

# Migration files
$migrations = @(
    @{
        Name = "01_fix_rls_policies"
        File = "fix-rls-policies.sql"
        Description = "Fix RLS policies to allow public read access"
    },
    @{
        Name = "02_create_market_prices"
        File = "create-market-prices-table.sql"
        Description = "Create market_prices table with sample data"
    },
    @{
        Name = "03_insert_sample_data"
        File = "insert-sample-data-fixed.sql"
        Description = "Insert sample users, listings, and transactions"
    }
)

# Function to execute SQL via Supabase REST API
function Invoke-SupabaseSql {
    param(
        [string]$Sql,
        [string]$Description
    )
    
    $url = "https://$ProjectRef.supabase.co/rest/v1/rpc/exec_sql"
    $headers = @{
        "apikey" = $ServiceRoleKey
        "Authorization" = "Bearer $ServiceRoleKey"
        "Content-Type" = "application/json"
        "Prefer" = "return=representation"
    }
    
    $body = @{
        sql_query = $Sql
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
        return @{ Success = $true; Data = $response }
    } catch {
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

# Function to execute SQL using PostgREST query
function Invoke-PostgrestQuery {
    param(
        [string]$Table,
        [string]$Query
    )
    
    $url = "https://$ProjectRef.supabase.co/rest/v1/$Table$Query"
    $headers = @{
        "apikey" = $ServiceRoleKey
        "Authorization" = "Bearer $ServiceRoleKey"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -Headers $headers
        return @{ Success = $true; Data = $response }
    } catch {
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

# Test connection
Write-Info "🔌 Testing database connection..."
$testResult = Invoke-PostgrestQuery -Table "users" -Query "?select=count&limit=1"
if ($testResult.Success) {
    Write-Success "✅ Database connection successful"
} else {
    Write-Warning "⚠️  Connection test inconclusive (table may not exist yet)"
}
Write-Host ""

# Check if we can execute arbitrary SQL
Write-Info "🔍 Checking SQL execution capabilities..."
Write-Warning "⚠️  Supabase REST API does not support arbitrary SQL execution"
Write-Warning "   You must use one of these methods:"
Write-Host ""
Write-Host "   Option 1: Supabase SQL Editor (Recommended)" -ForegroundColor Cyan
Write-Host "   ----------------------------------------" -ForegroundColor Gray
Write-Host "   1. Go to: https://supabase.com/dashboard/project/$ProjectRef/sql/new" -ForegroundColor White
Write-Host "   2. Copy and paste each SQL file content" -ForegroundColor White
Write-Host "   3. Click 'Run' for each migration" -ForegroundColor White
Write-Host ""
Write-Host "   Option 2: PostgreSQL psql CLI" -ForegroundColor Cyan
Write-Host "   ----------------------------------------" -ForegroundColor Gray
Write-Host "   Install: https://www.postgresql.org/download/windows/" -ForegroundColor White
Write-Host "   Then run the commands below" -ForegroundColor White
Write-Host ""

# Generate psql commands
Write-Info "📋 PostgreSQL psql Commands:"
Write-Host ""
Write-Host "# Set database password (get from Supabase dashboard):" -ForegroundColor Yellow
Write-Host '$env:PGPASSWORD="your-database-password"' -ForegroundColor White
Write-Host ""

foreach ($migration in $migrations) {
    $filePath = Join-Path $PSScriptRoot $migration.File
    if (Test-Path $filePath) {
        Write-Host "# $($migration.Description)" -ForegroundColor Green
        Write-Host "psql -h $ProjectRef.supabase.co -p 5432 -U postgres -d postgres -f `"$filePath`"" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Error "❌ File not found: $($migration.File)"
    }
}

Write-Host ""
Write-Info "📄 Migration Files Summary:"
Write-Host ""

foreach ($migration in $migrations) {
    $filePath = Join-Path $PSScriptRoot $migration.File
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        $lines = ($content -split "`n").Count
        $size = (Get-Item $filePath).Length
        
        Write-Host "  ✅ $($migration.File)" -ForegroundColor Green
        Write-Host "     $($migration.Description)" -ForegroundColor Gray
        Write-Host "     Lines: $lines | Size: $size bytes" -ForegroundColor Gray
        Write-Host ""
    } else {
        Write-Host "  ❌ $($migration.File) - NOT FOUND" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Info "=========================================="
Write-Info "Next Steps:"
Write-Info "=========================================="
Write-Host ""
Write-Host "Since direct SQL execution via API is not available," -ForegroundColor Yellow
Write-Host "please use the Supabase SQL Editor:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Open: https://supabase.com/dashboard/project/$ProjectRef/sql/new" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Run migrations in order:" -ForegroundColor Cyan
foreach ($migration in $migrations) {
    Write-Host "   - $($migration.File)" -ForegroundColor White
}
Write-Host ""
Write-Host "3. After running migrations, verify with:" -ForegroundColor Cyan
Write-Host "   .\test-backend-complete.ps1" -ForegroundColor White
Write-Host ""

# Offer to open browser
Write-Host "Would you like to open the Supabase SQL Editor now? (Y/N): " -NoNewline -ForegroundColor Yellow
$response = Read-Host
if ($response -eq "Y" -or $response -eq "y") {
    Start-Process "https://supabase.com/dashboard/project/$ProjectRef/sql/new"
    Write-Success "✅ Opened Supabase SQL Editor in browser"
}

Write-Host ""
Write-Info "Migration runner complete!"

