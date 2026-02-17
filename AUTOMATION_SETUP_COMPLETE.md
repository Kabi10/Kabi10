# Claude Code Automation Setup - Complete ✅

## What Was Configured

### 1. Lean CLAUDE.md (21 lines, ~150 tokens)
**Location**: `C:\Dev\Agrimarket\CLAUDE.md`

Reduced from 150 lines to 21 lines containing only:
- Project tech stack (Android MVVM + Compose, Node.js backend)
- Architecture paths and key patterns
- Build commands (`./gradlew assembleDebug`, `npm run dev`, `vercel --prod`)
- Core rules (read patterns first, build + test after changes, plan mode for multi-file work)

### 2. Strict .claudeignore
**Location**: `C:\Dev\Agrimarket\.claudeignore`

Excludes large/unnecessary context:
- `node_modules/`, `package-lock.json` (all projects)
- Build artifacts (`build/`, `dist/`, `.gradle/`, `*.apk`, `*.aab`)
- Generated files (app schemas, logs)
- `.git/`, `.github/`, IDE files
- Large assets (`*.png`, `*.jpg`, `*.mp4`, drawable-xxxhdpi)
- Test reports, coverage
- `notebooklm/` folder (already exported for other use)
- Secrets (`local.properties`, keystore files, `.env.production`)

**Impact**: Saves ~100MB+ from context window

### 3. Automation Hook (PostToolUse)
**Location**: `C:\Dev\Agrimarket\.claude\settings.local.json`

**What it does**: Automatically runs Prettier formatter on every file edited via the Edit tool

**Hook configuration**:
```json
"hooks": {
  "PostToolUse": [
    {
      "matcher": "Edit",
      "hooks": [
        {
          "type": "command",
          "command": "cd C:/Dev/Agrimarket && npx prettier --write \"$FILE_PATH\" 2>&1 || echo 'Prettier not configured'",
          "statusMessage": "Formatting code..."
        }
      ]
    }
  ]
}
```

**Note**: Falls back gracefully if Prettier isn't installed. To enable:
```bash
cd C:/Dev/Agrimarket
npm install --save-dev prettier
```

Then create `.prettierrc`:
```json
{
  "semi": true,
  "singleQuote": false,
  "tabWidth": 2,
  "trailingComma": "es5"
}
```

### 4. Custom Commands
**Location**: `C:\Dev\Agrimarket\.claude\commands/`

#### `/test` - Run Full Test Suite
**File**: `test.md`
**Action**: Runs Android unit tests + backend tests
```bash
./gradlew testDebugUnitTest && cd backend && npm test
```

#### `/doc [file_path]` - Add Documentation
**File**: `doc.md`
**Purpose**: Analyze and add comprehensive inline comments to any file
**Example**:
```
/doc app/src/main/java/com/senthapps/slagrimarket/ui/home/HomeViewModel.kt
```

Adds KDoc/JSDoc style comments explaining classes, functions, parameters, and complex logic.

---

## Environment Variable for Token Efficiency

### Windows (PowerShell - Permanent)
```powershell
[System.Environment]::SetEnvironmentVariable('DISABLE_NON_ESSENTIAL_MODEL_CALLS', '1', 'User')
```

### Windows (Command Prompt - Current Session)
```cmd
set DISABLE_NON_ESSENTIAL_MODEL_CALLS=1
```

### macOS/Linux (Permanent - add to ~/.zshrc or ~/.bashrc)
```bash
export DISABLE_NON_ESSENTIAL_MODEL_CALLS=1
```

### macOS/Linux (Current Session)
```bash
export DISABLE_NON_ESSENTIAL_MODEL_CALLS=1
```

**What it does**: Disables non-critical AI model calls that aren't directly related to your task execution (e.g., context summarization suggestions, optional analytics).

---

## Next Steps

1. **Install Prettier** (optional, for auto-formatting hook):
   ```bash
   cd C:/Dev/Agrimarket
   npm install --save-dev prettier
   ```

2. **Set environment variable** using command above for your OS

3. **Test custom commands**:
   ```
   /test
   /doc app/src/main/java/com/senthapps/slagrimarket/data/repository/ListingRepository.kt
   ```

4. **Verify .claudeignore**:
   - Run `@` in chat and verify no `node_modules/` files appear
   - Check context size is significantly smaller

---

## Token Savings Estimate

| Optimization | Estimated Savings |
|--------------|------------------|
| .claudeignore (excludes ~100MB) | ~500K-1M tokens |
| Lean CLAUDE.md (150 → 21 lines) | ~800 tokens per read |
| Auto-formatting (no manual requests) | ~50-100 tokens per edit |
| ENV var (fewer model calls) | ~10-20% overall reduction |

**Total**: Should see 15-25% reduction in token usage per session with faster response times.

---

## Troubleshooting

**Hook not running?**
- Check `.claude/settings.local.json` syntax
- Verify Prettier is installed: `npx prettier --version`
- Check Claude Code logs for errors

**Commands not showing up?**
- Commands require restart of Claude Code session
- Check `.claude/commands/*.md` files exist
- File names must match command names (e.g., `test.md` for `/test`)

**Context still too large?**
- Add more patterns to `.claudeignore`
- Use `@` file selector to check what's included
- Run `du -sh node_modules/` to find large directories

---

*Setup completed: 2026-02-15*
