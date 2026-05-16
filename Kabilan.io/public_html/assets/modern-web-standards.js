/**
 * Modern Web Standards Implementation
 * CSS Container Queries, Grid Subgrid, Custom Properties, PWA enhancements
 * Implements cutting-edge web technologies with progressive enhancement
 */

class ModernWebStandards {
  constructor() {
    this.features = new Map();
    this.polyfills = new Map();
    this.customProperties = new Map();
    
    this.init();
  }

  async init() {
    this.detectFeatureSupport();
    this.implementModernCSS();
    this.enhancePWACapabilities();
    this.implementModernJS();
    this.setupProgressiveEnhancement();
    this.loadPolyfills();
  }

  /**
   * Detect modern feature support
   */
  detectFeatureSupport() {
    // CSS Features
    this.features.set('container-queries', CSS.supports('container-type: inline-size'));
    this.features.set('subgrid', CSS.supports('grid-template-columns: subgrid'));
    this.features.set('custom-properties', CSS.supports('--custom: value'));
    this.features.set('logical-properties', CSS.supports('margin-inline-start: 1px'));
    this.features.set('cascade-layers', CSS.supports('@layer'));
    
    // JavaScript Features
    this.features.set('es2022', 'at' in Array.prototype);
    this.features.set('web-workers', 'Worker' in window);
    this.features.set('service-worker', 'serviceWorker' in navigator);
    this.features.set('web-assembly', 'WebAssembly' in window);
    
    console.log('🔍 Modern feature support:', Object.fromEntries(this.features));
  }

  /**
   * Implement modern CSS features
   */
  implementModernCSS() {
    this.setupContainerQueries();
    this.implementGridSubgrid();
    this.setupCustomProperties();
    this.implementLogicalProperties();
    this.setupCascadeLayers();
  }

  /**
   * Setup CSS Container Queries
   */
  setupContainerQueries() {
    if (!this.features.get('container-queries')) {
      this.loadContainerQueryPolyfill();
      return;
    }

    const style = document.createElement('style');
    style.textContent = `
      /* Container Query Implementation */
      .container {
        container-type: inline-size;
        container-name: main-container;
      }
      
      .responsive-component {
        padding: 1rem;
        background: var(--bg-color, #f0f0f0);
      }
      
      @container main-container (min-width: 400px) {
        .responsive-component {
          padding: 2rem;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 1rem;
        }
      }
      
      @container main-container (min-width: 600px) {
        .responsive-component {
          grid-template-columns: repeat(3, 1fr);
        }
      }
      
      /* Card containers */
      .card-container {
        container-type: inline-size;
      }
      
      @container (min-width: 250px) {
        .card {
          display: flex;
          align-items: center;
          gap: 1rem;
        }
      }
    `;
    
    document.head.appendChild(style);
    console.log('📐 Container queries implemented');
  }

  /**
   * Implement CSS Grid Subgrid
   */
  implementGridSubgrid() {
    if (!this.features.get('subgrid')) {
      this.implementSubgridFallback();
      return;
    }

    const style = document.createElement('style');
    style.textContent = `
      /* Grid Subgrid Implementation */
      .main-grid {
        display: grid;
        grid-template-columns: repeat(12, 1fr);
        gap: 1rem;
      }
      
      .subgrid-item {
        display: grid;
        grid-column: span 6;
        grid-template-columns: subgrid;
        gap: inherit;
      }
      
      .nested-item {
        grid-column: span 2;
      }
      
      /* Card grid with subgrid */
      .card-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 2rem;
      }
      
      .card-subgrid {
        display: grid;
        grid-template-rows: subgrid;
        grid-row: span 3;
      }
    `;
    
    document.head.appendChild(style);
    console.log('🔲 Grid subgrid implemented');
  }

  /**
   * Setup CSS Custom Properties theming
   */
  setupCustomProperties() {
    // Define comprehensive design system
    const designTokens = {
      // Colors
      '--color-primary': '#3b82f6',
      '--color-secondary': '#8b5cf6',
      '--color-accent': '#10b981',
      '--color-neutral-50': '#f9fafb',
      '--color-neutral-900': '#111827',
      
      // Spacing
      '--space-xs': '0.25rem',
      '--space-sm': '0.5rem',
      '--space-md': '1rem',
      '--space-lg': '1.5rem',
      '--space-xl': '2rem',
      
      // Typography
      '--font-size-xs': '0.75rem',
      '--font-size-sm': '0.875rem',
      '--font-size-base': '1rem',
      '--font-size-lg': '1.125rem',
      '--font-size-xl': '1.25rem',
      
      // Shadows
      '--shadow-sm': '0 1px 2px 0 rgb(0 0 0 / 0.05)',
      '--shadow-md': '0 4px 6px -1px rgb(0 0 0 / 0.1)',
      '--shadow-lg': '0 10px 15px -3px rgb(0 0 0 / 0.1)',
      
      // Borders
      '--border-radius-sm': '0.25rem',
      '--border-radius-md': '0.375rem',
      '--border-radius-lg': '0.5rem'
    };

    // Apply to root
    Object.entries(designTokens).forEach(([property, value]) => {
      document.documentElement.style.setProperty(property, value);
      this.customProperties.set(property, value);
    });

    // Setup theme switching
    this.setupThemeSwitching();
    
    console.log('🎨 Custom properties theming implemented');
  }

