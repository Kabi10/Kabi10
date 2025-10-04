package com.senthapps.slagrimarket.di

import android.content.Context
import androidx.room.Room
import com.senthapps.slagrimarket.data.dao.ActivityDao
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.MarketPriceDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.database.JaffnaMarketplaceDatabase
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enhanced DatabaseModule with support for new DAOs and proper migration handling
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJaffnaMarketplaceDatabase(
        @ApplicationContext context: Context
    ): JaffnaMarketplaceDatabase {
        return JaffnaMarketplaceDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: JaffnaMarketplaceDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideListingDao(database: JaffnaMarketplaceDatabase): ListingDao {
        return database.listingDao()
    }

    @Provides
    fun provideTransactionDao(database: JaffnaMarketplaceDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideLocalOpDao(database: JaffnaMarketplaceDatabase): LocalOpDao {
        return database.localOpDao()
    }

    @Provides
    fun provideMarketPriceDao(database: JaffnaMarketplaceDatabase): MarketPriceDao {
        return database.marketPriceDao()
    }

    @Provides
    fun provideActivityDao(database: JaffnaMarketplaceDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    fun provideNotificationDao(database: JaffnaMarketplaceDatabase): com.senthapps.slagrimarket.data.dao.NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideLanguagePreferences(
        @ApplicationContext context: Context
    ): LanguagePreferences {
        return LanguagePreferences(context)
    }
}
