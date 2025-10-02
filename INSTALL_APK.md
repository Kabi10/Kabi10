# 📱 Agrimarket APK Installation Guide

## ✅ Build Status: SUCCESS

**APK Built:** October 2, 2025  
**APK Size:** 14.21 MB  
**Build Type:** Debug  
**Backend URL:** `https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app`

---

## 📦 APK Location

```
C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk
```

---

## 🔧 Installation Methods

### Method 1: Using ADB (Recommended)

#### Prerequisites
- Android device connected via USB with USB debugging enabled
- ADB installed (found at: `C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe`)

#### Steps

1. **Enable USB Debugging on Android Device:**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times to enable Developer Options
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect Device:**
   - Connect your Android device via USB cable
   - Accept the "Allow USB Debugging" prompt on your device

3. **Verify Connection:**
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
   ```
   
   You should see your device listed:
   ```
   List of devices attached
   ABC123XYZ    device
   ```

4. **Install APK:**
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk
   ```
   
   The `-r` flag reinstalls the app if it already exists.

5. **Launch the App:**
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am start -n com.senthapps.slagrimarket/.MainActivity
   ```

---

### Method 2: Using Android Emulator

#### Start Emulator
```powershell
# List available emulators
C:\Users\Tharma\AppData\Local\Android\Sdk\emulator\emulator.exe -list-avds

# Start an emulator (replace 'Pixel_5_API_33' with your AVD name)
C:\Users\Tharma\AppData\Local\Android\Sdk\emulator\emulator.exe -avd Pixel_5_API_33
```

#### Install APK on Emulator
```powershell
# Wait for emulator to boot, then install
C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk
```

---

### Method 3: Manual Transfer

1. **Copy APK to Device:**
   - Connect device via USB
   - Copy `app-debug.apk` to device's Downloads folder
   - Or use ADB push:
     ```powershell
     C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe push C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk /sdcard/Download/
     ```

2. **Install from Device:**
   - Open File Manager on device
   - Navigate to Downloads folder
   - Tap on `app-debug.apk`
   - Allow installation from unknown sources if prompted
   - Tap "Install"

---

## 🚀 Quick Installation Script

Save this as `install-app.ps1` and run it:

```powershell
#!/usr/bin/env pwsh

Write-Host "🚀 Agrimarket APK Installation Script" -ForegroundColor Cyan
Write-Host "======================================`n" -ForegroundColor Cyan

$adbPath = "C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk"

# Check if ADB exists
if (-not (Test-Path $adbPath)) {
    Write-Host "❌ ADB not found at: $adbPath" -ForegroundColor Red
    exit 1
}

# Check if APK exists
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
    Write-Host "Run: .\gradlew.bat assembleDebug" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ ADB found" -ForegroundColor Green
Write-Host "✅ APK found ($([math]::Round((Get-Item $apkPath).Length / 1MB, 2)) MB)`n" -ForegroundColor Green

# Check connected devices
Write-Host "📱 Checking for connected devices..." -ForegroundColor Cyan
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices connected" -ForegroundColor Red
    Write-Host "`nPlease:" -ForegroundColor Yellow
    Write-Host "  1. Connect your Android device via USB" -ForegroundColor Yellow
    Write-Host "  2. Enable USB Debugging in Developer Options" -ForegroundColor Yellow
    Write-Host "  3. Accept the USB Debugging prompt on your device" -ForegroundColor Yellow
    Write-Host "`nThen run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Device connected`n" -ForegroundColor Green

