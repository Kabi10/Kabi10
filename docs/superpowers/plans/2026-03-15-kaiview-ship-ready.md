# KaiView Ship-Ready Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make KaiView installable via `pip install kaiview`, auto-configured on first run at `~/.kaiview/`, and configurable through an in-browser settings panel without touching any config file.

**Architecture:** Move `server.py` and `index.html` into a `kaiview/` Python sub-package. Config loading changes from `server.py`-adjacent `config.toml` to `~/.kaiview/config.toml`, created from a bundled template on first run. A `main()` entry point handles startup. Two new API routes (`GET/POST /api/settings`) with a frontend modal complete the settings panel.

**Tech Stack:** Python 3.10+, FastAPI, uvicorn, aiosqlite, tomllib (stdlib ≥3.11, tomli backport <3.11), pathlib, importlib.resources, pytest, FastAPI TestClient

**Spec:** `docs/superpowers/specs/2026-03-15-ship-ready-design.md` (Sections 1 and 3)

---

## File Map

| Action | Path | Purpose |
|--------|------|---------|
| Create | `kaiview/__init__.py` | Package marker, `__version__` |
| Move | `server.py` → `kaiview/server.py` | FastAPI app + new config logic + `main()` |
| Move | `index.html` → `kaiview/index.html` | Frontend SPA |
| Create | `kaiview/config_template.toml` | Bundled default config, copied on first run |
| Create | `pyproject.toml` | Build config, entry point, deps |
| Create | `tests/__init__.py` | Test package |
| Create | `tests/test_packaging.py` | Template bundling + `main()` existence |
| Create | `tests/test_config.py` | Config path resolution, first-run logic |
| Create | `tests/test_settings.py` | GET/POST /api/settings validation |

---

## Chunk 1: PyPI Packaging

### Task 1: Package structure and pyproject.toml

**Files:**
- Create: `kaiview/__init__.py`
- Create: `kaiview/config_template.toml`
- Move: `server.py` → `kaiview/server.py`
- Move: `index.html` → `kaiview/index.html`
- Create: `pyproject.toml`
- Create: `tests/__init__.py`
- Create: `tests/test_packaging.py`

- [ ] **Step 1: Write failing test for bundled config template**

Create `tests/__init__.py` (empty) and `tests/test_packaging.py`:
```python
import importlib.resources
import sys

if sys.version_info >= (3, 11):
    import tomllib
else:
    import tomli as tomllib


def test_config_template_is_bundled():
    """config_template.toml must be accessible as package data after install."""
    text = importlib.resources.files("kaiview").joinpath("config_template.toml").read_text()
    cfg = tomllib.loads(text)
    assert cfg["server"]["port"] == 3737
    assert cfg["projects"]["dev_dir"] == "~"
    assert "health" in cfg
    weights = cfg["health"]
    assert sum(weights.values()) == 100


def test_index_html_is_bundled():
    text = importlib.resources.files("kaiview").joinpath("index.html").read_text()
    assert "<html" in text.lower()
```

- [ ] **Step 2: Run tests — expect FAIL (package not yet structured)**
```bash
cd C:/Dev/kaiview
pytest tests/test_packaging.py -v
```
Expected: `ModuleNotFoundError: No module named 'kaiview'`

- [ ] **Step 3: Create package directory and files**

```bash
mkdir kaiview
```

Create `kaiview/__init__.py`:
```python
__version__ = "1.0.0"
```

Create `kaiview/config_template.toml`:
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

Move existing files into the package:
```bash
mv server.py kaiview/server.py
mv index.html kaiview/index.html
```

- [ ] **Step 4: Create pyproject.toml**

Create `pyproject.toml` at the repo root:
```toml
[build-system]
requires = ["setuptools>=68", "wheel"]
build-backend = "setuptools.backends.legacy:build"

[project]
name = "kaiview"
version = "1.0.0"
description = "AI-powered developer OS dashboard"
readme = "README.md"
license = {text = "MIT"}
requires-python = ">=3.10"
dependencies = [
    "fastapi>=0.100.0",
    "uvicorn[standard]>=0.23.0",
    "pydantic>=2.0.0",
    "aiosqlite>=0.19.0",
    "watchdog>=3.0.0",
    "httpx>=0.25.0",
    "tomli>=2.0.0; python_version < '3.11'",
]

[project.scripts]
kaiview = "kaiview.server:main"

[tool.setuptools.packages.find]
where = ["."]
include = ["kaiview*"]

[tool.setuptools.package-data]
kaiview = ["*.html", "*.toml"]

[tool.pytest.ini_options]
testpaths = ["tests"]
```

