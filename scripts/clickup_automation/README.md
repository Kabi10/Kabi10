# ClickUp Workspace Automation for Agrimarket

GitHub-ClickUp integration for the Agrimarket project.

## 📁 Files

### Active Scripts
- **`sync_github_issues_to_clickup.py`** - 🔄 Syncs GitHub issues to ClickUp tasks (ongoing use)

### Reference Data
- **`agrimarket_tasks.csv`** - 📊 Task reference data (16 tasks with priorities, components, and descriptions)
- **`requirements.txt`** - 📦 Python dependencies
- **`.env.example`** - 🔑 Template for ClickUp API credentials

## 🚀 Quick Start

### 1. Install Dependencies
```bash
cd scripts/clickup_automation
pip install -r requirements.txt
```

### 2. Configure Credentials
```bash
cp .env.example .env
# Edit .env with your ClickUp API token, Space ID, and Team ID
```

### 3. Sync GitHub Issues to ClickUp
```bash
python sync_github_issues_to_clickup.py
```

## 📊 Task Reference Data

The `agrimarket_tasks.csv` file contains 16 high-priority tasks organized by component:
- **Authentication & Security** - Real OTP-based authentication system
- **Deployment & DevOps** - Backend production deployment
- **QA & Testing** - Comprehensive testing suite
- **Architecture & Core Features** - Offline sync improvements
- **UI/UX** - Material Design 3 dark theme, network indicators, image optimization
- **Data Layer** - Database schema, query optimization
- **Documentation** - API docs, schema docs, team onboarding

## 🔗 GitHub Integration

The `sync_github_issues_to_clickup.py` script enables:
- ✅ Automatic GitHub issue → ClickUp task creation
- ✅ Bidirectional status synchronization
- ✅ PR ↔ task linking
- ✅ Label and priority mapping

## 📚 Documentation

- **[ClickUp API Docs](https://clickup.com/api)** - Official API reference
- **[GitHub API Docs](https://docs.github.com/en/rest)** - GitHub REST API reference

---

**Last Updated:** 2025-11-12 | **Status:** Active - GitHub Sync Only
