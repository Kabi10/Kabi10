# Ship-Ready Design — KaiView + Chorus
> Date: 2026-03-15
> Status: Approved
> Scope: Open-source viability for both projects — `pip install`, KaiView settings panel, Chorus onboarding wizard, Chorus retry handling

---

## Goal

Make KaiView and Chorus usable by external developers with zero friction: install from PyPI, configure through a UI, and get up and running without reading source code or editing config files by hand.

**Out of scope for this pass:** demo GIFs, localhost auth token, health degradation notifications, multi-project quick-switch, enhanced D3 keyword clustering, v1.0.0 GitHub release tags, API versioning, multi-browser support.

---

## Shared Conventions

### Storage locations
- KaiView config: `~/.kaiview/config.toml` (created on first run if absent)
- Chorus config + profiles: `~/.chorus/` directory
  - `~/.chorus/onboarding.json` — tracks per-platform login completion
  - `~/.chorus/profiles/{platform}/` — Playwright auth state per platform (storage_state.json)

### First-run detection
- KaiView: absence of `~/.kaiview/config.toml`
- Chorus onboarding wizard: absence of `~/.chorus/onboarding.json` OR zero platforms with `"completed": true` in that file

### Path handling
- All paths resolved with `pathlib.Path.expanduser().resolve()` — covers `~` on Windows and Unix
- Windows is a supported target platform; no Unix-only assumptions

### API error envelope (both projects)
All API error responses use:
```json
{"error": "Human-readable message", "code": "snake_case_error_code"}
```
HTTP status: 422 for validation errors, 500 for server errors.

### Versioning
Both projects use a static version string in `pyproject.toml`:
```toml
[project]
version = "1.0.0"
```
Version also exported from `__init__.py`: `__version__ = "1.0.0"`

---

## 1. PyPI Packaging — KaiView

### Package structure

```
kaiview/
  __init__.py              — exports __version__ = "1.0.0"
  server.py                — FastAPI app + main() entry point
  index.html               — served as package_data
  config_template.toml     — default config, bundled as package_data
pyproject.toml
README.md
LICENSE
```

`config_template.toml` is bundled via `pyproject.toml`:
```toml
[tool.setuptools.package-data]
kaiview = ["*.html", "*.toml"]
```
Loaded at runtime via `importlib.resources.files("kaiview").joinpath("config_template.toml")`.

### Entry point

```toml
[project.scripts]
kaiview = "kaiview.server:main"
```

### `main()` logic

```python
def main():
    config_path = Path.home() / ".kaiview" / "config.toml"
    if not config_path.exists():
        config_path.parent.mkdir(parents=True, exist_ok=True)
        template = importlib.resources.files("kaiview").joinpath("config_template.toml").read_text()
        config_path.write_text(template)
        print(f"Created default config at {config_path}")
        print("Edit dev_dir in that file if your projects aren't in your home directory.")

    # Load config into app state
    load_config(config_path)

    # Open browser after server starts (non-blocking, silently skips on headless)
    port = get_config().server.port
    def open_browser():
        import time, webbrowser
        time.sleep(1.2)
        try:
            webbrowser.open(f"http://localhost:{port}")
        except Exception:
            pass  # headless / CI — ignore
    threading.Thread(target=open_browser, daemon=True).start()

    uvicorn.run("kaiview.server:app", host="127.0.0.1", port=port, log_level="warning")
```

No `--reload` in production mode. No worker count tuning (uvicorn default = 1, sufficient for local tool).

### Default config template

```toml
[server]
port = 3737

[projects]
dev_dir = "~"
skip = [".git", "node_modules", "__pycache__", ".venv", "venv"]

[github]
pat = ""

[health]
commit_weight = 40
dirty_weight = 20
readme_weight = 20
description_weight = 20
```

`dev_dir = "~"` is stored literally and expanded via `Path.expanduser()` at runtime — works on all platforms.

---

## 2. PyPI Packaging — Chorus

### Package structure

