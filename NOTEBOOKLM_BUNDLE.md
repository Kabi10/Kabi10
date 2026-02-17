# Agrimarket Project Knowledge Base
## For NotebookLM - Complete Project Context

**Generated:** February 14, 2026
**Purpose:** Feed this into Google NotebookLM to enable AI-assisted planning, brainstorming, and decision-making for the Agrimarket project.

---

# SECTION 1: PROJECT OVERVIEW

## What is Agrimarket?

Sri Lanka Farmers Marketplace is an offline-first Android marketplace app for Sri Lankan farmers. It connects farmers with buyers, supports Tamil, Sinhala, and English, and is designed for low-end Android devices with poor internet connectivity.

**Live URLs:**
- Backend API: https://backend-psi-tan-18.vercel.app
- Web Landing: https://agrimarket-landing.vercel.app
- Database: Supabase (PostgreSQL) with Row Level Security
- GitHub: https://github.com/Kabi10/Srilanka-Farmers-Marketplace

**Package:** `com.senthapps.slagrimarket`

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Android UI | Jetpack Compose + Material Design 3 |
| Language | Kotlin 2.0 |
| Architecture | MVVM + Repository + Clean Architecture |
| DI | Hilt (Dagger) |
| Local DB | Room (offline-first with sync) |
| Networking | Retrofit + OkHttp |
| Real-time | Supabase Realtime (for chat) |
| Backend | Node.js + Express on Vercel (serverless) |
| Database | Supabase (PostgreSQL) |
| Auth | Custom OTP (phone-based, Sri Lankan numbers +94) |
| Live Prices | HDX HAPI (WFP food price data) |
| CI/CD | GitHub Actions + CodeQL |
| Crash Reporting | Firebase Crashlytics |
| Image Upload | Supabase Storage |

## Key Design Principles

1. **Tamil-first**: Primary interface in Tamil with English and Sinhala support
2. **Offline-first**: Full functionality without internet; sync when connected
3. **Mobile-first**: Optimized for low-end Android devices (min Android 7.0 / API 24)
4. **Simple verification**: OTP-based auth for Sri Lankan phone numbers (+94)
5. **Industrial design**: Custom "Human Industrial" design system with large touch targets for farmers

---

# SECTION 2: CURRENT STATUS (as of Feb 14, 2026)

## What's Working

| Feature | Backend | Android | Status |
|---------|---------|---------|--------|
| OTP Authentication | Deployed | Wired | Working (mock SMS in dev) |
| Listings CRUD | Deployed | Wired | Working (offline-first) |
| Transactions | Deployed | Wired | Working |
| Market Prices | Deployed (584 live prices from HDX HAPI) | Wired | Working |
| Live Price Refresh | Vercel cron (daily 6AM UTC) | Auto via API | Working |
| Photo-First Quick Listing | Uses existing API | New screen | Built (not tested on device) |
| Accessibility/Large Text | N/A | DataStore prefs | Built (not tested on device) |
| Offline Sync | Deployed | SyncWorker + Room | Working |
| Web Landing | Deployed | N/A | Live |
| Privacy/Terms Pages | Deployed | N/A | Live |

## What's UI-Only (No Backend Yet)

These features have Android UI screens but are Room-only (local database) with no backend API wiring:

| Feature | Android Screen | Backend Route | Wiring |
|---------|---------------|---------------|--------|
| Chat/Messaging | ConversationsScreen, ChatScreen | messages.js (created) | NOT wired to Android |
| Favorites | FavoritesScreen | favorites.js (created) | NOT wired to Android |
| Reviews | ReviewScreen | reviews.js (created) | NOT wired to Android |
| Notifications | NotificationsScreen | notifications.js (created) | NOT wired to Android |
| Profile Editing | ProfileScreen | users/profile PUT (exists) | NOT wired to Android |

**Key insight:** The backend routes for all 5 features have been created and deployed. The Android app has the UI screens. What's missing is the Retrofit API service interfaces and the repository wiring to connect them.

## Recent Security Fix

- Supabase anon key was hardcoded in `build.gradle.kts` and pushed to GitHub (GitGuardian alert)
- Fixed: moved to `local.properties` (gitignored), reads via `Properties().load()`
- Key rotation pending on Supabase dashboard

---

# SECTION 3: ARCHITECTURE

## Android App Structure

