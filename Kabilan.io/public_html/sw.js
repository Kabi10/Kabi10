// Enhanced Service Worker for Kabi Tharma Portfolio
// Version 2.0.0 - Advanced Caching Strategies

const CACHE_VERSION = '2.0.0';
const CACHE_NAME = `kabi-portfolio-v${CACHE_VERSION}`;

// Multiple cache strategies
const CACHES = {
  STATIC: `static-v${CACHE_VERSION}`,
  DYNAMIC: `dynamic-v${CACHE_VERSION}`,
  IMAGES: `images-v${CACHE_VERSION}`,
  API: `api-v${CACHE_VERSION}`,
  FONTS: `fonts-v${CACHE_VERSION}`,
  CRITICAL: `critical-v${CACHE_VERSION}`
};

// Cache configuration
const CACHE_CONFIG = {
  maxAge: {
    static: 30 * 24 * 60 * 60 * 1000, // 30 days
    dynamic: 7 * 24 * 60 * 60 * 1000,  // 7 days
    images: 14 * 24 * 60 * 60 * 1000,  // 14 days
    api: 5 * 60 * 1000,                // 5 minutes
    fonts: 365 * 24 * 60 * 60 * 1000   // 1 year
  },
  maxEntries: {
    static: 50,
    dynamic: 100,
    images: 200,
    api: 20,
    fonts: 10
  }
};

// Critical assets to cache immediately (cache-first strategy)
const CRITICAL_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/assets/critical.css',
  '/assets/critical-css-optimizer.js'
];

// Static assets (stale-while-revalidate strategy)
const STATIC_ASSETS = [
  '/assets/index-CIeRf8o1.css',
  '/assets/index-nipSLgUa.js',
  '/assets/performance.js',
  '/assets/accessibility.js',
  '/assets/resource-prioritizer.js',
  '/assets/css-splitter.js',
  '/vite.svg'
];

// Dynamic content patterns (network-first strategy)
const DYNAMIC_PATTERNS = [
  '/models/',
  '/textures/',
  '/assets/grid',
  '/assets/project-',
  '/assets/spotlight'
];

// Image patterns (cache-first with fallback)
const IMAGE_PATTERNS = [
  '.jpg', '.jpeg', '.png', '.webp', '.avif', '.svg', '.gif'
];

// Font patterns (cache-first, long-term)
const FONT_PATTERNS = [
  '.woff', '.woff2', '.ttf', '.otf'
];

// API patterns (network-first with short cache)
const API_PATTERNS = [
  '/api/', '/contact', '/form'
];

// Install event - cache critical and static assets
self.addEventListener('install', event => {
  console.log('Service Worker: Installing enhanced version...');
  
  event.waitUntil(
    Promise.all([
      // Cache critical assets first
      caches.open(CACHES.CRITICAL)
        .then(cache => {
          console.log('Service Worker: Caching critical assets');
          return cache.addAll(CRITICAL_ASSETS);
        }),
      
      // Cache static assets
      caches.open(CACHES.STATIC)
        .then(cache => {
          console.log('Service Worker: Caching static assets');
          return cache.addAll(STATIC_ASSETS);
        })
    ])
    .then(() => {
      console.log('Service Worker: All assets cached successfully');
      return self.skipWaiting();
    })
    .catch(error => {
      console.error('Service Worker: Error during installation', error);
    })
  );
});

// Activate event - clean up old caches and initialize cache management
self.addEventListener('activate', event => {
  console.log('Service Worker: Activating enhanced version...');
  
  event.waitUntil(
    Promise.all([
      // Clean up old caches
      caches.keys().then(cacheNames => {
        return Promise.all(
          cacheNames.map(cacheName => {
            const isCurrentCache = Object.values(CACHES).includes(cacheName);
            if (!isCurrentCache) {
              console.log('Service Worker: Deleting old cache', cacheName);
              return caches.delete(cacheName);
            }
          })
        );
      }),
      
      // Initialize cache cleanup scheduler
      initializeCacheCleanup(),
      
      // Claim all clients
      self.clients.claim()
    ])
    .then(() => {
      console.log('Service Worker: Enhanced activation complete');
      
      // Notify clients of successful activation
      self.clients.matchAll().then(clients => {
        clients.forEach(client => {
          client.postMessage({
            type: 'SW_ACTIVATED',
            version: CACHE_VERSION
          });
        });
      });
    })
  );
});

