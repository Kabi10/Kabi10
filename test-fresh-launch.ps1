# Test Fresh Launch of Agrimarket App
# This script launches the app and captures the current UI state

Write-Host "=== Testing Fresh Launch of Agrimarket App ===" -ForegroundColor Green

# Function to run ADB commands with proper path handling
function Invoke-AdbCommand {
    param([string]$Command)
    try {
        # Try different ADB paths
        $adbPaths = @(
            "adb",
            "$env:ANDROID_HOME\platform-tools\adb.exe",
            "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
            "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"
        )
        
        foreach ($adbPath in $adbPaths) {
            try {
                if (Test-Path $adbPath -ErrorAction SilentlyContinue) {
                    $result = & $adbPath $Command.Split(' ')
                    return $result
                } elseif ($adbPath -eq "adb") {
                    $result = & adb $Command.Split(' ')
                    return $result
                }
            } catch {
                continue
            }
        }
        throw "ADB not found in any expected location"
    } catch {
        Write-Host "Error running ADB command: $Command" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Step 1: Force stop any existing instance
Write-Host "`n📱 Step 1: Force stopping existing app instance..." -ForegroundColor Blue
Invoke-AdbCommand "shell am force-stop com.senthapps.slagrimarket"
Start-Sleep -Seconds 2

# Step 2: Clear app data for fresh start
Write-Host "`n📱 Step 2: Clearing app data..." -ForegroundColor Blue
Invoke-AdbCommand "shell pm clear com.senthapps.slagrimarket"
Start-Sleep -Seconds 2

# Step 3: Launch the app
Write-Host "`n📱 Step 3: Launching Agrimarket app..." -ForegroundColor Blue
$launchResult = Invoke-AdbCommand "shell am start -n com.senthapps.slagrimarket/.MainActivity"
Write-Host "Launch result: $launchResult" -ForegroundColor Gray
Start-Sleep -Seconds 5

# Step 4: Capture UI state
Write-Host "`n📱 Step 4: Capturing UI state..." -ForegroundColor Blue
Invoke-AdbCommand "shell uiautomator dump /sdcard/ui_fresh_launch.xml"
Invoke-AdbCommand "pull /sdcard/ui_fresh_launch.xml ./ui_fresh_launch.xml"

if (Test-Path "./ui_fresh_launch.xml") {
    Write-Host "✅ UI dump captured successfully!" -ForegroundColor Green
    
    # Step 5: Analyze the UI content
    Write-Host "`n📱 Step 5: Analyzing UI content..." -ForegroundColor Blue
    $uiContent = Get-Content "./ui_fresh_launch.xml" -Raw
    
    # Check for authentication screens
    $hasAuthScreens = $uiContent -match "சரிபார்ப்பு குறியீட்டை உள்ளிடவும்|Enter verification code|phone.*number|OTP|verification"
    
    # Check for home screen
    $hasHomeScreen = $uiContent -match "யாழ்ப்பாணம்|Jaffna|marketplace|home|browse|listing"
    
    # Check for market prices
    $hasMarketPrices = $uiContent -match "market.*price|சந்தை.*விலை|வெங்காயம்|onion"
    
    Write-Host "`n=== UI Analysis Results ===" -ForegroundColor Yellow
    Write-Host "Authentication screens detected: $hasAuthScreens" -ForegroundColor $(if($hasAuthScreens) {"Red"} else {"Green"})
    Write-Host "Home screen detected: $hasHomeScreen" -ForegroundColor $(if($hasHomeScreen) {"Green"} else {"Red"})
    Write-Host "Market prices detected: $hasMarketPrices" -ForegroundColor $(if($hasMarketPrices) {"Green"} else {"Yellow"})
    
    # Show package name to confirm correct app
    $packageName = "Unknown"
    if ($uiContent -match 'package="([^"]*)"') {
        $packageName = $matches[1]
    }
    Write-Host "Current app package: $packageName" -ForegroundColor Gray
    
    if ($hasAuthScreens) {
        Write-Host "`n❌ ISSUE CONFIRMED: Authentication screens are still appearing!" -ForegroundColor Red
        Write-Host "The app is not bypassing authentication as expected." -ForegroundColor Red
    } elseif ($hasHomeScreen) {
        Write-Host "`n✅ SUCCESS: App launched directly to home screen!" -ForegroundColor Green
        Write-Host "Authentication bypass is working correctly." -ForegroundColor Green
    } else {
        Write-Host "`n⚠️  UNKNOWN STATE: Neither auth nor home screen detected." -ForegroundColor Yellow
        Write-Host "App may be in an unexpected state." -ForegroundColor Yellow
    }
    
} else {
    Write-Host "❌ Failed to capture UI dump" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
