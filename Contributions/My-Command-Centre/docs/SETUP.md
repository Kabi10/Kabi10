# My Command Centre - Setup Guide

This guide will help you set up your private Command Centre repository for comprehensive GitHub portfolio management and automation.

## Prerequisites

### Required Tools
- **Git** - Version control
- **GitHub CLI** (`gh`) - GitHub API interactions
- **Python 3.11+** - Automation scripts
- **Node.js 18+** - Dashboard generation
- **GitHub Account** - With repository creation permissions

### Required Permissions
Your GitHub token needs the following scopes:
- `repo` - Full repository access
- `workflow` - GitHub Actions workflow access
- `read:user` - User profile information
- `read:org` - Organization information (if applicable)

## Initial Setup

### 1. Create Private Repository

```bash
# Create new private repository
gh repo create My-Command-Centre --private --description "Private intelligence and automation hub for GitHub portfolio management"

# Clone the repository
git clone https://github.com/yourusername/My-Command-Centre.git
cd My-Command-Centre
```

### 2. Copy Command Centre Files

Copy all the files from this setup to your new repository:

```bash
# Copy all files (adjust paths as needed)
cp -r /path/to/command-centre-files/* .

# Add all files to git
git add .
git commit -m "Initial Command Centre setup"
git push origin main
```

### 3. Configure GitHub Secrets

Set up required secrets in your repository:

```bash
# Set GitHub token for automation
gh secret set GITHUB_TOKEN --body "your_github_personal_access_token"

# Optional: Set additional configuration
gh secret set COMMAND_CENTRE_CONFIG --body "production"
```

### 4. Install Dependencies

#### Python Dependencies
```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install Python dependencies
pip install -r scripts/requirements.txt
```

#### Node.js Dependencies
```bash
# Install script dependencies
cd scripts
npm install
cd ..

# Install dashboard dependencies
cd dashboard
npm install
cd ..
```

### 5. Configure Settings

Edit `config/command-centre.yml` to customize your settings:

```yaml
# Update repository discovery settings
repository_discovery:
  filters:
    include_private: true
    include_public: true
    # Add your specific filters

# Configure health scoring weights
health_scoring:
  weights:
    commit_frequency: 25
    documentation_quality: 20
    # Adjust weights based on your priorities

# Set up open source contribution tracking
open_source:
  target_projects:
    - name: "HMIS"
      repo: "hmislk/hmis"
      fork: "yourusername/hmis"  # Update with your fork
```

### 6. Enable GitHub Pages

```bash
# Enable GitHub Pages for dashboard
gh api repos/:owner/:repo/pages \
  --method POST \
  --field source='{"branch":"main","path":"/dashboard/dist"}'
```

### 7. Test Initial Setup

```bash
# Test repository discovery
python scripts/repository_discovery.py \
  --config config/command-centre.yml \
  --output analytics/test-repositories.json

# Test health scoring
python scripts/health_scoring.py \
  --input analytics/test-repositories.json \
  --config config/command-centre.yml \
  --output analytics/test-health-scores.json

# Test dashboard generation
node dashboard/generate.js \
  --repositories analytics/test-repositories.json \
  --health-scores analytics/test-health-scores.json \
  --config config/command-centre.yml
```

## Workflow Configuration

### 1. Verify GitHub Actions

Check that workflows are properly configured:

```bash
# List workflows
gh workflow list

# Run repository discovery workflow manually
gh workflow run repository-discovery.yml
```

### 2. Monitor First Run

```bash
# Watch workflow execution
gh run watch

# View workflow logs
gh run view --log
```

### 3. Check Generated Files

After the first successful run, verify these files are created:
- `analytics/repositories.json`
- `analytics/health_scores.json`
- `analytics/portfolio_overview.json`
- `analytics/activity_heatmap.json`

## Dashboard Setup

### 1. Build Dashboard

```bash
cd dashboard
npm run build
```

### 2. Deploy to GitHub Pages

The GitHub Actions workflow automatically deploys the dashboard, but you can also deploy manually:

