// ===================================
// SL Agrimarket Landing Page JavaScript
// ===================================

const prefersReducedMotion = window.matchMedia(
  "(prefers-reduced-motion: reduce)",
).matches;

// ===================================
// Language Toggle
// ===================================
class LanguageManager {
  constructor() {
    this.currentLang = localStorage.getItem("agrimarket-lang") || "en";
    this.init();
  }

  init() {
    this.setLanguage(this.currentLang);

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

    // Update document lang for screen readers and font selection
    document.documentElement.setAttribute(
      "lang",
      lang === "ta" ? "ta" : lang === "si" ? "si" : "en",
    );
    document.body.setAttribute("data-lang", lang);

    // Update active state on all lang buttons (nav + footer)
    document.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.lang === lang);
    });

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
  }
}

// ===================================
// Navigation
// ===================================
class NavigationManager {
  constructor() {
    this.navbar = document.getElementById("navbar");
    this.mobileMenuToggle = document.getElementById("mobileMenuToggle");
    this.navMenu = document.getElementById("navMenu");
    this.init();
  }

  init() {
    window.addEventListener("scroll", () => this.handleScroll(), {
      passive: true,
    });

    if (this.mobileMenuToggle) {
      this.mobileMenuToggle.addEventListener("click", () =>
        this.toggleMobileMenu(),
      );
    }

    // Close mobile menu on nav link click
    document.querySelectorAll(".nav-link").forEach((link) => {
      link.addEventListener("click", () => {
        if (window.innerWidth <= 768) {
          this.closeMobileMenu();
        }
      });
    });

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
    const isOpen = this.navMenu.classList.toggle("active");
    this.mobileMenuToggle.classList.toggle("active", isOpen);
    this.mobileMenuToggle.setAttribute("aria-expanded", isOpen);
  }

  closeMobileMenu() {
    this.navMenu.classList.remove("active");
    this.mobileMenuToggle.classList.remove("active");
    this.mobileMenuToggle.setAttribute("aria-expanded", "false");
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
          const offsetTop = window.scrollY + rect.top - 80;
          window.scrollTo({
            top: offsetTop,
            behavior: prefersReducedMotion ? "auto" : "smooth",
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

    window.addEventListener("scroll", () => this.handleScroll(), {
      passive: true,
    });

    this.button.addEventListener("click", () => this.scrollToTop());
  }

  handleScroll() {
    if (window.scrollY > 400) {
      this.button.classList.add("visible");
    } else {
      this.button.classList.remove("visible");
    }
  }

  scrollToTop() {
    window.scrollTo({
      top: 0,
      behavior: prefersReducedMotion ? "auto" : "smooth",
    });
  }
}

// ===================================
// Intersection Observer for Animations
// ===================================
class AnimationManager {
  constructor() {
    if (prefersReducedMotion) return;
    this.init();
  }

  init() {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("animate-in");
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.15, rootMargin: "0px 0px -40px 0px" },
    );

    document
      .querySelectorAll(".feature-card, .step-card")
      .forEach((el) => observer.observe(el));
  }
}

// ===================================
// Initialize
// ===================================
document.addEventListener("DOMContentLoaded", () => {
  new LanguageManager();
  new NavigationManager();
  new ScrollToTopManager();
  new AnimationManager();
});
