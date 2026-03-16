# Chorus Ship-Ready Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Chorus installable via `pip install chorus`, guide first-time users through logging into each AI platform via a step-by-step onboarding wizard, and handle platform errors (timeout, rate-limit, auth expiry) gracefully with per-platform retry.

**Architecture:** Move `main.py` from the repo root into the `chorus/` package (it already has `chorus/` as a sub-package). All user data moves from repo-relative paths to `~/.chorus/`. A `main()` entry point probes for Playwright Chromium before starting. Onboarding state lives in `~/.chorus/onboarding.json`. Four new `/api/onboarding/*` endpoints drive the wizard. Error taxonomy is added to `selectors.json` and `run_platform()`. A new `/api/sessions/{id}/retry/{platform}` endpoint handles per-platform retry.

**Tech Stack:** Python 3.10+, FastAPI, uvicorn, Playwright (async), asyncio, pathlib, pytest, FastAPI TestClient

**Spec:** `docs/superpowers/specs/2026-03-15-ship-ready-design.md` (Sections 2, 4, 5)

---

## File Map

| Action | Path | Purpose |
|--------|------|---------|
| Move | `main.py` → `chorus/main.py` | FastAPI app + `main()` entry point |
| Modify | `chorus/browser.py` | Change `PROFILES_DIR` to `~/.chorus/profiles/` |
| Modify | `chorus/platforms/base.py` | Add `is_authenticated()` → bool (non-raising check) |
| Modify | `chorus/selectors.json` | Add `timeout_seconds` + `rate_limit_signals` per platform |
| Create | `pyproject.toml` | Build config, entry point, deps |
| Create | `chorus/__init__.py` | `__version__` |
| Create | `chorus/onboarding.py` | Onboarding state file read/write helpers |
| Create | `tests/__init__.py` | Test package marker |
| Create | `tests/test_packaging.py` | Entry point + Chromium probe test |
| Create | `tests/test_onboarding.py` | Onboarding state management tests |
| Create | `tests/test_retry.py` | Retry endpoint + error taxonomy tests |

---

## Chunk 1: PyPI Packaging

### Task 1: Package structure and pyproject.toml

**Files:**
- Move: `main.py` → `chorus/main.py`
- Create/update: `chorus/__init__.py`
- Create: `pyproject.toml`
- Create: `tests/__init__.py`
- Create: `tests/test_packaging.py`

> **Current state:** `main.py` lives at the repo root (`C:/Dev/chorus/main.py`). The `chorus/` sub-package already exists. Moving `main.py` into `chorus/main.py` makes it part of the package.

- [ ] **Step 1: Write failing test**

Create `tests/__init__.py` (empty) and `tests/test_packaging.py`:
```python
import inspect


def test_main_is_importable_and_callable():
    from chorus.main import main
    assert callable(main)
    sig = inspect.signature(main)
    required = [p for p in sig.parameters.values()
                if p.default is inspect.Parameter.empty]
    assert len(required) == 0


def test_version_exported():
    import chorus
    assert hasattr(chorus, "__version__")
    assert chorus.__version__ == "1.0.0"


def test_frontend_html_accessible_from_package():
    """index.html must be accessible as package data after install."""
    import importlib.resources
    text = importlib.resources.files("chorus").joinpath("frontend/index.html").read_text()
    assert "<html" in text.lower()
```

- [ ] **Step 2: Run — expect FAIL**
```bash
cd C:/Dev/chorus
pytest tests/test_packaging.py -v
```
Expected: `ModuleNotFoundError: No module named 'chorus.main'` (main.py not yet in package)

- [ ] **Step 3: Move main.py and frontend/ into the chorus/ package**

```bash
mv main.py chorus/main.py
mv frontend chorus/frontend
```

> **Why move frontend/:** `importlib.resources` can only serve files that live INSIDE the Python package directory. `frontend/index.html` currently sits at the repo root level; it must move to `chorus/frontend/index.html` so `package-data` picks it up.

> **Update imports in `chorus/main.py`:**
> ```python
> from chorus.browser import manager as browser_manager
> from chorus.websocket_manager import ws_manager
> from chorus.platforms.gemini import Gemini
> # etc.
> ```
> These relative imports still work correctly since `chorus/main.py` is now inside the `chorus` package. No import changes needed.

> **Also update:** `HTML_FILE` and `HISTORY_FILE` paths in `chorus/main.py`:
> ```python
> # OLD (remove these):
> HTML_FILE    = Path(__file__).parent / "frontend" / "index.html"
> HISTORY_FILE = Path(__file__).parent / "chorus_history.json"
>
> # NEW:
> import importlib.resources
> _CHORUS_DIR   = Path.home() / ".chorus"
> HISTORY_FILE  = _CHORUS_DIR / "chorus_history.json"
> # Read HTML into memory at startup — importlib.resources returns a Traversable, not a real
> # filesystem path in installed wheels. Read content directly; never store as Path.
> _HTML_CONTENT: str = importlib.resources.files("chorus").joinpath("frontend/index.html").read_text(encoding="utf-8")
> ```
>
> Update the root route:
> ```python
> @app.get("/", response_class=HTMLResponse)
> def root():
>     return _HTML_CONTENT
> ```
>
> Also add `_CHORUS_DIR.mkdir(parents=True, exist_ok=True)` early in the file (before `load_history()`).

- [ ] **Step 4: Update chorus/__init__.py**
```python
__version__ = "1.0.0"
```

- [ ] **Step 5: Create pyproject.toml**

Create at repo root `C:/Dev/chorus/pyproject.toml`:
```toml
[build-system]
requires = ["setuptools>=68", "wheel"]
build-backend = "setuptools.backends.legacy:build"

[project]
name = "chorus"
version = "1.0.0"
description = "Query multiple AI platforms simultaneously"
readme = "README.md"
license = {text = "MIT"}
requires-python = ">=3.10"
dependencies = [
    "fastapi>=0.100.0",
    "uvicorn[standard]>=0.23.0",
    "playwright>=1.40",
    "httpx>=0.25.0",
    "pydantic>=2.0.0",
]

[project.scripts]
chorus = "chorus.main:main"

[tool.setuptools.packages.find]
where = ["."]
include = ["chorus*"]

[tool.setuptools.package-data]
chorus = ["*.json", "frontend/*.html"]

[tool.pytest.ini_options]
testpaths = ["tests"]
```

