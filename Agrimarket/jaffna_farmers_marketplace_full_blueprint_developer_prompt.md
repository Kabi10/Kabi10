# Jaffna Farmers Marketplace — Full Blueprint

> A complete prompt / blueprint / guide + developer checklist you can paste into Augment Code or hand to engineers. Mobile-first (Android), Tamil-first, offline-first, cash-first marketplace for Jaffna farmers and local buyers.

---

# 1. Project Summary

**Name:** Jaffna Farmers Marketplace (யாழ்ப்பாணம் விவசாயிகள் சந்தை)

**Goal:** Connect farmers in Jaffna with local buyers. Allow farmers to list produce, buyers to view & coordinate pickups. Start with cash transactions; design to plug in card/online payments later.

**Primary constraints:** Low-end Android devices (min Android 7.0), intermittent connectivity, Tamil-first UX, simple verification, strong offline sync and conflict handling.

**Audience:** Tamil-speaking farmers (primary), local buyers/traders (secondary).

---

# 2. High-level architecture (one-paragraph)

Native Android app (Kotlin, MVVM) with local Room DB and offline operation queue. Backend REST API (versioned `/v1/`) backed by PostgreSQL, object storage (S3/GCS), Redis for caching/queues, and a job worker (e.g., Celery/Sidekiq). Signed image uploads, push notifications via FCM, OTP via SMS gateway. Admin web portal for verification, moderation & exports.

---

# 3. Developer Prompt (paste into Augment Code or share with team)

```
Build a native Android app (Kotlin) for "Jaffna Farmers Marketplace" targeting minSdk 24 (Android 7.0) and targetSdk 34.
- Architecture: MVVM + Repository + Use Room for local DB, Retrofit+OkHttp for network.
- Offline-first: implement an operation queue (opId, type, payload, clientTs, synced flag) and a `/v1/sync` endpoint on server.
- Auth: phone-number OTP login (JWT tokens). Sellers should have a KYC-lite flow for verification.
- Images: upload compressed images to signed S3/GCS URLs; auto-moderate via Vision API.
- Notifications: FCM + in-app message center + SMS fallback for critical alerts.
- Provide the following features: listing CRUD, image upload, search/filter by market/crop, transactions (cash pickup flow), contact farmer (call/sms), basic analytics events.
- Include admin web panel for user verification, listing moderation, transaction logs, CSV export.
- Instrument crash reporting (Sentry) and analytics (Firebase Analytics).
- Provide unit & integration tests, CI pipeline, Play Store staging flow.
```

---

# 4. Key non-functional requirements

- **Offline-first:** app must work for listing creation/viewing while offline and sync when online.
- **Low bandwidth:** image compression, small payloads, pagination, caching.
- **Localization:** Tamil-first with English support; proper font (Noto Sans Tamil), voice prompts and accessibility.
- **Security:** HTTPS, JWT, role-based access, input validation, rate-limits, encrypted sensitive fields.
- **Performance:** small APK, memory efficient, paging for lists, Glide for images.

---

# 5. Data models (client + server canonical)

### User
```json
{
  "id": "uuid",
  "name": "string",
  "phone": "+94...",
  "userType": "FARMER|BUYER",
  "verified": true|false,
  "language": "ta|en",
  "createdAt": "iso8601"
}
```

### Listing
```json
{
  "id":"uuid",
  "farmerId":"uuid",
  "cropType":"red_onion",
  "quantity": 100.0,
  "unit":"kg",
  "pricePerUnit": 200.0,
  "quality":"A",
  "harvestDate":"iso8601",
  "location":"Chavakachcheri",
  "images":["https://..."],
  "isActive":true,
  "createdAt":"iso8601",
  "updatedAt":"iso8601"
}
```

### Transaction (cash pickup)
```json
{
  "id":"uuid",
  "listingId":"uuid",
  "farmerId":"uuid",
  "buyerId":"uuid",
  "quantity":10.0,
  "totalAmount":2000.0,
  "pickupLocation":"address or market enum",
  "pickupDate":"iso8601",
  "status":"PENDING|CONFIRMED|IN_PROGRESS|COMPLETED|CANCELLED",
  "paymentMethod":"CASH",
  "createdAt":"iso8601"
}
```

### LocalOp (offline queue)
```json
{
  "opId":"client-generated-uuid",
  "type":"CREATE_LISTING|UPDATE_LISTING|DELETE_LISTING|CREATE_TRANSACTION",
  "payload":{...},
  "clientTs":"iso8601",
  "attempts":0
}
```

