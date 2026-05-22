"""
Batchcard — Pydantic v2 request/response models.
"""

from typing import Optional
from pydantic import BaseModel, Field


# ── Batches ────────────────────────────────────────────────────────────────────

class BatchCreate(BaseModel):
    batch_number: str = Field(..., min_length=3, max_length=64)
    product_name: str = Field(..., min_length=1, max_length=128)
    lot_number: str = Field(..., min_length=1, max_length=64)
    quantity: float = Field(..., gt=0)
    unit: str = Field(..., min_length=1, max_length=32)
    created_by: str = Field(..., min_length=1, max_length=128)


class BatchOut(BaseModel):
    id: int
    batch_number: str
    product_name: str
    lot_number: str
    quantity: float
    unit: str
    current_stage: str
    status: str
    created_by: str
    created_at: str
    updated_at: str


class BatchStatusUpdate(BaseModel):
    status: str = Field(..., pattern="^(active|completed|quarantined|rejected)$")
    performed_by: str = Field(..., min_length=1, max_length=128)


# ── Entries ────────────────────────────────────────────────────────────────────

class EntryCreate(BaseModel):
    stage_name: str = Field(..., min_length=1, max_length=64)
    field_name: str = Field(..., min_length=1, max_length=64)
    value: str = Field(..., min_length=1, max_length=512)
    unit: Optional[str] = Field(default=None, max_length=32)
    notes: Optional[str] = Field(default=None, max_length=1024)
    operator_name: str = Field(..., min_length=1, max_length=128)
    correction_of: Optional[int] = None


class EntryOut(BaseModel):
    id: int
    batch_id: int
    stage_name: str
    field_name: str
    value: str
    unit: Optional[str]
    notes: Optional[str]
    operator_name: str
    signature_hash: str
    created_at: str
    correction_of: Optional[int]


# ── Signatures ─────────────────────────────────────────────────────────────────

class SignatureCreate(BaseModel):
    stage_name: str = Field(..., min_length=1, max_length=64)
    operator_name: str = Field(..., min_length=1, max_length=128)
    action: str = Field(default="stage_completion")


class SignatureOut(BaseModel):
    id: int
    batch_id: int
    stage_name: str
    operator_name: str
    signature_hash: str
    signed_at: str
    ip_address: Optional[str]
    action: str


# ── Stages ─────────────────────────────────────────────────────────────────────

class StageOut(BaseModel):
    id: int
    name: str
    sequence_order: int
    description: Optional[str]
    required_fields: list[dict]


# ── Audit ──────────────────────────────────────────────────────────────────────

class AuditOut(BaseModel):
    id: int
    table_name: str
    record_id: Optional[int]
    action: str
    old_values: Optional[dict]
    new_values: Optional[dict]
    performed_by: str
    performed_at: str
    ip_address: Optional[str]


# ── Reports ───────────────────────────────────────────────────────────────────

class BatchReport(BaseModel):
    batch: BatchOut
    entries: list[EntryOut]
    signatures: list[SignatureOut]
    audit: list[AuditOut]
