"""
Tool: database

Demonstrates how to expose database access as MCP tools.

Patterns demonstrated:
- Parameterized queries (SQL injection prevention)
- Read-only mode enforcement
- Structured dict/list return types
- Input validation before hitting the DB
- Listing available tables (useful for AI context)

The bundled data.db uses SQLite so there's zero setup.
To connect a real database, swap the connection logic in _get_connection()
and point DB_PATH at a connection string or file path.
"""

import sqlite3
from pathlib import Path
from mcp.server.fastmcp import FastMCP

from src.config import settings

# Tables the query tool is allowed to read from.
# Add your own tables here. Remove this allowlist to permit all tables.
ALLOWED_TABLES = {"products", "orders", "inventory_log"}

# Columns that are never returned (e.g. password hashes, internal flags)
BLOCKED_COLUMNS: dict[str, set[str]] = {
    # "users": {"password_hash", "reset_token"},
}


def _get_connection() -> sqlite3.Connection:
    db_path = Path(settings.db_path)
    if not db_path.exists():
        raise FileNotFoundError(
            f"Database not found at {db_path}. "
            "Run: python scripts/seed_db.py"
        )
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row  # Returns dict-like rows
    return conn


def _validate_table(table: str) -> None:
    if table not in ALLOWED_TABLES:
        raise ValueError(
            f"Table '{table}' is not accessible. "
            f"Available tables: {sorted(ALLOWED_TABLES)}"
        )


def _validate_column(table: str, column: str) -> None:
    blocked = BLOCKED_COLUMNS.get(table, set())
    if column in blocked:
        raise ValueError(f"Column '{column}' is not accessible on table '{table}'")


def register(mcp: FastMCP) -> None:
    """Register database tools with the MCP server."""

    @mcp.tool()
    def list_tables() -> list[str]:
        """
        List all available database tables.

        Call this first to understand what data is available
        before querying specific tables.

        Returns:
            List of accessible table names.
        """
        return sorted(ALLOWED_TABLES)

    @mcp.tool()
    def describe_table(table: str) -> list[dict]:
        """
        Return the column names and types for a table.

        Use this to understand a table's structure before querying it.

        Args:
            table: Table name (use list_tables() to see options).

        Returns:
            List of dicts with 'column', 'type', 'nullable' keys.
        """
        _validate_table(table)
        conn = _get_connection()
        try:
            cursor = conn.execute(f"PRAGMA table_info({table})")
            rows = cursor.fetchall()
            return [
                {
                    "column": row["name"],
                    "type": row["type"],
                    "nullable": not row["notnull"],
                    "primary_key": bool(row["pk"]),
                }
                for row in rows
                if row["name"] not in BLOCKED_COLUMNS.get(table, set())
            ]
        finally:
            conn.close()

    @mcp.tool()
    def query_table(
        table: str,
        filters: dict | None = None,
        columns: list[str] | None = None,
        limit: int = 20,
        order_by: str | None = None,
        order_dir: str = "ASC",
    ) -> list[dict]:
        """
        Query a database table with optional filters, column selection,
        ordering, and row limit.

        Args:
            table: Table name to query.
            filters: Optional dict of {column: value} equality filters.
                     e.g. {"status": "pending", "category": "hardware"}
            columns: Optional list of column names to return.
                     Returns all columns if not specified.
            limit: Max rows to return (default 20, max 100).
            order_by: Column name to sort by.
            order_dir: Sort direction — "ASC" or "DESC".

        Returns:
            List of row dicts matching the query.

        Example:
            query_table("orders", filters={"status": "pending"}, limit=5)
        """
        _validate_table(table)
        limit = min(limit, 100)  # Hard cap

        if order_dir.upper() not in ("ASC", "DESC"):
            raise ValueError("order_dir must be 'ASC' or 'DESC'")

        # Build SELECT clause
        if columns:
            for col in columns:
                _validate_column(table, col)
            # Sanitize column names — only allow alphanumeric + underscore
            safe_cols = [c for c in columns if c.replace("_", "").isalnum()]
            select_clause = ", ".join(safe_cols)
        else:
            select_clause = "*"

        # Build WHERE clause
        params: list = []
        where_clause = ""
        if filters:
            conditions = []
            for col, val in filters.items():
                _validate_column(table, col)
                if not col.replace("_", "").isalnum():
                    raise ValueError(f"Invalid column name: {col}")
                conditions.append(f"{col} = ?")
                params.append(val)
            where_clause = "WHERE " + " AND ".join(conditions)

        # Build ORDER BY clause
        order_clause = ""
        if order_by:
            if not order_by.replace("_", "").isalnum():
                raise ValueError(f"Invalid order_by column: {order_by}")
            order_clause = f"ORDER BY {order_by} {order_dir.upper()}"

        sql = f"SELECT {select_clause} FROM {table} {where_clause} {order_clause} LIMIT ?"
        params.append(limit)

        conn = _get_connection()
        try:
            cursor = conn.execute(sql, params)
            rows = cursor.fetchall()
            return [dict(row) for row in rows]
        finally:
            conn.close()

    @mcp.tool()
    def count_rows(table: str, filters: dict | None = None) -> dict:
        """
        Count rows in a table, optionally with filters.

        Args:
            table: Table name.
            filters: Optional dict of {column: value} equality filters.

        Returns:
            Dict with 'table', 'filters', and 'count' keys.
        """
        _validate_table(table)

        params: list = []
        where_clause = ""
        if filters:
            conditions = []
            for col, val in filters.items():
                _validate_column(table, col)
                if not col.replace("_", "").isalnum():
                    raise ValueError(f"Invalid column name: {col}")
                conditions.append(f"{col} = ?")
                params.append(val)
            where_clause = "WHERE " + " AND ".join(conditions)

        sql = f"SELECT COUNT(*) as count FROM {table} {where_clause}"

        conn = _get_connection()
        try:
            cursor = conn.execute(sql, params)
            result = cursor.fetchone()
            return {
                "table": table,
                "filters": filters or {},
                "count": result["count"],
            }
        finally:
            conn.close()