---

# 6. Minimal REST API (v1) — contract summary

All endpoints use JSON and require `Authorization: Bearer <jwt>` (except OTP send/verify). Use pagination for list endpoints.

**Auth**
- `POST /v1/auth/otp/send` { phone }
- `POST /v1/auth/otp/verify` { phone, otp } -> { token, user }

**Listings**
- `GET /v1/listings?market=&cropType=&page=&q=`
- `GET /v1/listings/{id}`
- `POST /v1/listings` (auth farmer)
- `PUT /v1/listings/{id}` (owner)
- `DELETE /v1/listings/{id}` (owner)
- `POST /v1/listings/{id}/images` -> returns signed URL

**Transactions**
- `POST /v1/transactions` { listingId, qty, pickupDate, buyerContact }
- `GET /v1/transactions/{id}`
- `PATCH /v1/transactions/{id}` { status }

**Sync**
- `POST /v1/sync` { lastSyncAt, ops: [LocalOp] } -> { appliedOps:[], conflicts:[], serverState:[] }

**Admin** (admin token)
- `GET /v1/admin/listings?status=`
- `POST /v1/admin/verifyUser/{id}`
- `POST /v1/admin/takedown/{listingId}`


> Tip: Return `opResults` per op with server timestamps; include conflict reason codes.

---

# 7. Offline Sync Protocol (concrete)

1. **Ops queue**: Every user action that changes data is recorded as a LocalOp in local DB and applied optimistically to UI.
2. **Batch sync**: When online, client POSTs `ops` to `/v1/sync`. Server validates and applies ops in order.
3. **Server response**: for each op, return `{ opId, status: applied|rejected|conflict, serverObject }`.
4. **Conflict handling rules**:
   - For money/price/quantity edits: prefer server timestamp OR return conflict (manual resolve) if both sides edited the same field.
   - For create/delete: if server already deleted: mark client op rejected.
5. **Retries & backoff**: client must retry failed ops with exponential backoff and limit attempts; surface unsynced badges in UI.

---

# 8. Android implementation details

## Architecture & libraries
- **Language**: Kotlin
- **Arch**: MVVM + Repository
- **UI**: Jetpack Compose (preferred) OR Views + ViewBinding if targeting lower overhead
- **DB**: Room (with Migrations)
- **Network**: Retrofit + OkHttp (with interceptors for JWT + logging + network caching)
- **DI**: Hilt / Dagger
- **Image**: Glide or Coil (Coil recommended for Kotlin)
- **Background**: WorkManager for scheduled sync; CoroutineWorkers for coroutine support
- **Push**: Firebase Cloud Messaging
- **Auth**: SMS OTP via server
- **Testing**: JUnit, Robolectric (unit), Espresso/Compose Test (UI), MockWebServer for network

## Recommended modules (android project)
```
:app (ui)
:common (utils, models)
:data (room, retrofit, repositories)
:sync (sync manager, op queue)
:feature-listings
:feature-transactions
:feature-auth
```

---

# 9. UI/UX Guidelines (Tamil-first)

- All primary navigation labels in Tamil; provide English toggle in settings.
- Use Noto Sans Tamil font (fallbacks tested across devices).
- Very large tap targets, minimal text forms, pictorial icons for common crops.
- Audio guided flows for listing creation (recorded Tamil prompts + optional text)
- Onboarding: short 3-screen tutorial in Tamil with voice.
- Accessibility: TalkBack tests with Tamil TTS, large text setting, high-contrast toggle.

---

# 10. Image & Media strategy

- Client compress and resize images (max width 1024px, 1MB).
- Upload via signed URL to S3/GCS (server issues pre-signed URL after verifying user token).
- After upload, client notifies server to attach URL to listing.
- Run automatic moderation (vision API) server-side; if flagged, hide listing and send admin review notification.
- Use CDN for serving images.

---

# 11. Notifications & Messaging

- Use FCM for push messages (listing interested, transaction update, verification status).
- Maintain in-app message center (persisted in local DB).
- Use SMS fallback for OTP and critical transaction notifications (cost-managed).

---

# 12. Admin Portal

Minimal features:
- Admin login (2FA)
- Approve/Reject seller verifications (KYC-lite)
- Listing moderation (search / filter / takedown)
- View & export transaction logs (CSV)
- Basic analytics dashboard (DAU, Listings/day, Transactions/day)

