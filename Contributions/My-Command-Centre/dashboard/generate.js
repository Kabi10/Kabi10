#!/usr/bin/env node

/**
 * Dashboard Generator for My Command Centre
 * Generates interactive HTML dashboard from portfolio analytics
 */

const fs = require('fs-extra');
const path = require('path');
const moment = require('moment');

class DashboardGenerator {
    constructor() {
        this.outputDir = path.join(__dirname, 'dist');
        this.analyticsDir = path.join(__dirname, '..', 'analytics');
        this.templateDir = path.join(__dirname, 'templates');
        
        console.log('🎯 My Command Centre Dashboard Generator');
        console.log('📊 Generating interactive portfolio dashboard...');
    }

    async generate() {
        try {
            // Ensure output directory exists
            await fs.ensureDir(this.outputDir);
            
            // Load analytics data
            const analyticsData = await this.loadAnalyticsData();
            
            // Generate dashboard components
            await this.generateMainDashboard(analyticsData);
            await this.generateAssets();
            await this.generateDataFiles(analyticsData);
            
            console.log('✅ Dashboard generated successfully!');
            console.log(`📁 Output directory: ${this.outputDir}`);
            
        } catch (error) {
            console.error('❌ Dashboard generation failed:', error);
            throw error;
        }
    }

    async loadAnalyticsData() {
        console.log('📥 Loading analytics data...');
        
        const analyticsFiles = {
            repositories: 'repositories.json',
            healthScores: 'health_scores.json',
            portfolioOverview: 'portfolio_overview.json',
            contributionPatterns: 'contribution_patterns.json',
            activityHeatmap: 'activity_heatmap.json',
            alerts: 'alerts.json'
        };

        const data = {};
        
        for (const [key, filename] of Object.entries(analyticsFiles)) {
            const filePath = path.join(this.analyticsDir, filename);
            
            try {
                if (await fs.pathExists(filePath)) {
                    const fileContent = await fs.readJson(filePath);
                    data[key] = fileContent;
                    console.log(`  ✅ Loaded ${filename}`);
                } else {
                    console.log(`  ⚠️  ${filename} not found, using empty data`);
                    data[key] = {};
                }
            } catch (error) {
                console.log(`  ❌ Error loading ${filename}:`, error.message);
                data[key] = {};
            }
        }

        return data;
    }

    async generateMainDashboard(analyticsData) {
        console.log('🏗️  Generating main dashboard...');
        
        const dashboardHtml = this.createDashboardHtml(analyticsData);
        const indexPath = path.join(this.outputDir, 'index.html');
        
        await fs.writeFile(indexPath, dashboardHtml);
        console.log('  ✅ Main dashboard created');
    }