- [ ] **Step 6: Install and run tests**
```bash
pip install -e .
pytest tests/test_packaging.py -v
```
Expected: all PASS

- [ ] **Step 7: Commit**
```bash
git add chorus/__init__.py chorus/main.py pyproject.toml tests/
git commit -m "chore: move main.py into chorus/ package, add pyproject.toml"
```

---

### Task 2: Update data paths to ~/.chorus/ and fix browser profiles path

**Files:**
- Modify: `chorus/browser.py` — change `PROFILES_DIR`
- Modify: `chorus/main.py` — `_CHORUS_DIR` already added in Task 1; verify `load_history`/`save_history` use it

- [ ] **Step 1: Update PROFILES_DIR in chorus/browser.py**

In `chorus/browser.py`, replace:
```python
PROFILES_DIR = Path(__file__).parent.parent / "profiles"
```
with:
```python
PROFILES_DIR = Path.home() / ".chorus" / "profiles"
```

- [ ] **Step 2: Verify history file path in chorus/main.py**

Confirm `HISTORY_FILE` was updated in Task 1 to use `_CHORUS_DIR / "chorus_history.json"`. If not, do it now.

- [ ] **Step 3: Smoke test**
```bash
chorus &
sleep 3
curl -sf http://localhost:4747/ | head -3
kill %1
```
Expected: HTML served, `~/.chorus/` directory created, no errors about missing files

- [ ] **Step 4: Commit**
```bash
git add chorus/browser.py chorus/main.py
git commit -m "feat: move all user data to ~/.chorus/"
```

---

### Task 3: Add main() with Playwright Chromium probe

**Files:**
- Modify: `chorus/main.py` — add `main()` at bottom with Chromium detection and uvicorn launch

- [ ] **Step 1: Write failing test**

Add to `tests/test_packaging.py`:
```python
def test_main_exits_cleanly_when_chromium_missing(monkeypatch):
    """main() should print a clear message and exit(1) when Chromium binary absent."""
    import sys
    from unittest.mock import patch, MagicMock

    # Simulate missing Chromium by making executable_path not exist
    fake_path = "/nonexistent/chromium"
    mock_chromium = MagicMock()
    mock_chromium.executable_path = fake_path
    mock_pw = MagicMock()
    mock_pw.__enter__ = lambda s: mock_pw
    mock_pw.__exit__ = MagicMock(return_value=False)
    mock_pw.chromium = mock_chromium

    with patch("chorus.main.sync_playwright", return_value=mock_pw):
        with pytest.raises(SystemExit) as exc:
            from chorus.main import main
            # Call the probe directly (not full main, which would start uvicorn)
            from chorus.main import _check_playwright
            _check_playwright()
        assert exc.value.code == 1
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_packaging.py::test_main_exits_cleanly_when_chromium_missing -v
```
Expected: FAIL — `_check_playwright` not defined

- [ ] **Step 3: Add _check_playwright() and main() to chorus/main.py**

Add these imports at the top of `chorus/main.py` if not present:
```python
import sys
import threading
import webbrowser
from playwright.sync_api import sync_playwright
```

Add the probe function and entry point at the bottom:
```python
def _check_playwright():
    """Exit with clear message if Playwright Chromium binary is missing."""
    try:
        with sync_playwright() as p:
            binary = p.chromium.executable_path
            if not Path(binary).exists():
                raise FileNotFoundError(f"Chromium binary not found at {binary}")
    except Exception:
        print("Playwright Chromium not found. Run: playwright install chromium")
        print("Then restart chorus.")
        sys.exit(1)


def main():
    """Entry point for `chorus` CLI command."""
    _CHORUS_DIR.mkdir(parents=True, exist_ok=True)
    _check_playwright()

    port = 4747

    def _open_browser():
        import time
        time.sleep(1.5)
        try:
            webbrowser.open(f"http://localhost:{port}")
        except Exception:
            pass

    threading.Thread(target=_open_browser, daemon=True).start()
    uvicorn.run("chorus.main:app", host="127.0.0.1", port=port, log_level="warning")


if __name__ == "__main__":
    main()
```

- [ ] **Step 4: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 5: Commit**
```bash
git add chorus/main.py
git commit -m "feat: add main() entry point with Playwright Chromium probe"
```

---

## Chunk 2: Onboarding Wizard

### Task 4: Add is_authenticated() to base platform class

**Files:**
- Modify: `chorus/platforms/base.py` — add `is_authenticated()` returning bool

> **Current state:** `assert_authenticated()` raises `RuntimeError` if not logged in. `check_auth()` returns `True` if a login wall is detected (True = NOT authenticated). We need a non-raising bool check for polling.

- [ ] **Step 1: Write failing test**

Create `tests/test_onboarding.py`:
```python
import pytest
from unittest.mock import AsyncMock, MagicMock, patch


@pytest.mark.asyncio
async def test_is_authenticated_returns_true_when_logged_in():
    from chorus.platforms.base import BaseAI
    # Create a minimal concrete subclass
    class FakePlatform(BaseAI):
        name = "fake"
        url  = "https://fake.ai"
        platform_key = "fake"
        async def submit_prompt(self, prompt): pass
        async def wait_for_response(self, timeout=90): return ""

    mock_page = MagicMock()
    mock_page.url = "https://fake.ai/chat"  # no login keywords in URL
    mock_page.query_selector = AsyncMock(return_value=None)  # no login wall element

    ai = FakePlatform(mock_page)
    result = await ai.is_authenticated()
    assert result is True


@pytest.mark.asyncio
async def test_is_authenticated_returns_false_when_login_wall():
    from chorus.platforms.base import BaseAI

    class FakePlatform(BaseAI):
        name = "fake"
        url  = "https://fake.ai"
        platform_key = "fake"
        async def submit_prompt(self, prompt): pass
        async def wait_for_response(self, timeout=90): return ""

    mock_page = MagicMock()
    mock_page.url = "https://fake.ai/login"  # login keyword in URL
    mock_page.query_selector = AsyncMock(return_value=None)

    ai = FakePlatform(mock_page)
    result = await ai.is_authenticated()
    assert result is False
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pip install pytest-asyncio
pytest tests/test_onboarding.py -v
```
Expected: FAIL — `is_authenticated` method doesn't exist

- [ ] **Step 3: Add is_authenticated() to base.py**

In `chorus/platforms/base.py`, add after `check_auth()`:
```python
async def is_authenticated(self) -> bool:
    """Returns True if the user is logged in (inverse of check_auth)."""
    return not await self.check_auth()
```