Start with Django Admin or a small React app backed by the same API and admin-only endpoints.

---

# 13. Security & Privacy checklist

- Enforce HTTPS + HSTS.
- JWT tokens with access + refresh tokens (rotate on refresh).
- Rate limit auth endpoints and common actions.
- Store minimal PII; encrypt sensitive fields at rest (phone numbers optional), mask logs.
- Implement server-side validation for all inputs.
- Use signed image uploads (no direct client write to object store without signature).
- Logging & audit trail for CRUD ops on listings & transactions.
- Prepare Privacy Policy + Terms in Tamil and English.

---

# 14. Observability & Metrics

- Crash reporting: Sentry (mobile + backend).
- Analytics: Firebase Analytics or Mixpanel (events: listing_created, listing_viewed, contact_farmers, transaction_created, transaction_confirmed).
- Server logs: structured JSON logs to centralized storage (Cloud Logging / ELK).
- Monitor: uptime, DB latency, queue lengths, sync failure rate.

---

# 15. Testing & QA

- Unit tests for viewmodels, validators, repository logic.
- Integration tests for Room + network (use MockWebServer).
- E2E tests for core flows: signup, create listing, buy (transaction), sync conflict.
- Beta testing via Play Internal/Closed testing tracks (use staged rollouts).

---

# 16. CI/CD & Release

- CI: run `ktlint`, unit tests, instrumentation tests on PRs.
- CD: automated artifact upload to Play Store internal track (using `fastlane supply`).
- Versioning: semantic version + change log in Tamil + English.
- Feature flags for turning on/off features via remote config (Firebase Remote Config).

---

# 17. Infra & Cost (starter)

- **Start cheap / managed**:
  - DB: Supabase / AWS RDS (Postgres managed)
  - Storage: S3/GCS
  - App server: Render/Heroku/GCP App Engine
  - Background jobs: Heroku worker / Cloud Run or small EC2
  - SMS: local provider or Twilio (local rates)
  - CDN: CloudFront / Cloudflare
- **Estimated starter monthly**: $50–$500 (very small usage)
- **Growth stage (100k users)**: $500–$5k+/month depending on traffic, images, SMS volume, and analytics.

---

# 18. KPIs to track (instrument early)

- DAU, MAU (separate farmer vs buyer)
- Listings created per day
- Avg time from listing -> first contact
- Transaction conversion rate
- Sync success rate (client syncs / failures)
- Crash-free sessions
- App retention (1/7/30 day)

---

# 19. Legal & Community

- Local legal: ensure seller responsibilities and buyer disputes process are documented.
- Community outreach: partner with local agricultural extension offices to onboard farmers.
- Provide printed flyers and phone support in initial launch.

---

# 20. Launch checklist (go-to-market)

- [ ] App store listing in Tamil + English (screenshots + short video)
- [ ] Localized privacy policy & T&C
- [ ] SMS gateway account + sender ID
- [ ] Admin users seeded for verification & support
- [ ] Beta testers (20–100 farmers) recruited locally
- [ ] Crash reporting and analytics enabled
- [ ] Backup & DB snapshot schedule configured

---

# 21. Next artifacts I can generate for you (choose any)

- Full **OpenAPI (Swagger) spec** for `/v1` endpoints
- Room `@Entity` and `@Dao` classes for all models (Kotlin)
- Retrofit interfaces and sample Repositories/ViewModels
- Example `SyncManager` Kotlin class with coroutine workers
- Admin panel wireframes or a minimal React admin skeleton

---

# 22. Final developer checklist (copy/paste)

- [ ] Create project skeleton (modules: app, data, sync, features)
- [ ] Implement OTP auth flow + JWT handling
- [ ] Implement Room schema + migrations (users, listings, transactions, ops)
- [ ] Implement Retrofit API client + interceptors
- [ ] Implement image compression + signed uploads
- [ ] Implement SyncManager + WorkManager jobs
- [ ] Implement listing CRUD & transaction flows with optimistic UI
- [ ] Implement admin portal & verification flows
- [ ] Add analytics, Sentry, FCM
- [ ] Setup CI/CD + Play Store tracks
- [ ] Run pilot with 20–100 farmers, collect feedback

---

Thank you — the full blueprint is now in this document. If you want, I can now generate one of the artifacts from Section 21 (OpenAPI, Room entities, SyncManager code, Retrofit interfaces). Which one should I produce next?

