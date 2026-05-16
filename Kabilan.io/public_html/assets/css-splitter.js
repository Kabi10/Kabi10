/**
 * CSS Splitting Strategy
 * Implements intelligent CSS code splitting for optimal loading performance
 * Separates critical, above-the-fold, and non-critical styles
 */

class CSSSplitter {
  constructor() {
    this.criticalCSS = new Set();
    this.aboveFoldCSS = new Set();
    this.nonCriticalCSS = new Set();
    this.mediaQueries = new Map();
    this.loadedChunks = new Set();
    this.init();
  }

  init() {
    this.analyzeCSSStructure();
    this.implementSplittingStrategy();
    this.setupLazyLoading();
    this.optimizeMediaQueries();
    this.monitorPerformance();
  }

  /**
   * Analyze existing CSS structure and categorize styles
   */
  analyzeCSSStructure() {
    // Critical styles that must be loaded immediately
    this.criticalCSS = new Set([
      // Reset and base styles
      'reset', 'normalize', 'base', 'typography',
      // Layout essentials
      'layout', 'grid', 'flexbox', 'positioning',
      // Critical components
      'loading', 'header', 'navigation', 'hero',
      // Accessibility
      'focus', 'screen-reader', 'skip-links'
    ]);

    // Above-the-fold styles (visible without scrolling)
    this.aboveFoldCSS = new Set([
      // Hero section
      'hero-section', 'intro', 'banner',
      // Primary navigation
      'main-nav', 'menu', 'logo',
      // Critical buttons and forms
      'primary-button', 'cta', 'search-form',
      // Essential animations
      'fade-in', 'slide-in', 'loading-animation'
    ]);

    // Non-critical styles (below-the-fold or interactive)
    this.nonCriticalCSS = new Set([
      // Secondary components
      'footer', 'sidebar', 'testimonials', 'gallery',
      // Interactive elements
      'modal', 'dropdown', 'tooltip', 'carousel',
      // Advanced animations
      'parallax', 'hover-effects', 'transitions',
      // Print styles
      'print', 'media-print'
    ]);

    console.log('CSS Structure Analysis Complete');
  }

  /**
   * Implement CSS splitting strategy
   */
  implementSplittingStrategy() {
    // Create critical CSS bundle
    this.createCriticalBundle();
    
    // Create above-the-fold CSS bundle
    this.createAboveFoldBundle();
    
    // Create non-critical CSS chunks
    this.createNonCriticalChunks();
    
    // Setup progressive loading
    this.setupProgressiveLoading();
  }

  /**
   * Create critical CSS bundle (inlined in HTML)
   */
  createCriticalBundle() {
    const criticalStyles = `
      /* Critical CSS Bundle - Inlined for immediate rendering */
      
      /* Reset and Base */
      *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
      html{font-family:'General Sans',-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;line-height:1.5;-webkit-text-size-adjust:100%;scroll-behavior:smooth}
      body{background:linear-gradient(135deg,#010103 0%,#1c1c21 100%);color:#ffffff;overflow-x:hidden}
      
      /* Critical Layout */
      .container{width:100%;max-width:1200px;margin:0 auto;padding:0 1rem}
      .flex{display:flex}
      .grid{display:grid}
      .hidden{display:none}
      .block{display:block}
      .inline-block{display:inline-block}
      
      /* Critical Typography */
      h1,h2,h3,h4,h5,h6{font-weight:600;line-height:1.2}
      .text-center{text-align:center}
      .text-left{text-align:left}
      .text-right{text-align:right}
      
      /* Critical Spacing */
      .p-4{padding:1rem}
      .m-4{margin:1rem}
      .mt-4{margin-top:1rem}
      .mb-4{margin-bottom:1rem}
      
      /* Loading States */
      .loading{opacity:0.7;pointer-events:none}
      .loading::after{content:'';position:absolute;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.1);z-index:1000}
      
      /* Critical Accessibility */
      .sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}
      .focus\\:not-sr-only:focus{position:static;width:auto;height:auto;padding:inherit;margin:inherit;overflow:visible;clip:auto;white-space:normal}
      
      /* Critical Mobile */
      @media (max-width:768px){
        .container{padding:0 0.5rem}
        .text-center{text-align:center}
      }
    `;

    // Inject critical CSS immediately
    const criticalStyle = document.createElement('style');
    criticalStyle.id = 'critical-css-bundle';
    criticalStyle.textContent = criticalStyles;
    document.head.insertBefore(criticalStyle, document.head.firstChild);

    this.loadedChunks.add('critical');
    console.log('✅ Critical CSS bundle loaded');
  }

