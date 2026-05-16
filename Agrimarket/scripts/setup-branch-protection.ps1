# Setup Branch Protection for Agrimarket Repository
# This script helps set up branch protection rules for the master branch

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("public", "pro", "manual")]
    [string]$Option = "manual"
)

$RepoOwner = "Kabi10"
$RepoName = "Srilanka-Farmers-Marketplace"
$Branch = "master"
$ConfigFile = ".github/branch-protection-config.json"

Write-Host "🔒 Branch Protection Setup for $RepoOwner/$RepoName" -ForegroundColor Cyan
Write-Host ""

# Check if gh CLI is installed
try {
    $ghVersion = gh --version
    Write-Host "✅ GitHub CLI is installed" -ForegroundColor Green
} catch {
    Write-Host "❌ GitHub CLI is not installed. Please install it first:" -ForegroundColor Red
    Write-Host "   https://cli.github.com/" -ForegroundColor Yellow
    exit 1
}

# Check if user is authenticated
try {
    $authStatus = gh auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Not authenticated with GitHub CLI" -ForegroundColor Red
        Write-Host "   Run: gh auth login" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Authenticated with GitHub CLI" -ForegroundColor Green
} catch {
    Write-Host "❌ Authentication check failed" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check current repository visibility
Write-Host "🔍 Checking repository visibility..." -ForegroundColor Cyan
$repoInfo = gh repo view "$RepoOwner/$RepoName" --json visibility,isPrivate | ConvertFrom-Json

if ($repoInfo.isPrivate) {
    Write-Host "⚠️  Repository is PRIVATE" -ForegroundColor Yellow
    Write-Host "   Branch protection requires GitHub Pro for private repositories" -ForegroundColor Yellow
} else {
    Write-Host "✅ Repository is PUBLIC - branch protection is available" -ForegroundColor Green
}

Write-Host ""
Write-Host "📋 Available Options:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Make repository PUBLIC (FREE - enables branch protection)" -ForegroundColor White
Write-Host "   - Full branch protection features at no cost" -ForegroundColor Gray
Write-Host "   - Good for open-source projects like Agrimarket" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Keep PRIVATE with GitHub Pro ($4/month)" -ForegroundColor White
Write-Host "   - Requires upgrading your GitHub account" -ForegroundColor Gray
Write-Host "   - Visit: https://github.com/settings/billing/plans" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Manual setup via web interface" -ForegroundColor White
Write-Host "   - Visit: https://github.com/$RepoOwner/$RepoName/settings/branches" -ForegroundColor Gray
Write-Host ""

if ($Option -eq "public") {
    Write-Host "🔄 Option 1: Making repository public..." -ForegroundColor Cyan
    
    $confirm = Read-Host "Are you sure you want to make the repository public? (yes/no)"
    if ($confirm -ne "yes") {
        Write-Host "❌ Cancelled" -ForegroundColor Red
        exit 0
    }
    
    try {
        gh repo edit "$RepoOwner/$RepoName" --visibility public
        Write-Host "✅ Repository is now public" -ForegroundColor Green
        
        Write-Host ""
        Write-Host "🔒 Applying branch protection rules..." -ForegroundColor Cyan
        
        Get-Content $ConfigFile | gh api -X PUT "repos/$RepoOwner/$RepoName/branches/$Branch/protection" --input -
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Branch protection rules applied successfully!" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to apply branch protection rules" -ForegroundColor Red
            Write-Host "   Please set up manually at: https://github.com/$RepoOwner/$RepoName/settings/branches" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error: $_" -ForegroundColor Red
        exit 1
    }
    
} elseif ($Option -eq "pro") {
    Write-Host "🔄 Option 2: GitHub Pro setup..." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Please complete these steps:" -ForegroundColor Yellow
    Write-Host "1. Visit: https://github.com/settings/billing/plans" -ForegroundColor White
    Write-Host "2. Upgrade to GitHub Pro ($4/month)" -ForegroundColor White
    Write-Host "3. After upgrading, run this script again to apply protection" -ForegroundColor White
    Write-Host ""
    
    $upgraded = Read-Host "Have you upgraded to GitHub Pro? (yes/no)"
    if ($upgraded -eq "yes") {
        Write-Host ""
        Write-Host "🔒 Applying branch protection rules..." -ForegroundColor Cyan
        
        Get-Content $ConfigFile | gh api -X PUT "repos/$RepoOwner/$RepoName/branches/$Branch/protection" --input -
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Branch protection rules applied successfully!" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to apply branch protection rules" -ForegroundColor Red
            Write-Host "   Please set up manually at: https://github.com/$RepoOwner/$RepoName/settings/branches" -ForegroundColor Yellow
        }
    }
    
} else {
    Write-Host "🔄 Option 3: Manual setup instructions..." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Please follow these steps to set up branch protection manually:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Open this URL in your browser:" -ForegroundColor White
    Write-Host "   https://github.com/$RepoOwner/$RepoName/settings/branches" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "2. Click 'Add branch protection rule'" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Enter branch name pattern: $Branch" -ForegroundColor White
    Write-Host ""
    Write-Host "4. Enable these settings:" -ForegroundColor White
    Write-Host "   ✅ Require a pull request before merging" -ForegroundColor Green
    Write-Host "      - Require approvals: 1" -ForegroundColor Gray
    Write-Host "      - Dismiss stale pull request approvals when new commits are pushed" -ForegroundColor Gray
    Write-Host "   ✅ Do not allow bypassing the above settings" -ForegroundColor Green
    Write-Host "   ❌ Allow force pushes (keep unchecked)" -ForegroundColor Red
    Write-Host "   ❌ Allow deletions (keep unchecked)" -ForegroundColor Red
    Write-Host ""
    Write-Host "5. Click 'Create' or 'Save changes'" -ForegroundColor White
    Write-Host ""
    
    $openBrowser = Read-Host "Open the branch settings page in your browser? (yes/no)"
    if ($openBrowser -eq "yes") {
        Start-Process "https://github.com/$RepoOwner/$RepoName/settings/branches"
    }
}

Write-Host ""
Write-Host "📚 Additional Protection Measures:" -ForegroundColor Cyan
Write-Host "✅ CODEOWNERS file created at .github/CODEOWNERS" -ForegroundColor Green
Write-Host "✅ PR validation workflow created at .github/workflows/pr-validation.yml" -ForegroundColor Green
Write-Host ""
Write-Host "These files will help protect your repository even without full branch protection." -ForegroundColor Gray
Write-Host ""
Write-Host "Done! 🎉" -ForegroundColor Green

