#!/usr/bin/env bash
# pipeline.sh — Three-stage AI pipeline:
#   Stage 1: Gemini 2.5 Pro (full repo scan) → codebase analysis
#   Stage 2: DeepSeek R1 API (analysis + task) → implementation spec
#   Stage 3: Claude Code headless (spec) → execution
#
# Usage:
#   ./scripts/pipeline.sh "Add a favorites screen with offline support"
#   ./scripts/pipeline.sh "Fix sync conflict in SyncManager" --dry-run
#   ./scripts/pipeline.sh "Describe X" --analyze-only
#   DEEPSEEK_API_KEY=sk-xxx ./scripts/pipeline.sh "..."
#
# Options:
#   --dry-run        Stop after DeepSeek spec, print it, no Claude execution
#   --analyze-only   Stop after Gemini analysis, print it

set -euo pipefail

# ── Config ─────────────────────────────────────────────────────────────────
TASK="${1:-}"
DRY_RUN=false
ANALYZE_ONLY=false
TS=$(date +%s)
GEMINI_FILE="/tmp/agrimarket_gemini_${TS}.md"
GEMINI_RAW="/tmp/agrimarket_gemini_raw_${TS}.md"   # streaming scratch file
SPEC_FILE="/tmp/agrimarket_spec_${TS}.md"
API_KEY="${DEEPSEEK_API_KEY:-sk-d66970b2008048559766a7958f455cde}"
DEEPSEEK_ENDPOINT="https://api.deepseek.com/v1/chat/completions"
MODEL="deepseek-reasoner"

# ── Live dashboard config ───────────────────────────────────────────────────
AIPIPE_PORT="${AIPIPE_PORT:-4242}"
AIPIPE_URL="http://127.0.0.1:${AIPIPE_PORT}"
AIPIPE_ENABLED=true
AIPIPE_PID=""

# ── Arg parsing ────────────────────────────────────────────────────────────
for arg in "$@"; do
  [[ "$arg" == "--dry-run" ]]      && DRY_RUN=true
  [[ "$arg" == "--analyze-only" ]] && ANALYZE_ONLY=true
done

if [[ -z "$TASK" ]]; then
  echo "Usage: $0 \"<task description>\" [--dry-run|--analyze-only]" >&2
  exit 1
fi

# ── Helpers ────────────────────────────────────────────────────────────────
info()    { echo -e "\033[0;36m[pipeline]\033[0m $*"; }
success() { echo -e "\033[0;32m[pipeline]\033[0m $*"; }
warn()    { echo -e "\033[0;33m[pipeline]\033[0m $*"; }
die()     { echo -e "\033[0;31m[pipeline] ERROR:\033[0m $*" >&2; exit 1; }
sep()     { echo "─────────────────────────────────────────────────────"; }

# ── Live dashboard helpers ──────────────────────────────────────────────────
# All functions silently no-op when AIPIPE_ENABLED != true or on curl failure.

_aipipe_post() {
  # _aipipe_post /path '{"json":"body"}'
  [[ "$AIPIPE_ENABLED" != true ]] && return 0
  curl -s -X POST "${AIPIPE_URL}${1}" \
    -H "Content-Type: application/json" \
    --max-time 3 -d "$2" >/dev/null 2>&1 || true
}

_aipipe_json_from_file() {
  # Emit {"content":"<file contents escaped>"} to stdout
  local file="$1"
  [[ -f "$file" ]] || return 0
  $PYTHON -c "
import json, sys
try:
    content = open(sys.argv[1], encoding='utf-8', errors='replace').read()
    sys.stdout.write(json.dumps({'content': content}))
except Exception as e:
    sys.stderr.write(str(e) + '\n')
" "$file" 2>/dev/null || true
}

_aipipe_stage_start() {
  _aipipe_post "/api/stage/${1}/start" '{}'
}

_aipipe_stage_update() {
  # _aipipe_stage_update <stage_num> <content_file>
  [[ "$AIPIPE_ENABLED" != true ]] && return 0
  local payload; payload=$(_aipipe_json_from_file "$2") || return 0
  [[ -z "$payload" ]] && return 0
  _aipipe_post "/api/stage/${1}/update" "$payload"
}

_aipipe_stage_complete() {
  # _aipipe_stage_complete <stage_num> <content_file>
  [[ "$AIPIPE_ENABLED" != true ]] && return 0
  local payload; payload=$(_aipipe_json_from_file "$2") || return 0
  _aipipe_post "/api/stage/${1}/complete" "${payload:-{\}}"
}

