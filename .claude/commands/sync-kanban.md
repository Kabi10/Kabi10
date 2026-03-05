# /sync-kanban — Review and sync Vibe Kanban board status

Audit the Vibe Kanban board: surface stale issues, close completed work, and identify what's next.

## Steps

1. **List all issues** — call `mcp__vibe_kanban__list_issues` (no filter) to get full board snapshot
2. **Audit each status**:
   - **In Progress**: are they actually being worked on? If stale (no recent commits), flag them
   - **Todo**: are any blocked by dependencies? Note which can start now
   - **Done**: verify corresponding commits exist (`git log --oneline | grep KAB-N`)
3. **Cross-check with git** — run `git log --oneline -20` and map commits to KAB issues
4. **Report board state**:

   ```
   IN PROGRESS:
     KAB-3: SMS/OTP Auth [urgent] — active

   TODO (ready to start):
     KAB-2: Batch Sync [high]

   DONE (close if not already):
     KAB-6: Storage delete — committed 90b0060
     KAB-7: Test expansion — committed fcb9913

   NEXT RECOMMENDED: KAB-3 (urgent, in progress)
   ```

5. **Update stale issues** — move any wrongly-marked issues to correct status
6. **Close confirmed-done issues** — `mcp__vibe_kanban__update_issue` → `"Done"` for verified work

## Quick sync (status check only)

Just list issues without making changes — useful for a fast board overview before starting a session.

## Integration with /work-next

After `/sync-kanban`, call `/work-next` to automatically pick and start the highest-priority ready issue.
