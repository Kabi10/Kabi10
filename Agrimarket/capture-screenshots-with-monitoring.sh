#!/bin/bash

ANDROID_HOME="/c/Users/Tharma/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
EMULATOR="$ANDROID_HOME/emulator/emulator.exe"

echo "📸 Agrimarket Screenshot Capture with Monitoring"
echo "================================================"
echo ""

# Kill any existing emulators and adb servers
echo "🧹 Cleaning up existing processes..."
taskkill //F //IM emulator.exe //T 2>/dev/null || true
taskkill //F //IM qemu-system-x86_64.exe //T 2>/dev/null || true
"$ADB" kill-server 2>/dev/null || true
sleep 2
"$ADB" start-server
sleep 2

# Start emulator
echo "🚀 Starting emulator..."
"$EMULATOR" -avd Agrimarket_Screenshots -no-snapshot-load -no-audio -no-boot-anim &
EMULATOR_PID=$!
echo "   Emulator PID: $EMULATOR_PID"

# Wait for device
echo "⏳ Waiting for device..."
counter=0
max_wait=120

while [ $counter -lt $max_wait ]; do
    if "$ADB" devices | grep -q "emulator"; then
        echo "✅ Device detected!"
        break
    fi
    sleep 2
    counter=$((counter + 2))
    echo -n "."
done

if [ $counter -ge $max_wait ]; then
    echo ""
    echo "❌ Emulator didn't start"
    exit 1
fi

# Wait for boot complete
echo ""
echo "⏳ Waiting for boot complete..."
"$ADB" wait-for-device
while [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    echo -n "."
    sleep 2
done
echo ""
echo "✅ Boot complete!"
sleep 10  # Extra settling time

# Start logcat monitoring
mkdir -p screenshots
LOGCAT_FILE="screenshots/logcat-$(date +%Y%m%d-%H%M%S).txt"
echo ""
echo "📋 Starting logcat monitoring: $LOGCAT_FILE"
"$ADB" logcat -c  # Clear existing logs
"$ADB" logcat > "$LOGCAT_FILE" &
LOGCAT_PID=$!
echo "   Logcat PID: $LOGCAT_PID"
sleep 2

# Install app
echo ""
echo "📦 Installing Agrimarket app..."
if "$ADB" install -r app/build/outputs/apk/debug/app-debug.apk 2>&1 | tee screenshots/install-log.txt; then
    echo "✅ App installed!"
else
    echo "❌ Install failed!"
    cat screenshots/install-log.txt
    kill $LOGCAT_PID 2>/dev/null
    exit 1
fi

# Launch app
echo ""
echo "🚀 Launching app..."
"$ADB" shell am start -n com.senthapps.slagrimarket/.MainActivity
sleep 8  # Wait for app to fully load

# Check for crashes in logcat
echo ""
echo "🔍 Checking for app errors..."
if grep -i "FATAL EXCEPTION\|AndroidRuntime\|CRASH" "$LOGCAT_FILE" | head -20; then
    echo "⚠️  Found errors in logcat (check $LOGCAT_FILE for details)"
else
    echo "✅ No fatal errors detected"
fi

# Check if app is running
if "$ADB" shell pidof com.senthapps.slagrimarket > /dev/null; then
    echo "✅ App is running (PID: $("$ADB" shell pidof com.senthapps.slagrimarket))"
else
    echo "❌ App is not running!"
    echo "Last 50 lines of logcat:"
    tail -50 "$LOGCAT_FILE"
    kill $LOGCAT_PID 2>/dev/null
    exit 1
fi

echo ""
echo "📸 Taking screenshots..."
echo ""

# Screenshot 1: Home/Browse screen (wait for data to load)
sleep 3
echo "  📸 Screenshot 1: Home/Browse Screen"
"$ADB" exec-out screencap -p > screenshots/01-home-browse.png

# Scroll down
"$ADB" shell input swipe 500 1500 500 800 300
sleep 2

echo "  📸 Screenshot 2: Browse Screen (scrolled)"
"$ADB" exec-out screencap -p > screenshots/02-browse-scrolled.png

# Try to click on a listing (center of screen)
"$ADB" shell input tap 500 900
sleep 3

echo "  📸 Screenshot 3: Listing Detail"
"$ADB" exec-out screencap -p > screenshots/03-listing-detail.png

# Go back
"$ADB" shell input keyevent 4
sleep 1

# Try to open create listing (FAB or bottom nav)
"$ADB" shell input tap 540 2200  # Bottom navigation create button
sleep 2

echo "  📸 Screenshot 4: Create Listing"
"$ADB" exec-out screencap -p > screenshots/04-create-listing.png

# Go back
"$ADB" shell input keyevent 4
sleep 1

# Try search icon (top right)
"$ADB" shell input tap 900 200
sleep 2

echo "  📸 Screenshot 5: Search/Filter"
"$ADB" exec-out screencap -p > screenshots/05-search-filter.png

echo ""
echo "✅ Screenshots captured!"

# Final error check
echo ""
echo "🔍 Final error check..."
if grep -i "FATAL EXCEPTION\|AndroidRuntime.*CRASH" "$LOGCAT_FILE" | tail -20; then
    echo "⚠️  Found errors - check $LOGCAT_FILE"
else
    echo "✅ No fatal errors"
fi

# Stop logcat
kill $LOGCAT_PID 2>/dev/null
echo ""
echo "📋 Logcat saved to: $LOGCAT_FILE"

# Summary
echo ""
echo "================================================"
echo "✅ SCREENSHOT CAPTURE COMPLETE!"
echo "================================================"
echo ""
echo "📊 Screenshots:"
ls -lh screenshots/*.png 2>/dev/null | tail -5

SCREENSHOT_COUNT=$(ls screenshots/*.png 2>/dev/null | wc -l)
echo ""
echo "Total: $SCREENSHOT_COUNT screenshots"
echo "Logcat: $LOGCAT_FILE ($(wc -l < "$LOGCAT_FILE") lines)"
echo ""

if [ $SCREENSHOT_COUNT -ge 2 ]; then
    echo "✅ Ready for Play Store (minimum 2 screenshots)"
else
    echo "⚠️  Need at least 2 screenshots"
fi

echo ""
echo "Next: Review screenshots and check logcat for any issues"
echo ""
