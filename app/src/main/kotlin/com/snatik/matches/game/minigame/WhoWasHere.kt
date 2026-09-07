package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Who was here?": a party of characters shows itself, hides, and comes back one short;
 * the player picks who is missing from a few cards. Characters are the theme's image indices.
 */
class WhoWasHere private constructor(val turns: List<Turn>) {

    /** One party. [choices] holds [missing] exactly once, the rest are strangers to the party. */
    class Turn(val party: List<Int>, val missing: Int, val choices: List<Int>) {
        init {
            require(missing in party) { "the missing character must be at the party" }
            require(choices.count { it == missing } == 1) { "the missing character is offered once" }
            require(choices.none { it != missing && it in party }) { "other choices are strangers" }
        }

        /** Where the missing character stood, 0-based from the left. */
        val missingPosition: Int get() = party.indexOf(missing)
    }

    var turnIndex: Int = 0
        private set

    var mistakes: Int = 0
        private set

    val currentTurn: Turn? get() = turns.getOrNull(turnIndex)

    val isOver: Boolean get() = turnIndex >= turns.size

    /** Answers the current turn. True when [choice] is the missing character; that ends the turn. */
    fun answer(choice: Int): Boolean {
        val turn = currentTurn ?: return false
        if (choice != turn.missing) {
            mistakes++
            return false
        }
        turnIndex++
        return true
    }

    /** No mistakes earn three stars, one mistake two, anything more one: nobody leaves empty-handed. */
    val stars: Int get() = (3 - mistakes).coerceAtLeast(1)

    companion object {
        const val TURNS = 3
        const val CHOICES = 3
        const val MIN_PARTY = 3
        const val MAX_PARTY = 6

        /** The party grows by one every ten rounds along the road. */
        fun partySize(round: RoundSpec): Int = (MIN_PARTY + (round.index - 1) / 10).coerceAtMost(MAX_PARTY)

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): WhoWasHere {
            val size = partySize(round)
            require(characterCount >= size + CHOICES - 1) { "not enough characters for a party of $size" }
            val turns = List(TURNS) {
                val order = (0 until characterCount).shuffled(random)
                val party = order.take(size)
                val strangers = order.drop(size).take(CHOICES - 1)
                val missing = party[random.nextInt(size)]
                Turn(party, missing, (strangers + missing).shuffled(random))
            }
            return WhoWasHere(turns)
        }
    }
}
