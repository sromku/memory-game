package com.snatik.matches.game

import kotlin.random.Random

/**
 * An immutable, shuffled arrangement of tiles: which tile is paired with which, and which image
 * each tile shows. Images are opaque ints (drawable resource ids in the app, arbitrary in tests).
 */
class Board private constructor(
    private val partner: IntArray,
    private val image: IntArray,
) {
    val tileCount: Int get() = partner.size

    fun partnerOf(tile: Int): Int = partner[tile]

    fun imageOf(tile: Int): Int = image[tile]

    fun isPair(first: Int, second: Int): Boolean = first != second && partner[first] == second

    /** Distinct images used on this board, in tile order. */
    val images: Set<Int> get() = image.toSet()

    companion object {
        /**
         * Builds a board with [tileCount] tiles, picking [tileCount] / 2 images from [availableImages].
         */
        fun create(tileCount: Int, availableImages: List<Int>, random: Random = Random.Default): Board {
            require(tileCount > 0 && tileCount % 2 == 0) { "tileCount must be a positive even number, was $tileCount" }
            val pairCount = tileCount / 2
            require(availableImages.size >= pairCount) {
                "Need at least $pairCount images for $tileCount tiles, got ${availableImages.size}"
            }
            val positions = (0 until tileCount).shuffled(random)
            val chosenImages = availableImages.shuffled(random).take(pairCount)
            val partner = IntArray(tileCount)
            val image = IntArray(tileCount)
            for (pair in 0 until pairCount) {
                val a = positions[2 * pair]
                val b = positions[2 * pair + 1]
                partner[a] = b
                partner[b] = a
                image[a] = chosenImages[pair]
                image[b] = chosenImages[pair]
            }
            return Board(partner, image)
        }
    }
}
