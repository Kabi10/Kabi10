import sqlite3
from fastapi import FastAPI
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
import uvicorn
import os

DB_PATH = "thara.db"
app = FastAPI()


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


@app.get("/api/users")
def get_users():
    conn = get_db()
    rows = conn.execute("""
        SELECT
            u.user_id,
            u.username,
            u.first_name,
            u.last_seen,
            COALESCE(p.current_persona, 'thara') AS current_persona,
            COUNT(m.id)                           AS total_messages,
            MAX(m.timestamp)                      AS last_message_time,
            (SELECT content FROM messages
             WHERE user_id = u.user_id
             ORDER BY timestamp DESC LIMIT 1)     AS last_message,
            (SELECT role FROM messages
             WHERE user_id = u.user_id
             ORDER BY timestamp DESC LIMIT 1)     AS last_message_role
        FROM users u
        LEFT JOIN messages m       ON m.user_id = u.user_id
        LEFT JOIN user_profiles p  ON p.user_id = u.user_id
        GROUP BY u.user_id
        ORDER BY last_message_time DESC
    """).fetchall()
    conn.close()
    return [dict(r) for r in rows]


@app.get("/api/messages/{user_id}")
def get_messages(user_id: int):
    conn = get_db()
    rows = conn.execute("""
        SELECT id, user_id, role, content, timestamp,
               prompt_tokens, completion_tokens, cost_usd,
               COALESCE(persona_id, 'thara') AS persona_id
        FROM messages
        WHERE user_id = ?
        ORDER BY timestamp ASC
    """, (user_id,)).fetchall()
    conn.close()
    return [dict(r) for r in rows]


@app.get("/api/stats")
def get_stats():
    conn = get_db()
    row = conn.execute("""
        SELECT
            COUNT(*)                                      AS total_messages,
            SUM(CASE WHEN role='user' THEN 1 ELSE 0 END) AS user_messages,
            SUM(prompt_tokens)                            AS total_input_tokens,
            SUM(completion_tokens)                        AS total_output_tokens,
            SUM(cost_usd)                                 AS total_cost_usd,
            COUNT(DISTINCT user_id)                       AS total_users
        FROM messages
    """).fetchone()
    conn.close()
    return dict(row)


@app.get("/", response_class=HTMLResponse)
def dashboard():
    with open(os.path.join(os.path.dirname(__file__), "static", "index.html"), encoding="utf-8") as f:
        return f.read()


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=False)