```bash
# Deploy dashboard
gh workflow run repository-discovery.yml
```

### 3. Access Dashboard

Your dashboard will be available at:
```
https://yourusername.github.io/My-Command-Centre
```

## Customization

### 1. Adjust Automation Schedule

Edit `.github/workflows/repository-discovery.yml`:

```yaml
on:
  schedule:
    # Change from daily to your preferred schedule
    - cron: '0 6 * * *'  # Daily at 6 AM UTC
```

### 2. Configure Alert Thresholds

Edit `config/command-centre.yml`:

```yaml
intelligence:
  alerts:
    max_daily_alerts: 5  # Adjust based on your preference
    types:
      maintenance_needed: true
      contribution_opportunity: true
      # Enable/disable alert types
```

### 3. Customize Health Scoring

Adjust health scoring weights in `config/command-centre.yml`:

```yaml
health_scoring:
  weights:
    commit_frequency: 30      # Increase if you value activity
    documentation_quality: 25 # Increase if you value docs
    # Adjust other weights
```

### 4. Add Custom Strategic Documents

```bash
# Create new strategic document
cp templates/strategic-planning-template.md \
   strategy/development-thoughts/2024-01-15-my-new-strategy.md

# Edit and customize the document
# Commit to repository for tracking
```

## Troubleshooting

### Common Issues

#### 1. GitHub CLI Authentication
```bash
# Re-authenticate if needed
gh auth login --with-token < your_token_file
```

#### 2. Python Dependencies
```bash
# If dependencies fail to install
pip install --upgrade pip
pip install -r scripts/requirements.txt --force-reinstall
```

#### 3. Node.js Dependencies
```bash
# Clear npm cache if needed
npm cache clean --force
cd dashboard && npm install --force
```

#### 4. Workflow Failures
```bash
# Check workflow logs
gh run list --limit 5
gh run view [run-id] --log
```

### Debug Mode

Enable debug logging by setting environment variable:

```bash
export COMMAND_CENTRE_DEBUG=true
python scripts/repository_discovery.py --config config/command-centre.yml --output analytics/debug-test.json
```

### Validation

Validate your setup with the built-in checks:

```bash
# Run validation script (if created)
python scripts/validate_setup.py --config config/command-centre.yml
```

## Security Considerations

### 1. Token Security
- Never commit tokens to the repository
- Use GitHub Secrets for all sensitive data
- Regularly rotate your GitHub tokens

### 2. Private Repository
- Ensure repository remains private
- Regularly audit access permissions
- Monitor audit logs in `analytics/audit/`

### 3. Data Privacy
- All data stays within GitHub ecosystem
- No external API calls or third-party services
- Encrypted storage for sensitive strategic documents

## Maintenance

### Regular Tasks

#### Weekly
- Review generated alerts and issues
- Update strategic documents
- Check dashboard for insights

#### Monthly
- Review and update configuration
- Assess health scoring accuracy
- Update contribution methodology

#### Quarterly
- Strategic planning review
- Portfolio optimization assessment
- System performance evaluation

### Updates

Keep your Command Centre updated:

```bash
# Pull latest improvements (if you maintain a template)
git remote add template https://github.com/your-template-repo.git
git fetch template
git merge template/main --allow-unrelated-histories
```

## Support

### Documentation
- Main README: `README.md`
- API Documentation: `docs/api/`
- Strategic Planning: `strategy/README.md`

### Logs and Debugging
- Workflow logs: GitHub Actions interface
- Audit logs: `analytics/audit/`
- Error logs: Check individual script outputs

### Community
- GitHub Issues: For feature requests and bugs
- GitHub Discussions: For strategic planning discussions

---

**Next Steps:**
1. Complete the setup steps above
2. Run your first automation cycle
3. Review the generated dashboard and analytics
4. Customize configuration based on your needs
5. Start strategic planning with the provided templates

*Your Command Centre is now ready to provide comprehensive portfolio intelligence and automation!*
