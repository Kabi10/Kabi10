# Launch Assets — MCP Server Starter Kit

---

## LOOM SCRIPT (2.5 min)

No voiceover needed if you prefer text-only. But if you record:

---

**[0:00 — 0:15] Open with the problem**

Show the MCP docs page or a "build your first MCP server" tutorial.

Say: "Every MCP tutorial I've seen gets you to hello world and stops there.
No auth, no tests, no structure you'd actually ship. This is what comes after."

---

**[0:15 — 0:45] Clone and install**

Terminal, clean directory.

```
git clone [your repo URL]
cd mcp-server-starter
pip install -r requirements.txt
cp .env.example .env
```

Say: "Four commands. That's the entire setup."

---

**[0:45 — 1:10] Run the tests**

```
python -m pytest
```

Show the 47 passing tests, 1.2 second runtime.

Say: "47 tests, no network calls, runs in just over a second.
If you break something while customizing, you'll know immediately."

---

**[1:10 — 1:45] Connect to Claude Desktop**

Open `~/Library/Application Support/Claude/claude_desktop_config.json`.
Paste the config snippet from the README. Save. Restart Claude Desktop.

Open Claude Desktop. Show the MCP server listed under Settings → Developer.

Say: "One config snippet. Claude Desktop picks it up on restart."

---

**[1:45 — 2:15] Call a tool live**

In Claude Desktop, type something like:
"Query the products table and show me everything in the hardware category."

Show Claude calling `query_table` with the right filters, returning the results.

Then: "Write a file called summary.txt to the workspace with a one-line summary of what you found."

Show Claude calling `write_file`, then verify the file exists in the workspace directory.

Say: "That's the database tool and the filesystem tool working together.
No glue code. No prompt engineering. Just tools Claude can actually use."

---

**[2:15 — 2:30] Close**

Show the project structure briefly.

Say: "Four tool modules, each one a pattern you can clone for your own use case.
Auth, retry logic, caching, sandboxing — all included.
Link in the description."

---

**Recording tips:**
- Use a clean terminal with a large font (18px+)
- Dark terminal theme — matches the product aesthetic
- No need to edit heavily. One clean take is fine.
- Upload to Loom, copy the link, paste into the Gumroad description above the feature list

---

---

## REDDIT POSTS

Post these manually — don't use a scheduler, Reddit detects it.
Wait at least 3 days between posts across different subreddits.
Lead with value. The product mention is secondary.

---

### r/ClaudeAI

**Title:**
I built a production-ready MCP server starter kit because every tutorial stops at hello world

**Body:**
Been building MCP servers for a while and kept running into the same problem — the tutorials get you to a working tool in 10 minutes, but the jump from that to something you'd actually deploy is enormous.

Things none of the tutorials cover:
- How to structure a multi-tool server properly
- API key auth that you can toggle off for local dev
- Retry logic that actually handles rate limits (not just try/except)
- Sandboxing file access so the AI can't escape the workspace
- A test suite that doesn't require spinning up a real server

So I built a starter kit that handles all of that. 12 tools across 4 modules — database queries, external API calls, filesystem ops, and a minimal template to clone. 47 passing tests. Docker config. Claude Desktop and Cursor integration configs included.

Happy to answer questions about the architecture if anyone's curious. The database tool in particular has an interesting approach to injection prevention that doesn't rely on an ORM.

[Link in comments if anyone wants it]

---

### r/cursor (post a few days later)

**Title:**
Starter kit for building MCP tools in Cursor — production patterns, not demos

**Body:**
Noticed a lot of questions here about MCP servers and how to actually structure them beyond the basic examples.

I've been building these for a bit and put together a starter kit that covers the patterns that actually matter in production:

- Parameterized queries with table allowlisting (not just raw SQL execution)
- External API calls with exponential backoff on 429s — the naive version hammers the API and gets you blocked
- Filesystem tools that prevent path traversal (../../../etc/passwd style attacks)
- TTL cache so repeated tool calls in a conversation don't hit the API every time
- Toggle auth on/off via env var — leave it off for local stdio, turn it on for SSE deployment

Stack is Python + FastMCP. Cursor config snippet is included in the README.

Put it on Gumroad if anyone wants to skip the 2-3 days of setup: [link]

---

### r/mcp (if the subreddit exists / has traffic — check first)

**Title:**
Production patterns for Python MCP servers — what the docs don't cover

**Body:**
After building a few MCP servers I kept noticing the same gaps between what the documentation shows and what you actually need for something deployable.

Documented the patterns I ended up using and packaged them into a starter kit:

