# Agrimarket Landing Page Deployment Guide

This guide provides step-by-step instructions for deploying the Agrimarket landing page to various hosting platforms.

## 📋 Pre-Deployment Checklist

Before deploying, ensure you have:

- [ ] Added actual app screenshots to `assets/app-screenshot.png`
- [ ] Updated GitHub repository URL in `index.html` and `package.json`
- [ ] Updated email address (info@agrimarket.lk) if different
- [ ] Added social media links in the footer
- [ ] Tested the site locally
- [ ] Verified all three languages (EN/TA/SI) display correctly
- [ ] Optimized all images
- [ ] Updated meta descriptions and keywords

## 🚀 Deployment Options

### Option 1: Vercel (Recommended)

Vercel offers excellent performance, automatic HTTPS, and easy deployment.

#### Initial Setup

1. **Install Vercel CLI**:

   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**:

   ```bash
   vercel login
   ```

3. **Deploy from the web directory**:

   ```bash
   cd web
   vercel
   ```

4. **Follow the prompts**:
   - Set up and deploy: `Yes`
   - Which scope: Select your account
   - Link to existing project: `No`
   - Project name: `agrimarket-landing` (or your preferred name)
   - Directory: `./`
   - Override settings: `No`

5. **Production deployment**:
   ```bash
   vercel --prod
   ```

#### Custom Domain Setup

1. Go to your Vercel dashboard
2. Select your project
3. Go to Settings → Domains
4. Add your domain (e.g., `agrimarket.lk`)
5. Follow DNS configuration instructions

#### Environment Variables

If you add backend integration later:

1. Go to Settings → Environment Variables
2. Add your variables (API keys, etc.)

### Option 2: Netlify

Netlify is another excellent option with similar features to Vercel.

#### Initial Setup

1. **Install Netlify CLI**:

   ```bash
   npm install -g netlify-cli
   ```

2. **Login to Netlify**:

   ```bash
   netlify login
   ```

3. **Deploy**:

   ```bash
   cd web
   netlify deploy
   ```

4. **Production deployment**:
   ```bash
   netlify deploy --prod
   ```

#### Custom Domain Setup

1. Go to your Netlify dashboard
2. Select your site
3. Go to Domain settings
4. Add custom domain
5. Follow DNS configuration instructions

### Option 3: GitHub Pages

Free hosting directly from your GitHub repository.

#### Setup

1. **Create a new branch** for GitHub Pages:

   ```bash
   git checkout -b gh-pages
   ```

2. **Copy web files to root** (or configure to use web directory):

   ```bash
   # Option A: Copy files to root
   cp -r web/* .
   git add .
   git commit -m "Deploy to GitHub Pages"
   git push origin gh-pages

   # Option B: Use web directory (requires configuration)
   git push origin gh-pages
   ```

3. **Enable GitHub Pages**:
   - Go to repository Settings
   - Scroll to GitHub Pages section
   - Select `gh-pages` branch
   - Select root or `/web` folder
   - Save

4. **Custom domain** (optional):
   - Add a `CNAME` file with your domain
   - Configure DNS settings

### Option 4: Firebase Hosting

Google's hosting solution with excellent performance.

#### Setup

1. **Install Firebase CLI**:

   ```bash
   npm install -g firebase-tools
   ```

2. **Login to Firebase**:

   ```bash
   firebase login
   ```

3. **Initialize Firebase**:

   ```bash
   cd web
   firebase init hosting
   ```

4. **Configure**:
   - Select your Firebase project
   - Public directory: `.` (current directory)
   - Single-page app: `No`
   - Overwrite index.html: `No`

5. **Deploy**:
   ```bash
   firebase deploy --only hosting
   ```

### Option 5: AWS S3 + CloudFront

For enterprise-grade hosting with AWS.

#### Setup

1. **Create S3 bucket**:
   - Go to AWS S3 console
   - Create bucket with your domain name
   - Enable static website hosting
   - Set index document to `index.html`

