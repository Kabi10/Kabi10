"""
DistroKid Upload Automation Bot
================================
Reads base metadata from distrokid_batch_template.xlsx and auto-uploads
every audio file in the Songs/ folder as a separate single release.

Language is auto-detected from filename:
  "Rise of K.ai (French).wav"   → French
  "Rise of K.ai (Hindi).wav"    → Hindi
  "Rise of K.ai.wav"            → English (default)

Auth strategy:
  Loads your DistroKid session cookies from distrokid_cookies.json.
  Export this file once from Chrome using the Cookie-Editor extension:
    1. Install: https://chrome.google.com/webstore/detail/cookie-editor/hlkenndednhfkekhgcdicdfddnkalmdm
    2. Go to distrokid.com in Chrome (must be logged in)
    3. Click Cookie-Editor icon → Export → Export as JSON
    4. Save as C:\\Dev\\Distrokid\\distrokid_cookies.json

Usage:
    python upload_bot.py              # Upload all new songs in Songs/
    python upload_bot.py --discover   # Dump form selectors to selectors.json
"""

import os
import sys
import json
import time
import logging
import argparse
import pandas as pd
from datetime import datetime, timedelta
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

# ─── CONFIGURATION ────────────────────────────────────────────────────────────

TEMPLATE_FILE  = "distrokid_batch_template.xlsx"
SONGS_DIR      = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Songs")
COOKIES_FILE   = os.path.join(os.path.dirname(os.path.abspath(__file__)), "distrokid_cookies.json")
UPLOADED_CACHE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "uploaded_titles.json")
UPLOAD_URL     = "https://distrokid.com/new"
AUDIO_EXTENSIONS = (".wav", ".flac", ".aiff", ".aif", ".mp3")

# Translated release titles — K.ai is a proper noun (Digital Ruler), kept as-is
TITLE_MAP = {
    "Arabic":               "صعود K.ai",
    "French":               "L'Ascension de K.ai",
    "Hindi":                "K.ai का उदय",
    "Japanese":             "K.aiの台頭",
    "Korean":               "K.ai의 부상",
    "Chinese (Simplified)": "K.ai的崛起",
    "Portuguese":           "A Ascensão de K.ai",
    "Spanish":              "El Ascenso de K.ai",
    "Tamil":                "K.ai இன் எழுச்சி",
}

# Maps filename stem → DistroKid language label.
# Supports both the new translated filenames and old "(Language Cover)" style.
LANGUAGE_MAP = {
    # New translated filenames (stem without extension, lowercased)
    "صعود k.ai":               "Arabic",
    "l'ascension de k.ai":     "French",
    "k.ai का उदय":             "Hindi",
    "k.aiの台頭":               "Japanese",
    "k.ai의 부상":              "Korean",
    "k.ai的崛起":               "Chinese (Simplified)",
    "a ascensão de k.ai":      "Portuguese",
    "el ascenso de k.ai":      "Spanish",
    "k.ai இன் எழுச்சி":       "Tamil",
    # Legacy keyword fallbacks (old "(Language Cover)" filenames)
    "arabic":     "Arabic",
    "french":     "French",
    "hindi":      "Hindi",
    "japanese":   "Japanese",
    "korean":     "Korean",
    "mandarin":   "Chinese (Simplified)",
    "chinese":    "Chinese (Simplified)",
    "portugese":  "Portuguese",
    "portuguese": "Portuguese",
    "spanish":    "Spanish",
    "tamil":      "Tamil",
}

# ─── LOGGING ──────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(
        open(sys.stdout.fileno(), mode='w', encoding='utf-8', buffering=1, closefd=False)
    )]
)
logger = logging.getLogger("DistroBot")

# ─── EXCEL PARSING ────────────────────────────────────────────────────────────

def parse_metadata(file_path):
    """Returns release_info dict from the Release Info sheet."""
    logger.info(f"Parsing metadata from {file_path} ...")
    ri_df = pd.read_excel(file_path, sheet_name="Release Info",
                          skiprows=3, header=None, usecols="A:B")
    ri_df.columns = ["key", "value"]
    ri_df = ri_df.dropna(subset=["key"])
    ri_df["key"] = ri_df["key"].astype(str).str.strip()
    release_info = ri_df.set_index("key")["value"].to_dict()
    logger.info(f"  Release fields loaded: {len(release_info)}")
    return release_info