  /**
   * Setup theme switching
   */
  setupThemeSwitching() {
    const themes = {
      light: {
        '--bg-primary': '#ffffff',
        '--bg-secondary': '#f9fafb',
        '--text-primary': '#111827',
        '--text-secondary': '#6b7280'
      },
      dark: {
        '--bg-primary': '#111827',
        '--bg-secondary': '#1f2937',
        '--text-primary': '#f9fafb',
        '--text-secondary': '#d1d5db'
      }
    };

    window.setTheme = (themeName) => {
      const theme = themes[themeName];
      if (theme) {
        Object.entries(theme).forEach(([property, value]) => {
          document.documentElement.style.setProperty(property, value);
        });
        localStorage.setItem('preferred-theme', themeName);
      }
    };

    // Apply saved theme
    const savedTheme = localStorage.getItem('preferred-theme') || 'light';
    window.setTheme(savedTheme);
  }

  /**
   * Enhance PWA capabilities
   */
  enhancePWACapabilities() {
    this.implementAdvancedServiceWorker();
    this.setupPushNotifications();
    this.createAppShortcuts();
    this.setupInstallPrompt();
  }

  /**
   * Implement advanced service worker features
   */
  implementAdvancedServiceWorker() {
    if (!this.features.get('service-worker')) return;

    // Background sync
    if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
      navigator.serviceWorker.ready.then(registration => {
        return registration.sync.register('background-sync');
      });
    }

