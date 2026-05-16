/**
 * Comprehensive Content Security Policy System
 * Advanced CSP implementation with nonce generation, violation reporting, and progressive enhancement
 * Implements strict security policies with fallback mechanisms
 */

class CSPSecuritySystem {
  constructor() {
    this.nonces = new Map();
    this.violations = [];
    this.trustedDomains = new Set();
    this.reportingEndpoint = '/api/csp-report';
    
    this.config = {
      strictMode: false, // Temporarily disabled for inline styles
      reportOnly: false,
      enableNonces: true,
      enableHashes: true,
      allowInlineStyles: true, // Allow inline styles temporarily
      allowInlineScripts: false, // Keep scripts strict
      upgradeInsecureRequests: true
    };

    this.policies = {
      'default-src': ["'self'"],
      'script-src': ["'self'", 'blob:'], // Allow blob: for Three.js
      'style-src': ["'self'", "'unsafe-inline'", 'https://fonts.cdnfonts.com'], // Allow inline styles and font CDN
      'img-src': ["'self'", 'data:', 'https:', 'blob:'], // Allow blob: for Three.js textures
      'font-src': ["'self'", 'https://fonts.cdnfonts.com', 'https://fonts.gstatic.com'],
      'connect-src': ["'self'", 'blob:'], // Allow blob: for Three.js
      'media-src': ["'self'", 'blob:'],
      'object-src': ["'none'"],
      'base-uri': ["'self'"],
      'form-action': ["'self'"],
      'frame-ancestors': ["'none'"],
      'upgrade-insecure-requests': [],
      'worker-src': ["'self'", 'blob:'] // Allow blob: for service workers
    };
    
    this.init();
  }

  async init() {
    this.generateNonces();
    this.setupViolationReporting();
    this.implementCSPHeaders();
    this.setupProgressiveEnhancement();
    this.monitorSecurityViolations();
    this.setupTrustedDomains();
    this.implementHashGeneration();
    this.setupCSPValidation();
  }

  /**
   * Generate cryptographically secure nonces
   */
  generateNonces() {
    if (!this.config.enableNonces) return;
    
    // Generate nonce for scripts
    const scriptNonce = this.generateSecureNonce();
    this.nonces.set('script', scriptNonce);
    
    // Generate nonce for styles
    const styleNonce = this.generateSecureNonce();
    this.nonces.set('style', styleNonce);
    
    // Add nonces to CSP policies
    this.policies['script-src'].push(`'nonce-${scriptNonce}'`);
    this.policies['style-src'].push(`'nonce-${styleNonce}'`);
    
    console.log('🔐 Generated CSP nonces');
  }

  /**
   * Generate cryptographically secure nonce
   */
  generateSecureNonce() {
    if ('crypto' in window && 'getRandomValues' in crypto) {
      const array = new Uint8Array(16);
      crypto.getRandomValues(array);
      return btoa(String.fromCharCode.apply(null, array))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
    }
    
    // Fallback for older browsers
    return Math.random().toString(36).substring(2, 15) + 
           Math.random().toString(36).substring(2, 15);
  }

  /**
   * Setup violation reporting
   */
  setupViolationReporting() {
    // Listen for CSP violations
    document.addEventListener('securitypolicyviolation', (event) => {
      this.handleCSPViolation(event);
    });
    
    // Add report-uri to CSP
    this.policies['report-uri'] = [this.reportingEndpoint];
    this.policies['report-to'] = ['csp-endpoint'];
    
    // Setup Reporting API endpoint
    this.setupReportingAPI();
  }

  /**
   * Handle CSP violation
   */
  handleCSPViolation(event) {
    const violation = {
      documentURI: event.documentURI,
      referrer: event.referrer,
      blockedURI: event.blockedURI,
      violatedDirective: event.violatedDirective,
      effectiveDirective: event.effectiveDirective,
      originalPolicy: event.originalPolicy,
      sourceFile: event.sourceFile,
      lineNumber: event.lineNumber,
      columnNumber: event.columnNumber,
      statusCode: event.statusCode,
      timestamp: Date.now(),
      userAgent: navigator.userAgent
    };
    
    this.violations.push(violation);
    
    // Log violation
    console.warn('🚨 CSP Violation:', violation);
    
    // Report to server
    this.reportViolation(violation);
    
    // Take remedial action
    this.handleViolationRemediation(violation);
  }

