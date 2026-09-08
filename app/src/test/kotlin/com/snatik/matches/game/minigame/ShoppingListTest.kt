package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListTest {

    private fun round(index: Int) = RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_1, index)

    @Test
    fun `the row and the list grow along the road`() {
        assertEquals(5, ShoppingList.partySize(round(10)))
        assertEquals(6, ShoppingList.partySize(round(60)))
        assertEquals(2, ShoppingList.listLength(round(10)))
        assertEquals(3, ShoppingList.listLength(round(30)))
        assertEquals(3, ShoppingList.listLength(round(70)))
    }

    @Test
    fun `lists name distinct members of the row`() {
        repeat(30) { seed ->
            for (turn in ShoppingList.create(round(70), characterCount = 12, random = Random(seed)).turns) {
                assertEquals(6, turn.party.size)
                assertEquals(3, turn.list.size)
                assertEquals(3, turn.list.toSet().size)
                assertTrue(turn.list.all { it in 0 until 6 })
            }
        }
    }

    @Test
    fun `taps must follow the list, and a wrong one starts the list over`() {
        val game = ShoppingList.create(round(30), characterCount = 12, random = Random(9))
        val list = game.turns[0].list
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(list[0]))
        assertEquals(1, game.position)
        assertEquals(MiniGameRules.Answer.WRONG, game.answer(list[0]))
        assertEquals(0, game.position)
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(list[0]))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(list[1]))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(list[2]))
        assertEquals(1, game.turnIndex)
        assertTrue(game.turnJustDone)
        assertEquals(2, game.stars)
    }
}
