/**
 * Service Worker Manager
 * Manages service worker registration, updates, and communication
 * Implements advanced caching strategies and offline functionality
 */

class ServiceWorkerManager {
  constructor() {
    this.registration = null;
    this.isOnline = navigator.onLine;
    this.updateAvailable = false;
    this.offlineQueue = [];
    this.init();
  }

  async init() {
    if ('serviceWorker' in navigator) {
      await this.registerServiceWorker();
      this.setupEventListeners();
      this.setupOfflineHandling();
      this.monitorConnection();
    } else {
      console.warn('Service Worker not supported');
    }
  }

  /**
   * Register service worker with enhanced error handling
   */
  async registerServiceWorker() {
    try {
      this.registration = await navigator.serviceWorker.register('/sw.js', {
        scope: '/'
      });

      console.log('✅ Service Worker registered successfully');

      // Handle different registration states
      if (this.registration.installing) {
        console.log('Service Worker installing...');
        this.trackInstallProgress(this.registration.installing);
      } else if (this.registration.waiting) {
        console.log('Service Worker waiting...');
        this.updateAvailable = true;
        this.showUpdateNotification();
      } else if (this.registration.active) {
        console.log('Service Worker active');
      }

      // Listen for updates
      this.registration.addEventListener('updatefound', () => {
        console.log('Service Worker update found');
        this.trackInstallProgress(this.registration.installing);
      });

    } catch (error) {
      console.error('Service Worker registration failed:', error);
    }
  }

  /**
   * Track service worker installation progress
   */
  trackInstallProgress(worker) {
    worker.addEventListener('statechange', () => {
      console.log('Service Worker state changed:', worker.state);
      
      switch (worker.state) {
        case 'installed':
          if (navigator.serviceWorker.controller) {
            // New service worker installed, update available
            this.updateAvailable = true;
            this.showUpdateNotification();
          } else {
            // First install
            console.log('Service Worker installed for the first time');
            this.showInstallNotification();
          }
          break;
          
        case 'activated':
          console.log('Service Worker activated');
          this.updateAvailable = false;
          break;
          
        case 'redundant':
          console.log('Service Worker became redundant');
          break;
      }
    });
  }