- [ ] **Step 5: Install in editable mode and run tests**
```bash
pip install -e .
pytest tests/test_packaging.py -v
```
Expected: PASS

- [ ] **Step 6: Commit**
```bash
git add kaiview/__init__.py kaiview/config_template.toml kaiview/server.py kaiview/index.html pyproject.toml tests/
git commit -m "chore: restructure into kaiview/ package with pyproject.toml"
```

---

### Task 2: Update config loading to ~/.kaiview/

**Files:**
- Modify: `kaiview/server.py` — replace config block (lines 30–53), fix all config key references
- Create: `tests/test_config.py`

> **Current issue:** `server.py` loads config from `Path(__file__).parent / "config.toml"` using key `CFG.get("kaiview", {})` for dev_dir/skip and `CFG.get("github", {}).get("token")` for PAT. These all need updating.
>
> **Migration note:** Existing users may have `~/.kaiview/config.toml` (or the old repo-adjacent `config.toml`) using the old key structure (`[kaiview]` section, `token` instead of `pat`). The new `_load_config_from()` must detect and silently migrate old keys so existing installs don't break.
>
> **DB migration note:** Old `kaiview.db` lived at `Path(__file__).parent / "kaiview.db"` (inside the package dir). `_ensure_config()` must also check for this old DB and copy it to `~/.kaiview/kaiview.db` if the new location doesn't yet have a DB.

- [ ] **Step 1: Write failing tests**

Create `tests/test_config.py`:
```python
import sys
from pathlib import Path
from unittest.mock import patch


def test_build_default_config_has_required_keys():
    from kaiview.server import _build_default_config
    cfg = _build_default_config()
    assert cfg["server"]["port"] == 3737
    assert cfg["projects"]["dev_dir"] == "~"
    assert cfg["github"]["pat"] == ""
    assert "health" in cfg


def test_dev_dir_expands_tilde():
    from kaiview.server import _build_default_config
    cfg = _build_default_config()
    expanded = Path(cfg["projects"]["dev_dir"]).expanduser().resolve()
    assert expanded.exists()  # home dir always exists


def test_load_config_from_reads_toml(tmp_path):
    from kaiview.server import _load_config_from
    cfg_file = tmp_path / "config.toml"
    cfg_file.write_text(
        '[server]\nport = 9999\n[projects]\ndev_dir = "~"\nskip = []\n'
        '[github]\npat = ""\n[health]\ncommit_weight=40\ndirty_weight=20\n'
        'readme_weight=20\ndescription_weight=20\n',
        encoding="utf-8",
    )
    cfg = _load_config_from(cfg_file)
    assert cfg["server"]["port"] == 9999


def test_db_path_is_in_kaiview_dir():
    import kaiview.server as srv
    assert ".kaiview" in str(srv.DB_PATH)


def test_migrate_config_keys_old_kaiview_section():
    from kaiview.server import _migrate_config_keys
    old = {"kaiview": {"dev_dir": "~/projects", "skip": []}, "github": {"token": "ghp_abc"}}
    new = _migrate_config_keys(old)
    assert "projects" in new
    assert new["projects"]["dev_dir"] == "~/projects"
    assert "kaiview" not in new
    assert new["github"]["pat"] == "ghp_abc"
    assert "token" not in new["github"]
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_config.py -v
```
Expected: `ImportError` — `_build_default_config` doesn't exist yet

- [ ] **Step 3: Replace the config block in kaiview/server.py**

Replace from `# ── Config ──` down through `HEALTH_CFG = ...` (approximately lines 30–53 of the original) with:

