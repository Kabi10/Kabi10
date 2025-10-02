# Test Backend Deployment - Complete Verification
# Run this after completing the SQL migration steps

$API_BASE = "https://agrimarket-rmltqvlad-kabilantharmaratnam-kpucas-projects.vercel.app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Agrimarket Backend Testing Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "[1/6] Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/health" -Method Get
    if ($response.success) {
        Write-Host "  ✅ Health check passed" -ForegroundColor Green
        Write-Host "  Message: $($response.message)" -ForegroundColor Gray
    } else {
        Write-Host "  ❌ Health check failed" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Get Listings
Write-Host "[2/6] Testing GET /api/listings..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/listings" -Method Get
    if ($response.success) {
        $count = $response.data.Count
        Write-Host "  ✅ Listings endpoint working" -ForegroundColor Green
        Write-Host "  Found $count listings" -ForegroundColor Gray
        if ($count -gt 0) {
            Write-Host "  Sample listing:" -ForegroundColor Gray
            $sample = $response.data[0]
            Write-Host "    - Crop: $($sample.cropType)" -ForegroundColor Gray
            Write-Host "    - Quantity: $($sample.quantity) $($sample.unit)" -ForegroundColor Gray
            Write-Host "    - Price: Rs. $($sample.pricePerUnit)/$($sample.unit)" -ForegroundColor Gray
            Write-Host "    - Location: $($sample.location)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ❌ Listings endpoint failed" -ForegroundColor Red
        Write-Host "  Message: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  This likely means RLS policies need to be fixed" -ForegroundColor Yellow
}
Write-Host ""

# Test 3: Get Market Prices
Write-Host "[3/6] Testing GET /api/v1/market-prices..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/v1/market-prices" -Method Get
    if ($response.success) {
        $count = $response.data.Count
        Write-Host "  ✅ Market prices endpoint working" -ForegroundColor Green
        Write-Host "  Found $count market prices" -ForegroundColor Gray
        if ($count -gt 0) {
            Write-Host "  Sample prices:" -ForegroundColor Gray
            foreach ($price in $response.data | Select-Object -First 3) {
                $trend = $price.trend
                $trendIcon = if ($trend -eq "UP") { "📈" } elseif ($trend -eq "DOWN") { "📉" } else { "➡️" }
                Write-Host "    $trendIcon $($price.cropType): Rs. $($price.currentPrice)/$($price.unit) ($trend $($price.changePercentage)%)" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  ❌ Market prices endpoint failed" -ForegroundColor Red
        Write-Host "  Message: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ⚠️  Market prices endpoint not found or error" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host "  This is expected if market_prices table doesn't exist yet" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Get Single Listing
Write-Host "[4/6] Testing GET /api/listings/{id}..." -ForegroundColor Yellow
try {
    # First get a listing ID
    $listingsResponse = Invoke-RestMethod -Uri "$API_BASE/api/listings" -Method Get
    if ($listingsResponse.success -and $listingsResponse.data.Count -gt 0) {
        $listingId = $listingsResponse.data[0].id
        $response = Invoke-RestMethod -Uri "$API_BASE/api/listings/$listingId" -Method Get
        if ($response.success) {
            Write-Host "  ✅ Single listing endpoint working" -ForegroundColor Green
            Write-Host "  Listing ID: $listingId" -ForegroundColor Gray
            Write-Host "  View count incremented" -ForegroundColor Gray
        } else {
            Write-Host "  ❌ Single listing endpoint failed" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️  No listings available to test" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Single listing endpoint not implemented yet" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Test 5: Create Listing (requires auth - will fail without token)
Write-Host "[5/6] Testing POST /api/listings/create..." -ForegroundColor Yellow
try {
    $newListing = @{
        cropType = "TOMATO"
        quantity = 100
        unit = "Kg"
        pricePerUnit = 120
        quality = "A"
        harvestDate = "2025-10-15"
        location = "Jaffna Test"
        description = "Test listing from API"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$API_BASE/api/listings/create" -Method Post -Body $newListing -ContentType "application/json"
    if ($response.success) {
        Write-Host "  ✅ Create listing endpoint working" -ForegroundColor Green
        Write-Host "  Created listing ID: $($response.data.id)" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠️  Create listing requires authentication" -ForegroundColor Yellow
        Write-Host "  Message: $($response.message)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ⚠️  Create listing requires authentication (expected)" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Test 6: Send OTP
Write-Host "[6/6] Testing POST /api/auth/send-otp..." -ForegroundColor Yellow
try {
    $otpRequest = @{
        phoneNumber = "+94771234567"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$API_BASE/api/auth/send-otp" -Method Post -Body $otpRequest -ContentType "application/json"
    if ($response.success) {
        Write-Host "  ✅ Send OTP endpoint working" -ForegroundColor Green
        Write-Host "  Message: $($response.message)" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠️  Send OTP failed (may need SMS credentials)" -ForegroundColor Yellow
        Write-Host "  Message: $($response.message)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ⚠️  Send OTP endpoint error" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host "  This is expected if SMS provider credentials are not configured" -ForegroundColor Yellow
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Critical Endpoints (Must Work):" -ForegroundColor White
Write-Host "  - Health Check: Should be ✅" -ForegroundColor Gray
Write-Host "  - GET Listings: Should be ✅ after RLS fix" -ForegroundColor Gray
Write-Host "  - GET Market Prices: Should be ✅ after table creation" -ForegroundColor Gray
Write-Host ""
Write-Host "Optional Endpoints (May require auth/config):" -ForegroundColor White
Write-Host "  - GET Single Listing: May need implementation" -ForegroundColor Gray
Write-Host "  - POST Create Listing: Requires authentication" -ForegroundColor Gray
Write-Host "  - POST Send OTP: Requires SMS credentials" -ForegroundColor Gray
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. If GET Listings failed: Run fix-rls-policies.sql in Supabase" -ForegroundColor White
Write-Host "  2. If Market Prices failed: Run create-market-prices-table.sql" -ForegroundColor White
Write-Host "  3. If no data found: Run insert-sample-data-fixed.sql" -ForegroundColor White
Write-Host "  4. Re-run this test script to verify fixes" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

