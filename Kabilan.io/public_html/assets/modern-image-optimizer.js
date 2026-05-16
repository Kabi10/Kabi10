/**
 * Modern Image Optimization Pipeline
 * Advanced WebP/AVIF conversion, responsive images, and intelligent loading
 * Implements next-generation image optimization strategies
 */

class ModernImageOptimizer {
  constructor() {
    this.supportedFormats = {
      webp: false,
      avif: false,
      heic: false,
      jxl: false
    };
    
    this.config = {
      lazyLoadOffset: '50px',
      progressiveThreshold: 50000, // 50KB
      compressionQuality: {
        avif: 0.8,
        webp: 0.85,
        jpeg: 0.9
      },
      breakpoints: [320, 480, 768, 1024, 1200, 1600, 1920],
      densities: [1, 1.5, 2, 3],
      formats: ['avif', 'webp', 'jpeg', 'png']
    };
    
    this.observers = new Map();
    this.imageCache = new Map();
    this.performanceMetrics = {
      totalImages: 0,
      optimizedImages: 0,
      bytesLoaded: 0,
      bytesSaved: 0,
      loadTimes: [],
      formatUsage: {}
    };
    
    this.connectionInfo = null;
    this.devicePixelRatio = window.devicePixelRatio || 1;
    
    this.init();
  }

  async init() {
    await this.detectFormatSupport();
    this.detectConnectionInfo();
    this.setupAdvancedLazyLoading();
    this.setupResponsiveImageSystem();
    this.setupProgressiveLoading();
    this.setupImageErrorHandling();
    this.setupPerformanceMonitoring();
    this.optimizeExistingImages();
    this.preloadCriticalImages();
    this.setupImagePreprocessing();
  }