// Enhanced fetch event with intelligent caching strategies
self.addEventListener('fetch', event => {
  const { request } = event;
  const url = new URL(request.url);
  
  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }
  
  // Handle different request types with appropriate strategies
  if (url.origin === location.origin) {
    event.respondWith(handleRequest(request));
  } else if (FONT_PATTERNS.some(pattern => url.pathname.includes(pattern))) {
    // Handle external fonts (like Google Fonts)
    event.respondWith(handleFontRequest(request));
  }
});

// Main request handler with intelligent routing
async function handleRequest(request) {
  const url = new URL(request.url);
  const pathname = url.pathname;
  
  try {
    // Route to appropriate caching strategy
    if (isCriticalAsset(pathname)) {
      return await cacheFirstStrategy(request, CACHES.CRITICAL);
    } else if (isStaticAsset(pathname)) {
      return await staleWhileRevalidateStrategy(request, CACHES.STATIC);
    } else if (isImageAsset(pathname)) {
      return await cacheFirstWithFallbackStrategy(request, CACHES.IMAGES);
    } else if (isFontAsset(pathname)) {
      return await cacheFirstStrategy(request, CACHES.FONTS);
    } else if (isAPIRequest(pathname)) {
      return await networkFirstStrategy(request, CACHES.API);
    } else if (isDynamicAsset(pathname)) {
      return await networkFirstStrategy(request, CACHES.DYNAMIC);
    } else {
      // Default to stale-while-revalidate for unknown assets
      return await staleWhileRevalidateStrategy(request, CACHES.DYNAMIC);
    }
  } catch (error) {
    console.error('Service Worker: Request handling failed', error);
    return await handleOfflineResponse(request);
  }
}

// Cache-first strategy (for critical and static assets)
async function cacheFirstStrategy(request, cacheName) {
  const cachedResponse = await caches.match(request);
  
  if (cachedResponse) {
    // Check if cache is still valid
    if (await isCacheValid(cachedResponse, cacheName)) {
      console.log('Service Worker: Serving from cache (cache-first)', request.url);
      return cachedResponse;
    }
  }
  
  // Fetch from network and cache
  try {
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      await cacheResponse(request, networkResponse.clone(), cacheName);
    }
    return networkResponse;
  } catch (error) {
    // Return cached version even if expired, or offline fallback
    return cachedResponse || await handleOfflineResponse(request);
  }
}

// Network-first strategy (for dynamic content and APIs)
async function networkFirstStrategy(request, cacheName) {
  try {
    const networkResponse = await fetch(request);
    
    if (networkResponse.ok) {
      // Cache successful responses
      await cacheResponse(request, networkResponse.clone(), cacheName);
      console.log('Service Worker: Serving from network (network-first)', request.url);
      return networkResponse;
    }
    
    throw new Error(`Network response not ok: ${networkResponse.status}`);
  } catch (error) {
    // Fallback to cache
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      console.log('Service Worker: Serving from cache (network-first fallback)', request.url);
      return cachedResponse;
    }
    
    return await handleOfflineResponse(request);
  }
}

// Stale-while-revalidate strategy (for static assets that can be updated)
async function staleWhileRevalidateStrategy(request, cacheName) {
  const cachedResponse = await caches.match(request);
  
  // Always try to fetch fresh version in background
  const fetchPromise = fetch(request)
    .then(networkResponse => {
      if (networkResponse.ok) {
        cacheResponse(request, networkResponse.clone(), cacheName);
      }
      return networkResponse;
    })
    .catch(error => {
      console.warn('Service Worker: Background fetch failed', error);
    });
  
  if (cachedResponse) {
    console.log('Service Worker: Serving from cache (stale-while-revalidate)', request.url);
    return cachedResponse;
  }
  
  // No cached version, wait for network
  return await fetchPromise || await handleOfflineResponse(request);
}

// Cache-first with fallback strategy (for images)
async function cacheFirstWithFallbackStrategy(request, cacheName) {
  const cachedResponse = await caches.match(request);
  
  if (cachedResponse) {
    console.log('Service Worker: Serving image from cache', request.url);
    return cachedResponse;
  }
  
  try {
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      await cacheResponse(request, networkResponse.clone(), cacheName);
      return networkResponse;
    }
  } catch (error) {
    console.warn('Service Worker: Image fetch failed', error);
  }
  
  // Return placeholder image for failed image requests
  return await getImageFallback(request);
}

// Handle font requests (external fonts)
async function handleFontRequest(request) {
  const cachedResponse = await caches.match(request);
  
  if (cachedResponse) {
    return cachedResponse;
  }
  
  try {
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      await cacheResponse(request, networkResponse.clone(), CACHES.FONTS);
    }
    return networkResponse;
  } catch (error) {
    // Font loading failed, return empty response to prevent blocking
    return new Response('', {
      status: 200,
      headers: { 'Content-Type': 'font/woff2' }
    });
  }
}

