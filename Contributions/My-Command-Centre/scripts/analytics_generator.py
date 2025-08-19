#!/usr/bin/env python3
"""
Analytics Generator Script for My Command Centre
Generates comprehensive analytics reports from repository data
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
from collections import Counter, defaultdict
import statistics

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AnalyticsGenerator:
    """Generate analytics reports from repository data"""
    
    def __init__(self, config_path: str):
        """Initialize the analytics generator"""
        self.config = self._load_config(config_path)
        self.analytics_config = self.config.get('analytics', {})
        
        logger.info("Initialized analytics generator")
    
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
    
    def generate_analytics(self, repositories: List[Dict[str, Any]], output_dir: str) -> None:
        """Generate all analytics reports"""
        logger.info(f"Generating analytics for {len(repositories)} repositories")
        
        # Ensure output directory exists
        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)
        
        # Generate different types of analytics
        if self.analytics_config.get('reports', {}).get('portfolio_overview', True):
            self._generate_portfolio_overview(repositories, output_path)
        
        if self.analytics_config.get('reports', {}).get('activity_heatmap', True):
            self._generate_activity_heatmap(repositories, output_path)
        
        if self.analytics_config.get('reports', {}).get('contribution_patterns', True):
            self._generate_contribution_patterns(repositories, output_path)
        
        if self.analytics_config.get('reports', {}).get('health_trends', True):
            self._generate_health_trends(repositories, output_path)
        
        # Generate summary analytics
        self._generate_summary_analytics(repositories, output_path)
        
        logger.info(f"Analytics generation completed. Reports saved to {output_dir}")
    
    def _generate_portfolio_overview(self, repositories: List[Dict[str, Any]], output_path: Path) -> None:
        """Generate portfolio overview analytics"""
        logger.info("Generating portfolio overview")
        
        # Calculate overview statistics
        total_repos = len(repositories)
        private_repos = sum(1 for repo in repositories if repo.get('is_private', False))
        public_repos = total_repos - private_repos
        forked_repos = sum(1 for repo in repositories if repo.get('is_fork', False))
        archived_repos = sum(1 for repo in repositories if repo.get('is_archived', False))
        
        # Language statistics
        all_languages = []
        for repo in repositories:
            languages = repo.get('languages', {})
            all_languages.extend(languages.keys())
        
        language_counts = Counter(all_languages)
        top_languages = language_counts.most_common(10)
        
        # Activity statistics
        total_stars = sum(repo.get('stargazers_count', 0) for repo in repositories)
        total_forks = sum(repo.get('forks_count', 0) for repo in repositories)
        total_contributors = sum(repo.get('contributors_count', 0) for repo in repositories)
        
        # Size statistics
        sizes = [repo.get('size', 0) for repo in repositories if repo.get('size', 0) > 0]
        avg_size = statistics.mean(sizes) if sizes else 0
        
        # Recent activity
        now = datetime.now(timezone.utc)
        active_last_month = sum(
            1 for repo in repositories 
            if repo.get('updated_at') and 
            (now - datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))).days <= 30
        )
        
        overview = {
            'generated_at': now.isoformat(),
            'repository_counts': {
                'total': total_repos,
                'public': public_repos,
                'private': private_repos,
                'forked': forked_repos,
                'archived': archived_repos,
                'active_last_month': active_last_month
            },
            'language_distribution': {
                'top_languages': [{'language': lang, 'count': count} for lang, count in top_languages],
                'total_languages': len(language_counts)
            },
            'engagement_metrics': {
                'total_stars': total_stars,
                'total_forks': total_forks,
                'total_contributors': total_contributors,
                'avg_stars_per_repo': round(total_stars / total_repos, 2) if total_repos > 0 else 0,
                'avg_forks_per_repo': round(total_forks / total_repos, 2) if total_repos > 0 else 0
            },
            'size_metrics': {
                'average_size_kb': round(avg_size, 2),
                'total_size_kb': sum(sizes)
            }
        }
        
        # Save overview
        with open(output_path / 'portfolio_overview.json', 'w') as file:
            json.dump(overview, file, indent=2, default=str)
        
        # Generate markdown report
        self._generate_overview_markdown(overview, output_path)
    
    def _generate_overview_markdown(self, overview: Dict[str, Any], output_path: Path) -> None:
        """Generate markdown version of portfolio overview"""
        markdown_content = f"""# Portfolio Overview

*Generated on {overview['generated_at']}*

## Repository Statistics

| Metric | Count |
|--------|-------|
| Total Repositories | {overview['repository_counts']['total']} |
| Public Repositories | {overview['repository_counts']['public']} |
| Private Repositories | {overview['repository_counts']['private']} |
| Forked Repositories | {overview['repository_counts']['forked']} |
| Archived Repositories | {overview['repository_counts']['archived']} |
| Active (Last Month) | {overview['repository_counts']['active_last_month']} |

