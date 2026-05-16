# Android App Publishing Automation Setup

This repository contains automation tools for publishing Android apps to Google Play Store using both fastlane and Google Play Developer API approaches.

## 🚀 Quick Start

1. **Prerequisites Setup**
   ```bash
   # Install fastlane
   gem install fastlane

   # Install Node.js dependencies for API automation
   npm install
   ```

2. **Configure Credentials**
   - Place your Google Play service account JSON in `credentials/google-play-service-account.json`
   - Update `fastlane/.env` with your app details
   - Configure signing keys in `android/keystore/`

3. **Run Automation**
   ```bash
   # Using fastlane
   fastlane android deploy_to_play_store

   # Using Node.js API
   npm run deploy
   ```

## 📁 Project Structure

```
├── android/                    # Android project files
│   ├── app/
│   ├── keystore/              # Signing keys
│   └── build.gradle
├── fastlane/                  # Fastlane configuration
│   ├── Fastfile
│   ├── Appfile
│   └── .env
├── scripts/                   # Custom automation scripts
│   ├── google-play-api.js
│   ├── build-automation.js
│   └── metadata-sync.js
├── metadata/                  # Store listing content
│   ├── en-US/
│   ├── es-ES/
│   └── screenshots/
├── credentials/               # Service account keys
├── config/                    # Configuration files
└── docs/                     # Documentation
```

## 🔧 Features

### Automated Tasks
- ✅ APK/AAB build generation
- ✅ Version management and increment
- ✅ Upload to Google Play Console
- ✅ Release track management (internal/alpha/beta/production)
- ✅ Staged rollouts with percentage deployment
- ✅ Metadata and store listing updates
- ✅ Screenshot and asset management
- ✅ Multi-language content support
- ✅ Content rating automation

### Manual Tasks Checklist
- ⚠️ Initial app creation in Google Play Console
- ⚠️ Developer account verification
- ⚠️ Policy acceptance and legal agreements
- ⚠️ Manual review responses
- ⚠️ Country-specific regulatory requirements

## 📖 Documentation

- [Setup Guide](docs/setup-guide.md)
- [Fastlane Configuration](docs/fastlane-setup.md)
- [API Integration](docs/api-integration.md)
- [Troubleshooting](docs/troubleshooting.md)

## 🔒 Security

- Service account keys are gitignored
- Signing keys stored securely
- Environment variables for sensitive data