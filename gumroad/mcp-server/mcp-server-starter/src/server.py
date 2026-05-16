"""
MCP Server Starter Kit
======================
Entry point. Registers all tools and starts the server.

Transport modes:
    stdio  — for Claude Desktop and Cursor (default)
    sse    — for HTTP deployment (set TRANSPORT=sse in .env)

Quick start:
    python -m src.server

Or via the CLI script:
    python server.py
"""

from mcp.server.fastmcp import FastMCP

from src.config import settings
from src.tools import echo, database, filesystem, http_api

mcp = FastMCP(name=settings.server_name)

# Register all tool modules
echo.register(mcp)
database.register(mcp)
filesystem.register(mcp)
http_api.register(mcp)


def main() -> None:
    """Start the MCP server."""
    transport = settings.transport.lower()

    if transport not in ("stdio", "sse"):
        raise ValueError(
            f"Invalid TRANSPORT '{transport}'. Must be 'stdio' or 'sse'.\n"
            "- Use 'stdio' for Claude Desktop / Cursor\n"
            "- Use 'sse' for HTTP deployment"
        )

    print(f"Starting {settings.server_name} v{settings.server_version}")
    print(f"Transport: {transport}")
    print(f"Auth: {'enabled' if settings.auth_enabled else 'disabled'}")
    print(f"Tools: {len(mcp._tool_manager._tools)} registered")

    mcp.run(transport=transport)


if __name__ == "__main__":
    main()
