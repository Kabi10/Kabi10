# CROPIX / DOA API — LIVE TESTED SPEC

Tested: 2026-03-03. All results from actual HTTP calls to production API.

---

## Verified Base URL

https://digital.doa.gov.lk/cropix/api/v1

## Authentication

PUBLIC — no API key or token required for crop-details endpoints.
Geographic, management, prices: 403 (require registration token — not public yet).

---

## Live-tested Endpoints (all return HTTP 200, no auth)

### /crop-details/crop-categories

GET https://digital.doa.gov.lk/cropix/api/v1/crop-details/crop-categories
Total: 6 categories

Response payloadDto items:
{"id":1,"categoryId":"01","description":"Paddy"}
{"id":2,"categoryId":"02","description":"Field Crops"}
{"id":3,"categoryId":"03","description":"Vegetables"}
{"id":4,"categoryId":"04","description":"Root & Tuber Crops"}
{"id":5,"categoryId":"05","description":"Leafy Vegetables"}
{"id":6,"categoryId":"06","description":"Fruits"}

### /crop-details/crop-sub-categories

GET https://digital.doa.gov.lk/cropix/api/v1/crop-details/crop-sub-categories
Total: 12

Sample items:
{"id":1,"description":"Paddy","subCategoryId":"01/01","cropCategoryDTO":{"id":1,"categoryId":"01","description":"Paddy"}}
{"id":2,"description":"Coarse Grains","subCategoryId":"02/01","cropCategoryDTO":{"id":2,"categoryId":"02","description":"Field Crops"}}

### /crop-details/crops

GET https://digital.doa.gov.lk/cropix/api/v1/crop-details/crops
Total: 106 crops

Sample item:
{"id":1,"cropId":"01/01/001","description":"Paddy","cropType":"ANNUAL",
"scientificName":"Oryza sativa","presignedUrl":"https://eagri1.s3.ap-south-1.amazonaws.com/crop-image/1?..."}

Key fields: id, cropId (hierarchical: "categoryId/subCategoryId/cropNum"), description,
cropType (ANNUAL/SEASONAL/PERENNIAL), scientificName, presignedUrl (S3 image URL)

### /crop-details/crop-varieties

GET https://digital.doa.gov.lk/cropix/api/v1/crop-details/crop-varieties
Total: 721 varieties

Key fields on each variety:
varietyId — "01/01/001/029" (hierarchical)
varietyName — "Bg 360"
varietyDescription — "3 1/2 month white short oblong"
averageYield — "4.2" (tonnes/ha)
maturityTime — "105" (days)
seedRequirement — "80 kg/ha"
releasedYear — "1999"
specialCharacteristics — "Soft white samba"
cropVarietyImages — [{presignedUrl, type: "CROP"|"SEED"}] (S3 links with expiry)
cropDTO — nested full crop object
resistanceLevels — [{resistanceType:"BLAST", resistanceLevel:"S"}, ...]
ageGroup/cropStages — full growth stage timeline with day durations
pericarpColor, grainType, grainCategory — grain attributes

---

## Endpoints returning 403 (require token, not public yet)

- /geographical/provinces
- /geographical/districts
- /geographical/structure
- /crop-management/activities
- /crop-management/pests
- /crop-management/diseases
- /agriculture-season/seasons
- /crop-look/distribution
- /market-prices
- /prices
- /yield-forecast

403 does NOT change to 401 with dummy Bearer token — these are gated by role/permission, not just auth.

---

## Kotlin Models (matching live schema exactly)

```kotlin
// Generic envelope
data class CropixResponse<T>(
    val message: String?,
    val resultStatus: String,
    val httpStatus: String,
    val httpCode: String,
    @Json(name = "payloadDto") val payload: List<T>,
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val size: Int,
    val number: Int,
    val numberOfElements: Int
)

data class CropCategoryDto(
    val id: Int,
    val categoryId: String,   // "01"
    val description: String   // "Paddy"
)

data class CropSubCategoryDto(
    val id: Int,
    val subCategoryId: String,           // "01/01"
    val description: String,
    val cropCategoryDTO: CropCategoryDto
)

data class CropDto(
    val id: Int,
    val cropId: String,                  // "01/01/001"
    val description: String,
    val cropType: String,                // "ANNUAL", "SEASONAL", "PERENNIAL"
    val scientificName: String?,
    val presignedUrl: String?,           // S3 crop image URL
    val cropSubCategoryDTO: CropSubCategoryDto?
)

data class CropVarietyDto(
    val id: Int,
    val varietyId: String,               // "01/01/001/029"
    val varietyName: String,
    val varietyDescription: String?,
    val averageYield: String?,           // tonnes/ha as string
    val maturityTime: String?,           // days as string
    val seedRequirement: String?,
    val releasedYear: String?,
    val specialCharacteristics: String?,
    val presignedUrl: String?,           // main crop image (S3, ~5 day expiry)
    val cropDTO: CropDto?,
    val cropVarietyImages: List<CropVarietyImageDto>?
)

data class CropVarietyImageDto(
    val id: Int,
    val presignedUrl: String,
    val type: String                     // "CROP" or "SEED"
)
```

---

## Room Entities

```kotlin
@Entity(tableName = "doa_crop_categories")
data class DoaCropCategoryEntity(
    @PrimaryKey val id: Int,
    val categoryId: String,
    val description: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "doa_crops")
data class DoaCropEntity(
    @PrimaryKey val id: Int,
    val cropId: String,
    val description: String,
    val cropType: String,
    val scientificName: String?,
    val categoryId: String,
    val cachedAt: Long = System.currentTimeMillis()
)
```

---

## Retrofit Service (no auth interceptor needed)

```kotlin
interface CropixApiService {
    @GET("cropix/api/v1/crop-details/crop-categories")
    suspend fun getCropCategories(): CropixResponse<CropCategoryDto>

    @GET("cropix/api/v1/crop-details/crop-sub-categories")
    suspend fun getCropSubCategories(): CropixResponse<CropSubCategoryDto>

    @GET("cropix/api/v1/crop-details/crops")
    suspend fun getCrops(): CropixResponse<CropDto>

    @GET("cropix/api/v1/crop-details/crop-varieties")
    suspend fun getCropVarieties(): CropixResponse<CropVarietyDto>
}

// Hilt: separate Retrofit for DOA (different base URL from existing backend)
@Provides @Singleton @Named("cropix")
fun provideCropixRetrofit(): Retrofit = Retrofit.Builder()
    .baseUrl("https://digital.doa.gov.lk/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
```

---

## What this replaces in the app

- CropTypes.ALL_CROPS (hardcoded ~20 crops) -> 106 live DoA crops
- No crop images currently -> variety images from S3
- No yield data currently -> averageYield + maturityTime per variety
- Listing quality ("GRADE_A") could become variety-based

## Weekend integration scope (Day 1)

1. Add CropixApiService + Hilt module (30 min)
2. Add DoaCropCategoryEntity + migration + CropDao (30 min)
3. CropixRepository.syncCrops() with Room fallback (30 min)
4. Update QuickListingScreen crop picker to use live DoA list (30 min)
   Total: ~2 hours. No API key needed.

## NOT available / blocked by 403

- Market prices: use WFP data or manual DoA weekly bulletin
- Yield forecasts: use averageYield + maturityTime from varieties (we DO have this)
- District geo data: blocked, use existing hardcoded 25-district list
