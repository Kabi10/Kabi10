# cursor-rules Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the existing `Kabi10/Cursor` repo into a published npm package (`cursor-rules`) that auto-detects a project's stack and generates a tailored `.cursorrules` file via `npx cursor-rules init`.

**Architecture:** Three JS modules (`detect.js`, `compose.js`, `cli.js`) plus a `modules/` directory of 12 markdown rule files. The CLI uses `prompts` for interactive selection and `kleur` for terminal colours. Existing PowerShell/bash scripts are kept untouched. ESM throughout (`"type": "module"` in package.json). Tests use Node's built-in test runner (`node:test`).

**Tech Stack:** Node.js 18+, ESM, `prompts`, `kleur`, `node:test` (built-in, zero install)

**Repo:** `C:/Dev/cursor-rules` (already cloned from `Kabi10/Cursor`)

---

## File Map

| Action | Path | Purpose |
|--------|------|---------|
| Create | `package.json` | npm config, bin entry, deps |
| Create | `src/detect.js` | Reads project files → returns module ID array |
| Create | `src/compose.js` | Loads module .md files → concatenates → writes output |
| Create | `src/cli.js` | Entry point — orchestrates detect → select → compose → write |
| Create | `tests/detect.test.js` | Unit tests for detector |
| Create | `tests/compose.test.js` | Unit tests for composer |
| Create | `tests/init.test.js` | Integration test for full CLI flow |
| Create | `tests/fixtures/nextjs/package.json` | Fixture: Next.js project |
| Create | `tests/fixtures/fastapi/requirements.txt` | Fixture: FastAPI project |
| Create | `tests/fixtures/flutter/pubspec.yaml` | Fixture: Flutter project |
| Create | `modules/core.md` | Always-included: search-first, pre-impl checklist |
| Create | `modules/typescript.md` | TypeScript strict mode rules |
| Create | `modules/nextjs.md` | Next.js App Router patterns (from existing nextjs-patterns.mdc) |
| Create | `modules/fastapi.md` | FastAPI async patterns |
| Create | `modules/flutter.md` | Flutter widget/state patterns |
| Create | `modules/supabase.md` | Supabase RLS + auth patterns |
| Create | `modules/drizzle.md` | Drizzle ORM schema + query patterns |
| Create | `modules/shadcn.md` | Shadcn/ui component conventions |
| Create | `modules/saas.md` | SaaS: multi-tenancy, billing, auth (from existing .cursorrules-web-saas) |
| Create | `modules/ecommerce.md` | E-commerce patterns (from existing .cursorrules-ecommerce) |
| Create | `modules/claude-code.md` | CLAUDE.md conventions, memory system |
| Create | `modules/agentic.md` | Agent loop patterns, tool use safety |
| Modify | `README.md` | Rewritten for npm/GitHub audience |
| Keep | `build-rules.ps1` | Untouched (backwards compat) |
| Keep | `build-rules.sh` | Untouched (backwards compat) |

---

## Chunk 1: Package Scaffolding

### Task 1: package.json and project structure

**Files:**
- Create: `package.json`
- Create: `src/.gitkeep` (placeholder until src/ files are written)

- [ ] **Step 1: Create package.json**

Create `C:/Dev/cursor-rules/package.json`:
```json
{
  "name": "cursor-rules",
  "version": "1.0.0",
  "description": "Auto-detect your stack and generate a tailored .cursorrules file. npx cursor-rules init",
  "type": "module",
  "bin": {
    "cursor-rules": "src/cli.js"
  },
  "files": [
    "src/",
    "modules/"
  ],
  "scripts": {
    "test": "node --test tests/**/*.test.js"
  },
  "dependencies": {
    "kleur": "^4.1.5",
    "prompts": "^2.4.2"
  },
  "engines": {
    "node": ">=18"
  },
  "keywords": [
    "cursor",
    "cursorrules",
    "cursor-ai",
    "developer-tools",
    "cli"
  ],
  "license": "MIT",
  "repository": {
    "type": "git",
    "url": "https://github.com/Kabi10/cursor-rules.git"
  }
}
```

- [ ] **Step 2: Install dependencies**
```bash
cd C:/Dev/cursor-rules
npm install
```
Expected: `node_modules/` created with `prompts` and `kleur`.

- [ ] **Step 3: Create src/ directory**
```bash
mkdir src tests tests/fixtures tests/fixtures/nextjs tests/fixtures/fastapi tests/fixtures/flutter
```

- [ ] **Step 4: Create test fixtures**

Create `tests/fixtures/nextjs/package.json`:
```json
{
  "name": "my-app",
  "dependencies": {
    "next": "^14.0.0",
    "react": "^18.0.0",
    "typescript": "^5.0.0",
    "@supabase/supabase-js": "^2.0.0",
    "drizzle-orm": "^0.29.0"
  },
  "devDependencies": {
    "@types/node": "^20.0.0"
  }
}
```

Create `tests/fixtures/fastapi/requirements.txt`:
```
fastapi==0.110.0
uvicorn==0.29.0
pydantic==2.6.0
```

