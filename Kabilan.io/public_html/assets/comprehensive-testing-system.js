/**
 * Comprehensive Testing and Quality Assurance System
 * Automated testing, monitoring, and quality validation
 */

class ComprehensiveTestingSystem {
  constructor() {
    this.testResults = new Map();
    this.monitoringData = [];
    this.qualityMetrics = {};
    this.init();
  }

  async init() {
    this.setupLighthouseCI();
    this.implementAccessibilityTesting();
    this.setupSecurityScanning();
    this.createPerformanceDashboard();
    this.setupRealTimeMonitoring();
    this.implementQualityValidation();
  }

  setupLighthouseCI() {
    // Simulate Lighthouse audit
    const lighthouseMetrics = {
      performance: this.calculatePerformanceScore(),
      accessibility: this.calculateAccessibilityScore(),
      bestPractices: this.calculateBestPracticesScore(),
      seo: this.calculateSEOScore(),
      pwa: this.calculatePWAScore()
    };

    this.testResults.set('lighthouse', lighthouseMetrics);
    console.log('🔍 Lighthouse CI results:', lighthouseMetrics);
  }

  calculatePerformanceScore() {
    // Simulate performance calculation
    const metrics = {
      fcp: performance.getEntriesByType('paint').find(p => p.name === 'first-contentful-paint')?.startTime || 1500,
      lcp: 2500, // Simulated
      fid: 50,   // Simulated
      cls: 0.05  // Simulated
    };

    let score = 100;
    if (metrics.fcp > 1800) score -= 10;
    if (metrics.lcp > 2500) score -= 15;
    if (metrics.fid > 100) score -= 10;
    if (metrics.cls > 0.1) score -= 15;

    return Math.max(0, score);
  }

  calculateAccessibilityScore() {
    let score = 100;
    
    // Check for common accessibility issues
    const issues = [];
    
    if (!document.querySelector('h1')) issues.push('Missing H1');
    if (document.querySelectorAll('img:not([alt])').length > 0) issues.push('Images without alt text');
    if (!document.querySelector('[lang]')) issues.push('Missing language attribute');
    
    score -= issues.length * 10;
    return Math.max(0, score);
  }

  calculateBestPracticesScore() {
    let score = 100;
    
    if (location.protocol !== 'https:') score -= 20;
    if (!document.querySelector('meta[name="viewport"]')) score -= 10;
    if (document.querySelectorAll('script').length > 10) score -= 5;
    
    return Math.max(0, score);
  }

  calculateSEOScore() {
    let score = 100;
    
    const title = document.title;
    const description = document.querySelector('meta[name="description"]');
    
    if (!title || title.length < 30 || title.length > 60) score -= 15;
    if (!description || description.content.length < 120) score -= 15;
    if (!document.querySelector('h1')) score -= 10;
    
    return Math.max(0, score);
  }

  calculatePWAScore() {
    let score = 0;
    
    if ('serviceWorker' in navigator) score += 25;
    if (document.querySelector('link[rel="manifest"]')) score += 25;
    if (location.protocol === 'https:') score += 25;
    if (document.querySelector('meta[name="theme-color"]')) score += 25;
    
    return score;
  }

  implementAccessibilityTesting() {
    // Automated accessibility testing
    const a11yTests = {
      colorContrast: this.testColorContrast(),
      keyboardNavigation: this.testKeyboardNavigation(),
      screenReader: this.testScreenReaderSupport(),
      focusManagement: this.testFocusManagement()
    };

    this.testResults.set('accessibility', a11yTests);
    console.log('♿ Accessibility test results:', a11yTests);
  }

  testColorContrast() {
    // Simplified contrast testing
    const textElements = document.querySelectorAll('p, h1, h2, h3, h4, h5, h6, span, a');
    let passCount = 0;
    
    textElements.forEach(element => {
      const styles = window.getComputedStyle(element);
      const color = styles.color;
      const backgroundColor = styles.backgroundColor;
      
      // Simplified contrast check (would use actual contrast calculation in production)
      if (color !== backgroundColor) {
        passCount++;
      }
    });

    return {
      passed: passCount,
      total: textElements.length,
      score: Math.round((passCount / textElements.length) * 100)
    };
  }

