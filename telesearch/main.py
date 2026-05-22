"""
TG Search — Standalone Telegram Group/Channel Explorer
FastAPI + Telethon + SQLite. No new bots, no join/leave, no ban risk.
Deployed at /telesearch via nginx reverse proxy.
"""
import os
import sys
import json
import sqlite3
import secrets
import asyncio
from contextlib import asynccontextmanager
from typing import Optional

from fastapi import FastAPI, Request, Depends, HTTPException, status
from fastapi.responses import HTMLResponse, JSONResponse, FileResponse
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import uvicorn

from telethon import TelegramClient
from telethon.tl.functions.contacts import SearchRequest as TGSearchRequest
from telethon.tl.functions.messages import SearchGlobalRequest
from telethon.tl.types import Channel, Chat, User as TGUser, InputPeerEmpty, InputMessagesFilterEmpty

# ── Config ────────────────────────────────────────────────────────────
DB_PATH = os.path.join(os.path.dirname(__file__), "telesearch.db")
TG_API_ID = int(os.getenv("TG_API_ID", "31671257"))
TG_API_HASH = os.getenv("TG_API_HASH", "ec880ffa330d0c826b77a7ffc3a16255")
TG_SESSION = os.path.join(os.path.dirname(__file__), "session")
APP_USER = os.getenv("APP_USER", "admin")
APP_PASS = os.getenv("APP_PASS", "")
_tg_client: TelegramClient | None = None


# ── Database ──────────────────────────────────────────────────────────
def init_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.executescript("""
        PRAGMA journal_mode=WAL;
        PRAGMA synchronous=NORMAL;

        CREATE TABLE IF NOT EXISTS tg_index (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            telegram_id   BIGINT  UNIQUE NOT NULL,
            title         TEXT    NOT NULL,
            username      TEXT,
            type          TEXT    NOT NULL,
            members       INTEGER,
            description   TEXT    DEFAULT '',
            category      TEXT    DEFAULT 'uncategorized',
            tags          TEXT    DEFAULT '[]',
            notes         TEXT    DEFAULT '',
            is_active     INTEGER DEFAULT 1,
            verified      INTEGER DEFAULT 0,
            scam          INTEGER DEFAULT 0,
            last_checked  DATETIME,
            created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
        );
        CREATE INDEX IF NOT EXISTS idx_tg_index_category ON tg_index(category);
        CREATE INDEX IF NOT EXISTS idx_tg_index_type     ON tg_index(type);
        CREATE INDEX IF NOT EXISTS idx_tg_index_active   ON tg_index(is_active);
        CREATE INDEX IF NOT EXISTS idx_tg_index_username ON tg_index(username);

        CREATE TABLE IF NOT EXISTS flagged_groups (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            telegram_id     BIGINT  UNIQUE NOT NULL,
            title           TEXT    NOT NULL,
            username        TEXT,
            members         INTEGER,
            type            TEXT    NOT NULL DEFAULT 'group',
            keywords_matched TEXT    DEFAULT '[]',
            flag_category   TEXT    DEFAULT 'other',
            scan_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
            reviewed        INTEGER DEFAULT 0,
            notes           TEXT    DEFAULT ''
        );
        CREATE INDEX IF NOT EXISTS idx_flagged_reviewed ON flagged_groups(reviewed);
        CREATE INDEX IF NOT EXISTS idx_flagged_scan_date ON flagged_groups(scan_date);
        CREATE INDEX IF NOT EXISTS idx_flagged_members ON flagged_groups(members);
        CREATE INDEX IF NOT EXISTS idx_flagged_category ON flagged_groups(flag_category);
    """)
    conn.commit()
    conn.close()


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


# ── Lifespan ──────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    global _tg_client
    init_db()
    _tg_client = TelegramClient(TG_SESSION, TG_API_ID, TG_API_HASH)
    await _tg_client.connect()
    yield
    if _tg_client and _tg_client.is_connected():
        await _tg_client.disconnect()


app = FastAPI(lifespan=lifespan, docs_url=None, redoc_url=None)
_security = HTTPBasic()

# Static files: mount at /telesearch/static so URLs resolve correctly under the path prefix
app.mount("/static", StaticFiles(directory=os.path.join(os.path.dirname(__file__), "static")), name="static")


def require_auth(credentials: HTTPBasicCredentials = Depends(_security)) -> str:
    ok_user = secrets.compare_digest(credentials.username.encode(), APP_USER.encode())
    ok_pass = secrets.compare_digest(credentials.password.encode(), APP_PASS.encode())
    if not (ok_user and ok_pass):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Unauthorized",
            headers={"WWW-Authenticate": "Basic realm='TG Search'"},
        )


# ── Pydantic models ───────────────────────────────────────────────────
class TGSearchBody(BaseModel):
    query: str
    type: str = "all"
    sort: str = "relevance"
    min_members: int = 0
    max_members: Optional[int] = None


class TGIndexItemCreate(BaseModel):
    telegram_id: int
    title: str
    username: Optional[str] = None
    type: str
    members: Optional[int] = None
    description: Optional[str] = ""
    category: Optional[str] = "uncategorized"
    tags: Optional[list[str]] = []
    notes: Optional[str] = ""
    verified: bool = False
    scam: bool = False


class TGIndexItemUpdate(BaseModel):
    category: Optional[str] = None
    tags: Optional[list[str]] = None
    notes: Optional[str] = None


class TGIndexCheckBody(BaseModel):
    ids: Optional[list[int]] = None


class FlaggedGroupReview(BaseModel):
    reviewed: bool
    notes: Optional[str] = None


class ScannerRunBody(BaseModel):
    keywords: Optional[list[str]] = None
    max_members: int = 100
    max_results_per_keyword: int = 100


# ── Helpers ─────────────────────────────────────────────────────────
def _member_count(entity) -> int:
    participants = getattr(entity, "participants_count", None)
    if participants is not None:
        return participants
    return 0


def _query_variations(query: str) -> list[str]:
    words = query.lower().split()
    if not words:
        return []
    variants = []
    def add(q):
        if q not in variants:
            variants.append(q)
    add(" ".join(words))
    add("".join(words))
    add("_".join(words))
    add("-".join(words))
    for i in range(1, len(words)):
        add(" ".join(words[: i + 1]))
    return variants


