#!/usr/bin/env pwsh

# Agrimarket APK Installation Script
# Automatically installs the debug APK on connected Android device

Write-Host "`n🚀 Agrimarket APK Installation Script" -ForegroundColor Cyan
Write-Host "======================================`n" -ForegroundColor Cyan

$adbPath = "C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.senthapps.slagrimarket"
$mainActivity = "$packageName/.MainActivity"

# ============================================================================
# Step 1: Verify Prerequisites
# ============================================================================

Write-Host "📋 Step 1: Verifying Prerequisites" -ForegroundColor Yellow
Write-Host "───────────────────────────────────`n" -ForegroundColor Gray

# Check if ADB exists
if (-not (Test-Path $adbPath)) {
    Write-Host "❌ ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "`nPlease install Android SDK or update the ADB path in this script." -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ ADB found" -ForegroundColor Green

# Check if APK exists
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
    Write-Host "`nPlease build the APK first:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor Cyan
    exit 1
}

$apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "✅ APK found ($apkSize MB)" -ForegroundColor Green

# ============================================================================
# Step 2: Check Connected Devices
# ============================================================================

Write-Host "`n📱 Step 2: Checking Connected Devices" -ForegroundColor Yellow
Write-Host "──────────────────────────────────────`n" -ForegroundColor Gray

$devicesOutput = & $adbPath devices 2>&1
$devices = $devicesOutput | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices connected" -ForegroundColor Red
    Write-Host "`nPlease connect your Android device:" -ForegroundColor Yellow
    Write-Host "  1. Connect device via USB cable" -ForegroundColor Cyan
    Write-Host "  2. Enable USB Debugging in Developer Options" -ForegroundColor Cyan
    Write-Host "  3. Accept the 'Allow USB Debugging' prompt on your device" -ForegroundColor Cyan
    Write-Host "`nTo enable Developer Options:" -ForegroundColor Yellow
    Write-Host "  Settings → About Phone → Tap 'Build Number' 7 times" -ForegroundColor Cyan
    Write-Host "`nThen run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Device connected:" -ForegroundColor Green
foreach ($device in $devices) {
    $deviceId = ($device -split '\s+')[0]
    Write-Host "   Device ID: $deviceId" -ForegroundColor Gray
}

# ============================================================================
# Step 3: Check if App is Already Installed
# ============================================================================

Write-Host "`n🔍 Step 3: Checking Existing Installation" -ForegroundColor Yellow
Write-Host "──────────────────────────────────────────`n" -ForegroundColor Gray

$installedPackages = & $adbPath shell pm list packages | Select-String $packageName

if ($installedPackages) {
    Write-Host "⚠️  App is already installed" -ForegroundColor Yellow
    Write-Host "   Will reinstall (keeping data)..." -ForegroundColor Gray
} else {
    Write-Host "ℹ️  App not currently installed" -ForegroundColor Cyan
    Write-Host "   Will perform fresh installation..." -ForegroundColor Gray
}

# ============================================================================
# Step 4: Install APK
# ============================================================================

Write-Host "`n📦 Step 4: Installing APK" -ForegroundColor Yellow
Write-Host "─────────────────────────`n" -ForegroundColor Gray

Write-Host "Installing... (this may take a minute)" -ForegroundColor Cyan

try {
    $installOutput = & $adbPath install -r $apkPath 2>&1
    
    if ($installOutput -match "Success") {
        Write-Host "✅ Installation successful!" -ForegroundColor Green
    } elseif ($installOutput -match "INSTALL_FAILED") {
        Write-Host "❌ Installation failed!" -ForegroundColor Red
        Write-Host "`nError details:" -ForegroundColor Yellow
        Write-Host $installOutput -ForegroundColor Red
        
        Write-Host "`nTrying to uninstall and reinstall..." -ForegroundColor Yellow
        & $adbPath uninstall $packageName 2>&1 | Out-Null
        Start-Sleep -Seconds 2
        
        $retryOutput = & $adbPath install $apkPath 2>&1
        if ($retryOutput -match "Success") {
            Write-Host "✅ Installation successful on retry!" -ForegroundColor Green
        } else {
            Write-Host "❌ Installation failed again:" -ForegroundColor Red
            Write-Host $retryOutput -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "⚠️  Unexpected output:" -ForegroundColor Yellow
        Write-Host $installOutput -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Error during installation: $_" -ForegroundColor Red
    exit 1
}

# ============================================================================
# Step 5: Launch the App
# ============================================================================

Write-Host "`n🚀 Step 5: Launching App" -ForegroundColor Yellow
Write-Host "────────────────────────`n" -ForegroundColor Gray

try {
    & $adbPath shell am start -n $mainActivity 2>&1 | Out-Null
    Write-Host "✅ App launched successfully!" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Could not auto-launch app" -ForegroundColor Yellow
    Write-Host "   Please launch manually from your device" -ForegroundColor Gray
}

# ============================================================================
# Step 6: Display Summary
# ============================================================================

Write-Host "`n" -NoNewline
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✨ Installation Complete!" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan

Write-Host "`n📱 App Information:" -ForegroundColor Yellow
Write-Host "   Package: $packageName" -ForegroundColor Gray
Write-Host "   APK Size: $apkSize MB" -ForegroundColor Gray
Write-Host "   Backend: https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app" -ForegroundColor Gray

Write-Host "`n🧪 Testing Instructions:" -ForegroundColor Yellow
Write-Host "   1. The app should now be running on your device" -ForegroundColor Cyan
Write-Host "   2. Test authentication with phone: +94771234567" -ForegroundColor Cyan
Write-Host "   3. Use OTP: 123456 (development mode)" -ForegroundColor Cyan
Write-Host "   4. Explore all features: Home, Listings, Transactions, Profile" -ForegroundColor Cyan
Write-Host "   5. Test language switching (English/Tamil/Sinhala)" -ForegroundColor Cyan

Write-Host "`n📊 Monitor Logs:" -ForegroundColor Yellow
Write-Host "   To view app logs in real-time, run:" -ForegroundColor Gray
Write-Host "   $adbPath logcat | Select-String 'Agrimarket|Retrofit'" -ForegroundColor Cyan

Write-Host "`n🔧 Useful Commands:" -ForegroundColor Yellow
Write-Host "   Uninstall app:" -ForegroundColor Gray
Write-Host "   $adbPath uninstall $packageName" -ForegroundColor Cyan
Write-Host "`n   Clear app data:" -ForegroundColor Gray
Write-Host "   $adbPath shell pm clear $packageName" -ForegroundColor Cyan
Write-Host "`n   Restart app:" -ForegroundColor Gray
Write-Host "   $adbPath shell am start -n $mainActivity" -ForegroundColor Cyan

Write-Host "`n═══════════════════════════════════════`n" -ForegroundColor Cyan

