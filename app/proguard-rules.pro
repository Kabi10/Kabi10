# =====================================================
# Srilanka Farmers Marketplace - ProGuard Rules
# =====================================================

# Keep source file and line number information for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# =====================================================
# Retrofit / OkHttp
# =====================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# =====================================================
# Moshi (JSON Serialization)
# =====================================================
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
# Keep Moshi Kotlin adapter factory
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}
-keepnames @kotlin.Metadata class com.senthapps.slagrimarket.data.**

# =====================================================
# Enhanced Moshi Rules for Data Models
# =====================================================
# Keep all data classes in data.model package with JSON annotations
-keep @com.squareup.moshi.JsonClass class com.senthapps.slagrimarket.data.model.** { *; }
-keep class com.senthapps.slagrimarket.data.model.**JsonAdapter {
    <init>(...);
    <fields>;
}

# Keep Moshi adapters generated for our models
-keep class com.senthapps.slagrimarket.data.model.**$JsonAdapter {
    <init>(...);
    <fields>;
}

# Keep all type converters (Room uses reflection)
-keep class **Converters {
    <init>();
    public <methods>;
}

# Keep ListingConverters specifically
-keep class com.senthapps.slagrimarket.data.model.ListingConverters {
    <init>();
    public <methods>;
}

# =====================================================
# Room Database
# =====================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# =====================================================
# Firebase
# =====================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Crashlytics
-keepattributes *Annotation*
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# =====================================================
# Hilt / Dagger
# =====================================================
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# =====================================================
# Kotlin
# =====================================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# =====================================================
# App-specific Models (Keep for JSON serialization)
# =====================================================
-keep class com.senthapps.slagrimarket.data.model.** { *; }
-keep class com.senthapps.slagrimarket.data.remote.dto.** { *; }
-keep class com.senthapps.slagrimarket.data.api.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =====================================================
# Enhanced Enum Handling for JSON Serialization
# =====================================================
# Keep all enums with Json annotations
-keepclassmembers enum * {
    @com.squareup.moshi.Json <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
    ** name();
}

# Keep specific app enums that have methods used in UI
-keepclassmembers enum com.senthapps.slagrimarket.data.model.QualityGrade {
    public <methods>;
}
-keepclassmembers enum com.senthapps.slagrimarket.data.model.SyncStatus {
    public <methods>;
}

# =====================================================
# Google Maps
# =====================================================
-keep class com.google.android.libraries.maps.** { *; }
-keep class com.google.maps.android.** { *; }

# =====================================================
# Coil Image Loading
# =====================================================
-dontwarn coil.**
-keep class coil.** { *; }

# =====================================================
# AndroidX / Jetpack
# =====================================================
-keep class androidx.** { *; }
-dontwarn androidx.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Navigation
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# =====================================================
# Security
# =====================================================
# Keep security classes
-keep class androidx.security.crypto.** { *; }

# =====================================================
# Timber Logging (Remove in release)
# =====================================================
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# =====================================================
# Supabase
# =====================================================
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
-keep class io.github.jan_tennert.supabase.** { *; }
-dontwarn io.github.jan_tennert.supabase.**

# =====================================================
# Ktor Client
# =====================================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# =====================================================
# Enhanced Ktor 3.x and Supabase 3.x Support
# =====================================================
# Ktor 3.x serialization support
-keep class io.ktor.client.** { *; }
-keep class io.ktor.http.** { *; }
-keep class io.ktor.util.** { *; }
-dontwarn io.ktor.client.plugins.**
-dontwarn io.ktor.serialization.**

# Supabase 3.x realtime and postgrest
-keep class io.github.jan.supabase.realtime.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.annotations.**

# Websockets for Supabase Realtime
-keep class io.ktor.websocket.** { *; }
-dontwarn org.slf4j.**

# Remove debug logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# =====================================================
# General Android
# =====================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views for animations
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}