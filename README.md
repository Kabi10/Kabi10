# Development Workspace

This is a well-organized development workspace containing multiple projects across different technologies and domains.

## 📁 Directory Structure

```
Dev/
├── projects/                    # All active development projects
│   ├── web-applications/        # Web development projects
│   │   ├── rate-my-employer/    # Main RME project (consolidated)
│   │   ├── ecommerce/           # E-commerce platform
│   │   ├── deepseek-conversation/ # AI conversation interface
│   │   ├── dream-weaver/        # Dream analysis application
│   │   ├── questpdf/            # PDF processing tools
│   │   └── web-ui/              # Browser automation UI
│   ├── data-science/            # Data science and ML projects
│   │   ├── kaggle/              # Kaggle competitions
│   │   └── notebooks/           # Jupyter notebooks
│   ├── utilities/               # Standalone utility projects
│   │   ├── album-summarizer/    # Photo album management
│   │   ├── html-parser/         # HTML processing tools
│   │   ├── telegram-tools/      # Telegram bot utilities
│   │   ├── excel-tools/         # Excel analysis tools
│   │   └── python-scripts/      # General Python utilities
│   └── mobile/                  # Mobile development projects
│       ├── android/             # Android applications
│       └── flutter/             # Flutter applications
├── shared/                      # Shared resources across projects
│   └── common/                  # Common libraries and configurations
│       ├── api/                 # Shared API implementations
│       ├── assets/              # Images, fonts, and media files
│       ├── auth/                # Authentication utilities
│       ├── clients/             # API clients
│       ├── config/              # Configuration templates
│       ├── docs/                # Shared documentation
│       ├── libs/                # Common libraries
│       └── utils/               # Utility functions
├── tools/                       # Development tools and utilities
│   ├── rules/                   # Coding rules and guidelines
│   ├── dependency-guides/       # Dependency management guides
│   ├── ideas/                   # Project ideas and planning
│   └── scripts/                 # Utility scripts
└── archive/                     # Archived and legacy projects
    ├── ratemyemployer-legacy/   # Legacy RME version
    └── work-legacy/             # Old work projects
```

## 🚀 Key Features

- **Organized Structure**: Projects categorized by type and purpose
- **Consolidated RME**: All Rate My Employer variants merged into single project
- **Shared Resources**: Common libraries and configurations for code reuse
- **Clean Separation**: Active projects separated from archived content
- **Consistent Naming**: Kebab-case naming convention throughout

## 📋 Project Categories

### Web Applications
- **Rate My Employer**: Workplace review platform with admin tools
- **E-commerce**: Online shopping platform
- **DeepSeek Conversation**: AI-powered conversation interface
- **Dream Weaver**: Dream analysis and visualization
- **QuestPDF**: PDF generation and processing
- **Web UI**: Browser automation interface

### Data Science
- **Kaggle**: Competition submissions and experiments
- **Notebooks**: Research and analysis notebooks

### Utilities
- **Album Summarizer**: Photo management and organization
- **HTML Parser**: Web scraping and parsing tools
- **Telegram Tools**: Bot development and automation
- **Excel Tools**: Data analysis and reporting
- **Python Scripts**: General-purpose utilities

### Mobile
- **Android**: Native Android applications
- **Flutter**: Cross-platform mobile apps

## 🛠️ Development Guidelines

### Getting Started
1. Navigate to the appropriate project directory under `projects/`
2. Follow the project-specific README for setup instructions
3. Use shared resources from `shared/common/` when possible

### Project Structure Standards
- Use **kebab-case** for directory names
- Include comprehensive README.md in each project
- Follow the established patterns in `tools/rules/`
- Utilize shared configurations from `shared/common/config/`

### Shared Resources
The `shared/common/` directory contains reusable components:
- **API clients**: Pre-configured HTTP clients
- **Authentication**: OAuth and JWT utilities
- **Configuration**: Environment templates and settings
- **Documentation**: Shared guides and standards

### Development Tools
- **Rules**: Coding standards and best practices
- **Scripts**: Automation and utility scripts
- **Ideas**: Project planning and brainstorming
- **Dependency Guides**: Package management guidelines

## 📦 Technology Stack

### Web Technologies
- **Frontend**: React, Next.js, TypeScript, Tailwind CSS
- **Backend**: Node.js, Python, Express
- **Databases**: Supabase, PostgreSQL, SQLite
- **Deployment**: Vercel, Docker

### Data Science
- **Languages**: Python, R
- **Libraries**: Pandas, NumPy, Scikit-learn, TensorFlow
- **Notebooks**: Jupyter, Google Colab
- **Platforms**: Kaggle, Google Cloud

### Mobile Development
- **Android**: Kotlin, Java
- **Flutter**: Dart
- **Tools**: Android Studio, VS Code

## 🔧 Maintenance

### Regular Tasks
- Clean build artifacts: `node_modules/`, `.next/`, `dist/`
- Update dependencies across projects
- Archive completed or deprecated projects
- Review and update shared configurations

### Backup Strategy
- Critical projects backed up regularly
- Archive directory for legacy content
- Version control for all active projects

## 📚 Documentation

Each project contains its own documentation. For workspace-wide information:
- **Setup guides**: `shared/common/docs/`
- **Coding standards**: `tools/rules/`
- **Project ideas**: `tools/ideas/`

## 🤝 Contributing

1. Follow the established directory structure
2. Use shared resources when applicable
3. Document new projects thoroughly
4. Follow naming conventions consistently

---

*Last updated: August 2025*
*Workspace reorganized for improved maintainability and developer productivity*

## Directory Descriptions

### Project Directories

- **Python/** - Contains all Python-related projects, scripts, and applications
- **Android/** - Houses Android development projects and related mobile applications
- **Websites/** - Contains web development projects, including HTML, CSS, and JavaScript

### Common Resources

The `Common/` directory contains shared resources that can be used across different projects:

- **api/** - Contains shared API implementations, clients, and configurations
  - API clients for different services
  - Shared API utilities and helpers
  - Common API authentication and authorization
  - API documentation and specifications
- **assets/** - Store shared media files like images, fonts, icons, etc.
- **docs/** - Contains documentation that applies to multiple projects
- **libs/** - Houses shared libraries, utilities, and reusable code
- **config/** - Stores configuration files that might be needed across projects

## Getting Started

1. Choose the appropriate project directory based on your development needs
2. Create a new project folder within that directory
3. Use resources from the Common directory as needed
4. Follow the specific README guidelines within each project

### Using Common APIs

To use shared APIs across projects:
1. Import the required API client from `Common/api`
2. Configure API credentials in `Common/config`
3. Use shared authentication mechanisms from `Common/api/auth`
4. Follow API documentation in `Common/api/docs`

## Best Practices

1. Keep project-specific files within their respective project directories
2. Use the Common directory for truly shared resources only
3. Maintain clear documentation for each project
4. Follow consistent naming conventions across projects
5. Keep API credentials secure and never commit them to version control I am ready to edit files. Please provide the files you want to edit.
