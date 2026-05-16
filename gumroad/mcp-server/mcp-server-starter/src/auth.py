"""
API key authentication for the MCP server.

How it works:
- Auth is toggled via AUTH_ENABLED in .env (default: False for local dev)
- When enabled, all tool calls must include X-API-Key header
- Key is validated against API_KEY in .env

Usage in tools:
    @mcp.tool()
    def my_tool(ctx: Context, ...) -> ...:
        check_auth(ctx)
        ...

For stdio transport (Claude Desktop / Cursor), auth is typically not needed
since the server runs as a local process. Enable it when deploying via SSE.
"""

from src.config import settings


class AuthError(Exception):
    """Raised when API key is missing or invalid."""
    pass


def check_auth(api_key_header: str | None) -> None:
    """
    Validate API key. Call this at the top of any tool that requires auth.
    Raises AuthError if auth is enabled and key is invalid.
    """
    if not settings.auth_enabled:
        return

    if not api_key_header:
        raise AuthError("Missing X-API-Key header")

    if api_key_header != settings.api_key:
        raise AuthError("Invalid API key")


def get_api_key_from_env() -> str:
    """Return the configured API key. Useful for testing."""
    return settings.api_key
