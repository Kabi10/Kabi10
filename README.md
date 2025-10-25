# Jaffna Farmers Marketplace (யாழ்ப்பாணம் விவசாயிகள் சந்தை)

A Tamil-first, offline-first agricultural marketplace connecting farmers in Jaffna with local buyers.

---

## 👋 Welcome Interns!

**Congratulations on joining the Agrimarket project!** This is a real-world Android application that will help you learn modern Android development while building something meaningful for farmers in Jaffna, Sri Lanka.

### 🎓 What You'll Learn

By contributing to this project, you'll gain hands-on experience with:

- ✅ **Modern Android Development**: Jetpack Compose, Material Design 3, Kotlin
- ✅ **Clean Architecture**: MVVM pattern, Repository pattern, Dependency Injection (Hilt)
- ✅ **Offline-First Design**: Room database, data synchronization, background workers
- ✅ **Testing**: Unit tests, UI tests, MockK, Turbine
- ✅ **Real-World Features**: Authentication, image upload, maps, notifications
- ✅ **Trilingual Support**: English, Tamil (தமிழ்), Sinhala (සිංහල)
- ✅ **Professional Workflow**: Git, code reviews, documentation, issue tracking

### 🚀 Quick Start for Interns

**New to the project? Follow these steps:**

1. **Read the Documentation** (30 minutes)
   - 📖 [SETUP.md](SETUP.md) - Get your development environment ready
   - 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - Understand how the app is built
   - 🤝 [CONTRIBUTING.md](CONTRIBUTING.md) - Learn our development workflow
   - ⚠️ [KNOWN_ISSUES.md](KNOWN_ISSUES.md) - See current limitations

2. **Set Up Your Environment** (1-2 hours)
   - Install Android Studio
   - Clone the repository
   - Configure Firebase
   - Build and run the app

3. **Pick Your First Issue** (Start coding!)
   - Look for issues labeled [`good first issue`](../../issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22)
   - Start with something small to get familiar with the codebase
   - Don't hesitate to ask questions!

### 🌟 Recommended First Issues

We've prepared beginner-friendly issues to help you get started:

1. **[Easy]** [Implement Language Preference Persistence](../../issues) - Learn DataStore and state management (2-4 hours)
2. **[Medium]** [Add Unit Tests for ViewModels](../../issues) - Learn testing with JUnit and MockK (4-6 hours)
3. **[Hard]** [Add Search Filters to Listings](../../issues) - Build a complete feature with UI and logic (6-10 hours)

### 💡 Learning Resources

