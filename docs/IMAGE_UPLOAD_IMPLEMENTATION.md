# Image Upload Implementation Guide

## Overview
This document describes the complete image upload implementation for the Jaffna Agricultural Marketplace app, including both Android client and backend server components.

## Architecture

### Backend (Node.js + Supabase)

#### Storage Service
- **Location**: `backend/src/services/database.js`
- **Storage Provider**: Supabase Storage
- **Methods**:
  - `uploadFile(bucket, path, file, options)` - Upload file to Supabase Storage
  - `getFileUrl(bucket, path)` - Get public URL for uploaded file
  - `deleteFile(bucket, path)` - Delete file from storage

#### API Endpoints
- **Location**: `backend/src/routes/listings.js`

##### POST /api/v1/listings/:id/images
Upload images for a listing
```json
Request:
{
  "images": ["data:image/jpeg;base64,...", "data:image/jpeg;base64,..."]
}

Response:
{
  "success": true,
  "data": {
    "images": ["url1", "url2"]
  }
}
```

**Features**:
- Maximum 5 images per listing
- Validates listing ownership
- Stores images array in database
- Returns updated images list

### Android Client (Kotlin + Jetpack Compose)

#### Components

##### 1. ImagePicker Component
**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/common/ImagePicker.kt`

**Features**:
- Multi-image selection from gallery
- Image preview with thumbnails
- Remove individual images
- Maximum 5 images limit
- Trilingual support (English, Tamil, Sinhala)
- Empty state with add button
- Responsive grid layout

**Usage**:
```kotlin
ImagePicker(
    images = uiState.images,
    onImagesSelected = viewModel::updateImages,
    onImageRemoved = viewModel::removeImage,
    maxImages = 5,
    currentLanguage = currentLanguage
)
```

##### 2. Image Upload Utility
**Location**: `app/src/main/java/com/senthapps/slagrimarket/util/ImageUploadUtil.kt`

**Features**:
- URI to File conversion
- Multipart body preparation
- File name extraction
- Image compression (placeholder for future implementation)

##### 3. Repository Layer
**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/repository/ListingRepository.kt`

**Method**: `uploadImages(listingId, imageUris, context)`

**Current Implementation**:
- Converts URIs to base64 strings
- Handles multiple images
- Error handling and logging
- Returns list of image URLs/data

**Future Enhancement**:
```kotlin
// Upload to Supabase Storage via API
val response = storageApiService.uploadImages(
    listingId = listingId,
    request = UploadImagesRequest(images = base64Images)
)
```

##### 4. ViewModel
**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/listings/CreateListingViewModel.kt`

**Methods**:
- `updateImages(images: List<Uri>)` - Update selected images
- `removeImage(imageUri: Uri)` - Remove specific image
- `createListing()` - Uploads images before creating listing

**State**:
```kotlin
data class CreateListingUiState(
    val images: List<Uri> = emptyList(),
    // ... other fields
)
```

##### 5. Data Model
**Location**: `app/src/main/java/com/senthapps/slagrimarket/data/model/Listing.kt`

**Field**:
```kotlin
@Json(name = "images")
val images: List<String> = emptyList()
```

##### 6. Image Display
**Location**: `app/src/main/java/com/senthapps/slagrimarket/ui/listings/ListingDetailScreen.kt`

**Component**: `ImageGallery`
- Displays images in horizontal scrollable row
- Uses Coil for image loading
- Fallback to placeholder when no images
- Crop emoji display for empty state

## Flow Diagram

```
User selects images
    ↓
ImagePicker component
    ↓
ViewModel.updateImages()
    ↓
UI State updated
    ↓
User submits form
    ↓
ViewModel.createListing()
    ↓
Repository.uploadImages()
    ↓
Convert URIs to base64
    ↓
Repository.createListing(imageUrls)
    ↓
API POST /v1/listings
    ↓
Listing created with images
    ↓
