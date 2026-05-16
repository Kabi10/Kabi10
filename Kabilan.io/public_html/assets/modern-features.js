// Modern Features Implementation for Kabi Tharma Portfolio
// Includes smooth animations, PWA features, and modern JavaScript optimizations

class ModernFeatures {
  constructor() {
    this.animationObserver = null;
    this.scrollObserver = null;
    this.isOnline = navigator.onLine;
    this.installPrompt = null;
    this.init();
  }

  init() {
    this.setupSmoothAnimations();
    this.setupScrollAnimations();
    this.setupPWAFeatures();
    this.setupModernJSOptimizations();
    this.setupAdvancedInteractions();
    this.setupOfflineSupport();
    this.setupNotifications();
  }

  // Smooth Animations System
  setupSmoothAnimations() {
    // CSS-in-JS for dynamic animations
    this.injectAnimationStyles();
    
    // Intersection Observer for scroll-triggered animations
    this.setupScrollAnimations();
    
    // Page transition animations
    this.setupPageTransitions();
    
    // Micro-interactions
    this.setupMicroInteractions();
  }

  injectAnimationStyles() {
    const animationCSS = `
      @keyframes slideInUp {
        from {
          opacity: 0;
          transform: translateY(30px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      @keyframes slideInLeft {
        from {
          opacity: 0;
          transform: translateX(-30px);
        }
        to {
          opacity: 1;
          transform: translateX(0);
        }
      }

      @keyframes slideInRight {
        from {
          opacity: 0;
          transform: translateX(30px);
        }
        to {
          opacity: 1;
          transform: translateX(0);
        }
      }

      @keyframes scaleIn {
        from {
          opacity: 0;
          transform: scale(0.9);
        }
        to {
          opacity: 1;
          transform: scale(1);
        }
      }

      @keyframes rotateIn {
        from {
          opacity: 0;
          transform: rotate(-10deg) scale(0.9);
        }
        to {
          opacity: 1;
          transform: rotate(0deg) scale(1);
        }
      }

      @keyframes fadeIn {
        from {
          opacity: 0;
        }
        to {
          opacity: 1;
        }
      }

      .animate-slide-in-up {
        animation: slideInUp 0.6s ease-out forwards;
      }

      .animate-slide-in-left {
        animation: slideInLeft 0.6s ease-out forwards;
      }

      .animate-slide-in-right {
        animation: slideInRight 0.6s ease-out forwards;
      }

      .animate-scale-in {
        animation: scaleIn 0.4s ease-out forwards;
      }

      .animate-rotate-in {
        animation: rotateIn 0.5s ease-out forwards;
      }

      .animate-fade-in {
        animation: fadeIn 0.4s ease-out forwards;
      }

      .animate-on-scroll {
        opacity: 0;
        transform: translateY(30px);
        transition: all 0.6s ease-out;
      }

      .animate-on-scroll.visible {
        opacity: 1;
        transform: translateY(0);
      }

      .stagger-1 { animation-delay: 0.1s; }
      .stagger-2 { animation-delay: 0.2s; }
      .stagger-3 { animation-delay: 0.3s; }
      .stagger-4 { animation-delay: 0.4s; }
      .stagger-5 { animation-delay: 0.5s; }

      .hover-lift {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
      }

      .hover-lift:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
      }

      .hover-glow {
        transition: box-shadow 0.3s ease;
      }

      .hover-glow:hover {
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
      }

      .parallax-element {
        will-change: transform;
      }
    `;

    const style = document.createElement('style');
    style.textContent = animationCSS;
    document.head.appendChild(style);
  }