# Install APK
Write-Host "📦 Installing APK..." -ForegroundColor Cyan
try {
    $output = & $adbPath install -r $apkPath 2>&1
    
    if ($output -match "Success") {
        Write-Host "✅ Installation successful!`n" -ForegroundColor Green
        
        # Launch the app
        Write-Host "🚀 Launching app..." -ForegroundColor Cyan
        & $adbPath shell am start -n com.senthapps.slagrimarket/.MainActivity
        
        Write-Host "`n✨ App launched successfully!" -ForegroundColor Green
        Write-Host "`n📱 The app should now be running on your device." -ForegroundColor Cyan
        Write-Host "Backend URL: https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app" -ForegroundColor Gray
    } else {
        Write-Host "❌ Installation failed:" -ForegroundColor Red
        Write-Host $output -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Error during installation: $_" -ForegroundColor Red
}
```

---

## 🧪 Testing After Installation

### 1. Verify Backend Connection

The app is configured to connect to:
```
https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app/api/
```

### 2. Test Authentication Flow

1. Launch the app
2. Enter phone number: `+94771234567`
3. Tap "Send OTP"
4. Enter OTP: `123456` (development mode)
5. Tap "Verify"
6. You should be logged in as a demo user

### 3. Test Core Features

- **Home Screen:** View market prices and trending listings
- **Listings:** Browse and search crop listings
- **Create Listing:** (Farmers only) Add new crop listing
- **Transactions:** View purchase history
- **Profile:** View user profile and settings
- **Language Toggle:** Switch between English/Tamil/Sinhala

### 4. Monitor Logs

To view app logs in real-time:
```powershell
C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat | Select-String "Agrimarket|Retrofit|OkHttp"
```

Or save to file:
```powershell
C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat > app_logs.txt
```

---

## 🔍 Troubleshooting

### Device Not Detected

**Problem:** `adb devices` shows no devices

**Solutions:**
1. Check USB cable (try a different cable)
2. Enable USB Debugging on device
3. Revoke USB debugging authorizations and reconnect
4. Restart ADB server:
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe kill-server
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe start-server
   ```

### Installation Failed

**Problem:** Installation fails with error

**Solutions:**
1. Uninstall existing app first:
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe uninstall com.senthapps.slagrimarket
   ```
2. Check device storage space
3. Enable "Install from Unknown Sources" in device settings

### App Crashes on Launch

**Problem:** App crashes immediately after opening

**Solutions:**
1. Check logs:
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat -d | Select-String "AndroidRuntime"
   ```
2. Clear app data:
   ```powershell
   C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe shell pm clear com.senthapps.slagrimarket
   ```
3. Reinstall the app

### Network Errors

**Problem:** App shows "Network Error" or "Connection Failed"

**Solutions:**
1. Check device internet connection
2. Verify backend is running:
   - Open browser: https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app/health
3. Check app logs for specific error messages

---

## 📊 Build Information

### Updated Configuration Files

1. **ApiConfig.kt**
   ```kotlin
   const val PRODUCTION_BASE_URL = "https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app/api/"
   ```

2. **NetworkModule.kt**
   ```kotlin
   private const val BASE_URL = "https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app/api/"
   ```

### Build Output

- **Build Time:** ~3 minutes 34 seconds
- **APK Size:** 14.21 MB
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Warnings:** Deprecation warnings (non-critical)

---

## 🎯 Next Steps

1. ✅ **Build Complete** - APK successfully built
2. ⏭️ **Connect Device** - Connect Android device via USB
3. ⏭️ **Install APK** - Use ADB or manual installation
4. ⏭️ **Test App** - Verify all features work with production backend
5. ⏭️ **Monitor Logs** - Check for any runtime errors
6. ⏭️ **User Testing** - Test complete user flows

---

## 📞 Quick Reference

### ADB Path
```
C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools\adb.exe
```

### APK Path
```
C:\Dev\Agrimarket\app\build\outputs\apk\debug\app-debug.apk
```

### Backend URL
```
https://agrimarket-96pmevhf5-kabilantharmaratnam-kpucas-projects.vercel.app
```

### Package Name
```
com.senthapps.slagrimarket
```

### Main Activity
```
com.senthapps.slagrimarket.MainActivity
```

---

**✨ Your APK is ready for installation and testing!**

