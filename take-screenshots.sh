#!/bin/bash

ANDROID_HOME="/c/Users/Tharma/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb"

echo "📸 Screenshot Helper for Agrimarket"
echo "=================================="
echo ""

# Check if emulator is running
if ! $ADB devices | grep -q "emulator"; then
    echo "❌ No emulator detected!"
    echo ""
    echo "Please start emulator first:"
    echo "1. Open Android Studio"
    echo "2. Tools → Device Manager (or AVD Manager)"
    echo "3. Click ▶️ on any emulator"
    echo ""
    echo "Then run this script again!"
    exit 1
fi

echo "✅ Emulator detected!"
echo ""

# Install debug APK (easier to use than release)
echo "📦 Installing debug APK..."
if $ADB install -r app/build/outputs/apk/debug/app-debug.apk 2>&1; then
    echo "✅ App installed!"
else
    echo "⚠️  Install failed, but app might already be installed"
fi

echo ""
echo "=================================="
echo "🎬 SCREENSHOT INSTRUCTIONS"
echo "=================================="
echo ""
echo "The app is now installed on your emulator."
echo ""
echo "To take screenshots:"
echo ""
echo "METHOD 1: Android Studio (Recommended)"
echo "  1. Navigate to the screen you want"
echo "  2. In Android Studio: Tools → Screenshot"
echo "  3. OR click the camera icon in emulator toolbar"
echo "  4. Save to: screenshots/screenshot-1.png"
echo ""
echo "METHOD 2: This Script"
echo "  1. Navigate to the screen you want"
echo "  2. Press Enter in this terminal"
echo "  3. Screenshot saved automatically"
echo ""
echo "SCREENS TO CAPTURE:"
echo "  1. Home/Browse listings screen"
echo "  2. Listing detail view"
echo "  3. Create new listing screen"
echo "  4. Search/filter screen"
echo "  5. Map view (if available)"
echo ""
echo "=================================="
echo ""

# Create screenshots directory
mkdir -p screenshots

# Interactive screenshot taking
counter=1
while true; do
    read -p "Press Enter to take screenshot $counter (or 'q' to quit): " input
    if [ "$input" = "q" ]; then
        break
    fi
    
    # Take screenshot
    filename="screenshots/screenshot-$counter.png"
    $ADB exec-out screencap -p > "$filename"
    
    if [ -f "$filename" ]; then
        echo "✅ Screenshot $counter saved: $filename"
        counter=$((counter + 1))
    else
        echo "❌ Failed to take screenshot"
    fi
done

echo ""
echo "=================================="
echo "📊 SCREENSHOT SUMMARY"
echo "=================================="
ls -lh screenshots/*.png 2>/dev/null || echo "No screenshots taken"
echo ""
echo "Total screenshots: $((counter - 1))"
echo ""
echo "Next steps:"
echo "1. Review screenshots in: screenshots/"
echo "2. Upload to Play Console"
echo "3. Need at least 2 screenshots minimum"

