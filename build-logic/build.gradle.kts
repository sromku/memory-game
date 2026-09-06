// Developer tooling for the memory game, applied to the root project as the "memory-game.tools" plugin.
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("tools") {
            id = "memory-game.tools"
            implementationClass = "ToolsPlugin"
        }
    }
}
