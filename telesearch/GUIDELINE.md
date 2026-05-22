# TG Search — Project Guideline

## Purpose
Standalone Telegram group/channel discovery and indexing tool. Designed for **security researchers and threat intelligence professionals** to discover accidentally-public Telegram groups that may contain sensitive private content (e.g., personal photos, intimate moments, family albums) stored in groups mistakenly assumed to be private.

## Core Philosophy
- **No join/leave operations** — never join any group, only search public metadata
- **No message scraping** — only fetch group title, username, description, and member count
- **Read-only discovery** — no automated contact with group owners
- **Manual review only** — flagged groups are presented for human inspection, never auto-notified

## Architecture

```
Telegram MTProto (Telethon user session)
  -> messages.SearchGlobalRequest  (global search with pagination)
  -> contacts.SearchRequest        (fallback, query variants)
  -> get_entity(username)          (dead-link verification)
  -> SQLite telesearch.db          (tg_index + flagged_groups)
```

## Key Features

### 1. Enhanced Search (`/api/tg-search`)
- Uses `messages.SearchGlobalRequest` with pagination (up to 1000 results per query)
- Filter: `groups_only`, `broadcasts_only`, `users_only`
- Sort: `relevance`, `members` (desc), `small_first` (asc — for privacy discovery)
- Member range filter: `min_members`, `max_members`
- 3-second delay between pagination pages to avoid rate limits

### 2. Personal Index (`/api/tg-index`)
- Save search results to personal SQLite database
- Categorize, tag, and add notes
- Dead-link verification via `get_entity`

### 3. Background Privacy Scanner
- Periodic background job scanning sensitive keywords in multiple languages
- Targets: accidentally-public groups named like "Saved Messages", "Private Photos", "Family Only"
- Discovers small groups (1-100 members) that are most likely mistaken for private folders
- Stores results in `flagged_groups` table for manual review

### 4. Manual Review Workflow
- Flagged groups tab shows: title, username, member count, matched keywords, scan date
- Reviewed/unreviewed status tracking
- One-click open in Telegram
- Export to CSV for external analysis

## Database Schema

### tg_index (saved groups/channels)
```sql
CREATE TABLE tg_index (
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
```

### flagged_groups (discovered by scanner)
```sql
CREATE TABLE flagged_groups (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    telegram_id     BIGINT  UNIQUE NOT NULL,
    title           TEXT    NOT NULL,
    username        TEXT,
    members         INTEGER,
    type            TEXT    NOT NULL,  -- 'channel', 'group', 'bot'
    keywords_matched TEXT,  -- JSON array of matched search terms
    scan_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed        INTEGER DEFAULT 0,
    notes           TEXT    DEFAULT ''
);
```

## Rate Limiting & Safety
- Search pagination: 3-second delay between pages
- Background scanner: 5-second delay between queries
- Dead-link check: 0.6-second delay per item
- Max pages per search: 10 (1000 results)
- On FloodWaitError: parse wait time from error, sleep accordingly (max 5 minutes)
- **Never send messages or join groups**

## Sensitive Keyword Strategy (Privacy Scanner)

### English
Saved Messages, My Saves, Backup, Private, Personal, Secret, Our Photos, Our Pics, Personal Album, My Files, Family Only, Just Us, Memory 2024, Backup 2024, Do Not Share, Private Do Not Forward

### Russian
Сохраненные, Личное, Приватное, Секретное, Наши фото, Личный архив, Только мы

### Spanish
Mensajes Guardados, Archivos, Privado, Personal, Nuestras Fotos, Album Personal, Solo Nosotros

### Portuguese
Mensagens Salvas, Backup, Privado, Pessoal, Nossas Fotos, Album Pessoal, Só Nós

### Arabic
الرسائل المحفوظة, خاص, سري, صورنا, الألبوم الشخصي, عائلتي فقط

### Hindi
सेव किए गए, बैकअप, निजी, गुप्त, हमारी फोटो, व्यक्तिगत, सिर्फ हम

### Persian
ذخیره شده, پیام‌ها, خصوصی, محرمانه, عکس‌های ما, آلبوم شخصی, فقط ما

### Indonesian
Pesan Tersimpan, Cadangan, Pribadi, Rahasia, Foto Kita, Album Pribadi, Hanya Kita

### Turkish
Kayıtlı Mesajlar, Yedek, Özel, Gizli, Fotoğraflarımız, Kişisel Albüm, Sadece Biz

## Deployment
- Port: 8001 (Sanctum on 8000, Organiverse on 8010)
- Nginx: reverse proxy at /telesearch/
- Supervisor: telesearch service
- Auth: HTTP Basic Auth (admin / telesearch2024)

## Quick Deploy
```bash
python quick_deploy.py  # Uploads main.py + static/index.html to server via SSH
```
