package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeekAndFindTest {

    private fun round(difficulty: Difficulty, index: Int) = RoundSpec(GameTheme.ANIMALS, difficulty, index)

    @Test
    fun `targets are distinct pictures that are on the board`() {
        repeat(20) { seed ->
            val game = PeekAndFind.create(round(Difficulty.LEVEL_2, 10), characterCount = 28, random = Random(seed))
            assertEquals(12, game.board.tileCount)
            assertEquals(3, game.targets.size)
            assertEquals(3, game.targets.toSet().size)
            val onBoard = (0 until 12).map(game.board::imageOf).toSet()
            assertTrue(game.targets.all { it in onBoard })
        }
        assertEquals(3, PeekAndFind.create(round(Difficulty.LEVEL_1, 60), characterCount = 28).targets.size) // only three pictures on six cards
        assertEquals(4, PeekAndFind.create(round(Difficulty.LEVEL_3, 60), characterCount = 28).targets.size)
    }

    @Test
    fun `a right tile stays found, a wrong one costs, a found tile is ignored`() {
        val game = PeekAndFind.create(round(Difficulty.LEVEL_2, 10), characterCount = 28, random = Random(2))
        val target = game.currentTarget!!
        val right = (0 until 12).first { game.board.imageOf(it) == target }
        val wrong = (0 until 12).first { game.board.imageOf(it) != target }
        assertEquals(MiniGameRules.Answer.WRONG, game.answer(wrong))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(right))
        assertTrue(game.isFound(right))
        assertFalse(game.isFound(wrong))
        assertEquals(1, game.targetIndex)
        assertEquals(MiniGameRules.Answer.IGNORED, game.answer(right))
        assertEquals(2, game.stars)
    }
}
