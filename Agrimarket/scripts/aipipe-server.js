#!/usr/bin/env node
/**
 * aipipe-server.js — Live dashboard server for the AI pipeline.
 *
 * Starts an HTTP server, serves the live HTML dashboard, and accepts
 * stage update events from pipeline.sh via simple REST endpoints.
 *
 * Usage (called by pipeline.sh):
 *   PIPELINE_TITLE="My Task" AIPIPE_PORT=4242 node scripts/aipipe-server.js
 *
 * Endpoints:
 *   GET  /                       → dashboard HTML
 *   GET  /api/status             → JSON pipeline state
 *   POST /api/config             → { title?, stages? }
 *   POST /api/stage/:n/start    → mark stage n as running
 *   POST /api/stage/:n/update   → { content } partial content update (streaming)
 *   POST /api/stage/:n/complete → { content } mark done
 *   POST /api/stage/:n/error    → { error } mark failed
 */
"use strict";

const http = require("http");
const fs = require("fs");
const path = require("path");
const { exec } = require("child_process");

const PORT = parseInt(process.env.AIPIPE_PORT || "4242", 10);
const HTML_SRC = path.join(__dirname, "aipipe-live.html");

// ── Pipeline state ──────────────────────────────────────────────────────────
const pipeline = {
  title: process.env.PIPELINE_TITLE || "AI Pipeline",
  startedAt: new Date().toISOString(),
  stages: [
    {
      id: 1,
      name: "Gemini 2.5 Pro",
      label: "Codebase Analysis",
      status: "waiting",
      content: "",
      error: null,
      startedAt: null,
      completedAt: null,
    },
    {
      id: 2,
      name: "DeepSeek R1",
      label: "Implementation Spec",
      status: "waiting",
      content: "",
      error: null,
      startedAt: null,
      completedAt: null,
    },
    {
      id: 3,
      name: "Claude Code",
      label: "Code Execution",
      status: "waiting",
      content: "",
      error: null,
      startedAt: null,
      completedAt: null,
    },
  ],
};

// ── Helpers ─────────────────────────────────────────────────────────────────
const readBody = (req) =>
  new Promise((resolve) => {
    let buf = "";
    req.on("data", (c) => {
      buf += c;
    });
    req.on("end", () => {
      try {
        resolve(JSON.parse(buf));
      } catch {
        resolve({});
      }
    });
  });

const json = (res, code, data) => {
  res.writeHead(code, {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
  });
  res.end(JSON.stringify(data));
};

const STAGE_RE = /^\/api\/stage\/(\d+)\/(start|update|complete|error)$/;

// ── Request handler ─────────────────────────────────────────────────────────
const server = http.createServer(async (req, res) => {
  // CORS pre-flight
  if (req.method === "OPTIONS") {
    res.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    });
    return res.end();
  }

  // GET / — serve dashboard HTML
  if (req.method === "GET" && (req.url === "/" || req.url === "/index.html")) {
    try {
      const html = fs.readFileSync(HTML_SRC, "utf8");
      res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
      return res.end(html);
    } catch (e) {
      res.writeHead(500, { "Content-Type": "text/plain" });
      return res.end(`Cannot read ${HTML_SRC}: ${e.message}`);
    }
  }

  // GET /api/status — full pipeline state
  if (req.method === "GET" && req.url === "/api/status") {
    return json(res, 200, pipeline);
  }

  // POST /api/config — update title or replace stages
  if (req.method === "POST" && req.url === "/api/config") {
    const data = await readBody(req);
    if (data.title) pipeline.title = data.title;
    if (data.stages) pipeline.stages = data.stages;
    return json(res, 200, { ok: true });
  }

  // POST /api/stage/:n/(start|update|complete|error)
  const m = req.url.match(STAGE_RE);
  if (req.method === "POST" && m) {
    const id = parseInt(m[1], 10);
    const action = m[2];
    const stage = pipeline.stages.find((s) => s.id === id);
    if (!stage) return json(res, 404, { error: "Stage not found" });

    const data = await readBody(req);
    const now = new Date().toISOString();

    switch (action) {
      case "start":
        stage.status = "running";
        stage.startedAt = now;
        break;
      case "update":
        if (data.content != null) stage.content = data.content;
        break;
      case "complete":
        stage.status = "done";
        stage.completedAt = now;
        if (data.content != null) stage.content = data.content;
        break;
      case "error":
        stage.status = "error";
        stage.completedAt = now;
        stage.error = data.error || "Unknown error";
        break;
    }

    process.stdout.write(`[aipipe] stage ${id} → ${action}\n`);
    return json(res, 200, { ok: true });
  }

  res.writeHead(404, { "Content-Type": "text/plain" });
  res.end("Not found");
});

// ── Start ────────────────────────────────────────────────────────────────────
server.listen(PORT, "127.0.0.1", () => {
  const url = `http://localhost:${PORT}`;
  process.stdout.write(`[aipipe] Dashboard → ${url}\n`);
  process.stdout.write(`[aipipe] Task      → ${pipeline.title}\n`);

  // Open browser (platform-aware)
  const open =
    process.platform === "win32"
      ? `cmd /c start "" "${url}"`
      : process.platform === "darwin"
        ? `open "${url}"`
        : `xdg-open "${url}"`;

  exec(open, (err) => {
    if (err)
      process.stderr.write(`[aipipe] Could not open browser: ${err.message}\n`);
  });
});

// ── Graceful shutdown ────────────────────────────────────────────────────────
const shutdown = () => server.close(() => process.exit(0));
process.on("SIGTERM", shutdown);
process.on("SIGINT", shutdown);
