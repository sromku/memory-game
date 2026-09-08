package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RoundSpecTest {

    private val T = GameTheme.ANIMALS

    @Test
    fun `time tightens from the full time at round 1 to 70 percent at the last round`() {
        assertEquals(Difficulty.LEVEL_1.timeSeconds, RoundSpec(T, Difficulty.LEVEL_1, 1).timeSeconds)
        assertEquals((Difficulty.LEVEL_1.timeSeconds * 0.7f).toInt(), RoundSpec(T, Difficulty.LEVEL_1, Road.ROUNDS_PER_DIFFICULTY).timeSeconds)
        val times = Road.rounds(T, Difficulty.LEVEL_6).map { it.timeSeconds }
        assertEquals("never gets looser along the road", times.sortedDescending(), times)
        assertEquals(Difficulty.LEVEL_6.timeSeconds, times.first())
        assertEquals((Difficulty.LEVEL_6.timeSeconds * 0.7f).toInt(), times.last())
    }

    @Test
    fun `every fifth round is special`() {
        val special = Road.rounds(T, Difficulty.LEVEL_2).filter { it.isSpecial }.map { it.index }
        assertEquals((5..Road.ROUNDS_PER_DIFFICULTY step 5).toList(), special)
        assertFalse(RoundSpec(T, Difficulty.LEVEL_2, 1).isSpecial)
    }

    @Test
    fun `special rounds take turns through the mini-games`() {
        val games = Road.rounds(T, Difficulty.LEVEL_1).mapNotNull { it.miniGame }
        assertEquals(Road.ROUNDS_PER_DIFFICULTY / Road.SPECIAL_EVERY, games.size)
        assertEquals(MiniGame.entries.toList(), games.take(MiniGame.entries.size))
        assertEquals(games.take(MiniGame.entries.size), games.drop(MiniGame.entries.size).take(MiniGame.entries.size))
        assertNull(RoundSpec(T, Difficulty.LEVEL_1, 7).miniGame)
    }

    @Test
    fun `rounds link to the next one and stop at the end of the road`() {
        assertEquals(RoundSpec(T, Difficulty.LEVEL_3, 2), RoundSpec(T, Difficulty.LEVEL_3, 1).next)
        assertNull(RoundSpec(T, Difficulty.LEVEL_3, Road.ROUNDS_PER_DIFFICULTY).next)
        assertEquals(Road.ROUNDS_PER_DIFFICULTY, Road.rounds(T, Difficulty.LEVEL_3).size)
    }

    @Test
    fun `indices outside the road are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { RoundSpec(T, Difficulty.LEVEL_1, 0) }
        assertThrows(IllegalArgumentException::class.java) { RoundSpec(T, Difficulty.LEVEL_1, Road.ROUNDS_PER_DIFFICULTY + 1) }
    }
}
