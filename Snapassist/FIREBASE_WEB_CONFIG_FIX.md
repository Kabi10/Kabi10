# Firebase Web Configuration Fix Guide

## Current Errors
- `Firebase: Error (auth/invalid-api-key)` - API key is invalid or restricted
- `signInWithGoogle is not defined` - Function reference error (likely secondary to auth init failure)

## Root Cause Analysis

The Firebase web app configuration may have one or more issues:
1. **Web App Not Registered**: The web app might not be properly registered in Firebase Console
2. **API Key Restrictions**: The API key might have domain restrictions that don't include your current domain
3. **Authentication Not Enabled**: Google Sign-in might not be enabled in Firebase Auth
4. **Domain Authorization**: Current domain might not be in the authorized list

## Step-by-Step Fix

### 1. Verify/Register Web App in Firebase Console

**Go to Firebase Console → Project Settings → Your apps**

If no web app exists:
1. Click "Add App" → Web (</>) 
2. App nickname: `Snapassist Dashboard`
3. **✅ Enable Firebase Hosting**
4. Click "Register app"

If web app exists, click on it to get the config object.

### 2. Get Correct Firebase Configuration

**Copy the configuration object from Firebase Console:**

```javascript
// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "your-actual-api-key",
  authDomain: "snapassist-c2ef8.firebaseapp.com",
  projectId: "snapassist-c2ef8",
  storageBucket: "snapassist-c2ef8.firebasestorage.app",
  messagingSenderId: "653668871387",
  appId: "your-actual-app-id",
  measurementId: "your-measurement-id"
};
```

### 3. Enable Google Authentication

**Firebase Console → Authentication → Sign-in method:**
1. Click **Google** provider
2. **Enable** the provider
3. Add your **Project support email**
4. **Save**

### 4. Configure Authorized Domains

**Firebase Console → Authentication → Settings → Authorized domains:**

Add these domains:
- `localhost` (for development)
- `snapassist-c2ef8.web.app` (Firebase Hosting)
- `snapassist-c2ef8.firebaseapp.com` (Firebase Hosting)
- Any custom domains you're using

### 5. Check API Key Restrictions

**Google Cloud Console → APIs & Services → Credentials:**
1. Find your Web API key (usually named "Browser key (auto created by Firebase)")
2. Click on it to edit
3. **Application restrictions:**
   - Select "HTTP refererers (web sites)"
   - Add these referrers:
     - `localhost/*`
     - `*.firebaseapp.com/*`
     - `*.web.app/*`
4. **API restrictions:**
   - Ensure these APIs are enabled:
     - Firebase Authentication API
     - Cloud Firestore API
     - Firebase Functions API
     - Cloud Storage API

### 6. Update Web Configuration

Replace the Firebase config in `public/index.html` with the correct one from Firebase Console.

## Quick Test Commands

### Test Firebase CLI Connection
```bash
cd C:\Dev\Snapassist
firebase projects:list
# Should show snapassist-c2ef8 as active project
```

### Test Local Hosting
```bash
firebase serve --only hosting
# Should serve at http://localhost:5000
```

### Test Functions (if deployed)
```bash
firebase functions:log
# Should show recent function calls
```

## Alternative: Regenerate Web App

If issues persist, delete and recreate the web app:

1. **Firebase Console → Project Settings → Your apps**
2. Find your web app → **Delete app**
3. Create new web app (follow Step 1 above)
4. Update `public/index.html` with new config
5. Redeploy: `firebase deploy --only hosting`

## Debugging Commands

### Check Current Auth State in Browser Console
```javascript
// Open browser dev tools on the dashboard page
firebase.auth().currentUser;  // Should show null or user object
firebase.auth().onAuthStateChanged(user => console.log('User:', user));
```

### Verify Config in Browser Console
```javascript
// Check if Firebase is properly initialized
firebase.apps.length;  // Should be > 0
firebase.app().options;  // Should show your config
```

## Expected Working Flow

1. Open dashboard → No authentication errors in console
2. Click "Sign in with Google" → Google OAuth popup appears
3. Select account → Sign in successful
4. Dashboard shows "Signed in as your-email@domain.com"
5. Device dropdown populated (if Android has registered token)
6. All buttons (SNAP, LAUNCH, LIST_APPS) work without auth errors

## Common Issues and Solutions

### Issue: "popup-blocked"
**Solution**: The code already has fallback to redirect method. Just allow popups or let it redirect.

### Issue: "auth/unauthorized-domain"  
**Solution**: Add current domain to Firebase Auth authorized domains.

### Issue: "functions/unauthenticated"
**Solution**: Verify Google Sign-in is enabled and user is authenticated.

### Issue: API key errors persist
**Solution**: Generate new API key in Google Cloud Console and update Firebase config.

## Production Deployment Checklist

- [ ] Web app registered in Firebase Console
- [ ] Google Sign-in enabled with project support email
- [ ] Authorized domains include hosting domains
- [ ] API key configured for web referrers
- [ ] Functions deployed and callable
- [ ] Firestore security rules allow authenticated reads/writes
- [ ] Storage rules allow authenticated uploads

Once authentication works, the dashboard should successfully communicate with Cloud Functions and the Android app.