// Top-level build file. Plugin versions live in gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

/*
 * Artwork. Every bitmap resource is generated from art/original by an AI upscaler; see
 * buildSrc/src/main/kotlin/ArtPipeline.kt and README.md.
 *
 *   ./gradlew regenerateArt -Prealesrgan=/path/to/realesrgan-ncnn-vulkan [-Pcwebp=/opt/homebrew/bin/cwebp]
 */
tasks.register("regenerateArt") {
    group = "artwork"
    description = "Regenerates all bitmap resources and launcher icons from art/original."
    val realesrgan = providers.gradleProperty("realesrgan")
    val cwebp = providers.gradleProperty("cwebp").orElse("/opt/homebrew/bin/cwebp")
    val root = layout.projectDirectory.asFile
    doLast {
        val upscaler = File(realesrgan.orNull ?: error("Pass -Prealesrgan=/path/to/realesrgan-ncnn-vulkan (see README)"))
        check(upscaler.canExecute()) { "Not executable: $upscaler" }
        ArtPipeline(root, upscaler, File(cwebp.get()), logger::lifecycle).run()
    }
}

/*
 * Signing keystore. Lists a keystore's certificates, checks the configured MEMORY_GAME_* properties,
 * or tests password guesses from a file (one per line, never echoed). See docs/RELEASE.md.
 *
 *   ./gradlew checkKeystore                                    # the configured MEMORY_GAME_KEYSTORE
 *   ./gradlew checkKeystore -Pkeystore=/path/to/key.keystore   # any keystore, certificates only
 *   ./gradlew checkKeystore -Pkeystore=... -Pguesses=~/guesses.txt
 */
tasks.register("checkKeystore") {
    group = "release"
    description = "Inspects the signing keystore and verifies its passwords without printing them."
    val keystore = providers.gradleProperty("keystore").orElse(providers.gradleProperty("MEMORY_GAME_KEYSTORE"))
    val guesses = providers.gradleProperty("guesses")
    val storePassword = providers.gradleProperty("MEMORY_GAME_STORE_PASSWORD")
    val alias = providers.gradleProperty("MEMORY_GAME_KEY_ALIAS")
    val keyPassword = providers.gradleProperty("MEMORY_GAME_KEY_PASSWORD")
    doLast {
        val file = File(keystore.orNull?.takeIf { it.isNotBlank() } ?: error("Pass -Pkeystore=... or set MEMORY_GAME_KEYSTORE"))
        logger.lifecycle(KeystoreCheck.describe(file))
        when {
            guesses.isPresent -> logger.lifecycle(KeystoreCheck.tryCandidates(file, File(guesses.get()).readLines().filter { it.isNotEmpty() }))
            storePassword.orNull.isNullOrBlank() -> logger.lifecycle("(set MEMORY_GAME_STORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD to verify them)")
            else -> {
                val failure = KeystoreCheck.verify(file, storePassword.get(), alias.get(), keyPassword.orNull ?: storePassword.get())
                if (failure != null) throw GradleException(failure)
                logger.lifecycle("PASS  store password, alias '${alias.get()}' and key password all verified. Ready to sign.")
            }
        }
    }
}
