package com.senthapps.slagrimarket.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.senthapps.slagrimarket.BuildConfig
import com.senthapps.slagrimarket.data.dao.ActivityDao
import com.senthapps.slagrimarket.data.dao.DoaCropDao
import com.senthapps.slagrimarket.data.dao.FavoriteDao
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.MarketPriceDao
import com.senthapps.slagrimarket.data.dao.MessageDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.ActivityConverters
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.DoaCropCategoryEntity
import com.senthapps.slagrimarket.data.model.DoaCropEntity
import com.senthapps.slagrimarket.data.model.Favorite
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.ListingConverters
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionConverters
import com.senthapps.slagrimarket.data.model.User

/**
 * Enhanced JaffnaMarketplaceDatabase with support for market prices, activities, and enhanced models
 * Version 2: Added MarketPrice and Activity entities, enhanced Listing and Transaction models
 */
@Database(
    entities = [
        User::class,
        Listing::class,
        Transaction::class,
        LocalOp::class,
        MarketPrice::class,
        Activity::class,
        com.senthapps.slagrimarket.data.model.Notification::class,
        com.senthapps.slagrimarket.data.model.Review::class,
        com.senthapps.slagrimarket.data.model.Message::class,
        com.senthapps.slagrimarket.data.model.Conversation::class,
        com.senthapps.slagrimarket.data.model.Favorite::class,
        DoaCropCategoryEntity::class,
        DoaCropEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(
    ListingConverters::class,
    TransactionConverters::class,
    ActivityConverters::class
)
abstract class JaffnaMarketplaceDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun transactionDao(): TransactionDao
    abstract fun localOpDao(): LocalOpDao
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun activityDao(): ActivityDao
    abstract fun notificationDao(): com.senthapps.slagrimarket.data.dao.NotificationDao
    abstract fun reviewDao(): com.senthapps.slagrimarket.data.dao.ReviewDao
    abstract fun messageDao(): com.senthapps.slagrimarket.data.dao.MessageDao
    abstract fun favoriteDao(): com.senthapps.slagrimarket.data.dao.FavoriteDao
    abstract fun doaCropDao(): DoaCropDao

    companion object {
        private const val DATABASE_NAME = "jaffna_marketplace_database"

        @Volatile
        private var INSTANCE: JaffnaMarketplaceDatabase? = null

        /**
         * Migration from version 1 to 2
         * Adds MarketPrice and Activity tables, enhances Listing and Transaction tables
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create market_prices table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `market_prices` (
                        `id` TEXT NOT NULL,
                        `cropType` TEXT NOT NULL,
                        `cropNameTamil` TEXT NOT NULL,
                        `cropNameEnglish` TEXT NOT NULL,
                        `cropNameSinhala` TEXT NOT NULL,
                        `currentPrice` REAL NOT NULL,
                        `previousPrice` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `trend` TEXT NOT NULL,
                        `changePercentage` REAL NOT NULL,
                        `changeAmount` REAL NOT NULL,
                        `location` TEXT NOT NULL,
                        `locationTamil` TEXT NOT NULL,
                        `locationSinhala` TEXT NOT NULL,
                        `lastUpdated` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `reliability` REAL NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Create indices for market_prices
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_market_prices_cropType` ON `market_prices` (`cropType`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_market_prices_trend` ON `market_prices` (`trend`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_market_prices_location` ON `market_prices` (`location`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_market_prices_lastUpdated` ON `market_prices` (`lastUpdated`)")

                // Create activities table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `activities` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `titleTamil` TEXT NOT NULL,
                        `titleSinhala` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `descriptionTamil` TEXT NOT NULL,
                        `descriptionSinhala` TEXT NOT NULL,
                        `relatedEntityType` TEXT,
                        `relatedEntityId` TEXT,
                        `status` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `timestamp` TEXT NOT NULL,
                        `metadata` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `isActionable` INTEGER NOT NULL,
                        `expiresAt` TEXT,
                        `createdAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create indices for activities
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_userId` ON `activities` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_activityType` ON `activities` (`activityType`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_status` ON `activities` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_timestamp` ON `activities` (`timestamp`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_relatedEntityId` ON `activities` (`relatedEntityId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_priority` ON `activities` (`priority`)")

                // Add new columns to listings table
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `cropNameTamil` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `cropNameEnglish` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `cropNameSinhala` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `availableFrom` TEXT NOT NULL DEFAULT (date('now'))")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `availableUntil` TEXT NOT NULL DEFAULT (date('now', '+7 days'))")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `locationTamil` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `locationSinhala` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `description` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `descriptionTamil` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `descriptionSinhala` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `pickupLocations` TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `syncStatus` TEXT NOT NULL DEFAULT 'PENDING'")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `viewCount` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `listings` ADD COLUMN `inquiryCount` INTEGER NOT NULL DEFAULT 0")

                // Add new indices for listings
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_syncStatus` ON `listings` (`syncStatus`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_availableFrom` ON `listings` (`availableFrom`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_listings_availableUntil` ON `listings` (`availableUntil`)")

                // Add new columns to transactions table
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `unit` TEXT NOT NULL DEFAULT 'kg'")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pricePerUnit` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pickupLocationTamil` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pickupLocationSinhala` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pickupTime` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `paymentStatus` TEXT NOT NULL DEFAULT 'PENDING'")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `notesTamil` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `notesSinhala` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `farmerRating` INTEGER")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `buyerRating` INTEGER")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `updatedAt` TEXT NOT NULL DEFAULT (datetime('now'))")
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `completedAt` TEXT")

                // Add new indices for transactions
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_paymentStatus` ON `transactions` (`paymentStatus`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_pickupDate` ON `transactions` (`pickupDate`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_updatedAt` ON `transactions` (`updatedAt`)")
            }
        }

        /**
         * Migration from version 2 to 3
         * Adds notifications table
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `relatedId` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `createdAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_userId` ON `notifications` (`userId`)")
            }
        }

        /**
         * Migration from version 3 to 4
         * Adds reviews table
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reviews` (
                        `id` TEXT NOT NULL,
                        `transactionId` TEXT NOT NULL,
                        `reviewerId` TEXT NOT NULL,
                        `reviewerName` TEXT NOT NULL,
                        `revieweeId` TEXT NOT NULL,
                        `rating` INTEGER NOT NULL,
                        `comment` TEXT NOT NULL,
                        `reviewType` TEXT NOT NULL,
                        `createdAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_revieweeId` ON `reviews` (`revieweeId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_transactionId` ON `reviews` (`transactionId`)")
            }
        }

        /**
         * Migration from version 4 to 6
         * Adds messages, conversations, favorites tables
         * Adds new columns to users, listings, transactions, activities
         */
        private val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create messages table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `id` TEXT NOT NULL,
                        `conversationId` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `senderName` TEXT NOT NULL,
                        `receiverId` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `messageType` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `createdAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)")

                // Create conversations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversations` (
                        `id` TEXT NOT NULL,
                        `listingId` TEXT,
                        `participant1Id` TEXT NOT NULL,
                        `participant1Name` TEXT NOT NULL,
                        `participant2Id` TEXT NOT NULL,
                        `participant2Name` TEXT NOT NULL,
                        `lastMessage` TEXT,
                        `lastMessageTime` TEXT,
                        `unreadCount` INTEGER NOT NULL,
                        `createdAt` TEXT NOT NULL,
                        `updatedAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_participant1Id` ON `conversations` (`participant1Id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_participant2Id` ON `conversations` (`participant2Id`)")

                // Create favorites table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `favorites` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `listingId` TEXT NOT NULL,
                        `createdAt` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_favorites_userId` ON `favorites` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_favorites_listingId` ON `favorites` (`listingId`)")

                // Add location column to users (nullable)
                try { database.execSQL("ALTER TABLE `users` ADD COLUMN `location` TEXT") } catch (_: Exception) {}

                // Add new columns to listings
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `farmerPhone` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `story` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `farmingMethods` TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `certifications` TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `harvestedAt` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `listings` ADD COLUMN `sustainabilityPractices` TEXT NOT NULL DEFAULT '[]'") } catch (_: Exception) {}

                // Add new columns to transactions
                try { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `sellerName` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `buyerName` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `sellerPhone` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { database.execSQL("ALTER TABLE `transactions` ADD COLUMN `buyerPhone` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            }
        }

        /**
         * Migration from version 6 to 7
         * Adds DoA CROPIX crop cache tables (doa_crop_categories, doa_crops)
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `doa_crop_categories` (
                        `id` INTEGER NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `doa_crops` (
                        `id` INTEGER NOT NULL,
                        `cropId` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `cropType` TEXT,
                        `scientificName` TEXT,
                        `categoryId` TEXT,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 7 to 8
         * Seeds sample listings for debug/demo use
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (BuildConfig.DEBUG) prepopulateListings(database)
            }
        }

        /**
         * Database callback for prepopulating data
         */
        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Prepopulate market prices with sample data
                prepopulateMarketPrices(db)
                if (BuildConfig.DEBUG) prepopulateListings(db)
            }
        }

        /**
         * Prepopulate market prices with sample data
         */
        private fun prepopulateMarketPrices(db: SupportSQLiteDatabase) {
            // Insert sample market prices from MarketPrice.SampleMarketPrices
            val samplePrices = listOf(
                "('mp_red_onion', 'red_onion', 'வெங்காயம்', 'Red Onion', 'රතු ළූණු', 120.0, 114.0, 'kg', 'UP', 5.26, 6.0, 'Jaffna Central Market', 'யாழ்ப்பாணம் மத்திய சந்தை', 'යාපනය මධ්‍යම වෙළඳපොළ', datetime('now'), 1, 'market_data', 1.0)",
                "('mp_chili', 'chili', 'மிளகாய்', 'Chili', 'මිරිස්', 280.0, 313.0, 'kg', 'DOWN', -10.54, -33.0, 'Chavakachcheri Market', 'சாவகச்சேரி சந்தை', 'චාවකච්චේරි වෙළඳපොළ', datetime('now'), 1, 'market_data', 1.0)",
                "('mp_tomato', 'tomato', 'தக்காளி', 'Tomato', 'තක්කාලි', 95.0, 95.0, 'kg', 'STABLE', 0.0, 0.0, 'Jaffna Central Market', 'யாழ்ப்பாணம் மத்திய சந்தை', 'යාපනය මධ්‍යම වෙළඳපොළ', datetime('now'), 1, 'market_data', 1.0)",
                "('mp_brinjal', 'brinjal', 'கத்தரிக்காய்', 'Brinjal', 'වම්බටු', 85.0, 82.5, 'kg', 'UP', 3.03, 2.5, 'Chavakachcheri Market', 'சாவகச்சேரி சந்தை', 'චාවකච්චේරි වෙළඳපොළ', datetime('now'), 1, 'market_data', 1.0)",
                "('mp_okra', 'okra', 'வெண்டைக்காய்', 'Okra', 'බණ්ඩක්කා', 110.0, 110.0, 'kg', 'STABLE', 0.0, 0.0, 'Jaffna Central Market', 'யாழ்ப்பாணம் மத்திய சந்தை', 'යාපනය මධ්‍යම වෙළඳපොළ', datetime('now'), 1, 'market_data', 1.0)"
            )

            samplePrices.forEach { values ->
                db.execSQL("""
                    INSERT OR IGNORE INTO market_prices
                    (id, cropType, cropNameTamil, cropNameEnglish, cropNameSinhala, currentPrice, previousPrice, unit, trend, changePercentage, changeAmount, location, locationTamil, locationSinhala, lastUpdated, isActive, source, reliability)
                    VALUES $values
                """.trimIndent())
            }
        }

        /**
         * Prepopulate sample listings covering all 4 categories for debug/demo use
         */
        private fun prepopulateListings(db: SupportSQLiteDatabase) {
            val today = "2026-03-16"
            val sampleListings = listOf(
                // VEGETABLES
                "('debug_listing_tomato', 'debug_user_123', 'Tomato', 'தக்காளி', 'Tomato', 'තක්කාලි', 50.0, 'kg', 95.0, 'A', '$today', 'Jaffna', 1, datetime('now'), datetime('now'))",
                "('debug_listing_carrot', 'debug_user_123', 'Carrot', 'கேரட்', 'Carrot', 'කැරට්', 30.0, 'kg', 80.0, 'A', '$today', 'Chavakachcheri', 1, datetime('now'), datetime('now'))",
                "('debug_listing_onion', 'debug_user_123', 'Onion', 'வெங்காயம்', 'Red Onion', 'රතු ළූණු', 40.0, 'kg', 120.0, 'B', '$today', 'Jaffna', 1, datetime('now'), datetime('now'))",
                // FRUITS
                "('debug_listing_mango', 'debug_user_123', 'Mango', 'மாம்பழம்', 'Mango', 'අඹ', 20.0, 'kg', 200.0, 'A', '$today', 'Kilinochchi', 1, datetime('now'), datetime('now'))",
                "('debug_listing_banana', 'debug_user_123', 'Banana', 'வாழைப்பழம்', 'Banana', 'කෙසෙල්', 60.0, 'kg', 60.0, 'A', '$today', 'Jaffna', 1, datetime('now'), datetime('now'))",
                // GRAINS
                "('debug_listing_rice', 'debug_user_123', 'Rice', 'அரிசி', 'Rice', 'හාල්', 100.0, 'kg', 150.0, 'A', '$today', 'Vavuniya', 1, datetime('now'), datetime('now'))",
                // LIVESTOCK
                "('debug_listing_chicken', 'debug_user_123', 'Chicken', 'கோழி', 'Chicken', 'කුකුල්', 10.0, 'kg', 700.0, 'A', '$today', 'Jaffna', 1, datetime('now'), datetime('now'))"
            )
            sampleListings.forEach { values ->
                db.execSQL("""
                    INSERT OR IGNORE INTO listings
                    (id, farmerId, cropType, cropNameTamil, cropNameEnglish, cropNameSinhala, quantity, unit, pricePerUnit, quality, harvestDate, location, isActive, createdAt, updatedAt)
                    VALUES $values
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): JaffnaMarketplaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JaffnaMarketplaceDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .addCallback(databaseCallback)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
