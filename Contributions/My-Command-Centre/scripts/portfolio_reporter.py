#!/usr/bin/env python3
"""
Portfolio Reporter Script for My Command Centre
Generates comprehensive portfolio reports combining repository data and health scores
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
import statistics
from collections import Counter, defaultdict

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class PortfolioReporter:
    """Generate comprehensive portfolio reports"""
    
    def __init__(self, config_path: str):
        """Initialize the portfolio reporter"""
        self.config = self._load_config(config_path)
        self.analytics_config = self.config.get('analytics', {})
        
        logger.info("Initialized portfolio reporter")
    
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
    
    def generate_portfolio_report(self, repositories: List[Dict[str, Any]], 
                                health_scores: Dict[str, Any], output_path: str) -> None:
        """Generate comprehensive portfolio report"""
        logger.info("Generating comprehensive portfolio report")
        
        # Generate report sections
        report_data = {
            'metadata': self._generate_metadata(),
            'executive_summary': self._generate_executive_summary(repositories, health_scores),
            'repository_overview': self._generate_repository_overview(repositories),
            'health_analysis': self._generate_health_analysis(health_scores),
            'activity_analysis': self._generate_activity_analysis(repositories),
            'technology_analysis': self._generate_technology_analysis(repositories),
            'contribution_analysis': self._generate_contribution_analysis(repositories),
            'recommendations': self._generate_recommendations(repositories, health_scores),
            'trends_and_insights': self._generate_trends_insights(repositories, health_scores)
        }
        
        # Generate markdown report
        markdown_report = self._generate_markdown_report(report_data)
        
        # Save report
        output_dir = Path(output_path).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(output_path, 'w') as file:
            file.write(markdown_report)
        
        # Also save as JSON for programmatic access
        json_path = output_path.replace('.md', '.json')
        with open(json_path, 'w') as file:
            json.dump(report_data, file, indent=2, default=str)
        
        logger.info(f"Portfolio report saved to {output_path}")
    
    def _generate_metadata(self) -> Dict[str, Any]:
        """Generate report metadata"""
        return {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'report_version': '1.0.0',
            'generator': 'My Command Centre Portfolio Reporter',
            'report_type': 'comprehensive_portfolio_analysis'
        }
    
    def _generate_executive_summary(self, repositories: List[Dict[str, Any]], 
                                  health_scores: Dict[str, Any]) -> Dict[str, Any]:
        """Generate executive summary"""
        total_repos = len(repositories)
        public_repos = sum(1 for repo in repositories if not repo.get('is_private', False))
        private_repos = total_repos - public_repos
        
        # Calculate health distribution
        health_distribution = {'excellent': 0, 'good': 0, 'fair': 0, 'poor': 0, 'unknown': 0}
        total_score = 0
        scored_repos = 0
        
        for repo_health in health_scores.values():
            if isinstance(repo_health, dict) and 'health_grade' in repo_health:
                grade = repo_health['health_grade']
                health_distribution[grade] = health_distribution.get(grade, 0) + 1
                
                if 'overall_score' in repo_health:
                    total_score += repo_health['overall_score']
                    scored_repos += 1
        
        avg_health_score = total_score / scored_repos if scored_repos > 0 else 0
        
        # Activity metrics
        total_stars = sum(repo.get('stargazers_count', 0) for repo in repositories)
        total_forks = sum(repo.get('forks_count', 0) for repo in repositories)
        
        # Recent activity
        now = datetime.now(timezone.utc)
        active_last_month = sum(
            1 for repo in repositories 
            if repo.get('updated_at') and 
            (now - datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))).days <= 30
        )
        
        return {
            'total_repositories': total_repos,
            'repository_distribution': {
                'public': public_repos,
                'private': private_repos
            },
            'health_overview': {
                'average_score': round(avg_health_score, 2),
                'distribution': health_distribution,
                'health_percentage': round(
                    (health_distribution['excellent'] + health_distribution['good']) / total_repos * 100, 2
                ) if total_repos > 0 else 0
            },
            'engagement_metrics': {
                'total_stars': total_stars,
                'total_forks': total_forks,
                'avg_stars_per_repo': round(total_stars / total_repos, 2) if total_repos > 0 else 0
            },
            'activity_metrics': {
                'active_last_month': active_last_month,
                'activity_percentage': round(active_last_month / total_repos * 100, 2) if total_repos > 0 else 0
            }
        }
    
    def _generate_repository_overview(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate detailed repository overview"""
        # Repository categorization
        categories = {
            'personal_projects': [],
            'forked_repositories': [],
            'collaborative_projects': [],
            'archived_projects': []
        }
        
        for repo in repositories:
            if repo.get('is_archived', False):
                categories['archived_projects'].append(repo['name'])
            elif repo.get('is_fork', False):
                categories['forked_repositories'].append(repo['name'])
            elif repo.get('contributors_count', 0) > 1:
                categories['collaborative_projects'].append(repo['name'])
            else:
                categories['personal_projects'].append(repo['name'])
        
        # Size analysis
        sizes = [repo.get('size', 0) for repo in repositories if repo.get('size', 0) > 0]
        size_stats = {
            'total_size_kb': sum(sizes),
            'average_size_kb': round(statistics.mean(sizes), 2) if sizes else 0,
            'median_size_kb': round(statistics.median(sizes), 2) if sizes else 0,
            'largest_repo': max(repositories, key=lambda r: r.get('size', 0))['name'] if repositories else None,
            'smallest_repo': min(repositories, key=lambda r: r.get('size', 0) or float('inf'))['name'] if repositories else None
        }
        
        # Age analysis
        creation_dates = []
        for repo in repositories:
            if repo.get('created_at'):
                try:
                    created = datetime.fromisoformat(repo['created_at'].replace('Z', '+00:00'))
                    creation_dates.append(created)
                except:
                    continue
        
        age_stats = {}
        if creation_dates:
            oldest = min(creation_dates)
            newest = max(creation_dates)
            age_stats = {
                'oldest_repo_date': oldest.isoformat(),
                'newest_repo_date': newest.isoformat(),
                'portfolio_age_days': (datetime.now(timezone.utc) - oldest).days,
                'creation_rate_per_year': round(len(repositories) / max(1, (datetime.now(timezone.utc) - oldest).days / 365), 2)
            }
        
        return {
            'categorization': {k: len(v) for k, v in categories.items()},
            'detailed_categories': categories,
            'size_analysis': size_stats,
            'age_analysis': age_stats
        }
    
    def _generate_health_analysis(self, health_scores: Dict[str, Any]) -> Dict[str, Any]:
        """Generate detailed health analysis"""
        if not health_scores:
            return {'error': 'No health scores available'}
        
        # Extract scores
        scores = []
        component_scores = defaultdict(list)
        recommendations_count = Counter()
        
        for repo_name, health_data in health_scores.items():
            if isinstance(health_data, dict) and 'overall_score' in health_data:
                scores.append(health_data['overall_score'])
                
                # Component scores
                if 'component_scores' in health_data:
                    for component, score in health_data['component_scores'].items():
                        component_scores[component].append(score)
                
                # Recommendations
                if 'recommendations' in health_data:
                    for rec in health_data['recommendations']:
                        recommendations_count[rec] += 1
        
        # Calculate statistics
        health_stats = {}
        if scores:
            health_stats = {
                'average_score': round(statistics.mean(scores), 2),
                'median_score': round(statistics.median(scores), 2),
                'min_score': min(scores),
                'max_score': max(scores),
                'std_deviation': round(statistics.stdev(scores), 2) if len(scores) > 1 else 0
            }
        
        # Component analysis
        component_analysis = {}
        for component, comp_scores in component_scores.items():
            if comp_scores:
                component_analysis[component] = {
                    'average': round(statistics.mean(comp_scores), 2),
                    'min': min(comp_scores),
                    'max': max(comp_scores)
                }
        
        # Top recommendations
        top_recommendations = recommendations_count.most_common(10)
        
        return {
            'overall_statistics': health_stats,
            'component_analysis': component_analysis,
            'top_recommendations': [{'recommendation': rec, 'frequency': count} for rec, count in top_recommendations],
            'health_distribution': self._calculate_health_distribution(health_scores)
        }
    
    def _calculate_health_distribution(self, health_scores: Dict[str, Any]) -> Dict[str, int]:
        """Calculate health grade distribution"""
        distribution = {'excellent': 0, 'good': 0, 'fair': 0, 'poor': 0, 'unknown': 0}
        
        for health_data in health_scores.values():
            if isinstance(health_data, dict) and 'health_grade' in health_data:
                grade = health_data['health_grade']
                distribution[grade] = distribution.get(grade, 0) + 1
            else:
                distribution['unknown'] += 1
        
        return distribution
    
    def _generate_activity_analysis(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate activity analysis"""
        now = datetime.now(timezone.utc)
        
        # Activity periods
        activity_periods = {
            'last_7_days': 0,
            'last_30_days': 0,
            'last_90_days': 0,
            'last_year': 0,
            'older': 0
        }
        
        commit_activity = []
        update_activity = []
        
        for repo in repositories:
            # Update activity
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    days_since_update = (now - updated).days
                    update_activity.append(days_since_update)
                    
                    if days_since_update <= 7:
                        activity_periods['last_7_days'] += 1
                    elif days_since_update <= 30:
                        activity_periods['last_30_days'] += 1
                    elif days_since_update <= 90:
                        activity_periods['last_90_days'] += 1
                    elif days_since_update <= 365:
                        activity_periods['last_year'] += 1
                    else:
                        activity_periods['older'] += 1
                except:
                    activity_periods['older'] += 1
            
            # Commit activity
            if repo.get('recent_commits_count'):
                commit_activity.append(repo['recent_commits_count'])
        
        # Calculate activity statistics
        activity_stats = {}
        if update_activity:
            activity_stats = {
                'avg_days_since_update': round(statistics.mean(update_activity), 2),
                'median_days_since_update': round(statistics.median(update_activity), 2),
                'most_recently_updated': min(update_activity),
                'least_recently_updated': max(update_activity)
            }
        
        commit_stats = {}
        if commit_activity:
            commit_stats = {
                'avg_recent_commits': round(statistics.mean(commit_activity), 2),
                'total_recent_commits': sum(commit_activity),
                'max_commits_repo': max(commit_activity),
                'repos_with_commits': len([c for c in commit_activity if c > 0])
            }
        
        return {
            'activity_periods': activity_periods,
            'update_statistics': activity_stats,
            'commit_statistics': commit_stats,
            'activity_score': self._calculate_activity_score(activity_periods, len(repositories))
        }
    
    def _calculate_activity_score(self, activity_periods: Dict[str, int], total_repos: int) -> float:
        """Calculate overall activity score"""
        if total_repos == 0:
            return 0.0
        
        # Weight recent activity more heavily
        score = (
            activity_periods['last_7_days'] * 4 +
            activity_periods['last_30_days'] * 3 +
            activity_periods['last_90_days'] * 2 +
            activity_periods['last_year'] * 1
        ) / total_repos
        
        return round(min(score, 10.0), 2)  # Cap at 10
    
    def _generate_technology_analysis(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate technology stack analysis"""
        # Language analysis
        all_languages = []
        language_sizes = defaultdict(int)
        
        for repo in repositories:
            languages = repo.get('languages', {})
            for lang, size in languages.items():
                all_languages.append(lang)
                language_sizes[lang] += size
        
        language_counts = Counter(all_languages)
        top_languages = language_counts.most_common(15)
        
        # Primary language analysis
        primary_languages = [repo.get('primary_language') for repo in repositories if repo.get('primary_language')]
        primary_lang_counts = Counter(primary_languages)
        
        # Technology diversity
        unique_languages = len(language_counts)
        avg_languages_per_repo = round(len(all_languages) / len(repositories), 2) if repositories else 0
        
        return {
            'language_distribution': [{'language': lang, 'count': count} for lang, count in top_languages],
            'language_sizes': dict(sorted(language_sizes.items(), key=lambda x: x[1], reverse=True)[:10]),
            'primary_languages': [{'language': lang, 'count': count} for lang, count in primary_lang_counts.most_common(10)],
            'diversity_metrics': {
                'unique_languages': unique_languages,
                'avg_languages_per_repo': avg_languages_per_repo,
                'most_diverse_repo': self._find_most_diverse_repo(repositories)
            }
        }
    
    def _find_most_diverse_repo(self, repositories: List[Dict[str, Any]]) -> Optional[str]:
        """Find repository with most language diversity"""
        max_languages = 0
        most_diverse = None
        
        for repo in repositories:
            lang_count = len(repo.get('languages', {}))
            if lang_count > max_languages:
                max_languages = lang_count
                most_diverse = repo['name']
        
        return most_diverse
    
    def _generate_contribution_analysis(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Generate contribution pattern analysis"""
        # Open source contributions (forks)
        open_source_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        
        # Collaborative projects (multiple contributors)
        collaborative_repos = [repo for repo in repositories if repo.get('contributors_count', 0) > 1]
        
        # Popular projects (stars/forks)
        popular_repos = [repo for repo in repositories if repo.get('stargazers_count', 0) > 0 or repo.get('forks_count', 0) > 0]
        
        # Contribution metrics
        total_contributions = len(repositories)
        open_source_percentage = round(len(open_source_repos) / total_contributions * 100, 2) if total_contributions > 0 else 0
        collaborative_percentage = round(len(collaborative_repos) / total_contributions * 100, 2) if total_contributions > 0 else 0
        
        return {
            'contribution_types': {
                'open_source_contributions': len(open_source_repos),
                'collaborative_projects': len(collaborative_repos),
                'personal_projects': total_contributions - len(open_source_repos) - len(collaborative_repos),
                'popular_projects': len(popular_repos)
            },
            'contribution_percentages': {
                'open_source_percentage': open_source_percentage,
                'collaborative_percentage': collaborative_percentage,
                'personal_percentage': round(100 - open_source_percentage - collaborative_percentage, 2)
            },
            'top_contributed_repos': self._get_top_contributed_repos(repositories),
            'contribution_impact': self._calculate_contribution_impact(repositories)
        }
    
    def _get_top_contributed_repos(self, repositories: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Get top repositories by contribution impact"""
        scored_repos = []
        
        for repo in repositories:
            # Calculate impact score
            impact_score = (
                repo.get('stargazers_count', 0) * 3 +
                repo.get('forks_count', 0) * 5 +
                repo.get('contributors_count', 0) * 2 +
                (10 if repo.get('is_fork', False) else 0)  # Bonus for open source contributions
            )
            
            scored_repos.append({
                'name': repo['name'],
                'impact_score': impact_score,
                'stars': repo.get('stargazers_count', 0),
                'forks': repo.get('forks_count', 0),
                'contributors': repo.get('contributors_count', 0),
                'is_fork': repo.get('is_fork', False)
            })
        
        return sorted(scored_repos, key=lambda x: x['impact_score'], reverse=True)[:10]
    
    def _calculate_contribution_impact(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Calculate overall contribution impact"""
        total_stars = sum(repo.get('stargazers_count', 0) for repo in repositories)
        total_forks = sum(repo.get('forks_count', 0) for repo in repositories)
        total_contributors = sum(repo.get('contributors_count', 0) for repo in repositories)
        
        # Calculate reach (people who have interacted with your repositories)
        estimated_reach = total_stars + total_forks + total_contributors
        
        return {
            'total_stars_earned': total_stars,
            'total_forks_created': total_forks,
            'total_collaborators': total_contributors,
            'estimated_reach': estimated_reach,
            'impact_score': round((total_stars * 1 + total_forks * 2 + total_contributors * 3) / len(repositories), 2) if repositories else 0
        }
    
    def _generate_recommendations(self, repositories: List[Dict[str, Any]], 
                                health_scores: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate actionable recommendations"""
        recommendations = []
        
        # Health-based recommendations
        poor_health_repos = [
            name for name, health in health_scores.items()
            if isinstance(health, dict) and health.get('health_grade') == 'poor'
        ]
        
        if poor_health_repos:
            recommendations.append({
                'category': 'Repository Health',
                'priority': 'High',
                'title': 'Improve Poor Health Repositories',
                'description': f'Focus on improving {len(poor_health_repos)} repositories with poor health scores',
                'action_items': [
                    'Update documentation and README files',
                    'Add meaningful descriptions and topics',
                    'Address open issues and technical debt',
                    'Increase commit frequency'
                ],
                'affected_repos': poor_health_repos[:5]  # Show top 5
            })
        
        # Activity-based recommendations
        now = datetime.now(timezone.utc)
        stale_repos = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if (now - updated).days > 180:
                        stale_repos.append(repo['name'])
                except:
                    continue
        
        if stale_repos:
            recommendations.append({
                'category': 'Activity',
                'priority': 'Medium',
                'title': 'Update Stale Repositories',
                'description': f'Review and update {len(stale_repos)} repositories not updated in 6+ months',
                'action_items': [
                    'Review repository relevance',
                    'Archive outdated projects',
                    'Update dependencies',
                    'Refresh documentation'
                ],
                'affected_repos': stale_repos[:5]
            })
        
        # Documentation recommendations
        repos_without_description = [
            repo['name'] for repo in repositories 
            if not repo.get('description') or len(repo.get('description', '')) < 10
        ]
        
        if repos_without_description:
            recommendations.append({
                'category': 'Documentation',
                'priority': 'Medium',
                'title': 'Add Repository Descriptions',
                'description': f'Add meaningful descriptions to {len(repos_without_description)} repositories',
                'action_items': [
                    'Write clear, concise descriptions',
                    'Add relevant topics/tags',
                    'Include purpose and key features',
                    'Update README files'
                ],
                'affected_repos': repos_without_description[:5]
            })
        
        return recommendations
    
    def _generate_trends_insights(self, repositories: List[Dict[str, Any]], 
                                health_scores: Dict[str, Any]) -> Dict[str, Any]:
        """Generate trends and insights"""
        # This would typically compare with historical data
        # For now, we'll generate insights based on current state
        
        insights = []
        
        # Language trends
        language_counts = Counter()
        for repo in repositories:
            if repo.get('primary_language'):
                language_counts[repo['primary_language']] += 1
        
        if language_counts:
            top_language = language_counts.most_common(1)[0]
            insights.append(f"Your primary technology focus is {top_language[0]} with {top_language[1]} repositories")
        
        # Activity insights
        active_repos = sum(1 for repo in repositories if self._is_recently_active(repo))
        activity_percentage = round(active_repos / len(repositories) * 100, 2) if repositories else 0
        
        if activity_percentage > 70:
            insights.append(f"High activity level: {activity_percentage}% of repositories are actively maintained")
        elif activity_percentage < 30:
            insights.append(f"Low activity level: Only {activity_percentage}% of repositories are actively maintained")
        
        # Health insights
        if health_scores:
            avg_score = statistics.mean([
                h.get('overall_score', 0) for h in health_scores.values() 
                if isinstance(h, dict) and 'overall_score' in h
            ])
            
            if avg_score > 80:
                insights.append(f"Excellent portfolio health with average score of {avg_score:.1f}")
            elif avg_score < 50:
                insights.append(f"Portfolio health needs attention with average score of {avg_score:.1f}")
        
        return {
            'key_insights': insights,
            'portfolio_maturity': self._assess_portfolio_maturity(repositories),
            'growth_opportunities': self._identify_growth_opportunities(repositories, health_scores)
        }
    
    def _is_recently_active(self, repo: Dict[str, Any]) -> bool:
        """Check if repository is recently active (last 90 days)"""
        if not repo.get('updated_at'):
            return False
        
        try:
            updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
            return (datetime.now(timezone.utc) - updated).days <= 90
        except:
            return False
    
    def _assess_portfolio_maturity(self, repositories: List[Dict[str, Any]]) -> str:
        """Assess overall portfolio maturity"""
        if len(repositories) < 5:
            return "Emerging"
        elif len(repositories) < 20:
            return "Developing"
        elif len(repositories) < 50:
            return "Established"
        else:
            return "Mature"
    
    def _identify_growth_opportunities(self, repositories: List[Dict[str, Any]], 
                                     health_scores: Dict[str, Any]) -> List[str]:
        """Identify growth opportunities"""
        opportunities = []
        
        # Check for underutilized repositories
        popular_repos = [repo for repo in repositories if repo.get('stargazers_count', 0) > 5]
        if len(popular_repos) < len(repositories) * 0.2:
            opportunities.append("Increase visibility and promotion of repositories")
        
        # Check for collaboration opportunities
        solo_repos = [repo for repo in repositories if repo.get('contributors_count', 0) <= 1]
        if len(solo_repos) > len(repositories) * 0.8:
            opportunities.append("Seek collaboration opportunities and community engagement")
        
        # Check for open source contributions
        fork_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        if len(fork_repos) < len(repositories) * 0.3:
            opportunities.append("Increase open source contributions and community involvement")
        
        return opportunities
    
    def _generate_markdown_report(self, report_data: Dict[str, Any]) -> str:
        """Generate markdown formatted report"""
        metadata = report_data['metadata']
        summary = report_data['executive_summary']
        
        markdown = f"""# Portfolio Analysis Report

*Generated on {metadata['generated_at']} by {metadata['generator']}*

---

## Executive Summary

### Portfolio Overview
- **Total Repositories:** {summary['total_repositories']}
- **Public Repositories:** {summary['repository_distribution']['public']}
- **Private Repositories:** {summary['repository_distribution']['private']}

### Health Metrics
- **Average Health Score:** {summary['health_overview']['average_score']}/100
- **Healthy Repositories:** {summary['health_overview']['health_percentage']}%
- **Health Distribution:**
  - Excellent: {summary['health_overview']['distribution']['excellent']}
  - Good: {summary['health_overview']['distribution']['good']}
  - Fair: {summary['health_overview']['distribution']['fair']}
  - Poor: {summary['health_overview']['distribution']['poor']}

### Engagement Metrics
- **Total Stars:** {summary['engagement_metrics']['total_stars']}
- **Total Forks:** {summary['engagement_metrics']['total_forks']}
- **Average Stars per Repository:** {summary['engagement_metrics']['avg_stars_per_repo']}

### Activity Metrics
- **Active Last Month:** {summary['activity_metrics']['active_last_month']} repositories ({summary['activity_metrics']['activity_percentage']}%)

---

## Detailed Analysis

### Repository Health Analysis
"""
        
        # Add health analysis
        health_analysis = report_data['health_analysis']
        if 'overall_statistics' in health_analysis:
            stats = health_analysis['overall_statistics']
            markdown += f"""
**Health Statistics:**
- Average Score: {stats.get('average_score', 'N/A')}
- Median Score: {stats.get('median_score', 'N/A')}
- Score Range: {stats.get('min_score', 'N/A')} - {stats.get('max_score', 'N/A')}
- Standard Deviation: {stats.get('std_deviation', 'N/A')}
"""
        
        # Add technology analysis
        tech_analysis = report_data['technology_analysis']
        markdown += f"""
### Technology Stack Analysis

**Top Programming Languages:**
"""
        for lang_data in tech_analysis['language_distribution'][:5]:
            markdown += f"- {lang_data['language']}: {lang_data['count']} repositories\n"
        
        markdown += f"""
**Technology Diversity:**
- Unique Languages: {tech_analysis['diversity_metrics']['unique_languages']}
- Average Languages per Repository: {tech_analysis['diversity_metrics']['avg_languages_per_repo']}
"""
        
        # Add recommendations
        recommendations = report_data['recommendations']
        if recommendations:
            markdown += "\n### Recommendations\n\n"
            for i, rec in enumerate(recommendations, 1):
                markdown += f"""
#### {i}. {rec['title']} ({rec['priority']} Priority)

{rec['description']}

**Action Items:**
"""
                for action in rec['action_items']:
                    markdown += f"- {action}\n"
                
                if rec.get('affected_repos'):
                    markdown += f"\n**Affected Repositories:** {', '.join(rec['affected_repos'])}\n"
        
        # Add insights
        insights = report_data['trends_and_insights']
        if insights.get('key_insights'):
            markdown += "\n### Key Insights\n\n"
            for insight in insights['key_insights']:
                markdown += f"- {insight}\n"
        
        markdown += f"""
---

## Portfolio Maturity Assessment

**Maturity Level:** {insights.get('portfolio_maturity', 'Unknown')}

### Growth Opportunities
"""
        
        for opportunity in insights.get('growth_opportunities', []):
            markdown += f"- {opportunity}\n"
        
        markdown += f"""
---

*This report is automatically generated by My Command Centre. For detailed data and interactive analysis, see the dashboard at your GitHub Pages site.*

**Next Report:** Scheduled for {(datetime.now(timezone.utc) + timedelta(days=1)).strftime('%Y-%m-%d')}
"""
        
        return markdown

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Generate comprehensive portfolio report')
    parser.add_argument('--repositories', required=True, help='Path to repositories JSON file')
    parser.add_argument('--health-scores', required=True, help='Path to health scores JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output markdown file path')
    
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
        
        # Generate report
        reporter = PortfolioReporter(args.config)
        reporter.generate_portfolio_report(repositories, health_scores, args.output)
        
        logger.info("Portfolio report generation completed successfully")
        
    except Exception as e:
        logger.error(f"Portfolio report generation failed: {e}")
        raise

if __name__ == '__main__':
    main()
