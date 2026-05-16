/**
 * Critical CSS Optimizer
 * Extracts and optimizes critical above-the-fold CSS for optimal performance
 * Implements resource prioritization and loading strategies
 */

class CriticalCSSOptimizer {
  constructor() {
    this.criticalCSS = '';
    this.nonCriticalCSS = '';
    this.resourceHints = new Map();
    this.performanceObserver = null;
    this.init();
  }

  init() {
    this.extractCriticalCSS();
    this.setupResourceHints();
    this.implementAsyncCSSLoading();
    this.optimizeResourcePriority();
    this.monitorPerformance();
  }

  /**
   * Extract critical above-the-fold CSS
   */
  extractCriticalCSS() {
    // Critical CSS for immediate rendering
    this.criticalCSS = `
      /* Critical Reset and Base Styles */
      *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
      html{font-family:'General Sans',-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;line-height:1.5;-webkit-text-size-adjust:100%;scroll-behavior:smooth;overflow-x:hidden}
      body{background:linear-gradient(135deg,#010103 0%,#1c1c21 100%);color:#ffffff;font-family:inherit;line-height:inherit;overflow-x:hidden;position:relative}
      
      /* Critical Layout Classes */
      .min-h-screen{min-height:100vh}
      .flex{display:flex}
      .items-center{align-items:center}
      .justify-center{justify-content:center}
      .text-center{text-align:center}
      .relative{position:relative}
      .absolute{position:absolute}
      .fixed{position:fixed}
      .inset-0{top:0;right:0;bottom:0;left:0}
      
      /* Critical Typography */
      .text-3xl{font-size:1.875rem;line-height:2.25rem}
      .text-lg{font-size:1.125rem;line-height:1.75rem}
      .font-bold{font-weight:700}
      .text-white{color:#ffffff}
      .text-gray-400{color:#9ca3af}
      
      /* Critical Spacing */
      .p-8{padding:2rem}
      .mb-4{margin-bottom:1rem}
      .mb-6{margin-bottom:1.5rem}
      
      /* Loading Indicator Styles */
      #loading-indicator{background:rgba(14,14,16,0.95);backdrop-filter:blur(10px);z-index:9999;border:1px solid rgba(255,255,255,0.1)}
      .animate-spin{animation:spin 1s linear infinite}
      @keyframes spin{to{transform:rotate(360deg)}}
      .h-32{height:8rem}
      .w-32{width:8rem}
      .rounded-full{border-radius:9999px}
      .border-b-2{border-bottom-width:2px}
      .border-white{border-color:#ffffff}
      
      /* Accessibility */
      .sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}
      .focus\\:not-sr-only:focus{position:static;width:auto;height:auto;padding:inherit;margin:inherit;overflow:visible;clip:auto;white-space:normal}
      
      /* Critical Performance Optimizations */
      #root{position:relative;z-index:1;min-height:100vh;width:100%;max-width:100vw;overflow-x:hidden}
      
      /* Critical Mobile Responsive */
      @media (max-width:767px){
        html,body{overflow-x:hidden!important;width:100%!important;max-width:100vw!important}
        .text-3xl{font-size:clamp(1.5rem,4vw,2rem)!important;line-height:1.2!important}
        .text-lg{font-size:clamp(1rem,3vw,1.25rem)!important;line-height:1.4!important}
        .p-8{padding:1rem!important}
      }
    `;

    // Inject critical CSS immediately
    this.injectCriticalCSS();
  }

  /**
   * Inject critical CSS into document head
   */
  injectCriticalCSS() {
    const criticalStyle = document.createElement('style');
    criticalStyle.id = 'critical-css';
    criticalStyle.textContent = this.criticalCSS;
    
    // Insert before any existing stylesheets for highest priority
    const firstLink = document.querySelector('link[rel="stylesheet"]');
    if (firstLink) {
      document.head.insertBefore(criticalStyle, firstLink);
    } else {
      document.head.appendChild(criticalStyle);
    }

    // Mark critical CSS as loaded
    if ('performance' in window && 'mark' in performance) {
      performance.mark('critical-css-injected');
    }
  }