```
chorus/
  __init__.py
  main.py                  — FastAPI app + main() entry point
  browser.py
  websocket_manager.py
  platforms/
    __init__.py
    base.py
    gemini.py, chatgpt.py, claude.py, perplexity.py
    grok.py, copilot.py, deepseek.py, mistral.py
  selectors.json           — package_data
  frontend/
    index.html             — package_data
pyproject.toml
README.md
LICENSE
```

```toml
[tool.setuptools.package-data]
chorus = ["*.json", "frontend/*.html"]
```

### Entry point

```toml
[project.scripts]
chorus = "chorus.main:main"
```

### Playwright binary detection

At `main()` startup, before uvicorn:
```python
from playwright.sync_api import sync_playwright
try:
    with sync_playwright() as p:
        browser_path = p.chromium.executable_path
        if not Path(browser_path).exists():
            raise FileNotFoundError
except Exception:
    print("Playwright Chromium not found. Run: playwright install chromium")
    print("Then restart chorus.")
    sys.exit(1)
```

Exact error message: `"Playwright Chromium not found. Run: playwright install chromium"`

This check runs at startup — before uvicorn, before the onboarding wizard — so users hit the clear error message immediately, not mid-wizard.

### Dependencies

```toml
[project]
dependencies = [
  "fastapi>=0.100",
  "uvicorn[standard]>=0.23",
  "playwright>=1.40",
  "httpx>=0.25",
  "pydantic>=2.0",
]
```

Chromium = Chromium only (no Firefox/WebKit). All 8 platforms target Chromium.

---

## 3. KaiView — Settings Panel

### Backend routes

**`GET /api/settings`**
Returns current config. PAT masking: if PAT is non-empty, return `"__MASKED__"` (a fixed sentinel string, never a partial value). If empty, return `""`.

Frontend behaviour for PAT field:
- On modal open: if returned value is `"__MASKED__"`, populate the field with `"__MASKED__"` and show placeholder text "PAT saved — enter a new one to replace it"
- On save: if the PAT field value is still `"__MASKED__"`, send `"__MASKED__"` to the backend (do not update)
- If the user clears the field: send `""` (clears the stored PAT)
- If the user types a new value: send the new value

Backend `POST` rule: if `github_pat == "__MASKED__"`, skip updating the PAT field entirely.

Response shape:
```json
{
  "port": 3737,
  "dev_dir": "/home/user/projects",
  "github_pat": "ghp_***abcd",
  "skip": [".git", "node_modules"],
  "health": {
    "commit_weight": 40,
    "dirty_weight": 20,
    "readme_weight": 20,
    "description_weight": 20
  }
}
```

**`POST /api/settings`**
Accepts same shape. Validation:
- `dev_dir`: must exist as a directory after `Path.expanduser().resolve()`. Error code: `"dev_dir_not_found"`
- `port`: integer 1024–65535. Error code: `"invalid_port"`
- `github_pat`: any string (empty = clear). If value starts with `"ghp_***"`, skip updating (masked round-trip protection)
- Health weights: each value 0–100 (integers), sum must equal 100. Any weight can be 0. Error code: `"weights_dont_sum_to_100"`

On success: writes `~/.kaiview/config.toml`, hot-reloads in-memory config object (replaces the module-level config dict — no uvicorn restart).

Response on success:
```json
{"ok": true}
```
Or if port changed:
```json
{"ok": true, "restart_required": true, "new_port": 3738}
```

**Hot-reload definition:** The in-memory config object (a module-level dataclass/dict used by all route handlers) is updated in place. No uvicorn restart needed. Port change is the only exception — uvicorn is already bound to a port and cannot rebind without restart.

### Frontend modal

Gear icon (⚙) in top-right header. Opens a full overlay modal.

**Section: Projects**
- `dev_dir` — text input, full path, placeholder "~/projects"
- `skip` — comma-separated tag input

**Section: GitHub**
- `github_pat` — `<input type="password">`, placeholder "ghp_...", link: "Create a PAT on GitHub →"

