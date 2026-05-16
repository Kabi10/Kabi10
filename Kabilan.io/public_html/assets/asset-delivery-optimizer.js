/**
 * Asset Delivery Optimizer
 * Advanced JavaScript and CSS optimization with code splitting and dynamic imports
 * Implements intelligent bundling, compression, and delivery strategies
 */

class AssetDeliveryOptimizer {
  constructor() {
    this.config = {
      chunkSizeThreshold: 50000, // 50KB
      compressionThreshold: 1024, // 1KB
      preloadThreshold: 3, // Preload top 3 chunks
      cacheStrategy: 'stale-while-revalidate',
      bundleStrategy: 'route-based'
    };
    
    this.assetRegistry = new Map();
    this.loadedChunks = new Set();
    this.preloadedAssets = new Set();
    this.performanceMetrics = {
      totalAssets: 0,
      compressedAssets: 0,
      dynamicImports: 0,
      bundleSize: 0,
      loadTimes: []
    };
    
    this.compressionSupport = {
      gzip: false,
      brotli: false
    };
    
    this.init();
  }

  async init() {
    this.detectCompressionSupport();
    this.setupCodeSplitting();
    this.setupDynamicImports();
    this.setupAssetCompression();
    this.setupIntelligentPreloading();
    this.setupBundleOptimization();
    this.setupPerformanceMonitoring();
    this.optimizeExistingAssets();
  }

  /**
   * Detect compression support
   */
  detectCompressionSupport() {
    // Check Accept-Encoding header support
    if ('CompressionStream' in window) {
      this.compressionSupport.gzip = true;
    }
    
    // Brotli support detection
    const testBrotli = () => {
      try {
        return 'DecompressionStream' in window && 
               new DecompressionStream('gzip'); // Test if compression streams work
      } catch {
        return false;
      }
    };
    
    this.compressionSupport.brotli = testBrotli();
    
    console.log('🗜️ Compression support:', this.compressionSupport);
  }

  /**
   * Setup intelligent code splitting
   */
  setupCodeSplitting() {
    // Route-based code splitting
    this.setupRouteSplitting();
    
    // Feature-based code splitting
    this.setupFeatureSplitting();
    
    // Vendor code splitting
    this.setupVendorSplitting();
  }

  /**
   * Setup route-based code splitting
   */
  setupRouteSplitting() {
    const routes = this.detectRoutes();
    
    routes.forEach(route => {
      this.createRouteChunk(route);
    });
  }

  /**
   * Detect application routes
   */
  detectRoutes() {
    const routes = [];
    
    // Detect from navigation links
    const navLinks = document.querySelectorAll('a[href^="/"], a[href^="#"]');
    navLinks.forEach(link => {
      const href = link.getAttribute('href');
      if (href && !routes.includes(href)) {
        routes.push(href);
      }
    });
    
    // Detect from data attributes
    const routeElements = document.querySelectorAll('[data-route]');
    routeElements.forEach(element => {
      const route = element.dataset.route;
      if (route && !routes.includes(route)) {
        routes.push(route);
      }
    });
    
    return routes;
  }

  /**
   * Create route-specific chunk
   */
  createRouteChunk(route) {
    const chunkName = this.sanitizeChunkName(route);
    
    this.assetRegistry.set(chunkName, {
      type: 'route',
      route,
      loaded: false,
      size: 0,
      dependencies: [],
      loadTime: null
    });
  }

  /**
   * Setup feature-based code splitting
   */
  setupFeatureSplitting() {
    const features = [
      'modal', 'carousel', 'gallery', 'forms', 'charts', 
      'animations', 'three-js', 'video-player', 'maps'
    ];
    
    features.forEach(feature => {
      this.createFeatureChunk(feature);
    });
  }

  /**
   * Create feature-specific chunk
   */
  createFeatureChunk(feature) {
    this.assetRegistry.set(`feature-${feature}`, {
      type: 'feature',
      feature,
      loaded: false,
      size: 0,
      dependencies: [],
      loadTime: null,
      trigger: `[data-feature="${feature}"], .${feature}`
    });
  }

