# Activity Feed Backend Implementation

**Status**: ✅ **Code Complete** - Migration Pending
**Created**: 2026-02-16
**Phase**: Phase 4 - Android API Wiring

---

## 📊 Implementation Summary

### ✅ Files Created

1. **`backend/src/database/schema-activities.sql`**
   - Complete SQL schema for activities table
   - Trilingual support (English, Tamil, Sinhala)
   - 20+ activity types (listings, transactions, messages, reviews, price alerts, etc.)
   - Status workflow: ACTIVE → DISMISSED → ARCHIVED
   - Priority levels: LOW, NORMAL, HIGH, URGENT
   - 10 optimized indexes for query performance

2. **`backend/src/routes/activities.js`** (650+ lines)
   - **12 endpoints** matching `ActivityApiService.kt` exactly:
     - `GET /v1/activities` - List activities (paginated, filterable)
     - `GET /v1/activities/:id` - Get single activity
     - `GET /v1/activities/unread-count` - Get unread count
     - `GET /v1/activities/actionable-count` - Get actionable count
     - `GET /v1/activities/summary` - Get summary with distributions
     - `GET /v1/activities/recent` - Get recent activities
     - `POST /v1/activities` - Create activity
     - `PATCH /v1/activities/:id/read` - Mark as read
     - `PATCH /v1/activities/mark-all-read` - Mark all as read
     - `PATCH /v1/activities/:id/dismiss` - Dismiss activity
     - `PATCH /v1/activities/:id/archive` - Archive activity
     - `DELETE /v1/activities/:id` - Delete activity

3. **`backend/src/database/migrate-activities.js`**
   - Migration script with rollback support
   - Transaction-safe (BEGIN/COMMIT/ROLLBACK)
   - Verification checks for table and indexes

4. **`supabase/migrations/20260216000001_create_activities_table.sql`**
   - Proper Supabase migration following existing patterns
   - Row Level Security (RLS) policies for user data isolation
   - Trigger for `updated_at` timestamp auto-update
   - Cleanup function for expired activities
   - Comprehensive comments and documentation

### ✅ Files Modified

5. **`backend/src/server.js`**
   - Imported `activitiesRoutes`
   - Registered route: `apiRouter.use('/activities', authenticateToken, activitiesRoutes)`
   - Updated root endpoint documentation

---

## 🗄️ Database Schema

### Activities Table Structure

| Column                | Type         | Description                                  |
| --------------------- | ------------ | -------------------------------------------- |
| `id`                  | UUID         | Primary key (auto-generated)                 |
| `user_id`             | UUID         | Foreign key to users table                   |
| `activity_type`       | VARCHAR(50)  | Type of activity (enum)                      |
| `title`               | VARCHAR(255) | Activity title (English)                     |
| `title_tamil`         | VARCHAR(255) | Activity title (Tamil)                       |
| `title_sinhala`       | VARCHAR(255) | Activity title (Sinhala)                     |
| `description`         | TEXT         | Activity description (English)               |
| `description_tamil`   | TEXT         | Activity description (Tamil)                 |
| `description_sinhala` | TEXT         | Activity description (Sinhala)               |
| `related_entity_type` | VARCHAR(50)  | Entity type (e.g., "listing", "transaction") |
| `related_entity_id`   | UUID         | Entity ID                                    |
| `priority`            | VARCHAR(20)  | LOW, NORMAL, HIGH, URGENT                    |
| `status`              | VARCHAR(20)  | ACTIVE, DISMISSED, ARCHIVED                  |
| `is_read`             | BOOLEAN      | Read status (default: false)                 |
| `is_actionable`       | BOOLEAN      | Requires user action (default: false)        |
| `expires_at`          | TIMESTAMPTZ  | Optional expiration time                     |
| `created_at`          | TIMESTAMPTZ  | Creation timestamp                           |
| `updated_at`          | TIMESTAMPTZ  | Last update timestamp                        |
| `read_at`             | TIMESTAMPTZ  | When marked as read                          |
| `dismissed_at`        | TIMESTAMPTZ  | When dismissed                               |
| `archived_at`         | TIMESTAMPTZ  | When archived                                |
| `metadata`            | JSONB        | Flexible JSON storage                        |

### Activity Types (20 supported)