```python
import importlib
import importlib.resources
import threading
import webbrowser

# ── Config ────────────────────────────────────────────────────────────────────

_KAIVIEW_DIR = Path.home() / ".kaiview"
_CFG_FILE    = _KAIVIEW_DIR / "config.toml"


def _build_default_config() -> dict:
    if sys.version_info >= (3, 11):
        import tomllib as _tl
    else:
        try:
            import tomllib as _tl
        except ImportError:
            import tomli as _tl  # type: ignore
    text = importlib.resources.files("kaiview").joinpath("config_template.toml").read_text()
    return _tl.loads(text)


def _load_config_from(path: Path) -> dict:
    if sys.version_info >= (3, 11):
        import tomllib as _tl
    else:
        try:
            import tomllib as _tl
        except ImportError:
            import tomli as _tl  # type: ignore
    try:
        return _tl.loads(path.read_text(encoding="utf-8"))
    except Exception as e:
        print(f"[kaiview] config.toml parse error: {e} — using defaults")
        return _build_default_config()


def _migrate_config_keys(cfg: dict) -> dict:
    """Migrate old key structure to new on first load of an existing config."""
    # [kaiview] → [projects]
    if "kaiview" in cfg and "projects" not in cfg:
        cfg["projects"] = cfg.pop("kaiview")
    # github.token → github.pat
    if "github" in cfg and "token" in cfg["github"] and "pat" not in cfg["github"]:
        cfg["github"]["pat"] = cfg["github"].pop("token")
    return cfg


def _ensure_config() -> dict:
    _KAIVIEW_DIR.mkdir(parents=True, exist_ok=True)
    if not _CFG_FILE.exists():
        template_bytes = importlib.resources.files("kaiview").joinpath("config_template.toml").read_bytes()
        _CFG_FILE.write_bytes(template_bytes)
        print(f"[kaiview] Created default config at {_CFG_FILE}")

    # DB migration: copy old kaiview.db from package dir if new location is empty
    new_db = _KAIVIEW_DIR / "kaiview.db"
    if not new_db.exists():
        import importlib.util
        pkg_spec = importlib.util.find_spec("kaiview")
        if pkg_spec and pkg_spec.origin:
            old_db = Path(pkg_spec.origin).parent / "kaiview.db"
            if old_db.exists():
                import shutil
                shutil.copy2(old_db, new_db)
                print(f"[kaiview] Migrated database from {old_db} to {new_db}")

    return _migrate_config_keys(_load_config_from(_CFG_FILE))


CFG = _ensure_config()


def _dev_dir() -> Path:
    raw = CFG.get("projects", {}).get("dev_dir", "~")
    return Path(raw).expanduser().resolve()


def _skip_set() -> set:
    return set(CFG.get("projects", {}).get("skip", [
        ".git", "node_modules", "__pycache__", ".venv", "venv"
    ]))


DEV_DIR      = _dev_dir()
SKIP         = _skip_set()
DB_PATH      = _KAIVIEW_DIR / "kaiview.db"
SCHEMA_VER   = 3
GITHUB_TOKEN = CFG.get("github", {}).get("pat", "")
HEALTH_CFG   = CFG.get("health", {})
```

Remove `HTML_FILE` and replace with a pre-loaded string constant (works in both editable installs and built wheels — avoids the `Traversable` vs `Path` problem):
```python
# Read HTML into memory at startup. importlib.resources.files() returns a Traversable,
# NOT a filesystem Path in installed wheels. Always read the content directly.
_HTML_CONTENT: str = importlib.resources.files("kaiview").joinpath("index.html").read_text(encoding="utf-8")
```

Update the root route to serve `_HTML_CONTENT` instead of reading `HTML_FILE`:
```python
@app.get("/", response_class=HTMLResponse)
def root():
    return _HTML_CONTENT
```

- [ ] **Step 4: Fix remaining old config key references in server.py**

Search for any remaining old-style config lookups and update:
```bash
grep -n 'CFG.get("kaiview"' kaiview/server.py
grep -n 'get("token"' kaiview/server.py
grep -n 'Path(__file__).parent' kaiview/server.py
```
- Replace `CFG.get("kaiview", {}).get("dev_dir"` → `CFG.get("projects", {}).get("dev_dir"`
- Replace `CFG.get("github", {}).get("token"` → `CFG.get("github", {}).get("pat"`
- Replace any `Path(__file__).parent / "..."` for config/db/html with the new `_KAIVIEW_DIR` / `importlib.resources` equivalents above

- [ ] **Step 5: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 6: Commit**
```bash
git add kaiview/server.py tests/test_config.py
git commit -m "feat: load config from ~/.kaiview/config.toml, move DB to ~/.kaiview/"
```

---

### Task 3: Add main() entry point

**Files:**
- Modify: `kaiview/server.py` — add `main()` at the bottom

- [ ] **Step 1: Write failing test**

Add to `tests/test_packaging.py`:
```python
import inspect


def test_main_is_callable_with_no_required_args():
    from kaiview.server import main
    assert callable(main)
    sig = inspect.signature(main)
    required = [p for p in sig.parameters.values()
                if p.default is inspect.Parameter.empty]
    assert len(required) == 0
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_packaging.py::test_main_is_callable_with_no_required_args -v
```
Expected: FAIL — `ImportError: cannot import name 'main'`

- [ ] **Step 3: Add main() to kaiview/server.py**