    createDashboardHtml(analyticsData) {
        const { repositories, healthScores, portfolioOverview, contributionPatterns, alerts } = analyticsData;
        
        // Extract key metrics
        const totalRepos = repositories?.repositories?.length || 0;
        const healthData = healthScores?.health_scores || {};
        const healthyRepos = Object.values(healthData).filter(h => 
            typeof h === 'object' && ['excellent', 'good'].includes(h.health_grade)
        ).length;
        
        const sriLankanProjects = contributionPatterns?.sri_lankan_projects?.total_sri_lankan_projects || 0;
        const healthcareProjects = contributionPatterns?.healthcare_contributions?.total_healthcare_repositories || 0;
        const totalAlerts = alerts?.alerts?.length || 0;
        const highPriorityAlerts = alerts?.alerts?.filter(a => a.priority === 'high').length || 0;

        return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Command Centre - Portfolio Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        :root {
            --primary-color: #2563eb;
            --secondary-color: #64748b;
            --success-color: #059669;
            --warning-color: #d97706;
            --danger-color: #dc2626;
            --sri-lanka-color: #ff6b35;
            --healthcare-color: #10b981;
        }
        
        body {
            background-color: #f8fafc;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .navbar {
            background: linear-gradient(135deg, var(--primary-color), #1e40af);
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        
        .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 16px rgba(0,0,0,0.15);
        }
        
        .metric-card {
            background: linear-gradient(135deg, #ffffff, #f8fafc);
        }
        
        .metric-value {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0;
        }
        
        .metric-label {
            color: var(--secondary-color);
            font-size: 0.9rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .sri-lanka-badge {
            background: linear-gradient(135deg, var(--sri-lanka-color), #ff8f65);
            color: white;
        }
        
        .healthcare-badge {
            background: linear-gradient(135deg, var(--healthcare-color), #34d399);
            color: white;
        }
        
        .chart-container {
            position: relative;
            height: 300px;
        }
        
        .alert-item {
            border-left: 4px solid var(--warning-color);
            background: #fef3c7;
        }
        
        .alert-item.high-priority {
            border-left-color: var(--danger-color);
            background: #fee2e2;
        }
        
        .footer {
            background: #1f2937;
            color: #9ca3af;
            margin-top: 3rem;
        }
        
        .sri-lanka-flag {
            background: linear-gradient(to right, #ff6b35, #ffa500, #008000);
            height: 4px;
            border-radius: 2px;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="#">
                <i class="fas fa-chart-line me-2"></i>
                My Command Centre
            </a>
            <div class="navbar-nav ms-auto">
                <span class="navbar-text">
                    <i class="fas fa-shield-alt me-1"></i>
                    Private Portfolio Intelligence
                </span>
            </div>
        </div>
    </nav>

    <!-- Header -->
    <div class="container mt-4">
        <div class="row align-items-center mb-4">
            <div class="col">
                <h1 class="display-6 mb-2">
                    <i class="fas fa-tachometer-alt text-primary me-3"></i>
                    Portfolio Dashboard
                </h1>
                <p class="text-muted mb-0">
                    <i class="fas fa-calendar-alt me-2"></i>
                    Last updated: ${moment().format('MMMM Do YYYY, h:mm A')}
                </p>
                <div class="sri-lanka-flag mt-2"></div>
            </div>
            <div class="col-auto">
                <div class="badge sri-lanka-badge fs-6 px-3 py-2">
                    <i class="fas fa-heart me-2"></i>
                    Sri Lankan Social Good Focus
                </div>
            </div>
        </div>
    </div>

    <!-- Key Metrics -->
    <div class="container">
        <div class="row g-4 mb-5">
            <div class="col-md-3">
                <div class="card metric-card h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-code-branch text-primary fs-1 mb-3"></i>
                        <div class="metric-value text-primary">${totalRepos}</div>
                        <div class="metric-label">Total Repositories</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card metric-card h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-heart-pulse text-success fs-1 mb-3"></i>
                        <div class="metric-value text-success">${healthyRepos}</div>
                        <div class="metric-label">Healthy Repositories</div>
                        <small class="text-muted">${totalRepos > 0 ? Math.round(healthyRepos/totalRepos*100) : 0}% of portfolio</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card metric-card h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-flag text-warning fs-1 mb-3"></i>
                        <div class="metric-value" style="color: var(--sri-lanka-color)">${sriLankanProjects}</div>
                        <div class="metric-label">Sri Lankan Projects</div>
                        <small class="text-muted">Social good focus</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card metric-card h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-hospital text-info fs-1 mb-3"></i>
                        <div class="metric-value" style="color: var(--healthcare-color)">${healthcareProjects}</div>
                        <div class="metric-label">Healthcare Projects</div>
                        <small class="text-muted">HMIS & health tech</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- Charts and Analysis -->
        <div class="row g-4 mb-5">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">
                            <i class="fas fa-chart-pie me-2"></i>
                            Portfolio Health Distribution
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="healthChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-header" style="background: var(--sri-lanka-color); color: white;">
                        <h5 class="card-title mb-0">
                            <i class="fas fa-globe-asia me-2"></i>
                            Sri Lankan Focus
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="sriLankanChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Alerts and Actions -->
        ${totalAlerts > 0 ? `
        <div class="row mb-5">
            <div class="col">
                <div class="card">
                    <div class="card-header bg-warning text-dark">
                        <h5 class="card-title mb-0">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            Active Alerts (${totalAlerts})
                            ${highPriorityAlerts > 0 ? `<span class="badge bg-danger ms-2">${highPriorityAlerts} High Priority</span>` : ''}
                        </h5>
                    </div>
                    <div class="card-body">
                        <div id="alertsList">
                            ${this.generateAlertsHtml(alerts?.alerts || [])}
                        </div>
                    </div>
                </div>
            </div>
        </div>
        ` : ''}

        <!-- Strategic Focus Areas -->
        <div class="row g-4 mb-5">
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header healthcare-badge">
                        <h5 class="card-title mb-0">
                            <i class="fas fa-stethoscope me-2"></i>
                            Healthcare Technology Focus
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row text-center">
                            <div class="col-4">
                                <div class="metric-value text-success fs-4">HMIS</div>
                                <div class="metric-label">Primary Target</div>
                            </div>
                            <div class="col-4">
                                <div class="metric-value text-info fs-4">OpenMRS</div>
                                <div class="metric-label">Expansion</div>
                            </div>
                            <div class="col-4">
                                <div class="metric-value text-warning fs-4">DHIS2</div>
                                <div class="metric-label">Exploration</div>
                            </div>
                        </div>
                        <hr>
                        <p class="text-muted mb-0">
                            <i class="fas fa-target me-2"></i>
                            Focus on pharmacy module optimization, localization, and mobile health applications
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header sri-lanka-badge">
                        <h5 class="card-title mb-0">
                            <i class="fas fa-hands-helping me-2"></i>
                            Social Good Impact
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row text-center">
                            <div class="col-4">
                                <div class="metric-value text-primary fs-4">60%</div>
                                <div class="metric-label">Healthcare</div>
                            </div>
                            <div class="col-4">
                                <div class="metric-value text-secondary fs-4">30%</div>
                                <div class="metric-label">Government</div>
                            </div>
                            <div class="col-4">
                                <div class="metric-value text-success fs-4">10%</div>
                                <div class="metric-label">Language</div>
                            </div>
                        </div>
                        <hr>
                        <p class="text-muted mb-0">
                            <i class="fas fa-heart me-2"></i>
                            Direct impact on Sri Lankan healthcare delivery and digital inclusion
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer py-4">
        <div class="container">
            <div class="row align-items-center">
                <div class="col">
                    <p class="mb-0">
                        <i class="fas fa-shield-alt me-2"></i>
                        My Command Centre - Private Portfolio Intelligence
                    </p>
                    <small>🇱🇰 Built for Sri Lankan Social Good | 🏥 Healthcare Technology Focus | 🔒 Privacy-First Design</small>
                </div>
                <div class="col-auto">
                    <small class="text-muted">
                        Generated: ${moment().format('YYYY-MM-DD HH:mm')} UTC
                    </small>
                </div>
            </div>
        </div>
    </footer>

    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Health Distribution Chart
        const healthCtx = document.getElementById('healthChart').getContext('2d');
        new Chart(healthCtx, {
            type: 'doughnut',
            data: {
                labels: ['Excellent', 'Good', 'Fair', 'Poor'],
                datasets: [{
                    data: [${this.getHealthDistribution(healthData)}],
                    backgroundColor: ['#059669', '#10b981', '#f59e0b', '#ef4444'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        // Sri Lankan Projects Chart
        const sriLankanCtx = document.getElementById('sriLankanChart').getContext('2d');
        new Chart(sriLankanCtx, {
            type: 'bar',
            data: {
                labels: ['Healthcare', 'Government', 'Education', 'Language'],
                datasets: [{
                    data: [${this.getSriLankanDistribution(contributionPatterns)}],
                    backgroundColor: ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b'],
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>`;
    }

    generateAlertsHtml(alerts) {
        if (!alerts || alerts.length === 0) {
            return '<p class="text-muted">No active alerts</p>';
        }

        return alerts.slice(0, 5).map(alert => `
            <div class="alert-item ${alert.priority === 'high' ? 'high-priority' : ''} p-3 mb-3 rounded">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <h6 class="mb-1">
                            <i class="fas fa-${alert.priority === 'high' ? 'exclamation-circle' : 'info-circle'} me-2"></i>
                            ${alert.title || 'Alert'}
                        </h6>
                        <p class="mb-1">${alert.description || 'No description'}</p>
                        <small class="text-muted">
                            <i class="fas fa-clock me-1"></i>
                            ${moment(alert.created_at).fromNow()}
                        </small>
                    </div>
                    <span class="badge bg-${alert.priority === 'high' ? 'danger' : 'warning'} text-uppercase">
                        ${alert.priority || 'medium'}
                    </span>
                </div>
            </div>
        `).join('');
    }

    getHealthDistribution(healthData) {
        const distribution = { excellent: 0, good: 0, fair: 0, poor: 0 };
        
        Object.values(healthData).forEach(health => {
            if (typeof health === 'object' && health.health_grade) {
                distribution[health.health_grade] = (distribution[health.health_grade] || 0) + 1;
            }
        });

        return [distribution.excellent, distribution.good, distribution.fair, distribution.poor];
    }

    getSriLankanDistribution(contributionPatterns) {
        const domainBreakdown = contributionPatterns?.sri_lankan_projects?.domain_breakdown || {};
        
        return [
            domainBreakdown.healthcare?.repository_count || 0,
            domainBreakdown.governance?.repository_count || 0,
            domainBreakdown.education?.repository_count || 0,
            domainBreakdown.language?.repository_count || 0
        ];
    }

    async generateAssets() {
        console.log('📁 Generating assets...');
        
        // Create CSS directory
        const cssDir = path.join(this.outputDir, 'css');
        await fs.ensureDir(cssDir);
        
        // Create JS directory
        const jsDir = path.join(this.outputDir, 'js');
        await fs.ensureDir(jsDir);
        
        console.log('  ✅ Asset directories created');
    }

    async generateDataFiles(analyticsData) {
        console.log('📊 Generating data files...');
        
        // Create data directory
        const dataDir = path.join(this.outputDir, 'data');
        await fs.ensureDir(dataDir);
        
        // Write analytics data as JSON files for client-side access
        for (const [key, data] of Object.entries(analyticsData)) {
            const filename = `${key}.json`;
            const filePath = path.join(dataDir, filename);
            await fs.writeJson(filePath, data, { spaces: 2 });
        }
        
        console.log('  ✅ Data files generated');
    }
}

// Main execution
async function main() {
    try {
        const generator = new DashboardGenerator();
        await generator.generate();
        
        console.log('\n🎉 Dashboard generation completed successfully!');
        console.log('📍 Your dashboard is ready at: dashboard/dist/index.html');
        console.log('🌐 Deploy to GitHub Pages for live access');
        
    } catch (error) {
        console.error('\n❌ Dashboard generation failed:', error);
        process.exit(1);
    }
}

// Run if called directly
if (require.main === module) {
    main();
}

module.exports = DashboardGenerator;