## Language Distribution

| Language | Repository Count |
|----------|------------------|
"""
        
        for lang_data in overview['language_distribution']['top_languages']:
            markdown_content += f"| {lang_data['language']} | {lang_data['count']} |\n"
        
        markdown_content += f"""
## Engagement Metrics

| Metric | Value |
|--------|-------|
| Total Stars | {overview['engagement_metrics']['total_stars']} |
| Total Forks | {overview['engagement_metrics']['total_forks']} |
| Total Contributors | {overview['engagement_metrics']['total_contributors']} |
| Average Stars per Repository | {overview['engagement_metrics']['avg_stars_per_repo']} |
| Average Forks per Repository | {overview['engagement_metrics']['avg_forks_per_repo']} |

## Size Metrics

| Metric | Value |
|--------|-------|
| Average Repository Size | {overview['size_metrics']['average_size_kb']} KB |
| Total Portfolio Size | {overview['size_metrics']['total_size_kb']} KB |

---
*This report is automatically generated by My Command Centre*
"""
        
        with open(output_path / 'portfolio_overview.md', 'w') as file:
            file.write(markdown_content)
    
    def _generate_activity_heatmap(self, repositories: List[Dict[str, Any]], output_path: Path) -> None:
        """Generate activity heatmap data"""
        logger.info("Generating activity heatmap")
        
        # Collect activity data by date
        activity_by_date = defaultdict(int)
        
        for repo in repositories:
            # Count recent commits by date
            if 'last_commit' in repo and repo['last_commit']:
                commit_date = datetime.fromisoformat(repo['last_commit']['date'].replace('Z', '+00:00')).date()
                activity_by_date[commit_date.isoformat()] += 1
            
            # Count repository updates
            if repo.get('updated_at'):
                update_date = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00')).date()
                activity_by_date[update_date.isoformat()] += 1
        
        # Generate last 365 days of activity
        end_date = datetime.now(timezone.utc).date()
        start_date = end_date - timedelta(days=365)
        
        heatmap_data = []
        current_date = start_date
        
        while current_date <= end_date:
            date_str = current_date.isoformat()
            activity_count = activity_by_date.get(date_str, 0)
            
            heatmap_data.append({
                'date': date_str,
                'activity_count': activity_count,
                'day_of_week': current_date.weekday(),
                'week_of_year': current_date.isocalendar()[1]
            })
            
            current_date += timedelta(days=1)
        
        heatmap_analytics = {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'date_range': {
                'start_date': start_date.isoformat(),
                'end_date': end_date.isoformat()
            },
            'activity_data': heatmap_data,
            'summary': {
                'total_active_days': len([d for d in heatmap_data if d['activity_count'] > 0]),
                'max_daily_activity': max(d['activity_count'] for d in heatmap_data),
                'avg_daily_activity': round(statistics.mean(d['activity_count'] for d in heatmap_data), 2)
            }
        }
        
        with open(output_path / 'activity_heatmap.json', 'w') as file:
            json.dump(heatmap_analytics, file, indent=2, default=str)
    
    def _generate_contribution_patterns(self, repositories: List[Dict[str, Any]], output_path: Path) -> None:
        """Generate contribution pattern analytics"""
        logger.info("Generating contribution patterns")
        
        # Analyze contribution patterns
        contribution_data = {
            'open_source_repos': [],
            'personal_projects': [],
            'collaborative_repos': [],
            'maintenance_repos': []
        }
        
        for repo in repositories:
            repo_analysis = {
                'name': repo['name'],
                'full_name': repo['full_name'],
                'is_fork': repo.get('is_fork', False),
                'contributors_count': repo.get('contributors_count', 0),
                'stargazers_count': repo.get('stargazers_count', 0),
                'forks_count': repo.get('forks_count', 0),
                'primary_language': repo.get('primary_language'),
                'last_activity': repo.get('updated_at')
            }
            
            # Categorize repositories
            if repo.get('is_fork', False):
                contribution_data['open_source_repos'].append(repo_analysis)
            elif repo.get('contributors_count', 0) > 1:
                contribution_data['collaborative_repos'].append(repo_analysis)
            elif repo.get('stargazers_count', 0) > 0 or repo.get('forks_count', 0) > 0:
                contribution_data['personal_projects'].append(repo_analysis)
            else:
                contribution_data['maintenance_repos'].append(repo_analysis)
        
        # Generate patterns summary
        patterns_summary = {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'contribution_categories': {
                'open_source_contributions': len(contribution_data['open_source_repos']),
                'personal_projects': len(contribution_data['personal_projects']),
                'collaborative_projects': len(contribution_data['collaborative_repos']),
                'maintenance_projects': len(contribution_data['maintenance_repos'])
            },
            'detailed_analysis': contribution_data
        }
        
        with open(output_path / 'contribution_patterns.json', 'w') as file:
            json.dump(patterns_summary, file, indent=2, default=str)
    
    def _generate_health_trends(self, repositories: List[Dict[str, Any]], output_path: Path) -> None:
        """Generate health trends analytics"""
        logger.info("Generating health trends")
        
        # Analyze repository health indicators
        health_indicators = []
        
        for repo in repositories:
            # Calculate basic health indicators
            last_update = repo.get('updated_at')
            days_since_update = 0
            if last_update:
                update_date = datetime.fromisoformat(last_update.replace('Z', '+00:00'))
                days_since_update = (datetime.now(timezone.utc) - update_date).days
            
            health_data = {
                'name': repo['name'],
                'full_name': repo['full_name'],
                'days_since_update': days_since_update,
                'has_description': bool(repo.get('description')),
                'has_topics': len(repo.get('topics', [])) > 0,
                'open_issues_count': repo.get('open_issues_count', 0),
                'is_archived': repo.get('is_archived', False),
                'community_engagement': repo.get('stargazers_count', 0) + repo.get('forks_count', 0),
                'estimated_health': 'good' if days_since_update < 90 and not repo.get('is_archived', False) else 'needs_attention'
            }
            
            health_indicators.append(health_data)
        
        # Generate trends summary
        healthy_repos = [r for r in health_indicators if r['estimated_health'] == 'good']
        needs_attention = [r for r in health_indicators if r['estimated_health'] == 'needs_attention']
        
        trends_summary = {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'health_overview': {
                'healthy_repositories': len(healthy_repos),
                'repositories_needing_attention': len(needs_attention),
                'health_percentage': round(len(healthy_repos) / len(repositories) * 100, 2) if repositories else 0
            },
            'detailed_health_data': health_indicators,
            'recommendations': self._generate_health_recommendations(health_indicators)
        }
        
        with open(output_path / 'health_trends.json', 'w') as file:
            json.dump(trends_summary, file, indent=2, default=str)
    
    def _generate_health_recommendations(self, health_indicators: List[Dict[str, Any]]) -> List[str]:
        """Generate health improvement recommendations"""
        recommendations = []
        
        # Analyze common issues
        repos_without_description = [r for r in health_indicators if not r['has_description']]
        repos_without_topics = [r for r in health_indicators if not r['has_topics']]
        stale_repos = [r for r in health_indicators if r['days_since_update'] > 180]
        repos_with_many_issues = [r for r in health_indicators if r['open_issues_count'] > 10]
        
        if repos_without_description:
            recommendations.append(f"Add descriptions to {len(repos_without_description)} repositories")
        
        if repos_without_topics:
            recommendations.append(f"Add topics/tags to {len(repos_without_topics)} repositories")
        
        if stale_repos:
            recommendations.append(f"Update {len(stale_repos)} repositories that haven't been updated in 6+ months")
        
        if repos_with_many_issues:
            recommendations.append(f"Address open issues in {len(repos_with_many_issues)} repositories")
        
        return recommendations
    
    def _generate_summary_analytics(self, repositories: List[Dict[str, Any]], output_path: Path) -> None:
        """Generate summary analytics file"""
        logger.info("Generating summary analytics")
        
        summary = {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'total_repositories': len(repositories),
            'analytics_files_generated': [
                'portfolio_overview.json',
                'portfolio_overview.md',
                'activity_heatmap.json',
                'contribution_patterns.json',
                'health_trends.json'
            ],
            'next_generation_scheduled': (datetime.now(timezone.utc) + timedelta(days=1)).isoformat()
        }
        
        with open(output_path / 'analytics_summary.json', 'w') as file:
            json.dump(summary, file, indent=2, default=str)

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Generate repository analytics')
    parser.add_argument('--input', required=True, help='Input repositories JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output-dir', required=True, help='Output directory for analytics')
    
    args = parser.parse_args()
    
    try:
        # Load repository data
        with open(args.input, 'r') as file:
            repo_data = json.load(file)
        
        repositories = repo_data.get('repositories', [])
        logger.info(f"Loaded {len(repositories)} repositories from {args.input}")
        
        # Initialize analytics generator
        generator = AnalyticsGenerator(args.config)
        
        # Generate analytics
        generator.generate_analytics(repositories, args.output_dir)
        
        logger.info("Analytics generation completed successfully")
        
    except Exception as e:
        logger.error(f"Analytics generation failed: {e}")
        raise

if __name__ == '__main__':
    main()
