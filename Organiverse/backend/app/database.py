from __future__ import annotations

import os
import json
import sqlite3
import threading
from datetime import datetime, timezone
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_DB_PATH = PROJECT_ROOT / "backend" / "organiverse.sqlite3"


def _db_path() -> Path:
    raw_path = os.getenv("ORGANIVERSE_DB_PATH")
    if not raw_path:
        return DEFAULT_DB_PATH

    path = Path(raw_path)
    if path.is_absolute():
        return path
    return PROJECT_ROOT / path


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _row_to_message(row: sqlite3.Row) -> dict:
    metadata = {}
    if "metadata" in row.keys() and row["metadata"]:
        try:
            metadata = json.loads(row["metadata"])
        except json.JSONDecodeError:
            metadata = {}

    return {
        "id": row["id"],
        "sessionId": row["session_id"],
        "senderId": row["sender_id"],
        "senderName": row["sender_name"],
        "role": row["role"],
        "kind": row["kind"] if "kind" in row.keys() else "chat",
        "content": row["content"],
        "color": row["color"],
        "avatar": row["avatar"],
        "metadata": metadata,
        "createdAt": row["created_at"],
    }


class ChatStore:
    def __init__(self, db_path: Path | None = None) -> None:
        self.db_path = db_path or _db_path()
        self._lock = threading.Lock()

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(self.db_path)
        connection.row_factory = sqlite3.Row
        return connection

    def init_db(self) -> None:
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        with self._lock, self._connect() as connection:
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    sender_id TEXT NOT NULL,
                    sender_name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    kind TEXT NOT NULL DEFAULT 'chat',
                    content TEXT NOT NULL,
                    color TEXT NOT NULL,
                    avatar TEXT NOT NULL,
                    metadata TEXT NOT NULL DEFAULT '{}',
                    created_at TEXT NOT NULL
                )
                """
            )
            self._ensure_column(connection, "messages", "kind", "TEXT NOT NULL DEFAULT 'chat'")
            self._ensure_column(connection, "messages", "metadata", "TEXT NOT NULL DEFAULT '{}'")
            connection.execute(
                """
                CREATE INDEX IF NOT EXISTS idx_messages_session_id_id
                ON messages (session_id, id)
                """
            )
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS session_state (
                    session_id TEXT PRIMARY KEY,
                    state_json TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """
            )

    def _ensure_column(
        self,
        connection: sqlite3.Connection,
        table_name: str,
        column_name: str,
        definition: str,
    ) -> None:
        columns = {
            row["name"]
            for row in connection.execute(f"PRAGMA table_info({table_name})").fetchall()
        }
        if column_name not in columns:
            connection.execute(
                f"ALTER TABLE {table_name} ADD COLUMN {column_name} {definition}"
            )

    def add_message(
        self,
        *,
        session_id: str,
        sender_id: str,
        sender_name: str,
        role: str,
        content: str,
        color: str,
        avatar: str,
        kind: str = "chat",
        metadata: dict | None = None,
    ) -> dict:
        created_at = _utc_now()
        metadata_json = json.dumps(metadata or {})
        with self._lock, self._connect() as connection:
            cursor = connection.execute(
                """
                INSERT INTO messages (
                    session_id, sender_id, sender_name, role, kind, content, color,
                    avatar, metadata, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    session_id,
                    sender_id,
                    sender_name,
                    role,
                    kind,
                    content,
                    color,
                    avatar,
                    metadata_json,
                    created_at,
                ),
            )
            row = connection.execute(
                "SELECT * FROM messages WHERE id = ?",
                (cursor.lastrowid,),
            ).fetchone()
        return _row_to_message(row)

    def get_messages(self, session_id: str, limit: int = 80) -> list[dict]:
        with self._lock, self._connect() as connection:
            rows = connection.execute(
                """
                SELECT * FROM (
                    SELECT * FROM messages
                    WHERE session_id = ?
                    ORDER BY id DESC
                    LIMIT ?
                )
                ORDER BY id ASC
                """,
                (session_id, limit),
            ).fetchall()
        return [_row_to_message(row) for row in rows]

    def list_sessions(self, limit: int = 200) -> list[dict]:
        with self._lock, self._connect() as connection:
            rows = connection.execute(
                """
                SELECT
                    s.session_id,
                    s.state_json,
                    s.updated_at,
                    (SELECT COUNT(*) FROM messages m WHERE m.session_id = s.session_id) AS message_count,
                    (SELECT content FROM messages m
                     WHERE m.session_id = s.session_id
                     ORDER BY m.id DESC
                     LIMIT 1) AS last_message,
                    (SELECT sender_name FROM messages m
                     WHERE m.session_id = s.session_id
                     ORDER BY m.id DESC
                     LIMIT 1) AS last_sender_name,
                    (SELECT created_at FROM messages m
                     WHERE m.session_id = s.session_id
                     ORDER BY m.id DESC
                     LIMIT 1) AS last_message_at
                FROM session_state s
                ORDER BY s.updated_at DESC
                LIMIT ?
                """,
                (limit,),
            ).fetchall()

        sessions: list[dict] = []
        for row in rows:
            state = {}
            try:
                state = json.loads(row["state_json"]) if row["state_json"] else {}
            except json.JSONDecodeError:
                state = {}
            sessions.append(
                {
                    "sessionId": row["session_id"],
                    "createdAt": state.get("createdAt", ""),
                    "updatedAt": row["updated_at"],
                    "messageCount": int(row["message_count"] or 0),
                    "lastMessage": row["last_message"] or "",
                    "lastSenderName": row["last_sender_name"] or "",
                    "lastMessageAt": row["last_message_at"] or "",
                    "eventCount": int(state.get("eventCount", 0)),
                    "crisisCount": int(state.get("crisisCount", 0)),
                    "crisisActive": bool(state.get("crisisActive", False)),
                    "rebellionActive": bool(state.get("rebellionActive", False)),
                    "memorySummary": str(state.get("memorySummary", "")),
                    "weather": state.get("weather", {}),
                }
            )
        return sessions

    def get_state(self, session_id: str) -> dict | None:
        with self._lock, self._connect() as connection:
            row = connection.execute(
                "SELECT state_json FROM session_state WHERE session_id = ?",
                (session_id,),
            ).fetchone()
        if not row:
            return None
        try:
            return json.loads(row["state_json"])
        except json.JSONDecodeError:
            return None

    def save_state(self, session_id: str, state: dict) -> None:
        updated_at = _utc_now()
        state["updatedAt"] = updated_at
        with self._lock, self._connect() as connection:
            connection.execute(
                """
                INSERT INTO session_state (session_id, state_json, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(session_id) DO UPDATE SET
                    state_json = excluded.state_json,
                    updated_at = excluded.updated_at
                """,
                (session_id, json.dumps(state), updated_at),
            )

    def clear_session(self, session_id: str) -> None:
        with self._lock, self._connect() as connection:
            connection.execute("DELETE FROM messages WHERE session_id = ?", (session_id,))
            connection.execute(
                "DELETE FROM session_state WHERE session_id = ?",
                (session_id,),
            )

    def clear_messages(self, session_id: str) -> None:
        with self._lock, self._connect() as connection:
            connection.execute("DELETE FROM messages WHERE session_id = ?", (session_id,))
