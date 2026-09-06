package com.snatik.matches.game

import com.snatik.matches.game.GameEngine.Flip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val board = Board.create(6, listOf(1, 2, 3), Random(seed = 3))
    private val engine = GameEngine(board)

    @Test
    fun `first flip just turns the tile up`() {
        assertEquals(Flip.First(0), engine.flip(0))
        assertEquals(0, engine.faceUpTile)
        assertEquals(Flip.Ignored, engine.flip(0))
    }

    @Test
    fun `mismatch flips both back after resolve`() {
        val other = (1 until 6).first { !board.isPair(0, it) }
        engine.flip(0)
        assertEquals(Flip.Mismatch(0, other), engine.flip(other))
        assertNull(engine.faceUpTile)
        assertEquals("locked until resolved", Flip.Ignored, engine.flip(1))
        engine.resolve()
        assertEquals(Flip.First(1), engine.flip(1))
        assertFalse(engine.isMatched(0))
        assertFalse(engine.isMatched(other))
    }

    @Test
    fun `match marks both tiles and completes when the last pair is found`() {
        var matches = 0
        val remaining = (0 until 6).toMutableSet()
        while (remaining.isNotEmpty()) {
            val tile = remaining.first()
            val partner = board.partnerOf(tile)
            assertEquals(Flip.First(tile), engine.flip(tile))
            val flip = engine.flip(partner) as Flip.Match
            matches++
            assertEquals(tile, flip.first)
            assertEquals(partner, flip.second)
            assertEquals(matches == 3, flip.complete)
            assertTrue(engine.isMatched(tile) && engine.isMatched(partner))
            assertEquals(matches, engine.matchedPairCount)
            engine.resolve()
            remaining -= setOf(tile, partner)
        }
        assertTrue(engine.isComplete)
        assertEquals(Flip.Ignored, engine.flip(0))
    }

    @Test
    fun `matched tiles cannot be flipped again`() {
        engine.flip(0)
        engine.flip(board.partnerOf(0))
        engine.resolve()
        assertEquals(Flip.Ignored, engine.flip(0))
        assertEquals(Flip.Ignored, engine.flip(board.partnerOf(0)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `flipping a tile outside the board fails loudly`() {
        engine.flip(6)
    }
}
