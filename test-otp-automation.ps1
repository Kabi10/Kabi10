# Comprehensive OTP Verification Testing Script for Agrimarket App
# This script automates the complete OTP verification flow using ADB UI automation

param(
    [string]$DeviceId = "57221FDCQ000D7",
    [string]$TestPhoneNumber = "0771234567",
    [string]$TestOTP = "123456",
    [string]$TestName = "Test User",
    [int]$DelayBetweenSteps = 3
)

# Set up environment
$env:PATH += ";C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools"
$PackageName = "com.senthapps.slagrimarket"
$MainActivity = "$PackageName/.MainActivity"

# Helper functions
function Write-TestStep {
    param([string]$Message)
    Write-Host "`n=== $Message ===" -ForegroundColor Cyan
}

function Wait-ForSeconds {
    param([int]$Seconds)
    Write-Host "Waiting $Seconds seconds..." -ForegroundColor Yellow
    Start-Sleep -Seconds $Seconds
}

function Capture-UIState {
    param([string]$StepName)
    Write-Host "Capturing UI state for: $StepName" -ForegroundColor Green
    adb -s $DeviceId shell uiautomator dump /sdcard/ui_$StepName.xml
    adb -s $DeviceId pull /sdcard/ui_$StepName.xml ./ui_$StepName.xml
    adb -s $DeviceId shell screencap /sdcard/screenshot_$StepName.png
    adb -s $DeviceId pull /sdcard/screenshot_$StepName.png ./screenshot_$StepName.png
}

function Get-ElementCoordinates {
    param([string]$XmlFile, [string]$SearchText, [string]$SearchAttribute = "text")
    
    if (Test-Path $XmlFile) {
        [xml]$uiXml = Get-Content $XmlFile
        $element = $uiXml.SelectSingleNode("//node[@$SearchAttribute='$SearchText']")
        if ($element) {
            $bounds = $element.bounds
            if ($bounds -match 'bounds="(\d+),(\d+)\]\[(\d+),(\d+)"') {
                $x = [int](($matches[1] + $matches[3]) / 2)
                $y = [int](($matches[2] + $matches[4]) / 2)
                return @{ X = $x; Y = $y; Found = $true }
            }
        }
    }
    return @{ Found = $false }
}

function Find-ElementByResourceId {
    param([string]$XmlFile, [string]$ResourceId)
    
    if (Test-Path $XmlFile) {
        [xml]$uiXml = Get-Content $XmlFile
        $element = $uiXml.SelectSingleNode("//node[@resource-id='$ResourceId']")
        if ($element) {
            $bounds = $element.bounds
            if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
                $x = [int](($matches[1] + $matches[3]) / 2)
                $y = [int](($matches[2] + $matches[4]) / 2)
                return @{ X = $x; Y = $y; Found = $true }
            }
        }
    }
    return @{ Found = $false }
}

function Tap-Element {
    param([int]$X, [int]$Y, [string]$Description = "element")
    Write-Host "Tapping ${Description} at coordinates ($X, $Y)" -ForegroundColor Green
    adb -s $DeviceId shell input tap $X $Y
}

function Enter-Text {
    param([string]$Text, [string]$Description = "text")
    Write-Host "Entering ${Description}: $Text" -ForegroundColor Green
    adb -s $DeviceId shell input text "$Text"
}

function Clear-InputField {
    Write-Host "Clearing input field" -ForegroundColor Green
    adb -s $DeviceId shell input keyevent KEYCODE_CTRL_A
    adb -s $DeviceId shell input keyevent KEYCODE_DEL
}

function Start-LogcatMonitoring {
    Write-Host "Starting logcat monitoring for authentication events..." -ForegroundColor Green
    Start-Process -FilePath "adb" -ArgumentList "-s", $DeviceId, "logcat", "-s", "AuthViewModel:D", "JaffnaMarketplaceApplication:D", "AuthRepository:D" -RedirectStandardOutput "logcat_auth.log" -NoNewWindow
}

