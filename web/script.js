// ===================================
// Agrimarket Landing Page JavaScript
// ===================================

// Utility
const prefersReducedMotion = window.matchMedia(
  "(prefers-reduced-motion: reduce)",
).matches;

// ===================================
// Language Toggle Functionality
// ===================================
class LanguageManager {
  constructor() {
    this.currentLang = localStorage.getItem("agrimarket-lang") || "en";
    this.init();
  }

  init() {
    // Set initial language
    this.setLanguage(this.currentLang);

    // Add event listeners to language buttons
    document.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.addEventListener("click", (e) => {
        const lang = e.currentTarget.dataset.lang;
        this.setLanguage(lang);
      });
    });
  }

  setLanguage(lang) {
    this.currentLang = lang;
    localStorage.setItem("agrimarket-lang", lang);

    // Update document language for screen readers and fonts
    document.documentElement.setAttribute(
      "lang",
      lang === "ta" ? "ta" : lang === "si" ? "si" : "en",
    );
    document.body.setAttribute("data-lang", lang);

    // Update active button
    document.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.lang === lang);
    });

    // Update all translatable elements
    this.updateTranslations();
  }

  updateTranslations() {
    const elements = document.querySelectorAll("[data-en]");
    elements.forEach((element) => {
      const translation = element.getAttribute(`data-${this.currentLang}`);
      if (translation) {
        element.textContent = translation;
      }
    });

    // Update select options
    const selectOptions = document.querySelectorAll("option[data-en]");
    selectOptions.forEach((option) => {
      const translation = option.getAttribute(`data-${this.currentLang}`);
      if (translation) {
        option.textContent = translation;
      }
    });
  }
}

// ===================================
// Navigation Functionality
// ===================================
class NavigationManager {
  constructor() {
    this.navbar = document.getElementById("navbar");
    this.mobileMenuToggle = document.getElementById("mobileMenuToggle");
    this.navMenu = document.getElementById("navMenu");
    this.init();
  }

  init() {
    // Scroll event for navbar (passive for performance)
    window.addEventListener("scroll", () => this.handleScroll(), {
      passive: true,
    });

    // Mobile menu toggle
    if (this.mobileMenuToggle) {
      this.mobileMenuToggle.addEventListener("click", () =>
        this.toggleMobileMenu(),
      );
    }

    // Close mobile menu when clicking nav links
    document.querySelectorAll(".nav-link").forEach((link) => {
      link.addEventListener("click", () => {
        if (window.innerWidth <= 768) {
          this.closeMobileMenu();
        }
      });
    });

    // Smooth scroll for anchor links
    this.setupSmoothScroll();
  }

  handleScroll() {
    if (window.scrollY > 50) {
      this.navbar.classList.add("scrolled");
    } else {
      this.navbar.classList.remove("scrolled");
    }
  }

  toggleMobileMenu() {
    this.navMenu.classList.toggle("active");
    const icon = this.mobileMenuToggle.querySelector(".material-icons");
    icon.textContent = this.navMenu.classList.contains("active")
      ? "close"
      : "menu";
  }

  closeMobileMenu() {
    this.navMenu.classList.remove("active");
    const icon = this.mobileMenuToggle.querySelector(".material-icons");
    icon.textContent = "menu";
  }

  setupSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
      anchor.addEventListener("click", function (e) {
        const href = this.getAttribute("href");
        if (href === "#" || href === "") return;

        e.preventDefault();
        const target = document.querySelector(href);
        if (target) {
          const rect = target.getBoundingClientRect();
          const offsetTop = window.scrollY + rect.top - 80; // Account for fixed navbar
          window.scrollTo({
            top: offsetTop,
            behavior: "smooth",
          });
        }
      });
    });
  }
}

// ===================================
// Scroll to Top Button
// ===================================
class ScrollToTopManager {
  constructor() {
    this.button = document.getElementById("scrollToTop");
    this.init();
  }

  init() {
    if (!this.button) return;

    // Show/hide button based on scroll position
    window.addEventListener("scroll", () => this.handleScroll(), {
      passive: true,
    });

    // Scroll to top on click
    this.button.addEventListener("click", () => this.scrollToTop());
  }

  handleScroll() {
    if (window.scrollY > 300) {
      this.button.classList.add("visible");
    } else {
      this.button.classList.remove("visible");
    }
  }

  scrollToTop() {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }
}

// ===================================
// Workflow Tabs
// ===================================
class WorkflowTabsManager {
  constructor() {
    this.tabButtons = document.querySelectorAll(".tab-btn");
    this.init();
  }

  init() {
    this.tabButtons.forEach((btn) => {
      btn.addEventListener("click", (e) => {
        const tabName = e.currentTarget.dataset.tab;
        this.switchTab(tabName);
      });
    });
  }

  switchTab(tabName) {
    // Update active button
    this.tabButtons.forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.tab === tabName);
    });

    // Update active panel
    document.querySelectorAll(".workflow-panel").forEach((panel) => {
      panel.classList.toggle("active", panel.id === `${tabName}-workflow`);
    });
  }
}

// ===================================
// Intersection Observer for Animations
// ===================================
class AnimationManager {
  constructor() {
    this.init();
  }

  init() {
    const observerOptions = {
      threshold: 0.1,
      rootMargin: "0px 0px -50px 0px",
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("animate-in");
        }
      });
    }, observerOptions);

    // Observe elements that should animate
    const animateElements = document.querySelectorAll(
      ".feature-card, .step-card, .stat-card, .opportunity-card",
    );
    animateElements.forEach((el) => observer.observe(el));
  }
}

// ===================================
// Initialize All Managers
// ===================================
document.addEventListener("DOMContentLoaded", () => {
  // Initialize all functionality
  new LanguageManager();
  new NavigationManager();
  new ScrollToTopManager();
  new WorkflowTabsManager();
  // Optional: lightweight reveal animations are disabled by default for performance/a11y
  // new AnimationManager();
});
