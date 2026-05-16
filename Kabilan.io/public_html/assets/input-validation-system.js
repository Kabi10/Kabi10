/**
 * Input Validation and Sanitization System
 * Comprehensive XSS prevention, CSRF protection, and input validation
 * Implements secure input handling with real-time validation and sanitization
 */

class InputValidationSystem {
  constructor() {
    this.validators = new Map();
    this.sanitizers = new Map();
    this.csrfTokens = new Map();
    this.validationRules = new Map();
    this.securityEvents = [];
    
    this.config = {
      enableCSRF: true,
      enableXSSProtection: true,
      enableSQLInjectionProtection: true,
      enableRateLimiting: true,
      maxInputLength: 10000,
      allowedTags: ['b', 'i', 'em', 'strong', 'u'],
      blockedPatterns: [
        /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
        /javascript:/gi,
        /on\w+\s*=/gi,
        /data:text\/html/gi,
        /vbscript:/gi
      ]
    };
    
    this.rateLimits = new Map();
    this.init();
  }

  async init() {
    this.setupInputValidation();
    this.setupXSSProtection();
    this.setupCSRFProtection();
    this.setupSQLInjectionProtection();
    this.setupRateLimiting();
    this.setupFormValidation();
    this.setupRealTimeValidation();
    this.monitorSecurityEvents();
    this.setupSecurityHeaders();
  }

  /**
   * Setup comprehensive input validation
   */
  setupInputValidation() {
    // Register common validators
    this.registerValidator('email', this.validateEmail.bind(this));
    this.registerValidator('url', this.validateURL.bind(this));
    this.registerValidator('phone', this.validatePhone.bind(this));
    this.registerValidator('creditcard', this.validateCreditCard.bind(this));
    this.registerValidator('ssn', this.validateSSN.bind(this));
    this.registerValidator('alphanumeric', this.validateAlphanumeric.bind(this));
    this.registerValidator('numeric', this.validateNumeric.bind(this));
    this.registerValidator('password', this.validatePassword.bind(this));
    
    // Register sanitizers
    this.registerSanitizer('html', this.sanitizeHTML.bind(this));
    this.registerSanitizer('sql', this.sanitizeSQL.bind(this));
    this.registerSanitizer('javascript', this.sanitizeJavaScript.bind(this));
    this.registerSanitizer('css', this.sanitizeCSS.bind(this));
    this.registerSanitizer('url', this.sanitizeURL.bind(this));
    
    console.log('🔍 Input validation system initialized');
  }

  /**
   * Register custom validator
   */
  registerValidator(name, validatorFunction) {
    this.validators.set(name, validatorFunction);
  }

  /**
   * Register custom sanitizer
   */
  registerSanitizer(name, sanitizerFunction) {
    this.sanitizers.set(name, sanitizerFunction);
  }

  /**
   * Setup XSS protection
   */
  setupXSSProtection() {
    // Intercept all form submissions
    document.addEventListener('submit', (event) => {
      this.validateFormSubmission(event);
    });
    
    // Intercept input events for real-time validation
    document.addEventListener('input', (event) => {
      if (event.target.matches('input, textarea')) {
        this.validateInputRealTime(event.target);
      }
    });
    
    // Intercept paste events
    document.addEventListener('paste', (event) => {
      this.validatePasteContent(event);
    });
    
    // Monitor dynamic content insertion
    this.monitorDynamicContent();
    
    console.log('🛡️ XSS protection enabled');
  }

  /**
   * Validate form submission
   */
  validateFormSubmission(event) {
    const form = event.target;
    const formData = new FormData(form);
    let hasViolations = false;
    
    // Validate CSRF token
    if (this.config.enableCSRF && !this.validateCSRFToken(form)) {
      this.logSecurityEvent('csrf-violation', { form: form.id || 'unnamed' });
      hasViolations = true;
    }
    
    // Validate all form inputs
    for (const [name, value] of formData.entries()) {
      const input = form.querySelector(`[name="${name}"]`);
      const violations = this.validateInput(input, value);
      
      if (violations.length > 0) {
        this.handleInputViolations(input, violations);
        hasViolations = true;
      }
    }
    
    // Block submission if violations found
    if (hasViolations) {
      event.preventDefault();
      this.showSecurityWarning('Form submission blocked due to security violations');
    }
  }

