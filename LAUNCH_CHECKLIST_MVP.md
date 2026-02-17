# 🚀 Agrimarket MVP Launch Checklist

**Release Date:** 2026-02-16
**Version:** 1.0 (Build 1)
**APK:** app/build/outputs/apk/release/app-release.apk (11MB)
**Authentication:** Disabled (anonymous users for MVP)

---

## ✅ Pre-Launch Completed

- [x] Network security: HTTPS only enforced
- [x] ProGuard/R8: Code obfuscated and minified
- [x] APK signing: v2 signature scheme verified
- [x] APK size: 11MB (well under 50MB limit)
- [x] Authentication: Disabled for MVP launch
- [x] Build configuration: Release-ready

---

## 📱 Testing on Emulator (Required Before Submission)

### Start Emulator
1. Open Android Studio
2. Tools → Device Manager (or AVD Manager)
3. Click ▶️ to start an emulator (API 28+ recommended)

### Install Release APK
```bash
# Option 1: Using adb (if in PATH)
adb install app/build/outputs/apk/release/app-release.apk

# Option 2: Set ANDROID_HOME first
export ANDROID_HOME="/c/Users/Tharma/AppData/Local/Android/Sdk"
"$ANDROID_HOME/platform-tools/adb" install app/build/outputs/apk/release/app-release.apk

# Option 3: Drag and drop
# Simply drag the APK file onto the running emulator window
```

### Test Checklist (15 minutes)
- [ ] App launches without crashes
- [ ] **No login screen appears** (auth disabled)
- [ ] Browse listings screen loads
- [ ] Can view listing details
- [ ] Maps display correctly (if implemented)
- [ ] Create new listing works
- [ ] Images can be selected (camera/gallery)
- [ ] Search/filter works
- [ ] No network errors (HTTPS enforcement working)
- [ ] Navigation between screens works
- [ ] Back button behavior correct
- [ ] App doesn't crash on rotation
- [ ] Tamil/Sinhala/English languages work

**Critical:** Make sure there's NO login/authentication prompt!

---

## 🏪 Play Store Submission

### 1. Google Play Console Setup

**Create Account:**
- Go to: https://play.google.com/console
- Sign in with Google account
- Pay one-time $25 registration fee (required)

**Create App:**
1. Click "Create app"
2. Fill in:
   - App name: **Agrimarket - Sri Lanka Farmers Marketplace**
   - Default language: **English (United States)**
   - App or game: **App**
   - Free or paid: **Free**
   - Declarations: Check all boxes

### 2. Upload APK

**Internal Testing Track (Recommended First):**
1. Release → Internal testing
2. Create new release
3. Upload: `app/build/outputs/apk/release/app-release.apk`
4. Release name: "1.0 - MVP Launch"
5. Release notes:
   ```
   Initial release of Agrimarket - connecting Sri Lankan farmers with buyers.

   Features:
   - Browse fresh produce listings
   - Create and manage listings (farmers)
   - Direct communication with sellers
   - Location-based search
   - Multi-language support (Tamil, Sinhala, English)
   ```

**OR Production Track (Direct to Public):**
1. Release → Production
2. Upload APK
3. Fill same details as above

### 3. Store Listing

**App Details:**
- Short description (80 chars max):
  ```
  Connect Sri Lankan farmers with buyers. Fresh produce, fair prices.
  ```

- Full description (4000 chars max):
  ```
  Agrimarket brings Sri Lankan farmers and buyers together on one platform.

  🌾 FOR FARMERS:
  • List your fresh produce directly
  • Set your own prices
  • Reach buyers across Sri Lanka
  • No middlemen, better profits

  🛒 FOR BUYERS:
  • Buy fresh produce directly from farmers
  • Fair prices, quality guaranteed
  • Support local agriculture
  • Know your food's source

  ✨ FEATURES:
  • Multi-language support (Tamil, Sinhala, English)
  • Location-based search
  • Quality ratings and reviews
  • Secure in-app messaging
  • Easy listing management

  📍 REGIONS COVERED:
  Currently serving Northern Province (Jaffna, Kilinochchi, Mannar, Mullaitivu, Vavuniya)
  with plans to expand island-wide.

  🌱 MISSION:
  Empowering Sri Lankan farmers with direct market access while providing buyers
  with fresh, quality produce at fair prices.
  ```

**App Category:**
- Category: **Shopping** or **Food & Drink**
- Tags: agriculture, farming, marketplace, sri lanka, produce

**Contact Details:**
- Email: your-email@example.com
- Phone: +94 XXX XXX XXX (optional)
- Website: https://agrimarket-web.vercel.app (your landing page)

### 4. Graphics (REQUIRED)

You need to create these before submission:

**App Icon** (already have):
- ✅ 512x512 PNG
- Location: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

**Screenshots** (minimum 2 required):
- 📱 Phone: 1080x1920 or 1080x2340
- Minimum 2 screenshots, maximum 8

**Create Screenshots:**
```bash
# Option 1: From running emulator
1. Run app on emulator
2. Take screenshots of key screens:
   - Home/Browse screen
   - Listing details
   - Create listing
   - Search results
   - Map view (if implemented)

# Option 2: Use Android Studio
Tools → Logcat → Screenshot icon
```

**Screenshots to Capture:**
1. Browse listings screen (main screen)
2. Listing detail with images
3. Create new listing screen
4. Search/filter screen
5. Messages (if implemented)

**Feature Graphic** (REQUIRED):
- 1024x500 PNG
- Banner image shown in Play Store

**Create Feature Graphic:**
- Use Canva, Figma, or Photoshop
- Include: App name, tagline, key visual
- Keep text minimal (displays small on mobile)

