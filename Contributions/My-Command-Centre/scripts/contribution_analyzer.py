#!/usr/bin/env python3
"""
Contribution Analyzer Script for My Command Centre
Analyzes contribution patterns and generates insights for strategic planning
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
from collections import defaultdict, Counter
import statistics

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ContributionAnalyzer:
    """Analyze contribution patterns and generate strategic insights"""
    
    def __init__(self, config_path: str):
        """Initialize the contribution analyzer"""
        self.config = self._load_config(config_path)
        self.open_source_config = self.config.get('open_source', {})
        
        logger.info("Initialized contribution analyzer")
    
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
    
    def analyze_contribution_patterns(self, repositories: List[Dict[str, Any]], 
                                    output_path: str) -> None:
        """Analyze contribution patterns across repositories"""
        logger.info("Analyzing contribution patterns")
        
        # Analyze different aspects of contributions
        analysis = {
            'metadata': self._generate_metadata(),
            'open_source_contributions': self._analyze_open_source_contributions(repositories),
            'sri_lankan_projects': self._analyze_sri_lankan_projects(repositories),
            'healthcare_contributions': self._analyze_healthcare_contributions(repositories),
            'contribution_timeline': self._analyze_contribution_timeline(repositories),
            'technology_patterns': self._analyze_technology_patterns(repositories),
            'collaboration_patterns': self._analyze_collaboration_patterns(repositories),
            'impact_assessment': self._assess_contribution_impact(repositories),
            'strategic_recommendations': self._generate_strategic_recommendations(repositories)
        }
        
        # Save analysis
        output_dir = Path(output_path).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(output_path, 'w') as file:
            json.dump(analysis, file, indent=2, default=str)
        
        logger.info(f"Contribution analysis saved to {output_path}")
    
    def _generate_metadata(self) -> Dict[str, Any]:
        """Generate analysis metadata"""
        return {
            'generated_at': datetime.now(timezone.utc).isoformat(),
            'analysis_version': '1.0.0',
            'analyzer': 'My Command Centre Contribution Analyzer'
        }
    
    def _analyze_open_source_contributions(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze open source contribution patterns"""
        # Identify forked repositories (open source contributions)
        forked_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        
        # Analyze by target projects
        target_projects = self.open_source_config.get('target_projects', [])
        project_contributions = {}
        
        for project in target_projects:
            project_name = project.get('name', '')
            project_repo = project.get('repo', '')
            
            # Find matching repositories
            matching_repos = [
                repo for repo in forked_repos 
                if project_repo.lower() in repo.get('full_name', '').lower()
            ]
            
            if matching_repos:
                repo = matching_repos[0]  # Take the first match
                project_contributions[project_name] = {
                    'repository': repo['full_name'],
                    'last_activity': repo.get('updated_at'),
                    'commits_count': repo.get('recent_commits_count', 0),
                    'focus_areas': project.get('focus_areas', []),
                    'contribution_types': project.get('contribution_types', []),
                    'activity_score': self._calculate_activity_score(repo)
                }
        
        # Overall open source metrics
        total_forks = len(forked_repos)
        active_forks = len([repo for repo in forked_repos if self._is_recently_active(repo)])
        
        return {
            'total_forked_repositories': total_forks,
            'active_forked_repositories': active_forks,
            'activity_percentage': round(active_forks / total_forks * 100, 2) if total_forks > 0 else 0,
            'target_project_contributions': project_contributions,
            'contribution_methodology': self.open_source_config.get('methodology', {}),
            'monthly_contribution_goal': self.open_source_config.get('tracking', {}).get('contributions_per_month', 0)
        }
    
    def _analyze_sri_lankan_projects(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze contributions to Sri Lankan social good projects"""
        # Keywords to identify Sri Lankan projects
        sri_lankan_keywords = [
            'sri lanka', 'sinhala', 'tamil', 'colombo', 'hmis', 'eclk', 
            'icta', 'lsf', 'sahana', 'medicines', 'election', 'health'
        ]
        
        sri_lankan_repos = []
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in sri_lankan_keywords):
                sri_lankan_repos.append(repo)
        
        # Categorize by domain
        domains = {
            'healthcare': ['hmis', 'health', 'medical', 'hospital', 'medicines'],
            'governance': ['election', 'eclk', 'government', 'ministry'],
            'education': ['student', 'school', 'education', 'sis'],
            'language': ['sinhala', 'tamil', 'unicode', 'dictionary'],
            'disaster': ['sahana', 'disaster', 'emergency'],
            'transport': ['transport', 'bus', 'train', 'npsp']
        }
        
        domain_analysis = {}
        for domain, keywords in domains.items():
            domain_repos = []
            for repo in sri_lankan_repos:
                repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
                if any(keyword in repo_text for keyword in keywords):
                    domain_repos.append({
                        'name': repo['name'],
                        'description': repo.get('description', ''),
                        'last_activity': repo.get('updated_at'),
                        'stars': repo.get('stargazers_count', 0),
                        'forks': repo.get('forks_count', 0),
                        'is_fork': repo.get('is_fork', False)
                    })
            
            if domain_repos:
                domain_analysis[domain] = {
                    'repository_count': len(domain_repos),
                    'repositories': domain_repos,
                    'total_stars': sum(repo['stars'] for repo in domain_repos),
                    'contribution_opportunities': self._assess_domain_opportunities(domain, domain_repos)
                }
        
        return {
            'total_sri_lankan_projects': len(sri_lankan_repos),
            'domain_breakdown': domain_analysis,
            'social_impact_score': self._calculate_social_impact_score(sri_lankan_repos),
            'strategic_focus_alignment': self._assess_strategic_alignment(sri_lankan_repos)
        }
    
    def _analyze_healthcare_contributions(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze healthcare-specific contributions"""
        healthcare_keywords = [
            'health', 'medical', 'hospital', 'patient', 'clinical', 
            'pharmacy', 'medicine', 'diagnosis', 'treatment', 'hmis'
        ]
        
        healthcare_repos = []
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in healthcare_keywords):
                healthcare_repos.append(repo)
        
        # Analyze healthcare contribution patterns
        hmis_repos = [repo for repo in healthcare_repos if 'hmis' in repo.get('name', '').lower()]
        
        healthcare_analysis = {
            'total_healthcare_repositories': len(healthcare_repos),
            'hmis_contributions': {
                'repository_count': len(hmis_repos),
                'repositories': [
                    {
                        'name': repo['name'],
                        'last_activity': repo.get('updated_at'),
                        'commits': repo.get('recent_commits_count', 0),
                        'is_fork': repo.get('is_fork', False)
                    } for repo in hmis_repos
                ],
                'contribution_focus': self._analyze_hmis_contributions(hmis_repos)
            },
            'healthcare_domain_expertise': self._assess_healthcare_expertise(healthcare_repos),
            'patient_impact_potential': self._assess_patient_impact(healthcare_repos)
        }
        
        return healthcare_analysis
    
    def _analyze_contribution_timeline(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze contribution timeline patterns"""
        now = datetime.now(timezone.utc)
        
        # Analyze activity by time periods
        time_periods = {
            'last_7_days': 7,
            'last_30_days': 30,
            'last_90_days': 90,
            'last_year': 365
        }
        
        timeline_analysis = {}
        for period, days in time_periods.items():
            cutoff_date = now - timedelta(days=days)
            
            active_repos = []
            for repo in repositories:
                if repo.get('updated_at'):
                    try:
                        updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                        if updated >= cutoff_date:
                            active_repos.append(repo)
                    except:
                        continue
            
            timeline_analysis[period] = {
                'active_repositories': len(active_repos),
                'activity_percentage': round(len(active_repos) / len(repositories) * 100, 2) if repositories else 0,
                'contribution_types': self._categorize_recent_contributions(active_repos)
            }
        
        return timeline_analysis
    
    def _analyze_technology_patterns(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze technology usage patterns in contributions"""
        # Collect all languages
        all_languages = []
        language_contributions = defaultdict(list)
        
        for repo in repositories:
            primary_lang = repo.get('primary_language')
            if primary_lang:
                all_languages.append(primary_lang)
                language_contributions[primary_lang].append(repo['name'])
        
        language_counts = Counter(all_languages)
        
        # Analyze by contribution type
        open_source_languages = []
        personal_languages = []
        
        for repo in repositories:
            lang = repo.get('primary_language')
            if lang:
                if repo.get('is_fork', False):
                    open_source_languages.append(lang)
                else:
                    personal_languages.append(lang)
        
        return {
            'language_distribution': [
                {'language': lang, 'count': count} 
                for lang, count in language_counts.most_common(10)
            ],
            'open_source_languages': dict(Counter(open_source_languages).most_common(5)),
            'personal_project_languages': dict(Counter(personal_languages).most_common(5)),
            'technology_diversity_score': len(language_counts),
            'specialization_analysis': self._analyze_technology_specialization(language_counts)
        }
    
    def _analyze_collaboration_patterns(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze collaboration patterns"""
        solo_repos = [repo for repo in repositories if repo.get('contributors_count', 0) <= 1]
        collaborative_repos = [repo for repo in repositories if repo.get('contributors_count', 0) > 1]
        
        # Analyze collaboration by project type
        open_source_collaboration = [
            repo for repo in collaborative_repos if repo.get('is_fork', False)
        ]
        
        personal_collaboration = [
            repo for repo in collaborative_repos if not repo.get('is_fork', False)
        ]
        
        return {
            'total_repositories': len(repositories),
            'solo_repositories': len(solo_repos),
            'collaborative_repositories': len(collaborative_repos),
            'collaboration_percentage': round(len(collaborative_repos) / len(repositories) * 100, 2) if repositories else 0,
            'open_source_collaboration': {
                'count': len(open_source_collaboration),
                'projects': [repo['name'] for repo in open_source_collaboration[:5]]
            },
            'personal_project_collaboration': {
                'count': len(personal_collaboration),
                'projects': [repo['name'] for repo in personal_collaboration[:5]]
            },
            'community_engagement_score': self._calculate_community_engagement_score(repositories)
        }
    
    def _assess_contribution_impact(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Assess the impact of contributions"""
        # Calculate various impact metrics
        total_stars = sum(repo.get('stargazers_count', 0) for repo in repositories)
        total_forks = sum(repo.get('forks_count', 0) for repo in repositories)
        total_contributors = sum(repo.get('contributors_count', 0) for repo in repositories)
        
        # Identify high-impact repositories
        high_impact_repos = sorted(
            repositories,
            key=lambda r: (r.get('stargazers_count', 0) + r.get('forks_count', 0) * 2),
            reverse=True
        )[:5]
        
        # Assess social good impact
        social_good_repos = [
            repo for repo in repositories 
            if any(keyword in f"{repo.get('name', '')} {repo.get('description', '')}".lower() 
                  for keyword in ['health', 'education', 'government', 'humanitarian', 'disaster'])
        ]
        
        return {
            'engagement_metrics': {
                'total_stars': total_stars,
                'total_forks': total_forks,
                'total_contributors': total_contributors,
                'average_stars_per_repo': round(total_stars / len(repositories), 2) if repositories else 0
            },
            'high_impact_repositories': [
                {
                    'name': repo['name'],
                    'stars': repo.get('stargazers_count', 0),
                    'forks': repo.get('forks_count', 0),
                    'impact_score': repo.get('stargazers_count', 0) + repo.get('forks_count', 0) * 2
                } for repo in high_impact_repos
            ],
            'social_good_impact': {
                'repository_count': len(social_good_repos),
                'percentage_of_portfolio': round(len(social_good_repos) / len(repositories) * 100, 2) if repositories else 0,
                'focus_areas': self._identify_social_good_focus_areas(social_good_repos)
            },
            'professional_impact_score': self._calculate_professional_impact_score(repositories)
        }
    
    def _generate_strategic_recommendations(self, repositories: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Generate strategic recommendations based on analysis"""
        recommendations = []
        
        # Analyze current state
        forked_repos = [repo for repo in repositories if repo.get('is_fork', False)]
        active_forks = [repo for repo in forked_repos if self._is_recently_active(repo)]
        
        # Recommendation 1: Open source contribution frequency
        if len(active_forks) < len(forked_repos) * 0.5:
            recommendations.append({
                'category': 'Open Source Engagement',
                'priority': 'High',
                'title': 'Increase Active Open Source Contributions',
                'description': f'Only {len(active_forks)} of {len(forked_repos)} forked repositories show recent activity',
                'action_items': [
                    'Review stale forks for contribution opportunities',
                    'Sync with upstream repositories',
                    'Focus on 2-3 high-impact projects',
                    'Set monthly contribution targets'
                ]
            })
        
        # Recommendation 2: Sri Lankan project focus
        sri_lankan_repos = self._count_sri_lankan_repos(repositories)
        if sri_lankan_repos < len(repositories) * 0.3:
            recommendations.append({
                'category': 'Social Impact',
                'priority': 'Medium',
                'title': 'Expand Sri Lankan Social Good Contributions',
                'description': f'Only {sri_lankan_repos} repositories focus on Sri Lankan social good',
                'action_items': [
                    'Evaluate ECLK election system contributions',
                    'Explore OpenMRS localization opportunities',
                    'Contribute to DHIS2 health applications',
                    'Develop language technology tools'
                ]
            })
        
        # Recommendation 3: Healthcare specialization
        healthcare_repos = self._count_healthcare_repos(repositories)
        if healthcare_repos > 0:
            recommendations.append({
                'category': 'Domain Expertise',
                'priority': 'High',
                'title': 'Deepen Healthcare Technology Expertise',
                'description': f'Build on existing {healthcare_repos} healthcare-related repositories',
                'action_items': [
                    'Complete HMIS pharmacy module optimization',
                    'Begin OpenMRS community engagement',
                    'Develop healthcare interoperability expertise',
                    'Mentor new healthcare technology contributors'
                ]
            })
        
        return recommendations
    
    # Helper methods
    def _calculate_activity_score(self, repo: Dict[str, Any]) -> float:
        """Calculate activity score for a repository"""
        score = 0
        
        # Recent commits
        commits = repo.get('recent_commits_count', 0)
        score += min(commits * 2, 20)  # Max 20 points
        
        # Recent updates
        if repo.get('updated_at'):
            try:
                updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
                days_ago = (datetime.now(timezone.utc) - updated).days
                if days_ago <= 30:
                    score += 30
                elif days_ago <= 90:
                    score += 20
                elif days_ago <= 180:
                    score += 10
            except:
                pass
        
        # Community engagement
        score += min(repo.get('stargazers_count', 0), 25)
        score += min(repo.get('forks_count', 0) * 2, 25)
        
        return min(score, 100)
    
    def _is_recently_active(self, repo: Dict[str, Any]) -> bool:
        """Check if repository is recently active"""
        if not repo.get('updated_at'):
            return False
        
        try:
            updated = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
            return (datetime.now(timezone.utc) - updated).days <= 90
        except:
            return False
    
    def _count_sri_lankan_repos(self, repositories: List[Dict[str, Any]]) -> int:
        """Count repositories related to Sri Lankan projects"""
        keywords = ['sri lanka', 'sinhala', 'tamil', 'hmis', 'eclk', 'icta']
        count = 0
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in keywords):
                count += 1
        return count
    
    def _count_healthcare_repos(self, repositories: List[Dict[str, Any]]) -> int:
        """Count healthcare-related repositories"""
        keywords = ['health', 'medical', 'hospital', 'patient', 'hmis']
        count = 0
        for repo in repositories:
            repo_text = f"{repo.get('name', '')} {repo.get('description', '')}".lower()
            if any(keyword in repo_text for keyword in keywords):
                count += 1
        return count
    
    def _assess_domain_opportunities(self, domain: str, repos: List[Dict[str, Any]]) -> List[str]:
        """Assess contribution opportunities in a domain"""
        opportunities = []
        
        if domain == 'healthcare':
            opportunities = [
                'Localization for Sinhala/Tamil users',
                'Mobile applications for field workers',
                'Integration with national health systems',
                'Performance optimization for large datasets'
            ]
        elif domain == 'governance':
            opportunities = [
                'Transparency and accountability tools',
                'Citizen engagement applications',
                'Data visualization dashboards',
                'Security and audit improvements'
            ]
        elif domain == 'education':
            opportunities = [
                'Student information system improvements',
                'Teacher productivity tools',
                'Parent engagement applications',
                'Educational analytics platforms'
            ]
        
        return opportunities
    
    def _calculate_social_impact_score(self, repos: List[Dict[str, Any]]) -> float:
        """Calculate social impact score"""
        if not repos:
            return 0
        
        # Weight by project type and engagement
        score = 0
        for repo in repos:
            base_score = 10  # Base points for social good project
            
            # Add points for community engagement
            score += base_score + min(repo.get('stargazers_count', 0), 20)
            
            # Bonus for active projects
            if self._is_recently_active(repo):
                score += 15
        
        return round(score / len(repos), 2)
    
    def _assess_strategic_alignment(self, repos: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Assess alignment with strategic goals"""
        target_projects = self.open_source_config.get('target_projects', [])
        
        aligned_count = 0
        for repo in repos:
            for project in target_projects:
                if project.get('repo', '').lower() in repo.get('full_name', '').lower():
                    aligned_count += 1
                    break
        
        return {
            'aligned_projects': aligned_count,
            'total_projects': len(repos),
            'alignment_percentage': round(aligned_count / len(repos) * 100, 2) if repos else 0
        }
    
    def _analyze_hmis_contributions(self, hmis_repos: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze HMIS-specific contributions"""
        if not hmis_repos:
            return {}
        
        # This would be enhanced with actual commit analysis
        return {
            'focus_areas': ['pharmacy module', 'billing system', 'report optimization'],
            'contribution_types': ['bug fixes', 'performance improvements', 'code quality'],
            'impact_areas': ['inventory management', 'financial calculations', 'user experience']
        }
    
    def _assess_healthcare_expertise(self, repos: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Assess healthcare domain expertise level"""
        return {
            'expertise_level': 'Intermediate',
            'focus_areas': ['hospital management', 'pharmacy systems', 'health information systems'],
            'learning_opportunities': ['clinical workflows', 'health data standards', 'medical terminology']
        }
    
    def _assess_patient_impact(self, repos: List[Dict[str, Any]]) -> str:
        """Assess potential patient impact"""
        if any('hmis' in repo.get('name', '').lower() for repo in repos):
            return 'High - Direct impact on hospital operations and patient care'
        return 'Medium - Indirect impact through healthcare system improvements'
    
    def _categorize_recent_contributions(self, repos: List[Dict[str, Any]]) -> Dict[str, int]:
        """Categorize recent contributions by type"""
        categories = {
            'open_source': len([r for r in repos if r.get('is_fork', False)]),
            'personal_projects': len([r for r in repos if not r.get('is_fork', False)]),
            'healthcare': len([r for r in repos if 'health' in r.get('name', '').lower()]),
            'social_good': len([r for r in repos if any(
                keyword in f"{r.get('name', '')} {r.get('description', '')}".lower()
                for keyword in ['social', 'public', 'government', 'education']
            )])
        }
        return categories
    
    def _analyze_technology_specialization(self, language_counts: Counter) -> Dict[str, Any]:
        """Analyze technology specialization patterns"""
        total_repos = sum(language_counts.values())
        if not total_repos:
            return {}
        
        top_language = language_counts.most_common(1)[0] if language_counts else ('Unknown', 0)
        specialization_percentage = (top_language[1] / total_repos) * 100
        
        return {
            'primary_language': top_language[0],
            'specialization_percentage': round(specialization_percentage, 2),
            'specialization_level': 'High' if specialization_percentage > 50 else 'Moderate' if specialization_percentage > 30 else 'Diverse'
        }
    
    def _calculate_community_engagement_score(self, repos: List[Dict[str, Any]]) -> float:
        """Calculate community engagement score"""
        if not repos:
            return 0
        
        total_score = 0
        for repo in repos:
            score = 0
            score += min(repo.get('stargazers_count', 0), 50)
            score += min(repo.get('forks_count', 0) * 2, 50)
            score += min(repo.get('contributors_count', 0) * 5, 50)
            total_score += score
        
        return round(total_score / len(repos), 2)
    
    def _identify_social_good_focus_areas(self, repos: List[Dict[str, Any]]) -> List[str]:
        """Identify focus areas in social good projects"""
        focus_areas = []
        keywords_map = {
            'Healthcare': ['health', 'medical', 'hospital'],
            'Education': ['education', 'student', 'school'],
            'Governance': ['government', 'election', 'public'],
            'Humanitarian': ['disaster', 'emergency', 'relief']
        }
        
        for area, keywords in keywords_map.items():
            if any(
                any(keyword in f"{repo.get('name', '')} {repo.get('description', '')}".lower() 
                    for keyword in keywords)
                for repo in repos
            ):
                focus_areas.append(area)
        
        return focus_areas
    
    def _calculate_professional_impact_score(self, repos: List[Dict[str, Any]]) -> float:
        """Calculate professional impact score"""
        score = 0
        
        # Open source contributions
        open_source_repos = [r for r in repos if r.get('is_fork', False)]
        score += len(open_source_repos) * 10
        
        # Community recognition
        total_stars = sum(r.get('stargazers_count', 0) for r in repos)
        score += min(total_stars, 100)
        
        # Collaboration
        collaborative_repos = [r for r in repos if r.get('contributors_count', 0) > 1]
        score += len(collaborative_repos) * 5
        
        # Social impact
        social_repos = self._count_sri_lankan_repos(repos) + self._count_healthcare_repos(repos)
        score += social_repos * 15
        
        return round(score / len(repos), 2) if repos else 0

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Analyze contribution patterns')
    parser.add_argument('--repositories', required=True, help='Path to repositories JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output analysis JSON file')
    
    args = parser.parse_args()
    
    try:
        # Load repository data
        with open(args.repositories, 'r') as file:
            repo_data = json.load(file)
        repositories = repo_data.get('repositories', [])
        
        logger.info(f"Loaded {len(repositories)} repositories from {args.repositories}")
        
        # Analyze contributions
        analyzer = ContributionAnalyzer(args.config)
        analyzer.analyze_contribution_patterns(repositories, args.output)
        
        logger.info("Contribution analysis completed successfully")
        
    except Exception as e:
        logger.error(f"Contribution analysis failed: {e}")
        raise

if __name__ == '__main__':
    main()