  /**
   * Report violation to server (DISABLED to prevent connection storm)
   */
  async reportViolation(violation) {
    // DISABLED: This was causing hundreds of POST requests per second
    // Only log to console for debugging
    console.log('📊 CSP Violation (reporting disabled):', {
      type: violation.type,
      blockedURI: violation.blockedURI,
      violatedDirective: violation.violatedDirective
    });

    /* ORIGINAL CODE - DISABLED
    try {
      if ('sendBeacon' in navigator) {
        navigator.sendBeacon(this.reportingEndpoint, JSON.stringify(violation));
      } else {
        await fetch(this.reportingEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(violation)
        });
      }
    } catch (error) {
      console.warn('Failed to report CSP violation:', error);

      // Store locally for later reporting
      this.storeViolationLocally(violation);
    }
    */
  }

  /**
   * Store violation locally for offline reporting
   */
  storeViolationLocally(violation) {
    try {
      const stored = JSON.parse(localStorage.getItem('csp_violations') || '[]');
      stored.push(violation);
      
      // Keep only last 50 violations
      if (stored.length > 50) {
        stored.splice(0, stored.length - 50);
      }
      
      localStorage.setItem('csp_violations', JSON.stringify(stored));
    } catch (error) {
      console.warn('Failed to store CSP violation locally:', error);
    }
  }

  /**
   * Handle violation remediation
   */
  handleViolationRemediation(violation) {
    const { violatedDirective, blockedURI } = violation;
    
    // Handle inline script violations
    if (violatedDirective.includes('script-src') && blockedURI === 'inline') {
      this.remediateInlineScript(violation);
    }
    
    // Handle inline style violations
    if (violatedDirective.includes('style-src') && blockedURI === 'inline') {
      this.remediateInlineStyle(violation);
    }
    
    // Handle external resource violations
    if (blockedURI && blockedURI.startsWith('http')) {
      this.evaluateExternalResource(blockedURI, violatedDirective);
    }
  }

  /**
   * Remediate inline script violations
   */
  remediateInlineScript(violation) {
    // Find inline scripts without nonces
    const inlineScripts = document.querySelectorAll('script:not([src]):not([nonce])');
    
    inlineScripts.forEach(script => {
      if (script.textContent.trim()) {
        // Add nonce to inline script
        const scriptNonce = this.nonces.get('script');
        if (scriptNonce) {
          script.setAttribute('nonce', scriptNonce);
          console.log('🔧 Added nonce to inline script');
        } else {
          // Move to external file or generate hash
          this.moveScriptToExternal(script);
        }
      }
    });
  }

  /**
   * Remediate inline style violations
   */
  remediateInlineStyle(violation) {
    // Find inline styles without nonces
    const inlineStyles = document.querySelectorAll('style:not([nonce])');
    
    inlineStyles.forEach(style => {
      if (style.textContent.trim()) {
        // Add nonce to inline style
        const styleNonce = this.nonces.get('style');
        if (styleNonce) {
          style.setAttribute('nonce', styleNonce);
          console.log('🔧 Added nonce to inline style');
        } else {
          // Generate hash for style
          this.generateStyleHash(style);
        }
      }
    });
  }

  /**
   * Move inline script to external file
   */
  moveScriptToExternal(script) {
    const scriptContent = script.textContent;
    const scriptId = 'dynamic-script-' + Date.now();
    
    // Create blob URL for script
    const blob = new Blob([scriptContent], { type: 'application/javascript' });
    const scriptURL = URL.createObjectURL(blob);
    
    // Create new script element
    const newScript = document.createElement('script');
    newScript.src = scriptURL;
    newScript.id = scriptId;
    
    // Replace inline script
    script.parentNode.replaceChild(newScript, script);
    
    console.log('🔧 Moved inline script to external file');
  }

