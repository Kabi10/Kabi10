/**
 * Core Web Vitals Monitor
 * Real-time monitoring and reporting of Core Web Vitals metrics
 * Implements performance budget enforcement and automated alerts
 */

class CoreWebVitalsMonitor {
  constructor() {
    this.metrics = {
      lcp: { value: null, rating: null, entries: [] },
      fid: { value: null, rating: null, entries: [] },
      cls: { value: null, rating: null, entries: [] },
      fcp: { value: null, rating: null, entries: [] },
      ttfb: { value: null, rating: null, entries: [] },
      inp: { value: null, rating: null, entries: [] }
    };
    
    this.thresholds = {
      lcp: { good: 2500, needsImprovement: 4000 },
      fid: { good: 100, needsImprovement: 300 },
      cls: { good: 0.1, needsImprovement: 0.25 },
      fcp: { good: 1800, needsImprovement: 3000 },
      ttfb: { good: 800, needsImprovement: 1800 },
      inp: { good: 200, needsImprovement: 500 }
    };
    
    this.performanceBudget = {
      lcp: 2500,
      fid: 100,
      cls: 0.1,
      fcp: 1800,
      ttfb: 800,
      inp: 200
    };
    
    this.observers = new Map();
    this.reportingEnabled = true;
    this.alertsEnabled = true;
    
    this.init();
  }

  init() {
    this.setupPerformanceObservers();
    this.monitorResourceTiming();
    this.setupNavigationTiming();
    this.implementRealTimeMonitoring();
    this.setupPerformanceBudgetEnforcement();
    this.initializeReporting();
  }

  /**
   * Setup Performance Observer for Core Web Vitals
   */
  setupPerformanceObservers() {
    // Largest Contentful Paint (LCP)
    this.observeLCP();
    
    // First Input Delay (FID) and Interaction to Next Paint (INP)
    this.observeInputDelay();
    
    // Cumulative Layout Shift (CLS)
    this.observeLayoutShift();
    
    // First Contentful Paint (FCP)
    this.observePaint();
    
    // Time to First Byte (TTFB)
    this.observeNavigation();
  }