# ─── SONG FOLDER SCAN ─────────────────────────────────────────────────────────

def detect_language(filename: str) -> str:
    """Detect DistroKid language label from the filename.
    Tries exact stem match first (for translated filenames), then keyword scan."""
    stem = os.path.splitext(filename)[0].lower().strip()
    if stem in LANGUAGE_MAP:
        return LANGUAGE_MAP[stem]
    for keyword, lang in LANGUAGE_MAP.items():
        if keyword in stem:
            return lang
    return "English"


def find_artwork() -> str | None:
    """Find the artwork image in Songs/."""
    if os.path.isdir(SONGS_DIR):
        for fname in sorted(os.listdir(SONGS_DIR)):
            if fname.lower().endswith((".jpg", ".jpeg", ".png", ".gif")):
                return os.path.abspath(os.path.join(SONGS_DIR, fname))
    return None


def scan_songs_folder() -> list[tuple[str, str, str]]:
    """
    Returns a list of (song_title, language, audio_path) for every audio file
    in the Songs/ folder, sorted alphabetically by filename.
    Images are excluded.
    """
    songs = []
    if not os.path.isdir(SONGS_DIR):
        logger.error(f"Songs directory not found: {SONGS_DIR}")
        return songs
    for fname in sorted(os.listdir(SONGS_DIR)):
        if fname.lower().endswith(AUDIO_EXTENSIONS):
            audio_path = os.path.abspath(os.path.join(SONGS_DIR, fname))
            title      = os.path.splitext(fname)[0]   # strip extension
            language   = detect_language(fname)
            songs.append((title, language, audio_path))
            logger.info(f"  Found: {fname!r} → language={language!r}")
    return songs

# ─── AUTH ─────────────────────────────────────────────────────────────────────

def load_cookies():
    if not os.path.exists(COOKIES_FILE):
        print("\n" + "="*60)
        print("  ERROR: distrokid_cookies.json not found.")
        print("  Export cookies from Chrome via Cookie-Editor extension.")
        print(f"  Save as: {COOKIES_FILE}")
        print("="*60 + "\n")
        sys.exit(1)

    with open(COOKIES_FILE, "r", encoding="utf-8") as f:
        raw = json.load(f)

    SAMESITE_MAP = {
        "no_restriction": "None", "none": "None",
        "lax": "Lax", "strict": "Strict", "unspecified": "Lax",
    }
    pw_cookies = []
    for c in raw:
        expires  = c.get("expirationDate") or c.get("expires")
        raw_ss   = str(c.get("sameSite") or "").lower()
        pw_cookies.append({
            "name":     c["name"],
            "value":    c["value"],
            "domain":   c.get("domain", ""),
            "path":     c.get("path", "/"),
            "expires":  int(float(expires)) if expires and float(expires) > 0 else -1,
            "httpOnly": bool(c.get("httpOnly", False)),
            "secure":   bool(c.get("secure", False)),
            "sameSite": SAMESITE_MAP.get(raw_ss, "Lax"),
        })
    logger.info(f"Loaded {len(pw_cookies)} cookies from {COOKIES_FILE}")
    return pw_cookies


def ensure_auth(playwright):
    cookies = load_cookies()
    browser = playwright.chromium.launch(headless=False)
    context = browser.new_context()
    page    = context.new_page()
    page.goto("https://distrokid.com", wait_until="domcontentloaded", timeout=20000)
    context.add_cookies(cookies)
    page.close()
    logger.info("Session cookies injected — logged in as existing user.")
    return browser, context

# ─── DISCOVERY ────────────────────────────────────────────────────────────────

