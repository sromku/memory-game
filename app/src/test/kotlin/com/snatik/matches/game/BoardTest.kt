package com.snatik.matches.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardTest {

    private val images = (100 until 140).toList()

    @Test
    fun `every tile has exactly one partner and partners share an image`() {
        for (difficulty in Difficulty.entries) {
            val board = Board.create(difficulty.tileCount, images, Random(seed = 42))
            assertEquals(difficulty.tileCount, board.tileCount)
            for (tile in 0 until board.tileCount) {
                val partner = board.partnerOf(tile)
                assertNotEquals("tile $tile paired with itself", tile, partner)
                assertEquals("partner of partner", tile, board.partnerOf(partner))
                assertEquals(board.imageOf(tile), board.imageOf(partner))
                assertTrue(board.isPair(tile, partner))
                assertFalse(board.isPair(tile, tile))
            }
        }
    }

    @Test
    fun `each image is used by exactly two tiles`() {
        val board = Board.create(50, images, Random(seed = 7))
        val usage = (0 until 50).groupingBy(board::imageOf).eachCount()
        assertEquals(25, usage.size)
        assertTrue(usage.values.all { it == 2 })
        assertEquals(25, board.images.size)
    }

    @Test
    fun `different seeds give different layouts`() {
        val a = Board.create(12, images, Random(1))
        val b = Board.create(12, images, Random(2))
        val layoutA = (0 until 12).map { a.imageOf(it) }
        val layoutB = (0 until 12).map { b.imageOf(it) }
        assertNotEquals(layoutA, layoutB)
    }

    @Test
    fun `rejects boards that cannot be built`() {
        assertThrows(IllegalArgumentException::class.java) { Board.create(5, images) }
        assertThrows(IllegalArgumentException::class.java) { Board.create(0, images) }
        assertThrows(IllegalArgumentException::class.java) { Board.create(6, listOf(1, 2)) }
    }
}
