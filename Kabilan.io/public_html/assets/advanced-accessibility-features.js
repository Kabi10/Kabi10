/**
 * Advanced Accessibility Features System
 * High contrast mode, reduced motion preferences, voice commands, and user customization
 * Implements cutting-edge accessibility enhancements beyond WCAG requirements
 */

class AdvancedAccessibilityFeatures {
  constructor() {
    this.preferences = {
      highContrast: false,
      reducedMotion: false,
      largeText: false,
      darkMode: false,
      voiceCommands: false,
      screenReader: false,
      colorBlindness: 'none',
      dyslexiaMode: false,
      focusAssist: false
    };
    
    this.voiceRecognition = null;
    this.speechSynthesis = null;
    this.colorFilters = new Map();
    
    this.config = {
      contrastRatios: {
        normal: 7, // AAA level
        enhanced: 15 // Beyond AAA
      },
      textSizeMultipliers: {
        small: 0.875,
        normal: 1,
        large: 1.25,
        xlarge: 1.5,
        xxlarge: 2
      },
      animationDurations: {
        none: 0,
        reduced: 0.1,
        normal: 0.3,
        enhanced: 0.6
      }
    };
    
    this.init();
  }

  async init() {
    this.loadUserPreferences();
    this.detectSystemPreferences();
    this.setupHighContrastMode();
    this.setupReducedMotionMode();
    this.setupTextSizeControls();
    this.setupColorBlindnessSupport();
    this.setupDyslexiaMode();
    this.setupVoiceCommands();
    this.setupScreenReaderEnhancements();
    this.setupFocusAssist();
    this.setupAccessibilityPanel();
    this.setupPreferenceSync();
    this.monitorAccessibilityChanges();
  }

  /**
   * Load user preferences from storage
   */
  loadUserPreferences() {
    const stored = localStorage.getItem('accessibility_preferences');
    if (stored) {
      try {
        this.preferences = { ...this.preferences, ...JSON.parse(stored) };
      } catch (error) {
        console.warn('Failed to load accessibility preferences:', error);
      }
    }
  }

  /**
   * Save user preferences to storage
   */
  saveUserPreferences() {
    try {
      localStorage.setItem('accessibility_preferences', JSON.stringify(this.preferences));
      
      // Sync across tabs
      window.dispatchEvent(new CustomEvent('accessibility-preferences-changed', {
        detail: this.preferences
      }));
      
    } catch (error) {
      console.warn('Failed to save accessibility preferences:', error);
    }
  }

  /**
   * Detect system accessibility preferences
   */
  detectSystemPreferences() {
    // Reduced motion
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      this.preferences.reducedMotion = true;
    }
    
    // High contrast
    if (window.matchMedia('(prefers-contrast: high)').matches) {
      this.preferences.highContrast = true;
    }
    