```
app/src/main/java/com/senthapps/slagrimarket/
+-- data/
|   +-- api/           # Retrofit service interfaces (11 services)
|   +-- dao/           # Room DAOs
|   +-- model/         # Data models (Room entities)
|   +-- preferences/   # DataStore preferences (Language, Accessibility, Auth)
|   +-- repository/    # Repositories (offline-first pattern)
|   +-- sync/          # Supabase Realtime, SyncManager
+-- di/                # Hilt modules (Network, Database, Supabase, Firebase, Preferences)
+-- navigation/        # JaffnaMarketplaceNavigation.kt (single-activity, Compose Nav)
+-- ui/
|   +-- analytics/     # Analytics dashboard
|   +-- auth/          # Phone input + OTP verification screens
|   +-- chat/          # Conversations list + Chat screen
|   +-- components/    # Shared UI components (buttons, form fields, search bar)
|   +-- favorites/     # Favorites screen
|   +-- help/          # Help, FAQ, Contact, Privacy, Terms screens
|   +-- home/          # Home screen + Market prices screen
|   +-- listings/      # Listing detail, create listing, quick listing screens
|   +-- notifications/ # Notifications screen
|   +-- profile/       # Profile screen
|   +-- search/        # Search + Advanced search screens
|   +-- settings/      # Settings (language + accessibility)
|   +-- theme/         # Human Industrial design system (colors, typography, spacing)
|   +-- transactions/  # Transaction list + detail + create screens
+-- util/              # Utilities (RelativeTimeUtil, ErrorHandler)
```

## Offline-First Data Flow

```
User Action
    |
    v
ViewModel (StateFlow)
    |
    v
Repository
    |
    +---> Room DB (immediate, cached) ---> UI shows cached data
    |
    +---> Retrofit API (background) ---> Update Room ---> UI refreshes
    |
    +---> If offline: queue in LocalOp table ---> SyncWorker retries later
```

## Backend Structure

```
backend/src/
+-- routes/
|   +-- auth.js          # OTP send/verify
|   +-- listings.js      # CRUD + search + filters
|   +-- transactions.js  # CRUD + status workflow
|   +-- market-prices.js # GET prices + POST refresh-live
|   +-- messages.js      # Conversations + messages
|   +-- favorites.js     # Add/remove/check favorites
|   +-- reviews.js       # CRUD reviews
|   +-- notifications.js # CRUD notifications
|   +-- sync.js          # Offline operation sync
|   +-- users.js         # Profile CRUD
+-- services/
|   +-- smsService.js    # Twilio/Dialog/Mobitel/Mock SMS
|   +-- livePriceService.js  # HDX HAPI integration (WFP prices)
+-- middleware/
|   +-- auth.js          # JWT verification
|   +-- rateLimit.js     # Rate limiting
|   +-- requestSigning.js # HMAC signature validation
+-- database/
|   +-- schema.sql       # Full PostgreSQL schema
|   +-- connection.js    # Connection pooling
+-- config/
|   +-- supabase.js      # Supabase client config
+-- server.js            # Express app entry point
```

## Database Tables

| Table | Purpose | Rows (est.) |
|-------|---------|-------------|
| users | Farmer/buyer profiles with phone auth | ~100 |
| listings | Agricultural product listings | ~500 |
| transactions | Buy/sell transactions | ~200 |
| conversations | Chat thread metadata | ~50 |
| messages | Individual chat messages | ~500 |
| favorites | Saved listings | ~100 |
| reviews | Transaction reviews (1-5 stars) | ~50 |
| notifications | System/user notifications | ~200 |
| market_prices | Live commodity prices (592 rows from HDX HAPI) | 592 |
| local_ops | Offline operation queue | varies |
| otp_verifications | Phone OTP codes | varies |
| sync_metadata | Sync tracking per entity | varies |

---

# SECTION 4: LIVE MARKET PRICES

## Data Pipeline

```
HDX HAPI (WFP)  --->  livePriceService.js  --->  Supabase market_prices table
     |                      |                            |
     |                      |                            v
  38 commodities      Transform + trilingual      Android MarketPriceRepository
  2 SL markets        name mapping                       |
  Monthly data                                           v
  LKR currency                                  IndustrialMarketPricesScreen
```

## Key Details

- **Source:** World Food Programme via HDX HAPI API
- **Commodities:** 36 mapped (rice varieties, vegetables, fruits, fish, protein, staples)
- **Markets:** 13 Sri Lankan cities mapped with trilingual names
- **Refresh:** Vercel cron daily at 6AM UTC
- **Current count:** 584 live prices + 8 seed data = 592 total
- **Trilingual:** Every commodity and market has English, Tamil, and Sinhala names

---

# SECTION 5: IMPLEMENTATION PLAN (Remaining Work)

## What's Done (Phases 1-2 + partial 3, 6)

- Backend security fixes (SQL injection, OTP crypto, Supabase fallback)
- Database schema (all 12 tables with indexes and triggers)
- All 5 new backend routes (messages, favorites, reviews, notifications, market-prices refresh)
- 14 Supabase migrations pushed
- Vercel deployment verified
- Web landing page with privacy/terms
- Live market prices integration
- Accessibility/large text mode
- Photo-first quick listing screen

## What's Remaining

### Phase 4: Android API Wiring (the big one)
Create Retrofit interfaces and wire repositories for:
1. MessageApiService.kt + MessageRepository wiring
2. FavoriteApiService.kt + FavoriteRepository wiring
3. ReviewApiService.kt + ReviewRepository wiring
4. NotificationApiService.kt + NotificationRepository wiring
5. UserApiService.kt + AuthRepository profile wiring