  /**
   * Setup event listeners for service worker communication
   */
  setupEventListeners() {
    // Listen for messages from service worker
    navigator.serviceWorker.addEventListener('message', event => {
      this.handleServiceWorkerMessage(event.data);
    });

    // Listen for controller changes
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      console.log('Service Worker controller changed');
      // Reload page to get latest version
      if (this.updateAvailable) {
        window.location.reload();
      }
    });

    // Handle page visibility changes
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden && this.updateAvailable) {
        this.showUpdateNotification();
      }
    });
  }

  /**
   * Handle messages from service worker
   */
  handleServiceWorkerMessage(data) {
    switch (data.type) {
      case 'SW_ACTIVATED':
        console.log('Service Worker activated with version:', data.version);
        break;
        
      case 'FORM_SUBMITTED':
        this.handleOfflineFormSubmission(data);
        break;
        
      case 'CACHE_STATUS':
        console.log('Cache Status:', data.status);
        break;
        
      case 'CACHE_STATUS_ERROR':
        console.error('Cache Status Error:', data.error);
        break;
        
      default:
        console.log('Unknown message from Service Worker:', data);
    }
  }

  /**
   * Setup offline handling
   */
  setupOfflineHandling() {
    // Intercept form submissions for offline queuing
    document.addEventListener('submit', event => {
      if (!this.isOnline && event.target.matches('form[data-offline-queue]')) {
        event.preventDefault();
        this.queueFormSubmission(event.target);
      }
    });

    // Queue analytics events when offline
    this.setupOfflineAnalytics();
  }

  /**
   * Monitor connection status
   */
  monitorConnection() {
    window.addEventListener('online', () => {
      console.log('Connection restored');
      this.isOnline = true;
      this.processOfflineQueue();
      this.showConnectionStatus('online');
    });

    window.addEventListener('offline', () => {
      console.log('Connection lost');
      this.isOnline = false;
      this.showConnectionStatus('offline');
    });
  }

  /**
   * Queue form submission for offline handling
   */
  async queueFormSubmission(form) {
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());
    
    // Add to local queue
    this.offlineQueue.push({
      type: 'form',
      data,
      timestamp: Date.now()
    });

    // Send to service worker
    if (navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'QUEUE_FORM_SUBMISSION',
        formData: data
      });
    }

    this.showOfflineQueueNotification('Form submission queued for when you\'re back online');
  }

  /**
   * Setup offline analytics queuing
   */
  setupOfflineAnalytics() {
    // Override gtag function to queue events when offline
    if (typeof gtag !== 'undefined') {
      const originalGtag = window.gtag;
      
      window.gtag = (...args) => {
        if (this.isOnline) {
          originalGtag(...args);
        } else {
          // Queue analytics event
          if (navigator.serviceWorker.controller) {
            navigator.serviceWorker.controller.postMessage({
              type: 'QUEUE_ANALYTICS_EVENT',
              eventData: {
                action: args[0],
                data: args[1]
              }
            });
          }
        }
      };
    }
  }

  /**
   * Process offline queue when connection is restored
   */
  async processOfflineQueue() {
    if (this.offlineQueue.length === 0) return;

    console.log(`Processing ${this.offlineQueue.length} queued items`);
    
    // Trigger background sync
    if (this.registration && 'sync' in this.registration) {
      try {
        await this.registration.sync.register('contact-form');
        await this.registration.sync.register('analytics');
        
        // Clear local queue as service worker will handle it
        this.offlineQueue = [];
        
        this.showOfflineQueueNotification('Queued items are being processed');
      } catch (error) {
        console.error('Background sync registration failed:', error);
      }
    }
  }

  /**
   * Handle offline form submission completion
   */
  handleOfflineFormSubmission(data) {
    if (data.success) {
      this.showOfflineQueueNotification('Form submitted successfully!');
    } else {
      this.showOfflineQueueNotification('Form submission failed. Will retry later.');
    }
  }

  /**
   * Show update notification
   */
  showUpdateNotification() {
    const notification = document.createElement('div');
    notification.id = 'sw-update-notification';
    notification.className = 'sw-notification update';
    notification.innerHTML = `
      <div class="sw-notification-content">
        <span>🚀 New version available!</span>
        <button onclick="serviceWorkerManager.applyUpdate()">Update</button>
        <button onclick="this.parentElement.parentElement.remove()">Later</button>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-remove after 10 seconds
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 10000);
  }

  /**
   * Show install notification
   */
  showInstallNotification() {
    const notification = document.createElement('div');
    notification.className = 'sw-notification install';
    notification.innerHTML = `
      <div class="sw-notification-content">
        <span>✅ Portfolio is now available offline!</span>
        <button onclick="this.parentElement.parentElement.remove()">Got it</button>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 5000);
  }

  /**
   * Show connection status
   */
  showConnectionStatus(status) {
    const existing = document.getElementById('connection-status');
    if (existing) existing.remove();
    
    const notification = document.createElement('div');
    notification.id = 'connection-status';
    notification.className = `sw-notification connection ${status}`;
    notification.innerHTML = `
      <div class="sw-notification-content">
        <span>${status === 'online' ? '🌐 Back online' : '📱 You\'re offline'}</span>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-remove after 3 seconds
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 3000);
  }

  /**
   * Show offline queue notification
   */
  showOfflineQueueNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'sw-notification queue';
    notification.innerHTML = `
      <div class="sw-notification-content">
        <span>📋 ${message}</span>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 4000);
  }

  /**
   * Apply service worker update
   */
  async applyUpdate() {
    if (this.registration && this.registration.waiting) {
      // Tell the waiting service worker to skip waiting
      this.registration.waiting.postMessage({ type: 'SKIP_WAITING' });
      
      // Remove notification
      const notification = document.getElementById('sw-update-notification');
      if (notification) notification.remove();
      
      // Reload will happen automatically when controller changes
    }
  }

  /**
   * Get cache status
   */
  async getCacheStatus() {
    return new Promise((resolve, reject) => {
      if (!navigator.serviceWorker.controller) {
        reject(new Error('No service worker controller'));
        return;
      }

      const messageChannel = new MessageChannel();
      
      messageChannel.port1.onmessage = event => {
        if (event.data.type === 'CACHE_STATUS') {
          resolve(event.data.status);
        } else {
          reject(new Error(event.data.error));
        }
      };

      navigator.serviceWorker.controller.postMessage(
        { type: 'REQUEST_CACHE_STATUS' },
        [messageChannel.port2]
      );
    });
  }

  /**
   * Clear specific cache
   */
  async clearCache(cacheName) {
    if (navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'CLEAR_CACHE',
        cacheName
      });
    }
  }

  /**
   * Prefetch resources
   */
  async prefetchResources(resources) {
    if (navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'PREFETCH_RESOURCES',
        resources
      });
    }
  }

  /**
   * Get service worker status
   */
  getStatus() {
    return {
      supported: 'serviceWorker' in navigator,
      registered: !!this.registration,
      active: !!navigator.serviceWorker.controller,
      updateAvailable: this.updateAvailable,
      isOnline: this.isOnline,
      queuedItems: this.offlineQueue.length
    };
  }
}

// Add CSS for notifications
const notificationStyles = `
  .sw-notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: rgba(0, 0, 0, 0.9);
    color: white;
    padding: 1rem;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    z-index: 10000;
    max-width: 300px;
    animation: slideInRight 0.3s ease-out;
  }
  
  .sw-notification.update {
    background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  }
  
  .sw-notification.install {
    background: linear-gradient(135deg, #10b981, #059669);
  }
  
  .sw-notification.connection.online {
    background: linear-gradient(135deg, #10b981, #059669);
  }
  
  .sw-notification.connection.offline {
    background: linear-gradient(135deg, #f59e0b, #d97706);
  }
  
  .sw-notification.queue {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
  }
  
  .sw-notification-content {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .sw-notification button {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
    transition: background 0.2s;
  }
  
  .sw-notification button:hover {
    background: rgba(255, 255, 255, 0.3);
  }
  
  @keyframes slideInRight {
    from {
      opacity: 0;
      transform: translateX(100%);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }
  
  @media (max-width: 768px) {
    .sw-notification {
      top: 10px;
      right: 10px;
      left: 10px;
      max-width: none;
    }
  }
`;

// Inject notification styles
const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet);

// Initialize Service Worker Manager
const serviceWorkerManager = new ServiceWorkerManager();

// Export for global access
window.ServiceWorkerManager = ServiceWorkerManager;
window.serviceWorkerManager = serviceWorkerManager;

console.log('✅ Service Worker Manager initialized');