  /**
   * Create above-the-fold CSS bundle
   */
  createAboveFoldBundle() {
    const aboveFoldStyles = `
      /* Above-the-Fold CSS Bundle */
      
      /* Hero Section */
      .hero{min-height:100vh;display:flex;align-items:center;justify-content:center;position:relative}
      .hero-content{text-align:center;z-index:2}
      .hero-title{font-size:clamp(2rem,5vw,4rem);font-weight:900;margin-bottom:1rem}
      .hero-subtitle{font-size:clamp(1rem,2.5vw,1.5rem);opacity:0.8;margin-bottom:2rem}
      
      /* Navigation */
      .nav{position:fixed;top:0;left:0;right:0;z-index:1000;background:rgba(1,1,3,0.9);backdrop-filter:blur(10px)}
      .nav-container{display:flex;align-items:center;justify-content:space-between;padding:1rem}
      .nav-logo{font-size:1.5rem;font-weight:700}
      .nav-menu{display:flex;list-style:none;gap:2rem}
      .nav-link{color:inherit;text-decoration:none;transition:opacity 0.3s}
      .nav-link:hover{opacity:0.7}
      
      /* Primary Buttons */
      .btn-primary{background:linear-gradient(135deg,#3b82f6,#8b5cf6);color:white;padding:0.75rem 1.5rem;border:none;border-radius:0.5rem;font-weight:600;cursor:pointer;transition:transform 0.3s}
      .btn-primary:hover{transform:translateY(-2px)}
      
      /* Loading Animation */
      .animate-spin{animation:spin 1s linear infinite}
      @keyframes spin{to{transform:rotate(360deg)}}
      
      /* Fade In Animation */
      .fade-in{animation:fadeIn 0.6s ease-out}
      @keyframes fadeIn{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}
      
      /* Mobile Navigation */
      @media (max-width:768px){
        .nav-menu{display:none}
        .nav-toggle{display:block}
        .hero-title{font-size:2.5rem}
      }
    `;

    // Load above-the-fold CSS after critical CSS
    setTimeout(() => {
      this.loadCSSChunk('above-fold', aboveFoldStyles);
    }, 100);
  }

  /**
   * Create non-critical CSS chunks
   */
  createNonCriticalChunks() {
    const chunks = {
      // Components chunk
      components: `
        /* Components CSS Chunk */
        .card{background:rgba(255,255,255,0.05);border-radius:1rem;padding:1.5rem;backdrop-filter:blur(10px)}
        .modal{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.8);display:flex;align-items:center;justify-content:center;z-index:2000}
        .dropdown{position:relative;display:inline-block}
        .tooltip{position:absolute;background:#333;color:white;padding:0.5rem;border-radius:0.25rem;font-size:0.875rem}
      `,
      
      // Animations chunk
      animations: `
        /* Animations CSS Chunk */
        .parallax{transform:translateZ(0);will-change:transform}
        .hover-lift:hover{transform:translateY(-5px);transition:transform 0.3s}
        .slide-in-left{animation:slideInLeft 0.6s ease-out}
        @keyframes slideInLeft{from{opacity:0;transform:translateX(-50px)}to{opacity:1;transform:translateX(0)}}
        .bounce{animation:bounce 2s infinite}
        @keyframes bounce{0%,20%,53%,80%,to{transform:translateY(0)}40%,43%{transform:translateY(-30px)}70%{transform:translateY(-15px)}90%{transform:translateY(-4px)}}
      `,
      
      // Layout chunk
      layout: `
        /* Layout CSS Chunk */
        .grid-2{display:grid;grid-template-columns:repeat(2,1fr);gap:2rem}
        .grid-3{display:grid;grid-template-columns:repeat(3,1fr);gap:2rem}
        .grid-4{display:grid;grid-template-columns:repeat(4,1fr);gap:2rem}
        .sidebar{width:250px;background:rgba(255,255,255,0.05);padding:2rem}
        .main-content{flex:1;padding:2rem}
        @media (max-width:768px){
          .grid-2,.grid-3,.grid-4{grid-template-columns:1fr}
          .sidebar{width:100%;order:2}
        }
      `,
      
      // Forms chunk
      forms: `
        /* Forms CSS Chunk */
        .form-group{margin-bottom:1.5rem}
        .form-label{display:block;margin-bottom:0.5rem;font-weight:600}
        .form-input{width:100%;padding:0.75rem;border:1px solid rgba(255,255,255,0.2);border-radius:0.5rem;background:rgba(255,255,255,0.05);color:inherit}
        .form-input:focus{outline:none;border-color:#3b82f6;box-shadow:0 0 0 3px rgba(59,130,246,0.1)}
        .form-error{color:#ef4444;font-size:0.875rem;margin-top:0.25rem}
      `,
      
      // Print styles chunk
      print: `
        /* Print CSS Chunk */
        @media print{
          *{background:transparent!important;color:black!important;box-shadow:none!important}
          .no-print{display:none!important}
          .print-only{display:block!important}
          a,a:visited{text-decoration:underline}
          a[href]:after{content:" (" attr(href) ")"}
          .nav,.footer,.sidebar{display:none}
          .main-content{width:100%;margin:0;padding:0}
        }
      `
    };

    // Schedule non-critical chunks for lazy loading
    Object.entries(chunks).forEach(([chunkName, styles], index) => {
      setTimeout(() => {
        this.loadCSSChunk(chunkName, styles);
      }, 500 + (index * 200));
    });
  }

