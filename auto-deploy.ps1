# Automated Database Deployment
# Executes SQL migrations automatically using Supabase connection string

param(
    [string]$ServiceRoleKey = $env:SUPABASE_SERVICE_ROLE_KEY,
    [string]$DbPassword = $env:SUPABASE_DB_PASSWORD,
    [string]$ProjectRef = "lxsbdluguyaaxzaeovwx"
)

Write-Host "🚀 Agrimarket Auto-Deploy" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
if (-not $ServiceRoleKey) {
    Write-Host "❌ Missing: SUPABASE_SERVICE_ROLE_KEY" -ForegroundColor Red
    Write-Host ""
    Write-Host "Get it from: https://supabase.com/dashboard/project/$ProjectRef/settings/api" -ForegroundColor Yellow
    Write-Host "Then set: " -NoNewline
    Write-Host '$env:SUPABASE_SERVICE_ROLE_KEY="your-key"' -ForegroundColor Cyan
    Write-Host ""
    
    # Offer manual approach
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Gray
    Write-Host "ALTERNATIVE: Manual Deployment (Recommended)" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Run this instead:" -ForegroundColor Cyan
    Write-Host "  .\deploy-database.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "It will guide you through copy/pasting SQL in the web interface." -ForegroundColor Gray
    Write-Host ""
    exit 1
}

Write-Host "✅ Service Role Key found" -ForegroundColor Green
Write-Host "📍 Project: $ProjectRef" -ForegroundColor Cyan
Write-Host ""

# Test connection using REST API
Write-Host "🔌 Testing connection..." -ForegroundColor Cyan
$testUrl = "https://$ProjectRef.supabase.co/rest/v1/?select=*&limit=1"
$headers = @{
    "apikey" = $ServiceRoleKey
    "Authorization" = "Bearer $ServiceRoleKey"
}

try {
    $null = Invoke-WebRequest -Uri $testUrl -Headers $headers -Method Get -UseBasicParsing
    Write-Host "✅ Connection successful" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Connection test inconclusive" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host "IMPORTANT: Direct SQL Execution Limitation" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host ""
Write-Host "Supabase's REST API does not support arbitrary SQL execution." -ForegroundColor White
Write-Host "You must use one of these methods:" -ForegroundColor White
Write-Host ""
Write-Host "✅ RECOMMENDED: Web-based SQL Editor" -ForegroundColor Green
Write-Host "   Run: .\deploy-database.ps1" -ForegroundColor Cyan
Write-Host "   (Interactive script that copies SQL and opens browser)" -ForegroundColor Gray
Write-Host ""
Write-Host "✅ ALTERNATIVE: PostgreSQL psql CLI" -ForegroundColor Green
Write-Host "   1. Install PostgreSQL: https://www.postgresql.org/download/windows/" -ForegroundColor White
Write-Host "   2. Get database password from Supabase dashboard" -ForegroundColor White
Write-Host "   3. Run:" -ForegroundColor White
Write-Host '      $env:PGPASSWORD="your-db-password"' -ForegroundColor Cyan
Write-Host "      psql -h $ProjectRef.supabase.co -p 5432 -U postgres -d postgres -f backend\fix-rls-policies.sql" -ForegroundColor Cyan
Write-Host "      psql -h $ProjectRef.supabase.co -p 5432 -U postgres -d postgres -f backend\create-market-prices-table.sql" -ForegroundColor Cyan
Write-Host "      psql -h $ProjectRef.supabase.co -p 5432 -U postgres -d postgres -f backend\insert-sample-data-fixed.sql" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ EASIEST: Manual Copy/Paste" -ForegroundColor Green
Write-Host "   1. Open: https://supabase.com/dashboard/project/$ProjectRef/sql/new" -ForegroundColor White
Write-Host "   2. Copy contents of backend\fix-rls-policies.sql and run" -ForegroundColor White
Write-Host "   3. Copy contents of backend\create-market-prices-table.sql and run" -ForegroundColor White
Write-Host "   4. Copy contents of backend\insert-sample-data-fixed.sql and run" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host ""
Write-Host "Would you like to:" -ForegroundColor Yellow
Write-Host "  [1] Run interactive deployment (opens browser, copies SQL)" -ForegroundColor Cyan
Write-Host "  [2] Open SQL Editor manually" -ForegroundColor Cyan
Write-Host "  [3] Show file contents to copy manually" -ForegroundColor Cyan
Write-Host "  [Q] Quit" -ForegroundColor Red
Write-Host ""
Write-Host "Choice: " -NoNewline -ForegroundColor Yellow
$choice = Read-Host

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Launching interactive deployment..." -ForegroundColor Cyan
        & ".\deploy-database.ps1"
    }
    "2" {
        Write-Host ""
        Write-Host "Opening Supabase SQL Editor..." -ForegroundColor Cyan
        Start-Process "https://supabase.com/dashboard/project/$ProjectRef/sql/new"
        Write-Host "✅ Browser opened" -ForegroundColor Green
        Write-Host ""
        Write-Host "Copy and run these files in order:" -ForegroundColor Yellow
        Write-Host "  1. backend\fix-rls-policies.sql" -ForegroundColor White
        Write-Host "  2. backend\create-market-prices-table.sql" -ForegroundColor White
        Write-Host "  3. backend\insert-sample-data-fixed.sql" -ForegroundColor White
    }
    "3" {
        Write-Host ""
        $files = @(
            "backend\fix-rls-policies.sql",
            "backend\create-market-prices-table.sql",
            "backend\insert-sample-data-fixed.sql"
        )
        
        foreach ($file in $files) {
            Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
            Write-Host "File: $file" -ForegroundColor Yellow
            Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
            if (Test-Path $file) {
                Get-Content $file | Write-Host -ForegroundColor Gray
            } else {
                Write-Host "❌ File not found" -ForegroundColor Red
            }
            Write-Host ""
        }
    }
    default {
        Write-Host ""
        Write-Host "Exiting..." -ForegroundColor Yellow
    }
}

Write-Host ""

