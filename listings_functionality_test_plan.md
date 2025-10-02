# Listings Functionality Test Plan

## Overview
This document outlines the comprehensive testing plan for the listings functionality in the Agrimarket app based on the actual code implementation.

## Test Scope
Based on the code analysis, the listings functionality includes:

### 1. Home Screen Integration
- **Browse Quick Action Card**: Tamil "பார்க்கவும்" / English "Browse" 
- **FAB for Farmers**: Floating Action Button with Add icon for creating listings
- **Sell Quick Action Card**: For farmers only - Tamil "விற்கவும்" / English "Sell"

### 2. Listings Screen (`ListingsScreen.kt`)
- **Title**: Tamil "பட்டியல்கள்" / English "Listings"
- **Navigation**: Back button and Search icon in top bar
- **Empty State**: Shows "No listings available" with subtitle "New listings will be available soon"
- **Listings Display**: LazyColumn with ListingCard components
- **Loading State**: CircularProgressIndicator

### 3. Create Listing Screen (`CreateListingScreen.kt`)
- **Access**: Via FAB on home screen (farmers only) or Sell quick action
- **Form Fields**:
  - Crop Type (dropdown)
  - Quantity (text input)
  - Unit (dropdown)
  - Price per unit (text input)
  - Quality (dropdown)
  - Description (text input)
  - Location (text input)
- **Submit Button**: Tamil "பட்டியல் உருவாக்கவும்" / English "Create Listing"
- **Validation**: Form validation before submission
- **Loading State**: Button shows CircularProgressIndicator during creation

### 4. Search Functionality (`SearchScreen.kt`)
- **Access**: Via search icon in listings screen top bar
- **Search Bar**: Text input for search queries
- **Filter Options**: Crop type and location filters
- **Results Display**: Shows count and filtered listings
- **Search Logic**: Filters by crop type and location (case-insensitive)

## Test Cases

### TC1: Home Screen Navigation to Listings
**Objective**: Verify navigation from home screen to listings
**Steps**:
1. Launch app and complete authentication
2. Verify home screen displays with quick action cards
3. Tap "Browse" (பார்க்கவும்) quick action card
4. Verify navigation to listings screen

**Expected Results**:
- Browse card is visible and clickable
- Tapping navigates to listings screen
- Listings screen title shows "பட்டியல்கள் / Listings"

### TC2: FAB Visibility and Access (Farmers Only)
**Objective**: Verify FAB is visible for farmers and navigates to create listing
**Preconditions**: User is logged in as FARMER type
**Steps**:
1. On home screen, verify FAB is visible
2. Tap FAB
3. Verify navigation to create listing screen

**Expected Results**:
- FAB with Add icon is visible for farmers
- Tapping FAB navigates to create listing screen
- Create listing form is displayed

### TC3: Listings Screen Empty State
**Objective**: Verify empty state display when no listings exist
**Steps**:
1. Navigate to listings screen
2. Verify empty state display

**Expected Results**:
- Empty state message: "No listings available"
- Subtitle: "New listings will be available soon"
- Search icon is still accessible in top bar

### TC4: Create Listing Form Fields
**Objective**: Verify all form fields are present and functional
**Preconditions**: Navigate to create listing screen
**Steps**:
1. Verify all form fields are present:
   - Crop Type dropdown
   - Quantity input
   - Unit dropdown  
   - Price per unit input
   - Quality dropdown
   - Description input
   - Location input
2. Test dropdown functionality
3. Test text input functionality

**Expected Results**:
- All form fields are visible and accessible
- Dropdowns open and allow selection
- Text inputs accept user input
- Form maintains state during interaction

### TC5: Create Listing Validation
**Objective**: Verify form validation works correctly
**Steps**:
1. Try to submit form with empty required fields
2. Verify validation messages
3. Fill required fields and submit
4. Verify successful submission

**Expected Results**:
- Form prevents submission with missing required fields
- Validation messages are displayed
- Valid form submits successfully
- Success feedback is provided

### TC6: Search Functionality Access
**Objective**: Verify search can be accessed from listings screen
**Steps**:
1. Navigate to listings screen
2. Tap search icon in top bar
3. Verify search screen opens

**Expected Results**:
- Search icon is visible in listings top bar
- Tapping opens search screen
- Search input field is available

### TC7: Search and Filter Operations
**Objective**: Verify search and filter functionality
**Steps**:
1. Open search screen
2. Enter search query
3. Apply filters (crop type, location)
4. Verify results update

**Expected Results**:
- Search input accepts text
- Filter options are available
- Results update based on search/filter criteria
- Result count is displayed

## Technical Implementation Notes

### Key Components
- `HomeScreen.kt`: Contains FAB and quick action navigation
- `ListingsScreen.kt`: Main listings display with empty state
- `CreateListingScreen.kt`: Form for creating new listings
- `SearchScreen.kt`: Search and filter functionality
- `ListingsViewModel.kt`: Handles listings data and search logic
- `ListingRepository.kt`: Data layer for listings operations

### Navigation Flow
```
Home Screen → Browse → Listings Screen → Search → Search Screen
Home Screen → FAB → Create Listing Screen
Home Screen → Sell → Create Listing Screen
```

### User Type Considerations
- **Farmers**: Can see FAB, Sell quick action, and create listings
- **Buyers**: Can browse and search listings, no creation access

## Test Environment Requirements
- Android device/emulator with app installed
- Test user accounts for both FARMER and BUYER types
- Network connectivity for API operations
- ADB access for UI automation testing

## Success Criteria
- All navigation paths work correctly
- Form validation prevents invalid submissions
- Search and filter operations return expected results
- UI displays correctly in both Tamil and English
- User type restrictions are enforced properly
- Empty states display appropriate messages
