# GitHub Labels Configuration

This document provides recommended labels for the Agrimarket issue tracker. These labels help organize issues and make it easier for contributors (especially interns) to find tasks that match their skill level and interests.

## 🏷️ Recommended Labels

### Priority Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `priority: critical` | `#d73a4a` (red) | Critical bugs or security issues | App crashes, data loss, security vulnerabilities |
| `priority: high` | `#ff9800` (orange) | Important features or bugs | Major features, significant bugs affecting many users |
| `priority: medium` | `#fbca04` (yellow) | Normal priority | Standard features and bugs |
| `priority: low` | `#0e8a16` (green) | Nice to have | Minor improvements, cosmetic issues |

### Type Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `type: bug` | `#d73a4a` (red) | Something isn't working | Crashes, errors, unexpected behavior |
| `type: feature` | `#a2eeef` (light blue) | New feature or request | New functionality, enhancements |
| `type: enhancement` | `#84b6eb` (blue) | Improvement to existing feature | Optimizations, UX improvements |
| `type: documentation` | `#0075ca` (dark blue) | Documentation improvements | README, guides, code comments |
| `type: refactor` | `#d4c5f9` (purple) | Code refactoring | Code cleanup, architecture improvements |
| `type: testing` | `#bfd4f2` (light purple) | Testing related | Unit tests, UI tests, test infrastructure |

### Difficulty Labels (For Interns)

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `good first issue` | `#7057ff` (purple) | Good for newcomers | Simple, well-defined tasks for beginners |
| `beginner-friendly` | `#00ff00` (bright green) | Suitable for beginners | Tasks that don't require deep knowledge |
| `difficulty: easy` | `#c2e0c6` (light green) | Easy task | 2-4 hours, minimal complexity |
| `difficulty: medium` | `#fef2c0` (light yellow) | Medium difficulty | 4-8 hours, moderate complexity |
| `difficulty: hard` | `#f9d0c4` (light red) | Challenging task | 8+ hours, requires deep understanding |

### Component Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `component: ui` | `#e99695` (pink) | User interface | Compose screens, UI components |
| `component: backend` | `#5319e7` (purple) | Backend/API | Server-side code, API endpoints |
| `component: database` | `#1d76db` (blue) | Database related | Room, Supabase, migrations |
| `component: auth` | `#fbca04` (yellow) | Authentication | Login, OTP, user management |
| `component: offline` | `#0e8a16` (green) | Offline functionality | Sync, caching, offline-first |
| `component: firebase` | `#ff9800` (orange) | Firebase integration | Analytics, Crashlytics, Storage |
| `component: maps` | `#00bcd4` (cyan) | Maps and location | Google Maps, location features |

### Status Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `status: blocked` | `#d73a4a` (red) | Blocked by another issue | Cannot proceed until dependency resolved |
| `status: in progress` | `#fbca04` (yellow) | Currently being worked on | Someone is actively working on this |
| `status: needs review` | `#0075ca` (blue) | Needs code review | PR submitted, awaiting review |
| `status: needs info` | `#d876e3` (purple) | Needs more information | Waiting for clarification from reporter |
| `status: wontfix` | `#ffffff` (white) | Will not be fixed | Not a bug, or won't be addressed |
| `status: duplicate` | `#cfd3d7` (gray) | Duplicate issue | Already reported elsewhere |

### Special Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `help wanted` | `#008672` (teal) | Extra attention needed | Complex issues where help is appreciated |
| `question` | `#d876e3` (purple) | Question or discussion | Not a bug or feature, just a question |
| `breaking change` | `#d73a4a` (red) | Breaking API change | Changes that break existing functionality |
| `dependencies` | `#0366d6` (blue) | Dependency updates | Library or framework updates |
| `security` | `#ee0701` (bright red) | Security issue | Security vulnerabilities |
| `performance` | `#ff9800` (orange) | Performance improvement | Speed, memory, battery optimizations |
| `accessibility` | `#00ff00` (green) | Accessibility | Screen readers, color contrast, etc. |

### Language Labels

| Label | Color | Description | When to Use |
|-------|-------|-------------|-------------|
| `lang: tamil` | `#ff6b6b` (red) | Tamil language | Tamil translation or Tamil-specific issues |
| `lang: sinhala` | `#4ecdc4` (teal) | Sinhala language | Sinhala translation or Sinhala-specific issues |
| `lang: english` | `#95e1d3` (mint) | English language | English translation or English-specific issues |

