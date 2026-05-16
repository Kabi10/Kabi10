# MCP Server Starter Kit

Production-ready Python MCP server. Clone it. Have working tools in Claude Desktop or Cursor in under 5 minutes.

---

## What's included

**4 tool modules covering the most common real-world patterns:**

| Module | Tools | Pattern |
|---|---|---|
| `echo` | echo, reverse, word_count | Stateless transformation — the simplest template |
| `database` | list_tables, describe_table, query_table, count_rows | Parameterized SQL, injection prevention, column allowlisting |
| `filesystem` | list_files, read_file, write_file, delete_file, file_info | Sandboxed file ops, path traversal prevention |
| `http_api` | get_current_weather, get_country_info, http_get | External REST API calls, retry/backoff, TTL cache |

**Production patterns included:**

- API key authentication (header-based, toggle on/off via env)
- Pydantic settings — all config through `.env`, no hardcoded values
- Retry with exponential backoff on rate limit / server errors
- In-memory TTL cache for external API tools
- Path traversal prevention in filesystem tools
- Full pytest test suite (35+ tests)
- Docker + docker-compose for SSE deployment
- Seeded SQLite database — works out of the box, no setup

---

## Quick start

**Requirements:** Python 3.11+, pip, git

```bash
git clone https://github.com/<your-github-username>/mcp-server-starter
cd mcp-server-starter

pip install -r requirements.txt

cp .env.example .env

python -m src.server
```

You should see:
```
Starting mcp-server-starter v1.0.0
Transport: stdio
Auth: disabled
Tools: 12 registered
```

---

## Connect to Claude Desktop

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "mcp-server-starter": {
      "command": "python",
      "args": ["-m", "src.server"],
      "cwd": "/absolute/path/to/mcp-server-starter"
    }
  }
}
```

Restart Claude Desktop. The tools appear automatically.

---

## Connect to Cursor

Create `.cursor/mcp.json` in your project:

```json
{
  "mcpServers": {
    "mcp-server-starter": {
      "command": "python",
      "args": ["-m", "src.server"],
      "cwd": "/absolute/path/to/mcp-server-starter"
    }
  }
}
```

---

## Run the tests

```bash
pytest
```

All 35+ tests run against a temporary database and workspace —
no cleanup needed, no side effects.

---

## Add your own tools

Copy `src/tools/echo.py` as a starting template.
Full walkthrough in `docs/adding-tools.md`.

The pattern in 30 seconds:

```python
# src/tools/my_tool.py
from mcp.server.fastmcp import FastMCP

def register(mcp: FastMCP) -> None:

    @mcp.tool()
    def my_tool(query: str) -> dict:
        """Describe what this does. Claude reads this."""
        return {"result": query}
```

Then in `src/server.py`:
```python
from src.tools import my_tool
my_tool.register(mcp)
```

---

## Deploy via Docker

```bash
export API_KEY=your-secret-key
docker compose up -d
```

Server runs at `http://localhost:8000/sse`.
Full cloud deployment guide (Railway, Render, Fly.io) in `docs/deployment.md`.

---

## Project structure

```
mcp-server-starter/
├── src/
│   ├── server.py          # Entry point
│   ├── config.py          # Pydantic settings
│   ├── auth.py            # API key validation
│   └── tools/
│       ├── echo.py
│       ├── database.py
│       ├── filesystem.py
│       └── http_api.py
├── tests/                 # Full pytest suite
├── docs/
│   ├── adding-tools.md
│   └── deployment.md
├── data/
│   └── data.db            # Seeded SQLite database
├── workspace/             # Sandboxed file operations directory
├── .env.example
├── Dockerfile
└── docker-compose.yml
```

---

## License

Personal and commercial use permitted. You may use this code in client projects
and production systems. Resale of this starter kit as a standalone product is not permitted.
