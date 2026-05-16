// Testing and Validation Suite for Kabi Tharma Portfolio
// Comprehensive testing for performance, accessibility, and functionality

class TestingValidation {
  constructor() {
    this.testResults = {
      performance: {},
      accessibility: {},
      functionality: {},
      compatibility: {},
      seo: {}
    };
    this.init();
  }

  init() {
    this.runAllTests();
  }

  async runAllTests() {
    console.log('🧪 Starting comprehensive website testing...');
    
    await this.testPerformance();
    await this.testAccessibility();
    await this.testFunctionality();
    await this.testCompatibility();
    await this.testSEO();
    
    this.generateReport();
  }

  // Performance Testing
  async testPerformance() {
    console.log('⚡ Testing Performance...');
    
    const performanceTests = {
      coreWebVitals: await this.testCoreWebVitals(),
      resourceLoading: await this.testResourceLoading(),
      imageOptimization: await this.testImageOptimization(),
      caching: await this.testCaching(),
      compression: await this.testCompression()
    };
    
    this.testResults.performance = performanceTests;
  }

  async testCoreWebVitals() {
    const results = {
      lcp: null,
      fid: null,
      cls: null,
      fcp: null,
      ttfb: null
    };

    // Largest Contentful Paint
    if ('PerformanceObserver' in window) {
      try {
        const lcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries();
          const lastEntry = entries[entries.length - 1];
          results.lcp = lastEntry.startTime;
        });
        lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

        // First Contentful Paint
        const paintEntries = performance.getEntriesByType('paint');
        const fcpEntry = paintEntries.find(entry => entry.name === 'first-contentful-paint');
        if (fcpEntry) {
          results.fcp = fcpEntry.startTime;
        }

        // Time to First Byte
        const navigationEntry = performance.getEntriesByType('navigation')[0];
        if (navigationEntry) {
          results.ttfb = navigationEntry.responseStart - navigationEntry.requestStart;
        }

        // Cumulative Layout Shift
        let clsValue = 0;
        const clsObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries();
          entries.forEach(entry => {
            if (!entry.hadRecentInput) {
              clsValue += entry.value;
            }
          });
          results.cls = clsValue;
        });
        clsObserver.observe({ entryTypes: ['layout-shift'] });

      } catch (error) {
        console.warn('Core Web Vitals measurement failed:', error);
      }
    }

    return results;
  }

  async testResourceLoading() {
    const resources = performance.getEntriesByType('resource');
    const results = {
      totalResources: resources.length,
      slowResources: resources.filter(r => r.duration > 1000),
      largeResources: resources.filter(r => r.transferSize > 1000000),
      averageLoadTime: resources.reduce((sum, r) => sum + r.duration, 0) / resources.length,
      criticalResourcesOptimized: this.checkCriticalResources(resources)
    };

    return results;
  }

  checkCriticalResources(resources) {
    const criticalResources = ['index-CIeRf8o1.css', 'index-nipSLgUa.js'];
    const criticalResourceEntries = resources.filter(r => 
      criticalResources.some(cr => r.name.includes(cr))
    );

    return {
      found: criticalResourceEntries.length,
      expected: criticalResources.length,
      loadTimes: criticalResourceEntries.map(r => ({
        name: r.name,
        duration: r.duration,
        size: r.transferSize
      }))
    };
  }

  async testImageOptimization() {
    const images = document.querySelectorAll('img');
    const results = {
      totalImages: images.length,
      imagesWithAlt: Array.from(images).filter(img => img.alt).length,
      lazyLoadedImages: Array.from(images).filter(img => img.dataset.src).length,
      modernFormats: this.checkModernImageFormats(),
      responsiveImages: Array.from(images).filter(img => img.srcset).length
    };

    return results;
  }

  checkModernImageFormats() {
    const images = document.querySelectorAll('img');
    const formats = { webp: 0, avif: 0, jpeg: 0, png: 0 };
    
    images.forEach(img => {
      const src = img.src || img.dataset.src || '';
      if (src.includes('.webp')) formats.webp++;
      if (src.includes('.avif')) formats.avif++;
      if (src.includes('.jpg') || src.includes('.jpeg')) formats.jpeg++;
      if (src.includes('.png')) formats.png++;
    });

    return formats;
  }

  async testCaching() {
    const results = {
      serviceWorkerActive: 'serviceWorker' in navigator && navigator.serviceWorker.controller,
      cacheAPISupported: 'caches' in window,
      staticResourcesCached: await this.checkStaticResourcesCaching()
    };

    return results;
  }

  async checkStaticResourcesCaching() {
    if ('caches' in window) {
      try {
        const cacheNames = await caches.keys();
        const staticCache = await caches.open(cacheNames[0] || 'static-v1.0.0');
        const cachedRequests = await staticCache.keys();
        
        return {
          cacheNames: cacheNames,
          cachedResourcesCount: cachedRequests.length,
          cachedResources: cachedRequests.map(req => req.url)
        };
      } catch (error) {
        return { error: error.message };
      }
    }
    return { supported: false };
  }

  async testCompression() {
    const testUrl = window.location.origin + '/assets/index-CIeRf8o1.css';
    
    try {
      const response = await fetch(testUrl, { method: 'HEAD' });
      const contentEncoding = response.headers.get('content-encoding');
      const contentLength = response.headers.get('content-length');
      
      return {
        compressionEnabled: !!contentEncoding,
        compressionType: contentEncoding,
        contentLength: contentLength
      };
    } catch (error) {
      return { error: error.message };
    }
  }

  // Accessibility Testing
  async testAccessibility() {
    console.log('♿ Testing Accessibility...');
    
    const accessibilityTests = {
      semanticHTML: this.testSemanticHTML(),
      ariaLabels: this.testARIALabels(),
      keyboardNavigation: this.testKeyboardNavigation(),
      colorContrast: this.testColorContrast(),
      focusManagement: this.testFocusManagement(),
      screenReaderSupport: this.testScreenReaderSupport()
    };
    
    this.testResults.accessibility = accessibilityTests;
  }

  testSemanticHTML() {
    const semanticElements = ['header', 'nav', 'main', 'section', 'article', 'aside', 'footer'];
    const results = {};
    
    semanticElements.forEach(element => {
      results[element] = document.querySelectorAll(element).length;
    });

    results.headingStructure = this.checkHeadingStructure();
    results.landmarkRoles = this.checkLandmarkRoles();

    return results;
  }

  checkHeadingStructure() {
    const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6');
    const structure = Array.from(headings).map(h => ({
      level: parseInt(h.tagName.charAt(1)),
      text: h.textContent.trim(),
      hasId: !!h.id
    }));

    return {
      total: headings.length,
      structure: structure,
      hasH1: document.querySelectorAll('h1').length > 0,
      multipleH1: document.querySelectorAll('h1').length > 1
    };
  }

  checkLandmarkRoles() {
    const landmarks = ['banner', 'navigation', 'main', 'complementary', 'contentinfo'];
    const results = {};
    
    landmarks.forEach(landmark => {
      results[landmark] = document.querySelectorAll(`[role="${landmark}"]`).length;
    });

    return results;
  }

  testARIALabels() {
    const elementsNeedingLabels = document.querySelectorAll('button, input, select, textarea, a');
    const results = {
      total: elementsNeedingLabels.length,
      withLabels: 0,
      withoutLabels: []
    };

    elementsNeedingLabels.forEach(element => {
      const hasLabel = element.getAttribute('aria-label') || 
                      element.getAttribute('aria-labelledby') ||
                      element.textContent.trim() ||
                      (element.tagName === 'INPUT' && document.querySelector(`label[for="${element.id}"]`));

      if (hasLabel) {
        results.withLabels++;
      } else {
        results.withoutLabels.push({
          tagName: element.tagName,
          type: element.type,
          id: element.id,
          className: element.className
        });
      }
    });

    return results;
  }

  testKeyboardNavigation() {
    const focusableElements = document.querySelectorAll(
      'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
    );

    const results = {
      totalFocusableElements: focusableElements.length,
      skipLinksPresent: document.querySelectorAll('.skip-link, [href="#main-content"]').length > 0,
      focusTrapsImplemented: document.querySelectorAll('[role="dialog"]').length > 0,
      keyboardShortcutsAvailable: typeof window.modernFeatures?.setupKeyboardShortcuts === 'function'
    };

    return results;
  }

  testColorContrast() {
    // Basic color contrast testing (simplified)
    const textElements = document.querySelectorAll('p, h1, h2, h3, h4, h5, h6, span, a, button');
    const results = {
      totalTextElements: textElements.length,
      highContrastModeSupported: document.documentElement.classList.contains('high-contrast') ||
                                 getComputedStyle(document.documentElement).getPropertyValue('--color-text-primary'),
      darkModeSupported: document.documentElement.classList.contains('dark-mode') ||
                        window.matchMedia('(prefers-color-scheme: dark)').matches
    };

    return results;
  }

  testFocusManagement() {
    const results = {
      focusIndicatorsPresent: this.checkFocusIndicators(),
      focusTrapsImplemented: document.querySelectorAll('[role="dialog"]').length > 0,
      focusRestorationImplemented: typeof window.accessibilityEnhancer?.setupFocusRestoration === 'function'
    };

    return results;
  }

  checkFocusIndicators() {
    // Check if focus indicators are styled
    const style = getComputedStyle(document.documentElement);
    const hasCustomFocusStyles = style.getPropertyValue('--color-info') || 
                                document.querySelector('style')?.textContent.includes(':focus');
    
    return hasCustomFocusStyles;
  }

  testScreenReaderSupport() {
    const results = {
      liveRegionsPresent: document.querySelectorAll('[aria-live]').length > 0,
      screenReaderOnlyContent: document.querySelectorAll('.sr-only').length > 0,
      ariaDescribedElements: document.querySelectorAll('[aria-describedby]').length,
      roleAttributesUsed: document.querySelectorAll('[role]').length
    };

    return results;
  }

  // Functionality Testing
  async testFunctionality() {
    console.log('🔧 Testing Functionality...');
    
    const functionalityTests = {
      navigation: this.testNavigation(),
      forms: this.testForms(),
      interactiveElements: this.testInteractiveElements(),
      responsiveDesign: this.testResponsiveDesign(),
      errorHandling: this.testErrorHandling()
    };
    
    this.testResults.functionality = functionalityTests;
  }

  testNavigation() {
    const links = document.querySelectorAll('a[href]');
    const results = {
      totalLinks: links.length,
      internalLinks: Array.from(links).filter(link => link.href.includes(window.location.origin)).length,
      externalLinks: Array.from(links).filter(link => !link.href.includes(window.location.origin)).length,
      brokenLinks: [], // Would need actual testing
      smoothScrolling: getComputedStyle(document.documentElement).scrollBehavior === 'smooth'
    };

    return results;
  }

  testForms() {
    const forms = document.querySelectorAll('form');
    const inputs = document.querySelectorAll('input, textarea, select');
    
    const results = {
      totalForms: forms.length,
      totalInputs: inputs.length,
      requiredFields: document.querySelectorAll('[required]').length,
      validationImplemented: typeof window.accessibilityEnhancer?.validateForm === 'function',
      errorHandlingImplemented: document.querySelectorAll('.error-message').length >= 0
    };

    return results;
  }

  testInteractiveElements() {
    const buttons = document.querySelectorAll('button');
    const clickableElements = document.querySelectorAll('[onclick], [data-click]');
    
    const results = {
      totalButtons: buttons.length,
      clickableElements: clickableElements.length,
      hoverEffectsImplemented: document.querySelector('style')?.textContent.includes(':hover'),
      rippleEffectsImplemented: typeof window.modernFeatures?.setupRippleEffect === 'function'
    };

    return results;
  }

  testResponsiveDesign() {
    const viewport = document.querySelector('meta[name="viewport"]');
    const mediaQueries = this.extractMediaQueries();
    
    const results = {
      viewportMetaPresent: !!viewport,
      viewportContent: viewport?.content,
      mediaQueriesCount: mediaQueries.length,
      breakpoints: mediaQueries,
      flexboxUsed: this.checkCSSFeatureUsage('flex'),
      gridUsed: this.checkCSSFeatureUsage('grid')
    };

    return results;
  }

  extractMediaQueries() {
    const stylesheets = Array.from(document.styleSheets);
    const mediaQueries = [];
    
    stylesheets.forEach(stylesheet => {
      try {
        const rules = Array.from(stylesheet.cssRules || []);
        rules.forEach(rule => {
          if (rule.type === CSSRule.MEDIA_RULE) {
            mediaQueries.push(rule.conditionText);
          }
        });
      } catch (error) {
        // Cross-origin stylesheets may not be accessible
      }
    });

    return [...new Set(mediaQueries)];
  }

  checkCSSFeatureUsage(feature) {
    const stylesheets = Array.from(document.styleSheets);
    let featureUsed = false;
    
    stylesheets.forEach(stylesheet => {
      try {
        const cssText = Array.from(stylesheet.cssRules || [])
          .map(rule => rule.cssText)
          .join(' ');
        if (cssText.includes(feature)) {
          featureUsed = true;
        }
      } catch (error) {
        // Cross-origin stylesheets may not be accessible
      }
    });

    return featureUsed;
  }

  testErrorHandling() {
    const results = {
      globalErrorHandlerPresent: typeof window.onerror === 'function',
      unhandledRejectionHandlerPresent: typeof window.onunhandledrejection === 'function',
      serviceWorkerErrorHandling: 'serviceWorker' in navigator,
      fallbackContentPresent: document.querySelectorAll('noscript').length > 0
    };

    return results;
  }

  // Compatibility Testing
  async testCompatibility() {
    console.log('🌐 Testing Compatibility...');
    
    const compatibilityTests = {
      browserFeatures: this.testBrowserFeatures(),
      polyfills: this.testPolyfills(),
      fallbacks: this.testFallbacks()
    };
    
    this.testResults.compatibility = compatibilityTests;
  }

  testBrowserFeatures() {
    const features = {
      serviceWorker: 'serviceWorker' in navigator,
      intersectionObserver: 'IntersectionObserver' in window,
      webWorkers: 'Worker' in window,
      localStorage: 'localStorage' in window,
      sessionStorage: 'sessionStorage' in window,
      fetch: 'fetch' in window,
      promises: 'Promise' in window,
      es6Modules: 'import' in document.createElement('script'),
      webGL: this.checkWebGLSupport(),
      webAudio: 'AudioContext' in window || 'webkitAudioContext' in window,
      geolocation: 'geolocation' in navigator,
      notifications: 'Notification' in window,
      speechRecognition: 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window
    };

    return features;
  }

  checkWebGLSupport() {
    try {
      const canvas = document.createElement('canvas');
      return !!(canvas.getContext('webgl') || canvas.getContext('experimental-webgl'));
    } catch (error) {
      return false;
    }
  }

  testPolyfills() {
    const results = {
      polyfillsDetected: [],
      modernFeaturesUsed: typeof window.modernFeatures !== 'undefined',
      accessibilityEnhanced: typeof window.accessibilityEnhancer !== 'undefined',
      performanceOptimized: typeof window.portfolioPerformance !== 'undefined'
    };

    return results;
  }

  testFallbacks() {
    const results = {
      noscriptPresent: document.querySelectorAll('noscript').length > 0,
      imageAltTexts: document.querySelectorAll('img[alt]').length,
      cssGridFallbacks: this.checkCSSFeatureUsage('display: flex'),
      fontFallbacks: this.checkFontFallbacks()
    };

    return results;
  }

  checkFontFallbacks() {
    const fontFamilies = getComputedStyle(document.body).fontFamily;
    return fontFamilies.includes('sans-serif') || fontFamilies.includes('serif');
  }

  // SEO Testing
  async testSEO() {
    console.log('🔍 Testing SEO...');
    
    const seoTests = {
      metaTags: this.testMetaTags(),
      structuredData: this.testStructuredData(),
      sitemap: await this.testSitemap(),
      robotsTxt: await this.testRobotsTxt(),
      socialMedia: this.testSocialMediaTags()
    };
    
    this.testResults.seo = seoTests;
  }

  testMetaTags() {
    const requiredTags = ['title', 'description', 'viewport'];
    const results = {};
    
    requiredTags.forEach(tag => {
      const element = document.querySelector(`meta[name="${tag}"], ${tag}`);
      results[tag] = {
        present: !!element,
        content: element?.content || element?.textContent,
        length: (element?.content || element?.textContent || '').length
      };
    });

    // Additional meta tags
    results.charset = !!document.querySelector('meta[charset]');
    results.canonical = !!document.querySelector('link[rel="canonical"]');
    results.robots = !!document.querySelector('meta[name="robots"]');

    return results;
  }

  testStructuredData() {
    const jsonLdScripts = document.querySelectorAll('script[type="application/ld+json"]');
    const results = {
      jsonLdPresent: jsonLdScripts.length > 0,
      jsonLdCount: jsonLdScripts.length,
      validJsonLd: []
    };

    jsonLdScripts.forEach(script => {
      try {
        const data = JSON.parse(script.textContent);
        results.validJsonLd.push(data['@type'] || 'Unknown');
      } catch (error) {
        results.validJsonLd.push('Invalid JSON');
      }
    });

    return results;
  }

  async testSitemap() {
    try {
      const response = await fetch('/sitemap.xml');
      return {
        present: response.ok,
        status: response.status
      };
    } catch (error) {
      return {
        present: false,
        error: error.message
      };
    }
  }

  async testRobotsTxt() {
    try {
      const response = await fetch('/robots.txt');
      return {
        present: response.ok,
        status: response.status
      };
    } catch (error) {
      return {
        present: false,
        error: error.message
      };
    }
  }

  testSocialMediaTags() {
    const ogTags = document.querySelectorAll('meta[property^="og:"]');
    const twitterTags = document.querySelectorAll('meta[property^="twitter:"], meta[name^="twitter:"]');
    
    const results = {
      openGraph: {
        present: ogTags.length > 0,
        count: ogTags.length,
        tags: Array.from(ogTags).map(tag => tag.getAttribute('property'))
      },
      twitter: {
        present: twitterTags.length > 0,
        count: twitterTags.length,
        tags: Array.from(twitterTags).map(tag => tag.getAttribute('property') || tag.getAttribute('name'))
      }
    };

    return results;
  }

  // Report Generation
  generateReport() {
    console.log('📊 Generating Test Report...');
    
    const report = {
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent,
      results: this.testResults,
      summary: this.generateSummary()
    };

    this.displayReport(report);
    this.saveReport(report);
  }

  generateSummary() {
    const summary = {
      performance: this.summarizePerformance(),
      accessibility: this.summarizeAccessibility(),
      functionality: this.summarizeFunctionality(),
      compatibility: this.summarizeCompatibility(),
      seo: this.summarizeSEO(),
      overallScore: 0
    };

    // Calculate overall score
    const scores = Object.values(summary).filter(s => typeof s === 'number');
    summary.overallScore = scores.reduce((sum, score) => sum + score, 0) / scores.length;

    return summary;
  }

  summarizePerformance() {
    const perf = this.testResults.performance;
    let score = 100;

    // Deduct points for performance issues
    if (perf.coreWebVitals?.lcp > 2500) score -= 20;
    if (perf.coreWebVitals?.cls > 0.1) score -= 15;
    if (perf.resourceLoading?.slowResources?.length > 0) score -= 10;
    if (!perf.caching?.serviceWorkerActive) score -= 10;

    return Math.max(0, score);
  }

  summarizeAccessibility() {
    const a11y = this.testResults.accessibility;
    let score = 100;

    // Deduct points for accessibility issues
    if (a11y.ariaLabels?.withoutLabels?.length > 0) score -= 20;
    if (!a11y.semanticHTML?.headingStructure?.hasH1) score -= 15;
    if (!a11y.keyboardNavigation?.skipLinksPresent) score -= 10;
    if (!a11y.screenReaderSupport?.liveRegionsPresent) score -= 10;

    return Math.max(0, score);
  }

  summarizeFunctionality() {
    const func = this.testResults.functionality;
    let score = 100;

    // Deduct points for functionality issues
    if (!func.responsiveDesign?.viewportMetaPresent) score -= 20;
    if (func.responsiveDesign?.mediaQueriesCount < 2) score -= 15;
    if (!func.errorHandling?.globalErrorHandlerPresent) score -= 10;

    return Math.max(0, score);
  }

  summarizeCompatibility() {
    const compat = this.testResults.compatibility;
    let score = 100;

    // Deduct points for compatibility issues
    const modernFeatures = Object.values(compat.browserFeatures || {});
    const supportedFeatures = modernFeatures.filter(Boolean).length;
    const totalFeatures = modernFeatures.length;
    
    if (supportedFeatures / totalFeatures < 0.8) score -= 20;
    if (!compat.fallbacks?.noscriptPresent) score -= 10;

    return Math.max(0, score);
  }

  summarizeSEO() {
    const seo = this.testResults.seo;
    let score = 100;

    // Deduct points for SEO issues
    if (!seo.metaTags?.title?.present) score -= 25;
    if (!seo.metaTags?.description?.present) score -= 20;
    if (!seo.structuredData?.jsonLdPresent) score -= 15;
    if (!seo.sitemap?.present) score -= 10;
    if (!seo.socialMedia?.openGraph?.present) score -= 10;

    return Math.max(0, score);
  }

  displayReport(report) {
    // Create a visual report in the console
    console.group('🧪 Website Testing Report');
    console.log('📊 Overall Score:', report.summary.overallScore.toFixed(1) + '/100');
    console.log('⚡ Performance:', report.summary.performance + '/100');
    console.log('♿ Accessibility:', report.summary.accessibility + '/100');
    console.log('🔧 Functionality:', report.summary.functionality + '/100');
    console.log('🌐 Compatibility:', report.summary.compatibility + '/100');
    console.log('🔍 SEO:', report.summary.seo + '/100');
    console.groupEnd();

    // Display detailed results
    console.group('📋 Detailed Results');
    console.log('Performance:', report.results.performance);
    console.log('Accessibility:', report.results.accessibility);
    console.log('Functionality:', report.results.functionality);
    console.log('Compatibility:', report.results.compatibility);
    console.log('SEO:', report.results.seo);
    console.groupEnd();
  }

  saveReport(report) {
    // Save report to localStorage for later analysis
    try {
      localStorage.setItem('website-test-report', JSON.stringify(report));
      console.log('💾 Test report saved to localStorage');
    } catch (error) {
      console.warn('Failed to save test report:', error);
    }
  }

  // Public API
  getLastReport() {
    try {
      const report = localStorage.getItem('website-test-report');
      return report ? JSON.parse(report) : null;
    } catch (error) {
      return null;
    }
  }

  runSpecificTest(testType) {
    switch (testType) {
      case 'performance':
        return this.testPerformance();
      case 'accessibility':
        return this.testAccessibility();
      case 'functionality':
        return this.testFunctionality();
      case 'compatibility':
        return this.testCompatibility();
      case 'seo':
        return this.testSEO();
      default:
        console.warn('Unknown test type:', testType);
        return null;
    }
  }
}

// Initialize testing when page is fully loaded
window.addEventListener('load', () => {
  setTimeout(() => {
    window.testingSuite = new TestingValidation();
  }, 2000); // Wait 2 seconds for all scripts to initialize
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = TestingValidation;
}
