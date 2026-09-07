package com.snatik.matches.ui.menu

import com.snatik.matches.game.GameTheme
import java.util.TimeZone
import kotlin.random.Random

/**
 * The few animals standing on the grass of the menu. They are drawn from the day, so the same
 * friends are there every time the app opens today and different ones tomorrow; the first of
 * them is the friend of the day, who says hello when the menu appears.
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

    /** Today, counted in local days; nothing about the player, just the calendar. */
    val today: Long
        get() {
            val now = System.currentTimeMillis()
            return (now + TimeZone.getDefault().getOffset(now)) / DAY_MILLIS
        }

    val visitors: List<MenuVisitor> by lazy {
        val random = Random(today)
        val names = GameTheme.ANIMALS.characters.shuffled(random).take(3)
        val places = spots.shuffled(random).take(3)
        names.zip(places) { name, x -> MenuVisitor(name, x, 0.85f + random.nextFloat() * 0.3f) }
    }

    /** Today's hello, one of [greetings]. */
    fun greeting(greetings: Array<String>): String = greetings[(today % greetings.size).toInt()]

    private const val DAY_MILLIS = 86_400_000L
}
