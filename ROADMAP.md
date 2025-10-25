# Agrimarket Development Roadmap

This document outlines the planned features, improvements, and technical debt items for the Agrimarket Android app. Items are organized by priority and complexity to help contributors understand what needs to be done and where they can help.

## 📊 Current Status

- **Version**: 1.0-debug
- **Build Status**: ✅ Passing
- **Test Coverage**: ~0% (needs improvement)
- **Code Quality**: Good (MVVM architecture, clean code)
- **Production Ready**: No (see Critical Items below)

---

## 🚨 Critical Items (Must-Have for Production)

These items must be completed before the app can be released to real users.

### 1. Implement Real Authentication System

**Status**: 🔴 Not Started  
**Priority**: Critical  
**Difficulty**: Hard  
**Estimated Time**: 2-3 weeks  
**Labels**: `priority: critical`, `type: feature`, `component: auth`, `difficulty: hard`

**Description**:
Currently, the app uses a demo/mock authentication system that automatically logs in a test user. For production, we need real OTP-based authentication with SMS integration.

**Requirements**:
- [ ] Integrate SMS provider (Dialog Ideamart, Mobitel, or Twilio)
- [ ] Implement OTP generation and verification
- [ ] Add rate limiting to prevent abuse
- [ ] Implement session management with JWT tokens
- [ ] Add phone number validation for Sri Lankan numbers
- [ ] Handle OTP expiration and resend logic
- [ ] Add security measures (brute force protection, etc.)

**Acceptance Criteria**:
- Users can register with Sri Lankan phone number
- OTP is sent via SMS
- OTP verification works correctly
- Session persists across app restarts
- Logout functionality works
- Security best practices followed

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/AuthRepository.kt`
- `app/src/main/java/com/senthapps/slagrimarket/ui/auth/OtpVerificationScreen.kt`
- `backend/src/routes/auth.js`

**Resources**:
- [Dialog Ideamart SMS API](https://www.ideamart.io/)
- [Twilio SMS API](https://www.twilio.com/docs/sms)
- See `KNOWN_ISSUES.md` #2 for current limitations

---

### 2. Deploy Backend to Production

**Status**: 🟡 Partially Complete  
**Priority**: Critical  
**Difficulty**: Medium  
**Estimated Time**: 1 week  
**Labels**: `priority: critical`, `type: deployment`, `component: backend`, `difficulty: medium`

**Description**:
The backend API exists but may not be properly deployed or accessible. Need to verify deployment, configure environment variables, and ensure reliability.

**Requirements**:
- [ ] Verify Vercel deployment is working
- [ ] Configure production environment variables
- [ ] Set up Supabase production database
- [ ] Run database migrations
- [ ] Configure CORS for production
- [ ] Set up monitoring and logging
- [ ] Configure rate limiting
- [ ] Set up error tracking (Sentry or similar)
- [ ] Test all API endpoints in production

**Acceptance Criteria**:
- Backend is accessible at production URL
- All API endpoints return correct responses
- Database is properly configured
- Environment variables are secure
- Monitoring is in place
- Error tracking is working

**Related Files**:
- `backend/vercel.json`
- `backend/.env.example`
- `supabase/migrations/`

**Resources**:
- See `docs/PRODUCTION_DEPLOYMENT_GUIDE.md`
- See `KNOWN_ISSUES.md` #3 for current status

---

## 🎯 High Priority (Important for User Experience)

### 3. Comprehensive Testing Suite

**Status**: 🔴 Not Started  
**Priority**: High  
**Difficulty**: Medium  
**Estimated Time**: 3-4 weeks  
**Labels**: `priority: high`, `type: testing`, `difficulty: medium`, `help wanted`

**Description**:
The app has testing infrastructure but no actual tests. Need to add unit tests, integration tests, and UI tests to ensure code quality.

**Requirements**:
- [ ] Unit tests for all ViewModels (>70% coverage)
- [ ] Unit tests for all Repositories
- [ ] Unit tests for utility classes
- [ ] Integration tests for database operations
- [ ] UI tests for critical user flows
- [ ] Set up CI/CD with automated testing
- [ ] Configure code coverage reporting

**Acceptance Criteria**:
- >70% code coverage for ViewModels
- >60% code coverage for Repositories
- All critical user flows have UI tests
- Tests run automatically on PR
- Coverage reports generated

**Related Files**:
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumentation tests

**Resources**:
- See `CONTRIBUTING.md` for testing guidelines
- See `KNOWN_ISSUES.md` #1 for current state
- See issue template: "Add Unit Tests for ViewModels"

---

### 4. Offline Sync Improvements

**Status**: 🟡 Partially Complete  
**Priority**: High  
**Difficulty**: Hard  
**Estimated Time**: 2-3 weeks  
**Labels**: `priority: high`, `type: enhancement`, `component: offline`, `difficulty: hard`

**Description**:
The app has offline-first design with basic sync, but needs improvements for conflict resolution, better error handling, and user feedback.

**Requirements**:
- [ ] Implement robust conflict resolution strategy
- [ ] Add sync status indicators in UI
- [ ] Handle network state changes gracefully
- [ ] Implement exponential backoff for failed syncs
- [ ] Add manual sync trigger
- [ ] Show pending operations count
- [ ] Handle large data sets efficiently
- [ ] Add sync settings (WiFi only, etc.)

**Acceptance Criteria**:
- Conflicts are resolved correctly
- Users can see sync status
- Failed syncs retry automatically
- Manual sync works
- Sync works efficiently with large data

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/workers/SyncWorker.kt`
- `app/src/main/java/com/senthapps/slagrimarket/data/repository/SyncRepository.kt`