At the bottom of `kaiview/server.py`, replace or add:
```python
def main():
    """Entry point for `kaiview` CLI command."""
    port = CFG.get("server", {}).get("port", 3737)

    def _open_browser():
        import time
        time.sleep(1.2)
        try:
            webbrowser.open(f"http://localhost:{port}")
        except Exception:
            pass  # silently skip on headless/CI

    threading.Thread(target=_open_browser, daemon=True).start()
    uvicorn.run("kaiview.server:app", host="127.0.0.1", port=port, log_level="warning")


if __name__ == "__main__":
    main()
```

- [ ] **Step 4: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 5: Smoke test the entry point**
```bash
kaiview &
sleep 3
curl -sf http://localhost:3737/ | head -3
kill %1
```
Expected: HTML returned, no errors

- [ ] **Step 6: Commit**
```bash
git add kaiview/server.py
git commit -m "feat: add main() entry point — kaiview command now works"
```

---

## Chunk 2: Settings Panel

### Task 4: Settings panel — backend

**Files:**
- Modify: `kaiview/server.py` — add `SettingsResponse`, `SettingsUpdate`, `HealthWeights` models; `GET /api/settings`; `POST /api/settings`
- Create: `tests/test_settings.py`

- [ ] **Step 1: Write failing tests**

Create `tests/test_settings.py`:
```python
import pytest
from pathlib import Path

if __import__("sys").version_info >= (3, 11):
    import tomllib
else:
    import tomli as tomllib


@pytest.fixture
def client(tmp_path):
    """TestClient with isolated config file."""
    import kaiview.server as srv
    cfg_text = (
        '[server]\nport = 3737\n[projects]\ndev_dir = "~"\nskip = []\n'
        '[github]\npat = ""\n[health]\ncommit_weight=40\ndirty_weight=20\n'
        'readme_weight=20\ndescription_weight=20\n'
    )
    cfg_file = tmp_path / "config.toml"
    cfg_file.write_text(cfg_text, encoding="utf-8")
    # Redirect server's config file pointer
    srv._CFG_FILE = cfg_file
    srv.CFG = srv._load_config_from(cfg_file)
    from fastapi.testclient import TestClient
    return TestClient(srv.app)


def test_get_settings_returns_expected_shape(client):
    r = client.get("/api/settings")
    assert r.status_code == 200
    d = r.json()
    for key in ("port", "dev_dir", "github_pat", "skip", "health"):
        assert key in d, f"missing key: {key}"
    for w in ("commit_weight", "dirty_weight", "readme_weight", "description_weight"):
        assert w in d["health"]


def test_get_settings_masks_non_empty_pat(tmp_path):
    import kaiview.server as srv
    cfg_file = tmp_path / "config.toml"
    cfg_file.write_text(
        '[server]\nport=3737\n[projects]\ndev_dir="~"\nskip=[]\n'
        '[github]\npat="ghp_realtoken1234"\n[health]\ncommit_weight=40\n'
        'dirty_weight=20\nreadme_weight=20\ndescription_weight=20\n'
    )
    srv._CFG_FILE = cfg_file
    srv.CFG = srv._load_config_from(cfg_file)
    from fastapi.testclient import TestClient
    c = TestClient(srv.app)
    r = c.get("/api/settings")
    assert r.json()["github_pat"] == "__MASKED__"


def test_get_settings_empty_pat_returns_empty_string(client):
    r = client.get("/api/settings")
    assert r.json()["github_pat"] == ""


def _valid_body(**overrides):
    body = {
        "dev_dir": str(Path.home()),
        "port": 3737,
        "github_pat": "",
        "skip": [".git"],
        "health": {
            "commit_weight": 40, "dirty_weight": 20,
            "readme_weight": 20, "description_weight": 20,
        },
    }
    body.update(overrides)
    return body


def test_post_settings_success(client):
    r = client.post("/api/settings", json=_valid_body())
    assert r.status_code == 200
    assert r.json()["ok"] is True


def test_post_settings_invalid_dev_dir(client):
    r = client.post("/api/settings", json=_valid_body(dev_dir="/nonexistent/xyz_abc_123"))
    assert r.status_code == 422
    assert r.json()["code"] == "dev_dir_not_found"


def test_post_settings_weights_not_100(client):
    r = client.post("/api/settings", json=_valid_body(
        health={"commit_weight": 50, "dirty_weight": 20,
                "readme_weight": 20, "description_weight": 20}
    ))
    assert r.status_code == 422
    assert r.json()["code"] == "weights_dont_sum_to_100"


def test_post_settings_port_change_signals_restart(client, tmp_path):
    import kaiview.server as srv
    cfg_file = tmp_path / "config.toml"
    cfg_file.write_text(
        '[server]\nport=3737\n[projects]\ndev_dir="~"\nskip=[]\n'
        '[github]\npat=""\n[health]\ncommit_weight=40\ndirty_weight=20\n'
        'readme_weight=20\ndescription_weight=20\n'
    )
    srv._CFG_FILE = cfg_file
    srv.CFG = srv._load_config_from(cfg_file)
    from fastapi.testclient import TestClient
    c = TestClient(srv.app)
    r = c.post("/api/settings", json=_valid_body(port=3738))
    assert r.status_code == 200
    data = r.json()
    assert data["restart_required"] is True
    assert data["new_port"] == 3738


def test_post_settings_masked_pat_preserves_original(tmp_path):
    import kaiview.server as srv
    cfg_file = tmp_path / "config.toml"
    cfg_file.write_text(
        '[server]\nport=3737\n[projects]\ndev_dir="~"\nskip=[]\n'
        '[github]\npat="ghp_original_secret"\n[health]\ncommit_weight=40\n'
        'dirty_weight=20\nreadme_weight=20\ndescription_weight=20\n'
    )
    srv._CFG_FILE = cfg_file
    srv.CFG = srv._load_config_from(cfg_file)
    from fastapi.testclient import TestClient
    c = TestClient(srv.app)
    c.post("/api/settings", json=_valid_body(github_pat="__MASKED__"))
    saved = tomllib.loads(cfg_file.read_text())
    assert saved["github"]["pat"] == "ghp_original_secret"
```