Create `tests/fixtures/flutter/pubspec.yaml`:
```yaml
name: my_app
flutter:
  uses-material-design: true
dependencies:
  flutter:
    sdk: flutter
```

- [ ] **Step 5: Commit scaffold**
```bash
cd C:/Dev/cursor-rules
git add package.json package-lock.json tests/fixtures/
git commit -m "feat: add npm package scaffold and test fixtures"
```

---

## Chunk 2: Detector

### Task 2: detect.js — stack detection from project files

**Files:**
- Create: `tests/detect.test.js`
- Create: `src/detect.js`

- [ ] **Step 1: Write failing tests**

Create `tests/detect.test.js`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { join } from 'path';
import { fileURLToPath } from 'url';
import { detectModules } from '../src/detect.js';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const fixtures = join(__dirname, 'fixtures');

test('always includes core module', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(result.includes('core'));
});

test('detects nextjs from package.json', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(result.includes('nextjs'));
});

test('detects typescript from package.json', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(result.includes('typescript'));
});

test('detects supabase from package.json', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(result.includes('supabase'));
});

test('detects drizzle from package.json', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(result.includes('drizzle'));
});

test('detects fastapi from requirements.txt', () => {
  const result = detectModules(join(fixtures, 'fastapi'));
  assert.ok(result.includes('fastapi'));
});

test('detects flutter from pubspec.yaml', () => {
  const result = detectModules(join(fixtures, 'flutter'));
  assert.ok(result.includes('flutter'));
});

test('returns only core when no project files found', () => {
  const result = detectModules('/tmp/empty-dir-that-does-not-exist');
  assert.deepEqual(result, ['core']);
});

test('does not include optional modules by default', () => {
  const result = detectModules(join(fixtures, 'nextjs'));
  assert.ok(!result.includes('saas'));
  assert.ok(!result.includes('ecommerce'));
  assert.ok(!result.includes('claude-code'));
  assert.ok(!result.includes('agentic'));
});
```

- [ ] **Step 2: Run tests — expect FAIL**
```bash
cd C:/Dev/cursor-rules
node --test tests/detect.test.js
```
Expected: `Error: Cannot find module '../src/detect.js'`

- [ ] **Step 3: Implement detect.js**

Create `src/detect.js`:
```js
import { readFileSync, existsSync } from 'fs';
import { join } from 'path';

/**
 * Reads project files in cwd and returns an array of matched module IDs.
 * Always includes 'core'. Optional modules (saas, ecommerce, claude-code, agentic)
 * are never auto-detected — user opts in via CLI prompt.
 *
 * @param {string} cwd - directory to scan (defaults to process.cwd())
 * @returns {string[]} array of module IDs
 */
export function detectModules(cwd = process.cwd()) {
  const modules = new Set(['core']);

  // ── package.json ──────────────────────────────────────────────────────────
  const pkgPath = join(cwd, 'package.json');
  if (existsSync(pkgPath)) {
    try {
      const pkg = JSON.parse(readFileSync(pkgPath, 'utf8'));
      const deps = {
        ...pkg.dependencies,
        ...pkg.devDependencies,
        ...pkg.peerDependencies,
      };

      if (deps['next']) modules.add('nextjs');
      if (deps['typescript'] || deps['@types/node']) modules.add('typescript');
      if (deps['@supabase/supabase-js']) modules.add('supabase');
      if (deps['drizzle-orm']) modules.add('drizzle');
      if (
        deps['shadcn-ui'] ||
        deps['@shadcn/ui'] ||
        deps['@radix-ui/react-dialog']
      )
        modules.add('shadcn');
    } catch {
      // malformed package.json — skip silently
    }
  }

  // ── requirements.txt ──────────────────────────────────────────────────────
  const reqPath = join(cwd, 'requirements.txt');
  if (existsSync(reqPath)) {
    const reqs = readFileSync(reqPath, 'utf8').toLowerCase();
    if (reqs.includes('fastapi')) modules.add('fastapi');
  }

  // ── pubspec.yaml (Flutter) ─────────────────────────────────────────────────
  const pubspecPath = join(cwd, 'pubspec.yaml');
  if (existsSync(pubspecPath)) {
    const pubspec = readFileSync(pubspecPath, 'utf8');
    if (pubspec.includes('flutter:')) modules.add('flutter');
  }

  return Array.from(modules);
}
```

- [ ] **Step 4: Run tests — expect PASS**
```bash
cd C:/Dev/cursor-rules
node --test tests/detect.test.js
```
Expected: all 9 tests pass.

- [ ] **Step 5: Commit**
```bash
git add src/detect.js tests/detect.test.js
git commit -m "feat: add stack detector with tests"
```

---

## Chunk 3: Composer

### Task 3: compose.js — module loading and file writing

**Files:**
- Create: `tests/compose.test.js`
- Create: `src/compose.js`
- Create: `modules/core.md` (needed by compose tests)

- [ ] **Step 1: Create a minimal core.md for tests**

Create `modules/core.md`:
```markdown
# Core Rules

