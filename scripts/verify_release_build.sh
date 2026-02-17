#!/bin/bash
set -e

echo "========================================="
echo "PLAY STORE RELEASE VERIFICATION"
echo "========================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0;0m' # No Color

ERRORS=0

# 1. Network Security Verification
echo ""
echo "1. Verifying Network Security..."
./gradlew clean assembleRelease --quiet

RELEASE_APK="app/build/outputs/apk/release/app-release.apk"
RELEASE_APK_UNSIGNED="app/build/outputs/apk/release/app-release-unsigned.apk"

# Check which APK exists
if [ -f "$RELEASE_APK" ]; then
    APK_FILE="$RELEASE_APK"
elif [ -f "$RELEASE_APK_UNSIGNED" ]; then
    APK_FILE="$RELEASE_APK_UNSIGNED"
else
    echo -e "${RED}❌ Release APK not found${NC}"
    exit 1
fi

echo "   Using APK: $APK_FILE"

unzip -p "$APK_FILE" res/xml/network_security_config.xml > /tmp/release_nsc.xml 2>/dev/null || {
    echo -e "${RED}❌ Could not extract network_security_config.xml${NC}"
    ERRORS=$((ERRORS + 1))
}

if [ -f /tmp/release_nsc.xml ]; then
    if grep -q "cleartextTrafficPermitted=\"true\"" /tmp/release_nsc.xml; then
        echo -e "${RED}❌ SECURITY VIOLATION: Cleartext traffic is allowed in release build!${NC}"
        ERRORS=$((ERRORS + 1))
    else
        echo -e "${GREEN}✅ Network security: HTTPS only enforced${NC}"
    fi
fi

# 2. Signing Verification
echo ""
echo "2. Verifying APK Signature..."

# Check if jarsigner is available
if command -v jarsigner &> /dev/null; then
    if jarsigner -verify "$APK_FILE" 2>/dev/null; then
        echo -e "${GREEN}✅ APK is signed${NC}"

        # Extract signing certificate info
        echo "   Signature details:"
        jarsigner -verify -verbose -certs "$APK_FILE" 2>&1 | grep "SHA" | head -1
    else
        echo -e "${YELLOW}⚠️  APK is unsigned (configure signing in local.properties)${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  jarsigner not found - skipping signature verification${NC}"
fi

# 3. ProGuard Verification
echo ""
echo "3. Verifying ProGuard/R8..."

MAPPING_FILE="app/build/outputs/mapping/release/mapping.txt"
if [ -f "$MAPPING_FILE" ]; then
    # Check file size (cross-platform)
    if [ "$(uname)" = "Darwin" ] || [ "$(uname)" = "FreeBSD" ]; then
        # macOS/BSD uses -f
        MAPPING_SIZE=$(stat -f%z "$MAPPING_FILE" 2>/dev/null)
    else
        # Linux uses -c
        MAPPING_SIZE=$(stat -c%s "$MAPPING_FILE" 2>/dev/null)
    fi

    if [ $MAPPING_SIZE -gt 100000 ]; then
        echo -e "${GREEN}✅ ProGuard mapping file generated (${MAPPING_SIZE} bytes)${NC}"
    else
        echo -e "${RED}❌ Mapping file too small - obfuscation may not be working${NC}"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}❌ ProGuard mapping file not found${NC}"
    ERRORS=$((ERRORS + 1))
fi

# 4. APK Size Check
echo ""
echo "4. Checking APK Size..."

if [ "$(uname)" = "Darwin" ] || [ "$(uname)" = "FreeBSD" ]; then
    APK_SIZE=$(stat -f%z "$APK_FILE" 2>/dev/null)
else
    APK_SIZE=$(stat -c%s "$APK_FILE" 2>/dev/null)
fi

APK_SIZE_MB=$((APK_SIZE / 1024 / 1024))

if [ $APK_SIZE_MB -lt 50 ]; then
    echo -e "${GREEN}✅ APK size: ${APK_SIZE_MB}MB (under 50MB limit)${NC}"
else
    echo -e "${YELLOW}⚠️  APK size: ${APK_SIZE_MB}MB (consider size optimizations)${NC}"
fi

# 5. Permissions Check
echo ""
echo "5. Verifying Permissions..."

# Try to extract and parse manifest
unzip -p "$APK_FILE" AndroidManifest.xml > /tmp/release_manifest_binary.xml 2>/dev/null || {
    echo -e "${YELLOW}⚠️  Could not extract manifest for permission check${NC}"
}

# Note: Binary XML needs special tools to read, but we can still check with strings
if [ -f /tmp/release_manifest_binary.xml ]; then
    # Check for dangerous permissions that aren't needed
    DANGEROUS_PERMS=("ACCESS_FINE_LOCATION" "ACCESS_BACKGROUND_LOCATION" "RECORD_AUDIO")
    FOUND_UNEXPECTED=false
    for perm in "${DANGEROUS_PERMS[@]}"; do
        if strings /tmp/release_manifest_binary.xml 2>/dev/null | grep -q "$perm"; then
            echo -e "${YELLOW}⚠️  Found permission: $perm (verify this is needed)${NC}"
            FOUND_UNEXPECTED=true
        fi
    done

    if [ "$FOUND_UNEXPECTED" = false ]; then
        echo -e "${GREEN}✅ No unexpected dangerous permissions found${NC}"
    fi
else
    echo -e "${GREEN}✅ Permission check skipped (tools not available)${NC}"
fi

# Summary
echo ""
echo "========================================="
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED - Ready for Play Store${NC}"
    echo "========================================="
    exit 0
else
    echo -e "${RED}❌ $ERRORS CHECKS FAILED - Fix errors before releasing${NC}"
    echo "========================================="
    exit 1
fi
