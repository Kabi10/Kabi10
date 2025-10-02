# Run Database Migrations
# Wrapper script for execute-migrations.js

param(
    [string]$DbPassword = $env:SUPABASE_DB_PASSWORD,
    [string]$ProjectRef = "lxsbdluguyaaxzaeovwx"
)

Write-Host "🚀 Agrimarket Migration Runner" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""

# Check if password is provided
if (-not $DbPassword) {
    Write-Host "❌ Database password required" -ForegroundColor Red
    Write-Host ""
    Write-Host "Get your database password:" -ForegroundColor Yellow
    Write-Host "  1. Go to: https://supabase.com/dashboard/project/$ProjectRef/settings/database" -ForegroundColor White
    Write-Host "  2. Look for 'Database Password' section" -ForegroundColor White
    Write-Host "  3. Click 'Reset Database Password' if you don't have it" -ForegroundColor White
    Write-Host ""
    Write-Host "Then run:" -ForegroundColor Yellow
    Write-Host '  $env:SUPABASE_DB_PASSWORD="your-password"' -ForegroundColor Cyan
    Write-Host "  .\run-migrations.ps1" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or pass it directly:" -ForegroundColor Yellow
    Write-Host '  .\run-migrations.ps1 -DbPassword "your-password"' -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

# Set environment variable for Node.js script
$env:SUPABASE_DB_PASSWORD = $DbPassword

# Check if Node.js is installed
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js not found" -ForegroundColor Red
    Write-Host "Install from: https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

# Check if we're in the right directory
if (-not (Test-Path "backend\execute-migrations.js")) {
    Write-Host "❌ execute-migrations.js not found" -ForegroundColor Red
    Write-Host "Make sure you're in the Agrimarket directory" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Executing migrations..." -ForegroundColor Cyan
Write-Host ""

# Run the Node.js migration script
cd backend
node execute-migrations.js
$exitCode = $LASTEXITCODE
cd ..

if ($exitCode -eq 0) {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Green
    Write-Host "✅ Migrations completed successfully!" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Test backend: .\test-backend-complete.ps1" -ForegroundColor White
    Write-Host "  2. Update Android API URL in ApiConfig.kt" -ForegroundColor White
    Write-Host "  3. Rebuild app: .\gradlew clean assembleDebug" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Red
    Write-Host "❌ Migration failed" -ForegroundColor Red
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the error messages above for details." -ForegroundColor Yellow
    Write-Host ""
}