// Helper functions for request classification
function isCriticalAsset(pathname) {
  return CRITICAL_ASSETS.some(asset => pathname === asset || pathname.endsWith(asset));
}

function isStaticAsset(pathname) {
  return STATIC_ASSETS.some(asset => pathname.includes(asset)) ||
         pathname.endsWith('.css') || pathname.endsWith('.js');
}

function isImageAsset(pathname) {
  return IMAGE_PATTERNS.some(pattern => pathname.includes(pattern));
}

function isFontAsset(pathname) {
  return FONT_PATTERNS.some(pattern => pathname.includes(pattern));
}

function isAPIRequest(pathname) {
  return API_PATTERNS.some(pattern => pathname.includes(pattern));
}

function isDynamicAsset(pathname) {
  return DYNAMIC_PATTERNS.some(pattern => pathname.includes(pattern));
}

// Cache management utilities
async function cacheResponse(request, response, cacheName) {
  if (!response.ok) return;
  
  const cache = await caches.open(cacheName);
  
  // Add timestamp for cache validation
  const responseWithTimestamp = new Response(response.body, {
    status: response.status,
    statusText: response.statusText,
    headers: {
      ...response.headers,
      'sw-cached-at': Date.now().toString()
    }
  });
  
  await cache.put(request, responseWithTimestamp);
  
  // Enforce cache size limits
  await enforceCacheLimit(cacheName);
}

async function isCacheValid(response, cacheName) {
  const cachedAt = response.headers.get('sw-cached-at');
  if (!cachedAt) return true; // Assume valid if no timestamp
  
  const age = Date.now() - parseInt(cachedAt);
  const maxAge = CACHE_CONFIG.maxAge[getCacheType(cacheName)] || CACHE_CONFIG.maxAge.dynamic;
  
  return age < maxAge;
}

function getCacheType(cacheName) {
  for (const [type, name] of Object.entries(CACHES)) {
    if (name === cacheName) return type.toLowerCase();
  }
  return 'dynamic';
}

async function enforceCacheLimit(cacheName) {
  const cacheType = getCacheType(cacheName);
  const maxEntries = CACHE_CONFIG.maxEntries[cacheType] || CACHE_CONFIG.maxEntries.dynamic;
  
  const cache = await caches.open(cacheName);
  const keys = await cache.keys();
  
  if (keys.length > maxEntries) {
    // Remove oldest entries (simple FIFO)
    const entriesToDelete = keys.slice(0, keys.length - maxEntries);
    await Promise.all(entriesToDelete.map(key => cache.delete(key)));
    console.log(`Service Worker: Cleaned ${entriesToDelete.length} entries from ${cacheName}`);
  }
}

async function initializeCacheCleanup() {
  // Set up periodic cache cleanup
  setInterval(async () => {
    for (const cacheName of Object.values(CACHES)) {
      await cleanExpiredEntries(cacheName);
    }
  }, 60 * 60 * 1000); // Run every hour
}

async function cleanExpiredEntries(cacheName) {
  const cache = await caches.open(cacheName);
  const keys = await cache.keys();
  
  for (const key of keys) {
    const response = await cache.match(key);
    if (response && !(await isCacheValid(response, cacheName))) {
      await cache.delete(key);
      console.log(`Service Worker: Removed expired entry from ${cacheName}:`, key.url);
    }
  }
}