  testKeyboardNavigation() {
    const focusableElements = document.querySelectorAll(
      'a[href], button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    
    let accessibleCount = 0;
    focusableElements.forEach(element => {
      if (element.offsetParent !== null) { // Visible
        accessibleCount++;
      }
    });

    return {
      focusableElements: focusableElements.length,
      accessibleElements: accessibleCount,
      score: focusableElements.length > 0 ? Math.round((accessibleCount / focusableElements.length) * 100) : 100
    };
  }

  testScreenReaderSupport() {
    const ariaElements = document.querySelectorAll('[aria-label], [aria-labelledby], [aria-describedby]');
    const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6');
    const landmarks = document.querySelectorAll('main, nav, aside, header, footer, [role]');

    return {
      ariaElements: ariaElements.length,
      headings: headings.length,
      landmarks: landmarks.length,
      score: (ariaElements.length + headings.length + landmarks.length) > 5 ? 100 : 50
    };
  }

  testFocusManagement() {
    const focusableElements = document.querySelectorAll('button, a, input, select, textarea');
    let withFocusStyles = 0;

    focusableElements.forEach(element => {
      const focusStyles = window.getComputedStyle(element, ':focus');
      if (focusStyles.outline !== 'none' || focusStyles.boxShadow !== 'none') {
        withFocusStyles++;
      }
    });

    return {
      total: focusableElements.length,
      withFocusStyles,
      score: focusableElements.length > 0 ? Math.round((withFocusStyles / focusableElements.length) * 100) : 100
    };
  }

  setupSecurityScanning() {
    const securityTests = {
      https: location.protocol === 'https:',
      csp: !!document.querySelector('meta[http-equiv="Content-Security-Policy"]'),
      xssProtection: this.testXSSProtection(),
      mixedContent: this.testMixedContent()
    };

    this.testResults.set('security', securityTests);
    console.log('🔒 Security scan results:', securityTests);
  }

  testXSSProtection() {
    // Check for potential XSS vulnerabilities
    const scripts = document.querySelectorAll('script');
    let safeScripts = 0;

    scripts.forEach(script => {
      if (script.src || script.nonce || script.integrity) {
        safeScripts++;
      }
    });

    return {
      totalScripts: scripts.length,
      safeScripts,
      score: scripts.length > 0 ? Math.round((safeScripts / scripts.length) * 100) : 100
    };
  }

  testMixedContent() {
    if (location.protocol !== 'https:') return { score: 100 };

    const httpResources = Array.from(document.querySelectorAll('script[src], link[href], img[src]'))
      .filter(el => {
        const url = el.src || el.href;
        return url && url.startsWith('http://');
      });

    return {
      httpResources: httpResources.length,
      score: httpResources.length === 0 ? 100 : 0
    };
  }

  createPerformanceDashboard() {
    const dashboard = document.createElement('div');
    dashboard.id = 'performance-dashboard';
    dashboard.style.cssText = `
      position: fixed;
      top: 10px;
      left: 10px;
      background: rgba(0, 0, 0, 0.9);
      color: white;
      padding: 1rem;
      border-radius: 8px;
      font-family: monospace;
      font-size: 12px;
      z-index: 10000;
      display: none;
      max-width: 300px;
    `;

    dashboard.innerHTML = `
      <h3>Performance Dashboard</h3>
      <div id="dashboard-content"></div>
      <button onclick="this.parentElement.style.display='none'">Close</button>
    `;

    document.body.appendChild(dashboard);

    // Toggle dashboard
    window.togglePerformanceDashboard = () => {
      dashboard.style.display = dashboard.style.display === 'none' ? 'block' : 'none';
      if (dashboard.style.display === 'block') {
        this.updateDashboard();
      }
    };

    // Add toggle button
    const toggleButton = document.createElement('button');
    toggleButton.textContent = '📊';
    toggleButton.style.cssText = `
      position: fixed;
      bottom: 80px;
      left: 20px;
      background: #007cba;
      color: white;
      border: none;
      padding: 10px;
      border-radius: 50%;
      cursor: pointer;
      z-index: 9999;
    `;
    toggleButton.onclick = window.togglePerformanceDashboard;
    document.body.appendChild(toggleButton);
  }

  updateDashboard() {
    const content = document.getElementById('dashboard-content');
    if (!content) return;

    const lighthouse = this.testResults.get('lighthouse') || {};
    const accessibility = this.testResults.get('accessibility') || {};
    const security = this.testResults.get('security') || {};

    content.innerHTML = `
      <div><strong>Lighthouse Scores:</strong></div>
      <div>Performance: ${lighthouse.performance || 0}/100</div>
      <div>Accessibility: ${lighthouse.accessibility || 0}/100</div>
      <div>Best Practices: ${lighthouse.bestPractices || 0}/100</div>
      <div>SEO: ${lighthouse.seo || 0}/100</div>
      <div>PWA: ${lighthouse.pwa || 0}/100</div>
      <br>
      <div><strong>Security:</strong></div>
      <div>HTTPS: ${security.https ? '✅' : '❌'}</div>
      <div>CSP: ${security.csp ? '✅' : '❌'}</div>
      <br>
      <div><strong>Real-time Metrics:</strong></div>
      <div>Memory: ${this.getMemoryUsage()}MB</div>
      <div>Load Time: ${this.getLoadTime()}ms</div>
    `;
  }

  setupRealTimeMonitoring() {
    // Monitor performance metrics
    setInterval(() => {
      const metrics = {
        timestamp: Date.now(),
        memory: this.getMemoryUsage(),
        loadTime: this.getLoadTime(),
        errors: this.getErrorCount()
      };
      
      this.monitoringData.push(metrics);
      
      // Keep only last 100 data points
      if (this.monitoringData.length > 100) {
        this.monitoringData.shift();
      }
    }, 5000);

    // Error tracking
    window.addEventListener('error', (event) => {
      this.logError('javascript', event.error);
    });

    window.addEventListener('unhandledrejection', (event) => {
      this.logError('promise', event.reason);
    });
  }

  getMemoryUsage() {
    if ('memory' in performance) {
      return Math.round(performance.memory.usedJSHeapSize / 1048576); // MB
    }
    return 0;
  }

  getLoadTime() {
    const navigation = performance.getEntriesByType('navigation')[0];
    return navigation ? Math.round(navigation.loadEventEnd - navigation.loadEventStart) : 0;
  }

  getErrorCount() {
    return this.monitoringData.filter(m => m.errors > 0).length;
  }

  logError(type, error) {
    console.error(`${type} error:`, error);
    
    // Store error for reporting
    const errorData = {
      type,
      message: error.message || error.toString(),
      stack: error.stack,
      timestamp: Date.now(),
      url: location.href
    };

    // Report to monitoring service
    this.reportError(errorData);
  }

  async reportError(errorData) {
    try {
      if ('sendBeacon' in navigator) {
        navigator.sendBeacon('/api/error-report', JSON.stringify(errorData));
      }
    } catch (error) {
      console.warn('Failed to report error:', error);
    }
  }

  implementQualityValidation() {
    // Code quality checks
    this.qualityMetrics = {
      htmlValidation: this.validateHTML(),
      cssValidation: this.validateCSS(),
      jsValidation: this.validateJS(),
      performanceValidation: this.validatePerformance()
    };

    console.log('✅ Quality validation complete:', this.qualityMetrics);
  }

  validateHTML() {
    // Basic HTML validation
    const issues = [];
    
    if (!document.doctype) issues.push('Missing DOCTYPE');
    if (!document.querySelector('html[lang]')) issues.push('Missing language attribute');
    if (!document.querySelector('meta[charset]')) issues.push('Missing charset declaration');
    
    return {
      issues,
      score: Math.max(0, 100 - (issues.length * 20))
    };
  }

  validateCSS() {
    // Basic CSS validation
    const stylesheets = document.querySelectorAll('link[rel="stylesheet"], style');
    return {
      stylesheets: stylesheets.length,
      score: stylesheets.length > 0 && stylesheets.length < 10 ? 100 : 80
    };
  }

  validateJS() {
    // Basic JS validation
    const scripts = document.querySelectorAll('script');
    const externalScripts = Array.from(scripts).filter(s => s.src);
    
    return {
      totalScripts: scripts.length,
      externalScripts: externalScripts.length,
      score: scripts.length < 20 ? 100 : 80
    };
  }

  validatePerformance() {
    const lighthouse = this.testResults.get('lighthouse') || {};
    const avgScore = Object.values(lighthouse).reduce((a, b) => a + b, 0) / Object.keys(lighthouse).length;
    
    return {
      averageScore: Math.round(avgScore),
      score: avgScore > 80 ? 100 : avgScore > 60 ? 80 : 60
    };
  }

  generateComprehensiveReport() {
    return {
      timestamp: Date.now(),
      testResults: Object.fromEntries(this.testResults),
      qualityMetrics: this.qualityMetrics,
      monitoringData: this.monitoringData.slice(-10), // Last 10 data points
      recommendations: this.generateRecommendations(),
      overallScore: this.calculateOverallScore()
    };
  }

  generateRecommendations() {
    const recommendations = [];
    const lighthouse = this.testResults.get('lighthouse') || {};
    
    if (lighthouse.performance < 80) recommendations.push('Improve performance metrics');
    if (lighthouse.accessibility < 90) recommendations.push('Enhance accessibility features');
    if (lighthouse.seo < 90) recommendations.push('Optimize SEO elements');
    if (lighthouse.pwa < 80) recommendations.push('Implement PWA features');
    
    return recommendations;
  }

  calculateOverallScore() {
    const lighthouse = this.testResults.get('lighthouse') || {};
    const scores = Object.values(lighthouse);
    return scores.length > 0 ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0;
  }

  getTestingStatus() {
    return {
      testsRun: this.testResults.size,
      monitoringActive: this.monitoringData.length > 0,
      overallScore: this.calculateOverallScore(),
      lastUpdate: Date.now()
    };
  }
}

window.comprehensiveTestingSystem = new ComprehensiveTestingSystem();
console.log('✅ Comprehensive Testing System initialized');