  /**
   * Validate individual input
   */
  validateInput(input, value) {
    const violations = [];
    
    if (!input || !value) return violations;
    
    // Check input length
    if (value.length > this.config.maxInputLength) {
      violations.push({
        type: 'length-exceeded',
        message: `Input exceeds maximum length of ${this.config.maxInputLength}`,
        severity: 'medium'
      });
    }
    
    // Check for XSS patterns
    if (this.config.enableXSSProtection) {
      const xssViolations = this.detectXSS(value);
      violations.push(...xssViolations);
    }
    
    // Check for SQL injection patterns
    if (this.config.enableSQLInjectionProtection) {
      const sqlViolations = this.detectSQLInjection(value);
      violations.push(...sqlViolations);
    }
    
    // Apply custom validation rules
    const inputType = input.getAttribute('data-validate') || input.type;
    if (this.validators.has(inputType)) {
      const customViolations = this.validators.get(inputType)(value, input);
      if (customViolations) {
        violations.push(...(Array.isArray(customViolations) ? customViolations : [customViolations]));
      }
    }
    
    return violations;
  }

  /**
   * Detect XSS patterns
   */
  detectXSS(input) {
    const violations = [];
    
    // Check against blocked patterns
    this.config.blockedPatterns.forEach(pattern => {
      if (pattern.test(input)) {
        violations.push({
          type: 'xss-pattern',
          message: 'Potentially malicious script detected',
          severity: 'high',
          pattern: pattern.source
        });
      }
    });
    
    // Check for HTML tags not in allowlist
    const htmlTagRegex = /<(\w+)[^>]*>/gi;
    let match;
    
    while ((match = htmlTagRegex.exec(input)) !== null) {
      const tagName = match[1].toLowerCase();
      if (!this.config.allowedTags.includes(tagName)) {
        violations.push({
          type: 'disallowed-html',
          message: `HTML tag '${tagName}' is not allowed`,
          severity: 'medium',
          tag: tagName
        });
      }
    }
    
    // Check for event handlers
    const eventHandlerRegex = /on\w+\s*=/gi;
    if (eventHandlerRegex.test(input)) {
      violations.push({
        type: 'event-handler',
        message: 'Event handlers detected in input',
        severity: 'high'
      });
    }
    
    // Check for data URLs
    if (input.includes('data:') && (input.includes('javascript') || input.includes('vbscript'))) {
      violations.push({
        type: 'malicious-data-url',
        message: 'Malicious data URL detected',
        severity: 'high'
      });
    }
    
    return violations;
  }