  /**
   * Load CSS chunk
   */
  loadCSSChunk(chunkName, styles) {
    if (this.loadedChunks.has(chunkName)) {
      return;
    }

    const style = document.createElement('style');
    style.id = `css-chunk-${chunkName}`;
    style.textContent = styles;
    document.head.appendChild(style);

    this.loadedChunks.add(chunkName);
    
    // Mark performance milestone
    if ('performance' in window && 'mark' in performance) {
      performance.mark(`css-chunk-${chunkName}-loaded`);
    }

    console.log(`✅ CSS chunk loaded: ${chunkName}`);
  }

  /**
   * Setup lazy loading for CSS chunks
   */
  setupLazyLoading() {
    // Load chunks based on scroll position
    const loadChunkOnScroll = (chunkName, triggerPosition) => {
      const checkScroll = () => {
        if (window.scrollY > triggerPosition && !this.loadedChunks.has(chunkName)) {
          this.loadCSSChunkByName(chunkName);
          window.removeEventListener('scroll', checkScroll);
        }
      };

      window.addEventListener('scroll', checkScroll, { passive: true });
    };

    // Load chunks based on intersection
    if ('IntersectionObserver' in window) {
      this.setupIntersectionBasedLoading();
    }

    // Load chunks based on user interaction
    this.setupInteractionBasedLoading();
  }