DISCOVERY_JS = """
() => {
    const out = { inputs: [], selects: [], labels: [], buttons: [] };
    document.querySelectorAll('input').forEach(el => {
        const lbl = el.labels && el.labels[0] ? el.labels[0].innerText.trim() : null;
        out.inputs.push({ type: el.type, name: el.name||null, id: el.id||null,
            label: lbl, placeholder: el.placeholder||null, required: el.required,
            className: el.className.substring(0,80) });
    });
    document.querySelectorAll('select').forEach(el => {
        const lbl = el.labels && el.labels[0] ? el.labels[0].innerText.trim() : null;
        out.selects.push({ name: el.name||null, id: el.id||null, label: lbl,
            options: [...el.options].map(o=>({v:o.value,t:o.text})).slice(0,20),
            required: el.required });
    });
    document.querySelectorAll('label').forEach(el => {
        out.labels.push({ for: el.htmlFor||null, text: el.innerText.trim().substring(0,100) });
    });
    document.querySelectorAll('button,[type=submit]').forEach(el => {
        out.buttons.push({ text: (el.innerText||el.value||'').trim().substring(0,60),
            id: el.id||null, name: el.name||null });
    });
    return out;
}
"""

def run_discover():
    logger.info("=== DISCOVERY MODE ===")
    with sync_playwright() as p:
        browser, context = ensure_auth(p)
        page = context.new_page()
        page.goto(UPLOAD_URL, wait_until="load", timeout=45000)
        time.sleep(2)
        page.screenshot(path="page_initial.png")

        for sel in ["select[name='numSongs']", "#numSongs", "#howManySongsOnThisAlbum"]:
            if page.query_selector(sel):
                page.select_option(sel, index=1)
                time.sleep(2)
                break

        page.screenshot(path="page_expanded.png")
        data = page.evaluate(DISCOVERY_JS)

        with open("selectors.json", "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        logger.info("Saved selectors.json")

        print("\n--- INPUTS ---")
        for i, inp in enumerate(data["inputs"]):
            print(f"  [{i:02d}] type={inp['type']:10} name={str(inp['name'] or ''):35} "
                  f"id={str(inp['id'] or ''):35} label={inp['label'] or ''}")
        print("\n--- SELECTS ---")
        for i, sel in enumerate(data["selects"]):
            opts = [o["t"] for o in sel["options"][:5]]
            print(f"  [{i:02d}] name={str(sel['name'] or ''):35} id={str(sel['id'] or ''):35} "
                  f"opts={opts}")
        print("\nDone — review selectors.json")
        browser.close()

# ─── FORM HELPERS ─────────────────────────────────────────────────────────────

def try_fill(page, selectors, value, description="field", force=False):
    for sel in selectors:
        try:
            el = page.query_selector(sel)
            if el:
                try:
                    el.scroll_into_view_if_needed(timeout=5000)
                except Exception:
                    pass
                if force or el.is_visible():
                    try:
                        el.fill(str(value), timeout=5000)
                        logger.info(f"  ✓ Filled {description}: {value!r} via {sel!r}")
                        return True
                    except Exception:
                        if force:
                            page.evaluate("""(s, v) => {
                                const i = document.querySelector(s);
                                if (!i) return;
                                const setter = Object.getOwnPropertyDescriptor(
                                    window.HTMLInputElement.prototype, 'value').set;
                                setter.call(i, v);
                                ['input', 'change', 'blur'].forEach(e =>
                                    i.dispatchEvent(new Event(e, {bubbles: true})));
                            }""", sel, str(value))
                            logger.info(f"  ✓ Filled {description} via JS: {value!r}")
                            return True
        except Exception:
            pass
    logger.warning(f"  ✗ Could not fill {description}: {selectors}")
    return False


def try_select(page, selectors, value, description="select"):
    for sel in selectors:
        try:
            el = page.query_selector(sel)
            if el:
                try:
                    el.scroll_into_view_if_needed(timeout=5000)
                except Exception:
                    pass
                for match_type in ("label", "value"):
                    try:
                        page.select_option(sel, **{match_type: str(value)}, timeout=5000)
                        logger.info(f"  ✓ Selected {description}: {value!r} ({match_type}) via {sel!r}")
                        return True
                    except Exception:
                        pass
        except Exception:
            pass
    logger.warning(f"  ✗ Could not select {description}: {selectors}")
    return False


def try_check(page, selector, checked=True, description="checkbox"):
    try:
        el = page.query_selector(selector)
        if el:
            el.check() if checked else el.uncheck()
            logger.info(f"  ✓ {'Checked' if checked else 'Unchecked'} {description}")
            return True
    except Exception as e:
        logger.warning(f"  ✗ Could not set {description}: {e}")
    return False

# ─── DUPLICATE CHECK ──────────────────────────────────────────────────────────

def load_uploaded_cache() -> set:
    """Load previously uploaded titles from local JSON cache."""
    if os.path.exists(UPLOADED_CACHE):
        try:
            with open(UPLOADED_CACHE, "r", encoding="utf-8") as f:
                return set(t.lower().strip() for t in json.load(f))
        except Exception:
            pass
    return set()


def save_uploaded_cache(titles: set):
    """Persist uploaded titles to local JSON cache."""
    try:
        with open(UPLOADED_CACHE, "w", encoding="utf-8") as f:
            json.dump(sorted(titles), f, indent=2, ensure_ascii=False)
    except Exception as e:
        logger.warning(f"  Could not save uploaded cache: {e}")


def get_uploaded_titles(page) -> set:
    """Navigate to mymusic and collect all listed titles (lowercase).
    Merges live scrape with local cache so already-known titles are never missed."""
    logger.info("Checking distrokid.com/mymusic/ for already-uploaded songs ...")
    page.goto("https://distrokid.com/mymusic/", wait_until="load", timeout=60000)
    time.sleep(4)

    # Scroll twice to trigger any lazy-loaded content
    for _ in range(2):
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        time.sleep(2)

    titles = page.evaluate("""() => {
        const found = new Set();
        // DistroKid-specific: album title links and named elements
        const specific = [
            'a[href*="/album/"]',
            '[class*="albumTitle"]', '[class*="album-title"]',
            '[class*="songTitle"]',  '[class*="song-title"]',
            '[class*="releaseTitle"]', '[class*="release-title"]',
            '[data-testid*="title"]',
        ];
        specific.forEach(sel => {
            document.querySelectorAll(sel).forEach(el => {
                const t = (el.innerText || el.textContent || '').trim();
                if (t.length > 1 && t.length < 200) found.add(t.toLowerCase());
            });
        });
        // Broader fallback
        document.querySelectorAll('td, h1, h2, h3, h4').forEach(el => {
            const t = (el.innerText || '').trim();
            if (t.length > 1 && t.length < 200) found.add(t.toLowerCase());
        });
        return Array.from(found);
    }""")

    # Normalize: also add each line from multi-line entries so bare titles are detected.
    # e.g. "l'ascension de k.ai\n\nkabi" → also adds "l'ascension de k.ai"
    scraped = set()
    for t in titles:
        scraped.add(t)
        for line in t.split("\n"):
            line = line.strip()
            if line:
                scraped.add(line)
    logger.info(f"  Scraped {len(scraped)} title strings from mymusic page")

    cache = load_uploaded_cache()
    logger.info(f"  Local cache: {len(cache)} entries")

    result = scraped | cache
    logger.info(f"  Combined: {len(result)} entries")
    return result


def is_duplicate(title: str, uploaded: set) -> bool:
    """Exact-match check — avoids false positives like 'Rise of K.ai' blocking
    'Rise of K.ai (Arabic Cover)'."""
    return title.lower().strip() in uploaded

# ─── AUTO SUBMIT ──────────────────────────────────────────────────────────────

def dismiss_dialogs(page):
    """Dismiss any modal validation dialogs (OK / Got it / Continue anyway)."""
    for btn_text in ["OK", "Got it", "Continue anyway", "Close", "Dismiss"]:
        try:
            # get_by_role is the most reliable way to find buttons by name
            loc = page.get_by_role("button", name=btn_text, exact=True).first
            loc.wait_for(state="visible", timeout=3000)
            logger.info(f"  Dismissing dialog: {btn_text!r}")
            loc.click(timeout=5000)
            time.sleep(1)
            return True
        except Exception:
            pass
    return False


def click_through_submit(page):
    """
    After the form is fully filled:
      1. Click "Continue" to reach the "You're almost there!" preview page.
      2. Dismiss any validation dialogs (duplicate title warnings, etc.).
      3. Click the final upload/submit button below the preview card.
    Returns True if we detect a success/redirect, False if stuck.
    """
    for attempt in range(10):
        time.sleep(3)
        current_url = page.url

        # Success indicators
        if any(kw in current_url for kw in
               ("mymusic", "success", "thankyou", "thank-you", "dashboard", "/new#")):
            logger.info(f"  Submission confirmed — at {current_url!r}")
            return True

        # Mixea mastering upsell page — select "Use my originals" ($0) then click Done
        if "mixea" in current_url:
            logger.info(f"  On Mixea upsell page — selecting 'Use my originals' ...")
            # Step 1: click the free "Use my originals" option if visible
            for free_text in ["Use my originals", "Use Originals", "No mastering",
                              "No thanks", "Skip", "Decline", "Not now"]:
                try:
                    loc = page.get_by_role("button", name=free_text, exact=False).first
                    loc.wait_for(state="visible", timeout=3000)
                    logger.info(f"  Clicking Mixea option: {free_text!r}")
                    loc.click(timeout=5000)
                    time.sleep(2)
                    break
                except Exception:
                    pass
            # Step 2: click Done to finalise
            for done_text in ["Done", "Finish", "Submit", "Continue", "Next"]:
                try:
                    loc = page.get_by_role("button", name=done_text, exact=True).first
                    loc.wait_for(state="visible", timeout=5000)
                    logger.info(f"  Clicking Mixea Done: {done_text!r}")
                    loc.click(timeout=5000)
                    time.sleep(3)
                    break
                except Exception:
                    pass
            else:
                logger.info("  Mixea Done button not found — treating as submitted")
                return True
            continue

        # Step A: dismiss any blocking dialogs first (higher priority than submit buttons)
        if dismiss_dialogs(page):
            continue  # re-check after dismissal

        # Step B: scroll to bottom so hidden buttons become visible
        try:
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        except Exception:
            pass  # page may be navigating; re-check URL on next iteration
        time.sleep(0.5)

        # Step C: try submit/continue buttons in order
        # "Continue" shows the preview; the button AFTER preview submits the release
        clicked = False
        for text in ["Upload Your Music", "Upload", "Done", "Submit Release",
                     "Submit", "Finish", "Try again", "Continue", "Next", "Upload Now"]:
            try:
                loc = page.get_by_role("button", name=text, exact=True).first
                loc.wait_for(state="visible", timeout=2000)
                logger.info(f"  Clicking: {text!r} (attempt {attempt+1})")
                loc.click(timeout=5000)
                time.sleep(2)
                clicked = True
                break
            except Exception:
                pass

        if not clicked:
            # Last resort: take screenshot and report stuck
            page.screenshot(path=f"stuck_attempt_{attempt}.png")
            logger.info(f"  No button found at attempt {attempt+1}. URL: {current_url!r}")
            if attempt >= 3:
                return False

    return False

# ─── FILL ONE RELEASE FORM ────────────────────────────────────────────────────

def fill_release_form(page, release_info, song_title, language, audio_path, artist, artwork_path):
    """
    Navigate to /new and fill the entire form for one single song.
    Returns True if form was filled successfully.
    """
    tomorrow = (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%d")

    # Use translated title for album + track; fall back to original if not in map
    display_title = TITLE_MAP.get(language, song_title)
    logger.info(f"  Display title: {display_title!r}")

    logger.info(f"Navigating to {UPLOAD_URL} ...")
    page.goto(UPLOAD_URL, wait_until="load", timeout=45000)
    time.sleep(2)

    # ── Song count: always 1 (a single) ──────────────────────────────────────
    if not try_select(page,
            ["#howManySongsOnThisAlbum", "select[name='howmanysongs']"],
            "1 song (a single)", "Song count"):
        page.evaluate("""() => {
            const sel = document.querySelector('#howManySongsOnThisAlbum, select[name="howmanysongs"]');
            if (sel && sel.options.length > 1) {
                sel.selectedIndex = 1;
                sel.dispatchEvent(new Event('change', {bubbles: true}));
            }
        }""")
    time.sleep(2)

    # ── Global fields ─────────────────────────────────────────────────────────
    if artist:
        try_fill(page, ["#artistName", "input[name='bandname']"], artist, "Artist Name")

    # Wait for DistroKid's artist AJAX lookup to settle, then fill album title.
    # fill() fails on this React-controlled input; click + select-all + type is reliable.
    # Also overrides any pre-populated value from a previous upload.
    # Album title is CSS-hidden until the artist AJAX settles; use JS setter directly.
    # For singles DistroKid also accepts the track title as the album title, so this
    # is best-effort — failure is non-fatal.
    _album_set = page.evaluate(f"""() => {{
        const sel = '#albumTitleInput, input[name="albumtitle"]';
        const el = document.querySelector(sel);
        if (!el) return false;
        const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
        setter.call(el, {json.dumps(display_title)});
        ['input', 'change', 'blur'].forEach(ev => el.dispatchEvent(new Event(ev, {{bubbles:true}})));
        return el.value;
    }}""")
    if _album_set:
        logger.info(f"  ✓ Album Title set via JS: {_album_set!r}")
    else:
        logger.warning("  Album Title field not found in DOM — DistroKid will use track title")

    genre = str(release_info.get("* Primary Genre", release_info.get("Primary Genre", "Pop")))
    if genre and genre != "nan":
        try_select(page, ["#genrePrimary", "select[name='genre1']"], genre, "Primary Genre")
        time.sleep(1)

    # Language comes from filename detection, not spreadsheet
    # Use JS value setter as fallback — handles Tamil and other options beyond the first 20
    if not try_select(page, ["#language", "select[name='language']"], language, "Language"):
        result = page.evaluate(f"""() => {{
            const sel = document.querySelector('#language, select[name="language"]');
            if (!sel) return null;
            const opt = Array.from(sel.options).find(o =>
                o.text.trim() === '{language}' || o.text.includes('{language}'));
            if (!opt) return null;
            const setter = Object.getOwnPropertyDescriptor(
                window.HTMLSelectElement.prototype, 'value').set;
            setter.call(sel, opt.value);
            ['input', 'change'].forEach(ev => sel.dispatchEvent(new Event(ev, {{bubbles: true}})));
            return opt.text;
        }}""")
        if result:
            logger.info(f"  Language set via JS fallback: {result!r}")
        else:
            logger.warning(f"  Language not found in select: {language!r}")

    # Release date: tomorrow (guaranteed valid)
    raw_date = release_info.get("* Release Date (yyyy-mm-dd)",
               release_info.get("Release Date (yyyy-mm-dd)", ""))
    if raw_date and str(raw_date) != "nan":
        d = raw_date.strftime("%Y-%m-%d") if isinstance(raw_date, datetime) else str(raw_date)[:10]
        date_str = d if d >= tomorrow else tomorrow
    else:
        date_str = tomorrow

    filled = page.evaluate(f"""() => {{
        const input = document.querySelector(
            '#release-date-dp, input[name="releaseDate"], #rp-release-date-dp, input[name="rpReleaseDateInput"]');
        if (!input) return null;
        const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
        setter.call(input, '{date_str}');
        ['input', 'change', 'blur'].forEach(ev =>
            input.dispatchEvent(new Event(ev, {{bubbles: true}})));
        return input.value;
    }}""")
    logger.info(f"  Release Date: {filled!r}" if filled else "  Release Date field not found")

    # Artwork
    if artwork_path:
        try:
            page.set_input_files("#artwork", artwork_path)
            logger.info(f"  ✓ Artwork: {os.path.basename(artwork_path)!r}")
        except Exception as e:
            logger.warning(f"  ✗ Artwork failed: {e}")

    # ── Track 1 (always n=1 since we upload singles) ──────────────────────────
    n = 1
    logger.info(f"--- Track: {song_title!r} ({language}) ---")

    try_fill(page,
        [f"input[placeholder='Track {n} title']"],
        display_title, "Track Title")

    # Not explicit
    try_check(page, f"#js-not-explicit-radio-button-{n}", True, "Not Explicit")

    # Songwriter
    songwriter = str(release_info.get("🟢 Songwriter(s)",
                     release_info.get("Songwriter(s)", "Kabi Tharma")))
    if songwriter and songwriter != "nan":
        parts = songwriter.strip().split(" ", 1)
        try_fill(page, [f"input[name='songwriter_real_name_first{n}']"],
                 parts[0], "Songwriter First")
        if len(parts) > 1:
            try_fill(page, [f"input[name='songwriter_real_name_last{n}']"],
                     parts[1], "Songwriter Last")

    # ── Expand Apple credits section ──────────────────────────────────────────
    for expand_text in ["text=Add credits", "text=Apple Music credits",
                        "text=Track credits", "text=Credits"]:
        try:
            btn = page.query_selector(expand_text)
            if btn and btn.is_visible():
                btn.click()
                time.sleep(0.5)
                logger.info(f"  ✓ Expanded credits via: {expand_text!r}")
                break
        except Exception:
            pass
    time.sleep(0.3)

    # ── Performer name + role ─────────────────────────────────────────────────
    perf_el = page.query_selector(f"#track-{n}-performer-1-name")
    if perf_el:
        try:
            perf_el.scroll_into_view_if_needed(timeout=5000)
            perf_el.click(timeout=5000)
            perf_el.press("Control+a")
            page.keyboard.type(artist)
            perf_el.press("Tab")
            logger.info(f"  Performer: {artist!r}")
        except Exception as e:
            logger.warning(f"  Performer name failed: {e}")
    page.evaluate(f"""() => {{
        const sel = document.querySelector('#track-{n}-performer-1-role');
        if (!sel) return;
        const setter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;
        setter.call(sel, 'Background vocals');
        ['input', 'change'].forEach(ev => sel.dispatchEvent(new Event(ev, {{bubbles: true}})));
    }}""")
    logger.info("  Performer role: Background vocals")

    # ── Producer name + role ──────────────────────────────────────────────────
    prod_el = page.query_selector(f"#track-{n}-producer-1-name")
    if prod_el:
        try:
            prod_el.scroll_into_view_if_needed(timeout=5000)
            prod_el.click(timeout=5000)
            prod_el.press("Control+a")
            page.keyboard.type(artist)
            prod_el.press("Tab")
            logger.info(f"  Producer: {artist!r}")
        except Exception as e:
            logger.warning(f"  Producer name failed: {e}")
    page.evaluate(f"""() => {{
        const sel = document.querySelector('#track-{n}-producer-1-role');
        if (!sel) return;
        const setter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;
        setter.call(sel, 'Executive producer');
        ['input', 'change'].forEach(ev => sel.dispatchEvent(new Event(ev, {{bubbles: true}})));
    }}""")
    logger.info("  Producer role: Executive producer")

    # ── Audio file ────────────────────────────────────────────────────────────
    if audio_path:
        try:
            page.set_input_files(f"#js-track-upload-{n}", audio_path)
            logger.info(f"  ✓ Audio: {os.path.basename(audio_path)!r}")
        except Exception as e:
            logger.warning(f"  ✗ Audio upload failed: {e}")
            return False

    # Wait for S3 upload to finish: poll until progress text disappears or done button activates
    logger.info("  Waiting for audio upload to complete ...")
    deadline = time.time() + 180  # max 3 minutes
    while time.time() < deadline:
        uploading = page.evaluate("""() => {
            const prog = document.querySelector('[class*="progress"], [class*="uploading"], [class*="upload-status"]');
            if (prog && prog.innerText && /upload/i.test(prog.innerText)) return true;
            const btn = document.querySelector('#doneButton');
            if (btn && btn.disabled) return true;
            return false;
        }""")
        if not uploading:
            break
        time.sleep(3)
    logger.info("  Audio upload ready.")

    # ── Mandatory checkboxes ──────────────────────────────────────────────────
    logger.info("  Checking mandatory checkboxes ...")
    for cb in page.query_selector_all("input.areyousure"):
        try:
            if not cb.is_checked():
                try:
                    cb.scroll_into_view_if_needed(timeout=5000)
                except Exception:
                    pass
                cb.check(timeout=5000)
        except Exception:
            pass
    for cb_id in ["areyousurerecorded", "areyousureotherartist", "areyousuretandc",
                  "areyousureyoutube", "areyousurepromoservices"]:
        try:
            el = page.query_selector(f"#{cb_id}")
            if el and not el.is_checked():
                try:
                    el.scroll_into_view_if_needed(timeout=5000)
                except Exception:
                    pass
                el.check(timeout=5000)
        except Exception:
            pass

    page.screenshot(path=f"upload_{song_title[:30].replace(' ', '_')}.png")
    return True

# ─── MAIN UPLOAD ──────────────────────────────────────────────────────────────

def run_upload():
    # Parse base metadata from spreadsheet
    try:
        release_info = parse_metadata(TEMPLATE_FILE)
    except FileNotFoundError:
        logger.error(f"Template not found: {TEMPLATE_FILE}")
        sys.exit(1)

    artist = str(release_info.get("* Artist / Band Name",
                 release_info.get("Artist / Band Name", "Kabi")))
    logger.info(f"Artist: {artist!r}")

    # Scan Songs/ folder
    logger.info("\nScanning Songs/ folder ...")
    songs = scan_songs_folder()
    if not songs:
        logger.error("No audio files found in Songs/ folder.")
        sys.exit(1)
    logger.info(f"Found {len(songs)} audio file(s)")

    artwork_path = find_artwork()
    logger.info(f"Artwork: {artwork_path!r}")

    with sync_playwright() as p:
        browser, context = ensure_auth(p)
        page = context.new_page()

        # Check mymusic for already-uploaded titles
        uploaded = get_uploaded_titles(page)

        # Filter out duplicates
        to_upload = []
        for title, language, audio_path in songs:
            if is_duplicate(title, uploaded):
                logger.warning(f"  SKIP: {title!r} — already found in mymusic")
            else:
                to_upload.append((title, language, audio_path))
                logger.info(f"  QUEUE: {title!r} ({language})")

        if not to_upload:
            logger.info("All songs already uploaded. Nothing to do.")
            browser.close()
            return

        logger.info(f"\nUploading {len(to_upload)} song(s) ...\n")

        for i, (title, language, audio_path) in enumerate(to_upload):
            logger.info(f"\n{'='*60}")
            logger.info(f"[{i+1}/{len(to_upload)}] {title!r} — Language: {language}")
            logger.info(f"{'='*60}")

            success = fill_release_form(
                page, release_info, title, language, audio_path, artist, artwork_path
            )

            if not success:
                logger.error(f"  Form fill failed for {title!r} — skipping")
                continue

            # Auto-submit: click through all post-fill buttons
            logger.info("  Submitting ...")
            submitted = click_through_submit(page)
            if submitted:
                logger.info(f"  ✓ {title!r} submitted successfully")
                uploaded.add(title.lower().strip())
                save_uploaded_cache(uploaded)
            else:
                logger.warning(f"  Form for {title!r} may not have submitted cleanly")
                # Take screenshot for manual review
                page.screenshot(path=f"review_{title[:30].replace(' ', '_')}.png")
                logger.info(f"  Screenshot saved for review")

            time.sleep(2)  # brief pause between uploads

        browser.close()
        logger.info("\nAll done.")

# ─── ENTRY POINT ──────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser(description="DistroKid Upload Bot")
    ap.add_argument("--discover", action="store_true",
                    help="Dump all form selectors to selectors.json")
    args = ap.parse_args()

    if args.discover:
        run_discover()
    else:
        run_upload()

if __name__ == "__main__":
    main()
