/**
 * Enhanced Keyboard Navigation and Focus Management System
 * Comprehensive keyboard navigation with focus traps, shortcuts, and accessibility
 * Implements advanced focus management for complex UI components
 */

class KeyboardNavigationSystem {
  constructor() {
    this.focusableSelectors = [
      'a[href]:not([disabled])',
      'button:not([disabled])',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
      '[contenteditable="true"]',
      'audio[controls]',
      'video[controls]',
      'summary',
      'iframe'
    ].join(',');
    
    this.keyboardShortcuts = new Map();
    this.focusTraps = new Map();
    this.focusHistory = [];
    this.roving = new Map();
    
    this.config = {
      focusIndicatorStyle: {
        outline: '2px solid #0066cc',
        outlineOffset: '2px',
        borderRadius: '3px'
      },
      skipLinkStyle: {
        position: 'absolute',
        top: '-40px',
        left: '6px',
        background: '#000',
        color: '#fff',
        padding: '8px',
        textDecoration: 'none',
        borderRadius: '3px',
        zIndex: '10000'
      }
    };
    
    this.init();
  }

  async init() {
    this.setupKeyboardEventListeners();
    this.enhanceFocusIndicators();
    this.setupSkipLinks();
    this.setupKeyboardShortcuts();
    this.setupFocusTraps();
    this.setupRovingTabindex();
    this.setupSpatialNavigation();
    this.enhanceFormNavigation();
    this.setupAccessibilityAnnouncements();
    this.monitorFocusChanges();
  }

  /**
   * Setup comprehensive keyboard event listeners
   */
  setupKeyboardEventListeners() {
    // Main keyboard handler
    document.addEventListener('keydown', (event) => {
      this.handleKeyboardEvent(event);
    }, true);
    
    // Focus tracking
    document.addEventListener('focusin', (event) => {
      this.handleFocusIn(event);
    });
    
    document.addEventListener('focusout', (event) => {
      this.handleFocusOut(event);
    });
    
    // Prevent focus loss
    document.addEventListener('mousedown', (event) => {
      this.handleMouseDown(event);
    });
  }

  /**
   * Handle keyboard events with comprehensive navigation
   */
  handleKeyboardEvent(event) {
    const { key, ctrlKey, altKey, shiftKey, metaKey, target } = event;
    
    // Handle keyboard shortcuts
    if (this.handleKeyboardShortcuts(event)) {
      return;
    }
    
    // Handle escape key
    if (key === 'Escape') {
      this.handleEscape(event);
      return;
    }
    
    // Handle tab navigation
    if (key === 'Tab') {
      this.handleTabNavigation(event);
      return;
    }
    
    // Handle arrow key navigation
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(key)) {
      this.handleArrowNavigation(event);
      return;
    }
    
    // Handle Enter and Space
    if (key === 'Enter' || key === ' ') {
      this.handleActivation(event);
      return;
    }
    
    // Handle Home/End keys
    if (key === 'Home' || key === 'End') {
      this.handleHomeEnd(event);
      return;
    }
    
