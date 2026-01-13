# Jaffna Farmers Marketplace Backend API

A comprehensive Node.js backend API for the Jaffna Farmers Marketplace - a Tamil-first, offline-first agricultural marketplace connecting farmers in Jaffna with local buyers.

## 🌾 Overview

This backend provides REST API endpoints that seamlessly integrate with the Android MVP application, supporting:

- **OTP-based Authentication** for Sri Lankan phone numbers
- **Offline-first Synchronization** with conflict resolution
- **Listing Management** for agricultural products
- **Transaction Processing** with status tracking
- **Tamil-first Design** with bilingual support
- **SMS Integration** with local Sri Lankan providers

## 🚀 Quick Start

### Prerequisites

- Node.js 18+ and npm 9+
- PostgreSQL 12+
- Redis (optional, for caching)

### Installation

1. **Clone and setup:**
```bash
cd Agrimarket/backend
npm install
```

2. **Environment configuration:**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Database setup:**
```bash
# Create database
createdb jaffna_marketplace

# Run migrations
npm run migrate

# Seed initial data (optional)
npm run seed
```

4. **Start development server:**
```bash
npm run dev
```

The API will be available at `http://localhost:3000`

## 📱 Android Integration

This backend is specifically designed to integrate with the Jaffna Marketplace Android app:

### Authentication Flow
- Matches `AuthApiService.kt` contracts exactly
- Supports `SendOtpRequest/Response` and `LoginRequest/Response` models
- JWT token management with refresh token rotation

### Sync System
- Compatible with `SyncManager.kt` and `SyncApiService.kt`
- Handles `LocalOp` queue system for offline operations
- Implements delta sync with timestamp-based change tracking

### Data Models
- Database schema mirrors Android Room entities exactly
- JSON serialization compatible with Moshi adapters
- Supports all crop types, locations, and business rules from Android app

## 🏗️ Architecture

### Technology Stack
- **Framework:** Express.js with Node.js
- **Database:** PostgreSQL with connection pooling
- **Authentication:** JWT with bcrypt
- **SMS:** Dialog Ideamart, Mobitel mSpace, Twilio fallback
- **Logging:** Winston with structured logging
- **Validation:** Joi and express-validator
- **Security:** Helmet, CORS, rate limiting

### Project Structure
```
src/
├── routes/          # API route handlers
├── middleware/      # Authentication, error handling
├── services/        # Business logic (SMS, etc.)
├── database/        # Database connection and schema
├── utils/           # Helper functions and utilities
└── server.js        # Application entry point
```

## 🔐 Authentication

### OTP-based Phone Authentication

**Send OTP:**
```http
POST /api/v1/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "+94771234567"
}
```

**Verify OTP:**
```http
POST /api/v1/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+94771234567",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid",
    "phoneNumber": "+94771234567",
    "userType": "FARMER",
    "verified": true
  }
}
```

## 📊 API Endpoints

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/send-otp` | Send OTP to phone |
| POST | `/api/v1/auth/verify-otp` | Verify OTP and login |
| GET | `/api/v1/users/profile` | Get user profile |
| PUT | `/api/v1/users/profile` | Update user profile |
| GET | `/api/v1/listings` | Get listings with filters |
| POST | `/api/v1/listings` | Create new listing |
| GET | `/api/v1/transactions` | Get user transactions |
| POST | `/api/v1/transactions` | Create transaction |
| POST | `/api/v1/sync/operations` | Sync offline operations |

### Health Checks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Basic health check |
| GET | `/health/detailed` | Detailed system health |
| GET | `/health/database` | Database connectivity |
| GET | `/health/sms` | SMS service status |

## 🔄 Offline Sync System

The sync system handles offline operations from the Android app:

### Sync Operations
```http
POST /api/v1/sync/operations
Authorization: Bearer <token>
Content-Type: application/json

