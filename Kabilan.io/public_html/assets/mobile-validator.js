/**
 * Mobile Responsive Design Validator
 * Automatically checks for common mobile layout issues
 */

class MobileValidator {
  constructor() {
    this.issues = [];
    this.warnings = [];
    this.passed = [];
  }

  // Check for horizontal scrolling
  checkHorizontalScroll() {
    const bodyWidth = document.body.scrollWidth;
    const windowWidth = window.innerWidth;
    
    if (bodyWidth > windowWidth) {
      this.issues.push({
        type: 'horizontal-scroll',
        message: `Horizontal scroll detected: body width (${bodyWidth}px) > viewport width (${windowWidth}px)`,
        severity: 'high'
      });
    } else {
      this.passed.push('No horizontal scrolling detected');
    }
  }

  // Check button touch targets
  checkTouchTargets() {
    const buttons = document.querySelectorAll('button, a[role="button"], input[type="submit"], input[type="button"]');
    let smallButtons = 0;

    buttons.forEach((button, index) => {
      const rect = button.getBoundingClientRect();
      const minSize = 44; // WCAG 2.1 AA minimum

      if (rect.width < minSize || rect.height < minSize) {
        smallButtons++;
        this.issues.push({
          type: 'touch-target',
          message: `Button ${index + 1} too small: ${Math.round(rect.width)}x${Math.round(rect.height)}px (minimum: ${minSize}x${minSize}px)`,
          element: button,
          severity: 'medium'
        });
      }
    });

    if (smallButtons === 0) {
      this.passed.push(`All ${buttons.length} buttons have adequate touch targets`);
    }
  }

  // Check for overlapping elements
  checkOverlappingElements() {
    const fixedElements = document.querySelectorAll('[style*="position: fixed"], .fixed');
    const overlaps = [];

    for (let i = 0; i < fixedElements.length; i++) {
      for (let j = i + 1; j < fixedElements.length; j++) {
        const rect1 = fixedElements[i].getBoundingClientRect();
        const rect2 = fixedElements[j].getBoundingClientRect();

        if (this.isOverlapping(rect1, rect2)) {
          overlaps.push({
            element1: fixedElements[i],
            element2: fixedElements[j],
            rect1, rect2
          });
        }
      }
    }

    if (overlaps.length > 0) {
      overlaps.forEach((overlap, index) => {
        this.issues.push({
          type: 'overlap',
          message: `Fixed elements overlapping: ${overlap.element1.id || 'element'} and ${overlap.element2.id || 'element'}`,
          severity: 'high'
        });
      });
    } else {
      this.passed.push('No overlapping fixed elements detected');
    }
  }

  // Check text overflow
  checkTextOverflow() {
    const textElements = document.querySelectorAll('p, h1, h2, h3, h4, h5, h6, span, div');
    let overflowCount = 0;

    textElements.forEach((element) => {
      if (element.scrollWidth > element.clientWidth) {
        overflowCount++;
        this.warnings.push({
          type: 'text-overflow',
          message: `Text overflow detected in ${element.tagName.toLowerCase()}`,
          element: element,
          severity: 'low'
        });
      }
    });

    if (overflowCount === 0) {
      this.passed.push('No text overflow detected');
    } else {
      this.warnings.push({
        type: 'text-overflow-summary',
        message: `${overflowCount} elements with potential text overflow`,
        severity: 'low'
      });
    }
  }