  /**
   * Generate hash for style content
   */
  async generateStyleHash(style) {
    if (!this.config.enableHashes) return;
    
    const content = style.textContent.trim();
    const hash = await this.generateSHA256Hash(content);
    
    // Add hash to CSP policy
    if (!this.policies['style-src'].includes(`'sha256-${hash}'`)) {
      this.policies['style-src'].push(`'sha256-${hash}'`);
      this.updateCSPHeader();
    }
    
    console.log('🔧 Generated hash for inline style:', hash);
  }

  /**
   * Generate SHA256 hash
   */
  async generateSHA256Hash(content) {
    if ('crypto' in window && 'subtle' in crypto) {
      const encoder = new TextEncoder();
      const data = encoder.encode(content);
      const hashBuffer = await crypto.subtle.digest('SHA-256', data);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      return btoa(String.fromCharCode.apply(null, hashArray));
    }
    
    // Fallback hash generation
    return btoa(content).substring(0, 32);
  }

  /**
   * Evaluate external resource for trust
   */
  evaluateExternalResource(uri, directive) {
    const url = new URL(uri);
    const domain = url.hostname;
    
    // Check if domain is in trusted list
    if (this.trustedDomains.has(domain)) {
      this.addDomainToCSP(domain, directive);
    } else {
      // Log for manual review
      console.warn(`🔍 External resource blocked: ${uri} (${directive})`);
      this.logExternalResourceForReview(uri, directive);
    }
  }

  /**
   * Add trusted domain to CSP
   */
  addDomainToCSP(domain, directive) {
    const directiveKey = directive.replace(/-src.*/, '-src');
    
    if (this.policies[directiveKey]) {
      const domainEntry = `https://${domain}`;
      if (!this.policies[directiveKey].includes(domainEntry)) {
        this.policies[directiveKey].push(domainEntry);
        this.updateCSPHeader();
        console.log(`🔧 Added trusted domain to CSP: ${domain}`);
      }
    }
  }

  /**
   * Implement CSP headers
   */
  implementCSPHeaders() {
    const cspString = this.buildCSPString();
    
    // Set CSP via meta tag (fallback)
    this.setCSPMetaTag(cspString);
    
    // Request server-side CSP header
    this.requestServerCSP(cspString);
    
    console.log('🛡️ CSP Policy:', cspString);
  }

  /**
   * Build CSP string from policies
   */
  buildCSPString() {
    const directives = [];
    
    Object.entries(this.policies).forEach(([directive, values]) => {
      if (values.length > 0) {
        directives.push(`${directive} ${values.join(' ')}`);
      } else if (directive === 'upgrade-insecure-requests') {
        directives.push(directive);
      }
    });
    
    return directives.join('; ');
  }

  /**
   * Set CSP via meta tag
   */
  setCSPMetaTag(cspString) {
    // Remove existing CSP meta tag
    const existingMeta = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
    if (existingMeta) {
      existingMeta.remove();
    }
    
    // Create new CSP meta tag
    const meta = document.createElement('meta');
    meta.setAttribute('http-equiv', 'Content-Security-Policy');
    meta.setAttribute('content', cspString);
    
    document.head.appendChild(meta);
  }

