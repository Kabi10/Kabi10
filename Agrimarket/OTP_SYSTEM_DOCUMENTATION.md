# OTP Authentication System Documentation

## Overview
This document provides comprehensive documentation of the OTP (One-Time Password) authentication system that was implemented in the Agrimarket app. This documentation is created before removing the OTP system to enable future re-implementation.

**Date Created**: September 29, 2025  
**Status**: REMOVED FOR MVP - Documentation preserved for future implementation  
**Reason for Removal**: MVP presentation requirements - authentication barriers removed for demonstration

## System Architecture

### Frontend Components (Android App)

#### 1. AuthViewModel.kt
**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/auth/AuthViewModel.kt`

**Key Methods**:
- `sendOtp(phoneNumber: String)` - Initiates OTP sending process
- `verifyOtp(otp: String, userType: UserType, name: String)` - Verifies OTP and authenticates user
- `resendOtp()` - Resends OTP to the same phone number
- `clearError()` - Clears error state
- `isValidPhoneNumber(phone: String)` - Validates Sri Lankan phone numbers

**State Management**:
```kotlin
data class AuthUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    val phoneNumber: String = "",
    val otpId: String = "",
    val error: String? = null
)
```

**Bypass Logic** (Development Only):
- BuildConfig.BYPASS_OTP flag controls bypass behavior
- Complete authentication bypass for testing
- Mock user creation without server verification

#### 2. AuthRepository.kt
**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/repository/AuthRepository.kt`

**Key Methods**:
- `sendOtp(phoneNumber: String): Result<String>` - API call to send OTP
- `verifyOtp(phoneNumber: String, otp: String, otpId: String?): Result<User>` - API call to verify OTP
- `bypassOtpWithMockUser(phoneNumber: String, name: String, userType: UserType): Result<User>` - Development bypass
- `getCurrentUser(): User?` - Get current authenticated user
- `logout()` - Clear authentication state

**Dependencies**:
- AuthApiService for network calls
- AuthPreferences for token storage
- UserDao for local user data storage

#### 3. UI Screens

**PhoneInputScreen.kt**:
- Phone number entry interface
- Sri Lankan phone number validation
- Bilingual support (Tamil/English)
- Integration with AuthViewModel

**OtpVerificationScreen.kt**:
- OTP code entry (6 digits)
- User name input
- User type selection (FARMER/BUYER)
- Development bypass indicators
- Resend OTP functionality

#### 4. Navigation Integration
**Location**: `app/src/main/java/com/senthapps/slagrimarket/navigation/JaffnaMarketplaceNavigation.kt`

**Flow**:
1. PhoneInput → OtpVerification → Home (Normal flow)
2. PhoneInput → Home (Bypass flow)

### Backend Components

#### 1. OTP Generation Service
**Location**: `backend/src/services/auth.js`

**Methods**:
- `generateOTP()` - Creates 6-digit numeric OTP
- `sendOTP(phone, purpose)` - Stores OTP and triggers SMS
- `verifyOTP(phone, otp, purpose)` - Validates OTP against database

#### 2. API Endpoints

**Send OTP**: `backend/api/auth/send-otp.js`
- Endpoint: POST `/api/auth/send-otp`
- Validates phone number format
- Rate limiting (1 minute between requests)
- Stores OTP in database with 10-minute expiry
- Returns OTP in development mode

**Verify OTP**: `backend/api/auth/verify-otp.js`
- Endpoint: POST `/api/auth/verify-otp`
- Validates OTP format (6 digits)
- Checks OTP against database
- Handles attempt counting and expiry
- Creates/updates user record
- Returns JWT tokens

**Simplified Verify**: `backend/api/auth/verify-otp-simple.js`
- Alternative verification endpoint
- Streamlined validation logic

#### 3. Database Schema
**Location**: `backend/src/database/schema.sql`

**OTP Verifications Table**:
```sql
CREATE TABLE otp_verifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_phone_format CHECK (phone_number ~ '^\+94[0-9]{9}$'),
    CONSTRAINT max_otp_attempts CHECK (attempts <= 3)
);
```

### Build Configuration

