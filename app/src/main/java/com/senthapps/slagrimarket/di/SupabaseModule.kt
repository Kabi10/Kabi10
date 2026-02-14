package com.senthapps.slagrimarket.di

import com.senthapps.slagrimarket.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient? {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY

        if (url.isBlank() || key.isBlank()) {
            return null
        }

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Realtime)
        }
    }
}
