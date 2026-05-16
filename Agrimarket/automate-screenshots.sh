#!/bin/bash

ANDROID_HOME="/c/Users/Tharma/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb.exe"

echo "🤖 Automated Screenshot Capture"
echo "=================================="
echo ""

# Wait for device
echo "⏳ Waiting for emulator to be ready..."
counter=0
max_wait=120  # 2 minutes

while [ $counter -lt $max_wait ]; do
    if "$ADB" devices | grep -q "emulator"; then
        echo "✅ Emulator detected!"
        break
    fi
    sleep 2
    counter=$((counter + 2))
    echo -n "."
done

if [ $counter -ge $max_wait ]; then
    echo ""
    echo "❌ Emulator didn't start in time"
    exit 1
fi

echo ""
echo "⏳ Waiting for emulator to fully boot..."
"$ADB" wait-for-device
sleep 10  # Extra time for full boot

# Check boot complete
while [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    echo -n "."
    sleep 2
done

echo ""
echo "✅ Emulator fully booted!"
echo ""

# Install app
echo "📦 Installing Agrimarket app..."
if "$ADB" install -r app/build/outputs/apk/debug/app-debug.apk 2>&1; then
    echo "✅ App installed successfully!"
else
    echo "⚠️ Install may have failed, checking..."
fi

echo ""
echo "🚀 Launching app..."
"$ADB" shell am start -n com.senthapps.slagrimarket/.MainActivity

sleep 5  # Let app load

# Create screenshots directory
mkdir -p screenshots

echo ""
echo "📸 Taking screenshots..."
echo ""

# Screenshot 1: Home screen (should load automatically)
sleep 3
echo "  📸 Screenshot 1: Home/Browse Screen"
"$ADB" exec-out screencap -p > screenshots/01-home-browse.png

# Scroll down a bit to show more listings
"$ADB" shell input swipe 500 1500 500 800 300
sleep 1

echo "  📸 Screenshot 2: Browse Screen (scrolled)"
"$ADB" exec-out screencap -p > screenshots/02-browse-scrolled.png

# Try to click on a listing (if any exist)
"$ADB" shell input tap 500 800
sleep 2

echo "  📸 Screenshot 3: Listing Detail (if opened)"
"$ADB" exec-out screencap -p > screenshots/03-listing-detail.png

# Go back
"$ADB" shell input keyevent 4  # Back button
sleep 1

# Try to open create listing (usually a FAB or bottom nav)
# Bottom navigation - approximate positions
"$ADB" shell input tap 540 2200  # Center-right bottom (create button)
sleep 2

echo "  📸 Screenshot 4: Create/Add Screen"
"$ADB" exec-out screencap -p > screenshots/04-create-listing.png

# Navigate to other screens if they exist
"$ADB" shell input keyevent 4  # Back
sleep 1

# Try search/filter (top right usually)
"$ADB" shell input tap 900 200
sleep 2

echo "  📸 Screenshot 5: Search/Filter"
"$ADB" exec-out screencap -p > screenshots/05-search-filter.png

echo ""
echo "=================================="
echo "✅ SCREENSHOT CAPTURE COMPLETE!"
echo "=================================="
echo ""

# Check what we got
echo "📊 Screenshots taken:"
ls -lh screenshots/*.png 2>/dev/null

SCREENSHOT_COUNT=$(ls screenshots/*.png 2>/dev/null | wc -l)
echo ""
echo "Total: $SCREENSHOT_COUNT screenshots"
echo ""

if [ $SCREENSHOT_COUNT -ge 2 ]; then
    echo "✅ Minimum requirement met (2+ screenshots)"
else
    echo "⚠️ Need at least 2 screenshots for Play Store"
fi

echo ""
echo "📁 Screenshots saved in: screenshots/"
echo ""
echo "Next steps:"
echo "1. Review screenshots: open screenshots folder"
echo "2. Upload to Play Console"
echo "3. Submit app for review!"