## 📋 How to Create Labels in GitHub

### Option 1: Manual Creation (GitHub Web UI)

1. Go to your repository on GitHub
2. Click on **"Issues"** tab
3. Click on **"Labels"** button
4. Click **"New label"**
5. Enter name, description, and color
6. Click **"Create label"**

### Option 2: Bulk Creation (GitHub CLI)

If you have GitHub CLI installed:

```bash
# Install GitHub CLI first: https://cli.github.com/

# Create priority labels
gh label create "priority: critical" --color d73a4a --description "Critical bugs or security issues"
gh label create "priority: high" --color ff9800 --description "Important features or bugs"
gh label create "priority: medium" --color fbca04 --description "Normal priority"
gh label create "priority: low" --color 0e8a16 --description "Nice to have"

# Create type labels
gh label create "type: bug" --color d73a4a --description "Something isn't working"
gh label create "type: feature" --color a2eeef --description "New feature or request"
gh label create "type: enhancement" --color 84b6eb --description "Improvement to existing feature"
gh label create "type: documentation" --color 0075ca --description "Documentation improvements"
gh label create "type: refactor" --color d4c5f9 --description "Code refactoring"
gh label create "type: testing" --color bfd4f2 --description "Testing related"

# Create difficulty labels
gh label create "good first issue" --color 7057ff --description "Good for newcomers"
gh label create "beginner-friendly" --color 00ff00 --description "Suitable for beginners"
gh label create "difficulty: easy" --color c2e0c6 --description "Easy task (2-4 hours)"
gh label create "difficulty: medium" --color fef2c0 --description "Medium difficulty (4-8 hours)"
gh label create "difficulty: hard" --color f9d0c4 --description "Challenging task (8+ hours)"

# Create component labels
gh label create "component: ui" --color e99695 --description "User interface"
gh label create "component: backend" --color 5319e7 --description "Backend/API"
gh label create "component: database" --color 1d76db --description "Database related"
gh label create "component: auth" --color fbca04 --description "Authentication"
gh label create "component: offline" --color 0e8a16 --description "Offline functionality"
gh label create "component: firebase" --color ff9800 --description "Firebase integration"
gh label create "component: maps" --color 00bcd4 --description "Maps and location"

# Create status labels
gh label create "status: blocked" --color d73a4a --description "Blocked by another issue"
gh label create "status: in progress" --color fbca04 --description "Currently being worked on"
gh label create "status: needs review" --color 0075ca --description "Needs code review"
gh label create "status: needs info" --color d876e3 --description "Needs more information"

# Create special labels
gh label create "help wanted" --color 008672 --description "Extra attention needed"
gh label create "question" --color d876e3 --description "Question or discussion"
gh label create "security" --color ee0701 --description "Security issue"
gh label create "performance" --color ff9800 --description "Performance improvement"

# Create language labels
gh label create "lang: tamil" --color ff6b6b --description "Tamil language"
gh label create "lang: sinhala" --color 4ecdc4 --description "Sinhala language"
gh label create "lang: english" --color 95e1d3 --description "English language"
```

## 🎯 Label Usage Guidelines

### For Issue Creators

When creating an issue, apply:
1. **One type label** (bug, feature, etc.)
2. **One priority label** (if applicable)
3. **One or more component labels**
4. **Difficulty label** (if you know the complexity)
5. **Special labels** as needed (help wanted, good first issue, etc.)

### For Maintainers

- Add `good first issue` to tasks suitable for newcomers
- Add `help wanted` when you need community assistance
- Update `status:` labels as work progresses
- Use `priority:` labels to guide development focus

### For Interns

Look for issues with these labels:
- `good first issue` - Perfect for your first contribution
- `beginner-friendly` - Suitable for learning
- `difficulty: easy` - Quick wins
- `help wanted` - Maintainers need help here

## 📊 Example Issue Labeling

**Example 1: Beginner Bug Fix**
```
Labels: type: bug, priority: medium, component: ui, difficulty: easy, good first issue
```

**Example 2: Complex Feature**
```
Labels: type: feature, priority: high, component: backend, component: database, difficulty: hard, help wanted
```

**Example 3: Documentation Task**
```
Labels: type: documentation, priority: low, difficulty: easy, good first issue, beginner-friendly
```

---

**Note**: You can customize these labels based on your project's specific needs. The key is to be consistent and make it easy for contributors to find relevant issues.

