"""
Batchcard — FastAPI ALCOA++ batch tracking MVP.
"""

from contextlib import asynccontextmanager
from typing import Optional

import aiosqlite
from fastapi import FastAPI, Request, HTTPException, Depends
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, JSONResponse, PlainTextResponse

import database as db
from models import (
    BatchCreate, BatchOut, BatchStatusUpdate,
    EntryCreate, EntryOut,
    SignatureCreate, SignatureOut,
    StageOut, AuditOut, BatchReport,
)


# ── DB dependency ──────────────────────────────────────────────────────────────

async def get_db():
    async with aiosqlite.connect(db.DB_PATH) as conn:
        conn.row_factory = aiosqlite.Row
        yield conn


# ── Lifespan ───────────────────────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    await db.init_db()
    yield


app = FastAPI(title="Batchcard", version="0.1.0", lifespan=lifespan)
app.mount("/static", StaticFiles(directory="static"), name="static")


def _ip(request: Request) -> Optional[str]:
    return request.client.host if request.client else None


# ── Batches ────────────────────────────────────────────────────────────────────

@app.post("/api/batches", response_model=BatchOut)
async def create_batch(data: BatchCreate, request: Request, conn=Depends(get_db)):
    try:
        return await db.create_batch(
            conn, batch_number=data.batch_number, product_name=data.product_name,
            lot_number=data.lot_number, quantity=data.quantity, unit=data.unit,
            created_by=data.created_by, ip_address=_ip(request),
        )
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@app.get("/api/batches", response_model=list[BatchOut])
async def list_batches(status: Optional[str] = None, conn=Depends(get_db)):
    return await db.list_batches(conn, status=status)


@app.get("/api/batches/{batch_id}", response_model=BatchOut)
async def get_batch(batch_id: int, conn=Depends(get_db)):
    b = await db.get_batch(conn, batch_id)
    if not b:
        raise HTTPException(status_code=404, detail="Batch not found")
    return b


@app.patch("/api/batches/{batch_id}/status", response_model=BatchOut)
async def update_status(batch_id: int, data: BatchStatusUpdate, request: Request, conn=Depends(get_db)):
    b = await db.update_batch_status(
        conn, batch_id=batch_id, status=data.status,
        performed_by=data.performed_by, ip_address=_ip(request),
    )
    if not b:
        raise HTTPException(status_code=404, detail="Batch not found")
    return b


# ── Entries ────────────────────────────────────────────────────────────────────

@app.post("/api/batches/{batch_id}/entries", response_model=EntryOut)
async def add_entry(batch_id: int, data: EntryCreate, request: Request, conn=Depends(get_db)):
    batch = await db.get_batch(conn, batch_id)
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    if batch["status"] not in ("active",):
        raise HTTPException(status_code=400, detail=f"Cannot modify batch with status '{batch['status']}'")
    try:
        return await db.add_entry(
            conn, batch_id=batch_id, stage_name=data.stage_name,
            field_name=data.field_name, value=data.value,
            unit=data.unit, notes=data.notes,
            operator_name=data.operator_name, ip_address=_ip(request),
            correction_of=data.correction_of,
        )
    except ValueError as exc:
        raise HTTPException(status_code=409, detail=str(exc))


@app.get("/api/batches/{batch_id}/entries", response_model=list[EntryOut])
async def list_entries(batch_id: int, conn=Depends(get_db)):
    return await db.list_entries(conn, batch_id)


# ── Signatures ─────────────────────────────────────────────────────────────────

@app.post("/api/batches/{batch_id}/signatures", response_model=SignatureOut)
async def add_signature(batch_id: int, data: SignatureCreate, request: Request, conn=Depends(get_db)):
    batch = await db.get_batch(conn, batch_id)
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    return await db.add_signature(
        conn, batch_id=batch_id, stage_name=data.stage_name,
        operator_name=data.operator_name, ip_address=_ip(request), action=data.action,
    )


@app.get("/api/batches/{batch_id}/signatures", response_model=list[SignatureOut])
async def list_signatures(batch_id: int, conn=Depends(get_db)):
    return await db.list_signatures(conn, batch_id)


# ── Stages ─────────────────────────────────────────────────────────────────────

@app.get("/api/stages", response_model=list[StageOut])
async def list_stages(conn=Depends(get_db)):
    return await db.list_stages(conn)


# ── Audit ──────────────────────────────────────────────────────────────────────

@app.get("/api/audit", response_model=list[AuditOut])
async def list_audit(conn=Depends(get_db)):
    return await db.list_audit(conn)


@app.get("/api/batches/{batch_id}/audit", response_model=list[AuditOut])
async def batch_audit(batch_id: int, conn=Depends(get_db)):
    return await db.list_audit(conn, batch_id=batch_id)


# ── Reports ───────────────────────────────────────────────────────────────────

@app.get("/api/batches/{batch_id}/report", response_model=BatchReport)
async def batch_report(batch_id: int, conn=Depends(get_db)):
    batch = await db.get_batch(conn, batch_id)
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    entries = await db.list_entries(conn, batch_id)
    sigs = await db.list_signatures(conn, batch_id)
    audit = await db.list_audit(conn, batch_id)
    return {"batch": batch, "entries": entries, "signatures": sigs, "audit": audit}


@app.get("/api/batches/{batch_id}/export.csv")
async def export_csv(batch_id: int, conn=Depends(get_db)):
    import csv, io
    batch = await db.get_batch(conn, batch_id)
    if not batch:
        raise HTTPException(status_code=404, detail="Batch not found")
    entries = await db.list_entries(conn, batch_id)
    sigs = await db.list_signatures(conn, batch_id)

    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["Batchcard Export", "ALCOA++ Compliant"])
    writer.writerow([])
    writer.writerow(["Batch Number", batch["batch_number"]])
    writer.writerow(["Product", batch["product_name"]])
    writer.writerow(["Lot", batch["lot_number"]])
    writer.writerow(["Quantity", f"{batch['quantity']} {batch['unit']}"])
    writer.writerow(["Status", batch["status"]])
    writer.writerow(["Current Stage", batch["current_stage"]])
    writer.writerow(["Created By", batch["created_by"]])
    writer.writerow(["Created At", batch["created_at"]])
    writer.writerow([])
    writer.writerow(["Stage", "Field", "Value", "Unit", "Operator", "Timestamp", "Signature Hash"])
    for e in entries:
        writer.writerow([e["stage_name"], e["field_name"], e["value"], e.get("unit") or "", e["operator_name"], e["created_at"], e["signature_hash"]])
    writer.writerow([])
    writer.writerow(["Stage", "Operator", "Action", "Signed At", "Signature Hash"])
    for s in sigs:
        writer.writerow([s["stage_name"], s["operator_name"], s["action"], s["signed_at"], s["signature_hash"]])

    return PlainTextResponse(
        content=output.getvalue(),
        media_type="text/csv",
        headers={"Content-Disposition": f"attachment; filename={batch['batch_number']}_report.csv"},
    )


# ── SPA catch-all ─────────────────────────────────────────────────────────────

@app.get("/")
async def index():
    return FileResponse("static/index.html")


@app.get("/health")
async def health():
    return {"status": "ok"}
