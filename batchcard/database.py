"""
Batchcard — ALCOA++ compliant SQLite persistence layer.
Uses aiosqlite for async operations.
"""

import json
import hashlib
import os
from datetime import datetime, timezone
from typing import Any, Optional

import aiosqlite

DB_PATH = os.getenv("BATCHCARD_DB", "batchcard.db")
SECRET_SALT = os.getenv("BATCHCARD_SALT", "batchcard-local-salt-CHANGE-IN-PROD")


# ── Schema ─────────────────────────────────────────────────────────────────────

SCHEMA = """
CREATE TABLE IF NOT EXISTS batches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_number TEXT UNIQUE NOT NULL,
    product_name TEXT NOT NULL,
    lot_number TEXT NOT NULL,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL,
    current_stage TEXT NOT NULL DEFAULT 'created',
    status TEXT NOT NULL DEFAULT 'active',
    created_by TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stage_definitions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    sequence_order INTEGER NOT NULL,
    description TEXT,
    required_fields TEXT -- JSON array of {"name","type","unit","min","max"}
);

CREATE TABLE IF NOT EXISTS batch_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL REFERENCES batches(id),
    stage_name TEXT NOT NULL,
    field_name TEXT NOT NULL,
    value TEXT NOT NULL,
    unit TEXT,
    notes TEXT,
    operator_name TEXT NOT NULL,
    signature_hash TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correction_of INTEGER REFERENCES batch_entries(id),
    UNIQUE(batch_id, stage_name, field_name, correction_of)
);

CREATE TABLE IF NOT EXISTS signatures (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL REFERENCES batches(id),
    stage_name TEXT NOT NULL,
    operator_name TEXT NOT NULL,
    signature_hash TEXT NOT NULL,
    signed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address TEXT,
    action TEXT NOT NULL DEFAULT 'stage_completion'
);

CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    record_id INTEGER,
    action TEXT NOT NULL,
    old_values TEXT,
    new_values TEXT,
    performed_by TEXT NOT NULL,
    performed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address TEXT
);
"""

DEFAULT_STAGES = [
    {
        "name": "receiving",
        "sequence_order": 10,
        "description": "Raw material receiving and weighing",
        "required_fields": json.dumps([
            {"name": "gross_weight", "type": "number", "unit": "kg"},
            {"name": "tare_weight", "type": "number", "unit": "kg"},
            {"name": "net_weight", "type": "number", "unit": "kg"},
            {"name": "visual_inspection", "type": "select", "options": ["pass", "fail"]},
        ]),
    },
    {
        "name": "drying",
        "sequence_order": 20,
        "description": "Drying and curing process",
        "required_fields": json.dumps([
            {"name": "temperature", "type": "number", "unit": "°C", "min": 15, "max": 30},
            {"name": "humidity", "type": "number", "unit": "%RH", "min": 30, "max": 70},
            {"name": "duration", "type": "number", "unit": "hours"},
        ]),
    },
    {
        "name": "processing",
        "sequence_order": 30,
        "description": "Processing / extraction / milling",
        "required_fields": json.dumps([
            {"name": "method", "type": "text"},
            {"name": "yield_weight", "type": "number", "unit": "kg"},
        ]),
    },
    {
        "name": "packaging",
        "sequence_order": 40,
        "description": "Packaging and labelling",
        "required_fields": json.dumps([
            {"name": "package_count", "type": "integer"},
            {"name": "package_weight", "type": "number", "unit": "g"},
            {"name": "label_verified", "type": "select", "options": ["yes", "no"]},
        ]),
    },
    {
        "name": "qc_testing",
        "sequence_order": 50,
        "description": "Quality control and laboratory testing",
        "required_fields": json.dumps([
            {"name": "thc_potency", "type": "number", "unit": "%"},
            {"name": "cbd_potency", "type": "number", "unit": "%"},
            {"name": "moisture_content", "type": "number", "unit": "%"},
            {"name": "microbial_pass", "type": "select", "options": ["pass", "fail"]},
        ]),
    },
    {
        "name": "final_review",
        "sequence_order": 60,
        "description": "Final review, approval, and release",
        "required_fields": json.dumps([
            {"name": "review_notes", "type": "text"},
            {"name": "release_decision", "type": "select", "options": ["approved", "rejected", "quarantined"]},
        ]),
    },
]


