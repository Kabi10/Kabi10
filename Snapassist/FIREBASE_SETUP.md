# Firebase Setup and Deployment Guide

## Overview

This guide covers the complete Firebase setup for Snapassist, including CLI installation, project initialization, and deployment of Functions and Hosting.

## Prerequisites

- **Node.js 18+**: Required for Firebase Functions
- **npm**: Package manager for Firebase CLI and dependencies
- **Firebase Account**: Access to Firebase Console
- **Android Studio**: For testing the Android app integration

## 🚀 Quick Start

### 1. Firebase CLI Setup

```bash
# Install Firebase CLI globally (if not done via npm locally)
npm install -g firebase-tools

# Or use the local installation
npx firebase --version
```

### 2. Firebase Login

```bash
# Login to Firebase (opens browser for authentication)
npx firebase login

# Verify login status
npx firebase projects:list
```

### 3. Project Selection

Update `.firebaserc` with your Firebase project ID:

```json
{
  "projects": {
    "default": "your-firebase-project-id"
  }
}
```

### 4. Install Dependencies

```bash
# Install root dependencies
npm install

# Install Functions dependencies
cd functions
npm install
cd ..
```

### 5. Deploy Everything

```bash
# Deploy all components (Functions, Hosting, Firestore Rules, Storage Rules)
npx firebase deploy

# Or deploy individually
npx firebase deploy --only functions
npx firebase deploy --only hosting
npx firebase deploy --only firestore:rules
npx firebase deploy --only storage
```

## 📁 Project Structure

```
c:\Dev\Snapassist\
├── android-app/                    # Android application
│   ├── app/
│   │   ├── src/
│   │   ├── build.gradle.kts
│   │   └── google-services.json    # Firebase config for Android
│   └── ...
├── functions/                      # Firebase Functions (TypeScript)
│   ├── src/
│   │   └── index.ts                # Cloud Functions implementation
│   ├── package.json
│   ├── tsconfig.json
│   └── lib/                        # Compiled JavaScript (generated)
├── public/                         # Firebase Hosting files
│   └── index.html                  # Remote control web interface
├── firebase.json                   # Firebase configuration
├── .firebaserc                     # Project selection
├── firestore.rules                 # Firestore security rules
├── firestore.indexes.json          # Firestore query indexes
├── storage.rules                   # Storage security rules
├── package.json                    # Root dependencies
└── README.md
```

## 🔧 Firebase Configuration

### firebase.json

The main configuration file defining:

- **Hosting**: Serves the remote control web interface
- **Functions**: TypeScript Cloud Functions with build configuration
- **Storage**: Security rules for photo uploads
- **Firestore**: Database rules and indexes
- **Emulators**: Local development environment

### Key Configuration Features

```json
{
  "hosting": {
    "public": "public",
    "rewrites": [{"source": "**", "destination": "/index.html"}]
  },
  "functions": {
    "source": "functions",
    "predeploy": ["npm --prefix \"$RESOURCE_DIR\" run build"]
  },
  "emulators": {
    "hosting": {"port": 5000},
    "functions": {"port": 5001},
    "firestore": {"port": 8080},
    "storage": {"port": 9199}
  }
}
```

## ⚙️ Firebase Functions

### Available Endpoints

#### 1. Send SNAP Command
```
POST https://your-region-your-project.cloudfunctions.net/sendSnapCommand
```

**Request Body:**
```json
{
  "deviceTokens": ["fcm_token_1", "fcm_token_2"],
  "userId": "user_uid_optional"
}
```

**Response:**
```json
{
  "message": "SNAP command processing completed",
  "totalDevices": 2,
  "successful": 1,
  "failed": 1,
  "results": [
    {"token": "abc123...", "success": true, "messageId": "message_id"},
    {"token": "def456...", "success": false, "error": "Token not registered"}
  ]
}
```

#### 2. Get Active Devices
```
GET https://your-region-your-project.cloudfunctions.net/getActiveDevices?userId=user_uid
```

**Response:**
```json
{
  "userId": "user_uid",
  "deviceCount": 2,
  "devices": [
    {
      "deviceId": "device_id_1",
      "deviceModel": "Pixel 7",
      "deviceManufacturer": "Google",
      "androidVersion": "14",
      "lastUpdated": "2023-12-01 10:30:00",
      "token": "fcm_token"
    }
  ]
}
```

#### 3. Device Update Trigger (Automatic)

Firestore trigger that activates when devices are added/updated in `/devices/{deviceId}`:
- Logs new device registrations
- Tracks token updates
- Monitors device status changes

#### 4. Cleanup Schedule (Automatic)

Daily cleanup function (2 AM UTC) that removes inactive devices older than 30 days.

### Local Development

```bash
# Start Firebase Emulators
npx firebase emulators:start

# Access Emulator UI
# http://localhost:4000

# Test Functions locally
# http://localhost:5001/your-project/us-central1/sendSnapCommand
```

### Building and Deployment

```bash
# Build Functions (TypeScript → JavaScript)
cd functions
npm run build

# Deploy only Functions
npx firebase deploy --only functions

# View Function logs
npx firebase functions:log
```

