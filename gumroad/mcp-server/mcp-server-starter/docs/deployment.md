# Deployment Guide

## Local (stdio) — Claude Desktop and Cursor

This is the default mode. The MCP server runs as a local subprocess,
started automatically by the client. No ports, no auth needed.

### Claude Desktop

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`
(macOS) or `%APPDATA%\Claude\claude_desktop_config.json` (Windows):

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

Restart Claude Desktop. You should see the server listed under
Settings → Developer → MCP Servers.

### Cursor

Create or edit `.cursor/mcp.json` in your project root:

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

Or use the global config at `~/.cursor/mcp.json` to make it available
in all Cursor workspaces.

---

## Docker (SSE) — self-hosted server

Use this when you want the MCP server accessible over HTTP,
for example from multiple clients or a remote machine.

```bash
# Set your API key
export API_KEY=your-secret-key

# Build and start
docker compose up -d

# Check logs
docker compose logs -f
```

The server will be available at `http://localhost:8000/sse`.

Connect a client:

```json
{
  "mcpServers": {
    "mcp-server-starter": {
      "url": "http://localhost:8000/sse",
      "headers": {
        "X-API-Key": "your-secret-key"
      }
    }
  }
}
```

---

## Cloud deployment (Railway / Render / Fly.io)

All three platforms support Docker deploys with minimal config.

### Railway

```bash
# Install Railway CLI
npm i -g @railway/cli

railway login
railway init
railway up
```

Set environment variables in the Railway dashboard:
- `API_KEY` — your secret key
- `AUTH_ENABLED` — `true`
- `TRANSPORT` — `sse`

### Render

1. Connect your GitHub repo in the Render dashboard
2. New → Web Service → Docker
3. Set env vars: `API_KEY`, `AUTH_ENABLED=true`, `TRANSPORT=sse`
4. Deploy

### Fly.io

```bash
fly launch
fly secrets set API_KEY=your-secret-key AUTH_ENABLED=true TRANSPORT=sse
fly deploy
```

---

## Environment variables reference

| Variable | Default | Description |
|---|---|---|
| `TRANSPORT` | `stdio` | `stdio` or `sse` |
| `AUTH_ENABLED` | `false` | Enable API key auth |
| `API_KEY` | `dev-key-change-me` | Required when auth enabled |
| `DB_PATH` | `./data/data.db` | SQLite database path |
| `WORKSPACE_DIR` | `./workspace` | Sandboxed filesystem root |
| `EXTERNAL_API_TIMEOUT` | `10` | HTTP timeout in seconds |