  setupScrollAnimations() {
    if ('IntersectionObserver' in window) {
      this.animationObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const element = entry.target;
            this.triggerScrollAnimation(element);
            this.animationObserver.unobserve(element);
          }
        });
      }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
      });

      // Observe elements with animation classes
      this.observeAnimationElements();
    }

    // Parallax scrolling
    this.setupParallaxScrolling();
  }

  observeAnimationElements() {
    const animationElements = document.querySelectorAll(
      '.animate-on-scroll, [data-animate], .grid-container, .client-review, .work-content'
    );
    
    animationElements.forEach(element => {
      element.classList.add('animate-on-scroll');
      this.animationObserver.observe(element);
    });
  }

  triggerScrollAnimation(element) {
    element.classList.add('visible');
    
    // Add specific animation based on data attribute or position
    const animationType = element.dataset.animate || this.getAnimationForElement(element);
    
    if (animationType) {
      element.classList.add(animationType);
    }

    // Stagger child animations
    const children = element.querySelectorAll('.stagger-child');
    children.forEach((child, index) => {
      setTimeout(() => {
        child.classList.add('visible');
      }, index * 100);
    });
  }

  getAnimationForElement(element) {
    const rect = element.getBoundingClientRect();
    const centerX = window.innerWidth / 2;
    
    if (rect.left > centerX) {
      return 'animate-slide-in-right';
    } else if (rect.right < centerX) {
      return 'animate-slide-in-left';
    } else {
      return 'animate-slide-in-up';
    }
  }

  setupParallaxScrolling() {
    const parallaxElements = document.querySelectorAll('.parallax-element, [data-parallax]');
    
    if (parallaxElements.length > 0) {
      let ticking = false;
      
      const updateParallax = () => {
        const scrollY = window.pageYOffset;
        
        parallaxElements.forEach(element => {
          const speed = parseFloat(element.dataset.parallax) || 0.5;
          const yPos = -(scrollY * speed);
          element.style.transform = `translateY(${yPos}px)`;
        });
        
        ticking = false;
      };

      window.addEventListener('scroll', () => {
        if (!ticking) {
          requestAnimationFrame(updateParallax);
          ticking = true;
        }
      });
    }
  }

  setupPageTransitions() {
    // Smooth page transitions for SPA
    const originalPushState = history.pushState;
    const originalReplaceState = history.replaceState;

    history.pushState = function(...args) {
      window.dispatchEvent(new CustomEvent('pageTransitionStart'));
      originalPushState.apply(history, args);
      window.dispatchEvent(new CustomEvent('pageTransitionEnd'));
    };

    history.replaceState = function(...args) {
      window.dispatchEvent(new CustomEvent('pageTransitionStart'));
      originalReplaceState.apply(history, args);
      window.dispatchEvent(new CustomEvent('pageTransitionEnd'));
    };

    window.addEventListener('pageTransitionStart', () => {
      document.body.classList.add('page-transitioning');
    });

    window.addEventListener('pageTransitionEnd', () => {
      setTimeout(() => {
        document.body.classList.remove('page-transitioning');
        this.observeAnimationElements(); // Re-observe new elements
      }, 100);
    });
  }

  setupMicroInteractions() {
    // Button hover effects
    document.addEventListener('mouseover', (e) => {
      if (e.target.matches('button, .btn, a')) {
        e.target.classList.add('hover-lift');
      }
    });

    // Form input focus effects
    document.addEventListener('focusin', (e) => {
      if (e.target.matches('input, textarea, select')) {
        e.target.parentElement?.classList.add('focused');
      }
    });

    document.addEventListener('focusout', (e) => {
      if (e.target.matches('input, textarea, select')) {
        e.target.parentElement?.classList.remove('focused');
      }
    });

    // Click ripple effect
    this.setupRippleEffect();
  }

  setupRippleEffect() {
    document.addEventListener('click', (e) => {
      if (e.target.matches('button, .btn')) {
        const button = e.target;
        const rect = button.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;

        const ripple = document.createElement('span');
        ripple.style.cssText = `
          position: absolute;
          width: ${size}px;
          height: ${size}px;
          left: ${x}px;
          top: ${y}px;
          background: rgba(255, 255, 255, 0.3);
          border-radius: 50%;
          transform: scale(0);
          animation: ripple 0.6s linear;
          pointer-events: none;
        `;

        button.style.position = 'relative';
        button.style.overflow = 'hidden';
        button.appendChild(ripple);

        setTimeout(() => {
          ripple.remove();
        }, 600);
      }
    });

    // Add ripple animation
    const rippleCSS = `
      @keyframes ripple {
        to {
          transform: scale(4);
          opacity: 0;
        }
      }
    `;
    
    const style = document.createElement('style');
    style.textContent = rippleCSS;
    document.head.appendChild(style);
  }

  // Progressive Web App Features
  setupPWAFeatures() {
    this.setupInstallPrompt();
    this.setupAppUpdates();
    this.setupBackgroundSync();
    this.setupPushNotifications();
  }

  setupInstallPrompt() {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.installPrompt = e;
      this.showInstallButton();
    });

    window.addEventListener('appinstalled', () => {
      this.hideInstallButton();
      this.trackEvent('pwa_installed');
    });
  }

  showInstallButton() {
    const installButton = document.createElement('button');
    installButton.id = 'install-app';
    installButton.className = 'fixed bottom-4 right-4 bg-blue-600 text-white px-4 py-2 rounded-lg shadow-lg z-50 hover:bg-blue-700 transition-colors';
    installButton.innerHTML = `
      <svg class="w-5 h-5 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
      </svg>
      Install App
    `;
    
    installButton.addEventListener('click', () => {
      this.promptInstall();
    });

    document.body.appendChild(installButton);
  }

  hideInstallButton() {
    const installButton = document.getElementById('install-app');
    if (installButton) {
      installButton.remove();
    }
  }

  async promptInstall() {
    if (this.installPrompt) {
      this.installPrompt.prompt();
      const result = await this.installPrompt.userChoice;
      
      if (result.outcome === 'accepted') {
        this.trackEvent('pwa_install_accepted');
      } else {
        this.trackEvent('pwa_install_dismissed');
      }
      
      this.installPrompt = null;
      this.hideInstallButton();
    }
  }

  setupAppUpdates() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('controllerchange', () => {
        this.showUpdateNotification();
      });
    }
  }

  showUpdateNotification() {
    const notification = document.createElement('div');
    notification.className = 'fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg z-50';
    notification.innerHTML = `
      <div class="flex items-center space-x-3">
        <span>App updated successfully!</span>
        <button onclick="this.parentElement.parentElement.remove()" class="text-white hover:text-gray-200">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
      notification.remove();
    }, 5000);
  }

  setupBackgroundSync() {
    if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
      // Register background sync for form submissions
      window.addEventListener('online', () => {
        navigator.serviceWorker.ready.then(registration => {
          registration.sync.register('background-sync');
        });
      });
    }
  }

  setupPushNotifications() {
    if ('Notification' in window && 'serviceWorker' in navigator) {
      // Request notification permission on user interaction
      document.addEventListener('click', this.requestNotificationPermission.bind(this), { once: true });
    }
  }

  async requestNotificationPermission() {
    if (Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        this.trackEvent('notifications_enabled');
      }
    }
  }

  // Modern JavaScript Optimizations
  setupModernJSOptimizations() {
    this.setupLazyLoading();
    this.setupCodeSplitting();
    this.setupWebWorkers();
    this.setupModulePreloading();
  }

  setupLazyLoading() {
    // Dynamic import for heavy components
    const lazyComponents = document.querySelectorAll('[data-lazy-component]');
    
    lazyComponents.forEach(element => {
      const componentName = element.dataset.lazyComponent;
      
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadComponent(componentName, element);
            observer.unobserve(entry.target);
          }
        });
      });
      
      observer.observe(element);
    });
  }

  async loadComponent(componentName, element) {
    try {
      const module = await import(`/components/${componentName}.js`);
      const Component = module.default;
      new Component(element);
    } catch (error) {
      console.warn(`Failed to load component: ${componentName}`, error);
    }
  }

  setupCodeSplitting() {
    // Preload critical routes
    const criticalRoutes = ['/about', '/projects', '/contact'];
    
    criticalRoutes.forEach(route => {
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.href = route;
      document.head.appendChild(link);
    });
  }

  setupWebWorkers() {
    if ('Worker' in window) {
      // Setup web worker for heavy computations
      this.setupImageProcessingWorker();
      this.setupAnalyticsWorker();
    }
  }

  setupImageProcessingWorker() {
    // Use inline processing instead of web worker to avoid CSP issues
    this.imageProcessor = {
      postMessage: (data) => {
        const { imageData, operation } = data;

        // Simulate async processing
        setTimeout(() => {
          switch(operation) {
            case 'resize':
              // Image resizing logic would go here
              this.handleImageProcessingResult({ success: true, data: imageData });
              break;
            case 'compress':
              // Image compression logic would go here
              this.handleImageProcessingResult({ success: true, data: imageData });
              break;
          }
        }, 10);
      }
    };
  }

  handleImageProcessingResult(result) {
    // Handle the processing result
    console.log('Image processing result:', result);
  }

  setupAnalyticsWorker() {
    // Use inline analytics processing instead of web worker
    this.analyticsQueue = [];

    this.analyticsProcessor = {
      postMessage: (data) => {
        const { type, data: eventData } = data;

        if (type === 'track') {
          this.analyticsQueue.push(eventData);

          // Batch send analytics data
          if (this.analyticsQueue.length >= 10) {
            this.sendAnalytics();
          }
        }
      }
    };

    // Send remaining data every 30 seconds
    setInterval(() => {
      this.sendAnalytics();
    }, 30000);
  }

  sendAnalytics() {
    if (this.analyticsQueue.length > 0) {
      // Send analytics data to server
      console.log('Sending analytics:', this.analyticsQueue);
      this.analyticsQueue = [];
    }
  }

  setupModulePreloading() {
    // Preload ES modules
    const modulePreloads = [
      '/assets/performance.js',
      '/assets/accessibility.js',
      '/assets/image-optimizer.js'
    ];

    modulePreloads.forEach(module => {
      const link = document.createElement('link');
      link.rel = 'modulepreload';
      link.href = module;
      document.head.appendChild(link);
    });
  }

  // Advanced Interactions
  setupAdvancedInteractions() {
    this.setupGestureSupport();
    this.setupVoiceCommands();
    this.setupKeyboardShortcuts();
  }

  setupGestureSupport() {
    if ('ontouchstart' in window) {
      let startX, startY, startTime;

      document.addEventListener('touchstart', (e) => {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
        startTime = Date.now();
      });

      document.addEventListener('touchend', (e) => {
        const endX = e.changedTouches[0].clientX;
        const endY = e.changedTouches[0].clientY;
        const endTime = Date.now();

        const deltaX = endX - startX;
        const deltaY = endY - startY;
        const deltaTime = endTime - startTime;

        // Swipe detection
        if (Math.abs(deltaX) > 50 && deltaTime < 300) {
          if (deltaX > 0) {
            this.handleSwipeRight();
          } else {
            this.handleSwipeLeft();
          }
        }
      });
    }
  }

  handleSwipeLeft() {
    // Navigate to next section
    this.trackEvent('swipe_left');
  }

  handleSwipeRight() {
    // Navigate to previous section
    this.trackEvent('swipe_right');
  }

  setupVoiceCommands() {
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      this.recognition = new SpeechRecognition();
      
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US';

      this.recognition.onresult = (event) => {
        const command = event.results[0][0].transcript.toLowerCase();
        this.handleVoiceCommand(command);
      };

      // Add voice command button
      this.addVoiceCommandButton();
    }
  }

  addVoiceCommandButton() {
    const voiceButton = document.createElement('button');
    voiceButton.className = 'fixed bottom-20 right-4 bg-purple-600 text-white p-3 rounded-full shadow-lg z-50 hover:bg-purple-700 transition-colors';
    voiceButton.innerHTML = `
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path>
      </svg>
    `;
    
    voiceButton.addEventListener('click', () => {
      this.recognition.start();
    });

    document.body.appendChild(voiceButton);
  }

  handleVoiceCommand(command) {
    if (command.includes('scroll down')) {
      window.scrollBy(0, 500);
    } else if (command.includes('scroll up')) {
      window.scrollBy(0, -500);
    } else if (command.includes('go to top')) {
      window.scrollTo(0, 0);
    } else if (command.includes('contact')) {
      document.querySelector('#contact')?.scrollIntoView({ behavior: 'smooth' });
    } else if (command.includes('projects')) {
      document.querySelector('#projects')?.scrollIntoView({ behavior: 'smooth' });
    }
    
    this.trackEvent('voice_command', { command });
  }

  setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
      // Only handle shortcuts when not in input fields
      if (e.target.matches('input, textarea, select')) return;

      if (e.ctrlKey || e.metaKey) {
        switch (e.key) {
          case 'k':
            e.preventDefault();
            this.openSearch();
            break;
          case '/':
            e.preventDefault();
            this.openHelp();
            break;
        }
      }

      // Arrow key navigation
      switch (e.key) {
        case 'ArrowDown':
          if (e.ctrlKey) {
            e.preventDefault();
            this.scrollToNextSection();
          }
          break;
        case 'ArrowUp':
          if (e.ctrlKey) {
            e.preventDefault();
            this.scrollToPreviousSection();
          }
          break;
      }
    });
  }

  openSearch() {
    // Implement search functionality
    this.trackEvent('keyboard_shortcut', { action: 'search' });
  }

  openHelp() {
    // Show keyboard shortcuts help
    this.trackEvent('keyboard_shortcut', { action: 'help' });
  }

  scrollToNextSection() {
    const sections = document.querySelectorAll('section, .section');
    const currentScroll = window.pageYOffset;
    
    for (let section of sections) {
      if (section.offsetTop > currentScroll + 100) {
        section.scrollIntoView({ behavior: 'smooth' });
        break;
      }
    }
  }

  scrollToPreviousSection() {
    const sections = Array.from(document.querySelectorAll('section, .section')).reverse();
    const currentScroll = window.pageYOffset;
    
    for (let section of sections) {
      if (section.offsetTop < currentScroll - 100) {
        section.scrollIntoView({ behavior: 'smooth' });
        break;
      }
    }
  }

  // Offline Support
  setupOfflineSupport() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.showConnectionStatus('online');
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.showConnectionStatus('offline');
    });
  }

  showConnectionStatus(status) {
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 px-4 py-2 rounded-lg text-white z-50 ${
      status === 'online' ? 'bg-green-600' : 'bg-red-600'
    }`;
    notification.textContent = status === 'online' ? 'Back online!' : 'You are offline';

    document.body.appendChild(notification);

    setTimeout(() => {
      notification.remove();
    }, 3000);
  }

  // Notifications
  setupNotifications() {
    // Show welcome notification for first-time visitors
    if (!localStorage.getItem('visited')) {
      setTimeout(() => {
        this.showWelcomeNotification();
        localStorage.setItem('visited', 'true');
      }, 2000);
    }
  }

  showWelcomeNotification() {
    if (Notification.permission === 'granted') {
      new Notification('Welcome to Kabi Tharma\'s Portfolio!', {
        body: 'Explore my projects and get in touch.',
        icon: '/assets/icon-192x192.png',
        badge: '/assets/badge-72x72.png'
      });
    }
  }

  // Analytics and Tracking
  trackEvent(eventName, data = {}) {
    if (this.analyticsProcessor) {
      this.analyticsProcessor.postMessage({
        type: 'track',
        data: {
          event: eventName,
          timestamp: Date.now(),
          url: window.location.href,
          ...data
        }
      });
    }
  }

  // Public API
  getFeatureStatus() {
    return {
      animations: !!this.animationObserver,
      pwa: 'serviceWorker' in navigator,
      notifications: Notification.permission,
      online: this.isOnline,
      webWorkers: 'Worker' in window,
      voiceCommands: 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window
    };
  }

  // Cleanup
  destroy() {
    if (this.animationObserver) {
      this.animationObserver.disconnect();
    }
    if (this.scrollObserver) {
      this.scrollObserver.disconnect();
    }
    // Clean up inline processors
    this.imageProcessor = null;
    this.analyticsProcessor = null;
    this.analyticsQueue = [];
  }
}

// Initialize modern features
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.modernFeatures = new ModernFeatures();
  });
} else {
  window.modernFeatures = new ModernFeatures();
}

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ModernFeatures;
}
