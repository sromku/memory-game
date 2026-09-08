package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Odd one out": a row of the same character with one that is not, either a different
 * character or the same one in the wrong colours. The player taps the odd one. Answers are
 * positions in the row, left to right.
 */
class OddOneOut private constructor(val turns: List<Turn>) : MiniGameRules {

    enum class Kind {
        /** One character is another character. */
        DIFFERENT,
        /** One character is the same, recoloured. */
        RECOLOURED,
    }

    /** The row is [size] copies of [character], except [odd], which is [other] (or recoloured). */
    class Turn(val character: Int, val other: Int, val size: Int, val odd: Int, val kind: Kind) {
        init {
            require(odd in 0 until size) { "the odd one stands in the row" }
            require(kind == Kind.RECOLOURED || other != character) { "a different character must differ" }
        }
    }

    var turnIndex: Int = 0
        private set

    var mistakes: Int = 0
        private set

    val currentTurn: Turn? get() = turns.getOrNull(turnIndex)

    override val isOver: Boolean get() = turnIndex >= turns.size

    override val stars: Int get() = (3 - mistakes).coerceAtLeast(1)

    override fun answer(choice: Int): MiniGameRules.Answer {
        val turn = currentTurn ?: return MiniGameRules.Answer.IGNORED
        if (choice != turn.odd) {
            mistakes++
            return MiniGameRules.Answer.WRONG
        }
        turnIndex++
        return MiniGameRules.Answer.RIGHT
    }

    companion object {
        const val TURNS = 3

        /** Four in a row, five from round 50. */
        fun rowSize(round: RoundSpec): Int = if (round.index < 50) 4 else 5

        /** Different characters at first; from round 30 a recoloured twin half of the time. */
        fun kind(round: RoundSpec, random: Random): Kind =
            if (round.index >= 30 && random.nextBoolean()) Kind.RECOLOURED else Kind.DIFFERENT

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): OddOneOut {
            require(characterCount >= 2) { "two characters are needed" }
            val size = rowSize(round)
            val turns = List(TURNS) {
                val order = (0 until characterCount).shuffled(random)
                val kind = kind(round, random)
                Turn(order[0], if (kind == Kind.DIFFERENT) order[1] else order[0], size, random.nextInt(size), kind)
            }
            return OddOneOut(turns)
        }
    }
}
