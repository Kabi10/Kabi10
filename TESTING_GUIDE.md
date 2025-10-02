# Agrimarket Testing Guide

## Overview
This guide provides a comprehensive testing plan for the Jaffna Farmers Marketplace Android application after implementing the OTP bypass for development.

## Prerequisites
- Android device or emulator
- Debug build with OTP bypass enabled
- Development environment set up

## Testing Checklist

### 1. Authentication Flow Testing

#### OTP Bypass Verification
- [ ] Launch app in debug mode
- [ ] Verify bypass indicator appears on OTP screen
- [ ] Enter any 6-digit code and user details
- [ ] Confirm successful bypass to home screen
- [ ] Verify development mode banner on home screen

#### User State Verification
- [ ] Check user profile shows correct mock data
- [ ] Verify user type (FARMER/BUYER) is properly set
- [ ] Confirm authentication tokens are saved
- [ ] Test app restart maintains logged-in state

### 2. Home Screen Testing

#### UI Components
- [ ] Tamil and English text display correctly
- [ ] Welcome section shows user name and type
- [ ] Quick action cards are functional
- [ ] Popular crops section displays
- [ ] Recent listings section appears (may be empty)
- [ ] Development mode banner is visible

#### Navigation
- [ ] Profile icon navigates to profile screen
- [ ] Quick action "Browse" navigates to listings
- [ ] Quick action "Transactions" navigates to transactions
- [ ] FAB (for farmers) navigates to create listing

### 3. User Profile Testing

#### Profile Display
- [ ] User name displays correctly
- [ ] Phone number shows mock data
- [ ] User type (FARMER/BUYER) is correct
- [ ] Language preference is set to Tamil

#### Profile Management
- [ ] Edit profile functionality works
- [ ] User type switching works (if implemented)
- [ ] Settings are accessible
- [ ] Logout functionality works

### 4. Listings Functionality

#### View Listings
- [ ] Listings screen loads without errors
- [ ] Empty state displays properly (if no listings)
- [ ] Search functionality is accessible
- [ ] Filter options are available

#### Create Listing (Farmers Only)
- [ ] Create listing screen accessible from FAB
- [ ] Form fields display correctly
- [ ] Crop type selection works
- [ ] Quantity and price inputs function
- [ ] Image upload interface works (if implemented)
- [ ] Form validation works
- [ ] Listing creation saves locally

#### Listing Details
- [ ] Individual listing details display
- [ ] Contact farmer functionality works
- [ ] Image gallery works (if implemented)
- [ ] Back navigation functions

### 5. Search and Filter Testing

#### Search Functionality
- [ ] Search screen loads correctly
- [ ] Search input accepts text
- [ ] Search results display (may be empty)
- [ ] Search filters work
- [ ] Clear search functionality

#### Filter Options
- [ ] Crop type filters display
- [ ] Location filters work
- [ ] Price range filters function
- [ ] Quality filters work
- [ ] Filter combinations work
- [ ] Clear filters functionality

### 6. Transaction Testing

#### Transaction List
- [ ] Transactions screen loads
- [ ] Empty state displays (if no transactions)
- [ ] Transaction history shows (if any)
- [ ] Transaction status indicators work

#### Create Transaction (Buyers)
- [ ] Transaction creation from listing works
- [ ] Quantity selection functions
- [ ] Pickup location selection
- [ ] Date picker works
- [ ] Payment method selection
- [ ] Transaction confirmation

#### Transaction Management
- [ ] Transaction status updates
- [ ] Transaction details view
- [ ] Cancel transaction functionality
- [ ] Transaction notifications (if implemented)

### 7. Data Persistence Testing

#### Local Storage
- [ ] App data persists after restart
- [ ] User preferences are saved
- [ ] Draft listings are saved
- [ ] Search history is maintained

#### Offline Functionality
- [ ] App works without internet
- [ ] Offline data is accessible
- [ ] Sync indicators appear when online
- [ ] Data synchronization works

### 8. UI/UX Testing

#### Responsive Design
- [ ] App works on different screen sizes
- [ ] Portrait and landscape orientations
- [ ] Text scaling works
- [ ] Touch targets are appropriate

#### Accessibility
- [ ] Screen reader compatibility
- [ ] High contrast mode support
- [ ] Large text support
- [ ] Voice input functionality

#### Localization
- [ ] Tamil text displays correctly
- [ ] English fallbacks work
- [ ] Number formatting is correct
- [ ] Date/time formatting is appropriate

### 9. Performance Testing

#### App Performance
- [ ] App launches quickly
- [ ] Screen transitions are smooth
- [ ] No memory leaks during usage
- [ ] Battery usage is reasonable

#### Data Loading
- [ ] Lists load efficiently
- [ ] Images load properly
- [ ] Pagination works (if implemented)
- [ ] Loading indicators display

### 10. Error Handling

#### Network Errors
- [ ] Offline mode graceful handling
- [ ] Network timeout handling
- [ ] Server error responses
- [ ] Retry mechanisms work

#### Input Validation
- [ ] Form validation messages
- [ ] Invalid data handling
- [ ] Required field validation
- [ ] Data format validation

#### App Stability
- [ ] No crashes during normal use
- [ ] Graceful error recovery
- [ ] Error messages are user-friendly
- [ ] App state is maintained after errors

## Testing Tools

### Manual Testing
- Use Android device or emulator
- Test with different user types (FARMER/BUYER)
- Test with various data scenarios
- Test edge cases and error conditions

### Automated Testing
- Run unit tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
- Check test coverage reports

### Debugging Tools
- Use Android Studio debugger
- Monitor logs with Logcat
- Use Layout Inspector for UI issues
- Profile memory and performance

## Bug Reporting

When reporting bugs, include:
- Device/emulator information
- Android version
- App version (debug build)
- Steps to reproduce
- Expected vs actual behavior
- Screenshots/videos if applicable
- Relevant log entries

## Test Data

### Mock Users
- **Farmer**: Test User (0771234567)
- **Buyer**: Can switch user type in profile

### Test Scenarios
- Create listings with various crop types
- Test with different quantities and prices
- Use various pickup locations
- Test different transaction scenarios

## Success Criteria

The testing is successful when:
- All core features work without crashes
- User can navigate through all screens
- Data is properly saved and retrieved
- UI displays correctly in Tamil and English
- Performance is acceptable
- Error handling is graceful

## Next Steps

After successful testing:
1. Document any issues found
2. Prioritize bug fixes
3. Plan additional feature development
4. Prepare for production OTP re-enablement
5. Plan user acceptance testing
