from __future__ import annotations

import asyncio
import html
import os
import random
import secrets
import uuid
from collections import defaultdict
from contextlib import asynccontextmanager
from pathlib import Path

from dotenv import load_dotenv
from fastapi import Depends, FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from fastapi.security import HTTPBasic, HTTPBasicCredentials

from .ai import DeepSeekClient
from .ai import SUPPORTED_REPLY_LANGUAGES
from .database import ChatStore
from .organs import ORGANS, Organ
from .state import (
    build_report_card,
    create_initial_state,
    ensure_state_shape,
    ORGAN_IDS,
    organ_sequence,
    parse_specialty_mention,
    public_state,
    update_relationship_from_message,
    apply_user_event,
)


PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(PROJECT_ROOT / ".env")


store = ChatStore()
ai_client = DeepSeekClient()
session_locks: dict[str, asyncio.Lock] = defaultdict(asyncio.Lock)
admin_security = HTTPBasic()
ADMIN_USER = os.getenv("ORGANIVERSE_ADMIN_USER", "").strip()
ADMIN_PASS = os.getenv("ORGANIVERSE_ADMIN_PASS", "").strip()


class ConnectionManager:
    def __init__(self) -> None:
        self._connections: dict[str, set[WebSocket]] = defaultdict(set)
        self._lock = asyncio.Lock()

    async def connect(self, session_id: str, websocket: WebSocket) -> None:
        await websocket.accept()
        async with self._lock:
            self._connections[session_id].add(websocket)

    async def disconnect(self, session_id: str, websocket: WebSocket) -> None:
        async with self._lock:
            self._connections[session_id].discard(websocket)
            if not self._connections[session_id]:
                del self._connections[session_id]

    async def broadcast(self, session_id: str, payload: dict) -> None:
        async with self._lock:
            connections = list(self._connections.get(session_id, set()))

        stale_connections: list[WebSocket] = []
        for connection in connections:
            try:
                await connection.send_json(payload)
            except Exception:
                stale_connections.append(connection)

        if stale_connections:
            async with self._lock:
                for connection in stale_connections:
                    self._connections[session_id].discard(connection)


manager = ConnectionManager()


@asynccontextmanager
async def lifespan(_: FastAPI):
    await asyncio.to_thread(store.init_db)
    yield


