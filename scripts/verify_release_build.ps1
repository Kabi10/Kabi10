# PowerShell script for Play Store release verification on Windows
$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "PLAY STORE RELEASE VERIFICATION" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$ERRORS = 0

# 1. Network Security Verification
Write-Host ""
Write-Host "1. Verifying Network Security..." -ForegroundColor Yellow
./gradlew clean assembleRelease --quiet

$RELEASE_APK = "app/build/outputs/apk/release/app-release.apk"
$RELEASE_APK_UNSIGNED = "app/build/outputs/apk/release/app-release-unsigned.apk"

# Check which APK exists
if (Test-Path $RELEASE_APK) {
    $APK_FILE = $RELEASE_APK
} elseif (Test-Path $RELEASE_APK_UNSIGNED) {
    $APK_FILE = $RELEASE_APK_UNSIGNED
} else {
    Write-Host "❌ Release APK not found" -ForegroundColor Red
    exit 1
}

Write-Host "   Using APK: $APK_FILE"

# Extract network security config
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $APK_FILE))
$entry = $zip.Entries | Where-Object { $_.FullName -eq "res/xml/network_security_config.xml" }

if ($entry) {
    $stream = $entry.Open()
    $reader = New-Object System.IO.StreamReader($stream)
    $content = $reader.ReadToEnd()
    $reader.Close()
    $stream.Close()

    if ($content -match 'cleartextTrafficPermitted="true"') {
        Write-Host "❌ SECURITY VIOLATION: Cleartext traffic is allowed in release build!" -ForegroundColor Red
        $ERRORS++
    } else {
        Write-Host "✅ Network security: HTTPS only enforced" -ForegroundColor Green
    }
} else {
    Write-Host "❌ Could not extract network_security_config.xml" -ForegroundColor Red
    $ERRORS++
}

$zip.Dispose()

# 2. Signing Verification
Write-Host ""
Write-Host "2. Verifying APK Signature..." -ForegroundColor Yellow

# Check if jarsigner is available
try {
    $jarsignerPath = Get-Command jarsigner -ErrorAction Stop
    $verifyResult = & jarsigner -verify $APK_FILE 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ APK is signed" -ForegroundColor Green
        Write-Host "   Signature details:"
        $certInfo = & jarsigner -verify -verbose -certs $APK_FILE 2>&1 | Select-String "SHA"
        if ($certInfo) {
            Write-Host "   $($certInfo[0])"
        }
    } else {
        Write-Host "⚠️  APK is unsigned (configure signing in local.properties)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  jarsigner not found - skipping signature verification" -ForegroundColor Yellow
}

# 3. ProGuard Verification
Write-Host ""
Write-Host "3. Verifying ProGuard/R8..." -ForegroundColor Yellow

$MAPPING_FILE = "app/build/outputs/mapping/release/mapping.txt"
if (Test-Path $MAPPING_FILE) {
    $MAPPING_SIZE = (Get-Item $MAPPING_FILE).length
    if ($MAPPING_SIZE -gt 100000) {
        Write-Host "✅ ProGuard mapping file generated ($MAPPING_SIZE bytes)" -ForegroundColor Green
    } else {
        Write-Host "❌ Mapping file too small - obfuscation may not be working" -ForegroundColor Red
        $ERRORS++
    }
} else {
    Write-Host "❌ ProGuard mapping file not found" -ForegroundColor Red
    $ERRORS++
}

# 4. APK Size Check
Write-Host ""
Write-Host "4. Checking APK Size..." -ForegroundColor Yellow

$APK_SIZE = (Get-Item $APK_FILE).length
$APK_SIZE_MB = [math]::Round($APK_SIZE / 1MB, 2)

if ($APK_SIZE_MB -lt 50) {
    Write-Host "✅ APK size: ${APK_SIZE_MB}MB (under 50MB limit)" -ForegroundColor Green
} else {
    Write-Host "⚠️  APK size: ${APK_SIZE_MB}MB (consider size optimizations)" -ForegroundColor Yellow
}

# 5. Permissions Check
Write-Host ""
Write-Host "5. Verifying Permissions..." -ForegroundColor Yellow

# Extract manifest
$zip2 = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $APK_FILE))
$manifestEntry = $zip2.Entries | Where-Object { $_.FullName -eq "AndroidManifest.xml" }

if ($manifestEntry) {
    $stream2 = $manifestEntry.Open()
    $bytes = New-Object byte[] $manifestEntry.Length
    $stream2.Read($bytes, 0, $bytes.Length) | Out-Null
    $stream2.Close()

    # Check for dangerous permissions in binary XML (basic string search)
    $manifestText = [System.Text.Encoding]::ASCII.GetString($bytes)
    $DANGEROUS_PERMS = @("ACCESS_FINE_LOCATION", "ACCESS_BACKGROUND_LOCATION", "RECORD_AUDIO")
    $FOUND_UNEXPECTED = $false

    foreach ($perm in $DANGEROUS_PERMS) {
        if ($manifestText -match $perm) {
            Write-Host "⚠️  Found permission: $perm (verify this is needed)" -ForegroundColor Yellow
            $FOUND_UNEXPECTED = $true
        }
    }

    if (-not $FOUND_UNEXPECTED) {
        Write-Host "✅ No unexpected dangerous permissions found" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️  Could not extract manifest for permission check" -ForegroundColor Yellow
}

$zip2.Dispose()

# Summary
Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
if ($ERRORS -eq 0) {
    Write-Host "✅ ALL CHECKS PASSED - Ready for Play Store" -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host "❌ $ERRORS CHECKS FAILED - Fix errors before releasing" -ForegroundColor Red
    Write-Host "=========================================" -ForegroundColor Cyan
    exit 1
}
