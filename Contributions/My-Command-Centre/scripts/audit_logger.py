#!/usr/bin/env python3
"""
Audit Logger Script for My Command Centre
Logs all automation activities for security and compliance tracking
"""

import json
import argparse
import yaml
import os
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Dict, List, Any, Optional
import logging
import hashlib
import platform

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AuditLogger:
    """Audit logging system for Command Centre activities"""
    
    def __init__(self, config_path: str):
        """Initialize the audit logger"""
        self.config = self._load_config(config_path)
        self.security_config = self.config.get('security', {})
        self.audit_config = self.security_config.get('audit_logging', {})
        
        # Set up audit directory
        self.audit_dir = Path('analytics/audit')
        self.audit_dir.mkdir(parents=True, exist_ok=True)
        
        logger.info("Initialized audit logger")
    
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
    
    def log_workflow_execution(self, workflow_name: str, run_id: str, 
                             status: str, **kwargs) -> None:
        """Log workflow execution details"""
        if not self.audit_config.get('enable', True):
            return
        
        timestamp = datetime.now(timezone.utc)
        
        # Create audit entry
        audit_entry = {
            'timestamp': timestamp.isoformat(),
            'event_type': 'workflow_execution',
            'workflow_name': workflow_name,
            'run_id': run_id,
            'status': status,
            'environment': self._get_environment_info(),
            'metadata': kwargs,
            'audit_id': self._generate_audit_id(workflow_name, run_id, timestamp)
        }
        
        # Add security context
        audit_entry['security_context'] = self._get_security_context()
        
        # Log to file
        self._write_audit_log(audit_entry)
        
        logger.info(f"Logged workflow execution: {workflow_name} ({status})")
    
    def log_data_access(self, operation: str, resource: str, 
                       user_context: Optional[str] = None, **kwargs) -> None:
        """Log data access operations"""
        if not self.audit_config.get('enable', True):
            return
        
        timestamp = datetime.now(timezone.utc)
        
        audit_entry = {
            'timestamp': timestamp.isoformat(),
            'event_type': 'data_access',
            'operation': operation,
            'resource': resource,
            'user_context': user_context or 'system',
            'environment': self._get_environment_info(),
            'metadata': kwargs,
            'audit_id': self._generate_audit_id(operation, resource, timestamp)
        }
        
        audit_entry['security_context'] = self._get_security_context()
        
        self._write_audit_log(audit_entry)
        
        logger.debug(f"Logged data access: {operation} on {resource}")
    
    def log_security_event(self, event_type: str, severity: str, 
                          description: str, **kwargs) -> None:
        """Log security-related events"""
        timestamp = datetime.now(timezone.utc)
        
        audit_entry = {
            'timestamp': timestamp.isoformat(),
            'event_type': 'security_event',
            'security_event_type': event_type,
            'severity': severity,
            'description': description,
            'environment': self._get_environment_info(),
            'metadata': kwargs,
            'audit_id': self._generate_audit_id(event_type, severity, timestamp)
        }
        
        audit_entry['security_context'] = self._get_security_context()
        
        self._write_audit_log(audit_entry)
        
        logger.warning(f"Logged security event: {event_type} ({severity})")
    
    def log_configuration_change(self, component: str, change_type: str, 
                                old_value: Any, new_value: Any, **kwargs) -> None:
        """Log configuration changes"""
        if not self.audit_config.get('enable', True):
            return
        
        timestamp = datetime.now(timezone.utc)
        
        audit_entry = {
            'timestamp': timestamp.isoformat(),
            'event_type': 'configuration_change',
            'component': component,
            'change_type': change_type,
            'old_value': str(old_value),
            'new_value': str(new_value),
            'environment': self._get_environment_info(),
            'metadata': kwargs,
            'audit_id': self._generate_audit_id(component, change_type, timestamp)
        }
        
        audit_entry['security_context'] = self._get_security_context()
        
        self._write_audit_log(audit_entry)
        
        logger.info(f"Logged configuration change: {component} ({change_type})")
    
    def log_api_call(self, api_endpoint: str, method: str, status_code: int, 
                    response_size: Optional[int] = None, **kwargs) -> None:
        """Log API calls"""
        if not self.audit_config.get('enable', True):
            return
        
        timestamp = datetime.now(timezone.utc)
        
        audit_entry = {
            'timestamp': timestamp.isoformat(),
            'event_type': 'api_call',
            'api_endpoint': api_endpoint,
            'method': method,
            'status_code': status_code,
            'response_size': response_size,
            'environment': self._get_environment_info(),
            'metadata': kwargs,
            'audit_id': self._generate_audit_id(api_endpoint, method, timestamp)
        }
        
        audit_entry['security_context'] = self._get_security_context()
        
        self._write_audit_log(audit_entry)
        
        logger.debug(f"Logged API call: {method} {api_endpoint} ({status_code})")
    
    def _generate_audit_id(self, *components) -> str:
        """Generate unique audit ID"""
        content = ''.join(str(c) for c in components)
        return hashlib.sha256(content.encode()).hexdigest()[:16]
    
    def _get_environment_info(self) -> Dict[str, Any]:
        """Get environment information"""
        return {
            'platform': platform.platform(),
            'python_version': platform.python_version(),
            'hostname': platform.node(),
            'working_directory': str(Path.cwd()),
            'github_actions': os.getenv('GITHUB_ACTIONS') == 'true',
            'github_repository': os.getenv('GITHUB_REPOSITORY'),
            'github_ref': os.getenv('GITHUB_REF'),
            'github_sha': os.getenv('GITHUB_SHA')
        }
    
    def _get_security_context(self) -> Dict[str, Any]:
        """Get security context information"""
        return {
            'user_agent': 'My-Command-Centre/1.0',
            'session_id': self._generate_session_id(),
            'ip_address': self._get_ip_address(),
            'authentication_method': 'github_token',
            'permissions': self._get_permissions_context()
        }
    
    def _generate_session_id(self) -> str:
        """Generate session ID for tracking"""
        # Use GitHub run ID if available, otherwise generate one
        run_id = os.getenv('GITHUB_RUN_ID')
        if run_id:
            return f"gh-{run_id}"
        
        # Generate based on timestamp and process
        timestamp = datetime.now(timezone.utc).timestamp()
        pid = os.getpid()
        return hashlib.md5(f"{timestamp}-{pid}".encode()).hexdigest()[:12]
    
    def _get_ip_address(self) -> str:
        """Get IP address (placeholder for GitHub Actions)"""
        if os.getenv('GITHUB_ACTIONS') == 'true':
            return 'github-actions'
        return 'localhost'
    
    def _get_permissions_context(self) -> Dict[str, Any]:
        """Get permissions context"""
        return {
            'repository_access': 'read-write',
            'issues_access': 'write',
            'pages_access': 'write',
            'secrets_access': 'read'
        }
    
    def _write_audit_log(self, audit_entry: Dict[str, Any]) -> None:
        """Write audit entry to log file"""
        try:
            # Determine log file based on date
            date_str = datetime.now(timezone.utc).strftime('%Y-%m-%d')
            log_file = self.audit_dir / f"audit-{date_str}.jsonl"
            
            # Write as JSON Lines format
            with open(log_file, 'a') as file:
                json.dump(audit_entry, file, default=str)
                file.write('\n')
            
            # Also write to main audit log
            main_log = self.audit_dir / "audit.jsonl"
            with open(main_log, 'a') as file:
                json.dump(audit_entry, file, default=str)
                file.write('\n')
            
            # Rotate logs if needed
            self._rotate_logs_if_needed()
            
        except Exception as e:
            logger.error(f"Failed to write audit log: {e}")
    
    def _rotate_logs_if_needed(self) -> None:
        """Rotate audit logs based on retention policy"""
        try:
            retention_days = self.audit_config.get('retention_days', 365)
            cutoff_date = datetime.now(timezone.utc) - timedelta(days=retention_days)
            
            # Find old log files
            for log_file in self.audit_dir.glob("audit-*.jsonl"):
                try:
                    # Extract date from filename
                    date_part = log_file.stem.split('-', 1)[1]
                    file_date = datetime.strptime(date_part, '%Y-%m-%d').replace(tzinfo=timezone.utc)
                    
                    if file_date < cutoff_date:
                        log_file.unlink()
                        logger.info(f"Rotated old audit log: {log_file}")
                        
                except (ValueError, IndexError):
                    # Skip files that don't match expected format
                    continue
                    
        except Exception as e:
            logger.warning(f"Log rotation failed: {e}")
    
    def generate_audit_report(self, start_date: Optional[str] = None, 
                            end_date: Optional[str] = None) -> Dict[str, Any]:
        """Generate audit report for specified date range"""
        try:
            # Default to last 30 days if no dates specified
            if not end_date:
                end_date = datetime.now(timezone.utc).strftime('%Y-%m-%d')
            if not start_date:
                start_dt = datetime.now(timezone.utc) - timedelta(days=30)
                start_date = start_dt.strftime('%Y-%m-%d')
            
            # Read audit logs
            audit_entries = []
            for log_file in self.audit_dir.glob("audit-*.jsonl"):
                try:
                    date_part = log_file.stem.split('-', 1)[1]
                    if start_date <= date_part <= end_date:
                        with open(log_file, 'r') as file:
                            for line in file:
                                if line.strip():
                                    audit_entries.append(json.loads(line))
                except (ValueError, IndexError, json.JSONDecodeError):
                    continue
            
            # Generate report
            report = {
                'report_period': {
                    'start_date': start_date,
                    'end_date': end_date
                },
                'total_events': len(audit_entries),
                'event_summary': self._summarize_events(audit_entries),
                'security_events': self._filter_security_events(audit_entries),
                'workflow_executions': self._summarize_workflows(audit_entries),
                'api_activity': self._summarize_api_activity(audit_entries),
                'generated_at': datetime.now(timezone.utc).isoformat()
            }
            
            return report
            
        except Exception as e:
            logger.error(f"Failed to generate audit report: {e}")
            return {'error': str(e)}
    
    def _summarize_events(self, audit_entries: List[Dict[str, Any]]) -> Dict[str, int]:
        """Summarize events by type"""
        from collections import Counter
        event_types = [entry.get('event_type', 'unknown') for entry in audit_entries]
        return dict(Counter(event_types))
    
    def _filter_security_events(self, audit_entries: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Filter and return security events"""
        return [entry for entry in audit_entries if entry.get('event_type') == 'security_event']
    
    def _summarize_workflows(self, audit_entries: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Summarize workflow executions"""
        workflow_entries = [entry for entry in audit_entries if entry.get('event_type') == 'workflow_execution']
        
        from collections import Counter, defaultdict
        
        workflows = Counter(entry.get('workflow_name', 'unknown') for entry in workflow_entries)
        statuses = Counter(entry.get('status', 'unknown') for entry in workflow_entries)
        
        return {
            'total_executions': len(workflow_entries),
            'by_workflow': dict(workflows),
            'by_status': dict(statuses)
        }
    
    def _summarize_api_activity(self, audit_entries: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Summarize API activity"""
        api_entries = [entry for entry in audit_entries if entry.get('event_type') == 'api_call']
        
        from collections import Counter
        
        endpoints = Counter(entry.get('api_endpoint', 'unknown') for entry in api_entries)
        methods = Counter(entry.get('method', 'unknown') for entry in api_entries)
        status_codes = Counter(entry.get('status_code', 'unknown') for entry in api_entries)
        
        return {
            'total_calls': len(api_entries),
            'by_endpoint': dict(endpoints.most_common(10)),
            'by_method': dict(methods),
            'by_status_code': dict(status_codes)
        }

def main():
    """Main function for command-line execution"""
    parser = argparse.ArgumentParser(description='Log audit events for Command Centre')
    parser.add_argument('--workflow-name', help='Workflow name for workflow execution logging')
    parser.add_argument('--run-id', help='Run ID for workflow execution logging')
    parser.add_argument('--status', help='Status for workflow execution logging')
    parser.add_argument('--repositories-count', type=int, help='Number of repositories processed')
    parser.add_argument('--config', required=True, help='Path to configuration file')
    parser.add_argument('--generate-report', action='store_true', help='Generate audit report')
    parser.add_argument('--start-date', help='Start date for report (YYYY-MM-DD)')
    parser.add_argument('--end-date', help='End date for report (YYYY-MM-DD)')
    
    args = parser.parse_args()
    
    try:
        # Initialize audit logger
        audit_logger = AuditLogger(args.config)
        
        if args.generate_report:
            # Generate audit report
            report = audit_logger.generate_audit_report(args.start_date, args.end_date)
            
            # Save report
            report_file = audit_logger.audit_dir / f"audit-report-{datetime.now(timezone.utc).strftime('%Y%m%d')}.json"
            with open(report_file, 'w') as file:
                json.dump(report, file, indent=2, default=str)
            
            logger.info(f"Audit report generated: {report_file}")
            print(f"Total events: {report.get('total_events', 0)}")
            
        elif args.workflow_name and args.run_id and args.status:
            # Log workflow execution
            kwargs = {}
            if args.repositories_count is not None:
                kwargs['repositories_count'] = args.repositories_count
            
            audit_logger.log_workflow_execution(
                args.workflow_name, 
                args.run_id, 
                args.status,
                **kwargs
            )
            
        else:
            logger.error("Either --generate-report or workflow logging parameters are required")
            parser.print_help()
        
    except Exception as e:
        logger.error(f"Audit logging failed: {e}")
        raise

if __name__ == '__main__':
    main()