**Section: Server**
- `port` — `<input type="number" min="1024" max="65535">`
- If port differs from current value: amber banner "Changing the port requires restarting kaiview"

**Section: Health Weights**
- Four range sliders: commit_weight, dirty_weight, readme_weight, description_weight
- Live sum display: "Total: 100 / 100" (green) or "Total: 95 / 100" (red, save disabled)

**Save behaviour:**
- POST `/api/settings`
- Success → toast "Settings saved" (3s auto-dismiss)
- Port change → toast "Restart kaiview to apply port change"
- Validation error → inline error below the offending field (not a toast)
- Modal closeable via Escape key or ✕ button; unsaved changes prompt "Discard changes?"

---

## 4. Chorus — Onboarding Wizard

### Platforms in scope (all 8)
Gemini, ChatGPT, Claude (claude.ai), Perplexity, Grok, Microsoft Copilot, DeepSeek, Mistral

### Trigger conditions
- **Auto:** app load, `~/.chorus/onboarding.json` absent OR zero platforms with `"status": "completed"`
- **Manual:** "Manage Accounts" button in header (always visible)

### State file: `~/.chorus/onboarding.json`
```json
{
  "gemini": {"status": "completed", "completed_at": "2026-03-15T10:00:00Z"},
  "chatgpt": {"status": "skipped"},
  "claude": {"status": "pending"},
  ...
}
```
Statuses: `"pending"` | `"completed"` | `"skipped"`

### Wizard screens

**Screen 1 — Welcome**
> "Chorus opens a real browser for each AI platform so you can log in once. Your sessions are saved locally in `~/.chorus/profiles/`. You can skip any platform and add it later from Manage Accounts."

CTA: "Get Started →"

**Screen 2 — Platform checklist**
8 platform cards in a grid. Each card shows:
- Platform name
- Status badge: `Logged in ✓` (green) / `Pending` (grey) / `Skipped` (muted)
- "Set up" button (pending) or "Re-login" button (completed, for refreshing)

CTA at bottom: "Done →" (enabled when at least 1 platform is completed)

**Screen 3 — Per-platform login step**

Header: "Set up [Platform name]" with back arrow.

Steps shown to user:
1. "Click **Open Browser**. A Chromium window will open to [Platform]'s login page."
2. "Log in with your account."
3. "Once logged in, Chorus will detect your session automatically."

**Open Browser** button → `POST /api/onboarding/{platform}/open`
- Backend: Playwright navigates the platform's browser context to its login URL
- The Playwright browser window is visible (non-headless) so user can interact

After clicking Open Browser:
- Spinner: "Waiting for login..."
- Frontend polls `GET /api/onboarding/{platform}/status` every 2s
- **Timeout: 10 minutes.** After 10 min with no auth detected → spinner replaced with "Timed out. Try again or skip this platform." + Retry / Skip buttons
- On `{"authenticated": true}` → ✓ badge + "Continue →" button enabled

**Continue** before auth detected → warning modal: "We haven't detected a login yet. Continuing without logging in means [Platform] won't be available. Continue anyway or try again?" Options: "Try Again" / "Skip Platform"

**Skip** link: always visible. Calls `POST /api/onboarding/{platform}/skip` → sets status to `"skipped"` → returns to checklist.

**Screen 4 — Completion**
Lists logged-in (✓) and skipped platforms.
> "You can set up skipped platforms anytime from Manage Accounts."
CTA: "Start Using Chorus →" → calls `POST /api/onboarding/complete` → dismisses wizard

### Auth detection and storage state save

**Browser context lifecycle during onboarding:**
- `POST /api/onboarding/{platform}/open` creates a persistent Playwright browser context and stores it in a server-side dict keyed by platform name (`onboarding_contexts: dict[str, BrowserContext]`). The context stays open until explicitly closed.
- The context is closed only after one of: (a) storage state successfully saved, (b) user clicks Skip, (c) wizard is dismissed via Complete.

