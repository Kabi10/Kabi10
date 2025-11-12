#!/usr/bin/env python3
"""
Sync GitHub Issues to ClickUp Tasks

This script fetches GitHub issues from the Agrimarket repository and creates
corresponding ClickUp tasks with proper categorization and custom fields.

Usage:
    python sync_github_issues_to_clickup.py --all              # Sync all open issues
    python sync_github_issues_to_clickup.py --issue 14         # Sync specific issue
    python sync_github_issues_to_clickup.py --dry-run          # Preview without creating
    python sync_github_issues_to_clickup.py --priority high    # Sync only high priority

Author: Agrimarket Team
Date: 2025-11-05
"""

import os
import sys
import json
import argparse
import subprocess
from typing import Dict, List, Optional
from dotenv import load_dotenv
import requests

# Load environment variables
load_dotenv()

# Configuration
CLICKUP_API_TOKEN = os.getenv("CLICKUP_API_TOKEN")
CLICKUP_SPACE_ID = os.getenv("CLICKUP_SPACE_ID", "90187854362")
GITHUB_REPO = "Kabi10/Srilanka-Farmers-Marketplace"

# ClickUp API endpoints
CLICKUP_API_BASE = "https://api.clickup.com/api/v2"

# List IDs (from verify_workspace.py)
LIST_IDS = {
    "ui_ux": "901812942655",
    "architecture": "901812942657",
    "data_layer": "901812942659",
    "auth": "901812942662",
    "qa_testing": "901812942663",
    "devops": "901812942664",
    "future": "901812942666",
    "documentation": "901812942669"
}

# Issue categorization rules
ISSUE_CATEGORIES = {
    "language": "ui_ux",  # Language switching issues
    "button": "ui_ux",    # Button responsiveness issues
    "profile": "ui_ux",   # Profile page issues
    "filter": "ui_ux",    # Filter/search issues
    "logo": "ui_ux",      # Logo/branding issues
    "design": "ui_ux",    # Design issues
    "persistence": "data_layer",  # Data persistence issues
    "test": "qa_testing", # Testing issues
    "bug": "qa_testing",  # General bugs
}

# Priority mapping
PRIORITY_MAPPING = {
    "critical": ["button unresponsive", "not working", "appear twice"],
    "high": ["does not change language", "stay in english", "unresponsive"],
    "medium": ["not clickable", "missing", "not interactive"],
    "low": ["outdated", "unrefined", "not visible"]
}


def get_clickup_headers() -> Dict[str, str]:
    """Get headers for ClickUp API requests"""
    return {
        "Authorization": CLICKUP_API_TOKEN,
        "Content-Type": "application/json"
    }


def get_github_issue(issue_number: int) -> Optional[Dict]:
    """Fetch GitHub issue details using gh CLI"""
    try:
        cmd = [
            "gh", "issue", "view", str(issue_number),
            "--repo", GITHUB_REPO,
            "--json", "number,title,body,labels,state,author,createdAt,url"
        ]
        result = subprocess.run(cmd, capture_output=True, text=True, check=True, encoding='utf-8', errors='replace')
        return json.loads(result.stdout)
    except subprocess.CalledProcessError as e:
        print(f"❌ Error fetching GitHub issue #{issue_number}: {e}")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ Error parsing GitHub issue #{issue_number}: {e}")
        return None


def get_all_github_issues() -> List[Dict]:
    """Fetch all open GitHub issues using gh CLI"""
    try:
        cmd = [
            "gh", "issue", "list",
            "--repo", GITHUB_REPO,
            "--state", "open",
            "--limit", "100",
            "--json", "number,title,body,labels,state,author,createdAt,url"
        ]
        result = subprocess.run(cmd, capture_output=True, text=True, check=True, encoding='utf-8', errors='replace')
        if not result.stdout:
            print("❌ No output from gh CLI")
            return []
        issues = json.loads(result.stdout)
        print(f"✅ Found {len(issues)} open GitHub issues")
        return issues
    except subprocess.CalledProcessError as e:
        print(f"❌ Error fetching GitHub issues: {e}")
        return []
    except json.JSONDecodeError as e:
        print(f"❌ Error parsing GitHub issues: {e}")
        print(f"Output was: {result.stdout[:200]}")
        return []


