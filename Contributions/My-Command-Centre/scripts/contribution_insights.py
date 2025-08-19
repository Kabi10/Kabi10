#!/usr/bin/env python3
"""
Contribution Insights Generator for My Command Centre
Generates actionable insights from contribution pattern analysis
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

class ContributionInsightsGenerator:
    """Generate actionable insights from contribution analysis"""
    
    def __init__(self, config_path: str):
        """Initialize the insights generator"""
        self.config = self._load_config(config_path)
        
        logger.info("Initialized contribution insights generator")
    
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
    
    def generate_insights(self, patterns_path: str, output_path: str, period: str = 'weekly') -> None:
        """Generate insights from contribution patterns"""
        logger.info(f"Generating {period} contribution insights")
        
        # Load contribution patterns
        with open(patterns_path, 'r') as file:
            patterns = json.load(file)
        
        # Generate insights
        insights = {
            'metadata': self._generate_metadata(period),
            'executive_summary': self._generate_executive_summary(patterns),
            'key_insights': self._generate_key_insights(patterns),
            'strategic_recommendations': self._generate_strategic_recommendations(patterns),
            'sri_lankan_focus': self._generate_sri_lankan_insights(patterns),
            'healthcare_contributions': self._generate_healthcare_insights(patterns),
            'open_source_engagement': self._generate_open_source_insights(patterns),
            'technology_trends': self._generate_technology_insights(patterns),
            'next_steps': self._generate_next_steps(patterns, period)
        }
        
        # Generate markdown report
        markdown_report = self._generate_markdown_report(insights)
        
        # Save insights
        output_dir = Path(output_path).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(output_path, 'w') as file:
            file.write(markdown_report)
        
        # Also save as JSON
        json_path = output_path.replace('.md', '.json')
        with open(json_path, 'w') as file:
            json.dump(insights, file, indent=2, default=str)
        
        logger.info(f"Contribution insights saved to {output_path}")
    
    def _generate_metadata(self, period: str) -> Dict[str, Any]:
        """Generate insights metadata"""
        return {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'period': period,
            'insights_version': '1.0.0',
            'generator': 'My Command Centre Contribution Insights'
        }
    
    def _generate_executive_summary(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate executive summary of contribution patterns"""
        open_source = patterns.get('open_source_contributions', {})
        sri_lankan = patterns.get('sri_lankan_projects', {})
        healthcare = patterns.get('healthcare_contributions', {})
        impact = patterns.get('impact_assessment', {})
        
        return {
            'total_open_source_contributions': open_source.get('total_forked_repositories', 0),
            'active_contribution_rate': open_source.get('activity_percentage', 0),
            'sri_lankan_project_count': sri_lankan.get('total_sri_lankan_projects', 0),
            'healthcare_repository_count': healthcare.get('total_healthcare_repositories', 0),
            'social_impact_score': impact.get('social_good_impact', {}).get('percentage_of_portfolio', 0),
            'professional_impact_score': impact.get('professional_impact_score', 0),
            'key_achievement': self._identify_key_achievement(patterns),
            'primary_focus_area': self._identify_primary_focus(patterns)
        }
    
    def _generate_key_insights(self, patterns: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate key insights from patterns"""
        insights = []
        
        # Open source contribution insight
        open_source = patterns.get('open_source_contributions', {})
        if open_source.get('activity_percentage', 0) < 50:
            insights.append({
                'category': 'Open Source Engagement',
                'insight': 'Low Active Fork Engagement',
                'description': f"Only {open_source.get('activity_percentage', 0):.1f}% of forked repositories show recent activity",
                'impact': 'Medium',
                'recommendation': 'Focus on 2-3 high-impact projects for consistent contributions'
            })
        
        # Sri Lankan projects insight
        sri_lankan = patterns.get('sri_lankan_projects', {})
        healthcare_domain = sri_lankan.get('domain_breakdown', {}).get('healthcare', {})
        if healthcare_domain:
            insights.append({
                'category': 'Social Impact',
                'insight': 'Strong Healthcare Focus',
                'description': f"Healthcare domain shows {healthcare_domain.get('repository_count', 0)} repositories with significant potential",
                'impact': 'High',
                'recommendation': 'Leverage healthcare expertise for OpenMRS and DHIS2 contributions'
            })
        
        # Technology specialization insight
        tech_patterns = patterns.get('technology_patterns', {})
        specialization = tech_patterns.get('specialization_analysis', {})
        if specialization.get('specialization_level') == 'High':
            insights.append({
                'category': 'Technical Expertise',
                'insight': 'Strong Technology Specialization',
                'description': f"High specialization in {specialization.get('primary_language')} ({specialization.get('specialization_percentage', 0):.1f}%)",
                'impact': 'Medium',
                'recommendation': 'Consider diversifying to complementary technologies for broader impact'
            })
        
        # Collaboration insight
        collaboration = patterns.get('collaboration_patterns', {})
        if collaboration.get('collaboration_percentage', 0) < 30:
            insights.append({
                'category': 'Community Engagement',
                'insight': 'Limited Collaborative Projects',
                'description': f"Only {collaboration.get('collaboration_percentage', 0):.1f}% of repositories involve collaboration",
                'impact': 'Medium',
                'recommendation': 'Seek mentorship opportunities and community leadership roles'
            })
        
        # Impact insight
        impact = patterns.get('impact_assessment', {})
        social_good_percentage = impact.get('social_good_impact', {}).get('percentage_of_portfolio', 0)
        if social_good_percentage > 40:
            insights.append({
                'category': 'Social Impact',
                'insight': 'High Social Good Alignment',
                'description': f"{social_good_percentage:.1f}% of portfolio focuses on social good projects",
                'impact': 'High',
                'recommendation': 'Position as thought leader in technology for social good'
            })
        
        return insights
    
    def _generate_strategic_recommendations(self, patterns: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Generate strategic recommendations"""
        recommendations = patterns.get('strategic_recommendations', [])
        
        # Enhance with specific Sri Lankan context
        enhanced_recommendations = []
        for rec in recommendations:
            enhanced_rec = rec.copy()
            
            if rec.get('category') == 'Social Impact':
                enhanced_rec['sri_lankan_context'] = {
                    'priority_projects': ['OpenMRS localization', 'DHIS2 health apps', 'ECLK election tools'],
                    'impact_potential': 'Direct improvement to Sri Lankan healthcare and governance',
                    'timeline': '3-6 months for initial contributions'
                }
            elif rec.get('category') == 'Domain Expertise':
                enhanced_rec['healthcare_focus'] = {
                    'current_expertise': 'HMIS hospital management systems',
                    'expansion_areas': ['Clinical workflows', 'Health data standards', 'Interoperability'],
                    'learning_resources': ['OpenMRS documentation', 'DHIS2 training', 'HL7 FHIR standards']
                }
            
            enhanced_recommendations.append(enhanced_rec)
        
        return enhanced_recommendations
    
    def _generate_sri_lankan_insights(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate Sri Lankan project specific insights"""
        sri_lankan = patterns.get('sri_lankan_projects', {})
        domain_breakdown = sri_lankan.get('domain_breakdown', {})
        
        insights = {
            'total_projects': sri_lankan.get('total_sri_lankan_projects', 0),
            'domain_analysis': {},
            'opportunity_assessment': {},
            'strategic_alignment': sri_lankan.get('strategic_focus_alignment', {})
        }
        
        # Analyze each domain
        for domain, data in domain_breakdown.items():
            insights['domain_analysis'][domain] = {
                'repository_count': data.get('repository_count', 0),
                'total_stars': data.get('total_stars', 0),
                'contribution_opportunities': data.get('contribution_opportunities', []),
                'priority_level': self._assess_domain_priority(domain, data)
            }
        
        # Opportunity assessment
        insights['opportunity_assessment'] = {
            'immediate_opportunities': self._identify_immediate_opportunities(domain_breakdown),
            'medium_term_goals': self._identify_medium_term_goals(domain_breakdown),
            'long_term_vision': self._identify_long_term_vision(domain_breakdown)
        }
        
        return insights
    
    def _generate_healthcare_insights(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate healthcare-specific insights"""
        healthcare = patterns.get('healthcare_contributions', {})
        hmis_data = healthcare.get('hmis_contributions', {})
        
        return {
            'current_focus': {
                'hmis_repositories': hmis_data.get('repository_count', 0),
                'contribution_areas': hmis_data.get('contribution_focus', {}),
                'expertise_level': healthcare.get('healthcare_domain_expertise', {}).get('expertise_level', 'Unknown')
            },
            'expansion_opportunities': {
                'openmrs_potential': 'High - Global platform with Sri Lankan relevance',
                'dhis2_potential': 'High - Used by Sri Lankan Ministry of Health',
                'local_health_systems': 'Medium - Opportunity for custom solutions'
            },
            'impact_potential': healthcare.get('patient_impact_potential', 'Unknown'),
            'next_steps': [
                'Complete current HMIS optimization work',
                'Begin OpenMRS community engagement',
                'Explore DHIS2 app development opportunities',
                'Develop healthcare interoperability expertise'
            ]
        }
    
    def _generate_open_source_insights(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate open source engagement insights"""
        open_source = patterns.get('open_source_contributions', {})
        target_projects = open_source.get('target_project_contributions', {})
        
        insights = {
            'engagement_metrics': {
                'total_forks': open_source.get('total_forked_repositories', 0),
                'active_forks': open_source.get('active_forked_repositories', 0),
                'activity_rate': open_source.get('activity_percentage', 0)
            },
            'target_project_status': {},
            'methodology_effectiveness': self._assess_methodology_effectiveness(open_source),
            'community_building_opportunities': self._identify_community_opportunities(patterns)
        }
        
        # Analyze target projects
        for project, data in target_projects.items():
            insights['target_project_status'][project] = {
                'activity_score': data.get('activity_score', 0),
                'last_activity': data.get('last_activity'),
                'focus_areas': data.get('focus_areas', []),
                'status': 'Active' if data.get('activity_score', 0) > 50 else 'Needs Attention'
            }
        
        return insights
    
    def _generate_technology_insights(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate technology trend insights"""
        tech_patterns = patterns.get('technology_patterns', {})
        
        return {
            'language_distribution': tech_patterns.get('language_distribution', []),
            'specialization_analysis': tech_patterns.get('specialization_analysis', {}),
            'open_source_vs_personal': {
                'open_source_languages': tech_patterns.get('open_source_languages', {}),
                'personal_languages': tech_patterns.get('personal_project_languages', {})
            },
            'diversity_score': tech_patterns.get('technology_diversity_score', 0),
            'recommendations': self._generate_technology_recommendations(tech_patterns)
        }
    
    def _generate_next_steps(self, patterns: Dict[str, Any], period: str) -> Dict[str, Any]:
        """Generate next steps based on period and patterns"""
        if period == 'weekly':
            return self._generate_weekly_next_steps(patterns)
        elif period == 'monthly':
            return self._generate_monthly_next_steps(patterns)
        else:
            return self._generate_general_next_steps(patterns)
    
    def _generate_weekly_next_steps(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate weekly next steps"""
        return {
            'immediate_actions': [
                'Review and sync active forked repositories',
                'Complete pending HMIS contributions',
                'Engage with OpenMRS community discussions',
                'Update contribution methodology documentation'
            ],
            'focus_areas': [
                'HMIS pharmacy module optimization',
                'OpenMRS localization research',
                'DHIS2 app development exploration'
            ],
            'learning_objectives': [
                'Healthcare workflow understanding',
                'Open source community best practices',
                'Sri Lankan health system requirements'
            ]
        }
    
    def _generate_monthly_next_steps(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate monthly next steps"""
        return {
            'strategic_initiatives': [
                'Launch OpenMRS Sinhala localization project',
                'Develop first DHIS2 health application',
                'Establish mentorship in HMIS community',
                'Create Sri Lankan healthcare tech documentation'
            ],
            'skill_development': [
                'Healthcare interoperability standards',
                'Mobile health application development',
                'Community leadership and mentoring',
                'Technical writing and documentation'
            ],
            'network_building': [
                'Connect with Sri Lankan health tech professionals',
                'Join OpenMRS and DHIS2 communities',
                'Participate in healthcare technology conferences',
                'Establish relationships with Ministry of Health IT'
            ]
        }
    
    def _generate_general_next_steps(self, patterns: Dict[str, Any]) -> Dict[str, Any]:
        """Generate general next steps"""
        return {
            'short_term': ['Focus on active contributions', 'Build community relationships'],
            'medium_term': ['Expand to new projects', 'Develop domain expertise'],
            'long_term': ['Become community leader', 'Drive innovation in healthcare tech']
        }
    
    def _generate_markdown_report(self, insights: Dict[str, Any]) -> str:
        """Generate markdown report from insights"""
        metadata = insights['metadata']
        summary = insights['executive_summary']
        
        markdown = f"""# Contribution Insights Report - {metadata['period'].title()}

*Generated on {metadata['generated_at']} by {metadata['generator']}*

---

## Executive Summary

### Portfolio Overview
- **Open Source Contributions:** {summary['total_open_source_contributions']} repositories
- **Active Contribution Rate:** {summary['active_contribution_rate']:.1f}%
- **Sri Lankan Projects:** {summary['sri_lankan_project_count']} repositories
- **Healthcare Focus:** {summary['healthcare_repository_count']} repositories
- **Social Impact Score:** {summary['social_impact_score']:.1f}%

### Key Achievement
{summary['key_achievement']}

### Primary Focus Area
{summary['primary_focus_area']}

---

## Key Insights

"""
        
        for insight in insights['key_insights']:
            markdown += f"""### {insight['category']}: {insight['insight']}

**Description:** {insight['description']}  
**Impact Level:** {insight['impact']}  
**Recommendation:** {insight['recommendation']}

"""
        
        markdown += """---

## Strategic Recommendations

"""
        
        for i, rec in enumerate(insights['strategic_recommendations'], 1):
            markdown += f"""### {i}. {rec['title']}

**Category:** {rec['category']}  
**Priority:** {rec['priority']}

{rec['description']}

**Action Items:**
"""
            for action in rec['action_items']:
                markdown += f"- {action}\n"
            
            if 'sri_lankan_context' in rec:
                context = rec['sri_lankan_context']
                markdown += f"""
**Sri Lankan Context:**
- **Priority Projects:** {', '.join(context['priority_projects'])}
- **Impact Potential:** {context['impact_potential']}
- **Timeline:** {context['timeline']}
"""
            
            markdown += "\n"
        
        # Sri Lankan Focus Section
        sri_lankan = insights['sri_lankan_focus']
        markdown += f"""---

## Sri Lankan Open Source Focus

### Current Portfolio
- **Total Projects:** {sri_lankan['total_projects']}
- **Strategic Alignment:** {sri_lankan['strategic_alignment'].get('alignment_percentage', 0):.1f}%

### Domain Analysis
"""
        
        for domain, analysis in sri_lankan['domain_analysis'].items():
            markdown += f"""
#### {domain.title()}
- **Repositories:** {analysis['repository_count']}
- **Community Stars:** {analysis['total_stars']}
- **Priority Level:** {analysis['priority_level']}
"""
        
        # Healthcare Contributions Section
        healthcare = insights['healthcare_contributions']
        markdown += f"""---

## Healthcare Technology Contributions

### Current Focus
- **HMIS Repositories:** {healthcare['current_focus']['hmis_repositories']}
- **Expertise Level:** {healthcare['current_focus']['expertise_level']}
- **Patient Impact:** {healthcare['impact_potential']}

### Expansion Opportunities
- **OpenMRS:** {healthcare['expansion_opportunities']['openmrs_potential']}
- **DHIS2:** {healthcare['expansion_opportunities']['dhis2_potential']}
- **Local Systems:** {healthcare['expansion_opportunities']['local_health_systems']}

### Next Steps
"""
        
        for step in healthcare['next_steps']:
            markdown += f"- {step}\n"
        
        # Technology Trends Section
        tech = insights['technology_trends']
        markdown += f"""---

## Technology Trends

### Language Specialization
- **Primary Language:** {tech['specialization_analysis'].get('primary_language', 'Unknown')}
- **Specialization Level:** {tech['specialization_analysis'].get('specialization_level', 'Unknown')}
- **Diversity Score:** {tech['diversity_score']}

### Top Languages
"""
        
        for lang_data in tech['language_distribution'][:5]:
            markdown += f"- **{lang_data['language']}:** {lang_data['count']} repositories\n"
        
        # Next Steps Section
        next_steps = insights['next_steps']
        markdown += f"""---

## Next Steps

"""
        
        if 'immediate_actions' in next_steps:
            markdown += """### Immediate Actions (This Week)
"""
            for action in next_steps['immediate_actions']:
                markdown += f"- [ ] {action}\n"
        
        if 'strategic_initiatives' in next_steps:
            markdown += """
### Strategic Initiatives (This Month)
"""
            for initiative in next_steps['strategic_initiatives']:
                markdown += f"- [ ] {initiative}\n"
        
        if 'focus_areas' in next_steps:
            markdown += """
### Focus Areas
"""
            for area in next_steps['focus_areas']:
                markdown += f"- {area}\n"
        
        markdown += f"""
---

*This insights report is automatically generated by My Command Centre. For detailed data and interactive analysis, see the full analytics dashboard.*

**Next Report:** Scheduled for next {metadata['period']} cycle
"""
        
        return markdown
    
    # Helper methods
    def _identify_key_achievement(self, patterns: Dict[str, Any]) -> str:
        """Identify the key achievement from patterns"""
        healthcare = patterns.get('healthcare_contributions', {})
        if healthcare.get('total_healthcare_repositories', 0) > 0:
            return "Strong healthcare technology focus with direct patient impact potential"
        
        sri_lankan = patterns.get('sri_lankan_projects', {})
        if sri_lankan.get('total_sri_lankan_projects', 0) > 0:
            return "Meaningful contributions to Sri Lankan social good projects"
        
        return "Active open source contribution portfolio with diverse technology focus"
    
    def _identify_primary_focus(self, patterns: Dict[str, Any]) -> str:
        """Identify the primary focus area"""
        healthcare = patterns.get('healthcare_contributions', {})
        if healthcare.get('total_healthcare_repositories', 0) > 0:
            return "Healthcare Information Systems & Sri Lankan Social Good"
        
        open_source = patterns.get('open_source_contributions', {})
        if open_source.get('total_forked_repositories', 0) > 0:
            return "Open Source Community Contributions"
        
        return "Personal Project Development & Technology Exploration"
    
    def _assess_domain_priority(self, domain: str, data: Dict[str, Any]) -> str:
        """Assess priority level for a domain"""
        repo_count = data.get('repository_count', 0)
        stars = data.get('total_stars', 0)
        
        if domain == 'healthcare' and repo_count > 0:
            return 'High'
        elif domain == 'governance' and repo_count > 0:
            return 'Medium-High'
        elif repo_count > 0 and stars > 10:
            return 'Medium'
        else:
            return 'Low'
    
    def _identify_immediate_opportunities(self, domain_breakdown: Dict[str, Any]) -> List[str]:
        """Identify immediate contribution opportunities"""
        opportunities = []
        
        if 'healthcare' in domain_breakdown:
            opportunities.append('Complete HMIS pharmacy module optimization')
            opportunities.append('Begin OpenMRS community engagement')
        
        if 'governance' in domain_breakdown:
            opportunities.append('Monitor ECLK for election cycle contributions')
        
        if 'education' in domain_breakdown:
            opportunities.append('Address SIS maintenance issues')
        
        return opportunities
    
    def _identify_medium_term_goals(self, domain_breakdown: Dict[str, Any]) -> List[str]:
        """Identify medium-term goals"""
        return [
            'Launch OpenMRS Sinhala localization project',
            'Develop DHIS2 health applications',
            'Establish mentorship in healthcare tech community',
            'Create comprehensive Sri Lankan health tech documentation'
        ]
    
    def _identify_long_term_vision(self, domain_breakdown: Dict[str, Any]) -> List[str]:
        """Identify long-term vision items"""
        return [
            'Become recognized leader in Sri Lankan healthcare technology',
            'Drive adoption of open source health systems in Sri Lanka',
            'Establish healthcare technology innovation community',
            'Contribute to national health digitization strategy'
        ]
    
    def _assess_methodology_effectiveness(self, open_source: Dict[str, Any]) -> str:
        """Assess effectiveness of contribution methodology"""
        activity_rate = open_source.get('activity_percentage', 0)
        
        if activity_rate > 70:
            return 'Highly Effective'
        elif activity_rate > 50:
            return 'Effective'
        elif activity_rate > 30:
            return 'Moderately Effective'
        else:
            return 'Needs Improvement'
    
    def _identify_community_opportunities(self, patterns: Dict[str, Any]) -> List[str]:
        """Identify community building opportunities"""
        return [
            'Mentor new contributors in HMIS project',
            'Lead OpenMRS localization initiative',
            'Organize Sri Lankan healthcare tech meetups',
            'Contribute to open source healthcare conferences'
        ]
    
    def _generate_technology_recommendations(self, tech_patterns: Dict[str, Any]) -> List[str]:
        """Generate technology-specific recommendations"""
        recommendations = []
        
        specialization = tech_patterns.get('specialization_analysis', {})
        if specialization.get('specialization_level') == 'High':
            recommendations.append('Consider learning complementary technologies for broader impact')
        
        diversity_score = tech_patterns.get('technology_diversity_score', 0)
        if diversity_score < 5:
            recommendations.append('Explore new programming languages and frameworks')
        
        return recommendations

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Generate contribution insights')
    parser.add_argument('--patterns', required=True, help='Path to contribution patterns JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output insights markdown file')
    parser.add_argument('--period', default='weekly', choices=['weekly', 'monthly'], help='Reporting period')
    
    args = parser.parse_args()
    
    try:
        # Generate insights
        generator = ContributionInsightsGenerator(args.config)
        generator.generate_insights(args.patterns, args.output, args.period)
        
        logger.info("Contribution insights generation completed successfully")
        
    except Exception as e:
        logger.error(f"Contribution insights generation failed: {e}")
        raise

if __name__ == '__main__':
    main()