### 5. Content Rating

**Fill Questionnaire:**
1. Go to: Content rating
2. Start questionnaire
3. Select: Shopping
4. Answer questions (all NO for your app):
   - Violence: NO
   - Sexual content: NO
   - Drugs: NO
   - Gambling: NO
   - User-generated content: YES (messages, listings)
5. Submit

**Rating:** Likely to be **Everyone** or **Everyone 10+**

### 6. Target Audience & Content

**Target Age:**
- Primary: 18-65+ (adults)
- Appeals to: Everyone

**Store Presence:**
- Countries: Sri Lanka (primary)
- Can add: India, other South Asian countries later

**Pricing & Distribution:**
- Free app
- No in-app purchases (for now)
- No ads (for now)

### 7. Privacy Policy (REQUIRED)

**You MUST have a privacy policy URL before submission!**

**Option 1: Create Simple Privacy Policy:**

I can help you create one, or use online generators:
- https://www.privacypolicygenerator.info/
- https://www.freeprivacypolicy.com/

**Host on:**
- Your website: https://agrimarket-web.vercel.app/privacy
- GitHub Pages (free)
- Notion (public page)

**Required sections:**
- Data collection (phone number, location, photos)
- Data usage (marketplace functionality)
- Data sharing (between buyers/sellers)
- Data retention
- User rights (delete account, data export)
- Contact information

### 8. App Access (Internal Testing)

**Email List:**
Add test users who can access Internal Testing:
- Your email
- Team members
- Friends/family for testing

**Testing Period:**
- Minimum 14 days recommended
- Get feedback, fix critical bugs
- Then promote to production

### 9. Submit for Review

**Before Submitting:**
- [ ] All required fields filled
- [ ] Privacy policy URL added
- [ ] Screenshots uploaded (minimum 2)
- [ ] Feature graphic uploaded
- [ ] Content rating completed
- [ ] APK uploaded and signed
- [ ] Store listing looks good (preview it!)

**Submit:**
1. Review everything one more time
2. Click "Send X items for review"
3. Review time: 1-7 days (usually 1-3 days)

---

## 📊 Post-Submission

### Monitor Review Status
- Check Play Console daily
- Email notifications enabled
- Typical review: 1-3 days

### Prepare for Launch
- [ ] Announce on social media when approved
- [ ] Share Play Store link with early users
- [ ] Monitor ratings and reviews
- [ ] Have support email ready for user questions

### Post-Launch Monitoring

**Week 1:**
- Daily: Check crash reports (Firebase Crashlytics)
- Daily: Read user reviews
- Daily: Monitor install/uninstall rates
- Fix critical bugs immediately

**Metrics to Watch:**
- Install rate
- Crash-free rate (target: >99%)
- ANR (App Not Responding) rate
- User ratings (target: >4.0)
- User reviews feedback

---

## 🐛 Known Limitations (Document for Support)

**Authentication:**
- ⚠️ No user accounts in v1.0
- Users are anonymous
- No saved preferences across devices
- **Fix:** Implement Firebase Auth in v1.1

**Without Auth, Users CANNOT:**
- Save favorites across devices
- Have profile/account settings
- Get personalized recommendations
- Receive push notifications (user-specific)

**What DOES Work:**
- Browse all listings
- Create listings (locally stored)
- Search and filter
- View maps
- Use messaging (if implemented)
- Multi-language support

---

## 📝 Version 1.1 Roadmap

**Priority Features:**
1. **Firebase Phone Authentication** (3-5 hours)
   - Proper user accounts
   - Cloud sync
   - Personalization

2. **Push Notifications** (2 hours)
   - New messages alerts
   - Transaction updates

3. **User Profiles** (4 hours)
   - Farmer verification
   - Ratings and reviews
   - Transaction history

**Timeline:** 1-2 weeks after v1.0 launch

---

## 🆘 Common Issues & Solutions

### "App not available in your country"
**Cause:** Country restrictions in Play Console
**Fix:** Go to Production → Countries/Regions → Add Sri Lanka

### "App requires an update"
**Cause:** Min SDK version too high
**Fix:** Your minSdk=24 (Android 7.0) is fine for 95%+ devices

### Users report crashes
**Fix:** Check Firebase Crashlytics, fix critical issues, push update

### Low ratings
**Action:** Respond to reviews, fix reported issues, improve in v1.1

---

## 📞 Support Channels

**Set up BEFORE launch:**
- [ ] Support email: support@agrimarket.lk (or Gmail)
- [ ] Facebook page (optional)
- [ ] WhatsApp business number (popular in Sri Lanka)

**In Play Store Listing:**
- List support email
- Add website URL
- Optional: Privacy policy link

---

## ✅ Final Checklist Before Submission

- [ ] Tested on emulator - no crashes
- [ ] No authentication screen appears
- [ ] All core features work
- [ ] Screenshots captured (minimum 2)
- [ ] Feature graphic created
- [ ] Privacy policy written and hosted
- [ ] Support email set up
- [ ] Store listing reviewed
- [ ] APK signed and verified
- [ ] Content rating completed
- [ ] Ready to click "Submit for review"

---

## 🎉 You're Ready to Launch!

**Next Step:** Start with Internal Testing for 7-14 days, then promote to Production.

**Estimated Timeline:**
- Internal testing setup: 30 minutes
- Review approval: 1-3 days
- Testing period: 7-14 days
- Production release: After testing complete

**Questions?** Check:
- Play Console Help: https://support.google.com/googleplay/android-developer
- This checklist: Document everything

**Good luck with your launch! 🚀**
