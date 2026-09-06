import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Registers the developer tasks:
 *
 *   ./gradlew regenerateArt -Prealesrgan=/path/to/realesrgan-ncnn-vulkan -Pvtracer=/path/to/vtracer [-Pcwebp=...]
 *       Regenerates every bitmap resource, launcher icon and vector card character from art/original
 *       (see ArtPipeline and CharacterTracer).
 *
 *   ./gradlew checkKeystore                                    the configured MEMORY_GAME_KEYSTORE
 *   ./gradlew checkKeystore -Pkeystore=/path/to/key.keystore   any keystore, certificates only
 *   ./gradlew checkKeystore -Pkeystore=... -Pguesses=file      test password guesses, one per line, never echoed
 */
class ToolsPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        tasks.register("regenerateArt") {
            group = "artwork"
            description = "Regenerates all bitmap resources and launcher icons from art/original."
            val realesrgan = providers.gradleProperty("realesrgan")
            val cwebp = providers.gradleProperty("cwebp").orElse("/opt/homebrew/bin/cwebp")
            val vtracer = providers.gradleProperty("vtracer")
            val root = layout.projectDirectory.asFile
            doLast {
                val upscaler = File(realesrgan.orNull ?: throw GradleException("Pass -Prealesrgan=/path/to/realesrgan-ncnn-vulkan (see README)"))
                if (!upscaler.canExecute()) throw GradleException("Not executable: $upscaler")
                ArtPipeline(root, upscaler, File(cwebp.get()), vtracer.orNull?.let(::File), logger::lifecycle).run()
            }
        }

        tasks.register("checkKeystore") {
            group = "release"
            description = "Inspects the signing keystore and verifies its passwords without printing them."
            val keystore = providers.gradleProperty("keystore").orElse(providers.gradleProperty("MEMORY_GAME_KEYSTORE"))
            val guesses = providers.gradleProperty("guesses")
            val storePassword = providers.gradleProperty("MEMORY_GAME_STORE_PASSWORD")
            val alias = providers.gradleProperty("MEMORY_GAME_KEY_ALIAS")
            val keyPassword = providers.gradleProperty("MEMORY_GAME_KEY_PASSWORD")
            doLast {
                val file = File(keystore.orNull?.takeIf { it.isNotBlank() } ?: throw GradleException("Pass -Pkeystore=... or set MEMORY_GAME_KEYSTORE"))
                logger.lifecycle(KeystoreCheck.describe(file))
                when {
                    guesses.isPresent ->
                        logger.lifecycle(KeystoreCheck.tryCandidates(file, File(guesses.get()).readLines().filter { it.isNotEmpty() }))
                    storePassword.orNull.isNullOrBlank() ->
                        logger.lifecycle("(set MEMORY_GAME_STORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD to verify them)")
                    else -> {
                        val failure = KeystoreCheck.verify(file, storePassword.get(), alias.get(), keyPassword.orNull ?: storePassword.get())
                        if (failure != null) throw GradleException(failure)
                        logger.lifecycle("PASS  store password, alias '${alias.get()}' and key password all verified. Ready to sign.")
                    }
                }
            }
        }
        Unit
    }
}
