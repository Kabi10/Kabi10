# cursor-rules: npm Package Design Spec

**Date:** 2026-04-04  
**Repo:** github.com/Kabi10/Cursor → rename to `cursor-rules`  
**npm package:** `cursor-rules`  
**Entry point:** `npx cursor-rules init`

---

## 1. Problem & Opportunity

The Cursor AI editor uses `.cursorrules` files to inject project-specific instructions into every AI conversation. Developers need tailored rules per stack, but:

- Writing rules from scratch is tedious and inconsistent
- Existing web generators (cursorrules.org, cursor.directory, etc.) are saturated and require a browser
- `PatrickJS/awesome-cursorrules` (38k stars) is a static collection — no composition, no auto-detection
- No tool currently reads your project files and generates rules automatically

The `cursor-rules` npm package fills the gap: **auto-detect your stack, compose modular rules, write the file in one command.**

---

## 2. Goal

Publish `cursor-rules` to npm so any developer can run:

```bash
npx cursor-rules init
```

...inside any project and get a tailored `.cursorrules` (or `.cursor/rules/project.mdc`) file in under 10 seconds, with zero configuration required.

---

## 3. Scope

**In scope:**
- Stack auto-detection from project files
- Interactive module selector (confirm/toggle detected modules, add extras)
- Module composer (concatenate selected `.md` module files)
- Output in both legacy (`.cursorrules`) and new Cursor format (`.cursor/rules/project.mdc`)
- 12 starter modules covering the most-used stacks in 2026
- Backwards-compatible: existing PowerShell/bash scripts remain untouched

**Out of scope (v1):**
- Web UI / web generator
- MCP server
- Rule linter
- CI/CD integration mode

---

## 4. Architecture

```
cursor-rules/
  src/
    cli.js          Entry point — parses args, orchestrates detect → select → compose → write
    detect.js       Reads project files, returns array of matched module IDs
    compose.js      Loads module .md files, concatenates, writes output file
  modules/          Portable .md rule files (one per module)
    core.md
    typescript.md
    nextjs.md
    fastapi.md
    flutter.md
    supabase.md
    drizzle.md
    shadcn.md
    saas.md
    ecommerce.md
    claude-code.md
    agentic.md
  package.json      npm config, bin entry: "cursor-rules": "src/cli.js"
  README.md         Rewritten for npm audience
  build-rules.ps1   Kept (backwards compat)
  build-rules.sh    Kept (backwards compat)
```

---

## 5. Data Flow

```
npx cursor-rules init
  │
  ├─ 1. DETECT
  │     Scan cwd for: package.json, requirements.txt, build.gradle,
  │     Cargo.toml, pubspec.yaml, go.mod
  │     Map findings → module IDs:
  │       "next" in package.json deps       → [nextjs, typescript, core]
  │       "fastapi" in requirements.txt     → [fastapi, core]
  │       "flutter" in pubspec.yaml         → [flutter, core]
  │       "@supabase/supabase-js" in deps   → add supabase
  │       "drizzle-orm" in deps             → add drizzle
  │       "@shadcn/ui" or "shadcn" in deps  → add shadcn
  │       Nothing found                     → fall through to manual pick
  │
  ├─ 2. SELECT (interactive)
  │     Print: "Detected: Next.js, TypeScript, Supabase"
  │     Show checkbox list — detected modules pre-checked,
  │     optional extras (saas, ecommerce, claude-code, agentic) unchecked
  │     User toggles with arrow keys + space, confirms with Enter
  │
  ├─ 3. FORMAT
  │     Ask: "Output format?"
  │       [1] .cursorrules  (legacy, works with all Cursor versions)
  │       [2] .cursor/rules/project.mdc  (new 2026 format)
  │
  ├─ 4. COMPOSE
  │     Load selected module .md files from package modules/ dir
  │     Concatenate with section headers
  │     Prepend token count estimate
  │
  └─ 5. WRITE
        If target file exists → ask: overwrite / append / cancel
        Write file
        Print: "Written to .cursorrules (~1,240 tokens). Restart Cursor to apply."
```

---

## 6. Module Library (v1 — 12 modules)

| Module | Trigger | Contents |
|--------|---------|----------|
| `core` | always included | Pre-implementation checklist, search-before-build, no hallucination rules |
| `typescript` | `typescript` in deps | Strict mode, type safety patterns, no `any` |
| `nextjs` | `next` in deps | App Router patterns, RSC rules, file conventions |
| `fastapi` | `fastapi` in requirements | Async patterns, Pydantic models, dependency injection |
| `flutter` | `flutter:` key in `pubspec.yaml` | Widget patterns, state management, platform conventions |
| `supabase` | `@supabase/supabase-js` in deps | RLS rules, auth patterns, realtime usage |
| `drizzle` | `drizzle-orm` in deps | Schema patterns, migration rules, query conventions |
| `shadcn` | `shadcn` in deps | Component usage patterns, variant conventions |
| `saas` | manual opt-in | Multi-tenancy, billing, auth flow, feature flags |
| `ecommerce` | manual opt-in | Cart, checkout, inventory, payment patterns |
| `claude-code` | manual opt-in | CLAUDE.md conventions, tool use patterns, memory system |
| `agentic` | manual opt-in | Agent loop patterns, tool use safety, context management |

Existing modules from the repo (`modules/core/`, `patterns/nextjs-patterns.mdc`, etc.) are migrated and updated as the seed content for these files.

---

## 7. Error Handling

| Scenario | Behavior |
|----------|----------|
| No project files found | Skip auto-detect, show full module list for manual selection |
| Output file already exists | Prompt: overwrite / append / cancel (default: cancel) |
| Node.js < 18 | Print: "cursor-rules requires Node 18+. Download at nodejs.org" and exit 1 |
| Module file missing from package | Skip with warning, continue composing remaining modules |
| User cancels (Ctrl+C) | Exit cleanly, no file written, no error message |

---

## 8. Testing

**Unit: `tests/detect.test.js`**
- Mock file system with fixture project files
- Assert correct module IDs returned for Next.js, FastAPI, Flutter projects
- Assert empty detection falls through gracefully

**Unit: `tests/compose.test.js`**
- Assert composed output contains section headers for each selected module
- Assert token count estimate is printed
- Assert both output formats write to correct paths

**Integration: `tests/init.test.js`**
- Run `cli.js` against a fixture Next.js project directory
- Assert `.cursorrules` is written with correct content
- Assert existing file prompts for overwrite

---

## 9. npm Package Config

```json
{
  "name": "cursor-rules",
  "version": "1.0.0",
  "bin": { "cursor-rules": "src/cli.js" },
  "engines": { "node": ">=18" },
  "files": ["src/", "modules/"],
  "license": "MIT"
}
```

Dependencies: `prompts` (interactive CLI), `kleur` (terminal colors). No heavy deps.

---

## 10. Repo Changes (non-code)

- Rename GitHub repo: `Cursor` → `cursor-rules`
- Add GitHub topics: `cursor`, `cursorrules`, `cursor-ai`, `developer-tools`, `cli`, `npm`
- Add repo description: "Auto-detect your stack and generate a tailored .cursorrules file. npx cursor-rules init"
- Rewrite README for npm audience
- Submit to `PatrickJS/awesome-cursorrules` as a related tool (PR to their README)

---

## 11. Success Criteria

- `npx cursor-rules init` works end-to-end in a Next.js project
- All 12 modules present with meaningful content
- Published to npm as `cursor-rules`
- GitHub repo renamed and topics set
- PR submitted to awesome-cursorrules
