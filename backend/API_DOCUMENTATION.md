# Jaffna Farmers Marketplace API Documentation

Complete API documentation for the Jaffna Farmers Marketplace backend, designed to integrate seamlessly with the Android MVP application.

## 🔗 Base URL

- **Development:** `http://localhost:3000`
- **Production:** `https://api.jaffna-marketplace.com`

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication. Most endpoints require a valid JWT token in the Authorization header.

### Header Format
```
Authorization: Bearer <jwt_token>
```

### Token Lifecycle
- **Access Token:** 24 hours
- **Refresh Token:** 7 days

## 📱 Authentication Endpoints

### Send OTP
Send OTP to Sri Lankan phone number for verification.

**Endpoint:** `POST /api/v1/auth/send-otp`

**Request Body:**
```json
{
  "phoneNumber": "+94771234567"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

**Response (Development Mode):**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "otp": "123456"
}
```

**Rate Limiting:** 5 requests per 15 minutes per phone number

---

### Verify OTP
Verify OTP and authenticate user (login/register).

**Endpoint:** `POST /api/v1/auth/verify-otp`

**Request Body:**
```json
{
  "phoneNumber": "+94771234567",
  "otp": "123456"
}
```

**Response (Success):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "phoneNumber": "+94771234567",
    "name": null,
    "userType": "BUYER",
    "location": null,
    "verified": true,
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### Refresh Token
Get new access token using refresh token.

**Endpoint:** `POST /api/v1/auth/refresh-token`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 👤 User Endpoints

### Get User Profile
Get current user's profile information.

**Endpoint:** `GET /api/v1/users/profile`
**Authentication:** Required

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "phoneNumber": "+94771234567",
    "name": "Ravi Farmer",
    "userType": "FARMER",
    "location": "Jaffna North",
    "verified": true,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### Update User Profile
Update current user's profile information.

**Endpoint:** `PUT /api/v1/users/profile`
**Authentication:** Required

**Request Body:**
```json
{
  "name": "Ravi Farmer",
  "location": "Jaffna North",
  "userType": "FARMER"
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "phoneNumber": "+94771234567",
    "name": "Ravi Farmer",
    "userType": "FARMER",
    "location": "Jaffna North",
    "verified": true,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T12:00:00.000Z"
  }
}
```

---

### Get User Statistics
Get dashboard statistics for current user.

**Endpoint:** `GET /api/v1/users/stats/dashboard`
**Authentication:** Required

**Response (Farmer):**
```json
{
  "success": true,
  "data": {
    "listings": {
      "total_listings": "5",
      "active_listings": "3",
      "available_listings": "2"
    },
    "transactions": {
      "total_transactions": "12",
      "pending_transactions": "2",
      "completed_transactions": "8",
      "total_earnings": "15000.00"
    }
  }
}
```

**Response (Buyer):**
```json
{
  "success": true,
  "data": {
    "purchases": {
      "total_purchases": "8",
      "pending_purchases": "1",
      "completed_purchases": "6",
      "total_spent": "5000.00"
    }
  }
}
```

## 🌾 Listing Endpoints

### Get Listings
Get all active listings with optional filtering.

**Endpoint:** `GET /api/v1/listings`
**Authentication:** Required

**Query Parameters:**
- `cropType` (string): Filter by crop type
- `location` (string): Filter by location (partial match)
- `minPrice` (number): Minimum price per unit
- `maxPrice` (number): Maximum price per unit
- `quality` (string): A, B, or C
- `farmerId` (string): Filter by farmer ID
- `page` (number): Page number (default: 1)
- `limit` (number): Items per page (default: 20)
- `sortBy` (string): Sort field (default: created_at)
- `sortOrder` (string): ASC or DESC (default: DESC)

**Example Request:**
```
GET /api/v1/listings?cropType=rice&location=Jaffna&minPrice=100&page=1&limit=10
```

