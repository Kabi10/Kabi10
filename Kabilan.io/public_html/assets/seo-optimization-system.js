/**
 * SEO and Structured Data Optimization System
 * Comprehensive SEO implementation with structured data and monitoring
 */

class SEOOptimizationSystem {
  constructor() {
    this.structuredData = new Map();
    this.metaTags = new Map();
    this.seoIssues = [];
    this.init();
  }

  async init() {
    this.implementStructuredData();
    this.optimizeMetaTags();
    this.setupSocialMediaTags();
    this.createSitemap();
    this.setupSEOMonitoring();
    this.implementBreadcrumbs();
  }

  implementStructuredData() {
    // Person/Professional schema
    const personSchema = {
      "@context": "https://schema.org",
      "@type": "Person",
      "name": "Kabi Tharma",
      "jobTitle": "Full Stack Developer & UI/UX Designer",
      "url": "https://kabilan.io",
      "sameAs": [
        "https://linkedin.com/in/kabitharma",
        "https://github.com/kabitharma",
        "https://twitter.com/kabitharma"
      ],
      "knowsAbout": [
        "React", "Node.js", "JavaScript", "TypeScript", "UI/UX Design"
      ],
      "worksFor": {
        "@type": "Organization",
        "name": "Freelance"
      }
    };

    // Website schema
    const websiteSchema = {
      "@context": "https://schema.org",
      "@type": "WebSite",
      "name": "Kabi Tharma Portfolio",
      "url": "https://kabilan.io",
      "potentialAction": {
        "@type": "SearchAction",
        "target": "https://kabilan.io/search?q={search_term_string}",
        "query-input": "required name=search_term_string"
      }
    };

    // Portfolio schema
    const portfolioSchema = {
      "@context": "https://schema.org",
      "@type": "CreativeWork",
      "name": "Portfolio Projects",
      "creator": {
        "@type": "Person",
        "name": "Kabi Tharma"
      },
      "about": "Full Stack Development and UI/UX Design Projects"
    };

    this.addStructuredData('person', personSchema);
    this.addStructuredData('website', websiteSchema);
    this.addStructuredData('portfolio', portfolioSchema);
  }

  addStructuredData(type, schema) {
    const script = document.createElement('script');
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify(schema);
    document.head.appendChild(script);
    this.structuredData.set(type, schema);
  }

  optimizeMetaTags() {
    const metaTags = {
      'description': 'Experienced Full Stack Developer and UI/UX Designer specializing in React, Node.js, and modern web technologies. View my portfolio of innovative projects.',
      'keywords': 'Full Stack Developer, UI/UX Designer, React, Node.js, JavaScript, Portfolio, Web Development',
      'author': 'Kabi Tharma',
      'robots': 'index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1',
      'googlebot': 'index, follow',
      'viewport': 'width=device-width, initial-scale=1.0',
      'theme-color': '#010103'
    };

    Object.entries(metaTags).forEach(([name, content]) => {
      this.setMetaTag(name, content);
    });
  }

  setMetaTag(name, content) {
    let meta = document.querySelector(`meta[name="${name}"]`);
    if (!meta) {
      meta = document.createElement('meta');
      meta.name = name;
      document.head.appendChild(meta);
    }
    meta.content = content;
    this.metaTags.set(name, content);
  }

  setupSocialMediaTags() {
    // Open Graph tags
    const ogTags = {
      'og:title': 'Kabi Tharma - Full Stack Developer & UI/UX Designer',
      'og:description': 'Experienced Full Stack Developer and UI/UX Designer specializing in React, Node.js, and modern web technologies.',
      'og:image': 'https://kabilan.io/assets/og-image.jpg',
      'og:url': 'https://kabilan.io/',
      'og:type': 'website',
      'og:site_name': 'Kabi Tharma Portfolio'
    };

    // Twitter Card tags
    const twitterTags = {
      'twitter:card': 'summary_large_image',
      'twitter:title': 'Kabi Tharma - Full Stack Developer & UI/UX Designer',
      'twitter:description': 'Experienced Full Stack Developer and UI/UX Designer specializing in React, Node.js, and modern web technologies.',
      'twitter:image': 'https://kabilan.io/assets/og-image.jpg',
      'twitter:creator': '@kabitharma'
    };

    [...Object.entries(ogTags), ...Object.entries(twitterTags)].forEach(([property, content]) => {
      this.setSocialMetaTag(property, content);
    });
  }

