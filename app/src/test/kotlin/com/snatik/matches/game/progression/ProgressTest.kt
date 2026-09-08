package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {

    private val T = GameTheme.ANIMALS

    private val one = Difficulty.LEVEL_1
    private val two = Difficulty.LEVEL_2

    private fun played(difficulty: Difficulty, rounds: Int, stars: Int = 2, from: Progress = Progress.EMPTY): Progress =
        (1..rounds).fold(from) { p, i -> p.record(RoundSpec(T, difficulty, i), RoundResult(stars, 30)) }

    @Test
    fun `recording keeps the better result`() {
        val round = RoundSpec(T, one, 1)
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
        val after = before.record(RoundSpec(T, one, 1), RoundResult(3, 20))
        assertEquals(Progress.EMPTY, before)
        assertNotEquals(before, after)
        assertEquals(after, Progress.of(after.entries))
        assertNull(before.resultOf(RoundSpec(T, one, 1)))
    }

    @Test
    fun `the next round is the first without a result`() {
        assertEquals(RoundSpec(T, one, 1), Progress.EMPTY.nextRound(T, one))
        val p = played(one, 3).record(RoundSpec(T, one, 7), RoundResult(1, 40))
        assertEquals(RoundSpec(T, one, 4), p.nextRound(T, one))
        assertNull(played(one, Road.ROUNDS_PER_DIFFICULTY).nextRound(T, one))
    }

    @Test
    fun `the first difficulty is open and the next opens after ten rounds`() {
        assertTrue(Progress.EMPTY.isUnlocked(T, one))
        assertFalse(Progress.EMPTY.isUnlocked(T, two))
        assertFalse(played(one, Road.ROUNDS_TO_UNLOCK_NEXT - 1).isUnlocked(T, two))
        assertTrue(played(one, Road.ROUNDS_TO_UNLOCK_NEXT).isUnlocked(T, two))
        assertFalse("each difficulty opens only from the one before it", played(one, Road.ROUNDS_PER_DIFFICULTY).isUnlocked(T, Difficulty.LEVEL_3))
        assertTrue("a road with a result on it is open", played(Difficulty.LEVEL_5, 1).isUnlocked(T, Difficulty.LEVEL_5))
    }

    @Test
    fun `stars and counts are per difficulty and in total`() {
        val p = played(one, 4, stars = 3).record(RoundSpec(T, two, 1), RoundResult(1, 60))
        assertEquals(4, p.completedCount(T, one))
        assertEquals(12, p.starsOn(T, one))
        assertEquals(1, p.starsOn(T, two))
        assertEquals(13, p.totalStars)
        assertEquals(listOf(1, 2, 3, 4), p.completedRounds(T, one).map { it.index })
        assertTrue(played(one, Road.ROUNDS_PER_DIFFICULTY).isCompleted(RoundSpec(T, one, Road.ROUNDS_PER_DIFFICULTY)))
        assertFalse(played(one, Road.ROUNDS_PER_DIFFICULTY - 1).isCompleted(RoundSpec(T, one, Road.ROUNDS_PER_DIFFICULTY)))
    }

    @Test
    fun `average stars summarise a road`() {
        assertEquals(0, Progress.EMPTY.averageStars(T, one))
        assertEquals(3, played(one, 4, stars = 3).averageStars(T, one))
        val mixed = played(one, 2, stars = 3).record(RoundSpec(T, one, 3), RoundResult(1, 30)) // 7 over 3 rounds
        assertEquals(2, mixed.averageStars(T, one))
        val low = played(one, 3, stars = 1).record(RoundSpec(T, one, 4), RoundResult(2, 30)) // 5 over 4 rounds
        assertEquals(1, low.averageStars(T, one))
    }

    @Test
    fun `every theme has its own roads`() {
        val animals = played(one, Road.ROUNDS_TO_UNLOCK_NEXT)
        assertTrue(animals.isUnlocked(GameTheme.ANIMALS, two))
        assertFalse("progress in one theme opens nothing in another", animals.isUnlocked(GameTheme.MONSTERS, two))
        assertEquals(RoundSpec(GameTheme.MONSTERS, one, 1), animals.nextRound(GameTheme.MONSTERS, one))
        assertEquals(0, animals.completedCount(GameTheme.MONSTERS, one))
        assertEquals(2, animals.themeStars(GameTheme.ANIMALS))
        assertEquals(0, animals.themeStars(GameTheme.MONSTERS))
    }

    @Test
    fun `a friend joins the album for every ten rounds of a theme`() {
        assertTrue(Progress.EMPTY.friendsOf(T).isEmpty())
        val nine = played(one, 9)
        assertTrue(nine.friendsOf(T).isEmpty())
        val ten = played(one, 10)
        assertEquals(1, ten.friendsOf(T).size)
        val twentyFive = played(two, 15, from = ten)
        assertEquals(2, twentyFive.friendsOf(T).size)
        assertEquals("friends keep their order", ten.friendsOf(T), twentyFive.friendsOf(T).take(1))
        assertTrue(twentyFive.friendsOf(GameTheme.MONSTERS).isEmpty())
        val order = Friends.order(T)
        assertEquals(T.characters.size, order.toSet().size)
        assertEquals(order, Friends.order(T))
    }

    @Test
    fun `quick play picks the next round of the highest open difficulty with rounds left`() {
        assertEquals(RoundSpec(T, one, 1), Progress.EMPTY.quickPlayRound(T))
        val opened = played(one, Road.ROUNDS_TO_UNLOCK_NEXT)
        assertEquals(RoundSpec(T, two, 1), opened.quickPlayRound(T))
        val twoDone = played(two, Road.ROUNDS_PER_DIFFICULTY, from = opened)
        assertEquals(RoundSpec(T, Difficulty.LEVEL_3, 1), twoDone.quickPlayRound(T))
        val everything = Difficulty.entries.fold(Progress.EMPTY) { p, d -> played(d, Road.ROUNDS_PER_DIFFICULTY, stars = 3, from = p) }
        assertNull(everything.quickPlayRound(T))
    }
}
