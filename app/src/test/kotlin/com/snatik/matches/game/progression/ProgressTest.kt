package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {

    private val one = Difficulty.LEVEL_1
    private val two = Difficulty.LEVEL_2

    private fun played(difficulty: Difficulty, rounds: Int, stars: Int = 2, from: Progress = Progress.EMPTY): Progress =
        (1..rounds).fold(from) { p, i -> p.record(RoundSpec(difficulty, i), RoundResult(stars, 30)) }

    @Test
    fun `recording keeps the better result`() {
        val round = RoundSpec(one, 1)
        var p = Progress.EMPTY.record(round, RoundResult(1, 50))
        p = p.record(round, RoundResult(3, 40))
        assertEquals(RoundResult(3, 40), p.resultOf(round))
        p = p.record(round, RoundResult(2, 10))
        assertEquals("fewer stars never replace more", RoundResult(3, 40), p.resultOf(round))
        p = p.record(round, RoundResult(3, 25))
        assertEquals("equal stars keep the faster time", RoundResult(3, 25), p.resultOf(round))
    }

    @Test
    fun `progress values are immutable and comparable`() {
        val before = Progress.EMPTY
        val after = before.record(RoundSpec(one, 1), RoundResult(3, 20))
        assertEquals(Progress.EMPTY, before)
        assertNotEquals(before, after)
        assertEquals(after, Progress.of(after.entries))
        assertNull(before.resultOf(RoundSpec(one, 1)))
    }

    @Test
    fun `the next round is the first without a result`() {
        assertEquals(RoundSpec(one, 1), Progress.EMPTY.nextRound(one))
        val p = played(one, 3).record(RoundSpec(one, 7), RoundResult(1, 40))
        assertEquals(RoundSpec(one, 4), p.nextRound(one))
        assertNull(played(one, Road.ROUNDS_PER_DIFFICULTY).nextRound(one))
    }

    @Test
    fun `the first difficulty is open and the next opens after ten rounds`() {
        assertTrue(Progress.EMPTY.isUnlocked(one))
        assertFalse(Progress.EMPTY.isUnlocked(two))
        assertFalse(played(one, Road.ROUNDS_TO_UNLOCK_NEXT - 1).isUnlocked(two))
        assertTrue(played(one, Road.ROUNDS_TO_UNLOCK_NEXT).isUnlocked(two))
        assertFalse("each difficulty opens only from the one before it", played(one, 40).isUnlocked(Difficulty.LEVEL_3))
    }

    @Test
    fun `stars and counts are per difficulty and in total`() {
        val p = played(one, 4, stars = 3).record(RoundSpec(two, 1), RoundResult(1, 60))
        assertEquals(4, p.completedCount(one))
        assertEquals(12, p.starsOn(one))
        assertEquals(1, p.starsOn(two))
        assertEquals(13, p.totalStars)
        assertEquals(listOf(1, 2, 3, 4), p.completedRounds(one).map { it.index })
        assertTrue(played(one, 40).isCompleted(RoundSpec(one, 40)))
        assertFalse(played(one, 39).isCompleted(RoundSpec(one, 40)))
    }

    @Test
    fun `quick play picks the next round of the highest open difficulty with rounds left`() {
        assertEquals(RoundSpec(one, 1), Progress.EMPTY.quickPlayRound())
        val opened = played(one, Road.ROUNDS_TO_UNLOCK_NEXT)
        assertEquals(RoundSpec(two, 1), opened.quickPlayRound())
        val twoDone = played(two, 40, from = opened)
        assertEquals(RoundSpec(Difficulty.LEVEL_3, 1), twoDone.quickPlayRound())
        val everything = Difficulty.entries.fold(Progress.EMPTY) { p, d -> played(d, 40, stars = 3, from = p) }
        assertNull(everything.quickPlayRound())
    }
}
