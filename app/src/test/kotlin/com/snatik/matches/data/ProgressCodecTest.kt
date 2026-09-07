package com.snatik.matches.data

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCodecTest {

    private val sample = Progress.EMPTY
        .record(RoundSpec(Difficulty.LEVEL_2, 3), RoundResult(3, 41))
        .record(RoundSpec(Difficulty.LEVEL_1, 1), RoundResult(2, 55))
        .record(RoundSpec(Difficulty.LEVEL_1, 2), RoundResult(0, 70))

    @Test
    fun `encodes a header and one sorted line per round`() {
        val expected = listOf("memory-game-progress 1", "1 1 2 55", "1 2 0 70", "2 3 3 41", "").joinToString("\n")
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
            "memory-game-progress 1",
            "1 1 3 10",
            "garbage line",
            "9 1 3 10", // no such difficulty
            "1 99 3 10", // no such round
            "2 2 5 10", // too many stars
            "2 3 1 -4", // negative time
            "2 4 2 12",
        )
        val partly = ProgressCodec.decode(lines.joinToString("\n"))
        assertEquals(RoundResult(3, 10), partly.resultOf(RoundSpec(Difficulty.LEVEL_1, 1)))
        assertEquals(RoundResult(2, 12), partly.resultOf(RoundSpec(Difficulty.LEVEL_2, 4)))
        assertEquals(2, partly.entries.size)
    }
}