```sql
'NEW_LISTING', 'LISTING_UPDATE', 'LISTING_EXPIRED', 'LISTING_SOLD',
'NEW_TRANSACTION', 'TRANSACTION_UPDATE', 'TRANSACTION_COMPLETED', 'TRANSACTION_CANCELLED',
'NEW_MESSAGE', 'MESSAGE_UNREAD',
'NEW_REVIEW', 'REVIEW_RECEIVED',
'PRICE_ALERT', 'PRICE_DROP', 'PRICE_SURGE',
'SYSTEM_ANNOUNCEMENT', 'ACCOUNT_UPDATE', 'VERIFICATION_REQUIRED',
'FAVORITE_AVAILABLE', 'FAVORITE_PRICE_DROP',
'OTHER'
```

### Indexes (10 optimized)

- `idx_activities_user_id` - User-based filtering
- `idx_activities_type` - Activity type filtering
- `idx_activities_status` - Status filtering
- `idx_activities_priority` - Priority sorting
- `idx_activities_unread` - Unread activities (partial index)
- `idx_activities_actionable` - Actionable activities (partial index)
- `idx_activities_created_at` - Chronological sorting
- `idx_activities_expires_at` - Expiration tracking (partial index)
- `idx_activities_user_status` - Combined user + status + time
- `idx_activities_user_filters` - Combined user + status + read + time

---

## 🔌 API Endpoints

All endpoints require authentication (`Authorization: Bearer <JWT>`).

### 1. GET /api/v1/activities

**Query Parameters**:

- `activityType` (string) - Filter by activity type
- `status` (string) - Filter by status (ACTIVE, DISMISSED, ARCHIVED)
- `priority` (string) - Filter by priority (LOW, NORMAL, HIGH, URGENT)
- `isRead` (boolean) - Filter by read status
- `isActionable` (boolean) - Filter actionable activities
- `fromDate` (ISO8601) - Filter from date
- `toDate` (ISO8601) - Filter to date
- `page` (int, default: 1) - Page number
- `limit` (int, default: 20, max: 100) - Results per page
- `sortBy` (string, default: "timestamp") - Sort field (timestamp, priority, type)
- `sortOrder` (string, default: "desc") - Sort order (asc, desc)
- `language` (string, default: "en") - Language for titles/descriptions (en, ta, si)

**Response**:

```json
{
  "activities": [
    /* Activity objects */
  ],
  "totalCount": 150,
  "page": 1,
  "totalPages": 8,
  "hasNext": true,
  "hasPrevious": false,
  "lastUpdated": "2026-02-16T10:30:00Z"
}
```

### 2. GET /api/v1/activities/:id

**Response**: Single Activity object

### 3. GET /api/v1/activities/unread-count

**Response**:

```json
{
  "count": 12
}
```

### 4. GET /api/v1/activities/actionable-count

**Response**:

```json
{
  "count": 5
}
```

### 5. GET /api/v1/activities/summary

**Query Parameters**:

- `timeframe` (string, default: "24h") - Time window (24h, 7d, 30d)

**Response**:

```json
{
  "totalActivities": 150,
  "unreadCount": 12,
  "actionableCount": 5,
  "typeDistribution": {
    "NEW_LISTING": 45,
    "NEW_MESSAGE": 30,
    "PRICE_ALERT": 25
  },
  "priorityDistribution": {
    "HIGH": 10,
    "NORMAL": 120,
    "LOW": 20
  },
  "recentActivities": [
    /* Last 5 activities */
  ]
}
```

### 6. GET /api/v1/activities/recent

**Query Parameters**:

- `limit` (int, default: 10, max: 50) - Number of recent activities

**Response**: Same as GET /activities (but only recent, active activities)

### 7. POST /api/v1/activities

**Request Body**:

```json
{
  "userId": "uuid",
  "activityType": "NEW_LISTING",
  "title": "New tomato listing",
  "titleTamil": "புதிய தக்காளி பட்டியல்",
  "titleSinhala": "නව තක්කාලි ලැයිස්තුව",
  "description": "New listing created",
  "descriptionTamil": "புதிய பட்டியல் உருவாக்கப்பட்டது",
  "descriptionSinhala": "නව ලැයිස්තුව නිර්මාණය කරන ලදී",
  "relatedEntityType": "listing",
  "relatedEntityId": "uuid",
  "priority": "NORMAL",
  "isActionable": false,
  "expiresAt": "2026-03-16T00:00:00Z",
  "metadata": { "cropType": "tomato", "quantity": 50 }
}
```

**Response**: Created Activity object (201 Created)

### 8-12. PATCH/DELETE Endpoints

