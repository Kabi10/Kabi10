/**
 * App Initialization Script
 * Handles loading management, error handling, and basic app setup
 * Moved from inline scripts to external file for CSP compliance
 */

// Performance monitoring
if ('performance' in window && 'mark' in performance) {
  performance.mark('app-init-start');
}

// Enhanced loading management
window.addEventListener('load', function() {
  const loadingIndicator = document.getElementById('loading-indicator');
  if (loadingIndicator) {
    // Fade out loading indicator
    loadingIndicator.style.opacity = '0';
    loadingIndicator.style.transition = 'opacity 0.5s ease-out';
    setTimeout(() => {
      loadingIndicator.style.display = 'none';
      console.log('✅ Loading screen hidden by window load event');
    }, 500);
  }
});

// Error handling
window.addEventListener('error', function(e) {
  console.error('🚨 JavaScript Error:', e.error);
  console.error('🚨 Error details:', e.filename, e.lineno, e.colno);
  
  // If there's a critical error, hide loading screen
  setTimeout(function() {
    const loadingIndicator = document.getElementById('loading-indicator');
    if (loadingIndicator) {
      loadingIndicator.style.display = 'none';
      document.body.innerHTML += '<div style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: rgba(0,0,0,0.9); color: white; padding: 20px; border-radius: 8px; z-index: 10000;"><h3>Loading Error</h3><p>There was an error loading the portfolio. Please refresh the page.</p><button onclick="location.reload()" style="margin-top: 10px; padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 4px; cursor: pointer;">Refresh Page</button></div>';
    }
  }, 1000);
});

// Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function() {
    navigator.serviceWorker.register('/sw.js')
      .then(function(registration) {
        console.log('✅ ServiceWorker registration successful');
      })
      .catch(function(err) {
        console.log('❌ ServiceWorker registration failed: ', err);
      });
  });
}

// Contact Form Enhancement
document.addEventListener('DOMContentLoaded', function() {
  setTimeout(function() {
    const forms = document.querySelectorAll('form');
    forms.forEach(function(form) {
      form.addEventListener('submit', function(e) {
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
          submitBtn.disabled = true;
          submitBtn.textContent = 'Sending...';
          setTimeout(function() {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Send Message';
          }, 3000);
        }
      });
    });
  }, 2000);
});

// Debug: Check if React app mounted
setTimeout(function() {
  const root = document.getElementById('root');
  const loadingIndicator = document.getElementById('loading-indicator');
  
  console.log('🔍 Debug: Root element exists:', !!root);
  console.log('🔍 Debug: Root has content:', root ? root.innerHTML.length > 0 : false);
  console.log('🔍 Debug: Loading indicator exists:', !!loadingIndicator);
  
  if (loadingIndicator && (!root || root.innerHTML.length === 0)) {
    console.log('⚠️ React app failed to mount, hiding loading screen');
    loadingIndicator.style.display = 'none';
  }
}, 3000);

// Fallback: Remove loading indicator after timeout
setTimeout(function() {
  const loadingIndicator = document.getElementById('loading-indicator');
  if (loadingIndicator && loadingIndicator.style.display !== 'none') {
    console.log('⏰ Fallback: Hiding loading screen after 5 seconds');
    loadingIndicator.style.display = 'none';
  }
}, 5000);

// Keyboard navigation enhancement
document.addEventListener('keydown', function(e) {
  // Add visual focus indicators for keyboard navigation
  if (e.key === 'Tab') {
    document.body.classList.add('keyboard-navigation');
  }
});

document.addEventListener('mousedown', function() {
  document.body.classList.remove('keyboard-navigation');
});

// Reduced motion preference
if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
  document.documentElement.style.setProperty('--animation-duration', '0.01ms');
  document.documentElement.style.setProperty('--transition-duration', '0.01ms');
}

// Performance mark
if ('performance' in window && 'mark' in performance) {
  performance.mark('app-init-end');
  performance.measure('app-initialization', 'app-init-start', 'app-init-end');
}

// Features panel toggle function (moved from inline onclick)
window.toggleFeaturesPanel = function() {
  const panel = document.getElementById('features-panel');
  if (panel) {
    if (panel.style.display === 'none' || !panel.style.display) {
      panel.style.display = 'block';
      panel.style.animation = 'slideInRight 0.3s ease-out';
    } else {
      panel.style.animation = 'slideOutRight 0.3s ease-out';
      setTimeout(() => {
        panel.style.display = 'none';
      }, 300);
    }
  }
};

// Close banner function (moved from inline onclick)
window.closeBanner = function(element) {
  if (element && element.parentElement && element.parentElement.parentElement) {
    element.parentElement.parentElement.style.display = 'none';
  }
};

// Setup event listeners for buttons (replacing inline onclick)
document.addEventListener('DOMContentLoaded', function() {
  // Setup close button for modernization banner
  const closeBannerBtn = document.querySelector('[data-close-banner]');
  if (closeBannerBtn) {
    closeBannerBtn.addEventListener('click', function() {
      closeBanner(this);
    });
  }

  // Setup features FAB button
  const featuresFabBtn = document.querySelector('[data-toggle-features]');
  if (featuresFabBtn) {
    featuresFabBtn.addEventListener('click', function() {
      toggleFeaturesPanel();
    });
  }
});

console.log('✅ App initialization script loaded');