- [ ] **Step 4: Run tests**
```bash
pytest tests/test_onboarding.py -v
```
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add chorus/platforms/base.py tests/test_onboarding.py
git commit -m "feat: add is_authenticated() -> bool to BaseAI for polling"
```

---

### Task 5: Onboarding state helpers

**Files:**
- Create: `chorus/onboarding.py` — read/write `~/.chorus/onboarding.json`

- [ ] **Step 1: Write failing tests**

Add to `tests/test_onboarding.py`:
```python
def test_onboarding_state_created_if_missing(tmp_path):
    from chorus import onboarding
    state_file = tmp_path / "onboarding.json"
    state = onboarding.load_state(state_file)
    # All 8 platforms present, all pending
    for p in onboarding.ALL_PLATFORMS:
        assert state[p]["status"] == "pending"


def test_mark_completed_writes_timestamp(tmp_path):
    from chorus import onboarding
    state_file = tmp_path / "onboarding.json"
    onboarding.mark_completed("gemini", state_file)
    state = onboarding.load_state(state_file)
    assert state["gemini"]["status"] == "completed"
    assert "completed_at" in state["gemini"]


def test_mark_skipped(tmp_path):
    from chorus import onboarding
    state_file = tmp_path / "onboarding.json"
    onboarding.mark_skipped("chatgpt", state_file)
    state = onboarding.load_state(state_file)
    assert state["chatgpt"]["status"] == "skipped"


def test_needs_onboarding_true_when_all_pending(tmp_path):
    from chorus import onboarding
    state_file = tmp_path / "onboarding.json"
    assert onboarding.needs_onboarding(state_file) is True


def test_needs_onboarding_false_when_one_completed(tmp_path):
    from chorus import onboarding
    state_file = tmp_path / "onboarding.json"
    onboarding.mark_completed("gemini", state_file)
    assert onboarding.needs_onboarding(state_file) is False
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_onboarding.py -v
```
Expected: FAIL — `chorus.onboarding` doesn't exist

- [ ] **Step 3: Create chorus/onboarding.py**

```python
"""Onboarding state management — ~/.chorus/onboarding.json."""
import json
from datetime import datetime, timezone
from pathlib import Path

ALL_PLATFORMS = [
    "gemini", "chatgpt", "claude", "perplexity",
    "grok", "copilot", "deepseek", "mistral",
]

_DEFAULT_STATE = {p: {"status": "pending"} for p in ALL_PLATFORMS}


def load_state(state_file: Path | None = None) -> dict:
    if state_file is None:
        state_file = Path.home() / ".chorus" / "onboarding.json"
    if state_file.exists():
        try:
            data = json.loads(state_file.read_text(encoding="utf-8"))
            # Ensure all platforms present (handles new platforms added in updates)
            for p in ALL_PLATFORMS:
                if p not in data:
                    data[p] = {"status": "pending"}
            return data
        except Exception:
            pass
    return {p: {"status": "pending"} for p in ALL_PLATFORMS}


def _save_state(state: dict, state_file: Path) -> None:
    state_file.parent.mkdir(parents=True, exist_ok=True)
    state_file.write_text(json.dumps(state, indent=2, ensure_ascii=False), encoding="utf-8")


def mark_completed(platform: str, state_file: Path | None = None) -> None:
    if state_file is None:
        state_file = Path.home() / ".chorus" / "onboarding.json"
    state = load_state(state_file)
    state[platform] = {
        "status": "completed",
        "completed_at": datetime.now(timezone.utc).isoformat(),
    }
    _save_state(state, state_file)


def mark_skipped(platform: str, state_file: Path | None = None) -> None:
    if state_file is None:
        state_file = Path.home() / ".chorus" / "onboarding.json"
    state = load_state(state_file)
    state[platform] = {"status": "skipped"}
    _save_state(state, state_file)


def needs_onboarding(state_file: Path | None = None) -> bool:
    """Returns True if zero platforms have status='completed'.
    This is a first-launch gate: once ANY platform is completed the wizard
    stops auto-triggering. Users can always re-open via Manage Accounts."""
    state = load_state(state_file)
    return not any(v.get("status") == "completed" for v in state.values())
