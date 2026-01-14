# E2E Mock Test Script for Agrimarket Backend
# Runs automated tests against MOCK_DB mode
# Usage: powershell -ExecutionPolicy Bypass -File scripts/e2e-mock.ps1

$ErrorActionPreference = "Stop"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Agrimarket Backend E2E Tests (MOCK_DB)   " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$env:SMS_MODE = "mock"
$env:MOCK_DB = "true"
$env:NODE_ENV = "development"
$env:JWT_SECRET = "test-secret-key"
$env:JWT_REFRESH_SECRET = "test-refresh-secret"

$BaseUrl = "http://localhost:3000"
$TestPhone = "+94771234567"
$PassCount = 0
$FailCount = 0

function Write-TestResult($name, $passed, $details = "") {
    if ($passed) {
        Write-Host "[PASS] $name" -ForegroundColor Green
        $script:PassCount++
    } else {
        Write-Host "[FAIL] $name" -ForegroundColor Red
        if ($details) { Write-Host "       $details" -ForegroundColor Yellow }
        $script:FailCount++
    }
}

# Start backend server
Write-Host "Starting backend server..." -ForegroundColor Yellow
$serverProcess = Start-Process -FilePath "node" -ArgumentList "src/server.js" -WorkingDirectory $PSScriptRoot\.. -PassThru -WindowStyle Hidden

# Wait for server to be ready
$maxWait = 30
$waited = 0
while ($waited -lt $maxWait) {
    try {
        $health = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get -ErrorAction SilentlyContinue
        if ($health.status -eq "healthy") {
            Write-Host "Server ready!" -ForegroundColor Green
            break
        }
    } catch { }
    Start-Sleep -Seconds 1
    $waited++
}

if ($waited -ge $maxWait) {
    Write-Host "Server failed to start within $maxWait seconds" -ForegroundColor Red
    if ($serverProcess) { Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue }
    exit 1
}

Write-Host ""
Write-Host "Running E2E Tests..." -ForegroundColor Cyan
Write-Host ""

try {
    # Test 1: Send OTP
    Write-Host "Step 1: POST /api/v1/auth/send-otp"
    $sendOtpBody = @{ phoneNumber = $TestPhone } | ConvertTo-Json
    $sendOtpResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/send-otp" -Method Post -Body $sendOtpBody -ContentType "application/json"

    $otpPassed = $sendOtpResponse.success -and $sendOtpResponse.otp -and $sendOtpResponse.otpId
    Write-TestResult "send-otp" $otpPassed

    if (-not $otpPassed) { throw "send-otp failed" }
    $otp = $sendOtpResponse.otp
    $otpId = $sendOtpResponse.otpId
    Write-Host "       OTP: $otp" -ForegroundColor Gray

    # Test 2: Verify OTP
    Write-Host "Step 2: POST /api/v1/auth/verify-otp"
    $verifyBody = @{ phoneNumber = $TestPhone; otp = $otp; otpId = $otpId } | ConvertTo-Json
    $verifyResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json"

    $verifyPassed = $verifyResponse.success -and $verifyResponse.token -and $verifyResponse.user
    Write-TestResult "verify-otp" $verifyPassed

    if (-not $verifyPassed) { throw "verify-otp failed" }
    $token = $verifyResponse.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Host "       User: $($verifyResponse.user.id)" -ForegroundColor Gray

    # Test 3: GET Listings
    Write-Host "Step 3: GET /api/v1/listings"
    $listingsResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/listings" -Method Get -Headers $headers

    $listingsPassed = $listingsResponse.success -and $null -ne $listingsResponse.listings
    Write-TestResult "get-listings" $listingsPassed

    # Test 4: Create Listing
    Write-Host "Step 4: POST /api/v1/listings"
    $createListingBody = @{
        cropType = "tomato"
        quantity = 100
        unit = "kg"
        pricePerUnit = 150
        location = "Jaffna"
    } | ConvertTo-Json
    $createResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/listings" -Method Post -Body $createListingBody -ContentType "application/json" -Headers $headers

    $createPassed = $createResponse.success -and $createResponse.data.id
    Write-TestResult "create-listing" $createPassed
    if ($createPassed) {
        Write-Host "       Listing ID: $($createResponse.data.id)" -ForegroundColor Gray
    }

    # Test 5: GET Transactions
    Write-Host "Step 5: GET /api/v1/transactions"
    $txResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/transactions" -Method Get -Headers $headers

    $txPassed = $txResponse.success -and $null -ne $txResponse.transactions
    Write-TestResult "get-transactions" $txPassed

} catch {
    Write-Host "Error: $_" -ForegroundColor Red
} finally {
    # Cleanup: Stop server
    Write-Host ""
    Write-Host "Stopping server..." -ForegroundColor Yellow
    if ($serverProcess) {
        Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    }
    # Also kill any orphan node processes on port 3000
    Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object { $_.Id -ne $PID } | Stop-Process -Force -ErrorAction SilentlyContinue
}

# Summary
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Results: $PassCount passed, $FailCount failed" -ForegroundColor $(if ($FailCount -eq 0) { "Green" } else { "Red" })
Write-Host "============================================" -ForegroundColor Cyan

if ($FailCount -gt 0) { exit 1 }
exit 0
