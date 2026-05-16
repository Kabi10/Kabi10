"""
Shared pytest fixtures.

Key design decisions:
- Tools are sync functions, so call_tool() calls tool.fn() directly (no asyncio)
- Settings are patched directly on the imported module-level objects so
  tools pick up temp DB/workspace paths at test time
- Each test gets a fresh MCP server instance via the mcp_server fixture
"""

import sqlite3
from pathlib import Path

import pytest
from mcp.server.fastmcp import FastMCP


def call_tool(server: FastMCP, tool_name: str, **kwargs):
    """Call a registered tool by name and return its result directly."""
    tool = server._tool_manager._tools.get(tool_name)
    assert tool is not None, f"Tool '{tool_name}' not registered on server"
    return tool.fn(**kwargs)


@pytest.fixture(scope="session")
def temp_db(tmp_path_factory) -> Path:
    """Create a temporary SQLite database seeded with sample data."""
    db_dir = tmp_path_factory.mktemp("data")
    db_path = db_dir / "test.db"

    conn = sqlite3.connect(db_path)
    c = conn.cursor()
    c.executescript("""
        CREATE TABLE products (
            id       INTEGER PRIMARY KEY,
            name     TEXT    NOT NULL,
            category TEXT    NOT NULL,
            price    REAL    NOT NULL,
            stock    INTEGER NOT NULL,
            sku      TEXT    UNIQUE NOT NULL
        );
        CREATE TABLE orders (
            id         INTEGER PRIMARY KEY,
            product_id INTEGER,
            quantity   INTEGER NOT NULL,
            status     TEXT    NOT NULL,
            created_at TEXT    NOT NULL
        );
        CREATE TABLE inventory_log (
            id         INTEGER PRIMARY KEY,
            product_id INTEGER,
            change     INTEGER NOT NULL,
            reason     TEXT    NOT NULL,
            logged_at  TEXT    NOT NULL
        );
    """)
    c.executemany("INSERT INTO products VALUES (?,?,?,?,?,?)", [
        (1, "Widget A", "hardware", 9.99,  150, "WGT-001"),
        (2, "Widget B", "hardware", 14.99,  80, "WGT-002"),
        (3, "Pro Sub",  "software", 99.99, 999, "SUB-PRO"),
    ])
    c.executemany("INSERT INTO orders VALUES (?,?,?,?,?)", [
        (1, 1, 5, "completed", "2024-01-10T09:00:00"),
        (2, 2, 2, "pending",   "2024-01-11T10:30:00"),
        (3, 3, 1, "pending",   "2024-01-12T14:00:00"),
    ])
    conn.commit()
    conn.close()
    return db_path


@pytest.fixture(scope="session")
def temp_workspace(tmp_path_factory) -> Path:
    """Create a temporary workspace directory pre-seeded with sample files."""
    workspace = tmp_path_factory.mktemp("workspace")
    (workspace / "hello.txt").write_text("Hello from the workspace!")
    (workspace / "notes.md").write_text("# Notes\n\nSome content here.")
    subdir = workspace / "reports"
    subdir.mkdir()
    (subdir / "report.txt").write_text("Report data.")
    return workspace


@pytest.fixture
def mcp_server(temp_db, temp_workspace) -> FastMCP:
    """
    Fresh MCP server with all tools registered, config pointed at temp paths.
    Patches settings directly on the imported module-level objects.
    """
    import src.tools.database as db_module
    import src.tools.filesystem as fs_module

    db_module.settings.db_path = str(temp_db)
    fs_module.settings.workspace_dir = str(temp_workspace)
    fs_module.settings.auth_enabled = False

    server = FastMCP(name="test-server")

    from src.tools import echo, database, filesystem, http_api
    echo.register(server)
    database.register(server)
    filesystem.register(server)
    http_api.register(server)

    return server
