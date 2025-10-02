# Direct SQL Execution via Supabase Management API
# This script executes SQL migrations using Supabase's Management API

param(
    [string]$AccessToken = $env:SUPABASE_ACCESS_TOKEN,
    [string]$ProjectRef = "lxsbdluguyaaxzaeovwx"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Agrimarket Migration Executor" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

if (-not $AccessToken) {
    Write-Host "❌ SUPABASE_ACCESS_TOKEN not set" -ForegroundColor Red
    Write-Host ""
    Write-Host "To get your access token:" -ForegroundColor Yellow
    Write-Host "1. Go to: https://supabase.com/dashboard/account/tokens" -ForegroundColor White
    Write-Host "2. Generate a new token" -ForegroundColor White
    Write-Host "3. Set it: " -NoNewline -ForegroundColor White
    Write-Host '$env:SUPABASE_ACCESS_TOKEN="your-token"' -ForegroundColor Cyan
    Write-Host ""
    Write-Host "OR use the simpler web-based approach:" -ForegroundColor Yellow
    Write-Host "1. Go to: https://supabase.com/dashboard/project/$ProjectRef/sql/new" -ForegroundColor White
    Write-Host "2. Copy/paste SQL from each file and run" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Migration files
$migrations = @(
    "fix-rls-policies.sql",
    "create-market-prices-table.sql",
    "insert-sample-data-fixed.sql"
)

Write-Host "📍 Project: $ProjectRef" -ForegroundColor Cyan
Write-Host "🔑 Token: $($AccessToken.Substring(0, 20))..." -ForegroundColor Cyan
Write-Host ""

foreach ($migrationFile in $migrations) {
    $filePath = Join-Path "backend" $migrationFile
    
    if (-not (Test-Path $filePath)) {
        Write-Host "❌ File not found: $filePath" -ForegroundColor Red
        continue
    }
    
    Write-Host "📄 Executing: $migrationFile" -ForegroundColor Yellow
    
    $sql = Get-Content $filePath -Raw
    
    # Supabase Management API endpoint for SQL execution
    $url = "https://api.supabase.com/v1/projects/$ProjectRef/database/query"
    
    $headers = @{
        "Authorization" = "Bearer $AccessToken"
        "Content-Type" = "application/json"
    }
    
    $body = @{
        query = $sql
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
        Write-Host "✅ Success: $migrationFile" -ForegroundColor Green
        if ($response.result) {
            Write-Host "   Result: $($response.result | ConvertTo-Json -Compress)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "❌ Failed: $migrationFile" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        
        # Check if it's an auth error
        if ($_.Exception.Message -like "*401*" -or $_.Exception.Message -like "*403*") {
            Write-Host ""
            Write-Host "⚠️  Authentication failed. Your access token may be invalid or expired." -ForegroundColor Yellow
            Write-Host "   Get a new token from: https://supabase.com/dashboard/account/tokens" -ForegroundColor White
            exit 1
        }
    }
    
    Write-Host ""
}

Write-Host "✅ Migration execution complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Verify with: .\test-backend-complete.ps1" -ForegroundColor Cyan

