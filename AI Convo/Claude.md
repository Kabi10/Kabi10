This is a rich question. Let me think through each layer carefully, because the answers are interconnected.

---

## 1. The Missing Mental Model: Context Switching Cost

Your dashboard currently answers **"what do I have?"** — it's an inventory. What it doesn't answer is **"where was I?"** and **"what should I do next?"**

As a solo dev across 16 projects spanning wildly different domains, your biggest productivity tax isn't finding projects — it's *resuming* them. Every time you context-switch from an Arduino project to a FastAPI backend, you're paying a cognitive reload cost: what was I doing, what was broken, what was next, what files were open, what mental model was I holding?

The missing workflow is **session continuity**. Your dashboard should function less like a project directory and more like a **workbench with warm seats** — when you sit back down at a project, it should hand you back the mental state you left behind. That means capturing not just *what* a project is, but *where you left off* and *what's next* every time you step away.

The practical implication: every project card should have a prominent, low-friction "working on / next up" field that you update as you leave a project — not a full journal, just a single sentence like `"debugging BLE handshake timeout on ESP32, check serial monitor output"`. That one sentence, visible on the card, eliminates 10-15 minutes of re-orientation.

---

## 2. The Activity & Focus System

This should be built around three time-based lenses and one derived signal.

**The three lenses:**

**Active Now** — projects with git commits in the last 48 hours, or that you've manually flagged as "in focus." This is your workbench. Show these first, always, with the "where I left off" note prominent.

**Needs Attention** — projects where something is unresolved. This is derived from a combination of signals: the project has a `next_action` set but no commits in 7+ days, the project has status "in progress" but has gone stale, there are dirty git files (uncommitted work you might lose), or you've set an explicit reminder/deadline.

**Neglected** — projects with no commits in 30+ days that aren't explicitly marked as paused or archived. The point isn't guilt — it's surfacing projects that might have bit-rotted dependencies or forgotten half-finished features.

**The derived signal: a "Staleness Score"**

Calculate this per-project as a simple function of days since last commit, weighted by status. An "active" project that hasn't been touched in 14 days gets a high staleness score. A "paused" project untouched for 60 days gets a low one. Surface this as a subtle visual treatment on each card — a colored border or heat indicator — so you can scan your entire portfolio's health at a glance without reading anything.

**Data to surface on the dashboard:**

A small top-bar summary: `"3 active · 2 need attention · 4 neglected · 7 paused"` with click-to-filter. Below each project card, show a compact activity sparkline — a tiny 30-day or 90-day commit frequency graph. This gives you instant pattern recognition across all 16 projects without opening any of them.

---

## 3. Making AI Assignment Actually Useful

The label approach is a placeholder for something much more powerful. Here's how to make it earn its place:

**Per-project AI context payload.** Instead of just `"assigned_ai": "Claude"`, store a structured block that captures what the AI needs to know to be immediately useful on that project. This includes the tech stack, key architectural decisions, coding conventions, and — critically — a brief system prompt or context snippet you'd paste when starting a new AI session for that project.

**One-click context copy.** The highest-value interaction is: you click a button on the project card, and it copies to your clipboard a pre-built context block like:

```
Project: AgriMarket
Stack: React Native, Firebase, Tamil localization
Current focus: Implementing seller listing flow
Key constraint: Must work on low-end Android devices
Style: Functional components, Zustand for state
```

That alone saves you 2-3 minutes of preamble every time you open a chat with an AI assistant. Over 16 projects, across dozens of sessions a week, that's significant.

**Session log.** Optionally track when you used which AI on which project and what the topic was — not full transcripts, just a one-line summary. Over time this becomes a searchable history: "what did I ask Claude about the BLE module three weeks ago?" This is lightweight to maintain if you add a quick-log field that you fill in after an AI session.

**The data model for this:**

```json
"ai_config": {
  "preferred_model": "claude-opus",
  "context_snippet": "React Native app for Sri Lankan farmers...",
  "stack_summary": "React Native, Firebase, Zustand, Tamil i18n",
  "conventions": "functional components, no class syntax, Tailwind-style",
  "session_log": [
    {
      "date": "2026-03-12",
      "model": "claude-opus",
      "topic": "Fixed Zustand store hydration bug",
      "outcome": "resolved"
    }
  ]
}
```

---

## 4. Proposed Data Model Upgrade

Here's a `projects_meta.json` schema that supports everything above without becoming unwieldy:

```json
{
  "schema_version": 2,
  "projects": {
    "agrimarket": {
      "name": "AgriMarket",
      "path": "C:\\Dev\\AgriMarket",
      "category": "mobile",
      "status": "active",
      "pinned": true,
      "description": "Marketplace app for Sri Lankan farmers",

      "focus": {
        "current_task": "Seller listing flow UI",
        "next_action": "Connect image upload to Firebase Storage",
        "blockers": "Need to test on low-RAM device",
        "updated_at": "2026-03-14T18:30:00Z"
      },

      "ai_config": {
        "preferred_model": "claude-opus",
        "context_snippet": "React Native app targeting older Tamil-speaking farmers...",
        "stack_summary": "React Native 0.73, Firebase, Zustand, react-i18next",
        "conventions": "functional components, no classes, async/await throughout",
        "session_log": []
      },

      "activity": {
        "last_commit_at": "2026-03-14T16:45:00Z",
        "last_opened_at": "2026-03-14T18:30:00Z",
        "staleness_days": 0,
        "commit_streak": 5,
        "recent_commit_counts": [3, 5, 0, 2, 1, 0, 4]
      },

      "notes": "Targeting Google Play launch Q2 2026",
      "tags": ["startup", "priority", "tamil"],
      "links": {
        "repo": "https://github.com/...",
        "docs": "",
        "deploy": ""
      },

      "created_at": "2025-11-01T00:00:00Z",
      "updated_at": "2026-03-14T18:30:00Z"
    }
  }
}
```

**Key design decisions:**

The `focus` block is the session continuity layer — three fields that are cheap to update and high-value to read. The `activity` block is computed by the backend on each scan, not manually maintained. The `recent_commit_counts` is a rolling 7-day array that powers the sparkline without needing to query git every render. The `ai_config` is the only block that grows over time (via `session_log`), and you can cap it at the last 20 entries. The `tags` array gives you flexible cross-cutting filters beyond category/status (you might want to see all "priority" projects regardless of category, or all "tamil" projects). The `links` block is simple but handles the common case of wanting to jump to a repo, docs site, or deployment.

The `schema_version` field lets your backend migrate gracefully as you evolve this.

---

## 5. The Highest Daily-Use Feature

**A "Resume" button on every project card.**

One click does three things: opens the project in VS Code, copies the AI context snippet to your clipboard, and displays the `focus.current_task` and `focus.next_action` in a brief toast or overlay so you see it before VS Code finishes loading.

When you're *done* with a session, a corresponding "Park" action prompts you for a one-line update to `current_task` and `next_action` (pre-filled with the previous values so you can just tweak them), then saves and timestamps the `focus` block.

This resume/park loop is the atomic habit that makes the entire system work. Every other feature — the staleness tracking, the AI context, the activity sparklines — becomes more valuable the more consistently you use this one interaction. And it works because it's two clicks and one sentence, not a project management ritual.

---

The through-line across all five answers: your dashboard's next evolution isn't about showing more data — it's about **reducing the cost of picking up where you left off**. Every feature should be evaluated against that standard. If it doesn't make context-switching cheaper, it's not worth the complexity.