  // Check form usability
  checkFormUsability() {
    const inputs = document.querySelectorAll('input, textarea, select');
    let smallInputs = 0;

    inputs.forEach((input, index) => {
      const rect = input.getBoundingClientRect();
      const computedStyle = window.getComputedStyle(input);
      const fontSize = parseFloat(computedStyle.fontSize);

      // Check input size
      if (rect.height < 44) {
        smallInputs++;
        this.issues.push({
          type: 'input-size',
          message: `Input ${index + 1} too small: ${Math.round(rect.height)}px height (minimum: 44px)`,
          element: input,
          severity: 'medium'
        });
      }

      // Check font size (iOS zoom prevention)
      if (fontSize < 16) {
        this.warnings.push({
          type: 'input-font',
          message: `Input ${index + 1} font size ${fontSize}px may cause zoom on iOS (recommended: 16px+)`,
          element: input,
          severity: 'low'
        });
      }
    });

    if (smallInputs === 0) {
      this.passed.push(`All ${inputs.length} form inputs have adequate size`);
    }
  }

  // Check viewport meta tag
  checkViewportMeta() {
    const viewportMeta = document.querySelector('meta[name="viewport"]');
    
    if (!viewportMeta) {
      this.issues.push({
        type: 'viewport-meta',
        message: 'Missing viewport meta tag',
        severity: 'high'
      });
    } else {
      const content = viewportMeta.getAttribute('content');
      if (!content.includes('width=device-width')) {
        this.issues.push({
          type: 'viewport-meta',
          message: 'Viewport meta tag missing width=device-width',
          severity: 'medium'
        });
      } else {
        this.passed.push('Viewport meta tag properly configured');
      }
    }
  }

  // Helper function to check if two rectangles overlap
  isOverlapping(rect1, rect2) {
    return !(rect1.right < rect2.left || 
             rect2.right < rect1.left || 
             rect1.bottom < rect2.top || 
             rect2.bottom < rect1.top);
  }

  // Run all checks
  runAllChecks() {
    console.log('🔍 Running Mobile Responsive Design Validation...');
    
    this.checkHorizontalScroll();
    this.checkTouchTargets();
    this.checkOverlappingElements();
    this.checkTextOverflow();
    this.checkFormUsability();
    this.checkViewportMeta();

    this.generateReport();
  }

  // Generate validation report
  generateReport() {
    console.log('\n📱 Mobile Validation Report');
    console.log('=' .repeat(50));

    // Passed checks
    if (this.passed.length > 0) {
      console.log('\n✅ Passed Checks:');
      this.passed.forEach(check => console.log(`  ✓ ${check}`));
    }

    // Warnings
    if (this.warnings.length > 0) {
      console.log('\n⚠️  Warnings:');
      this.warnings.forEach(warning => {
        console.log(`  ⚠️  ${warning.message}`);
      });
    }

    // Issues
    if (this.issues.length > 0) {
      console.log('\n❌ Issues Found:');
      this.issues.forEach(issue => {
        const severity = issue.severity === 'high' ? '🔴' : 
                        issue.severity === 'medium' ? '🟡' : '🟠';
        console.log(`  ${severity} ${issue.message}`);
      });
    }

    // Summary
    console.log('\n📊 Summary:');
    console.log(`  Passed: ${this.passed.length}`);
    console.log(`  Warnings: ${this.warnings.length}`);
    console.log(`  Issues: ${this.issues.length}`);

    if (this.issues.length === 0) {
      console.log('\n🎉 All critical mobile responsive checks passed!');
    } else {
      console.log('\n🔧 Please address the issues above for optimal mobile experience.');
    }

    return {
      passed: this.passed.length,
      warnings: this.warnings.length,
      issues: this.issues.length,
      details: {
        passed: this.passed,
        warnings: this.warnings,
        issues: this.issues
      }
    };
  }
}

// Auto-run validation when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
      const validator = new MobileValidator();
      validator.runAllChecks();
    }, 1000); // Wait for layout to settle
  });
} else {
  setTimeout(() => {
    const validator = new MobileValidator();
    validator.runAllChecks();
  }, 1000);
}

// Export for manual use
window.MobileValidator = MobileValidator;

// Add manual validation command
window.validateMobile = () => {
  const validator = new MobileValidator();
  return validator.runAllChecks();
};

console.log('📱 Mobile Validator loaded. Run validateMobile() to check manually.');