  /**
   * Request server-side CSP header
   */
  async requestServerCSP(cspString) {
    try {
      await fetch('/api/set-csp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          csp: cspString,
          reportOnly: this.config.reportOnly
        })
      });
    } catch (error) {
      console.warn('Failed to set server-side CSP:', error);
    }
  }

  /**
   * Setup progressive enhancement
   */
  setupProgressiveEnhancement() {
    // Start with strict policy
    if (this.config.strictMode) {
      this.enableStrictMode();
    }
    
    // Monitor for compatibility issues
    this.monitorCompatibility();
    
    // Gradually relax policy if needed
    this.setupGradualRelaxation();
  }

  /**
   * Enable strict CSP mode
   */
  enableStrictMode() {
    // Remove unsafe-inline and unsafe-eval
    Object.keys(this.policies).forEach(directive => {
      this.policies[directive] = this.policies[directive].filter(value => 
        !value.includes('unsafe-inline') && !value.includes('unsafe-eval')
      );
    });
    
    // Add strict-dynamic for scripts (if nonces are used)
    if (this.config.enableNonces && this.nonces.has('script')) {
      this.policies['script-src'].push("'strict-dynamic'");
    }
    
    console.log('🔒 Strict CSP mode enabled');
  }

  /**
   * Monitor compatibility issues
   */
  monitorCompatibility() {
    let violationCount = 0;
    const maxViolations = 10;
    const timeWindow = 60000; // 1 minute
    
    const checkCompatibility = () => {
      violationCount = this.violations.filter(v => 
        Date.now() - v.timestamp < timeWindow
      ).length;
      
      if (violationCount > maxViolations) {
        console.warn('🚨 High CSP violation rate detected, considering policy relaxation');
        this.considerPolicyRelaxation();
      }
    };
    
    setInterval(checkCompatibility, 10000); // Check every 10 seconds
  }

  /**
   * Consider policy relaxation
   */
  considerPolicyRelaxation() {
    const recentViolations = this.violations.filter(v => 
      Date.now() - v.timestamp < 60000
    );
    
    // Analyze violation patterns
    const violationTypes = {};
    recentViolations.forEach(v => {
      const key = v.violatedDirective;
      violationTypes[key] = (violationTypes[key] || 0) + 1;
    });
    
    // Relax most problematic directives
    Object.entries(violationTypes).forEach(([directive, count]) => {
      if (count > 5) {
        this.relaxDirective(directive);
      }
    });
  }

  /**
   * Relax specific directive
   */
  relaxDirective(directive) {
    const directiveKey = directive.replace(/-src.*/, '-src');
    
    if (this.policies[directiveKey]) {
      // Add unsafe-inline as last resort
      if (!this.policies[directiveKey].includes("'unsafe-inline'")) {
        this.policies[directiveKey].push("'unsafe-inline'");
        this.updateCSPHeader();
        
        console.warn(`⚠️ Relaxed CSP directive: ${directiveKey}`);
      }
    }
  }

  /**
   * Setup trusted domains
   */
  setupTrustedDomains() {
    // Common trusted domains
    const trustedDomains = [
      'fonts.googleapis.com',
      'fonts.gstatic.com',
      'cdnjs.cloudflare.com',
      'cdn.jsdelivr.net',
      'unpkg.com',
      'fonts.cdnfonts.com'
    ];
    
    trustedDomains.forEach(domain => {
      this.trustedDomains.add(domain);
    });
    
    // Add trusted domains to appropriate directives
    this.addTrustedDomainsToCSP();
  }

  /**
   * Add trusted domains to CSP
   */
  addTrustedDomainsToCSP() {
    // Font sources
    this.policies['font-src'].push('https://fonts.gstatic.com');
    this.policies['style-src'].push('https://fonts.googleapis.com');
    
    // CDN sources
    this.policies['script-src'].push('https://cdnjs.cloudflare.com');
    this.policies['script-src'].push('https://cdn.jsdelivr.net');
    
    console.log('🔗 Added trusted domains to CSP');
  }

  /**
   * Implement hash generation for existing scripts
   */
  async implementHashGeneration() {
    if (!this.config.enableHashes) return;
    
    const inlineScripts = document.querySelectorAll('script:not([src]):not([nonce])');
    const inlineStyles = document.querySelectorAll('style:not([nonce])');
    
    // Generate hashes for scripts
    for (const script of inlineScripts) {
      if (script.textContent.trim()) {
        const hash = await this.generateSHA256Hash(script.textContent.trim());
        this.policies['script-src'].push(`'sha256-${hash}'`);
      }
    }
    
    // Generate hashes for styles
    for (const style of inlineStyles) {
      if (style.textContent.trim()) {
        const hash = await this.generateSHA256Hash(style.textContent.trim());
        this.policies['style-src'].push(`'sha256-${hash}'`);
      }
    }
    
    console.log('🔐 Generated hashes for inline content');
  }

  /**
   * Setup CSP validation
   */
  setupCSPValidation() {
    // Validate current CSP policy
    this.validateCSPPolicy();
    
    // Setup periodic validation
    setInterval(() => {
      this.validateCSPPolicy();
    }, 300000); // Every 5 minutes
  }

  /**
   * Validate CSP policy
   */
  validateCSPPolicy() {
    const issues = [];
    
    // Check for unsafe directives
    Object.entries(this.policies).forEach(([directive, values]) => {
      values.forEach(value => {
        if (value.includes('unsafe-inline')) {
          issues.push(`Unsafe inline detected in ${directive}`);
        }
        if (value.includes('unsafe-eval')) {
          issues.push(`Unsafe eval detected in ${directive}`);
        }
        if (value === '*') {
          issues.push(`Wildcard detected in ${directive}`);
        }
      });
    });
    
    // Check for missing directives
    const requiredDirectives = ['default-src', 'script-src', 'style-src', 'object-src'];
    requiredDirectives.forEach(directive => {
      if (!this.policies[directive]) {
        issues.push(`Missing required directive: ${directive}`);
      }
    });
    
    if (issues.length > 0) {
      console.warn('🚨 CSP Policy Issues:', issues);
    } else {
      console.log('✅ CSP Policy validation passed');
    }
    
    return issues;
  }

  /**
   * Setup Reporting API
   */
  setupReportingAPI() {
    // Configure Reporting API endpoint
    const reportingConfig = {
      group: 'csp-endpoint',
      max_age: 86400, // 24 hours
      endpoints: [{
        url: this.reportingEndpoint
      }]
    };
    
    // Set Report-To header via meta tag
    const meta = document.createElement('meta');
    meta.setAttribute('http-equiv', 'Report-To');
    meta.setAttribute('content', JSON.stringify(reportingConfig));
    document.head.appendChild(meta);
  }

  /**
   * Monitor security violations
   */
  monitorSecurityViolations() {
    // Track violation trends
    setInterval(() => {
      this.analyzeViolationTrends();
    }, 60000); // Every minute
    
    // Report offline violations when online
    window.addEventListener('online', () => {
      this.reportOfflineViolations();
    });
  }

  /**
   * Analyze violation trends
   */
  analyzeViolationTrends() {
    const recentViolations = this.violations.filter(v => 
      Date.now() - v.timestamp < 300000 // Last 5 minutes
    );
    
    if (recentViolations.length === 0) return;
    
    // Group by directive
    const byDirective = {};
    recentViolations.forEach(v => {
      const key = v.violatedDirective;
      byDirective[key] = (byDirective[key] || 0) + 1;
    });
    
    // Log trends
    console.log('📊 CSP Violation Trends (5min):', byDirective);
    
    // Alert on high violation rates
    Object.entries(byDirective).forEach(([directive, count]) => {
      if (count > 5) {
        console.warn(`🚨 High violation rate for ${directive}: ${count} violations`);
      }
    });
  }

  /**
   * Report offline violations (DISABLED to prevent connection storm)
   */
  async reportOfflineViolations() {
    // DISABLED: This was causing connection storms
    // Just clear the stored violations
    try {
      const stored = JSON.parse(localStorage.getItem('csp_violations') || '[]');

      if (stored.length > 0) {
        console.log(`📊 Found ${stored.length} offline CSP violations (reporting disabled)`);
        localStorage.removeItem('csp_violations');
      }
    } catch (error) {
      console.error('Failed to clear offline violations:', error);
    }

    /* ORIGINAL CODE - DISABLED
    try {
      const stored = JSON.parse(localStorage.getItem('csp_violations') || '[]');

      if (stored.length > 0) {
        await fetch(this.reportingEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            violations: stored,
            offline: true
          })
        });

        // Clear stored violations
        localStorage.removeItem('csp_violations');
        console.log(`📤 Reported ${stored.length} offline CSP violations`);
      }
    } catch (error) {
      console.warn('Failed to report offline violations:', error);
    }
    */
  }

  /**
   * Update CSP header
   */
  updateCSPHeader() {
    const cspString = this.buildCSPString();
    this.setCSPMetaTag(cspString);
    this.requestServerCSP(cspString);
  }

  /**
   * Log external resource for review
   */
  logExternalResourceForReview(uri, directive) {
    const review = {
      uri,
      directive,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      referrer: document.referrer
    };
    
    try {
      const stored = JSON.parse(localStorage.getItem('csp_review_queue') || '[]');
      stored.push(review);
      
      // Keep only last 100 items
      if (stored.length > 100) {
        stored.splice(0, stored.length - 100);
      }
      
      localStorage.setItem('csp_review_queue', JSON.stringify(stored));
    } catch (error) {
      console.warn('Failed to log resource for review:', error);
    }
  }

  /**
   * Generate CSP report
   */
  generateReport() {
    const report = {
      timestamp: Date.now(),
      config: this.config,
      policies: this.policies,
      violations: this.violations,
      violationSummary: this.getViolationSummary(),
      trustedDomains: Array.from(this.trustedDomains),
      nonces: Object.fromEntries(this.nonces),
      validationIssues: this.validateCSPPolicy(),
      recommendations: this.generateRecommendations()
    };
    
    console.log('🛡️ CSP Security Report:', report);
    localStorage.setItem('csp_security_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Get violation summary
   */
  getViolationSummary() {
    const summary = {
      total: this.violations.length,
      byDirective: {},
      byTimeframe: {
        lastHour: 0,
        lastDay: 0,
        lastWeek: 0
      }
    };
    
    const now = Date.now();
    const hour = 60 * 60 * 1000;
    const day = 24 * hour;
    const week = 7 * day;
    
    this.violations.forEach(v => {
      // By directive
      const directive = v.violatedDirective;
      summary.byDirective[directive] = (summary.byDirective[directive] || 0) + 1;
      
      // By timeframe
      const age = now - v.timestamp;
      if (age < hour) summary.byTimeframe.lastHour++;
      if (age < day) summary.byTimeframe.lastDay++;
      if (age < week) summary.byTimeframe.lastWeek++;
    });
    
    return summary;
  }

  /**
   * Generate security recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    // Check for unsafe directives
    Object.entries(this.policies).forEach(([directive, values]) => {
      if (values.includes("'unsafe-inline'")) {
        recommendations.push(`Remove 'unsafe-inline' from ${directive} and use nonces or hashes`);
      }
      if (values.includes("'unsafe-eval'")) {
        recommendations.push(`Remove 'unsafe-eval' from ${directive} and avoid dynamic code execution`);
      }
    });
    
    // Check violation rates
    const violationRate = this.violations.length;
    if (violationRate > 50) {
      recommendations.push('High violation rate detected. Review and adjust CSP policy');
    }
    
    // Check for missing directives
    if (!this.policies['frame-ancestors']) {
      recommendations.push("Add 'frame-ancestors' directive to prevent clickjacking");
    }
    
    if (!this.policies['base-uri']) {
      recommendations.push("Add 'base-uri' directive to prevent base tag injection");
    }
    
    return recommendations;
  }

  /**
   * Get current CSP status
   */
  getStatus() {
    return {
      policiesActive: Object.keys(this.policies).length,
      noncesGenerated: this.nonces.size,
      trustedDomains: this.trustedDomains.size,
      violations: this.violations.length,
      strictMode: this.config.strictMode,
      reportOnly: this.config.reportOnly
    };
  }
}

// Initialize CSP Security System
const cspSecuritySystem = new CSPSecuritySystem();

// Export for global access
window.CSPSecuritySystem = CSPSecuritySystem;
window.cspSecuritySystem = cspSecuritySystem;

console.log('✅ CSP Security System initialized');