# ── Telegram Search ───────────────────────────────────────────────────
@app.post("/api/tg-search")
async def tg_search(body: TGSearchBody, _: str = Depends(require_auth)):
    global _tg_client
    if _tg_client is None or not _tg_client.is_connected():
        raise HTTPException(status_code=503, detail="Telegram client not connected")
    if not await _tg_client.is_user_authorized():
        raise HTTPException(status_code=401, detail="Telegram session not authenticated. Run 'python create_session_local.py' locally, log in with your phone number, then upload the .session file to the server and restart telesearch.")

    seen_ids: set[int] = set()
    channels: list = []
    groups:   list = []
    bots:     list = []

    # Use contacts.SearchRequest to search groups/channels by name
    # (messages.SearchGlobalRequest searches for messages, not group names)
    variants = _query_variations(body.query)

    for i, variant in enumerate(variants):
        try:
            result = await asyncio.wait_for(
                _tg_client(TGSearchRequest(q=variant, limit=100)),
                timeout=20,
            )
            for entity in result.chats + result.users:
                if entity.id in seen_ids:
                    continue
                seen_ids.add(entity.id)

                if isinstance(entity, Channel):
                    if entity.broadcast:
                        channels.append(entity)
                    else:
                        groups.append(entity)
                elif isinstance(entity, Chat):
                    groups.append(entity)
                elif isinstance(entity, TGUser) and entity.bot:
                    bots.append(entity)
        except asyncio.TimeoutError:
            continue
        except Exception as exc:
            if "flood" in str(exc).lower() or "FLOOD_WAIT" in str(exc):
                import re
                wait_match = re.search(r'FLOOD_WAIT_(\d+)', str(exc))
                wait_time = int(wait_match.group(1)) if wait_match else 60
                await asyncio.sleep(min(wait_time, 300))
            continue

        if i < len(variants) - 1:
            await asyncio.sleep(0.4)

    # Member range filtering
    if body.min_members > 0:
        channels = [c for c in channels if _member_count(c) >= body.min_members]
        groups   = [g for g in groups   if _member_count(g) >= body.min_members]
    if body.max_members is not None:
        channels = [c for c in channels if _member_count(c) <= body.max_members]
        groups   = [g for g in groups   if _member_count(g) <= body.max_members]

    # Sorting
    if body.sort == "members":
        channels.sort(key=_member_count, reverse=True)
        groups.sort(key=_member_count, reverse=True)
        bots.sort(key=lambda b: b.first_name or "", reverse=False)
    elif body.sort == "small_first":
        # Reverse sort - smallest groups first (for privacy scanning)
        channels.sort(key=_member_count, reverse=False)
        groups.sort(key=_member_count, reverse=False)
        bots.sort(key=lambda b: b.first_name or "", reverse=False)

    def _serialize(entity, kind: str) -> dict:
        uname    = getattr(entity, "username", None) or None
        members  = _member_count(entity)
        verified = bool(getattr(entity, "verified", False))
        scam     = bool(getattr(entity, "scam",     False))
        if kind == "bot":
            title = (getattr(entity, "first_name", "") or "") + (" " + (getattr(entity, "last_name", "") or "")).rstrip()
        else:
            title = getattr(entity, "title", "") or ""
        return {
            "id":       entity.id,
            "title":    title,
            "username": uname,
            "link":     f"https://t.me/{uname}" if uname else None,
            "members":  members if members else None,
            "type":     kind,
            "verified": verified,
            "scam":     scam,
        }

    result_list: list[dict] = []
    filter_type = body.type

    if filter_type in ("all", "channel"):
        for e in channels:
            result_list.append(_serialize(e, "channel"))
    if filter_type in ("all", "group"):
        for e in groups:
            result_list.append(_serialize(e, "group"))
    if filter_type in ("all", "bot"):
        for e in bots:
            result_list.append(_serialize(e, "bot"))

    # When showing all and sorting by members, interleave channels + groups together
    if filter_type == "all" and body.sort in ("members", "small_first"):
        combined = [r for r in result_list if r["type"] in ("channel", "group")]
        bot_items = [r for r in result_list if r["type"] == "bot"]
        reverse_order = body.sort == "members"  # True for large-first, False for small-first
        combined.sort(key=lambda r: r["members"] or 0, reverse=reverse_order)
        result_list = combined + bot_items

    return result_list


# ── Index CRUD ────────────────────────────────────────────────────────
@app.get("/api/tg-index")
def list_index(
    category: Optional[str] = None,
    type: Optional[str] = None,
    active_only: bool = False,
    search: Optional[str] = None,
    _: str = Depends(require_auth),
):
    conn = get_db()
    conditions = ["1=1"]
    params: list = []
    if category:
        conditions.append("category = ?")
        params.append(category)
    if type:
        conditions.append("type = ?")
        params.append(type)
    if active_only:
        conditions.append("is_active = 1")
    if search:
        conditions.append("(title LIKE ? OR username LIKE ? OR tags LIKE ?)")
        like = f"%{search}%"
        params.extend([like, like, like])

    where_clause = " AND ".join(conditions)
    rows = conn.execute(
        f"""
        SELECT id, telegram_id, title, username, type, members, description,
               category, tags, notes, is_active, verified, scam, last_checked, created_at
        FROM tg_index
        WHERE {where_clause}
        ORDER BY created_at DESC
        """,
        params,
    ).fetchall()
    conn.close()
    result = []
    for r in rows:
        d = dict(r)
        d["tags"] = json.loads(d["tags"] or "[]")
        d["is_active"] = bool(d["is_active"])
        d["verified"] = bool(d["verified"])
        d["scam"] = bool(d["scam"])
        result.append(d)
    return result


@app.post("/api/tg-index")
def add_to_index(body: TGIndexItemCreate, _: str = Depends(require_auth)):
    conn = get_db()
    try:
        conn.execute(
            """
            INSERT INTO tg_index (telegram_id, title, username, type, members,
                                  description, category, tags, notes, verified, scam, last_checked)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
            """,
            (
                body.telegram_id,
                body.title,
                body.username,
                body.type,
                body.members,
                body.description or "",
                body.category or "uncategorized",
                json.dumps(body.tags or []),
                body.notes or "",
                int(body.verified),
                int(body.scam),
            ),
        )
        conn.commit()
        row = conn.execute(
            "SELECT * FROM tg_index WHERE telegram_id = ?", (body.telegram_id,)
        ).fetchone()
        conn.close()
        if not row:
            raise HTTPException(status_code=500, detail="Failed to retrieve inserted row")
        d = dict(row)
        d["tags"] = json.loads(d["tags"] or "[]")
        d["is_active"] = bool(d["is_active"])
        return d
    except sqlite3.IntegrityError:
        conn.close()
        raise HTTPException(status_code=409, detail="Item already in index")


@app.delete("/api/tg-index/{item_id}")
def remove_from_index(item_id: int, _: str = Depends(require_auth)):
    conn = get_db()
    cur = conn.execute("DELETE FROM tg_index WHERE id = ?", (item_id,))
    conn.commit()
    conn.close()
    if cur.rowcount == 0:
        raise HTTPException(status_code=404, detail="Item not found")
    return {"ok": True}


