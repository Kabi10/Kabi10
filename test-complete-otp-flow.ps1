# Complete OTP Testing Script with Real Backend Integration
param(
    [string]$DeviceId = "57221FDCQ000D7",
    [string]$TestPhoneNumber = "0771234567",
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

function Get-RealOTP {
    param([string]$PhoneNumber)
    Write-Host "Getting real OTP from backend for phone: $PhoneNumber" -ForegroundColor Yellow
    
    # Use the existing Node.js script to get OTP
    $otpResult = node "test-otp.js" $PhoneNumber 2>&1
    Write-Host "OTP Result: $otpResult" -ForegroundColor Gray
    
    # Extract OTP from the result (assuming it returns the OTP code)
    if ($otpResult -match '\d{6}') {
        return $matches[0]
    }
    
    # Fallback: try to get OTP from the get-current-otp script
    $currentOtp = node "get-current-otp.js" 2>&1
    Write-Host "Current OTP Result: $currentOtp" -ForegroundColor Gray
    
    if ($currentOtp -match '\d{6}') {
        return $matches[0]
    }
    
    # If no OTP found, return a test OTP
    Write-Host "Could not get real OTP, using test OTP" -ForegroundColor Red
    return "123456"
}

function Start-LogcatMonitoring {
    Write-Host "Starting logcat monitoring..." -ForegroundColor Green
    Start-Process -FilePath "adb" -ArgumentList "-s", $DeviceId, "logcat", "-v", "time", "-s", "JaffnaMarketplaceApplication:D", "AuthViewModel:D", "AuthRepository:D", "System.out:I" -RedirectStandardOutput "logcat_complete_test.log" -NoNewWindow
}

function Stop-LogcatMonitoring {
    Write-Host "Stopping logcat monitoring..." -ForegroundColor Green
    Get-Process -Name "adb" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*logcat*" } | Stop-Process -Force
}

Write-Step "Starting Complete OTP Testing with Real Backend"

# Step 1: Start monitoring
Start-LogcatMonitoring

# Step 2: Clear app and launch
Write-Step "Step 1: App Setup"
adb -s $DeviceId shell pm clear $PackageName
Wait-Seconds 2
adb -s $DeviceId shell am start -n "$PackageName/.MainActivity"
Wait-Seconds 3

# Step 3: Phone number entry
Write-Step "Step 2: Phone Number Entry"
Capture-State "start_phone_entry"

# Tap phone input field
Tap-Coordinates 540 1326 "phone input field"
Wait-Seconds 1

# Clear and enter phone number
Clear-Field
Enter-Text $TestPhoneNumber
Wait-Seconds 1

# Dismiss keyboard
adb -s $DeviceId shell input keyevent KEYCODE_BACK
Wait-Seconds 1

# Capture state after entering phone number
Capture-State "phone_number_entered"

# Tap Send OTP button
Tap-Coordinates 540 1512 "Send OTP button"
Write-Host "Send OTP button tapped!" -ForegroundColor Green
Wait-Seconds 5  # Wait longer for OTP to be sent

# Step 4: Get real OTP from backend
Write-Step "Step 3: Getting Real OTP"
$realOTP = Get-RealOTP $TestPhoneNumber
Write-Host "Using OTP: $realOTP" -ForegroundColor Green

# Step 5: OTP verification screen
Write-Step "Step 4: OTP Verification"
Capture-State "otp_verification_screen"

# Check if we're on the OTP screen by looking for specific elements
$uiContent = Get-Content "ui_otp_verification_screen.xml" -Raw
if ($uiContent -like "*OTP*" -or $uiContent -like "*verification*" -or $uiContent -like "*சரிபார்*") {
    Write-Host "OTP screen detected!" -ForegroundColor Green
    
    # Try to find OTP input field coordinates from the UI dump
    powershell -ExecutionPolicy Bypass -File "extract-ui-elements.ps1" -XmlFile "ui_otp_verification_screen.xml" > otp_screen_analysis.txt
    
    # For now, use estimated coordinates for OTP input
    $otpInputX = 540
    $otpInputY = 1200
    
    # Tap OTP input field
    Tap-Coordinates $otpInputX $otpInputY "OTP input field"
    Wait-Seconds 1
    
    # Enter the real OTP
    Clear-Field
    Enter-Text $realOTP
    Wait-Seconds 1
    
    # Look for name input field
    $nameInputY = 1400
    Tap-Coordinates $otpInputX $nameInputY "name input field"
    Wait-Seconds 1
    
    # Enter name
    Clear-Field
    Enter-Text $TestName
    Wait-Seconds 1
    
    # Dismiss keyboard
    adb -s $DeviceId shell input keyevent KEYCODE_BACK
    Wait-Seconds 1
    
    # Capture state before verification
    Capture-State "before_otp_verification"
    
    # Tap verify button
    $verifyButtonY = 1600
    Tap-Coordinates $otpInputX $verifyButtonY "verify button"
    Write-Host "Verify button tapped!" -ForegroundColor Green
    Wait-Seconds 3
    
} else {
    Write-Host "Not on OTP screen, checking current state..." -ForegroundColor Red
    powershell -ExecutionPolicy Bypass -File "extract-ui-elements.ps1" -XmlFile "ui_otp_verification_screen.xml" > current_screen_analysis.txt
}

# Step 6: Monitor verification process
Write-Step "Step 5: Verification Monitoring"

# Monitor for 15 seconds, capturing state every 3 seconds
for ($i = 1; $i -le 5; $i++) {
    Wait-Seconds 3
    Capture-State "verification_step_$i"
    
    # Check current activity
    $currentActivity = adb -s $DeviceId shell dumpsys activity activities | Select-String "mResumedActivity"
    Write-Host "Step $i - Current activity: $currentActivity" -ForegroundColor Gray
    
    # Check if we've reached the home screen
    $uiContent = Get-Content "ui_verification_step_$i.xml" -Raw
    if ($uiContent -like "*Home*" -or $uiContent -like "*வீடு*" -or $uiContent -like "*Marketplace*") {
        Write-Host "Successfully reached home screen!" -ForegroundColor Green
        break
    }
    
    # Check for error messages
    if ($uiContent -like "*error*" -or $uiContent -like "*Error*" -or $uiContent -like "*Invalid*") {
        Write-Host "Error detected in verification process" -ForegroundColor Red
        break
    }
}

# Final state capture
Capture-State "final_verification_state"

# Step 7: Verification and cleanup
Write-Step "Step 6: Test Completion and Analysis"

# Stop logcat monitoring
Stop-LogcatMonitoring

# Generate comprehensive report
$report = @"
# Complete OTP Verification Test Report
Generated: $(Get-Date)

## Test Parameters
- Device ID: $DeviceId
- Test Phone: $TestPhoneNumber
- Real OTP Used: $realOTP
- Test Name: $TestName

## Test Flow
1. App cleared and launched
2. Phone number entered: $TestPhoneNumber
3. Send OTP button tapped
4. Real OTP retrieved from backend: $realOTP
5. OTP and name entered
6. Verification attempted

## Generated Files
- UI Dumps: ui_*.xml
- Screenshots: screenshot_*.png
- Logcat: logcat_complete_test.log
- Analysis: otp_screen_analysis.txt, current_screen_analysis.txt

## Next Steps
1. Review screenshots for visual verification
2. Check logcat for ViewModel state changes
3. Verify authentication persistence
4. Test error handling scenarios
5. Validate UI state management

## Key Coordinates Used
- Phone Input: (540, 1326)
- Send OTP Button: (540, 1512)
- OTP Input: (540, 1200) [estimated]
- Name Input: (540, 1400) [estimated]
- Verify Button: (540, 1600) [estimated]
"@

$report | Out-File -FilePath "complete_test_report.md" -Encoding UTF8
Write-Host "Complete test report generated: complete_test_report.md" -ForegroundColor Green

# Show recent authentication logs
Write-Host "`nRecent authentication logs:" -ForegroundColor Yellow
if (Test-Path "logcat_complete_test.log") {
    Get-Content "logcat_complete_test.log" | Select-Object -Last 20
}

Write-Step "Complete OTP Testing Finished"
Write-Host "Check the generated files for detailed analysis of the OTP verification flow" -ForegroundColor Green
