#!/usr/bin/env python3
"""
Alert Generator Script for My Command Centre
Generates intelligent alerts based on repository health and activity patterns
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
from collections import defaultdict

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AlertGenerator:
    """Generate intelligent alerts for portfolio management"""
    
    def __init__(self, config_path: str):
        """Initialize the alert generator"""
        self.config = self._load_config(config_path)
        self.intelligence_config = self.config.get('intelligence', {})
        self.alert_config = self.intelligence_config.get('alerts', {})
        
        logger.info("Initialized alert generator")
    
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
    
    def generate_alerts(self, repositories: List[Dict[str, Any]], 
                       health_scores: Dict[str, Any], output_path: str) -> None:
        """Generate all types of alerts"""
        logger.info("Generating portfolio alerts")
        
        alerts = []
        
        # Generate different types of alerts
        alerts.extend(self._generate_maintenance_alerts(repositories, health_scores))
        alerts.extend(self._generate_security_alerts(repositories))
        alerts.extend(self._generate_contribution_alerts(repositories))
        alerts.extend(self._generate_optimization_alerts(repositories, health_scores))
        alerts.extend(self._generate_activity_alerts(repositories))
        
        # Sort alerts by priority and date
        alerts.sort(key=lambda x: (self._get_priority_weight(x['priority']), x['created_at']), reverse=True)
        
        # Limit alerts based on configuration
        max_alerts = self.alert_config.get('max_daily_alerts', 10)
        alerts = alerts[:max_alerts]
        
        # Prepare alert data
        alert_data = {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'total_alerts': len(alerts),
            'alert_summary': self._generate_alert_summary(alerts),
            'alerts': alerts
        }
        
        # Save alerts
        output_dir = Path(output_path).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(output_path, 'w') as file:
            json.dump(alert_data, file, indent=2, default=str)
        
        logger.info(f"Generated {len(alerts)} alerts, saved to {output_path}")
    
    def _get_priority_weight(self, priority: str) -> int:
        """Get numeric weight for priority sorting"""
        weights = {'critical': 4, 'high': 3, 'medium': 2, 'low': 1}
        return weights.get(priority.lower(), 0)
    
    def _generate_maintenance_alerts(self, repositories: List[Dict[str, Any]], 
                                   health_scores: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate maintenance-related alerts"""
        alerts = []
        now = datetime.now(timezone.utc)
        
        # Stale repository alerts
        stale_repos = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    days_since_update = (now - updated).days
                    
                    if days_since_update > 365:
                        stale_repos.append({
                            'name': repo['name'],
                            'days_stale': days_since_update,
                            'last_update': repo['updated_at']
                        })
                except:
                    continue
        
        if stale_repos:
            alerts.append({
                'id': f"maintenance_stale_{now.strftime('%Y%m%d')}",
                'type': 'maintenance_needed',
                'priority': 'medium',
                'title': f'{len(stale_repos)} Repositories Need Maintenance',
                'description': f'Found {len(stale_repos)} repositories that haven\'t been updated in over a year',
                'details': {
                    'affected_repositories': stale_repos[:5],  # Show top 5
                    'total_affected': len(stale_repos),
                    'recommended_actions': [
                        'Review repository relevance',
                        'Archive outdated projects',
                        'Update dependencies and documentation',
                        'Consider consolidation opportunities'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['maintenance', 'stale-repositories']
            })
        
        # Poor health score alerts
        poor_health_repos = []
        for repo_name, health_data in health_scores.items():
            if isinstance(health_data, dict) and health_data.get('health_grade') == 'poor':
                poor_health_repos.append({
                    'name': repo_name.split('/')[-1],
                    'score': health_data.get('overall_score', 0),
                    'recommendations': health_data.get('recommendations', [])
                })
        
        if poor_health_repos:
            alerts.append({
                'id': f"health_poor_{now.strftime('%Y%m%d')}",
                'type': 'maintenance_needed',
                'priority': 'high',
                'title': f'{len(poor_health_repos)} Repositories Have Poor Health Scores',
                'description': f'Repositories with poor health scores need immediate attention',
                'details': {
                    'affected_repositories': poor_health_repos[:3],
                    'total_affected': len(poor_health_repos),
                    'common_issues': self._extract_common_issues(poor_health_repos),
                    'recommended_actions': [
                        'Improve documentation quality',
                        'Increase commit frequency',
                        'Address open issues',
                        'Add meaningful descriptions and topics'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['health', 'poor-score']
            })
        
        # Missing documentation alerts
        undocumented_repos = []
        for repo in repositories:
            if not repo.get('description') or len(repo.get('description', '')) < 10:
                undocumented_repos.append(repo['name'])
        
        if len(undocumented_repos) > 5:  # Only alert if significant number
            alerts.append({
                'id': f"docs_missing_{now.strftime('%Y%m%d')}",
                'type': 'maintenance_needed',
                'priority': 'low',
                'title': f'{len(undocumented_repos)} Repositories Lack Documentation',
                'description': 'Multiple repositories are missing descriptions or adequate documentation',
                'details': {
                    'affected_repositories': undocumented_repos[:5],
                    'total_affected': len(undocumented_repos),
                    'recommended_actions': [
                        'Add meaningful repository descriptions',
                        'Create or update README files',
                        'Add relevant topics and tags',
                        'Document installation and usage'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['documentation', 'missing-description']
            })
        
        return alerts
    
    def _generate_security_alerts(self, repositories: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Generate security-related alerts"""
        alerts = []
        now = datetime.now(timezone.utc)
        
        # Public repositories with potentially sensitive information
        public_repos_to_review = []
        for repo in repositories:
            if not repo.get('is_private', True):  # Public repository
                # Check for potentially sensitive patterns in name or description
                name = repo.get('name', '').lower()
                description = repo.get('description', '').lower()
                
                sensitive_keywords = ['config', 'secret', 'key', 'password', 'token', 'credential']
                if any(keyword in name or keyword in description for keyword in sensitive_keywords):
                    public_repos_to_review.append(repo['name'])
        
        if public_repos_to_review:
            alerts.append({
                'id': f"security_review_{now.strftime('%Y%m%d')}",
                'type': 'security_update',
                'priority': 'high',
                'title': 'Public Repositories May Contain Sensitive Information',
                'description': f'{len(public_repos_to_review)} public repositories may need security review',
                'details': {
                    'affected_repositories': public_repos_to_review,
                    'recommended_actions': [
                        'Review repository contents for sensitive information',
                        'Check commit history for accidentally committed secrets',
                        'Consider making repositories private if needed',
                        'Implement .gitignore for sensitive files'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['security', 'public-repository-review']
            })
        
        return alerts
    
    def _generate_contribution_alerts(self, repositories: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Generate contribution opportunity alerts"""
        alerts = []
        now = datetime.now(timezone.utc)
        
        # Open source contribution opportunities
        fork_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        
        # Check for forks that haven't been updated recently
        stale_forks = []
        for repo in fork_repos:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if (now - updated).days > 90:  # 3 months
                        stale_forks.append(repo['name'])
                except:
                    continue
        
        if stale_forks:
            alerts.append({
                'id': f"contrib_stale_forks_{now.strftime('%Y%m%d')}",
                'type': 'contribution_opportunity',
                'priority': 'medium',
                'title': f'{len(stale_forks)} Forked Repositories Need Attention',
                'description': 'Several forked repositories haven\'t been updated recently',
                'details': {
                    'affected_repositories': stale_forks[:5],
                    'total_affected': len(stale_forks),
                    'recommended_actions': [
                        'Sync with upstream repositories',
                        'Review for new contribution opportunities',
                        'Submit pending pull requests',
                        'Archive inactive forks'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['contribution', 'stale-forks']
            })
        
        # Low collaboration alert
        solo_repos = [repo for repo in repositories if repo.get('contributors_count', 0) <= 1]
        collaboration_percentage = (len(repositories) - len(solo_repos)) / len(repositories) * 100 if repositories else 0
        
        if collaboration_percentage < 20:  # Less than 20% collaborative
            alerts.append({
                'id': f"contrib_low_collaboration_{now.strftime('%Y%m%d')}",
                'type': 'contribution_opportunity',
                'priority': 'low',
                'title': 'Low Collaboration Rate Detected',
                'description': f'Only {collaboration_percentage:.1f}% of repositories have multiple contributors',
                'details': {
                    'collaboration_percentage': round(collaboration_percentage, 1),
                    'solo_repositories': len(solo_repos),
                    'recommended_actions': [
                        'Promote repositories to attract contributors',
                        'Participate in open source communities',
                        'Create beginner-friendly issues',
                        'Improve documentation to welcome contributors'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['collaboration', 'community-building']
            })
        
        return alerts
    
    def _generate_optimization_alerts(self, repositories: List[Dict[str, Any]], 
                                    health_scores: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate portfolio optimization alerts"""
        alerts = []
        now = datetime.now(timezone.utc)
        
        # Repository consolidation opportunities
        small_repos = [repo for repo in repositories if repo.get('size', 0) < 100]  # Less than 100KB
        if len(small_repos) > 10:
            alerts.append({
                'id': f"optimize_consolidation_{now.strftime('%Y%m%d')}",
                'type': 'portfolio_optimization',
                'priority': 'low',
                'title': f'{len(small_repos)} Small Repositories Could Be Consolidated',
                'description': 'Multiple small repositories might benefit from consolidation',
                'details': {
                    'small_repositories': [repo['name'] for repo in small_repos[:10]],
                    'total_small_repos': len(small_repos),
                    'recommended_actions': [
                        'Review small repositories for consolidation opportunities',
                        'Combine related projects into monorepos',
                        'Archive experimental or outdated projects',
                        'Create umbrella repositories for related tools'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['optimization', 'consolidation']
            })
        
        # Visibility optimization
        popular_repos = [repo for repo in repositories if repo.get('stargazers_count', 0) > 5]
        visibility_percentage = len(popular_repos) / len(repositories) * 100 if repositories else 0
        
        if visibility_percentage < 15:  # Less than 15% have good visibility
            alerts.append({
                'id': f"optimize_visibility_{now.strftime('%Y%m%d')}",
                'type': 'portfolio_optimization',
                'priority': 'medium',
                'title': 'Low Portfolio Visibility Detected',
                'description': f'Only {visibility_percentage:.1f}% of repositories have significant stars',
                'details': {
                    'visibility_percentage': round(visibility_percentage, 1),
                    'popular_repositories': len(popular_repos),
                    'recommended_actions': [
                        'Improve repository descriptions and documentation',
                        'Add topics and tags for discoverability',
                        'Share projects on social media and forums',
                        'Write blog posts about your projects'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['visibility', 'promotion']
            })
        
        return alerts
    
    def _generate_activity_alerts(self, repositories: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Generate activity-related alerts"""
        alerts = []
        now = datetime.now(timezone.utc)
        
        # Low activity alert
        active_repos = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if (now - updated).days <= 30:  # Active in last 30 days
                        active_repos.append(repo['name'])
                except:
                    continue
        
        activity_percentage = len(active_repos) / len(repositories) * 100 if repositories else 0
        
        if activity_percentage < 25:  # Less than 25% active
            alerts.append({
                'id': f"activity_low_{now.strftime('%Y%m%d')}",
                'type': 'maintenance_needed',
                'priority': 'medium',
                'title': 'Low Portfolio Activity Detected',
                'description': f'Only {activity_percentage:.1f}% of repositories were active in the last month',
                'details': {
                    'activity_percentage': round(activity_percentage, 1),
                    'active_repositories': len(active_repos),
                    'total_repositories': len(repositories),
                    'recommended_actions': [
                        'Review and prioritize active projects',
                        'Archive inactive repositories',
                        'Set regular maintenance schedules',
                        'Focus development efforts on key projects'
                    ]
                },
                'created_at': now.isoformat(),
                'labels': ['activity', 'maintenance-schedule']
            })
        
        return alerts
    
    def _extract_common_issues(self, poor_health_repos: List[Dict[str, Any]]) -> List[str]:
        """Extract common issues from poor health repositories"""
        all_recommendations = []
        for repo in poor_health_repos:
            all_recommendations.extend(repo.get('recommendations', []))
        
        # Count frequency of recommendations
        from collections import Counter
        recommendation_counts = Counter(all_recommendations)
        
        # Return top 3 most common issues
        return [rec for rec, count in recommendation_counts.most_common(3)]
    
    def _generate_alert_summary(self, alerts: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate summary of alerts"""
        summary = {
            'total_alerts': len(alerts),
            'by_priority': defaultdict(int),
            'by_type': defaultdict(int),
            'critical_count': 0,
            'high_priority_count': 0
        }
        
        for alert in alerts:
            priority = alert.get('priority', 'unknown')
            alert_type = alert.get('type', 'unknown')
            
            summary['by_priority'][priority] += 1
            summary['by_type'][alert_type] += 1
            
            if priority == 'critical':
                summary['critical_count'] += 1
            elif priority == 'high':
                summary['high_priority_count'] += 1
        
        return dict(summary)

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Generate portfolio alerts')
    parser.add_argument('--repositories', required=True, help='Path to repositories JSON file')
    parser.add_argument('--health-scores', required=True, help='Path to health scores JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', default='analytics/alerts.json', help='Output alerts JSON file')
    
    args = parser.parse_args()
    
    try:
        # Load data
        with open(args.repositories, 'r') as file:
            repo_data = json.load(file)
        repositories = repo_data.get('repositories', [])
        
        with open(args.health_scores, 'r') as file:
            health_data = json.load(file)
        health_scores = health_data.get('health_scores', {})
        
        logger.info(f"Loaded {len(repositories)} repositories and {len(health_scores)} health scores")
        
        # Generate alerts
        generator = AlertGenerator(args.config)
        generator.generate_alerts(repositories, health_scores, args.output)
        
        logger.info("Alert generation completed successfully")
        
    except Exception as e:
        logger.error(f"Alert generation failed: {e}")
        raise

if __name__ == '__main__':
    main()