  /**
   * Setup vendor code splitting
   */
  setupVendorSplitting() {
    const vendors = [
      'react', 'vue', 'angular', 'three', 'gsap', 
      'lodash', 'moment', 'chart-js', 'axios'
    ];
    
    vendors.forEach(vendor => {
      this.createVendorChunk(vendor);
    });
  }

  /**
   * Create vendor-specific chunk
   */
  createVendorChunk(vendor) {
    this.assetRegistry.set(`vendor-${vendor}`, {
      type: 'vendor',
      vendor,
      loaded: false,
      size: 0,
      dependencies: [],
      loadTime: null,
      priority: 'low'
    });
  }

  /**
   * Setup dynamic imports system
   */
  setupDynamicImports() {
    // Feature-based dynamic imports
    this.setupFeatureDynamicImports();
    
    // Route-based dynamic imports
    this.setupRouteDynamicImports();
    
    // Intersection-based dynamic imports
    this.setupIntersectionDynamicImports();
  }

  /**
   * Setup feature-based dynamic imports
   */
  setupFeatureDynamicImports() {
    // Observe feature triggers
    const featureObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const feature = this.detectFeatureFromElement(entry.target);
          if (feature) {
            this.loadFeatureChunk(feature);
            featureObserver.unobserve(entry.target);
          }
        }
      });
    }, {
      rootMargin: '100px 0px',
      threshold: 0.1
    });

    // Observe elements that might need features
    document.querySelectorAll('[data-feature], .modal-trigger, .carousel, .gallery').forEach(el => {
      featureObserver.observe(el);
    });
  }

  /**
   * Detect feature from element
   */
  detectFeatureFromElement(element) {
    // Check data-feature attribute
    if (element.dataset.feature) {
      return element.dataset.feature;
    }
    
    // Check class names
    const classList = Array.from(element.classList);
    const features = ['modal', 'carousel', 'gallery', 'chart', 'video'];
    
    for (const feature of features) {
      if (classList.some(cls => cls.includes(feature))) {
        return feature;
      }
    }
    
    return null;
  }

  /**
   * Load feature chunk dynamically
   */
  async loadFeatureChunk(feature) {
    const chunkName = `feature-${feature}`;
    const chunk = this.assetRegistry.get(chunkName);
    
    if (!chunk || chunk.loaded) return;
    
    const startTime = performance.now();
    
    try {
      // Dynamic import based on feature
      const module = await this.importFeatureModule(feature);
      
      chunk.loaded = true;
      chunk.loadTime = performance.now() - startTime;
      chunk.module = module;
      
      this.loadedChunks.add(chunkName);
      this.performanceMetrics.dynamicImports++;
      
      console.log(`📦 Loaded feature chunk: ${feature} (${chunk.loadTime.toFixed(2)}ms)`);
      
      // Initialize feature if it has an init method
      if (module && typeof module.init === 'function') {
        module.init();
      }
      
    } catch (error) {
      console.error(`Failed to load feature chunk: ${feature}`, error);
    }
  }

  /**
   * Import feature module dynamically
   */
  async importFeatureModule(feature) {
    const moduleMap = {
      'modal': () => import('./features/modal.js'),
      'carousel': () => import('./features/carousel.js'),
      'gallery': () => import('./features/gallery.js'),
      'forms': () => import('./features/forms.js'),
      'charts': () => import('./features/charts.js'),
      'animations': () => import('./features/animations.js'),
      'three-js': () => import('./features/three-js.js'),
      'video-player': () => import('./features/video-player.js'),
      'maps': () => import('./features/maps.js')
    };
    
    const importFunction = moduleMap[feature];
    if (importFunction) {
      return await importFunction();
    }
    
    // Fallback: try to import from features directory
    return await import(`./features/${feature}.js`);
  }

  /**
   * Setup route-based dynamic imports
   */
  setupRouteDynamicImports() {
    // Listen for navigation events
    window.addEventListener('popstate', (event) => {
      this.handleRouteChange(window.location.pathname);
    });
    
    // Intercept navigation links
    document.addEventListener('click', (event) => {
      const link = event.target.closest('a[href]');
      if (link && this.isInternalLink(link)) {
        const route = link.getAttribute('href');
        this.preloadRouteChunk(route);
      }
    });
  }

  /**
   * Handle route change
   */
  async handleRouteChange(route) {
    const chunkName = this.sanitizeChunkName(route);
    await this.loadRouteChunk(chunkName);
  }

  /**
   * Load route chunk
   */
  async loadRouteChunk(chunkName) {
    const chunk = this.assetRegistry.get(chunkName);
    
    if (!chunk || chunk.loaded) return;
    
    const startTime = performance.now();
    
    try {
      // Load route-specific assets
      const assets = await this.loadRouteAssets(chunk.route);
      
      chunk.loaded = true;
      chunk.loadTime = performance.now() - startTime;
      chunk.assets = assets;
      
      this.loadedChunks.add(chunkName);
      
      console.log(`🛣️ Loaded route chunk: ${chunk.route} (${chunk.loadTime.toFixed(2)}ms)`);
      
    } catch (error) {
      console.error(`Failed to load route chunk: ${chunk.route}`, error);
    }
  }

  /**
   * Load route-specific assets
   */
  async loadRouteAssets(route) {
    const assets = {
      css: [],
      js: []
    };
    
    // Load route-specific CSS
    const cssPath = `/assets/routes${route}.css`;
    if (await this.assetExists(cssPath)) {
      await this.loadCSS(cssPath);
      assets.css.push(cssPath);
    }
    
    // Load route-specific JavaScript
    const jsPath = `/assets/routes${route}.js`;
    if (await this.assetExists(jsPath)) {
      await this.loadJS(jsPath);
      assets.js.push(jsPath);
    }
    
    return assets;
  }

  /**
   * Setup intersection-based dynamic imports
   */
  setupIntersectionDynamicImports() {
    const intersectionObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const chunkName = entry.target.dataset.chunk;
          if (chunkName) {
            this.loadChunk(chunkName);
            intersectionObserver.unobserve(entry.target);
          }
        }
      });
    }, {
      rootMargin: '200px 0px',
      threshold: 0.01
    });

    // Observe elements with chunk data
    document.querySelectorAll('[data-chunk]').forEach(el => {
      intersectionObserver.observe(el);
    });
  }

  /**
   * Setup asset compression
   */
  setupAssetCompression() {
    // Client-side compression for dynamic content
    this.setupClientSideCompression();
    
    // Request compression-optimized assets
    this.setupCompressionHeaders();
  }

  /**
   * Setup client-side compression
   */
  setupClientSideCompression() {
    if ('CompressionStream' in window) {
      // Enable compression for large payloads
      this.enablePayloadCompression();
    }
  }

  /**
   * Enable payload compression for large data
   */
  enablePayloadCompression() {
    const originalFetch = window.fetch;
    
    window.fetch = async (url, options = {}) => {
      // Add compression headers
      const headers = new Headers(options.headers);
      
      if (this.compressionSupport.brotli) {
        headers.set('Accept-Encoding', 'br, gzip, deflate');
      } else if (this.compressionSupport.gzip) {
        headers.set('Accept-Encoding', 'gzip, deflate');
      }
      
      return originalFetch(url, {
        ...options,
        headers
      });
    };
  }

  /**
   * Setup compression headers
   */
  setupCompressionHeaders() {
    // Request compressed versions of assets
    const links = document.querySelectorAll('link[rel="stylesheet"], script[src]');
    
    links.forEach(link => {
      const url = link.href || link.src;
      if (url && this.shouldCompress(url)) {
        this.requestCompressedAsset(url);
      }
    });
  }

  /**
   * Check if asset should be compressed
   */
  shouldCompress(url) {
    const compressibleTypes = ['.css', '.js', '.json', '.svg'];
    return compressibleTypes.some(type => url.includes(type));
  }

  /**
   * Request compressed version of asset
   */
  async requestCompressedAsset(url) {
    const compressedUrl = this.getCompressedUrl(url);
    
    if (await this.assetExists(compressedUrl)) {
      console.log(`🗜️ Using compressed asset: ${compressedUrl}`);
      return compressedUrl;
    }
    
    return url;
  }

  /**
   * Get compressed URL
   */
  getCompressedUrl(url) {
    if (this.compressionSupport.brotli) {
      return url + '.br';
    } else if (this.compressionSupport.gzip) {
      return url + '.gz';
    }
    return url;
  }

  /**
   * Setup intelligent preloading
   */
  setupIntelligentPreloading() {
    // Preload based on user behavior
    this.setupBehaviorBasedPreloading();
    
    // Preload based on route prediction
    this.setupRoutePredictionPreloading();
    
    // Preload critical chunks
    this.preloadCriticalChunks();
  }

  /**
   * Setup behavior-based preloading
   */
  setupBehaviorBasedPreloading() {
    let hoverTimer;
    
    // Preload on hover with delay
    document.addEventListener('mouseenter', (event) => {
      const link = event.target.closest('a[href]');
      if (link && this.isInternalLink(link)) {
        hoverTimer = setTimeout(() => {
          this.preloadRouteChunk(link.getAttribute('href'));
        }, 100); // 100ms delay
      }
    }, true);
    
    document.addEventListener('mouseleave', () => {
      if (hoverTimer) {
        clearTimeout(hoverTimer);
      }
    }, true);
    
    // Preload on focus
    document.addEventListener('focusin', (event) => {
      const link = event.target.closest('a[href]');
      if (link && this.isInternalLink(link)) {
        this.preloadRouteChunk(link.getAttribute('href'));
      }
    });
  }

  /**
   * Setup route prediction preloading
   */
  setupRoutePredictionPreloading() {
    // Analyze navigation patterns
    this.analyzeNavigationPatterns();
    
    // Preload likely next routes
    this.preloadLikelyRoutes();
  }

  /**
   * Analyze navigation patterns
   */
  analyzeNavigationPatterns() {
    const patterns = JSON.parse(localStorage.getItem('navigation_patterns') || '{}');
    const currentRoute = window.location.pathname;
    
    // Track current route
    if (!patterns[currentRoute]) {
      patterns[currentRoute] = { visits: 0, nextRoutes: {} };
    }
    patterns[currentRoute].visits++;
    
    // Store patterns
    localStorage.setItem('navigation_patterns', JSON.stringify(patterns));
  }

  /**
   * Preload likely next routes
   */
  preloadLikelyRoutes() {
    const patterns = JSON.parse(localStorage.getItem('navigation_patterns') || '{}');
    const currentRoute = window.location.pathname;
    const currentPattern = patterns[currentRoute];
    
    if (currentPattern && currentPattern.nextRoutes) {
      // Sort by frequency and preload top routes
      const sortedRoutes = Object.entries(currentPattern.nextRoutes)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 3); // Top 3 routes
      
      sortedRoutes.forEach(([route]) => {
        this.preloadRouteChunk(route);
      });
    }
  }

  /**
   * Preload critical chunks
   */
  preloadCriticalChunks() {
    const criticalChunks = Array.from(this.assetRegistry.entries())
      .filter(([, chunk]) => chunk.priority === 'high')
      .slice(0, this.config.preloadThreshold);
    
    criticalChunks.forEach(([chunkName]) => {
      this.preloadChunk(chunkName);
    });
  }

  /**
   * Preload chunk
   */
  async preloadChunk(chunkName) {
    if (this.preloadedAssets.has(chunkName)) return;
    
    const chunk = this.assetRegistry.get(chunkName);
    if (!chunk) return;
    
    try {
      // Create preload links
      await this.createPreloadLinks(chunk);
      
      this.preloadedAssets.add(chunkName);
      console.log(`🚀 Preloaded chunk: ${chunkName}`);
      
    } catch (error) {
      console.warn(`Failed to preload chunk: ${chunkName}`, error);
    }
  }

  /**
   * Create preload links for chunk
   */
  async createPreloadLinks(chunk) {
    const assets = await this.getChunkAssets(chunk);
    
    assets.forEach(asset => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.href = asset.url;
      link.as = asset.type;
      
      if (asset.type === 'script') {
        link.crossOrigin = 'anonymous';
      }
      
      document.head.appendChild(link);
    });
  }

  /**
   * Get assets for chunk
   */
  async getChunkAssets(chunk) {
    const assets = [];
    
    // Add CSS assets
    if (chunk.css) {
      chunk.css.forEach(url => {
        assets.push({ url, type: 'style' });
      });
    }
    
    // Add JS assets
    if (chunk.js) {
      chunk.js.forEach(url => {
        assets.push({ url, type: 'script' });
      });
    }
    
    return assets;
  }

  /**
   * Setup bundle optimization
   */
  setupBundleOptimization() {
    // Analyze bundle sizes
    this.analyzeBundleSizes();
    
    // Optimize bundle loading order
    this.optimizeBundleOrder();
    
    // Setup bundle splitting strategies
    this.setupBundleSplitting();
  }

  /**
   * Analyze bundle sizes
   */
  analyzeBundleSizes() {
    const scripts = document.querySelectorAll('script[src]');
    const stylesheets = document.querySelectorAll('link[rel="stylesheet"]');
    
    [...scripts, ...stylesheets].forEach(async asset => {
      const url = asset.src || asset.href;
      const size = await this.getAssetSize(url);
      
      this.performanceMetrics.bundleSize += size;
      
      if (size > this.config.chunkSizeThreshold) {
        console.warn(`📦 Large bundle detected: ${url} (${(size / 1024).toFixed(2)}KB)`);
      }
    });
  }

  /**
   * Get asset size
   */
  async getAssetSize(url) {
    try {
      const response = await fetch(url, { method: 'HEAD' });
      const contentLength = response.headers.get('content-length');
      return contentLength ? parseInt(contentLength) : 0;
    } catch {
      return 0;
    }
  }

  /**
   * Optimize bundle loading order
   */
  optimizeBundleOrder() {
    const scripts = document.querySelectorAll('script[src]:not([async]):not([defer])');
    
    scripts.forEach((script, index) => {
      // Add defer to non-critical scripts
      if (index > 2 && !this.isCriticalScript(script.src)) {
        script.defer = true;
      }
    });
  }

  /**
   * Check if script is critical
   */
  isCriticalScript(src) {
    const criticalPatterns = ['critical', 'essential', 'polyfill', 'vendor'];
    return criticalPatterns.some(pattern => src.includes(pattern));
  }

  /**
   * Setup bundle splitting strategies
   */
  setupBundleSplitting() {
    // Implement different splitting strategies based on config
    switch (this.config.bundleStrategy) {
      case 'route-based':
        this.implementRouteSplitting();
        break;
      case 'feature-based':
        this.implementFeatureSplitting();
        break;
      case 'size-based':
        this.implementSizeSplitting();
        break;
    }
  }

  /**
   * Setup performance monitoring
   */
  setupPerformanceMonitoring() {
    // Monitor asset loading performance
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver(list => {
        list.getEntries().forEach(entry => {
          if (entry.initiatorType === 'script' || entry.initiatorType === 'link') {
            this.analyzeAssetPerformance(entry);
          }
        });
      });
      
      observer.observe({ entryTypes: ['resource'] });
    }
    
    // Monitor bundle performance
    this.monitorBundlePerformance();
  }

  /**
   * Analyze asset performance
   */
  analyzeAssetPerformance(entry) {
    const { name, duration, transferSize, encodedBodySize } = entry;
    
    this.performanceMetrics.loadTimes.push(duration);
    
    // Check for performance issues
    if (duration > 1000) {
      console.warn(`🐌 Slow asset load: ${name} (${duration.toFixed(2)}ms)`);
    }
    
    if (transferSize > 100000) {
      console.warn(`📦 Large asset: ${name} (${(transferSize / 1024).toFixed(2)}KB)`);
    }
    
    // Check compression efficiency
    if (encodedBodySize && transferSize) {
      const compressionRatio = (1 - transferSize / encodedBodySize) * 100;
      if (compressionRatio < 30) {
        console.warn(`🗜️ Poor compression: ${name} (${compressionRatio.toFixed(1)}%)`);
      }
    }
  }

  /**
   * Monitor bundle performance
   */
  monitorBundlePerformance() {
    // Track bundle load completion
    window.addEventListener('load', () => {
      const totalLoadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
      console.log(`📊 Total bundle load time: ${totalLoadTime}ms`);
      
      // Generate performance report
      setTimeout(() => {
        this.generatePerformanceReport();
      }, 1000);
    });
  }

  /**
   * Utility functions
   */
  
  sanitizeChunkName(route) {
    return route.replace(/[^a-zA-Z0-9]/g, '-').replace(/^-+|-+$/g, '');
  }
  
  isInternalLink(link) {
    const href = link.getAttribute('href');
    return href && (href.startsWith('/') || href.startsWith('#') || 
           href.startsWith(window.location.origin));
  }
  
  async assetExists(url) {
    try {
      const response = await fetch(url, { method: 'HEAD' });
      return response.ok;
    } catch {
      return false;
    }
  }
  
  async loadCSS(url) {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = url;
      link.onload = resolve;
      link.onerror = reject;
      document.head.appendChild(link);
    });
  }
  
  async loadJS(url) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }
  
  async loadChunk(chunkName) {
    const chunk = this.assetRegistry.get(chunkName);
    if (!chunk) return;
    
    switch (chunk.type) {
      case 'feature':
        return this.loadFeatureChunk(chunk.feature);
      case 'route':
        return this.loadRouteChunk(chunkName);
      case 'vendor':
        return this.loadVendorChunk(chunk.vendor);
    }
  }
  
  async loadVendorChunk(vendor) {
    // Implementation for vendor chunk loading
    console.log(`Loading vendor chunk: ${vendor}`);
  }
  
  preloadRouteChunk(route) {
    const chunkName = this.sanitizeChunkName(route);
    this.preloadChunk(chunkName);
  }

  /**
   * Generate performance report
   */
  generatePerformanceReport() {
    const avgLoadTime = this.performanceMetrics.loadTimes.length > 0 ?
      this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0) / this.performanceMetrics.loadTimes.length : 0;
    
    const report = {
      timestamp: Date.now(),
      performanceMetrics: {
        ...this.performanceMetrics,
        averageLoadTime: avgLoadTime
      },
      compressionSupport: this.compressionSupport,
      loadedChunks: Array.from(this.loadedChunks),
      preloadedAssets: Array.from(this.preloadedAssets),
      assetRegistry: Object.fromEntries(this.assetRegistry),
      recommendations: this.generateRecommendations()
    };
    
    console.log('📊 Asset Delivery Report:', report);
    localStorage.setItem('asset_delivery_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Generate optimization recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    if (this.performanceMetrics.bundleSize > 500000) {
      recommendations.push('Bundle size is large. Consider more aggressive code splitting');
    }
    
    const avgLoadTime = this.performanceMetrics.loadTimes.length > 0 ?
      this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0) / this.performanceMetrics.loadTimes.length : 0;
    
    if (avgLoadTime > 1000) {
      recommendations.push('Asset load times are high. Consider CDN or compression optimization');
    }
    
    if (this.performanceMetrics.dynamicImports < 3) {
      recommendations.push('Low dynamic import usage. Consider implementing more code splitting');
    }
    
    return recommendations;
  }

  /**
   * Get current status
   */
  getStatus() {
    return {
      totalAssets: this.performanceMetrics.totalAssets,
      loadedChunks: this.loadedChunks.size,
      preloadedAssets: this.preloadedAssets.size,
      bundleSize: this.performanceMetrics.bundleSize,
      compressionSupport: this.compressionSupport
    };
  }
}

// Initialize Asset Delivery Optimizer
const assetDeliveryOptimizer = new AssetDeliveryOptimizer();

// Export for global access
window.AssetDeliveryOptimizer = AssetDeliveryOptimizer;
window.assetDeliveryOptimizer = assetDeliveryOptimizer;

console.log('✅ Asset Delivery Optimizer initialized');