`GET /api/onboarding/{platform}/status`:
1. Retrieves the open browser context from `onboarding_contexts[platform]`
2. Calls `await platform_instance.assert_authenticated(context)` — lightweight Playwright check for authenticated UI element vs login wall, no navigation
3. If `authenticated == True`: **immediately** calls `await context.storage_state(path=str(Path.home() / ".chorus" / "profiles" / platform / "storage_state.json"))` before returning the response. Creates parent directory if absent.
4. Updates `onboarding.json` status to `"completed"` with timestamp
5. Returns `{"authenticated": bool, "profile_exists": bool}`

The save happens inside the `/status` endpoint handler, synchronously before the response is returned, while the context is guaranteed open. The frontend never needs to call a separate save endpoint.

### Re-auth flow (mid-query)

When `assert_authenticated()` fails during a live query:
1. Platform's asyncio task returns error code `auth_expired`
2. Platform card shows amber badge "Session expired" + "Re-login" button
3. Clicking Re-login opens the wizard directly at Screen 3 for that platform (not Screen 1)
4. After successful re-auth, user clicks "Done" → wizard closes → `POST /api/sessions/{id}/retry/{platform}` fires automatically
5. Other platforms' results remain visible and intact throughout

---

## 5. Chorus — Rate Limit / Retry

### Error taxonomy

| Code | Trigger | Detection |
|------|---------|-----------|
| `timeout` | Task exceeds 60s | `asyncio.wait_for` raises `asyncio.TimeoutError`. 60s measured from asyncio Task start (prompt submission). |
| `rate_limited` | Platform throttling | Per-platform strings in `selectors.json` under `"rate_limit_signals"` key. Universal fallbacks: `["too many requests", "rate limit", "try again later", "quota exceeded"]` (case-insensitive page text match). |
| `auth_expired` | Session lost mid-query | `assert_authenticated()` returns False during response extraction. |
| `selector_error` | UI element not found | Playwright `TimeoutError` on element wait that is NOT caused by rate limit or auth. |

Per-platform rate limit signals in `selectors.json`:
```json
{
  "gemini": {
    "timeout_seconds": 60,
    "rate_limit_signals": ["You've reached your limit", "Too many requests"],
    ...
  }
}
```

### Backend behaviour

Each platform runs as `asyncio.Task` wrapped in `asyncio.wait_for(task, timeout=platform_timeout)`.

On failure:
```python
{"platform": "gemini", "status": "error", "error_code": "rate_limited",
 "message": "Gemini is rate-limiting requests. Wait a moment and click Retry."}
```
Sent via WebSocket to frontend. Human-readable messages are defined per error code — not raw exception strings.

Other platform tasks are not cancelled. Partial results (successful platforms) are immediately visible.

### Retry

`POST /api/sessions/{id}/retry/{platform}`
- Re-runs only the specified platform against the same prompt
- Previous result for that platform is replaced on success
- Max retries: 3 per platform per session (4th retry attempt returns `{"error": "Max retries reached", "code": "max_retries"}`)
- Concurrent retries for the same platform/session are rejected (return 409)

### Frontend

Failed platform card:
- Red border + error badge with human-readable message (from `message` field, never raw exception)
- **Retry** button (hidden after max retries, replaced with "Max retries reached")
- **Re-login** button replaces Retry when error_code is `auth_expired`

While retry is in progress: card shows spinner, Retry button disabled.

Partial success display: successful platform results are shown immediately as they arrive. Failed cards coexist with successful ones — users do not wait for all platforms before seeing any results.

---

## Delivery order

1. PyPI packaging — KaiView (`pyproject.toml`, module restructure, `main()`, config template)
2. PyPI packaging — Chorus (`pyproject.toml`, module restructure, `main()`, Playwright probe)
3. KaiView settings panel (backend `GET`/`POST /api/settings` + frontend modal)
4. Chorus onboarding wizard (backend endpoints + `~/.chorus/onboarding.json` + frontend screens)
5. Chorus rate limit / retry (error taxonomy in `selectors.json` + retry endpoint + frontend card states)
6. End-to-end verification: `pip install -e .` both projects, run through first-run flow on a clean config
7. Commit + push both repos