Always search for existing code before implementing anything new.
```

- [ ] **Step 2: Write failing tests**

Create `tests/compose.test.js`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, rmSync, readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { tmpdir } from 'os';
import { composeModules, writeRules, estimateTokens } from '../src/compose.js';

test('composeModules returns content from selected modules', () => {
  const result = composeModules(['core']);
  assert.ok(result.includes('Core Rules'));
});

test('composeModules warns and skips missing module', () => {
  // Should not throw — just skips
  const result = composeModules(['core', 'nonexistent-module']);
  assert.ok(result.includes('Core Rules'));
});

test('estimateTokens returns a number', () => {
  const tokens = estimateTokens('hello world');
  assert.equal(typeof tokens, 'number');
  assert.ok(tokens > 0);
});

test('estimateTokens approximates at ~4 chars per token', () => {
  const tokens = estimateTokens('a'.repeat(400));
  assert.equal(tokens, 100);
});

test('writeRules writes .cursorrules in legacy format', () => {
  const tmp = mkdtempSync(join(tmpdir(), 'cursor-test-'));
  try {
    const outPath = writeRules('test content', 'legacy', tmp);
    assert.equal(outPath, join(tmp, '.cursorrules'));
    assert.equal(readFileSync(outPath, 'utf8'), 'test content');
  } finally {
    rmSync(tmp, { recursive: true });
  }
});

test('writeRules writes .cursor/rules/project.mdc in mdc format', () => {
  const tmp = mkdtempSync(join(tmpdir(), 'cursor-test-'));
  try {
    const outPath = writeRules('test content', 'mdc', tmp);
    assert.equal(outPath, join(tmp, '.cursor', 'rules', 'project.mdc'));
    assert.ok(existsSync(outPath));
    assert.equal(readFileSync(outPath, 'utf8'), 'test content');
  } finally {
    rmSync(tmp, { recursive: true });
  }
});
```

- [ ] **Step 3: Run tests — expect FAIL**
```bash
cd C:/Dev/cursor-rules
node --test tests/compose.test.js
```
Expected: `Error: Cannot find module '../src/compose.js'`

- [ ] **Step 4: Implement compose.js**

Create `src/compose.js`:
```js
import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const MODULES_DIR = join(__dirname, '..', 'modules');

/**
 * Loads selected module .md files and concatenates them.
 *
 * @param {string[]} moduleIds
 * @returns {string} composed rules content
 */
export function composeModules(moduleIds) {
  const sections = moduleIds.map((id) => {
    const modPath = join(MODULES_DIR, `${id}.md`);
    if (!existsSync(modPath)) {
      console.warn(`Warning: module "${id}" not found, skipping`);
      return null;
    }
    return readFileSync(modPath, 'utf8').trim();
  }).filter(Boolean);

  return sections.join('\n\n---\n\n');
}

/**
 * Rough token estimate: 1 token ≈ 4 characters.
 *
 * @param {string} text
 * @returns {number}
 */
export function estimateTokens(text) {
  return Math.ceil(text.length / 4);
}

/**
 * Writes the composed content to the appropriate file.
 *
 * @param {string} content
 * @param {'legacy' | 'mdc'} format
 * @param {string} cwd - target directory (defaults to process.cwd())
 * @returns {string} absolute path of written file
 */
export function writeRules(content, format, cwd = process.cwd()) {
  let outPath;
  if (format === 'mdc') {
    const rulesDir = join(cwd, '.cursor', 'rules');
    mkdirSync(rulesDir, { recursive: true });
    outPath = join(rulesDir, 'project.mdc');
  } else {
    outPath = join(cwd, '.cursorrules');
  }
  writeFileSync(outPath, content, 'utf8');
  return outPath;
}
```

- [ ] **Step 5: Run tests — expect PASS**
```bash
cd C:/Dev/cursor-rules
node --test tests/compose.test.js
```
Expected: all 6 tests pass.

- [ ] **Step 6: Commit**
```bash
git add src/compose.js tests/compose.test.js modules/core.md
git commit -m "feat: add module composer with tests"
```

---

## Chunk 4: Module Library

### Task 4: Write the 11 remaining module .md files

**Files:** `modules/{typescript,nextjs,fastapi,flutter,supabase,drizzle,shadcn,saas,ecommerce,claude-code,agentic}.md`

> These are seeded from the existing repo content where available and written fresh otherwise. Content is concise and actionable — aim for 30–80 lines each.

- [ ] **Step 1: Create modules/typescript.md**

```markdown
# TypeScript Rules

## Strict Mode
Always enable strict mode. Never disable it to silence errors.
```tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "exactOptionalPropertyTypes": true
  }
}
```

## Type Safety
- Never use `any`. Use `unknown` and narrow with type guards.
- Prefer `interface` for object shapes, `type` for unions and intersections.
- Export types alongside their implementations.
- Use `satisfies` operator to validate objects against types without widening.

## Examples

```ts
// WRONG
function process(data: any) { return data.value; }

