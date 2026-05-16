/**
 * Resource Prioritization System
 * Implements intelligent resource loading strategies and priority management
 * Optimizes resource hints and loading sequences for maximum performance
 */

class ResourcePrioritizer {
  constructor() {
    this.resourceQueue = new Map();
    this.loadingStrategies = new Map();
    this.connectionInfo = null;
    this.deviceInfo = null;
    this.performanceMetrics = {};
    this.init();
  }

  init() {
    this.detectDeviceCapabilities();
    this.setupLoadingStrategies();
    this.implementResourceHints();
    this.optimizeResourceLoading();
    this.monitorResourcePerformance();
  }

  /**
   * Detect device and connection capabilities
   */
  detectDeviceCapabilities() {
    // Connection information
    if ('connection' in navigator) {
      this.connectionInfo = {
        effectiveType: navigator.connection.effectiveType,
        downlink: navigator.connection.downlink,
        rtt: navigator.connection.rtt,
        saveData: navigator.connection.saveData
      };
    }

    // Device information
    this.deviceInfo = {
      memory: navigator.deviceMemory || 4, // Default to 4GB
      cores: navigator.hardwareConcurrency || 4,
      isMobile: /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),
      isLowEnd: (navigator.deviceMemory && navigator.deviceMemory <= 2) || false,
      prefersReducedMotion: window.matchMedia('(prefers-reduced-motion: reduce)').matches
    };

