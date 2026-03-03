# /work-task — Work on a specific Vibe Kanban issue

Usage: `/work-task KAB-N`

Load full context for a specific issue and implement it.

## Steps

1. **Fetch issue** — call `mcp__vibe_kanban__get_issue` using the issue ID for KAB-N (list first if you need the UUID)
2. **Read context** — review title, description, sub-issues, and relationships
3. **Mark in progress** — `mcp__vibe_kanban__update_issue` → status `"In Progress"` if not already
4. **Explore codebase** — read referenced files; follow existing patterns (e.g., `ListingRepository.kt` for offline-first, existing routes for backend pattern)
5. **Plan if complex** — for multi-file changes, enter plan mode and get approval before coding
6. **Implement** — make changes; prefer editing existing files over creating new ones
7. **Verify** — build + test:
   - Android: `./gradlew :app:compileDebugKotlin` then `./gradlew testDebugUnitTest`
   - Backend: `npm test` in `backend/`
8. **Commit** — use conventional commit with KAB reference: `feat(scope): description (KAB-N)`
9. **Close** — `mcp__vibe_kanban__update_issue` → status `"Done"`

## Pipeline option

For complex tasks, run the Gemini→DeepSeek analysis before implementing:

```
/pipeline "KAB-N: <task title>"
```

This uses Gemini 2.5 Pro for codebase analysis and DeepSeek R1 for implementation spec, saving Claude tokens for the actual coding.

## Reference: Key file paths

- Android repositories: `app/src/main/java/com/senthapps/slagrimarket/data/repository/`
- Android ViewModels: `app/src/main/java/com/senthapps/slagrimarket/ui/`
- Backend routes: `backend/src/routes/`
- Backend services: `backend/src/services/`
- Tests: `app/src/test/java/com/senthapps/slagrimarket/`
