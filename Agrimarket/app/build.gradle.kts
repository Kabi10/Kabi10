plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf.plugin)
    jacoco
}

// Load local.properties for secrets
import java.io.FileInputStream
import java.util.Properties
val localProperties = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    FileInputStream(localPropsFile).use { localProperties.load(it) }
}

android {
    namespace = "com.senthapps.slagrimarket"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.senthapps.slagrimarket"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "com.senthapps.slagrimarket.HiltTestRunner"

        // Room schema export directory
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    signingConfigs {
        create("release") {
            // Try environment variables first (for CI/CD), then local.properties
            val keystorePath = System.getenv("KEYSTORE_PATH")
                ?: localProperties.getProperty("KEYSTORE_PATH")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties.getProperty("KEYSTORE_PASSWORD")
            val keyAliasValue = System.getenv("KEY_ALIAS")
                ?: localProperties.getProperty("KEY_ALIAS", "agrimarket")
            val keyPasswordValue = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("KEY_PASSWORD")

            // Validate signing configuration
            if (keystorePath.isNullOrBlank() || keystorePassword.isNullOrBlank() || keyPasswordValue.isNullOrBlank()) {
                logger.warn("""
                    ⚠️  WARNING: Release signing is not properly configured.

                    To sign release builds, set either:

                    1. Environment Variables (for CI/CD):
                       export KEYSTORE_PATH=/path/to/keystore.jks
                       export KEYSTORE_PASSWORD=your_keystore_password
                       export KEY_ALIAS=agrimarket
                       export KEY_PASSWORD=your_key_password

                    2. local.properties (for local development):
                       KEYSTORE_PATH=/path/to/keystore.jks
                       KEYSTORE_PASSWORD=your_keystore_password
                       KEY_ALIAS=agrimarket
                       KEY_PASSWORD=your_key_password

                    Release builds will be unsigned until this is configured.
                """.trimIndent())
            }

            // Set signing config (will be null/empty if not configured)
            storeFile = keystorePath?.let { rootProject.file(it) }
            storePassword = keystorePassword
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Removed applicationIdSuffix for Firebase compatibility
            versionNameSuffix = "-debug"

            // API Configuration for debug builds
            // Default: 10.0.2.2:3000 works on Android emulator (routes to host localhost)
            // Physical device: set DEBUG_BASE_URL in local.properties, e.g.:
            //   DEBUG_BASE_URL=https://agrimarket-kappa.vercel.app/api/
            buildConfigField(
                "String", "BASE_URL",
                "\"${localProperties.getProperty("DEBUG_BASE_URL", "http://10.0.2.2:3000/api/")}\""
            )
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Apply signing configuration
            signingConfig = signingConfigs.getByName("release")

            // API Configuration for release builds
            buildConfigField("String", "BASE_URL", "\"https://backend-psi-tan-18.vercel.app/api/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Library Desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Architecture Components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.codegen)

    // Image Loading
    implementation(libs.coil.compose)

    // Maps
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Accompanist (Google Compose utilities)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Security
    implementation(libs.androidx.security.crypto)

    // Logging
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.perf)

    // Supabase Realtime (for chat)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.android)

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // LeakCanary for memory leak detection in debug builds
    debugImplementation(libs.leakcanary)
}

// JaCoCo Test Coverage Configuration
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**",
        "**/*_Factory*",
        "**/*_MembersInjector*",
        "**/Hilt_*",
        "**/*_HiltModules*",
        "**/Dagger*Component*",
        "**/*Module_*",
        "**/ComposableSingletons*",
        "**/*Kt$*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec")
    })
}