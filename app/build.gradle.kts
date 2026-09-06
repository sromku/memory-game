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
        versionCode = 1009
        versionName = "1.1.1"
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

/*
 * Children's-app guard. The Play listing declares this app for children under the Families policy
 * and certifies COPPA/GDPR compliance (see CLAUDE.md). This task fails any build that introduces
 * something that would break that: a manifest permission, or a dependency from a family of SDKs
 * that collects data, shows ads, or phones home. Extend the lists deliberately, never quietly.
 */
val checkChildSafety = tasks.register("checkChildSafety") {
    group = "verification"
    description = "Fails if the app declares permissions or depends on data-collecting, ad, or network SDKs."
    val forbiddenDependencyPrefixes = listOf(
        "com.google.android.gms:play-services-ads", "com.google.android.gms:play-services-analytics",
        "com.google.android.gms:play-services-auth", "com.google.android.gms:play-services-games",
        "com.google.firebase", "com.facebook", "com.appsflyer", "com.adjust", "com.amplitude", "com.mixpanel",
        "com.segment", "io.sentry", "com.bugsnag", "com.unity3d.ads", "com.applovin", "com.ironsource",
        "com.chartboost", "com.vungle", "com.onesignal", "com.braze", "com.mopub", "com.squareup.okhttp3",
        "com.squareup.retrofit2", "io.ktor:ktor-client",
    )
    val manifest = layout.projectDirectory.file("src/main/AndroidManifest.xml")
    val dependencies = configurations.named("releaseRuntimeClasspath").map { config ->
        config.incoming.resolutionResult.allComponents.map { it.id.displayName }
    }
    doLast {
        val permissions = Regex("<uses-permission[^>]*android:name=\"([^\"]+)\"").findAll(manifest.asFile.readText()).map { it.groupValues[1] }.toList()
        if (permissions.isNotEmpty()) {
            throw GradleException("Children's-app guard: the manifest declares permissions $permissions. The app must declare none; see CLAUDE.md.")
        }
        val offending = dependencies.get().filter { dep -> forbiddenDependencyPrefixes.any { dep.startsWith(it) } }
        if (offending.isNotEmpty()) {
            throw GradleException("Children's-app guard: forbidden dependencies $offending. No ads, analytics, or network SDKs; see CLAUDE.md.")
        }
    }
}

tasks.named("preBuild") { dependsOn(checkChildSafety) }