def categorize_issue(issue: Dict) -> str:
    """Determine which ClickUp list the issue belongs to"""
    title = issue["title"].lower()
    body = (issue.get("body") or "").lower()
    labels = [label["name"].lower() for label in issue.get("labels", [])]
    
    # Check labels first
    if "good first issue" in labels:
        # Check if it's a test-related issue
        if "test" in title or "test" in body:
            return "qa_testing"
        # Check if it's a data/persistence issue
        if "persistence" in title or "datastore" in body:
            return "data_layer"
        # Default to UI/UX for good first issues
        return "ui_ux"
    
    # Check title/body for keywords
    for keyword, list_key in ISSUE_CATEGORIES.items():
        if keyword in title or keyword in body:
            return list_key
    
    # Default to QA & Testing for bugs
    if "bug" in labels:
        return "qa_testing"
    
    # Default to Future Enhancements for enhancements
    if "enhancement" in labels:
        return "future"
    
    # Default to Documentation
    return "documentation"


def determine_priority(issue: Dict) -> str:
    """Determine issue priority based on title and labels"""
    title = issue["title"].lower()
    
    for priority, keywords in PRIORITY_MAPPING.items():
        for keyword in keywords:
            if keyword in title:
                return priority
    
    # Default priority based on labels
    labels = [label["name"].lower() for label in issue.get("labels", [])]
    if "bug" in labels:
        return "medium"
    if "enhancement" in labels:
        return "low"
    
    return "medium"


def determine_difficulty(issue: Dict) -> str:
    """Determine issue difficulty"""
    title = issue["title"].lower()
    labels = [label["name"].lower() for label in issue.get("labels", [])]
    
    # Check for explicit difficulty in title
    if "[easy]" in title:
        return "Easy"
    if "[medium]" in title:
        return "Medium"
    if "[hard]" in title:
        return "Hard"
    
    # Good first issues are usually Easy or Medium
    if "good first issue" in labels:
        # Check complexity indicators
        if "test" in title or "filter" in title:
            return "Medium"
        return "Easy"
    
    # Bugs are usually Medium
    if "bug" in labels:
        # Multiple related bugs might be harder
        if "unresponsive" in title or "not working" in title:
            return "Medium"
        return "Easy"
    
    # Enhancements vary
    if "enhancement" in labels:
        if "design" in title or "missing" in title:
            return "Medium"
        return "Easy"
    
    return "Medium"


def estimate_time(difficulty: str, issue: Dict) -> str:
    """Estimate time based on difficulty and issue type"""
    title = issue["title"].lower()
    
    # Check for explicit time estimate in body
    body = issue.get("body") or ""
    if "2-4 hours" in body or "2-4h" in body:
        return "2-4h"
    if "4-6 hours" in body:
        return "1d"
    if "8-12 hours" in body:
        return "2-3d"
    
    # Estimate based on difficulty
    if difficulty == "Easy":
        return "2-4h"
    elif difficulty == "Medium":
        return "1d"
    elif difficulty == "Hard":
        return "2-3d"
    
    return "1d"


def create_clickup_task(issue: Dict, list_id: str, dry_run: bool = False) -> Optional[str]:
    """Create a ClickUp task from a GitHub issue"""
    
    # Prepare task data
    title = issue["title"]
    description = f"""**GitHub Issue**: {issue['url']}

**Created**: {issue['createdAt']}
**Author**: {issue['author']['login']}

---

{issue.get('body', 'No description provided.')}
"""
    
    # Determine custom field values
    difficulty = determine_difficulty(issue)
    priority = determine_priority(issue)
    estimated_time = estimate_time(difficulty, issue)
    labels = [label["name"] for label in issue.get("labels", [])]
    is_good_first_issue = "good first issue" in [l.lower() for l in labels]
    
    task_data = {
        "name": title,
        "description": description,
        "status": "to do",
        "priority": 3 if priority == "high" else 4,  # 3 = high, 4 = normal
        "tags": labels,
    }
    
    if dry_run:
        print(f"\n📋 Would create ClickUp task:")
        print(f"   Title: {title}")
        print(f"   List: {list_id}")
        print(f"   Difficulty: {difficulty}")
        print(f"   Priority: {priority}")
        print(f"   Estimated Time: {estimated_time}")
        print(f"   Good First Issue: {is_good_first_issue}")
        print(f"   GitHub Issue: {issue['url']}")
        return None
    
    # Create task via API
    url = f"{CLICKUP_API_BASE}/list/{list_id}/task"
    response = requests.post(url, headers=get_clickup_headers(), json=task_data)
    
    if response.status_code == 200:
        task = response.json()
        task_id = task["id"]
        print(f"✅ Created ClickUp task: {task_id} - {title}")
        return task_id
    else:
        print(f"❌ Failed to create task: {response.status_code} - {response.text}")
        return None


