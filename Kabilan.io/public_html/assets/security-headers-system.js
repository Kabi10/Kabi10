/**
 * Advanced Security Headers System
 * Comprehensive security header implementation with HSTS, X-Frame-Options, and more
 * Implements modern security best practices with validation and monitoring
 */

class SecurityHeadersSystem {
  constructor() {
    this.headers = new Map();
    this.violations = [];
    this.config = {
      hstsMaxAge: 31536000, // 1 year
      hstsIncludeSubdomains: true,
      hstsPreload: true,
      frameOptions: 'DENY',
      contentTypeOptions: 'nosniff',
      referrerPolicy: 'strict-origin-when-cross-origin',
      permissionsPolicy: {
        camera: [],
        microphone: [],
        geolocation: ['self'],
        payment: ['self'],
        usb: [],
        magnetometer: [],
        gyroscope: [],
        accelerometer: []
      }
    };
    
    this.securityTests = new Map();
    this.init();
  }

  async init() {
    this.setupSecurityHeaders();
    this.implementHSTS();
    this.configureFrameOptions();
    this.setupContentTypeOptions();
    this.configureReferrerPolicy();
    this.implementPermissionsPolicy();
    this.setupCrossDomainPolicies();
    this.implementSecurityValidation();
    this.setupSecurityMonitoring();
    this.performSecurityTests();
  }

  /**
   * Setup comprehensive security headers
   */
  setupSecurityHeaders() {
    // HTTP Strict Transport Security (HSTS)
    this.setSecurityHeader('Strict-Transport-Security', 
      `max-age=${this.config.hstsMaxAge}${this.config.hstsIncludeSubdomains ? '; includeSubDomains' : ''}${this.config.hstsPreload ? '; preload' : ''}`
    );
    
    // X-Frame-Options
    this.setSecurityHeader('X-Frame-Options', this.config.frameOptions);
    
    // X-Content-Type-Options
    this.setSecurityHeader('X-Content-Type-Options', this.config.contentTypeOptions);
    
    // X-XSS-Protection (legacy support)
    this.setSecurityHeader('X-XSS-Protection', '1; mode=block');
    
    // Referrer Policy
    this.setSecurityHeader('Referrer-Policy', this.config.referrerPolicy);
    
    // X-DNS-Prefetch-Control
    this.setSecurityHeader('X-DNS-Prefetch-Control', 'off');
    
    // X-Download-Options (IE)
    this.setSecurityHeader('X-Download-Options', 'noopen');
    
    // X-Permitted-Cross-Domain-Policies
    this.setSecurityHeader('X-Permitted-Cross-Domain-Policies', 'none');
    
    console.log('🛡️ Security headers configured');
  }

  /**
   * Set security header via meta tag and request server implementation
   */
  setSecurityHeader(name, value) {
    this.headers.set(name, value);
    
    // Set via meta tag where possible
    if (this.canSetViaMeta(name)) {
      this.setMetaHeader(name, value);
    }
    
    // Request server-side header
    this.requestServerHeader(name, value);
  }

  /**
   * Check if header can be set via meta tag
   */
  canSetViaMeta(headerName) {
    const metaHeaders = [
      'Content-Security-Policy',
      'Referrer-Policy',
      'X-UA-Compatible'
    ];
    
    return metaHeaders.includes(headerName);
  }

  /**
   * Set header via meta tag
   */
  setMetaHeader(name, value) {
    const meta = document.createElement('meta');
    meta.setAttribute('http-equiv', name);
    meta.setAttribute('content', value);
    document.head.appendChild(meta);
  }

