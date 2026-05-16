# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project does

Python automation bot that reads release metadata from `distrokid_batch_template.xlsx`, scans `Songs/` for audio files, and auto-fills + submits the DistroKid upload form using Playwright.

## Setup

```bash
pip install pandas openpyxl playwright
python -m playwright install chromium
```

## Commands

```bash
# Discover live form selectors (run when form breaks)
python upload_bot.py --discover

# Upload all new songs in Songs/
python upload_bot.py

# Alternative discovery script (uses persistent Chrome profile instead of cookies)
python discover_selectors.py
```

## Auth

The bot authenticates by injecting session cookies, not by launching your real Chrome profile. Before running:
1. Install the [Cookie-Editor](https://chrome.google.com/webstore/detail/cookie-editor/hlkenndednhfkekhgcdicdfddnkalmdm) Chrome extension
2. Go to distrokid.com while logged in
3. Cookie-Editor → Export → Export as JSON → save as `distrokid_cookies.json`

If cookies expire, re-export and overwrite `distrokid_cookies.json`.

## Architecture

**`upload_bot.py`** — single-file bot, all logic here:
- `parse_metadata()` — reads `Release Info` sheet from xlsx (skips 3 header rows, A:B columns as key/value pairs)
- `scan_songs_folder()` — scans `Songs/` for audio files; language is auto-detected from filename keywords (e.g. `(French)` → `"French"`, `(Mandarin)` → `"Chinese (Simplified)"`)
- `load_cookies()` / `ensure_auth()` — loads `distrokid_cookies.json` and injects into a new headless Playwright context
- `get_uploaded_titles()` — scrapes distrokid.com/mymusic/ to detect already-uploaded songs (prevents duplicates)
- `fill_release_form()` — fills one single-song release; each audio file is treated as its own single (album title = track title)
- `click_through_submit()` — auto-clicks through the post-fill button sequence (Continue → dismiss dialogs → Upload/Done)
- `run_discover()` — dumps all form field names/IDs to `selectors.json`

**`discover_selectors.py`** — standalone alternative to `--discover`; uses a persistent Chrome profile (`C:\Users\Tharma\AppData\Local\Google\Chrome\User Data`) instead of cookie injection.

## Form-fill fallback chain

Filling form fields uses a three-tier approach:
1. Playwright's native `fill()` / `select_option()` on CSS selectors
2. JS property setter (`Object.getOwnPropertyDescriptor(...).set`) to bypass React's controlled inputs
3. Synthetic DOM events (`input`, `change`, `blur`) to trigger React state updates

When selectors break (DistroKid updates their form), run `--discover` and update the selector lists at the top of `fill_release_form()`.

## Key files

| File | Purpose |
|------|---------|
| `distrokid_batch_template.xlsx` | Release metadata (artist, genre, release date, songwriter) |
| `Songs/` | Audio files (`.wav`, `.flac`, `.aiff`, `.mp3`) + one artwork image |
| `distrokid_cookies.json` | Session cookies — not committed, must be exported manually |
| `selectors.json` | Output of `--discover`; documents current live form fields |
| `step_*.png` / `upload_*.png` / `review_*.png` | Debug screenshots from the last run |

## Spreadsheet format

`distrokid_batch_template.xlsx` has a `Release Info` sheet where rows 1–3 are headers and data starts at row 4, column A = field name, column B = value. Key fields used by the bot:

- `* Artist / Band Name`
- `* Primary Genre`
- `* Release Date (yyyy-mm-dd)`
- `🟢 Songwriter(s)` (space-separated first + last name)
