# Precise OTP Testing Script with Exact Coordinates
param(
    [string]$DeviceId = "57221FDCQ000D7",
    [string]$TestPhoneNumber = "0771234567",
    [string]$TestOTP = "123456",
    [string]$TestName = "Test User"
)

# Set up environment
$env:PATH += ";C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools"
$PackageName = "com.senthapps.slagrimarket"

function Write-Step {
    param([string]$Message)
    Write-Host "`n=== $Message ===" -ForegroundColor Cyan
}

function Wait-Seconds {
    param([int]$Seconds)
    Write-Host "Waiting $Seconds seconds..." -ForegroundColor Yellow
    Start-Sleep -Seconds $Seconds
}

function Capture-State {
    param([string]$StepName)
    Write-Host "Capturing state: $StepName" -ForegroundColor Green
    adb -s $DeviceId shell uiautomator dump /sdcard/ui_$StepName.xml
    adb -s $DeviceId pull /sdcard/ui_$StepName.xml ./ui_$StepName.xml
    adb -s $DeviceId shell screencap /sdcard/screenshot_$StepName.png
    adb -s $DeviceId pull /sdcard/screenshot_$StepName.png ./screenshot_$StepName.png
}

function Tap-Coordinates {
    param([int]$X, [int]$Y, [string]$Description)
    Write-Host "Tapping ${Description} at ($X, $Y)" -ForegroundColor Green
    adb -s $DeviceId shell input tap $X $Y
}

function Enter-Text {
    param([string]$Text)
    Write-Host "Entering text: $Text" -ForegroundColor Green
    adb -s $DeviceId shell input text "$Text"
}

function Clear-Field {
    Write-Host "Clearing input field" -ForegroundColor Green
    adb -s $DeviceId shell input keyevent KEYCODE_CTRL_A
    adb -s $DeviceId shell input keyevent KEYCODE_DEL
}

Write-Step "Starting Precise OTP Testing"

# Step 1: Clear app and launch
Write-Step "Step 1: App Setup"
adb -s $DeviceId shell pm clear $PackageName
Wait-Seconds 2
adb -s $DeviceId shell am start -n "$PackageName/.MainActivity"
Wait-Seconds 3

# Step 2: Phone number entry
Write-Step "Step 2: Phone Number Entry"
Capture-State "phone_screen"

# Tap phone input field (coordinates from analysis)
Tap-Coordinates 540 1326 "phone input field"
Wait-Seconds 1

# Clear any existing text and enter phone number
Clear-Field
Enter-Text $TestPhoneNumber
Wait-Seconds 1

# Dismiss keyboard
adb -s $DeviceId shell input keyevent KEYCODE_BACK
Wait-Seconds 1

# Capture state after entering phone number
Capture-State "phone_entered"

# Tap Send OTP button (coordinates from analysis)
Tap-Coordinates 540 1512 "Send OTP button"
Write-Host "Send OTP button tapped!" -ForegroundColor Green
Wait-Seconds 3

# Step 3: OTP verification screen
Write-Step "Step 3: OTP Verification"
Capture-State "otp_screen"

# Extract coordinates for OTP screen elements
powershell -ExecutionPolicy Bypass -File "extract-ui-elements.ps1" -XmlFile "ui_otp_screen.xml" > otp_analysis.txt

# Read the analysis to find OTP input coordinates
$otpAnalysis = Get-Content "otp_analysis.txt" -Raw

# Look for OTP input field - typically an EditText on OTP screen
# For now, let's try common OTP input locations
$otpInputX = 540  # Center X is usually consistent
$otpInputY = 1200  # Approximate Y for OTP input

Write-Host "Attempting to tap OTP input field..." -ForegroundColor Yellow
Tap-Coordinates $otpInputX $otpInputY "OTP input field"
Wait-Seconds 1

# Enter OTP
Enter-Text $TestOTP
Wait-Seconds 1

# Look for name input field (usually below OTP)
$nameInputY = 1400  # Approximate Y for name input
Write-Host "Attempting to tap name input field..." -ForegroundColor Yellow
Tap-Coordinates $otpInputX $nameInputY "name input field"
Wait-Seconds 1

# Enter name
Enter-Text $TestName
Wait-Seconds 1

# Dismiss keyboard
adb -s $DeviceId shell input keyevent KEYCODE_BACK
Wait-Seconds 1

# Capture state before verification
Capture-State "before_verify"

# Look for verify button (usually at bottom)
$verifyButtonY = 1600  # Approximate Y for verify button
Write-Host "Attempting to tap verify button..." -ForegroundColor Yellow
Tap-Coordinates $otpInputX $verifyButtonY "verify button"
Wait-Seconds 2

# Step 4: Monitor verification process
Write-Step "Step 4: Verification Monitoring"

# Monitor for 10 seconds, capturing state every 2 seconds
for ($i = 1; $i -le 5; $i++) {
    Wait-Seconds 2
    Capture-State "verify_step_$i"
    
    # Check current activity
    $currentActivity = adb -s $DeviceId shell dumpsys activity activities | Select-String "mResumedActivity"
    Write-Host "Current activity: $currentActivity" -ForegroundColor Gray
}

# Final state capture
Capture-State "final_state"

Write-Step "Testing Complete"

# Generate summary
$summary = @"
# OTP Testing Summary
Generated: $(Get-Date)

## Test Parameters
- Device: $DeviceId
- Phone: $TestPhoneNumber
- OTP: $TestOTP
- Name: $TestName

## Key Coordinates Used
- Phone Input: (540, 1326)
- Send OTP Button: (540, 1512)
- OTP Input: (540, 1200) [estimated]
- Name Input: (540, 1400) [estimated]
- Verify Button: (540, 1600) [estimated]

## Generated Files
- UI Dumps: ui_*.xml
- Screenshots: screenshot_*.png
- OTP Analysis: otp_analysis.txt

## Next Steps
1. Check screenshots for visual verification
2. Analyze UI dumps for exact OTP screen coordinates
3. Verify authentication state in app
4. Test error handling scenarios
"@

$summary | Out-File -FilePath "test_summary.md" -Encoding UTF8
Write-Host "Test summary saved to: test_summary.md" -ForegroundColor Green

# Show final logcat for authentication events
Write-Host "`nRecent authentication logs:" -ForegroundColor Yellow
adb -s $DeviceId logcat -d | Select-String "Auth|OTP|slagrimarket" | Select-Object -Last 10