def add_comment_to_github_issue(issue_number: int, clickup_task_id: str, dry_run: bool = False):
    """Add a comment to GitHub issue with ClickUp task link"""
    comment = f"""📋 **ClickUp Task Created**

This issue is now tracked in ClickUp: [CU-{clickup_task_id}](https://app.clickup.com/t/{clickup_task_id})

Team members: Please work from the ClickUp task and reference this GitHub issue for context.
"""

    if dry_run:
        print(f"   Would add comment to GitHub issue #{issue_number}")
        return

    try:
        cmd = [
            "gh", "issue", "comment", str(issue_number),
            "--repo", GITHUB_REPO,
            "--body", comment
        ]
        subprocess.run(cmd, check=True, capture_output=True, encoding='utf-8', errors='replace')
        print(f"   ✅ Added comment to GitHub issue #{issue_number}")
    except subprocess.CalledProcessError as e:
        print(f"   ❌ Failed to add comment: {e}")


def main():
    parser = argparse.ArgumentParser(description="Sync GitHub issues to ClickUp tasks")
    parser.add_argument("--all", action="store_true", help="Sync all open issues")
    parser.add_argument("--issue", type=int, help="Sync specific issue number")
    parser.add_argument("--priority", choices=["critical", "high", "medium", "low"], help="Sync only issues with this priority")
    parser.add_argument("--dry-run", action="store_true", help="Preview without creating tasks")
    
    args = parser.parse_args()
    
    # Validate environment
    if not CLICKUP_API_TOKEN:
        print("❌ Error: CLICKUP_API_TOKEN not found in .env file")
        sys.exit(1)
    
    print("🔄 GitHub Issues → ClickUp Tasks Sync")
    print("=" * 50)
    
    if args.dry_run:
        print("⚠️  DRY RUN MODE - No tasks will be created\n")
    
    # Fetch issues
    if args.issue:
        issue = get_github_issue(args.issue)
        if not issue:
            sys.exit(1)
        issues = [issue]
    elif args.all:
        issues = get_all_github_issues()
    else:
        print("❌ Error: Specify --all or --issue <number>")
        sys.exit(1)
    
    # Filter by priority if specified
    if args.priority:
        issues = [i for i in issues if determine_priority(i) == args.priority]
        print(f"📊 Filtered to {len(issues)} {args.priority} priority issues\n")
    
    # Process each issue
    created_count = 0
    for issue in issues:
        issue_number = issue["number"]
        print(f"\n📌 Processing GitHub Issue #{issue_number}: {issue['title']}")
        
        # Determine list
        list_key = categorize_issue(issue)
        list_id = LIST_IDS.get(list_key)
        
        if not list_id:
            print(f"   ⚠️  Unknown list key: {list_key}, skipping")
            continue
        
        print(f"   📂 Category: {list_key}")
        
        # Create ClickUp task
        task_id = create_clickup_task(issue, list_id, dry_run=args.dry_run)
        
        if task_id:
            created_count += 1
            # Add comment to GitHub issue
            add_comment_to_github_issue(issue_number, task_id, dry_run=args.dry_run)
    
    print("\n" + "=" * 50)
    if args.dry_run:
        print(f"✅ Dry run complete: Would create {len(issues)} ClickUp tasks")
    else:
        print(f"✅ Sync complete: Created {created_count}/{len(issues)} ClickUp tasks")


if __name__ == "__main__":
    main()

