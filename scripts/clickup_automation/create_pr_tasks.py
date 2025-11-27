#!/usr/bin/env python3
"""
Create ClickUp tasks for merged PRs that don't have tasks

This script creates ClickUp tasks for the 4 PRs merged today that don't have corresponding tasks.

Author: Agrimarket Team
Date: 2025-11-26
"""

import os
import sys
from dotenv import load_dotenv
import requests

# Load environment variables
load_dotenv()

# Configuration
CLICKUP_API_TOKEN = os.getenv("CLICKUP_API_TOKEN")
CLICKUP_API_BASE = "https://api.clickup.com/api/v2"

# List IDs
LIST_IDS = {
    "ui_ux": "901812942655",
    "data_layer": "901812942659",
    "qa_testing": "901812942663",
}


def get_clickup_headers():
    """Get headers for ClickUp API requests"""
    return {
        "Authorization": CLICKUP_API_TOKEN,
        "Content-Type": "application/json"
    }


def create_task(name, description, list_id, status="complete", priority=2, tags=None):
    """Create a ClickUp task"""
    task_data = {
        "name": name,
        "description": description,
        "status": status,
        "priority": priority,
        "tags": tags or []
    }
    
    url = f"{CLICKUP_API_BASE}/list/{list_id}/task"
    
    try:
        response = requests.post(url, headers=get_clickup_headers(), json=task_data)
        if response.status_code == 200:
            task = response.json()
            print(f"✅ Created task: {name}")
            return task["id"]
        else:
            print(f"❌ Failed to create task: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"❌ Error creating task: {e}")
        return None


def main():
    """Create tasks for merged PRs"""
    print("🔄 Creating ClickUp Tasks for Merged PRs")
    print("=" * 60)
    
    if not CLICKUP_API_TOKEN:
        print("❌ Error: CLICKUP_API_TOKEN not found")
        sys.exit(1)
    
    # PR #23: DatabaseModule fix
    create_task(
        name="[CRITICAL] Fix DatabaseModule.kt duplicate LanguagePreferences provider",
        description="""**PR #23** - Merged 2025-11-26

**Problem**: DatabaseModule.kt had duplicate LanguagePreferences provider causing build failure.

**Solution**: Removed duplicate provider, keeping only the correct implementation.

**Impact**: Fixed critical build blocker that prevented app compilation.

**PR URL**: https://github.com/Kabi10/Srilanka-Farmers-Marketplace/pull/23
**Status**: ✅ Merged and deployed
""",
        list_id=LIST_IDS["data_layer"],
        status="complete",
        priority=1,  # Urgent
        tags=["critical", "bug", "build-fix"]
    )
    
    # PR #17: Sinhala title and error messages
    create_task(
        name="Fix Critical Trilingual Bugs - Sinhala Title and ViewModel Error Messages",
        description="""**PR #17** - Merged 2025-11-26

**Changes**:
1. Fixed Sinhala language showing Tamil title
2. Replaced all hardcoded English error messages with localized strings
3. Added 21 new error message string resources in all three languages
4. Updated all ViewModels to use translated error messages

**Impact**: Complete trilingual support for error messages across the app.

**PR URL**: https://github.com/Kabi10/Srilanka-Farmers-Marketplace/pull/17
**Status**: ✅ Merged and deployed
""",
        list_id=LIST_IDS["ui_ux"],
        status="complete",
        priority=2,  # High
        tags=["trilingual", "bug", "localization"]
    )
    
    # PR #18: Popular crops, placeholders, reviews
    create_task(
        name="Fix High-Priority Trilingual Issues - Popular Crops, Placeholders, Reviews",
        description="""**PR #18** - Merged 2025-11-26

**Changes**:
1. Translated popular crops section using CropTypes.getCropName()
2. Localized location input placeholders in CreateListingScreen and EditProfileScreen
3. Translated review count text in ListingDetailScreen
4. Removed deprecated .capitalize() function usage

**Impact**: Improved trilingual support consistency across UI components.

**PR URL**: https://github.com/Kabi10/Srilanka-Farmers-Marketplace/pull/18
**Status**: ✅ Merged and deployed
""",
        list_id=LIST_IDS["ui_ux"],
        status="complete",
        priority=2,  # High
        tags=["trilingual", "enhancement", "localization"]
    )
    
    # PR #16: Orders page and filter options
    create_task(
        name="Complete trilingual support: Orders page and filter options",
        description="""**PR #16** - Merged 2025-11-26

**Changes**:
1. Added LanguageToggleViewModel to TransactionsScreen
2. Translated all transaction status labels and UI elements
3. Added LanguageToggleViewModel to SearchScreen
4. Translated all filter options and search UI
5. Crop types and locations now use proper translation utilities

**Resolved Issues**: #5, #6

**Impact**: Complete trilingual support for Orders page and search filters.

**PR URL**: https://github.com/Kabi10/Srilanka-Farmers-Marketplace/pull/16
**Status**: ✅ Merged and deployed
""",
        list_id=LIST_IDS["ui_ux"],
        status="complete",
        priority=2,  # High
        tags=["trilingual", "enhancement", "localization"]
    )
    
    print("\n" + "=" * 60)
    print("✅ All PR tasks created successfully!")
    print("=" * 60)


if __name__ == "__main__":
    main()