#### Debug Build (Development)
```kotlin
buildTypes {
    debug {
        buildConfigField("boolean", "BYPASS_OTP", "true")
        buildConfigField("String", "DEBUG_PHONE", "\"0771234567\"")
        buildConfigField("String", "DEBUG_USER_NAME", "\"Test User\"")
        buildConfigField("String", "DEBUG_USER_TYPE", "\"FARMER\"")
    }
}
```

#### Release Build (Production)
```kotlin
buildTypes {
    release {
        buildConfigField("boolean", "BYPASS_OTP", "false")
        buildConfigField("String", "DEBUG_PHONE", "\"\"")
        buildConfigField("String", "DEBUG_USER_NAME", "\"\"")
        buildConfigField("String", "DEBUG_USER_TYPE", "\"\"")
    }
}
```

## Phone Number Validation

### Current Implementation
```kotlin
private fun isValidPhoneNumber(phone: String): Boolean {
    val cleanPhone = phone.replace(Regex("[\\s-()]"), "")
    return cleanPhone.matches(Regex("^(\\+94|0)?7[0-9]{8}$"))
}
```

### Supported Formats
- International: +94 77 123 4567
- Local: 077 123 4567
- Without spaces: 0771234567

## Security Features

### Rate Limiting
- 1 minute between OTP requests per phone number
- Maximum 3 verification attempts per OTP
- 10-minute OTP expiry time

### Data Protection
- OTPs stored with expiry timestamps
- Automatic cleanup of expired OTPs
- Secure token generation for authentication

### Development Safety
- Bypass only enabled in debug builds
- Clear visual indicators when bypass is active
- Comprehensive logging for debugging

## Integration Points

### SMS Service Integration
**Note**: SMS service integration was planned but not implemented
**Location**: `backend/src/services/auth.js` line 35-40

```javascript
// TODO: Integrate with your SMS service here
// For now, return OTP for testing (remove in production)
if (process.env.NODE_ENV === 'development') {
    console.log(`OTP for ${phone}: ${otp}`);
    return { success: true, otp }; // Remove this in production
}
```

### Recommended SMS Providers
- Twilio
- AWS SNS
- Dialog SMS API (Sri Lanka)
- Mobitel SMS API (Sri Lanka)

## Testing Framework

### Automated Testing
- PowerShell scripts for UI automation
- ADB integration for device testing
- Comprehensive test scenarios

### Manual Testing Procedures
- Phone number validation testing
- OTP flow verification
- Error handling validation
- Bypass mechanism testing

## Removal Process (Completed for MVP)

### Files Removed/Modified
1. **AuthViewModel.kt** - OTP methods removed
2. **AuthRepository.kt** - OTP API calls removed
3. **PhoneInputScreen.kt** - OTP UI removed
4. **OtpVerificationScreen.kt** - Entire screen removed
5. **Navigation** - OTP routes removed
6. **Build configuration** - OTP flags removed

### Database Cleanup
- `otp_verifications` table can be dropped
- Related indexes and constraints removed

### API Cleanup
- OTP endpoints can be disabled/removed
- OTP-related middleware removed

## Re-implementation Guide

### Prerequisites for Re-implementation
1. SMS service provider setup
2. Database schema restoration
3. API endpoint reactivation
4. Frontend component restoration

### Step-by-Step Re-implementation
1. Restore database schema from this documentation
2. Implement SMS service integration
3. Restore backend API endpoints
4. Restore frontend UI components
5. Update navigation flow
6. Add build configuration flags
7. Implement comprehensive testing

### Estimated Re-implementation Time
- Backend: 2-3 days
- Frontend: 2-3 days
- Testing: 1-2 days
- **Total**: 5-8 days

## Security Considerations for Re-implementation

### Production Requirements
1. Remove all bypass mechanisms
2. Implement proper SMS service
3. Add comprehensive rate limiting
4. Implement proper error handling
5. Add monitoring and logging
6. Conduct security testing

### Compliance Requirements
- GDPR compliance for user data
- Local telecommunications regulations
- Data retention policies
- Privacy policy updates

## Backup Information

### Code Backup Location
All removed code is preserved in this documentation and can be restored from:
- Git history (commit before removal)
- This documentation file
- Backup files created during removal process

### Configuration Backup
All build configurations, API endpoints, and database schemas are documented above for complete restoration capability.

---

**Important**: This system was fully functional and tested before removal. Re-implementation should be straightforward using this documentation as a guide.