### Phase 5: Real-Time Chat
1. SupabaseModule.kt (Hilt DI for Supabase client)
2. ChatRealtimeService.kt (subscribe to postgres_changes on messages)
3. Update ChatViewModel to use real-time subscriptions

### Phase 6: Play Store (partially done)
- Privacy/terms pages: DONE
- Store listing content: DONE
- Release signing: configured (needs keystore password)
- Remove cleartext traffic: needs to be done
- Build signed AAB: needs keystore

---

# SECTION 6: HOW TO USE CLAUDE CODE EFFECTIVELY

## What Claude Code Can Do

1. **Read and edit any file** in the codebase
2. **Run builds and tests** (`./gradlew assembleDebug`, `./gradlew testDebugUnitTest`)
3. **Deploy to Vercel** (`vercel --prod`)
4. **Push Supabase migrations** (`npx supabase db push`)
5. **Create and manage git commits/PRs**
6. **Run backend locally** (`npm run dev`)
7. **Test APIs** via curl
8. **Search the web** for documentation and solutions

## Best Practices for Working with Claude Code on This Project

1. **Always test APIs before implementing** - curl the endpoint first
2. **Build after every change** - `./gradlew assembleDebug` catches errors fast
3. **Follow existing patterns** - look at ListingRepository for offline-first, NetworkModule for DI
4. **Use plan mode** for multi-file changes - prevents wasted work
5. **Commit incrementally** - small focused commits are easier to review
6. **Check component signatures** - IndustrialFormField uses `errorMessage` not `error`

## Common Commands

```bash
# Android build
./gradlew assembleDebug

# Android tests
./gradlew testDebugUnitTest

# Backend local dev
cd backend && npm run dev

# Deploy backend
cd backend && vercel --prod

# Push Supabase migrations
cd supabase && npx supabase db push

# Test API endpoint
curl https://backend-psi-tan-18.vercel.app/api/v1/market-prices?limit=5

# Refresh live prices
curl -X POST https://backend-psi-tan-18.vercel.app/api/v1/market-prices/refresh-live
```

## Patterns to Follow

### Adding a new API-backed feature:
1. Create Retrofit interface in `data/api/`
2. Register in `di/NetworkModule.kt`
3. Wire repository in `data/repository/` (offline-first: Room cache + API refresh)
4. Inject repository in ViewModel
5. Connect ViewModel to Composable screen
6. Add navigation route in `JaffnaMarketplaceNavigation.kt`

### Adding a new screen:
1. Create Composable in appropriate `ui/` subfolder
2. Use `HumanIndustrial` design tokens (colors, typography, spacing)
3. Support all 3 languages via `when(language)` pattern
4. Add `Screen.Name` to sealed class in navigation
5. Add `composable(Screen.Name.route)` in NavHost

---

# SECTION 7: BRAINSTORMING PROMPTS FOR NOTEBOOKLM

Use these prompts to get the most out of NotebookLM with this knowledge base:

### Planning
- "What are the remaining features that need Android API wiring? Prioritize them."
- "What's the fastest path to getting the app on Google Play Store?"
- "What are the risks of launching without real-time chat?"

### Architecture
- "How does the offline-first pattern work in this app? What could break?"
- "What would need to change to support 100,000 users?"
- "How should I implement push notifications with Firebase Cloud Messaging?"

### Product
- "What features would be most valuable to Sri Lankan farmers?"
- "How can we improve the onboarding experience for farmers who aren't tech-savvy?"
- "What's missing from the marketplace flow that buyers would need?"

### Technical Debt
- "What are the biggest technical risks in the current codebase?"
- "Which parts of the app have the least test coverage?"
- "What security improvements should be prioritized?"

### Business
- "What would a monetization strategy look like for this app?"
- "How does this compare to existing agricultural marketplaces in South Asia?"
- "What partnerships (government, NGO, telco) would accelerate adoption?"

---

# SECTION 8: KEY FILES REFERENCE

| File | Purpose | When to Read |
|------|---------|-------------|
| `CLAUDE.md` | AI agent instructions, project rules | Before any coding session |
| `README.md` | Project overview, quick start | Onboarding |
| `docs/DOCUMENTATION.md` | Full architecture + dev guide | Architecture questions |
| `PRODUCTION_READINESS_ASSESSMENT.md` | Security/testing/deploy checklist | Launch planning |
| `backend/README.md` | API endpoints, SMS config, deployment | Backend work |
| `backend/src/database/schema.sql` | Complete DB schema (12 tables) | Data modeling |
| `navigation/JaffnaMarketplaceNavigation.kt` | All screen routes + navigation | Adding screens |
| `di/NetworkModule.kt` | All Retrofit service registrations | Adding API services |
| `backend/src/services/livePriceService.js` | HDX HAPI integration | Price data work |
| `app/build.gradle.kts` | Dependencies, build config, signing | Build issues |

---

*This document was generated by Claude Code for use with Google NotebookLM. It contains the complete context needed to understand, plan, and build the Agrimarket project.*
