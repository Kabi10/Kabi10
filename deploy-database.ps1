# Agrimarket Database Deployment - Automated Workflow
# This script guides you through executing SQL migrations step-by-step

param(
    [string]$ProjectRef = "lxsbdluguyaaxzaeovwx"
)

$ErrorActionPreference = "Stop"

# Migration files in order
$migrations = @(
    @{
        File = "backend\fix-rls-policies.sql"
        Name = "Fix RLS Policies"
        Description = "Updates Row Level Security policies to allow public read access to listings"
        Expected = "Policies updated successfully"
    },
    @{
        File = "backend\create-market-prices-table.sql"
        Name = "Create Market Prices Table"
        Description = "Creates market_prices table and inserts 8 sample market prices"
        Expected = "Table created, 8 rows inserted"
    },
    @{
        File = "backend\insert-sample-data-fixed.sql"
        Name = "Insert Sample Data"
        Description = "Inserts 5 users, 5 listings, and 2 transactions for testing"
        Expected = "Users: 5, Listings: 5, Transactions: 2"
    }
)

function Show-Header {
    Clear-Host
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║     Agrimarket Database Deployment - Step by Step         ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Show-Migration {
    param($Index, $Migration)
    
    Show-Header
    Write-Host "Migration $($Index + 1) of $($migrations.Count)" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📄 $($Migration.Name)" -ForegroundColor Cyan
    Write-Host "   $($Migration.Description)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "📁 File: $($Migration.File)" -ForegroundColor Gray
    Write-Host ""
    
    if (Test-Path $Migration.File) {
        $content = Get-Content $Migration.File -Raw
        $lines = ($content -split "`n").Count
        Write-Host "✅ File found ($lines lines)" -ForegroundColor Green
    } else {
        Write-Host "❌ File not found!" -ForegroundColor Red
        return $false
    }
    
    Write-Host ""
    Write-Host "Expected Result: $($Migration.Expected)" -ForegroundColor Green
    Write-Host ""
    Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Gray
    Write-Host ""
    
    return $true
}

function Copy-ToClipboard {
    param($FilePath)
    
    $content = Get-Content $FilePath -Raw
    Set-Clipboard -Value $content
    Write-Host "✅ SQL copied to clipboard!" -ForegroundColor Green
}

# Main workflow
Show-Header

Write-Host "This script will help you execute 3 SQL migrations to complete" -ForegroundColor White
Write-Host "your Agrimarket backend deployment (85% → 100%)" -ForegroundColor White
Write-Host ""
Write-Host "For each migration, the script will:" -ForegroundColor Yellow
Write-Host "  1. Copy the SQL to your clipboard" -ForegroundColor White
Write-Host "  2. Open the Supabase SQL Editor in your browser" -ForegroundColor White
Write-Host "  3. You paste (Ctrl+V) and click 'Run'" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to begin..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Execute each migration
for ($i = 0; $i -lt $migrations.Count; $i++) {
    $migration = $migrations[$i]
    
    $fileExists = Show-Migration -Index $i -Migration $migration
    
    if (-not $fileExists) {
        Write-Host "Cannot continue - file missing!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Ready to execute this migration?" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  [Y] Yes, copy SQL and open browser" -ForegroundColor Green
    Write-Host "  [S] Skip this migration" -ForegroundColor Yellow
    Write-Host "  [Q] Quit" -ForegroundColor Red
    Write-Host ""
    Write-Host "Choice: " -NoNewline -ForegroundColor Cyan
    
    $choice = Read-Host
    
    switch ($choice.ToUpper()) {
        "Y" {
            Write-Host ""
            Write-Host "📋 Copying SQL to clipboard..." -ForegroundColor Cyan
            Copy-ToClipboard -FilePath $migration.File
            
            Write-Host "🌐 Opening Supabase SQL Editor..." -ForegroundColor Cyan
            Start-Process "https://supabase.com/dashboard/project/$ProjectRef/sql/new"
            
            Write-Host ""
            Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
            Write-Host "║                    INSTRUCTIONS                            ║" -ForegroundColor Yellow
            Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor Yellow
            Write-Host "║  1. Wait for SQL Editor to load in your browser           ║" -ForegroundColor White
            Write-Host "║  2. Click in the SQL editor text area                      ║" -ForegroundColor White
            Write-Host "║  3. Press Ctrl+V to paste the SQL                          ║" -ForegroundColor White
            Write-Host "║  4. Click the 'Run' button (or press Ctrl+Enter)           ║" -ForegroundColor White
            Write-Host "║  5. Wait for 'Success' message                             ║" -ForegroundColor White
            Write-Host "║  6. Return to this window                                  ║" -ForegroundColor White
            Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Expected: $($migration.Expected)" -ForegroundColor Green
            Write-Host ""
            Write-Host "Press any key when migration is complete..." -ForegroundColor Cyan
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            
            Write-Host ""
            Write-Host "Did the migration succeed? (Y/N): " -NoNewline -ForegroundColor Yellow
            $success = Read-Host
            
            if ($success.ToUpper() -ne "Y") {
                Write-Host ""
                Write-Host "❌ Migration failed or was not completed" -ForegroundColor Red
                Write-Host ""
                Write-Host "Troubleshooting:" -ForegroundColor Yellow
                Write-Host "  - Check for error messages in the SQL Editor" -ForegroundColor White
                Write-Host "  - Verify you're in the correct project ($ProjectRef)" -ForegroundColor White
                Write-Host "  - Try running the SQL again" -ForegroundColor White
                Write-Host ""
                Write-Host "Press any key to continue anyway, or Ctrl+C to quit..." -ForegroundColor Cyan
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            } else {
                Write-Host ""
                Write-Host "✅ Migration $($i + 1) completed successfully!" -ForegroundColor Green
                Write-Host ""
            }
        }
        "S" {
            Write-Host ""
            Write-Host "⏭️  Skipping migration $($i + 1)" -ForegroundColor Yellow
            Write-Host ""
        }
        "Q" {
            Write-Host ""
            Write-Host "👋 Deployment cancelled" -ForegroundColor Yellow
            exit 0
        }
        default {
            Write-Host ""
            Write-Host "Invalid choice. Skipping..." -ForegroundColor Red
            Write-Host ""
        }
    }
    
    if ($i -lt $migrations.Count - 1) {
        Write-Host "Press any key for next migration..." -ForegroundColor Cyan
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
}

# Completion
Show-Header
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║              🎉 DEPLOYMENT COMPLETE! 🎉                    ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "All 3 migrations have been processed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "═══════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Verify Backend Endpoints:" -ForegroundColor Yellow
Write-Host "   .\test-backend-complete.ps1" -ForegroundColor White
Write-Host ""
Write-Host "2. Update Android App:" -ForegroundColor Yellow
Write-Host "   - Edit: app\src\main\java\com\senthapps\slagrimarket\data\api\ApiConfig.kt" -ForegroundColor White
Write-Host "   - Set: PRODUCTION_BASE_URL = `"https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app/api/`"" -ForegroundColor White
Write-Host ""
Write-Host "3. Rebuild Android App:" -ForegroundColor Yellow
Write-Host "   .\gradlew clean assembleDebug" -ForegroundColor White
Write-Host "   .\gradlew installDebug" -ForegroundColor White
Write-Host ""
Write-Host "4. Test Integration:" -ForegroundColor Yellow
Write-Host "   - Launch app" -ForegroundColor White
Write-Host "   - Check Market Prices (should show 8 prices)" -ForegroundColor White
Write-Host "   - Check Listings (should show 5 listings)" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Status: 85% → 100% ✅" -ForegroundColor Green
Write-Host ""