```

- [ ] **Step 4: Run tests**
```bash
pytest tests/test_onboarding.py -v
```
Expected: all PASS

- [ ] **Step 5: Commit**
```bash
git add chorus/onboarding.py tests/test_onboarding.py
git commit -m "feat: add onboarding state helpers (chorus/onboarding.py)"
```

---

### Task 6: Onboarding backend endpoints

**Files:**
- Modify: `chorus/main.py` — add `onboarding_contexts` dict, four `/api/onboarding/*` routes

- [ ] **Step 1: Write failing tests**

Add to `tests/test_onboarding.py`:
```python
import pytest
from fastapi.testclient import TestClient
from pathlib import Path
from unittest.mock import AsyncMock, MagicMock, patch


@pytest.fixture
def client(tmp_path):
    import chorus.main as m
    # Redirect onboarding state file
    m._ONBOARDING_FILE = tmp_path / "onboarding.json"
    from fastapi.testclient import TestClient
    return TestClient(m.app)


def test_onboarding_status_pending_for_unknown_platform(client):
    r = client.get("/api/onboarding/gemini/status")
    assert r.status_code == 200
    data = r.json()
    assert "authenticated" in data
    assert "profile_exists" in data


def test_onboarding_skip_marks_platform_skipped(client, tmp_path):
    import chorus.main as m
    m._ONBOARDING_FILE = tmp_path / "onboarding.json"
    r = client.post("/api/onboarding/chatgpt/skip")
    assert r.status_code == 200
    from chorus.onboarding import load_state
    state = load_state(tmp_path / "onboarding.json")
    assert state["chatgpt"]["status"] == "skipped"


def test_onboarding_unknown_platform_returns_400(client):
    r = client.post("/api/onboarding/fakePlatform/skip")
    assert r.status_code == 400
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_onboarding.py::test_onboarding_status_pending_for_unknown_platform -v
```
Expected: FAIL — endpoints don't exist

- [ ] **Step 3: Add module-level state and onboarding routes to chorus/main.py**

Add after existing imports:
```python
from chorus import onboarding as _onboarding

_ONBOARDING_FILE    = Path.home() / ".chorus" / "onboarding.json"
_onboarding_ctxs: dict[str, object] = {}  # platform -> open BrowserContext (cleaned up on close/skip/shutdown)
```

Add a shutdown hook to close any lingering onboarding browser contexts:
```python
@app.on_event("shutdown")
async def shutdown():
    await browser_manager.stop()
    # Clean up any open onboarding contexts
    for ctx in list(_onboarding_ctxs.values()):
        try:
            await ctx.close()
        except Exception:
            pass
    _onboarding_ctxs.clear()
```
> **Note:** The existing `shutdown()` handler already calls `browser_manager.stop()`. Merge these two shutdown handlers into one rather than defining two handlers with the same decorator.

Add route handlers:
```python
# ── Onboarding ────────────────────────────────────────────────────────────────

@app.post("/api/onboarding/{platform}/open")
async def onboarding_open(platform: str):
    """Open a visible browser window for the user to log in."""
    if platform not in PLATFORMS:
        raise HTTPException(400, f"Unknown platform: {platform}")
    try:
        profile_dir = Path.home() / ".chorus" / "profiles" / platform / "default"
        profile_dir.mkdir(parents=True, exist_ok=True)
        ctx = await browser_manager._playwright.chromium.launch_persistent_context(
            user_data_dir=str(profile_dir),
            headless=False,
            args=["--disable-blink-features=AutomationControlled"],
            viewport={"width": 1280, "height": 800},
        )
        _onboarding_ctxs[platform] = ctx
        page = ctx.pages[0] if ctx.pages else await ctx.new_page()
        platform_instance = PLATFORMS[platform](page)
        await page.goto(platform_instance.url, wait_until="domcontentloaded", timeout=20000)
        return {"ok": True, "message": f"Browser opened for {platform}"}
    except Exception as e:
        raise HTTPException(500, str(e))


@app.get("/api/onboarding/{platform}/status")
async def onboarding_status(platform: str):
    """Poll authentication state. Saves storage_state.json when authenticated."""
    if platform not in PLATFORMS:
        raise HTTPException(400, f"Unknown platform: {platform}")

    profile_dir = Path.home() / ".chorus" / "profiles" / platform / "default"
    profile_exists = (profile_dir / "storage_state.json").exists()

    ctx = _onboarding_ctxs.get(platform)
    if ctx is None:
        return {"authenticated": False, "profile_exists": profile_exists}

    try:
        # Reuse the existing page from the open context (the page the user logged in on).
        # ctx.pages[0] is always the page opened by /open. Never create a new page here
        # as that would navigate away from the user's logged-in session.
        page = ctx.pages[0] if ctx.pages else await ctx.new_page()
        platform_instance = PLATFORMS[platform](page)
        authenticated = await platform_instance.is_authenticated()

        if authenticated:
            # Persistent context (launch_persistent_context) already saves session data to
            # the profile directory on close. We close the context here to flush that data,
            # then mark onboarding complete.
            # profile_dir is the SAME directory browser_manager will use for run_platform():
            #   ~/.chorus/profiles/{platform}/default  (set by PROFILES_DIR in browser.py)
            await ctx.close()
            _onboarding_ctxs.pop(platform, None)
            _onboarding.mark_completed(platform, _ONBOARDING_FILE)
            profile_exists = True

        return {"authenticated": authenticated, "profile_exists": profile_exists}
    except Exception as e:
        return {"authenticated": False, "profile_exists": profile_exists, "error": str(e)}


@app.post("/api/onboarding/{platform}/skip")
def onboarding_skip(platform: str):
    if platform not in PLATFORMS:
        raise HTTPException(400, f"Unknown platform: {platform}")
    _onboarding.mark_skipped(platform, _ONBOARDING_FILE)
    return {"ok": True}


@app.post("/api/onboarding/complete")
def onboarding_complete():
    """Called when the wizard is dismissed."""
    return {"ok": True}


@app.get("/api/onboarding/state")
def get_onboarding_state():
    """Return full onboarding state for the wizard UI."""
    return _onboarding.load_state(_ONBOARDING_FILE)
```

- [ ] **Step 4: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 5: Commit**
```bash
git add chorus/main.py
git commit -m "feat: add onboarding backend endpoints (open/status/skip/complete)"
```

---

### Task 7: Onboarding wizard frontend

**Files:**
- Modify: `chorus/frontend/index.html` — add "Manage Accounts" button, 4 wizard screens, JS

- [ ] **Step 1: Add "Manage Accounts" button to the header**

Find the header/nav element in `chorus/frontend/index.html` and add:
```html
<button id="onboarding-btn" onclick="openOnboarding()" title="Manage Accounts"
        style="background:none;border:1px solid rgba(255,255,255,.15);cursor:pointer;
               padding:6px 14px;border-radius:6px;color:inherit;font-size:.83rem;">
  Manage Accounts
</button>
```

- [ ] **Step 2: Add onboarding wizard overlay HTML**

Before `</body>`:
```html
<!-- ── Onboarding Wizard ──────────────────────────────────────── -->
<div id="ob-overlay"
     style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.65);
            z-index:950;align-items:center;justify-content:center;">
  <div id="ob-modal"
       style="background:var(--card-bg,#1a1a1a);border:1px solid var(--border,#2a2a2a);
              border-radius:14px;padding:36px 40px;width:560px;max-height:88vh;
              overflow-y:auto;position:relative;box-shadow:0 24px 80px rgba(0,0,0,.5);">

    <!-- Screen 1: Welcome -->
    <div id="ob-s1">
      <h2 style="margin:0 0 14px;font-size:1.15rem;font-weight:600;">Welcome to Chorus</h2>
      <p style="opacity:.7;line-height:1.6;margin:0 0 28px;font-size:.9rem;">
        Chorus opens a real browser for each AI platform so you can log in once.
        Your sessions are saved locally in <code>~/.chorus/profiles/</code>.
        You can skip any platform and add it later.
      </p>
      <button onclick="ob_showChecklist()"
              style="padding:10px 24px;background:#2563eb;border:none;border-radius:8px;
                     cursor:pointer;color:#fff;font-size:.9rem;font-weight:500;">
        Get Started →
      </button>
    </div>

    <!-- Screen 2: Checklist -->
    <div id="ob-s2" style="display:none;">
      <h2 style="margin:0 0 18px;font-size:1rem;font-weight:600;">Connect Your AI Platforms</h2>
      <div id="ob-platform-grid"
           style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:24px;"></div>
      <div style="display:flex;justify-content:flex-end;gap:10px;">
        <button onclick="closeOnboarding()"
                style="padding:8px 18px;background:none;border:1px solid var(--border,#2a2a2a);
                       border-radius:7px;cursor:pointer;color:inherit;font-size:.87rem;">
          Close
        </button>
        <button id="ob-done-btn" onclick="ob_showCompletion()" disabled
                style="padding:8px 20px;background:#2563eb;border:none;border-radius:7px;
                       cursor:pointer;color:#fff;font-size:.87rem;font-weight:500;opacity:.45;">
          Done →
        </button>
      </div>
    </div>

    <!-- Screen 3: Per-platform step -->
    <div id="ob-s3" style="display:none;">
      <button onclick="ob_showChecklist()"
              style="background:none;border:none;cursor:pointer;color:inherit;
                     font-size:.85rem;opacity:.6;margin-bottom:16px;padding:0;">← Back</button>
      <h2 id="ob-plat-title" style="margin:0 0 8px;font-size:1rem;font-weight:600;"></h2>
      <p style="opacity:.65;font-size:.85rem;margin:0 0 24px;line-height:1.55;">
        Click <strong>Open Browser</strong>. A Chromium window will open to the login page.
        Log in with your account. Chorus will detect your session automatically.
      </p>
      <div style="display:flex;gap:10px;align-items:center;margin-bottom:18px;">
        <button id="ob-open-btn" onclick="ob_openBrowser()"
                style="padding:9px 20px;background:#2563eb;border:none;border-radius:7px;
                       cursor:pointer;color:#fff;font-size:.87rem;font-weight:500;">
          Open Browser
        </button>
        <span id="ob-wait-msg" style="display:none;font-size:.83rem;opacity:.6;">
          ⏳ Waiting for login…
        </span>
        <span id="ob-auth-ok" style="display:none;color:#22c55e;font-size:.87rem;">✓ Logged in</span>
        <span id="ob-timeout-msg" style="display:none;color:#f87171;font-size:.83rem;">
          Timed out. Try again or skip.
        </span>
      </div>
      <div style="display:flex;gap:10px;justify-content:flex-end;">
        <button id="ob-skip-btn" onclick="ob_skip()"
                style="padding:8px 16px;background:none;border:1px solid var(--border,#2a2a2a);
                       border-radius:7px;cursor:pointer;color:inherit;font-size:.83rem;opacity:.7;">
          Skip
        </button>
        <button id="ob-continue-btn" onclick="ob_showChecklist()" disabled
                style="padding:8px 20px;background:#2563eb;border:none;border-radius:7px;
                       cursor:pointer;color:#fff;font-size:.87rem;font-weight:500;opacity:.45;">
          Continue →
        </button>
      </div>
    </div>

    <!-- Screen 4: Completion -->
    <div id="ob-s4" style="display:none;">
      <h2 style="margin:0 0 14px;font-size:1rem;font-weight:600;">You're all set!</h2>
      <div id="ob-completion-list" style="margin-bottom:20px;"></div>
      <p style="font-size:.82rem;opacity:.55;margin:0 0 24px;">
        You can set up skipped platforms anytime from Manage Accounts.
      </p>
      <button onclick="ob_finish()"
              style="padding:10px 24px;background:#2563eb;border:none;border-radius:8px;
                     cursor:pointer;color:#fff;font-size:.9rem;font-weight:500;">
        Start Using Chorus →
      </button>
    </div>
  </div>
</div>
```

- [ ] **Step 3: Add onboarding JavaScript**

In a `<script>` block before `</body>`:
```javascript
// ── Onboarding Wizard ───────────────────────────────────────────────────────
(function () {
  const PLATFORM_META_LOCAL = window.PLATFORM_META || {};
  let _currentPlatform = null;
  let _pollTimer = null;
  let _timeoutTimer = null;
  const POLL_MS = 2000;
  const TIMEOUT_MS = 10 * 60 * 1000; // 10 minutes

  function _show(id) {
    for (const s of ['ob-s1','ob-s2','ob-s3','ob-s4'])
      document.getElementById(s).style.display = s === id ? 'block' : 'none';
  }

  async function _loadState() {
    const r = await fetch('/api/onboarding/state');
    return await r.json();
  }

  window.openOnboarding = async function () {
    const state = await _loadState();
    const hasAny = Object.values(state).some(v => v.status === 'completed');
    document.getElementById('ob-overlay').style.display = 'flex';
    if (hasAny) {
      ob_showChecklist();
    } else {
      _show('ob-s1');
    }
  };

  window.closeOnboarding = function () {
    _stopPoll();
    document.getElementById('ob-overlay').style.display = 'none';
  };

  window.ob_showChecklist = async function () {
    _stopPoll();
    const state = await _loadState();
    const grid = document.getElementById('ob-platform-grid');
    grid.innerHTML = '';
    let hasCompleted = false;
    for (const [key, meta] of Object.entries(window._platformMeta || {})) {
      const s = (state[key] || {}).status || 'pending';
      if (s === 'completed') hasCompleted = true;
      const badge = s === 'completed'
        ? '<span style="color:#22c55e;font-size:.78rem;">✓ Logged in</span>'
        : s === 'skipped'
        ? '<span style="opacity:.4;font-size:.78rem;">Skipped</span>'
        : '<span style="opacity:.5;font-size:.78rem;">Pending</span>';
      const btn = s !== 'completed'
        ? `<button onclick="ob_startPlatform('${key}')"
                   style="margin-top:6px;padding:5px 12px;background:#2563eb;border:none;
                          border-radius:5px;cursor:pointer;color:#fff;font-size:.78rem;">
             Set up
           </button>`
        : `<button onclick="ob_startPlatform('${key}')"
                   style="margin-top:6px;padding:5px 12px;background:none;
                          border:1px solid rgba(255,255,255,.15);border-radius:5px;
                          cursor:pointer;color:inherit;font-size:.78rem;opacity:.6;">
             Re-login
           </button>`;
      grid.insertAdjacentHTML('beforeend', `
        <div style="padding:14px;border:1px solid var(--border,#2a2a2a);border-radius:9px;">
          <div style="font-size:.88rem;font-weight:500;margin-bottom:4px;">
            <span style="color:${(meta.color||'#888')};margin-right:6px;">${meta.icon||''}</span>
            ${meta.name}
          </div>
          ${badge}${btn}
        </div>`);
    }
    const doneBtn = document.getElementById('ob-done-btn');
    doneBtn.disabled    = !hasCompleted;
    doneBtn.style.opacity = hasCompleted ? '1' : '.45';
    _show('ob-s2');
  };

  window.ob_startPlatform = function (platform) {
    _currentPlatform = platform;
    _stopPoll();
    document.getElementById('ob-plat-title').textContent =
      `Set up ${(window._platformMeta[platform] || {}).name || platform}`;
    document.getElementById('ob-wait-msg').style.display    = 'none';
    document.getElementById('ob-auth-ok').style.display     = 'none';
    document.getElementById('ob-timeout-msg').style.display = 'none';
    document.getElementById('ob-open-btn').disabled         = false;
    const cont = document.getElementById('ob-continue-btn');
    cont.disabled    = true;
    cont.style.opacity = '.45';
    _show('ob-s3');
  };

  window.ob_openBrowser = async function () {
    document.getElementById('ob-open-btn').disabled     = true;
    document.getElementById('ob-wait-msg').style.display = 'inline';
    await fetch(`/api/onboarding/${_currentPlatform}/open`, {method:'POST'});
    _startPoll();
    _timeoutTimer = setTimeout(() => {
      _stopPoll();
      document.getElementById('ob-wait-msg').style.display    = 'none';
      document.getElementById('ob-timeout-msg').style.display = 'inline';
      document.getElementById('ob-open-btn').disabled         = false;
    }, TIMEOUT_MS);
  };

  function _startPoll() {
    _pollTimer = setInterval(async () => {
      const r = await fetch(`/api/onboarding/${_currentPlatform}/status`);
      const d = await r.json();
      if (d.authenticated) {
        _stopPoll();
        document.getElementById('ob-wait-msg').style.display = 'none';
        document.getElementById('ob-auth-ok').style.display  = 'inline';
        const cont = document.getElementById('ob-continue-btn');
        cont.disabled    = false;
        cont.style.opacity = '1';
      }
    }, POLL_MS);
  }

  function _stopPoll() {
    if (_pollTimer)   { clearInterval(_pollTimer);  _pollTimer = null; }
    if (_timeoutTimer){ clearTimeout(_timeoutTimer); _timeoutTimer = null; }
  }

  window.ob_skip = async function () {
    await fetch(`/api/onboarding/${_currentPlatform}/skip`, {method:'POST'});
    ob_showChecklist();
  };

  window.ob_showCompletion = async function () {
    _stopPoll();
    const state = await _loadState();
    const list  = document.getElementById('ob-completion-list');
    list.innerHTML = '';
    for (const [key, info] of Object.entries(state)) {
      const meta = (window._platformMeta || {})[key] || {};
      const s = info.status;
      list.insertAdjacentHTML('beforeend', `
        <div style="display:flex;align-items:center;gap:10px;padding:6px 0;
                    border-bottom:1px solid var(--border,#2a2a2a);font-size:.87rem;">
          <span style="color:${meta.color||'#888'};min-width:20px;">${meta.icon||''}</span>
          <span style="flex:1;">${meta.name||key}</span>
          <span style="font-size:.8rem;${s==='completed'?'color:#22c55e':'opacity:.4'}">
            ${s==='completed'?'✓ Logged in':s==='skipped'?'Skipped':'Pending'}
          </span>
        </div>`);
    }
    _show('ob-s4');
  };

  window.ob_finish = async function () {
    await fetch('/api/onboarding/complete', {method:'POST'});
    closeOnboarding();
  };

  // Auto-trigger on page load if onboarding needed
  window.addEventListener('load', async () => {
    // Populate _platformMeta from API
    try {
      const r = await fetch('/api/platforms');
      window._platformMeta = await r.json();
    } catch(e) { window._platformMeta = {}; }

    const state = await _loadState();
    const needsSetup = !Object.values(state).some(v => v.status === 'completed');
    if (needsSetup) {
      document.getElementById('ob-overlay').style.display = 'flex';
      _show('ob-s1');
    }
  });
})();
```

- [ ] **Step 4: Manual smoke test**
```bash
chorus &
sleep 3
```
1. Open http://localhost:4747
2. On a clean `~/.chorus/` (no completed platforms), wizard Welcome screen appears
3. Click "Get Started →" → platform checklist shows all 8 platforms
4. Click "Set up" on Gemini → Screen 3 appears
5. Click "Open Browser" → Chromium window opens to Gemini login page
6. Click "Skip" → returns to checklist, Gemini shows "Skipped"
7. Click "Manage Accounts" → opens wizard at checklist

- [ ] **Step 5: Commit**
```bash
git add chorus/frontend/index.html
git commit -m "feat: onboarding wizard frontend — 4 screens, polling, skip flow"
```

---

## Chunk 3: Rate Limit and Retry

### Task 8: Add error signals to selectors.json

**Files:**
- Modify: `chorus/selectors.json` — add `timeout_seconds` and `rate_limit_signals` per platform

- [ ] **Step 1: Open selectors.json and add to each platform block**

For each of the 8 platforms in `chorus/selectors.json`, add inside their top-level object:
```json
"timeout_seconds": 60,
"rate_limit_signals": []
```

With platform-specific signals:
- **gemini:** `["You've reached your limit", "Too many requests", "quota exceeded"]`
- **chatgpt:** `["You've sent too many messages", "Try again later", "rate limit"]`
- **claude:** `["rate limit", "Too many requests", "usage limit"]`
- **perplexity:** `["rate limit", "Too many requests"]`
- **grok:** `["rate limit", "Too many requests", "You've reached your daily limit"]`
- **copilot:** `["Too many requests", "Something went wrong", "rate limit"]`
- **deepseek:** `["Too many requests", "rate limit", "server is busy"]`
- **mistral:** `["Too many requests", "rate limit", "quota"]`

- [ ] **Step 2: Write test verifying selectors have required fields**

Create `tests/test_retry.py`:
```python
import json
import importlib.resources


def _load_selectors():
    text = importlib.resources.files("chorus").joinpath("selectors.json").read_text()
    return json.loads(text)


ALL_PLATFORMS = ["gemini","chatgpt","claude","perplexity","grok","copilot","deepseek","mistral"]


def test_all_platforms_have_timeout_seconds():
    sel = _load_selectors()
    for p in ALL_PLATFORMS:
        assert p in sel, f"platform {p} missing from selectors.json"
        assert "timeout_seconds" in sel[p], f"{p} missing timeout_seconds"
        assert isinstance(sel[p]["timeout_seconds"], int)


def test_all_platforms_have_rate_limit_signals():
    sel = _load_selectors()
    for p in ALL_PLATFORMS:
        assert "rate_limit_signals" in sel[p], f"{p} missing rate_limit_signals"
        assert isinstance(sel[p]["rate_limit_signals"], list)
```

- [ ] **Step 3: Run tests**
```bash
pytest tests/test_retry.py::test_all_platforms_have_timeout_seconds tests/test_retry.py::test_all_platforms_have_rate_limit_signals -v
```
Expected: PASS

- [ ] **Step 4: Commit**
```bash
git add chorus/selectors.json tests/test_retry.py
git commit -m "feat: add timeout_seconds + rate_limit_signals to selectors.json"
```

---

### Task 9: Update run_platform() with error taxonomy and retry endpoint

**Files:**
- Modify: `chorus/main.py` — update `run_platform()`, add `_classify_error()`, add retry endpoint

- [ ] **Step 1: Write failing tests**

Add to `tests/test_retry.py`:
```python
import pytest
from fastapi.testclient import TestClient
from unittest.mock import AsyncMock, patch, MagicMock
import asyncio


def _make_client():
    import chorus.main as m
    from fastapi.testclient import TestClient
    return TestClient(m.app)


def test_classify_error_timeout():
    from chorus.main import _classify_error
    err = asyncio.TimeoutError()
    code, msg = _classify_error("gemini", err, page_text="")
    assert code == "timeout"


def test_classify_error_rate_limited():
    from chorus.main import _classify_error
    err = Exception("some error")
    code, msg = _classify_error("gemini", err, page_text="You've reached your limit today")
    assert code == "rate_limited"


def test_classify_error_selector_error():
    from chorus.main import _classify_error
    from playwright._impl._errors import TimeoutError as PlaywrightTimeout
    err = PlaywrightTimeout("waiting for selector failed")
    code, msg = _classify_error("gemini", err, page_text="normal page content")
    assert code == "selector_error"


def test_retry_endpoint_404_for_unknown_session():
    c = _make_client()
    r = c.post("/api/sessions/nosuchid/retry/gemini")
    assert r.status_code == 404


def test_retry_endpoint_409_on_concurrent_retry():
    import chorus.main as m
    # Simulate a retry already in progress
    m.active_sessions["sess1"] = {
        "prompt": "test", "platforms": ["gemini"],
        "responses": {}, "status": "complete",
        "_retrying": {"gemini"},
    }
    c = _make_client()
    r = c.post("/api/sessions/sess1/retry/gemini")
    assert r.status_code == 409
    del m.active_sessions["sess1"]
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_retry.py -v
```
Expected: FAIL — `_classify_error` not defined, retry endpoint missing

- [ ] **Step 3: Add _classify_error() to chorus/main.py**

Add after imports:
```python
import asyncio
from playwright.async_api import TimeoutError as PlaywrightTimeout  # public API, not _impl

_UNIVERSAL_RATE_SIGNALS = [
    "too many requests", "rate limit", "try again later", "quota exceeded",
]

def _classify_error(platform: str, exc: Exception, page_text: str = "") -> tuple[str, str]:
    """Map an exception + page content to (error_code, human_message)."""

    page_lower = page_text.lower()

    # Check rate limit signals (platform-specific + universal)
    signals = list(ALL_SELECTORS.get(platform, {}).get("rate_limit_signals", []))
    signals += _UNIVERSAL_RATE_SIGNALS
    if any(s.lower() in page_lower for s in signals):
        return "rate_limited", f"{PLATFORM_META[platform]['name']} is rate-limiting requests. Wait a moment and retry."

    # asyncio.TimeoutError is a subclass of TimeoutError in Python 3.11+,
    # but a separate class in 3.10. Check both to be safe.
    if isinstance(exc, (asyncio.TimeoutError, TimeoutError)) and not isinstance(exc, PlaywrightTimeout):
        return "timeout", f"{PLATFORM_META[platform]['name']} took too long to respond. Try retrying."

    if isinstance(exc, PlaywrightTimeout) and "rate" not in page_lower:
        return "selector_error", (
            f"{PLATFORM_META[platform]['name']} UI may have changed. "
            "Check for a platform update in the Chorus repo."
        )

    return "unknown", f"{PLATFORM_META[platform]['name']}: {str(exc)[:120]}"
```

- [ ] **Step 4: Update run_platform() to use error taxonomy**

Replace the existing `run_platform()` function:
```python
async def run_platform(session_id: str, platform_key: str, prompt: str, profile: str):
    await ws_manager.send_status(session_id, platform_key, "waiting", "Opening browser…")
    platform_timeout = ALL_SELECTORS.get(platform_key, {}).get("timeout_seconds", 60)
    page_text = ""
    try:
        page = await browser_manager.get_page(platform_key, profile)
        PlatformClass = PLATFORMS[platform_key]
        ai = PlatformClass(page)

        await ws_manager.send_status(session_id, platform_key, "typing", "Submitting prompt…")
        await asyncio.wait_for(ai.submit_prompt(prompt), timeout=platform_timeout)

        await ws_manager.send_status(session_id, platform_key, "typing", "Waiting for response…")
        response = await asyncio.wait_for(
            ai.wait_for_response(timeout=platform_timeout),
            timeout=platform_timeout + 5,
        )

        active_sessions[session_id]["responses"][platform_key] = response
        await ws_manager.send_status(session_id, platform_key, "done", "Done", response)

    except Exception as e:
        try:
            page = await browser_manager.get_page(platform_key, profile)
            page_text = await page.content()
        except Exception:
            pass
        error_code, message = _classify_error(platform_key, e, page_text)

        # Attempt to check if auth expired — page state may be broken after a failure,
        # so this is best-effort only. Wrapped in its own try/except.
        try:
            p = await browser_manager.get_page(platform_key, profile)
            ai = PLATFORMS[platform_key](p)
            if not await asyncio.wait_for(ai.is_authenticated(), timeout=5):
                error_code, message = "auth_expired", (
                    f"Your {PLATFORM_META[platform_key]['name']} session has expired. "
                    "Click Re-login to reconnect."
                )
        except Exception:
            pass  # auth check is best-effort; use the error_code from _classify_error if this fails

        active_sessions[session_id]["responses"][platform_key] = {
            "error": True, "error_code": error_code, "message": message,
        }
        await ws_manager.send_status(session_id, platform_key, "error", message,
                                     extra={"error_code": error_code})
```

> NOTE: Also update `ALL_SELECTORS` in `main.py`. Add after the PLATFORMS dict:
> ```python
> from chorus.platforms.base import ALL_SELECTORS
> ```
> (It's already loaded in base.py — re-import it here so `run_platform` can use it.)

- [ ] **Step 5: Add retry endpoint**

```python
_retry_locks: dict[str, asyncio.Lock] = {}  # key = "{session_id}:{platform}"

@app.post("/api/sessions/{session_id}/retry/{platform}")
async def retry_platform(session_id: str, platform: str):
    if session_id not in active_sessions:
        raise HTTPException(404, "Session not found")
    if platform not in PLATFORMS:
        raise HTTPException(400, f"Unknown platform: {platform}")

    lock_key = f"{session_id}:{platform}"
    lock = _retry_locks.setdefault(lock_key, asyncio.Lock())

    if lock.locked():
        raise HTTPException(409, "Retry already in progress for this platform")

    session = active_sessions[session_id]
    retrying: set = session.setdefault("_retrying", set())

    if platform in retrying:
        raise HTTPException(409, "Retry already in progress for this platform")

    retry_count = session.setdefault("_retry_counts", {})
    if retry_count.get(platform, 0) >= 3:
        raise HTTPException(422, json.dumps({"error": "Max retries reached", "code": "max_retries"}))

    retrying.add(platform)
    retry_count[platform] = retry_count.get(platform, 0) + 1

    profile = session.get("profiles", {}).get(platform, "default")
    asyncio.create_task(_retry_and_cleanup(session_id, platform, session["prompt"], profile))
    return {"ok": True}


async def _retry_and_cleanup(session_id: str, platform: str, prompt: str, profile: str):
    try:
        await run_platform(session_id, platform, prompt, profile)
    finally:
        active_sessions.get(session_id, {}).get("_retrying", set()).discard(platform)
```

- [ ] **Step 6: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 7: Commit**
```bash
git add chorus/main.py
git commit -m "feat: error taxonomy, per-platform timeout, retry endpoint"
```

---

### Task 10: Update frontend for error states and retry

**Files:**
- Modify: `chorus/frontend/index.html` — update platform cards to show error + Retry/Re-login buttons

- [ ] **Step 1: Find the platform card rendering in index.html**

Search for the section that renders platform response cards (look for `"done"`, `"error"` status handling in the WebSocket message handler).

- [ ] **Step 2: Update error card rendering**

When a WebSocket message arrives with `status === "error"`, render the platform card with:
```javascript
// In the WebSocket message handler, update the platform card on error:
function renderPlatformCard(platform, status, message, extra) {
  const card = document.getElementById(`card-${platform}`);
  if (!card) return;

  if (status === 'error') {
    const errorCode = (extra || {}).error_code || 'unknown';
    const isAuthExpired = errorCode === 'auth_expired';
    const retryLabel = isAuthExpired ? 'Re-login' : 'Retry';
    const retryAction = isAuthExpired
      ? `ob_startPlatform('${platform}'); openOnboarding();`
      : `retryPlatform('${platform}')`;

    card.style.borderColor = '#ef4444';
    card.querySelector('.card-status').innerHTML = `
      <span style="color:#f87171;font-size:.8rem;">⚠ ${message}</span>
      <div style="margin-top:8px;display:flex;gap:8px;">
        <button onclick="${retryAction}" data-retry="${platform}"
                style="padding:5px 14px;background:${isAuthExpired?'#7c3aed':'#dc2626'};
                       border:none;border-radius:5px;cursor:pointer;color:#fff;font-size:.78rem;">
          ${retryLabel}
        </button>
      </div>`;
  }
}

async function retryPlatform(platform) {
  const sessionId = window._currentSessionId;
  if (!sessionId) return;
  const btn = document.querySelector(`[data-retry="${platform}"]`);
  if (btn) { btn.disabled = true; btn.textContent = 'Retrying…'; }

  const r = await fetch(`/api/sessions/${sessionId}/retry/${platform}`, {method:'POST'});
  if (!r.ok) {
    const d = await r.json();
    if (btn) { btn.disabled = false; btn.textContent = 'Retry'; }
    if (d.code === 'max_retries' && btn) {
      btn.textContent = 'Max retries reached';
      btn.style.opacity = '.4';
    }
  }
}
```

> **Note:** The exact integration point depends on the current WebSocket handler structure in `index.html`. Find the handler that processes `{type: "status", platform: ..., status: ...}` messages and add the error card logic there. Preserve all existing card rendering for `"waiting"`, `"typing"`, and `"done"` states.

- [ ] **Step 3: Manual smoke test**
```bash
chorus
```
1. Submit a query with all platforms selected
2. Disconnect from the internet mid-query — platform cards should show red error borders with messages
3. Reconnect and click Retry on a failed platform — spinner appears, result populates

- [ ] **Step 4: Run full test suite**
```bash
pytest tests/ -v --tb=short
```
Expected: all PASS

- [ ] **Step 5: Commit**
```bash
git add chorus/frontend/index.html
git commit -m "feat: error card states, Retry + Re-login buttons per platform"
```

---

## Chunk 4: Final Verification

### Task 11: End-to-end clean install test

- [ ] **Step 1: Uninstall and reinstall**
```bash
pip uninstall chorus -y
pip install -e .
```

- [ ] **Step 2: Verify Chromium probe works**
```bash
# Temporarily rename the Playwright executable to simulate missing binary
chorus 2>&1 | head -5
```
(If Chromium IS installed, server starts. To test the missing-binary path, temporarily uninstall: `playwright uninstall chromium`, run `chorus`, confirm error message, then `playwright install chromium`.)

Expected error message: `Playwright Chromium not found. Run: playwright install chromium`

- [ ] **Step 3: Wipe ~/.chorus/ to test first-run onboarding**
```bash
mv ~/.chorus ~/.chorus.bak 2>/dev/null || true
chorus &
sleep 3
curl -sf http://localhost:4747/ | grep -i "onboarding\|welcome" | head -3
kill %1
```
Expected: Page served, onboarding wizard auto-triggered on load

- [ ] **Step 4: Run full test suite**
```bash
pytest tests/ -v --tb=short
```
Expected: all PASS

- [ ] **Step 5: Restore backup**
```bash
rm -rf ~/.chorus
mv ~/.chorus.bak ~/.chorus 2>/dev/null || true
```

- [ ] **Step 6: Push to GitHub**
```bash
git push origin master
```