- [ ] **Step 2: Run — expect FAIL**
```bash
pytest tests/test_settings.py -v
```
Expected: FAIL — `/api/settings` routes don't exist

- [ ] **Step 3: Add models to kaiview/server.py**

After the existing Pydantic models, add:
```python
# ── Settings models ───────────────────────────────────────────────────────────

class HealthWeights(BaseModel):
    commit_weight:      int
    dirty_weight:       int
    readme_weight:      int
    description_weight: int

class SettingsResponse(BaseModel):
    port:       int
    dev_dir:    str
    github_pat: str
    skip:       list[str]
    health:     HealthWeights

class SettingsUpdate(BaseModel):
    port:       int
    dev_dir:    str
    github_pat: str
    skip:       list[str]
    health:     HealthWeights
```

- [ ] **Step 4: Add GET /api/settings route**

```python
@app.get("/api/settings", response_model=SettingsResponse)
def get_settings():
    raw_pat = CFG.get("github", {}).get("pat", "")
    return {
        "port":       CFG.get("server", {}).get("port", 3737),
        "dev_dir":    CFG.get("projects", {}).get("dev_dir", "~"),
        "github_pat": "__MASKED__" if raw_pat else "",
        "skip":       CFG.get("projects", {}).get("skip", []),
        "health":     CFG.get("health", {
            "commit_weight": 40, "dirty_weight": 20,
            "readme_weight": 20, "description_weight": 20,
        }),
    }
```

- [ ] **Step 5: Add POST /api/settings route**

```python
@app.post("/api/settings")
def update_settings(body: SettingsUpdate):
    from fastapi.responses import JSONResponse

    dev_path = Path(body.dev_dir).expanduser().resolve()
    if not dev_path.is_dir():
        return JSONResponse(422, {"error": f"Directory not found: {body.dev_dir}", "code": "dev_dir_not_found"})

    if not (1024 <= body.port <= 65535):
        return JSONResponse(422, {"error": "Port must be 1024–65535", "code": "invalid_port"})

    w = body.health
    total = w.commit_weight + w.dirty_weight + w.readme_weight + w.description_weight
    if total != 100:
        return JSONResponse(422, {"error": f"Weights must sum to 100 (got {total})", "code": "weights_dont_sum_to_100"})

    existing_pat = CFG.get("github", {}).get("pat", "")
    new_pat = existing_pat if body.github_pat == "__MASKED__" else body.github_pat
    current_port = CFG.get("server", {}).get("port", 3737)

    skip_toml = ", ".join(f'"{s}"' for s in body.skip)
    new_toml = (
        f'[server]\nport = {body.port}\n\n'
        f'[projects]\ndev_dir = "{body.dev_dir}"\nskip = [{skip_toml}]\n\n'
        f'[github]\npat = "{new_pat}"\n\n'
        f'[health]\ncommit_weight = {w.commit_weight}\n'
        f'dirty_weight = {w.dirty_weight}\n'
        f'readme_weight = {w.readme_weight}\n'
        f'description_weight = {w.description_weight}\n'
    )
    _CFG_FILE.write_text(new_toml, encoding="utf-8")

    # Hot-reload in-memory config.
    # DB_PATH is used via module global in every aiosqlite.connect(DB_PATH) call —
    # aiosqlite opens a new connection per request (no pool), so reassigning
    # the global works immediately. Port is written to disk ONLY; the running
    # uvicorn instance is NOT restarted — user must restart kaiview manually.
    global CFG, DEV_DIR, SKIP, GITHUB_TOKEN, HEALTH_CFG
    CFG          = _load_config_from(_CFG_FILE)
    DEV_DIR      = _dev_dir()
    SKIP         = _skip_set()
    GITHUB_TOKEN = CFG.get("github", {}).get("pat", "")
    HEALTH_CFG   = CFG.get("health", {})

    result: dict = {"ok": True}
    if body.port != current_port:
        result["restart_required"] = True
        result["new_port"]         = body.port
    return result
```

