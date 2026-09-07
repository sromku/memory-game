package com.snatik.matches.ui.menu

import com.snatik.matches.game.GameTheme
import kotlin.random.Random

/**
 * The few animals standing on the grass of the menu. Picked once per process, so the same friends
 * are there for as long as the app lives, and different ones the next time it is opened.
 */
class MenuVisitor(
    /** Character asset name, from the animals theme. */
    val name: String,
    /** Horizontal position as a fraction of the drawn scene width. */
    val x: Float,
    /** Size relative to the standard visitor height, for a little variety. */
    val scale: Float,
)

object MenuVisitors {
    /** Standing spots on the grass, left and right of the buttons in the middle. */
    private val spots = listOf(0.07f, 0.17f, 0.28f, 0.72f, 0.83f, 0.93f)

    val visitors: List<MenuVisitor> by lazy {
        val random = Random(System.nanoTime())
        val names = GameTheme.ANIMALS.characters.shuffled(random).take(3)
        val places = spots.shuffled(random).take(3).sorted()
        names.zip(places) { name, x -> MenuVisitor(name, x, 0.85f + random.nextFloat() * 0.3f) }
    }
}