## 🌐 Firebase Hosting

### Remote Control Interface

The hosting serves a web-based remote control interface at:
```
https://your-project.web.app
```

### Features

- **Firebase Connection**: Connect to your Firebase project
- **Device Management**: View registered devices
- **Remote SNAP**: Send capture commands to devices
- **Real-time Logs**: View command status and results
- **FCM Token Input**: Manual token entry for testing

### Local Development

```bash
# Serve hosting locally
npx firebase serve --only hosting

# Access at http://localhost:5000
```

## 📊 Firestore Database

### Collections

#### `/devices/{deviceId}`

**Document Structure:**
```json
{
  "token": "fcm_registration_token",
  "deviceId": "android_device_id",
  "deviceModel": "Pixel 7",
  "deviceManufacturer": "Google",
  "androidVersion": "14",
  "appVersion": "1.0",
  "userId": "firebase_user_uid",
  "lastUpdated": "2023-12-01 10:30:00",
  "isActive": true
}
```

### Security Rules

- Users can only access their own device documents
- Authenticated access required
- Document ID should match Android device ID
- UserId must match authenticated user's UID

### Indexes

Optimized for common queries:
- `userId + isActive + lastUpdated` (descending)
- `isActive + lastUpdated` (ascending) for cleanup

## 🔒 Security Configuration

### Storage Rules (storage.rules)

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /shots/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Firestore Rules (firestore.rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /devices/{deviceId} {
      allow read, write: if request.auth != null && 
                           request.auth.uid == request.resource.data.userId;
    }
  }
}
```

## 🧪 Testing Guide

### 1. Emulator Testing

```bash
# Start all emulators
npx firebase emulators:start

# Test Functions with curl
curl -X POST http://localhost:5001/your-project/us-central1/sendSnapCommand \
  -H "Content-Type: application/json" \
  -d '{"deviceTokens": ["test_token"], "userId": "test_user"}'
```

### 2. Android Integration Testing

1. **Deploy Functions**: `npx firebase deploy --only functions`
2. **Update Android App**: Point to production Functions URLs
3. **Test End-to-End**: Android app → Firestore → Functions → FCM → Android app

### 3. Web Interface Testing

1. **Deploy Hosting**: `npx firebase deploy --only hosting`
2. **Access Web Interface**: `https://your-project.web.app`
3. **Connect to Firebase**: Enter project ID
4. **Test Commands**: Send SNAP commands to registered devices

## 🚀 Deployment Commands

```bash
# Full deployment
npx firebase deploy

# Individual deployments
npx firebase deploy --only functions
npx firebase deploy --only hosting
npx firebase deploy --only firestore:rules
npx firebase deploy --only storage

# Deploy to specific environment
npx firebase deploy --project production
npx firebase deploy --project staging
```

## 📋 Production Checklist

### Before First Deploy

- [ ] Create Firebase project in Console
- [ ] Enable Authentication, Firestore, Functions, Hosting, Storage
- [ ] Add `google-services.json` to Android app
- [ ] Update `.firebaserc` with correct project ID
- [ ] Install dependencies: `npm install && cd functions && npm install`

### Deployment Verification

- [ ] Functions deployed successfully: Check Firebase Console
- [ ] Hosting accessible: Visit `https://your-project.web.app`
- [ ] Storage rules active: Test unauthorized access (should fail)
- [ ] Firestore rules active: Test cross-user access (should fail)
- [ ] Android app can connect and register devices

### Security Verification

- [ ] Test unauthenticated access to Firestore (should fail)
- [ ] Test cross-user device access (should fail)
- [ ] Test storage access with wrong user (should fail)
- [ ] Verify HTTPS-only access to Functions

## 🔍 Monitoring and Logs

### Function Logs

```bash
# Real-time logs
npx firebase functions:log

# Specific function logs
npx firebase functions:log --only sendSnapCommand

# View in Firebase Console
# https://console.firebase.google.com/project/your-project/functions/logs
```

### Performance Monitoring

- Firebase Console → Functions → Performance tab
- Monitor execution time, memory usage, error rates
- Set up alerts for failures or performance degradation

### Analytics

- Firebase Console → Analytics (if enabled)
- Track user engagement with web interface
- Monitor device registration patterns

## 🆘 Troubleshooting

### Common Issues

**Firebase CLI not found:**
```bash
# Use npx for local installation
npx firebase --version

# Or install globally
npm install -g firebase-tools
```

**Permission denied during deploy:**
```bash
# Re-authenticate
npx firebase login --reauth
```

**Function deployment timeout:**
```bash
# Increase timeout
npx firebase deploy --only functions --force
```

**CORS errors in web interface:**
- Functions automatically include CORS headers
- Check browser console for specific errors
- Verify Firebase project ID in web interface

### Debug Mode

```bash
# Enable debug logging
DEBUG=* npx firebase deploy --only functions

# Verbose emulator logs
npx firebase emulators:start --debug
```

## 📞 Support Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)
- [Cloud Functions Documentation](https://firebase.google.com/docs/functions)
- [Firebase Hosting Documentation](https://firebase.google.com/docs/hosting)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)