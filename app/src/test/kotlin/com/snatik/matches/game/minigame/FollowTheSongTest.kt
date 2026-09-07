package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.minigame.FollowTheSong.Outcome
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowTheSongTest {

    private fun round(index: Int) = RoundSpec(Difficulty.LEVEL_3, index)

    @Test
    fun `the song and the party grow along the road`() {
        assertEquals(3, FollowTheSong.songLength(round(10)))
        assertEquals(4, FollowTheSong.songLength(round(20)))
        assertEquals(6, FollowTheSong.songLength(round(40)))
        assertEquals(3, FollowTheSong.partySize(round(10)))
        assertEquals(3, FollowTheSong.partySize(round(20)))
        assertEquals(4, FollowTheSong.partySize(round(30)))
        assertEquals(4, FollowTheSong.partySize(round(40)))
    }

    @Test
    fun `a song only names singers of the party`() {
        repeat(30) { seed ->
            val game = FollowTheSong.create(round(40), Random(seed))
            assertEquals(6, game.song.size)
            assertTrue(game.song.all { it in 0 until game.partySize })
        }
    }

    @Test
    fun `each turn asks for one more note and the last one ends the game`() {
        val game = FollowTheSong.create(round(10), Random(3))
        val song = game.song
        assertEquals(song.take(1), game.currentSequence)
        assertEquals(Outcome.TURN_COMPLETE, game.tap(song[0]))
        assertEquals(song.take(2), game.currentSequence)
        assertEquals(Outcome.CONTINUE, game.tap(song[0]))
        assertEquals(Outcome.TURN_COMPLETE, game.tap(song[1]))
        assertEquals(Outcome.CONTINUE, game.tap(song[0]))
        assertEquals(Outcome.CONTINUE, game.tap(song[1]))
        assertFalse(game.isOver)
        assertEquals(Outcome.SONG_COMPLETE, game.tap(song[2]))
        assertTrue(game.isOver)
        assertEquals(Outcome.IGNORED, game.tap(song[0]))
        assertEquals(0, game.mistakes)
        assertEquals(3, game.stars)
    }

    @Test
    fun `a wrong note restarts the turn and costs stars gently`() {
        val game = FollowTheSong.create(round(10), Random(5))
        val song = game.song
        game.tap(song[0])
        game.tap(song[0])
        val wrong = (0 until game.partySize).first { it != song[1] }
        assertEquals(Outcome.MISTAKE, game.tap(wrong))
        assertEquals("the turn starts over from the first note", 0, game.position)
        assertEquals(2, game.length)
        assertEquals(2, game.stars)
        game.tap(wrong)
        assertEquals(2, game.stars)
        game.tap(wrong)
        assertEquals(1, game.stars)
    }
}
