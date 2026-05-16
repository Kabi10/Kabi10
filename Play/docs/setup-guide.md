# Setup Guide

This guide will walk you through setting up the Android app publishing automation system.

## Prerequisites

### 1. Development Environment
- **Node.js** (v16 or higher)
- **Ruby** (for fastlane)
- **Android SDK** and **Android Studio**
- **Git** for version control

### 2. Google Play Console Setup
- Google Play Developer account ($25 one-time fee)
- App created in Google Play Console
- Google Cloud Project with Play Developer API enabled

## Step 1: Install Dependencies

### Install Node.js Dependencies
```bash
npm install
```

### Install Fastlane
```bash
# Using RubyGems
gem install fastlane

# Or using Homebrew (macOS)
brew install fastlane
```

## Step 2: Google Play Console Configuration

### 1. Create Service Account
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google Play Developer API
4. Create a service account:
   - Go to IAM & Admin > Service Accounts
   - Click "Create Service Account"
   - Name it "play-store-automation"
   - Download the JSON key file

### 2. Grant Permissions
1. Go to Google Play Console
2. Navigate to Setup > API access
3. Link your Google Cloud project
4. Grant permissions to the service account:
   - View app information and download bulk reports
   - Manage store presence
   - Manage production APKs
   - Manage testing track APKs

### 3. Store Credentials
```bash
# Create credentials directory
mkdir credentials

# Copy your service account key
cp /path/to/your/service-account-key.json credentials/google-play-service-account.json
```

## Step 3: Android Project Setup

### 1. Signing Configuration
Create a release keystore:
```bash
keytool -genkey -v -keystore android/keystore/release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Configure build.gradle
Add signing configuration to `android/app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file('../keystore/release.keystore')
            storePassword System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias 'release'
            keyPassword System.getenv("ANDROID_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Step 4: Environment Configuration

### 1. Copy Environment Template
```bash
cp fastlane/.env fastlane/.env.local
```

### 2. Update Environment Variables
Edit `fastlane/.env.local`:
```bash
# Android App Configuration
PACKAGE_NAME=com.yourcompany.yourapp

# Signing Configuration
ANDROID_KEYSTORE_PASSWORD=your_keystore_password
ANDROID_KEY_PASSWORD=your_key_password

# Optional: Slack notifications
SLACK_URL=https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK
```

## Step 5: Initialize Metadata

### 1. Create Metadata Structure
```bash
npm run metadata init
```

### 2. Update App Information
Edit the files in `metadata/en-US/`:
- `title.txt` - App name (max 50 characters)
- `short_description.txt` - Brief description (max 80 characters)
- `full_description.txt` - Detailed description (max 4000 characters)
- `release_notes.txt` - What's new in this version

### 3. Add Screenshots
Place screenshots in:
- `metadata/screenshots/en-US/phoneScreenshots/` (2-8 images)
- `metadata/screenshots/en-US/tabletScreenshots/` (optional)

## Step 6: Test the Setup

### 1. Validate Configuration
```bash
npm run metadata validate
```

### 2. Build Test APK
```bash
npm run build -- --type debug
```

### 3. Test Deployment (Internal Track)
```bash
npm run deploy:internal
```

## Step 7: CI/CD Integration

### GitHub Actions Example
Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy to Play Store

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        run: npm install

      - name: Setup Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.0'

      - name: Install Fastlane
        run: gem install fastlane

      - name: Deploy to Play Store
        env:
          PACKAGE_NAME: ${{ secrets.PACKAGE_NAME }}
          ANDROID_KEYSTORE_PASSWORD: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          ANDROID_KEY_PASSWORD: ${{ secrets.ANDROID_KEY_PASSWORD }}
        run: fastlane android deploy_to_play_store track:production
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify service account JSON is valid
   - Check API permissions in Google Play Console
   - Ensure Google Play Developer API is enabled

2. **Build Failed**
   - Check Android SDK installation
   - Verify signing configuration
   - Ensure all dependencies are installed

3. **Upload Failed**
   - Check version code is incremented
   - Verify APK/AAB is signed correctly
   - Ensure package name matches

### Getting Help

- Check the [troubleshooting guide](troubleshooting.md)
- Review fastlane logs in `fastlane/report.xml`
- Check Google Play Console for detailed error messages

## Security Best Practices

1. **Never commit sensitive files**:
   - Service account keys
   - Signing keystores
   - Environment files with passwords

2. **Use environment variables** for all sensitive data

3. **Rotate credentials** regularly

4. **Limit service account permissions** to minimum required

5. **Use separate keystores** for debug and release builds