# ── Helpers ────────────────────────────────────────────────────────────────────

def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def hash_signature(operator_name: str, batch_id: int, stage_name: str, timestamp: str) -> str:
    """GMP-style digital signature hash."""
    payload = f"{operator_name}:{batch_id}:{stage_name}:{timestamp}:{SECRET_SALT}"
    return hashlib.sha256(payload.encode()).hexdigest()


def validate_stage_sequence(current: str, target: str, stages: list[dict]) -> bool:
    """Ensure target stage comes after current stage in the defined sequence."""
    seq = {s["name"]: s["sequence_order"] for s in stages}
    if current not in seq:
        return target in seq  # fresh batch can go to any stage
    return seq.get(target, 999) >= seq.get(current, 0)


# ── Init ───────────────────────────────────────────────────────────────────────

async def init_db() -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.executescript(SCHEMA)
        # Seed stage definitions if empty
        cursor = await db.execute("SELECT COUNT(*) FROM stage_definitions")
        row = await cursor.fetchone()
        if row and row[0] == 0:
            for s in DEFAULT_STAGES:
                await db.execute(
                    "INSERT INTO stage_definitions (name, sequence_order, description, required_fields) VALUES (?,?,?,?)",
                    (s["name"], s["sequence_order"], s["description"], s["required_fields"]),
                )
        await db.commit()


# ── Audit logging ──────────────────────────────────────────────────────────────

async def log_audit(
    db: aiosqlite.Connection,
    *,
    table_name: str,
    record_id: Optional[int],
    action: str,
    old: Optional[dict] = None,
    new: Optional[dict] = None,
    performed_by: str = "system",
    ip_address: Optional[str] = None,
) -> None:
    await db.execute(
        "INSERT INTO audit_log (table_name, record_id, action, old_values, new_values, performed_by, ip_address) VALUES (?,?,?,?,?,?,?)",
        (table_name, record_id, action, json.dumps(old) if old else None, json.dumps(new) if new else None, performed_by, ip_address),
    )
    await db.commit()


# ── Batches ────────────────────────────────────────────────────────────────────

async def create_batch(
    db: aiosqlite.Connection,
    *,
    batch_number: str,
    product_name: str,
    lot_number: str,
    quantity: float,
    unit: str,
    created_by: str,
    ip_address: Optional[str] = None,
) -> dict[str, Any]:
    cursor = await db.execute(
        "INSERT INTO batches (batch_number, product_name, lot_number, quantity, unit, created_by) VALUES (?,?,?,?,?,?)",
        (batch_number, product_name, lot_number, quantity, unit, created_by),
    )
    await db.commit()
    batch_id = cursor.lastrowid
    await log_audit(
        db, table_name="batches", record_id=batch_id, action="INSERT",
        new={"batch_number": batch_number, "product_name": product_name},
        performed_by=created_by, ip_address=ip_address,
    )
    return await get_batch(db, batch_id)


async def get_batch(db: aiosqlite.Connection, batch_id: int) -> Optional[dict[str, Any]]:
    async with db.execute(
        "SELECT id, batch_number, product_name, lot_number, quantity, unit, current_stage, status, created_by, created_at, updated_at FROM batches WHERE id=?",
        (batch_id,),
    ) as cursor:
        row = await cursor.fetchone()
    if not row:
        return None
    return {
        "id": row[0], "batch_number": row[1], "product_name": row[2], "lot_number": row[3],
        "quantity": row[4], "unit": row[5], "current_stage": row[6], "status": row[7],
        "created_by": row[8], "created_at": row[9], "updated_at": row[10],
    }


