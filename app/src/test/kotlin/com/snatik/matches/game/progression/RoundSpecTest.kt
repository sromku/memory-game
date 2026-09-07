package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RoundSpecTest {

    @Test
    fun `time tightens from the full time at round 1 to 70 percent at round 40`() {
        assertEquals(Difficulty.LEVEL_1.timeSeconds, RoundSpec(Difficulty.LEVEL_1, 1).timeSeconds)
        assertEquals((Difficulty.LEVEL_1.timeSeconds * 0.7f).toInt(), RoundSpec(Difficulty.LEVEL_1, 40).timeSeconds)
        val times = Road.rounds(Difficulty.LEVEL_6).map { it.timeSeconds }
        assertEquals("never gets looser along the road", times.sortedDescending(), times)
        assertEquals(Difficulty.LEVEL_6.timeSeconds, times.first())
        assertEquals((Difficulty.LEVEL_6.timeSeconds * 0.7f).toInt(), times.last())
    }

    @Test
    fun `every fifth round is special`() {
        val special = Road.rounds(Difficulty.LEVEL_2).filter { it.isSpecial }.map { it.index }
        assertEquals(listOf(5, 10, 15, 20, 25, 30, 35, 40), special)
        assertFalse(RoundSpec(Difficulty.LEVEL_2, 1).isSpecial)
    }

    @Test
    fun `special rounds alternate between the two mini-games`() {
        val games = Road.rounds(Difficulty.LEVEL_1).mapNotNull { it.miniGame }
        assertEquals(
            listOf(
                MiniGame.WHO_WAS_HERE, MiniGame.FOLLOW_THE_SONG, MiniGame.WHO_WAS_HERE, MiniGame.FOLLOW_THE_SONG,
                MiniGame.WHO_WAS_HERE, MiniGame.FOLLOW_THE_SONG, MiniGame.WHO_WAS_HERE, MiniGame.FOLLOW_THE_SONG,
            ),
            games,
        )
        assertNull(RoundSpec(Difficulty.LEVEL_1, 7).miniGame)
    }

    @Test
    fun `rounds link to the next one and stop at the end of the road`() {
        assertEquals(RoundSpec(Difficulty.LEVEL_3, 2), RoundSpec(Difficulty.LEVEL_3, 1).next)
        assertNull(RoundSpec(Difficulty.LEVEL_3, Road.ROUNDS_PER_DIFFICULTY).next)
        assertEquals(Road.ROUNDS_PER_DIFFICULTY, Road.rounds(Difficulty.LEVEL_3).size)
    }

    @Test
    fun `indices outside the road are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { RoundSpec(Difficulty.LEVEL_1, 0) }
        assertThrows(IllegalArgumentException::class.java) { RoundSpec(Difficulty.LEVEL_1, Road.ROUNDS_PER_DIFFICULTY + 1) }
    }
}
