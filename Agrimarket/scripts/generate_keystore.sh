#!/bin/bash
# Generate a release keystore for Agrimarket Play Store submission
# Run this once — keep the .jks file and passwords safe (do NOT commit them)
#
# After running, add these lines to local.properties:
#   KEYSTORE_PATH=agrimarket-release.jks
#   KEYSTORE_PASSWORD=<your_password>
#   KEY_ALIAS=agrimarket
#   KEY_PASSWORD=<your_password>

set -e

KEYSTORE_FILE="agrimarket-release.jks"
KEY_ALIAS="agrimarket"

if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  $KEYSTORE_FILE already exists. Delete it first if you want to regenerate."
    exit 1
fi

echo "🔑 Generating Agrimarket release keystore..."
echo "   You will be prompted for passwords and certificate details."
echo "   Use a strong password and keep it safe — you cannot change it later."
echo ""

keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storetype JKS

echo ""
echo "✅ Keystore generated: $KEYSTORE_FILE"
echo ""
echo "Add the following to local.properties (do NOT commit this file):"
echo ""
echo "  KEYSTORE_PATH=$(pwd)/$KEYSTORE_FILE"
echo "  KEYSTORE_PASSWORD=<your_keystore_password>"
echo "  KEY_ALIAS=$KEY_ALIAS"
echo "  KEY_PASSWORD=<your_key_password>"
echo ""
echo "Then build the release AAB:"
echo "  ./gradlew bundleRelease"
echo ""
echo "The signed AAB will be at:"
echo "  app/build/outputs/bundle/release/app-release.aab"