**Auth:** Header-based API key with an env toggle. Off by default for stdio (Claude Desktop / Cursor), on by default in the Docker config for SSE deployment.

**Retry logic:** Exponential backoff specifically on 429 and 5xx. The naive version catches all exceptions and retries — this one only retries the retryable ones.

**Filesystem sandboxing:** All file ops resolve to absolute paths and verify they're inside the workspace root before executing. Blocks both `../` traversal and absolute path injection.

**Caching:** Simple in-memory TTL dict. Prevents the same external API call from firing 4 times in one conversation.

**Testing:** Tools are sync functions so they test without a running server — just call `tool.fn(**kwargs)` directly. 47 tests, 1.2 second runtime.

The kit is on Gumroad ($39): [link]
Repo is private (collaborator invite on purchase) but happy to discuss any of the implementation choices here.

---

---

## TWITTER / X POSTS

Plain text, no emojis. Post these as threads, not single tweets.
Space them out — one per week after launch.

---

### Launch thread (post day of listing)

Tweet 1:
Every MCP server tutorial I've seen stops at hello world.

No auth. No tests. No structure you'd ship.

I fixed that.

---

Tweet 2:
mcp-server-starter is a production-ready Python MCP server with 12 tools across 4 modules.

What's actually included:

- Database queries with injection prevention
- External API calls with retry/backoff
- Filesystem tools with path traversal blocking
- A minimal template to clone for your own tools

---

Tweet 3:
The patterns that took me the longest to figure out:

Auth that toggles off for local dev (stdio) but on for deployed SSE — same codebase, one env var.

Retry logic that only retries retryable errors. 429 and 5xx, not everything.

Filesystem sandboxing that resolves absolute paths before checking — ../../../etc/passwd doesn't work.

---

Tweet 4:
47 passing tests. Runs in 1.2 seconds. No network calls required.

Claude Desktop config and Cursor config included in the README.

Docker + docker-compose for SSE deployment.

$39 on Gumroad. Link in bio.

---

### Value thread (post 1 week later — no product mention until the end)

Tweet 1:
Three things I got wrong when I built my first MCP server:

---

Tweet 2:
1. I used try/except around the whole request and retried everything.

Rate limit errors (429) are retryable. Auth errors (401) are not. Treating them the same means you retry failures that will never succeed and waste time.

Retry on 429 and 5xx. Raise immediately on everything else.

---

Tweet 3:
2. I let the AI write to any path it wanted.

Obvious in retrospect. If your write_file tool doesn't validate the path, `../../../etc/cron.d/backdoor` is a valid input.

Resolve to absolute path. Check it starts with your workspace root. Reject anything else.

---

Tweet 4:
3. I called the external API on every tool invocation.

In a multi-step conversation, Claude might call the same weather tool 3 times in 2 minutes. Without a cache, that's 3 API calls, possible rate limiting, and unnecessary latency.

A 5-minute in-memory TTL cache costs 10 lines of code.

---

Tweet 5:
All three of these patterns are in the MCP starter kit I put on Gumroad.

$39. Private GitHub repo access on purchase.

[link]

---

---

## LINKEDIN POST (post 3-5 days after launch)

Built something this week I wish had existed when I started working with MCP.

Background: I've been building Model Context Protocol servers for Claude Desktop and Cursor — custom tools that let AI assistants interact with databases, APIs, and file systems in production environments.

The problem is that every tutorial and example I found covers the basics and stops there. The gap between "working demo" and "something I'd actually deploy" was about 3 days of trial and error each time.

So I packaged the production patterns I kept rewriting:

- API key auth that toggles off for local development
- Exponential backoff that distinguishes retryable errors from permanent failures
- Filesystem sandboxing that blocks path traversal attacks
- In-memory TTL caching for external API tools
- A full pytest suite that runs in under 2 seconds

12 tools, 47 tests, Docker config, and integration configs for both Claude Desktop and Cursor.

It's on Gumroad for $39 if you're building in this space: [link]

Happy to answer questions about the architecture or MCP development generally in the comments.

---

---

## TIMING PLAN

Day 0 (listing goes live): Post launch thread on Twitter/X
Day 1: Post in r/ClaudeAI
Day 4: Post in r/cursor
Day 7: Post value thread on Twitter/X (no product mention until last tweet)
Day 10: LinkedIn post
Day 14: Post in r/mcp if traffic looks viable

Check which Reddit posts get traction. Double down on that subreddit with a follow-up value post (not a product post — a genuine tip or breakdown) 2-3 weeks later.

First sale usually comes from Reddit. Repeat sales and word of mouth come from the quality of the product itself.
