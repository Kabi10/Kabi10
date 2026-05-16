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

## Workflow: Vibe Kanban + AI Pipeline

Issue tracker: **Vibe Kanban** (MCP tools: `mcp__vibe_kanban__*`). Issues use KAB-N IDs.

### Standard task flow

1. `/work-next` — pick highest-priority open KAB issue
2. `/work-task KAB-N` — load context and implement a specific issue
3. `/pipeline "description"` — run Gemini→DeepSeek→Claude for complex analysis
4. `/sync-kanban` — review board status and close completed issues

### AI Pipeline (`scripts/pipeline.sh`)

Three-stage token-efficient pipeline:

- **Stage 1 — Gemini 2.5 Pro** (`mcp__gemini-bridge-mcp__gemini_execute`): codebase analysis, gap detection
- **Stage 2 — DeepSeek R1** (API at `https://api.deepseek.com/v1/chat/completions`, model `deepseek-reasoner`): implementation spec
- **Stage 3 — Claude Code**: surgical implementation using the spec

Use Gemini/DeepSeek for exploration and specs; reserve Claude tokens for actual code changes.

```bash
./scripts/pipeline.sh "describe the task"     # full pipeline
./scripts/pipeline.sh "..." --analyze-only    # Gemini analysis only
./scripts/pipeline.sh "..." --dry-run         # spec without coding
```

Live dashboard runs on `http://localhost:4242` via `scripts/aipipe-server.js`.

## Production Access

### Supabase (PostgreSQL)

Pull production credentials:

```bash
vercel env pull --environment production backend/.env.prod
```

Gives `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_PORT`, `DB_NAME` for direct pg pool connections.

- Host: `aws-1-ap-southeast-1.pooler.supabase.com`
- Project ref: `lxsbdluguyaaxzaeovwx`

### Vercel

```bash
vercel ls          # list deployments
vercel inspect     # current deployment details
vercel --prod      # deploy to production
```

## Open Issues (Vibe Kanban)

| ID    | Title                  | Priority | Status |
| ----- | ---------------------- | -------- | ------ |
| KAB-3 | SMS/OTP Authentication | Urgent   | To Do  |
| KAB-9 | Batch Sync Reliability | High     | To Do  |

**KAB-3 notes**: SMS service fully implemented with Mobitel mSpace (recommended), Dialog (unresponsive), and Twilio (fallback). Set `SMS_MODE=mobitel` with `MOBITEL_USERNAME`/`MOBITEL_PASSWORD` env vars. Currently runs in mock mode (logs OTP to console). Android OTP screens implemented.

**KAB-9 notes**: Retry backoff, conflict resolution UX, sync status indicator, stale ops cleanup. Key files: `SyncManager.kt`, `SyncApiService.kt`, `LocalOp.kt`.

### Completed

- KAB-4: Activities table migration
- KAB-5: Image compression (`ImageUploadUtil.prepareImageForUpload`)
- KAB-6: Storage delete endpoint (`DELETE /api/v1/storage/delete`)
- KAB-7: Unit test expansion (258 tests across 21 files)
- KAB-8: Field Mode accessibility (toggle, 72dp targets, Tamil labels, voice input, 40sp prices)
- KAB-10: CROPIX DoA integration (CropixApiService, DoACropEntity, yield tips, DoA badge)