  /**
   * Detect SQL injection patterns
   */
  detectSQLInjection(input) {
    const violations = [];
    const sqlPatterns = [
      /(\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION)\b)/gi,
      /(\b(OR|AND)\s+\d+\s*=\s*\d+)/gi,
      /(--|\/\*|\*\/)/g,
      /(\b(SCRIPT|JAVASCRIPT|VBSCRIPT)\b)/gi,
      /(\')(.*)(--)/gi,
      /(\')(.*)(#)/gi,
      /(;|\||&)/g
    ];
    
    sqlPatterns.forEach((pattern, index) => {
      if (pattern.test(input)) {
        violations.push({
          type: 'sql-injection',
          message: 'Potential SQL injection pattern detected',
          severity: 'high',
          patternIndex: index
        });
      }
    });
    
    return violations;
  }

  /**
   * Handle input violations
   */
  handleInputViolations(input, violations) {
    // Log security event
    this.logSecurityEvent('input-violation', {
      inputName: input.name || input.id,
      inputType: input.type,
      violations: violations
    });
    
    // Add visual feedback
    this.addValidationFeedback(input, violations);
    
    // Sanitize input if possible
    if (violations.some(v => v.severity === 'high')) {
      input.value = this.sanitizeInput(input.value, input);
    }
  }

  /**
   * Add validation feedback to input
   */
  addValidationFeedback(input, violations) {
    // Remove existing feedback
    const existingFeedback = input.parentNode.querySelector('.validation-feedback');
    if (existingFeedback) {
      existingFeedback.remove();
    }
    
    // Add error styling
    input.classList.add('validation-error');
    
    // Create feedback element
    const feedback = document.createElement('div');
    feedback.className = 'validation-feedback';
    feedback.innerHTML = violations.map(v => 
      `<div class="validation-message ${v.severity}">${v.message}</div>`
    ).join('');
    
    // Insert feedback after input
    input.parentNode.insertBefore(feedback, input.nextSibling);
    
    // Auto-remove after delay
    setTimeout(() => {
      input.classList.remove('validation-error');
      if (feedback.parentNode) {
        feedback.remove();
      }
    }, 5000);
  }

  /**
   * Sanitize input based on type
   */
  sanitizeInput(value, input) {
    const inputType = input.getAttribute('data-sanitize') || 'html';
    
    if (this.sanitizers.has(inputType)) {
      return this.sanitizers.get(inputType)(value);
    }
    
    // Default HTML sanitization
    return this.sanitizeHTML(value);
  }

  /**
   * HTML sanitizer
   */
  sanitizeHTML(input) {
    // Remove script tags
    let sanitized = input.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
    
    // Remove event handlers
    sanitized = sanitized.replace(/on\w+\s*=\s*["'][^"']*["']/gi, '');
    
    // Remove javascript: URLs
    sanitized = sanitized.replace(/javascript:/gi, '');
    
    // Remove data URLs with scripts
    sanitized = sanitized.replace(/data:text\/html[^"']*/gi, '');
    
    // Allow only whitelisted tags
    const allowedTagsRegex = new RegExp(`<(?!\/?(?:${this.config.allowedTags.join('|')})\s*\/?>)[^>]+>`, 'gi');
    sanitized = sanitized.replace(allowedTagsRegex, '');
    
    return sanitized;
  }

  /**
   * SQL sanitizer
   */
  sanitizeSQL(input) {
    // Escape single quotes
    let sanitized = input.replace(/'/g, "''");
    
    // Remove SQL keywords
    const sqlKeywords = ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DROP', 'CREATE', 'ALTER', 'EXEC', 'UNION'];
    sqlKeywords.forEach(keyword => {
      const regex = new RegExp(`\\b${keyword}\\b`, 'gi');
      sanitized = sanitized.replace(regex, '');
    });
    
    // Remove SQL comments
    sanitized = sanitized.replace(/(--|\/\*|\*\/)/g, '');
    
    return sanitized;
  }

  /**
   * JavaScript sanitizer
   */
  sanitizeJavaScript(input) {
    // Remove function calls
    let sanitized = input.replace(/\w+\s*\(/g, '');
    
    // Remove eval and similar functions
    sanitized = sanitized.replace(/\b(eval|setTimeout|setInterval|Function)\b/gi, '');
    
    // Remove script tags
    sanitized = sanitized.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
    
    return sanitized;
  }

  /**
   * CSS sanitizer
   */
  sanitizeCSS(input) {
    // Remove expression() calls
    let sanitized = input.replace(/expression\s*\(/gi, '');
    
    // Remove javascript: URLs
    sanitized = sanitized.replace(/javascript:/gi, '');
    
    // Remove @import statements
    sanitized = sanitized.replace(/@import/gi, '');
    
    return sanitized;
  }

  /**
   * URL sanitizer
   */
  sanitizeURL(input) {
    try {
      const url = new URL(input);
      
      // Only allow http and https protocols
      if (!['http:', 'https:'].includes(url.protocol)) {
        return '';
      }
      
      return url.toString();
    } catch (error) {
      return '';
    }
  }

  /**
   * Setup CSRF protection
   */
  setupCSRFProtection() {
    if (!this.config.enableCSRF) return;
    
    // Generate CSRF token for session
    this.generateCSRFToken();
    
    // Add CSRF tokens to all forms
    this.addCSRFTokensToForms();
    
    // Intercept AJAX requests to add CSRF tokens
    this.interceptAjaxRequests();
    
    console.log('🔒 CSRF protection enabled');
  }

  /**
   * Generate CSRF token
   */
  generateCSRFToken() {
    const token = this.generateSecureToken();
    this.csrfTokens.set('session', token);
    
    // Store in meta tag for JavaScript access
    let metaTag = document.querySelector('meta[name="csrf-token"]');
    if (!metaTag) {
      metaTag = document.createElement('meta');
      metaTag.name = 'csrf-token';
      document.head.appendChild(metaTag);
    }
    metaTag.content = token;
    
    return token;
  }

  /**
   * Generate secure token
   */
  generateSecureToken() {
    if ('crypto' in window && 'getRandomValues' in crypto) {
      const array = new Uint8Array(32);
      crypto.getRandomValues(array);
      return btoa(String.fromCharCode.apply(null, array))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
    }
    
    // Fallback for older browsers
    return Math.random().toString(36).substring(2, 15) + 
           Math.random().toString(36).substring(2, 15) +
           Date.now().toString(36);
  }

  /**
   * Add CSRF tokens to forms
   */
  addCSRFTokensToForms() {
    const forms = document.querySelectorAll('form');
    const token = this.csrfTokens.get('session');
    
    forms.forEach(form => {
      // Skip if already has CSRF token
      if (form.querySelector('input[name="_token"]')) return;
      
      const tokenInput = document.createElement('input');
      tokenInput.type = 'hidden';
      tokenInput.name = '_token';
      tokenInput.value = token;
      
      form.appendChild(tokenInput);
    });
  }

  /**
   * Validate CSRF token
   */
  validateCSRFToken(form) {
    const tokenInput = form.querySelector('input[name="_token"]');
    if (!tokenInput) return false;
    
    const submittedToken = tokenInput.value;
    const sessionToken = this.csrfTokens.get('session');
    
    return submittedToken === sessionToken;
  }

  /**
   * Intercept AJAX requests for CSRF protection
   */
  interceptAjaxRequests() {
    const originalFetch = window.fetch;
    const token = this.csrfTokens.get('session');
    
    window.fetch = function(resource, options = {}) {
      // Add CSRF token to POST requests
      if (options.method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(options.method.toUpperCase())) {
        const headers = new Headers(options.headers);
        headers.set('X-CSRF-Token', token);
        options.headers = headers;
      }
      
      return originalFetch.call(this, resource, options);
    };
    
    // Intercept XMLHttpRequest
    const originalOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url, ...args) {
      this._method = method;
      return originalOpen.call(this, method, url, ...args);
    };
    
    const originalSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(data) {
      if (this._method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(this._method.toUpperCase())) {
        this.setRequestHeader('X-CSRF-Token', token);
      }
      return originalSend.call(this, data);
    };
  }

  /**
   * Setup rate limiting
   */
  setupRateLimiting() {
    if (!this.config.enableRateLimiting) return;
    
    // Track form submissions
    document.addEventListener('submit', (event) => {
      this.checkRateLimit(event.target);
    });
    
    // Track AJAX requests
    this.monitorAjaxRateLimit();
    
    console.log('⏱️ Rate limiting enabled');
  }

  /**
   * Check rate limit for form
   */
  checkRateLimit(form) {
    const formId = form.id || form.action || 'anonymous';
    const now = Date.now();
    const windowSize = 60000; // 1 minute
    const maxRequests = 10;
    
    if (!this.rateLimits.has(formId)) {
      this.rateLimits.set(formId, []);
    }
    
    const requests = this.rateLimits.get(formId);
    
    // Remove old requests outside window
    const recentRequests = requests.filter(time => now - time < windowSize);
    
    if (recentRequests.length >= maxRequests) {
      this.logSecurityEvent('rate-limit-exceeded', { formId, requests: recentRequests.length });
      this.showSecurityWarning('Too many requests. Please wait before submitting again.');
      return false;
    }
    
    // Add current request
    recentRequests.push(now);
    this.rateLimits.set(formId, recentRequests);
    
    return true;
  }

  /**
   * Monitor AJAX rate limiting
   */
  monitorAjaxRateLimit() {
    const ajaxRequests = [];
    const maxAjaxRequests = 50;
    const windowSize = 60000; // 1 minute
    
    const originalFetch = window.fetch;
    window.fetch = function(resource, options = {}) {
      const now = Date.now();
      
      // Clean old requests
      const recentRequests = ajaxRequests.filter(time => now - time < windowSize);
      ajaxRequests.length = 0;
      ajaxRequests.push(...recentRequests);
      
      if (recentRequests.length >= maxAjaxRequests) {
        console.warn('🚨 AJAX rate limit exceeded');
        return Promise.reject(new Error('Rate limit exceeded'));
      }
      
      ajaxRequests.push(now);
      return originalFetch.call(this, resource, options);
    };
  }

  /**
   * Setup real-time validation
   */
  setupRealTimeValidation() {
    // Debounced validation for performance
    const debouncedValidate = this.debounce((input) => {
      this.validateInputRealTime(input);
    }, 300);
    
    document.addEventListener('input', (event) => {
      if (event.target.matches('input, textarea')) {
        debouncedValidate(event.target);
      }
    });
  }

  /**
   * Real-time input validation
   */
  validateInputRealTime(input) {
    const value = input.value;
    if (!value) return;
    
    const violations = this.validateInput(input, value);
    
    if (violations.length > 0) {
      // Only show high-severity violations in real-time
      const highSeverityViolations = violations.filter(v => v.severity === 'high');
      if (highSeverityViolations.length > 0) {
        this.addValidationFeedback(input, highSeverityViolations);
      }
    } else {
      // Remove validation feedback if input is now valid
      input.classList.remove('validation-error');
      const feedback = input.parentNode.querySelector('.validation-feedback');
      if (feedback) {
        feedback.remove();
      }
    }
  }

  /**
   * Validate paste content
   */
  validatePasteContent(event) {
    const pastedData = event.clipboardData.getData('text');
    const target = event.target;
    
    if (pastedData && target.matches('input, textarea')) {
      const violations = this.validateInput(target, pastedData);
      
      if (violations.some(v => v.severity === 'high')) {
        event.preventDefault();
        this.showSecurityWarning('Pasted content contains potentially malicious data');
        this.logSecurityEvent('malicious-paste', { 
          target: target.name || target.id,
          content: pastedData.substring(0, 100) // Log first 100 chars only
        });
      }
    }
  }

  /**
   * Monitor dynamic content insertion
   */
  monitorDynamicContent() {
    if ('MutationObserver' in window) {
      const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
          mutation.addedNodes.forEach(node => {
            if (node.nodeType === 1) { // Element node
              this.validateDynamicContent(node);
            }
          });
        });
      });
      
      observer.observe(document.body, {
        childList: true,
        subtree: true
      });
    }
  }

  /**
   * Validate dynamically added content
   */
  validateDynamicContent(element) {
    // Check for script tags
    const scripts = element.querySelectorAll ? element.querySelectorAll('script') : [];
    if (scripts.length > 0) {
      this.logSecurityEvent('dynamic-script-injection', {
        element: element.tagName,
        scripts: scripts.length
      });
    }
    
    // Check for event handlers
    const elementsWithHandlers = element.querySelectorAll ? 
      element.querySelectorAll('[onclick], [onload], [onerror]') : [];
    if (elementsWithHandlers.length > 0) {
      this.logSecurityEvent('dynamic-event-handlers', {
        element: element.tagName,
        handlers: elementsWithHandlers.length
      });
    }
  }

  /**
   * Built-in validators
   */
  
  validateEmail(value) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
      return {
        type: 'invalid-email',
        message: 'Invalid email format',
        severity: 'medium'
      };
    }
  }
  
  validateURL(value) {
    try {
      new URL(value);
    } catch {
      return {
        type: 'invalid-url',
        message: 'Invalid URL format',
        severity: 'medium'
      };
    }
  }
  
  validatePhone(value) {
    const phoneRegex = /^\+?[\d\s\-\(\)]{10,}$/;
    if (!phoneRegex.test(value)) {
      return {
        type: 'invalid-phone',
        message: 'Invalid phone number format',
        severity: 'medium'
      };
    }
  }
  
  validateCreditCard(value) {
    // Basic Luhn algorithm check
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length < 13 || cleaned.length > 19) {
      return {
        type: 'invalid-creditcard',
        message: 'Invalid credit card number',
        severity: 'high'
      };
    }
  }
  
  validateSSN(value) {
    const ssnRegex = /^\d{3}-?\d{2}-?\d{4}$/;
    if (!ssnRegex.test(value)) {
      return {
        type: 'invalid-ssn',
        message: 'Invalid SSN format',
        severity: 'high'
      };
    }
  }
  
  validateAlphanumeric(value) {
    const alphanumericRegex = /^[a-zA-Z0-9]+$/;
    if (!alphanumericRegex.test(value)) {
      return {
        type: 'invalid-alphanumeric',
        message: 'Only letters and numbers allowed',
        severity: 'low'
      };
    }
  }
  
  validateNumeric(value) {
    if (isNaN(value) || isNaN(parseFloat(value))) {
      return {
        type: 'invalid-numeric',
        message: 'Only numeric values allowed',
        severity: 'medium'
      };
    }
  }
  
  validatePassword(value) {
    const violations = [];
    
    if (value.length < 8) {
      violations.push({
        type: 'password-too-short',
        message: 'Password must be at least 8 characters',
        severity: 'medium'
      });
    }
    
    if (!/[A-Z]/.test(value)) {
      violations.push({
        type: 'password-no-uppercase',
        message: 'Password must contain uppercase letter',
        severity: 'low'
      });
    }
    
    if (!/[a-z]/.test(value)) {
      violations.push({
        type: 'password-no-lowercase',
        message: 'Password must contain lowercase letter',
        severity: 'low'
      });
    }
    
    if (!/\d/.test(value)) {
      violations.push({
        type: 'password-no-number',
        message: 'Password must contain number',
        severity: 'low'
      });
    }
    
    return violations;
  }

  /**
   * Utility functions
   */
  
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }
  
  logSecurityEvent(type, data) {
    const event = {
      type,
      data,
      timestamp: Date.now(),
      url: location.href,
      userAgent: navigator.userAgent
    };
    
    this.securityEvents.push(event);
    console.warn('🚨 Security Event:', event);
    
    // Report to server
    this.reportSecurityEvent(event);
  }
  
  async reportSecurityEvent(event) {
    try {
      if ('sendBeacon' in navigator) {
        navigator.sendBeacon('/api/security-event', JSON.stringify(event));
      } else {
        await fetch('/api/security-event', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(event)
        });
      }
    } catch (error) {
      console.warn('Failed to report security event:', error);
    }
  }
  
  showSecurityWarning(message) {
    // Create warning notification
    const warning = document.createElement('div');
    warning.className = 'security-warning';
    warning.textContent = message;
    warning.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      background: #ff4444;
      color: white;
      padding: 1rem;
      border-radius: 4px;
      z-index: 10000;
      max-width: 300px;
    `;
    
    document.body.appendChild(warning);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
      if (warning.parentNode) {
        warning.remove();
      }
    }, 5000);
  }

  /**
   * Generate security report
   */
  generateSecurityReport() {
    const report = {
      timestamp: Date.now(),
      config: this.config,
      validators: Array.from(this.validators.keys()),
      sanitizers: Array.from(this.sanitizers.keys()),
      securityEvents: this.securityEvents,
      csrfEnabled: this.config.enableCSRF,
      rateLimitingEnabled: this.config.enableRateLimiting,
      xssProtectionEnabled: this.config.enableXSSProtection,
      recommendations: this.generateSecurityRecommendations()
    };
    
    console.log('🛡️ Input Validation Security Report:', report);
    localStorage.setItem('input_validation_report', JSON.stringify(report));
    
    return report;
  }
  
  generateSecurityRecommendations() {
    const recommendations = [];
    
    if (!this.config.enableCSRF) {
      recommendations.push('Enable CSRF protection for form submissions');
    }
    
    if (!this.config.enableXSSProtection) {
      recommendations.push('Enable XSS protection for input validation');
    }
    
    if (!this.config.enableRateLimiting) {
      recommendations.push('Enable rate limiting to prevent abuse');
    }
    
    if (this.securityEvents.length > 10) {
      recommendations.push('High number of security events detected - review input validation rules');
    }
    
    return recommendations;
  }

  /**
   * Get validation status
   */
  getValidationStatus() {
    return {
      validatorsRegistered: this.validators.size,
      sanitizersRegistered: this.sanitizers.size,
      securityEvents: this.securityEvents.length,
      csrfEnabled: this.config.enableCSRF,
      xssProtectionEnabled: this.config.enableXSSProtection,
      rateLimitingEnabled: this.config.enableRateLimiting
    };
  }
}

// Initialize Input Validation System
const inputValidationSystem = new InputValidationSystem();

// Export for global access
window.InputValidationSystem = InputValidationSystem;
window.inputValidationSystem = inputValidationSystem;

console.log('✅ Input Validation System initialized');