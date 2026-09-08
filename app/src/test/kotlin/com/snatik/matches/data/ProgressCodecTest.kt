package com.snatik.matches.data

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCodecTest {

    private val T = GameTheme.ANIMALS

    private val sample = Progress.EMPTY
        .record(RoundSpec(T, Difficulty.LEVEL_2, 3), RoundResult(3, 41))
        .record(RoundSpec(T, Difficulty.LEVEL_1, 1), RoundResult(2, 55))
        .record(RoundSpec(T, Difficulty.LEVEL_1, 2), RoundResult(0, 70))

    @Test
    fun `encodes a header and one sorted line per round`() {
        val expected = listOf("memory-game-progress 2", "1 1 1 2 55", "1 1 2 0 70", "1 2 3 3 41", "").joinToString("\n")
        assertEquals(expected, ProgressCodec.encode(sample))
    }

    @Test
    fun `round trips`() {
        assertEquals(sample, ProgressCodec.decode(ProgressCodec.encode(sample)))
        assertEquals(Progress.EMPTY, ProgressCodec.decode(ProgressCodec.encode(Progress.EMPTY)))
    }

    @Test
    fun `damaged or foreign content degrades instead of failing`() {
        assertEquals(Progress.EMPTY, ProgressCodec.decode(""))
        assertEquals(Progress.EMPTY, ProgressCodec.decode("something else entirely\n1 1 3 10"))
        val lines = listOf(
            "memory-game-progress 2",
            "1 1 1 3 10",
            "garbage line",
            "1 9 1 3 10", // no such difficulty
            "1 1 999 3 10", // no such round
            "1 2 2 5 10", // too many stars
            "1 2 3 1 -4", // negative time
            "7 1 1 3 10", // no such theme
            "2 3 5 3 9", // a monsters round
            "1 2 4 2 12",
        )
        val partly = ProgressCodec.decode(lines.joinToString("\n"))
        assertEquals(RoundResult(3, 10), partly.resultOf(RoundSpec(T, Difficulty.LEVEL_1, 1)))
        assertEquals(RoundResult(2, 12), partly.resultOf(RoundSpec(T, Difficulty.LEVEL_2, 4)))
        assertEquals(RoundResult(3, 9), partly.resultOf(RoundSpec(GameTheme.MONSTERS, Difficulty.LEVEL_3, 5)))
        assertEquals(3, partly.entries.size)
    }

    @Test
    fun `version 1 files, which had no theme, read as the animals theme`() {
        val old = ProgressCodec.decode("memory-game-progress 1\n1 1 3 12\n2 1 3 30\n")
        assertEquals(RoundResult(3, 12), old.resultOf(RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_1, 1)))
        assertEquals(RoundResult(3, 30), old.resultOf(RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_2, 1)))
        assertEquals(2, old.entries.size)
    }
}
