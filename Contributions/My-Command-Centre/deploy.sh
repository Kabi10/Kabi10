#!/bin/bash

# My Command Centre Deployment Script
# Automates the complete setup and deployment of the Command Centre system

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REPO_NAME="My-Command-Centre"
REPO_DESCRIPTION="Private intelligence and automation hub for GitHub portfolio management"
CONFIG_FILE="config/command-centre.yml"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if gh CLI is installed
    if ! command -v gh &> /dev/null; then
        log_error "GitHub CLI (gh) is not installed. Please install it first."
        echo "Visit: https://cli.github.com/"
        exit 1
    fi
    
    # Check if gh is authenticated
    if ! gh auth status &> /dev/null; then
        log_error "GitHub CLI is not authenticated. Please run 'gh auth login' first."
        exit 1
    fi
    
    # Check if git is installed
    if ! command -v git &> /dev/null; then
        log_error "Git is not installed. Please install it first."
        exit 1
    fi
    
    # Check if python3 is installed
    if ! command -v python3 &> /dev/null; then
        log_error "Python 3 is not installed. Please install it first."
        exit 1
    fi
    
    # Check if node is installed
    if ! command -v node &> /dev/null; then
        log_error "Node.js is not installed. Please install it first."
        exit 1
    fi
    
    # Check if npm is installed
    if ! command -v npm &> /dev/null; then
        log_error "npm is not installed. Please install it first."
        exit 1
    fi
    
    log_success "All prerequisites are installed"
}

create_repository() {
    log_info "Creating private GitHub repository..."
    
    # Check if repository already exists
    if gh repo view "$REPO_NAME" &> /dev/null; then
        log_warning "Repository $REPO_NAME already exists"
        read -p "Do you want to continue with the existing repository? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Deployment cancelled"
            exit 0
        fi
    else
        # Create the repository
        gh repo create "$REPO_NAME" --private --description "$REPO_DESCRIPTION"
        log_success "Repository created successfully"
    fi
}

setup_local_repository() {
    log_info "Setting up local repository..."
    
    # Get the current directory name
    CURRENT_DIR=$(basename "$PWD")
    
    if [ "$CURRENT_DIR" != "$REPO_NAME" ]; then
        log_error "Please run this script from the $REPO_NAME directory"
        exit 1
    fi
    
    # Initialize git if not already initialized
    if [ ! -d ".git" ]; then
        git init
        log_info "Initialized git repository"
    fi
    
    # Get GitHub username
    GITHUB_USER=$(gh api user --jq .login)
    
    # Set remote origin if not already set
    if ! git remote get-url origin &> /dev/null; then
        git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"
        log_info "Added remote origin"
    fi
    
    log_success "Local repository setup complete"
}

install_dependencies() {
    log_info "Installing dependencies..."
    
    # Create virtual environment for Python
    if [ ! -d "venv" ]; then
        python3 -m venv venv
        log_info "Created Python virtual environment"
    fi
    
    # Activate virtual environment
    source venv/bin/activate
    
    # Install Python dependencies
    if [ -f "scripts/requirements.txt" ]; then
        pip install -r scripts/requirements.txt
        log_success "Python dependencies installed"
    else
        log_warning "scripts/requirements.txt not found"
    fi
    
    # Install Node.js dependencies for scripts
    if [ -f "scripts/package.json" ]; then
        cd scripts
        npm install
        cd ..
        log_success "Scripts Node.js dependencies installed"
    else
        log_warning "scripts/package.json not found"
    fi
    
    # Install Node.js dependencies for dashboard
    if [ -f "dashboard/package.json" ]; then
        cd dashboard
        npm install
        cd ..
        log_success "Dashboard Node.js dependencies installed"
    else
        log_warning "dashboard/package.json not found"
    fi
}

configure_secrets() {
    log_info "Configuring GitHub secrets..."
    
    # Check if GITHUB_TOKEN is already set
    if gh secret list | grep -q "GITHUB_TOKEN"; then
        log_warning "GITHUB_TOKEN secret already exists"
    else
        # Prompt for GitHub token
        echo "Please enter your GitHub Personal Access Token:"
        echo "The token needs the following scopes: repo, workflow, read:user, read:org"
        read -s -p "GitHub Token: " GITHUB_TOKEN
        echo
        
        if [ -n "$GITHUB_TOKEN" ]; then
            echo "$GITHUB_TOKEN" | gh secret set GITHUB_TOKEN
            log_success "GITHUB_TOKEN secret configured"
        else
            log_error "GitHub token is required"
            exit 1
        fi
    fi
}