async def list_batches(db: aiosqlite.Connection, status: Optional[str] = None) -> list[dict[str, Any]]:
    sql = "SELECT id, batch_number, product_name, lot_number, quantity, unit, current_stage, status, created_by, created_at FROM batches"
    params: tuple = ()
    if status:
        sql += " WHERE status=?"
        params = (status,)
    sql += " ORDER BY created_at DESC"
    async with db.execute(sql, params) as cursor:
        rows = await cursor.fetchall()
    return [
        {
            "id": r[0], "batch_number": r[1], "product_name": r[2], "lot_number": r[3],
            "quantity": r[4], "unit": r[5], "current_stage": r[6], "status": r[7],
            "created_by": r[8], "created_at": r[9],
        }
        for r in rows
    ]


async def update_batch_status(
    db: aiosqlite.Connection,
    *,
    batch_id: int,
    status: str,
    performed_by: str,
    ip_address: Optional[str] = None,
) -> Optional[dict[str, Any]]:
    old = await get_batch(db, batch_id)
    if not old:
        return None
    await db.execute(
        "UPDATE batches SET status=?, updated_at=? WHERE id=?",
        (status, utc_now(), batch_id),
    )
    await db.commit()
    await log_audit(
        db, table_name="batches", record_id=batch_id, action="UPDATE",
        old={"status": old["status"]}, new={"status": status},
        performed_by=performed_by, ip_address=ip_address,
    )
    return await get_batch(db, batch_id)


# ── Entries ────────────────────────────────────────────────────────────────────

async def add_entry(
    db: aiosqlite.Connection,
    *,
    batch_id: int,
    stage_name: str,
    field_name: str,
    value: str,
    unit: Optional[str] = None,
    notes: Optional[str] = None,
    operator_name: str,
    ip_address: Optional[str] = None,
    correction_of: Optional[int] = None,
) -> dict[str, Any]:
    ts = utc_now()
    sig = hash_signature(operator_name, batch_id, stage_name, ts)

    # Check for existing uncorrected entry on same field
    if correction_of is None:
        async with db.execute(
            "SELECT id FROM batch_entries WHERE batch_id=? AND stage_name=? AND field_name=? AND correction_of IS NULL",
            (batch_id, stage_name, field_name),
        ) as cur:
            existing = await cur.fetchone()
        if existing:
            raise ValueError(f"Entry already exists for {stage_name}/{field_name}. Use correction_of={existing[0]} to amend.")

    cursor = await db.execute(
        "INSERT INTO batch_entries (batch_id, stage_name, field_name, value, unit, notes, operator_name, signature_hash, correction_of) VALUES (?,?,?,?,?,?,?,?,?)",
        (batch_id, stage_name, field_name, value, unit, notes, operator_name, sig, correction_of),
    )
    await db.commit()
    entry_id = cursor.lastrowid

    # Update batch current_stage if this stage is later in sequence
    batch = await get_batch(db, batch_id)
    stages = await list_stages(db)
    if validate_stage_sequence(batch["current_stage"], stage_name, stages):
        await db.execute("UPDATE batches SET current_stage=?, updated_at=? WHERE id=?", (stage_name, ts, batch_id))
        await db.commit()

    await log_audit(
        db, table_name="batch_entries", record_id=entry_id, action="INSERT",
        new={"batch_id": batch_id, "stage_name": stage_name, "field_name": field_name, "value": value},
        performed_by=operator_name, ip_address=ip_address,
    )
    return await get_entry(db, entry_id)


async def get_entry(db: aiosqlite.Connection, entry_id: int) -> Optional[dict[str, Any]]:
    async with db.execute(
        "SELECT id, batch_id, stage_name, field_name, value, unit, notes, operator_name, signature_hash, created_at, correction_of FROM batch_entries WHERE id=?",
        (entry_id,),
    ) as cursor:
        row = await cursor.fetchone()
    if not row:
        return None
    return {
        "id": row[0], "batch_id": row[1], "stage_name": row[2], "field_name": row[3],
        "value": row[4], "unit": row[5], "notes": row[6], "operator_name": row[7],
        "signature_hash": row[8], "created_at": row[9], "correction_of": row[10],
    }