2. **Upload files**:

   ```bash
   aws s3 sync . s3://your-bucket-name --exclude ".git/*"
   ```

3. **Configure CloudFront** (optional but recommended):
   - Create CloudFront distribution
   - Set origin to your S3 bucket
   - Configure SSL certificate
   - Set default root object to `index.html`

4. **Configure DNS**:
   - Point your domain to CloudFront distribution

## 🔧 Post-Deployment Configuration

### 1. DNS Configuration

For custom domain (e.g., agrimarket.lk):

**A Record**:

```
Type: A
Name: @
Value: [Your hosting provider's IP]
```

**CNAME Record** (for www):

```
Type: CNAME
Name: www
Value: agrimarket.lk
```

### 2. SSL Certificate

Most modern hosting providers (Vercel, Netlify, Firebase) provide automatic SSL certificates. If not:

- Use Let's Encrypt for free SSL
- Configure through your hosting provider's dashboard

### 3. Analytics Setup

Add Google Analytics or similar:

1. Create analytics account
2. Get tracking ID
3. Add to `index.html` before `</head>`:

```html
<!-- Google Analytics -->
<script
  async
  src="https://www.googletagmanager.com/gtag/js?id=GA_MEASUREMENT_ID"
></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag() {
    dataLayer.push(arguments);
  }
  gtag("js", new Date());
  gtag("config", "GA_MEASUREMENT_ID");
</script>
```

### 4. SEO Configuration

1. **Submit sitemap** to Google Search Console:
   - Create `sitemap.xml`
   - Submit to search engines

2. **robots.txt**:

   ```
   User-agent: *
   Allow: /
   Sitemap: https://agrimarket.lk/sitemap.xml
   ```

3. **Open Graph tags** (already included in index.html):
   - Verify with Facebook Debugger
   - Verify with Twitter Card Validator

## 🔄 Continuous Deployment

### GitHub Actions (for automated deployment)

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Vercel

on:
  push:
    branches: [main]
    paths:
      - "web/**"

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
          working-directory: ./web
```

## 📊 Monitoring

### Performance Monitoring

1. **Google PageSpeed Insights**: https://pagespeed.web.dev/
2. **GTmetrix**: https://gtmetrix.com/
3. **WebPageTest**: https://www.webpagetest.org/

### Uptime Monitoring

1. **UptimeRobot**: Free uptime monitoring
2. **Pingdom**: Comprehensive monitoring
3. **StatusCake**: Free tier available

## 🐛 Troubleshooting

### Common Issues

1. **404 errors on refresh**:
   - Configure hosting to serve `index.html` for all routes
   - Add redirect rules in `vercel.json` or hosting config

2. **CSS/JS not loading**:
   - Check file paths are relative
   - Verify MIME types are correct
   - Check browser console for errors

3. **Fonts not loading**:
   - Verify Google Fonts CDN is accessible
   - Check CORS headers

4. **Images not displaying**:
   - Verify image paths
   - Check image file names (case-sensitive on some servers)
   - Ensure images are in the `assets` directory

## 📝 Maintenance

### Regular Updates

1. **Content updates**: Edit `index.html`
2. **Style updates**: Edit `styles.css`
3. **Functionality updates**: Edit `script.js`
4. **Redeploy**: Run deployment command

### Backup

- Keep repository backed up on GitHub
- Export hosting configuration
- Document custom settings

## 🎯 Performance Optimization

1. **Image optimization**:
   - Use WebP format where supported
   - Compress images before upload
   - Use responsive images

2. **Minification**:
   - Minify CSS and JavaScript for production
   - Use build tools if needed

3. **Caching**:
   - Configure cache headers (already in `vercel.json`)
   - Use CDN for static assets

## 📞 Support

For deployment issues:

- Check hosting provider documentation
- Review deployment logs
- Contact hosting support

For code issues:

- Check browser console
- Review GitHub issues
- Contact development team

---

**Last Updated**: 2026-01-04
**Version**: 1.0.0
