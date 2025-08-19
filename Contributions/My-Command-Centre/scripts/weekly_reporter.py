#!/usr/bin/env python3
"""
Weekly Reporter Script for My Command Centre
Generates comprehensive weekly portfolio reports and insights
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class WeeklyReporter:
    """Generate comprehensive weekly portfolio reports"""
    
    def __init__(self, config_path: str):
        """Initialize the weekly reporter"""
        self.config = self._load_config(config_path)
        
        logger.info("Initialized weekly reporter")
    
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
    
    def generate_weekly_report(self, output_path: str, force_regenerate: bool = False) -> None:
        """Generate comprehensive weekly report"""
        logger.info("Generating weekly portfolio report")
        
        # Load latest analytics data
        analytics_data = self._load_analytics_data()
        
        # Generate report sections
        report = {
            'metadata': self._generate_metadata(),
            'executive_summary': self._generate_executive_summary(analytics_data),
            'portfolio_health': self._analyze_portfolio_health(analytics_data),
            'contribution_activity': self._analyze_contribution_activity(analytics_data),
            'sri_lankan_projects': self._analyze_sri_lankan_projects(analytics_data),
            'healthcare_focus': self._analyze_healthcare_focus(analytics_data),
            'strategic_insights': self._generate_strategic_insights(analytics_data),
            'weekly_achievements': self._identify_weekly_achievements(analytics_data),
            'next_week_priorities': self._generate_next_week_priorities(analytics_data),
            'action_items': self._generate_action_items(analytics_data)
        }
        
        # Generate markdown report
        markdown_report = self._generate_markdown_report(report)
        
        # Save report
        output_dir = Path(output_path).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(output_path, 'w') as file:
            file.write(markdown_report)
        
        # Also save as JSON
        json_path = output_path.replace('.md', '.json')
        with open(json_path, 'w') as file:
            json.dump(report, file, indent=2, default=str)
        
        logger.info(f"Weekly report saved to {output_path}")
    
    def _load_analytics_data(self) -> Dict[str, Any]:
        """Load all relevant analytics data"""
        analytics_data = {}
        
        # Define analytics files to load
        analytics_files = {
            'repositories': 'analytics/repositories.json',
            'health_scores': 'analytics/health_scores.json',
            'portfolio_overview': 'analytics/portfolio_overview.json',
            'contribution_patterns': 'analytics/contribution_patterns.json',
            'activity_heatmap': 'analytics/activity_heatmap.json',
            'alerts': 'analytics/alerts.json'
        }
        
        for key, file_path in analytics_files.items():
            if Path(file_path).exists():
                try:
                    with open(file_path, 'r') as file:
                        data = json.load(file)
                        analytics_data[key] = data
                except Exception as e:
                    logger.warning(f"Could not load {file_path}: {e}")
                    analytics_data[key] = {}
            else:
                analytics_data[key] = {}
        
        return analytics_data
    
    def _generate_metadata(self) -> Dict[str, Any]:
        """Generate report metadata"""
        now = datetime.now(timezone.utc)
        week_start = now - timedelta(days=now.weekday())
        week_end = week_start + timedelta(days=6)
        
        return {
            'generated_at': now.isoformat(),
            'report_type': 'weekly_portfolio_report',
            'week_start': week_start.strftime('%Y-%m-%d'),
            'week_end': week_end.strftime('%Y-%m-%d'),
            'week_number': now.strftime('%Y-W%U'),
            'generator': 'My Command Centre Weekly Reporter'
        }
    
    def _generate_executive_summary(self, analytics_data: Dict[str, Any]) -> Dict[str, Any]:
        """Generate executive summary"""
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        portfolio_overview = analytics_data.get('portfolio_overview', {})
        alerts = analytics_data.get('alerts', {}).get('alerts', [])
        
        # Calculate key metrics
        total_repos = len(repositories)
        healthy_repos = len([name for name, health in health_scores.items() 
                           if isinstance(health, dict) and health.get('health_grade') in ['excellent', 'good']])
        
        active_repos = len([repo for repo in repositories if self._is_recently_active(repo)])
        
        # Count Sri Lankan and healthcare projects
        sri_lankan_count = self._count_sri_lankan_projects(repositories)
        healthcare_count = self._count_healthcare_projects(repositories)
        
        return {
            'total_repositories': total_repos,
            'healthy_repositories': healthy_repos,
            'health_percentage': round(healthy_repos / total_repos * 100, 1) if total_repos > 0 else 0,
            'active_repositories': active_repos,
            'activity_percentage': round(active_repos / total_repos * 100, 1) if total_repos > 0 else 0,
            'sri_lankan_projects': sri_lankan_count,
            'healthcare_projects': healthcare_count,
            'total_alerts': len(alerts),
            'high_priority_alerts': len([a for a in alerts if a.get('priority') == 'high']),
            'key_focus': self._determine_key_focus(analytics_data)
        }
    
    def _analyze_portfolio_health(self, analytics_data: Dict[str, Any]) -> Dict[str, Any]:
        """Analyze portfolio health trends"""
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        
        # Calculate health distribution
        health_distribution = {'excellent': 0, 'good': 0, 'fair': 0, 'poor': 0}
        total_score = 0
        scored_repos = 0
        
        for health_data in health_scores.values():
            if isinstance(health_data, dict):
                grade = health_data.get('health_grade', 'unknown')
                if grade in health_distribution:
                    health_distribution[grade] += 1
                
                score = health_data.get('overall_score')
                if score is not None:
                    total_score += score
                    scored_repos += 1
        
        avg_score = total_score / scored_repos if scored_repos > 0 else 0
        
        # Identify repositories needing attention
        needs_attention = []
        for repo_name, health_data in health_scores.items():
            if isinstance(health_data, dict) and health_data.get('health_grade') == 'poor':
                needs_attention.append({
                    'name': repo_name.split('/')[-1],
                    'score': health_data.get('overall_score', 0),
                    'issues': health_data.get('recommendations', [])[:3]
                })
        
        return {
            'average_score': round(avg_score, 1),
            'health_distribution': health_distribution,
            'repositories_needing_attention': needs_attention[:5],
            'health_trend': self._calculate_health_trend(analytics_data),
            'improvement_opportunities': self._identify_improvement_opportunities(health_scores)
        }
    
    def _analyze_contribution_activity(self, analytics_data: Dict[str, Any]) -> Dict[str, Any]:
        """Analyze contribution activity patterns"""
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        
        # Analyze open source contributions
        open_source = contribution_patterns.get('open_source_contributions', {})
        forked_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        active_forks = [repo for repo in forked_repos if self._is_recently_active(repo)]
        
        # Analyze contribution types
        contribution_types = {
            'open_source_forks': len(forked_repos),
            'active_forks': len(active_forks),
            'personal_projects': len(repositories) - len(forked_repos),
            'collaborative_projects': len([repo for repo in repositories if repo.get('contributors_count', 0) > 1])
        }
        
        # Weekly activity summary
        now = datetime.now(timezone.utc)
        week_ago = now - timedelta(days=7)
        
        weekly_activity = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if updated >= week_ago:
                        weekly_activity.append({
                            'name': repo['name'],
                            'last_update': repo['updated_at'],
                            'is_fork': repo.get('is_fork', False),
                            'commits': repo.get('recent_commits_count', 0)
                        })
                except:
                    continue
        
        return {
            'contribution_types': contribution_types,
            'weekly_activity': weekly_activity,
            'activity_rate': open_source.get('activity_percentage', 0),
            'target_projects': open_source.get('target_project_contributions', {}),
            'contribution_focus': self._analyze_contribution_focus(repositories)
        }
    
    def _analyze_sri_lankan_projects(self, analytics_data: Dict[str, Any]) -> Dict[str, Any]:
        """Analyze Sri Lankan project contributions"""
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        
        # Get Sri Lankan project data
        sri_lankan_data = contribution_patterns.get('sri_lankan_projects', {})
        domain_breakdown = sri_lankan_data.get('domain_breakdown', {})
        
        # Identify active Sri Lankan projects
        sri_lankan_repos = []
        sri_lankan_keywords = [
            'sri lanka', 'sinhala', 'tamil', 'colombo', 'hmis', 'eclk', 
            'icta', 'lsf', 'sahana', 'medicines', 'election', 'health'
        ]
        
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in sri_lankan_keywords):
                sri_lankan_repos.append({
                    'name': repo['name'],
                    'description': repo.get('description', ''),
                    'is_fork': repo.get('is_fork', False),
                    'last_activity': repo.get('updated_at'),
                    'stars': repo.get('stargazers_count', 0),
                    'domain': self._classify_project_domain(repo)
                })
        
        # Analyze contribution opportunities
        opportunities = []
        for domain, data in domain_breakdown.items():
            if data.get('repository_count', 0) > 0:
                opportunities.extend(data.get('contribution_opportunities', []))
        
        return {
            'total_projects': len(sri_lankan_repos),
            'active_projects': len([repo for repo in sri_lankan_repos if self._is_recently_active_dict(repo)]),
            'domain_breakdown': domain_breakdown,
            'project_list': sri_lankan_repos,
            'contribution_opportunities': opportunities[:5],
            'social_impact_score': sri_lankan_data.get('social_impact_score', 0),
            'strategic_alignment': sri_lankan_data.get('strategic_focus_alignment', {})
        }
    
    def _analyze_healthcare_focus(self, analytics_data: Dict[str, Any]) -> Dict[str, Any]:
        """Analyze healthcare technology contributions"""
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        
        # Get healthcare contribution data
        healthcare_data = contribution_patterns.get('healthcare_contributions', {})
        hmis_data = healthcare_data.get('hmis_contributions', {})
        
        # Identify healthcare repositories
        healthcare_repos = []
        healthcare_keywords = ['health', 'medical', 'hospital', 'patient', 'clinical', 'pharmacy', 'hmis']
        
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in healthcare_keywords):
                healthcare_repos.append({
                    'name': repo['name'],
                    'description': repo.get('description', ''),
                    'is_fork': repo.get('is_fork', False),
                    'last_activity': repo.get('updated_at'),
                    'focus_area': self._identify_healthcare_focus_area(repo)
                })
        
        # HMIS specific analysis
        hmis_repos = [repo for repo in healthcare_repos if 'hmis' in repo['name'].lower()]
        
        return {
            'total_healthcare_repos': len(healthcare_repos),
            'hmis_repositories': len(hmis_repos),
            'healthcare_projects': healthcare_repos,
            'hmis_focus': hmis_data.get('contribution_focus', {}),
            'expertise_level': healthcare_data.get('healthcare_domain_expertise', {}).get('expertise_level', 'Developing'),
            'patient_impact': healthcare_data.get('patient_impact_potential', 'Medium'),
            'expansion_opportunities': {
                'openmrs': 'High potential for Sinhala/Tamil localization',
                'dhis2': 'Sri Lankan Ministry of Health integration opportunities',
                'mobile_health': 'Field health worker applications'
            }
        }
    
    def _generate_strategic_insights(self, analytics_data: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate strategic insights for the week"""
        insights = []
        
        # Portfolio health insight
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        poor_health_count = len([h for h in health_scores.values() 
                               if isinstance(h, dict) and h.get('health_grade') == 'poor'])
        
        if poor_health_count > 0:
            insights.append({
                'category': 'Portfolio Health',
                'insight': f'{poor_health_count} repositories need immediate attention',
                'recommendation': 'Focus on improving documentation and addressing technical debt',
                'priority': 'High' if poor_health_count > 3 else 'Medium'
            })
        
        # Contribution activity insight
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        open_source = contribution_patterns.get('open_source_contributions', {})
        activity_rate = open_source.get('activity_percentage', 0)
        
        if activity_rate < 50:
            insights.append({
                'category': 'Open Source Engagement',
                'insight': f'Only {activity_rate:.1f}% of forked repositories show recent activity',
                'recommendation': 'Focus on 2-3 high-impact projects for consistent contributions',
                'priority': 'Medium'
            })
        
        # Sri Lankan project insight
        sri_lankan_data = contribution_patterns.get('sri_lankan_projects', {})
        healthcare_domain = sri_lankan_data.get('domain_breakdown', {}).get('healthcare', {})
        
        if healthcare_domain.get('repository_count', 0) > 0:
            insights.append({
                'category': 'Social Impact',
                'insight': 'Strong healthcare technology focus with direct patient impact potential',
                'recommendation': 'Leverage expertise for OpenMRS and DHIS2 contributions',
                'priority': 'High'
            })
        
        return insights
    
    def _identify_weekly_achievements(self, analytics_data: Dict[str, Any]) -> List[str]:
        """Identify key achievements for the week"""
        achievements = []
        
        # Check for recent repository activity
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        now = datetime.now(timezone.utc)
        week_ago = now - timedelta(days=7)
        
        recent_activity = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if updated >= week_ago:
                        recent_activity.append(repo['name'])
                except:
                    continue
        
        if recent_activity:
            achievements.append(f"Active development in {len(recent_activity)} repositories this week")
        
        # Check for health improvements
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        good_health_count = len([h for h in health_scores.values() 
                               if isinstance(h, dict) and h.get('health_grade') in ['excellent', 'good']])
        
        if good_health_count > 0:
            achievements.append(f"Maintained good health in {good_health_count} repositories")
        
        # Check for Sri Lankan project contributions
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        sri_lankan_data = contribution_patterns.get('sri_lankan_projects', {})
        
        if sri_lankan_data.get('total_sri_lankan_projects', 0) > 0:
            achievements.append("Continued focus on Sri Lankan social good projects")
        
        return achievements if achievements else ["Maintained portfolio stability and monitoring"]
    
    def _generate_next_week_priorities(self, analytics_data: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate priorities for next week"""
        priorities = []
        
        # Health improvement priorities
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        poor_health_repos = [name for name, health in health_scores.items() 
                           if isinstance(health, dict) and health.get('health_grade') == 'poor']
        
        if poor_health_repos:
            priorities.append({
                'category': 'Portfolio Health',
                'priority': 'High',
                'task': f'Improve health of {len(poor_health_repos)} repositories',
                'details': f'Focus on: {", ".join([r.split("/")[-1] for r in poor_health_repos[:3]])}'
            })
        
        # Open source contribution priorities
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        target_projects = contribution_patterns.get('open_source_contributions', {}).get('target_project_contributions', {})
        
        for project, data in target_projects.items():
            if data.get('activity_score', 0) < 50:
                priorities.append({
                    'category': 'Open Source Contributions',
                    'priority': 'Medium',
                    'task': f'Increase activity in {project}',
                    'details': 'Sync with upstream and identify contribution opportunities'
                })
        
        # Sri Lankan project priorities
        sri_lankan_data = contribution_patterns.get('sri_lankan_projects', {})
        domain_breakdown = sri_lankan_data.get('domain_breakdown', {})
        
        if 'healthcare' in domain_breakdown:
            priorities.append({
                'category': 'Social Impact',
                'priority': 'High',
                'task': 'Advance healthcare technology contributions',
                'details': 'Continue HMIS work and explore OpenMRS opportunities'
            })
        
        return priorities
    
    def _generate_action_items(self, analytics_data: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate specific action items"""
        action_items = []
        
        # From alerts
        alerts = analytics_data.get('alerts', {}).get('alerts', [])
        for alert in alerts[:3]:  # Top 3 alerts
            if alert.get('priority') in ['high', 'critical']:
                action_items.append({
                    'source': 'Alert System',
                    'action': alert.get('title', 'Address alert'),
                    'description': alert.get('description', ''),
                    'priority': alert.get('priority', 'medium'),
                    'due_date': 'This week'
                })
        
        # Strategic recommendations
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        recommendations = contribution_patterns.get('strategic_recommendations', [])
        
        for rec in recommendations[:2]:  # Top 2 recommendations
            action_items.append({
                'source': 'Strategic Analysis',
                'action': rec.get('title', 'Strategic action'),
                'description': rec.get('description', ''),
                'priority': rec.get('priority', 'medium').lower(),
                'due_date': 'Next 2 weeks'
            })
        
        return action_items
    
    def _generate_markdown_report(self, report: Dict[str, Any]) -> str:
        """Generate markdown formatted report"""
        metadata = report['metadata']
        summary = report['executive_summary']
        
        markdown = f"""# Weekly Portfolio Report - {metadata['week_number']}

**Report Period:** {metadata['week_start']} to {metadata['week_end']}  
**Generated:** {metadata['generated_at']}  

---

## 📊 Executive Summary

### Portfolio Overview
- **Total Repositories:** {summary['total_repositories']}
- **Healthy Repositories:** {summary['healthy_repositories']} ({summary['health_percentage']}%)
- **Active This Week:** {summary['active_repositories']} ({summary['activity_percentage']}%)
- **Sri Lankan Projects:** {summary['sri_lankan_projects']}
- **Healthcare Projects:** {summary['healthcare_projects']}

### Alert Status
- **Total Alerts:** {summary['total_alerts']}
- **High Priority:** {summary['high_priority_alerts']}

### Key Focus This Week
{summary['key_focus']}

---

## 🏥 Portfolio Health Analysis

"""
        
        health = report['portfolio_health']
        markdown += f"""**Average Health Score:** {health['average_score']}/100

**Health Distribution:**
- Excellent: {health['health_distribution']['excellent']} repositories
- Good: {health['health_distribution']['good']} repositories  
- Fair: {health['health_distribution']['fair']} repositories
- Poor: {health['health_distribution']['poor']} repositories

"""
        
        if health['repositories_needing_attention']:
            markdown += "**Repositories Needing Attention:**\n"
            for repo in health['repositories_needing_attention']:
                markdown += f"- **{repo['name']}** (Score: {repo['score']}) - {', '.join(repo['issues'][:2])}\n"
        
        # Contribution Activity
        activity = report['contribution_activity']
        markdown += f"""
---

## 🤝 Contribution Activity

**This Week's Activity:**
- **Repositories Updated:** {len(activity['weekly_activity'])}
- **Open Source Forks:** {activity['contribution_types']['open_source_forks']}
- **Active Forks:** {activity['contribution_types']['active_forks']}
- **Fork Activity Rate:** {activity['activity_rate']:.1f}%

"""
        
        if activity['weekly_activity']:
            markdown += "**Recent Repository Activity:**\n"
            for repo in activity['weekly_activity'][:5]:
                fork_indicator = " (Fork)" if repo['is_fork'] else ""
                markdown += f"- **{repo['name']}**{fork_indicator} - {repo['last_update']}\n"
        
        # Sri Lankan Projects
        sri_lankan = report['sri_lankan_projects']
        markdown += f"""
---

## 🇱🇰 Sri Lankan Social Good Projects

**Project Overview:**
- **Total Projects:** {sri_lankan['total_projects']}
- **Active Projects:** {sri_lankan['active_projects']}
- **Social Impact Score:** {sri_lankan['social_impact_score']:.1f}

**Domain Breakdown:**
"""
        
        for domain, data in sri_lankan['domain_breakdown'].items():
            if data.get('repository_count', 0) > 0:
                markdown += f"- **{domain.title()}:** {data['repository_count']} repositories\n"
        
        if sri_lankan['contribution_opportunities']:
            markdown += "\n**Immediate Opportunities:**\n"
            for opp in sri_lankan['contribution_opportunities']:
                markdown += f"- {opp}\n"
        
        # Healthcare Focus
        healthcare = report['healthcare_focus']
        markdown += f"""
---

## 🏥 Healthcare Technology Focus

**Healthcare Portfolio:**
- **Total Healthcare Repos:** {healthcare['total_healthcare_repos']}
- **HMIS Repositories:** {healthcare['hmis_repositories']}
- **Expertise Level:** {healthcare['expertise_level']}
- **Patient Impact Potential:** {healthcare['patient_impact']}

**Expansion Opportunities:**
"""
        
        for area, description in healthcare['expansion_opportunities'].items():
            markdown += f"- **{area.replace('_', ' ').title()}:** {description}\n"
        
        # Strategic Insights
        insights = report['strategic_insights']
        if insights:
            markdown += "\n---\n\n## 💡 Strategic Insights\n\n"
            for insight in insights:
                markdown += f"### {insight['category']} ({insight['priority']} Priority)\n\n"
                markdown += f"**Insight:** {insight['insight']}\n\n"
                markdown += f"**Recommendation:** {insight['recommendation']}\n\n"
        
        # Weekly Achievements
        achievements = report['weekly_achievements']
        markdown += "---\n\n## 🎉 This Week's Achievements\n\n"
        for achievement in achievements:
            markdown += f"- {achievement}\n"
        
        # Next Week Priorities
        priorities = report['next_week_priorities']
        if priorities:
            markdown += "\n---\n\n## 📋 Next Week's Priorities\n\n"
            for priority in priorities:
                markdown += f"### {priority['category']} - {priority['priority']} Priority\n\n"
                markdown += f"**Task:** {priority['task']}\n\n"
                markdown += f"**Details:** {priority['details']}\n\n"
        
        # Action Items
        action_items = report['action_items']
        if action_items:
            markdown += "---\n\n## ✅ Action Items\n\n"
            for item in action_items:
                markdown += f"- [ ] **{item['action']}** ({item['priority'].title()} Priority)\n"
                markdown += f"  - {item['description']}\n"
                markdown += f"  - Due: {item['due_date']}\n\n"
        
        markdown += f"""---

## 📈 Portfolio Dashboard

Access your live dashboard for detailed analytics and interactive visualizations:
[Portfolio Dashboard](../../)

**Next Report:** {(datetime.now(timezone.utc) + timedelta(days=7)).strftime('%Y-%m-%d')}

---

*This report is automatically generated by My Command Centre. For detailed data and strategic planning, see the full analytics dashboard.*
"""
        
        return markdown
    
    # Helper methods
    def _is_recently_active(self, repo: Dict[str, Any]) -> bool:
        """Check if repository is recently active (last 30 days)"""
        if not repo.get('updated_at'):
            return False
        
        try:
            updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
            return (datetime.now(timezone.utc) - updated).days <= 30
        except:
            return False
    
    def _is_recently_active_dict(self, repo_dict: Dict[str, Any]) -> bool:
        """Check if repository dict is recently active"""
        if not repo_dict.get('last_activity'):
            return False
        
        try:
            updated = datetime.fromisoformat(repo_dict['last_activity'].replace('Z', '+00:00'))
            return (datetime.now(timezone.utc) - updated).days <= 30
        except:
            return False
    
    def _count_sri_lankan_projects(self, repositories: List[Dict[str, Any]]) -> int:
        """Count Sri Lankan related projects"""
        keywords = ['sri lanka', 'sinhala', 'tamil', 'hmis', 'eclk', 'icta', 'sahana']
        count = 0
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in keywords):
                count += 1
        return count
    
    def _count_healthcare_projects(self, repositories: List[Dict[str, Any]]) -> int:
        """Count healthcare related projects"""
        keywords = ['health', 'medical', 'hospital', 'patient', 'hmis', 'clinical']
        count = 0
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in keywords):
                count += 1
        return count
    
    def _determine_key_focus(self, analytics_data: Dict[str, Any]) -> str:
        """Determine the key focus area for the week"""
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        
        # Check healthcare focus
        healthcare_data = contribution_patterns.get('healthcare_contributions', {})
        if healthcare_data.get('total_healthcare_repositories', 0) > 0:
            return "Healthcare Technology & Sri Lankan Social Good Projects"
        
        # Check Sri Lankan projects
        sri_lankan_data = contribution_patterns.get('sri_lankan_projects', {})
        if sri_lankan_data.get('total_sri_lankan_projects', 0) > 0:
            return "Sri Lankan Open Source & Social Impact Projects"
        
        # Default focus
        return "Open Source Portfolio Development & Community Engagement"
    
    def _calculate_health_trend(self, analytics_data: Dict[str, Any]) -> str:
        """Calculate health trend direction"""
        # This would compare with historical data
        # For now, return a placeholder
        return "Stable"
    
    def _identify_improvement_opportunities(self, health_scores: Dict[str, Any]) -> List[str]:
        """Identify improvement opportunities"""
        opportunities = []
        
        # Count common issues
        all_recommendations = []
        for health_data in health_scores.values():
            if isinstance(health_data, dict):
                all_recommendations.extend(health_data.get('recommendations', []))
        
        from collections import Counter
        common_issues = Counter(all_recommendations).most_common(3)
        
        for issue, count in common_issues:
            if count > 1:
                opportunities.append(f"Address '{issue}' across {count} repositories")
        
        return opportunities
    
    def _analyze_contribution_focus(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze contribution focus areas"""
        focus_areas = {
            'healthcare': 0,
            'government': 0,
            'education': 0,
            'language': 0,
            'general': 0
        }
        
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            
            if any(keyword in repo_text for keyword in ['health', 'medical', 'hospital', 'hmis']):
                focus_areas['healthcare'] += 1
            elif any(keyword in repo_text for keyword in ['government', 'election', 'eclk', 'ministry']):
                focus_areas['government'] += 1
            elif any(keyword in repo_text for keyword in ['education', 'student', 'school', 'sis']):
                focus_areas['education'] += 1
            elif any(keyword in repo_text for keyword in ['sinhala', 'tamil', 'language', 'unicode']):
                focus_areas['language'] += 1
            else:
                focus_areas['general'] += 1
        
        return focus_areas
    
    def _classify_project_domain(self, repo: Dict[str, Any]) -> str:
        """Classify project domain"""
        repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
        
        if any(keyword in repo_text for keyword in ['health', 'medical', 'hospital', 'hmis']):
            return 'healthcare'
        elif any(keyword in repo_text for keyword in ['government', 'election', 'eclk']):
            return 'governance'
        elif any(keyword in repo_text for keyword in ['education', 'student', 'school']):
            return 'education'
        elif any(keyword in repo_text for keyword in ['sinhala', 'tamil', 'language']):
            return 'language'
        elif any(keyword in repo_text for keyword in ['disaster', 'emergency', 'sahana']):
            return 'disaster'
        else:
            return 'general'
    
    def _identify_healthcare_focus_area(self, repo: Dict[str, Any]) -> str:
        """Identify healthcare focus area"""
        repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
        
        if 'hmis' in repo_text:
            return 'Hospital Management'
        elif any(keyword in repo_text for keyword in ['pharmacy', 'medicine', 'drug']):
            return 'Pharmacy Systems'
        elif any(keyword in repo_text for keyword in ['patient', 'clinical', 'diagnosis']):
            return 'Clinical Systems'
        elif any(keyword in repo_text for keyword in ['public health', 'epidemiology', 'surveillance']):
            return 'Public Health'
        else:
            return 'General Healthcare'

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Generate weekly portfolio report')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output markdown file path')
    parser.add_argument('--force-regenerate', action='store_true', help='Force regenerate report')
    
    args = parser.parse_args()
    
    try:
        # Generate weekly report
        reporter = WeeklyReporter(args.config)
        reporter.generate_weekly_report(args.output, args.force_regenerate)
        
        logger.info("Weekly report generation completed successfully")
        
    except Exception as e:
        logger.error(f"Weekly report generation failed: {e}")
        raise

if __name__ == '__main__':
    main()