async def list_entries(db: aiosqlite.Connection, batch_id: int) -> list[dict[str, Any]]:
    async with db.execute(
        "SELECT id, batch_id, stage_name, field_name, value, unit, notes, operator_name, signature_hash, created_at, correction_of FROM batch_entries WHERE batch_id=? ORDER BY created_at DESC",
        (batch_id,),
    ) as cursor:
        rows = await cursor.fetchall()
    return [
        {
            "id": r[0], "batch_id": r[1], "stage_name": r[2], "field_name": r[3],
            "value": r[4], "unit": r[5], "notes": r[6], "operator_name": r[7],
            "signature_hash": r[8], "created_at": r[9], "correction_of": r[10],
        }
        for r in rows
    ]


# ── Stage definitions ─────────────────────────────────────────────────────────

async def list_stages(db: aiosqlite.Connection) -> list[dict[str, Any]]:
    async with db.execute(
        "SELECT id, name, sequence_order, description, required_fields FROM stage_definitions ORDER BY sequence_order"
    ) as cursor:
        rows = await cursor.fetchall()
    return [
        {
            "id": r[0], "name": r[1], "sequence_order": r[2],
            "description": r[3], "required_fields": json.loads(r[4]) if r[4] else [],
        }
        for r in rows
    ]


# ── Signatures ─────────────────────────────────────────────────────────────────

async def add_signature(
    db: aiosqlite.Connection,
    *,
    batch_id: int,
    stage_name: str,
    operator_name: str,
    ip_address: Optional[str] = None,
    action: str = "stage_completion",
) -> dict[str, Any]:
    ts = utc_now()
    sig = hash_signature(operator_name, batch_id, stage_name, ts)
    cursor = await db.execute(
        "INSERT INTO signatures (batch_id, stage_name, operator_name, signature_hash, signed_at, ip_address, action) VALUES (?,?,?,?,?,?,?)",
        (batch_id, stage_name, operator_name, sig, ts, ip_address, action),
    )
    await db.commit()
    sig_id = cursor.lastrowid
    await log_audit(
        db, table_name="signatures", record_id=sig_id, action="INSERT",
        new={"batch_id": batch_id, "stage_name": stage_name, "operator_name": operator_name},
        performed_by=operator_name, ip_address=ip_address,
    )
    return {
        "id": sig_id, "batch_id": batch_id, "stage_name": stage_name,
        "operator_name": operator_name, "signature_hash": sig,
        "signed_at": ts, "ip_address": ip_address, "action": action,
    }


async def list_signatures(db: aiosqlite.Connection, batch_id: int) -> list[dict[str, Any]]:
    async with db.execute(
        "SELECT id, batch_id, stage_name, operator_name, signature_hash, signed_at, ip_address, action FROM signatures WHERE batch_id=? ORDER BY signed_at DESC",
        (batch_id,),
    ) as cursor:
        rows = await cursor.fetchall()
    return [
        {
            "id": r[0], "batch_id": r[1], "stage_name": r[2], "operator_name": r[3],
            "signature_hash": r[4], "signed_at": r[5], "ip_address": r[6], "action": r[7],
        }
        for r in rows
    ]


# ── Audit ──────────────────────────────────────────────────────────────────────

async def list_audit(db: aiosqlite.Connection, batch_id: Optional[int] = None) -> list[dict[str, Any]]:
    sql = "SELECT id, table_name, record_id, action, old_values, new_values, performed_by, performed_at, ip_address FROM audit_log"
    params: tuple = ()
    if batch_id:
        sql += " WHERE record_id=? AND table_name='batch_entries'"
        params = (batch_id,)
    sql += " ORDER BY performed_at DESC LIMIT 500"
    async with db.execute(sql, params) as cursor:
        rows = await cursor.fetchall()
    return [
        {
            "id": r[0], "table_name": r[1], "record_id": r[2], "action": r[3],
            "old_values": json.loads(r[4]) if r[4] else None,
            "new_values": json.loads(r[5]) if r[5] else None,
            "performed_by": r[6], "performed_at": r[7], "ip_address": r[8],
        }
        for r in rows
    ]