  /**
   * Observe Largest Contentful Paint
   */
  observeLCP() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        
        this.updateMetric('lcp', lastEntry.startTime, lastEntry);
        this.checkPerformanceBudget('lcp', lastEntry.startTime);
      });

      try {
        observer.observe({ entryTypes: ['largest-contentful-paint'] });
        this.observers.set('lcp', observer);
      } catch (error) {
        console.warn('LCP observation not supported:', error);
      }
    }
  }

  /**
   * Observe First Input Delay and Interaction to Next Paint
   */
  observeInputDelay() {
    if ('PerformanceObserver' in window) {
      // First Input Delay
      const fidObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          const fid = entry.processingStart - entry.startTime;
          this.updateMetric('fid', fid, entry);
          this.checkPerformanceBudget('fid', fid);
        });
      });

      try {
        fidObserver.observe({ entryTypes: ['first-input'] });
        this.observers.set('fid', fidObserver);
      } catch (error) {
        console.warn('FID observation not supported:', error);
      }

      // Interaction to Next Paint (INP)
      const inpObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.duration) {
            this.updateMetric('inp', entry.duration, entry);
            this.checkPerformanceBudget('inp', entry.duration);
          }
        });
      });

      try {
        inpObserver.observe({ entryTypes: ['event'] });
        this.observers.set('inp', inpObserver);
      } catch (error) {
        console.warn('INP observation not supported:', error);
      }
    }
  }

  /**
   * Observe Cumulative Layout Shift
   */
  observeLayoutShift() {
    if ('PerformanceObserver' in window) {
      let clsValue = 0;
      let sessionValue = 0;
      let sessionEntries = [];

      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          // Only count layout shifts without recent user input
          if (!entry.hadRecentInput) {
            const firstSessionEntry = sessionEntries[0];
            const lastSessionEntry = sessionEntries[sessionEntries.length - 1];

            // If the entry occurred less than 1 second after the previous entry
            // and less than 5 seconds after the first entry in the session,
            // include the entry in the current session. Otherwise, start a new session.
            if (sessionValue &&
                entry.startTime - lastSessionEntry.startTime < 1000 &&
                entry.startTime - firstSessionEntry.startTime < 5000) {
              sessionValue += entry.value;
              sessionEntries.push(entry);
            } else {
              sessionValue = entry.value;
              sessionEntries = [entry];
            }

            // If the current session value is larger than the current CLS value,
            // update CLS and the entries contributing to it.
            if (sessionValue > clsValue) {
              clsValue = sessionValue;
              this.updateMetric('cls', clsValue, sessionEntries);
              this.checkPerformanceBudget('cls', clsValue);
            }
          }
        });
      });

      try {
        observer.observe({ entryTypes: ['layout-shift'] });
        this.observers.set('cls', observer);
      } catch (error) {
        console.warn('CLS observation not supported:', error);
      }
    }
  }

  /**
   * Observe Paint Timing
   */
  observePaint() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.name === 'first-contentful-paint') {
            this.updateMetric('fcp', entry.startTime, entry);
            this.checkPerformanceBudget('fcp', entry.startTime);
          }
        });
      });

      try {
        observer.observe({ entryTypes: ['paint'] });
        this.observers.set('paint', observer);
      } catch (error) {
        console.warn('Paint observation not supported:', error);
      }
    }
  }

  /**
   * Observe Navigation Timing
   */
  observeNavigation() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          const ttfb = entry.responseStart - entry.requestStart;
          this.updateMetric('ttfb', ttfb, entry);
          this.checkPerformanceBudget('ttfb', ttfb);
        });
      });

      try {
        observer.observe({ entryTypes: ['navigation'] });
        this.observers.set('navigation', observer);
      } catch (error) {
        console.warn('Navigation observation not supported:', error);
      }
    }
  }

  /**
   * Monitor resource timing for performance insights
   */
  monitorResourceTiming() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          this.analyzeResourcePerformance(entry);
        });
      });

      try {
        observer.observe({ entryTypes: ['resource'] });
        this.observers.set('resource', observer);
      } catch (error) {
        console.warn('Resource timing observation not supported:', error);
      }
    }
  }

  /**
   * Setup navigation timing monitoring
   */
  setupNavigationTiming() {
    window.addEventListener('load', () => {
      // Use setTimeout to ensure all resources are loaded
      setTimeout(() => {
        this.collectNavigationMetrics();
      }, 0);
    });
  }

  /**
   * Collect navigation timing metrics
   */
  collectNavigationMetrics() {
    if ('performance' in window && performance.getEntriesByType) {
      const navigation = performance.getEntriesByType('navigation')[0];
      
      if (navigation) {
        const metrics = {
          dns: navigation.domainLookupEnd - navigation.domainLookupStart,
          tcp: navigation.connectEnd - navigation.connectStart,
          ssl: navigation.secureConnectionStart > 0 ? 
               navigation.connectEnd - navigation.secureConnectionStart : 0,
          ttfb: navigation.responseStart - navigation.requestStart,
          download: navigation.responseEnd - navigation.responseStart,
          domProcessing: navigation.domContentLoadedEventStart - navigation.responseEnd,
          domComplete: navigation.domComplete - navigation.domContentLoadedEventStart,
          loadComplete: navigation.loadEventEnd - navigation.loadEventStart
        };

        this.analyzeNavigationMetrics(metrics);
      }
    }
  }

  /**
   * Update metric value and rating
   */
  updateMetric(metricName, value, entry) {
    const metric = this.metrics[metricName];
    metric.value = value;
    metric.rating = this.getRating(metricName, value);
    
    if (Array.isArray(entry)) {
      metric.entries = entry;
    } else {
      metric.entries.push(entry);
    }

    // Log metric update
    console.log(`📊 ${metricName.toUpperCase()}: ${value.toFixed(2)}${this.getUnit(metricName)} (${metric.rating})`);

    // Trigger real-time update
    this.triggerMetricUpdate(metricName, metric);
  }

  /**
   * Get rating for metric value
   */
  getRating(metricName, value) {
    const thresholds = this.thresholds[metricName];
    if (!thresholds) return 'unknown';

    if (value <= thresholds.good) return 'good';
    if (value <= thresholds.needsImprovement) return 'needs-improvement';
    return 'poor';
  }

  /**
   * Get unit for metric
   */
  getUnit(metricName) {
    switch (metricName) {
      case 'cls': return '';
      case 'fid':
      case 'inp':
      case 'lcp':
      case 'fcp':
      case 'ttfb': return 'ms';
      default: return '';
    }
  }

  /**
   * Check performance budget compliance
   */
  checkPerformanceBudget(metricName, value) {
    const budget = this.performanceBudget[metricName];
    
    if (value > budget) {
      const overage = value - budget;
      const percentage = ((overage / budget) * 100).toFixed(1);
      
      console.warn(`⚠️ Performance Budget Exceeded: ${metricName.toUpperCase()} is ${overage.toFixed(2)}${this.getUnit(metricName)} over budget (${percentage}% over)`);
      
      if (this.alertsEnabled) {
        this.triggerPerformanceAlert(metricName, value, budget, percentage);
      }
    } else {
      console.log(`✅ Performance Budget OK: ${metricName.toUpperCase()} within budget`);
    }
  }

  /**
   * Trigger performance alert
   */
  triggerPerformanceAlert(metricName, value, budget, percentage) {
    const alert = {
      type: 'performance-budget-exceeded',
      metric: metricName,
      value,
      budget,
      overage: value - budget,
      percentage,
      timestamp: Date.now(),
      url: window.location.href
    };

    // Store alert
    this.storeAlert(alert);

    // Show visual alert
    this.showPerformanceAlert(alert);

    // Send to analytics
    this.reportToAnalytics('performance_budget_exceeded', alert);
  }

  /**
   * Show visual performance alert
   */
  showPerformanceAlert(alert) {
    const alertElement = document.createElement('div');
    alertElement.className = 'performance-alert';
    alertElement.innerHTML = `
      <div class="performance-alert-content">
        <span class="alert-icon">⚠️</span>
        <div class="alert-text">
          <strong>Performance Budget Exceeded</strong>
          <br>${alert.metric.toUpperCase()}: ${alert.value.toFixed(2)}${this.getUnit(alert.metric)} 
          (${alert.percentage}% over budget)
        </div>
        <button onclick="this.parentElement.parentElement.remove()" class="alert-close">×</button>
      </div>
    `;

    document.body.appendChild(alertElement);

    // Auto-remove after 10 seconds
    setTimeout(() => {
      if (alertElement.parentElement) {
        alertElement.remove();
      }
    }, 10000);
  }

  /**
   * Implement real-time monitoring dashboard
   */
  implementRealTimeMonitoring() {
    // Create monitoring dashboard
    this.createMonitoringDashboard();
    
    // Update dashboard every second
    setInterval(() => {
      this.updateDashboard();
    }, 1000);
  }

  /**
   * Create monitoring dashboard
   */
  createMonitoringDashboard() {
    const dashboard = document.createElement('div');
    dashboard.id = 'cwv-dashboard';
    dashboard.className = 'cwv-dashboard hidden';
    dashboard.innerHTML = `
      <div class="cwv-dashboard-header">
        <h3>Core Web Vitals Monitor</h3>
        <button onclick="this.parentElement.parentElement.classList.toggle('hidden')" class="cwv-toggle">📊</button>
      </div>
      <div class="cwv-dashboard-content">
        <div class="cwv-metric" data-metric="lcp">
          <span class="cwv-label">LCP</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
        <div class="cwv-metric" data-metric="fid">
          <span class="cwv-label">FID</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
        <div class="cwv-metric" data-metric="cls">
          <span class="cwv-label">CLS</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
        <div class="cwv-metric" data-metric="fcp">
          <span class="cwv-label">FCP</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
        <div class="cwv-metric" data-metric="ttfb">
          <span class="cwv-label">TTFB</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
        <div class="cwv-metric" data-metric="inp">
          <span class="cwv-label">INP</span>
          <span class="cwv-value">-</span>
          <span class="cwv-rating">-</span>
        </div>
      </div>
      <div class="cwv-dashboard-actions">
        <button onclick="coreWebVitalsMonitor.generateReport()">Generate Report</button>
        <button onclick="coreWebVitalsMonitor.exportMetrics()">Export Data</button>
      </div>
    `;

    document.body.appendChild(dashboard);

    // Add toggle button
    const toggleButton = document.createElement('button');
    toggleButton.className = 'cwv-dashboard-toggle';
    toggleButton.innerHTML = '📊';
    toggleButton.onclick = () => dashboard.classList.toggle('hidden');
    document.body.appendChild(toggleButton);
  }

  /**
   * Update dashboard with current metrics
   */
  updateDashboard() {
    const dashboard = document.getElementById('cwv-dashboard');
    if (!dashboard) return;

    Object.entries(this.metrics).forEach(([metricName, metric]) => {
      const metricElement = dashboard.querySelector(`[data-metric="${metricName}"]`);
      if (metricElement && metric.value !== null) {
        const valueElement = metricElement.querySelector('.cwv-value');
        const ratingElement = metricElement.querySelector('.cwv-rating');
        
        valueElement.textContent = `${metric.value.toFixed(2)}${this.getUnit(metricName)}`;
        ratingElement.textContent = metric.rating;
        ratingElement.className = `cwv-rating ${metric.rating}`;
      }
    });
  }

  /**
   * Trigger metric update event
   */
  triggerMetricUpdate(metricName, metric) {
    const event = new CustomEvent('cwv-metric-updated', {
      detail: { metricName, metric }
    });
    document.dispatchEvent(event);
  }

  /**
   * Setup performance budget enforcement
   */
  setupPerformanceBudgetEnforcement() {
    // Monitor for budget violations
    document.addEventListener('cwv-metric-updated', (event) => {
      const { metricName, metric } = event.detail;
      
      if (metric.rating === 'poor') {
        this.handlePoorPerformance(metricName, metric);
      }
    });
  }

  /**
   * Handle poor performance detection
   */
  handlePoorPerformance(metricName, metric) {
    console.warn(`🚨 Poor performance detected: ${metricName.toUpperCase()} = ${metric.value.toFixed(2)}${this.getUnit(metricName)}`);
    
    // Implement performance optimizations based on metric
    switch (metricName) {
      case 'lcp':
        this.optimizeLCP();
        break;
      case 'fid':
      case 'inp':
        this.optimizeInteractivity();
        break;
      case 'cls':
        this.optimizeLayoutStability();
        break;
      case 'fcp':
        this.optimizeFCP();
        break;
      case 'ttfb':
        this.optimizeTTFB();
        break;
    }
  }

  /**
   * Optimize LCP performance
   */
  optimizeLCP() {
    console.log('🔧 Applying LCP optimizations...');
    
    // Preload LCP image if detected
    const lcpElements = document.querySelectorAll('img, video, [style*="background-image"]');
    lcpElements.forEach(element => {
      if (element.getBoundingClientRect().top < window.innerHeight) {
        if (element.tagName === 'IMG' && !element.loading) {
          element.loading = 'eager';
        }
      }
    });
  }

  /**
   * Optimize interactivity (FID/INP)
   */
  optimizeInteractivity() {
    console.log('🔧 Applying interactivity optimizations...');
    
    // Defer non-critical JavaScript
    const scripts = document.querySelectorAll('script[src]:not([async]):not([defer])');
    scripts.forEach(script => {
      if (!script.src.includes('critical') && !script.src.includes('essential')) {
        script.defer = true;
      }
    });
  }

  /**
   * Optimize layout stability (CLS)
   */
  optimizeLayoutStability() {
    console.log('🔧 Applying layout stability optimizations...');
    
    // Add dimensions to images without them
    const images = document.querySelectorAll('img:not([width]):not([height])');
    images.forEach(img => {
      if (img.naturalWidth && img.naturalHeight) {
        img.width = img.naturalWidth;
        img.height = img.naturalHeight;
      }
    });
  }

  /**
   * Optimize FCP
   */
  optimizeFCP() {
    console.log('🔧 Applying FCP optimizations...');
    
    // Inline critical CSS if not already done
    if (!document.getElementById('critical-css-inline')) {
      // This would be handled by the CSS optimizer
      console.log('Critical CSS optimization needed');
    }
  }

  /**
   * Optimize TTFB
   */
  optimizeTTFB() {
    console.log('🔧 Applying TTFB optimizations...');
    
    // Enable service worker caching if not active
    if ('serviceWorker' in navigator && !navigator.serviceWorker.controller) {
      console.log('Service Worker activation needed for TTFB optimization');
    }
  }

  /**
   * Analyze resource performance
   */
  analyzeResourcePerformance(entry) {
    const duration = entry.duration;
    const size = entry.transferSize;
    
    // Flag slow resources
    if (duration > 1000) {
      console.warn(`🐌 Slow resource: ${entry.name} (${duration.toFixed(2)}ms)`);
    }
    
    // Flag large resources
    if (size > 100000) {
      console.warn(`📦 Large resource: ${entry.name} (${(size / 1024).toFixed(2)}KB)`);
    }
    
    // Check for render-blocking resources
    if (entry.renderBlockingStatus === 'blocking') {
      console.warn(`🚫 Render-blocking resource: ${entry.name}`);
    }
  }

  /**
   * Analyze navigation metrics
   */
  analyzeNavigationMetrics(metrics) {
    console.log('📈 Navigation Timing Analysis:', metrics);
    
    // Check for performance issues
    if (metrics.dns > 100) {
      console.warn('⚠️ Slow DNS lookup detected');
    }
    
    if (metrics.tcp > 100) {
      console.warn('⚠️ Slow TCP connection detected');
    }
    
    if (metrics.ttfb > 800) {
      console.warn('⚠️ Slow TTFB detected');
    }
    
    if (metrics.domProcessing > 1000) {
      console.warn('⚠️ Slow DOM processing detected');
    }
  }

  /**
   * Initialize reporting system
   */
  initializeReporting() {
    // Report metrics when page becomes hidden
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.reportMetrics();
      }
    });

    // Report metrics before page unload
    window.addEventListener('beforeunload', () => {
      this.reportMetrics();
    });

    // Report metrics periodically
    setInterval(() => {
      this.reportMetrics();
    }, 30000); // Every 30 seconds
  }

  /**
   * Report metrics to analytics
   */
  reportMetrics() {
    if (!this.reportingEnabled) return;

    Object.entries(this.metrics).forEach(([metricName, metric]) => {
      if (metric.value !== null) {
        this.reportToAnalytics('core_web_vital', {
          metric_name: metricName,
          metric_value: metric.value,
          metric_rating: metric.rating,
          page_url: window.location.href,
          user_agent: navigator.userAgent,
          connection_type: navigator.connection?.effectiveType || 'unknown'
        });
      }
    });
  }

  /**
   * Report to analytics service
   */
  reportToAnalytics(eventName, data) {
    // Google Analytics 4
    if (typeof gtag !== 'undefined') {
      gtag('event', eventName, data);
    }

    // Custom analytics endpoint
    if (navigator.sendBeacon) {
      const payload = JSON.stringify({
        event: eventName,
        data,
        timestamp: Date.now(),
        url: window.location.href
      });
      
      navigator.sendBeacon('/api/analytics', payload);
    }

    // Store locally for offline reporting
    this.storeMetricLocally(eventName, data);
  }

  /**
   * Store metric locally
   */
  storeMetricLocally(eventName, data) {
    try {
      const stored = JSON.parse(localStorage.getItem('cwv_metrics') || '[]');
      stored.push({
        event: eventName,
        data,
        timestamp: Date.now()
      });
      
      // Keep only last 100 entries
      if (stored.length > 100) {
        stored.splice(0, stored.length - 100);
      }
      
      localStorage.setItem('cwv_metrics', JSON.stringify(stored));
    } catch (error) {
      console.warn('Failed to store metrics locally:', error);
    }
  }

  /**
   * Store alert
   */
  storeAlert(alert) {
    try {
      const alerts = JSON.parse(localStorage.getItem('performance_alerts') || '[]');
      alerts.push(alert);
      
      // Keep only last 50 alerts
      if (alerts.length > 50) {
        alerts.splice(0, alerts.length - 50);
      }
      
      localStorage.setItem('performance_alerts', JSON.stringify(alerts));
    } catch (error) {
      console.warn('Failed to store alert:', error);
    }
  }

  /**
   * Generate comprehensive report
   */
  generateReport() {
    const report = {
      timestamp: Date.now(),
      url: window.location.href,
      userAgent: navigator.userAgent,
      connection: navigator.connection ? {
        effectiveType: navigator.connection.effectiveType,
        downlink: navigator.connection.downlink,
        rtt: navigator.connection.rtt
      } : null,
      metrics: this.metrics,
      performanceBudget: this.performanceBudget,
      budgetCompliance: this.calculateBudgetCompliance(),
      recommendations: this.generateRecommendations(),
      alerts: JSON.parse(localStorage.getItem('performance_alerts') || '[]')
    };

    console.log('📊 Core Web Vitals Report:', report);
    
    // Store report
    localStorage.setItem('cwv_report', JSON.stringify(report));
    
    // Download report
    this.downloadReport(report);
    
    return report;
  }

  /**
   * Calculate budget compliance
   */
  calculateBudgetCompliance() {
    const compliance = {};
    
    Object.entries(this.metrics).forEach(([metricName, metric]) => {
      if (metric.value !== null) {
        const budget = this.performanceBudget[metricName];
        compliance[metricName] = {
          withinBudget: metric.value <= budget,
          overage: Math.max(0, metric.value - budget),
          percentage: ((metric.value / budget) * 100).toFixed(1)
        };
      }
    });
    
    return compliance;
  }

  /**
   * Generate performance recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    Object.entries(this.metrics).forEach(([metricName, metric]) => {
      if (metric.rating === 'poor') {
        switch (metricName) {
          case 'lcp':
            recommendations.push('Optimize Largest Contentful Paint: Preload critical images, optimize server response times, eliminate render-blocking resources');
            break;
          case 'fid':
            recommendations.push('Improve First Input Delay: Reduce JavaScript execution time, break up long tasks, use web workers');
            break;
          case 'cls':
            recommendations.push('Reduce Cumulative Layout Shift: Set dimensions for images and videos, avoid inserting content above existing content');
            break;
          case 'fcp':
            recommendations.push('Optimize First Contentful Paint: Inline critical CSS, eliminate render-blocking resources, optimize fonts');
            break;
          case 'ttfb':
            recommendations.push('Improve Time to First Byte: Optimize server response times, use CDN, implement caching');
            break;
          case 'inp':
            recommendations.push('Optimize Interaction to Next Paint: Reduce JavaScript execution time, optimize event handlers');
            break;
        }
      }
    });
    
    return recommendations;
  }

  /**
   * Export metrics data
   */
  exportMetrics() {
    const data = {
      metrics: this.metrics,
      storedMetrics: JSON.parse(localStorage.getItem('cwv_metrics') || '[]'),
      alerts: JSON.parse(localStorage.getItem('performance_alerts') || '[]'),
      exportedAt: new Date().toISOString()
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `cwv-metrics-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    
    URL.revokeObjectURL(url);
  }

  /**
   * Download report
   */
  downloadReport(report) {
    const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `cwv-report-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    
    URL.revokeObjectURL(url);
  }

  /**
   * Get current metrics
   */
  getMetrics() {
    return this.metrics;
  }

  /**
   * Get performance score
   */
  getPerformanceScore() {
    let totalScore = 0;
    let metricCount = 0;

    Object.entries(this.metrics).forEach(([metricName, metric]) => {
      if (metric.value !== null && metric.rating) {
        let score = 0;
        switch (metric.rating) {
          case 'good': score = 100; break;
          case 'needs-improvement': score = 50; break;
          case 'poor': score = 0; break;
        }
        totalScore += score;
        metricCount++;
      }
    });

    return metricCount > 0 ? Math.round(totalScore / metricCount) : 0;
  }

  /**
   * Cleanup observers
   */
  destroy() {
    this.observers.forEach(observer => {
      observer.disconnect();
    });
    this.observers.clear();
  }
}