**Response (Success):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "farmerId": "550e8400-e29b-41d4-a716-446655440001",
      "cropType": "rice",
      "quantity": 100.0,
      "unit": "kg",
      "pricePerUnit": 150.0,
      "quality": "A",
      "location": "Jaffna North",
      "pickupLocations": ["Farm Gate", "Jaffna Market"],
      "availableFrom": "2024-01-01",
      "availableUntil": "2024-01-31",
      "description": "High quality red rice, organically grown",
      "images": [],
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z",
      "farmerName": "Ravi Farmer",
      "farmerContact": "+94771234567"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 25,
    "totalPages": 3
  }
}
```

---

### Get Listing Details
Get detailed information about a specific listing.

**Endpoint:** `GET /api/v1/listings/{id}`
**Authentication:** Required

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "farmerId": "550e8400-e29b-41d4-a716-446655440001",
    "cropType": "rice",
    "quantity": 100.0,
    "unit": "kg",
    "pricePerUnit": 150.0,
    "quality": "A",
    "location": "Jaffna North",
    "pickupLocations": ["Farm Gate", "Jaffna Market"],
    "availableFrom": "2024-01-01",
    "availableUntil": "2024-01-31",
    "description": "High quality red rice, organically grown",
    "images": [],
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z",
    "viewCount": 15,
    "farmer": {
      "name": "Ravi Farmer",
      "contact": "+94771234567",
      "location": "Jaffna North"
    }
  }
}
```

---

### Create Listing
Create a new listing (farmers only).

**Endpoint:** `POST /api/v1/listings`
**Authentication:** Required (Farmer only)

**Request Body:**
```json
{
  "cropType": "rice",
  "quantity": 100.0,
  "unit": "kg",
  "pricePerUnit": 150.0,
  "quality": "A",
  "location": "Jaffna North",
  "pickupLocations": ["Farm Gate", "Jaffna Market"],
  "availableFrom": "2024-01-01",
  "availableUntil": "2024-01-31",
  "description": "High quality red rice, organically grown",
  "images": []
}
```

**Valid Crop Types:**
`rice`, `coconut`, `banana`, `mango`, `papaya`, `pineapple`, `tomato`, `onion`, `potato`, `carrot`, `cabbage`, `beans`, `okra`, `eggplant`, `chili`, `curry_leaves`, `coriander`, `mint`, `lemongrass`, `ginger`, `turmeric`, `other`

**Valid Units:**
`kg`, `g`, `lb`, `piece`, `bunch`, `bag`

**Valid Quality:**
`A`, `B`, `C`

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "farmerId": "550e8400-e29b-41d4-a716-446655440001",
    "cropType": "rice",
    "quantity": 100.0,
    "unit": "kg",
    "pricePerUnit": 150.0,
    "quality": "A",
    "location": "Jaffna North",
    "pickupLocations": ["Farm Gate", "Jaffna Market"],
    "availableFrom": "2024-01-01",
    "availableUntil": "2024-01-31",
    "description": "High quality red rice, organically grown",
    "images": [],
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

## 💰 Transaction Endpoints

### Get Transactions
Get user's transactions with filtering.

**Endpoint:** `GET /api/v1/transactions`
**Authentication:** Required

**Query Parameters:**
- `status` (string): Filter by status
- `role` (string): 'farmer' or 'buyer' to filter by role
- `page` (number): Page number (default: 1)
- `limit` (number): Items per page (default: 20)
- `sortBy` (string): Sort field (default: created_at)
- `sortOrder` (string): ASC or DESC (default: DESC)

