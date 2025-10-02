#!/usr/bin/env pwsh

# Jaffna Farmers Marketplace - Vercel Deployment Script
Write-Host "🚀 Jaffna Farmers Marketplace - Vercel Deployment" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# Step 1: Check Vercel CLI
Write-Host "`n📦 Step 1: Checking Vercel CLI..." -ForegroundColor Yellow
$vercelInstalled = Get-Command vercel -ErrorAction SilentlyContinue
if (-not $vercelInstalled) {
    Write-Host "Installing Vercel CLI..." -ForegroundColor Cyan
    npm install -g vercel
} else {
    Write-Host "✅ Vercel CLI is installed" -ForegroundColor Green
}

# Step 2: Login
Write-Host "`n🔐 Step 2: Vercel Login..." -ForegroundColor Yellow
Write-Host "Opening browser for authentication..." -ForegroundColor Cyan
vercel login

# Step 3: Setup backend
Write-Host "`n📁 Step 3: Setting up backend..." -ForegroundColor Yellow
Set-Location backend

if (-not (Test-Path "node_modules")) {
    Write-Host "Installing dependencies..." -ForegroundColor Cyan
    npm install
} else {
    Write-Host "✅ Dependencies ready" -ForegroundColor Green
}

# Step 4: Environment variables prompt
Write-Host "`n🔧 Step 4: Environment Variables" -ForegroundColor Yellow
Write-Host "You need to set these environment variables:" -ForegroundColor Cyan
Write-Host "- SUPABASE_URL" -ForegroundColor Gray
Write-Host "- SUPABASE_ANON_KEY" -ForegroundColor Gray
Write-Host "- SUPABASE_SERVICE_ROLE_KEY" -ForegroundColor Gray
Write-Host "- JWT_SECRET" -ForegroundColor Gray
Write-Host "- JWT_REFRESH_SECRET" -ForegroundColor Gray
Write-Host "- SMS_PROVIDER (dialog)" -ForegroundColor Gray
Write-Host "- DIALOG_API_KEY" -ForegroundColor Gray
Write-Host "- DIALOG_API_SECRET" -ForegroundColor Gray
Write-Host "- NODE_ENV (production)" -ForegroundColor Gray

Write-Host "`nSet environment variables now? (y/n): " -ForegroundColor Yellow -NoNewline
$response = Read-Host

if ($response -eq "y" -or $response -eq "Y") {
    Write-Host "Setting environment variables..." -ForegroundColor Cyan
    Write-Host "Follow the prompts for each variable." -ForegroundColor Gray
    
    vercel env add SUPABASE_URL production
    vercel env add SUPABASE_ANON_KEY production
    vercel env add SUPABASE_SERVICE_ROLE_KEY production
    vercel env add JWT_SECRET production
    vercel env add JWT_REFRESH_SECRET production
    vercel env add SMS_PROVIDER production
    vercel env add DIALOG_API_KEY production
    vercel env add DIALOG_API_SECRET production
    vercel env add NODE_ENV production
}

# Step 5: Deploy
Write-Host "`n🚀 Step 5: Deploying to Vercel..." -ForegroundColor Yellow
Write-Host "Starting production deployment..." -ForegroundColor Cyan

vercel --prod

Write-Host "`n✅ Deployment completed!" -ForegroundColor Green
Write-Host "Check the URL above for your live API." -ForegroundColor Green

# Step 6: Test deployment
Write-Host "`n🧪 Testing deployment..." -ForegroundColor Yellow
Write-Host "You can test your API health endpoint manually:" -ForegroundColor Cyan
Write-Host "curl https://your-deployment-url.vercel.app/health" -ForegroundColor Gray

Write-Host "`n🎉 Deployment process finished!" -ForegroundColor Green
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Update your Android app's API URL" -ForegroundColor Cyan
Write-Host "2. Test all endpoints" -ForegroundColor Cyan
Write-Host "3. Verify SMS functionality" -ForegroundColor Cyan

Set-Location ..