**Resources**:
- See `ARCHITECTURE.md` for offline-first design
- See `KNOWN_ISSUES.md` #8 for current limitations

---

## 🌟 Medium Priority (Nice to Have)

### 5. ProGuard/R8 Configuration

**Status**: 🔴 Not Started  
**Priority**: Medium  
**Difficulty**: Easy  
**Estimated Time**: 1 week  
**Labels**: `priority: medium`, `type: enhancement`, `difficulty: easy`, `good first issue`

**Description**:
Release builds currently have minification disabled, resulting in larger APK size and easier reverse engineering.

**Requirements**:
- [ ] Enable R8 for release builds
- [ ] Add keep rules for Retrofit
- [ ] Add keep rules for Moshi
- [ ] Add keep rules for Room
- [ ] Add keep rules for Firebase
- [ ] Test release build thoroughly
- [ ] Verify no crashes from obfuscation
- [ ] Measure APK size reduction

**Acceptance Criteria**:
- Release builds are minified
- APK size reduced by >30%
- No crashes from obfuscation
- All features work in release build

**Related Files**:
- `app/build.gradle.kts`
- `app/proguard-rules.pro`

**Resources**:
- See `KNOWN_ISSUES.md` #10
- [R8 Documentation](https://developer.android.com/studio/build/shrink-code)

---

### 6. Network Status Indicators

**Status**: 🔴 Not Started  
**Priority**: Medium  
**Difficulty**: Easy  
**Estimated Time**: 1 week  
**Labels**: `priority: medium`, `type: enhancement`, `component: ui`, `difficulty: easy`, `good first issue`

**Description**:
The app doesn't clearly indicate when it's operating in offline mode or when data is stale.

**Requirements**:
- [ ] Add network status ViewModel
- [ ] Show "Offline Mode" banner when no connectivity
- [ ] Indicate stale data with timestamp
- [ ] Add "Last synced" information
- [ ] Show sync status in UI
- [ ] Add pull-to-refresh with network check

**Acceptance Criteria**:
- Users can see network status
- Offline mode is clearly indicated
- Data freshness is visible
- Sync status is shown

**Related Files**:
- All screen composables
- Create new `NetworkStatusViewModel.kt`

**Resources**:
- See `KNOWN_ISSUES.md` #11
- [ConnectivityManager Documentation](https://developer.android.com/training/monitoring-device-state/connectivity-status-type)

---

### 7. Image Loading Optimization

**Status**: 🔴 Not Started  
**Priority**: Medium  
**Difficulty**: Medium  
**Estimated Time**: 1-2 weeks  
**Labels**: `priority: medium`, `type: performance`, `component: ui`, `difficulty: medium`

**Description**:
Image loading works but could be optimized for better performance and user experience.

**Requirements**:
- [ ] Implement image caching strategy
- [ ] Add placeholder images
- [ ] Add error images
- [ ] Implement lazy loading for galleries
- [ ] Compress images before upload
- [ ] Add image size limits
- [ ] Optimize thumbnail generation
- [ ] Add loading indicators

**Acceptance Criteria**:
- Images load faster
- Placeholders shown while loading
- Error states handled gracefully
- Upload size reduced
- Better user experience

**Related Files**:
- `app/src/main/java/com/senthapps/slagrimarket/util/ImageUploadUtil.kt`
- All screens displaying images

**Resources**:
- See `KNOWN_ISSUES.md` #12
- [Coil Documentation](https://coil-kt.github.io/coil/)

---

## 💡 Low Priority (Future Enhancements)

### 8. Database Query Optimization

**Status**: 🔴 Not Started  
**Priority**: Low  
**Difficulty**: Medium  
**Estimated Time**: 1-2 weeks  
**Labels**: `priority: low`, `type: performance`, `component: database`, `difficulty: medium`

**Description**:
Add indexes and optimize queries for better performance with large datasets.

**Requirements**:
- [ ] Add indexes to frequently queried columns
- [ ] Optimize complex queries
- [ ] Add query logging in debug builds
- [ ] Monitor query performance
- [ ] Implement pagination for large lists
- [ ] Add database profiling

**Related Files**:
- All DAO files in `app/src/main/java/com/senthapps/slagrimarket/data/dao/`

**Resources**:
- See `KNOWN_ISSUES.md` #13
- [Room Performance Best Practices](https://developer.android.com/training/data-storage/room/performance)

---

### 9. API Documentation

**Status**: 🔴 Not Started  
**Priority**: Low  
**Difficulty**: Easy  
**Estimated Time**: 1 week  
**Labels**: `priority: low`, `type: documentation`, `difficulty: easy`, `good first issue`

**Description**:
Backend API endpoints are not documented, making it difficult for new developers to understand the API.

**Requirements**:
- [ ] Add OpenAPI/Swagger documentation
- [ ] Document all endpoints
- [ ] Include example requests/responses
- [ ] Add authentication documentation
- [ ] Document error codes
- [ ] Add API versioning strategy

**Related Files**:
- `backend/README.md`
- Create `backend/docs/API.md`

**Resources**:
- See `KNOWN_ISSUES.md` #14
- [OpenAPI Specification](https://swagger.io/specification/)

---

### 10. Database Schema Documentation

**Status**: 🔴 Not Started  
**Priority**: Low  
**Difficulty**: Easy  
**Estimated Time**: 3-5 days  
**Labels**: `priority: low`, `type: documentation`, `difficulty: easy`, `good first issue`

**Description**:
Room database schema is auto-generated but not documented in a human-readable format.

**Requirements**:
- [ ] Create ER diagram
- [ ] Document table relationships
- [ ] Explain migration strategy
- [ ] Document indexes and constraints
- [ ] Add schema versioning guide

**Related Files**:
- `app/schemas/` - Auto-generated schema files
- Create `docs/DATABASE_SCHEMA.md`

**Resources**:
- See `KNOWN_ISSUES.md` #15

---

## 🎨 Feature Requests (Community Driven)

These features can be added based on user feedback and community contributions.

### 11. Advanced Search and Filters

**Status**: 🔴 Not Started  
**Priority**: Medium  
**Difficulty**: Hard  
**Labels**: `type: feature`, `component: ui`, `difficulty: hard`, `help wanted`

**Description**:
Add comprehensive search and filtering capabilities to help users find listings.

**See Issue Template**: "Add Search Filters to Listings Screen"

---

### 12. Push Notifications

**Status**: 🟡 Partially Complete  
**Priority**: Medium  
**Difficulty**: Medium  
**Labels**: `type: feature`, `component: firebase`, `difficulty: medium`

**Description**:
Firebase Cloud Messaging is configured but not fully implemented. Add notifications for new messages, transactions, etc.

---

### 13. In-App Messaging

**Status**: 🟡 Partially Complete  
**Priority**: Medium  
**Difficulty**: Hard  
**Labels**: `type: feature`, `component: ui`, `difficulty: hard`

**Description**:
Chat functionality exists but needs improvements for real-time messaging, read receipts, etc.

---

## 📈 Progress Tracking

| Category | Total Items | Completed | In Progress | Not Started |
|----------|-------------|-----------|-------------|-------------|
| Critical | 2 | 0 | 0 | 2 |
| High Priority | 2 | 0 | 0 | 2 |
| Medium Priority | 3 | 0 | 0 | 3 |
| Low Priority | 3 | 0 | 0 | 3 |
| **Total** | **10** | **0** | **0** | **10** |

---

## 🤝 How to Contribute

1. Pick an item from this roadmap
2. Check if there's an existing issue for it
3. If not, create a new issue referencing this roadmap
4. Follow the guidelines in `CONTRIBUTING.md`
5. Submit a pull request when ready

**Questions?** Open a discussion or comment on the relevant issue!

---

**Last Updated**: 2025-10-25  
**Next Review**: 2025-11-25

