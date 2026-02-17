# Agrimarket - Sri Lanka Farmers Marketplace

**Android (Kotlin)**: MVVM + Compose + Hilt + Room + Retrofit | **Backend (Node.js)**: Express + Supabase PostgreSQL | **Offline-first** with sync

## Architecture
- Android: `app/src/main/java/com/senthapps/slagrimarket/` - MVVM pattern, single-activity Compose navigation
- Backend: `backend/src/` - RESTful API at `/api/v1/*`, serverless on Vercel
- DB: 12 tables (users, listings, transactions, messages, favorites, reviews, notifications, market_prices, etc.)

## Standards
- Kotlin: Coroutines/Flow, immutable UI state (StateFlow), no Fragments/XML
- API: Retrofit services in `data/api/`, repositories offline-first (Room cache → API refresh)
- Tests: MockK + Turbine, run `./gradlew testDebugUnitTest`
- Build: `./gradlew assembleDebug` (Android), `npm run dev` (backend local), `vercel --prod` (deploy)

## Key Rules
- Read existing patterns before changing (e.g., `ListingRepository.kt` for offline-first)
- Build + test after changes
- No speculative refactors or dependency upgrades
- Plan mode for multi-file changes
