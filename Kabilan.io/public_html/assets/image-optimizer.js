// Enhanced Image Optimization Pipeline
// Modern image formats, responsive loading, and advanced optimization strategies

class ImageOptimizer {
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
      loadTimes: []
    };
    
    this.init();
  }

  async init() {
    await this.detectFormatSupport();
    this.setupAdvancedLazyLoading();
    this.setupResponsiveImages();
    this.setupProgressiveLoading();
    this.setupImageErrorHandling();
    this.setupPerformanceMonitoring();
    this.optimizeExistingImages();
    this.setupConnectionAwareLoading();
    this.preloadCriticalImages();
  }

  // Enhanced format detection for modern image formats
  async detectFormatSupport() {
    const formats = ['webp', 'avif', 'heic', 'jxl'];
    
    await Promise.all(formats.map(async format => {
      this.supportedFormats[format] = await this.supportsFormat(format);
    }));
    
    // Add classes to document for CSS targeting
    Object.entries(this.supportedFormats).forEach(([format, supported]) => {
      if (supported) {
        document.documentElement.classList.add(format);
      }
    });
    
    console.log('📸 Image format support:', this.supportedFormats);
    
    // Store support info for service worker
    if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'IMAGE_FORMAT_SUPPORT',
        formats: this.supportedFormats
      });
    }
  }

  supportsFormat(format) {
    return new Promise(resolve => {
      const img = new Image();
      img.onload = () => resolve(img.height === 2);
      img.onerror = () => resolve(false);
      
      const testImages = {
        webp: 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA',
        avif: 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAIAAAACAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgABogQEAwgMg8f8D///8WfhwB8+ErK42A='
      };
      
      img.src = testImages[format];
    });
  }

  // Setup responsive images with optimal format selection
  setupResponsiveImages() {
    const images = document.querySelectorAll('img[data-responsive]');
    
    images.forEach(img => {
      this.makeImageResponsive(img);
    });
  }

  makeImageResponsive(img) {
    const baseSrc = img.dataset.src || img.src;
    const sizes = img.dataset.sizes || '(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw';
    
    // Generate srcset with different sizes and formats
    const srcset = this.generateSrcSet(baseSrc);
    
    if (srcset) {
      img.srcset = srcset;
      img.sizes = sizes;
    }
  }

  generateSrcSet(baseSrc) {
    // Extract file extension and path
    const lastDot = baseSrc.lastIndexOf('.');
    const basePath = baseSrc.substring(0, lastDot);
    const extension = baseSrc.substring(lastDot);
    
    const sizes = [400, 800, 1200, 1600];
    const formats = [];
    
    // Add AVIF if supported
    if (this.supportedFormats.avif) {
      formats.push('avif');
    }
    
    // Add WebP if supported
    if (this.supportedFormats.webp) {
      formats.push('webp');
    }
    
    // Always include original format as fallback
    formats.push(extension.substring(1));
    
    // Generate srcset string
    const srcsetEntries = [];
    
    formats.forEach(format => {
      sizes.forEach(size => {
        srcsetEntries.push(`${basePath}-${size}w.${format} ${size}w`);
      });
    });
    
    return srcsetEntries.join(', ');
  }

  // Progressive loading with blur-up effect
  setupProgressiveLoading() {
    const images = document.querySelectorAll('img[data-progressive]');
    
    images.forEach(img => {
      this.setupProgressiveImage(img);
    });
  }

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
      background: #f0f0f0;
    `;
    
    // Insert container before image
    img.parentNode.insertBefore(container, img);
    container.appendChild(img);
    
    // Load low quality image first
    img.src = lowQualitySrc;
    img.style.cssText = `
      filter: blur(5px);
      transition: filter 0.3s ease;
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
    };
    highQualityImg.src = highQualitySrc;
  }

  // Error handling and fallbacks
  setupImageErrorHandling() {
    document.addEventListener('error', (e) => {
      if (e.target.tagName === 'IMG') {
        this.handleImageError(e.target);
      }
    }, true);
  }

  handleImageError(img) {
    // Try fallback formats
    if (img.dataset.fallback) {
      img.src = img.dataset.fallback;
      return;
    }
    
    // Generate placeholder
    this.createPlaceholder(img);
  }

  createPlaceholder(img) {
    const width = img.width || 300;
    const height = img.height || 200;
    
    // Create SVG placeholder
    const placeholder = `data:image/svg+xml;base64,${btoa(`
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
        <rect width="100%" height="100%" fill="#f0f0f0"/>
        <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" font-family="Arial, sans-serif" font-size="14">
          Image not available
        </text>
      </svg>
    `)}`;
    
    img.src = placeholder;
    img.classList.add('placeholder');
  }

  // Preload critical images
  preloadCriticalImages() {
    const criticalImages = document.querySelectorAll('img[data-critical]');
    
    criticalImages.forEach(img => {
      const src = img.dataset.src || img.src;
      if (src) {
        const preloadLink = document.createElement('link');
        preloadLink.rel = 'preload';
        preloadLink.as = 'image';
        preloadLink.href = src;
        document.head.appendChild(preloadLink);
      }
    });
  }

  // Optimize images based on connection speed
  optimizeForConnection() {
    if ('connection' in navigator) {
      const connection = navigator.connection;
      const effectiveType = connection.effectiveType;
      
      // Adjust image quality based on connection
      if (effectiveType === 'slow-2g' || effectiveType === '2g') {
        this.setImageQuality('low');
      } else if (effectiveType === '3g') {
        this.setImageQuality('medium');
      } else {
        this.setImageQuality('high');
      }
    }
  }

  setImageQuality(quality) {
    const qualityMap = {
      low: 0.3,
      medium: 0.7,
      high: 1.0
    };
    
    document.documentElement.style.setProperty('--image-quality', qualityMap[quality]);
    document.documentElement.classList.add(`quality-${quality}`);
  }

  // Intersection Observer for lazy loading
  setupIntersectionObserver() {
    if ('IntersectionObserver' in window) {
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

      // Observe all images with data-src
      document.querySelectorAll('img[data-src]').forEach(img => {
        imageObserver.observe(img);
      });
    }
  }

  loadImage(img) {
    const src = img.dataset.src;
    if (src) {
      img.src = src;
      img.removeAttribute('data-src');
      img.classList.add('loaded');
    }
  }

  // Public API
  optimizeImage(img, options = {}) {
    if (options.responsive) {
      this.makeImageResponsive(img);
    }
    
    if (options.progressive) {
      this.setupProgressiveImage(img);
    }
    
    if (options.lazy) {
      this.setupIntersectionObserver();
    }
  }

  // Get optimization stats
  getStats() {
    return {
      supportedFormats: this.supportedFormats,
      totalImages: document.querySelectorAll('img').length,
      lazyImages: document.querySelectorAll('img[data-src]').length,
      loadedImages: document.querySelectorAll('img.loaded').length
    };
  }
}

// Initialize image optimizer
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.imageOptimizer = new ImageOptimizer();
  });
} else {
  window.imageOptimizer = new ImageOptimizer();
}

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ImageOptimizer;
}
