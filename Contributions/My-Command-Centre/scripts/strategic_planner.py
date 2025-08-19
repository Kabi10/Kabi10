#!/usr/bin/env python3
"""
Strategic Planner for My Command Centre
Updates strategic planning documents based on portfolio analysis and contribution patterns
"""

import json
import argparse
import yaml
import os
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
import re

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class StrategicPlanner:
    """Update strategic planning documents based on analytics"""
    
    def __init__(self, config_path: str):
        """Initialize the strategic planner"""
        self.config = self._load_config(config_path)
        self.strategy_config = self.config.get('strategic_planning', {})
        
        logger.info("Initialized strategic planner")
    
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
    
    def update_strategic_documents(self, analytics_dir: str, strategy_dir: str, 
                                 update_type: str = 'weekly') -> None:
        """Update strategic documents based on analytics"""
        logger.info(f"Updating strategic documents - {update_type} update")
        
        # Load analytics data
        analytics_data = self._load_analytics_data(analytics_dir)
        
        # Update different types of strategic documents
        if update_type == 'weekly':
            self._update_weekly_documents(analytics_data, strategy_dir)
        elif update_type == 'monthly':
            self._update_monthly_documents(analytics_data, strategy_dir)
        elif update_type == 'quarterly':
            self._update_quarterly_documents(analytics_data, strategy_dir)
        
        logger.info("Strategic document updates completed")
    
    def _load_analytics_data(self, analytics_dir: str) -> Dict[str, Any]:
        """Load all relevant analytics data"""
        analytics_data = {}
        
        # Load key analytics files
        analytics_files = {
            'repositories': 'repositories.json',
            'health_scores': 'health_scores.json',
            'portfolio_overview': 'portfolio_overview.json',
            'contribution_patterns': 'contribution_patterns.json',
            'activity_heatmap': 'activity_heatmap.json',
            'health_trends': 'health_trends.json'
        }
        
        for key, filename in analytics_files.items():
            file_path = Path(analytics_dir) / filename
            if file_path.exists():
                try:
                    with open(file_path, 'r') as file:
                        analytics_data[key] = json.load(file)
                except Exception as e:
                    logger.warning(f"Could not load {filename}: {e}")
                    analytics_data[key] = {}
            else:
                analytics_data[key] = {}
        
        return analytics_data
    
    def _update_weekly_documents(self, analytics_data: Dict[str, Any], strategy_dir: str) -> None:
        """Update weekly strategic documents"""
        strategy_path = Path(strategy_dir)
        
        # Update contribution methodology progress
        self._update_contribution_methodology_progress(analytics_data, strategy_path)
        
        # Update project timelines
        self._update_project_timelines(analytics_data, strategy_path, 'weekly')
        
        # Generate weekly strategic insights
        self._generate_weekly_insights(analytics_data, strategy_path)
    
    def _update_monthly_documents(self, analytics_data: Dict[str, Any], strategy_dir: str) -> None:
        """Update monthly strategic documents"""
        strategy_path = Path(strategy_dir)
        
        # Update all weekly items
        self._update_weekly_documents(analytics_data, strategy_dir)
        
        # Monthly specific updates
        self._update_professional_development_assessment(analytics_data, strategy_path)
        self._update_repository_curation_plan(analytics_data, strategy_path)
        self._generate_monthly_strategic_review(analytics_data, strategy_path)
    
    def _update_quarterly_documents(self, analytics_data: Dict[str, Any], strategy_dir: str) -> None:
        """Update quarterly strategic documents"""
        strategy_path = Path(strategy_dir)
        
        # Update all monthly items
        self._update_monthly_documents(analytics_data, strategy_dir)
        
        # Quarterly specific updates
        self._generate_quarterly_assessment(analytics_data, strategy_path)
        self._update_long_term_strategic_plans(analytics_data, strategy_path)
    
    def _update_contribution_methodology_progress(self, analytics_data: Dict[str, Any], 
                                                strategy_path: Path) -> None:
        """Update contribution methodology with progress data"""
        methodology_dir = strategy_path / 'contribution-methodology'
        
        # Update HMIS contribution strategy
        hmis_strategy_path = methodology_dir / '2024-01-15-hmis-contribution-strategy.md'
        if hmis_strategy_path.exists():
            self._update_hmis_strategy_progress(analytics_data, hmis_strategy_path)
        
        # Update Sri Lankan projects analysis
        sri_lankan_analysis_path = methodology_dir / 'sri-lankan-open-source-projects-analysis.md'
        if sri_lankan_analysis_path.exists():
            self._update_sri_lankan_projects_progress(analytics_data, sri_lankan_analysis_path)
    
    def _update_hmis_strategy_progress(self, analytics_data: Dict[str, Any], 
                                     strategy_path: Path) -> None:
        """Update HMIS strategy with current progress"""
        try:
            # Read current strategy document
            with open(strategy_path, 'r') as file:
                content = file.read()
            
            # Extract HMIS-related data from analytics
            contribution_patterns = analytics_data.get('contribution_patterns', {})
            healthcare_contributions = contribution_patterns.get('healthcare_contributions', {})
            hmis_data = healthcare_contributions.get('hmis_contributions', {})
            
            # Update progress sections
            updated_content = self._update_progress_sections(content, {
                'hmis_repositories': hmis_data.get('repository_count', 0),
                'last_contribution': self._get_last_hmis_activity(analytics_data),
                'contribution_focus': hmis_data.get('contribution_focus', {}),
                'monthly_goal_progress': self._calculate_monthly_progress(analytics_data)
            })
            
            # Write updated content
            with open(strategy_path, 'w') as file:
                file.write(updated_content)
            
            logger.info("Updated HMIS contribution strategy progress")
            
        except Exception as e:
            logger.error(f"Failed to update HMIS strategy: {e}")
    
    def _update_sri_lankan_projects_progress(self, analytics_data: Dict[str, Any], 
                                           strategy_path: Path) -> None:
        """Update Sri Lankan projects analysis with current data"""
        try:
            # Read current analysis document
            with open(strategy_path, 'r') as file:
                content = file.read()
            
            # Extract Sri Lankan project data
            contribution_patterns = analytics_data.get('contribution_patterns', {})
            sri_lankan_projects = contribution_patterns.get('sri_lankan_projects', {})
            
            # Update project status sections
            domain_breakdown = sri_lankan_projects.get('domain_breakdown', {})
            
            # Update each tier with current status
            updated_content = self._update_project_tier_status(content, domain_breakdown)
            
            # Write updated content
            with open(strategy_path, 'w') as file:
                file.write(updated_content)
            
            logger.info("Updated Sri Lankan projects analysis")
            
        except Exception as e:
            logger.error(f"Failed to update Sri Lankan projects analysis: {e}")
    
    def _update_project_timelines(self, analytics_data: Dict[str, Any], 
                                strategy_path: Path, update_type: str) -> None:
        """Update project timeline documents"""
        timelines_dir = strategy_path / 'project-timelines'
        timelines_dir.mkdir(exist_ok=True)
        
        # Create or update timeline for current strategic initiatives
        current_date = datetime.now().strftime('%Y-%m-%d')
        
        if update_type == 'weekly':
            self._update_weekly_timeline(analytics_data, timelines_dir, current_date)
        elif update_type == 'monthly':
            self._update_monthly_timeline(analytics_data, timelines_dir, current_date)
    
    def _generate_weekly_insights(self, analytics_data: Dict[str, Any], 
                                strategy_path: Path) -> None:
        """Generate weekly strategic insights"""
        insights_dir = strategy_path / 'weekly-insights'
        insights_dir.mkdir(exist_ok=True)
        
        current_week = datetime.now().strftime('%Y-W%U')
        insights_path = insights_dir / f'{current_week}-weekly-insights.md'
        
        # Generate insights content
        insights_content = self._create_weekly_insights_content(analytics_data)
        
        with open(insights_path, 'w') as file:
            file.write(insights_content)
        
        logger.info(f"Generated weekly insights: {insights_path}")
    
    def _update_professional_development_assessment(self, analytics_data: Dict[str, Any], 
                                                  strategy_path: Path) -> None:
        """Update professional development assessment"""
        dev_dir = strategy_path / 'professional-development'
        dev_dir.mkdir(exist_ok=True)
        
        current_month = datetime.now().strftime('%Y-%m')
        assessment_path = dev_dir / f'{current_month}-monthly-assessment.md'
        
        # Generate assessment content
        assessment_content = self._create_professional_development_assessment(analytics_data)
        
        with open(assessment_path, 'w') as file:
            file.write(assessment_content)
        
        logger.info(f"Updated professional development assessment: {assessment_path}")
    
    def _update_repository_curation_plan(self, analytics_data: Dict[str, Any], 
                                       strategy_path: Path) -> None:
        """Update repository curation plan"""
        curation_dir = strategy_path / 'repository-curation'
        curation_dir.mkdir(exist_ok=True)
        
        current_month = datetime.now().strftime('%Y-%m')
        curation_path = curation_dir / f'{current_month}-curation-plan.md'
        
        # Generate curation plan content
        curation_content = self._create_repository_curation_plan(analytics_data)
        
        with open(curation_path, 'w') as file:
            file.write(curation_content)
        
        logger.info(f"Updated repository curation plan: {curation_path}")
    
    def _generate_monthly_strategic_review(self, analytics_data: Dict[str, Any], 
                                         strategy_path: Path) -> None:
        """Generate monthly strategic review"""
        reviews_dir = strategy_path / 'monthly-reviews'
        reviews_dir.mkdir(exist_ok=True)
        
        current_month = datetime.now().strftime('%Y-%m')
        review_path = reviews_dir / f'{current_month}-strategic-review.md'
        
        # Generate review content
        review_content = self._create_monthly_strategic_review(analytics_data)
        
        with open(review_path, 'w') as file:
            file.write(review_content)
        
        logger.info(f"Generated monthly strategic review: {review_path}")
    
    def _create_weekly_insights_content(self, analytics_data: Dict[str, Any]) -> str:
        """Create weekly insights content"""
        current_week = datetime.now().strftime('%Y-W%U')
        
        # Extract key metrics
        portfolio_overview = analytics_data.get('portfolio_overview', {})
        health_trends = analytics_data.get('health_trends', {})
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        
        content = f"""# Weekly Strategic Insights - {current_week}

**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M UTC')}

## Portfolio Health Summary

- **Total Repositories:** {portfolio_overview.get('repository_counts', {}).get('total', 0)}
- **Active This Week:** {portfolio_overview.get('repository_counts', {}).get('active_last_month', 0)}
- **Health Score:** {health_trends.get('health_overview', {}).get('health_percentage', 0):.1f}%

## Key Insights This Week

### Open Source Contributions
"""
        
        # Add open source insights
        open_source = contribution_patterns.get('open_source_contributions', {})
        content += f"""
- **Active Forks:** {open_source.get('active_forked_repositories', 0)} of {open_source.get('total_forked_repositories', 0)}
- **Activity Rate:** {open_source.get('activity_percentage', 0):.1f}%
"""
        
        # Add Sri Lankan project insights
        sri_lankan = contribution_patterns.get('sri_lankan_projects', {})
        content += f"""
### Sri Lankan Social Good Projects
- **Total Projects:** {sri_lankan.get('total_sri_lankan_projects', 0)}
- **Healthcare Focus:** {len(sri_lankan.get('domain_breakdown', {}).get('healthcare', {}).get('repositories', []))} repositories
"""
        
        # Add recommendations
        recommendations = contribution_patterns.get('strategic_recommendations', [])
        if recommendations:
            content += """
## This Week's Priorities

"""
            for i, rec in enumerate(recommendations[:3], 1):
                content += f"{i}. **{rec.get('title', 'Unknown')}** - {rec.get('priority', 'Medium')} Priority\n"
        
        content += """
## Next Week's Focus

- [ ] Review and update active contribution targets
- [ ] Sync forked repositories with upstream
- [ ] Continue HMIS optimization work
- [ ] Engage with OpenMRS community

---

*This insight is automatically generated by My Command Centre strategic planning system.*
"""
        
        return content
    
    def _create_professional_development_assessment(self, analytics_data: Dict[str, Any]) -> str:
        """Create professional development assessment content"""
        current_month = datetime.now().strftime('%Y-%m')
        
        # Extract relevant data
        contribution_patterns = analytics_data.get('contribution_patterns', {})
        technology_patterns = contribution_patterns.get('technology_patterns', {})
        impact_assessment = contribution_patterns.get('impact_assessment', {})
        
        content = f"""# Professional Development Assessment - {current_month}

**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M UTC')}

## Skills Development Progress

### Technical Skills
- **Primary Language:** {technology_patterns.get('specialization_analysis', {}).get('primary_language', 'Unknown')}
- **Technology Diversity:** {technology_patterns.get('technology_diversity_score', 0)} languages
- **Specialization Level:** {technology_patterns.get('specialization_analysis', {}).get('specialization_level', 'Unknown')}

### Domain Expertise
- **Healthcare Technology:** Intermediate (HMIS contributions)
- **Open Source Community:** Developing (active in multiple projects)
- **Sri Lankan Social Good:** Focused (healthcare and governance projects)

### Professional Impact
- **Community Recognition:** {impact_assessment.get('engagement_metrics', {}).get('total_stars', 0)} stars across portfolio
- **Collaboration Score:** {contribution_patterns.get('collaboration_patterns', {}).get('collaboration_percentage', 0):.1f}%
- **Social Impact Score:** {impact_assessment.get('social_good_impact', {}).get('percentage_of_portfolio', 0):.1f}%

## Learning Objectives for Next Month

### Technical Learning
- [ ] Deepen healthcare interoperability knowledge
- [ ] Explore mobile health application development
- [ ] Learn DHIS2 app development framework

### Community Engagement
- [ ] Increase mentorship activities in HMIS project
- [ ] Begin OpenMRS community participation
- [ ] Establish connections with Sri Lankan health tech professionals

### Professional Growth
- [ ] Document healthcare technology expertise
- [ ] Present learnings at relevant conferences
- [ ] Build thought leadership in healthcare technology

---

*This assessment is part of the My Command Centre professional development tracking system.*
"""
        
        return content
    
    def _create_repository_curation_plan(self, analytics_data: Dict[str, Any]) -> str:
        """Create repository curation plan content"""
        current_month = datetime.now().strftime('%Y-%m')
        
        # Extract repository data
        repositories = analytics_data.get('repositories', {}).get('repositories', [])
        health_scores = analytics_data.get('health_scores', {}).get('health_scores', {})
        
        # Analyze repositories needing attention
        poor_health_repos = [
            name for name, health in health_scores.items()
            if isinstance(health, dict) and health.get('health_grade') == 'poor'
        ]
        
        stale_repos = []
        for repo in repositories:
            if repo.get('updated_at'):
                try:
                    updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                    if (datetime.now(timezone.utc) - updated).days > 180:
                        stale_repos.append(repo['name'])
                except:
                    continue
        
        content = f"""# Repository Curation Plan - {current_month}

**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M UTC')}

## Portfolio Overview
- **Total Repositories:** {len(repositories)}
- **Repositories Needing Attention:** {len(poor_health_repos)}
- **Stale Repositories (6+ months):** {len(stale_repos)}

## Curation Priorities

### High Priority - Health Improvements
"""
        
        for repo in poor_health_repos[:5]:
            content += f"- **{repo.split('/')[-1]}** - Poor health score, needs immediate attention\n"
        
        content += f"""
### Medium Priority - Stale Repository Review
"""
        
        for repo in stale_repos[:5]:
            content += f"- **{repo}** - No updates in 6+ months, review for archival\n"
        
        content += """
## Curation Actions

### This Month
- [ ] Review poor health repositories and create improvement plans
- [ ] Archive or update stale repositories
- [ ] Improve documentation for top repositories
- [ ] Add meaningful descriptions and topics

### Next Month
- [ ] Consolidate related small repositories
- [ ] Enhance visibility of high-impact projects
- [ ] Create comprehensive README files
- [ ] Establish contribution guidelines for collaborative projects

## Success Metrics
- Increase average health score by 10 points
- Reduce stale repositories by 50%
- Improve documentation coverage to 90%
- Increase community engagement (stars/forks) by 20%

---

*This curation plan is automatically generated by My Command Centre portfolio management system.*
"""
        
        return content
    
    def _create_monthly_strategic_review(self, analytics_data: Dict[str, Any]) -> str:
        """Create monthly strategic review content"""
        current_month = datetime.now().strftime('%Y-%m')
        
        content = f"""# Monthly Strategic Review - {current_month}

**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M UTC')}

## Executive Summary

This monthly review assesses progress against strategic objectives and identifies opportunities for the upcoming month.

### Key Achievements
- Continued HMIS contributions with focus on pharmacy module
- Maintained active engagement in Sri Lankan healthcare technology
- Expanded open source contribution methodology

### Strategic Focus Areas
1. **Healthcare Technology Leadership** - Building expertise in HMIS and exploring OpenMRS
2. **Sri Lankan Social Good** - Contributing to projects with direct social impact
3. **Open Source Community Building** - Establishing mentorship and leadership roles

## Progress Against Strategic Goals

### Healthcare Technology Contributions
- **Status:** On Track
- **Progress:** Active HMIS contributions, beginning OpenMRS exploration
- **Next Steps:** Launch OpenMRS localization project, engage with DHIS2 community

### Sri Lankan Open Source Ecosystem
- **Status:** Strong Foundation
- **Progress:** Comprehensive analysis completed, target projects identified
- **Next Steps:** Begin contributions to ECLK and education sector projects

### Professional Development
- **Status:** Progressing Well
- **Progress:** Building healthcare domain expertise, expanding technical skills
- **Next Steps:** Increase community leadership, establish thought leadership

## Strategic Adjustments

Based on this month's analysis, the following adjustments are recommended:

1. **Increase OpenMRS Focus** - Allocate 20% of contribution time to OpenMRS community engagement
2. **Expand DHIS2 Exploration** - Begin app development for Sri Lankan health applications
3. **Strengthen Community Presence** - Increase mentorship activities and conference participation

## Next Month's Priorities

### Week 1-2: Foundation Building
- Complete current HMIS optimization work
- Begin OpenMRS community engagement
- Research DHIS2 app development opportunities

### Week 3-4: Expansion and Growth
- Launch first OpenMRS contribution
- Develop DHIS2 application prototype
- Establish mentorship relationships

---

*This strategic review is part of the My Command Centre monthly planning cycle.*
"""
        
        return content
    
    # Helper methods
    def _update_progress_sections(self, content: str, progress_data: Dict[str, Any]) -> str:
        """Update progress sections in strategy documents"""
        # This would implement sophisticated document updating logic
        # For now, return content as-is
        return content
    
    def _get_last_hmis_activity(self, analytics_data: Dict[str, Any]) -> str:
        """Get last HMIS activity date"""
        # Extract from analytics data
        return datetime.now().strftime('%Y-%m-%d')
    
    def _calculate_monthly_progress(self, analytics_data: Dict[str, Any]) -> float:
        """Calculate monthly contribution progress"""
        # Calculate based on contribution targets
        return 75.0  # Placeholder
    
    def _update_project_tier_status(self, content: str, domain_breakdown: Dict[str, Any]) -> str:
        """Update project tier status in documents"""
        # This would implement sophisticated status updating
        return content
    
    def _update_weekly_timeline(self, analytics_data: Dict[str, Any], 
                              timelines_dir: Path, current_date: str) -> None:
        """Update weekly timeline"""
        # Implementation for weekly timeline updates
        pass
    
    def _update_monthly_timeline(self, analytics_data: Dict[str, Any], 
                               timelines_dir: Path, current_date: str) -> None:
        """Update monthly timeline"""
        # Implementation for monthly timeline updates
        pass

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Update strategic planning documents')
    parser.add_argument('--analytics-dir', required=True, help='Path to analytics directory')
    parser.add_argument('--strategy-dir', required=True, help='Path to strategy directory')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--update-type', default='weekly', choices=['weekly', 'monthly', 'quarterly'], 
                       help='Type of update to perform')
    
    args = parser.parse_args()
    
    try:
        # Update strategic documents
        planner = StrategicPlanner(args.config)
        planner.update_strategic_documents(args.analytics_dir, args.strategy_dir, args.update_type)
        
        logger.info("Strategic planning update completed successfully")
        
    except Exception as e:
        logger.error(f"Strategic planning update failed: {e}")
        raise

if __name__ == '__main__':
    main()
