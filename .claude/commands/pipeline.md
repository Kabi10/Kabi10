# /pipeline — Run the Gemini→DeepSeek→Claude AI pipeline

Usage: `/pipeline "task description or KAB-N title"`

Token-efficient 3-stage AI pipeline for complex tasks. Use this before coding when the task is ambiguous, spans many files, or requires architectural decisions.

## Stage 1: Gemini Analysis (codebase exploration)

Use `mcp__gemini-bridge-mcp__gemini_execute` with `model: "gemini-2.5-pro"` to:

- Read and analyze relevant source files
- Identify gaps, patterns, and constraints
- Produce a structured findings report

Example prompt for Gemini:

```
Analyze the Agrimarket codebase for [task].
Read these files: [list relevant files].
Identify: current state, gaps, dependencies, risks.
Output structured findings for a DeepSeek implementation spec.
```

## Stage 2: DeepSeek R1 Spec (implementation planning)

Call DeepSeek R1 API directly with Gemini's findings:

```python
import urllib.request, json

payload = {
    "model": "deepseek-reasoner",
    "messages": [
        {"role": "system", "content": "You are a senior Android/Node.js engineer. Produce a concise implementation spec."},
        {"role": "user", "content": f"Gemini findings:\n{gemini_output}\n\nTask: {task}\n\nProduce step-by-step implementation spec with exact file paths and code snippets."}
    ],
    "max_tokens": 4000
}
req = urllib.request.Request(
    "https://api.deepseek.com/v1/chat/completions",
    data=json.dumps(payload).encode(),
    headers={"Authorization": "Bearer <key-from-pipeline.sh>", "Content-Type": "application/json"}
)
with urllib.request.urlopen(req) as r:
    spec = json.loads(r.read())["choices"][0]["message"]["content"]
```

## Stage 3: Claude Implementation

Use the DeepSeek spec as a precise blueprint. Read only the files the spec references; implement surgically without exploration.

## Shell script alternative

```bash
./scripts/pipeline.sh "task description"         # full 3-stage pipeline
./scripts/pipeline.sh "task description" --analyze-only   # Gemini only
./scripts/pipeline.sh "task description" --dry-run        # spec, no code
```

Live dashboard: `node scripts/aipipe-server.js` → http://localhost:4242

## When to use pipeline vs direct coding

| Situation                         | Approach                |
| --------------------------------- | ----------------------- |
| Single-file bug fix               | Code directly           |
| New feature, clear spec           | Code directly           |
| Multi-file feature, unclear scope | `/pipeline` first       |
| Architectural decision needed     | `/pipeline` + plan mode |
| Need to explore unfamiliar area   | `/pipeline` Gemini-only |
