#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Test Authentication Bypass Fix for Agrimarket MVP
.DESCRIPTION
    Verifies that the app launches directly to the home screen without authentication prompts
    after the AuthRepository fix that initializes mock authentication state.
.NOTES
    Author: Augment Agent
    Date: 2025-01-29
    Purpose: Verify authentication bypass functionality
#>

# Function to execute ADB commands with error handling
function Invoke-AdbCommand {
    param([string]$Command)
    try {
        $result = Invoke-Expression "adb $Command" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "⚠️ ADB command failed: adb $Command" -ForegroundColor Yellow
            Write-Host "Error: $result" -ForegroundColor Red
            return $null
        }
        return $result
    }
    catch {
        Write-Host "❌ ADB command error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Function to wait for UI to stabilize
function Wait-ForUI {
    param([int]$Seconds = 3)
    Write-Host "⏳ Waiting $Seconds seconds for UI to stabilize..." -ForegroundColor Gray
    Start-Sleep -Seconds $Seconds
}

# Function to capture UI state
function Get-UIState {
    param([string]$OutputFile)
    $null = Invoke-AdbCommand "shell uiautomator dump /sdcard/$OutputFile"
    $null = Invoke-AdbCommand "pull /sdcard/$OutputFile ./$OutputFile"
    
    if (Test-Path $OutputFile) {
        return Get-Content $OutputFile -Raw
    }
    return $null
}

# Function to check if we're on home screen
function Test-HomeScreen {
    param([string]$UIContent)
    # Look for Tamil text, home screen indicators, or demo user content
    return $UIContent -match "யாழ்ப்பாணம்|Demo User|சந்தை விலைகள்|Market Prices|home|dashboard"
}

# Function to check for authentication screens
function Test-AuthScreen {
    param([string]$UIContent)
    # Look for phone input, OTP, or authentication prompts
    return $UIContent -match "phone.*number|தொலைபேசி.*எண்|OTP|verification|send.*code"
}

Write-Host "🧪 Testing Authentication Bypass Fix" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

try {
    # Step 1: Check ADB connection
    Write-Host "`n📱 Step 1: Checking ADB connection..." -ForegroundColor Blue
    $devices = Invoke-AdbCommand "devices"
    if ($devices -match "device$") {
        Write-Host "✅ ADB device connected" -ForegroundColor Green
    } else {
        Write-Host "❌ No ADB device found. Please connect a device or start emulator." -ForegroundColor Red
        exit 1
    }

    # Step 2: Clear app data to ensure fresh start
    Write-Host "`n🧹 Step 2: Clearing app data for fresh test..." -ForegroundColor Blue
    $null = Invoke-AdbCommand "shell pm clear com.senthapps.slagrimarket"
    Wait-ForUI -Seconds 2

    # Step 3: Launch app
    Write-Host "`n🚀 Step 3: Launching Agrimarket app..." -ForegroundColor Blue
    $null = Invoke-AdbCommand "shell am start -n com.senthapps.slagrimarket/.MainActivity"
    Wait-ForUI -Seconds 5

    # Step 4: Capture and analyze initial state
    Write-Host "`n🔍 Step 4: Analyzing app state..." -ForegroundColor Blue
    $uiContent = Get-UIState -OutputFile "ui_auth_bypass_test.xml"
    
    if ($null -eq $uiContent) {
        Write-Host "❌ Failed to capture UI state" -ForegroundColor Red
        exit 1
    }

    # Step 5: Check results
    Write-Host "`n📊 Step 5: Test Results" -ForegroundColor Blue
    Write-Host "======================" -ForegroundColor Blue

    if (Test-HomeScreen -UIContent $uiContent) {
        Write-Host "✅ SUCCESS: App launched directly to home screen!" -ForegroundColor Green
        Write-Host "✅ Authentication bypass is working correctly" -ForegroundColor Green
        
        # Check for specific MVP elements
        if ($uiContent -match "Demo User") {
            Write-Host "✅ Demo User detected in UI" -ForegroundColor Green
        }
        if ($uiContent -match "சந்தை விலைகள்|Market Prices") {
            Write-Host "✅ Market Prices section detected" -ForegroundColor Green
        }
        
        Write-Host "`n🎉 Authentication bypass fix is successful!" -ForegroundColor Green
        
    } elseif (Test-AuthScreen -UIContent $uiContent) {
        Write-Host "❌ FAILED: App is showing authentication screen" -ForegroundColor Red
        Write-Host "❌ Authentication bypass is not working" -ForegroundColor Red
        
        # Capture screenshot for debugging
        $null = Invoke-AdbCommand "shell screencap -p /sdcard/auth_bypass_failed.png"
        $null = Invoke-AdbCommand "pull /sdcard/auth_bypass_failed.png ./auth_bypass_failed.png"
        Write-Host "📸 Screenshot saved as auth_bypass_failed.png" -ForegroundColor Yellow
        
    } else {
        Write-Host "❓ UNKNOWN: App is in unexpected state" -ForegroundColor Yellow
        Write-Host "UI Content Preview (first 300 chars):" -ForegroundColor Gray
        Write-Host $uiContent.Substring(0, [Math]::Min(300, $uiContent.Length)) -ForegroundColor Gray
    }

    # Step 6: Cleanup
    Write-Host "`n🧹 Step 6: Cleaning up test files..." -ForegroundColor Blue
    if (Test-Path "ui_auth_bypass_test.xml") {
        Remove-Item "ui_auth_bypass_test.xml" -Force
    }

} catch {
    Write-Host "❌ Test failed with error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Authentication bypass test completed!" -ForegroundColor Cyan
