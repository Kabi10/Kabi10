// Accessibility Enhancement Script for Kabi Tharma Portfolio
// Implements WCAG 2.1 AA compliance and modern accessibility features

class AccessibilityEnhancer {
  constructor() {
    this.focusableElements = [
      'a[href]',
      'button:not([disabled])',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
      '[contenteditable="true"]'
    ].join(',');
    
    this.preferences = {
      reducedMotion: false,
      highContrast: false,
      largeText: false,
      darkMode: false
    };
    
    this.init();
  }

  init() {
    this.detectUserPreferences();
    this.enhanceKeyboardNavigation();
    this.improveScreenReaderSupport();
    this.addARIALabels();
    this.enhanceColorContrast();
    this.setupFocusManagement();
    this.addAccessibilityControls();
    this.setupLiveRegions();
    this.enhanceFormAccessibility();
  }

  // Detect user accessibility preferences
  detectUserPreferences() {
    // Reduced motion preference
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      this.preferences.reducedMotion = true;
      document.documentElement.classList.add('reduce-motion');
      this.reduceAnimations();
    }

    // High contrast preference
    if (window.matchMedia('(prefers-contrast: high)').matches) {
      this.preferences.highContrast = true;
      document.documentElement.classList.add('high-contrast');
    }

    // Dark mode preference
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      this.preferences.darkMode = true;
      document.documentElement.classList.add('dark-mode');
    }

    // Listen for preference changes
    window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', (e) => {
      this.preferences.reducedMotion = e.matches;
      document.documentElement.classList.toggle('reduce-motion', e.matches);
      if (e.matches) this.reduceAnimations();
    });

    window.matchMedia('(prefers-contrast: high)').addEventListener('change', (e) => {
      this.preferences.highContrast = e.matches;
      document.documentElement.classList.toggle('high-contrast', e.matches);
    });
  }

  reduceAnimations() {
    const style = document.createElement('style');
    style.textContent = `
      *, *::before, *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
        scroll-behavior: auto !important;
      }
    `;
    document.head.appendChild(style);
  }

  // Enhanced keyboard navigation
  enhanceKeyboardNavigation() {
    // Track keyboard usage
    let isUsingKeyboard = false;

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        isUsingKeyboard = true;
        document.body.classList.add('keyboard-navigation');
      }

      // Escape key handling
      if (e.key === 'Escape') {
        this.handleEscapeKey();
      }

      // Arrow key navigation for custom components
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
        this.handleArrowKeyNavigation(e);
      }
    });

    document.addEventListener('mousedown', () => {
      isUsingKeyboard = false;
      document.body.classList.remove('keyboard-navigation');
    });

    // Skip links
    this.addSkipLinks();
  }

  addSkipLinks() {
    // Skip links disabled - hidden from visual display
    return;
  }

  handleEscapeKey() {
    // Close modals, dropdowns, etc.
    const openModals = document.querySelectorAll('[role="dialog"][aria-hidden="false"]');
    openModals.forEach(modal => {
      modal.setAttribute('aria-hidden', 'true');
      modal.style.display = 'none';
    });

    const openDropdowns = document.querySelectorAll('[aria-expanded="true"]');
    openDropdowns.forEach(dropdown => {
      dropdown.setAttribute('aria-expanded', 'false');
    });
  }

  handleArrowKeyNavigation(e) {
    const activeElement = document.activeElement;
    const parent = activeElement.closest('[role="menu"], [role="listbox"], [role="grid"]');
    
    if (parent) {
      e.preventDefault();
      const items = parent.querySelectorAll('[role="menuitem"], [role="option"], [role="gridcell"]');
      const currentIndex = Array.from(items).indexOf(activeElement);
      
      let nextIndex;
      switch (e.key) {
        case 'ArrowDown':
        case 'ArrowRight':
          nextIndex = (currentIndex + 1) % items.length;
          break;
        case 'ArrowUp':
        case 'ArrowLeft':
          nextIndex = (currentIndex - 1 + items.length) % items.length;
          break;
      }
      
      if (nextIndex !== undefined) {
        items[nextIndex].focus();
      }
    }
  }

  // Screen reader support
  improveScreenReaderSupport() {
    // Add screen reader only descriptions
    this.addScreenReaderDescriptions();
    
    // Announce page changes
    this.announcePageChanges();
    
    // Handle dynamic content updates
    this.handleDynamicContent();
  }

  addScreenReaderDescriptions() {
    // Add descriptions for complex UI elements
    const complexElements = document.querySelectorAll('canvas, svg, [data-3d-model]');
    
    complexElements.forEach(element => {
      if (!element.getAttribute('aria-label') && !element.getAttribute('aria-labelledby')) {
        const description = this.generateElementDescription(element);
        if (description) {
          const descId = `desc-${Math.random().toString(36).substr(2, 9)}`;
          const descElement = document.createElement('div');
          descElement.id = descId;
          descElement.className = 'sr-only';
          descElement.textContent = description;
          element.parentNode.insertBefore(descElement, element.nextSibling);
          element.setAttribute('aria-describedby', descId);
        }
      }
    });
  }

  generateElementDescription(element) {
    if (element.tagName === 'CANVAS') {
      return 'Interactive 3D visualization. Use keyboard navigation to explore.';
    }
    if (element.tagName === 'SVG') {
      return element.querySelector('title')?.textContent || 'Graphic element';
    }
    if (element.hasAttribute('data-3d-model')) {
      return 'Interactive 3D model. Use arrow keys to rotate and explore.';
    }
    return null;
  }

  announcePageChanges() {
    // Create announcement region
    const announcer = document.createElement('div');
    announcer.id = 'page-announcer';
    announcer.setAttribute('aria-live', 'polite');
    announcer.setAttribute('aria-atomic', 'true');
    announcer.className = 'sr-only';
    document.body.appendChild(announcer);

    // Announce route changes (for SPA)
    let lastUrl = location.href;
    new MutationObserver(() => {
      const url = location.href;
      if (url !== lastUrl) {
        lastUrl = url;
        this.announcePageChange();
      }
    }).observe(document, { subtree: true, childList: true });
  }

  announcePageChange() {
    const announcer = document.getElementById('page-announcer');
    const pageTitle = document.title;
    const mainHeading = document.querySelector('h1')?.textContent;
    
    const announcement = mainHeading 
      ? `Navigated to ${mainHeading}` 
      : `Page changed: ${pageTitle}`;
    
    announcer.textContent = announcement;
  }

  handleDynamicContent() {
    // Observe dynamic content changes
    const observer = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        if (mutation.type === 'childList') {
          mutation.addedNodes.forEach(node => {
            if (node.nodeType === Node.ELEMENT_NODE) {
              this.enhanceNewContent(node);
            }
          });
        }
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  enhanceNewContent(element) {
    // Add ARIA labels to new elements
    this.addARIALabelsToElement(element);
    
    // Enhance form elements
    const forms = element.querySelectorAll('form, input, textarea, select');
    forms.forEach(form => this.enhanceFormElement(form));
  }

  // ARIA labels and roles
  addARIALabels() {
    // Navigation elements
    const navElements = document.querySelectorAll('nav');
    navElements.forEach((nav, index) => {
      if (!nav.getAttribute('aria-label')) {
        nav.setAttribute('aria-label', index === 0 ? 'Main navigation' : `Navigation ${index + 1}`);
      }
    });

    // Buttons without labels
    const buttons = document.querySelectorAll('button:not([aria-label]):not([aria-labelledby])');
    buttons.forEach(button => {
      const text = button.textContent.trim();
      const icon = button.querySelector('svg, img');
      
      if (!text && icon) {
        const label = this.generateButtonLabel(button);
        if (label) {
          button.setAttribute('aria-label', label);
        }
      }
    });

    // Links without descriptive text
    const links = document.querySelectorAll('a:not([aria-label]):not([aria-labelledby])');
    links.forEach(link => {
      const text = link.textContent.trim();
      if (!text || text === 'Read more' || text === 'Click here') {
        const context = this.getLinkContext(link);
        if (context) {
          link.setAttribute('aria-label', `${text} ${context}`);
        }
      }
    });

    // Images without alt text
    const images = document.querySelectorAll('img:not([alt])');
    images.forEach(img => {
      img.setAttribute('alt', ''); // Decorative by default
    });
  }

  addARIALabelsToElement(element) {
    // Add ARIA labels to specific element and its children
    const buttons = element.querySelectorAll('button:not([aria-label])');
    buttons.forEach(button => {
      const label = this.generateButtonLabel(button);
      if (label) {
        button.setAttribute('aria-label', label);
      }
    });
  }

  generateButtonLabel(button) {
    const icon = button.querySelector('svg, img');
    const classes = button.className;
    
    if (classes.includes('close')) return 'Close';
    if (classes.includes('menu')) return 'Open menu';
    if (classes.includes('search')) return 'Search';
    if (classes.includes('play')) return 'Play';
    if (classes.includes('pause')) return 'Pause';
    if (classes.includes('next')) return 'Next';
    if (classes.includes('previous')) return 'Previous';
    
    return null;
  }

  getLinkContext(link) {
    const parent = link.closest('article, section, .card, .project');
    if (parent) {
      const heading = parent.querySelector('h1, h2, h3, h4, h5, h6');
      if (heading) {
        return `about ${heading.textContent.trim()}`;
      }
    }
    return null;
  }

  // Color contrast enhancement
  enhanceColorContrast() {
    if (this.preferences.highContrast) {
      const style = document.createElement('style');
      style.textContent = `
        :root {
          --color-text-primary: #ffffff !important;
          --color-text-secondary: #ffffff !important;
          --color-text-muted: #cccccc !important;
          --color-accent: #ffffff !important;
        }
        
        .text-gray-400 { color: #cccccc !important; }
        .text-neutral-400 { color: #cccccc !important; }
        .text-white-500 { color: #ffffff !important; }
        .text-white-600 { color: #ffffff !important; }
        .text-white-800 { color: #ffffff !important; }
        
        button, .btn {
          border: 2px solid #ffffff !important;
        }
        
        a:focus, button:focus, input:focus, textarea:focus {
          outline: 3px solid #ffffff !important;
          outline-offset: 2px !important;
        }
      `;
      document.head.appendChild(style);
    }
  }

  // Focus management
  setupFocusManagement() {
    // Focus trap for modals
    this.setupFocusTraps();
    
    // Focus restoration
    this.setupFocusRestoration();
    
    // Enhanced focus indicators
    this.enhanceFocusIndicators();
  }

  setupFocusTraps() {
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        const modal = document.querySelector('[role="dialog"]:not([aria-hidden="true"])');
        if (modal) {
          this.trapFocus(e, modal);
        }
      }
    });
  }

  trapFocus(e, container) {
    const focusableElements = container.querySelectorAll(this.focusableElements);
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    if (e.shiftKey) {
      if (document.activeElement === firstElement) {
        e.preventDefault();
        lastElement.focus();
      }
    } else {
      if (document.activeElement === lastElement) {
        e.preventDefault();
        firstElement.focus();
      }
    }
  }

  setupFocusRestoration() {
    let lastFocusedElement = null;

    document.addEventListener('focusin', (e) => {
      if (!e.target.closest('[role="dialog"]')) {
        lastFocusedElement = e.target;
      }
    });

    // Restore focus when modal closes
    const observer = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'aria-hidden') {
          const modal = mutation.target;
          if (modal.getAttribute('aria-hidden') === 'true' && lastFocusedElement) {
            lastFocusedElement.focus();
          }
        }
      });
    });

    document.querySelectorAll('[role="dialog"]').forEach(modal => {
      observer.observe(modal, { attributes: true });
    });
  }

  enhanceFocusIndicators() {
    const style = document.createElement('style');
    style.textContent = `
      .keyboard-navigation *:focus {
        outline: 2px solid #3b82f6 !important;
        outline-offset: 2px !important;
        border-radius: 4px !important;
      }
      
      .keyboard-navigation button:focus,
      .keyboard-navigation a:focus {
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3) !important;
      }
    `;
    document.head.appendChild(style);
  }

  // Live regions for dynamic updates
  setupLiveRegions() {
    // Status messages
    const statusRegion = document.createElement('div');
    statusRegion.id = 'status-region';
    statusRegion.setAttribute('aria-live', 'polite');
    statusRegion.setAttribute('aria-atomic', 'true');
    statusRegion.className = 'sr-only';
    document.body.appendChild(statusRegion);

    // Alert messages
    const alertRegion = document.createElement('div');
    alertRegion.id = 'alert-region';
    alertRegion.setAttribute('aria-live', 'assertive');
    alertRegion.setAttribute('aria-atomic', 'true');
    alertRegion.className = 'sr-only';
    document.body.appendChild(alertRegion);
  }

  announceStatus(message) {
    const statusRegion = document.getElementById('status-region');
    statusRegion.textContent = message;
  }

  announceAlert(message) {
    const alertRegion = document.getElementById('alert-region');
    alertRegion.textContent = message;
  }

  // Form accessibility
  enhanceFormAccessibility() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => this.enhanceForm(form));
  }

  enhanceForm(form) {
    // Associate labels with inputs
    const inputs = form.querySelectorAll('input, textarea, select');
    inputs.forEach(input => {
      if (!input.getAttribute('aria-label') && !input.getAttribute('aria-labelledby')) {
        const label = form.querySelector(`label[for="${input.id}"]`) || 
                     input.closest('.form-group')?.querySelector('label');
        
        if (label && !label.getAttribute('for')) {
          if (!input.id) {
            input.id = `input-${Math.random().toString(36).substr(2, 9)}`;
          }
          label.setAttribute('for', input.id);
        }
      }
    });

    // Add error handling
    form.addEventListener('submit', (e) => {
      const errors = this.validateForm(form);
      if (errors.length > 0) {
        e.preventDefault();
        this.displayFormErrors(form, errors);
      }
    });
  }

  enhanceFormElement(element) {
    if (element.tagName === 'FORM') {
      this.enhanceForm(element);
    } else if (['INPUT', 'TEXTAREA', 'SELECT'].includes(element.tagName)) {
      // Individual form element enhancement
      if (!element.getAttribute('aria-label') && !element.getAttribute('aria-labelledby')) {
        const label = element.closest('form')?.querySelector(`label[for="${element.id}"]`);
        if (label && !label.getAttribute('for')) {
          if (!element.id) {
            element.id = `input-${Math.random().toString(36).substr(2, 9)}`;
          }
          label.setAttribute('for', element.id);
        }
      }
    }
  }

  validateForm(form) {
    const errors = [];
    const requiredFields = form.querySelectorAll('[required]');
    
    requiredFields.forEach(field => {
      if (!field.value.trim()) {
        errors.push({
          field: field,
          message: `${this.getFieldLabel(field)} is required`
        });
      }
    });
    
    return errors;
  }

  getFieldLabel(field) {
    const label = document.querySelector(`label[for="${field.id}"]`);
    return label?.textContent.trim() || field.getAttribute('aria-label') || field.name || 'Field';
  }

  displayFormErrors(form, errors) {
    // Remove existing error messages
    form.querySelectorAll('.error-message').forEach(error => error.remove());
    
    errors.forEach(error => {
      const errorElement = document.createElement('div');
      errorElement.className = 'error-message text-red-500 text-sm mt-1';
      errorElement.textContent = error.message;
      errorElement.setAttribute('role', 'alert');
      
      error.field.parentNode.appendChild(errorElement);
      error.field.setAttribute('aria-invalid', 'true');
      error.field.setAttribute('aria-describedby', errorElement.id = `error-${error.field.id}`);
    });
    
    // Focus first error field
    if (errors.length > 0) {
      errors[0].field.focus();
      this.announceAlert(`Form has ${errors.length} error${errors.length > 1 ? 's' : ''}`);
    }
  }

  // Accessibility controls
  addAccessibilityControls() {
    const controlsContainer = document.createElement('div');
    controlsContainer.className = 'accessibility-controls fixed top-4 right-4 z-50 bg-black-200 p-4 rounded-lg border border-white/20';
    controlsContainer.setAttribute('aria-label', 'Accessibility controls');
    
    const controls = [
      { id: 'toggle-high-contrast', label: 'High Contrast', action: () => this.toggleHighContrast() },
      { id: 'toggle-large-text', label: 'Large Text', action: () => this.toggleLargeText() },
      { id: 'toggle-reduced-motion', label: 'Reduce Motion', action: () => this.toggleReducedMotion() }
    ];
    
    controls.forEach(control => {
      const button = document.createElement('button');
      button.id = control.id;
      button.textContent = control.label;
      button.className = 'block w-full text-left px-3 py-2 text-sm text-white hover:bg-white/10 rounded';
      button.addEventListener('click', control.action);
      controlsContainer.appendChild(button);
    });
    
    // Toggle button for controls
    const toggleButton = document.createElement('button');
    toggleButton.textContent = 'Accessibility';
    toggleButton.className = 'accessibility-toggle bg-blue-600 text-white px-3 py-2 rounded-md text-sm';
    toggleButton.setAttribute('aria-expanded', 'false');
    toggleButton.setAttribute('aria-controls', 'accessibility-controls');
    
    controlsContainer.id = 'accessibility-controls';
    controlsContainer.style.display = 'none';
    
    toggleButton.addEventListener('click', () => {
      const isExpanded = toggleButton.getAttribute('aria-expanded') === 'true';
      toggleButton.setAttribute('aria-expanded', !isExpanded);
      controlsContainer.style.display = isExpanded ? 'none' : 'block';
    });
    
    // Hide accessibility toggle button
    toggleButton.style.display = 'none';
    controlsContainer.style.display = 'none';

    document.body.appendChild(toggleButton);
    document.body.appendChild(controlsContainer);
  }

  toggleHighContrast() {
    this.preferences.highContrast = !this.preferences.highContrast;
    document.documentElement.classList.toggle('high-contrast', this.preferences.highContrast);
    this.announceStatus(`High contrast ${this.preferences.highContrast ? 'enabled' : 'disabled'}`);
  }

  toggleLargeText() {
    this.preferences.largeText = !this.preferences.largeText;
    document.documentElement.classList.toggle('large-text', this.preferences.largeText);
    this.announceStatus(`Large text ${this.preferences.largeText ? 'enabled' : 'disabled'}`);
  }

  toggleReducedMotion() {
    this.preferences.reducedMotion = !this.preferences.reducedMotion;
    document.documentElement.classList.toggle('reduce-motion', this.preferences.reducedMotion);
    if (this.preferences.reducedMotion) {
      this.reduceAnimations();
    }
    // Reduced motion announcement disabled
  }

  // Public API
  getAccessibilityStatus() {
    return {
      preferences: this.preferences,
      focusableElementsCount: document.querySelectorAll(this.focusableElements).length,
      imagesWithoutAlt: document.querySelectorAll('img:not([alt])').length,
      buttonsWithoutLabels: document.querySelectorAll('button:not([aria-label]):not([aria-labelledby])').length
    };
  }
}

// Initialize accessibility enhancer
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.accessibilityEnhancer = new AccessibilityEnhancer();
  });
} else {
  window.accessibilityEnhancer = new AccessibilityEnhancer();
}

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = AccessibilityEnhancer;
}