@app.put("/api/tg-index/{item_id}")
def update_index_item(item_id: int, body: TGIndexItemUpdate, _: str = Depends(require_auth)):
    conn = get_db()
    row = conn.execute("SELECT id FROM tg_index WHERE id = ?", (item_id,)).fetchone()
    if not row:
        conn.close()
        raise HTTPException(status_code=404, detail="Item not found")

    updates = []
    params = []
    if body.category is not None:
        updates.append("category = ?")
        params.append(body.category)
    if body.tags is not None:
        updates.append("tags = ?")
        params.append(json.dumps(body.tags))
    if body.notes is not None:
        updates.append("notes = ?")
        params.append(body.notes)

    if not updates:
        conn.close()
        raise HTTPException(status_code=400, detail="No fields to update")

    params.append(item_id)
    conn.execute(f"UPDATE tg_index SET {', '.join(updates)} WHERE id = ?", params)
    conn.commit()
    updated = conn.execute("SELECT * FROM tg_index WHERE id = ?", (item_id,)).fetchone()
    conn.close()
    d = dict(updated)
    d["tags"] = json.loads(d["tags"] or "[]")
    d["is_active"] = bool(d["is_active"])
    return d


@app.post("/api/tg-index/check")
async def check_index_status(body: TGIndexCheckBody = TGIndexCheckBody(), _: str = Depends(require_auth)):
    global _tg_client
    if _tg_client is None or not _tg_client.is_connected():
        raise HTTPException(status_code=503, detail="Telegram client not connected")

    conn = get_db()
    if body.ids:
        placeholders = ",".join("?" * len(body.ids))
        rows = conn.execute(
            f"SELECT id, telegram_id, username FROM tg_index WHERE id IN ({placeholders})",
            body.ids,
        ).fetchall()
    else:
        rows = conn.execute("SELECT id, telegram_id, username FROM tg_index").fetchall()
    conn.close()

    checked = 0
    changed = 0
    for row in rows:
        item_id, _tg_id, username = row
        is_active = False

        try:
            if username:
                entity = await asyncio.wait_for(_tg_client.get_entity(username), timeout=10)
                is_active = entity is not None
        except Exception:
            is_active = False

        conn = get_db()
        old = conn.execute("SELECT is_active FROM tg_index WHERE id = ?", (item_id,)).fetchone()
        old_active = bool(old[0]) if old else True
        conn.execute(
            "UPDATE tg_index SET is_active = ?, last_checked = datetime('now') WHERE id = ?",
            (int(is_active), item_id),
        )
        conn.commit()
        conn.close()

        checked += 1
        if is_active != old_active:
            changed += 1

        await asyncio.sleep(0.6)

    return {"checked": checked, "changed": changed}


@app.get("/api/tg-index/categories")
def list_categories(_: str = Depends(require_auth)):
    conn = get_db()
    rows = conn.execute("SELECT DISTINCT category FROM tg_index ORDER BY category").fetchall()
    conn.close()
    return [r[0] for r in rows if r[0]]


@app.get("/api/stats")
def get_stats(_: str = Depends(require_auth)):
    """Lightweight health-check endpoint for the guide."""
    conn = get_db()
    total = conn.execute("SELECT COUNT(*) FROM tg_index").fetchone()[0]
    active = conn.execute("SELECT COUNT(*) FROM tg_index WHERE is_active = 1").fetchone()[0]
    conn.close()
    return {"total_items": total, "active_items": active, "ok": True}


# ── Flagged Groups API ────────────────────────────────────────────────
@app.get("/api/flagged-groups")
def list_flagged_groups(
    reviewed: Optional[bool] = None,
    min_members: int = 0,
    max_members: Optional[int] = None,
    category: Optional[str] = None,
    _: str = Depends(require_auth),
):
    conn = get_db()
    conditions = ["1=1"]
    params: list = []
    if reviewed is not None:
        conditions.append("reviewed = ?")
        params.append(int(reviewed))
    if min_members > 0:
        conditions.append("members >= ?")
        params.append(min_members)
    if max_members is not None:
        conditions.append("members <= ?")
        params.append(max_members)
    if category:
        conditions.append("flag_category = ?")
        params.append(category)

    where_clause = " AND ".join(conditions)
    rows = conn.execute(
        f"""
        SELECT id, telegram_id, title, username, members, type,
               keywords_matched, flag_category, scan_date, reviewed, notes
        FROM flagged_groups
        WHERE {where_clause}
        ORDER BY scan_date DESC
        """,
        params,
    ).fetchall()
    conn.close()
    result = []
    for r in rows:
        d = dict(r)
        d["keywords_matched"] = json.loads(d["keywords_matched"] or "[]")
        d["reviewed"] = bool(d["reviewed"])
        result.append(d)
    return result


@app.put("/api/flagged-groups/{group_id}/review")
def review_flagged_group(group_id: int, body: FlaggedGroupReview, _: str = Depends(require_auth)):
    conn = get_db()
    row = conn.execute("SELECT id FROM flagged_groups WHERE id = ?", (group_id,)).fetchone()
    if not row:
        conn.close()
        raise HTTPException(status_code=404, detail="Flagged group not found")

    updates = []
    params = []
    updates.append("reviewed = ?")
    params.append(int(body.reviewed))
    if body.notes is not None:
        updates.append("notes = ?")
        params.append(body.notes)
    params.append(group_id)

    conn.execute(f"UPDATE flagged_groups SET {', '.join(updates)} WHERE id = ?", params)
    conn.commit()
    updated = conn.execute("SELECT * FROM flagged_groups WHERE id = ?", (group_id,)).fetchone()
    conn.close()
    d = dict(updated)
    d["keywords_matched"] = json.loads(d["keywords_matched"] or "[]")
    d["reviewed"] = bool(d["reviewed"])
    return d


@app.delete("/api/flagged-groups/{group_id}")
def delete_flagged_group(group_id: int, _: str = Depends(require_auth)):
    conn = get_db()
    cur = conn.execute("DELETE FROM flagged_groups WHERE id = ?", (group_id,))
    conn.commit()
    conn.close()
    if cur.rowcount == 0:
        raise HTTPException(status_code=404, detail="Flagged group not found")
    return {"ok": True}


@app.get("/api/flagged-groups/stats")
def flagged_groups_stats(_: str = Depends(require_auth)):
    conn = get_db()
    total = conn.execute("SELECT COUNT(*) FROM flagged_groups").fetchone()[0]
    reviewed = conn.execute("SELECT COUNT(*) FROM flagged_groups WHERE reviewed = 1").fetchone()[0]
    unreviewed = conn.execute("SELECT COUNT(*) FROM flagged_groups WHERE reviewed = 0").fetchone()[0]
    small = conn.execute("SELECT COUNT(*) FROM flagged_groups WHERE members <= 10").fetchone()[0]
    # Category breakdown
    cats = {}
    for row in conn.execute("SELECT flag_category, COUNT(*) as cnt FROM flagged_groups GROUP BY flag_category").fetchall():
        cats[row[0]] = row[1]
    conn.close()
    return {"total": total, "reviewed": reviewed, "unreviewed": unreviewed, "small_groups": small, "categories": cats}


