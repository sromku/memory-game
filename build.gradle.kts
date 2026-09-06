// Top-level build file. Plugin versions live in gradle/libs.versions.toml.
// "memory-game.tools" (tools/) adds the developer tasks regenerateArt and checkKeystore.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("memory-game.tools")
}