app = FastAPI(title="Organiverse", version="0.2.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def load_state(session_id: str) -> dict:
    state = store.get_state(session_id)
    return ensure_state_shape(session_id, state)


async def save_state(session_id: str, state: dict) -> None:
    await asyncio.to_thread(store.save_state, session_id, state)


async def broadcast_state(session_id: str, state: dict) -> None:
    await manager.broadcast(
        session_id,
        {"type": "state", "state": public_state(state)},
    )


async def broadcast_achievements(session_id: str, state: dict, achievements: list[dict]) -> None:
    if not achievements:
        return
    if not state.get("settings", {}).get("achievementsEnabled", True):
        return
    for achievement in achievements:
        await manager.broadcast(
            session_id,
            {"type": "achievement", "achievement": achievement},
        )


@app.get("/health")
async def health() -> dict:
    return {
        "ok": True,
        "aiMode": "mock" if ai_client.mock_enabled else "deepseek",
        "model": ai_client.model,
        "multilingualModel": ai_client.multilingual_model,
        "multilingualEnabled": bool(ai_client.multilingual_api_key),
        "features": [
            "relationships",
            "memory",
            "report_cards",
            "specialties",
            "diaries",
            "weather",
            "achievements",
        "streaming",
        ],
    }


def require_admin(credentials: HTTPBasicCredentials = Depends(admin_security)) -> str:
    if not ADMIN_USER or not ADMIN_PASS:
        raise HTTPException(status_code=503, detail="Admin auth is not configured.")
    username_ok = secrets.compare_digest(credentials.username.encode(), ADMIN_USER.encode())
    password_ok = secrets.compare_digest(credentials.password.encode(), ADMIN_PASS.encode())
    if not (username_ok and password_ok):
        raise HTTPException(
            status_code=401,
            detail="Unauthorized",
            headers={"WWW-Authenticate": 'Basic realm="Organiverse Admin"'},
        )
    return credentials.username


@app.get("/organs")
async def organs() -> list[dict[str, str]]:
    return [organ.public_dict() for organ in ORGANS]


@app.get("/sessions/{session_id}/state")
async def session_state(session_id: str) -> dict:
    state = load_state(session_id)
    await save_state(session_id, state)
    return public_state(state)


@app.get("/sessions/{session_id}/summary")
async def session_summary(session_id: str) -> dict:
    state = load_state(session_id)
    return await ai_client.generate_summary(session_id=session_id, state=state)


@app.get("/sessions/{session_id}/export.html", response_class=HTMLResponse)
async def export_session(session_id: str) -> str:
    messages = await asyncio.to_thread(store.get_messages, session_id, 1000)
    state = load_state(session_id)
    return build_export_html(session_id=session_id, messages=messages, state=state)


@app.get("/admin/api/sessions")
async def admin_sessions(_: str = Depends(require_admin)) -> list[dict]:
    return await asyncio.to_thread(store.list_sessions, 200)


@app.get("/admin/api/sessions/{session_id}/messages")
async def admin_session_messages(session_id: str, _: str = Depends(require_admin)) -> list[dict]:
    return await asyncio.to_thread(store.get_messages, session_id, 1000)


@app.get("/admin/api/sessions/{session_id}/state")
async def admin_session_state(session_id: str, _: str = Depends(require_admin)) -> dict:
    state = load_state(session_id)
    return public_state(state)


@app.websocket("/ws/{session_id}")
async def websocket_endpoint(websocket: WebSocket, session_id: str) -> None:
    await manager.connect(session_id, websocket)
    try:
        state = load_state(session_id)
        await save_state(session_id, state)
        history = await asyncio.to_thread(store.get_messages, session_id, 140)
        await websocket.send_json(
            {
                "type": "history",
                "sessionId": session_id,
                "messages": history,
                "organs": [organ.public_dict() for organ in ORGANS],
                "state": public_state(state),
            }
        )

        while True:
            payload = await websocket.receive_json()
            message_type = payload.get("type")

            if message_type == "user_message":
                await handle_user_message(session_id=session_id, payload=payload)
                continue
            if message_type == "settings_update":
                await handle_settings_update(session_id=session_id, payload=payload)
                continue
            if message_type == "diary_request":
                await handle_diary_request(session_id=session_id)
                continue
            if message_type == "clear_chat":
                await handle_clear_chat(session_id=session_id)
                continue
            if message_type == "reset_session":
                await handle_reset_session(session_id=session_id)
                continue

            await websocket.send_json({"type": "error", "message": "Unsupported message type."})
    except WebSocketDisconnect:
        await manager.disconnect(session_id, websocket)
    except Exception as exc:
        await manager.broadcast(
            session_id,
            {"type": "error", "message": f"Server error: {exc.__class__.__name__}"},
        )
        await manager.disconnect(session_id, websocket)


async def handle_user_message(*, session_id: str, payload: dict) -> None:
    content = str(payload.get("content", "")).strip()
    if not content:
        await manager.broadcast(session_id, {"type": "error", "message": "Message content is required."})
        return

    user_name = str(payload.get("userName") or "You").strip()[:28] or "You"
    target_organ_id, request_text = parse_specialty_mention(content)

    async with session_locks[session_id]:
        state = load_state(session_id)
        achievements = apply_user_event(state, content, user_name=user_name)
        await save_state(session_id, state)

        user_message = await asyncio.to_thread(
            store.add_message,
            session_id=session_id,
            sender_id="user",
            sender_name=user_name,
            role="user",
            content=content,
            color="#60a5fa",
            avatar=user_name[:1].upper() or "U",
            metadata={"targetOrganId": target_organ_id},
        )
        await manager.broadcast(session_id, {"type": "message", "message": user_message})
        await broadcast_state(session_id, state)
        await broadcast_achievements(session_id, state, achievements)

        await run_organ_round(
            session_id=session_id,
            latest_event=request_text,
            target_organ_id=target_organ_id,
            state=state,
        )

        if int(state.get("eventCount", 0)) % 5 == 0:
            await add_report_card(session_id=session_id, state=state)

        await save_state(session_id, state)
        await broadcast_state(session_id, state)


async def handle_settings_update(*, session_id: str, payload: dict) -> None:
    state = load_state(session_id)
    settings = state.setdefault("settings", {})
    incoming = dict(payload.get("settings") or {})
    api_key_supplied = "apiKey" in incoming
    api_key = str(incoming.pop("apiKey", "")).strip()

    if api_key:
        ai_client.set_session_key(session_id, api_key)
    elif api_key_supplied:
        ai_client.reset_session_key(session_id)

    allowed_keys = {
        "enabledOrgans",
        "verbosity",
        "soundEnabled",
        "achievementsEnabled",
        "volume",
        "replyLanguage",
    }
    for key, value in incoming.items():
        if key not in allowed_keys:
            continue
        if key == "replyLanguage":
            language = str(value or "").strip()
            if language in SUPPORTED_REPLY_LANGUAGES:
                settings[key] = language
            continue
        settings[key] = value

    await save_state(session_id, state)
    await broadcast_state(session_id, state)


async def handle_diary_request(*, session_id: str) -> None:
    state = load_state(session_id)
    diaries = state.setdefault("diaries", {})
    if not diaries:
        entries = await asyncio.gather(
            *[
                ai_client.generate_diary(session_id=session_id, organ=organ, state=state)
                for organ in ORGANS
            ]
        )
        for organ, entry in zip(ORGANS, entries):
            diaries[organ.id] = {
                "organId": organ.id,
                "organName": organ.name,
                "color": organ.color,
                "entry": entry,
            }
        await save_state(session_id, state)

    await manager.broadcast(
        session_id,
        {"type": "diary", "diaries": list(diaries.values())},
    )


async def handle_clear_chat(*, session_id: str) -> None:
    await asyncio.to_thread(store.clear_messages, session_id)
    state = load_state(session_id)
    state["events"] = []
    state["substanceTally"] = {}
    state["lastFoods"] = []
    state["junkFoodCount"] = 0
    state["hydrationCount"] = 0
    state["eventCount"] = 0
    state["crisisCount"] = 0
    state["crisisActive"] = False
    state["rebellionActive"] = False
    state["diaries"] = {}
    state["moods"] = {organ_id: 0 for organ_id in ORGAN_IDS}
    state["memorySummary"] = "No user events logged yet."
    await save_state(session_id, state)
    await manager.broadcast(
        session_id,
        {
            "type": "chat_cleared",
            "messages": [],
            "state": public_state(state),
        },
    )


async def handle_reset_session(*, session_id: str) -> None:
    await asyncio.to_thread(store.clear_session, session_id)
    ai_client.reset_session_key(session_id)
    state = create_initial_state(session_id)
    await save_state(session_id, state)
    await manager.broadcast(
        session_id,
        {
            "type": "reset",
            "messages": [],
            "state": public_state(state),
            "organs": [organ.public_dict() for organ in ORGANS],
        },
    )


async def run_organ_round(
    *,
    session_id: str,
    latest_event: str,
    target_organ_id: str | None,
    state: dict,
) -> None:
    organs = organ_sequence(state, target_organ_id)
    if not organs:
        await manager.broadcast(session_id, {"type": "organ_round_complete"})
        return

    await manager.broadcast(
        session_id,
        {
            "type": "organ_round_started",
            "targetOrganId": target_organ_id,
            "organs": [organ.public_dict() for organ in organs],
        },
    )

    history = await asyncio.to_thread(store.get_messages, session_id, 80)

    try:
        for index, organ in enumerate(organs):
            if index > 0:
                await asyncio.sleep(0.3 + random.random() * 0.2)
            organ_message = await stream_organ_message(
                session_id=session_id,
                organ=organ,
                history=history,
                latest_event=latest_event,
                state=state,
                specialty_mode=target_organ_id == organ.id,
                target_organ_id=target_organ_id,
            )
            history.append(organ_message)
    finally:
        await manager.broadcast(session_id, {"type": "organ_round_complete"})


async def stream_organ_message(
    *,
    session_id: str,
    organ: Organ,
    history: list[dict],
    latest_event: str,
    state: dict,
    specialty_mode: bool,
    target_organ_id: str | None,
) -> dict:
    temp_id = f"stream-{uuid.uuid4().hex}"
    pending_message = {
        "id": temp_id,
        "sessionId": session_id,
        "senderId": organ.id,
        "senderName": organ.name,
        "role": "organ",
        "kind": "chat",
        "content": "",
        "color": organ.color,
        "avatar": organ.avatar,
        "metadata": {
            "streaming": True,
            "specialtyMode": specialty_mode,
            "interjection": bool(target_organ_id and target_organ_id != organ.id),
        },
        "createdAt": "",
    }
    await manager.broadcast(
        session_id,
        {"type": "message_start", "tempId": temp_id, "message": pending_message},
    )

    content_parts = []
    async for chunk in ai_client.stream_reply(
        session_id=session_id,
        organ=organ,
        history=history,
        latest_event=latest_event,
        state=state,
        specialty_mode=specialty_mode,
    ):
        content_parts.append(chunk)
        await manager.broadcast(
            session_id,
            {"type": "message_chunk", "tempId": temp_id, "chunk": chunk},
        )

    final_content = "".join(content_parts).strip() or "..."
    organ_message = await asyncio.to_thread(
        store.add_message,
        session_id=session_id,
        sender_id=organ.id,
        sender_name=organ.name,
        role="organ",
        content=final_content,
        color=organ.color,
        avatar=organ.avatar,
        metadata={
            "specialtyMode": specialty_mode,
            "interjection": bool(target_organ_id and target_organ_id != organ.id),
        },
    )

    achievements = update_relationship_from_message(state, organ.id, final_content)
    await save_state(session_id, state)
    await manager.broadcast(
        session_id,
        {"type": "message_final", "tempId": temp_id, "message": organ_message},
    )
    await broadcast_state(session_id, state)
    await broadcast_achievements(session_id, state, achievements)
    return organ_message


async def add_report_card(*, session_id: str, state: dict) -> None:
    report = build_report_card(state)
    message = await asyncio.to_thread(
        store.add_message,
        session_id=session_id,
        sender_id="report",
        sender_name="Body Report Card",
        role="report",
        kind="report_card",
        content=report["title"],
        color="#f59e0b",
        avatar="R",
        metadata=report,
    )
    await manager.broadcast(session_id, {"type": "message", "message": message})


def build_export_html(*, session_id: str, messages: list[dict], state: dict) -> str:
    rows = []
    for message in messages:
        safe_sender = html.escape(message["senderName"])
        safe_content = html.escape(message["content"])
        color = html.escape(message.get("color") or "#888")
        created_at = html.escape(message.get("createdAt") or "")
        if message.get("kind") == "report_card":
            report_rows = []
            for row in message.get("metadata", {}).get("rows", []):
                report_rows.append(
                    "<li>"
                    f"<strong>{html.escape(row.get('organName', 'Organ'))}: {html.escape(row.get('grade', '?'))}</strong> "
                    f"{html.escape(row.get('domain', ''))}. {html.escape(row.get('comment', ''))}"
                    "</li>"
                )
            body = f"<ul>{''.join(report_rows)}</ul>"
        else:
            body = f"<p>{safe_content}</p>"
        rows.append(
            "<article class='message'>"
            f"<div class='avatar' style='background:{color}'>{html.escape(message.get('avatar') or '')}</div>"
            "<div>"
            f"<div class='meta'><strong>{safe_sender}</strong><span>{created_at}</span></div>"
            f"<div class='bubble' style='border-color:{color}'>{body}</div>"
            "</div>"
            "</article>"
        )

    weather = public_state(state)["weather"]["label"]
    return f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Organiverse Export</title>
  <style>
    body {{ margin: 0; background: #101218; color: #f7efe3; font-family: Inter, Segoe UI, sans-serif; }}
    main {{ max-width: 900px; margin: 0 auto; padding: 32px 18px; }}
    header {{ border-bottom: 1px solid #2b3140; margin-bottom: 24px; padding-bottom: 18px; }}
    .message {{ display: flex; gap: 12px; margin: 16px 0; }}
    .avatar {{ width: 38px; height: 38px; border-radius: 50%; display: grid; place-items: center; color: white; font-weight: 800; }}
    .meta {{ display: flex; gap: 10px; color: #aeb6c8; font-size: 12px; margin-bottom: 5px; }}
    .bubble {{ background: #171b24; border: 1px solid; border-radius: 14px; padding: 12px 14px; line-height: 1.55; }}
    .bubble p {{ margin: 0; }}
  </style>
</head>
<body>
  <main>
    <header>
      <h1>Organiverse Chat Export</h1>
      <p>Session {html.escape(session_id)}. Body weather: {html.escape(weather)}.</p>
    </header>
    {''.join(rows)}
  </main>
</body>
</html>"""