_aipipe_stage_error() {
  # _aipipe_stage_error <stage_num> <message>
  [[ "$AIPIPE_ENABLED" != true ]] && return 0
  local payload; payload=$($PYTHON -c "import json,sys; print(json.dumps({'error':sys.argv[1]}))" "$2" 2>/dev/null) || return 0
  _aipipe_post "/api/stage/${1}/error" "$payload"
}

# Starts watching a file and streaming its content to the server every 2s.
# Sets the variable named by $3 to the watcher's background PID.
_aipipe_watch_start() {
  local stage_num="$1" watch_file="$2"
  (
    local last_size=0
    while true; do
      sleep 2
      if [[ -f "$watch_file" ]]; then
        local cur_size
        cur_size=$(wc -c < "$watch_file" 2>/dev/null || echo 0)
        if [[ "$cur_size" -gt "$last_size" ]]; then
          _aipipe_stage_update "$stage_num" "$watch_file"
          last_size=$cur_size
        fi
      fi
    done
  ) &
  # Caller captures $! immediately after this call, before doing anything else
}

_aipipe_watch_stop() {
  local pid="${1:-}"
  [[ -n "$pid" ]] && kill "$pid" 2>/dev/null || true
}

# Start the live dashboard server, open browser, wait until ready.
_aipipe_server_start() {
  [[ "$AIPIPE_ENABLED" != true ]] && return 0
  local script_dir
  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  local server_js="${script_dir}/aipipe-server.js"
  if [[ ! -f "$server_js" ]]; then
    warn "aipipe-server.js not found at ${server_js} — dashboard disabled"
    AIPIPE_ENABLED=false; return 0
  fi
  command -v node >/dev/null 2>&1 || { warn "node not found — dashboard disabled"; AIPIPE_ENABLED=false; return 0; }

  PIPELINE_TITLE="$TASK" AIPIPE_PORT="$AIPIPE_PORT" \
    node "$server_js" &
  AIPIPE_PID=$!

  # Wait up to 5 s for the server to accept connections
  local tries=0
  while [[ $tries -lt 10 ]]; do
    sleep 0.5
    curl -s --max-time 1 "${AIPIPE_URL}/api/status" >/dev/null 2>&1 && return 0
    ((tries++)) || true
  done
  warn "aipipe server didn't start in time — dashboard may not update"
}

_aipipe_server_stop() {
  [[ -n "$AIPIPE_PID" ]] && kill "$AIPIPE_PID" 2>/dev/null || true
  AIPIPE_PID=""
}

# ── Dependency checks ──────────────────────────────────────────────────────
command -v gemini >/dev/null 2>&1 || die "gemini CLI is required (npm install -g @google/gemini-cli)"
command -v curl   >/dev/null 2>&1 || die "curl is required"
command -v python3 >/dev/null 2>&1 || command -v python >/dev/null 2>&1 || die "python3 is required"
PYTHON=$(command -v python3 2>/dev/null || command -v python)
if [[ "$DRY_RUN" == false && "$ANALYZE_ONLY" == false ]]; then
  command -v claude >/dev/null 2>&1 || die "claude CLI is required for execution stage"
fi

# ── Repo root ──────────────────────────────────────────────────────────────
REPO_ROOT="$(git -C "$(dirname "$0")/.." rev-parse --show-toplevel 2>/dev/null || pwd)"

# ── Start live dashboard (opens browser, waits for server) ─────────────────
trap '_aipipe_server_stop' EXIT INT TERM
_aipipe_server_start

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 1 — Gemini 2.5 Pro: Full Codebase Analysis
# ══════════════════════════════════════════════════════════════════════════════
info "Stage 1: Gemini 2.5 Pro — full codebase analysis..."
_aipipe_stage_start 1

GEMINI_PROMPT="You are analysing the Agrimarket repository to prepare context for an AI coding agent.

Task the agent will implement: ${TASK}

Your job (analysis only — no implementation):
1. Read CLAUDE.md for architecture rules and constraints.
2. Identify all files relevant to the task (list exact repo-relative paths).
3. Read those files. For each, describe: current structure, key patterns, data flow, public API.
4. Describe what must NOT be broken: offline sync guarantees, Room/Flow patterns, Hilt wiring, existing navigation graph.
5. Note any patterns the implementation must follow (e.g. how ListingRepository does offline-first, how ViewModels expose StateFlow).

