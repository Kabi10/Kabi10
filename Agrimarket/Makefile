.PHONY: sync-issues sync-completed sync-all plan spec analyze help

help:
	@echo "Available commands:"
	@echo "  make sync-issues        - Sync open GitHub issues to ClickUp tasks"
	@echo "  make sync-completed     - Sync merged PRs and closed issues to ClickUp"
	@echo "  make sync-all           - Run full synchronization"
	@echo "  make plan TASK='...'    - Gemini → DeepSeek spec → Claude Code execution"
	@echo "  make spec TASK='...'    - Gemini → DeepSeek spec only (no Claude execution)"
	@echo "  make analyze TASK='...' - Gemini analysis only (Stage 1 output)"

# Sync open GitHub issues to ClickUp
sync-issues:
	cd scripts/clickup_automation && python sync_github_issues_to_clickup.py --all

# Sync merged PRs and closed issues to ClickUp
sync-completed:
	cd scripts/clickup_automation && python sync_completed_work.py --all

# Full synchronization
sync-all: sync-issues sync-completed

# Gemini → DeepSeek R1 → Claude Code three-stage pipeline
# Usage: make plan TASK="Add a favorites screen with offline support"
plan:
	@test -n "$(TASK)" || (echo "Usage: make plan TASK='your task description'" && exit 1)
	./scripts/pipeline.sh "$(TASK)"

# Gemini → DeepSeek spec only (no Claude execution)
# Usage: make spec TASK="Fix sync conflict in SyncManager"
spec:
	@test -n "$(TASK)" || (echo "Usage: make spec TASK='your task description'" && exit 1)
	./scripts/pipeline.sh "$(TASK)" --dry-run

# Gemini analysis only (Stage 1 output)
# Usage: make analyze TASK="Describe the favorites data flow"
analyze:
	@test -n "$(TASK)" || (echo "Usage: make analyze TASK='your task description'" && exit 1)
	./scripts/pipeline.sh "$(TASK)" --analyze-only
