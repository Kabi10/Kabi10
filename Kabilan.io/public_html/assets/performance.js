// Performance Optimization Script for Kabi Tharma Portfolio
// Handles lazy loading, image optimization, and performance monitoring

class PerformanceOptimizer {
  constructor() {
    this.observers = new Map();
    this.performanceMetrics = {};
    this.init();
  }

  init() {
    this.setupLazyLoading();
    this.setupImageOptimization();
    this.setupPerformanceMonitoring();
    this.setupResourceHints();
    this.setupCriticalResourceLoading();
  }

  // Lazy Loading Implementation
  setupLazyLoading() {
    if ('IntersectionObserver' in window) {
      // Lazy load images
      const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target;
            this.loadImage(img);
            imageObserver.unobserve(img);
          }
        });
      }, {
        rootMargin: '50px 0px',
        threshold: 0.1
      });

      // Lazy load 3D models
      const modelObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const model = entry.target;
            this.loadModel(model);
            modelObserver.unobserve(model);
          }
        });
      }, {
        rootMargin: '100px 0px',
        threshold: 0.1
      });

      this.observers.set('images', imageObserver);
      this.observers.set('models', modelObserver);

      // Observe elements when DOM is ready
      this.observeLazyElements();
    }
  }

  observeLazyElements() {
    // Observe images with data-src attribute
    document.querySelectorAll('img[data-src]').forEach(img => {
      this.observers.get('images').observe(img);
    });

    // Observe 3D model containers
    document.querySelectorAll('[data-model]').forEach(model => {
      this.observers.get('models').observe(model);
    });
  }

  loadImage(img) {
    const src = img.dataset.src;
    if (src) {
      // Create a new image to preload
      const newImg = new Image();
      newImg.onload = () => {
        img.src = src;
        img.classList.add('loaded');
        img.removeAttribute('data-src');
      };
      newImg.onerror = () => {
        img.classList.add('error');
        console.error('Failed to load image:', src);
      };
      newImg.src = src;
    }
  }

  loadModel(modelContainer) {
    const modelPath = modelContainer.dataset.model;
    if (modelPath) {
      // Dispatch custom event for React component to handle
      modelContainer.dispatchEvent(new CustomEvent('loadModel', {
        detail: { modelPath }
      }));
    }
  }

  // Image Optimization
  setupImageOptimization() {
    // Add WebP support detection
    this.supportsWebP = this.checkWebPSupport();
    
    // Add AVIF support detection
    this.supportsAVIF = this.checkAVIFSupport();
  }

  checkWebPSupport() {
    return new Promise(resolve => {
      const webP = new Image();
      webP.onload = webP.onerror = () => {
        resolve(webP.height === 2);
      };
      webP.src = 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA';
    });
  }

  checkAVIFSupport() {
    return new Promise(resolve => {
      const avif = new Image();
      avif.onload = avif.onerror = () => {
        resolve(avif.height === 2);
      };
      avif.src = 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAIAAAACAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgABogQEAwgMg8f8D///8WfhwB8+ErK42A=';
    });
  }

  // Performance Monitoring
  setupPerformanceMonitoring() {
    // Core Web Vitals monitoring
    this.measureCoreWebVitals();
    
    // Resource timing
    this.measureResourceTiming();
    
    // Custom performance marks
    this.setupCustomMarks();
  }

  measureCoreWebVitals() {
    // Largest Contentful Paint (LCP)
    if ('PerformanceObserver' in window) {
      const lcpObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        this.performanceMetrics.lcp = lastEntry.startTime;
        console.log('LCP:', lastEntry.startTime);
      });
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

      // First Input Delay (FID)
      const fidObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          this.performanceMetrics.fid = entry.processingStart - entry.startTime;
          console.log('FID:', entry.processingStart - entry.startTime);
        });
      });
      fidObserver.observe({ entryTypes: ['first-input'] });

      // Cumulative Layout Shift (CLS)
      let clsValue = 0;
      const clsObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          if (!entry.hadRecentInput) {
            clsValue += entry.value;
          }
        });
        this.performanceMetrics.cls = clsValue;
        console.log('CLS:', clsValue);
      });
      clsObserver.observe({ entryTypes: ['layout-shift'] });
    }
  }

  measureResourceTiming() {
    window.addEventListener('load', () => {
      setTimeout(() => {
        const resources = performance.getEntriesByType('resource');
        const slowResources = resources.filter(resource => resource.duration > 1000);
        
        if (slowResources.length > 0) {
          console.warn('Slow loading resources:', slowResources);
        }

        this.performanceMetrics.resourceCount = resources.length;
        this.performanceMetrics.totalResourceTime = resources.reduce((total, resource) => total + resource.duration, 0);
      }, 1000);
    });
  }

  setupCustomMarks() {
    // Mark when critical resources are loaded
    window.addEventListener('load', () => {
      performance.mark('portfolio-loaded');
    });

    // Mark when React app is ready
    window.addEventListener('portfolio-ready', () => {
      performance.mark('portfolio-ready');
      performance.measure('portfolio-init-time', 'navigationStart', 'portfolio-ready');
    });
  }

  // Resource Hints
  setupResourceHints() {
    // Preload critical fonts
    this.preloadFont('https://fonts.cdnfonts.com/css/general-sans');
    
    // Prefetch likely next resources
    this.prefetchResources([
      '/models/computer.glb',
      '/models/cube.glb',
      '/models/desk.glb'
    ]);
  }

  preloadFont(fontUrl) {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.href = fontUrl;
    link.as = 'style';
    link.crossOrigin = 'anonymous';
    document.head.appendChild(link);
  }

  prefetchResources(urls) {
    urls.forEach(url => {
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.href = url;
      document.head.appendChild(link);
    });
  }

  setupCriticalResourceLoading() {
    // Prioritize above-the-fold content
    const criticalImages = document.querySelectorAll('img[data-critical]');
    criticalImages.forEach(img => {
      if (img.dataset.src) {
        img.src = img.dataset.src;
        img.removeAttribute('data-src');
      }
    });
  }

  // Public API
  getPerformanceMetrics() {
    return this.performanceMetrics;
  }

  reportPerformance() {
    // This could send data to analytics service
    console.log('Performance Metrics:', this.performanceMetrics);
    
    // Example: Send to analytics
    // analytics.track('performance', this.performanceMetrics);
  }

  // Cleanup
  destroy() {
    this.observers.forEach(observer => observer.disconnect());
    this.observers.clear();
  }
}

// Initialize performance optimizer when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.portfolioPerformance = new PerformanceOptimizer();
  });
} else {
  window.portfolioPerformance = new PerformanceOptimizer();
}

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = PerformanceOptimizer;
}
