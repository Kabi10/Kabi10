# Next.js Application Template

## 📋 Project Setup Checklist

### Initial Setup
- [ ] Create project directory under `projects/web-applications/`
- [ ] Use kebab-case naming convention
- [ ] Initialize Next.js project: `npx create-next-app@latest`
- [ ] Configure TypeScript if not already enabled
- [ ] Set up Tailwind CSS for styling

### Project Structure
```
project-name/
├── src/
│   ├── app/                 # Next.js app router
│   ├── components/          # React components
│   │   ├── ui/             # Reusable UI components
│   │   └── features/       # Feature-specific components
│   ├── lib/                # Utilities and configurations
│   ├── hooks/              # Custom React hooks
│   ├── types/              # TypeScript type definitions
│   └── styles/             # Global styles
├── public/                 # Static assets
├── docs/                   # Project documentation
├── tests/                  # Test files
│   ├── __tests__/          # Unit tests
│   ├── e2e/               # End-to-end tests
│   └── __mocks__/         # Test mocks
└── scripts/               # Build and utility scripts
```

### Essential Files
- [ ] `README.md` with setup instructions
- [ ] `package.json` with proper scripts
- [ ] `tsconfig.json` for TypeScript configuration
- [ ] `tailwind.config.js` for styling
- [ ] `next.config.js` for Next.js configuration
- [ ] `.env.example` for environment variables
- [ ] `.gitignore` appropriate for Next.js

### Dependencies

#### Core Dependencies
```json
{
  "dependencies": {
    "next": "^14.0.0",
    "react": "^18.0.0",
    "react-dom": "^18.0.0",
    "typescript": "^5.0.0"
  }
}
```

#### Recommended Dependencies
```json
{
  "dependencies": {
    "@tailwindcss/forms": "^0.5.0",
    "@tailwindcss/typography": "^0.5.0",
    "clsx": "^2.0.0",
    "tailwind-merge": "^2.0.0",
    "lucide-react": "^0.400.0"
  },
  "devDependencies": {
    "@types/node": "^20.0.0",
    "@types/react": "^18.0.0",
    "@types/react-dom": "^18.0.0",
    "eslint": "^8.0.0",
    "eslint-config-next": "^14.0.0",
    "prettier": "^3.0.0",
    "tailwindcss": "^3.0.0"
  }
}
```

### Configuration Files

#### `package.json` Scripts
```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "lint:fix": "next lint --fix",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:e2e": "playwright test"
  }
}
```

#### `tsconfig.json` Base Configuration
```json
{
  "compilerOptions": {
    "target": "es5",
    "lib": ["dom", "dom.iterable", "es6"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

### Development Setup

#### Environment Variables
Create `.env.example`:
```env
# Database
DATABASE_URL="your-database-url"

# Authentication
NEXTAUTH_SECRET="your-secret"
NEXTAUTH_URL="http://localhost:3000"

# API Keys
API_KEY="your-api-key"
```

#### Shared Resources Integration
- [ ] Import shared UI components from `shared/common/`
- [ ] Use shared configuration templates
- [ ] Reference shared authentication utilities
- [ ] Utilize shared API clients when applicable

### Testing Setup

#### Jest Configuration
```javascript
// jest.config.js
const nextJest = require('next/jest')

const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
}

module.exports = createJestConfig(customJestConfig)
```

#### Playwright for E2E Testing
```javascript
// playwright.config.ts
import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
})
```

### Documentation Requirements

#### README.md Template
```markdown
# Project Name

Brief description of the project.

## Features
- List key features
- Include screenshots if applicable

## Tech Stack
- Next.js 14
- TypeScript
- Tailwind CSS
- [Other technologies]

## Getting Started
1. Install dependencies: `npm install`
2. Copy environment variables: `cp .env.example .env.local`
3. Run development server: `npm run dev`
4. Open [http://localhost:3000](http://localhost:3000)

## Scripts
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run test` - Run tests
- `npm run lint` - Run linting

## Deployment
[Deployment instructions]

## Contributing
[Contributing guidelines]
```

### Deployment Considerations

#### Vercel Deployment
- [ ] Configure `vercel.json` if needed
- [ ] Set up environment variables in Vercel dashboard
- [ ] Configure custom domains if applicable
- [ ] Set up preview deployments for branches

#### Docker Support (Optional)
```dockerfile
# Dockerfile
FROM node:18-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM node:18-alpine AS builder
WORKDIR /app
COPY . .
COPY --from=deps /app/node_modules ./node_modules
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app
ENV NODE_ENV production
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000
ENV PORT 3000

CMD ["node", "server.js"]
```

### Final Checklist
- [ ] All dependencies installed and working
- [ ] TypeScript configuration complete
- [ ] Linting and formatting configured
- [ ] Testing setup complete
- [ ] Documentation written
- [ ] Environment variables configured
- [ ] Deployment strategy planned
- [ ] Shared resources integrated
- [ ] Code quality tools configured

---

*This template is part of the workspace standardization initiative.*
*Customize as needed for specific project requirements.*
