# Test Fix Checklist

## Quick Reference for Fixing Compilation Errors

### Step-by-Step Fix Process

#### 1. Fix CreateListingViewModelTest.kt

**Errors to Fix:**
- [ ] Line 4: Remove `import ListingStatus` (doesn't exist)
- [ ] Line 278-294: Fix `createListing()` helper method:
  ```kotlin
  // Change from:
  private fun createListing(cropType: String) = Listing(...)
  
  // To:
  private fun createListing(
      id: String,
      farmerId: String,
      cropType: String
  ) = Listing(
      id = id,
      farmerId = farmerId,
      cropType = cropType,
      quantity = 100.0,
      unit = "kg",
      pricePerUnit = 150.0,
      quality = QualityGrade.A,
      harvestDate = "2025-09-15",
      location = "Jaffna"
  )
  ```
- [ ] Line 293-321: Fix `createListing()` mock calls:
  ```kotlin
  // Change:
  coEvery { authRepository.getCurrentUserId() } returns "user123"
  coEvery { listingRepository.createListing(any()) } returns Result.success(Unit)
  
  // To:
  coEvery { authRepository.getCurrentUser() } returns flowOf(testUser)
  coEvery { listingRepository.createListing(any()) } returns Result.success(testListing)
  ```

#### 2. Fix HomeViewModelTest.kt

**Errors to Fix:**
- [ ] Lines 33-37: Fix repository mock setup:
  ```kotlin
  // Change:
  coEvery { userRepository.getCurrentUserFlow() } returns flowOf(testUser)
  coEvery { marketPriceRepository.getMarketPrices() } returns flowOf(Resource.Success(prices))
  
  // To:
  every { userRepository.getCurrentUserFlow() } returns flowOf(testUser)
  every { marketPriceRepository.getMarketPrices(any()) } returns flowOf(Resource.Success(prices))
  ```
- [ ] Lines 177, 206: Fix state property names:
  ```kotlin
  // Change:
  assertEquals(2, state.todaysOrders)
  assertEquals(5000.0, state.todaysRevenue, 0.01)
  
  // To:
  assertEquals(2, state.statistics.todaysOrders)
  assertEquals(5000.0, state.statistics.todaysRevenue, 0.01)
  ```
- [ ] Lines 265-316: Fix helper methods:
  ```kotlin
  // User helper:
  private fun createTestUser(id: String = "user123", name: String = "Test User") = User(
      id = id,
      name = name,
      phone = "+94771234567",  // NOT phoneNumber
      userType = UserType.FARMER,
      verified = true  // NOT isVerified
  )
  
  // MarketPrice helper:
  private fun createTestMarketPrice(cropType: String, price: Double, location: String = "Jaffna") = MarketPrice(
      id = "mp_$cropType",
      cropType = cropType,
      cropNameTamil = "வெங்காயம்",
      cropNameEnglish = "Red Onion",
      cropNameSinhala = "රතු ළූණු",
      currentPrice = price,  // NOT price
      trend = PriceTrend.STABLE,
      location = location
  )
  
  // Activity helper:
  private fun createTestActivity(id: String, userId: String, description: String) = Activity(
      id = id,
      userId = userId,
      activityType = ActivityType.LISTING_CREATED,  // NOT type
      title = "Test Activity",
      description = description,
      timestamp = Instant.now().toString()  // String, not Instant
  )
  
  // Transaction helper:
  private fun createTestTransaction(id: String, farmerId: String, buyerId: String, amount: Double) = Transaction(
      id = id,
      listingId = "listing1",
      farmerId = farmerId,  // NOT sellerId
      buyerId = buyerId,
      quantity = 100.0,
      pricePerUnit = 50.0,
      totalAmount = amount,
      pickupLocation = "Jaffna",
      pickupDate = LocalDate.now().toString(),  // String!
      status = TransactionStatus.COMPLETED,
      createdAt = Instant.now().toString()  // String!
  )
  ```

#### 3. Fix ListingRepositoryTest.kt

**Errors to Fix:**
- [ ] Line 28: Add moshi parameter:
  ```kotlin
  // Change:
  repository = ListingRepository(listingApiService, listingDao, localOpDao)
  
  // To:
  private val moshi = Moshi.Builder().build()
  repository = ListingRepository(listingApiService, listingDao, localOpDao, moshi)
  ```
- [ ] Lines 49-68: Fix DAO and API method names:
  ```kotlin
  // Change:
  every { listingDao.getAllListingsFlow() } returns flowOf(dbListings)
  coEvery { listingApiService.getListings() } returns Response.success(networkListings)
  repository.getListings().test { ... }
  
  // To:
  every { listingDao.getAllActiveListingsFlow() } returns flowOf(dbListings)
  coEvery { listingApiService.getListings(any(), any()) } returns 
      Response.success(ListingsResponse(listings = networkListings, total = networkListings.size))
  repository.getAllActiveListings().test { ... }
  ```
- [ ] Lines 58-68: Fix Resource type assertions:
  ```kotlin
  // Change:
  assertTrue(loading is Resource.Loading)
  assertTrue(dbData is Resource.Success)
  
  // To:
  assertTrue(loading is Resource.Loading<*>)
  assertTrue(dbData is Resource.Success<*>)
  assertEquals(1, (dbData as Resource.Success<List<Listing>>).data.size)
  ```
- [ ] Lines 145-157: Fix createListing test:
  ```kotlin
  // Change:
  coEvery { listingRepository.createListing(listing) } returns Result.success(Unit)
  
  // To:
  coEvery { listingDao.insertListing(any()) } just Runs
  coEvery { localOpDao.insertOp(any()) } just Runs
  
  val result = repository.createListing(listing)
  
  assertTrue(result.isSuccess)
  coVerify { listingDao.insertListing(any()) }
  coVerify { localOpDao.insertOp(match { 
      it.type == OpType.CREATE_LISTING && it.synced == false  // NOT status == OpStatus.PENDING
  }) }
  ```
- [ ] Lines 215-268: Fix refresh tests:
  ```kotlin
  // Change:
  coEvery { listingApiService.getListingsByUser(any()) } returns Response.success(listings)
  repository.refreshListings(userId).test { ... }
  
  // To:
  coEvery { listingApiService.getListings(any(), any()) } returns 
      Response.success(ListingsResponse(listings = listings, total = listings.size))
  repository.refreshListings().test { ... }
  ```
- [ ] Lines 310-320: Fix helper method:
  ```kotlin
  private fun createTestListing(id: String, cropType: String, farmerId: String = "user123") = Listing(
      id = id,
      farmerId = farmerId,  // NOT userId
      cropType = cropType,
      quantity = 100.0,
      unit = "kg",
      pricePerUnit = 150.0,
      quality = QualityGrade.A,  // NOT "A"
      harvestDate = "2025-09-15",
      location = "Jaffna"
  )
  ```

#### 4. Fix MarketPriceRepositoryTest.kt

**Errors to Fix:**
- [ ] Lines 47-72: Fix DAO and API method names:
  ```kotlin
  // Change:
  every { marketPriceDao.getAllMarketPricesFlow() } returns flowOf(dbPrices)
  coEvery { marketPriceApiService.getMarketPrices() } returns Response.success(networkPrices)
  repository.getMarketPrices().test { ... }
  
  // To:
  every { marketPriceDao.getMarketPricesFlow() } returns flowOf(dbPrices)
  coEvery { marketPriceApiService.getMarketPrices() } returns 
      Response.success(MarketPricesResponse(prices = networkPrices, total = networkPrices.size))
  repository.getMarketPrices(forceRefresh = false).test { ... }
  ```
- [ ] Lines 56-68: Fix Resource type assertions (same as ListingRepositoryTest)
- [ ] Lines 330-335: Fix helper method:
  ```kotlin
  private fun createTestMarketPrice(
      cropType: String,
      price: Double,
      location: String = "Jaffna",
      trend: PriceTrend = PriceTrend.STABLE,
      changePercentage: Double = 0.0
  ) = MarketPrice(
      id = "price_$cropType",
      cropType = cropType,
      cropNameTamil = "வெங்காயம்",  // REQUIRED
      cropNameEnglish = "Red Onion",  // REQUIRED
      cropNameSinhala = "රතු ළූණු",  // REQUIRED
      currentPrice = price,  // NOT price
      unit = "kg",
      trend = trend,
      changePercentage = changePercentage,
      location = location
  )
  ```

#### 5. Fix SyncManagerTest.kt

**Errors to Fix:**
- [ ] Lines 115, 150: Fix quality parameter:
  ```kotlin
  // Change:
  quality = "A"
  
  // To:
  quality = QualityGrade.A
  ```
- [ ] Lines 191-192: Fix API response types:
  ```kotlin
  // Change:
  coEvery { listingApiService.createListing(any()) } returns Response.success(listing)
  coEvery { listingApiService.deleteListing(any()) } returns Response.success(Unit)
  
  // To:
  coEvery { listingApiService.createListing(any()) } returns Response.success(listing)
  coEvery { listingApiService.deleteListing(any()) } returns Response.success(DeleteResponse(success = true))
  ```
- [ ] Lines 212-213: Fix LocalOp status:
  ```kotlin
  // Change:
  createTestLocalOp("op1", OpType.CREATE_LISTING, "{}", status = OpStatus.FAILED)
  
  // To:
  createTestLocalOp("op1", OpType.CREATE_LISTING, "{}", synced = false, errorMessage = "Failed")
  ```
- [ ] Lines 323-351: Fix helper methods:
  ```kotlin
  private fun createTestListing(id: String, cropType: String, farmerId: String = "user123") = Listing(
      id = id,
      farmerId = farmerId,  // NOT userId
      cropType = cropType,
      quantity = 100.0,
      unit = "kg",
      pricePerUnit = 150.0,
      quality = QualityGrade.A,  // NOT "A"
      harvestDate = "2025-09-15",
      location = "Jaffna"
  )
  
  private fun createTestLocalOp(
      opId: String,
      type: OpType,
      payload: String,
      clientId: String? = null,
      entityId: String? = null,
      synced: Boolean = false  // NOT status
  ) = LocalOp(
      opId = opId,
      type = type,
      entityId = entityId,
      clientId = clientId,
      payload = payload,
      synced = synced,  // NOT status
      clientTs = Instant.now().toString(),  // NOT createdAt
      attempts = 0  // NOT retryCount
  )
  ```

## Final Steps

After fixing all files:

1. Run `./gradlew test` to verify compilation
2. Fix any remaining errors
3. Run tests again to check for runtime failures
4. Update test assertions based on actual behavior
5. Commit working tests

## Quick Commands

```bash
# Clean and test
./gradlew clean test

# Test specific class
./gradlew test --tests CreateListingViewModelTest

# Test with stacktrace
./gradlew test --stacktrace

# Test with info logging
./gradlew test --info
```