- [ ] **Step 6: Run all tests**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 7: Commit**
```bash
git add kaiview/server.py tests/test_settings.py
git commit -m "feat: add GET/POST /api/settings with validation and hot-reload"
```

---

### Task 5: Settings panel — frontend modal

**Files:**
- Modify: `kaiview/index.html` — gear icon in header, modal overlay, JS

- [ ] **Step 1: Add gear icon button to the header**

In `kaiview/index.html`, find the header element (look for `<header` or the top nav bar). Add the gear button alongside any existing header controls:
```html
<button id="settings-btn" onclick="openSettings()" title="Settings"
  style="background:none;border:none;cursor:pointer;font-size:1.1rem;
         padding:4px 8px;opacity:0.65;color:inherit;" aria-label="Open settings">⚙</button>
```

- [ ] **Step 2: Add settings modal HTML**

Before the closing `</body>` tag:
```html
<!-- ── Settings Modal ─────────────────────────────────────────── -->
<div id="settings-overlay"
     style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.55);
            z-index:900;align-items:center;justify-content:center;">
  <div style="background:var(--card-bg,#1e1e1e);border:1px solid var(--border,#2e2e2e);
              border-radius:12px;padding:28px 32px;width:540px;max-height:85vh;
              overflow-y:auto;position:relative;box-shadow:0 20px 60px rgba(0,0,0,.4);">
    <button onclick="closeSettings()"
            style="position:absolute;top:14px;right:18px;background:none;border:none;
                   cursor:pointer;font-size:1.1rem;opacity:0.6;color:inherit;">✕</button>
    <h2 style="margin:0 0 22px;font-size:1rem;font-weight:600;letter-spacing:.02em;">Settings</h2>

    <!-- Projects -->
    <section style="margin-bottom:18px;">
      <h3 style="font-size:.8rem;opacity:.5;text-transform:uppercase;letter-spacing:.08em;
                 margin:0 0 10px;">Projects</h3>
      <label style="display:block;margin-bottom:12px;">
        <span style="font-size:.85rem;display:block;margin-bottom:5px;">Projects folder</span>
        <input id="s-devdir" type="text" placeholder="~/projects"
               style="width:100%;box-sizing:border-box;padding:9px 12px;
                      background:var(--input-bg,#252525);border:1px solid var(--border,#2e2e2e);
                      border-radius:7px;color:inherit;font-size:.9rem;font-family:inherit;">
        <span id="s-devdir-err" style="display:none;color:#f87171;font-size:.78rem;margin-top:4px;"></span>
      </label>
      <label style="display:block;">
        <span style="font-size:.85rem;display:block;margin-bottom:5px;">Skip folders (comma-separated)</span>
        <input id="s-skip" type="text" placeholder=".git, node_modules, __pycache__"
               style="width:100%;box-sizing:border-box;padding:9px 12px;
                      background:var(--input-bg,#252525);border:1px solid var(--border,#2e2e2e);
                      border-radius:7px;color:inherit;font-size:.9rem;font-family:inherit;">
      </label>
    </section>

    <!-- GitHub -->
    <section style="margin-bottom:18px;">
      <h3 style="font-size:.8rem;opacity:.5;text-transform:uppercase;letter-spacing:.08em;
                 margin:0 0 10px;">GitHub</h3>
      <label style="display:block;">
        <span style="font-size:.85rem;display:block;margin-bottom:5px;">
          Personal Access Token
          <a href="https://github.com/settings/tokens" target="_blank"
             style="font-size:.75rem;opacity:.5;margin-left:8px;color:inherit;">Create →</a>
        </span>
        <!-- PAT field: shows placeholder when masked. Clear the field entirely to remove the PAT. -->
        <input id="s-pat" type="password" placeholder="ghp_... (blank to remove)"
               style="width:100%;box-sizing:border-box;padding:9px 12px;
                      background:var(--input-bg,#252525);border:1px solid var(--border,#2e2e2e);
                      border-radius:7px;color:inherit;font-size:.9rem;font-family:monospace;">
      </label>
    </section>

    <!-- Server -->
    <section style="margin-bottom:18px;">
      <h3 style="font-size:.8rem;opacity:.5;text-transform:uppercase;letter-spacing:.08em;
                 margin:0 0 10px;">Server</h3>
      <label style="display:flex;align-items:center;gap:12px;">
        <span style="font-size:.85rem;">Port</span>
        <input id="s-port" type="number" min="1024" max="65535"
               style="width:110px;padding:9px 12px;background:var(--input-bg,#252525);
                      border:1px solid var(--border,#2e2e2e);border-radius:7px;
                      color:inherit;font-size:.9rem;font-family:inherit;">
      </label>
      <div id="s-port-warn"
           style="display:none;margin-top:10px;padding:9px 13px;
                  background:rgba(217,119,6,.1);border:1px solid rgba(217,119,6,.4);
                  border-radius:7px;font-size:.8rem;color:#d97706;">
        ⚠ Changing the port requires restarting kaiview
      </div>
    </section>

    <!-- Health Weights -->
    <section style="margin-bottom:24px;">
      <h3 style="font-size:.8rem;opacity:.5;text-transform:uppercase;letter-spacing:.08em;
                 margin:0 0 10px;">Health Score Weights</h3>
      <div id="s-weights-err"
           style="display:none;color:#f87171;font-size:.78rem;margin-bottom:8px;">
        Weights must sum to 100 — current total: <strong id="s-wsum">0</strong>
      </div>
      <div id="s-sliders"></div>
    </section>

    <div style="display:flex;gap:10px;justify-content:flex-end;align-items:center;">
      <span id="s-toast" style="display:none;font-size:.82rem;padding:6px 14px;
                                 border-radius:6px;"></span>
      <button onclick="closeSettings()"
              style="padding:8px 18px;background:none;border:1px solid var(--border,#2e2e2e);
                     border-radius:7px;cursor:pointer;color:inherit;font-size:.88rem;">Cancel</button>
      <button id="s-save" onclick="saveSettings()"
              style="padding:8px 20px;background:#2563eb;border:none;border-radius:7px;
                     cursor:pointer;color:#fff;font-size:.88rem;font-weight:500;">Save</button>
    </div>
  </div>
</div>
```

