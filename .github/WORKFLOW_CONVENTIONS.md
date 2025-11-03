# Agrimarket GitHub-ClickUp Workflow Conventions

This document defines the team workflow conventions for using GitHub and ClickUp together on the Agrimarket project.

## Table of Contents

1. [Branch Naming Conventions](#branch-naming-conventions)
2. [Pull Request Conventions](#pull-request-conventions)
3. [Commit Message Conventions](#commit-message-conventions)
4. [Task Management Workflow](#task-management-workflow)
5. [Code Review Process](#code-review-process)
6. [Merge and Release Process](#merge-and-release-process)

## Branch Naming Conventions

All branches must follow this format to enable automatic ClickUp integration:

```
[type]/CU-[task-id]-[description]
```

### Branch Type

Use one of the following types:

| Type | Purpose | Example |
|------|---------|---------|
| `feature` | New feature development | `feature/CU-abc123-add-marketplace-filters` |
| `bugfix` | Bug fixes | `bugfix/CU-def456-fix-login-crash` |
| `hotfix` | Critical production fixes | `hotfix/CU-ghi789-critical-security-patch` |
| `refactor` | Code refactoring | `refactor/CU-jkl012-improve-performance` |
| `docs` | Documentation updates | `docs/CU-mno345-update-api-docs` |
| `test` | Test additions/improvements | `test/CU-pqr678-add-unit-tests` |
| `chore` | Maintenance tasks | `chore/CU-stu901-update-dependencies` |

### Task ID Format

- **Format**: `CU-[alphanumeric]`
- **Examples**: `CU-abc123`, `CU-xyz789`, `CU-task001`
- **Source**: Copy from ClickUp task URL or task ID field
- **Case**: Use as-is from ClickUp (usually lowercase)

### Description Format

- Use kebab-case (hyphens, no spaces)
- Keep it concise (3-5 words)
- Be descriptive but not too long
- Examples:
  - `add-marketplace-filters`
  - `fix-login-crash`
  - `improve-performance`
  - `update-api-documentation`

### Complete Examples

```bash
# Feature branch
git checkout -b feature/CU-abc123-add-marketplace-filters

# Bug fix branch
git checkout -b bugfix/CU-def456-fix-login-crash

# Hotfix branch
git checkout -b hotfix/CU-ghi789-critical-security-patch

# Refactoring branch
git checkout -b refactor/CU-jkl012-improve-performance

# Documentation branch
git checkout -b docs/CU-mno345-update-api-docs
```

## Pull Request Conventions

### PR Title Format

```
[CU-task-id] Brief description of changes
```

**Rules**:
- Start with `[CU-task-id]` in square brackets
- Follow with a brief, descriptive title
- Use imperative mood (e.g., "Add", "Fix", "Update", not "Added", "Fixed")
- Keep total length under 72 characters when possible
- Match the task ID from your branch name

**Examples**:
```
[CU-abc123] Add marketplace filters
[CU-def456] Fix login crash on Android 12
[CU-ghi789] Critical security patch for auth
[CU-jkl012] Improve API response performance
[CU-mno345] Update API documentation
```

### PR Description Template

Use this template for all PRs:

```markdown
## ClickUp Task
[CU-task-id](https://app.clickup.com/t/[task-id])

## Description
Brief description of what this PR does and why.

## Changes
- Change 1
- Change 2
- Change 3

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Breaking change
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Refactoring

## Testing
How to test these changes:
1. Step 1
2. Step 2
3. Step 3

## Screenshots (if applicable)
Add screenshots or GIFs demonstrating the changes.

## Checklist
- [ ] Code follows project style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes
- [ ] Tested locally
- [ ] Tested on target platform (Android/Web/Backend)
```

### PR Labels

Use GitHub labels to categorize PRs:

- `android` - Android app changes
- `backend` - Backend API changes
- `web` - Web landing page changes
- `database` - Database schema/migration changes
- `documentation` - Documentation updates
- `bug` - Bug fixes
- `feature` - New features
- `enhancement` - Improvements to existing features
- `performance` - Performance improvements
- `security` - Security-related changes
- `breaking-change` - Breaking API/behavior changes
- `wip` - Work in progress (don't merge yet)
- `ready-for-review` - Ready for code review

## Commit Message Conventions

Follow conventional commits format:

```
[type]([scope]): [subject]

[body]

[footer]
```

### Commit Type

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Code style changes (formatting, semicolons, etc.)
- `refactor` - Code refactoring
- `perf` - Performance improvements
- `test` - Test additions/changes
- `chore` - Build, dependencies, tooling
- `ci` - CI/CD configuration

### Commit Scope

Optional scope indicating what part of the codebase:
- `android` - Android app
- `backend` - Backend API
- `web` - Web landing page
- `database` - Database
- `auth` - Authentication
- `listings` - Listings feature
- etc.

### Examples

```bash
# Simple commit
git commit -m "feat(android): add marketplace filters"

# Commit with body
git commit -m "fix(backend): fix login crash on Android 12

- Fixed null pointer exception in auth handler
- Added validation for phone number format
- Updated error messages for clarity"

# Commit with footer (references ClickUp task)
git commit -m "feat(android): add marketplace filters

Implements filtering by category, price range, and location.

Closes CU-abc123"
```

## Task Management Workflow

### Creating a Task in ClickUp

1. **Create task** with clear title and description
2. **Set priority** (Urgent, High, Normal, Low)
3. **Assign to team member**
4. **Set due date** if applicable
5. **Add labels** (e.g., `android`, `backend`, `bug`, `feature`)
6. **Note the task ID** (e.g., `CU-abc123`)

### Starting Work on a Task

1. **Update task status** in ClickUp to "In Progress"
2. **Create branch** using task ID:
   ```bash
   git checkout -b feature/CU-abc123-task-description
   ```
3. **Make commits** referencing the task ID
4. **Push branch** to GitHub

### During Development

1. **Keep task updated** with progress comments
2. **Link any related tasks** in ClickUp
3. **Update status** if blocked or waiting for review
4. **Communicate blockers** in task comments

### Creating a Pull Request

1. **Push branch** to GitHub
2. **Create PR** with title `[CU-task-id] Description`
3. **Fill PR description** using the template
4. **Request reviewers** from team
5. **Link PR** to ClickUp task (automatic via integration)

### Code Review

1. **Assign reviewers** in GitHub
2. **Reviewers check** code quality, tests, documentation
3. **Address feedback** with new commits
4. **Re-request review** after changes
5. **Approve** when ready to merge

### Merging and Closing

1. **Ensure all checks pass** (tests, linting, etc.)
2. **Get approval** from at least one reviewer
3. **Merge PR** to main branch
4. **Delete branch** after merge
5. **Update task status** in ClickUp to "Done"
6. **Add completion comment** with PR link

## Code Review Process

### For Reviewers

1. **Check PR title and description** for clarity
2. **Verify branch and PR naming** follows conventions
3. **Review code changes** for:
   - Code quality and style
   - Test coverage
   - Documentation
   - Performance implications
   - Security concerns
4. **Run tests locally** if needed
5. **Leave constructive comments** with suggestions
6. **Approve or request changes**

### For Authors

1. **Respond to all comments** (even if just acknowledging)
2. **Make requested changes** in new commits
3. **Don't force-push** after review starts (makes history hard to follow)
4. **Re-request review** after addressing feedback
5. **Thank reviewers** for their time

### Review Checklist

- [ ] PR title follows `[CU-xxxxx] Description` format
- [ ] Branch name includes task ID
- [ ] PR description is complete and clear
- [ ] Code follows project style guidelines
- [ ] Tests are added/updated
- [ ] Documentation is updated
- [ ] No breaking changes (or clearly documented)
- [ ] All CI checks pass
- [ ] Code is performant
- [ ] No security issues

## Merge and Release Process

### Before Merging

1. **All checks pass** (tests, linting, builds)
2. **At least one approval** from code reviewer
3. **No merge conflicts** or conflicts resolved
4. **Branch is up-to-date** with main
5. **Task status** updated in ClickUp

### Merging

1. **Use "Squash and merge"** for feature branches (keeps history clean)
2. **Use "Create a merge commit"** for release branches
3. **Delete branch** after merge
4. **Verify merge** in GitHub

### After Merging

1. **Update ClickUp task** status to "Done"
2. **Add completion comment** with PR link
3. **Monitor for issues** in staging/production
4. **Communicate** to team about deployment

### Release Process

1. **Create release branch** from main: `release/v1.2.3`
2. **Update version numbers** in all files
3. **Update CHANGELOG.md** with release notes
4. **Create PR** for release branch
5. **Get approval** from project lead
6. **Merge** to main
7. **Create GitHub release** with tag `v1.2.3`
8. **Deploy** to production
9. **Announce** release to team

## Examples

### Complete Workflow Example

```bash
# 1. Create task in ClickUp (e.g., CU-abc123)

# 2. Create and checkout branch
git checkout -b feature/CU-abc123-add-marketplace-filters

# 3. Make changes and commit
echo "Add filter functionality" >> app/src/main/java/com/example/Filters.kt
git add .
git commit -m "feat(android): add marketplace filters

- Implement category filter
- Implement price range filter
- Add filter UI components

Closes CU-abc123"

# 4. Push branch
git push origin feature/CU-abc123-add-marketplace-filters

# 5. Create PR on GitHub with title:
# [CU-abc123] Add marketplace filters

# 6. Fill PR description using template

# 7. Request review from team

# 8. Address review feedback with new commits

# 9. Merge PR (squash and merge)

# 10. Update ClickUp task status to "Done"
```

## Tips and Best Practices

1. **Keep PRs small** - Easier to review, faster to merge
2. **One task per PR** - Avoid mixing multiple features
3. **Write clear commit messages** - Future you will thank you
4. **Test before pushing** - Save CI time and reviewer time
5. **Communicate early** - Ask questions in task comments
6. **Update documentation** - Keep docs in sync with code
7. **Review promptly** - Don't let PRs sit for days
8. **Be respectful** - Code reviews are about code, not people
9. **Celebrate wins** - Acknowledge good work and merged PRs
10. **Learn from mistakes** - Use retrospectives to improve process

## Questions?

If you have questions about these conventions:
1. Check this document first
2. Ask in the #development Slack channel
3. Open an issue in the repository
4. Contact the project lead

