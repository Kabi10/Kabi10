# Firebase Authentication Configuration & Troubleshooting

## ✅ Verified Information from CLI

**Firebase CLI Status:**
- ✅ Authenticated as: `kabiedu@gmail.com` 
- ✅ Project ID: `snapassist-c2ef8`
- ✅ Web App ID: `1:653668871387:web:904e13b3db2d0a86c6b0fe`
- ✅ Hosting Server: Running at `http://localhost:5000`

**Confirmed Firebase Configuration:**
```javascript
{
  "projectId": "snapassist-c2ef8",
  "appId": "1:653668871387:web:904e13b3db2d0a86c6b0fe",
  "storageBucket": "snapassist-c2ef8.firebasestorage.app",
  "apiKey": "AIzaSyB2vGBCPf_oNo47zpnyd_nTunSq-pQ-Q4E",
  "authDomain": "snapassist-c2ef8.firebaseapp.com",
  "messagingSenderId": "653668871387",
  "measurementId": "G-J9V82ERFJ8"
}
```

## 🔍 Root Cause Analysis

The Firebase configuration in your HTML file **IS CORRECT**. The `auth/invalid-api-key` error is likely caused by one of these issues:

### 1. **Google Sign-In Provider Not Enabled**
The most common cause is that Google authentication provider is not enabled in Firebase Console.

### 2. **Authorized Domains Missing**
The current domain (`localhost` or your hosting domain) may not be in the authorized domains list.

### 3. **API Key Restrictions**
The API key might have HTTP referrer restrictions that don't include your current domain.

## 🛠️ Step-by-Step Solution

### Step 1: Enable Google Sign-In Provider

1. **Open Firebase Console**: https://console.firebase.google.com/
2. **Select Project**: `snapassist-c2ef8`
3. **Go to Authentication → Sign-in method**
4. **Click Google Provider**
5. **Enable** the provider
6. **Set Project support email**: Use `kabiedu@gmail.com`
7. **Save**

### Step 2: Add Authorized Domains

**Still in Authentication → Settings → Authorized domains:**

Add these domains:
- `localhost` ✅ (for development)
- `snapassist-c2ef8.web.app` ✅ (Firebase Hosting)
- `snapassist-c2ef8.firebaseapp.com` ✅ (Firebase Hosting)

### Step 3: Check API Key Restrictions

1. **Google Cloud Console**: https://console.cloud.google.com/
2. **Select Project**: `snapassist-c2ef8`
3. **APIs & Services → Credentials**
4. **Find**: "Browser key (auto created by Firebase)" or similar
5. **Click** to edit the API key
6. **Application restrictions**:
   - Select "HTTP referrers (web sites)"
   - Add referrers:
     - `localhost/*`
     - `*.firebaseapp.com/*`
     - `*.web.app/*`
7. **API restrictions**: 
   - Ensure these APIs are enabled:
     - Identity and Access Management (IAM) API
     - Firebase Authentication API
     - Cloud Firestore API

### Step 4: Test the Configuration

**Open the local hosting server:**
```
http://localhost:5000
```

**Expected Results:**
1. ✅ No Firebase initialization errors in console
2. ✅ "Sign in with Google" button works
3. ✅ Google OAuth popup appears
4. ✅ After authentication: "Signed in as your-email@domain.com"

## 🧪 Testing Commands

### Test Authentication Flow
```bash
# Start local hosting (already running)
cd C:\Dev\Snapassist
npx firebase serve --only hosting --port 5000

# Open browser to http://localhost:5000
# Open Developer Tools → Console
# Look for any Firebase errors
```

### Verify Authentication Configuration
```bash
# Check current project
npx firebase projects:list

# Verify web app configuration  
npx firebase apps:sdkconfig WEB 1:653668871387:web:904e13b3db2d0a86c6b0fe
```

## 🚨 Quick Debug Steps

### If Still Getting `auth/invalid-api-key`:

1. **Regenerate API Key**:
   - Google Cloud Console → Credentials
   - Create new "API Key" 
   - Configure for "HTTP referrers"
   - Update Firebase project with new key

2. **Check Browser Network Tab**:
   - Look for failed requests to `identitytoolkit.googleapis.com`
   - Check response for specific error messages

3. **Try Incognito Mode**:
   - Rules out browser cache/extension issues

### If Getting `signInWithGoogle is not defined`:

This error is secondary to the Firebase initialization failure. Once authentication is properly configured, this will resolve automatically.

## 📝 Configuration Verification Checklist

**Firebase Console Checklist:**
- [ ] Authentication → Sign-in method → Google **Enabled**
- [ ] Authentication → Settings → Authorized domains include `localhost`
- [ ] Project settings → Web apps → Snapassist Dashboard **exists**

**Google Cloud Console Checklist:**
- [ ] APIs & Services → Credentials → Browser key **configured**
- [ ] API key restrictions → HTTP referrers **include localhost, *.firebaseapp.com**
- [ ] API restrictions → Required APIs **enabled**

**Local Testing Checklist:**
- [ ] `npx firebase serve --only hosting` **running**
- [ ] `http://localhost:5000` **loads without Firebase errors**
- [ ] Browser console **shows no Firebase initialization errors**
- [ ] "Sign in with Google" **triggers OAuth popup**

## 🔄 Expected Working Flow

1. **Load Dashboard** → Firebase initializes successfully
2. **Click "Sign in with Google"** → Google OAuth popup opens  
3. **Select Google Account** → Authentication succeeds
4. **Dashboard Updates** → Shows "Signed in as [email]"
5. **Device List** → Loads from Firestore
6. **Commands Work** → SNAP, LAUNCH, LIST_APPS function properly

## 🎯 Next Steps After Authentication Works

Once authentication is working:

1. **Deploy to Firebase Hosting**:
   ```bash
   npx firebase deploy --only hosting
   ```

2. **Test Cloud Functions**:
   - Ensure Functions are deployed (requires Blaze plan)
   - Test SNAP, LAUNCH, LIST_APPS commands

3. **Verify Android Integration**:
   - Use DiagnosticsActivity to register FCM token
   - Test end-to-end SNAP from dashboard to device

The Firebase configuration is correct - the issue is in the Console settings. Follow the steps above and authentication should work perfectly! 🚀