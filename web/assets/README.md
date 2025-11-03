# Assets Directory

This directory contains images and other static assets for the Agrimarket landing page.

## Required Assets

### favicon.png
- **Size**: 32x32 px or 64x64 px
- **Format**: PNG with transparency
- **Description**: Site favicon displayed in browser tabs
- **Recommendation**: Use the Agrimarket logo or a simple wheat/agriculture icon

### app-screenshot.png
- **Size**: 1080x2340 px (recommended)
- **Format**: PNG or JPG
- **Description**: Screenshot of the Agrimarket mobile app for the hero section
- **Recommendation**: Use an actual screenshot from the Android app showing the home screen or key features

## Optional Assets

### logo.svg
- **Format**: SVG
- **Description**: Agrimarket logo in vector format
- **Usage**: Can be used in the header and footer

### hero-background.jpg
- **Size**: 1920x1080 px or larger
- **Format**: JPG (optimized)
- **Description**: Background image for the hero section
- **Recommendation**: Agricultural scene from Jaffna

### feature-images/
- **Description**: Images for feature cards
- **Recommendation**: Icons or illustrations representing each feature

## Image Optimization

Before adding images, optimize them:

1. **PNG files**: Use tools like TinyPNG or ImageOptim
2. **JPG files**: Compress to 80-85% quality
3. **SVG files**: Minify using SVGO
4. **Consider WebP**: For better compression and quality

## Placeholder Images

Until actual assets are available, the landing page uses:
- Emoji icons (🌾, 📱, etc.) for visual elements
- Material Icons for UI elements
- CSS gradients for backgrounds
- Placeholder divs for images

## Adding Images

1. Place images in this directory
2. Update the `src` attributes in `index.html`:
   ```html
   <img src="assets/app-screenshot.png" alt="Agrimarket App">
   ```
3. Update the favicon link in the `<head>`:
   ```html
   <link rel="icon" type="image/png" href="assets/favicon.png">
   ```

## Image Credits

When using images, ensure you have the rights to use them and provide proper attribution if required.

