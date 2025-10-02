# Copy SQL Migrations to Clipboard
# Run this script to copy each migration SQL to clipboard for pasting into Supabase SQL Editor

$migrations = @(
    @{
        File = "backend\fix-rls-policies.sql"
        Name = "Migration 1: Fix RLS Policies"
        Description = "Updates Row Level Security to allow public read access"
    },
    @{
        File = "backend\create-market-prices-table.sql"
        Name = "Migration 2: Create Market Prices Table"
        Description = "Creates market_prices table with 8 sample prices"
    },
    @{
        File = "backend\insert-sample-data-fixed.sql"
        Name = "Migration 3: Insert Sample Data"
        Description = "Inserts 5 users, 5 listings, 2 transactions"
    }
)

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     Agrimarket SQL Migration Copy Helper                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will copy each SQL migration to your clipboard." -ForegroundColor White
Write-Host "After copying, paste it into the Supabase SQL Editor and click Run." -ForegroundColor White
Write-Host ""

# Open SQL Editor
Write-Host "Opening Supabase SQL Editor..." -ForegroundColor Cyan
Start-Process "https://supabase.com/dashboard/project/lxsbdluguyaaxzaeovwx/sql/new"
Start-Sleep -Seconds 2

foreach ($migration in $migrations) {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Yellow
    Write-Host $migration.Name -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Yellow
    Write-Host $migration.Description -ForegroundColor Gray
    Write-Host ""
    
    if (Test-Path $migration.File) {
        $content = Get-Content $migration.File -Raw
        $lines = ($content -split "`n").Count
        
        Write-Host "📄 File: $($migration.File) ($lines lines)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Copying to clipboard..." -ForegroundColor Yellow
        
        Set-Clipboard -Value $content
        
        Write-Host "✅ SQL copied to clipboard!" -ForegroundColor Green
        Write-Host ""
        Write-Host "INSTRUCTIONS:" -ForegroundColor Yellow
        Write-Host "  1. Go to the Supabase SQL Editor (should be open in browser)" -ForegroundColor White
        Write-Host "  2. Click in the SQL editor text area" -ForegroundColor White
        Write-Host "  3. Press Ctrl+A to select all, then Ctrl+V to paste" -ForegroundColor White
        Write-Host "  4. Click 'Run' button (or press Ctrl+Enter)" -ForegroundColor White
        Write-Host "  5. Wait for 'Success' message" -ForegroundColor White
        Write-Host "  6. Return here and press Enter for next migration" -ForegroundColor White
        Write-Host ""
        
        Read-Host "Press Enter when ready for next migration"
    } else {
        Write-Host "❌ File not found: $($migration.File)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║              🎉 ALL MIGRATIONS COPIED! 🎉                  ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "After running all 3 migrations in Supabase, verify with:" -ForegroundColor Cyan
Write-Host "  .\test-backend-complete.ps1" -ForegroundColor White
Write-Host ""