    // Dark mode
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      this.preferences.darkMode = true;
    }
    
    // Listen for changes
    window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', (e) => {
      this.setReducedMotion(e.matches);
    });
    
    window.matchMedia('(prefers-contrast: high)').addEventListener('change', (e) => {
      this.setHighContrast(e.matches);
    });
    
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      this.setDarkMode(e.matches);
    });
    
    console.log('🎯 Detected system preferences:', {
      reducedMotion: this.preferences.reducedMotion,
      highContrast: this.preferences.highContrast,
      darkMode: this.preferences.darkMode
    });
  }

  /**
   * Setup high contrast mode
   */
  setupHighContrastMode() {
    if (this.preferences.highContrast) {
      this.enableHighContrast();
    }
  }

  /**
   * Enable high contrast mode
   */
  enableHighContrast() {
    document.documentElement.classList.add('high-contrast');
    
    // Add high contrast CSS
    if (!document.getElementById('high-contrast-styles')) {
      const style = document.createElement('style');
      style.id = 'high-contrast-styles';
      style.textContent = `
        .high-contrast {
          --bg-primary: #000000;
          --bg-secondary: #1a1a1a;
          --text-primary: #ffffff;
          --text-secondary: #cccccc;
          --accent-primary: #ffff00;
          --accent-secondary: #00ffff;
          --border-color: #ffffff;
          --focus-color: #ffff00;
          --error-color: #ff0000;
          --success-color: #00ff00;
          --warning-color: #ffaa00;
        }
        
        .high-contrast * {
          background-color: var(--bg-primary) !important;
          color: var(--text-primary) !important;
          border-color: var(--border-color) !important;
        }
        
        .high-contrast a {
          color: var(--accent-secondary) !important;
          text-decoration: underline !important;
        }
        
        .high-contrast button,
        .high-contrast input,
        .high-contrast select,
        .high-contrast textarea {
          background-color: var(--bg-secondary) !important;
          color: var(--text-primary) !important;
          border: 2px solid var(--border-color) !important;
        }
        
        .high-contrast *:focus {
          outline: 3px solid var(--focus-color) !important;
          outline-offset: 2px !important;
          background-color: var(--bg-secondary) !important;
        }
        
        .high-contrast img {
          filter: contrast(150%) brightness(120%) !important;
        }
        
        .high-contrast .btn-primary,
        .high-contrast [role="button"] {
          background-color: var(--accent-primary) !important;
          color: var(--bg-primary) !important;
          border: 2px solid var(--text-primary) !important;
        }
        
        .high-contrast .error {
          color: var(--error-color) !important;
          background-color: var(--bg-primary) !important;
        }
        
        .high-contrast .success {
          color: var(--success-color) !important;
          background-color: var(--bg-primary) !important;
        }
        
        .high-contrast .warning {
          color: var(--warning-color) !important;
          background-color: var(--bg-primary) !important;
        }
      `;
      document.head.appendChild(style);
    }
    
    this.announceChange('High contrast mode enabled');
  }

  /**
   * Disable high contrast mode
   */
  disableHighContrast() {
    document.documentElement.classList.remove('high-contrast');
    this.announceChange('High contrast mode disabled');
  }

  /**
   * Setup reduced motion mode
   */
  setupReducedMotionMode() {
    if (this.preferences.reducedMotion) {
      this.enableReducedMotion();
    }
  }

  /**
   * Enable reduced motion mode
   */
  enableReducedMotion() {
    document.documentElement.classList.add('reduce-motion');
    
    // Add reduced motion CSS
    if (!document.getElementById('reduced-motion-styles')) {
      const style = document.createElement('style');
      style.id = 'reduced-motion-styles';
      style.textContent = `
        .reduce-motion *,
        .reduce-motion *::before,
        .reduce-motion *::after {
          animation-duration: 0.01ms !important;
          animation-iteration-count: 1 !important;
          transition-duration: 0.01ms !important;
          scroll-behavior: auto !important;
        }
        
        .reduce-motion .parallax {
          transform: none !important;
        }
        
        .reduce-motion .carousel {
          scroll-behavior: auto !important;
        }
        
        .reduce-motion video {
          animation: none !important;
        }
        
        .reduce-motion .loading-spinner {
          animation: none !important;
        }
        
        .reduce-motion .fade-in,
        .reduce-motion .slide-in,
        .reduce-motion .zoom-in {
          opacity: 1 !important;
          transform: none !important;
        }
      `;
      document.head.appendChild(style);
    }
    
    // Pause auto-playing videos
    document.querySelectorAll('video[autoplay]').forEach(video => {
      video.pause();
    });
    
    // Reduced motion announcement disabled
  }

  /**
   * Disable reduced motion mode
   */
  disableReducedMotion() {
    document.documentElement.classList.remove('reduce-motion');
    // Reduced motion announcement disabled
  }

  /**
   * Setup text size controls
   */
  setupTextSizeControls() {
    if (this.preferences.largeText) {
      this.setTextSize('large');
    }
  }

  /**
   * Set text size
   */
  setTextSize(size) {
    const multiplier = this.config.textSizeMultipliers[size] || 1;
    
    document.documentElement.style.setProperty('--text-scale', multiplier);
    document.documentElement.setAttribute('data-text-size', size);
    
    // Add text scaling CSS if not present
    if (!document.getElementById('text-scaling-styles')) {
      const style = document.createElement('style');
      style.id = 'text-scaling-styles';
      style.textContent = `
        :root {
          --text-scale: 1;
        }
        
        body {
          font-size: calc(1rem * var(--text-scale));
        }
        
        h1 { font-size: calc(2.5rem * var(--text-scale)); }
        h2 { font-size: calc(2rem * var(--text-scale)); }
        h3 { font-size: calc(1.75rem * var(--text-scale)); }
        h4 { font-size: calc(1.5rem * var(--text-scale)); }
        h5 { font-size: calc(1.25rem * var(--text-scale)); }
        h6 { font-size: calc(1.125rem * var(--text-scale)); }
        
        p, li, td, th {
          font-size: calc(1rem * var(--text-scale));
          line-height: calc(1.5 * var(--text-scale));
        }
        
        small {
          font-size: calc(0.875rem * var(--text-scale));
        }
        
        button, input, select, textarea {
          font-size: calc(1rem * var(--text-scale));
        }
      `;
      document.head.appendChild(style);
    }
    
    this.preferences.largeText = size !== 'normal';
    this.announceChange(`Text size set to ${size}`);
  }

  /**
   * Setup color blindness support
   */
  setupColorBlindnessSupport() {
    this.colorFilters.set('protanopia', 'url(#protanopia-filter)');
    this.colorFilters.set('deuteranopia', 'url(#deuteranopia-filter)');
    this.colorFilters.set('tritanopia', 'url(#tritanopia-filter)');
    this.colorFilters.set('achromatopsia', 'url(#achromatopsia-filter)');
    
    this.createColorBlindnessFilters();
    
    if (this.preferences.colorBlindness !== 'none') {
      this.applyColorBlindnessFilter(this.preferences.colorBlindness);
    }
  }

  /**
   * Create SVG filters for color blindness simulation
   */
  createColorBlindnessFilters() {
    if (document.getElementById('colorblind-filters')) return;
    
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.id = 'colorblind-filters';
    svg.style.cssText = 'position: absolute; width: 0; height: 0; pointer-events: none;';
    
    svg.innerHTML = `
      <defs>
        <!-- Protanopia (red-blind) -->
        <filter id="protanopia-filter">
          <feColorMatrix type="matrix" values="0.567 0.433 0 0 0
                                               0.558 0.442 0 0 0
                                               0 0.242 0.758 0 0
                                               0 0 0 1 0"/>
        </filter>
        
        <!-- Deuteranopia (green-blind) -->
        <filter id="deuteranopia-filter">
          <feColorMatrix type="matrix" values="0.625 0.375 0 0 0
                                               0.7 0.3 0 0 0
                                               0 0.3 0.7 0 0
                                               0 0 0 1 0"/>
        </filter>
        
        <!-- Tritanopia (blue-blind) -->
        <filter id="tritanopia-filter">
          <feColorMatrix type="matrix" values="0.95 0.05 0 0 0
                                               0 0.433 0.567 0 0
                                               0 0.475 0.525 0 0
                                               0 0 0 1 0"/>
        </filter>
        
        <!-- Achromatopsia (complete color blindness) -->
        <filter id="achromatopsia-filter">
          <feColorMatrix type="matrix" values="0.299 0.587 0.114 0 0
                                               0.299 0.587 0.114 0 0
                                               0.299 0.587 0.114 0 0
                                               0 0 0 1 0"/>
        </filter>
      </defs>
    `;
    
    document.body.appendChild(svg);
  }

  /**
   * Apply color blindness filter
   */
  applyColorBlindnessFilter(type) {
    const filter = this.colorFilters.get(type);
    
    if (filter) {
      document.documentElement.style.filter = filter;
      this.preferences.colorBlindness = type;
      this.announceChange(`Color blindness filter applied: ${type}`);
    } else {
      document.documentElement.style.filter = 'none';
      this.preferences.colorBlindness = 'none';
      this.announceChange('Color blindness filter removed');
    }
  }

  /**
   * Setup dyslexia-friendly mode
   */
  setupDyslexiaMode() {
    if (this.preferences.dyslexiaMode) {
      this.enableDyslexiaMode();
    }
  }

  /**
   * Enable dyslexia-friendly mode
   */
  enableDyslexiaMode() {
    document.documentElement.classList.add('dyslexia-friendly');
    
    if (!document.getElementById('dyslexia-styles')) {
      const style = document.createElement('style');
      style.id = 'dyslexia-styles';
      style.textContent = `
        .dyslexia-friendly {
          --dyslexia-font: 'OpenDyslexic', 'Comic Sans MS', cursive;
          --letter-spacing: 0.12em;
          --word-spacing: 0.16em;
          --line-height: 1.8;
        }
        
        .dyslexia-friendly * {
          font-family: var(--dyslexia-font) !important;
          letter-spacing: var(--letter-spacing) !important;
          word-spacing: var(--word-spacing) !important;
          line-height: var(--line-height) !important;
        }
        
        .dyslexia-friendly p,
        .dyslexia-friendly li {
          margin-bottom: 1.5em !important;
        }
        
        .dyslexia-friendly h1,
        .dyslexia-friendly h2,
        .dyslexia-friendly h3,
        .dyslexia-friendly h4,
        .dyslexia-friendly h5,
        .dyslexia-friendly h6 {
          margin-bottom: 1em !important;
          font-weight: bold !important;
        }
        
        .dyslexia-friendly a {
          text-decoration: underline !important;
          text-decoration-thickness: 2px !important;
        }
        
        .dyslexia-friendly .text-justify {
          text-align: left !important;
        }
      `;
      document.head.appendChild(style);
    }
    
    // Load OpenDyslexic font if available
    this.loadDyslexiaFont();
    
    this.announceChange('Dyslexia-friendly mode enabled');
  }

  /**
   * Load dyslexia-friendly font
   */
  loadDyslexiaFont() {
    if (!document.querySelector('link[href*="opendyslexic"]')) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = 'https://fonts.googleapis.com/css2?family=OpenDyslexic:wght@400;700&display=swap';
      document.head.appendChild(link);
    }
  }

  /**
   * Setup voice commands
   */
  async setupVoiceCommands() {
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      this.voiceRecognition = new SpeechRecognition();
      
      this.voiceRecognition.continuous = true;
      this.voiceRecognition.interimResults = false;
      this.voiceRecognition.lang = 'en-US';
      
      this.voiceRecognition.onresult = (event) => {
        this.handleVoiceCommand(event);
      };
      
      this.voiceRecognition.onerror = (event) => {
        console.warn('Voice recognition error:', event.error);
      };
      
      // Setup speech synthesis
      if ('speechSynthesis' in window) {
        this.speechSynthesis = window.speechSynthesis;
      }
      
      if (this.preferences.voiceCommands) {
        this.enableVoiceCommands();
      }
    }
  }

  /**
   * Enable voice commands
   */
  enableVoiceCommands() {
    if (!this.voiceRecognition) return;
    
    try {
      this.voiceRecognition.start();
      this.preferences.voiceCommands = true;
      this.announceChange('Voice commands enabled. Say "help" for available commands.');
    } catch (error) {
      console.warn('Failed to start voice recognition:', error);
    }
  }

  /**
   * Disable voice commands
   */
  disableVoiceCommands() {
    if (this.voiceRecognition) {
      this.voiceRecognition.stop();
      this.preferences.voiceCommands = false;
      this.announceChange('Voice commands disabled');
    }
  }

  /**
   * Handle voice command
   */
  handleVoiceCommand(event) {
    const command = event.results[event.results.length - 1][0].transcript.toLowerCase().trim();
    
    console.log('Voice command:', command);
    
    // Navigation commands
    if (command.includes('go to main') || command.includes('main content')) {
      this.focusElement(document.querySelector('main, [role="main"]'));
      this.speak('Navigated to main content');
    } else if (command.includes('go to navigation') || command.includes('menu')) {
      this.focusElement(document.querySelector('nav, [role="navigation"]'));
      this.speak('Navigated to navigation');
    } else if (command.includes('search')) {
      this.focusElement(document.querySelector('input[type="search"], .search-input'));
      this.speak('Focused search');
    }
    
    // Accessibility commands
    else if (command.includes('high contrast')) {
      this.toggleHighContrast();
    } else if (command.includes('reduce motion') || command.includes('reduced motion')) {
      this.toggleReducedMotion();
    } else if (command.includes('large text') || command.includes('bigger text')) {
      this.setTextSize('large');
    } else if (command.includes('normal text')) {
      this.setTextSize('normal');
    }
    
    // Reading commands
    else if (command.includes('read page') || command.includes('read this')) {
      this.readPageContent();
    } else if (command.includes('stop reading')) {
      this.stopSpeaking();
    }
    
    // Help command
    else if (command.includes('help') || command.includes('commands')) {
      this.speakVoiceHelp();
    }
    
    // Unknown command
    else {
      this.speak('Command not recognized. Say "help" for available commands.');
    }
  }

  /**
   * Speak text using speech synthesis
   */
  speak(text, options = {}) {
    if (!this.speechSynthesis) return;
    
    // Stop current speech
    this.speechSynthesis.cancel();
    
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = options.rate || 1;
    utterance.pitch = options.pitch || 1;
    utterance.volume = options.volume || 1;
    
    this.speechSynthesis.speak(utterance);
  }

  /**
   * Stop speaking
   */
  stopSpeaking() {
    if (this.speechSynthesis) {
      this.speechSynthesis.cancel();
    }
  }

  /**
   * Read page content
   */
  readPageContent() {
    const main = document.querySelector('main, [role="main"]') || document.body;
    const textContent = this.extractReadableText(main);
    
    if (textContent) {
      this.speak(textContent);
    } else {
      this.speak('No readable content found on this page');
    }
  }

  /**
   * Extract readable text from element
   */
  extractReadableText(element) {
    const walker = document.createTreeWalker(
      element,
      NodeFilter.SHOW_TEXT,
      {
        acceptNode: (node) => {
          const parent = node.parentElement;
          if (!parent) return NodeFilter.FILTER_REJECT;
          
          // Skip hidden elements
          const style = window.getComputedStyle(parent);
          if (style.display === 'none' || style.visibility === 'hidden') {
            return NodeFilter.FILTER_REJECT;
          }
          
          // Skip script and style elements
          if (['SCRIPT', 'STYLE', 'NOSCRIPT'].includes(parent.tagName)) {
            return NodeFilter.FILTER_REJECT;
          }
          
          return NodeFilter.FILTER_ACCEPT;
        }
      }
    );
    
    const textNodes = [];
    let node;
    
    while (node = walker.nextNode()) {
      const text = node.textContent.trim();
      if (text) {
        textNodes.push(text);
      }
    }
    
    return textNodes.join(' ').replace(/\s+/g, ' ').trim();
  }

  /**
   * Speak voice help
   */
  speakVoiceHelp() {
    const helpText = `
      Available voice commands:
      Navigation: "go to main", "go to navigation", "search"
      Accessibility: "high contrast", "reduce motion", "large text", "normal text"
      Reading: "read page", "stop reading"
      Say "help" to repeat this message.
    `;
    
    this.speak(helpText);
  }

  /**
   * Setup screen reader enhancements
   */
  setupScreenReaderEnhancements() {
    // Detect screen reader usage
    this.detectScreenReader();
    
    // Enhance ARIA live regions
    this.enhanceAriaLiveRegions();
    
    // Add reading landmarks
    this.addReadingLandmarks();
  }

  /**
   * Detect screen reader usage
   */
  detectScreenReader() {
    // Check for common screen reader indicators
    const indicators = [
      navigator.userAgent.includes('NVDA'),
      navigator.userAgent.includes('JAWS'),
      navigator.userAgent.includes('VoiceOver'),
      window.speechSynthesis && window.speechSynthesis.getVoices().length > 0,
      document.querySelector('[aria-live]') !== null
    ];
    
    if (indicators.some(Boolean)) {
      this.preferences.screenReader = true;
      this.enableScreenReaderMode();
    }
  }

  /**
   * Enable screen reader mode
   */
  enableScreenReaderMode() {
    document.documentElement.classList.add('screen-reader-mode');
    
    // Add screen reader specific styles
    if (!document.getElementById('screen-reader-styles')) {
      const style = document.createElement('style');
      style.id = 'screen-reader-styles';
      style.textContent = `
        .screen-reader-mode .sr-only {
          position: static !important;
          width: auto !important;
          height: auto !important;
          padding: 0 !important;
          margin: 0 !important;
          overflow: visible !important;
          clip: auto !important;
          white-space: normal !important;
          border: 0 !important;
        }
        
        .screen-reader-mode .decorative {
          display: none !important;
        }
        
        .screen-reader-mode img:not([alt]) {
          display: none !important;
        }
      `;
      document.head.appendChild(style);
    }
  }

  /**
   * Setup focus assist
   */
  setupFocusAssist() {
    if (this.preferences.focusAssist) {
      this.enableFocusAssist();
    }
  }

  /**
   * Enable focus assist
   */
  enableFocusAssist() {
    document.documentElement.classList.add('focus-assist');
    
    if (!document.getElementById('focus-assist-styles')) {
      const style = document.createElement('style');
      style.id = 'focus-assist-styles';
      style.textContent = `
        .focus-assist *:focus {
          outline: 4px solid #ff6b35 !important;
          outline-offset: 4px !important;
          box-shadow: 0 0 0 8px rgba(255, 107, 53, 0.3) !important;
          background-color: rgba(255, 255, 255, 0.9) !important;
          color: #000 !important;
          z-index: 9999 !important;
          position: relative !important;
        }
        
        .focus-assist *:focus::after {
          content: attr(aria-label) attr(title) attr(alt);
          position: absolute;
          top: 100%;
          left: 0;
          background: #000;
          color: #fff;
          padding: 4px 8px;
          font-size: 14px;
          white-space: nowrap;
          z-index: 10000;
          border-radius: 4px;
        }
      `;
      document.head.appendChild(style);
    }
    
    this.announceChange('Focus assist enabled');
  }

  /**
   * Setup accessibility control panel
   */
  setupAccessibilityPanel() {
    this.createAccessibilityPanel();
  }

  /**
   * Create accessibility control panel
   */
  createAccessibilityPanel() {
    if (document.getElementById('accessibility-panel')) return;
    
    const panel = document.createElement('div');
    panel.id = 'accessibility-panel';
    panel.className = 'accessibility-panel';
    panel.setAttribute('role', 'dialog');
    panel.setAttribute('aria-label', 'Accessibility Settings');
    panel.innerHTML = `
      <div class="accessibility-panel-header">
        <h2>Accessibility Settings</h2>
        <button class="close-btn" aria-label="Close accessibility panel">×</button>
      </div>
      <div class="accessibility-panel-content">
        <div class="setting-group">
          <h3>Visual</h3>
          <label class="setting-item">
            <input type="checkbox" id="high-contrast-toggle">
            <span>High Contrast Mode</span>
          </label>
          <label class="setting-item">
            <input type="checkbox" id="dark-mode-toggle">
            <span>Dark Mode</span>
          </label>
          <label class="setting-item">
            <span>Text Size</span>
            <select id="text-size-select">
              <option value="small">Small</option>
              <option value="normal" selected>Normal</option>
              <option value="large">Large</option>
              <option value="xlarge">Extra Large</option>
              <option value="xxlarge">XX Large</option>
            </select>
          </label>
          <label class="setting-item">
            <span>Color Blindness</span>
            <select id="colorblind-select">
              <option value="none" selected>None</option>
              <option value="protanopia">Protanopia (Red-blind)</option>
              <option value="deuteranopia">Deuteranopia (Green-blind)</option>
              <option value="tritanopia">Tritanopia (Blue-blind)</option>
              <option value="achromatopsia">Achromatopsia (No color)</option>
            </select>
          </label>
        </div>
        
        <div class="setting-group">
          <h3>Motion & Animation</h3>
          <label class="setting-item">
            <input type="checkbox" id="reduced-motion-toggle">
            <span>Reduce Motion</span>
          </label>
        </div>
        
        <div class="setting-group">
          <h3>Reading & Comprehension</h3>
          <label class="setting-item">
            <input type="checkbox" id="dyslexia-toggle">
            <span>Dyslexia-Friendly Font</span>
          </label>
          <label class="setting-item">
            <input type="checkbox" id="focus-assist-toggle">
            <span>Enhanced Focus Indicators</span>
          </label>
        </div>
        
        <div class="setting-group">
          <h3>Audio & Voice</h3>
          <label class="setting-item">
            <input type="checkbox" id="voice-commands-toggle">
            <span>Voice Commands</span>
          </label>
          <button id="read-page-btn" class="action-btn">Read This Page</button>
        </div>
        
        <div class="setting-group">
          <h3>Actions</h3>
          <button id="reset-settings-btn" class="action-btn">Reset All Settings</button>
          <button id="save-settings-btn" class="action-btn primary">Save Settings</button>
        </div>
      </div>
    `;
    
    // Add panel styles
    this.addAccessibilityPanelStyles();
    
    // Setup panel interactions
    this.setupPanelInteractions(panel);
    
    document.body.appendChild(panel);
    
    // Create toggle button
    this.createAccessibilityToggle();
  }

  /**
   * Add accessibility panel styles
   */
  addAccessibilityPanelStyles() {
    if (document.getElementById('accessibility-panel-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'accessibility-panel-styles';
    style.textContent = `
      .accessibility-panel {
        position: fixed;
        top: 20px;
        right: 20px;
        width: 350px;
        max-width: calc(100vw - 40px);
        background: #fff;
        border: 2px solid #333;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        display: none;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
      
      .accessibility-panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border-bottom: 1px solid #ddd;
        background: #f5f5f5;
      }
      
      .accessibility-panel-header h2 {
        margin: 0;
        font-size: 18px;
        color: #333;
      }
      
      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        padding: 4px;
        color: #666;
      }
      
      .accessibility-panel-content {
        padding: 16px;
        max-height: 60vh;
        overflow-y: auto;
      }
      
      .setting-group {
        margin-bottom: 20px;
      }
      
      .setting-group h3 {
        margin: 0 0 12px 0;
        font-size: 16px;
        color: #333;
        border-bottom: 1px solid #eee;
        padding-bottom: 4px;
      }
      
      .setting-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        cursor: pointer;
      }
      
      .setting-item input,
      .setting-item select {
        margin-left: 8px;
      }
      
      .action-btn {
        width: 100%;
        padding: 10px;
        margin: 4px 0;
        border: 1px solid #ddd;
        border-radius: 4px;
        background: #f9f9f9;
        cursor: pointer;
        font-size: 14px;
      }
      
      .action-btn:hover {
        background: #e9e9e9;
      }
      
      .action-btn.primary {
        background: #007cba;
        color: white;
        border-color: #007cba;
      }
      
      .action-btn.primary:hover {
        background: #005a87;
      }
      
      .accessibility-toggle {
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background: #007cba;
        color: white;
        border: none;
        cursor: pointer;
        font-size: 24px;
        z-index: 9999;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      }
      
      .accessibility-toggle:hover {
        background: #005a87;
      }
      
      @media (max-width: 768px) {
        .accessibility-panel {
          top: 10px;
          right: 10px;
          left: 10px;
          width: auto;
        }
      }
    `;
    
    document.head.appendChild(style);
  }

  /**
   * Setup panel interactions
   */
  setupPanelInteractions(panel) {
    // Close button
    panel.querySelector('.close-btn').addEventListener('click', () => {
      this.hideAccessibilityPanel();
    });
    
    // Setting toggles
    panel.querySelector('#high-contrast-toggle').addEventListener('change', (e) => {
      this.setHighContrast(e.target.checked);
    });
    
    panel.querySelector('#dark-mode-toggle').addEventListener('change', (e) => {
      this.setDarkMode(e.target.checked);
    });
    
    panel.querySelector('#reduced-motion-toggle').addEventListener('change', (e) => {
      this.setReducedMotion(e.target.checked);
    });
    
    panel.querySelector('#dyslexia-toggle').addEventListener('change', (e) => {
      this.setDyslexiaMode(e.target.checked);
    });
    
    panel.querySelector('#focus-assist-toggle').addEventListener('change', (e) => {
      this.setFocusAssist(e.target.checked);
    });
    
    panel.querySelector('#voice-commands-toggle').addEventListener('change', (e) => {
      this.setVoiceCommands(e.target.checked);
    });
    
    // Selects
    panel.querySelector('#text-size-select').addEventListener('change', (e) => {
      this.setTextSize(e.target.value);
    });
    
    panel.querySelector('#colorblind-select').addEventListener('change', (e) => {
      this.applyColorBlindnessFilter(e.target.value);
    });
    
    // Action buttons
    panel.querySelector('#read-page-btn').addEventListener('click', () => {
      this.readPageContent();
    });
    
    panel.querySelector('#reset-settings-btn').addEventListener('click', () => {
      this.resetAllSettings();
    });
    
    panel.querySelector('#save-settings-btn').addEventListener('click', () => {
      this.saveUserPreferences();
      this.announceChange('Settings saved');
    });
    
    // Update panel with current settings
    this.updatePanelSettings();
  }

  /**
   * Create accessibility toggle button
   */
  createAccessibilityToggle() {
    if (document.getElementById('accessibility-toggle')) return;

    const toggle = document.createElement('button');
    toggle.id = 'accessibility-toggle';
    toggle.className = 'accessibility-toggle';
    toggle.innerHTML = '♿';
    toggle.setAttribute('aria-label', 'Open accessibility settings');

    // Hide the accessibility toggle button
    toggle.style.display = 'none';

    toggle.addEventListener('click', () => {
      this.toggleAccessibilityPanel();
    });

    document.body.appendChild(toggle);
  }

  /**
   * Toggle accessibility panel
   */
  toggleAccessibilityPanel() {
    const panel = document.getElementById('accessibility-panel');
    if (panel.style.display === 'none' || !panel.style.display) {
      this.showAccessibilityPanel();
    } else {
      this.hideAccessibilityPanel();
    }
  }

  /**
   * Show accessibility panel
   */
  showAccessibilityPanel() {
    const panel = document.getElementById('accessibility-panel');
    panel.style.display = 'block';
    
    // Focus first interactive element
    const firstInput = panel.querySelector('input, select, button');
    if (firstInput) {
      firstInput.focus();
    }
    
    this.announceChange('Accessibility settings panel opened');
  }

  /**
   * Hide accessibility panel
   */
  hideAccessibilityPanel() {
    const panel = document.getElementById('accessibility-panel');
    panel.style.display = 'none';
    
    // Return focus to toggle button
    document.getElementById('accessibility-toggle').focus();
    
    this.announceChange('Accessibility settings panel closed');
  }

  /**
   * Update panel with current settings
   */
  updatePanelSettings() {
    const panel = document.getElementById('accessibility-panel');
    if (!panel) return;
    
    panel.querySelector('#high-contrast-toggle').checked = this.preferences.highContrast;
    panel.querySelector('#dark-mode-toggle').checked = this.preferences.darkMode;
    panel.querySelector('#reduced-motion-toggle').checked = this.preferences.reducedMotion;
    panel.querySelector('#dyslexia-toggle').checked = this.preferences.dyslexiaMode;
    panel.querySelector('#focus-assist-toggle').checked = this.preferences.focusAssist;
    panel.querySelector('#voice-commands-toggle').checked = this.preferences.voiceCommands;
    panel.querySelector('#colorblind-select').value = this.preferences.colorBlindness;
  }

  /**
   * Setup preference synchronization across tabs
   */
  setupPreferenceSync() {
    window.addEventListener('accessibility-preferences-changed', (event) => {
      this.preferences = { ...this.preferences, ...event.detail };
      this.applyAllPreferences();
    });
    
    // Listen for storage changes
    window.addEventListener('storage', (event) => {
      if (event.key === 'accessibility_preferences') {
        this.loadUserPreferences();
        this.applyAllPreferences();
      }
    });
  }

  /**
   * Apply all current preferences
   */
  applyAllPreferences() {
    if (this.preferences.highContrast) this.enableHighContrast();
    else this.disableHighContrast();
    
    if (this.preferences.reducedMotion) this.enableReducedMotion();
    else this.disableReducedMotion();
    
    if (this.preferences.dyslexiaMode) this.enableDyslexiaMode();
    else this.disableDyslexiaMode();
    
    if (this.preferences.focusAssist) this.enableFocusAssist();
    else this.disableFocusAssist();
    
    if (this.preferences.voiceCommands) this.enableVoiceCommands();
    else this.disableVoiceCommands();
    
    this.applyColorBlindnessFilter(this.preferences.colorBlindness);
    
    this.updatePanelSettings();
  }

  /**
   * Monitor accessibility changes
   */
  monitorAccessibilityChanges() {
    // Monitor for new content that needs accessibility enhancements
    if ('MutationObserver' in window) {
      const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
          mutation.addedNodes.forEach(node => {
            if (node.nodeType === 1) {
              this.enhanceNewContent(node);
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
   * Enhance new content with accessibility features
   */
  enhanceNewContent(element) {
    // Apply current preferences to new content
    if (this.preferences.highContrast) {
      element.classList?.add('high-contrast');
    }
    
    if (this.preferences.dyslexiaMode) {
      element.classList?.add('dyslexia-friendly');
    }
    
    // Add focus indicators to new interactive elements
    const interactiveElements = element.querySelectorAll?.('button, a, input, select, textarea') || [];
    interactiveElements.forEach(el => {
      if (this.preferences.focusAssist) {
        el.classList.add('focus-assist');
      }
    });
  }

  /**
   * Utility methods for setting preferences
   */
  
  setHighContrast(enabled) {
    this.preferences.highContrast = enabled;
    if (enabled) this.enableHighContrast();
    else this.disableHighContrast();
    this.saveUserPreferences();
  }
  
  setReducedMotion(enabled) {
    this.preferences.reducedMotion = enabled;
    if (enabled) this.enableReducedMotion();
    else this.disableReducedMotion();
    this.saveUserPreferences();
  }
  
  setDarkMode(enabled) {
    this.preferences.darkMode = enabled;
    document.documentElement.classList.toggle('dark-mode', enabled);
    this.announceChange(`Dark mode ${enabled ? 'enabled' : 'disabled'}`);
    this.saveUserPreferences();
  }
  
  setDyslexiaMode(enabled) {
    this.preferences.dyslexiaMode = enabled;
    if (enabled) this.enableDyslexiaMode();
    else this.disableDyslexiaMode();
    this.saveUserPreferences();
  }
  
  setFocusAssist(enabled) {
    this.preferences.focusAssist = enabled;
    if (enabled) this.enableFocusAssist();
    else this.disableFocusAssist();
    this.saveUserPreferences();
  }
  
  setVoiceCommands(enabled) {
    this.preferences.voiceCommands = enabled;
    if (enabled) this.enableVoiceCommands();
    else this.disableVoiceCommands();
    this.saveUserPreferences();
  }
  
  disableDyslexiaMode() {
    document.documentElement.classList.remove('dyslexia-friendly');
    this.announceChange('Dyslexia-friendly mode disabled');
  }
  
  disableFocusAssist() {
    document.documentElement.classList.remove('focus-assist');
    this.announceChange('Focus assist disabled');
  }
  
  toggleHighContrast() {
    this.setHighContrast(!this.preferences.highContrast);
  }
  
  toggleReducedMotion() {
    this.setReducedMotion(!this.preferences.reducedMotion);
  }
  
  resetAllSettings() {
    this.preferences = {
      highContrast: false,
      reducedMotion: false,
      largeText: false,
      darkMode: false,
      voiceCommands: false,
      screenReader: false,
      colorBlindness: 'none',
      dyslexiaMode: false,
      focusAssist: false
    };
    
    this.applyAllPreferences();
    this.saveUserPreferences();
    this.announceChange('All accessibility settings reset to default');
  }
  
  focusElement(element) {
    if (element && element.focus) {
      element.focus();
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }
  
  announceChange(message) {
    // Announce to screen readers
    const announcer = document.getElementById('a11y-announcer') || 
                     document.querySelector('[aria-live]');
    
    if (announcer) {
      announcer.textContent = message;
      setTimeout(() => announcer.textContent = '', 1000);
    }
    
    console.log('♿ Accessibility:', message);
  }

  /**
   * Get current accessibility status
   */
  getStatus() {
    return {
      preferences: this.preferences,
      voiceCommandsAvailable: !!this.voiceRecognition,
      speechSynthesisAvailable: !!this.speechSynthesis,
      panelVisible: document.getElementById('accessibility-panel')?.style.display !== 'none'
    };
  }
}

// Initialize Advanced Accessibility Features
const advancedAccessibilityFeatures = new AdvancedAccessibilityFeatures();

// Export for global access
window.AdvancedAccessibilityFeatures = AdvancedAccessibilityFeatures;
window.advancedAccessibilityFeatures = advancedAccessibilityFeatures;

console.log('✅ Advanced Accessibility Features initialized');