Success response
```

## Configuration

### Backend Setup

1. **Supabase Storage Bucket**:
   - Create bucket named `listing-images`
   - Set public access for read
   - Configure CORS for your domain

2. **Environment Variables**:
```env
SUPABASE_URL=your_supabase_url
SUPABASE_ANON_KEY=your_anon_key
SUPABASE_SERVICE_KEY=your_service_key
```

3. **Database Schema**:
```sql
ALTER TABLE listings 
ADD COLUMN images TEXT[] DEFAULT '{}';
```

### Android Setup

1. **Dependencies** (already in `build.gradle.kts`):
```kotlin
implementation("io.coil-kt:coil-compose:2.x.x")
implementation("com.squareup.retrofit2:retrofit:2.x.x")
```

2. **Permissions** (already in `AndroidManifest.xml`):
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## Current Status

### ✅ Implemented
- Image selection from gallery
- Multi-image support (up to 5)
- Image preview with remove functionality
- Trilingual UI
- Image state management in ViewModel
- Base64 encoding for upload
- Backend API endpoint for image upload
- Image display in listing details
- Supabase Storage integration in backend

### 🚧 To Be Implemented

#### High Priority
1. **Actual Cloud Upload**:
   - Upload base64 images to Supabase Storage
   - Return public URLs instead of base64
   - Implement progress indicators

2. **Image Compression**:
   - Compress images before upload
   - Target: max 1MB per image
   - Maintain aspect ratio
   - Quality: 85%

3. **Camera Capture**:
   - Add camera permission
   - Implement camera capture
   - Save to temp file
   - Add to image list

#### Medium Priority
4. **Image Optimization**:
   - Resize to max 1920x1080
   - Generate thumbnails
   - WebP format support

5. **Upload Progress**:
   - Show upload progress bar
   - Cancel upload functionality
   - Retry failed uploads

6. **Error Handling**:
   - Network error recovery
   - File size validation
   - Format validation
   - User-friendly error messages

#### Low Priority
7. **Advanced Features**:
   - Image reordering
   - Image cropping
   - Filters/adjustments
   - Batch upload optimization

## Testing

### Manual Testing Checklist
- [ ] Select single image from gallery
- [ ] Select multiple images (up to 5)
- [ ] Try to select more than 5 images
- [ ] Remove individual images
- [ ] Create listing with images
- [ ] View listing with images
- [ ] Create listing without images
- [ ] Test on different Android versions
- [ ] Test with different image formats (JPEG, PNG, WebP)
- [ ] Test with large images (>5MB)
- [ ] Test offline behavior

### Backend Testing
```bash
# Test image upload endpoint
curl -X POST http://localhost:3000/api/v1/listings/{id}/images \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "images": ["data:image/jpeg;base64,/9j/4AAQ..."]
  }'
```

## Performance Considerations

### Current Implementation
- Base64 encoding increases payload size by ~33%
- No compression applied
- All images loaded in memory

### Recommended Optimizations
1. **Compress before encoding**: Reduce file size by 70-80%
2. **Stream upload**: Don't load entire file in memory
3. **Parallel uploads**: Upload multiple images concurrently
4. **CDN caching**: Use Supabase CDN for fast delivery
5. **Lazy loading**: Load images on demand in list views

## Security

### Current Measures
- User authentication required
- Ownership validation
- Maximum file count limit

### Recommended Additions
1. **File type validation**: Only allow JPEG, PNG, WebP
2. **File size limit**: Max 5MB per image
3. **Content scanning**: Scan for inappropriate content
4. **Rate limiting**: Prevent abuse
5. **Signed URLs**: Use temporary signed URLs for uploads

## Troubleshooting

### Common Issues

**Issue**: Images not displaying
- Check image URLs are valid
- Verify Supabase bucket is public
- Check CORS configuration

**Issue**: Upload fails
- Verify authentication token
- Check file size limits
- Ensure proper permissions

**Issue**: Out of memory
- Implement image compression
- Use streaming upload
- Reduce image resolution

## Future Enhancements

1. **Video Support**: Allow short video clips
2. **360° Photos**: Support panoramic images
3. **AI Features**: Auto-crop, quality detection
4. **Social Sharing**: Share listings with images
5. **Image Analytics**: Track which images get most views

## References

- [Supabase Storage Documentation](https://supabase.com/docs/guides/storage)
- [Coil Image Loading](https://coil-kt.github.io/coil/)
- [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)
- [Retrofit Multipart Upload](https://square.github.io/retrofit/)
