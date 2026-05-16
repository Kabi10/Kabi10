#!/usr/bin/env python3
"""
Custom HTTP Server with CSP Headers for Kabilan.io Portfolio
Serves static files with proper Content Security Policy headers
"""

import http.server
import socketserver
import os
from urllib.parse import unquote

PORT = 8000

class CSPHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Custom HTTP request handler with CSP headers"""
    
    def end_headers(self):
        """Add security headers before ending headers (CSP DISABLED)"""
        
        # CSP DISABLED - Content Security Policy has been disabled to resolve persistent issues
        # including HTTP 501 errors, connection storms, and CSP violation warnings.
        # The CSP system was causing more problems than it solved in the current configuration.
        # Other security headers remain active to maintain baseline security protections.
        #
        # Original CSP policy (commented out):
        # csp_policy = (
        #     "default-src 'self'; "
        #     "script-src 'self' blob:; "
        #     "style-src 'self' 'unsafe-inline' https://fonts.cdnfonts.com; "
        #     "img-src 'self' data: https: blob:; "
        #     "font-src 'self' https://fonts.cdnfonts.com https://fonts.gstatic.com; "
        #     "connect-src 'self' blob:; "
        #     "media-src 'self' blob:; "
        #     "object-src 'none'; "
        #     "base-uri 'self'; "
        #     "form-action 'self'; "
        #     "frame-ancestors 'none'; "
        #     "worker-src 'self' blob:; "
        #     "upgrade-insecure-requests"
        # )
        # self.send_header('Content-Security-Policy', csp_policy)
        
        # Additional security headers (KEPT ACTIVE)
        self.send_header('X-Content-Type-Options', 'nosniff')
        self.send_header('X-Frame-Options', 'DENY')
        self.send_header('X-XSS-Protection', '1; mode=block')
        self.send_header('Referrer-Policy', 'strict-origin-when-cross-origin')
        self.send_header('Permissions-Policy', 'geolocation=(), microphone=(), camera=()')
        
        # CORS headers (if needed for local development)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        
        # Cache control for development
        if self.path.endswith(('.html', '.json')):
            self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        else:
            self.send_header('Cache-Control', 'public, max-age=3600')
        
        super().end_headers()
    
    def do_GET(self):
        """Handle GET requests with proper MIME types"""
        # Decode URL
        path = unquote(self.path)

        # Serve index.html for root path
        if path == '/':
            self.path = '/index.html'

        return super().do_GET()

    def do_POST(self):
        """Handle POST requests (primarily for CSP violation reporting)"""
        # Decode URL
        path = unquote(self.path)

        # Handle CSP violation reports
        if path == '/api/security-violation' or path.startswith('/api/security-violation'):
            try:
                # Read the request body
                content_length = int(self.headers.get('Content-Length', 0))
                post_data = self.rfile.read(content_length)

                # Try to parse as JSON
                try:
                    import json
                    violation_data = json.loads(post_data.decode('utf-8'))

                    # Log the violation with formatting
                    print("\n" + "="*70)
                    print("🚨 CSP VIOLATION REPORT")
                    print("="*70)
                    print(f"Timestamp: {self.log_date_time_string()}")
                    print(f"Violation Type: {violation_data.get('type', 'Unknown')}")
                    print(f"Blocked URI: {violation_data.get('blockedURI', 'N/A')}")
                    print(f"Violated Directive: {violation_data.get('violatedDirective', 'N/A')}")
                    print(f"Original Policy: {violation_data.get('originalPolicy', 'N/A')}")
                    print(f"Source File: {violation_data.get('sourceFile', 'N/A')}")
                    print(f"Line Number: {violation_data.get('lineNumber', 'N/A')}")
                    print(f"Column Number: {violation_data.get('columnNumber', 'N/A')}")

                    # Pretty print full JSON for debugging
                    print("\nFull Report:")
                    print(json.dumps(violation_data, indent=2))
                    print("="*70 + "\n")

                except json.JSONDecodeError:
                    # If not JSON, just log the raw data
                    print("\n" + "="*70)
                    print("🚨 CSP VIOLATION REPORT (Raw)")
                    print("="*70)
                    print(f"Timestamp: {self.log_date_time_string()}")
                    print(f"Raw Data: {post_data.decode('utf-8', errors='ignore')}")
                    print("="*70 + "\n")

                # Send success response
                self.send_response(204)  # 204 No Content
                self.send_header('Content-Type', 'application/json')
                self.end_headers()

            except Exception as e:
                # Log error and send 500 response
                print(f"\n❌ Error processing CSP violation report: {e}\n")
                self.send_response(500)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(b'{"error": "Internal server error"}')

        # Handle error reports
        elif path == '/api/error-report' or path.startswith('/api/error-report'):
            try:
                # Read the request body
                content_length = int(self.headers.get('Content-Length', 0))
                post_data = self.rfile.read(content_length)

                # Try to parse as JSON
                try:
                    import json
                    error_data = json.loads(post_data.decode('utf-8'))

                    # Log the error report
                    print("\n" + "="*70)
                    print("⚠️  CLIENT ERROR REPORT")
                    print("="*70)
                    print(f"Timestamp: {self.log_date_time_string()}")
                    print(f"Error Type: {error_data.get('type', 'Unknown')}")
                    print(f"Message: {error_data.get('message', 'N/A')}")
                    print(f"Source: {error_data.get('source', 'N/A')}")
                    print(f"Line: {error_data.get('line', 'N/A')}")
                    print(f"Column: {error_data.get('column', 'N/A')}")
                    print(f"Stack: {error_data.get('stack', 'N/A')}")
                    print("="*70 + "\n")

                except json.JSONDecodeError:
                    print(f"\n⚠️ Invalid JSON in error report\n")

                # Send success response
                self.send_response(204)  # 204 No Content
                self.send_header('Content-Type', 'application/json')
                self.end_headers()

            except Exception as e:
                print(f"\n❌ Error processing error report: {e}\n")
                self.send_response(500)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(b'{"error": "Internal server error"}')

        # Handle contact form submissions
        elif path == '/api/contact' or path.startswith('/api/contact'):
            try:
                # Read the request body
                content_length = int(self.headers.get('Content-Length', 0))
                post_data = self.rfile.read(content_length)

                # Try to parse as JSON
                try:
                    import json
                    form_data = json.loads(post_data.decode('utf-8'))

                    # Log the form submission
                    print("\n" + "="*70)
                    print("📧 CONTACT FORM SUBMISSION")
                    print("="*70)
                    print(f"Timestamp: {self.log_date_time_string()}")
                    print(f"Name: {form_data.get('name', 'N/A')}")
                    print(f"Email: {form_data.get('email', 'N/A')}")
                    print(f"Message: {form_data.get('message', 'N/A')}")
                    print("="*70 + "\n")

                except json.JSONDecodeError:
                    print(f"\n⚠️ Invalid JSON in contact form submission\n")

                # Send success response
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(b'{"success": true, "message": "Form submitted successfully"}')

            except Exception as e:
                print(f"\n❌ Error processing contact form: {e}\n")
                self.send_response(500)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(b'{"error": "Internal server error"}')

        else:
            # Unknown POST endpoint
            self.send_response(404)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(b'{"error": "Endpoint not found"}')

    def do_OPTIONS(self):
        """Handle OPTIONS requests for CORS preflight"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
    
    def log_message(self, format, *args):
        """Custom log format with colors"""
        status_code = args[1] if len(args) > 1 else '000'
        
        # Color codes
        GREEN = '\033[92m'
        YELLOW = '\033[93m'
        RED = '\033[91m'
        RESET = '\033[0m'
        BLUE = '\033[94m'
        
        # Determine color based on status code
        if status_code.startswith('2'):
            color = GREEN
        elif status_code.startswith('3'):
            color = BLUE
        elif status_code.startswith('4'):
            color = YELLOW
        else:
            color = RED
        
        print(f"{color}[{self.log_date_time_string()}] {format % args}{RESET}")