  /**
   * Enhanced format detection for modern image formats
   */
  async detectFormatSupport() {
    const testImages = {
      webp: 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA',
      avif: 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAIAAAACAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgABogQEAwgMg8f8D///8WfhwB8+ErK42A=',
      heic: 'data:image/heic;base64,AAAAGGZ0eXBoZWljAAAAAG1pZjFoZWljbWlhZg==',
      jxl: 'data:image/jxl;base64,/woIELASCAgQAFwASxLFgkWAHL0xqnCBCV0qDp901Te/5QM='
    };

    const formatPromises = Object.entries(testImages).map(([format, dataUrl]) => 
      this.supportsFormat(format, dataUrl)
    );

    const results = await Promise.all(formatPromises);
    
    Object.keys(testImages).forEach((format, index) => {
      this.supportedFormats[format] = results[index];
    });
    
    // Add classes to document for CSS targeting
    Object.entries(this.supportedFormats).forEach(([format, supported]) => {
      if (supported) {
        document.documentElement.classList.add(`supports-${format}`);
      }
    });
    
    console.log('📸 Modern image format support:', this.supportedFormats);
    
    // Store support info for service worker
    if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'IMAGE_FORMAT_SUPPORT',
        formats: this.supportedFormats
      });
    }
  }

  /**
   * Test support for specific image format
   */
  supportsFormat(format, dataUrl) {
    return new Promise(resolve => {
      const img = new Image();
      img.onload = () => resolve(img.height === 2);
      img.onerror = () => resolve(false);
      img.src = dataUrl;
      
      // Timeout after 1 second
      setTimeout(() => resolve(false), 1000);
    });
  }

  /**
   * Detect connection information for adaptive loading
   */
  detectConnectionInfo() {
    if ('connection' in navigator) {
      this.connectionInfo = {
        effectiveType: navigator.connection.effectiveType,
        downlink: navigator.connection.downlink,
        rtt: navigator.connection.rtt,
        saveData: navigator.connection.saveData
      };
      
      // Listen for connection changes
      navigator.connection.addEventListener('change', () => {
        this.detectConnectionInfo();
        this.adjustQualityForConnection();
      });
    }
    
    console.log('🌐 Connection info:', this.connectionInfo);
  }

  /**
   * Setup advanced lazy loading with Intersection Observer
   */
  setupAdvancedLazyLoading() {
    if ('IntersectionObserver' in window) {
      // Main lazy loading observer
      const lazyObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadImage(entry.target);
            lazyObserver.unobserve(entry.target);
          }
        });
      }, {
        rootMargin: this.config.lazyLoadOffset,
        threshold: 0.1
      });

      // Preload observer (larger margin for preloading)
      const preloadObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.preloadImage(entry.target);
            preloadObserver.unobserve(entry.target);
          }
        });
      }, {
        rootMargin: '200px 0px',
        threshold: 0.01
      });

      this.observers.set('lazy', lazyObserver);
      this.observers.set('preload', preloadObserver);

      // Observe existing images
      this.observeImages();
    }
  }

  /**
   * Observe images for lazy loading
   */
  observeImages() {
    const lazyImages = document.querySelectorAll('img[data-src], img[loading="lazy"]');
    const preloadImages = document.querySelectorAll('img[data-preload]');

    lazyImages.forEach(img => {
      this.observers.get('lazy')?.observe(img);
    });

    preloadImages.forEach(img => {
      this.observers.get('preload')?.observe(img);
    });
  }

  /**
   * Setup responsive image system with modern formats
   */
  setupResponsiveImageSystem() {
    const responsiveImages = document.querySelectorAll('img[data-responsive], picture');
    
    responsiveImages.forEach(element => {
      if (element.tagName === 'IMG') {
        this.convertToResponsiveImage(element);
      } else if (element.tagName === 'PICTURE') {
        this.optimizePictureElement(element);
      }
    });
  }

  /**
   * Convert regular image to responsive with modern formats
   */
  convertToResponsiveImage(img) {
    const baseSrc = img.dataset.src || img.src;
    const alt = img.alt || '';
    const className = img.className || '';
    
    // Create picture element
    const picture = document.createElement('picture');
    picture.className = className;
    
    // Generate sources for different formats
    const sources = this.generateResponsiveSources(baseSrc);
    
    sources.forEach(source => {
      const sourceElement = document.createElement('source');
      sourceElement.srcset = source.srcset;
      sourceElement.type = source.type;
      sourceElement.sizes = source.sizes;
      picture.appendChild(sourceElement);
    });
    
    // Create fallback img
    const fallbackImg = document.createElement('img');
    fallbackImg.src = baseSrc;
    fallbackImg.alt = alt;
    fallbackImg.loading = 'lazy';
    fallbackImg.decoding = 'async';
    
    picture.appendChild(fallbackImg);
    
    // Replace original image
    img.parentNode.replaceChild(picture, img);
    
    this.performanceMetrics.optimizedImages++;
  }

  /**
   * Generate responsive sources for different formats and sizes
   */
  generateResponsiveSources(baseSrc) {
    const sources = [];
    const basePath = baseSrc.substring(0, baseSrc.lastIndexOf('.'));
    const extension = baseSrc.substring(baseSrc.lastIndexOf('.') + 1);
    
    // Generate sizes string
    const sizes = '(max-width: 480px) 100vw, (max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw';
    
    // Generate sources for supported modern formats
    if (this.supportedFormats.avif) {
      sources.push({
        type: 'image/avif',
        srcset: this.generateSrcSet(basePath, 'avif'),
        sizes
      });
    }
    
    if (this.supportedFormats.webp) {
      sources.push({
        type: 'image/webp',
        srcset: this.generateSrcSet(basePath, 'webp'),
        sizes
      });
    }
    
    // Fallback to original format
    sources.push({
      type: `image/${extension}`,
      srcset: this.generateSrcSet(basePath, extension),
      sizes
    });
    
    return sources;
  }

  /**
   * Generate srcset for specific format
   */
  generateSrcSet(basePath, format) {
    const srcsetEntries = [];
    
    this.config.breakpoints.forEach(width => {
      // Generate for different pixel densities
      this.config.densities.forEach(density => {
        const actualWidth = Math.round(width * density);
        const descriptor = density === 1 ? `${width}w` : `${actualWidth}w`;
        srcsetEntries.push(`${basePath}-${actualWidth}.${format} ${descriptor}`);
      });
    });
    
    return srcsetEntries.join(', ');
  }

  /**
   * Optimize existing picture elements
   */
  optimizePictureElement(picture) {
    const sources = picture.querySelectorAll('source');
    const img = picture.querySelector('img');
    
    // Add modern format sources if not present
    const hasAvif = Array.from(sources).some(s => s.type === 'image/avif');
    const hasWebp = Array.from(sources).some(s => s.type === 'image/webp');
    
    if (!hasAvif && this.supportedFormats.avif) {
      this.addModernFormatSource(picture, 'avif');
    }
    
    if (!hasWebp && this.supportedFormats.webp) {
      this.addModernFormatSource(picture, 'webp');
    }
    
    // Optimize img element
    if (img) {
      img.loading = 'lazy';
      img.decoding = 'async';
    }
  }

  /**
   * Add modern format source to picture element
   */
  addModernFormatSource(picture, format) {
    const img = picture.querySelector('img');
    if (!img || !img.src) return;
    
    const basePath = img.src.substring(0, img.src.lastIndexOf('.'));
    const source = document.createElement('source');
    
    source.type = `image/${format}`;
    source.srcset = this.generateSrcSet(basePath, format);
    source.sizes = '(max-width: 768px) 100vw, 50vw';
    
    // Insert before the first source or img
    const firstChild = picture.firstElementChild;
    picture.insertBefore(source, firstChild);
  }

  /**
   * Setup progressive loading with blur-up effect
   */
  setupProgressiveLoading() {
    const progressiveImages = document.querySelectorAll('img[data-progressive]');
    
    progressiveImages.forEach(img => {
      this.setupProgressiveImage(img);
    });
  }

  /**
   * Setup individual progressive image
   */
  setupProgressiveImage(img) {
    const lowQualitySrc = img.dataset.placeholder;
    const highQualitySrc = img.dataset.src;
    
    if (!lowQualitySrc || !highQualitySrc) return;
    
    // Create container for progressive loading
    const container = document.createElement('div');
    container.className = 'progressive-image-container';
    container.style.cssText = `
      position: relative;
      overflow: hidden;
      background: linear-gradient(45deg, #f0f0f0 25%, transparent 25%), 
                  linear-gradient(-45deg, #f0f0f0 25%, transparent 25%), 
                  linear-gradient(45deg, transparent 75%, #f0f0f0 75%), 
                  linear-gradient(-45deg, transparent 75%, #f0f0f0 75%);
      background-size: 20px 20px;
      background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
    `;
    
    // Insert container before image
    img.parentNode.insertBefore(container, img);
    container.appendChild(img);
    
    // Load low quality image first
    img.src = lowQualitySrc;
    img.style.cssText = `
      filter: blur(5px);
      transition: filter 0.5s ease, opacity 0.5s ease;
      width: 100%;
      height: 100%;
      object-fit: cover;
    `;
    
    // Preload high quality image
    const highQualityImg = new Image();
    highQualityImg.onload = () => {
      img.src = highQualitySrc;
      img.style.filter = 'blur(0)';
      img.classList.add('loaded');
      
      // Track performance
      this.trackImageLoad(highQualitySrc, performance.now());
    };
    
    highQualityImg.onerror = () => {
      console.warn('Failed to load high quality image:', highQualitySrc);
    };
    
    highQualityImg.src = highQualitySrc;
  }

  /**
   * Load image with format optimization
   */
  async loadImage(img) {
    const startTime = performance.now();
    const originalSrc = img.dataset.src || img.src;
    
    if (!originalSrc) return;
    
    // Get optimal format and size
    const optimizedSrc = await this.getOptimizedImageSrc(originalSrc, img);
    
    // Create new image for loading
    const newImg = new Image();
    
    newImg.onload = () => {
      img.src = optimizedSrc;
      img.removeAttribute('data-src');
      img.classList.add('loaded');
      
      // Track performance
      this.trackImageLoad(optimizedSrc, performance.now() - startTime);
    };
    
    newImg.onerror = () => {
      // Fallback to original
      img.src = originalSrc;
      img.classList.add('loaded', 'fallback');
      console.warn('Failed to load optimized image, using fallback:', originalSrc);
    };
    
    newImg.src = optimizedSrc;
    this.performanceMetrics.totalImages++;
  }

  /**
   * Preload image for better UX
   */
  preloadImage(img) {
    const src = img.dataset.src || img.src;
    if (!src || this.imageCache.has(src)) return;
    
    const preloadImg = new Image();
    preloadImg.onload = () => {
      this.imageCache.set(src, preloadImg);
    };
    preloadImg.src = src;
  }

  /**
   * Get optimized image source based on format support and connection
   */
  async getOptimizedImageSrc(originalSrc, imgElement) {
    const basePath = originalSrc.substring(0, originalSrc.lastIndexOf('.'));
    const extension = originalSrc.substring(originalSrc.lastIndexOf('.') + 1);
    
    // Determine optimal format
    let optimalFormat = extension;
    if (this.supportedFormats.avif && !this.connectionInfo?.saveData) {
      optimalFormat = 'avif';
    } else if (this.supportedFormats.webp) {
      optimalFormat = 'webp';
    }
    
    // Determine optimal size
    const optimalWidth = this.getOptimalImageWidth(imgElement);
    
    // Construct optimized URL
    const optimizedSrc = `${basePath}-${optimalWidth}.${optimalFormat}`;
    
    // Check if optimized version exists
    if (await this.imageExists(optimizedSrc)) {
      this.performanceMetrics.formatUsage[optimalFormat] = 
        (this.performanceMetrics.formatUsage[optimalFormat] || 0) + 1;
      return optimizedSrc;
    }
    
    // Fallback to original
    return originalSrc;
  }

  /**
   * Get optimal image width based on element size and device pixel ratio
   */
  getOptimalImageWidth(imgElement) {
    const rect = imgElement.getBoundingClientRect();
    const displayWidth = rect.width || imgElement.offsetWidth || 300;
    const optimalWidth = Math.ceil(displayWidth * this.devicePixelRatio);
    
    // Find closest breakpoint
    return this.config.breakpoints.reduce((prev, curr) => 
      Math.abs(curr - optimalWidth) < Math.abs(prev - optimalWidth) ? curr : prev
    );
  }

  /**
   * Check if image exists
   */
  imageExists(src) {
    return new Promise(resolve => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = src;
      
      // Timeout after 2 seconds
      setTimeout(() => resolve(false), 2000);
    });
  }

  /**
   * Setup image error handling with intelligent fallbacks
   */
  setupImageErrorHandling() {
    document.addEventListener('error', (e) => {
      if (e.target.tagName === 'IMG') {
        this.handleImageError(e.target);
      }
    }, true);
  }

  /**
   * Handle image loading errors with smart fallbacks
   */
  handleImageError(img) {
    // Try fallback formats in order of preference
    const fallbackFormats = ['webp', 'jpeg', 'png'];
    const currentSrc = img.src;
    const basePath = currentSrc.substring(0, currentSrc.lastIndexOf('.'));
    const currentFormat = currentSrc.substring(currentSrc.lastIndexOf('.') + 1);
    
    // Find next format to try
    const currentIndex = fallbackFormats.indexOf(currentFormat);
    const nextFormat = fallbackFormats[currentIndex + 1];
    
    if (nextFormat) {
      const fallbackSrc = `${basePath}.${nextFormat}`;
      console.log(`Trying fallback format: ${nextFormat} for ${img.src}`);
      img.src = fallbackSrc;
      return;
    }
    
    // If all formats failed, create placeholder
    this.createAdvancedPlaceholder(img);
  }

  /**
   * Create advanced SVG placeholder
   */
  createAdvancedPlaceholder(img) {
    const width = img.width || img.offsetWidth || 300;
    const height = img.height || img.offsetHeight || 200;
    
    const placeholder = `data:image/svg+xml;base64,${btoa(`
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
            <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#e0e0e0" stroke-width="1"/>
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)"/>
        <rect width="100%" height="100%" fill="rgba(240,240,240,0.8)"/>
        <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" 
              font-family="Arial, sans-serif" font-size="14">
          📷 Image unavailable
        </text>
      </svg>
    `)}`;
    
    img.src = placeholder;
    img.classList.add('placeholder');
  }

  /**
   * Preload critical images
   */
  preloadCriticalImages() {
    const criticalImages = document.querySelectorAll('img[data-critical], img[data-priority="high"]');
    
    criticalImages.forEach(async img => {
      const src = img.dataset.src || img.src;
      if (src) {
        const optimizedSrc = await this.getOptimizedImageSrc(src, img);
        
        const preloadLink = document.createElement('link');
        preloadLink.rel = 'preload';
        preloadLink.as = 'image';
        preloadLink.href = optimizedSrc;
        preloadLink.fetchPriority = 'high';
        
        document.head.appendChild(preloadLink);
        
        console.log('🚀 Preloading critical image:', optimizedSrc);
      }
    });
  }

  /**
   * Setup image preprocessing for better performance
   */
  setupImagePreprocessing() {
    // Process images on page load
    window.addEventListener('load', () => {
      this.processAllImages();
    });
    
    // Process new images added dynamically
    if ('MutationObserver' in window) {
      const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
          mutation.addedNodes.forEach(node => {
            if (node.nodeType === 1) { // Element node
              const images = node.tagName === 'IMG' ? [node] : 
                           node.querySelectorAll ? node.querySelectorAll('img') : [];
              images.forEach(img => this.processImage(img));
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
   * Process all images on the page
   */
  processAllImages() {
    const images = document.querySelectorAll('img');
    images.forEach(img => this.processImage(img));
  }

  /**
   * Process individual image for optimization
   */
  processImage(img) {
    // Add loading and decoding attributes
    if (!img.hasAttribute('loading')) {
      img.loading = 'lazy';
    }
    
    if (!img.hasAttribute('decoding')) {
      img.decoding = 'async';
    }
    
    // Add to lazy loading if has data-src
    if (img.dataset.src && this.observers.has('lazy')) {
      this.observers.get('lazy').observe(img);
    }
    
    // Convert to responsive if marked
    if (img.dataset.responsive && img.tagName === 'IMG') {
      this.convertToResponsiveImage(img);
    }
  }

  /**
   * Adjust image quality based on connection
   */
  adjustQualityForConnection() {
    if (!this.connectionInfo) return;
    
    const { effectiveType, saveData } = this.connectionInfo;
    let quality = 'high';
    
    if (saveData || effectiveType === 'slow-2g' || effectiveType === '2g') {
      quality = 'low';
    } else if (effectiveType === '3g') {
      quality = 'medium';
    }
    
    document.documentElement.setAttribute('data-image-quality', quality);
    console.log(`📶 Adjusted image quality to: ${quality}`);
  }

  /**
   * Setup performance monitoring
   */
  setupPerformanceMonitoring() {
    // Monitor image loading performance
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver(list => {
        list.getEntries().forEach(entry => {
          if (entry.initiatorType === 'img') {
            this.analyzeImagePerformance(entry);
          }
        });
      });
      
      observer.observe({ entryTypes: ['resource'] });
    }
  }

  /**
   * Analyze image loading performance
   */
  analyzeImagePerformance(entry) {
    const { name, duration, transferSize, encodedBodySize } = entry;
    
    // Track metrics
    this.performanceMetrics.bytesLoaded += transferSize || 0;
    this.performanceMetrics.loadTimes.push(duration);
    
    // Log slow images
    if (duration > 1000) {
      console.warn(`🐌 Slow image load: ${name} (${duration.toFixed(2)}ms)`);
    }
    
    // Log large images
    if (transferSize > 100000) {
      console.warn(`📦 Large image: ${name} (${(transferSize / 1024).toFixed(2)}KB)`);
    }
  }

  /**
   * Track image load completion
   */
  trackImageLoad(src, loadTime) {
    this.performanceMetrics.loadTimes.push(loadTime);
    
    if (loadTime < 500) {
      console.log(`⚡ Fast image load: ${src} (${loadTime.toFixed(2)}ms)`);
    }
  }

  /**
   * Optimize existing images on the page
   */
  optimizeExistingImages() {
    const images = document.querySelectorAll('img:not([data-optimized])');
    
    images.forEach(img => {
      // Mark as processed
      img.dataset.optimized = 'true';
      
      // Add modern attributes
      if (!img.hasAttribute('loading')) {
        img.loading = 'lazy';
      }
      
      if (!img.hasAttribute('decoding')) {
        img.decoding = 'async';
      }
      
      // Add to observers if needed
      if (img.dataset.src) {
        this.observers.get('lazy')?.observe(img);
      }
    });
  }

  /**
   * Generate performance report
   */
  generateReport() {
    const avgLoadTime = this.performanceMetrics.loadTimes.length > 0 ?
      this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0) / this.performanceMetrics.loadTimes.length : 0;
    
    const report = {
      timestamp: Date.now(),
      supportedFormats: this.supportedFormats,
      performanceMetrics: {
        ...this.performanceMetrics,
        averageLoadTime: avgLoadTime,
        totalLoadTime: this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0)
      },
      connectionInfo: this.connectionInfo,
      devicePixelRatio: this.devicePixelRatio,
      recommendations: this.generateRecommendations()
    };
    
    console.log('📊 Image Optimization Report:', report);
    localStorage.setItem('image_optimization_report', JSON.stringify(report));
    
    return report;
  }

  /**
   * Generate optimization recommendations
   */
  generateRecommendations() {
    const recommendations = [];
    
    if (this.performanceMetrics.bytesLoaded > 1000000) {
      recommendations.push('Consider implementing more aggressive image compression');
    }
    
    const avgLoadTime = this.performanceMetrics.loadTimes.length > 0 ?
      this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0) / this.performanceMetrics.loadTimes.length : 0;
    
    if (avgLoadTime > 1000) {
      recommendations.push('Image load times are high. Consider using a CDN or optimizing image sizes');
    }
    
    if (!this.supportedFormats.avif && !this.supportedFormats.webp) {
      recommendations.push('Browser does not support modern image formats. Consider polyfills or server-side detection');
    }
    
    return recommendations;
  }

  /**
   * Get current optimization status
   */
  getStatus() {
    return {
      supportedFormats: this.supportedFormats,
      totalImages: this.performanceMetrics.totalImages,
      optimizedImages: this.performanceMetrics.optimizedImages,
      bytesLoaded: this.performanceMetrics.bytesLoaded,
      averageLoadTime: this.performanceMetrics.loadTimes.length > 0 ?
        this.performanceMetrics.loadTimes.reduce((a, b) => a + b, 0) / this.performanceMetrics.loadTimes.length : 0
    };
  }

  /**
   * Cleanup observers
   */
  destroy() {
    this.observers.forEach(observer => observer.disconnect());
    this.observers.clear();
  }
}

// Initialize Modern Image Optimizer
const modernImageOptimizer = new ModernImageOptimizer();

// Export for global access
window.ModernImageOptimizer = ModernImageOptimizer;
window.modernImageOptimizer = modernImageOptimizer;

console.log('✅ Modern Image Optimizer initialized');