- [ ] **Step 3: Add settings JavaScript**

In a `<script>` block (before `</body>`):
```javascript
// ── Settings ────────────────────────────────────────────────────────────────
(function () {
  const WEIGHT_KEYS   = ['commit_weight','dirty_weight','readme_weight','description_weight'];
  const WEIGHT_LABELS = {
    commit_weight: 'Commit staleness', dirty_weight: 'Dirty files',
    readme_weight: 'Has README',       description_weight: 'Has description',
  };
  let _origPort = 3737;

  window.openSettings = async function () {
    const r = await fetch('/api/settings');
    const d = await r.json();
    _origPort = d.port;
    document.getElementById('s-devdir').value = d.dev_dir || '';
    document.getElementById('s-skip').value   = (d.skip || []).join(', ');
    // If PAT is masked: show placeholder text, leave value as __MASKED__.
    // User can clear the field entirely to delete the PAT, or type a new value to replace it.
    document.getElementById('s-pat').value = d.github_pat || '';
    if (d.github_pat === '__MASKED__') {
      document.getElementById('s-pat').placeholder = 'PAT saved — clear to remove, or type to replace';
    }
    document.getElementById('s-port').value    = d.port;
    document.getElementById('s-port-warn').style.display = 'none';
    document.getElementById('s-devdir-err').style.display = 'none';

    const box = document.getElementById('s-sliders');
    box.innerHTML = '';
    for (const k of WEIGHT_KEYS) {
      const v = (d.health || {})[k] ?? 25;
      box.insertAdjacentHTML('beforeend', `
        <label style="display:flex;align-items:center;gap:10px;margin-bottom:10px;">
          <span style="font-size:.83rem;min-width:135px;opacity:.8;">${WEIGHT_LABELS[k]}</span>
          <input type="range" min="0" max="100" value="${v}" id="hw-${k}"
                 oninput="_syncWeights()" style="flex:1;accent-color:#2563eb;">
          <span id="hwv-${k}" style="min-width:26px;text-align:right;font-size:.83rem;">${v}</span>
        </label>`);
    }
    _syncWeights();

    const overlay = document.getElementById('settings-overlay');
    overlay.style.display = 'flex';
    document.addEventListener('keydown', _escHandler);
  };

  window._syncWeights = function () {
    let sum = 0;
    for (const k of WEIGHT_KEYS) {
      const v = parseInt(document.getElementById('hw-' + k)?.value ?? 0);
      const lbl = document.getElementById('hwv-' + k);
      if (lbl) lbl.textContent = v;
      sum += v;
    }
    document.getElementById('s-wsum').textContent = sum;
    const bad = sum !== 100;
    document.getElementById('s-weights-err').style.display = bad ? 'block' : 'none';
    const btn = document.getElementById('s-save');
    btn.disabled    = bad;
    btn.style.opacity = bad ? '0.45' : '1';
  };

  document.getElementById('s-port')?.addEventListener('input', function () {
    document.getElementById('s-port-warn').style.display =
      parseInt(this.value) !== _origPort ? 'block' : 'none';
  });

  function _escHandler(e) { if (e.key === 'Escape') closeSettings(); }

  window.closeSettings = function () {
    document.getElementById('settings-overlay').style.display = 'none';
    document.removeEventListener('keydown', _escHandler);
  };

  // Close on overlay click (outside modal box)
  document.getElementById('settings-overlay')?.addEventListener('click', function (e) {
    if (e.target === this) closeSettings();
  });

  window.saveSettings = async function () {
    document.getElementById('s-devdir-err').style.display = 'none';
    const health = {};
    for (const k of WEIGHT_KEYS) health[k] = parseInt(document.getElementById('hw-' + k).value);
    const body = {
      dev_dir:    document.getElementById('s-devdir').value.trim(),
      skip:       document.getElementById('s-skip').value.split(',').map(s => s.trim()).filter(Boolean),
      github_pat: document.getElementById('s-pat').value,
      port:       parseInt(document.getElementById('s-port').value),
      health,
    };
    const r    = await fetch('/api/settings', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
    const data = await r.json();
    if (!r.ok) {
      if (data.code === 'dev_dir_not_found') {
        const el = document.getElementById('s-devdir-err');
        el.textContent = data.error; el.style.display = 'block';
      }
      return;
    }
    const toast = document.getElementById('s-toast');
    if (data.restart_required) {
      toast.style.background = 'rgba(217,119,6,.2)';
      toast.style.color = '#d97706';
      toast.textContent = `Restart kaiview to apply port ${data.new_port}`;
    } else {
      toast.style.background = 'rgba(34,197,94,.15)';
      toast.style.color = '#22c55e';
      toast.textContent = 'Settings saved';
    }
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
  };
})();
```

