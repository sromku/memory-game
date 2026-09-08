package com.snatik.matches.game.progression

import com.snatik.matches.game.GameTheme
import kotlin.random.Random

/** The friends album: which characters a theme gives away, in which order. */
object Friends {
    /** Rounds on a theme's roads per friend. */
    const val ROUNDS_PER_FRIEND = 10

    /** The theme's characters in the order they join the album; fixed, so a friend never changes. */
    fun order(theme: GameTheme): List<Int> = theme.characters.indices.shuffled(Random(theme.id * 7919L))
}
