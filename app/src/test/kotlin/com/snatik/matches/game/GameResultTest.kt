package com.snatik.matches.game

import com.snatik.matches.game.progression.Road
import com.snatik.matches.game.progression.RoundSpec
import org.junit.Assert.assertEquals
import org.junit.Test

class GameResultTest {

    private val T = GameTheme.ANIMALS

    private val level1 = RoundSpec(T, Difficulty.LEVEL_1, 1) // 60 seconds

    @Test
    fun `three stars within half the time`() {
        assertEquals(3, GameResult.compute(level1, 1, passedSeconds = 30).stars)
        assertEquals(3, GameResult.compute(level1, 1, passedSeconds = 0).stars)
    }

    @Test
    fun `two stars within four fifths of the time`() {
        assertEquals(2, GameResult.compute(level1, 1, passedSeconds = 31).stars)
        assertEquals(2, GameResult.compute(level1, 1, passedSeconds = 48).stars)
    }

    @Test
    fun `one star before the clock runs out`() {
        assertEquals(1, GameResult.compute(level1, 1, passedSeconds = 49).stars)
        assertEquals(1, GameResult.compute(level1, 1, passedSeconds = 59).stars)
    }

    @Test
    fun `no stars once the time is up`() {
        assertEquals(0, GameResult.compute(level1, 1, passedSeconds = 60).stars)
        assertEquals(0, GameResult.compute(level1, 1, passedSeconds = 500).stars)
    }

    @Test
    fun `score is level times remaining seconds times theme multiplier`() {
        val result = GameResult.compute(RoundSpec(T, Difficulty.LEVEL_4, 1), themeMultiplier = 3, passedSeconds = 50)
        assertEquals(100, result.remainingSeconds)
        assertEquals(4 * 100 * 3, result.score)
        assertEquals(50, result.passedSeconds)
    }

    @Test
    fun `overtime never produces negative time or score`() {
        val result = GameResult.compute(level1, themeMultiplier = 2, passedSeconds = 90)
        assertEquals(0, result.remainingSeconds)
        assertEquals(0, result.score)
    }

    @Test
    fun `later rounds judge against their tighter time`() {
        val last = RoundSpec(T, Difficulty.LEVEL_1, Road.ROUNDS_PER_DIFFICULTY) // 42 seconds
        assertEquals(3, GameResult.compute(last, 1, passedSeconds = 21).stars)
        assertEquals(2, GameResult.compute(last, 1, passedSeconds = 22).stars)
        assertEquals(0, GameResult.compute(last, 1, passedSeconds = 42).stars)
        assertEquals(21, GameResult.compute(last, 1, passedSeconds = 21).remainingSeconds)
    }
}
