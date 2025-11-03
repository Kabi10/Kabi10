# GitHub-ClickUp Integration Setup Guide

Complete setup guide for bidirectional GitHub-ClickUp integration on the Agrimarket project.

## Overview

This integration enables:
- **Automatic PR Linking**: PRs automatically link to ClickUp tasks via task IDs in PR titles/branch names
- **Status Sync**: PR status changes (opened, merged, closed) reflect in ClickUp
- **Review Tracking**: Code reviews and comments sync to ClickUp tasks
- **Workflow Automation**: Reduces manual task management and keeps teams in sync

## Prerequisites

- **GitHub Repository Admin Access** - Configure webhooks and Actions secrets
- **ClickUp Workspace Admin Access** - Create API tokens and configure webhooks
- **ClickUp API Token** - For authenticating requests to ClickUp
- **GitHub Personal Access Token** - For webhook authentication

## Step 1: GitHub Setup

### 1.1 Create GitHub Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Set scopes: `repo`, `workflow`, `read:org`
4. Copy token (won't be shown again) and store securely

### 1.2 Add GitHub Actions Secrets

1. Go to repository → Settings → Secrets and variables → Actions
2. Add secret: `CLICKUP_API_TOKEN` = Your ClickUp API token (from Step 2.2)

### 1.3 Enable GitHub Actions

1. Go to repository → Settings → Actions → General
2. Select "Allow all actions and reusable workflows"
3. Verify workflow file exists: `.github/workflows/clickup-integration.yml`

## Step 2: ClickUp Setup

### 2.1 Get ClickUp Workspace & Team IDs

1. Log in to ClickUp
2. Workspace ID: Found in URL `https://app.clickup.com/[WORKSPACE_ID]/...`
3. Team ID: Go to Settings → Integrations → API (displayed on page)

### 2.2 Create ClickUp API Token

1. Go to ClickUp Settings → Integrations → API
2. Click "Generate" to create new API token
3. Copy token and store securely
4. Add to GitHub Actions secrets as `CLICKUP_API_TOKEN`

## Step 3: Configure Webhooks

### 3.1 GitHub Webhook to ClickUp

1. Go to repository → Settings → Webhooks → Add webhook
2. Configure:
   - **Payload URL**: `https://api.clickup.com/api/v2/webhook`
   - **Content type**: `application/json`
   - **Events**: `Pull requests`, `Pull request reviews`, `Push`
   - **Active**: ✓ Checked
3. Click "Add webhook"

### 3.2 ClickUp Webhook to GitHub

1. Go to ClickUp Settings → Integrations → Webhooks → Add Webhook
2. Configure:
   - **Webhook URL**: `https://api.github.com/repos/[OWNER]/[REPO]/dispatches`
   - **Events**: `Task Updated`, `Task Status Changed`, `Task Assigned`
   - **Authentication**: Bearer Token (use GitHub Personal Access Token)
3. Click "Create"

### 3.3 Enable ClickUp Native GitHub Integration

1. Go to ClickUp Settings → Integrations → GitHub
2. Click "Connect" and authorize ClickUp
3. Select Agrimarket repository
4. Enable: Auto-link PRs, Sync PR status, Sync comments
5. Click "Save"

## Step 4: Test the Integration

### Quick Test

1. Create test branch: `git checkout -b feature/CU-test-integration`
2. Make a change: `echo "# Test" >> TEST.md && git add TEST.md && git commit -m "test"`
3. Push: `git push origin feature/CU-test-integration`
4. Create PR with title: `[CU-test] Test GitHub-ClickUp integration`
5. Verify in GitHub: Check Actions tab for workflow execution
6. Verify in ClickUp: Check that PR link appears in task `CU-test`
7. Merge PR and verify ClickUp task status updates
8. Delete test branch and test file

### Verification Checklist

- [ ] GitHub Actions workflow runs on PR creation
- [ ] PR appears linked in ClickUp task
- [ ] ClickUp task status updates when PR is merged
- [ ] Webhook delivery logs show successful deliveries
- [ ] No errors in GitHub Actions logs

## Workflow Conventions

**See `.github/WORKFLOW_CONVENTIONS.md` for complete details.**

Quick reference:
- **Branch**: `[type]/CU-[task-id]-[description]` (e.g., `feature/CU-abc123-add-filters`)
- **PR Title**: `[CU-task-id] Brief description` (e.g., `[CU-abc123] Add marketplace filters`)
- **Commit**: `[type]([scope]): description` (e.g., `feat(android): add filters`)

## Troubleshooting

| Problem | Solution |
|---------|----------|
| PR not linking to ClickUp | Verify PR title has `[CU-xxxxx]` format; check GitHub Actions logs; verify task exists in ClickUp |
| ClickUp task not updating | Check ClickUp webhook is configured; verify API token is valid; check webhook delivery logs |
| Workflow not running | Verify workflow file exists; check GitHub Actions enabled; verify PR title/branch format correct |
| Webhook delivery fails | Verify webhook URL correct; check API tokens valid; review delivery logs for errors |
| Task ID not recognized | Verify PR title format `[CU-xxxxx]` (case-sensitive); check branch includes task ID |

**Check logs**:
- GitHub Actions: Repository → Actions tab
- GitHub Webhooks: Settings → Webhooks → Recent Deliveries
- ClickUp Webhooks: Settings → Integrations → Webhooks → Delivery History

## Security Best Practices

- Store all tokens in GitHub Secrets, never commit them
- Use minimal required permissions for API tokens
- Rotate API tokens periodically (every 90 days)
- Regularly review webhook delivery logs
- Use HTTPS for all webhook URLs

## Resources

- [ClickUp API Documentation](https://clickup.com/api)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Webhooks Documentation](https://docs.github.com/en/developers/webhooks-and-events/webhooks)
- [Workflow Conventions](./WORKFLOW_CONVENTIONS.md)
- [Quick Reference](./QUICK_REFERENCE.md)

## Next Steps

1. Complete all setup steps above
2. Run the quick test in Step 4
3. Review `.github/WORKFLOW_CONVENTIONS.md` with your team
4. Share `.github/QUICK_REFERENCE.md` with developers
5. Monitor webhook delivery logs for first week

