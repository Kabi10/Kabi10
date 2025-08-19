#!/usr/bin/env python3
"""
Repository Health Scoring Script for My Command Centre
Calculates health scores for repositories based on multiple factors
"""

import json
import argparse
import yaml
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
import statistics
import re

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class RepositoryHealthScorer:
    """Calculate health scores for repositories"""
    
    def __init__(self, config_path: str):
        """Initialize the health scoring system"""
        self.config = self._load_config(config_path)
        self.scoring_config = self.config.get('health_scoring', {})
        self.weights = self.scoring_config.get('weights', {})
        self.thresholds = self.scoring_config.get('thresholds', {})
        
        logger.info("Initialized repository health scoring system")
    
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
    
    def calculate_health_scores(self, repositories: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Calculate health scores for all repositories"""
        logger.info(f"Calculating health scores for {len(repositories)} repositories")
        
        health_scores = {}
        
        for repo in repositories:
            try:
                score_data = self._calculate_repository_health(repo)
                health_scores[repo['full_name']] = score_data
                logger.debug(f"Calculated health score for {repo['name']}: {score_data['overall_score']}")
            except Exception as e:
                logger.error(f"Failed to calculate health score for {repo['name']}: {e}")
                health_scores[repo['full_name']] = {
                    'overall_score': 0,
                    'health_grade': 'unknown',
                    'error': str(e),
                    'calculated_at': datetime.now(timezone.utc).isoformat()
                }
        
        return {
            'scoring_metadata': {
                'timestamp': datetime.now(timezone.utc).isoformat(),
                'total_repositories': len(repositories),
                'scoring_version': '1.0.0',
                'weights_used': self.weights,
                'thresholds_used': self.thresholds
            },
            'health_scores': health_scores
        }
    
    def _calculate_repository_health(self, repo: Dict[str, Any]) -> Dict[str, Any]:
        """Calculate health score for a single repository"""
        scores = {}
        
        # Calculate individual component scores
        scores['commit_frequency'] = self._score_commit_frequency(repo)
        scores['documentation_quality'] = self._score_documentation_quality(repo)
        scores['issue_response_time'] = self._score_issue_response_time(repo)
        scores['community_engagement'] = self._score_community_engagement(repo)
        scores['code_quality'] = self._score_code_quality(repo)
        scores['maintenance'] = self._score_maintenance(repo)
        
        # Calculate weighted overall score
        overall_score = 0
        for component, score in scores.items():
            weight = self.weights.get(component, 0) / 100.0
            overall_score += score * weight
        
        # Determine health grade
        health_grade = self._determine_health_grade(overall_score)
        
        return {
            'overall_score': round(overall_score, 2),
            'health_grade': health_grade,
            'component_scores': scores,
            'recommendations': self._generate_recommendations(scores, repo),
            'calculated_at': datetime.now(timezone.utc).isoformat()
        }
    
    def _score_commit_frequency(self, repo: Dict[str, Any]) -> float:
        """Score based on recent commit activity"""
        try:
            if 'recent_commits_count' not in repo:
                return 50.0  # Default score if data unavailable
            
            commits_count = repo.get('recent_commits_count', 0)
            
            # Score based on recent activity (last 50 commits analyzed)
            if commits_count >= 40:
                return 100.0
            elif commits_count >= 20:
                return 80.0
            elif commits_count >= 10:
                return 60.0
            elif commits_count >= 5:
                return 40.0
            elif commits_count > 0:
                return 20.0
            else:
                return 0.0
                
        except Exception as e:
            logger.warning(f"Error scoring commit frequency for {repo['name']}: {e}")
            return 50.0
    
    def _score_documentation_quality(self, repo: Dict[str, Any]) -> float:
        """Score based on documentation quality indicators"""
        try:
            score = 0.0
            
            # Check if description exists and is meaningful
            description = repo.get('description', '')
            if description and len(description) > 20:
                score += 30.0
            elif description:
                score += 15.0
            
            # Check for topics/tags
            topics = repo.get('topics', [])
            if len(topics) >= 3:
                score += 25.0
            elif len(topics) >= 1:
                score += 15.0
            
            # Assume README exists if repository is well-maintained
            # (This could be enhanced with actual file checking via API)
            if repo.get('stargazers_count', 0) > 0 or repo.get('forks_count', 0) > 0:
                score += 25.0
            
            # Check for releases (indicates good versioning practices)
            if repo.get('releases_count', 0) > 0:
                score += 20.0
            
            return min(score, 100.0)
            
        except Exception as e:
            logger.warning(f"Error scoring documentation quality for {repo['name']}: {e}")
            return 50.0
    
    def _score_issue_response_time(self, repo: Dict[str, Any]) -> float:
        """Score based on issue response and resolution times"""
        try:
            recent_issues = repo.get('recent_issues', [])
            if not recent_issues:
                return 70.0  # Neutral score if no issues
            
            response_scores = []
            
            for issue in recent_issues:
                created_at = datetime.fromisoformat(issue['created_at'].replace('Z', '+00:00'))
                updated_at = datetime.fromisoformat(issue['updated_at'].replace('Z', '+00:00'))
                
                # Calculate response time in days
                response_time = (updated_at - created_at).days
                
                # Score based on response time
                if response_time <= 1:
                    response_scores.append(100.0)
                elif response_time <= 7:
                    response_scores.append(80.0)
                elif response_time <= 30:
                    response_scores.append(60.0)
                elif response_time <= 90:
                    response_scores.append(40.0)
                else:
                    response_scores.append(20.0)
            
            return statistics.mean(response_scores) if response_scores else 70.0
            
        except Exception as e:
            logger.warning(f"Error scoring issue response time for {repo['name']}: {e}")
            return 70.0
    
    def _score_community_engagement(self, repo: Dict[str, Any]) -> float:
        """Score based on community engagement metrics"""
        try:
            score = 0.0
            
            # Stars scoring
            stars = repo.get('stargazers_count', 0)
            if stars >= 100:
                score += 40.0
            elif stars >= 50:
                score += 30.0
            elif stars >= 10:
                score += 20.0
            elif stars >= 1:
                score += 10.0
            
            # Forks scoring
            forks = repo.get('forks_count', 0)
            if forks >= 20:
                score += 30.0
            elif forks >= 10:
                score += 20.0
            elif forks >= 5:
                score += 15.0
            elif forks >= 1:
                score += 10.0
            
            # Contributors scoring
            contributors = repo.get('contributors_count', 0)
            if contributors >= 10:
                score += 30.0
            elif contributors >= 5:
                score += 20.0
            elif contributors >= 2:
                score += 15.0
            elif contributors >= 1:
                score += 10.0
            
            return min(score, 100.0)
            
        except Exception as e:
            logger.warning(f"Error scoring community engagement for {repo['name']}: {e}")
            return 50.0
    
    def _score_code_quality(self, repo: Dict[str, Any]) -> float:
        """Score based on code quality indicators"""
        try:
            score = 50.0  # Base score
            
            # Language diversity (indicates good architecture)
            languages = repo.get('languages', {})
            if len(languages) >= 3:
                score += 20.0
            elif len(languages) >= 2:
                score += 10.0
            
            # Repository size (not too small, not too large)
            size = repo.get('size', 0)
            if 100 <= size <= 10000:  # KB
                score += 20.0
            elif 10 <= size <= 50000:
                score += 10.0
            
            # Recent activity indicates maintained code
            if repo.get('recent_commits_count', 0) > 0:
                score += 10.0
            
            return min(score, 100.0)
            
        except Exception as e:
            logger.warning(f"Error scoring code quality for {repo['name']}: {e}")
            return 50.0
    
    def _score_maintenance(self, repo: Dict[str, Any]) -> float:
        """Score based on maintenance indicators"""
        try:
            score = 0.0
            
            # Recent updates
            updated_at = datetime.fromisoformat(repo['updated_at'].replace('Z', '+00:00'))
            days_since_update = (datetime.now(timezone.utc) - updated_at).days
            
            if days_since_update <= 7:
                score += 40.0
            elif days_since_update <= 30:
                score += 30.0
            elif days_since_update <= 90:
                score += 20.0
            elif days_since_update <= 365:
                score += 10.0
            
            # Not archived
            if not repo.get('is_archived', False):
                score += 30.0
            
            # Has releases (indicates version management)
            if repo.get('releases_count', 0) > 0:
                score += 20.0
            
            # Low open issues relative to activity
            open_issues = repo.get('open_issues_count', 0)
            if open_issues <= 5:
                score += 10.0
            elif open_issues <= 20:
                score += 5.0
            
            return min(score, 100.0)
            
        except Exception as e:
            logger.warning(f"Error scoring maintenance for {repo['name']}: {e}")
            return 50.0
    
    def _determine_health_grade(self, score: float) -> str:
        """Determine health grade based on score"""
        if score >= self.thresholds.get('excellent', 85):
            return 'excellent'
        elif score >= self.thresholds.get('good', 70):
            return 'good'
        elif score >= self.thresholds.get('fair', 50):
            return 'fair'
        else:
            return 'poor'
    
    def _generate_recommendations(self, scores: Dict[str, float], repo: Dict[str, Any]) -> List[str]:
        """Generate improvement recommendations based on scores"""
        recommendations = []
        
        if scores['commit_frequency'] < 50:
            recommendations.append("Increase commit frequency to show active development")
        
        if scores['documentation_quality'] < 60:
            recommendations.append("Improve documentation: add detailed README, meaningful description, and topics")
        
        if scores['issue_response_time'] < 60:
            recommendations.append("Improve issue response time and resolution")
        
        if scores['community_engagement'] < 40:
            recommendations.append("Increase community engagement: promote repository, encourage contributions")
        
        if scores['maintenance'] < 60:
            recommendations.append("Improve maintenance: regular updates, manage open issues, create releases")
        
        return recommendations

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Calculate repository health scores')
    parser.add_argument('--input', required=True, help='Input repositories JSON file')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', required=True, help='Output health scores JSON file')
    
    args = parser.parse_args()
    
    try:
        # Load repository data
        with open(args.input, 'r') as file:
            repo_data = json.load(file)
        
        repositories = repo_data.get('repositories', [])
        logger.info(f"Loaded {len(repositories)} repositories from {args.input}")
        
        # Initialize health scorer
        scorer = RepositoryHealthScorer(args.config)
        
        # Calculate health scores
        health_scores = scorer.calculate_health_scores(repositories)
        
        # Save results
        output_dir = Path(args.output).parent
        output_dir.mkdir(parents=True, exist_ok=True)
        
        with open(args.output, 'w') as file:
            json.dump(health_scores, file, indent=2, default=str)
        
        logger.info(f"Saved health scores to {args.output}")
        
        # Output summary
        scores = health_scores['health_scores']
        avg_score = sum(s.get('overall_score', 0) for s in scores.values()) / len(scores) if scores else 0
        print(f"Average health score: {avg_score:.2f}")
        
    except Exception as e:
        logger.error(f"Health scoring failed: {e}")
        raise

if __name__ == '__main__':
    main()