  setSocialMetaTag(property, content) {
    let meta = document.querySelector(`meta[property="${property}"], meta[name="${property}"]`);
    if (!meta) {
      meta = document.createElement('meta');
      if (property.startsWith('og:')) {
        meta.setAttribute('property', property);
      } else {
        meta.setAttribute('name', property);
      }
      document.head.appendChild(meta);
    }
    meta.content = content;
  }

  createSitemap() {
    const pages = [
      { url: '/', priority: 1.0, changefreq: 'weekly' },
      { url: '/#about', priority: 0.8, changefreq: 'monthly' },
      { url: '/#portfolio', priority: 0.9, changefreq: 'weekly' },
      { url: '/#contact', priority: 0.7, changefreq: 'monthly' }
    ];

    const sitemap = this.generateSitemapXML(pages);
    console.log('🗺️ Sitemap generated:', sitemap);
  }

  generateSitemapXML(pages) {
    const baseUrl = 'https://kabilan.io';
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${pages.map(page => `  <url>
    <loc>${baseUrl}${page.url}</loc>
    <changefreq>${page.changefreq}</changefreq>
    <priority>${page.priority}</priority>
    <lastmod>${new Date().toISOString().split('T')[0]}</lastmod>
  </url>`).join('\n')}
</urlset>`;
    return xml;
  }

  setupSEOMonitoring() {
    // Check for SEO issues
    this.auditSEO();
    
    // Monitor page changes
    if ('MutationObserver' in window) {
      const observer = new MutationObserver(() => {
        this.auditSEO();
      });
      observer.observe(document.head, { childList: true, subtree: true });
    }
  }

  auditSEO() {
    this.seoIssues = [];

    // Check title
    const title = document.title;
    if (!title || title.length < 30 || title.length > 60) {
      this.seoIssues.push('Title should be 30-60 characters');
    }

    // Check meta description
    const description = document.querySelector('meta[name="description"]');
    if (!description || description.content.length < 120 || description.content.length > 160) {
      this.seoIssues.push('Meta description should be 120-160 characters');
    }

    // Check headings
    const h1s = document.querySelectorAll('h1');
    if (h1s.length === 0) {
      this.seoIssues.push('Missing H1 tag');
    } else if (h1s.length > 1) {
      this.seoIssues.push('Multiple H1 tags found');
    }

    // Check images
    const images = document.querySelectorAll('img:not([alt])');
    if (images.length > 0) {
      this.seoIssues.push(`${images.length} images missing alt text`);
    }

    if (this.seoIssues.length > 0) {
      console.warn('🚨 SEO Issues:', this.seoIssues);
    } else {
      console.log('✅ SEO audit passed');
    }
  }

  implementBreadcrumbs() {
    const breadcrumbSchema = {
      "@context": "https://schema.org",
      "@type": "BreadcrumbList",
      "itemListElement": [
        {
          "@type": "ListItem",
          "position": 1,
          "name": "Home",
          "item": "https://kabilan.io"
        },
        {
          "@type": "ListItem",
          "position": 2,
          "name": "Portfolio",
          "item": "https://kabilan.io/#portfolio"
        }
      ]
    };

    this.addStructuredData('breadcrumbs', breadcrumbSchema);
  }

  generateSEOReport() {
    return {
      timestamp: Date.now(),
      metaTags: Object.fromEntries(this.metaTags),
      structuredData: Object.fromEntries(this.structuredData),
      seoIssues: this.seoIssues,
      recommendations: this.generateSEORecommendations()
    };
  }

  generateSEORecommendations() {
    const recommendations = [];
    
    if (this.seoIssues.length > 0) {
      recommendations.push('Fix identified SEO issues');
    }
    
    if (!this.structuredData.has('organization')) {
      recommendations.push('Add Organization structured data');
    }
    
    if (!document.querySelector('link[rel="canonical"]')) {
      recommendations.push('Add canonical URL');
    }
    
    return recommendations;
  }
}

window.seoOptimizationSystem = new SEOOptimizationSystem();
console.log('✅ SEO Optimization System initialized');