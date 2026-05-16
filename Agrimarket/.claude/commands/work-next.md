# /work-next — Pick and start the next Vibe Kanban task

Find the highest-priority open issue in Vibe Kanban and begin working on it.

## Steps

1. **List open issues** — call `mcp__vibe_kanban__list_issues` filtered by `status: "In Progress"` first, then `status: "Todo"` if none in progress
2. **Select by priority** — prefer: urgent > high > medium > low; within same priority, prefer lower KAB-N number
3. **Get full context** — call `mcp__vibe_kanban__get_issue` on the chosen issue to read its description and any sub-issues
4. **Mark in progress** — call `mcp__vibe_kanban__update_issue` to set status `"In Progress"` if not already
5. **Announce and implement** — state which issue you're starting (e.g., "Starting KAB-3: SMS/OTP Authentication"), then implement it following project standards

## Pipeline decision

- Simple/well-understood task → implement directly
- Complex/unclear task → run `/pipeline "KAB-N: <title>"` first for Gemini analysis + DeepSeek spec before coding

## When done

- Run `./gradlew testDebugUnitTest` (Android) and/or `npm test` (backend) to verify
- Commit with message referencing the KAB issue (e.g., `feat(auth): implement SMS OTP (KAB-3)`)
- Call `mcp__vibe_kanban__update_issue` to set status `"Done"`
- Call `/work-next` again to pick the next task
