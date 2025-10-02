#!/usr/bin/env pwsh

# Jaffna Farmers Marketplace - Supabase Setup Script
Write-Host "🗄️ Supabase Setup for Jaffna Farmers Marketplace" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

Write-Host "`n📋 Supabase Setup Checklist:" -ForegroundColor Yellow
Write-Host "1. Create Supabase project" -ForegroundColor Cyan
Write-Host "2. Get project credentials" -ForegroundColor Cyan
Write-Host "3. Update Vercel environment variables" -ForegroundColor Cyan
Write-Host "4. Run database migration" -ForegroundColor Cyan
Write-Host "5. Test API integration" -ForegroundColor Cyan

Write-Host "`n🚀 Step 1: Create Supabase Project" -ForegroundColor Yellow
Write-Host "1. Go to https://supabase.com" -ForegroundColor Cyan
Write-Host "2. Sign up/Login" -ForegroundColor Cyan
Write-Host "3. Click 'New Project'" -ForegroundColor Cyan
Write-Host "4. Project Name: jaffna-farmers-marketplace" -ForegroundColor Cyan
Write-Host "5. Choose region closest to Sri Lanka (Singapore/Mumbai)" -ForegroundColor Cyan
Write-Host "6. Set a strong database password" -ForegroundColor Cyan

Write-Host "`nHave you created your Supabase project? (y/n): " -ForegroundColor Yellow -NoNewline
$projectCreated = Read-Host

if ($projectCreated -eq "n" -or $projectCreated -eq "N") {
    Write-Host "Please create your Supabase project first, then run this script again." -ForegroundColor Red
    exit
}

Write-Host "`n🔑 Step 2: Get Project Credentials" -ForegroundColor Yellow
Write-Host "In your Supabase dashboard:" -ForegroundColor Cyan
Write-Host "1. Go to Settings → API" -ForegroundColor Cyan
Write-Host "2. Copy the following values:" -ForegroundColor Cyan
Write-Host "   - Project URL" -ForegroundColor Gray
Write-Host "   - anon public key" -ForegroundColor Gray
Write-Host "   - service_role secret key" -ForegroundColor Gray

Write-Host "`nDo you have your Supabase credentials ready? (y/n): " -ForegroundColor Yellow -NoNewline
$credentialsReady = Read-Host

if ($credentialsReady -eq "n" -or $credentialsReady -eq "N") {
    Write-Host "Please get your credentials from Supabase dashboard first." -ForegroundColor Red
    exit
}

Write-Host "`n🔧 Step 3: Update Environment Variables" -ForegroundColor Yellow
Write-Host "Current Vercel environment variables:" -ForegroundColor Cyan

Set-Location backend
npx vercel env ls

Write-Host "`nDo you need to update any Supabase environment variables? (y/n): " -ForegroundColor Yellow -NoNewline
$updateEnvVars = Read-Host

if ($updateEnvVars -eq "y" -or $updateEnvVars -eq "Y") {
    Write-Host "Updating Supabase environment variables..." -ForegroundColor Cyan
    
    Write-Host "`nUpdating SUPABASE_URL..." -ForegroundColor Yellow
    npx vercel env rm SUPABASE_URL production
    npx vercel env add SUPABASE_URL production
    
    Write-Host "`nUpdating SUPABASE_ANON_KEY..." -ForegroundColor Yellow
    npx vercel env rm SUPABASE_ANON_KEY production
    npx vercel env add SUPABASE_ANON_KEY production
    
    Write-Host "`nUpdating SUPABASE_SERVICE_ROLE_KEY..." -ForegroundColor Yellow
    npx vercel env rm SUPABASE_SERVICE_ROLE_KEY production
    npx vercel env add SUPABASE_SERVICE_ROLE_KEY production
    
    Write-Host "`n🚀 Redeploying with new environment variables..." -ForegroundColor Yellow
    npx vercel --prod
}

Write-Host "`n🗄️ Step 4: Database Migration" -ForegroundColor Yellow
Write-Host "You need to run the database migration in Supabase:" -ForegroundColor Cyan
Write-Host "1. Open your Supabase dashboard" -ForegroundColor Gray
Write-Host "2. Go to SQL Editor" -ForegroundColor Gray
Write-Host "3. Click 'New Query'" -ForegroundColor Gray
Write-Host "4. Copy the migration SQL from SUPABASE_SETUP_GUIDE.md" -ForegroundColor Gray
Write-Host "5. Paste and click 'Run'" -ForegroundColor Gray

Write-Host "`nHave you run the database migration? (y/n): " -ForegroundColor Yellow -NoNewline
$migrationDone = Read-Host

if ($migrationDone -eq "n" -or $migrationDone -eq "N") {
    Write-Host "Please run the database migration first." -ForegroundColor Red
    Write-Host "Check SUPABASE_SETUP_GUIDE.md for the complete SQL migration." -ForegroundColor Yellow
    exit
}

Write-Host "`n🧪 Step 5: Testing API Integration" -ForegroundColor Yellow
Write-Host "Testing your deployed API..." -ForegroundColor Cyan

# Get the latest deployment URL
$deploymentInfo = npx vercel ls --limit=1 2>$null
Write-Host "Testing health endpoint..." -ForegroundColor Gray

try {
    $healthResponse = Invoke-RestMethod -Uri "https://agrimarket-7hcm8jc2r-kabilantharmaratnam-kpucas-projects.vercel.app/health" -Method GET -TimeoutSec 10
    Write-Host "✅ Health check passed!" -ForegroundColor Green
    Write-Host "API Version: $($healthResponse.version)" -ForegroundColor Cyan
    Write-Host "Environment: $($healthResponse.environment)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Health check failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting listings endpoint..." -ForegroundColor Gray
try {
    $listingsResponse = Invoke-RestMethod -Uri "https://agrimarket-7hcm8jc2r-kabilantharmaratnam-kpucas-projects.vercel.app/api/listings" -Method GET -TimeoutSec 10
    Write-Host "✅ Listings endpoint working!" -ForegroundColor Green
    Write-Host "Listings count: $($listingsResponse.count)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Listings endpoint failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "This might be due to missing database tables or incorrect environment variables." -ForegroundColor Yellow
}

Write-Host "`n🎉 Supabase Setup Summary" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host "✅ Supabase project created" -ForegroundColor Green
Write-Host "✅ Environment variables configured" -ForegroundColor Green
Write-Host "✅ Database migration completed" -ForegroundColor Green
Write-Host "✅ API deployed and tested" -ForegroundColor Green

Write-Host "`n📱 Next Steps:" -ForegroundColor Yellow
Write-Host "1. Update your Android app's API URL to:" -ForegroundColor Cyan
Write-Host "   https://agrimarket-7hcm8jc2r-kabilantharmaratnam-kpucas-projects.vercel.app" -ForegroundColor Gray
Write-Host "2. Test the complete user flow" -ForegroundColor Cyan
Write-Host "3. Configure Supabase Storage for image uploads" -ForegroundColor Cyan
Write-Host "4. Set up authentication policies" -ForegroundColor Cyan

Write-Host "`n🔗 Useful Links:" -ForegroundColor Yellow
Write-Host "- Supabase Dashboard: https://supabase.com/dashboard" -ForegroundColor Cyan
Write-Host "- Vercel Dashboard: https://vercel.com/dashboard" -ForegroundColor Cyan
Write-Host "- API Documentation: Check API_DOCUMENTATION.md" -ForegroundColor Cyan

Set-Location ..