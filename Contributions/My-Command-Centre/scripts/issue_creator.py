#!/usr/bin/env python3
"""
Issue Creator Script for My Command Centre
Creates GitHub issues from generated alerts for tracking and management
"""

import json
import argparse
import yaml
import os
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
import hashlib

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class IssueCreator:
    """Create GitHub issues from alerts"""
    
    def __init__(self, config_path: str, github_token: str):
        """Initialize the issue creator"""
        self.config = self._load_config(config_path)
        self.github_token = github_token
        self.alert_config = self.config.get('intelligence', {}).get('alerts', {})
        
        # Check if GitHub CLI is available
        self._verify_github_cli()
        
        logger.info("Initialized issue creator")
    
    def _load_config(self, config_path: str) -> Dict[str, Any]:
        """Load configuration from YAML file"""
        try:
            with open(config_path, 'r') as file:
                config = yaml.safe_load(file)
            logger.info(f"Loaded configuration from {config_path}")
            return config
        except Exception as e:
            logger.error(f"Failed to load configuration: {e}")
            raise
    
    def _verify_github_cli(self) -> None:
        """Verify GitHub CLI is available and authenticated"""
        try:
            result = subprocess.run(['gh', 'auth', 'status'], 
                                  capture_output=True, text=True, check=True)
            logger.info("GitHub CLI is authenticated and ready")
        except subprocess.CalledProcessError:
            logger.warning("GitHub CLI not authenticated, attempting to authenticate")
            try:
                # Set up authentication with token
                env = os.environ.copy()
                env['GITHUB_TOKEN'] = self.github_token
                subprocess.run(['gh', 'auth', 'login', '--with-token'], 
                             input=self.github_token, text=True, env=env, check=True)
                logger.info("GitHub CLI authentication successful")
            except subprocess.CalledProcessError as e:
                logger.error(f"Failed to authenticate GitHub CLI: {e}")
                raise
        except FileNotFoundError:
            logger.error("GitHub CLI (gh) not found. Please install GitHub CLI.")
            raise
    
    def create_issues_from_alerts(self, alerts_path: str) -> None:
        """Create GitHub issues from alerts file"""
        logger.info("Creating GitHub issues from alerts")
        
        # Load alerts
        with open(alerts_path, 'r') as file:
            alert_data = json.load(file)
        
        alerts = alert_data.get('alerts', [])
        if not alerts:
            logger.info("No alerts to process")
            return
        
        # Check if issue creation is enabled
        if not self.alert_config.get('enable_github_issues', True):
            logger.info("GitHub issue creation is disabled in configuration")
            return
        
        # Get existing issues to avoid duplicates
        existing_issues = self._get_existing_alert_issues()
        
        created_count = 0
        skipped_count = 0
        
        for alert in alerts:
            try:
                # Check if issue already exists
                issue_id = self._generate_issue_id(alert)
                if issue_id in existing_issues:
                    logger.debug(f"Issue already exists for alert: {alert['title']}")
                    skipped_count += 1
                    continue
                
                # Create issue
                if self._create_github_issue(alert):
                    created_count += 1
                    logger.info(f"Created issue for alert: {alert['title']}")
                else:
                    logger.warning(f"Failed to create issue for alert: {alert['title']}")
                
            except Exception as e:
                logger.error(f"Error processing alert {alert.get('title', 'Unknown')}: {e}")
                continue
        
        logger.info(f"Issue creation completed: {created_count} created, {skipped_count} skipped")
    
    def _get_existing_alert_issues(self) -> set:
        """Get list of existing alert issues to avoid duplicates"""
        try:
            # Get issues with command-centre label
            labels = self.alert_config.get('alert_labels', ['command-centre', 'automated-alert'])
            label_filter = ','.join(labels)
            
            cmd = ['gh', 'issue', 'list', '--label', label_filter, '--json', 'title,body']
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            
            issues = json.loads(result.stdout)
            existing_ids = set()
            
            for issue in issues:
                # Extract alert ID from issue body
                body = issue.get('body', '')
                if 'Alert ID:' in body:
                    lines = body.split('\n')
                    for line in lines:
                        if line.strip().startswith('Alert ID:'):
                            alert_id = line.split(':', 1)[1].strip()
                            existing_ids.add(alert_id)
                            break
            
            logger.info(f"Found {len(existing_ids)} existing alert issues")
            return existing_ids
            
        except Exception as e:
            logger.warning(f"Could not retrieve existing issues: {e}")
            return set()
    
    def _generate_issue_id(self, alert: Dict[str, Any]) -> str:
        """Generate unique ID for alert"""
        # Use alert ID if available, otherwise generate from content
        if 'id' in alert:
            return alert['id']
        
        # Generate hash from alert content
        content = f"{alert.get('title', '')}{alert.get('type', '')}{alert.get('created_at', '')}"
        return hashlib.md5(content.encode()).hexdigest()[:12]
    
    def _create_github_issue(self, alert: Dict[str, Any]) -> bool:
        """Create a GitHub issue from an alert"""
        try:
            # Prepare issue content
            title = self._format_issue_title(alert)
            body = self._format_issue_body(alert)
            labels = self._get_issue_labels(alert)
            
            # Create issue using GitHub CLI
            cmd = ['gh', 'issue', 'create', '--title', title, '--body', body]
            
            # Add labels
            if labels:
                cmd.extend(['--label', ','.join(labels)])
            
            # Set assignee to self if configured
            cmd.extend(['--assignee', '@me'])
            
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            
            # Extract issue URL from output
            issue_url = result.stdout.strip()
            logger.debug(f"Created issue: {issue_url}")
            
            return True
            
        except subprocess.CalledProcessError as e:
            logger.error(f"Failed to create GitHub issue: {e}")
            logger.error(f"Command output: {e.stdout}")
            logger.error(f"Command error: {e.stderr}")
            return False
    
    def _format_issue_title(self, alert: Dict[str, Any]) -> str:
        """Format issue title from alert"""
        priority = alert.get('priority', 'medium').upper()
        title = alert.get('title', 'Command Centre Alert')
        
        # Add priority prefix for high/critical alerts
        if priority in ['HIGH', 'CRITICAL']:
            return f"[{priority}] {title}"
        else:
            return f"[ALERT] {title}"
    
    def _format_issue_body(self, alert: Dict[str, Any]) -> str:
        """Format issue body from alert"""
        alert_id = self._generate_issue_id(alert)
        created_at = alert.get('created_at', datetime.now(timezone.utc).isoformat())
        
        body = f"""## Alert Details

**Alert ID:** {alert_id}  
**Type:** {alert.get('type', 'Unknown')}  
**Priority:** {alert.get('priority', 'Medium').title()}  
**Created:** {created_at}  

### Description

{alert.get('description', 'No description available')}

"""
        
        # Add details section if available
        details = alert.get('details', {})
        if details:
            body += "### Details\n\n"
            
            # Affected repositories
            if 'affected_repositories' in details:
                affected = details['affected_repositories']
                total_affected = details.get('total_affected', len(affected))
                
                body += f"**Affected Repositories:** {total_affected} total\n\n"
                
                if isinstance(affected[0], dict):
                    # Detailed repository info
                    for repo in affected[:5]:  # Show max 5
                        body += f"- **{repo.get('name', 'Unknown')}**"
                        if 'days_stale' in repo:
                            body += f" (stale for {repo['days_stale']} days)"
                        elif 'score' in repo:
                            body += f" (health score: {repo['score']})"
                        body += "\n"
                else:
                    # Simple repository list
                    for repo in affected[:5]:
                        body += f"- {repo}\n"
                
                if total_affected > 5:
                    body += f"- ... and {total_affected - 5} more\n"
                
                body += "\n"
            
            # Recommended actions
            if 'recommended_actions' in details:
                body += "**Recommended Actions:**\n\n"
                for action in details['recommended_actions']:
                    body += f"- [ ] {action}\n"
                body += "\n"
            
            # Additional metrics
            for key, value in details.items():
                if key not in ['affected_repositories', 'recommended_actions', 'total_affected']:
                    if isinstance(value, (int, float)):
                        body += f"**{key.replace('_', ' ').title()}:** {value}\n"
                    elif isinstance(value, str) and len(value) < 100:
                        body += f"**{key.replace('_', ' ').title()}:** {value}\n"
        
        # Add footer
        body += f"""
---

### Next Steps

1. Review the affected repositories and details above
2. Complete the recommended actions checklist
3. Update this issue with progress notes
4. Close this issue when resolved

**Auto-generated by My Command Centre** | [View Dashboard](../../) | [Analytics](../../tree/main/analytics)

> This issue was automatically created from portfolio analysis. Regular monitoring helps maintain repository health and identifies optimization opportunities.
"""
        
        return body
    
    def _get_issue_labels(self, alert: Dict[str, Any]) -> List[str]:
        """Get labels for the issue"""
        labels = self.alert_config.get('alert_labels', ['command-centre', 'automated-alert']).copy()
        
        # Add priority label
        priority = alert.get('priority', 'medium').lower()
        labels.append(f"priority-{priority}")
        
        # Add type label
        alert_type = alert.get('type', 'unknown').replace('_', '-')
        labels.append(f"type-{alert_type}")
        
        # Add custom labels from alert
        if 'labels' in alert:
            labels.extend(alert['labels'])
        
        # Remove duplicates and return
        return list(set(labels))
    
    def close_resolved_issues(self) -> None:
        """Close issues that have been resolved (optional feature)"""
        try:
            # This could be enhanced to automatically close issues
            # when the underlying problems are resolved
            logger.info("Checking for resolved issues...")
            
            # Get open alert issues
            labels = self.alert_config.get('alert_labels', ['command-centre', 'automated-alert'])
            label_filter = ','.join(labels)
            
            cmd = ['gh', 'issue', 'list', '--label', label_filter, '--state', 'open', '--json', 'number,title,body']
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            
            open_issues = json.loads(result.stdout)
            logger.info(f"Found {len(open_issues)} open alert issues")
            
            # For now, just log the count
            # Future enhancement: implement resolution detection logic
            
        except Exception as e:
            logger.warning(f"Could not check resolved issues: {e}")

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Create GitHub issues from alerts')
    parser.add_argument('--alerts', required=True, help='Path to alerts JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--github-token', help='GitHub token (or use GITHUB_TOKEN env var)')
    parser.add_argument('--close-resolved', action='store_true', help='Check and close resolved issues')
    
    args = parser.parse_args()
    
    try:
        # Get GitHub token
        github_token = args.github_token or os.getenv('GITHUB_TOKEN')
        if not github_token:
            logger.error("GitHub token is required. Use --github-token or set GITHUB_TOKEN environment variable.")
            return
        
        # Initialize issue creator
        creator = IssueCreator(args.config, github_token)
        
        # Create issues from alerts
        creator.create_issues_from_alerts(args.alerts)
        
        # Optionally check for resolved issues
        if args.close_resolved:
            creator.close_resolved_issues()
        
        logger.info("Issue creation process completed successfully")
        
    except Exception as e:
        logger.error(f"Issue creation failed: {e}")
        raise

if __name__ == '__main__':
    main()
