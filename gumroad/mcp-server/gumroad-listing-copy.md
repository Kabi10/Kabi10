# Gumroad Listing — MCP Server Starter Kit

---

## PRODUCT NAME
MCP Server Starter Kit — Production-Ready Python

---

## TAGLINE (shown under product name)
Clone it. Have working tools in Claude Desktop or Cursor in 5 minutes.

---

## DESCRIPTION (paste into Gumroad's description field)

Most MCP tutorials give you a hello world that breaks the moment you try to do something real.

This is what comes after.

mcp-server-starter is a complete, production-structured Python MCP server with 12 tools across 4 modules — covering the patterns you'll actually need: database queries, external API calls, sandboxed file access, and a bare-minimum template to clone for your own tools.

---

WHAT'S INCLUDED

12 tools across 4 modules:

- echo / reverse / word_count — the simplest possible pattern, ideal as a starting template
- list_tables / describe_table / query_table / count_rows — parameterized SQLite queries with injection prevention and column allowlisting
- list_files / read_file / write_file / delete_file / file_info — sandboxed file operations with path traversal prevention
- get_current_weather / get_country_info / http_get — external REST API calls with retry/backoff and TTL caching

Production patterns included:

- API key authentication (header-based, toggle on/off via .env)
- Pydantic settings — all config through environment variables, zero hardcoded values
- Exponential backoff on rate limit and server errors
- In-memory TTL cache for external API tools
- Path traversal prevention baked into the filesystem tools
- 47 passing tests, no network calls required
- Docker + docker-compose for SSE/HTTP deployment
- Seeded SQLite database — works out of the box, no setup
- Claude Desktop and Cursor integration configs included

---

QUICK START

git clone [repo]
cd mcp-server-starter
pip install -r requirements.txt
cp .env.example .env
python -m src.server

Then add the config snippet to Claude Desktop or Cursor. Done.

---

WHAT YOU SKIP BY BUYING THIS

2-3 days reading the MCP spec, figuring out project structure, writing boilerplate auth, debugging your first tool registration, and learning why your retry logic doesn't handle 429s correctly.

---

WHO THIS IS FOR

- Developers building custom MCP tools for Claude Desktop or Cursor
- Teams wanting a clean starting point before writing domain-specific tools
- Anyone who's seen MCP tutorials and wants the production version, not the demo version

---

DELIVERY

Private GitHub repository. You receive a collaborator invite immediately after purchase. Future updates are pushed to the same repo — you get them automatically.

---

LICENSE

Personal and commercial use permitted. You may use this code in client projects and production systems. Resale of this starter kit as a standalone product is not permitted.

---

## PRICE
$39

## REFUND POLICY (paste into Gumroad refund field)
No refunds after repository access has been granted. If you have a technical issue, contact me and I'll resolve it.

---

## TAGS (enter into Gumroad tags field)
mcp, model context protocol, claude, cursor, python, fastmcp, ai tools, developer tools, starter kit, boilerplate

---

## SUGGESTED CONTENT RATING
Everyone

---

## CALL TO ACTION BUTTON TEXT
Get instant access

---

## FAQ SECTION (add as additional content block)

Q: Does this work with Claude Desktop?
A: Yes. The README includes the exact config snippet to paste into claude_desktop_config.json.

Q: Does this work with Cursor?
A: Yes. Config snippet for .cursor/mcp.json is included.

Q: What Python version is required?
A: Python 3.11 or higher.

Q: Can I use this for a client project?
A: Yes. Commercial use in client projects is permitted under the license.

Q: What if a dependency updates and something breaks?
A: Push the fix to the repo and all buyers get it automatically via the collaborator access.

Q: Is the database SQLite only?
A: The bundled example uses SQLite (zero setup). The connection logic is in one function — swap it for Postgres or MySQL by changing _get_connection() in database.py.
