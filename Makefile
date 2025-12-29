.PHONY: sync-issues sync-completed sync-all help

help:
	@echo "Available commands:"
	@echo "  make sync-issues      - Sync open GitHub issues to ClickUp tasks"
	@echo "  make sync-completed   - Sync merged PRs and closed issues to ClickUp"
	@echo "  make sync-all         - Run full synchronization"

# Sync open GitHub issues to ClickUp
sync-issues:
	cd scripts/clickup_automation && python sync_github_issues_to_clickup.py --all

# Sync merged PRs and closed issues to ClickUp
sync-completed:
	cd scripts/clickup_automation && python sync_completed_work.py --all

# Full synchronization
sync-all: sync-issues sync-completed
