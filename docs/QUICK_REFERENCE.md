# 🚀 Pre-Launch Quick Reference

**Copy-paste commands for testing - December 21, 2025**

---

## Setup (Run First)
```powershell
$env:ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $env:ADB devices
```

---

## Phase 1: Automated Tests
```powershell
.\gradlew test                     # Unit tests (48)
.\gradlew lint                     # Lint checks
.\gradlew connectedAndroidTest     # UI tests (27)
.\gradlew assembleRelease          # Release build
```

---

## Phase 2: Logcat Monitoring
```powershell
# Terminal 1 - Run continuously
& $env:ADB logcat -v time | Select-String "(OkHttp|HTTP|200|201|Auth|Sync)"

# Or save to file
& $env:ADB logcat -v time > logcat.log
```

---

## Phase 3: App Control
```powershell
# Fresh start
& $env:ADB shell pm clear com.senthapps.slagrimarket
& $env:ADB shell am start -n com.senthapps.slagrimarket/.MainActivity

# Kill app
& $env:ADB shell am force-stop com.senthapps.slagrimarket
```

---

## Phase 4: Network Testing
```powershell
# Airplane mode ON (offline)
& $env:ADB shell cmd connectivity airplane-mode enable

# Airplane mode OFF (online)
& $env:ADB shell cmd connectivity airplane-mode disable
```

---

## Phase 5: Language Testing
```powershell
# Tamil
& $env:ADB shell "setprop persist.sys.locale ta-IN; stop; start"

# Sinhala
& $env:ADB shell "setprop persist.sys.locale si-LK; stop; start"

# English (reset)
& $env:ADB shell "setprop persist.sys.locale en-US; stop; start"
```

---

## Phase 6: Dark Mode
```powershell
& $env:ADB shell "cmd uimode night yes"   # Enable
& $env:ADB shell "cmd uimode night no"    # Disable
```

---

## Phase 7: Screenshots
```powershell
& $env:ADB exec-out screencap -p > screenshot.png
```

---

## Phase 8: TalkBack
```powershell
# Enable
& $env:ADB shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
& $env:ADB shell settings put secure accessibility_enabled 1

# Disable
& $env:ADB shell settings put secure enabled_accessibility_services ""
& $env:ADB shell settings put secure accessibility_enabled 0
```

---

## Quick Checklist

| # | Item | Command | ✓ |
|---|------|---------|---|
| 1 | API Health | `Invoke-RestMethod "https://agrimarket-bf32inyap-kabilantharmaratnam-kpucas-projects.vercel.app/api/health"` | [ ] |
| 2 | Auth Flow | Manual login with OTP | [ ] |
| 3 | Sync Test | Toggle airplane mode | [ ] |
| 4 | Offline Data | View cached listings offline | [ ] |
| 5 | Create Listing | Add new listing, check logcat | [ ] |
| 6 | Unit Tests | `.\gradlew test` | [ ] |
| 7 | UI Tests | `.\gradlew connectedAndroidTest` | [ ] |
| 8 | Tamil UI | Language toggle | [ ] |
| 9 | Dark Mode | Theme consistency | [ ] |
| 10 | TalkBack | Accessibility | [ ] |

---

**Full checklist:** [PRE_LAUNCH_CHECKLIST.md](file:///c:/Dev/Agrimarket/docs/PRE_LAUNCH_CHECKLIST.md)