# Main testing flow
Write-TestStep "Starting Comprehensive OTP Verification Testing"

# Step 1: Setup and app launch
Write-TestStep "Step 1: Setup and App Launch"
Write-Host "Clearing app data to ensure clean state..." -ForegroundColor Yellow
adb -s $DeviceId shell pm clear $PackageName
Wait-ForSeconds 2

Write-Host "Launching Agrimarket app..." -ForegroundColor Yellow
adb -s $DeviceId shell am start -n $MainActivity
Wait-ForSeconds $DelayBetweenSteps

# Start monitoring logs
Start-LogcatMonitoring

# Step 2: Capture initial state and navigate to phone input
Write-TestStep "Step 2: Phone Number Entry Screen"
Capture-UIState "initial_launch"

# Look for phone input field
$phoneInputCoords = Find-ElementByResourceId "ui_initial_launch.xml" "com.senthapps.slagrimarket:id/phoneInput"
if (-not $phoneInputCoords.Found) {
    # Try alternative methods to find phone input
    $phoneInputCoords = Get-ElementCoordinates "ui_initial_launch.xml" "Phone Number" "content-desc"
    if (-not $phoneInputCoords.Found) {
        $phoneInputCoords = Get-ElementCoordinates "ui_initial_launch.xml" "Enter phone number" "hint"
    }
}

if ($phoneInputCoords.Found) {
    Write-Host "Found phone input field" -ForegroundColor Green
    Tap-Element $phoneInputCoords.X $phoneInputCoords.Y "phone input field"
    Wait-ForSeconds 1
    
    Clear-InputField
    Enter-Text $TestPhoneNumber "test phone number"
    Wait-ForSeconds 1
    
    # Dismiss keyboard
    adb -s $DeviceId shell input keyevent KEYCODE_BACK
    Wait-ForSeconds 1
    
    # Find and tap Send OTP button
    Capture-UIState "phone_entered"
    $sendOtpCoords = Get-ElementCoordinates "ui_phone_entered.xml" "Send OTP"
    if (-not $sendOtpCoords.Found) {
        $sendOtpCoords = Get-ElementCoordinates "ui_phone_entered.xml" "அனுப்பு" # Tamil text
    }
    
    if ($sendOtpCoords.Found) {
        Tap-Element $sendOtpCoords.X $sendOtpCoords.Y "Send OTP button"
        Write-Host "OTP request sent!" -ForegroundColor Green
        Wait-ForSeconds $DelayBetweenSteps
    } else {
        Write-Host "Could not find Send OTP button" -ForegroundColor Red
        Capture-UIState "send_otp_not_found"
    }
} else {
    Write-Host "Could not find phone input field" -ForegroundColor Red
    Capture-UIState "phone_input_not_found"
}

# Step 3: OTP Verification Screen
Write-TestStep "Step 3: OTP Verification Screen"
Wait-ForSeconds 2
Capture-UIState "otp_screen"

# Find OTP input field
$otpInputCoords = Find-ElementByResourceId "ui_otp_screen.xml" "com.senthapps.slagrimarket:id/otpInput"
if (-not $otpInputCoords.Found) {
    $otpInputCoords = Get-ElementCoordinates "ui_otp_screen.xml" "Enter OTP" "hint"
    if (-not $otpInputCoords.Found) {
        $otpInputCoords = Get-ElementCoordinates "ui_otp_screen.xml" "OTP" "content-desc"
    }
}