# ── Scanner API ───────────────────────────────────────────────────────
# Categorized keyword -> flag_category mapping
PRIVACY_KEYWORDS = {
    # ── INTIMATE: Couple photos, private moments ──
    "Our Photos": "intimate", "Our Pics": "intimate", "Just Us": "intimate",
    "Eyes Only": "intimate", "Your Eyes Only": "intimate", "For Your Eyes": "intimate",
    "Between Us": "intimate", "Ours Only": "intimate", "No One Else": "intimate",
    "My Love": "intimate", "My Baby": "intimate", "My Girl": "intimate", "My Boy": "intimate",
    "Honeymoon": "intimate", "Anniversary": "intimate", "Date Night": "intimate",
    "Bedroom": "intimate", "Pillow Talk": "intimate", "Late Night": "intimate",
    "Naughty": "intimate", "Dirty": "intimate", "Wild Night": "intimate",
    "Couple Only": "intimate", "Couple Photos": "intimate", "Couple Album": "intimate",
    "Babe Photos": "intimate", "Babe Pics": "intimate", "Hot Photos": "intimate",
    "Spicy": "intimate", "Kinky": "intimate", "Behind Closed Doors": "intimate",
    "Secret Album": "intimate", "Hidden Folder": "intimate", "Private Moments": "intimate",
    "Private Album": "intimate", "Private Pics": "intimate", "Private Gallery": "intimate",
    "Our Private": "intimate", "Us Only": "intimate", "Only Us": "intimate",
    "Intimate": "intimate", "Personal Moments": "intimate", "Personal Photos": "intimate",
    "My Private": "intimate", "My Secret": "intimate", "My Hidden": "intimate",
    "Nudes": "intimate", "Nude": "intimate", "Topless": "intimate",
    "Sexting": "intimate", "Sext": "intimate", "Dick Pic": "intimate", "Dick Pics": "intimate",
    "Boobs": "intimate", "Ass Pics": "intimate", "Booty": "intimate",
    "Selfie Album": "intimate", "Selfies": "intimate", "Mirror Selfies": "intimate",
    "Shower": "intimate", "Bath": "intimate", "Undressed": "intimate",
    "Strip": "intimate", "Stripping": "intimate", "Explicit": "intimate",
    # Russian intimate
    "Наши фото": "intimate", "Только мы": "intimate", "Интим": "intimate",
    "Интимное": "intimate", "Интим фото": "intimate", "Только для тебя": "intimate",
    "Секретное": "intimate", "Приватное": "intimate", "Личное": "intimate",
    "Голые": "intimate", "Обнаженные": "intimate", "Эротика": "intimate",
    # Spanish intimate
    "Nuestras Fotos": "intimate", "Solo Nosotros": "intimate", "Intimo": "intimate",
    "Intimas": "intimate", "Fotos Privadas": "intimate", "Solo Para Ti": "intimate",
    "Desnudos": "intimate", "Desnudas": "intimate", "Erotico": "intimate",
    "Nuestro Secreto": "intimate", "Album Intimo": "intimate",
    # Portuguese intimate
    "Nossas Fotos": "intimate", "Só Nós": "intimate", "Intimo": "intimate",
    "Intimas": "intimate", "Fotos Privadas": "intimate", "Nudes": "intimate",
    "Pelados": "intimate", "Peladas": "intimate", "Erotico": "intimate",
    # Arabic intimate
    "صورنا": "intimate", "خاص": "intimate", "سري": "intimate",
    "صور خاصة": "intimate", "صور شخصية": "intimate", "صور عارية": "intimate",
    "صور جنسية": "intimate", "فقط نحن": "intimate",
    # Hindi intimate
    "हमारी फोटो": "intimate", "निजी": "intimate", "गुप्त": "intimate",
    "सिर्फ हम": "intimate", "निजी फोटो": "intimate", "अंतरंग": "intimate",
    # Persian intimate
    "عکس‌های ما": "intimate", "خصوصی": "intimate", "محرمانه": "intimate",
    "فقط ما": "intimate", "عکس خصوصی": "intimate", "عکس های شخصی": "intimate",
    # Indonesian intimate
    "Foto Kita": "intimate", "Pribadi": "intimate", "Rahasia": "intimate",
    "Hanya Kita": "intimate", "Foto Pribadi": "intimate", "Intim": "intimate",
    "Telanjang": "intimate", "Bugil": "intimate",
    # Turkish intimate
    "Fotoğraflarımız": "intimate", "Sadece Biz": "intimate", "Özel": "intimate",
    "Gizli": "intimate", "Özel Foto": "intimate", "Mahrem": "intimate",
    "Çıplak": "intimate", "Erotik": "intimate",

    # ── PERSONAL: Saved messages, private files, backups ──
    "Saved Messages": "personal", "My Saves": "personal", "Backup": "personal",
    "Backup 2024": "personal", "Backup 2025": "personal", "Backup 2026": "personal",
    "Personal": "personal", "Personal Album": "personal", "My Files": "personal",
    "Private": "personal", "Secret": "personal", "Do Not Share": "personal",
    "Private Do Not Forward": "personal", "My Storage": "personal",
    "Archive": "personal", "Archived": "personal", "Personal Archive": "personal",
    "My Backup": "personal", "Phone Backup": "personal", "Chat Backup": "personal",
    "My Documents": "personal", "My Data": "personal", "My Stuff": "personal",
    "For Me": "personal", "Mine Only": "personal", "Keep Safe": "personal",
    "Save For Later": "personal", "Bookmarks": "personal", "My Bookmarks": "personal",
    "Notes": "personal", "My Notes": "personal", "Important Files": "personal",
    "Personal Files": "personal", "Private Files": "personal", "Confidential": "personal",
    "Encrypted": "personal", "Vault": "personal", "My Vault": "personal",
    "Draft": "personal", "Drafts": "personal", "My Drafts": "personal",
    "Saved Links": "personal", "My Links": "personal", "Link Dump": "personal",
    # Russian personal
    "Сохраненные": "personal", "Личный архив": "personal", "Мои файлы": "personal",
    "Мои сохранения": "personal", "Бэкап": "personal", "Архив": "personal",
    "Мои заметки": "personal", "Для себя": "personal",
    # Spanish personal
    "Mensajes Guardados": "personal", "Archivos": "personal", "Album Personal": "personal",
    "Mis Archivos": "personal", "Para Mi": "personal", "Mis Notas": "personal",
    # Portuguese personal
    "Mensagens Salvas": "personal", "Cadangan": "personal", "Album Pessoal": "personal",
    "Meus Arquivos": "personal", "Para Mim": "personal",
    # Arabic personal
    "الرسائل المحفوظة": "personal", "الألبوم الشخصي": "personal", "ملفاتي": "personal",
    # Hindi personal
    "सेव किए गए": "personal", "बैकअप": "personal", "व्यक्तिगत": "personal",
    # Persian personal
    "ذخیره شده": "personal", "پیام‌ها": "personal", "آلبوم شخصی": "personal",
    # Indonesian personal
    "Pesan Tersimpan": "personal", "Album Pribadi": "personal", "File Saya": "personal",
    # Turkish personal
    "Kayıtlı Mesajlar": "personal", "Kişisel Albüm": "personal", "Dosyalarım": "personal",

    # ── FAMILY: Family-only groups accidentally public ──
    "Family Only": "family", "Family Photos": "family", "Family Pics": "family",
    "Family Group": "family", "Family Album": "family", "Family Chat": "family",
    "Fam Only": "family", "My Family": "family", "Our Family": "family",
    "Mom Dad": "family", "Mom And Dad": "family", "Parents Only": "family",
    "Kids Photos": "family", "Baby Photos": "family", "Baby Pics": "family",
    "Children Photos": "family", "Kid Pics": "family", "Family Moments": "family",
    "Family Memories": "family", "Family Private": "family", "Family Secret": "family",
    "Cousins": "family", "Siblings": "family", "Brothers": "family", "Sisters": "family",
    "Home Photos": "family", "Home Videos": "family", "House Pics": "family",
    "Vacation Photos": "family", "Trip Photos": "family", "Holiday Pics": "family",
    "Christmas Photos": "family", "Birthday Photos": "family", "Wedding Photos": "family",
    "Graduation Pics": "family", "School Photos": "family",
    # Arabic family
    "عائلتي فقط": "family", "صور العائلة": "family", "عائلتي": "family",
    # Hindi family
    "परिवार": "family", "परिवार फोटो": "family",
    # Russian family
    "Семья": "family", "Семейное": "family", "Семейные фото": "family",
    # Spanish family
    "Familia": "family", "Fotos Familiares": "family", "Solo Familia": "family",
    # Portuguese family
    "Familia": "family", "Fotos Familia": "family", "So Familia": "family",

    # ── MEMORIES: Year-based memory/backup groups ──
    "Memory 2020": "memories", "Memory 2021": "memories", "Memory 2022": "memories",
    "Memory 2023": "memories", "Memory 2024": "memories", "Memory 2025": "memories",
    "Memory 2026": "memories", "Memories 2024": "memories", "Memories 2025": "memories",
    "Memories 2026": "memories", "Photo Dump 2024": "memories", "Photo Dump 2025": "memories",
    "Photo Dump 2026": "memories", "Camera Roll": "memories", "Old Photos": "memories",
    "Throwback": "memories", "Flashback": "memories", "Nostalgia": "memories",
    "Good Times": "memories", "Best Moments": "memories", "Golden Days": "memories",
    "The Vault": "memories", "Time Capsule": "memories", "Keepsakes": "memories",
    "Treasured": "memories", "Precious Moments": "memories", "Unforgettable": "memories",

    # ── FINANCIAL: Financial/identity data ──
    "Bank": "financial", "Banking": "financial", "Credit Card": "financial",
    "Passwords": "financial", "Password": "financial", "Login Info": "financial",
    "Accounts": "financial", "Account Info": "financial", "Financial": "financial",
    "Tax": "financial", "Taxes": "financial", "Invoice": "financial",
    "Receipts": "financial", "Pay Stub": "financial", "Salary": "financial",
    "ID Card": "financial", "Passport": "financial", "Driver License": "financial",
    "SSN": "financial", "Social Security": "financial", "Insurance": "financial",
    "Medical Records": "financial", "Health Records": "financial", "Prescriptions": "financial",
    "Wallet": "financial", "Crypto Wallet": "financial", "Private Keys": "financial",
    "Seed Phrase": "financial", "Recovery Phrase": "financial", "2FA": "financial",
    "API Keys": "financial", "Tokens": "financial", "Credentials": "financial",

    # ── VULNERABLE LANGUAGES: Low digital literacy regions ──
    # Urdu (Pakistan, India - 230M speakers)
    "محفوظ پیغامات": "personal", "نجی": "intimate", "خفیہ": "intimate",
    "ہماری تصاویر": "intimate", "صرف ہم": "intimate", "نجی تصاویر": "intimate",
    "خاندان": "family", "بیک اپ": "personal", "ذاتی": "personal",
    "برہنہ": "intimate", "فحش": "intimate", "جنسی": "intimate",
    # Bengali (Bangladesh, India - 270M speakers)
    "সংরক্ষিত বার্তা": "personal", "ব্যক্তিগত": "intimate", "গোপন": "intimate",
    "আমাদের ছবি": "intimate", "শুধু আমরা": "intimate", "পরিবার": "family",
    "ব্যাকআপ": "personal", "নগ্ন": "intimate", "আপত্তিকর": "intimate",
    # Nepali (Nepal - 30M)
    "सुरक्षित सन्देश": "personal", "व्यक्तिगत": "intimate", "गोप्य": "intimate",
    "हाम्रो फोटो": "intimate", "मात्र हामी": "intimate", "परिवार": "family",
    "ब्याकअप": "personal", "नाङ्गो": "intimate", "यौन": "intimate",
    # Sinhala (Sri Lanka - 17M)
    "සුරකින ලද පණිවිඩ": "personal", "පුද්ගලික": "intimate", "රහසිගත": "intimate",
    "අපේ ඡායාරූප": "intimate", "අපි විතරයි": "intimate", "පවුල": "family",
    # Tagalog/Filipino (Philippines - 100M)
    "Naka-save na Mensahe": "personal", "Pribado": "intimate", "Lihim": "intimate",
    "Ating Mga Larawan": "intimate", "Tayo Lang": "intimate", "Pamilya": "family",
    "Hubad": "intimate", "Malaswa": "intimate", "Sekswal": "intimate",
    # Vietnamese (Vietnam - 85M)
    "Tin Nhắn Đã Lưu": "personal", "Riêng Tư": "intimate", "Bí Mật": "intimate",
    "Ảnh Của Chúng Tôi": "intimate", "Chỉ Chúng Tôi": "intimate", "Gia Đình": "family",
    "Khỏa Thân": "intimate", "Nhạy Cảm": "intimate", "Sao Lưu": "personal",
    # Thai (Thailand - 70M)
    "ข้อความที่บันทึกไว้": "personal", "ส่วนตัว": "intimate", "ความลับ": "intimate",
    "รูปของเรา": "intimate", "แค่เรา": "intimate", "ครอบครัว": "family",
    "เปลือย": "intimate", "ลับเฉพาะ": "intimate", "สำรอง": "personal",
    # Burmese/Myanmar (Myanmar - 38M)
    "သိမ်းထားသောစာများ": "personal", "ကိုယ်ရေး": "intimate", "လျှို့ဝှက်": "intimate",
    "ငါတို့ဓာတ်ပုံ": "intimate", "ငါတို့ပဲ": "intimate", "မိသားစု": "family",
    # Khmer/Cambodian (Cambodia - 16M)
    "សារដែលបានរក្សាទុក": "personal", "ឯកជន": "intimate", "សម្ងាត់": "intimate",
    "រូបថតរបស់យើង": "intimate", "តែយើង": "intimate", "គ្រួសារ": "family",
    # Swahili (East Africa - 200M)
    "Ujumbe Uliohifadhiwa": "personal", "Binafsi": "intimate", "Siri": "intimate",
    "Picha Zetu": "intimate", "Sisi Tu": "intimate", "Familia": "family",
    "Uchi": "intimate", "Ngono": "intimate", "Hifadhi": "personal",
    # Amharic (Ethiopia - 60M)
    "የተቀመጡ መልዕክቶች": "personal", "የግል": "intimate", "ሚስጥር": "intimate",
    "የኛ ፎቶዎች": "intimate", "እኛ ብቻ": "intimate", "ቤተሰብ": "family",
    # Hausa (West Africa - 80M)
    "Ajiye Sakonni": "personal", "Na Sirri": "intimate", "Asiri": "intimate",
    "Hotunan Mu": "intimate", "Mu Kadai": "intimate", "Iyali": "family",
    # Yoruba (Nigeria - 45M)
    "Awọn Ifiranṣẹ Ti Fipamọ": "personal", "Adani": "intimate", "Asiri": "intimate",
    "Awọn Fọto Wa": "intimate", "Awa Nikan": "intimate", "Ebi": "family",
    # Zulu (South Africa - 30M)
    "Imilayezo Egciniwe": "personal", "Okuyimfihlo": "intimate", "Imfihlo": "intimate",
    "Izithombe Zethu": "intimate", "Thina Kuphela": "intimate", "Umndeni": "family",
    # Ukrainian (Ukraine - 40M)
    "Збережені повідомлення": "personal", "Особисте": "intimate", "Секрет": "intimate",
    "Наші фото": "intimate", "Тільки ми": "intimate", "Сім'я": "family",
    "Оголені": "intimate", "Інтим": "intimate", "Резервна копія": "personal",
    # Romanian (Romania - 24M)
    "Mesaje Salvate": "personal", "Privat": "intimate", "Secret": "intimate",
    "Pozele Noastre": "intimate", "Doar Noi": "intimate", "Familie": "family",
    "Gol": "intimate", "Intim": "intimate", "Backup": "personal",
    # Bulgarian (Bulgaria - 7M)
    "Запазени съобщения": "personal", "Лично": "intimate", "Тайна": "intimate",
    "Нашите снимки": "intimate", "Само ние": "intimate", "Семейство": "family",
    # Serbian/Croatian/Bosnian (Balkans - 20M)
    "Sačuvane Poruke": "personal", "Privatno": "intimate", "Tajna": "intimate",
    "Naše Fotografije": "intimate", "Samo Mi": "intimate", "Porodica": "family",
    "Goli": "intimate", "Intimno": "intimate", "Rezervna Kopija": "personal",
    # Kazakh (Kazakhstan - 15M)
    "Сақталған хабарлар": "personal", "Жеке": "intimate", "Құпия": "intimate",
    "Біздің фото": "intimate", "Тек біз": "intimate", "Отбасы": "family",
    # Uzbek (Uzbekistan - 35M)
    "Saqlangan Xabarlar": "personal", "Shaxsiy": "intimate", "Maxfiy": "intimate",
    "Bizning Rasmlar": "intimate", "Faqat Biz": "intimate", "Oila": "family",
    # Kyrgyz (Kyrgyzstan - 7M)
    "Сакталган билдирүүлөр": "personal", "Жеке": "intimate", "Жашыруун": "intimate",
    "Биздин сүрөт": "intimate", "Биз гана": "intimate", "Үй-бүлө": "family",
    # Tamil (Sri Lanka, India - 80M)
    "சேமித்த செய்திகள்": "personal", "தனிப்பட்ட": "intimate", "ரகசிய": "intimate",
    "எங்கள் புகைப்படங்கள்": "intimate", "நாங்கள் மட்டும்": "intimate", "குடும்பம்": "family",
    # Telugu (India - 90M)
    "సేవ్ చేసిన సందేశాలు": "personal", "వ్యక్తిగత": "intimate", "రహస్య": "intimate",
    "మా ఫోటోలు": "intimate", "మేము మాత్రమే": "intimate", "కుటుంబం": "family",
    # Marathi (India - 95M)
    "जतन केलेले संदेश": "personal", "खाजगी": "intimate", "गुप्त": "intimate",
    "आमचे फोटो": "intimate", "फक्त आम्ही": "intimate", "कुटुंब": "family",
    # Kannada (India - 50M)
    "ಉಳಿಸಿದ ಸಂದೇಶಗಳು": "personal", "ಖಾಸಗಿ": "intimate", "ರಹಸ್ಯ": "intimate",
    "ನಮ್ಮ ಫೋಟೋಗಳು": "intimate", "ನಾವು ಮಾತ್ರ": "intimate", "ಕುಟುಂಬ": "family",
    # Malayalam (India - 40M)
    "സംരക്ഷിച്ച സന്ദേശങ്ങൾ": "personal", "സ്വകാര്യ": "intimate", "രഹസ്യ": "intimate",
    "ഞങ്ങളുടെ ഫോട്ടോകൾ": "intimate", "ഞങ്ങൾ മാത്രം": "intimate", "കുടുംബം": "family",
    # Gujarati (India - 60M)
    "સાચવેલા સંદેશા": "personal", "ખાનગી": "intimate", "ગુપ્ત": "intimate",
    "અમારા ફોટા": "intimate", "ફક્ત અમે": "intimate", "કુટુંબ": "family",
    # Punjabi (India, Pakistan - 125M)
    "ਸੁਰੱਖਿਅਤ ਸੁਨੇਹੇ": "personal", "ਨਿੱਜੀ": "intimate", "ਗੁਪਤ": "intimate",
    "ਸਾਡੀਆਂ ਫੋਟੋਆਂ": "intimate", "ਸਿਰਫ ਅਸੀਂ": "intimate", "ਪਰਿਵਾਰ": "family",

    # ── CHILDREN: Kids photos, school, underage content ──
    "Kids Photos": "children", "Kid Photos": "children", "Child Photos": "children",
    "Children Photos": "children", "Baby Pics": "children", "Toddler": "children",
    "My Kids": "children", "Our Kids": "children", "My Children": "children",
    "School Photos": "children", "Class Photos": "children", "Student Photos": "children",
    "Kindergarten": "children", "Preschool": "children", "Daycare": "children",
    "Babysitter": "children", "Nanny": "children", "Playgroup": "children",
    "Scout Group": "children", "Youth Group": "children", "Teen": "children",
    "Minors": "children", "Underage": "children", "Juvenile": "children",
    "My Son": "children", "My Daughter": "children", "Our Son": "children", "Our Daughter": "children",
    "First Birthday": "children", "Baby Shower": "children", "Newborn": "children",
    "Ultrasound": "children", "Pregnancy": "children", "Maternity": "children",
    "Pediatric": "children", "Pediatrics": "children", "Kids Album": "children",
    "Children Album": "children", "Family Kids": "children",

    # ── ADULT: 18+ content, porn, escort ──
    "Adult Content": "adult", "Porn": "adult", "Pornography": "adult",
    "XXX": "adult", "NSFW": "adult", "OnlyFans": "adult",
    "Escort": "adult", "Hooker": "adult", "Prostitute": "adult",
    "Sugar Daddy": "adult", "Sugar Baby": "adult", "Webcam": "adult",
    "Cam Girl": "adult", "Stripper": "adult", "Exotic": "adult",
    "Massage Parlor": "adult", "Happy Ending": "adult", "Brothel": "adult",
    "Swingers": "adult", "Threesome": "adult", "Gangbang": "adult",
    "BDSM": "adult", "Fetish": "adult", "Kink": "adult",
    "Amateur": "adult", "Homemade": "adult", "Real Amateur": "adult",
    "Leaked Video": "adult", "Sex Tape": "adult", "Sextape": "adult",
    "Scandal": "adult", "Leaked Scandal": "adult", "Celebrity Leak": "adult",
    "Deepfake": "adult", "Revenge Porn": "adult", "Non Consensual": "adult",
    "Blackmail": "adult", "Extortion": "adult", "Upskirt": "adult",
    "Voyeur": "adult", "Hidden Cam": "adult", "Spy Cam": "adult",
    "Only For Adults": "adult", "18 Plus": "adult", "Over 18": "adult",
    "Mature": "adult", "Milf": "adult", "Gilf": "adult",
    "Teen Porn": "adult", "Barely Legal": "adult", "Jailbait": "adult",
    # Adult in other languages
    "Pornografia": "adult", "Porno": "adult", "Erotik": "adult",
    "Seks": "adult", "Sexo": "adult", "Sexe": "adult",
    "Pornografi": "adult", "Pornofilm": "adult", "Eroottinen": "adult",
    "Pornografie": "adult", "Seksueel": "adult", "Pornografico": "adult",

    # ── STOLEN: Hacked accounts, leaks, dumps ──
    "Leaked": "stolen", "Leaked Photos": "stolen", "Leaked Videos": "stolen",
    "Hacked": "stolen", "Hack": "stolen", "Breach": "stolen",
    "Data Breach": "stolen", "Data Dump": "stolen", "Database": "stolen",
    "Dump": "stolen", "Credit Card Dump": "stolen", "CC Dump": "stolen",
    "Fullz": "stolen", "Dox": "stolen", "Doxxing": "stolen",
    "Exposed": "stolen", "Compromised": "stolen", "Vulnerability": "stolen",
    "Stolen Photos": "stolen", "Stolen Videos": "stolen", "Stolen Data": "stolen",
    "Stolen Identity": "stolen", "Identity Theft": "stolen", "Fake ID": "stolen",
    "Account Dump": "stolen", "Email Dump": "stolen", "Phone Dump": "stolen",
    "Social Media Hack": "stolen", "Instagram Hack": "stolen", "Snapchat Hack": "stolen",
    "Facebook Hack": "stolen", "TikTok Hack": "stolen", "Twitter Hack": "stolen",
    "OF Leak": "stolen", "OnlyFans Leak": "stolen", "Celebrity Hack": "stolen",
    "Revenge": "stolen", "Ex Girlfriend": "stolen", "Ex Boyfriend": "stolen",
    "Ex Wife": "stolen", "Ex Husband": "stolen", "Cheating": "stolen",
    "Infidelity": "stolen", "Caught Cheating": "stolen", "Blackmail": "stolen",
    "Ransom": "stolen", "Extortion": "stolen", "Threat": "stolen",

    # ── WORK: Company internal groups, work backups ──
    "Company Backup": "work", "Office Backup": "work", "Work Files": "work",
    "Internal": "work", "Confidential Work": "work", "Internal Only": "work",
    "Employee Data": "work", "Staff Data": "work", "HR Files": "work",
    "Payroll": "work", "Salary Data": "work", "Employee Records": "work",
    "Company Documents": "work", "Project Files": "work", "Client Data": "work",
    "Customer Data": "work", "Sales Data": "work", "Invoice Data": "work",
    "Contract": "work", "NDA": "work", "Trade Secret": "work",
    "Intellectual Property": "work", "IP": "work", "Prototype": "work",
    "Board Meeting": "work", "Executive": "work", "CEO": "work",
    "Strategy": "work", "Business Plan": "work", "Competitor": "work",
    "Work Chat": "work", "Team Chat": "work", "Department": "work",
    "HR Chat": "work", "IT Department": "work", "Finance Team": "work",

    # ── MEDICAL: Health data, patient records ──
    "Medical Records": "medical", "Patient Data": "medical", "Health Records": "medical",
    "Diagnosis": "medical", "Prescription": "medical", "Medication": "medical",
    "Lab Results": "medical", "Blood Test": "medical", "X Ray": "medical",
    "MRI": "medical", "CT Scan": "medical", "Ultrasound": "medical",
    "Doctor Notes": "medical", "Hospital Records": "medical", "Clinic": "medical",
    "Mental Health": "medical", "Therapy": "medical", "Counseling": "medical",
    "Psychiatrist": "medical", "Psychologist": "medical", "Addiction": "medical",
    "Rehab": "medical", "Recovery": "medical", "Surgery": "medical",
    "Pregnancy Test": "medical", "STD": "medical", "HIV": "medical",
    "Cancer": "medical", "Terminal": "medical", "Hospice": "medical",
    "Insurance Claim": "medical", "Disability": "medical", "Medical Certificate": "medical",

    # ── LEGAL: Court cases, legal documents ──
    "Court Documents": "legal", "Case Files": "legal", "Legal Documents": "legal",
    "Lawyer": "legal", "Attorney": "legal", "Solicitor": "legal",
    "Lawsuit": "legal", "Litigation": "legal", "Arrest": "legal",
    "Criminal Record": "legal", "Police Report": "legal", "Investigation": "legal",
    "Evidence": "legal", "Witness": "legal", "Deposition": "legal",
    "Subpoena": "legal", "Warrant": "legal", "Search Warrant": "legal",
    "Divorce Papers": "legal", "Custody": "legal", "Child Custody": "legal",
    "Adoption Papers": "legal", "Will": "legal", "Testament": "legal",
    "Property Deed": "legal", "Title Deed": "legal", "Lease": "legal",
    "Contract": "legal", "Agreement": "legal", "Settlement": "legal",
    "Judgment": "legal", "Verdict": "legal", "Sentence": "legal",

    # ── SCHOOL: Student data, educational ──
    "Student Records": "school", "Student Data": "school", "Student Photos": "school",
    "Class Photos": "school", "Yearbook": "school", "Graduation": "school",
    "Report Card": "school", "Transcript": "school", "GPA": "school",
    "Exam": "school", "Test Answers": "school", "Cheating": "school",
    "Teacher": "school", "Professor": "school", "Faculty": "school",
    "Campus": "school", "University": "school", "College": "school",
    "Dorm": "school", "Fraternity": "school", "Sorority": "school",
    "Student Group": "school", "Club": "school", "Sports Team": "school",
    "Academic": "school", "Research": "school", "Thesis": "school",
    "Dissertation": "school", "Plagiarism": "school", "Academic Dishonesty": "school",

    # ── RELIGIOUS: Religious communities, places of worship ──
    "Church Group": "religious", "Mosque Group": "religious", "Temple Group": "religious",
    "Synagogue": "religious", "Congregation": "religious", "Fellowship": "religious",
    "Bible Study": "religious", "Quran Study": "religious", "Prayer Group": "religious",
    "Worship": "religious", "Sunday School": "religious", "Youth Ministry": "religious",
    "Missionary": "religious", "Pilgrimage": "religious", "Hajj": "religious",
    "Religious School": "religious", "Madrasa": "religious", "Seminary": "religious",
    "Faith": "religious", "Spiritual": "religious", "Devotional": "religious",
    "Religious Ceremony": "religious", "Baptism": "religious", "Communion": "religious",
    "Confirmation": "religious", "Bar Mitzvah": "religious", "Bat Mitzvah": "religious",
    "Wedding Ceremony": "religious", "Funeral": "religious", "Memorial": "religious",

    # ── MILITARY: Military, government, law enforcement ──
    "Military": "military", "Army": "military", "Navy": "military",
    "Air Force": "military", "Marines": "military", "National Guard": "military",
    "Special Forces": "military", "Intelligence": "military", "Classified": "military",
    "Top Secret": "military", "Secret Documents": "military", "Defense": "military",
    "Pentagon": "military", "Base": "military", "Barracks": "military",
    "Deployment": "military", "Mission": "military", "Operation": "military",
    "Police": "military", "Law Enforcement": "military", "Sheriff": "military",
    "Border Patrol": "military", "Customs": "military", "Immigration": "military",
    "Government": "military", "Federal": "military", "State Department": "military",
    "Diplomatic": "military", "Embassy": "military", "Consulate": "military",
    "Prison": "military", "Correctional": "military", "Detention": "military",

    # ── TRAVEL: Travel photos, passports, visas ──
    "Passport": "travel", "Visa": "travel", "Travel Documents": "travel",
    "Boarding Pass": "travel", "Flight": "travel", "Hotel": "travel",
    "Resort": "travel", "Vacation Photos": "travel", "Trip Photos": "travel",
    "Tour": "travel", "Itinerary": "travel", "Travel Plans": "travel",
    "Travel Insurance": "travel", "Visa Application": "travel", "Embassy": "travel",
    "Border Crossing": "travel", "Customs Declaration": "travel", "Immigration": "travel",
    "Airport": "travel", "TSA": "travel", "Security Check": "travel",
    "Car Rental": "travel", "Driver License": "travel", "International": "travel",

    # ── DATING: Dating apps, hookups, relationships ──
    "Dating": "dating", "Dating App": "dating", "Tinder": "dating",
    "Bumble": "dating", "Hinge": "dating", "OkCupid": "dating",
    "Match": "dating", "Plenty of Fish": "dating", "Grindr": "dating",
    "Hookup": "dating", "Casual": "dating", "One Night Stand": "dating",
    "Friends With Benefits": "dating", "FWB": "dating", "Open Relationship": "dating",
    "Polyamory": "dating", "Poly": "dating", "Swinging": "dating",
    "Speed Dating": "dating", "Blind Date": "dating", "First Date": "dating",
    "Long Distance": "dating", "Online Dating": "dating", "Virtual Date": "dating",
    "Sugar Dating": "dating", "Arrangement": "dating", "Companionship": "dating",

    # ── SHOPPING: Credit cards, receipts, purchases ──
    "Credit Card": "shopping", "Debit Card": "shopping", "Bank Card": "shopping",
    "Receipt": "shopping", "Invoice": "shopping", "Purchase": "shopping",
    "Order": "shopping", "Shipping": "shopping", "Delivery": "shopping",
    "Amazon Order": "shopping", "Ebay": "shopping", "PayPal": "shopping",
    "Gift Card": "shopping", "Voucher": "shopping", "Coupon": "shopping",
    "Refund": "shopping", "Return": "shopping", "Exchange": "shopping",
    "Wishlist": "shopping", "Cart": "shopping", "Checkout": "shopping",
    "Bank Statement": "shopping", "Transaction": "shopping", "Payment": "shopping",
}


