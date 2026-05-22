# Organiverse

Organiverse is a real-time group chat inside a body. Heart, Brain, Liver,
Lungs, Stomach, and Kidneys are persistent AI characters that react to user
events, argue with each other, remember the session, and stream replies into a
shared chat.

Live deployment:

```text
http://65.21.53.29/Organiverse/
https://apps.65.21.53.29.sslip.io/Organiverse/
```

## Features

- FastAPI WebSocket backend with streamed organ replies.
- DeepSeek `deepseek-chat` integration with mock fallback when no API key is set.
- SQLite chat history and per-session state.
- Organ mood, body weather, crisis/rebellion visual states, and report cards.
- Relationship scores with natural alliances and rivalries.
- Rolling event memory injected into organ prompts.
- `@Heart`, `@Brain`, `@Liver`, `@Lungs`, `@Stomach`, and `@Kidneys` specialty mode.
- Diary tab with on-demand cached private diary entries for all organs.
- Achievements with non-repeating toast notifications.
- Multiplayer rooms through the `?room=` URL parameter.
- Exportable styled HTML transcript and generated session summary.
- Dark mode by default, light toggle, responsive UI, animated SVG organ avatars.
- Optional synthesized Web Audio sounds controlled from settings.

## Stack

- Backend: FastAPI, WebSockets, SQLite
- Frontend: React, Vite, Tailwind CSS
- AI: DeepSeek OpenAI-compatible chat completions with `deepseek-chat`
- Production process: supervisor
- Production proxy/static hosting: nginx with HTTPS from certbot

## Project Structure

```text
Organiverse/
  backend/
    app/
      ai.py          DeepSeek streaming client, diaries, summaries
      database.py    SQLite messages and session state store
      main.py        FastAPI app, WebSocket protocol, export endpoints
      organs.py      organ roster, colors, specialties, prompts
      state.py       moods, relationships, memory, report cards, weather
    requirements.txt
  deploy/
    deploy_hetzner.py  Separate Hetzner deploy script
    smoke_public.py    HTTPS/WSS public smoke test
  frontend/
    src/
      App.tsx
      main.tsx
      styles.css
    package.json
  .env.example
```

## Local Setup

```powershell
cd C:\Dev\Organiverse
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r backend\requirements.txt
Copy-Item .env.example .env
```

Set `DEEPSEEK_API_KEY` in `.env` for real AI responses:

```env
DEEPSEEK_API_KEY=your_key_here
DEEPSEEK_API_BASE=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat
ORGANIVERSE_MOCK_AI=0
ORGANIVERSE_DB_PATH=backend/organiverse.sqlite3
```

If `DEEPSEEK_API_KEY` is empty, the backend automatically uses mock responses
so WebSocket streaming and the UI still work end to end.

Start the backend:

```powershell
cd C:\Dev\Organiverse
.\.venv\Scripts\python.exe -m uvicorn backend.app.main:app --reload --host 127.0.0.1 --port 8000
```

Start the frontend:

```powershell
cd C:\Dev\Organiverse\frontend
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

In Vite dev, the frontend defaults to:

```text
ws://127.0.0.1:8000/ws/{sessionId}
```

In production, the frontend uses same-origin WebSockets:

```text
wss://your-domain/Organiverse/ws/{sessionId}
```

## Multiplayer

Each session is keyed by its room code. The browser writes the room into the URL:

```text
http://65.21.53.29/Organiverse/?room=session-abc123
```

Sharing that URL lets another client join the same body state. Both clients send
events through the same WebSocket session and receive the same streamed organ
responses, state updates, achievements, report cards, diaries, and reset events.

## WebSocket Protocol

Client to server:

- `user_message` with `content` and optional `userName`
- `settings_update` with settings such as enabled organs, verbosity, sounds, or API key
- `diary_request`
- `reset_session`

Server to client:

- `history`
- `state`
- `message`
- `message_start`
- `message_chunk`
- `message_final`
- `organ_round_started`
- `organ_round_complete`
- `achievement`
- `diary`
- `reset`
- `error`

## HTTP Endpoints

```text
GET /health
GET /organs
GET /sessions/{session_id}/state
GET /sessions/{session_id}/summary
GET /sessions/{session_id}/export.html
WS  /ws/{session_id}
```

## Hetzner Deployment

Organiverse is deployed as a separate stack from Sanctum:

- App directory: `/opt/organiverse`
- Python venv: `/opt/organiverse/venv`
- Backend: `127.0.0.1:8010`
- Frontend: `/opt/organiverse/frontend/dist`
- Supervisor program: `organiverse-backend`
- nginx site: `/etc/nginx/sites-available/organiverse`
- Env file: `/opt/organiverse/.env`
- Gateway path: `/Organiverse/`
- Gateway guide: [../HOSTING_GUIDE.md](../HOSTING_GUIDE.md)

Deploy from Windows:

```powershell
cd C:\Dev\Organiverse
$env:HETZNER_HOST = "65.21.53.29"
$env:HETZNER_USER = "root"
$env:HETZNER_PASSWORD = "<server-password>"
$env:ORGANIVERSE_DOMAIN = "apps.65.21.53.29.sslip.io"
$env:DEEPSEEK_API_KEY = "<deepseek-key>"
.\.venv\Scripts\python.exe deploy\deploy_hetzner.py
```

Redeploy without re-running certbot:

```powershell
.\.venv\Scripts\python.exe deploy\deploy_hetzner.py --skip-certbot
```

Public smoke test:

```powershell
.\.venv\Scripts\python.exe deploy\smoke_public.py http://65.21.53.29/Organiverse
.\.venv\Scripts\python.exe deploy\smoke_public.py https://apps.65.21.53.29.sslip.io/Organiverse
```