Output format: structured markdown with these exact sections:
## Relevant Files
## Architecture Patterns
## Data Flow
## Constraints & Must-Nots
## Implementation Notes

Be concrete and specific — file names, class names, function names. No implementation code."

# Run Gemini, stream output to GEMINI_RAW in real-time; start dashboard watcher
_aipipe_watch_start 1 "$GEMINI_RAW"
_GEMINI_WATCHER=$!

GEMINI_ANALYSIS=$(cd "$REPO_ROOT" && gemini -m gemini-2.5-pro --yolo -p "$GEMINI_PROMPT" 2>/dev/null \
  | sed 's/\x1b\[[0-9;]*m//g' | tee "$GEMINI_RAW") || true

if [[ -z "$GEMINI_ANALYSIS" ]]; then
  # Retry without --yolo in case the flag isn't supported
  GEMINI_ANALYSIS=$(cd "$REPO_ROOT" && gemini -m gemini-2.5-pro -p "$GEMINI_PROMPT" 2>/dev/null \
    | sed 's/\x1b\[[0-9;]*m//g' | tee "$GEMINI_RAW") || true
fi

_aipipe_watch_stop "$_GEMINI_WATCHER"

if [[ -z "$GEMINI_ANALYSIS" ]]; then
  _aipipe_stage_error 1 "Gemini returned empty output"
  die "Gemini returned empty output. Check that 'gemini' CLI is authenticated and working."
fi

# Save formatted output to file
{
  echo "# Gemini Codebase Analysis"
  echo "# Task: $TASK"
  echo "# Generated: $(date -u '+%Y-%m-%d %H:%M UTC') by gemini-2.5-pro"
  echo ""
  echo "$GEMINI_ANALYSIS"
} > "$GEMINI_FILE"

_aipipe_stage_complete 1 "$GEMINI_FILE"
success "Gemini analysis saved to: $GEMINI_FILE"

if [[ "$ANALYZE_ONLY" == true ]]; then
  echo ""
  sep
  cat "$GEMINI_FILE"
  sep
  warn "--analyze-only: stopping after Stage 1."
  exit 0
fi

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 2 — DeepSeek R1: Implementation Spec
# ══════════════════════════════════════════════════════════════════════════════
info "Stage 2: DeepSeek R1 — generating implementation spec..."
_aipipe_stage_start 2

SYSTEM_PROMPT="You are a senior Android/backend engineer who specialises in the Agrimarket app.
Architecture: MVVM + Jetpack Compose + Hilt + Room + Retrofit, offline-first.
Backend: Node.js/Express + Supabase PostgreSQL on Vercel.
Your job: given a Gemini codebase analysis and a task, produce a crisp, actionable IMPLEMENTATION SPEC
that another AI coding agent (Claude Code) will execute verbatim.

The spec MUST include exactly these five sections:
1. **Objective** — one sentence
2. **Affected files** — exact repo-relative paths, one per line, with brief reason
3. **Step-by-step implementation** — numbered, ordered by dependency, no ambiguity
4. **Edge cases & constraints** — offline handling, error states, existing patterns to follow
5. **Verification** — what to run/check to confirm success (build commands, test assertions)

Be concrete. Prefer existing patterns over new abstractions. No markdown fluff."

USER_PROMPT="## Gemini Codebase Analysis
${GEMINI_ANALYSIS}

## Task
${TASK}"

# Build JSON payload (jq handles all escaping)
# Write prompts to temp files — Python handles Unicode (Tamil/Sinhala etc.) correctly
_SYS_FILE=$(mktemp); _USR_FILE=$(mktemp)
printf '%s' "$SYSTEM_PROMPT" > "$_SYS_FILE"
printf '%s' "$USER_PROMPT"   > "$_USR_FILE"
PAYLOAD=$($PYTHON - "$_SYS_FILE" "$_USR_FILE" "$MODEL" <<'PYEOF'
import json, sys
sys_c = open(sys.argv[1], encoding='utf-8').read()
usr_c = open(sys.argv[2], encoding='utf-8').read()
p = {"model": sys.argv[3], "messages": [{"role": "system", "content": sys_c}, {"role": "user", "content": usr_c}], "max_tokens": 4096, "temperature": 0.1}
sys.stdout.write(json.dumps(p))
PYEOF
)
rm -f "$_SYS_FILE" "$_USR_FILE"

