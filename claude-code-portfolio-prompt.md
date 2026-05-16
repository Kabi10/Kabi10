# Claude Code Prompt — Fiverr Portfolio Extractor
# Paste this entire block into Claude Code

I need you to scan all my GitHub repositories, analyze each one, and generate Fiverr portfolio entries for the best candidates. Here's the plan:

## Phase 1: Discovery

Use the GitHub CLI (`gh`) to list all my repos. Run:

```
gh repo list --limit 50 --json name,description,primaryLanguage,updatedAt,createdAt,url --jq '.[] | [.name, .primaryLanguage.name // "N/A", .description // "No description", .createdAt, .updatedAt, .url] | @tsv'
```

If `gh` isn't authenticated, run `gh auth login` first and follow the prompts.

Save the output to `~/fiverr-portfolio/repos-discovery.txt`.

## Phase 2: Deep Scan

For each repo found, clone it into a temp directory and extract:

```bash
mkdir -p ~/fiverr-portfolio/scans
```

For each repo, run this sequence:
1. `gh repo clone <repo-name> ~/fiverr-portfolio/temp-scan --depth 1`
2. Check for and cat these files (if they exist):
   - `package.json` (look at dependencies and devDependencies)
   - `requirements.txt` or `pyproject.toml` or `Pipfile`
   - `build.gradle` or `build.gradle.kts`
   - `pubspec.yaml`
   - `docker-compose.yml` or `Dockerfile`
   - `README.md` (first 100 lines)
   - Any `.gs` files (Google Apps Script)
   - Any `mcp` references: `grep -r "mcp\|model.context.protocol\|fastmcp\|@modelcontextprotocol" --include="*.py" --include="*.ts" --include="*.js" --include="*.json" -l`
   - Any AI/LLM references: `grep -r "openai\|anthropic\|langchain\|llamaindex\|chromadb\|pinecone\|rag\|embedding" --include="*.py" --include="*.ts" --include="*.js" --include="*.json" -l`
   - Any FastAPI references: `grep -r "fastapi\|FastAPI\|from fastapi" --include="*.py" -l`
3. Get the directory tree: `find . -type f -not -path '*/node_modules/*' -not -path '*/.git/*' -not -path '*/build/*' -not -path '*/__pycache__/*' | head -80`
4. Get first and last commit dates: `git log --format="%ai" --reverse | head -1` and `git log --format="%ai" -1`
5. Save all findings to `~/fiverr-portfolio/scans/<repo-name>.txt`
6. `rm -rf ~/fiverr-portfolio/temp-scan`

## Phase 3: Classification

After scanning all repos, classify each one into these categories:

**HIGH PRIORITY** — Directly supports one of my 4 Fiverr gigs:
- Gig 1: Google Apps Script + AI Automation (look for: .gs files, Google APIs, Sheets automation, Apps Script)
- Gig 2: MCP Server Development (look for: MCP protocol, fastmcp, claude tools, model context protocol)
- Gig 3: FastAPI AI Backend (look for: FastAPI, Pydantic, async Python APIs, AI/LLM integration)
- Gig 4: RAG Document Q&A (look for: LangChain, LlamaIndex, ChromaDB, Pinecone, vector embeddings, document processing)

**MEDIUM PRIORITY** — Demonstrates relevant skills:
- Android/Kotlin apps (especially with AI features or Supabase)
- Telegram bots with AI
- Any multi-AI consultation tools
- Data dashboards or pipelines

**LOW PRIORITY / SKIP** — Not useful for Fiverr portfolio:
- Incomplete/abandoned repos (fewer than 5 commits)
- Tutorial follow-alongs
- Forks with no modifications
- Pure config or dotfile repos

Save the classification to `~/fiverr-portfolio/classification.md`.

## Phase 4: Generate Portfolio Entries

For each HIGH and MEDIUM priority project, generate a Fiverr portfolio entry in this exact format:

```
═══════════════════════════════════════
PROJECT: [Repo Name]
MAPS TO: Gig 1 / Gig 2 / Gig 3 / Gig 4
PRIORITY: HIGH / MEDIUM
═══════════════════════════════════════

FIVERR FORM FIELDS:

Project name (max 50 chars):
[Client-facing name, NOT the repo name. Example: "AI-Powered Inventory Tracking Dashboard"]

Industry:
[Best fit: Technology / E-commerce / Healthcare / Agriculture / Education / Entertainment / Finance / Marketing / Manufacturing / Other]

Project duration:
[Calculate from first commit to last meaningful commit]

Project cost:
[Fiverr-equivalent price: simple scripts $200-$500, full apps $500-$2,000, complex systems $2,000-$5,000]

Project started on:
[MM/YYYY from first commit]

Project description (max 1400 chars):
[Write as a Fiverr portfolio piece in first person. Structure:
- What was built and the problem it solves
- Specific technical deliverables  
- Tech stack
- Key result or capability

RULES:
- Do NOT mention GitHub, open source, personal project, or learning exercise
- Frame personal projects as "internal tools" or "proof-of-concept systems"
- Frame everything as professional deliverables
- Write in first person: "I built..."
- Keep under 1400 characters exactly]

TECH STACK:
[Every language, framework, library, API detected from actual dependency files]

SCREENSHOT SUGGESTIONS:
[2-3 specific screens, components, or outputs to capture. Be specific — name routes, components, or files that have visual output]
```

Save all entries to `~/fiverr-portfolio/portfolio-entries.md`.

## Phase 5: Optional — Use Gemini CLI for deeper analysis

If `gemini` CLI is installed (check with `which gemini`), for each HIGH priority repo, you can pipe the scan results to Gemini for a second opinion:

```bash
cat ~/fiverr-portfolio/scans/<repo-name>.txt | gemini -p "Based on this repository scan, write a 1400-character Fiverr portfolio description. Frame it as client work, first person, professional tone. Focus on: what was built, technical deliverables, tech stack, business value."
```

If Gemini CLI is not installed but you want to use it:
```bash
npm install -g @anthropic-ai/gemini-cli
# OR
npx @google/gemini-cli
```

If neither Gemini nor DeepSeek CLI is available, that's fine — proceed with your own analysis. The GitHub data is sufficient.

## Phase 6: Summary

At the end, create a final summary file at `~/fiverr-portfolio/SUMMARY.md`:

```markdown
# Fiverr Portfolio — Ready to Upload

## Repos Scanned: X
## High Priority: X  
## Medium Priority: X
## Skipped: X

## Recommended Portfolio Pieces (upload these first):

| # | Project Name (50 chars) | Maps to Gig | Industry | Cost | Has Screenshots? |
|---|---|---|---|---|---|

## Portfolio Gaps:
[Identify which gigs still need portfolio proof and suggest what to build]

## Next Steps:
[Specific actions — which screenshots to take, which demos to build]
```

---

## Important notes:

- Work through all phases sequentially. Don't skip the discovery step.
- If any repo requires authentication or is private, `gh` will handle it since you're already authenticated.
- Keep the temp-scan cleanup step — don't leave 34 cloned repos on disk.
- If a repo is very large (>500MB), skip the clone and just use `gh api` to fetch key files instead.
- The most important output is `portfolio-entries.md` — that's what I'll paste into Fiverr.