- **Android Basics**: [Android Developer Guides](https://developer.android.com/guide)
- **Jetpack Compose**: [Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- **Kotlin**: [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- **Git Workflow**: [GitHub Flow Guide](https://guides.github.com/introduction/flow/)

### 🤝 Getting Help

**Stuck? Don't worry, we're here to help!**

- 💬 **Ask Questions**: Comment on the issue you're working on
- 📚 **Check Documentation**: Most answers are in `ARCHITECTURE.md` or `SETUP.md`
- 🐛 **Report Problems**: Create a new issue if you find bugs
- 🔍 **Search First**: Check if someone else had the same question

### 📊 Project Status

- **Build Status**: ✅ Passing
- **Test Coverage**: ~0% (Help us improve this!)
- **Code Quality**: Good (MVVM architecture, clean code)
- **Documentation**: Comprehensive
- **Intern Readiness**: 9.5/10

### 🎯 Project Goals

This app aims to:
- 🌾 Help farmers in Jaffna sell their produce directly to buyers
- 📱 Work offline in areas with poor internet connectivity
- 🌍 Support Tamil, English, and Sinhala languages
- 💰 Enable both cash and digital transactions
- 📊 Provide market price insights to farmers

**Your contributions will directly impact real farmers!** 🚜✨

---

## 🌾 Overview

This is a complete mobile-first marketplace application built to serve the agricultural community in Jaffna, Sri Lanka. The platform enables farmers to list their produce and connect directly with local buyers, supporting both cash transactions and future digital payment integration.

**Key Design Principles:**
- **Tamil-first**: Primary interface in Tamil with English and Sinhala support
- **Offline-first**: Full functionality without internet connectivity
- **Mobile-first**: Optimized for low-end Android devices (min Android 7.0)
- **Simple verification**: OTP-based authentication for Sri Lankan phone numbers

## 🚀 Technology Stack

- **Frontend**: Android (Jetpack Compose + Material Design 3)
- **Backend**: Serverless Node.js functions (Vercel)
- **Database**: Supabase (PostgreSQL)
- **Authentication**: Custom OTP system
- **Deployment**: Vercel (backend) + Manual APK distribution

## 🏗️ Quick Start

### Prerequisites

- **Android Development**: Android Studio, JDK 11+
- **Backend Development**: Node.js 18+, npm 9+
- **Database**: Supabase CLI
- **Deployment**: Vercel CLI

### Building the Android App

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Build release APK
./gradlew assembleRelease
```

### Backend Development

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Start local development server
npm run dev

# Run tests
npm test
```

### Database Setup

```bash
# Initialize Supabase project
npx supabase init

# Start local Supabase stack
npx supabase start

# Apply database migrations
npx supabase db push

# View local dashboard
npx supabase status
```

### Deployment

```bash
# Deploy backend to Vercel
cd backend
vercel deploy --prod

# Deploy database migrations to production
npx supabase db push --linked

# Link local project to remote Supabase project
npx supabase link --project-ref YOUR_PROJECT_REF
```

## 📱 Key Features

### Core Functionality
- **Product Listings**: Create, edit, and manage agricultural product listings
- **Market Prices**: Real-time commodity pricing information
- **Transaction Management**: Complete transaction lifecycle tracking
- **User Profiles**: Farmer and buyer profile management

### Technical Features
- **Trilingual Support**: English, Tamil (தமிழ்), and Sinhala (සිංහල) interfaces
- **OTP Authentication**: Secure phone number verification for Sri Lankan numbers
- **Offline-First Architecture**: Full app functionality without internet connection
- **Real-time Sync**: Automatic data synchronization when connectivity is restored
- **Conflict Resolution**: Smart handling of offline data conflicts
- **Dark Theme**: Material Design 3 dark theme support
- **Responsive Design**: Optimized for various screen sizes and orientations

### User Experience
- **Simple Navigation**: Intuitive bottom navigation with clear iconography
- **Language Toggle**: Easy switching between supported languages
- **Accessibility**: Screen reader support and high contrast options
- **Performance**: Optimized for low-end Android devices

## 🏛️ Architecture Overview

### Frontend (Android App)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room for local storage with offline sync
- **Networking**: Retrofit with OkHttp for API communication
- **Dependency Injection**: Hilt for dependency management

### Backend (Serverless API)
- **Runtime**: Node.js 18+ serverless functions
- **Framework**: Express.js with middleware for CORS, rate limiting
- **Database**: Supabase (PostgreSQL) with Row Level Security
- **Authentication**: Custom JWT-based OTP verification
- **File Storage**: Supabase Storage for product images

### Database Schema
- **Users**: Farmer and buyer profiles with phone verification
- **Listings**: Product listings with categories, pricing, and availability
- **Transactions**: Order tracking and payment status
- **Activities**: User activity feed and notifications
- **Market Prices**: Real-time commodity pricing data

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📚 Documentation

For comprehensive project specifications, development guidelines, and implementation details, see:
- [Complete Project Blueprint](jaffna_farmers_marketplace_full_blueprint_developer_prompt.md)

## 🤝 Contributing

This is a focused agricultural marketplace project serving the Jaffna farming community. The codebase prioritizes hands-on development and testing over extensive documentation.

### Development Workflow
1. Build and test Android app on physical devices
2. Verify backend API functionality with real data
3. Test offline sync and conflict resolution
4. Validate trilingual interface across all features
5. Ensure OTP authentication works with Sri Lankan phone numbers

For detailed development guidelines and project requirements, refer to the complete blueprint documentation.
