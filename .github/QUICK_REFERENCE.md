# GitHub-ClickUp Integration Quick Reference

Quick reference guide for the Agrimarket GitHub-ClickUp integration.

## Quick Start

### For Developers

1. **Create a ClickUp task** and note the task ID (e.g., `CU-abc123`)

2. **Create a feature branch**:
   ```bash
   git checkout -b feature/CU-abc123-brief-description
   ```

3. **Make changes and commit**:
   ```bash
   git add .
   git commit -m "feat(scope): description"
   git push origin feature/CU-abc123-brief-description
   ```

4. **Create a PR** with title:
   ```
   [CU-abc123] Brief description of changes
   ```

5. **Fill PR description** using the template

6. **Request review** from team

7. **Address feedback** and re-request review

8. **Merge PR** when approved

9. **Update ClickUp task** status to "Done"

## Naming Conventions

### Branch Names
```
[type]/CU-[task-id]-[description]
```

**Types**: `feature`, `bugfix`, `hotfix`, `refactor`, `docs`, `test`, `chore`

**Examples**:
- `feature/CU-abc123-add-marketplace-filters`
- `bugfix/CU-def456-fix-login-crash`
- `hotfix/CU-ghi789-critical-security-patch`

### PR Titles
```
[CU-task-id] Brief description
```

**Examples**:
- `[CU-abc123] Add marketplace filters`
- `[CU-def456] Fix login crash on Android 12`
- `[CU-ghi789] Critical security patch for auth`

### Commit Messages
```
[type]([scope]): [subject]
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `ci`

**Examples**:
- `feat(android): add marketplace filters`
- `fix(backend): fix login crash`
- `docs(api): update API documentation`

## Common Tasks

### Create a Feature Branch
```bash
git checkout -b feature/CU-abc123-feature-name
```

### Push Branch
```bash
git push origin feature/CU-abc123-feature-name
```

### Create a Pull Request
1. Go to GitHub repository
2. Click "New Pull Request"
3. Set title: `[CU-abc123] Description`
4. Fill description using template
5. Click "Create Pull Request"

### Request Review
1. In PR, click "Request a review"
2. Select reviewer
3. Click to request

### Address Review Feedback
```bash
# Make changes
git add .
git commit -m "review: address feedback"
git push origin feature/CU-abc123-feature-name

# Re-request review in GitHub
```

### Merge PR
1. In PR, click "Merge pull request"
2. Select "Squash and merge"
3. Click "Confirm squash and merge"
4. Delete branch

### Update ClickUp Task
1. Go to ClickUp task
2. Update status to "Done"
3. Add comment with PR link

## GitHub Actions Workflow

The integration includes automatic:

- ✅ **Task ID Extraction** - Extracts task ID from PR title or branch name
- ✅ **PR Linking** - Links PR to ClickUp task
- ✅ **Status Comments** - Adds comments about PR status
- ✅ **Format Validation** - Validates PR title and branch name format
- ✅ **Review Sync** - Syncs reviews to ClickUp

## ClickUp Webhook Events

Configured webhooks sync:

- ✅ **Task Status Changes** - Updates reflected in GitHub
- ✅ **Task Assignments** - Synced to GitHub
- ✅ **Task Comments** - Synced to GitHub PR
- ✅ **Task Updates** - Reflected in GitHub

## Troubleshooting

### PR Not Linking to ClickUp

**Check**:
1. PR title includes `[CU-xxxxx]` format
2. Task ID exists in ClickUp
3. GitHub Actions workflow ran (check Actions tab)
4. GitHub webhook delivery logs

**Fix**:
1. Update PR title to include task ID
2. Verify task exists in ClickUp
3. Check GitHub Actions logs for errors
4. Manually link PR in ClickUp if needed

### ClickUp Task Not Updating

**Check**:
1. ClickUp webhook is configured
2. API token is valid
3. Webhook delivery logs show success
4. Task status workflow is correct

**Fix**:
1. Verify webhook configuration
2. Regenerate API token if expired
3. Check webhook delivery logs for errors
4. Manually update task status if needed

### Workflow Not Running

**Check**:
1. Workflow file exists: `.github/workflows/clickup-integration.yml`
2. GitHub Actions are enabled
3. PR title/branch name format is correct
4. Check Actions tab for errors

**Fix**:
1. Verify workflow file exists
2. Enable GitHub Actions in settings
3. Use correct naming format
4. Check Actions logs for specific errors

## Important Links

- **Setup Guide**: `.github/CLICKUP_INTEGRATION_SETUP.md`
- **Workflow Conventions**: `.github/WORKFLOW_CONVENTIONS.md`
- **Webhook Setup**: `.github/CLICKUP_WEBHOOK_SETUP.md`
- **Testing Guide**: `.github/TESTING_INTEGRATION.md`
- **This Guide**: `.github/QUICK_REFERENCE.md`

## Credentials Needed

### GitHub
- [ ] GitHub Personal Access Token (for webhooks)
  - Scopes: `repo`, `workflow`
  - Store in GitHub Secrets as `CLICKUP_API_TOKEN`

### ClickUp
- [ ] ClickUp API Token
  - Get from: Settings → Integrations → API
  - Store in GitHub Secrets as `CLICKUP_API_TOKEN`
- [ ] ClickUp Workspace ID
  - Found in URL: `https://app.clickup.com/[WORKSPACE_ID]/...`
- [ ] ClickUp Team ID
  - Get from: Settings → Integrations → API

## Team Workflow

1. **Create Task** in ClickUp
2. **Create Branch** with task ID
3. **Make Changes** and commit
4. **Create PR** with task ID in title
5. **Request Review** from team
6. **Address Feedback** with new commits
7. **Merge PR** when approved
8. **Update Task** status to Done

## Best Practices

✅ **Do**:
- Use task IDs in branch names and PR titles
- Keep PRs small and focused
- Write clear commit messages
- Request reviews promptly
- Update ClickUp task status
- Test before pushing
- Review code promptly

❌ **Don't**:
- Mix multiple tasks in one PR
- Forget task ID in PR title
- Force-push after review starts
- Leave PRs unreviewed for days
- Forget to update ClickUp task
- Commit without testing
- Merge without approval

## Getting Help

1. Check this Quick Reference
2. Read the full setup guide
3. Check GitHub Actions logs
4. Check ClickUp webhook logs
5. Ask in team chat
6. Contact project lead

## Useful Commands

```bash
# Create and checkout feature branch
git checkout -b feature/CU-abc123-description

# View current branch
git branch

# Push branch
git push origin feature/CU-abc123-description

# View recent commits
git log --oneline -10

# View changes
git diff

# Stage changes
git add .

# Commit with message
git commit -m "feat(scope): description"

# View status
git status

# Pull latest changes
git pull origin main

# Rebase on main
git rebase main

# Force push (use carefully!)
git push origin feature/CU-abc123-description --force-with-lease
```

## Keyboard Shortcuts

### GitHub
- `g` + `p` - Go to pull requests
- `g` + `i` - Go to issues
- `?` - Show keyboard shortcuts

### ClickUp
- `c` - Create new task
- `s` - Search
- `?` - Show keyboard shortcuts

## Contact

For questions or issues:
- **Slack**: #development channel
- **GitHub**: Open an issue
- **Email**: team@agrimarket.lk
- **Project Lead**: [Contact info]

---

**Last Updated**: 2024-01-15
**Version**: 1.0
**Status**: Active

