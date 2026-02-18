# Farmer-Friendly UX Improvements — Wave 1–3

**Completed:** February 18, 2026
**Target Users:** 60+ year old Sri Lankan farmers (low literacy, limited smartphone experience)
**Core Design Principle:** Voice → Post → Phone call. No typing required.

---

## What Was Built

### Wave 1 — Pure Visual (Zero Risk)

#### Home Screen (`IndustrialHomeScreen.kt`)
- **Greeting header**: Dark earth background, trilingual greeting (ආයුබෝවන් / வணக்கம் / Hello), farmer name + 📍 district + 👨‍🌾 avatar
- **AA text size toggle**: Cycles large/normal text without entering settings
- **Price ticker strip**: Sun-yellow 44dp bar with top-5 crop prices as scrolling LazyRow (emoji + price + ▲▼— trend)
- **Emoji tiles**: 🛒 BUY / 🌾 SELL / 📊 PRICES / 📦 ORDERS — large 40sp emoji above label in each quadrant
- **Active listing badge**: Gold badge on SELL tile showing count of live listings

#### Market Prices Screen (`IndustrialMarketPricesScreen.kt`)
- **Crop emoji at 36sp** left of each row (🌾 🍅 🥕 etc.)
- **Trend arrows**: ▲ green (price up) / ▼ red (price down) / ➡ gray (stable) + change amount
- **LIVE/OFFLINE indicator**: Green dot when connected, amber "OFFLINE — Cached data" banner when not
- **Harvest-gradient header**: Dark earth colour matching home screen

---

### Wave 2 — Input Components

#### NumericKeypadDialog (`ui/components/NumericKeypadDialog.kt`)
- Phone-style 0–9 keypad with 80dp keys and 48sp display
- Used for quantity and price entry — **no keyboard** needed
- Prevents leading zeros, green ✓ confirm button

#### DistrictPickerDialog (`ui/components/DistrictPickerDialog.kt`)
- All **25 Sri Lanka districts** listed by province, scrollable
- Trilingual display (English / Sinhala / Tamil)
- Checkmark highlights selected district — **no typing needed**
- Used in: EditProfile location, QuickListing location

#### VoiceInputButton (`ui/components/VoiceInputButton.kt`)
- Added **"123"** TextButton below mic — fallback to NumericKeypadDialog
- `onShowNumericKeypad` callback param (default no-op for backward compat)

---

### Wave 3 — Data Wiring & Confirmations

#### LastUsedPreferences (`data/preferences/LastUsedPreferences.kt`)
- DataStore persistence: `last_crop_type`, `last_price`, `last_location`
- Written on every successful listing post
- Read on QuickListing open to **pre-fill** last crop selection, price
- Read on success screen for **TTS readback** of what was posted

#### QuickListingScreen — GPS Auto-Fill (`ui/listings/QuickListingScreen.kt`)
- **🛰 GPS button** beside district picker triggers FusedLocationProviderClient
- Requests `ACCESS_COARSE_LOCATION`, then maps coordinates to nearest district via **Haversine distance** from 25 district centroids
- Farmer's district auto-fills without typing or scrolling

#### QuickListingScreen — Voice Crop Confirmation
- After voice recognition, if spoken text matches a known crop (fuzzy), shows:
  `"Did you mean 🌶️ Chili / மிளகாய் / මිරිස්? ✅ YES ❌ NO"`
- YES selects the crop, NO clears and reopens voice/keypad
- Handles background noise and accent variation

#### CreateListingSuccessScreen — TTS Readback (`ui/listings/CreateListingSuccessScreen.kt`)
- **TextToSpeech** reads: "You posted [crop] at Rs [price] from [location]. Buyers will contact you soon."
- Language adapts to current app language (Sinhala/Tamil/English)
- `DisposableEffect` ensures TTS engine is shutdown when leaving screen
- Plus: **haptic feedback** (CONFIRM) on screen appear + 80sp ✅ emoji

#### IndustrialListingDetailScreen — CALL First (`ui/listings/IndustrialListingDetailScreen.kt`)
- **CALL SELLER** = full-width 64dp green box — the first and biggest action
- Seller phone number shown as visible text above buttons
- **WhatsApp** deep-link with `ActivityNotFoundException` fallback to phone dialer
- Chat relabelled "✉️ TYPE MESSAGE" and moved below

#### EditProfileScreen — District Picker
- Location field replaced with tappable 56dp box → DistrictPickerDialog
- No keyboard needed for editing district

#### Notifications — Order/Message CTA (`ui/notifications/NotificationsScreen.kt`)
- ORDER_RECEIVED / ORDER_CONFIRMED / etc.: shows **"VIEW ORDER →"** text button directly in notification row
- NEW_MESSAGE: shows **"VIEW MESSAGE →"** button
- Tapping navigates directly to TransactionDetail — no hunting through menus

#### Portrait Lock
- `android:screenOrientation="portrait"` in AndroidManifest — app never rotates

---

## Files Created

| File | Purpose |
|------|---------|
| `ui/components/NumericKeypadDialog.kt` | Big-digit keypad (no typing) |
| `ui/components/DistrictPickerDialog.kt` | 25 districts scroll picker |
| `data/preferences/LastUsedPreferences.kt` | Persist last crop/price/location |
| `ui/listings/CreateListingSuccessViewModel.kt` | Read back last listing for TTS |

## Files Modified

| File | Change |
|------|--------|
| `ui/home/IndustrialHomeScreen.kt` | Greeting header, price ticker, emoji tiles, badge |
| `ui/home/IndustrialMarketPricesScreen.kt` | Crop emoji, trend arrows, OFFLINE banner |
| `ui/home/HomeViewModel.kt` | `activeListingCount` in state |
| `ui/listings/QuickListingScreen.kt` | GPS auto-fill, district picker, numeric keypad, voice crop confirm |
| `ui/listings/CreateListingSuccessScreen.kt` | TTS readback, haptic, ✅ tick |
| `ui/listings/IndustrialListingDetailScreen.kt` | CALL first, WhatsApp fallback, visible phone |
| `ui/listings/CreateListingViewModel.kt` | LastUsedPreferences injection, pre-fill, save on success |
| `ui/profile/EditProfileScreen.kt` | District picker for location field |
| `ui/notifications/NotificationsScreen.kt` | VIEW ORDER / VIEW MESSAGE CTA buttons |
| `ui/components/VoiceInputButton.kt` | "123" fallback to numeric keypad |
| `navigation/JaffnaMarketplaceNavigation.kt` | HomeViewModel wiring, language to success screen |
| `di/PreferencesModule.kt` | LastUsedPreferences Hilt binding |
| `AndroidManifest.xml` | Portrait orientation lock |

---

## What Was Deferred

- **Agent Mode** (village agent managing 10-15 farmers) — needs dedicated planning session
- **Full TTS with exact qty** — qty not persisted in LastUsedPreferences (only crop/price/location)
- **Swipe gesture audit** — grepped codebase, no SwipeToDismiss found; screens are already clean
- **True SMS fallback** — requires Twilio backend integration