def run_server():
    """Start the HTTP server (CSP DISABLED)"""
    
    # Change to the script's directory
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    # Create server
    with socketserver.TCPServer(("", PORT), CSPHTTPRequestHandler) as httpd:
        print("\n" + "="*70)
        print("🚀 Kabilan.io Portfolio Server")
        print("="*70)
        print(f"\n✅ Server running at: http://localhost:{PORT}/")
        print(f"✅ Serving files from: {os.getcwd()}")
        print("\n⚠️  CSP Status: DISABLED")
        print("   Content Security Policy has been disabled to resolve persistent issues.")
        print("   Other security headers remain active for baseline protection.")
        print("\n🔒 Security Headers Enabled:")
        print("   ✗ Content-Security-Policy (DISABLED)")
        print("   ✓ X-Content-Type-Options: nosniff")
        print("   ✓ X-Frame-Options: DENY")
        print("   ✓ X-XSS-Protection: 1; mode=block")
        print("   ✓ Referrer-Policy: strict-origin-when-cross-origin")
        print("   ✓ Permissions-Policy: geolocation=(), microphone=(), camera=()")
        print("\n🔌 API Endpoints:")
        print("   ✓ POST /api/contact - Contact form submissions")
        print("\n📊 Server Logs:")
        print("-"*70 + "\n")
        
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\n" + "="*70)
            print("🛑 Server stopped by user")
            print("="*70 + "\n")

if __name__ == "__main__":
    run_server()

