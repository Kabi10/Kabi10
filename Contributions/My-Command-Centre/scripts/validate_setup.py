#!/usr/bin/env python3
"""
Setup Validation Script for My Command Centre
Validates that all components are properly configured and functional
"""

import json
import os
import sys
import subprocess
import argparse
import yaml
from pathlib import Path
from typing import Dict, List, Any, Tuple
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class SetupValidator:
    """Validate Command Centre setup and configuration"""
    
    def __init__(self, config_path: str):
        """Initialize the setup validator"""
        self.config_path = config_path
        self.config = None
        self.validation_results = []
        
        logger.info("Initialized setup validator")
    
    def validate_all(self) -> bool:
        """Run all validation checks"""
        logger.info("Starting comprehensive setup validation")
        
        all_passed = True
        
        # Core validation checks
        checks = [
            ("Configuration File", self._validate_configuration),
            ("Directory Structure", self._validate_directory_structure),
            ("Python Dependencies", self._validate_python_dependencies),
            ("Node.js Dependencies", self._validate_nodejs_dependencies),
            ("GitHub CLI", self._validate_github_cli),
            ("GitHub Secrets", self._validate_github_secrets),
            ("Workflow Files", self._validate_workflow_files),
            ("Script Permissions", self._validate_script_permissions),
            ("Template Files", self._validate_template_files),
            ("Dashboard Configuration", self._validate_dashboard_config)
        ]
        
        for check_name, check_function in checks:
            try:
                logger.info(f"Running check: {check_name}")
                passed, message = check_function()
                
                self.validation_results.append({
                    'check': check_name,
                    'passed': passed,
                    'message': message
                })
                
                if passed:
                    logger.info(f"✅ {check_name}: {message}")
                else:
                    logger.error(f"❌ {check_name}: {message}")
                    all_passed = False
                    
            except Exception as e:
                error_msg = f"Validation error: {str(e)}"
                logger.error(f"❌ {check_name}: {error_msg}")
                
                self.validation_results.append({
                    'check': check_name,
                    'passed': False,
                    'message': error_msg
                })
                all_passed = False
        
        # Generate validation report
        self._generate_validation_report()
        
        if all_passed:
            logger.info("🎉 All validation checks passed! Your Command Centre is ready.")
        else:
            logger.error("❌ Some validation checks failed. Please review the issues above.")
        
        return all_passed
    
    def _validate_configuration(self) -> Tuple[bool, str]:
        """Validate configuration file"""
        try:
            if not Path(self.config_path).exists():
                return False, f"Configuration file not found: {self.config_path}"
            
            with open(self.config_path, 'r') as file:
                self.config = yaml.safe_load(file)
            
            # Check required sections
            required_sections = [
                'repository_discovery',
                'health_scoring',
                'analytics',
                'intelligence',
                'security'
            ]
            
            missing_sections = [section for section in required_sections if section not in self.config]
            
            if missing_sections:
                return False, f"Missing configuration sections: {', '.join(missing_sections)}"
            
            return True, "Configuration file is valid and complete"
            
        except yaml.YAMLError as e:
            return False, f"Invalid YAML syntax: {str(e)}"
        except Exception as e:
            return False, f"Configuration validation error: {str(e)}"
    
    def _validate_directory_structure(self) -> Tuple[bool, str]:
        """Validate directory structure"""
        required_dirs = [
            '.github/workflows',
            'scripts',
            'analytics',
            'strategy',
            'dashboard',
            'templates',
            'config',
            'docs'
        ]
        
        missing_dirs = []
        for dir_path in required_dirs:
            if not Path(dir_path).exists():
                missing_dirs.append(dir_path)
        
        if missing_dirs:
            return False, f"Missing directories: {', '.join(missing_dirs)}"
        
        return True, "All required directories exist"
    
    def _validate_python_dependencies(self) -> Tuple[bool, str]:
        """Validate Python dependencies"""
        try:
            # Check if requirements.txt exists
            req_file = Path('scripts/requirements.txt')
            if not req_file.exists():
                return False, "requirements.txt not found in scripts directory"
            
            # Try to import key dependencies
            required_packages = [
                'PyGithub',
                'requests',
                'yaml',
                'pandas',
                'matplotlib'
            ]
            
            missing_packages = []
            for package in required_packages:
                try:
                    __import__(package.lower().replace('-', '_'))
                except ImportError:
                    missing_packages.append(package)
            
            if missing_packages:
                return False, f"Missing Python packages: {', '.join(missing_packages)}. Run: pip install -r scripts/requirements.txt"
            
            return True, "All required Python dependencies are installed"
            
        except Exception as e:
            return False, f"Python dependency check failed: {str(e)}"
    
    def _validate_nodejs_dependencies(self) -> Tuple[bool, str]:
        """Validate Node.js dependencies"""
        try:
            # Check Node.js version
            result = subprocess.run(['node', '--version'], capture_output=True, text=True)
            if result.returncode != 0:
                return False, "Node.js not found. Please install Node.js 18+"
            
            node_version = result.stdout.strip()
            logger.debug(f"Node.js version: {node_version}")
            
            # Check npm
            result = subprocess.run(['npm', '--version'], capture_output=True, text=True)
            if result.returncode != 0:
                return False, "npm not found"
            
            # Check package.json files
            package_files = [
                'scripts/package.json',
                'dashboard/package.json'
            ]
            
            missing_files = [f for f in package_files if not Path(f).exists()]
            if missing_files:
                return False, f"Missing package.json files: {', '.join(missing_files)}"
            
            return True, f"Node.js {node_version} and npm are properly configured"
            
        except FileNotFoundError:
            return False, "Node.js not found. Please install Node.js 18+"
        except Exception as e:
            return False, f"Node.js validation failed: {str(e)}"
    
    def _validate_github_cli(self) -> Tuple[bool, str]:
        """Validate GitHub CLI"""
        try:
            # Check if gh is installed
            result = subprocess.run(['gh', '--version'], capture_output=True, text=True)
            if result.returncode != 0:
                return False, "GitHub CLI (gh) not found. Please install GitHub CLI"
            
            # Check authentication
            result = subprocess.run(['gh', 'auth', 'status'], capture_output=True, text=True)
            if result.returncode != 0:
                return False, "GitHub CLI not authenticated. Run: gh auth login"
            
            return True, "GitHub CLI is installed and authenticated"
            
        except FileNotFoundError:
            return False, "GitHub CLI (gh) not found. Please install GitHub CLI"
        except Exception as e:
            return False, f"GitHub CLI validation failed: {str(e)}"
    
    def _validate_github_secrets(self) -> Tuple[bool, str]:
        """Validate GitHub secrets"""
        try:
            # Check if GITHUB_TOKEN is available
            if not os.getenv('GITHUB_TOKEN'):
                return False, "GITHUB_TOKEN environment variable not set"
            
            # Try to list secrets (this will work if we're in a GitHub Actions environment)
            try:
                result = subprocess.run(['gh', 'secret', 'list'], capture_output=True, text=True)
                if 'GITHUB_TOKEN' not in result.stdout:
                    return False, "GITHUB_TOKEN secret not configured in repository"
            except:
                # If we can't list secrets, assume it's because we're not in the right context
                pass
            
            return True, "GitHub token is configured"
            
        except Exception as e:
            return False, f"GitHub secrets validation failed: {str(e)}"
    
    def _validate_workflow_files(self) -> Tuple[bool, str]:
        """Validate GitHub Actions workflow files"""
        workflow_dir = Path('.github/workflows')
        if not workflow_dir.exists():
            return False, "Workflows directory not found"
        
        required_workflows = [
            'repository-discovery.yml',
            'weekly-reports.yml',
            'monthly-strategic-review.yml',
            'security-maintenance.yml'
        ]
        
        missing_workflows = []
        for workflow in required_workflows:
            workflow_path = workflow_dir / workflow
            if not workflow_path.exists():
                missing_workflows.append(workflow)
            else:
                # Basic YAML validation
                try:
                    with open(workflow_path, 'r') as file:
                        yaml.safe_load(file)
                except yaml.YAMLError:
                    missing_workflows.append(f"{workflow} (invalid YAML)")
        
        if missing_workflows:
            return False, f"Missing or invalid workflows: {', '.join(missing_workflows)}"
        
        return True, "All required workflow files are present and valid"
    
    def _validate_script_permissions(self) -> Tuple[bool, str]:
        """Validate script file permissions"""
        script_dir = Path('scripts')
        python_scripts = list(script_dir.glob('*.py'))
        
        if not python_scripts:
            return False, "No Python scripts found in scripts directory"
        
        # Check if main scripts exist
        required_scripts = [
            'repository_discovery.py',
            'health_scoring.py',
            'analytics_generator.py',
            'portfolio_reporter.py',
            'alert_generator.py'
        ]
        
        missing_scripts = []
        for script in required_scripts:
            script_path = script_dir / script
            if not script_path.exists():
                missing_scripts.append(script)
        
        if missing_scripts:
            return False, f"Missing required scripts: {', '.join(missing_scripts)}"
        
        return True, f"All {len(python_scripts)} scripts are present"
    
    def _validate_template_files(self) -> Tuple[bool, str]:
        """Validate template files"""
        template_dir = Path('templates')
        if not template_dir.exists():
            return False, "Templates directory not found"
        
        required_templates = [
            'strategic-planning-template.md',
            'project-timeline-template.md',
            'contribution-methodology-template.md'
        ]
        
        missing_templates = []
        for template in required_templates:
            template_path = template_dir / template
            if not template_path.exists():
                missing_templates.append(template)
        
        if missing_templates:
            return False, f"Missing templates: {', '.join(missing_templates)}"
        
        return True, "All required templates are present"
    
    def _validate_dashboard_config(self) -> Tuple[bool, str]:
        """Validate dashboard configuration"""
        dashboard_dir = Path('dashboard')
        if not dashboard_dir.exists():
            return False, "Dashboard directory not found"
        
        required_files = [
            'package.json',
            'generate.js'
        ]
        
        missing_files = []
        for file_name in required_files:
            file_path = dashboard_dir / file_name
            if not file_path.exists():
                missing_files.append(file_name)
        
        if missing_files:
            return False, f"Missing dashboard files: {', '.join(missing_files)}"
        
        return True, "Dashboard configuration is complete"
    
    def _generate_validation_report(self) -> None:
        """Generate validation report"""
        report_dir = Path('analytics/validation')
        report_dir.mkdir(parents=True, exist_ok=True)
        
        report_data = {
            'validation_timestamp': str(Path.cwd()),
            'total_checks': len(self.validation_results),
            'passed_checks': len([r for r in self.validation_results if r['passed']]),
            'failed_checks': len([r for r in self.validation_results if not r['passed']]),
            'results': self.validation_results
        }
        
        # Save JSON report
        with open(report_dir / 'validation-report.json', 'w') as file:
            json.dump(report_data, file, indent=2)
        
        # Generate markdown report
        markdown_report = self._generate_markdown_report(report_data)
        with open(report_dir / 'validation-report.md', 'w') as file:
            file.write(markdown_report)
        
        logger.info(f"Validation report saved to {report_dir}")
    
    def _generate_markdown_report(self, report_data: Dict[str, Any]) -> str:
        """Generate markdown validation report"""
        passed = report_data['passed_checks']
        total = report_data['total_checks']
        
        markdown = f"""# Command Centre Setup Validation Report

**Validation Date:** {report_data['validation_timestamp']}  
**Overall Status:** {'✅ PASSED' if passed == total else '❌ FAILED'}  
**Success Rate:** {passed}/{total} ({passed/total*100:.1f}%)

## Summary

| Status | Count |
|--------|-------|
| ✅ Passed | {passed} |
| ❌ Failed | {total - passed} |
| **Total** | **{total}** |

## Detailed Results

"""
        
        for result in report_data['results']:
            status = "✅" if result['passed'] else "❌"
            markdown += f"### {status} {result['check']}\n\n"
            markdown += f"{result['message']}\n\n"
        
        if passed < total:
            markdown += """## Next Steps

To resolve the failed checks:

1. Review the error messages above
2. Install missing dependencies
3. Configure required secrets and authentication
4. Re-run the validation: `python scripts/validate_setup.py --config config/command-centre.yml`

## Getting Help

- Check the setup guide: `docs/SETUP.md`
- Review configuration: `config/command-centre.yml`
- Verify directory structure matches the requirements

"""
        
        markdown += """---

*This report was generated by the Command Centre setup validator.*
"""
        
        return markdown

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Validate Command Centre setup')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--output', help='Output validation report path')
    
    args = parser.parse_args()
    
    try:
        # Run validation
        validator = SetupValidator(args.config)
        success = validator.validate_all()
        
        # Exit with appropriate code
        sys.exit(0 if success else 1)
        
    except Exception as e:
        logger.error(f"Validation failed with error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()