- **PATCH /api/v1/activities/:id/read** - Returns `{ "success": true }`
- **PATCH /api/v1/activities/mark-all-read** - Returns `{ "success": true, "markedCount": 12 }`
- **PATCH /api/v1/activities/:id/dismiss** - Returns `{ "success": true }`
- **PATCH /api/v1/activities/:id/archive** - Returns `{ "success": true }`
- **DELETE /api/v1/activities/:id** - Returns `{ "success": true, "message": "Activity deleted successfully" }`

---

## 🔐 Security

### Authentication

- All endpoints require JWT authentication
- Uses `authenticateToken` middleware
- User ID extracted from `req.user.userId`

### Row Level Security (RLS)

- Supabase RLS policies ensure users can only access their own activities
- Policies for SELECT, INSERT, UPDATE, DELETE
- Service role can bypass RLS for admin operations

### Validation

- Input validation using `express-validator`
- UUID validation for IDs
- Enum validation for activity types, status, priority
- Date validation for ISO8601 timestamps

---

## ⚠️ Migration Status

### ✅ Code Implementation: COMPLETE

All backend route code is implemented and registered in the server.

### ⏳ Database Migration: PENDING

**Migration file created**: `supabase/migrations/20260216000001_create_activities_table.sql`

**To apply the migration**:

#### Option 1: Supabase CLI (Recommended)

```bash
cd supabase
npx supabase db push
```

#### Option 2: Supabase Dashboard

1. Go to Supabase project dashboard
2. Navigate to Database → Migrations
3. Upload `20260216000001_create_activities_table.sql`
4. Run migration

#### Option 3: Direct SQL (Supabase SQL Editor)

1. Open Supabase SQL Editor
2. Copy contents of `supabase/migrations/20260216000001_create_activities_table.sql`
3. Execute SQL

---

## ✅ Verification Checklist

After migration is applied:

- [ ] **Table Created**: Verify `activities` table exists in Supabase dashboard
- [ ] **Indexes Created**: Check 10 indexes were created
- [ ] **RLS Policies**: Verify 4 RLS policies are active
- [ ] **Trigger**: Verify `update_activities_updated_at` trigger exists
- [ ] **Function**: Verify `cleanup_expired_activities()` function exists

### Test Endpoints:

```bash
# 1. Create test activity (requires auth token)
curl -X POST https://backend-psi-tan-18.vercel.app/api/v1/activities \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "YOUR_USER_ID",
    "activityType": "NEW_LISTING",
    "title": "Test Activity",
    "description": "This is a test activity"
  }'

# 2. Get activities
curl -X GET https://backend-psi-tan-18.vercel.app/api/v1/activities \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. Get unread count
curl -X GET https://backend-psi-tan-18.vercel.app/api/v1/activities/unread-count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Get summary
curl -X GET https://backend-psi-tan-18.vercel.app/api/v1/activities/summary?timeframe=24h \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 Next Steps

1. **Apply Migration**: Run `npx supabase db push` in the `supabase/` directory
2. **Test Endpoints**: Use the curl commands above to verify endpoints work
3. **Android Integration**: The Android app already has `ActivityApiService.kt` - no changes needed
4. **Generate Sample Data**: Create a few test activities to populate the feed
5. **Monitor Performance**: Check query performance with real data (indexes should handle 10,000+ activities efficiently)

---

## 🔄 Comparison: Activities vs Notifications

| Feature          | Activities                                    | Notifications                 |
| ---------------- | --------------------------------------------- | ----------------------------- |
| **Purpose**      | Historical activity feed                      | Real-time alerts              |
| **Lifecycle**    | ACTIVE → DISMISSED → ARCHIVED                 | Created → Read → Deleted      |
| **Retention**    | Long-term (historical record)                 | Short-term (ephemeral)        |
| **Metadata**     | Rich (priority, actionable, expiration)       | Simple (type, title, message) |
| **Multilingual** | Full (title + description in 3 languages)     | Partial (single language)     |
| **Filtering**    | Advanced (type, status, priority, date range) | Basic (read status)           |
| **Use Cases**    | "Your listing was viewed 50 times this week"  | "New message from buyer"      |

---

## 📚 References

- **Android Contract**: `app/src/main/java/com/senthapps/slagrimarket/data/api/ActivityApiService.kt`
- **Backend Routes**: `backend/src/routes/activities.js`
- **Database Schema**: `supabase/migrations/20260216000001_create_activities_table.sql`
- **Server Registration**: `backend/src/server.js` (lines 32, 135, 162)

---

**Status**: ✅ **Ready for Migration & Testing**
**Estimated Testing Time**: 30 minutes
**Production Ready**: After successful migration verification
