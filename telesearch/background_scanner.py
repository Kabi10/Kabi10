"""
Background Privacy Scanner for TG Search
Runs as a standalone process or via cron to discover accidentally-public
small groups that may contain sensitive content.

Usage:
    python background_scanner.py           # Run once
    python background_scanner.py --loop    # Run continuously every 30 min
"""
import os
import sys
import json
import sqlite3
import asyncio
import argparse
from datetime import datetime

from telethon import TelegramClient
from telethon.tl.functions.contacts import SearchRequest
from telethon.tl.types import Channel, Chat

# ── Config ────────────────────────────────────────────────────────────
DB_PATH = os.path.join(os.path.dirname(__file__), "telesearch.db")
TG_API_ID = int(os.getenv("TG_API_ID", "31671257"))
TG_API_HASH = os.getenv("TG_API_HASH", "ec880ffa330d0c826b77a7ffc3a16255")
TG_SESSION = os.path.join(os.path.dirname(__file__), "session")

DEFAULT_KEYWORDS = [
    # English
    "Saved Messages", "My Saves", "Backup", "Private", "Personal", "Secret",
    "Our Photos", "Our Pics", "Personal Album", "My Files", "Family Only", "Just Us",
    "Memory 2024", "Memory 2025", "Memory 2026",
    "Backup 2024", "Backup 2025", "Backup 2026",
    "Do Not Share", "Private Do Not Forward", "Eyes Only",
    # Russian
    "Сохраненные", "Личное", "Приватное", "Секретное", "Наши фото", "Личный архив", "Только мы",
    # Spanish
    "Mensajes Guardados", "Archivos", "Privado", "Personal", "Nuestras Fotos", "Album Personal", "Solo Nosotros",
    # Portuguese
    "Mensagens Salvas", "Cadangan", "Privado", "Pessoal", "Nossas Fotos", "Album Pessoal", "Só Nós",
    # Arabic
    "الرسائل المحفوظة", "خاص", "سري", "صورنا", "الألبوم الشخصي", "عائلتي فقط",
    # Hindi
    "सेव किए गए", "बैकअप", "निजी", "गुप्त", "हमारी फोटो", "व्यक्तिगत", "सिर्फ हम",
    # Persian
    "ذخیره شده", "پیام‌ها", "خصوصی", "محرمانه", "عکس‌های ما", "آلبوم شخصی", "فقط ما",
    # Indonesian
    "Pesan Tersimpan", "Pribadi", "Rahasia", "Foto Kita", "Album Pribadi", "Hanya Kita",
    # Turkish
    "Kayıtlı Mesajlar", "Özel", "Gizli", "Fotoğraflarımız", "Kişisel Albüm", "Sadece Biz",
]


def _member_count(entity) -> int:
    participants = getattr(entity, "participants_count", None)
    return participants if participants is not None else 0


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


async def scan_once(client: TelegramClient, keywords=None, max_members=100, max_results_per_keyword=100):
    """Run one scan pass."""
    keywords = keywords or DEFAULT_KEYWORDS
    found_count = 0
    searched_count = 0
    print(f"[{datetime.now().isoformat()}] Starting scan with {len(keywords)} keywords...")

    for keyword in keywords:
        searched_count += 1
        print(f"  [{searched_count}/{len(keywords)}] Searching: '{keyword}'")

        try:
            result = await asyncio.wait_for(
                client(SearchRequest(q=keyword, limit=max_results_per_keyword)),
                timeout=20,
            )

            for entity in result.chats + result.users:
                # Filter for groups only
                if isinstance(entity, Channel):
                    if entity.broadcast:
                        continue  # Skip channels, keep groups
                elif not isinstance(entity, Chat):
                    continue  # Skip users/bots

                members = _member_count(entity)
                if members > max_members:
                    continue

                # Check for duplicates
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
                        INSERT INTO flagged_groups
                        (telegram_id, title, username, members, type, keywords_matched, scan_date, reviewed)
                        VALUES (?, ?, ?, ?, ?, ?, datetime('now'), 0)
                        """,
                        (
                            entity.id,
                            entity.title or "",
                            getattr(entity, "username", None),
                            members,
                            "group",
                            json.dumps([keyword]),
                        ),
                    )
                    conn.commit()
                    found_count += 1
                    print(f"    + Found: '{entity.title}' (@{getattr(entity, 'username', None)}) [{members} members]")
                except sqlite3.IntegrityError:
                    pass
                conn.close()

            await asyncio.sleep(5.0)

        except asyncio.TimeoutError:
            print(f"    ! Timeout for '{keyword}'")
        except Exception as exc:
            print(f"    ! Error for '{keyword}': {exc}")

    print(f"[{datetime.now().isoformat()}] Scan complete. Found {found_count} new groups.")
    return {"searched": searched_count, "found": found_count}


async def run_loop(client: TelegramClient, interval_minutes=30):
    """Run scan continuously at intervals."""
    while True:
        await scan_once(client)
        print(f"[{datetime.now().isoformat()}] Sleeping for {interval_minutes} minutes...")
        await asyncio.sleep(interval_minutes * 60)


async def main():
    parser = argparse.ArgumentParser(description="TG Search Privacy Scanner")
    parser.add_argument("--loop", action="store_true", help="Run continuously every 30 minutes")
    parser.add_argument("--interval", type=int, default=30, help="Interval in minutes (default: 30)")
    parser.add_argument("--max-members", type=int, default=100, help="Max members filter (default: 100)")
    parser.add_argument("--keywords", type=str, help="Path to JSON file with custom keyword list")
    args = parser.parse_args()

    custom_keywords = None
    if args.keywords:
        with open(args.keywords, "r", encoding="utf-8") as f:
            custom_keywords = json.load(f)

    print(f"[{datetime.now().isoformat()}] Connecting to Telegram...")
    client = TelegramClient(TG_SESSION, TG_API_ID, TG_API_HASH)
    await client.connect()

    if not await client.is_user_authorized():
        print("ERROR: Telegram session not authenticated. Run create_session_local.py first.")
        await client.disconnect()
        sys.exit(1)

    print(f"[{datetime.now().isoformat()}] Connected and authenticated.")

    if args.loop:
        await run_loop(client, interval_minutes=args.interval)
    else:
        result = await scan_once(client, keywords=custom_keywords, max_members=args.max_members)
        print(json.dumps(result, indent=2))

    await client.disconnect()
    print(f"[{datetime.now().isoformat()}] Disconnected.")


if __name__ == "__main__":
    asyncio.run(main())