// Add CSS for dashboard and alerts
const cwvStyles = `
  .cwv-dashboard {
    position: fixed;
    top: 20px;
    left: 20px;
    background: rgba(0, 0, 0, 0.9);
    color: white;
    padding: 1rem;
    border-radius: 8px;
    font-family: monospace;
    font-size: 12px;
    z-index: 10000;
    min-width: 300px;
    backdrop-filter: blur(10px);
  }
  
  .cwv-dashboard.hidden {
    display: none;
  }
  
  .cwv-dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    padding-bottom: 0.5rem;
  }
  
  .cwv-dashboard-header h3 {
    margin: 0;
    font-size: 14px;
  }
  
  .cwv-toggle {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    font-size: 16px;
  }
  
  .cwv-dashboard-content {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
    margin-bottom: 1rem;
  }
  
  .cwv-metric {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
  }
  
  .cwv-label {
    font-weight: bold;
  }
  
  .cwv-rating.good {
    color: #10b981;
  }
  
  .cwv-rating.needs-improvement {
    color: #f59e0b;
  }
  
  .cwv-rating.poor {
    color: #ef4444;
  }
  
  .cwv-dashboard-actions {
    display: flex;
    gap: 0.5rem;
  }
  
  .cwv-dashboard-actions button {
    flex: 1;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    border-radius: 4px;
    cursor: pointer;
    font-size: 11px;
  }
  
  .cwv-dashboard-actions button:hover {
    background: rgba(255, 255, 255, 0.3);
  }
  
  .cwv-dashboard-toggle {
    position: fixed;
    bottom: 20px;
    left: 20px;
    background: rgba(0, 0, 0, 0.8);
    border: none;
    color: white;
    padding: 1rem;
    border-radius: 50%;
    cursor: pointer;
    font-size: 20px;
    z-index: 9999;
    backdrop-filter: blur(10px);
  }
  
  .performance-alert {
    position: fixed;
    top: 20px;
    right: 20px;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: white;
    padding: 1rem;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    z-index: 10001;
    max-width: 400px;
    animation: slideInRight 0.3s ease-out;
  }
  
  .performance-alert-content {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .alert-icon {
    font-size: 24px;
    flex-shrink: 0;
  }
  
  .alert-text {
    flex: 1;
    font-size: 14px;
  }
  
  .alert-close {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    font-size: 20px;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  @media (max-width: 768px) {
    .cwv-dashboard {
      top: 10px;
      left: 10px;
      right: 10px;
      min-width: auto;
    }
    
    .cwv-dashboard-content {
      grid-template-columns: 1fr;
    }
    
    .performance-alert {
      top: 10px;
      right: 10px;
      left: 10px;
      max-width: none;
    }
  }
`;

// Inject styles
const styleSheet = document.createElement('style');
styleSheet.textContent = cwvStyles;
document.head.appendChild(styleSheet);

// Initialize Core Web Vitals Monitor
const coreWebVitalsMonitor = new CoreWebVitalsMonitor();

// Export for global access
window.CoreWebVitalsMonitor = CoreWebVitalsMonitor;
window.coreWebVitalsMonitor = coreWebVitalsMonitor;

console.log('✅ Core Web Vitals Monitor initialized');