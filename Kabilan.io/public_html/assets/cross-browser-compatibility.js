/**
 * Cross-Browser Compatibility System
 * Progressive enhancement, feature detection, and automated testing
 */

class CrossBrowserCompatibility {
  constructor() {
    this.browserSupport = new Map();
    this.polyfills = new Map();
    this.fallbacks = new Map();
    this.init();
  }

  async init() {
    this.detectBrowser();
    this.setupFeatureDetection();
    this.implementProgressiveEnhancement();
    this.loadPolyfills();
    this.setupAutomatedTesting();
  }

  detectBrowser() {
    const ua = navigator.userAgent;
    this.browserSupport.set('chrome', /Chrome/.test(ua));
    this.browserSupport.set('firefox', /Firefox/.test(ua));
    this.browserSupport.set('safari', /Safari/.test(ua) && !/Chrome/.test(ua));
    this.browserSupport.set('edge', /Edg/.test(ua));
    this.browserSupport.set('ie', /Trident/.test(ua));
    
    console.log('🌐 Browser detected:', Object.fromEntries(this.browserSupport));
  }

  setupFeatureDetection() {
    // Modern feature detection
    const features = {
      'css-grid': CSS.supports('display: grid'),
      'css-flexbox': CSS.supports('display: flex'),
      'css-custom-properties': CSS.supports('--custom: value'),
      'intersection-observer': 'IntersectionObserver' in window,
      'web-workers': 'Worker' in window,
      'fetch': 'fetch' in window,
      'promises': 'Promise' in window,
      'es6-modules': 'noModule' in HTMLScriptElement.prototype
    };

    Object.entries(features).forEach(([feature, supported]) => {
      document.documentElement.classList.add(supported ? `supports-${feature}` : `no-${feature}`);
    });
  }

  implementProgressiveEnhancement() {
    // CSS Grid fallback
    if (!CSS.supports('display: grid')) {
      this.addFallbackCSS('grid', `
        .grid { display: flex; flex-wrap: wrap; }
        .grid-item { flex: 1 1 300px; }
      `);
    }

    // Flexbox fallback
    if (!CSS.supports('display: flex')) {
      this.addFallbackCSS('flexbox', `
        .flex { display: table; width: 100%; }
        .flex-item { display: table-cell; vertical-align: top; }
      `);
    }
  }

  addFallbackCSS(name, css) {
    const style = document.createElement('style');
    style.id = `fallback-${name}`;
    style.textContent = css;
    document.head.appendChild(style);
    this.fallbacks.set(name, style);
  }

  async loadPolyfills() {
    const polyfillsNeeded = [];

    if (!('fetch' in window)) polyfillsNeeded.push('fetch');
    if (!('Promise' in window)) polyfillsNeeded.push('promise');
    if (!('IntersectionObserver' in window)) polyfillsNeeded.push('intersection-observer');

    for (const polyfill of polyfillsNeeded) {
      await this.loadPolyfill(polyfill);
    }
  }

  async loadPolyfill(name) {
    const polyfillUrls = {
      'fetch': 'https://polyfill.io/v3/polyfill.min.js?features=fetch',
      'promise': 'https://polyfill.io/v3/polyfill.min.js?features=Promise',
      'intersection-observer': 'https://polyfill.io/v3/polyfill.min.js?features=IntersectionObserver'
    };

    const url = polyfillUrls[name];
    if (url) {
      const script = document.createElement('script');
      script.src = url;
      document.head.appendChild(script);
      this.polyfills.set(name, true);
    }
  }

  setupAutomatedTesting() {
    // Basic compatibility tests
    const tests = {
      'css-grid': () => CSS.supports('display: grid'),
      'flexbox': () => CSS.supports('display: flex'),
      'fetch': () => 'fetch' in window,
      'promises': () => 'Promise' in window
    };

    const results = {};
    Object.entries(tests).forEach(([test, fn]) => {
      results[test] = fn();
    });

    console.log('🧪 Compatibility test results:', results);
    return results;
  }

  getCompatibilityReport() {
    return {
      browser: Object.fromEntries(this.browserSupport),
      polyfills: Array.from(this.polyfills.keys()),
      fallbacks: Array.from(this.fallbacks.keys()),
      testResults: this.setupAutomatedTesting()
    };
  }
}

window.crossBrowserCompatibility = new CrossBrowserCompatibility();
console.log('✅ Cross-Browser Compatibility initialized');