// RIGHT
function process(data: unknown) {
  if (typeof data === 'object' && data !== null && 'value' in data) {
    return data.value;
  }
  throw new Error('Invalid data shape');
}
```
```

- [ ] **Step 2: Create modules/nextjs.md**

```markdown
# Next.js Rules

## Component Model
- Default to Server Components. Add `'use client'` only when you need browser APIs, event handlers, or useState/useEffect.
- Never import server-only code in Client Components.
- Co-locate server logic with the page that needs it.

## App Router Conventions
- `page.tsx` — route UI
- `layout.tsx` — shared shell (do not fetch data here unless it applies to every child)
- `loading.tsx` — Suspense boundary
- `error.tsx` — error boundary (must be a Client Component)
- `route.ts` — API route handler

## Data Fetching
```tsx
// CORRECT: fetch in Server Component
export default async function Page() {
  const data = await fetch('https://api.example.com/items', {
    next: { revalidate: 60 },
  }).then(r => r.json());
  return <ItemList items={data} />;
}
```

## Server Actions
Use Server Actions for mutations. Never build a separate API route just to handle a form submit.
```tsx
async function createItem(formData: FormData) {
  'use server';
  const name = formData.get('name') as string;
  await db.insert(items).values({ name });
  revalidatePath('/items');
}
```

## Rules
- Always use `next/image` for images — never `<img>`.
- Always use `next/link` for internal navigation — never `<a>`.
- Metadata must be exported from `page.tsx` or `layout.tsx`, not set with `<head>`.
```

- [ ] **Step 3: Create modules/fastapi.md**

```markdown
# FastAPI Rules

## Async First
All route handlers must be `async def`. Never mix sync and async in the same handler.

## Pydantic Models
Define request and response models explicitly. Never use `dict` as a route parameter type.
```python
# WRONG
@app.post("/items")
async def create_item(data: dict):
    ...

# RIGHT
class ItemCreate(BaseModel):
    name: str
    price: float

class ItemResponse(BaseModel):
    id: int
    name: str
    price: float

@app.post("/items", response_model=ItemResponse)
async def create_item(data: ItemCreate, db: AsyncSession = Depends(get_db)):
    ...
```

## Dependency Injection
Use `Depends()` for database sessions, auth, and shared resources. Never instantiate shared resources inside route handlers.

## Error Handling
Use `HTTPException` for expected errors. Use a global exception handler for unexpected ones.
```python
@app.exception_handler(Exception)
async def global_handler(request: Request, exc: Exception):
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})
```

## Rules
- Use `lifespan` context manager for startup/shutdown logic — never `@app.on_event`.
- Always type-annotate return values on route handlers.
- Keep routers in separate files under `routers/`. Import and `include_router` in `main.py`.
```

- [ ] **Step 4: Create modules/flutter.md**

```markdown
# Flutter Rules

## Widget Structure
- Prefer `StatelessWidget` by default. Only use `StatefulWidget` when local mutable state is required.
- Extract widgets into separate files when they exceed ~80 lines.
- Never build complex widget trees inline in `build()` — extract to named methods or widgets.

## State Management
- Use `provider` or `riverpod` for shared state. Never pass state down more than 2 widget levels via constructor.
- Keep business logic out of widgets — use a ViewModel or Notifier class.

## Naming Conventions
- Widget files: `snake_case.dart`
- Widget classes: `PascalCase`
- Private widgets (used only in one file): prefix with `_`

## Performance
- Use `const` constructors wherever possible.
- Use `ListView.builder` for long lists — never `ListView` with children array.
- Avoid rebuilding parent widgets for child state changes — use `Consumer` or scoped providers.

## Rules
- Always provide `key` parameters to widgets in lists.
- Never use `BuildContext` across async gaps without checking `mounted`.
- Platform-specific code goes in `platform/` with `import 'package:flutter/foundation.dart'` guards.
```

- [ ] **Step 5: Create modules/supabase.md**

```markdown
# Supabase Rules

## Row Level Security
Enable RLS on every table. Never leave a table without policies.
```sql
-- ALWAYS enable RLS
ALTER TABLE items ENABLE ROW LEVEL SECURITY;

-- Users can only see their own rows
CREATE POLICY "users see own items"
  ON items FOR SELECT
  USING (auth.uid() = user_id);
```

## Auth
- Use `supabase.auth.getUser()` server-side to get the authenticated user. Never trust `getSession()` alone for authorization.
- Store the Supabase client in a singleton. Never instantiate it inside a component.

## Queries
```ts
// WRONG: no error handling
const { data } = await supabase.from('items').select('*');

// RIGHT
const { data, error } = await supabase.from('items').select('id, name, created_at');
if (error) throw new Error(error.message);
```

## Rules
- Never expose `SUPABASE_SERVICE_ROLE_KEY` to the client. Use it only in server-side code.
- Use typed clients: generate types with `supabase gen types typescript`.
- Prefer server-side Supabase client in Next.js Server Components and Route Handlers.
```

- [ ] **Step 6: Create modules/drizzle.md**