- [ ] **Step 4: Manual smoke test**
```bash
kaiview
```
1. Open http://localhost:3737
2. Click ⚙ in the header — modal opens with current config values
3. Change dev_dir to `/nonexistent` → Save → inline error appears under the field
4. Set valid dev_dir → adjust health sliders to sum ≠ 100 → Save disabled
5. Restore sum to 100 → Save → green "Settings saved" toast
6. Change port to 3738 → port-change warning banner appears
7. Press Escape → modal closes

- [ ] **Step 5: Run full test suite**
```bash
pytest tests/ -v
```
Expected: all PASS

- [ ] **Step 6: Commit**
```bash
git add kaiview/index.html
git commit -m "feat: in-browser settings panel — gear icon, modal, save/hot-reload"
```

---

## Chunk 3: Final Verification

### Task 6: End-to-end clean install test

- [ ] **Step 1: Uninstall and reinstall from scratch**
```bash
pip uninstall kaiview -y
pip install -e .
```

- [ ] **Step 2: Wipe local config to test first-run**
```bash
mv ~/.kaiview ~/.kaiview.bak 2>/dev/null || true
```

- [ ] **Step 3: Run kaiview and verify first-run behaviour**
```bash
kaiview &
sleep 3
curl -sf http://localhost:3737/ | head -3
kill %1
```
Expected:
- Console prints `[kaiview] Created default config at ~/.kaiview/config.toml`
- HTML response received

- [ ] **Step 4: Verify config file was created with correct defaults**
```bash
cat ~/.kaiview/config.toml
```
Expected: contains `port = 3737`, `dev_dir = "~"`

- [ ] **Step 5: Run full test suite one final time**
```bash
pytest tests/ -v --tb=short
```
Expected: all PASS

- [ ] **Step 6: Restore backup config**
```bash
rm -rf ~/.kaiview
mv ~/.kaiview.bak ~/.kaiview 2>/dev/null || true
```

- [ ] **Step 7: Push to GitHub**
```bash
git push origin master
```