{
  "operations": [
    {
      "opId": "uuid",
      "type": "CREATE_LISTING",
      "payload": { ... },
      "clientTs": "2024-01-01T00:00:00Z",
      "clientId": "uuid"
    }
  ],
  "lastSyncAt": "2024-01-01T00:00:00Z"
}
```

### Conflict Resolution
- Server-wins strategy for conflicts
- Returns conflicted operations with server data
- Client can resolve conflicts and retry

## 📱 SMS Integration

### SMS_MODE Configuration

The `SMS_MODE` environment variable controls OTP delivery:

| Mode | Description | Use Case |
|------|-------------|----------|
| `mock` | Logs OTP to console, does NOT send SMS | Development/testing |
| `dialog` | Send via Dialog Ideamart API | Production (Sri Lanka) |
| `mobitel` | Send via Mobitel mSpace API | Production (Sri Lanka) |
| `twilio` | Send via Twilio API | International fallback |

**Mock Mode Example Output:**
```
============================================================
📱 MOCK SMS - OTP CODE (NOT SENT)
============================================================
Phone: +94771234567
OTP:   482615
============================================================
```

### OTP Security Features
- **Max 3 attempts** per OTP before invalidation
- **60-second cooldown** between OTP requests
- **5-minute expiry** for each OTP
- **No master OTP bypass** in any environment

### Provider Configuration

**Dialog Ideamart (Primary Sri Lankan):**
```env
SMS_MODE=dialog
DIALOG_API_KEY=your_api_key
DIALOG_API_SECRET=your_api_secret
DIALOG_SENDER_ID=JaffnaFarm
```

**Mobitel mSpace (Alternative):**
```env
SMS_MODE=mobitel
MOBITEL_USERNAME=your_username
MOBITEL_PASSWORD=your_password
```

**Twilio (International):**
```env
SMS_MODE=twilio
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
TWILIO_PHONE_NUMBER=+1234567890
```

## 🚀 Deployment

### Production Environment

**Recommended Hosting:**
- **DigitalOcean Droplets** (Singapore region for low latency to Sri Lanka)
- **AWS Asia Pacific (Mumbai)** for enterprise scale
- **Local Sri Lankan providers** for data sovereignty

### Docker Deployment
```bash
# Build image
docker build -t jaffna-marketplace-api .

# Run with environment
docker run -d \
  --name jaffna-api \
  -p 3000:3000 \
  --env-file .env \
  jaffna-marketplace-api
```

### Environment Variables
```env
# Required
NODE_ENV=production
PORT=3000
DB_HOST=localhost
DB_NAME=jaffna_marketplace
DB_USER=postgres
DB_PASSWORD=secure_password
JWT_SECRET=your_jwt_secret

# SMS (choose one)
SMS_PROVIDER=dialog
DIALOG_API_KEY=your_key
DIALOG_API_SECRET=your_secret
```

## 💰 Cost Analysis

### Monthly Costs (Estimated)

**Small Scale (100 users, 1000 SMS/month):**
- DigitalOcean Droplet (2GB): $12/month
- PostgreSQL Database: $15/month
- SMS (Dialog): $5/month
- **Total: ~$32/month**

**Medium Scale (1000 users, 10000 SMS/month):**
- DigitalOcean Droplet (4GB): $24/month
- Managed PostgreSQL: $30/month
- SMS (Dialog): $50/month
- **Total: ~$104/month**

**Large Scale (10000 users, 100000 SMS/month):**
- AWS EC2 (t3.large): $60/month
- RDS PostgreSQL: $100/month
- SMS (Dialog): $500/month
- Load Balancer: $20/month
- **Total: ~$680/month**

## 🔧 Development

### Scripts
```bash
npm run dev          # Start development server
npm run start        # Start production server
npm run test         # Run tests
npm run migrate      # Run database migrations
npm run seed         # Seed database
npm run lint         # Lint code
```

### Testing
```bash
# Run all tests
npm test

# Run with coverage
npm run test:coverage

# Run specific test file
npm test -- auth.test.js
```

## 🛡️ Security

- **Rate Limiting:** 100 requests per 15 minutes
- **OTP Rate Limiting:** 5 OTP requests per 15 minutes
- **JWT Security:** HS256 with secure secrets
- **Input Validation:** Comprehensive validation on all endpoints
- **SQL Injection Protection:** Parameterized queries
- **CORS:** Configured for production domains
- **Helmet:** Security headers enabled

## 📈 Monitoring

### Health Checks
- `/health` - Basic health status
- `/health/detailed` - Full system status
- `/health/database` - Database connectivity
- `/health/readiness` - Kubernetes readiness probe
- `/health/liveness` - Kubernetes liveness probe

### Logging
- Structured JSON logging with Winston
- Request/response logging
- Error tracking with stack traces
- Business event logging
- Security event logging

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Email: support@jaffna-marketplace.com
- Documentation: [API Docs](https://api.jaffna-marketplace.com/docs)

---

**Built with ❤️ for the farming community of Jaffna, Sri Lanka**
