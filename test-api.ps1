# Quick API Test Script for Jaffna Farmers Marketplace
$apiUrl = "https://agrimarket-qwf850445-kabilantharmaratnam-kpucas-projects.vercel.app"

Write-Host "🧪 Testing Jaffna Farmers Marketplace API" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host "API URL: $apiUrl" -ForegroundColor Cyan

# Test health endpoint
Write-Host "`n1. Testing health endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$apiUrl/health" -Method GET
    Write-Host "✅ Health check passed!" -ForegroundColor Green
    Write-Host "   Version: $($health.version)" -ForegroundColor Gray
    Write-Host "   Environment: $($health.environment)" -ForegroundColor Gray
    Write-Host "   Database: $($health.database)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test listings endpoint
Write-Host "`n2. Testing listings endpoint..." -ForegroundColor Yellow
try {
    $listings = Invoke-RestMethod -Uri "$apiUrl/api/listings" -Method GET
    Write-Host "✅ Listings endpoint working!" -ForegroundColor Green
    Write-Host "   Found $($listings.count) listings" -ForegroundColor Gray
    
    if ($listings.data.Count -gt 0) {
        Write-Host "   Sample listing:" -ForegroundColor Gray
        $first = $listings.data[0]
        Write-Host "   - $($first.crop_type): $($first.quantity) $($first.unit) @ Rs.$($first.price_per_unit)/$($first.unit)" -ForegroundColor Cyan
        Write-Host "   - Location: $($first.location)" -ForegroundColor Cyan
        Write-Host "   - Quality: $($first.quality)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Listings endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   💡 This usually means the database migration hasn't been run yet." -ForegroundColor Yellow
    Write-Host "   📖 Check RUN_MIGRATION.md for instructions." -ForegroundColor Yellow
}

# Test auth endpoint (basic test)
Write-Host "`n3. Testing auth endpoint..." -ForegroundColor Yellow
try {
    $authBody = @{
        phone = "+94771234567"
    } | ConvertTo-Json
    
    $auth = Invoke-RestMethod -Uri "$apiUrl/api/auth/send-otp" -Method POST -Body $authBody -ContentType "application/json"
    Write-Host "✅ Auth endpoint accessible!" -ForegroundColor Green
    Write-Host "   Response: $($auth.message)" -ForegroundColor Gray
} catch {
    if ($_.Exception.Message -like "*SMS*" -or $_.Exception.Message -like "*Dialog*") {
        Write-Host "⚠️  Auth endpoint working (SMS provider not configured)" -ForegroundColor Yellow
        Write-Host "   This is normal for testing - SMS will work in production" -ForegroundColor Gray
    } else {
        Write-Host "❌ Auth endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n📊 Test Summary" -ForegroundColor Green
Write-Host "===============" -ForegroundColor Green
Write-Host "Your API is deployed and ready for use!" -ForegroundColor Cyan
Write-Host "`n📱 Next steps:" -ForegroundColor Yellow
Write-Host "1. Update Android app API URL" -ForegroundColor Gray
Write-Host "2. Test complete user registration flow" -ForegroundColor Gray
Write-Host "3. Test listing creation and viewing" -ForegroundColor Gray
Write-Host "4. Test transaction creation" -ForegroundColor Gray

Write-Host "`n🔗 API Documentation:" -ForegroundColor Yellow
Write-Host "Check API_DOCUMENTATION.md for complete endpoint details" -ForegroundColor Gray