async function handleOfflineResponse(request) {
  const url = new URL(request.url);
  
  if (request.headers.get('accept')?.includes('text/html')) {
    // Return cached index.html for navigation requests
    const cachedIndex = await caches.match('/index.html');
    if (cachedIndex) return cachedIndex;
    
    // Return offline page
    return new Response(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>Offline - Kabi Tharma Portfolio</title>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
                   background: linear-gradient(135deg, #010103 0%, #1c1c21 100%); 
                   color: white; text-align: center; padding: 2rem; }
            .offline-message { max-width: 500px; margin: 0 auto; }
            .retry-btn { background: #3b82f6; color: white; border: none; padding: 1rem 2rem; 
                        border-radius: 0.5rem; cursor: pointer; margin-top: 1rem; }
          </style>
        </head>
        <body>
          <div class="offline-message">
            <h1>You're Offline</h1>
            <p>It looks like you're not connected to the internet. Some content may not be available.</p>
            <button class="retry-btn" onclick="window.location.reload()">Try Again</button>
          </div>
        </body>
      </html>
    `, {
      headers: { 'Content-Type': 'text/html' }
    });
  }
  
  // Return appropriate offline response for other resource types
  return new Response('Offline', {
    status: 503,
    statusText: 'Service Unavailable'
  });
}

async function getImageFallback(request) {
  // Return a simple SVG placeholder for failed images
  const svg = `
    <svg width="400" height="300" xmlns="http://www.w3.org/2000/svg">
      <rect width="100%" height="100%" fill="#1c1c21"/>
      <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#ffffff" font-family="Arial, sans-serif">
        Image Unavailable
      </text>
    </svg>
  `;
  
  return new Response(svg, {
    headers: { 'Content-Type': 'image/svg+xml' }
  });
}

// Enhanced background sync for offline actions
self.addEventListener('sync', event => {
  console.log('Service Worker: Background sync', event.tag);
  
  if (event.tag === 'contact-form') {
    event.waitUntil(handleOfflineContactForm());
  } else if (event.tag === 'analytics') {
    event.waitUntil(handleOfflineAnalytics());
  } else if (event.tag === 'cache-cleanup') {
    event.waitUntil(performCacheCleanup());
  }
});

// Push notifications (for future use)
self.addEventListener('push', event => {
  console.log('Service Worker: Push received');
  
  const options = {
    body: event.data ? event.data.text() : 'New update available!',
    icon: '/assets/icon-192x192.png',
    badge: '/assets/badge-72x72.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    },
    actions: [
      {
        action: 'explore',
        title: 'View Portfolio',
        icon: '/assets/action-explore.png'
      },
      {
        action: 'close',
        title: 'Close',
        icon: '/assets/action-close.png'
      }
    ]
  };
  
  event.waitUntil(
    self.registration.showNotification('Kabi Portfolio', options)
  );
});

// Handle notification clicks
self.addEventListener('notificationclick', event => {
  console.log('Service Worker: Notification clicked');
  
  event.notification.close();
  
  if (event.action === 'explore') {
    event.waitUntil(
      clients.openWindow('/')
    );
  }
});

// Enhanced offline form handling
async function handleOfflineContactForm() {
  try {
    console.log('Service Worker: Processing offline contact form submissions');
    
    // Get queued form submissions from IndexedDB
    const queuedSubmissions = await getQueuedSubmissions();
    
    for (const submission of queuedSubmissions) {
      try {
        const response = await fetch('/api/contact', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(submission.data)
        });
        
        if (response.ok) {
          await removeQueuedSubmission(submission.id);
          console.log('Service Worker: Successfully sent queued form submission');
          
          // Notify client of successful submission
          const clients = await self.clients.matchAll();
          clients.forEach(client => {
            client.postMessage({
              type: 'FORM_SUBMITTED',
              success: true,
              submissionId: submission.id
            });
          });
        }
      } catch (error) {
        console.error('Service Worker: Failed to send queued submission', error);
      }
    }
  } catch (error) {
    console.error('Service Worker: Error handling offline contact forms', error);
  }
}

// Handle offline analytics
async function handleOfflineAnalytics() {
  try {
    console.log('Service Worker: Processing offline analytics events');
    
    const queuedEvents = await getQueuedAnalytics();
    
    for (const event of queuedEvents) {
      try {
        // Send to analytics service (Google Analytics, etc.)
        if (typeof gtag !== 'undefined') {
          gtag('event', event.action, event.data);
        }
        
        await removeQueuedAnalytics(event.id);
      } catch (error) {
        console.error('Service Worker: Failed to send analytics event', error);
      }
    }
  } catch (error) {
    console.error('Service Worker: Error handling offline analytics', error);
  }
}

// Perform comprehensive cache cleanup
async function performCacheCleanup() {
  try {
    console.log('Service Worker: Performing comprehensive cache cleanup');
    
    for (const cacheName of Object.values(CACHES)) {
      await cleanExpiredEntries(cacheName);
      await enforceCacheLimit(cacheName);
    }
    
    // Clean up orphaned caches
    const allCacheNames = await caches.keys();
    const validCacheNames = Object.values(CACHES);
    
    for (const cacheName of allCacheNames) {
      if (!validCacheNames.includes(cacheName)) {
        await caches.delete(cacheName);
        console.log('Service Worker: Deleted orphaned cache', cacheName);
      }
    }
  } catch (error) {
    console.error('Service Worker: Error during cache cleanup', error);
  }
}

// IndexedDB utilities for offline queue management
async function getQueuedSubmissions() {
  // Simplified implementation - would use IndexedDB in production
  return JSON.parse(localStorage.getItem('queuedSubmissions') || '[]');
}

async function removeQueuedSubmission(id) {
  const submissions = await getQueuedSubmissions();
  const filtered = submissions.filter(s => s.id !== id);
  localStorage.setItem('queuedSubmissions', JSON.stringify(filtered));
}

async function getQueuedAnalytics() {
  return JSON.parse(localStorage.getItem('queuedAnalytics') || '[]');
}

async function removeQueuedAnalytics(id) {
  const events = await getQueuedAnalytics();
  const filtered = events.filter(e => e.id !== id);
  localStorage.setItem('queuedAnalytics', JSON.stringify(filtered));
}

// Enhanced message handling and performance monitoring
self.addEventListener('message', event => {
  const { data } = event;
  
  if (!data) return;
  
  switch (data.type) {
    case 'PERFORMANCE_MARK':
      console.log('Service Worker: Performance mark', data.mark);
      break;
      
    case 'CRITICAL_CSS_OPTIMIZED':
      console.log('Service Worker: Critical CSS optimization complete', data.metrics);
      break;
      
    case 'QUEUE_FORM_SUBMISSION':
      queueFormSubmission(data.formData);
      break;
      
    case 'QUEUE_ANALYTICS_EVENT':
      queueAnalyticsEvent(data.eventData);
      break;
      
    case 'REQUEST_CACHE_STATUS':
      sendCacheStatus(event.ports[0]);
      break;
      
    case 'CLEAR_CACHE':
      clearSpecificCache(data.cacheName);
      break;
      
    case 'PREFETCH_RESOURCES':
      prefetchResources(data.resources);
      break;
      
    default:
      console.log('Service Worker: Unknown message type', data.type);
  }
});

// Queue form submission for offline handling
async function queueFormSubmission(formData) {
  try {
    const submissions = await getQueuedSubmissions();
    const newSubmission = {
      id: Date.now().toString(),
      data: formData,
      timestamp: Date.now()
    };
    
    submissions.push(newSubmission);
    localStorage.setItem('queuedSubmissions', JSON.stringify(submissions));
    
    console.log('Service Worker: Form submission queued for offline handling');
    
    // Register background sync
    if ('serviceWorker' in self && 'sync' in self.registration) {
      await self.registration.sync.register('contact-form');
    }
  } catch (error) {
    console.error('Service Worker: Error queuing form submission', error);
  }
}

// Queue analytics event for offline handling
async function queueAnalyticsEvent(eventData) {
  try {
    const events = await getQueuedAnalytics();
    const newEvent = {
      id: Date.now().toString(),
      ...eventData,
      timestamp: Date.now()
    };
    
    events.push(newEvent);
    localStorage.setItem('queuedAnalytics', JSON.stringify(events));
    
    console.log('Service Worker: Analytics event queued');
    
    // Register background sync
    if ('serviceWorker' in self && 'sync' in self.registration) {
      await self.registration.sync.register('analytics');
    }
  } catch (error) {
    console.error('Service Worker: Error queuing analytics event', error);
  }
}

// Send cache status to client
async function sendCacheStatus(port) {
  try {
    const status = {};
    
    for (const [type, cacheName] of Object.entries(CACHES)) {
      const cache = await caches.open(cacheName);
      const keys = await cache.keys();
      status[type] = {
        name: cacheName,
        entries: keys.length,
        urls: keys.map(key => key.url)
      };
    }
    
    port.postMessage({
      type: 'CACHE_STATUS',
      status
    });
  } catch (error) {
    console.error('Service Worker: Error getting cache status', error);
    port.postMessage({
      type: 'CACHE_STATUS_ERROR',
      error: error.message
    });
  }
}

// Clear specific cache
async function clearSpecificCache(cacheName) {
  try {
    const deleted = await caches.delete(cacheName);
    console.log(`Service Worker: Cache ${cacheName} cleared:`, deleted);
  } catch (error) {
    console.error('Service Worker: Error clearing cache', error);
  }
}

// Prefetch resources
async function prefetchResources(resources) {
  try {
    console.log('Service Worker: Prefetching resources', resources);
    
    for (const resource of resources) {
      try {
        const response = await fetch(resource);
        if (response.ok) {
          // Determine appropriate cache
          const cacheName = isImageAsset(resource) ? CACHES.IMAGES : CACHES.DYNAMIC;
          await cacheResponse(new Request(resource), response, cacheName);
        }
      } catch (error) {
        console.warn('Service Worker: Failed to prefetch resource', resource, error);
      }
    }
  } catch (error) {
    console.error('Service Worker: Error prefetching resources', error);
  }
}

console.log('Service Worker: Script loaded');
