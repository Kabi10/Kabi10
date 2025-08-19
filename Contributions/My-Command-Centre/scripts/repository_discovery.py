#!/usr/bin/env python3
"""
Repository Discovery Script for My Command Centre
Discovers and catalogs all public/private repositories using GitHub CLI and API
"""

import json
import os
import sys
import subprocess
import argparse
import yaml
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
from github import Github
import requests

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class RepositoryDiscovery:
    """Main class for repository discovery and metadata collection"""
    
    def __init__(self, config_path: str, github_token: Optional[str] = None):
        """Initialize the repository discovery system"""
        self.config = self._load_config(config_path)
        self.github_token = github_token or os.getenv('GITHUB_TOKEN')
        
        if not self.github_token:
            raise ValueError("GitHub token is required. Set GITHUB_TOKEN environment variable.")
        
        self.github = Github(self.github_token)
        self.user = self.github.get_user()
        
        logger.info(f"Initialized repository discovery for user: {self.user.login}")
    
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
    
    def discover_repositories(self, force_scan: bool = False) -> List[Dict[str, Any]]:
        """Discover all repositories using GitHub CLI and API"""
        logger.info("Starting repository discovery...")
        
        repositories = []
        
        try:
            # Get repositories using GitHub CLI for comprehensive access
            cmd = ["gh", "repo", "list", "--json", 
                   "name,owner,isPrivate,isFork,isArchived,createdAt,updatedAt,url,description,primaryLanguage,stargazerCount,forkCount"]
            
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            repo_list = json.loads(result.stdout)
            
            logger.info(f"Found {len(repo_list)} repositories via GitHub CLI")
            
            # Process each repository
            for repo_data in repo_list:
                if self._should_include_repository(repo_data):
                    enhanced_repo = self._enhance_repository_data(repo_data)
                    repositories.append(enhanced_repo)
            
            logger.info(f"Processed {len(repositories)} repositories after filtering")
            
        except subprocess.CalledProcessError as e:
            logger.error(f"GitHub CLI command failed: {e}")
            # Fallback to PyGithub API
            repositories = self._fallback_api_discovery()
        except Exception as e:
            logger.error(f"Repository discovery failed: {e}")
            raise
        
        return repositories
    
    def _should_include_repository(self, repo_data: Dict[str, Any]) -> bool:
        """Check if repository should be included based on filters"""
        filters = self.config.get('repository_discovery', {}).get('filters', {})
        
        # Check private/public filter
        if not filters.get('include_private', True) and repo_data.get('isPrivate'):
            return False
        
        if not filters.get('include_public', True) and not repo_data.get('isPrivate'):
            return False
        
        # Check fork filter
        if not filters.get('include_forks', True) and repo_data.get('isFork'):
            return False
        
        # Check archived filter
        if not filters.get('include_archived', True) and repo_data.get('isArchived'):
            return False
        
        # Check activity filter
        min_activity_days = filters.get('min_activity_days', 365)
        if min_activity_days > 0:
            updated_at = datetime.fromisoformat(repo_data.get('updatedAt', '').replace('Z', '+00:00'))
            days_since_update = (datetime.now(timezone.utc) - updated_at).days
            if days_since_update > min_activity_days:
                return False
        
        return True
    
    def _enhance_repository_data(self, repo_data: Dict[str, Any]) -> Dict[str, Any]:
        """Enhance repository data with additional metadata"""
        repo_name = repo_data['name']
        owner_login = repo_data['owner']['login']
        
        try:
            # Get repository object from PyGithub for additional data
            repo = self.github.get_repo(f"{owner_login}/{repo_name}")
            
            # Collect metadata based on configuration
            metadata_config = self.config.get('repository_discovery', {}).get('metadata', {})
            
            enhanced_data = {
                'name': repo_name,
                'full_name': repo.full_name,
                'owner': owner_login,
                'description': repo_data.get('description', ''),
                'url': repo_data.get('url', ''),
                'is_private': repo_data.get('isPrivate', False),
                'is_fork': repo_data.get('isFork', False),
                'is_archived': repo_data.get('isArchived', False),
                'created_at': repo_data.get('createdAt', ''),
                'updated_at': repo_data.get('updatedAt', ''),
                'pushed_at': repo.pushed_at.isoformat() if repo.pushed_at else None,
                'size': repo.size,
                'default_branch': repo.default_branch,
                'discovery_timestamp': datetime.now(timezone.utc).isoformat()
            }
            
            # Add optional metadata
            if metadata_config.get('collect_stars', True):
                enhanced_data['stargazers_count'] = repo.stargazers_count
            
            if metadata_config.get('collect_forks', True):
                enhanced_data['forks_count'] = repo.forks_count
            
            if metadata_config.get('collect_languages', True):
                enhanced_data['languages'] = repo.get_languages()
                enhanced_data['primary_language'] = repo.language
            
            if metadata_config.get('collect_contributors', True):
                try:
                    contributors = list(repo.get_contributors())
                    enhanced_data['contributors_count'] = len(contributors)
                    enhanced_data['top_contributors'] = [
                        {'login': c.login, 'contributions': c.contributions} 
                        for c in contributors[:5]
                    ]
                except Exception as e:
                    logger.warning(f"Could not get contributors for {repo_name}: {e}")
                    enhanced_data['contributors_count'] = 0
            
            if metadata_config.get('collect_issues', True):
                enhanced_data['open_issues_count'] = repo.open_issues_count
                try:
                    # Get recent issues for response time analysis
                    recent_issues = list(repo.get_issues(state='all', sort='updated')[:10])
                    enhanced_data['recent_issues'] = [
                        {
                            'number': issue.number,
                            'title': issue.title,
                            'state': issue.state,
                            'created_at': issue.created_at.isoformat(),
                            'updated_at': issue.updated_at.isoformat(),
                            'closed_at': issue.closed_at.isoformat() if issue.closed_at else None
                        }
                        for issue in recent_issues
                    ]
                except Exception as e:
                    logger.warning(f"Could not get issues for {repo_name}: {e}")
            
            if metadata_config.get('collect_pull_requests', True):
                try:
                    open_prs = list(repo.get_pulls(state='open'))
                    enhanced_data['open_pull_requests_count'] = len(open_prs)
                except Exception as e:
                    logger.warning(f"Could not get pull requests for {repo_name}: {e}")
            
            if metadata_config.get('collect_releases', True):
                try:
                    releases = list(repo.get_releases()[:5])
                    enhanced_data['releases_count'] = repo.get_releases().totalCount
                    enhanced_data['latest_release'] = {
                        'tag_name': releases[0].tag_name,
                        'name': releases[0].name,
                        'published_at': releases[0].published_at.isoformat()
                    } if releases else None
                except Exception as e:
                    logger.warning(f"Could not get releases for {repo_name}: {e}")
            
            if metadata_config.get('collect_topics', True):
                enhanced_data['topics'] = repo.get_topics()
            
            # Add commit activity data
            try:
                # Get recent commits for activity analysis
                commits = list(repo.get_commits()[:50])
                enhanced_data['recent_commits_count'] = len(commits)
                if commits:
                    enhanced_data['last_commit'] = {
                        'sha': commits[0].sha,
                        'message': commits[0].commit.message,
                        'author': commits[0].commit.author.name,
                        'date': commits[0].commit.author.date.isoformat()
                    }
            except Exception as e:
                logger.warning(f"Could not get commits for {repo_name}: {e}")
            
            logger.debug(f"Enhanced data for repository: {repo_name}")
            return enhanced_data
            
        except Exception as e:
            logger.error(f"Failed to enhance data for repository {repo_name}: {e}")
            # Return basic data if enhancement fails
            return {
                'name': repo_name,
                'full_name': f"{owner_login}/{repo_name}",
                'owner': owner_login,
                'error': str(e),
                'discovery_timestamp': datetime.now(timezone.utc).isoformat()
            }
    
    def _fallback_api_discovery(self) -> List[Dict[str, Any]]:
        """Fallback method using PyGithub API directly"""
        logger.info("Using fallback API discovery method")
        repositories = []
        
        try:
            for repo in self.user.get_repos():
                repo_data = {
                    'name': repo.name,
                    'owner': {'login': repo.owner.login},
                    'isPrivate': repo.private,
                    'isFork': repo.fork,
                    'isArchived': repo.archived,
                    'createdAt': repo.created_at.isoformat(),
                    'updatedAt': repo.updated_at.isoformat(),
                    'url': repo.html_url,
                    'description': repo.description
                }
                
                if self._should_include_repository(repo_data):
                    enhanced_repo = self._enhance_repository_data(repo_data)
                    repositories.append(enhanced_repo)
            
            logger.info(f"Fallback discovery found {len(repositories)} repositories")
            
        except Exception as e:
            logger.error(f"Fallback API discovery failed: {e}")
            raise
        
        return repositories
    
    def save_repositories(self, repositories: List[Dict[str, Any]], output_path: str) -> None:
        """Save repository data to JSON file"""
        try:
            # Ensure output directory exists
            output_dir = Path(output_path).parent
            output_dir.mkdir(parents=True, exist_ok=True)
            
            # Prepare output data
            output_data = {
                'discovery_metadata': {
                    'timestamp': datetime.now(timezone.utc).isoformat(),
                    'total_repositories': len(repositories),
                    'discovery_version': '1.0.0',
                    'config_used': self.config.get('repository_discovery', {})
                },
                'repositories': repositories
            }
            
            # Save to file
            with open(output_path, 'w') as file:
                json.dump(output_data, file, indent=2, default=str)
            
            logger.info(f"Saved {len(repositories)} repositories to {output_path}")
            
        except Exception as e:
            logger.error(f"Failed to save repositories: {e}")
            raise

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Discover and catalog GitHub repositories')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output JSON file path')
    parser.add_argument('--force-scan', action='store_true', help='Force full repository scan')
    parser.add_argument('--github-token', help='GitHub token (or use GITHUB_TOKEN env var)')
    
    args = parser.parse_args()
    
    try:
        # Initialize discovery system
        discovery = RepositoryDiscovery(args.config, args.github_token)
        
        # Discover repositories
        repositories = discovery.discover_repositories(args.force_scan)
        
        # Save results
        discovery.save_repositories(repositories, args.output)
        
        # Output summary for GitHub Actions
        print(f"repositories={json.dumps([r['name'] for r in repositories])}")
        print(f"total_count={len(repositories)}")
        
        logger.info("Repository discovery completed successfully")
        
    except Exception as e:
        logger.error(f"Repository discovery failed: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()
