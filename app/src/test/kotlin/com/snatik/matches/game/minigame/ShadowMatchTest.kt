package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowMatchTest {

    private fun round(index: Int) = RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_1, index)

    @Test
    fun `every turn offers the target among three different cards`() {
        repeat(40) { seed ->
            val game = ShadowMatch.create(round(20), characterCount = 12, random = Random(seed))
            assertEquals(3, game.turns.size)
            for (turn in game.turns) {
                assertEquals(3, turn.choices.size)
                assertEquals(3, turn.choices.toSet().size)
                assertTrue(turn.target in turn.choices)
            }
        }
        assertEquals(4, ShadowMatch.create(round(60), characterCount = 12).turns.size)
    }

    @Test
    fun `answers move on or cost stars`() {
        val game = ShadowMatch.create(round(20), characterCount = 12, random = Random(7))
        val turn = game.turns[0]
        assertEquals(MiniGameRules.Answer.WRONG, game.answer(turn.choices.first { it != turn.target }))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(turn.target))
        assertEquals(1, game.turnIndex)
        assertEquals(2, game.stars)
    }
}
