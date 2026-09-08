package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Shopping list": a party stands in a row; a short list of them is shown, then hidden;
 * the player taps them in the row in the same order. A wrong tap starts the list over. Answers
 * are positions in the row.
 */
class ShoppingList private constructor(val turns: List<Turn>) : MiniGameRules {

    /** [list] holds positions in [party], in the order to tap them, each at most once. */
    class Turn(val party: List<Int>, val list: List<Int>) {
        init {
            require(list.isNotEmpty() && list.all { it in party.indices }) { "the list names members of the party" }
            require(list.toSet().size == list.size) { "each member is on the list once" }
        }
    }

    var turnIndex: Int = 0
        private set

    /** How many of the current list have been tapped in order. */
    var position: Int = 0
        private set

    var mistakes: Int = 0
        private set

    val currentTurn: Turn? get() = turns.getOrNull(turnIndex)

    override val isOver: Boolean get() = turnIndex >= turns.size

    override val stars: Int
        get() = when {
            mistakes == 0 -> 3
            mistakes <= 2 -> 2
            else -> 1
        }

    /** True when the turn's list has just been completed by the last answer. */
    val turnJustDone: Boolean get() = position == 0 && turnIndex > 0

    override fun answer(choice: Int): MiniGameRules.Answer {
        val turn = currentTurn ?: return MiniGameRules.Answer.IGNORED
        if (choice != turn.list[position]) {
            mistakes++
            position = 0
            return MiniGameRules.Answer.WRONG
        }
        position++
        if (position == turn.list.size) {
            position = 0
            turnIndex++
        }
        return MiniGameRules.Answer.RIGHT
    }

    companion object {
        const val TURNS = 3

        /** Five in the row, six from round 50. */
        fun partySize(round: RoundSpec): Int = if (round.index < 50) 5 else 6

        /** Two on the list, three from round 30 (the screen shows up to three cards). */
        fun listLength(round: RoundSpec): Int = if (round.index < 30) 2 else 3

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): ShoppingList {
            val size = partySize(round)
            require(characterCount >= size) { "not enough characters for the row" }
            val turns = List(TURNS) {
                val party = (0 until characterCount).shuffled(random).take(size)
                Turn(party, party.indices.shuffled(random).take(listLength(round)))
            }
            return ShoppingList(turns)
        }
    }
}
