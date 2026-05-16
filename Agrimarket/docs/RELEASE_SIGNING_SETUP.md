# Release Signing Configuration

## Overview

Production APKs must be signed with a release keystore for Play Store distribution. This guide explains how to configure signing for both local development and CI/CD.

## Prerequisites

### 1. Generate Release Keystore (One-Time Setup)

```bash
keytool -genkey -v -keystore agrimarket-release.keystore \
  -alias agrimarket -keyalg RSA -keysize 2048 -validity 10000
```

You will be prompted to enter:

- **Keystore password**: Create a strong password (save this securely!)
- **Key password**: Create a strong password (can be same as keystore password)
- **Organization details**: Your name, organization, city, state, country

**Important**: Save these passwords in a secure password manager (1Password, LastPass, etc.). You will need them for every release build.

### 2. Secure Storage

⚠️ **CRITICAL SECURITY REQUIREMENTS**:

- **NEVER** commit the keystore file to git
- **NEVER** commit passwords to git or share them in plain text
- Store keystore file in a secure location (NOT in the project directory)
- Back up the keystore file in multiple secure locations
- Upload keystore to CI/CD secrets (GitHub Secrets, etc.)

**Why this matters**: If you lose the keystore or passwords, you will **never be able to update your app** on the Play Store. You would have to publish a completely new app with a different package name.

## Configuration Methods

### Method 1: Local Development (local.properties)

Add the following to `local.properties` in the project root (this file is already .gitignored):

```properties
# Release Signing
KEYSTORE_PATH=/absolute/path/to/agrimarket-release.keystore
KEYSTORE_PASSWORD=your_keystore_password_here
KEY_ALIAS=agrimarket
KEY_PASSWORD=your_key_password_here

# Supabase (already configured)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_anon_key_here
```

**Notes**:

- Use absolute paths for `KEYSTORE_PATH` (e.g., `C:/Users/YourName/keystores/agrimarket-release.keystore` on Windows)
- On Windows, use forward slashes (/) or escaped backslashes (\\\\) in paths
- Do NOT use relative paths - they may not resolve correctly during builds

### Method 2: CI/CD (Environment Variables)

Configure in GitHub Actions Secrets or your CI/CD platform:

1. **Encode keystore to Base64** (for CI/CD upload):

```bash
# Linux/Mac
base64 -i agrimarket-release.keystore -o keystore.base64

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("agrimarket-release.keystore")) | Out-File keystore.base64
```

2. **Add secrets to GitHub**:
   - Go to: Repository Settings → Secrets and variables → Actions
   - Add the following secrets:
     - `KEYSTORE_BASE64`: Content of keystore.base64 file
     - `KEYSTORE_PASSWORD`: Your keystore password
     - `KEY_ALIAS`: `agrimarket`
     - `KEY_PASSWORD`: Your key password

3. **In GitHub Actions workflow**, decode the keystore:

```yaml
- name: Decode Keystore
  env:
    KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
  run: |
    echo $KEYSTORE_BASE64 | base64 -d > $HOME/keystore.jks

- name: Build Release APK
  env:
    KEYSTORE_PATH: $HOME/keystore.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleRelease
```

## Verification

### Build Release APK

```bash
./gradlew assembleRelease
```

### Success Indicators

✅ **Properly Configured**:

- No signing warnings in build output
- APK generated at `app/build/outputs/apk/release/app-release.apk` (note: **not** `-unsigned.apk`)
- Signature verification passes:
  ```bash
  jarsigner -verify app/build/outputs/apk/release/app-release.apk
  # Should output: "jar verified."
  ```

### Failure Indicators

❌ **Not Configured**:

- Warning in build output: `⚠️ Release build will be UNSIGNED`
- APK filename: `app-release-unsigned.apk`
- Signature verification fails or says "jar is unsigned"

### View Signature Details

To verify your APK is properly signed and view certificate details:

```bash
# Verify signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# View certificate fingerprints (needed for Play Console, Firebase, etc.)
keytool -list -v -keystore agrimarket-release.keystore -alias agrimarket
```

**Important**: Copy the SHA-1 and SHA-256 fingerprints - you'll need these for:

- Google Play Console app signing configuration
- Firebase app configuration
- Google Maps API keys
- Google Sign-In setup

## Security Best Practices

### 1. Keystore Security

- ✅ **DO**: Store keystore in a secure, backed-up location
- ✅ **DO**: Use strong, unique passwords (minimum 12 characters)
- ✅ **DO**: Back up keystore to multiple secure locations (encrypted cloud storage, offline backup)
- ❌ **DON'T**: Commit keystore to git (even private repos)
- ❌ **DON'T**: Share keystore via email, Slack, or unencrypted channels
- ❌ **DON'T**: Store passwords in plain text files

### 2. Password Management

- Use a password manager (1Password, LastPass, Bitwarden)
- Share passwords with team members using password manager's secure sharing
- Rotate passwords annually or when team members leave

### 3. Access Control

- Limit keystore access to release managers only
- For teams: Use Google Play App Signing (Play Console manages the keystore)
- Document who has access to the keystore

### 4. Backup Strategy

Create multiple secure backups:

1. **Encrypted cloud storage** (Google Drive, Dropbox with encryption)
2. **Offline backup** (USB drive in secure location)
3. **Password manager** (attach keystore to a secure note)
4. **Company vault** (if applicable)

**Test your backups** periodically to ensure they're not corrupted.

## Troubleshooting

### Error: "Keystore was tampered with, or password was incorrect"

**Cause**: Wrong password or corrupted keystore file

**Solutions**:

1. Verify you're using the correct keystore password:
   ```bash
   keytool -list -v -keystore agrimarket-release.keystore
   # Enter password when prompted
   ```
2. Check `KEYSTORE_PASSWORD` exactly matches the password you set
3. Verify `KEY_PASSWORD` matches the key password (may be different from keystore password)
4. If file is corrupted, restore from backup

### Error: "Keystore file does not exist"

**Cause**: Wrong path or file moved

**Solutions**:

1. Verify the path in `local.properties` is correct and absolute
2. Check the keystore file exists at that location
3. On Windows, ensure path uses forward slashes or escaped backslashes

### Error: "Cannot locate Android SDK"

**Cause**: Android SDK not found

**Solutions**:

1. Set `ANDROID_HOME` environment variable:

   ```bash
   # Linux/Mac
   export ANDROID_HOME=$HOME/Android/Sdk

   # Windows
   set ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk
   ```

2. Verify Android SDK is installed

### Warning: "Release build will be UNSIGNED"

**Cause**: Signing config not properly set or keystore file not found

**Solutions**:

1. Check `local.properties` has all 4 properties set
2. Verify `KEYSTORE_PATH` points to existing file
3. Ensure passwords are not empty strings
4. Review build output for the full warning message

## Password Rotation

To change keystore passwords (recommended annually):

```bash
# Change keystore password
keytool -storepasswd -keystore agrimarket-release.keystore

# Change key password
keytool -keypasswd -alias agrimarket -keystore agrimarket-release.keystore
```

**After rotation**:

1. Update `local.properties` with new passwords
2. Update CI/CD secrets with new passwords
3. Update password manager entries
4. Test release build to verify new passwords work

## Google Play App Signing (Recommended)

For enhanced security, consider using **Google Play App Signing**:

1. **Benefits**:
   - Google manages the final signing key
   - You can lose your upload key without losing your app
   - Automatic APK optimization for different devices

2. **Setup**:
   - Go to Play Console → Your App → Release → Setup → App Signing
   - Follow instructions to enroll (one-time, irreversible)
   - Upload your existing keystore or generate a new upload key

3. **After Enrollment**:
   - Build and sign APKs with your "upload key" (same process as above)
   - Google re-signs with the app signing key before distribution
   - Upload key can be reset if compromised

## Additional Resources

- [Android Official Signing Guide](https://developer.android.com/studio/publish/app-signing)
- [Play Console App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Keytool Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

## Support

If you encounter issues not covered in this guide:

1. Check build output for detailed error messages
2. Verify all 4 signing properties are set correctly
3. Test keystore access with `keytool -list` command
4. Ensure keystore file has not been corrupted
5. Restore from backup if necessary