    // Handle Page Up/Down
    if (key === 'PageUp' || key === 'PageDown') {
      this.handlePageNavigation(event);
      return;
    }
  }

  /**
   * Handle keyboard shortcuts
   */
  handleKeyboardShortcuts(event) {
    const shortcutKey = this.getShortcutKey(event);
    const handler = this.keyboardShortcuts.get(shortcutKey);
    
    if (handler) {
      event.preventDefault();
      handler(event);
      return true;
    }
    
    return false;
  }

  /**
   * Get shortcut key string
   */
  getShortcutKey(event) {
    const parts = [];
    
    if (event.ctrlKey) parts.push('Ctrl');
    if (event.altKey) parts.push('Alt');
    if (event.shiftKey) parts.push('Shift');
    if (event.metaKey) parts.push('Meta');
    
    parts.push(event.key);
    
    return parts.join('+');
  }

  /**
   * Handle escape key for closing modals/menus
   */
  handleEscape(event) {
    const { target } = event;
    
    // Close modal
    const modal = target.closest('[role="dialog"], .modal, .popup');
    if (modal) {
      this.closeModal(modal);
      event.preventDefault();
      return;
    }
    
    // Close dropdown/menu
    const menu = target.closest('[role="menu"], [role="listbox"], .dropdown-menu');
    if (menu) {
      this.closeMenu(menu);
      event.preventDefault();
      return;
    }
    
    // Exit fullscreen
    if (document.fullscreenElement) {
      document.exitFullscreen();
      event.preventDefault();
      return;
    }
    
    // Clear search/filter
    if (target.type === 'search' || target.classList.contains('search-input')) {
      target.value = '';
      this.announceToScreenReader('Search cleared');
      event.preventDefault();
      return;
    }
  }

  /**
   * Handle tab navigation with focus traps
   */
  handleTabNavigation(event) {
    const { target, shiftKey } = event;
    
    // Check if we're in a focus trap
    const trapContainer = this.getFocusTrapContainer(target);
    if (trapContainer) {
      this.handleFocusTrap(event, trapContainer);
      return;
    }
    
    // Handle roving tabindex
    const rovingContainer = this.getRovingContainer(target);
    if (rovingContainer) {
      this.handleRovingTabindex(event, rovingContainer);
      return;
    }
    
    // Normal tab navigation with enhancements
    this.enhanceTabNavigation(event);
  }

  /**
   * Handle arrow key navigation
   */
  handleArrowNavigation(event) {
    const { key, target } = event;
    const role = target.getAttribute('role');
    const parent = target.parentElement;
    const parentRole = parent?.getAttribute('role');
    
    // Menu navigation
    if (role === 'menuitem' || parentRole === 'menu' || parentRole === 'menubar') {
      this.handleMenuNavigation(event);
      return;
    }
    
    // Tab navigation
    if (role === 'tab' || parentRole === 'tablist') {
      this.handleTabListNavigation(event);
      return;
    }
    
    // Grid navigation
    if (role === 'gridcell' || parentRole === 'grid') {
      this.handleGridNavigation(event);
      return;
    }
    
    // Listbox navigation
    if (role === 'option' || parentRole === 'listbox') {
      this.handleListboxNavigation(event);
      return;
    }
    
    // Slider navigation
    if (role === 'slider') {
      this.handleSliderNavigation(event);
      return;
    }
    
    // Custom component navigation
    if (target.dataset.arrowNavigation) {
      this.handleCustomArrowNavigation(event);
      return;
    }
  }

  /**
   * Handle menu navigation with arrow keys
   */
  handleMenuNavigation(event) {
    const { key, target } = event;
    const menu = target.closest('[role="menu"], [role="menubar"]');
    if (!menu) return;
    
    const menuItems = Array.from(menu.querySelectorAll('[role="menuitem"]:not([disabled])'));
    const currentIndex = menuItems.indexOf(target);
    
    let nextIndex;
    
    if (menu.getAttribute('role') === 'menubar') {
      // Horizontal menu
      if (key === 'ArrowLeft') {
        nextIndex = currentIndex > 0 ? currentIndex - 1 : menuItems.length - 1;
      } else if (key === 'ArrowRight') {
        nextIndex = currentIndex < menuItems.length - 1 ? currentIndex + 1 : 0;
      }
    } else {
      // Vertical menu
      if (key === 'ArrowUp') {
        nextIndex = currentIndex > 0 ? currentIndex - 1 : menuItems.length - 1;
      } else if (key === 'ArrowDown') {
        nextIndex = currentIndex < menuItems.length - 1 ? currentIndex + 1 : 0;
      }
    }
    
    if (nextIndex !== undefined) {
      menuItems[nextIndex].focus();
      event.preventDefault();
    }
  }

  /**
   * Handle tab list navigation
   */
  handleTabListNavigation(event) {
    const { key, target } = event;
    const tablist = target.closest('[role="tablist"]');
    if (!tablist) return;
    
    const tabs = Array.from(tablist.querySelectorAll('[role="tab"]:not([disabled])'));
    const currentIndex = tabs.indexOf(target);
    
    let nextIndex;
    
    if (key === 'ArrowLeft') {
      nextIndex = currentIndex > 0 ? currentIndex - 1 : tabs.length - 1;
    } else if (key === 'ArrowRight') {
      nextIndex = currentIndex < tabs.length - 1 ? currentIndex + 1 : 0;
    } else if (key === 'Home') {
      nextIndex = 0;
    } else if (key === 'End') {
      nextIndex = tabs.length - 1;
    }
    
    if (nextIndex !== undefined) {
      // Update tab selection
      tabs.forEach((tab, index) => {
        const isSelected = index === nextIndex;
        tab.setAttribute('aria-selected', isSelected);
        tab.setAttribute('tabindex', isSelected ? '0' : '-1');
        
        // Show/hide corresponding tab panel
        const panelId = tab.getAttribute('aria-controls');
        if (panelId) {
          const panel = document.getElementById(panelId);
          if (panel) {
            panel.hidden = !isSelected;
          }
        }
      });
      
      tabs[nextIndex].focus();
      event.preventDefault();
    }
  }

  /**
   * Handle grid navigation
   */
  handleGridNavigation(event) {
    const { key, target } = event;
    const grid = target.closest('[role="grid"]');
    if (!grid) return;
    
    const rows = Array.from(grid.querySelectorAll('[role="row"]'));
    const currentCell = target.closest('[role="gridcell"]');
    const currentRow = currentCell.closest('[role="row"]');
    const currentRowIndex = rows.indexOf(currentRow);
    const cells = Array.from(currentRow.querySelectorAll('[role="gridcell"]'));
    const currentCellIndex = cells.indexOf(currentCell);
    
    let targetCell;
    
    switch (key) {
      case 'ArrowLeft':
        targetCell = cells[currentCellIndex - 1];
        break;
      case 'ArrowRight':
        targetCell = cells[currentCellIndex + 1];
        break;
      case 'ArrowUp':
        if (currentRowIndex > 0) {
          const prevRow = rows[currentRowIndex - 1];
          const prevRowCells = prevRow.querySelectorAll('[role="gridcell"]');
          targetCell = prevRowCells[Math.min(currentCellIndex, prevRowCells.length - 1)];
        }
        break;
      case 'ArrowDown':
        if (currentRowIndex < rows.length - 1) {
          const nextRow = rows[currentRowIndex + 1];
          const nextRowCells = nextRow.querySelectorAll('[role="gridcell"]');
          targetCell = nextRowCells[Math.min(currentCellIndex, nextRowCells.length - 1)];
        }
        break;
      case 'Home':
        targetCell = cells[0];
        break;
      case 'End':
        targetCell = cells[cells.length - 1];
        break;
    }
    
    if (targetCell) {
      targetCell.focus();
      event.preventDefault();
    }
  }

  /**
   * Handle activation (Enter/Space)
   */
  handleActivation(event) {
    const { key, target } = event;
    const role = target.getAttribute('role');
    
    // Handle custom buttons
    if (role === 'button' && target.tagName !== 'BUTTON') {
      target.click();
      event.preventDefault();
      return;
    }
    
    // Handle links with role="button"
    if (target.tagName === 'A' && role === 'button') {
      target.click();
      event.preventDefault();
      return;
    }
    
    // Handle checkboxes and radio buttons (Space only)
    if (key === ' ' && (target.type === 'checkbox' || target.type === 'radio')) {
      // Let default behavior handle it
      return;
    }
    
    // Handle custom checkboxes
    if (role === 'checkbox') {
      this.toggleCheckbox(target);
      event.preventDefault();
      return;
    }
    
    // Handle tabs
    if (role === 'tab') {
      this.activateTab(target);
      event.preventDefault();
      return;
    }
    
    // Handle menu items
    if (role === 'menuitem') {
      target.click();
      event.preventDefault();
      return;
    }
  }

  /**
   * Setup enhanced focus indicators
   */
  enhanceFocusIndicators() {
    // Add CSS for enhanced focus indicators
    const style = document.createElement('style');
    style.textContent = `
      /* Enhanced focus indicators */
      *:focus {
        outline: ${this.config.focusIndicatorStyle.outline} !important;
        outline-offset: ${this.config.focusIndicatorStyle.outlineOffset} !important;
        border-radius: ${this.config.focusIndicatorStyle.borderRadius};
      }
      
      /* High contrast focus indicators */
      @media (prefers-contrast: high) {
        *:focus {
          outline: 3px solid #000 !important;
          outline-offset: 2px !important;
          background-color: #ffff00 !important;
          color: #000 !important;
        }
      }
      
      /* Reduced motion focus indicators */
      @media (prefers-reduced-motion: reduce) {
        *:focus {
          transition: none !important;
        }
      }
      
      /* Skip links */
      .skip-link {
        position: absolute;
        top: -40px;
        left: 6px;
        background: #000;
        color: #fff;
        padding: 8px;
        text-decoration: none;
        border-radius: 3px;
        z-index: 10000;
        transition: top 0.3s;
      }
      
      .skip-link:focus {
        top: 6px;
      }
      
      /* Focus trap indicators */
      .focus-trap-active {
        position: relative;
      }
      
      .focus-trap-active::before {
        content: '';
        position: absolute;
        top: -2px;
        left: -2px;
        right: -2px;
        bottom: -2px;
        border: 2px dashed #0066cc;
        pointer-events: none;
        z-index: 1000;
      }
    `;
    
    document.head.appendChild(style);
  }

  /**
   * Setup skip links for keyboard navigation
   */
  setupSkipLinks() {
    if (document.querySelector('.skip-links')) return;
    
    const skipLinks = document.createElement('nav');
    skipLinks.className = 'skip-links';
    skipLinks.setAttribute('aria-label', 'Skip links');
    
    // Skip links disabled - hidden from visual display
    const links = [];
    
    links.forEach(link => {
      const target = document.querySelector(link.href);
      if (target) {
        const skipLink = document.createElement('a');
        skipLink.href = link.href;
        skipLink.className = 'skip-link';
        skipLink.textContent = link.text;
        
        skipLink.addEventListener('click', (event) => {
          event.preventDefault();
          this.focusElement(target);
          this.announceToScreenReader(`Skipped to ${link.text.toLowerCase()}`);
        });
        
        skipLinks.appendChild(skipLink);
      }
    });
    
    document.body.insertBefore(skipLinks, document.body.firstChild);
  }

  /**
   * Setup keyboard shortcuts
   */
  setupKeyboardShortcuts() {
    // Navigation shortcuts
    this.addKeyboardShortcut('Alt+1', () => this.focusLandmark('main'));
    this.addKeyboardShortcut('Alt+2', () => this.focusLandmark('navigation'));
    this.addKeyboardShortcut('Alt+3', () => this.focusLandmark('search'));
    this.addKeyboardShortcut('Alt+0', () => this.showKeyboardHelp());
    
    // Application shortcuts
    this.addKeyboardShortcut('Ctrl+/', () => this.toggleSearch());
    this.addKeyboardShortcut('Escape', () => this.closeAllModals());
    
    // Accessibility shortcuts
    this.addKeyboardShortcut('Alt+Shift+A', () => this.toggleAccessibilityMode());
    this.addKeyboardShortcut('Alt+Shift+C', () => this.toggleHighContrast());
    this.addKeyboardShortcut('Alt+Shift+M', () => this.toggleReducedMotion());
  }

  /**
   * Add keyboard shortcut
   */
  addKeyboardShortcut(key, handler) {
    this.keyboardShortcuts.set(key, handler);
  }

  /**
   * Setup focus traps for modals and dialogs
   */
  setupFocusTraps() {
    // Observe for new modals
    if ('MutationObserver' in window) {
      const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
          mutation.addedNodes.forEach(node => {
            if (node.nodeType === 1) {
              const modals = node.matches?.('[role="dialog"]') ? [node] : 
                           node.querySelectorAll?.('[role="dialog"], .modal') || [];
              
              modals.forEach(modal => {
                if (modal.offsetParent !== null) { // Visible
                  this.createFocusTrap(modal);
                }
              });
            }
          });
        });
      });
      
      observer.observe(document.body, {
        childList: true,
        subtree: true
      });
    }
    
    // Setup existing modals
    document.querySelectorAll('[role="dialog"], .modal').forEach(modal => {
      this.createFocusTrap(modal);
    });
  }

  /**
   * Create focus trap for element
   */
  createFocusTrap(container) {
    const focusableElements = this.getFocusableElements(container);
    
    if (focusableElements.length === 0) return;
    
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];
    
    const trapData = {
      container,
      firstElement,
      lastElement,
      previousFocus: document.activeElement,
      active: false
    };
    
    this.focusTraps.set(container, trapData);
    
    // Activate trap when modal becomes visible
    const observer = new MutationObserver(() => {
      const isVisible = container.offsetParent !== null;
      if (isVisible && !trapData.active) {
        this.activateFocusTrap(container);
      } else if (!isVisible && trapData.active) {
        this.deactivateFocusTrap(container);
      }
    });
    
    observer.observe(container, {
      attributes: true,
      attributeFilter: ['style', 'class', 'hidden']
    });
  }

  /**
   * Activate focus trap
   */
  activateFocusTrap(container) {
    const trapData = this.focusTraps.get(container);
    if (!trapData) return;
    
    trapData.active = true;
    trapData.previousFocus = document.activeElement;
    
    // Focus first element
    trapData.firstElement.focus();
    
    // Add visual indicator
    container.classList.add('focus-trap-active');
    
    this.announceToScreenReader('Dialog opened. Press Escape to close.');
  }

  /**
   * Deactivate focus trap
   */
  deactivateFocusTrap(container) {
    const trapData = this.focusTraps.get(container);
    if (!trapData) return;
    
    trapData.active = false;
    
    // Restore previous focus
    if (trapData.previousFocus && trapData.previousFocus.focus) {
      trapData.previousFocus.focus();
    }
    
    // Remove visual indicator
    container.classList.remove('focus-trap-active');
    
    this.announceToScreenReader('Dialog closed.');
  }

  /**
   * Handle focus trap navigation
   */
  handleFocusTrap(event, container) {
    const trapData = this.focusTraps.get(container);
    if (!trapData || !trapData.active) return;
    
    const { shiftKey, target } = event;
    
    if (shiftKey) {
      // Shift+Tab - moving backwards
      if (target === trapData.firstElement) {
        trapData.lastElement.focus();
        event.preventDefault();
      }
    } else {
      // Tab - moving forwards
      if (target === trapData.lastElement) {
        trapData.firstElement.focus();
        event.preventDefault();
      }
    }
  }

  /**
   * Setup roving tabindex for complex widgets
   */
  setupRovingTabindex() {
    const widgets = document.querySelectorAll('[role="tablist"], [role="menu"], [role="menubar"], [role="grid"]');
    
    widgets.forEach(widget => {
      this.setupRovingForWidget(widget);
    });
  }

  /**
   * Setup roving tabindex for specific widget
   */
  setupRovingForWidget(widget) {
    const role = widget.getAttribute('role');
    let itemSelector;
    
    switch (role) {
      case 'tablist':
        itemSelector = '[role="tab"]';
        break;
      case 'menu':
      case 'menubar':
        itemSelector = '[role="menuitem"]';
        break;
      case 'grid':
        itemSelector = '[role="gridcell"]';
        break;
      default:
        return;
    }
    
    const items = Array.from(widget.querySelectorAll(itemSelector));
    
    if (items.length === 0) return;
    
    // Set initial tabindex values
    items.forEach((item, index) => {
      item.setAttribute('tabindex', index === 0 ? '0' : '-1');
    });
    
    this.roving.set(widget, { items, currentIndex: 0 });
  }

  /**
   * Handle roving tabindex navigation
   */
  handleRovingTabindex(event, container) {
    const rovingData = this.roving.get(container);
    if (!rovingData) return;
    
    const { items, currentIndex } = rovingData;
    const { shiftKey } = event;
    
    let newIndex;
    
    if (shiftKey) {
      newIndex = currentIndex > 0 ? currentIndex - 1 : items.length - 1;
    } else {
      newIndex = currentIndex < items.length - 1 ? currentIndex + 1 : 0;
    }
    
    // Update tabindex values
    items[currentIndex].setAttribute('tabindex', '-1');
    items[newIndex].setAttribute('tabindex', '0');
    items[newIndex].focus();
    
    rovingData.currentIndex = newIndex;
    
    event.preventDefault();
  }

  /**
   * Setup spatial navigation for 2D layouts
   */
  setupSpatialNavigation() {
    // Enable spatial navigation for grid layouts
    const grids = document.querySelectorAll('.grid-layout, [data-spatial-nav]');
    
    grids.forEach(grid => {
      this.enableSpatialNavigation(grid);
    });
  }

  /**
   * Enable spatial navigation for element
   */
  enableSpatialNavigation(container) {
    const items = Array.from(container.querySelectorAll(this.focusableSelectors));
    
    items.forEach(item => {
      item.addEventListener('keydown', (event) => {
        if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
          const nextItem = this.findSpatialNeighbor(item, event.key, items);
          if (nextItem) {
            nextItem.focus();
            event.preventDefault();
          }
        }
      });
    });
  }

  /**
   * Find spatial neighbor in given direction
   */
  findSpatialNeighbor(current, direction, items) {
    const currentRect = current.getBoundingClientRect();
    const candidates = items.filter(item => item !== current);
    
    let bestCandidate = null;
    let bestDistance = Infinity;
    
    candidates.forEach(candidate => {
      const candidateRect = candidate.getBoundingClientRect();
      
      let isValidDirection = false;
      let distance = 0;
      
      switch (direction) {
        case 'ArrowUp':
          isValidDirection = candidateRect.bottom <= currentRect.top;
          distance = currentRect.top - candidateRect.bottom + 
                   Math.abs(candidateRect.left - currentRect.left);
          break;
        case 'ArrowDown':
          isValidDirection = candidateRect.top >= currentRect.bottom;
          distance = candidateRect.top - currentRect.bottom + 
                   Math.abs(candidateRect.left - currentRect.left);
          break;
        case 'ArrowLeft':
          isValidDirection = candidateRect.right <= currentRect.left;
          distance = currentRect.left - candidateRect.right + 
                   Math.abs(candidateRect.top - currentRect.top);
          break;
        case 'ArrowRight':
          isValidDirection = candidateRect.left >= currentRect.right;
          distance = candidateRect.left - currentRect.right + 
                   Math.abs(candidateRect.top - currentRect.top);
          break;
      }
      
      if (isValidDirection && distance < bestDistance) {
        bestDistance = distance;
        bestCandidate = candidate;
      }
    });
    
    return bestCandidate;
  }

  /**
   * Enhance form navigation
   */
  enhanceFormNavigation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
      this.setupFormKeyboardNavigation(form);
    });
  }

  /**
   * Setup keyboard navigation for form
   */
  setupFormKeyboardNavigation(form) {
    const formElements = Array.from(form.querySelectorAll('input, select, textarea, button'));
    
    formElements.forEach((element, index) => {
      element.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && element.type !== 'textarea') {
          // Move to next form element or submit
          const nextElement = formElements[index + 1];
          if (nextElement) {
            nextElement.focus();
            event.preventDefault();
          } else {
            // Submit form if on last element
            const submitButton = form.querySelector('button[type="submit"], input[type="submit"]');
            if (submitButton) {
              submitButton.click();
            }
          }
        }
      });
    });
  }

  /**
   * Setup accessibility announcements
   */
  setupAccessibilityAnnouncements() {
    // Create live region for announcements
    if (!document.getElementById('a11y-announcer')) {
      const announcer = document.createElement('div');
      announcer.id = 'a11y-announcer';
      announcer.setAttribute('aria-live', 'polite');
      announcer.setAttribute('aria-atomic', 'true');
      announcer.style.cssText = `
        position: absolute;
        left: -10000px;
        width: 1px;
        height: 1px;
        overflow: hidden;
      `;
      document.body.appendChild(announcer);
    }
  }

  /**
   * Announce message to screen readers
   */
  announceToScreenReader(message, priority = 'polite') {
    const announcer = document.getElementById('a11y-announcer');
    if (announcer) {
      announcer.setAttribute('aria-live', priority);
      announcer.textContent = message;
      
      // Clear after announcement
      setTimeout(() => {
        announcer.textContent = '';
      }, 1000);
    }
  }

  /**
   * Monitor focus changes for debugging and analytics
   */
  monitorFocusChanges() {
    let focusPath = [];
    
    document.addEventListener('focusin', (event) => {
      const element = event.target;
      const elementInfo = {
        tagName: element.tagName,
        id: element.id,
        className: element.className,
        role: element.getAttribute('role'),
        timestamp: Date.now()
      };
      
      focusPath.push(elementInfo);
      
      // Keep only last 10 focus changes
      if (focusPath.length > 10) {
        focusPath.shift();
      }
      
      // Store for debugging
      this.focusHistory = focusPath;
    });
  }

  /**
   * Utility functions
   */
  
  getFocusableElements(container = document) {
    return Array.from(container.querySelectorAll(this.focusableSelectors))
      .filter(element => this.isVisible(element) && !element.disabled);
  }
  
  isVisible(element) {
    return element.offsetParent !== null && 
           window.getComputedStyle(element).visibility !== 'hidden';
  }
  
  focusElement(element) {
    if (element && element.focus) {
      element.focus();
      
      // Scroll into view if needed
      element.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }
  
  getFocusTrapContainer(element) {
    return element.closest('[role="dialog"], .modal, .focus-trap');
  }
  
  getRovingContainer(element) {
    return element.closest('[role="tablist"], [role="menu"], [role="menubar"], [role="grid"]');
  }
  
  closeModal(modal) {
    modal.style.display = 'none';
    modal.hidden = true;
    this.deactivateFocusTrap(modal);
  }
  
  closeMenu(menu) {
    menu.style.display = 'none';
    menu.hidden = true;
  }
  
  toggleCheckbox(checkbox) {
    const checked = checkbox.getAttribute('aria-checked') === 'true';
    checkbox.setAttribute('aria-checked', !checked);
    this.announceToScreenReader(`Checkbox ${!checked ? 'checked' : 'unchecked'}`);
  }
  
  activateTab(tab) {
    const tablist = tab.closest('[role="tablist"]');
    if (!tablist) return;
    
    const tabs = Array.from(tablist.querySelectorAll('[role="tab"]'));
    const panels = tabs.map(t => document.getElementById(t.getAttribute('aria-controls')))
                      .filter(Boolean);
    
    tabs.forEach((t, index) => {
      const isSelected = t === tab;
      t.setAttribute('aria-selected', isSelected);
      t.setAttribute('tabindex', isSelected ? '0' : '-1');
      
      if (panels[index]) {
        panels[index].hidden = !isSelected;
      }
    });
  }
  
  focusLandmark(landmark) {
    const element = document.querySelector(`[role="${landmark}"], ${landmark}`);
    if (element) {
      this.focusElement(element);
      this.announceToScreenReader(`Focused ${landmark} landmark`);
    }
  }
  
  showKeyboardHelp() {
    this.announceToScreenReader('Keyboard shortcuts: Alt+1 for main content, Alt+2 for navigation, Alt+3 for search, Escape to close dialogs');
  }
  
  toggleSearch() {
    const searchInput = document.querySelector('input[type="search"], .search-input');
    if (searchInput) {
      this.focusElement(searchInput);
    }
  }
  
  closeAllModals() {
    document.querySelectorAll('[role="dialog"], .modal').forEach(modal => {
      if (modal.offsetParent !== null) {
        this.closeModal(modal);
      }
    });
  }

  /**
   * Get current navigation status
   */
  getStatus() {
    return {
      focusTraps: this.focusTraps.size,
      keyboardShortcuts: this.keyboardShortcuts.size,
      rovingWidgets: this.roving.size,
      focusHistory: this.focusHistory.slice(-5), // Last 5 focus changes
      currentFocus: document.activeElement?.tagName || 'none'
    };
  }
}

// Initialize Keyboard Navigation System
const keyboardNavigationSystem = new KeyboardNavigationSystem();

// Export for global access
window.KeyboardNavigationSystem = KeyboardNavigationSystem;
window.keyboardNavigationSystem = keyboardNavigationSystem;

console.log('✅ Enhanced Keyboard Navigation System initialized');