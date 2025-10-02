# Complete Authentication Bypass Test Script
# This script tests the complete authentication bypass functionality

param(
    [string]$DeviceId = "57221FDCQ000D7",
    [string]$AdbPath = "C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

Write-Host "🚀 Starting Complete Authentication Bypass Test" -ForegroundColor Green
Write-Host "Device ID: $DeviceId" -ForegroundColor Cyan

# Function to execute ADB command
function Invoke-AdbCommand {
    param([string]$Command)
    Write-Host "Executing: $Command" -ForegroundColor Yellow
    $result = & $AdbPath -s $DeviceId $Command.Split(' ')
    return $result
}

# Function to wait for UI to update
function Wait-ForUI {
    param([int]$Seconds = 3)
    Write-Host "Waiting $Seconds seconds for UI to update..." -ForegroundColor Gray
    Start-Sleep -Seconds $Seconds
}

# Function to capture and analyze UI
function Get-UIState {
    param([string]$OutputFile)
    Invoke-AdbCommand "shell uiautomator dump /sdcard/$OutputFile"
    Invoke-AdbCommand "pull /sdcard/$OutputFile ./$OutputFile"
    
    if (Test-Path $OutputFile) {
        $content = Get-Content $OutputFile -Raw
        return $content
    }
    return $null
}

# Function to check if we're on home screen
function Test-HomeScreen {
    param([string]$UIContent)
    return $UIContent -match "browse|listing|farmer|buyer|fab|create|search|home|dashboard"
}

# Function to check if we're on phone input screen
function Test-PhoneInputScreen {
    param([string]$UIContent)
    return $UIContent -match "phone.*number|தொலைபேசி.*எண்|send.*otp"
}

try {
    # Step 1: Force stop and restart app
    Write-Host "`n📱 Step 1: Restarting application..." -ForegroundColor Blue
    Invoke-AdbCommand "shell am force-stop com.senthapps.slagrimarket"
    Wait-ForUI -Seconds 2
    Invoke-AdbCommand "shell am start -n com.senthapps.slagrimarket/.MainActivity"
    Wait-ForUI -Seconds 5

    # Step 2: Check initial state
    Write-Host "`n🔍 Step 2: Checking initial application state..." -ForegroundColor Blue
    $initialUI = Get-UIState -OutputFile "ui_initial_state.xml"
    
    if (Test-HomeScreen -UIContent $initialUI) {
        Write-Host "✅ SUCCESS: App already authenticated - on home screen!" -ForegroundColor Green
        exit 0
    }
    
    if (-not (Test-PhoneInputScreen -UIContent $initialUI)) {
        Write-Host "❌ ERROR: App not on expected phone input screen" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "📱 App is on phone input screen - proceeding with bypass test" -ForegroundColor Cyan

    # Step 3: Clear any existing phone number and enter test number
    Write-Host "`n📞 Step 3: Entering phone number..." -ForegroundColor Blue
    
    # Tap on phone input field
    Invoke-AdbCommand "shell input tap 540 1290"
    Wait-ForUI -Seconds 1
    
    # Clear existing text and enter phone number
    Invoke-AdbCommand "shell input keyevent KEYCODE_CTRL_A"
    Invoke-AdbCommand "shell input keyevent KEYCODE_DEL"
    Invoke-AdbCommand "shell input text 0771234567"
    Wait-ForUI -Seconds 2

    # Step 4: Trigger authentication bypass
    Write-Host "`n🔐 Step 4: Triggering authentication bypass..." -ForegroundColor Blue
    
    # Tap Send OTP button to trigger bypass
    Invoke-AdbCommand "shell input tap 540 1549"
    Write-Host "Tapped Send OTP button - waiting for bypass to activate..." -ForegroundColor Yellow
    
    # Wait longer for authentication to complete
    Wait-ForUI -Seconds 8

    # Step 5: Check if bypass worked
    Write-Host "`n✅ Step 5: Verifying authentication bypass..." -ForegroundColor Blue
    $finalUI = Get-UIState -OutputFile "ui_final_bypass_state.xml"

    if (Test-HomeScreen -UIContent $finalUI) {
        Write-Host "🎉 SUCCESS: Authentication bypass worked! App is now on home screen." -ForegroundColor Green
        Write-Host "✅ Complete authentication bypass test PASSED" -ForegroundColor Green

        # Capture screenshot for documentation
        Invoke-AdbCommand "shell screencap -p /sdcard/screenshot_home_success.png"
        Invoke-AdbCommand "pull /sdcard/screenshot_home_success.png ./screenshot_home_success.png"
    }
    elseif (Test-PhoneInputScreen -UIContent $finalUI) {
        Write-Host "❌ FAILED: Still on phone input screen - bypass did not work" -ForegroundColor Red

        # Check for error messages
        if ($finalUI -match "failed.*send.*otp|error") {
            Write-Host "🔍 Found error message in UI - bypass logic may not be executing" -ForegroundColor Yellow
        }

        # Capture screenshot for debugging
        Invoke-AdbCommand "shell screencap -p /sdcard/screenshot_bypass_failed.png"
        Invoke-AdbCommand "pull /sdcard/screenshot_bypass_failed.png ./screenshot_bypass_failed.png"
    }
    else {
        Write-Host "❓ UNKNOWN: App is in unexpected state" -ForegroundColor Yellow
        Write-Host "UI Content Preview:" -ForegroundColor Gray
        Write-Host ($finalUI.Substring(0, [Math]::Min(500, $finalUI.Length))) -ForegroundColor Gray
    }

} catch {
    Write-Host "❌ ERROR: Test execution failed: $($_.Exception.Message)" -ForegroundColor Red
    return $false
} finally {
    Write-Host "`n📊 Test completed. Check UI dumps and screenshots for details." -ForegroundColor Cyan
}