HTTP_RESPONSE=$(curl -s -w "\n%{http_code}" \
  -X POST "$DEEPSEEK_ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_KEY" \
  -d "$PAYLOAD")

HTTP_BODY=$(echo "$HTTP_RESPONSE" | head -n -1)
HTTP_CODE=$(echo "$HTTP_RESPONSE" | tail -n 1)

[[ "$HTTP_CODE" != "200" ]] && die "DeepSeek API returned HTTP $HTTP_CODE:\n$HTTP_BODY"

SPEC=$(echo "$HTTP_BODY" | $PYTHON -c "
import json,sys
try:
    j=json.load(sys.stdin)
    sys.stdout.write(j['choices'][0]['message']['content'] or '')
except: pass
")
if [[ -z "$SPEC" ]]; then
  _aipipe_stage_error 2 "DeepSeek returned empty content"
  die "DeepSeek returned empty content. Response:\n$HTTP_BODY"
fi

# Save spec to file
{
  echo "# Implementation Spec"
  echo "# Task: $TASK"
  echo "# Generated: $(date -u '+%Y-%m-%d %H:%M UTC') by DeepSeek R1"
  echo "# Gemini analysis: $GEMINI_FILE"
  echo ""
  echo "$SPEC"
} > "$SPEC_FILE"

_aipipe_stage_complete 2 "$SPEC_FILE"
success "Spec saved to: $SPEC_FILE"
echo ""
sep
cat "$SPEC_FILE"
sep
echo ""

if [[ "$DRY_RUN" == true ]]; then
  warn "--dry-run: skipping Claude execution stage."
  warn "To execute manually: cat $SPEC_FILE | claude -p \"Execute the plan in stdin\""
  exit 0
fi

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 3 — Claude Code: Headless Execution
# ══════════════════════════════════════════════════════════════════════════════
info "Stage 3: Claude Code — executing spec..."
_aipipe_stage_start 3

# Capture Claude output and stream it to the dashboard in real-time.
# --output-format stream-json emits NDJSON events as Claude works, avoiding
# the full-buffer problem that occurs when stdout is piped (not a TTY).
# The Python parser extracts text from each assistant event and writes to
# CLAUDE_OUT_FILE with explicit flushing so the watcher sees live progress.
CLAUDE_OUT_FILE="/tmp/agrimarket_claude_${TS}.md"
_aipipe_watch_start 3 "$CLAUDE_OUT_FILE"
_CLAUDE_WATCHER=$!

CLAUDE_FAILED=false
claude -p "Execute the implementation plan provided in stdin exactly as specified. Work in the repository at ${REPO_ROOT}. Follow the affected files list and step-by-step plan. After implementation, run the verification steps listed." \
  --output-format stream-json \
  < "$SPEC_FILE" 2>/dev/null | \
$PYTHON -u - "$CLAUDE_OUT_FILE" <<'PYEOF' || CLAUDE_FAILED=true
import json, sys
out = open(sys.argv[1], 'w', encoding='utf-8', buffering=1)
for raw in sys.stdin:
    raw = raw.strip()
    if not raw:
        continue
    try:
        ev = json.loads(raw)
        t = ev.get('type', '')
        if t == 'assistant':
            for block in ev.get('message', {}).get('content', []):
                if isinstance(block, dict) and block.get('type') == 'text':
                    chunk = block['text']
                    out.write(chunk); out.flush()
                    sys.stdout.write(chunk); sys.stdout.flush()
        elif t == 'tool_use':
            line = '\n**[tool]** `' + ev.get('tool_name', '?') + '`\n'
            out.write(line); out.flush()
            sys.stdout.write(line); sys.stdout.flush()
        elif t == 'result':
            txt = ev.get('result', '')
            if txt:
                out.write('\n\n---\n' + txt); out.flush()
                sys.stdout.write(txt + '\n'); sys.stdout.flush()
    except Exception as e:
        sys.stderr.write('[aipipe] stream-parse: ' + str(e) + '\n')
out.close()
PYEOF

_aipipe_watch_stop "$_CLAUDE_WATCHER"

if [[ "$CLAUDE_FAILED" == true ]]; then
  _aipipe_stage_error 3 "Claude Code execution failed"
  die "Claude Code execution failed"
fi

_aipipe_stage_complete 3 "$CLAUDE_OUT_FILE"

success "Pipeline complete."
success "Gemini analysis : $GEMINI_FILE"
success "DeepSeek spec   : $SPEC_FILE"