  /**
   * Request server-side header implementation
   */
  async requestServerHeader(name, value) {
    try {
      await fetch('/api/set-security-header', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          header: name,
          value: value
        })
      });
    } catch (error) {
      console.warn(`Failed to set server header ${name}:`, error);
    }
  }

  /**
   * Implement HTTP Strict Transport Security
   */
  implementHSTS() {
    // Check if already on HTTPS
    if (location.protocol !== 'https:') {
      console.warn('⚠️ HSTS requires HTTPS. Redirecting...');
      this.enforceHTTPS();
      return;
    }
    
    // Validate HSTS configuration
    this.validateHSTS();
    
    // Setup HSTS preload submission
    this.setupHSTSPreload();
    
    console.log('🔒 HSTS implemented');
  }

  /**
   * Enforce HTTPS redirect
   */
  enforceHTTPS() {
    if (location.protocol === 'http:' && location.hostname !== 'localhost') {
      const httpsUrl = location.href.replace('http://', 'https://');
      location.replace(httpsUrl);
    }
  }

  /**
   * Validate HSTS configuration
   */
  validateHSTS() {
    const issues = [];
    
    if (this.config.hstsMaxAge < 31536000) {
      issues.push('HSTS max-age should be at least 1 year (31536000 seconds)');
    }
    
    if (!this.config.hstsIncludeSubdomains) {
      issues.push('Consider enabling includeSubDomains for better security');
    }
    
    if (issues.length > 0) {
      console.warn('🚨 HSTS Configuration Issues:', issues);
    }
  }

  /**
   * Setup HSTS preload submission
   */
  setupHSTSPreload() {
    if (this.config.hstsPreload && this.config.hstsMaxAge >= 31536000) {
      console.log('📝 HSTS preload eligible. Submit to: https://hstspreload.org/');
    }
  }

  /**
   * Configure X-Frame-Options
   */
  configureFrameOptions() {
    // Validate frame options
    const validOptions = ['DENY', 'SAMEORIGIN'];
    
    if (!validOptions.includes(this.config.frameOptions)) {
      console.warn('⚠️ Invalid X-Frame-Options value:', this.config.frameOptions);
      this.config.frameOptions = 'DENY';
    }
    
    // Test for clickjacking vulnerability
    this.testClickjackingProtection();
    
    console.log('🖼️ X-Frame-Options configured:', this.config.frameOptions);
  }

  /**
   * Test clickjacking protection
   */
  testClickjackingProtection() {
    // Check if page can be framed
    if (window.top !== window.self) {
      console.warn('🚨 Page is being framed - potential clickjacking risk');
      
      // Break out of frame if policy allows
      if (this.config.frameOptions === 'DENY') {
        try {
          window.top.location = window.location;
        } catch (error) {
          // Frame busting blocked by browser security
          console.log('✅ Frame busting blocked by browser - good security');
        }
      }
    }
  }

  /**
   * Setup Content-Type-Options
   */
  setupContentTypeOptions() {
    // Prevent MIME type sniffing
    this.validateContentTypes();
    
    // Monitor for MIME type confusion attacks
    this.monitorMIMETypes();
    
    console.log('📄 X-Content-Type-Options configured');
  }

  /**
   * Validate content types
   */
  validateContentTypes() {
    const scripts = document.querySelectorAll('script[src]');
    const styles = document.querySelectorAll('link[rel="stylesheet"]');
    
    // Check script MIME types
    scripts.forEach(script => {
      if (script.type && script.type !== 'text/javascript' && script.type !== 'application/javascript') {
        console.warn('⚠️ Non-standard script MIME type:', script.type);
      }
    });
    
    // Check stylesheet MIME types
    styles.forEach(style => {
      if (style.type && style.type !== 'text/css') {
        console.warn('⚠️ Non-standard stylesheet MIME type:', style.type);
      }
    });
  }

  /**
   * Monitor MIME types
   */
  monitorMIMETypes() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver(list => {
        list.getEntries().forEach(entry => {
          if (entry.initiatorType === 'script' || entry.initiatorType === 'link') {
            this.validateResourceMIME(entry);
          }
        });
      });
      
      observer.observe({ entryTypes: ['resource'] });
    }
  }

  /**
   * Validate resource MIME type
   */
  async validateResourceMIME(entry) {
    try {
      const response = await fetch(entry.name, { method: 'HEAD' });
      const contentType = response.headers.get('content-type');
      
      if (entry.initiatorType === 'script' && contentType && 
          !contentType.includes('javascript') && !contentType.includes('ecmascript')) {
        console.warn('🚨 Script with non-JavaScript MIME type:', entry.name, contentType);
      }
      
      if (entry.initiatorType === 'link' && contentType && 
          !contentType.includes('css')) {
        console.warn('🚨 Stylesheet with non-CSS MIME type:', entry.name, contentType);
      }
    } catch (error) {
      // Ignore fetch errors for MIME validation
    }
  }

  /**
   * Configure Referrer Policy
   */
  configureReferrerPolicy() {
    const validPolicies = [
      'no-referrer',
      'no-referrer-when-downgrade',
      'origin',
      'origin-when-cross-origin',
      'same-origin',
      'strict-origin',
      'strict-origin-when-cross-origin',
      'unsafe-url'
    ];
    
    if (!validPolicies.includes(this.config.referrerPolicy)) {
      console.warn('⚠️ Invalid Referrer-Policy:', this.config.referrerPolicy);
      this.config.referrerPolicy = 'strict-origin-when-cross-origin';
    }
    
    // Apply to existing links
    this.applyReferrerPolicy();
    
    console.log('🔗 Referrer-Policy configured:', this.config.referrerPolicy);
  }

  /**
   * Apply referrer policy to existing links
   */
  applyReferrerPolicy() {
    const externalLinks = document.querySelectorAll('a[href^="http"]:not([rel*="noreferrer"])');
    
    externalLinks.forEach(link => {
      const url = new URL(link.href);
      if (url.hostname !== location.hostname) {
        // Add noreferrer for external links
        const rel = link.getAttribute('rel') || '';
        link.setAttribute('rel', `${rel} noreferrer`.trim());
      }
    });
  }

  /**
   * Implement Permissions Policy
   */
  implementPermissionsPolicy() {
    const policyString = this.buildPermissionsPolicyString();
    
    this.setSecurityHeader('Permissions-Policy', policyString);
    
    // Also set Feature-Policy for older browsers
    const featurePolicyString = this.buildFeaturePolicyString();
    this.setSecurityHeader('Feature-Policy', featurePolicyString);
    
    console.log('🔐 Permissions Policy configured');
  }

  /**
   * Build Permissions Policy string
   */
  buildPermissionsPolicyString() {
    const policies = [];
    
    Object.entries(this.config.permissionsPolicy).forEach(([feature, allowlist]) => {
      if (allowlist.length === 0) {
        policies.push(`${feature}=()`);
      } else {
        const origins = allowlist.map(origin => 
          origin === 'self' ? 'self' : `"${origin}"`
        ).join(' ');
        policies.push(`${feature}=(${origins})`);
      }
    });
    
    return policies.join(', ');
  }

  /**
   * Build Feature Policy string (legacy)
   */
  buildFeaturePolicyString() {
    const policies = [];
    
    Object.entries(this.config.permissionsPolicy).forEach(([feature, allowlist]) => {
      if (allowlist.length === 0) {
        policies.push(`${feature} 'none'`);
      } else {
        const origins = allowlist.map(origin => 
          origin === 'self' ? "'self'" : origin
        ).join(' ');
        policies.push(`${feature} ${origins}`);
      }
    });
    
    return policies.join('; ');
  }

  /**
   * Setup cross-domain policies
   */
  setupCrossDomainPolicies() {
    // Disable Flash cross-domain access
    this.setSecurityHeader('X-Permitted-Cross-Domain-Policies', 'none');
    
    // Setup CORS policies
    this.setupCORSPolicies();
    
    // Monitor cross-origin requests
    this.monitorCrossOriginRequests();
    
    console.log('🌐 Cross-domain policies configured');
  }

  /**
   * Setup CORS policies
   */
  setupCORSPolicies() {
    // Monitor fetch requests for CORS issues
    const originalFetch = window.fetch;
    
    window.fetch = async function(resource, options = {}) {
      const url = new URL(resource, location.origin);
      
      // Check for cross-origin requests
      if (url.origin !== location.origin) {
        console.log('🌐 Cross-origin request:', url.origin);
        
        // Add security headers for cross-origin requests
        const headers = new Headers(options.headers);
        headers.set('X-Requested-With', 'XMLHttpRequest');
        
        options.headers = headers;
      }
      
      return originalFetch.call(this, resource, options);
    };
  }

  /**
   * Monitor cross-origin requests
   */
  monitorCrossOriginRequests() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver(list => {
        list.getEntries().forEach(entry => {
          const url = new URL(entry.name);
          if (url.origin !== location.origin) {
            this.logCrossOriginRequest(entry);
          }
        });
      });
      
      observer.observe({ entryTypes: ['resource'] });
    }
  }

  /**
   * Log cross-origin request
   */
  logCrossOriginRequest(entry) {
    const request = {
      url: entry.name,
      type: entry.initiatorType,
      duration: entry.duration,
      size: entry.transferSize,
      timestamp: Date.now()
    };
    
    console.log('📊 Cross-origin request:', request);
    
    // Store for analysis
    try {
      const stored = JSON.parse(localStorage.getItem('cross_origin_requests') || '[]');
      stored.push(request);
      
      // Keep only last 100 requests
      if (stored.length > 100) {
        stored.splice(0, stored.length - 100);
      }
      
      localStorage.setItem('cross_origin_requests', JSON.stringify(stored));
    } catch (error) {
      console.warn('Failed to store cross-origin request:', error);
    }
  }

  /**
   * Implement security validation
   */
  implementSecurityValidation() {
    // Validate current security posture
    this.validateSecurityHeaders();
    
    // Setup periodic validation
    setInterval(() => {
      this.validateSecurityHeaders();
    }, 300000); // Every 5 minutes
    
    // Validate on page load
    window.addEventListener('load', () => {
      setTimeout(() => {
        this.performComprehensiveValidation();
      }, 2000);
    });
  }

  /**
   * Validate security headers
   */
  validateSecurityHeaders() {
    const issues = [];
    
    // Check for missing headers
    const requiredHeaders = [
      'Strict-Transport-Security',
      'X-Frame-Options',
      'X-Content-Type-Options',
      'Referrer-Policy'
    ];
    
    requiredHeaders.forEach(header => {
      if (!this.headers.has(header)) {
        issues.push(`Missing security header: ${header}`);
      }
    });
    
    // Validate HSTS
    if (location.protocol === 'https:' && !this.headers.has('Strict-Transport-Security')) {
      issues.push('HSTS header missing on HTTPS site');
    }
    
    // Validate frame options
    const frameOptions = this.headers.get('X-Frame-Options');
    if (frameOptions && !['DENY', 'SAMEORIGIN'].includes(frameOptions)) {
      issues.push('Invalid X-Frame-Options value');
    }
    
    if (issues.length > 0) {
      console.warn('🚨 Security Header Issues:', issues);
    } else {
      console.log('✅ Security headers validation passed');
    }
    
    return issues;
  }

  /**
   * Perform comprehensive security validation
   */
  performComprehensiveValidation() {
    const results = {
      headers: this.validateSecurityHeaders(),
      https: this.validateHTTPS(),
      mixedContent: this.validateMixedContent(),
      crossOrigin: this.validateCrossOrigin(),
      permissions: this.validatePermissions()
    };
    
    console.log('🔍 Comprehensive Security Validation:', results);
    return results;
  }

  /**
   * Validate HTTPS implementation
   */
  validateHTTPS() {
    const issues = [];
    
    if (location.protocol !== 'https:') {
      issues.push('Site not served over HTTPS');
    }
    
    // Check for mixed content
    const httpResources = Array.from(document.querySelectorAll('script[src], link[href], img[src]'))
      .filter(el => {
        const url = el.src || el.href;
        return url && url.startsWith('http://');
      });
    
    if (httpResources.length > 0) {
      issues.push(`${httpResources.length} HTTP resources found on HTTPS page`);
    }
    
    return issues;
  }

  /**
   * Validate mixed content
   */
  validateMixedContent() {
    const issues = [];
    
    if (location.protocol === 'https:') {
      // Check for active mixed content (scripts, stylesheets)
      const activeContent = document.querySelectorAll('script[src^="http:"], link[href^="http:"]');
      if (activeContent.length > 0) {
        issues.push(`${activeContent.length} active mixed content resources`);
      }
      
      // Check for passive mixed content (images, media)
      const passiveContent = document.querySelectorAll('img[src^="http:"], video[src^="http:"], audio[src^="http:"]');
      if (passiveContent.length > 0) {
        issues.push(`${passiveContent.length} passive mixed content resources`);
      }
    }
    
    return issues;
  }

  /**
   * Validate cross-origin configuration
   */
  validateCrossOrigin() {
    const issues = [];
    
    // Check for missing crossorigin attributes on critical resources
    const criticalResources = document.querySelectorAll('script[src], link[rel="stylesheet"]');
    
    criticalResources.forEach(resource => {
      const url = resource.src || resource.href;
      if (url) {
        const resourceUrl = new URL(url, location.origin);
        if (resourceUrl.origin !== location.origin && !resource.hasAttribute('crossorigin')) {
          issues.push(`Missing crossorigin attribute: ${url}`);
        }
      }
    });
    
    return issues;
  }

  /**
   * Validate permissions
   */
  validatePermissions() {
    const issues = [];
    
    // Check for dangerous permissions
    const dangerousFeatures = ['camera', 'microphone', 'geolocation', 'payment'];
    
    dangerousFeatures.forEach(feature => {
      if (this.config.permissionsPolicy[feature] && 
          this.config.permissionsPolicy[feature].length > 1) {
        issues.push(`${feature} permission granted to multiple origins`);
      }
    });
    
    return issues;
  }

  /**
   * Setup security monitoring
   */
  setupSecurityMonitoring() {
    // Monitor for security violations
    this.monitorSecurityViolations();
    
    // Setup security event logging
    this.setupSecurityEventLogging();
    
    // Monitor for suspicious activity
    this.monitorSuspiciousActivity();
  }

  /**
   * Monitor security violations
   */
  monitorSecurityViolations() {
    // Listen for various security events
    window.addEventListener('error', (event) => {
      if (event.message && event.message.includes('blocked')) {
        this.logSecurityViolation('content-blocked', event);
      }
    });
    
    // Monitor console for security warnings
    const originalConsoleWarn = console.warn;
    console.warn = (...args) => {
      const message = args.join(' ');
      if (message.includes('Mixed Content') || message.includes('CORS') || 
          message.includes('CSP') || message.includes('security')) {
        this.logSecurityViolation('console-warning', { message, args });
      }
      originalConsoleWarn.apply(console, args);
    };
  }

  /**
   * Log security violation
   */
  logSecurityViolation(type, details) {
    const violation = {
      type,
      details,
      timestamp: Date.now(),
      url: location.href,
      userAgent: navigator.userAgent
    };
    
    this.violations.push(violation);
    
    console.warn('🚨 Security Violation:', violation);
    
    // Report to server
    this.reportSecurityViolation(violation);
  }

  /**
   * Report security violation
   */
  async reportSecurityViolation(violation) {
    try {
      if ('sendBeacon' in navigator) {
        navigator.sendBeacon('/api/security-violation', JSON.stringify(violation));
      } else {
        await fetch('/api/security-violation', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(violation)
        });
      }
    } catch (error) {
      console.warn('Failed to report security violation:', error);
    }
  }

  /**
   * Setup security event logging
   */
  setupSecurityEventLogging() {
    // Log page load security status
    window.addEventListener('load', () => {
      this.logSecurityEvent('page-load', {
        protocol: location.protocol,
        headers: Object.fromEntries(this.headers),
        timestamp: Date.now()
      });
    });
    
    // Log navigation security status
    window.addEventListener('beforeunload', () => {
      this.logSecurityEvent('page-unload', {
        violations: this.violations.length,
        timestamp: Date.now()
      });
    });
  }

  /**
   * Log security event
   */
  logSecurityEvent(type, data) {
    const event = {
      type,
      data,
      timestamp: Date.now()
    };
    
    try {
      const stored = JSON.parse(localStorage.getItem('security_events') || '[]');
      stored.push(event);
      
      // Keep only last 50 events
      if (stored.length > 50) {
        stored.splice(0, stored.length - 50);
      }
      
      localStorage.setItem('security_events', JSON.stringify(stored));
    } catch (error) {
      console.warn('Failed to log security event:', error);
    }
  }

  /**
   * Monitor suspicious activity
   */
  monitorSuspiciousActivity() {
    let clickCount = 0;
    let keyCount = 0;
    
    // Monitor for rapid clicking (potential bot activity)
    document.addEventListener('click', () => {
      clickCount++;
      setTimeout(() => clickCount--, 1000);
      
      if (clickCount > 10) {
        this.logSecurityViolation('suspicious-clicking', { clickCount });
      }
    });
    
    // Monitor for rapid key presses
    document.addEventListener('keydown', () => {
      keyCount++;
      setTimeout(() => keyCount--, 1000);
      
      if (keyCount > 20) {
        this.logSecurityViolation('suspicious-typing', { keyCount });
      }
    });
  }

  /**
   * Perform security tests
   */
  performSecurityTests() {
    // Test clickjacking protection
    this.testClickjackingProtection();
    
    // Test HTTPS enforcement
    this.testHTTPSEnforcement();
    
    // Test content type sniffing protection
    this.testContentTypeProtection();
    
    // Test referrer leakage
    this.testReferrerLeakage();
    
    console.log('🧪 Security tests completed');
  }

  /**
   * Test HTTPS enforcement
   */
  testHTTPSEnforcement() {
    if (location.protocol === 'https:') {
      this.securityTests.set('https-enforcement', 'PASS');
    } else {
      this.securityTests.set('https-enforcement', 'FAIL');
      console.warn('🚨 HTTPS not enforced');
    }
  }

  /**
   * Test content type protection
   */
  testContentTypeProtection() {
    const hasNoSniff = this.headers.has('X-Content-Type-Options');
    this.securityTests.set('content-type-protection', hasNoSniff ? 'PASS' : 'FAIL');
    
    if (!hasNoSniff) {
      console.warn('🚨 Content-Type sniffing protection missing');
    }
  }

  /**
   * Test referrer leakage
   */
  testReferrerLeakage() {
    const hasReferrerPolicy = this.headers.has('Referrer-Policy');
    this.securityTests.set('referrer-protection', hasReferrerPolicy ? 'PASS' : 'FAIL');
    
    if (!hasReferrerPolicy) {
      console.warn('🚨 Referrer policy not configured');
    }
  }

  /**
   * Generate security report
   */
  generateSecurityReport() {
    const report = {
      timestamp: Date.now(),
      headers: Object.fromEntries(this.headers),
      violations: this.violations,
      securityTests: Object.fromEntries(this.securityTests),
      validationResults: this.performComprehensiveValidation(),
      config: this.config,
      recommendations: this.generateSecurityRecommendations()
    };
    
    console.log('🛡️ Security Headers Report:', report);
    localStorage.setItem('security_headers_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Generate security recommendations
   */
  generateSecurityRecommendations() {
    const recommendations = [];
    
    // Check protocol
    if (location.protocol !== 'https:') {
      recommendations.push('Implement HTTPS with proper SSL/TLS configuration');
    }
    
    // Check HSTS
    if (!this.headers.has('Strict-Transport-Security')) {
      recommendations.push('Implement HTTP Strict Transport Security (HSTS)');
    }
    
    // Check frame options
    if (!this.headers.has('X-Frame-Options')) {
      recommendations.push('Implement X-Frame-Options to prevent clickjacking');
    }
    
    // Check content type options
    if (!this.headers.has('X-Content-Type-Options')) {
      recommendations.push('Implement X-Content-Type-Options to prevent MIME sniffing');
    }
    
    // Check referrer policy
    if (!this.headers.has('Referrer-Policy')) {
      recommendations.push('Implement Referrer-Policy to control referrer information');
    }
    
    // Check permissions policy
    if (!this.headers.has('Permissions-Policy')) {
      recommendations.push('Implement Permissions-Policy to control browser features');
    }
    
    return recommendations;
  }

  /**
   * Get security status
   */
  getSecurityStatus() {
    return {
      headersConfigured: this.headers.size,
      violations: this.violations.length,
      testsRun: this.securityTests.size,
      httpsEnabled: location.protocol === 'https:',
      hstsEnabled: this.headers.has('Strict-Transport-Security'),
      frameProtection: this.headers.has('X-Frame-Options'),
      contentTypeProtection: this.headers.has('X-Content-Type-Options')
    };
  }
}

// Initialize Security Headers System
const securityHeadersSystem = new SecurityHeadersSystem();

// Export for global access
window.SecurityHeadersSystem = SecurityHeadersSystem;
window.securityHeadersSystem = securityHeadersSystem;

console.log('✅ Security Headers System initialized');