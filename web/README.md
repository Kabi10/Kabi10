# Agrimarket Landing Page

Professional landing page for Agrimarket.lk - connecting farmers directly to consumers in Jaffna, Sri Lanka.

## 🌟 Features

- **Trilingual Support**: Full support for English, Tamil (தமிழ்), and Sinhala (සිංහල)
- **Material Design 3**: Modern, clean design following Material Design 3 principles
- **Responsive Design**: Fully responsive across desktop, tablet, and mobile devices
- **Offline-First Ready**: Static site that loads quickly and works reliably
- **SEO Optimized**: Proper meta tags and semantic HTML for search engines
- **Accessibility**: WCAG compliant with proper ARIA labels and keyboard navigation

## 🎨 Design System

### Colors
- **Primary Green**: `#16a34a` - Used for CTAs and primary actions
- **Secondary Blue**: `#60a5fa` - Used for secondary actions
- **Dark Background**: `#0f172a` - Main background color
- **Surface Dark**: `#1e293b` - Card and surface backgrounds

### Typography
- **Sans-serif**: Inter (English)
- **Tamil**: Noto Sans Tamil
- **Sinhala**: Noto Sans Sinhala

### Spacing
- Based on 8dp grid system
- Border radius: 16dp standard

## 📁 File Structure

```
web/
├── index.html          # Main HTML file
├── styles.css          # All CSS styles
├── script.js           # JavaScript functionality
├── vercel.json         # Vercel deployment configuration
├── README.md           # This file
└── assets/             # Images and other assets
    ├── favicon.png     # Site favicon
    └── app-screenshot.png  # App screenshot for hero section
```

## 🚀 Deployment

### Deploy to Vercel

1. **Install Vercel CLI** (if not already installed):
   ```bash
   npm install -g vercel
   ```

2. **Deploy from the web directory**:
   ```bash
   cd web
   vercel
   ```

3. **Follow the prompts**:
   - Set up and deploy: Yes
   - Which scope: Select your account
   - Link to existing project: No
   - Project name: agrimarket-landing
   - Directory: ./
   - Override settings: No

4. **Production deployment**:
   ```bash
   vercel --prod
   ```

### Deploy to Netlify

1. **Install Netlify CLI**:
   ```bash
   npm install -g netlify-cli
   ```

2. **Deploy**:
   ```bash
   cd web
   netlify deploy
   ```

3. **Production deployment**:
   ```bash
   netlify deploy --prod
   ```

### Deploy to GitHub Pages

1. **Create a new branch** `gh-pages`:
   ```bash
   git checkout -b gh-pages
   ```

2. **Copy web files to root** (or configure GitHub Pages to use the web directory)

3. **Push to GitHub**:
   ```bash
   git push origin gh-pages
   ```

4. **Enable GitHub Pages** in repository settings pointing to the `gh-pages` branch

## 🛠️ Local Development

Simply open `index.html` in a web browser, or use a local server:

### Using Python
```bash
cd web
python -m http.server 8000
```

### Using Node.js (http-server)
```bash
npm install -g http-server
cd web
http-server
```

### Using PHP
```bash
cd web
php -S localhost:8000
```

Then open `http://localhost:8000` in your browser.

## 📝 Customization

### Adding Images

1. Create an `assets` directory in the `web` folder
2. Add your images:
   - `favicon.png` - Site favicon (32x32 or 64x64 px)
   - `app-screenshot.png` - App screenshot for hero section (recommended: 1080x2340 px)

### Updating Content

All content is in `index.html` with trilingual support using `data-*` attributes:
- `data-en` - English text
- `data-ta` - Tamil text
- `data-si` - Sinhala text

Example:
```html
<h1 data-en="Welcome" data-ta="வரவேற்கிறோம்" data-si="ආයුබෝවන්">Welcome</h1>
```

### Updating Colors

Edit CSS variables in `styles.css`:
```css
:root {
    --primary-green: #16a34a;
    --secondary-blue: #60a5fa;
    /* ... other variables */
}
```

### Adding Sections

1. Add HTML structure in `index.html`
2. Add corresponding styles in `styles.css`
3. Add any interactive functionality in `script.js`

## 🌐 Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Mobile browsers (iOS Safari, Chrome Mobile)

## 📱 Responsive Breakpoints

- **Desktop**: > 1024px
- **Tablet**: 768px - 1024px
- **Mobile**: < 768px
- **Small Mobile**: < 480px

## ✨ Features Implemented

- [x] Trilingual language toggle (EN/TA/SI)
- [x] Responsive navigation with mobile menu
- [x] Smooth scrolling to sections
- [x] Animated statistics counter
- [x] Workflow tabs (Farmer/Buyer)
- [x] Contact form (frontend only)
- [x] Scroll to top button
- [x] Intersection Observer animations
- [x] Material Design 3 styling
- [x] SEO meta tags
- [x] Social media integration ready

## 🔧 TODO

- [ ] Add actual app screenshots
- [ ] Add favicon
- [ ] Connect contact form to backend/email service
- [ ] Add actual download links for Android app
- [ ] Add analytics (Google Analytics, etc.)
- [ ] Add social media links
- [ ] Optimize images
- [ ] Add sitemap.xml
- [ ] Add robots.txt

## 📄 License

This project is part of the Agrimarket platform. See the main repository LICENSE file for details.

## 🤝 Contributing

This landing page is part of the larger Agrimarket project. For contribution guidelines, see the main repository's CONTRIBUTING.md.

## 📧 Contact

For questions or support:
- Email: info@agrimarket.lk
- GitHub: [Agrimarket Repository](https://github.com/Kabi10/Srilanka-Farmers-Marketplace)

---

Built with ❤️ for the farming community of Jaffna, Sri Lanka 🌾