if ($otpInputCoords.Found) {
    Write-Host "Found OTP input field" -ForegroundColor Green
    Tap-Element $otpInputCoords.X $otpInputCoords.Y "OTP input field"
    Wait-ForSeconds 1
    
    Clear-InputField
    Enter-Text $TestOTP "OTP code"
    Wait-ForSeconds 1
    
    # Find and fill name field
    $nameInputCoords = Find-ElementByResourceId "ui_otp_screen.xml" "com.senthapps.slagrimarket:id/nameInput"
    if (-not $nameInputCoords.Found) {
        $nameInputCoords = Get-ElementCoordinates "ui_otp_screen.xml" "Enter your name" "hint"
    }
    
    if ($nameInputCoords.Found) {
        Write-Host "Found name input field" -ForegroundColor Green
        Tap-Element $nameInputCoords.X $nameInputCoords.Y "name input field"
        Wait-ForSeconds 1
        
        Clear-InputField
        Enter-Text $TestName "user name"
        Wait-ForSeconds 1
    }
    
    # Dismiss keyboard before tapping verify
    adb -s $DeviceId shell input keyevent KEYCODE_BACK
    Wait-ForSeconds 1
    
    # Capture state before verification
    Capture-UIState "before_verify"
    
    # Find and tap Verify button
    $verifyCoords = Get-ElementCoordinates "ui_before_verify.xml" "Verify"
    if (-not $verifyCoords.Found) {
        $verifyCoords = Get-ElementCoordinates "ui_before_verify.xml" "சரிபார்க்கவும்" # Tamil text
    }
    
    if ($verifyCoords.Found) {
        Write-Host "Tapping Verify button..." -ForegroundColor Green
        Tap-Element $verifyCoords.X $verifyCoords.Y "Verify button"
        Wait-ForSeconds $DelayBetweenSteps
        
        # Monitor verification process
        Write-Host "Monitoring verification process..." -ForegroundColor Yellow
        for ($i = 1; $i -le 10; $i++) {
            Wait-ForSeconds 1
            Capture-UIState "verify_step_$i"
            
            # Check if we've navigated to home screen or if there's an error
            if (Test-Path "ui_verify_step_$i.xml") {
                [xml]$currentUI = Get-Content "ui_verify_step_$i.xml"
                $errorElement = $currentUI.SelectSingleNode("//node[contains(@text,'error') or contains(@text,'Error') or contains(@text,'Invalid')]")
                $homeElement = $currentUI.SelectSingleNode("//node[contains(@text,'Home') or contains(@text,'வீடு')]")
                
                if ($errorElement) {
                    Write-Host "Error detected: $($errorElement.text)" -ForegroundColor Red
                    break
                }
                if ($homeElement) {
                    Write-Host "Successfully navigated to home screen!" -ForegroundColor Green
                    break
                }
            }
        }
    } else {
        Write-Host "Could not find Verify button" -ForegroundColor Red
        Capture-UIState "verify_button_not_found"
    }
} else {
    Write-Host "Could not find OTP input field" -ForegroundColor Red
    Capture-UIState "otp_input_not_found"
}

# Step 4: Final state verification
Write-TestStep "Step 4: Final State Verification"
Capture-UIState "final_state"

# Stop logcat monitoring
Stop-Process -Name "adb" -ErrorAction SilentlyContinue

Write-TestStep "Testing Complete"
Write-Host "Check the generated UI dumps and screenshots for detailed analysis" -ForegroundColor Green
Write-Host "Logcat output saved to: logcat_auth.log" -ForegroundColor Green

# Generate summary report
$reportContent = @"
# OTP Verification Test Report
Generated: $(Get-Date)

## Test Parameters
- Device ID: $DeviceId
- Test Phone: $TestPhoneNumber
- Test OTP: $TestOTP
- Test Name: $TestName

## Generated Files
- UI Dumps: ui_*.xml
- Screenshots: screenshot_*.png
- Auth Logs: logcat_auth.log

## Next Steps
1. Review UI dumps for element identification accuracy
2. Check screenshots for visual verification
3. Analyze logcat for ViewModel state changes
4. Verify authentication persistence
"@

$reportContent | Out-File -FilePath "test_report.md" -Encoding UTF8
Write-Host "Test report generated: test_report.md" -ForegroundColor Green