  /**
   * Setup resource hints for optimal loading
   */
  setupResourceHints() {
    const hints = [
      // Preconnect to external domains
      { rel: 'preconnect', href: 'https://fonts.cdnfonts.com', crossorigin: true },
      { rel: 'dns-prefetch', href: 'https://fonts.cdnfonts.com' },
      
      // Preload critical resources
      { rel: 'preload', href: '/assets/index-CIeRf8o1.css', as: 'style', crossorigin: true },
      { rel: 'preload', href: '/assets/index-nipSLgUa.js', as: 'script', crossorigin: true },
      { rel: 'preload', href: 'https://fonts.cdnfonts.com/css/general-sans', as: 'style', crossorigin: true },
      
      // Prefetch non-critical resources
      { rel: 'prefetch', href: '/assets/modern-enhancements.css' },
      { rel: 'prefetch', href: '/assets/performance.js' },
      { rel: 'prefetch', href: '/assets/accessibility.js' }
    ];

    hints.forEach(hint => {
      const link = document.createElement('link');
      Object.keys(hint).forEach(key => {
        if (key === 'crossorigin' && hint[key]) {
          link.setAttribute('crossorigin', '');
        } else {
          link.setAttribute(key, hint[key]);
        }
      });
      
      // Add to resource hints map for tracking
      this.resourceHints.set(hint.href, {
        type: hint.rel,
        loaded: false,
        loadTime: null
      });
      
      document.head.appendChild(link);
    });
  }

  /**
   * Implement asynchronous CSS loading for non-critical styles
   */
  implementAsyncCSSLoading() {
    const nonCriticalCSS = [
      '/assets/modern-enhancements.css',
      '/assets/index-CIeRf8o1.css'
    ];

    nonCriticalCSS.forEach((cssFile, index) => {
      // Use setTimeout to defer loading after critical rendering
      setTimeout(() => {
        this.loadCSSAsync(cssFile);
      }, index * 100); // Stagger loading
    });
  }

  /**
   * Load CSS asynchronously without blocking rendering
   */
  loadCSSAsync(href) {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.as = 'style';
    link.href = href;
    link.crossOrigin = '';
    
    link.onload = () => {
      // Convert preload to stylesheet
      link.rel = 'stylesheet';
      
      // Track loading performance
      if (this.resourceHints.has(href)) {
        this.resourceHints.set(href, {
          ...this.resourceHints.get(href),
          loaded: true,
          loadTime: performance.now()
        });
      }

      // Mark performance milestone
      if ('performance' in window && 'mark' in performance) {
        performance.mark(`css-loaded-${href.split('/').pop()}`);
      }
    };

    link.onerror = () => {
      console.warn(`Failed to load CSS: ${href}`);
    };

    document.head.appendChild(link);

    // Fallback for browsers without preload support
    const noscriptFallback = document.createElement('noscript');
    noscriptFallback.innerHTML = `<link rel="stylesheet" href="${href}">`;
    document.head.appendChild(noscriptFallback);
  }

  /**
   * Optimize resource loading priority
   */
  optimizeResourcePriority() {
    // Set high priority for critical resources
    const criticalResources = document.querySelectorAll('link[rel="preload"][as="style"], link[rel="preload"][as="script"]');
    criticalResources.forEach(resource => {
      if (resource.href.includes('index-') || resource.href.includes('critical')) {
        resource.setAttribute('importance', 'high');
      }
    });

    // Set low priority for non-critical resources
    const nonCriticalResources = document.querySelectorAll('link[rel="prefetch"]');
    nonCriticalResources.forEach(resource => {
      resource.setAttribute('importance', 'low');
    });

    // Implement resource scheduling based on connection speed
    if ('connection' in navigator) {
      const connection = navigator.connection;
      if (connection.effectiveType === 'slow-2g' || connection.effectiveType === '2g') {
        // Delay non-critical resources on slow connections
        this.delayNonCriticalResources(2000);
      } else if (connection.effectiveType === '3g') {
        this.delayNonCriticalResources(1000);
      }
    }
  }

  /**
   * Delay loading of non-critical resources
   */
  delayNonCriticalResources(delay) {
    const nonCriticalLinks = document.querySelectorAll('link[rel="prefetch"]');
    nonCriticalLinks.forEach(link => {
      const originalHref = link.href;
      link.removeAttribute('href');
      
      setTimeout(() => {
        link.href = originalHref;
      }, delay);
    });
  }

