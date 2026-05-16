# Development Workspace Guide

## 📋 Overview

This guide provides comprehensive information about the development workspace structure, conventions, and best practices.

## 🏗️ Architecture

### Directory Structure
```
Dev/
├── projects/           # All active development projects
├── shared/            # Shared resources and utilities
├── tools/             # Development tools and scripts
└── archive/           # Legacy and archived projects
```

### Project Categories

#### Web Applications (`projects/web-applications/`)
- **rate-my-employer**: Workplace review platform (consolidated from 3 variants)
- **ecommerce**: E-commerce platform with Prisma/Next.js
- **deepseek-conversation**: AI conversation interface
- **dream-weaver**: Dream analysis application
- **questpdf**: PDF processing tools
- **web-ui**: Browser automation interface

#### Data Science (`projects/data-science/`)
- **kaggle**: Competition submissions and experiments
- **notebooks**: Research and analysis notebooks

#### Utilities (`projects/utilities/`)
- **album-summarizer**: Photo management with cloud integration
- **html-parser**: Web scraping and parsing tools
- **telegram-tools**: Bot development and automation
- **excel-tools**: Data analysis and reporting
- **python-scripts**: General-purpose utilities

#### Mobile (`projects/mobile/`)
- **android**: Native Android applications
- **flutter**: Cross-platform mobile apps

## 🛠️ Development Standards

### Naming Conventions
- **Directories**: kebab-case (e.g., `rate-my-employer`)
- **Files**: Follow language conventions
- **Branches**: feature/fix/hotfix prefixes
- **Commits**: Conventional commit format

### Project Structure
Each project should include:
- `README.md` with setup instructions
- `package.json` or equivalent dependency file
- `.gitignore` appropriate for the technology
- Documentation in `docs/` subdirectory
- Tests in appropriate test directories

### Shared Resources Usage
- Use `shared/common/` for reusable components
- Reference shared configurations when possible
- Contribute back to shared resources when creating reusable code

## 🔧 Tools and Scripts

### Available Tools
- **Rules**: Coding standards in `tools/rules/`
- **Scripts**: Utility scripts in `tools/scripts/`
- **Ideas**: Project planning in `tools/ideas/`
- **Dependency Guides**: Package management in `tools/dependency-guides/`

### Common Scripts
- Build artifact cleanup
- Dependency updates
- Code formatting and linting
- Project initialization templates

## 📦 Technology Stack

### Frontend Technologies
- React, Next.js, TypeScript
- Tailwind CSS, Styled Components
- State management: Redux, Zustand
- Testing: Jest, Playwright, Vitest

### Backend Technologies
- Node.js, Express, Python
- Databases: PostgreSQL, Supabase, SQLite
- Authentication: Supabase Auth, JWT
- APIs: REST, GraphQL

### Development Tools
- Package Managers: npm, yarn, pnpm
- Build Tools: Vite, Webpack, Next.js
- Code Quality: ESLint, Prettier, TypeScript
- Version Control: Git with conventional commits

## 🚀 Getting Started

### New Project Setup
1. Choose appropriate category under `projects/`
2. Use kebab-case naming convention
3. Copy relevant template from `shared/templates/`
4. Follow project-specific setup in README
5. Configure shared resources as needed

### Existing Project Development
1. Navigate to project directory
2. Install dependencies: `npm install` or equivalent
3. Review project README for specific instructions
4. Use shared configurations when available

## 📚 Documentation Standards

### Project Documentation
- Comprehensive README with setup instructions
- API documentation for backend services
- Component documentation for frontend projects
- Deployment and configuration guides

### Shared Documentation
- Workspace-wide standards in `shared/docs/`
- Reusable patterns and templates
- Best practices and conventions
- Troubleshooting guides

## 🔄 Maintenance

### Regular Tasks
- Clean build artifacts across projects
- Update dependencies and security patches
- Review and archive completed projects
- Update shared resources and templates

### Backup and Archive
- Active projects under version control
- Legacy projects in `archive/` directory
- Regular backup of critical configurations
- Documentation of major changes

## 🤝 Contributing

### Code Contributions
1. Follow established naming conventions
2. Use shared resources when applicable
3. Document new features thoroughly
4. Write tests for new functionality
5. Follow commit message conventions

### Shared Resource Contributions
- Add reusable components to `shared/common/`
- Update templates in `shared/templates/`
- Improve documentation in `shared/docs/`
- Share useful scripts in `tools/scripts/`

## 📞 Support

### Getting Help
1. Check project-specific README files
2. Review shared documentation
3. Check troubleshooting guides
4. Consult team members or documentation

### Reporting Issues
- Document steps to reproduce
- Include relevant error messages
- Specify environment and versions
- Suggest potential solutions if known

---

*This guide is maintained as part of the workspace reorganization initiative.*
*Last updated: August 2025*
