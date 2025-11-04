# Placeholder Images Guide

This directory contains assets for the Agrimarket landing page. Some images are placeholders and should be replaced with actual assets before production deployment.

## Required Images

### 1. favicon.svg ✅
- **Status:** Present
- **Purpose:** Browser tab icon
- **Specifications:** SVG format, 32x32 or 64x64 px recommended
- **Current:** Placeholder SVG

### 2. app-screenshot.png ⚠️
- **Status:** MISSING - Needs to be added
- **Purpose:** Hero section app preview
- **Specifications:** 
  - Format: PNG or WebP
  - Recommended size: 1080x2340 px (phone aspect ratio)
  - Max file size: 500KB (optimize for web)
- **How to create:**
  1. Take a screenshot of the Android app home screen
  2. Use a device frame mockup (e.g., from [Mockuphone](https://mockuphone.com))
  3. Optimize the image using [TinyPNG](https://tinypng.com)
  4. Save as `app-screenshot.png` in this directory

### 3. logo.png (Optional)
- **Status:** Not required (using text logo)
- **Purpose:** Brand logo for header/footer
- **Specifications:** PNG with transparent background, 200x200 px

### 4. og-image.png (Recommended for SEO)
- **Status:** MISSING - Should be added for social sharing
- **Purpose:** Open Graph image for social media previews
- **Specifications:**
  - Format: PNG or JPG
  - Size: 1200x630 px (Facebook/LinkedIn standard)
  - Max file size: 1MB
- **Content suggestions:**
  - App screenshot with branding
  - Key features highlight
  - Trilingual text (EN/TA/SI)

## How to Add Images

### Option 1: Manual Upload
1. Create or obtain the image file
2. Place it in the `web/assets/` directory
3. Update references in `web/index.html` if needed

### Option 2: Using Git
```bash
# From project root
cd web/assets

# Add your image
# (copy the file here)

# Commit the change
git add app-screenshot.png
git commit -m "Add app screenshot for landing page"
git push
```

## Image Optimization Tips

Before adding images to the repository:

1. **Compress images:**
   - Use [TinyPNG](https://tinypng.com) for PNG files
   - Use [Squoosh](https://squoosh.app) for advanced optimization
   - Target: <200KB for screenshots, <50KB for icons

2. **Use appropriate formats:**
   - **PNG:** Screenshots, images with transparency
   - **JPG:** Photos, complex images without transparency
   - **SVG:** Icons, logos, simple graphics
   - **WebP:** Modern format with better compression (provide fallback)

3. **Responsive images:**
   - Provide multiple sizes for different screen resolutions
   - Use `srcset` attribute in HTML for responsive loading

## Current Status

- ✅ favicon.svg - Present
- ⚠️ app-screenshot.png - **NEEDS TO BE ADDED**
- ⚠️ og-image.png - **RECOMMENDED FOR SEO**
- ℹ️ logo.png - Optional

## Next Steps

1. **Priority 1:** Add `app-screenshot.png`
   - Take screenshot from Android app
   - Add device frame mockup
   - Optimize and add to this directory

2. **Priority 2:** Add `og-image.png`
   - Create social media preview image
   - Test with [Facebook Sharing Debugger](https://developers.facebook.com/tools/debug/)

3. **Priority 3:** Optimize existing images
   - Ensure favicon.svg is optimized
   - Consider creating favicon.ico for older browsers

## References

- [Web.dev Image Optimization Guide](https://web.dev/fast/#optimize-your-images)
- [Open Graph Protocol](https://ogp.me/)
- [Favicon Generator](https://realfavicongenerator.net/)

---

**Note:** Remember to update `.gitignore` if you want to exclude large image files from version control. For production, consider using a CDN for image hosting.

