# Thara Bot

The original prototype for what would later become Sanctum — a single-persona Telegram AI companion bot named Thara. Built in pure Python with no external frameworks beyond python-telegram-bot, it demonstrates how a minimal bot architecture (handler → AI client → database → dashboard) can be extended into a multi-persona platform.

## Overview

Thara is a Telegram bot that acts as a warm, nurturing companion (wife / nurse archetype). Unlike the later Sanctum architecture, this prototype hard-codes the persona into `ai_client.py`, keeps the dashboard in a single FastAPI file, and uses a single SQLite database (`thara.db`) for all persistence.

The repository is valuable as a **minimal reference implementation** — if you want to understand how Sanctum works under the hood, this is the stripped-down version without the multi-bot dispatcher, referral system, or Stripe integration.

## Architecture

```
bot.py          →  ai_client.py  →  DeepSeek API
   ↓                    ↓
database.py  ←  config.py
   ↓
dashboard.py  →  static/index.html
```

| File | Responsibility | Lines of Code |
|------|---------------|---------------|
| `bot.py` | Telegram handlers, rate limiting (40/hr), typing simulation, command routing | ~180 |
| `ai_client.py` | DeepSeek API wrapper, system prompt injection, context window management | ~70 |
| `config.py` | Tokens, `ADMIN_IDS`, `DB_PATH`, feature flags | ~120 |
| `dashboard.py` | FastAPI admin — user list, message logs, ban/unban, health check | ~70 |
| `database.py` | aiosqlite schema — `users`, `messages`, `user_profiles` | ~140 |
| `static/index.html` | Single-file Telegram-style dashboard UI | ~1 (embedded) |

## Running

```bash
pip install -r requirements.txt
# Edit config.py with your Telegram token and DeepSeek key

# Terminal 1 — bot
python bot.py

# Terminal 2 — dashboard
python dashboard.py
# → http://localhost:8000
```

## Key Differences from Sanctum

| Feature | Thara Bot | Sanctum |
|---------|-----------|---------|
| Personas | 1 (hard-coded) | 5+ (DB-driven, editable) |
| Database | Single `thara.db` | Split `sanctum.db` + legacy `thara.db` |
| Dashboard | Inline HTML in `static/` | Full Jinja2 templates + CSS |
| AI Provider | DeepSeek only | DeepSeek + OpenAI fallback |
| Payments | None | Stripe checkout + webhooks |
| Referrals | None | Full credit + referral loop |
| Bot Architecture | Single bot file | Multi-bot dispatcher (`hub_bot.py`) |
| Voice | None | Edge-TTS |

## Schema

```sql
users           -- telegram_id, username, first_seen
messages        -- id, user_id, role, content, timestamp
user_profiles   -- user_id, active_persona, credits, premium
```

## Why Keep This?

- **Educational**: The smallest possible version of the companion-bot pattern
- **Deployable**: Runs on a $5 VPS without Docker or complex orchestration
- **Forkable**: Easy to adapt for a single-purpose bot (therapist, tutor, etc.)

## License

Private — All rights reserved.
