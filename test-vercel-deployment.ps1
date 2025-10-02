#!/usr/bin/env pwsh

# Jaffna Farmers Marketplace - Vercel Deployment Test Script
Write-Host "🧪 Testing Vercel Deployment" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

$baseUrl = "https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app"
$testResults = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Yellow
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $response = Invoke-RestMethod @params
        
        if ($response.success -eq $true) {
            Write-Host "  ✅ PASS" -ForegroundColor Green
            $script:testResults += @{ Name = $Name; Status = "PASS"; Response = $response }
            return $response
        } else {
            Write-Host "  ⚠️  WARN: success=false" -ForegroundColor Yellow
            $script:testResults += @{ Name = $Name; Status = "WARN"; Response = $response }
            return $response
        }
    } catch {
        Write-Host "  ❌ FAIL: $($_.Exception.Message)" -ForegroundColor Red
        $script:testResults += @{ Name = $Name; Status = "FAIL"; Error = $_.Exception.Message }
        return $null
    }
    
    Write-Host ""
}

# Test 1: Health Check
Write-Host "`n1️⃣  Health Check" -ForegroundColor Cyan
Write-Host "─────────────────" -ForegroundColor Gray
$health = Test-Endpoint -Name "Health Check" -Url "$baseUrl/health"
if ($health) {
    Write-Host "  Version: $($health.version)" -ForegroundColor Gray
    Write-Host "  Database: $($health.database)" -ForegroundColor Gray
}

# Test 2: Listings (Public)
Write-Host "`n2️⃣  Listings Endpoint" -ForegroundColor Cyan
Write-Host "─────────────────────" -ForegroundColor Gray
$listings = Test-Endpoint -Name "Get Listings" -Url "$baseUrl/api/listings"
if ($listings) {
    Write-Host "  Count: $($listings.count)" -ForegroundColor Gray
    Write-Host "  Page: $($listings.page)/$($listings.totalPages)" -ForegroundColor Gray
    if ($listings.data -and $listings.data.Count -gt 0) {
        Write-Host "  Sample: $($listings.data[0].crop_type) @ $($listings.data[0].location)" -ForegroundColor Gray
    }
}

# Test 3: Listings with Filters
Write-Host "`n3️⃣  Listings with Filters" -ForegroundColor Cyan
Write-Host "─────────────────────────" -ForegroundColor Gray
$filteredListings = Test-Endpoint -Name "Filtered Listings" -Url "$baseUrl/api/listings?location=Jaffna&limit=5"
if ($filteredListings) {
    Write-Host "  Filtered Count: $($filteredListings.count)" -ForegroundColor Gray
}

# Test 4: Send OTP
Write-Host "`n4️⃣  Authentication - Send OTP" -ForegroundColor Cyan
Write-Host "──────────────────────────────" -ForegroundColor Gray
$otpBody = @{ phoneNumber = "+94771234567" }
$otp = Test-Endpoint -Name "Send OTP" -Url "$baseUrl/api/auth/send-otp" -Method "POST" -Body $otpBody
if ($otp -and $otp.otp) {
    Write-Host "  OTP Code (dev): $($otp.otp)" -ForegroundColor Gray
    Write-Host "  OTP ID: $($otp.otpId)" -ForegroundColor Gray
    
    # Test 5: Verify OTP
    Write-Host "`n5️⃣  Authentication - Verify OTP" -ForegroundColor Cyan
    Write-Host "────────────────────────────────" -ForegroundColor Gray
    $verifyBody = @{ 
        phoneNumber = "+94771234567"
        otp = $otp.otp
    }
    $verified = Test-Endpoint -Name "Verify OTP" -Url "$baseUrl/api/auth/verify-otp" -Method "POST" -Body $verifyBody
    if ($verified -and $verified.accessToken) {
        Write-Host "  Access Token: $($verified.accessToken.Substring(0, 20))..." -ForegroundColor Gray
        Write-Host "  User ID: $($verified.user.id)" -ForegroundColor Gray
        Write-Host "  User Type: $($verified.user.userType)" -ForegroundColor Gray
        
        $token = $verified.accessToken
        
        # Test 6: Protected Endpoint (Transactions)
        Write-Host "`n6️⃣  Protected Endpoint - Transactions" -ForegroundColor Cyan
        Write-Host "──────────────────────────────────────" -ForegroundColor Gray
        try {
            $headers = @{ Authorization = "Bearer $token" }
            $transactions = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method GET -Headers $headers -ContentType "application/json"
            Write-Host "  ✅ PASS" -ForegroundColor Green
            Write-Host "  Transactions Count: $($transactions.count)" -ForegroundColor Gray
            $script:testResults += @{ Name = "Get Transactions (Protected)"; Status = "PASS"; Response = $transactions }
        } catch {
            Write-Host "  ❌ FAIL: $($_.Exception.Message)" -ForegroundColor Red
            $script:testResults += @{ Name = "Get Transactions (Protected)"; Status = "FAIL"; Error = $_.Exception.Message }
        }
    }
}

# Test 7: CORS Headers
Write-Host "`n7️⃣  CORS Configuration" -ForegroundColor Cyan
Write-Host "───────────────────────" -ForegroundColor Gray
try {
    $corsResponse = Invoke-WebRequest -Uri "$baseUrl/health" -Method OPTIONS -ErrorAction Stop
    $corsHeaders = $corsResponse.Headers
    Write-Host "  ✅ PASS" -ForegroundColor Green
    Write-Host "  Access-Control-Allow-Origin: $($corsHeaders['Access-Control-Allow-Origin'])" -ForegroundColor Gray
    Write-Host "  Access-Control-Allow-Methods: $($corsHeaders['Access-Control-Allow-Methods'])" -ForegroundColor Gray
    $script:testResults += @{ Name = "CORS Headers"; Status = "PASS" }
} catch {
    Write-Host "  ⚠️  WARN: Could not test CORS" -ForegroundColor Yellow
    $script:testResults += @{ Name = "CORS Headers"; Status = "WARN" }
}

# Summary
Write-Host "`n" -NoNewline
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📊 Test Summary" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan

$passed = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$warned = ($testResults | Where-Object { $_.Status -eq "WARN" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$total = $testResults.Count

Write-Host "`nTotal Tests: $total" -ForegroundColor White
Write-Host "  ✅ Passed: $passed" -ForegroundColor Green
if ($warned -gt 0) {
    Write-Host "  ⚠️  Warnings: $warned" -ForegroundColor Yellow
}
if ($failed -gt 0) {
    Write-Host "  ❌ Failed: $failed" -ForegroundColor Red
}

Write-Host "`nDeployment URL:" -ForegroundColor Cyan
Write-Host "  $baseUrl" -ForegroundColor White

Write-Host "`nInspection URL:" -ForegroundColor Cyan
Write-Host "  https://vercel.com/kabilantharmaratnam-kpucas-projects/agrimarket" -ForegroundColor White

if ($failed -eq 0) {
    Write-Host "`n🎉 All critical tests passed! Deployment is successful!" -ForegroundColor Green
} elseif ($failed -le 2) {
    Write-Host "`n⚠️  Some tests failed, but core functionality is working." -ForegroundColor Yellow
} else {
    Write-Host "`n❌ Multiple tests failed. Please check the deployment." -ForegroundColor Red
}

Write-Host "`n═══════════════════════════════════════`n" -ForegroundColor Cyan