  /**
   * Setup intersection-based CSS loading
   */
  setupIntersectionBasedLoading() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const chunkName = entry.target.dataset.cssChunk;
          if (chunkName && !this.loadedChunks.has(chunkName)) {
            this.loadCSSChunkByName(chunkName);
            observer.unobserve(entry.target);
          }
        }
      });
    }, {
      rootMargin: '100px 0px',
      threshold: 0.1
    });

    // Observe elements that need specific CSS chunks
    document.querySelectorAll('[data-css-chunk]').forEach(el => {
      observer.observe(el);
    });
  }

  /**
   * Setup interaction-based CSS loading
   */
  setupInteractionBasedLoading() {
    // Load form styles on first form interaction
    const loadFormStyles = () => {
      this.loadCSSChunkByName('forms');
      document.removeEventListener('focusin', loadFormStyles);
    };

    document.addEventListener('focusin', (e) => {
      if (e.target.matches('input, textarea, select')) {
        loadFormStyles();
      }
    });

    // Load animation styles on first hover
    const loadAnimationStyles = () => {
      this.loadCSSChunkByName('animations');
      document.removeEventListener('mouseover', loadAnimationStyles);
    };

    document.addEventListener('mouseover', loadAnimationStyles, { once: true });

    // Load print styles when print is triggered
    window.addEventListener('beforeprint', () => {
      this.loadCSSChunkByName('print');
    });
  }

  /**
   * Load CSS chunk by name
   */
  loadCSSChunkByName(chunkName) {
    // This would typically load from external files
    // For now, we'll simulate with predefined chunks
    const chunkMap = {
      'components': '/assets/css-chunks/components.css',
      'animations': '/assets/css-chunks/animations.css',
      'layout': '/assets/css-chunks/layout.css',
      'forms': '/assets/css-chunks/forms.css',
      'print': '/assets/css-chunks/print.css'
    };

    const chunkUrl = chunkMap[chunkName];
    if (chunkUrl) {
      this.loadExternalCSSChunk(chunkName, chunkUrl);
    }
  }

  /**
   * Load external CSS chunk
   */
  loadExternalCSSChunk(chunkName, url) {
    if (this.loadedChunks.has(chunkName)) {
      return;
    }

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = url;
    link.id = `css-chunk-${chunkName}`;
    
    link.onload = () => {
      this.loadedChunks.add(chunkName);
      console.log(`✅ External CSS chunk loaded: ${chunkName}`);
    };

    link.onerror = () => {
      console.warn(`❌ Failed to load CSS chunk: ${chunkName}`);
    };

    document.head.appendChild(link);
  }

  /**
   * Optimize media queries
   */
  optimizeMediaQueries() {
    // Group media queries by breakpoint
    this.mediaQueries.set('mobile', '(max-width: 767px)');
    this.mediaQueries.set('tablet', '(min-width: 768px) and (max-width: 1023px)');
    this.mediaQueries.set('desktop', '(min-width: 1024px)');
    this.mediaQueries.set('large', '(min-width: 1440px)');
    this.mediaQueries.set('print', 'print');
    this.mediaQueries.set('reduced-motion', '(prefers-reduced-motion: reduce)');
    this.mediaQueries.set('high-contrast', '(prefers-contrast: high)');

    // Load device-specific styles
    this.loadDeviceSpecificStyles();
  }

  /**
   * Load device-specific styles
   */
  loadDeviceSpecificStyles() {
    const isMobile = window.matchMedia('(max-width: 767px)').matches;
    const isTablet = window.matchMedia('(min-width: 768px) and (max-width: 1023px)').matches;
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // Load mobile-specific styles
    if (isMobile) {
      this.loadCSSChunk('mobile', `
        /* Mobile-specific styles */
        .mobile-only{display:block}
        .desktop-only{display:none}
        .touch-friendly{min-height:44px;min-width:44px}
        .mobile-nav{position:fixed;bottom:0;left:0;right:0;background:rgba(1,1,3,0.95)}
      `);
    }

    // Disable animations for users who prefer reduced motion
    if (prefersReducedMotion) {
      this.loadCSSChunk('reduced-motion', `
        /* Reduced motion styles */
        *,*::before,*::after{animation-duration:0.01ms!important;animation-iteration-count:1!important;transition-duration:0.01ms!important}
        .parallax{transform:none!important}
      `);
    }
  }

  /**
   * Monitor CSS performance
   */
  monitorPerformance() {
    // Track CSS loading performance
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach(entry => {
          if (entry.name.includes('.css') || entry.name.includes('css-chunk')) {
            console.log(`CSS Performance: ${entry.name} - ${entry.duration}ms`);
          }
        });
      });

      observer.observe({ entryTypes: ['resource'] });
    }

    // Monitor render-blocking resources
    this.monitorRenderBlocking();
  }

  /**
   * Monitor render-blocking resources
   */
  monitorRenderBlocking() {
    const checkRenderBlocking = () => {
      const stylesheets = document.querySelectorAll('link[rel="stylesheet"]');
      let renderBlockingCount = 0;

      stylesheets.forEach(link => {
        if (!link.media || link.media === 'all' || link.media === 'screen') {
          renderBlockingCount++;
        }
      });

      if (renderBlockingCount > 3) {
        console.warn(`⚠️ ${renderBlockingCount} render-blocking stylesheets detected`);
      }
    };

    // Check after DOM is loaded
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', checkRenderBlocking);
    } else {
      checkRenderBlocking();
    }
  }

  /**
   * Generate CSS splitting report
   */
  generateReport() {
    const report = {
      timestamp: Date.now(),
      loadedChunks: Array.from(this.loadedChunks),
      totalChunks: this.loadedChunks.size,
      criticalCSSSize: document.getElementById('critical-css-bundle')?.textContent.length || 0,
      mediaQueries: Object.fromEntries(this.mediaQueries),
      performanceMarks: performance.getEntriesByType ? 
        performance.getEntriesByType('mark').filter(mark => mark.name.includes('css-chunk')) : [],
      recommendations: this.generateRecommendations()
    };

    console.log('CSS Splitting Report:', report);
    localStorage.setItem('css_splitting_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Generate recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    if (this.loadedChunks.size < 3) {
      recommendations.push('Consider implementing more CSS chunks for better performance');
    }

    const criticalSize = document.getElementById('critical-css-bundle')?.textContent.length || 0;
    if (criticalSize > 14000) {
      recommendations.push('Critical CSS is large. Consider further optimization');
    }

    const stylesheetCount = document.querySelectorAll('link[rel="stylesheet"]').length;
    if (stylesheetCount > 5) {
      recommendations.push('Too many stylesheets. Consider bundling');
    }

    return recommendations;
  }

  /**
   * Get current status
   */
  getStatus() {
    return {
      loadedChunks: Array.from(this.loadedChunks),
      totalChunks: this.loadedChunks.size,
      criticalCSSLoaded: this.loadedChunks.has('critical'),
      aboveFoldCSSLoaded: this.loadedChunks.has('above-fold')
    };
  }
}

// Initialize CSS Splitter
const cssSplitter = new CSSSplitter();

// Export for global access
window.CSSSplitter = CSSSplitter;
window.cssSplitter = cssSplitter;

console.log('✅ CSS Splitter initialized');