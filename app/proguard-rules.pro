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