# Feature Spec: Industrial UI Integration

## Goal

Replace existing screens with industrial UI screens and remove all navigation transitions to create a brutal, machinery-inspired user experience.

## Scope (Single Feature Only)

- [x] Replace HomeScreen with IndustrialHomeScreen
- [x] Replace MarketPricesScreen with IndustrialMarketPricesScreen
- [x] Replace ListingDetailScreen with IndustrialListingDetailScreen
- [x] Replace CreateListingScreen with IndustrialCreateListingScreen
- [x] Add IndustrialCategorySelectionScreen to navigation
- [x] Add IndustrialListingsListScreen to navigation
- [x] Remove all navigation enter/exit transitions
- [x] Wire up proper navigation flow for buyer journey (Category → List → Detail)

## Files to Modify

- `navigation/JaffnaMarketplaceNavigation.kt` - Update all composable routes
- Navigation transitions will be set to `EnterTransition.None` and `ExitTransition.None`

## New Routes to Add

- `categories` - Category selection screen (2×2 grid)
- `category_listings/{category}` - Listings list for a specific category

## Navigation Flow Changes

### OLD FLOW (Home → Listings → Detail):

```
Home
 ├─ BUY → Listings (all listings)
 └─ SELL → CreateListing
```

### NEW FLOW (Home → Categories → List → Detail):

```
IndustrialHome
 ├─ BUY → Categories (2×2 grid) → CategoryListings (filtered) → Detail
 ├─ SELL → CreateListing
 ├─ PRICES → MarketPrices
 └─ ORDERS → Transactions
```

## Implementation Strategy

### Phase 1: Update Navigation Graph

1. Import industrial screen imports
2. Add new routes for Categories and CategoryListings
3. Replace old screen composables with industrial versions
4. Add transition animations = None to NavHost

### Phase 2: Wire Navigation Callbacks

1. IndustrialHomeScreen callbacks:
   - onNavigateToSell → CreateListing route
   - onNavigateToBuy → Categories route (NEW)
   - onNavigateToPrices → MarketPrices route
   - onNavigateToOrders → Transactions route

2. IndustrialCategorySelectionScreen callbacks:
   - onCategorySelected → CategoryListings route with category parameter

3. IndustrialListingsListScreen callbacks:
   - onListingClick → ListingDetail route
   - onNavigateBack → pop back stack

4. IndustrialMarketPricesScreen callbacks:
   - onNavigateBack → pop back stack

5. IndustrialListingDetailScreen callbacks:
   - onNavigateBack → pop back stack
   - onCallSeller → (placeholder - maybe open phone dialer)
   - onEdit → (future: edit listing screen)
   - onDelete → (delete listing, navigate back)

6. IndustrialCreateListingScreen callbacks:
   - onNavigateBack → pop back stack
   - onSubmit → (save listing, navigate back)

### Phase 3: Data Wiring

IndustrialListingsListScreen and IndustrialListingDetailScreen need real data:

- Use existing ListingsViewModel
- Fetch listings via Hilt injection
- Filter by category when displaying category listings

## Out of Scope

- Changing existing ViewModels (reuse them)
- Modifying bottom navigation bar (keep it for now)
- Updating tests (will do in separate task)
- Implementing full CRUD operations (just navigation shell)
- Removing old screen files (keep for gradual migration)

## Success Criteria

- [x] App builds successfully
- [x] Navigation flows from Home → Categories → Listings → Detail
- [x] All industrial screens display correctly (pending manual verification)
- [x] No animations on navigation transitions
- [x] Back button works on all screens (wired up)
- [x] Market prices screen accessible from home
- [x] Create listing screen accessible from home
- [x] Transactions screen accessible from home

## Implementation Summary

### Completed (2026-01-07)

All navigation integration tasks have been completed:

1. **Navigation Graph Updated** (`navigation/JaffnaMarketplaceNavigation.kt`)
   - Added `EnterTransition.None` and `ExitTransition.None` to all navigation transitions
   - Replaced `HomeScreen` with `IndustrialHomeScreen` (2×2 grid)
   - Added new routes: `categories` and `category_listings/{category}`
   - Wired up all industrial screens with existing ViewModels

2. **Screen Replacements**
   - Home → IndustrialHomeScreen (2×2 text-only tiles)
   - MarketPrices → IndustrialMarketPricesScreen (alternating row list)
   - ListingDetail → IndustrialListingDetailScreen (huge price display)
   - CreateListing → IndustrialCreateListingScreen (single-scroll form)
   - Added IndustrialCategorySelectionScreen (2×2 category grid)
   - Added IndustrialListingsListScreen (filtered list view)

3. **Navigation Flow**

   ```
   IndustrialHome (2×2 grid)
    ├─ BUY → Categories → CategoryListings → ListingDetail
    ├─ SELL → CreateListing
    ├─ PRICES → MarketPrices
    └─ ORDERS → Transactions
   ```

4. **Data Integration**
   - Connected ListingsViewModel to category and detail screens
   - Connected HomeViewModel to market prices screen
   - Implemented category filtering logic based on cropType field
   - Created ListingPreview adapter for industrial list UI

5. **Build Status**
   - ✅ Kotlin compilation successful
   - ✅ Debug APK assembled successfully
   - ⚠️ One unrelated deprecation warning in BottomNavigationBar.kt

### Pending Manual Verification

The following should be verified on a physical device or emulator:

- [ ] 2×2 home grid displays correctly and tiles are tappable
- [ ] Category selection navigates to filtered listings
- [ ] Listing click navigates to detail screen with correct data
- [ ] Back buttons work on all screens
- [ ] No animation occurs during any navigation transition
- [ ] Market prices list displays with alternating backgrounds
- [ ] Create listing form scrolls properly and validation works
- [ ] Touch targets are at least 64dp (thick finger friendly)
- [ ] Text is readable in high-glare outdoor conditions (pure black on white)

### Known Pending Work (Out of Scope for This Integration)

- Photo picker implementation in CreateListingScreen
- Phone dialer integration for "CALL SELLER" button
- Edit listing functionality
- Delete listing functionality with actual data deletion
- Real timestamp calculation for "UPDATED: X AGO" text
- isOwnListing determination based on current user ID
- Form submission ViewModel wiring in CreateListingScreen
- Unit and instrumented tests for new industrial screens