@app.get("/api/scanner/keywords")
def get_scanner_keywords(_: str = Depends(require_auth)):
    return {"keywords": list(PRIVACY_KEYWORDS.keys()), "count": len(PRIVACY_KEYWORDS)}


@app.post("/api/scanner/run")
async def run_scanner(body: ScannerRunBody = ScannerRunBody(), _: str = Depends(require_auth)):
    global _tg_client
    if _tg_client is None or not _tg_client.is_connected():
        raise HTTPException(status_code=503, detail="Telegram client not connected")
    if not await _tg_client.is_user_authorized():
        raise HTTPException(status_code=401, detail="Telegram session not authenticated")

    kw_source = body.keywords or list(PRIVACY_KEYWORDS.keys())
    found_groups = []
    total_searched = 0

    for keyword in kw_source:
        total_searched += 1
        # Get category from dict if available, else 'other'
        category = PRIVACY_KEYWORDS.get(keyword, "other")

        try:
            # Use contacts.SearchRequest to search by group name
            result = await asyncio.wait_for(
                _tg_client(TGSearchRequest(q=keyword, limit=100)),
                timeout=20,
            )

            for entity in result.chats + result.users:
                # Filter for groups only (non-broadcast channels + regular chats)
                if isinstance(entity, Channel):
                    if entity.broadcast:
                        continue  # Skip channels, keep only groups
                elif not isinstance(entity, Chat):
                    continue  # Skip users, bots, etc.

                members = _member_count(entity)
                if members > body.max_members:
                    continue

                # Check if already exists
                conn = get_db()
                existing = conn.execute(
                    "SELECT id FROM flagged_groups WHERE telegram_id = ?",
                    (entity.id,)
                ).fetchone()
                conn.close()
                if existing:
                    continue

                conn = get_db()
                try:
                    conn.execute(
                        """
                        INSERT INTO flagged_groups (telegram_id, title, username, members, type, keywords_matched, flag_category, scan_date, reviewed)
                        VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), 0)
                        """,
                        (
                            entity.id,
                            entity.title or "",
                            getattr(entity, "username", None),
                            members,
                            "group",
                            json.dumps([keyword]),
                            category,
                        ),
                    )
                    conn.commit()
                    found_groups.append({
                        "telegram_id": entity.id,
                        "title": entity.title or "",
                        "username": getattr(entity, "username", None),
                        "members": members,
                        "category": category,
                    })
                except sqlite3.IntegrityError:
                    pass
                conn.close()

            await asyncio.sleep(5.0)  # Rate limit between keywords

        except asyncio.TimeoutError:
            continue
        except Exception:
            continue

    return {"searched": total_searched, "found": len(found_groups), "groups": found_groups}


# ── Frontend ──────────────────────────────────────────────────────────
@app.get("/", response_class=HTMLResponse)
def index(_: str = Depends(require_auth)):
    with open(os.path.join(os.path.dirname(__file__), "static", "index.html"), encoding="utf-8") as f:
        return f.read()


if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8001, reload=False)
