plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val keystorePath: String = providers.gradleProperty("MEMORY_GAME_KEYSTORE").orNull.orEmpty()
val releaseSigningConfigured = keystorePath.isNotBlank()

android {
    namespace = "com.snatik.matches"
    compileSdk = 36

    defaultConfig {
        // Must stay "com.snatik.matches" so Google Play treats this as an update of the existing app.
        applicationId = "com.snatik.matches"
        minSdk = 24
        targetSdk = 36
        // The last published build was versionCode 1007 (versionName 1.01.001007). Every upload must be higher.
        versionCode = 1008
        versionName = "1.1.0"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.gradleProperty("MEMORY_GAME_STORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("MEMORY_GAME_KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("MEMORY_GAME_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable += listOf(
            // Text sizes are in dp on purpose: the labels sit inside fixed-size artwork and must not
            // grow with the system font setting.
            "SpUsage",
            // Phone and tablet buckets legitimately coincide at some sizes (80dp at 3x == 160dp at 1.5x).
            "IconDuplicatesConfig",
            // Version bumps are a deliberate decision, not a lint failure.
            "GradleDependency", "AndroidGradlePluginVersion",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