customize_configuration() {
    log_info "Customizing configuration..."
    
    if [ -f "$CONFIG_FILE" ]; then
        # Get GitHub username for configuration
        GITHUB_USER=$(gh api user --jq .login)
        
        # Update configuration with user-specific settings
        sed -i.bak "s/yourusername/$GITHUB_USER/g" "$CONFIG_FILE" 2>/dev/null || \
        sed -i "s/yourusername/$GITHUB_USER/g" "$CONFIG_FILE"
        
        log_success "Configuration customized for user: $GITHUB_USER"
    else
        log_warning "Configuration file not found: $CONFIG_FILE"
    fi
}

enable_github_pages() {
    log_info "Enabling GitHub Pages..."
    
    # Enable GitHub Pages
    gh api repos/:owner/:repo/pages \
        --method POST \
        --field source='{"branch":"main","path":"/dashboard/dist"}' \
        2>/dev/null || log_warning "GitHub Pages may already be enabled or needs manual setup"
    
    log_success "GitHub Pages configuration attempted"
}

validate_setup() {
    log_info "Validating setup..."
    
    # Activate virtual environment
    source venv/bin/activate
    
    # Run validation script
    if [ -f "scripts/validate_setup.py" ]; then
        python scripts/validate_setup.py --config "$CONFIG_FILE"
        log_success "Setup validation completed"
    else
        log_warning "Validation script not found"
    fi
}

commit_and_push() {
    log_info "Committing and pushing to GitHub..."
    
    # Add all files
    git add .
    
    # Create initial commit
    git commit -m "🎯 Initial Command Centre setup

- Complete automation and analytics system
- Strategic planning framework
- Sri Lankan open source project focus
- Healthcare technology contribution methodology
- Privacy-first design with GitHub-only dependencies

Ready for portfolio intelligence and strategic guidance!" || log_warning "Nothing to commit or commit already exists"
    
    # Push to main branch
    git branch -M main
    git push -u origin main
    
    log_success "Code pushed to GitHub successfully"
}

run_initial_workflow() {
    log_info "Running initial workflow..."
    
    # Wait a moment for the repository to be ready
    sleep 5
    
    # Trigger the repository discovery workflow
    gh workflow run repository-discovery.yml || log_warning "Could not trigger workflow automatically"
    
    log_success "Initial workflow triggered"
}

display_completion_info() {
    echo
    log_success "🎉 My Command Centre deployment completed successfully!"
    echo
    echo -e "${BLUE}📊 Your Command Centre is now active with:${NC}"
    echo "  ✅ Automated repository discovery and health scoring"
    echo "  ✅ Strategic planning framework for Sri Lankan social good projects"
    echo "  ✅ Healthcare technology contribution methodology (HMIS, OpenMRS, DHIS2)"
    echo "  ✅ Weekly and monthly automated reporting"
    echo "  ✅ Private dashboard with portfolio analytics"
    echo "  ✅ Intelligent alerting and recommendation system"
    echo
    echo -e "${BLUE}🔗 Access your resources:${NC}"
    GITHUB_USER=$(gh api user --jq .login)
    echo "  📊 Dashboard: https://$GITHUB_USER.github.io/$REPO_NAME"
    echo "  📁 Repository: https://github.com/$GITHUB_USER/$REPO_NAME"
    echo "  🤖 Workflows: https://github.com/$GITHUB_USER/$REPO_NAME/actions"
    echo
    echo -e "${BLUE}📋 Next steps:${NC}"
    echo "  1. Review the generated analytics in the repository"
    echo "  2. Customize strategic planning documents in /strategy/"
    echo "  3. Begin contributing to Sri Lankan healthcare projects"
    echo "  4. Monitor weekly reports and strategic insights"
    echo
    echo -e "${BLUE}🏥 Sri Lankan Healthcare Focus:${NC}"
    echo "  • HMIS contributions (hmislk/hmis) - Primary target"
    echo "  • OpenMRS localization opportunities"
    echo "  • DHIS2 health application development"
    echo "  • Government digital services (ECLK, SIS)"
    echo
    echo -e "${GREEN}Your portfolio intelligence system is ready! 🚀${NC}"
}

# Main deployment process
main() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    My Command Centre                         ║"
    echo "║              Deployment & Setup Script                      ║"
    echo "║                                                              ║"
    echo "║  🎯 Portfolio Intelligence & Automation Hub                 ║"
    echo "║  🇱🇰 Sri Lankan Social Good Project Focus                   ║"
    echo "║  🏥 Healthcare Technology Contribution System               ║"
    echo "║  🔒 Privacy-First Design                                    ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo
    
    # Deployment steps
    check_prerequisites
    create_repository
    setup_local_repository
    install_dependencies
    configure_secrets
    customize_configuration
    enable_github_pages
    validate_setup
    commit_and_push
    run_initial_workflow
    display_completion_info
}

# Run main function
main "$@"
