package com.snatik.matches.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DifficultyTest {

    @Test
    fun `grids are full rectangles with an even number of tiles`() {
        Difficulty.entries.forEach {
            assertEquals(it.name, 0, it.tileCount % 2)
            assertEquals(it.name, it.tileCount, it.rows * it.columns)
            assertEquals(it.name, it.tileCount / 2, it.pairCount)
        }
    }

    @Test
    fun `levels are numbered one to six in order`() {
        assertEquals((1..6).toList(), Difficulty.entries.map { it.level })
        assertEquals(Difficulty.LEVEL_3, Difficulty.fromLevel(3))
        assertEquals(Difficulty.LEVEL_2, Difficulty.LEVEL_1.next)
        assertNull(Difficulty.LEVEL_6.next)
    }
}
