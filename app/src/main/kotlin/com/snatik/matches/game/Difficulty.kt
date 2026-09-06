package com.snatik.matches.game

/**
 * The six board sizes the game offers. [level] is the number shown to the player and is also the
 * value persisted in the score store, so it must never change for an existing entry.
 */
enum class Difficulty(val level: Int, val tileCount: Int, val columns: Int, val timeSeconds: Int) {
    LEVEL_1(level = 1, tileCount = 6, columns = 3, timeSeconds = 60),
    LEVEL_2(level = 2, tileCount = 12, columns = 4, timeSeconds = 90),
    LEVEL_3(level = 3, tileCount = 18, columns = 6, timeSeconds = 120),
    LEVEL_4(level = 4, tileCount = 28, columns = 7, timeSeconds = 150),
    LEVEL_5(level = 5, tileCount = 32, columns = 8, timeSeconds = 180),
    LEVEL_6(level = 6, tileCount = 50, columns = 10, timeSeconds = 210);

    val rows: Int get() = tileCount / columns
    val pairCount: Int get() = tileCount / 2

    /** The next harder level, or null when this is already the hardest one. */
    val next: Difficulty? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun fromLevel(level: Int): Difficulty = entries.first { it.level == level }
    }
}
