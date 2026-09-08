package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Follow the song": a party sings a song one note per character, and the player
 * repeats it by tapping the singers in order. Every turn asks for one more note of the song; a
 * wrong tap restarts the turn. Positions are 0-based places in the party, left to right.
 */
class FollowTheSong private constructor(val partySize: Int, val song: List<Int>) : MiniGameRules {

    enum class Outcome {
        /** Right so far; more notes to go. */
        CONTINUE,
        /** The turn's notes were all right; the song grows. */
        TURN_COMPLETE,
        /** The whole song was repeated; the game is over. */
        SONG_COMPLETE,
        /** A wrong note; the turn starts over. */
        MISTAKE,
        /** Nothing to answer. */
        IGNORED,
    }

    /** How many notes of the song this turn asks for. */
    var length: Int = 1
        private set

    /** The next note expected, as an index into the song. */
    var position: Int = 0
        private set

    var mistakes: Int = 0
        private set

    /** The notes to sing before this turn. */
    val currentSequence: List<Int> get() = song.take(length)

    override val isOver: Boolean get() = length > song.size

    /** No mistakes earn three stars, up to two earn two, more earn one. */
    override val stars: Int
        get() = when {
            mistakes == 0 -> 3
            mistakes <= 2 -> 2
            else -> 1
        }

    fun tap(singer: Int): Outcome {
        if (isOver) return Outcome.IGNORED
        if (singer != song[position]) {
            mistakes++
            position = 0
            return Outcome.MISTAKE
        }
        position++
        if (position < length) return Outcome.CONTINUE
        position = 0
        length++
        return if (isOver) Outcome.SONG_COMPLETE else Outcome.TURN_COMPLETE
    }

    companion object {
        const val MIN_SONG = 3
        const val MAX_SONG = 7
        const val MIN_PARTY = 3
        const val MAX_PARTY = 4

        /** Three notes on round 10, one more every ten rounds, up to [MAX_SONG]. */
        fun songLength(round: RoundSpec): Int = (MIN_SONG + (round.index - 1) / 10).coerceAtMost(MAX_SONG)

        /** Three singers until round 30, four from then on. */
        fun partySize(round: RoundSpec): Int = (MIN_PARTY + (round.index - 1) / 20).coerceAtMost(MAX_PARTY)

        fun create(round: RoundSpec, random: Random = Random.Default): FollowTheSong {
            val size = partySize(round)
            val song = List(songLength(round)) { random.nextInt(size) }
            return FollowTheSong(size, song)
        }
    }
}
