import aiosqlite
from config import DB_PATH


async def init_db() -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.executescript("""
            CREATE TABLE IF NOT EXISTS users (
                user_id    INTEGER PRIMARY KEY,
                username   TEXT,
                first_name TEXT,
                last_seen  DATETIME DEFAULT CURRENT_TIMESTAMP
            );

            CREATE TABLE IF NOT EXISTS messages (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id           INTEGER NOT NULL,
                role              TEXT NOT NULL,
                content           TEXT NOT NULL,
                timestamp         DATETIME DEFAULT CURRENT_TIMESTAMP,
                prompt_tokens     INTEGER DEFAULT 0,
                completion_tokens INTEGER DEFAULT 0,
                cost_usd          REAL DEFAULT 0.0,
                persona_id        TEXT DEFAULT 'thara',
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );

            CREATE TABLE IF NOT EXISTS user_profiles (
                user_id         INTEGER PRIMARY KEY,
                display_name    TEXT DEFAULT '',
                current_persona TEXT DEFAULT 'thara',
                memory          TEXT DEFAULT '',
                updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );

            CREATE INDEX IF NOT EXISTS idx_messages_user_id   ON messages(user_id);
            CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp);
        """)

        # Zero-downtime migrations for existing DBs
        cursor = await db.execute("PRAGMA table_info(user_profiles)")
        cols = {row[1] for row in await cursor.fetchall()}
        if "current_persona" not in cols:
            await db.execute("ALTER TABLE user_profiles ADD COLUMN current_persona TEXT DEFAULT 'thara'")
        if "display_name" not in cols:
            await db.execute("ALTER TABLE user_profiles ADD COLUMN display_name TEXT DEFAULT ''")

        cursor = await db.execute("PRAGMA table_info(messages)")
        cols = {row[1] for row in await cursor.fetchall()}
        if "persona_id" not in cols:
            await db.execute("ALTER TABLE messages ADD COLUMN persona_id TEXT DEFAULT 'thara'")

        await db.commit()


async def upsert_user(user_id: int, username: str | None, first_name: str | None) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            INSERT INTO users (user_id, username, first_name, last_seen)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(user_id) DO UPDATE SET
                username   = excluded.username,
                first_name = excluded.first_name,
                last_seen  = CURRENT_TIMESTAMP
        """, (user_id, username, first_name))
        await db.commit()


async def log_message(
    user_id: int,
    role: str,
    content: str,
    prompt_tokens: int = 0,
    completion_tokens: int = 0,
    cost_usd: float = 0.0,
    persona_id: str = "thara",
) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            INSERT INTO messages (user_id, role, content, prompt_tokens, completion_tokens, cost_usd, persona_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, (user_id, role, content, prompt_tokens, completion_tokens, cost_usd, persona_id))
        await db.commit()


async def load_recent_messages(user_id: int, limit: int = 20) -> list[dict]:
    async with aiosqlite.connect(DB_PATH) as db:
        db.row_factory = aiosqlite.Row
        async with db.execute("""
            SELECT role, content FROM messages
            WHERE user_id = ?
            ORDER BY timestamp DESC
            LIMIT ?
        """, (user_id, limit)) as cursor:
            rows = await cursor.fetchall()
    return [{"role": r["role"], "content": r["content"]} for r in reversed(rows)]


async def get_user_persona(user_id: int) -> str:
    async with aiosqlite.connect(DB_PATH) as db:
        db.row_factory = aiosqlite.Row
        async with db.execute(
            "SELECT current_persona FROM user_profiles WHERE user_id = ?", (user_id,)
        ) as cursor:
            row = await cursor.fetchone()
    return (row["current_persona"] or "thara") if row else "thara"


async def set_user_persona(user_id: int, persona_id: str) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            INSERT INTO user_profiles (user_id, current_persona, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(user_id) DO UPDATE SET
                current_persona = excluded.current_persona,
                updated_at      = CURRENT_TIMESTAMP
        """, (user_id, persona_id))
        await db.commit()


async def get_user_memory(user_id: int) -> str:
    async with aiosqlite.connect(DB_PATH) as db:
        db.row_factory = aiosqlite.Row
        async with db.execute(
            "SELECT memory FROM user_profiles WHERE user_id = ?", (user_id,)
        ) as cursor:
            row = await cursor.fetchone()
    return (row["memory"] or "") if row else ""


async def update_user_memory(user_id: int, memory: str) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            INSERT INTO user_profiles (user_id, memory, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(user_id) DO UPDATE SET
                memory     = excluded.memory,
                updated_at = CURRENT_TIMESTAMP
        """, (user_id, memory))
        await db.commit()


async def get_display_name(user_id: int) -> str:
    async with aiosqlite.connect(DB_PATH) as db:
        db.row_factory = aiosqlite.Row
        async with db.execute(
            "SELECT display_name FROM user_profiles WHERE user_id = ?", (user_id,)
        ) as cursor:
            row = await cursor.fetchone()
    return (row["display_name"] or "") if row else ""


async def set_display_name(user_id: int, name: str) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute("""
            INSERT INTO user_profiles (user_id, display_name, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(user_id) DO UPDATE SET
                display_name = excluded.display_name,
                updated_at   = CURRENT_TIMESTAMP
        """, (user_id, name))
        await db.commit()