**Response (Success):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "listingId": "550e8400-e29b-41d4-a716-446655440001",
      "farmerId": "550e8400-e29b-41d4-a716-446655440002",
      "buyerId": "550e8400-e29b-41d4-a716-446655440003",
      "quantity": 10.0,
      "totalAmount": 1500.0,
      "pickupLocation": "Jaffna Market",
      "pickupDate": "2024-01-03",
      "status": "PENDING",
      "paymentMethod": "CASH",
      "notes": "Please call before pickup",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z",
      "listing": {
        "cropType": "rice",
        "unit": "kg",
        "pricePerUnit": 150.0
      },
      "farmer": {
        "name": "Ravi Farmer",
        "contact": "+94771234567"
      },
      "buyer": {
        "name": "Priya Buyer",
        "contact": "+94771234568"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 5,
    "totalPages": 1
  }
}
```

---

### Create Transaction
Create a new transaction (place order).

**Endpoint:** `POST /api/v1/transactions`
**Authentication:** Required

**Request Body:**
```json
{
  "listingId": "550e8400-e29b-41d4-a716-446655440001",
  "quantity": 10.0,
  "totalAmount": 1500.0,
  "pickupLocation": "Jaffna Market",
  "pickupDate": "2024-01-03",
  "buyerContact": "+94771234568",
  "notes": "Please call before pickup"
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "listingId": "550e8400-e29b-41d4-a716-446655440001",
    "farmerId": "550e8400-e29b-41d4-a716-446655440002",
    "buyerId": "550e8400-e29b-41d4-a716-446655440003",
    "quantity": 10.0,
    "totalAmount": 1500.0,
    "pickupLocation": "Jaffna Market",
    "pickupDate": "2024-01-03",
    "status": "PENDING",
    "paymentMethod": "CASH",
    "notes": "Please call before pickup",
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### Update Transaction Status
Update the status of a transaction.

**Endpoint:** `PATCH /api/v1/transactions/{id}/status`
**Authentication:** Required (Farmer or Buyer involved in transaction)

**Request Body:**
```json
{
  "status": "CONFIRMED",
  "notes": "Confirmed for pickup tomorrow"
}
```

**Valid Status Transitions:**
- `PENDING` → `CONFIRMED`, `CANCELLED`
- `CONFIRMED` → `IN_PROGRESS`, `CANCELLED`
- `IN_PROGRESS` → `COMPLETED`, `CANCELLED`
- `COMPLETED` → (no transitions)
- `CANCELLED` → (no transitions)

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "CONFIRMED",
    "updatedAt": "2024-01-01T12:00:00.000Z",
    "notes": "Confirmed for pickup tomorrow"
  }
}
```

## 🔄 Sync Endpoints

### Sync Operations
Process batch of offline operations from client.

**Endpoint:** `POST /api/v1/sync/operations`
**Authentication:** Required

**Request Body:**
```json
{
  "operations": [
    {
      "opId": "550e8400-e29b-41d4-a716-446655440000",
      "type": "CREATE_LISTING",
      "payload": {
        "cropType": "rice",
        "quantity": 100.0,
        "unit": "kg",
        "pricePerUnit": 150.0,
        "quality": "A",
        "location": "Jaffna North",
        "pickupLocations": ["Farm Gate"],
        "availableFrom": "2024-01-01",
        "availableUntil": "2024-01-31",
        "description": "High quality rice"
      },
      "clientTs": "2024-01-01T00:00:00.000Z",
      "clientId": "550e8400-e29b-41d4-a716-446655440001"
    }
  ],
  "lastSyncAt": "2024-01-01T00:00:00.000Z"
}
```

**Valid Operation Types:**
- `CREATE_LISTING`
- `UPDATE_LISTING`
- `DELETE_LISTING`
- `CREATE_TRANSACTION`
- `UPDATE_TRANSACTION`
- `UPDATE_USER`

**Response (Success):**
```json
{
  "success": true,
  "appliedOps": ["550e8400-e29b-41d4-a716-446655440000"],
  "conflicts": [],
  "errors": [],
  "serverData": {
    "users": [],
    "listings": [],
    "transactions": []
  },
  "serverTimestamp": "2024-01-01T12:00:00.000Z"
}
```

## 🏥 Health Check Endpoints

### Basic Health Check
**Endpoint:** `GET /health`

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "uptime": 3600,
  "environment": "production",
  "version": "1.0.0",
  "responseTime": 5
}
```

### Detailed Health Check
**Endpoint:** `GET /health/detailed`

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "uptime": 3600,
  "environment": "production",
  "version": "1.0.0",
  "services": {
    "database": {
      "status": "healthy",
      "timestamp": "2024-01-01T12:00:00.000Z",
      "pool": {
        "totalCount": 10,
        "idleCount": 8,
        "waitingCount": 0
      }
    },
    "sms": {
      "status": "healthy",
      "provider": "dialog",
      "mockMode": false,
      "configured": true
    }
  },
  "system": {
    "memory": {
      "rss": 52428800,
      "heapTotal": 29360128,
      "heapUsed": 20132096
    },
    "platform": "linux",
    "nodeVersion": "v18.17.0"
  },
  "responseTime": 15
}
```

## ❌ Error Responses

All error responses follow this format:

```json
{
  "success": false,
  "error": "Error message",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "path": "/api/v1/endpoint",
  "method": "POST"
}
```

### Common Error Codes

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `UNAUTHORIZED` | 401 | Authentication required |
| `TOKEN_EXPIRED` | 401 | JWT token expired |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource conflict |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Server error |

## 📊 Rate Limiting

- **General API:** 100 requests per 15 minutes
- **OTP Endpoints:** 5 requests per 15 minutes per phone number
- **Headers included in response:**
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `X-RateLimit-Reset`

## 🔒 Security Headers

All responses include security headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

---

**API Version:** v1  
**Last Updated:** January 2024  
**Support:** api-support@jaffna-marketplace.com