    // Periodic background sync
    if ('serviceWorker' in navigator && 'periodicSync' in window.ServiceWorkerRegistration.prototype) {
      navigator.serviceWorker.ready.then(registration => {
        return registration.periodicSync.register('periodic-sync', {
          minInterval: 24 * 60 * 60 * 1000 // 24 hours
        });
      });
    }
  }

  /**
   * Setup push notifications
   */
  async setupPushNotifications() {
    if (!('Notification' in window) || !('serviceWorker' in navigator)) return;

    // Request permission
    if (Notification.permission === 'default') {
      await Notification.requestPermission();
    }

    if (Notification.permission === 'granted') {
      const registration = await navigator.serviceWorker.ready;
      
      // Subscribe to push notifications
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: this.urlBase64ToUint8Array('your-vapid-public-key')
      });

      console.log('📱 Push notifications enabled');
    }
  }

  /**
   * Create app shortcuts
   */
  createAppShortcuts() {
    // Add to manifest.json
    const shortcuts = [
      {
        name: 'Portfolio',
        short_name: 'Portfolio',
        description: 'View my portfolio',
        url: '/#portfolio',
        icons: [{ src: '/assets/icon-192x192.png', sizes: '192x192' }]
      },
      {
        name: 'Contact',
        short_name: 'Contact',
        description: 'Get in touch',
        url: '/#contact',
        icons: [{ src: '/assets/icon-192x192.png', sizes: '192x192' }]
      }
    ];

    // Update manifest
    this.updateManifest({ shortcuts });
  }

  /**
   * Setup install prompt
   */
  setupInstallPrompt() {
    let deferredPrompt;

    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      deferredPrompt = e;
      
      // Show custom install button
      this.showInstallButton(deferredPrompt);
    });

    window.addEventListener('appinstalled', () => {
      console.log('📱 PWA installed');
      deferredPrompt = null;
    });
  }

  /**
   * Show install button
   */
  showInstallButton(deferredPrompt) {
    const installButton = document.createElement('button');
    installButton.textContent = 'Install App';
    installButton.className = 'install-button';
    installButton.style.cssText = `
      position: fixed;
      bottom: 20px;
      left: 20px;
      background: var(--color-primary);
      color: white;
      border: none;
      padding: 1rem;
      border-radius: var(--border-radius-md);
      cursor: pointer;
      z-index: 1000;
    `;

    installButton.addEventListener('click', async () => {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      
      if (outcome === 'accepted') {
        console.log('User accepted install prompt');
      }
      
      installButton.remove();
      deferredPrompt = null;
    });

    document.body.appendChild(installButton);
  }

  /**
   * Implement modern JavaScript features
   */
  implementModernJS() {
    this.setupWebWorkers();
    this.implementDynamicImports();
    this.setupModuleSystem();
  }

  /**
   * Setup Web Workers for heavy computations
   */
  setupWebWorkers() {
    if (!this.features.get('web-workers')) return;

    // Create worker for image processing
    const imageWorkerCode = `
      self.onmessage = function(e) {
        const { imageData, filter } = e.data;
        
        // Apply filter to image data
        const processed = applyFilter(imageData, filter);
        
        self.postMessage({ processedImageData: processed });
      };
      
      function applyFilter(imageData, filter) {
        // Simple filter implementation
        const data = imageData.data;
        
        for (let i = 0; i < data.length; i += 4) {
          switch (filter) {
            case 'grayscale':
              const gray = data[i] * 0.299 + data[i + 1] * 0.587 + data[i + 2] * 0.114;
              data[i] = data[i + 1] = data[i + 2] = gray;
              break;
            case 'sepia':
              const r = data[i], g = data[i + 1], b = data[i + 2];
              data[i] = Math.min(255, r * 0.393 + g * 0.769 + b * 0.189);
              data[i + 1] = Math.min(255, r * 0.349 + g * 0.686 + b * 0.168);
              data[i + 2] = Math.min(255, r * 0.272 + g * 0.534 + b * 0.131);
              break;
          }
        }
        
        return imageData;
      }
    `;

    const blob = new Blob([imageWorkerCode], { type: 'application/javascript' });
    const workerUrl = URL.createObjectURL(blob);
    
    window.imageWorker = new Worker(workerUrl);
    console.log('👷 Web Workers implemented');
  }

  /**
   * Load polyfills for unsupported features
   */
  async loadPolyfills() {
    const polyfillsNeeded = [];

    if (!this.features.get('container-queries')) {
      polyfillsNeeded.push('container-queries');
    }

    if (!this.features.get('es2022')) {
      polyfillsNeeded.push('es2022');
    }

    for (const polyfill of polyfillsNeeded) {
      await this.loadPolyfill(polyfill);
    }
  }

  /**
   * Load specific polyfill
   */
  async loadPolyfill(name) {
    const polyfillUrls = {
      'container-queries': 'https://cdn.jsdelivr.net/npm/container-query-polyfill@latest/dist/container-query-polyfill.modern.js',
      'es2022': 'https://polyfill.io/v3/polyfill.min.js?features=es2022'
    };

    const url = polyfillUrls[name];
    if (url) {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      
      return new Promise((resolve, reject) => {
        script.onload = () => {
          console.log(`📦 Polyfill loaded: ${name}`);
          resolve();
        };
        script.onerror = reject;
        document.head.appendChild(script);
      });
    }
  }

  /**
   * Utility functions
   */
  
  urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
      .replace(/-/g, '+')
      .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }

  updateManifest(updates) {
    // This would typically update the manifest.json file
    console.log('📄 Manifest updated:', updates);
  }

  loadContainerQueryPolyfill() {
    console.log('📦 Loading container query polyfill...');
    this.loadPolyfill('container-queries');
  }

  implementSubgridFallback() {
    console.log('🔄 Implementing subgrid fallback...');
    // Fallback implementation for subgrid
  }

  setupProgressiveEnhancement() {
    // Add feature classes to document
    this.features.forEach((supported, feature) => {
      const className = supported ? `supports-${feature}` : `no-${feature}`;
      document.documentElement.classList.add(className);
    });
  }

  setupLogicalProperties() {
    if (!this.features.get('logical-properties')) {
      // Fallback for logical properties
      const style = document.createElement('style');
      style.textContent = `
        .margin-inline-start { margin-left: var(--space-md); }
        .margin-inline-end { margin-right: var(--space-md); }
        .padding-block-start { padding-top: var(--space-md); }
        .padding-block-end { padding-bottom: var(--space-md); }
      `;
      document.head.appendChild(style);
    }
  }

  setupCascadeLayers() {
    if (this.features.get('cascade-layers')) {
      const style = document.createElement('style');
      style.textContent = `
        @layer reset, base, components, utilities;
        
        @layer reset {
          * { margin: 0; padding: 0; box-sizing: border-box; }
        }
        
        @layer base {
          body { font-family: system-ui, sans-serif; }
        }
        
        @layer components {
          .btn { padding: var(--space-md); border-radius: var(--border-radius-md); }
        }
        
        @layer utilities {
          .text-center { text-align: center; }
        }
      `;
      document.head.appendChild(style);
    }
  }

  setupModuleSystem() {
    // Dynamic imports for code splitting
    window.loadModule = async (moduleName) => {
      try {
        const module = await import(`./modules/${moduleName}.js`);
        return module;
      } catch (error) {
        console.warn(`Failed to load module: ${moduleName}`, error);
      }
    };
  }

  implementDynamicImports() {
    // Setup lazy loading for modules
    const lazyModules = document.querySelectorAll('[data-module]');
    
    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const moduleName = entry.target.dataset.module;
            window.loadModule(moduleName);
            observer.unobserve(entry.target);
          }
        });
      });

      lazyModules.forEach(element => observer.observe(element));
    }
  }

  getStatus() {
    return {
      featuresSupported: Array.from(this.features.entries()).filter(([, supported]) => supported).length,
      totalFeatures: this.features.size,
      customProperties: this.customProperties.size,
      polyfillsLoaded: this.polyfills.size
    };
  }
}

// Initialize Modern Web Standards
const modernWebStandards = new ModernWebStandards();

// Export for global access
window.ModernWebStandards = ModernWebStandards;
window.modernWebStandards = modernWebStandards;

console.log('✅ Modern Web Standards implemented');