  /**
   * Monitor performance metrics
   */
  monitorPerformance() {
    if ('PerformanceObserver' in window) {
      // Monitor resource loading
      this.performanceObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.entryType === 'resource' && entry.name.includes('.css')) {
            this.trackCSSPerformance(entry);
          }
        });
      });

      this.performanceObserver.observe({ entryTypes: ['resource'] });

      // Monitor paint metrics
      const paintObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.name === 'first-contentful-paint') {
            console.log(`First Contentful Paint: ${entry.startTime}ms`);
            this.reportPerformanceMetric('fcp', entry.startTime);
          }
        });
      });

      paintObserver.observe({ entryTypes: ['paint'] });
    }

    // Fallback performance tracking
    window.addEventListener('load', () => {
      setTimeout(() => {
        this.generatePerformanceReport();
      }, 1000);
    });
  }

  /**
   * Track CSS loading performance
   */
  trackCSSPerformance(entry) {
    const metrics = {
      name: entry.name,
      duration: entry.duration,
      transferSize: entry.transferSize,
      encodedBodySize: entry.encodedBodySize,
      decodedBodySize: entry.decodedBodySize,
      startTime: entry.startTime,
      responseEnd: entry.responseEnd
    };

    console.log('CSS Performance:', metrics);
    
    // Store metrics for reporting
    if (!window.cssPerformanceMetrics) {
      window.cssPerformanceMetrics = [];
    }
    window.cssPerformanceMetrics.push(metrics);
  }

  /**
   * Report performance metric to analytics
   */
  reportPerformanceMetric(metric, value) {
    // Send to analytics service or store locally
    if (window.gtag) {
      window.gtag('event', 'performance_metric', {
        metric_name: metric,
        metric_value: Math.round(value),
        custom_parameter: 'critical_css_optimization'
      });
    }

    // Store in localStorage for debugging
    const perfData = JSON.parse(localStorage.getItem('performance_metrics') || '{}');
    perfData[metric] = value;
    perfData.timestamp = Date.now();
    localStorage.setItem('performance_metrics', JSON.stringify(perfData));
  }

  /**
   * Generate comprehensive performance report
   */
  generatePerformanceReport() {
    const report = {
      timestamp: Date.now(),
      criticalCSSSize: this.criticalCSS.length,
      resourceHints: Array.from(this.resourceHints.entries()),
      performanceMarks: [],
      recommendations: []
    };

    // Collect performance marks
    if ('performance' in window && performance.getEntriesByType) {
      report.performanceMarks = performance.getEntriesByType('mark')
        .filter(mark => mark.name.includes('css') || mark.name.includes('critical'))
        .map(mark => ({ name: mark.name, startTime: mark.startTime }));
    }

    // Generate recommendations
    if (report.criticalCSSSize > 14000) {
      report.recommendations.push('Critical CSS size is large. Consider further optimization.');
    }

    const unloadedResources = Array.from(this.resourceHints.entries())
      .filter(([, data]) => !data.loaded);
    
    if (unloadedResources.length > 0) {
      report.recommendations.push(`${unloadedResources.length} resources failed to load.`);
    }

    console.log('Critical CSS Performance Report:', report);
    
    // Store report
    localStorage.setItem('critical_css_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Get current performance metrics
   */
  getPerformanceMetrics() {
    return {
      criticalCSSInjected: !!document.getElementById('critical-css'),
      resourceHintsCount: this.resourceHints.size,
      loadedResources: Array.from(this.resourceHints.values()).filter(r => r.loaded).length,
      performanceMarks: performance.getEntriesByType ? performance.getEntriesByType('mark').length : 0
    };
  }

  /**
   * Cleanup and destroy optimizer
   */
  destroy() {
    if (this.performanceObserver) {
      this.performanceObserver.disconnect();
    }
  }
}

// Initialize Critical CSS Optimizer
const criticalCSSOptimizer = new CriticalCSSOptimizer();

// Export for global access
window.CriticalCSSOptimizer = CriticalCSSOptimizer;
window.criticalCSSOptimizer = criticalCSSOptimizer;

// Performance monitoring integration
if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
  navigator.serviceWorker.controller.postMessage({
    type: 'CRITICAL_CSS_OPTIMIZED',
    metrics: criticalCSSOptimizer.getPerformanceMetrics()
  });
}

console.log('✅ Critical CSS Optimizer initialized');