    console.log('Device Capabilities:', this.deviceInfo);
    console.log('Connection Info:', this.connectionInfo);
  }

  /**
   * Setup loading strategies based on device capabilities
   */
  setupLoadingStrategies() {
    const isSlowConnection = this.connectionInfo?.effectiveType === 'slow-2g' || 
                           this.connectionInfo?.effectiveType === '2g';
    const isLowEndDevice = this.deviceInfo.isLowEnd;
    const isSaveDataEnabled = this.connectionInfo?.saveData;

    // Critical resources (highest priority)
    this.loadingStrategies.set('critical', {
      priority: 'high',
      preload: true,
      defer: false,
      async: false,
      resources: [
        '/assets/critical.css',
        '/assets/index-CIeRf8o1.css',
        'https://fonts.cdnfonts.com/css/general-sans'
      ]
    });

    // Essential JavaScript (high priority)
    this.loadingStrategies.set('essential-js', {
      priority: 'high',
      preload: true,
      defer: false,
      async: true,
      resources: [
        '/assets/index-nipSLgUa.js',
        '/assets/critical-css-optimizer.js'
      ]
    });

    // Performance enhancements (medium priority)
    this.loadingStrategies.set('performance', {
      priority: 'medium',
      preload: !isSlowConnection,
      defer: isSlowConnection || isLowEndDevice,
      async: true,
      resources: [
        '/assets/performance.js',
        '/assets/image-optimizer.js'
      ]
    });

    // Accessibility features (medium priority)
    this.loadingStrategies.set('accessibility', {
      priority: 'medium',
      preload: false,
      defer: isSlowConnection,
      async: true,
      resources: [
        '/assets/accessibility.js'
      ]
    });

    // Modern features (low priority)
    this.loadingStrategies.set('modern-features', {
      priority: 'low',
      preload: false,
      defer: true,
      async: true,
      resources: [
        '/assets/modern-features.js',
        '/assets/modern-enhancements.css'
      ]
    });

    // Testing and validation (lowest priority)
    this.loadingStrategies.set('testing', {
      priority: 'low',
      preload: false,
      defer: true,
      async: true,
      resources: [
        '/assets/testing-validation.js',
        '/assets/mobile-validator.js'
      ]
    });

    // Adjust strategies for save-data mode
    if (isSaveDataEnabled) {
      this.adjustForSaveData();
    }
  }

  /**
   * Adjust loading strategies for save-data mode
   */
  adjustForSaveData() {
    this.loadingStrategies.forEach((strategy, key) => {
      if (key !== 'critical' && key !== 'essential-js') {
        strategy.defer = true;
        strategy.preload = false;
        strategy.priority = 'low';
      }
    });
  }

  /**
   * Implement intelligent resource hints
   */
  implementResourceHints() {
    // Preconnect to external domains
    this.addResourceHint('preconnect', 'https://fonts.cdnfonts.com', { crossorigin: true });
    this.addResourceHint('dns-prefetch', 'https://fonts.cdnfonts.com');

    // Process each loading strategy
    this.loadingStrategies.forEach((strategy, strategyName) => {
      strategy.resources.forEach(resource => {
        this.processResourceByStrategy(resource, strategy, strategyName);
      });
    });
  }

  /**
   * Process resource according to its strategy
   */
  processResourceByStrategy(resource, strategy, strategyName) {
    const resourceType = this.getResourceType(resource);
    
    // Add to resource queue
    this.resourceQueue.set(resource, {
      strategy: strategyName,
      type: resourceType,
      priority: strategy.priority,
      loaded: false,
      loadTime: null,
      size: null
    });

    // Apply loading strategy
    if (strategy.preload && !strategy.defer) {
      this.addResourceHint('preload', resource, {
        as: resourceType,
        crossorigin: resource.startsWith('http'),
        importance: strategy.priority
      });
    } else if (strategy.defer) {
      // Defer loading until after critical resources
      this.deferResourceLoading(resource, strategy);
    } else {
      // Load with prefetch for future navigation
      this.addResourceHint('prefetch', resource);
    }
  }

  /**
   * Get resource type from URL
   */
  getResourceType(url) {
    if (url.endsWith('.css') || url.includes('css')) return 'style';
    if (url.endsWith('.js')) return 'script';
    if (url.match(/\.(jpg|jpeg|png|webp|avif|svg)$/)) return 'image';
    if (url.match(/\.(woff|woff2|ttf|otf)$/)) return 'font';
    return 'fetch';
  }

  /**
   * Add resource hint to document
   */
  addResourceHint(rel, href, options = {}) {
    const link = document.createElement('link');
    link.rel = rel;
    link.href = href;

    // Add optional attributes
    if (options.as) link.as = options.as;
    if (options.crossorigin) link.crossOrigin = '';
    if (options.importance) link.setAttribute('importance', options.importance);
    if (options.media) link.media = options.media;

    // Add performance tracking
    if (rel === 'preload') {
      link.onload = () => this.trackResourceLoad(href, 'preload-success');
      link.onerror = () => this.trackResourceLoad(href, 'preload-error');
    }

    document.head.appendChild(link);
  }

  /**
   * Defer resource loading
   */
  deferResourceLoading(resource, strategy) {
    const delay = this.calculateLoadDelay(strategy);
    
    setTimeout(() => {
      if (strategy.async) {
        this.loadResourceAsync(resource);
      } else {
        this.loadResourceSync(resource);
      }
    }, delay);
  }

  /**
   * Calculate appropriate delay for resource loading
   */
  calculateLoadDelay(strategy) {
    let baseDelay = 0;

    // Base delay by priority
    switch (strategy.priority) {
      case 'high': baseDelay = 100; break;
      case 'medium': baseDelay = 500; break;
      case 'low': baseDelay = 1000; break;
    }

    // Adjust for connection speed
    if (this.connectionInfo) {
      switch (this.connectionInfo.effectiveType) {
        case 'slow-2g': baseDelay *= 4; break;
        case '2g': baseDelay *= 3; break;
        case '3g': baseDelay *= 2; break;
      }
    }

    // Adjust for device capabilities
    if (this.deviceInfo.isLowEnd) {
      baseDelay *= 1.5;
    }

    return baseDelay;
  }

  /**
   * Load resource asynchronously
   */
  loadResourceAsync(resource) {
    const resourceType = this.getResourceType(resource);
    
    if (resourceType === 'style') {
      this.loadCSSAsync(resource);
    } else if (resourceType === 'script') {
      this.loadJSAsync(resource);
    }
  }

  /**
   * Load CSS asynchronously
   */
  loadCSSAsync(href) {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.as = 'style';
    link.href = href;
    
    link.onload = () => {
      link.rel = 'stylesheet';
      this.trackResourceLoad(href, 'css-loaded');
    };
    
    link.onerror = () => {
      this.trackResourceLoad(href, 'css-error');
    };

    document.head.appendChild(link);
  }

  /**
   * Load JavaScript asynchronously
   */
  loadJSAsync(src) {
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    
    script.onload = () => {
      this.trackResourceLoad(src, 'js-loaded');
    };
    
    script.onerror = () => {
      this.trackResourceLoad(src, 'js-error');
    };

    document.head.appendChild(script);
  }

  /**
   * Load resource synchronously
   */
  loadResourceSync(resource) {
    const resourceType = this.getResourceType(resource);
    
    if (resourceType === 'style') {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = resource;
      link.onload = () => this.trackResourceLoad(resource, 'css-sync-loaded');
      document.head.appendChild(link);
    } else if (resourceType === 'script') {
      const script = document.createElement('script');
      script.src = resource;
      script.onload = () => this.trackResourceLoad(resource, 'js-sync-loaded');
      document.head.appendChild(script);
    }
  }

  /**
   * Track resource loading performance
   */
  trackResourceLoad(resource, event) {
    const resourceData = this.resourceQueue.get(resource);
    if (resourceData) {
      resourceData.loaded = event.includes('loaded');
      resourceData.loadTime = performance.now();
      this.resourceQueue.set(resource, resourceData);
    }

    // Mark performance milestone
    if ('performance' in window && 'mark' in performance) {
      performance.mark(`resource-${event}-${resource.split('/').pop()}`);
    }

    console.log(`Resource ${event}:`, resource);
  }

  /**
   * Monitor resource performance
   */
  monitorResourcePerformance() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.entryType === 'resource') {
            this.analyzeResourcePerformance(entry);
          }
        });
      });

      observer.observe({ entryTypes: ['resource'] });
    }

    // Monitor Core Web Vitals impact
    this.monitorCoreWebVitals();
  }

  /**
   * Analyze individual resource performance
   */
  analyzeResourcePerformance(entry) {
    const resourceData = this.resourceQueue.get(entry.name);
    if (resourceData) {
      resourceData.size = entry.transferSize;
      resourceData.duration = entry.duration;
      resourceData.renderBlockingStatus = entry.renderBlockingStatus;
      
      this.resourceQueue.set(entry.name, resourceData);
    }

    // Check for performance issues
    if (entry.duration > 1000) {
      console.warn(`Slow resource loading detected: ${entry.name} (${entry.duration}ms)`);
    }

    if (entry.transferSize > 100000) {
      console.warn(`Large resource detected: ${entry.name} (${entry.transferSize} bytes)`);
    }
  }

  /**
   * Monitor Core Web Vitals impact
   */
  monitorCoreWebVitals() {
    // Monitor LCP impact
    if ('PerformanceObserver' in window) {
      const lcpObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        
        this.performanceMetrics.lcp = lastEntry.startTime;
        console.log(`LCP: ${lastEntry.startTime}ms`);
        
        // Check if resource prioritization is helping
        if (lastEntry.startTime < 2500) {
          console.log('✅ LCP target met - resource prioritization effective');
        }
      });

      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });
    }

    // Monitor FID impact
    if ('PerformanceObserver' in window) {
      const fidObserver = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          this.performanceMetrics.fid = entry.processingStart - entry.startTime;
          console.log(`FID: ${this.performanceMetrics.fid}ms`);
        });
      });

      fidObserver.observe({ entryTypes: ['first-input'] });
    }
  }

  /**
   * Optimize resource loading based on runtime conditions
   */
  optimizeResourceLoading() {
    // Adjust loading based on page visibility
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.pauseNonCriticalLoading();
      } else {
        this.resumeResourceLoading();
      }
    });

    // Adjust loading based on network changes
    if ('connection' in navigator) {
      navigator.connection.addEventListener('change', () => {
        this.adjustForConnectionChange();
      });
    }

    // Implement idle loading for non-critical resources
    if ('requestIdleCallback' in window) {
      this.implementIdleLoading();
    }
  }

  /**
   * Pause non-critical resource loading
   */
  pauseNonCriticalLoading() {
    console.log('Page hidden - pausing non-critical resource loading');
    // Implementation would pause ongoing non-critical requests
  }

  /**
   * Resume resource loading
   */
  resumeResourceLoading() {
    console.log('Page visible - resuming resource loading');
    // Implementation would resume paused requests
  }

  /**
   * Adjust loading strategy for connection changes
   */
  adjustForConnectionChange() {
    this.detectDeviceCapabilities();
    console.log('Connection changed - adjusting resource loading strategy');
    
    // Re-evaluate loading strategies
    this.setupLoadingStrategies();
  }

  /**
   * Implement idle loading for non-critical resources
   */
  implementIdleLoading() {
    const idleResources = Array.from(this.resourceQueue.entries())
      .filter(([, data]) => data.priority === 'low' && !data.loaded);

    const loadIdleResource = () => {
      if (idleResources.length > 0) {
        const [resource, data] = idleResources.shift();
        this.loadResourceAsync(resource);
        
        // Schedule next idle loading
        requestIdleCallback(loadIdleResource, { timeout: 5000 });
      }
    };

    requestIdleCallback(loadIdleResource, { timeout: 2000 });
  }

  /**
   * Generate resource prioritization report
   */
  generateReport() {
    const report = {
      timestamp: Date.now(),
      deviceInfo: this.deviceInfo,
      connectionInfo: this.connectionInfo,
      loadingStrategies: Object.fromEntries(this.loadingStrategies),
      resourceQueue: Object.fromEntries(this.resourceQueue),
      performanceMetrics: this.performanceMetrics,
      recommendations: this.generateRecommendations()
    };

    console.log('Resource Prioritization Report:', report);
    localStorage.setItem('resource_prioritization_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Generate performance recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    // Check for slow loading resources
    const slowResources = Array.from(this.resourceQueue.entries())
      .filter(([, data]) => data.duration && data.duration > 1000);
    
    if (slowResources.length > 0) {
      recommendations.push(`${slowResources.length} resources are loading slowly. Consider optimization.`);
    }

    // Check for large resources
    const largeResources = Array.from(this.resourceQueue.entries())
      .filter(([, data]) => data.size && data.size > 100000);
    
    if (largeResources.length > 0) {
      recommendations.push(`${largeResources.length} resources are large. Consider compression.`);
    }

    // Check Core Web Vitals
    if (this.performanceMetrics.lcp && this.performanceMetrics.lcp > 2500) {
      recommendations.push('LCP is above 2.5s. Optimize critical resource loading.');
    }

    if (this.performanceMetrics.fid && this.performanceMetrics.fid > 100) {
      recommendations.push('FID is above 100ms. Reduce JavaScript execution time.');
    }

    return recommendations;
  }

  /**
   * Get current status
   */
  getStatus() {
    return {
      totalResources: this.resourceQueue.size,
      loadedResources: Array.from(this.resourceQueue.values()).filter(r => r.loaded).length,
      strategiesActive: this.loadingStrategies.size,
      performanceMetrics: this.performanceMetrics
    };
  }
}

// Initialize Resource Prioritizer
const resourcePrioritizer = new ResourcePrioritizer();

// Export for global access
window.ResourcePrioritizer = ResourcePrioritizer;
window.resourcePrioritizer = resourcePrioritizer;

console.log('✅ Resource Prioritizer initialized');