```markdown
# Drizzle ORM Rules

## Schema Definition
Define all tables in `db/schema.ts`. Export every table and its inferred types.
```ts
import { pgTable, serial, text, timestamp } from 'drizzle-orm/pg-core';
import { InferInsertModel, InferSelectModel } from 'drizzle-orm';

export const items = pgTable('items', {
  id: serial('id').primaryKey(),
  name: text('name').notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

export type Item = InferSelectModel<typeof items>;
export type NewItem = InferInsertModel<typeof items>;
```

## Queries
- Use the query builder (`db.select().from()`) for reads.
- Use `db.insert().values().returning()` for inserts when you need the created row.
- Never write raw SQL unless the query builder cannot express it.

## Migrations
- Run `drizzle-kit generate` to create migration files. Never edit generated files manually.
- Apply migrations with `drizzle-kit migrate` in CI before deploying.

## Rules
- Keep the `db` instance in `db/index.ts`. Import it everywhere else.
- Never pass the `db` instance to client components.
- Use transactions (`db.transaction()`) for multi-step writes.
```

- [ ] **Step 7: Create modules/shadcn.md**

```markdown
# Shadcn/UI Rules

## Component Usage
- Never modify files inside `components/ui/` — these are generated and will be overwritten.
- Extend behaviour by wrapping `ui/` components in your own `components/` directory.

```tsx
// WRONG: editing ui/button.tsx directly
// RIGHT: wrap it
import { Button } from '@/components/ui/button';

export function PrimaryButton({ children, ...props }) {
  return <Button variant="default" size="lg" {...props}>{children}</Button>;
}
```

## Variants
Use `cva` (class-variance-authority) for variant logic — never inline conditional className strings.
```tsx
import { cva } from 'class-variance-authority';

const badge = cva('rounded-full px-2 py-0.5 text-xs font-medium', {
  variants: {
    status: {
      active: 'bg-green-100 text-green-800',
      inactive: 'bg-gray-100 text-gray-600',
    },
  },
});
```

## Rules
- Install components with `npx shadcn@latest add <component>` — never copy-paste from docs.
- Keep `components/ui/` in `.gitignore` awareness — treat as vendor code.
- Use `cn()` utility for merging Tailwind classes, not manual string concatenation.
```

- [ ] **Step 8: Create modules/saas.md**

```markdown
# SaaS Rules

## Multi-Tenancy
Every database query that touches tenant data MUST filter by `org_id` or `team_id`. Never return rows across tenant boundaries.
```ts
// WRONG
const items = await db.select().from(items);

// RIGHT
const items = await db.select().from(items).where(eq(items.orgId, currentOrgId));
```

## Authentication Flow
1. Sign up → create user record → create org → create membership
2. Sign in → verify identity → load org membership → set session
3. Invitation → create pending invite → email link → accept → create membership

## Feature Flags
Gate new features behind flags from day one. Never release directly to all users.
```ts
if (await featureEnabled('new-dashboard', orgId)) {
  return <NewDashboard />;
}
return <LegacyDashboard />;
```

## Billing
- Sync subscription state from Stripe webhooks to your DB. Never compute plan limits from Stripe API at request time.
- Store: `plan`, `status`, `current_period_end`, `stripe_customer_id`, `stripe_subscription_id`.
- Check limits server-side before every usage-gated operation.

## Rules
- All admin routes must check `role === 'admin'` server-side — never client-side only.
- Soft-delete users and orgs — never hard delete (compliance, audit trail).
- Every API endpoint must be authenticated. No public endpoints unless intentionally designed as such.
```

- [ ] **Step 9: Create modules/ecommerce.md**

```markdown
# E-commerce Rules

## Cart
- Cart state lives server-side (DB or Redis). Client-side cart is a cache only.
- Validate cart contents and prices server-side at checkout — never trust client prices.

## Checkout Flow
1. Cart review → shipping address → payment → order confirmation
2. Never skip server-side inventory check before charging.
3. Create the order record AFTER payment succeeds, not before.

## Payments
- Use Stripe Payment Intents — never Charges API (deprecated).
- Handle webhooks for: `payment_intent.succeeded`, `payment_intent.payment_failed`, `charge.refunded`.
- Idempotency: use `idempotency_key` on all Stripe API calls.

## Inventory
```ts
// Atomic stock decrement — prevent overselling
await db.transaction(async (tx) => {
  const [product] = await tx
    .select()
    .from(products)
    .where(eq(products.id, productId))
    .for('update');
  if (product.stock < quantity) throw new Error('Insufficient stock');
  await tx.update(products)
    .set({ stock: product.stock - quantity })
    .where(eq(products.id, productId));
});
```

## Rules
- Never log full card numbers or CVV — ever.
- PCI compliance: use Stripe Elements or Stripe Checkout, never build your own card form.
- All prices are stored as integers (cents) in the DB — never floats.
```

- [ ] **Step 10: Create modules/claude-code.md**

```markdown
# Claude Code Rules

## CLAUDE.md
Every project must have a `CLAUDE.md` at the repo root. It is loaded into every Claude Code session automatically.

Sections to include:
- Build, test, and lint commands
- Architecture overview (1–2 paragraphs)
- Key file locations
- Any non-obvious conventions

## Memory System
Use `~/.claude/projects/<project>/memory/` for persistent facts across sessions.
- `user_*.md` — user profile and preferences
- `feedback_*.md` — corrections and confirmed approaches
- `project_*.md` — project goals, decisions, deadlines
- `MEMORY.md` — index file (one line per memory)

## Tool Use
- Read files before editing them — never guess at content.
- Prefer dedicated tools (Read, Edit, Grep, Glob) over Bash for file operations.
- Use parallel tool calls for independent operations.

## Commit Convention
End every commit message with:
```
Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

## Rules
- Do not add features, refactor, or "improve" code beyond what was asked.
- Do not add comments or docstrings to code you did not change.
- Verify before claiming work is complete — run the tests, check the output.
```

- [ ] **Step 11: Create modules/agentic.md**

```markdown
# Agentic Coding Rules

## Plan Before You Build
For any task requiring 3+ file changes, write a plan first. Get approval before touching code.

## Small, Reversible Steps
- One logical change per commit.
- If a step fails, revert and diagnose before retrying.
- Never run destructive operations (reset --hard, rm -rf, DROP TABLE) without explicit user approval.

## Context Hygiene
- At the start of each task, read the files you will change. Never edit from memory.
- At the end of each task, verify: run tests, check output, confirm the file was actually written.

## Tool Discipline
- Search before creating — use Grep/Glob to check if something already exists.
- Prefer reading small focused files over large ones. If a file is >500 lines, read only the relevant section.
- Never call the same failing tool twice without changing something.

## Verification Loop
```
implement → run test → if fail: diagnose → minimal fix → run test again
                     → if pass: commit → next task
```

## Rules
- Do not retry a failing command identically — read the error first.
- Do not batch multiple unrelated changes into one commit.
- If blocked after 2 attempts, stop and ask the user — do not thrash.
```

- [ ] **Step 12: Update core.md with full content**

Replace `modules/core.md` with:
```markdown
# Core Rules

## Search Before You Build
Before implementing anything, search the codebase for existing solutions.
1. Search for related functionality by keyword
2. List the directory you're about to create a file in
3. Check for similar files by name pattern

Never create a new file if an existing one can be enhanced.

## Pre-Implementation Checklist
Before writing ANY code:
- [ ] I have read the files I am about to change
- [ ] I have searched for existing implementations
- [ ] I understand why a new file is needed (if creating one)
- [ ] I have a clear picture of inputs, outputs, and side effects

## Implementation Discipline
- Write the smallest change that achieves the goal.
- Do not add features, error handling, or abstractions beyond what was asked.
- Do not refactor surrounding code unless it directly blocks the task.
- Do not add comments or docstrings to code you did not write.

## Commit Hygiene
- One logical change per commit.
- Commit message describes WHY, not WHAT.
- Stage specific files — never `git add .` blindly.

## Verification
Before claiming a task is done:
1. Run the tests
2. Read the output
3. Check that the file was written / the change was applied

"It should work" is not verification. Evidence is.
```

- [ ] **Step 13: Commit all modules**
```bash
cd C:/Dev/cursor-rules
git add modules/
git commit -m "feat: add 12 rule modules for core, ts, nextjs, fastapi, flutter, supabase, drizzle, shadcn, saas, ecommerce, claude-code, agentic"
```

---

## Chunk 5: CLI

### Task 5: cli.js — interactive entry point

**Files:**
- Create: `src/cli.js`
- Create: `tests/init.test.js`

- [ ] **Step 1: Write integration test**

Create `tests/init.test.js`:
```js
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, rmSync, existsSync, writeFileSync } from 'fs';
import { join } from 'path';
import { tmpdir } from 'os';
import { composeModules, writeRules } from '../src/compose.js';
import { detectModules } from '../src/detect.js';

// Integration: detect + compose + write pipeline (no CLI prompts)
test('full pipeline: detect nextjs project and write .cursorrules', () => {
  const tmp = mkdtempSync(join(tmpdir(), 'cursor-int-'));
  try {
    // Write a minimal Next.js package.json into tmp
    writeFileSync(join(tmp, 'package.json'), JSON.stringify({
      dependencies: { next: '^14', typescript: '^5' }
    }));

    const modules = detectModules(tmp);
    assert.ok(modules.includes('core'));
    assert.ok(modules.includes('nextjs'));
    assert.ok(modules.includes('typescript'));

    const content = composeModules(modules);
    assert.ok(content.length > 100);

    const outPath = writeRules(content, 'legacy', tmp);
    assert.ok(existsSync(outPath));
  } finally {
    rmSync(tmp, { recursive: true });
  }
});
```

- [ ] **Step 2: Run integration test — expect PASS** (uses already-implemented detect + compose)
```bash
cd C:/Dev/cursor-rules
node --test tests/init.test.js
```
Expected: 1 test passes.

- [ ] **Step 3: Create src/cli.js**

Create `src/cli.js`:
```js
#!/usr/bin/env node
import prompts from 'prompts';
import kleur from 'kleur';
import { existsSync } from 'fs';
import { detectModules } from './detect.js';
import { composeModules, writeRules, estimateTokens } from './compose.js';

const ALL_MODULES = [
  'core',
  'typescript',
  'nextjs',
  'fastapi',
  'flutter',
  'supabase',
  'drizzle',
  'shadcn',
  'saas',
  'ecommerce',
  'claude-code',
  'agentic',
];

// Exit cleanly on Ctrl+C
process.on('SIGINT', () => {
  console.log(kleur.yellow('\nCancelled.'));
  process.exit(0);
});

async function main() {
  // Node version check
  const [major] = process.versions.node.split('.').map(Number);
  if (major < 18) {
    console.error(
      kleur.red('cursor-rules requires Node 18+. Download at https://nodejs.org')
    );
    process.exit(1);
  }

  console.log(kleur.bold('\ncursor-rules init\n'));

  const detected = detectModules();

  if (detected.length > 1) {
    console.log(kleur.green('Detected: ') + detected.join(', ') + '\n');
  } else {
    console.log(kleur.dim('No stack detected — select modules manually.\n'));
  }

  // Module selection
  const { selected } = await prompts({
    type: 'multiselect',
    name: 'selected',
    message: 'Select modules to include (space to toggle, enter to confirm):',
    choices: ALL_MODULES.map((m) => ({
      title: m,
      value: m,
      selected: detected.includes(m),
    })),
    min: 1,
  });

  if (!selected || selected.length === 0) {
    console.log(kleur.yellow('No modules selected. Exiting.'));
    process.exit(0);
  }

  // Format selection
  const { format } = await prompts({
    type: 'select',
    name: 'format',
    message: 'Output format:',
    choices: [
      {
        title: '.cursorrules  (legacy — works with all Cursor versions)',
        value: 'legacy',
      },
      {
        title: '.cursor/rules/project.mdc  (2026 format)',
        value: 'mdc',
      },
    ],
  });

  if (!format) process.exit(0);

  const outFile =
    format === 'mdc' ? '.cursor/rules/project.mdc' : '.cursorrules';

  // Overwrite check
  if (existsSync(outFile)) {
    const { action } = await prompts({
      type: 'select',
      name: 'action',
      message: `${kleur.yellow(outFile)} already exists:`,
      choices: [
        { title: 'Overwrite', value: 'overwrite' },
        { title: 'Cancel', value: 'cancel' },
      ],
      initial: 1,
    });
    if (!action || action === 'cancel') {
      console.log(kleur.yellow('Cancelled.'));
      process.exit(0);
    }
  }

  const content = composeModules(selected);
  const outPath = writeRules(content, format);
  const tokens = estimateTokens(content);

  console.log(
    '\n' +
      kleur.green('Done! ') +
      `Written to ${kleur.bold(outPath)} (~${tokens} tokens)`
  );
  console.log(kleur.dim('Restart Cursor to apply changes.\n'));
}

main().catch((err) => {
  console.error(kleur.red('Error: ') + err.message);
  process.exit(1);
});
```

- [ ] **Step 4: Make cli.js executable and smoke-test it**
```bash
cd C:/Dev/cursor-rules
node src/cli.js --help 2>&1 || node src/cli.js
```
Expected: The interactive prompt appears asking to select modules.
Press Ctrl+C to exit — it should print "Cancelled." and exit 0.

- [ ] **Step 5: Run all tests**
```bash
cd C:/Dev/cursor-rules
node --test tests/detect.test.js tests/compose.test.js tests/init.test.js
```
Expected: all tests pass.

- [ ] **Step 6: Commit**
```bash
git add src/cli.js tests/init.test.js
git commit -m "feat: add interactive CLI entry point"
```

---

## Chunk 6: Repo Housekeeping

### Task 6: README, GitHub metadata, npm publish prep

**Files:**
- Modify: `README.md`
- Modify: `.gitignore`

- [ ] **Step 1: Update .gitignore**

Add to `.gitignore`:
```
node_modules/
.cursorrules
.cursor/
```

- [ ] **Step 2: Rewrite README.md**

Replace the full content of `README.md` with:
```markdown
# cursor-rules

Auto-detect your project's stack and generate a tailored `.cursorrules` file in one command. No cloning required.

```bash
npx cursor-rules init
```

## What it does

1. Scans your project for `package.json`, `requirements.txt`, `pubspec.yaml`, etc.
2. Shows which modules were detected, lets you toggle extras
3. Writes a composed `.cursorrules` (or `.cursor/rules/project.mdc`) to your project root

## Detected stacks

| File found | Modules added |
|-----------|--------------|
| `next` in package.json | `nextjs` + `typescript` |
| `typescript` in package.json | `typescript` |
| `@supabase/supabase-js` | `supabase` |
| `drizzle-orm` | `drizzle` |
| `shadcn` / `@radix-ui` | `shadcn` |
| `fastapi` in requirements.txt | `fastapi` |
| `flutter:` in pubspec.yaml | `flutter` |

`core` is always included.

## Optional modules

Select these manually during `init`:

- `saas` — multi-tenancy, billing, feature flags
- `ecommerce` — cart, checkout, inventory, payments
- `claude-code` — CLAUDE.md conventions, memory system
- `agentic` — agent loop patterns, tool use safety

## Output formats

- `.cursorrules` — legacy format, works with all Cursor versions
- `.cursor/rules/project.mdc` — 2026 format for Cursor 0.45+

## Requirements

Node.js 18+

## Modular builder (advanced)

If you prefer to compose rules with a script instead of `npx`, the original
`build-rules.ps1` (Windows) and `build-rules.sh` (Unix) are still available.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Adding a new module is just adding a `.md` file to `modules/`.

## License

MIT
```

- [ ] **Step 3: Update GitHub repo metadata via gh CLI**
```bash
gh repo edit Kabi10/Cursor \
  --name cursor-rules \
  --description "Auto-detect your stack and generate a tailored .cursorrules file. npx cursor-rules init" \
  --add-topic cursor \
  --add-topic cursorrules \
  --add-topic cursor-ai \
  --add-topic developer-tools \
  --add-topic cli \
  --add-topic npm
```
Expected: no error, repo renamed on GitHub.

- [ ] **Step 4: Update git remote to match new repo name**
```bash
cd C:/Dev/cursor-rules
git remote set-url origin https://github.com/Kabi10/cursor-rules.git
```

- [ ] **Step 5: Commit README and .gitignore changes**
```bash
git add README.md .gitignore
git commit -m "docs: rewrite README for npm audience and update .gitignore"
```

- [ ] **Step 6: Push to GitHub**
```bash
git push origin main
```

---

## Chunk 7: npm Publish

### Task 7: Publish to npm

- [ ] **Step 1: Check npm login**
```bash
npm whoami
```
Expected: your npm username. If not logged in: `npm login`

- [ ] **Step 2: Dry run to verify package contents**
```bash
cd C:/Dev/cursor-rules
npm pack --dry-run
```
Expected: output lists `src/cli.js`, `src/detect.js`, `src/compose.js`, and all 12 files in `modules/`. Confirm `node_modules/` is NOT listed.

- [ ] **Step 3: Publish**
```bash
npm publish --access public
```
Expected: `npm notice Publishing to https://registry.npmjs.org/ ...` followed by success message.

- [ ] **Step 4: Smoke-test the published package**

In a separate temporary directory:
```bash
mkdir /tmp/test-project && cd /tmp/test-project
echo '{"dependencies":{"next":"^14","typescript":"^5"}}' > package.json
npx cursor-rules init
```
Expected: prompts appear, detected modules include `nextjs` and `typescript`.

- [ ] **Step 5: Final commit and tag**
```bash
cd C:/Dev/cursor-rules
git tag v1.0.0
git push origin main --tags
```

---

## Chunk 8: Distribution

### Task 8: Submit to awesome-cursorrules

- [ ] **Step 1: Fork PatrickJS/awesome-cursorrules on GitHub**
```bash
gh repo fork PatrickJS/awesome-cursorrules --clone=false
```

- [ ] **Step 2: Find the right place in their README**
```bash
gh api repos/PatrickJS/awesome-cursorrules/contents/README.md \
  | python3 -c "import sys,json,base64; d=json.load(sys.stdin); print(base64.b64decode(d['content']).decode())" \
  | grep -n "tool\|builder\|generator" | head -20
```

- [ ] **Step 3: Create PR adding cursor-rules to their Related Tools section**
```bash
gh pr create \
  --repo PatrickJS/awesome-cursorrules \
  --title "Add cursor-rules: npm package that auto-detects stack and generates .cursorrules" \
  --body "**cursor-rules** auto-detects your project's stack (Next.js, FastAPI, Flutter, etc.) and generates a composed \`.cursorrules\` file via \`npx cursor-rules init\`. No cloning required.

GitHub: https://github.com/Kabi10/cursor-rules
npm: https://www.npmjs.com/package/cursor-rules"
```

---

## Self-Review

**Spec coverage check:**
- Auto-detection from project files → Task 2 (detect.js)
- Interactive module selector → Task 5 (cli.js)
- 12 modules → Task 4 (all 12 written with actual content)
- Both output formats → Task 3 (compose.js writeRules)
- Backwards-compatible scripts kept → not touched anywhere
- Repo rename + topics → Task 6
- npm publish → Task 7
- Submit to awesome-cursorrules → Task 8
- README rewrite → Task 6

**Placeholder scan:** No TBDs, no "implement later", all code blocks are complete.

**Type consistency:**
- `detectModules(cwd?)` → `string[]` — consistent across detect.js, tests, cli.js
- `composeModules(moduleIds)` → `string` — consistent
- `writeRules(content, format, cwd?)` → `string` (path) — consistent
- `estimateTokens(text)` → `number` — consistent
- `format` is `'legacy' | 'mdc'` throughout

All good.
